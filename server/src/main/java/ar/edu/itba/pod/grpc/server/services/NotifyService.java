package ar.edu.itba.pod.grpc.server.services;

import airport.Models;
import airport.NotifyServiceGrpc;
import airport.NotifyServiceOuterClass;
import ar.edu.itba.pod.grpc.server.exeptions.AirlineNotRegisteredForNotificationsException;
import ar.edu.itba.pod.grpc.server.models.*;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NotifyService extends NotifyServiceGrpc.NotifyServiceImplBase {
    private final static Logger logger = LoggerFactory.getLogger(NotifyService.class);
    private final static ConcurrentMap<String, StreamObserver<NotifyServiceOuterClass.Notification>> streamObserverConcurrentMap = new ConcurrentHashMap<>();
    private final static String lock = "lock";
    private final AirportRepository repository;
    public NotifyService() {
        repository = AirportRepository.getInstance();
    }

    @Override
    public void notifyAirline(NotifyServiceOuterClass.NotifyRequest request, StreamObserver<NotifyServiceOuterClass.Notification> responseObserver) {
        synchronized (lock) {
            repository.registerForNotifications(request.getAirlineName());
            streamObserverConcurrentMap.put(request.getAirlineName(), responseObserver);
        }
        notifyAirline(request.getAirlineName(), request.getAirlineName() + " registered successfully for check-in events.");
    }

    private void notifyAirline(String airline, String message){
        synchronized (lock) {
            StreamObserver<NotifyServiceOuterClass.Notification> streamObserver = streamObserverConcurrentMap.get(airline);
            if (streamObserver != null)
                streamObserver.onNext(NotifyServiceOuterClass.Notification.newBuilder().setMessage(message).build());
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
    public void notifyAssignedRange(Airline airline, CounterRange counterRange, String sectorName) {
        String notificationMessage = String.format("%d counters (%d-%d) in Sector %s are now checking in passengers from %s %s flights.",
                counterRange.getCounterCount(),
                counterRange.getFirstCounter().getNum(),
                counterRange.getLastCounter().getNum(),
                sectorName,
                airline.getName(),
                getFlightCodes(counterRange.getFlights()));
        notifyAirline(airline.getName(), notificationMessage);
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

    public void notifyPassengerCheckIn(Booking booking, int counterCode, String sectorName) {
        String notification = String.format("Check-in successful of %s for flight %s at counter %d in Sector %s",
                booking.getCode(),booking.getFlight().getCode(), counterCode,sectorName);
        notifyAirline(booking.getAirlineName(), notification);
    }

    public void notifyPendingAssignments(List<PendingAssignment> pendingAssignmentList, String sectorName) {
        for (PendingAssignment pendingAssignment : pendingAssignmentList) {
            Flight flight = pendingAssignment.getFlights().peek();
            if (flight != null)
                notifyAssignedRange(pendingAssignment.getAirline(), flight.getCounterRange(), sectorName);
        }

        if (!pendingAssignmentList.isEmpty()) {
            AtomicInteger pos = new AtomicInteger(1);
            ConcurrentLinkedQueue<PendingAssignment> list = repository.getPendingAssignments(sectorName);
            list.forEach(pendingAssignment -> {
                notifyPendingChange(pendingAssignment.getAirline(), pendingAssignment.getCountVal(), pendingAssignment.getFlights(), pos, sectorName, list.size());
            });
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

    public void notifyPendingChange(Airline airline, AtomicInteger countVal, ConcurrentLinkedQueue<Flight> flightConcurrentLinkedQueue, AtomicInteger pos, String sectorName, int pendingAmount) {
        String notification = String.format("%d counters for flights %s is pending with %d other pendings ahead",
                countVal.get(),getFlightCodes(flightConcurrentLinkedQueue), pendingAmount - pos.get());
        notifyAirline(airline.getName(), notification);
    }

    @Override
    public void notifyRemoveAirline(NotifyServiceOuterClass.NotifyRequest request, StreamObserver<NotifyServiceOuterClass.NotificationResponse> responseObserver) {
        synchronized (lock) {
            StreamObserver<NotifyServiceOuterClass.Notification> streamObserver = streamObserverConcurrentMap.get(request.getAirlineName());
            if (streamObserver == null) {
                throw new AirlineNotRegisteredForNotificationsException(request.getAirlineName());
            }
            streamObserver.onCompleted();
        }
        responseObserver.onNext(NotifyServiceOuterClass.NotificationResponse.newBuilder().setResponse(Models.SimpleStatusResponse.OK).build());
        responseObserver.onCompleted();
    }
}
