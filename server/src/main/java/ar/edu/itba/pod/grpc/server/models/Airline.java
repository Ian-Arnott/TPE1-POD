package ar.edu.itba.pod.grpc.server.models;

import airport.NotifyServiceOuterClass;
import io.grpc.stub.StreamObserver;

import java.security.InvalidParameterException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    public void notifyAirline(String message){
        shouldNotifyLock.lock();
        try {
            if (!shouldNotify)
                throw new InvalidParameterException("Airline is not registered for notifications.");
            notificationObserver.onNext(NotifyServiceOuterClass.Notification.newBuilder().setMessage(message).build());
        } finally {
            shouldNotifyLock.unlock();
        }
    }


}
