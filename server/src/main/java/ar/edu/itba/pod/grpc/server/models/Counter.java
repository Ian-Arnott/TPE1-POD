package ar.edu.itba.pod.grpc.server.models;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private String airline;
    private ConcurrentLinkedQueue<Flight> flights;
    private AtomicBoolean isCheckingIn;
    private AtomicBoolean isFirstInRange;
    private AtomicInteger lastInRange;

    public Counter() {
        flights = null;
        airline = null;
        isCheckingIn = new AtomicBoolean(false);
        isFirstInRange = new AtomicBoolean(false);
        lastInRange = new AtomicInteger(0);
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
}
