package ar.edu.itba.pod.grpc.server.services;

import airport.Models;
import airport.NotifyServiceGrpc;
import airport.NotifyServiceOuterClass;
import ar.edu.itba.pod.grpc.server.exeptions.AirlineAlreadyRegisteredException;
import ar.edu.itba.pod.grpc.server.exeptions.AirlineNotRegisteredForNotificationsException;
import ar.edu.itba.pod.grpc.server.models.*;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class NotifyService extends NotifyServiceGrpc.NotifyServiceImplBase {
    private final static Logger logger = LoggerFactory.getLogger(NotifyService.class);
    private final static Map<String, StreamObserver<NotifyServiceOuterClass.Notification>> streamObserverMap = new HashMap<>();
    private final static String lock = "lock";
    private final AirportRepository repository;
    public NotifyService() {
        repository = AirportRepository.getInstance();
    }

    @Override
    public void notifyAirline(NotifyServiceOuterClass.NotifyRequest request, StreamObserver<NotifyServiceOuterClass.Notification> responseObserver) {
        synchronized (lock) {
            if (streamObserverMap.get(request.getAirlineName()) != null)
                throw new AirlineAlreadyRegisteredException(request.getAirlineName());
            repository.registerForNotifications(request.getAirlineName());
            streamObserverMap.put(request.getAirlineName(), responseObserver);
        }
        notifyAirline(request.getAirlineName(), request.getAirlineName() + " registered successfully for check-in events.");
    }

    private void notifyAirline(String airline, String message){
        synchronized (lock) {
            StreamObserver<NotifyServiceOuterClass.Notification> streamObserver = streamObserverMap.get(airline);
            if (streamObserver != null)
                try {
                    streamObserver.onNext(NotifyServiceOuterClass.Notification.newBuilder().setMessage(message).build());
                } catch (StatusRuntimeException e) {
                    streamObserverMap.put(airline, null);
                }
        }
    }

    private static String getFlightCodes(List<?> flightList) {
        StringBuilder flightString = new StringBuilder();
        AtomicInteger counter = new AtomicInteger();
        int flightsAmount = flightList.size();
        flightList.forEach(flight -> {
            flightString.append(flight.toString());
            counter.getAndIncrement();
            if (counter.get() < flightsAmount)
                flightString.append("|");
        });
        return flightString.toString();
    }
    private static String getFlightCodes(ConcurrentLinkedQueue<?> flightList) {
        StringBuilder flightString = new StringBuilder();
        AtomicInteger counter = new AtomicInteger();
        int flightsAmount = flightList.size();
        flightList.forEach(flight -> {
            flightString.append(flight.toString());
            counter.getAndIncrement();
            if (counter.get() < flightsAmount)
                flightString.append("|");
        });
        return flightString.toString();
    }

    public void notifyAssignedRange(String airlineName, int counterCount, int lastVal, List<String> flightCodes, String sectorName) {
        String notificationMessage = String.format("%d counters (%d-%d) in Sector %s are now checking in passengers from %s %s flights.",
                counterCount,
                lastVal - counterCount + 1,
                lastVal,
                sectorName,
                airlineName,
                getFlightCodes(flightCodes)
        );
        notifyAirline(airlineName, notificationMessage);
    }
    public void notifyBookingInQueue(
            String airlineName,
            String bookingCode,
            String flightCode,
            int peopleInLine,
            int firstCounter,
            int lastCounter,
            String sectorName
    ) {
        String notification = String.format(
                "Booking %s for flight %s from %s is now waiting to check-in on counters (%d-%d) in Sector %s with %d people in line",
                bookingCode,
                flightCode,
                airlineName,
                firstCounter,
                lastCounter,
                sectorName, peopleInLine
        );
        notifyAirline(airlineName, notification);
    }

    public void notifyPassengerCheckIn(Booking.BookingRecord booking, int counterCode, String sectorName) {
        String notification = String.format("Check-in successful of %s for flight %s at counter %d in Sector %s",
                booking.bookingCode(), booking.flightCode(), counterCode,sectorName);
        notifyAirline(booking.airlineName(), notification);
    }

    public void notifyPendingAssignments(List<PendingAssignment.PendingAssignmentRecord> pendingAssignmentList, String sectorName) {
        int i = 1;
        for (PendingAssignment.PendingAssignmentRecord pendingAssignment : pendingAssignmentList) {
            if (!pendingAssignment.isPending()) {
                notifyAssignedRange(
                        pendingAssignment.airlineName(),
                        pendingAssignment.counterCount(),
                        pendingAssignment.firstCounter(),
                        pendingAssignment.flightCodes(),
                        sectorName
                );
            } else {
                notifyPendingChange(
                        pendingAssignment.airlineName(),
                        sectorName,
                        pendingAssignment.counterCount(),
                        pendingAssignment.flightCodes(),
                        i++
                );
            }
        }
    }
    public void notifyPendingAssignment(String airlineName, int countVal, List<String> flightCodes, String sectorName, int pendingsAhead) {
        String notification = String.format("%d counters in Sector %s for flights %s is pending with %d other pendings ahead",
                countVal, sectorName, getFlightCodes(flightCodes), pendingsAhead);
        notifyAirline(airlineName, notification);
    }

    public void notifyFreeCounterRange(String airlineName, ConcurrentLinkedQueue<String> flightStrings, int fromVal, AtomicInteger freedAmount, String sectorName) {
        String notification = String.format(
                "Ended check-in for flights %s on counters (%d-%d) from Sector %s",
                getFlightCodes(flightStrings),
                fromVal,
                fromVal - 1 + freedAmount.get(),
                sectorName
        );
        notifyAirline(airlineName, notification);
    }

    public void notifyPendingChange(String airline, String sectorName, int countVal, List<String> flightConcurrentLinkedQueue, int pos) {
        String notification = String.format("%d counters in Sector %s for flights %s is pending with %d other pendings ahead",
                countVal, sectorName, getFlightCodes(flightConcurrentLinkedQueue), pos);
        notifyAirline(airline, notification);
    }

    @Override
    public void notifyRemoveAirline(NotifyServiceOuterClass.NotifyRequest request, StreamObserver<NotifyServiceOuterClass.NotificationResponse> responseObserver) {
        synchronized (lock) {
            StreamObserver<NotifyServiceOuterClass.Notification> streamObserver = streamObserverMap.get(request.getAirlineName());
            if (streamObserver == null) {
                throw new AirlineNotRegisteredForNotificationsException(request.getAirlineName());
            }
            streamObserver.onCompleted();
            streamObserverMap.put(request.getAirlineName(), null);
        }
        responseObserver.onNext(NotifyServiceOuterClass.NotificationResponse.newBuilder().setResponse(Models.SimpleStatusResponse.OK).build());
        responseObserver.onCompleted();
    }
}
