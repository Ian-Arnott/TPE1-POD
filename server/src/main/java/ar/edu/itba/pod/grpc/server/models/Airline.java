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

    public void registerForNotifications() {
        shouldNotify = true;
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
