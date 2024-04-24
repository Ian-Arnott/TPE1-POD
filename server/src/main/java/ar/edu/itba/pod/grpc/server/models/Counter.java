package ar.edu.itba.pod.grpc.server.models;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private String airline;
    private ConcurrentLinkedQueue<Flight> flights;
    private final ConcurrentLinkedQueue<Booking> bookingQueue;
    private final AtomicBoolean isCheckingIn;
    private final AtomicBoolean isFirstInRange;
    private final AtomicInteger lastInRange;

    public Counter() {
        flights = null;
        airline = null;
        isCheckingIn = new AtomicBoolean(false);
        isFirstInRange = new AtomicBoolean(false);
        lastInRange = new AtomicInteger(0);
        this.bookingQueue = new ConcurrentLinkedQueue<>();
    }

    public AtomicBoolean getIsFirstInRange() {
        return isFirstInRange;
    }

    public AtomicInteger getLastInRange() {
        return lastInRange;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public void setFlights(ConcurrentLinkedQueue<Flight> flights) {
        this.flights = flights;
    }

    public AtomicBoolean getIsCheckingIn() {
        return isCheckingIn;
    }

    public ConcurrentLinkedQueue<Flight> getFlights() {
        return flights;
    }

    public String getAirline() {
        return airline;
    }

    public ConcurrentLinkedQueue<Booking> getBookingQueue() {
        return bookingQueue;
    }

    public synchronized boolean bookingQueueEmpty() {
        return bookingQueue.isEmpty();
    }
}
