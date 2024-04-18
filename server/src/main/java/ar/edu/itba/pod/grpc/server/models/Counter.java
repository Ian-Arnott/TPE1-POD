package ar.edu.itba.pod.grpc.server.models;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Counter {
    private String airline;
    private ConcurrentLinkedQueue<Flight> flights;
    private AtomicBoolean isCheckingIn;

    public Counter() {
        flights = null;
        airline = null;
        isCheckingIn = new AtomicBoolean(false);
    }

    public boolean isCheckingIn() {
        return isCheckingIn.get();
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public void setFlights(ConcurrentLinkedQueue<Flight> flights) {
        this.flights = flights;
    }

    public void setIsCheckingIn(AtomicBoolean isCheckingIn) {
        this.isCheckingIn = isCheckingIn;
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
