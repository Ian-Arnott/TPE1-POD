package ar.edu.itba.pod.grpc.server.models;

import airport.NotifyServiceOuterClass;
import io.grpc.stub.StreamObserver;

import java.security.InvalidParameterException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Airline {

    private final String name;
    private final ConcurrentMap<String, Booking> bookings;
    private final ConcurrentMap<String, Flight> flights;
    private final Lock shouldNotifyLock;
    private boolean shouldNotify;
    private StreamObserver<NotifyServiceOuterClass.Notification> notificationObserver;

    public Airline(String name) {
        this.name = name;
        this.bookings = new ConcurrentHashMap<>();
        this.flights = new ConcurrentHashMap<>();
        this.shouldNotifyLock = new ReentrantLock();
        this.shouldNotify = false;
    }

    public String getName() {
        return name;
    }

    public ConcurrentMap<String, Booking> getBookings() {
        return bookings;
    }

    public ConcurrentMap<String, Flight> getFlights() {
        return flights;
    }

    public void registerForNotifications(StreamObserver<NotifyServiceOuterClass.Notification> responseObserver) {
        shouldNotifyLock.lock();
        try {
            if (shouldNotify)
                throw new InvalidParameterException("Airline already registered for notifications.");
            shouldNotify = true;
            this.notificationObserver = responseObserver;
        } finally {
            shouldNotifyLock.unlock();
        }
    }

    public StreamObserver<NotifyServiceOuterClass.Notification> unregisterForNotifications() {
        StreamObserver<NotifyServiceOuterClass.Notification> toReturn;

        shouldNotifyLock.lock();
        try {
            if (!shouldNotify)
                throw new InvalidParameterException("Airline already unregistered for notifications.");
            shouldNotify = false;

            toReturn = notificationObserver;
            this.notificationObserver = null;
        } finally {
            shouldNotifyLock.unlock();
        }
        return toReturn;
    }

    public boolean isShouldNotify() {
        return shouldNotify;
    }

    private void notifyAirline(String message){
        shouldNotifyLock.lock();
        try {
            if (!shouldNotify)
                throw new InvalidParameterException("Airline is not registered for notifications.");
            notificationObserver.onNext(NotifyServiceOuterClass.Notification.newBuilder().setMessage(message).build());
        } finally {
            shouldNotifyLock.unlock();
        }
    }


    public void notifyRegistered() {
        notifyAirline(this.name + " registered successfully for check-in events.");
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

    public void notifyAssignedRange(CounterRange counterRange, String sectorName) {

        String notificationMessage = String.format("%d counters (%d-%d) in Sector %s are now checking in passengers from %s %s flights.",
                counterRange.getCounterCount(),
                counterRange.getFirstCounter().getNum(),
                counterRange.getLastCounter().getNum(),
                sectorName,
                this.name,
                getFlightCodes(counterRange.getFlights()));

        notifyAirline(notificationMessage);
    }

    public void notifyBookingInQueue(Booking booking, int peopleInLine, Counter counter, String sectorName) {
        String notification = String.format(
                "Booking %s for flight %s from %s is now waiting to check-in on counters (%d-%d) in Sector %s with %d people in line"
                ,booking.getCode(),booking.getFlight().getCode(), this.name, counter.getFirstInRange().get(), counter.getLastInRange().get(),
                sectorName, peopleInLine
        );
        notifyAirline(notification);
    }

    public void notifyPassengerCheckIn(Booking booking, int counterCode, String sectorName) {
        String notification = String.format("Check-in successful of %s for flight %s at counter %d in Sector %s",
                booking.getCode(),booking.getFlight().getCode(), counterCode,sectorName);
        notifyAirline(notification);
    }

    public void notifyPendingAssignment(PendingAssignment pendingAssignment, String sectorName, int pendingsAhead) {
        String notification = String.format("%d counters in Sector %s for flights %s is pending with %d other pendings ahead",
                pendingAssignment.getCountVal().get(), sectorName, getFlightCodes(pendingAssignment.getFlights()), pendingsAhead);
        notifyAirline(notification);
    }

    public void notifyFreeCounterRange(ConcurrentLinkedQueue<String> flightStrings, int fromVal, AtomicInteger freedAmount, String sectorName) {
        String notification = String.format("Ended check-in for flights %s on counters (%d-%d) from Sector %s",getFlightCodes(flightStrings)
                ,fromVal, fromVal-1+freedAmount.get(), sectorName);
        notifyAirline(notification);
    }

    public void notifyPendingChange(AtomicInteger countVal, ConcurrentLinkedQueue<Flight> flightConcurrentLinkedQueue, AtomicInteger pos, String sectorName, int pendingAmount) {
        String notification = String.format("%d counters for flights %s is pending with %d other pendings ahead",
                countVal.get(),getFlightCodes(flightConcurrentLinkedQueue), pendingAmount - pos.get());
        notifyAirline(notification);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airline airline = (Airline) o;
        return shouldNotify == airline.shouldNotify && Objects.equals(name, airline.name) && Objects.equals(bookings, airline.bookings) && Objects.equals(flights, airline.flights) && Objects.equals(shouldNotifyLock, airline.shouldNotifyLock) && Objects.equals(notificationObserver, airline.notificationObserver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bookings, flights, shouldNotifyLock, shouldNotify, notificationObserver);
    }

}
