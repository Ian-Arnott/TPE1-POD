package ar.edu.itba.pod.grpc.server.models;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PendingAssignment {
    private final ConcurrentLinkedQueue<Flight> flights;
    private final AtomicInteger countVal;

    public PendingAssignment(ConcurrentLinkedQueue<Flight> flights, Integer countVal) {
        this.flights = flights;
        this.countVal = new AtomicInteger(countVal);
    }

    public ConcurrentLinkedQueue<Flight> getFlights() {
        return flights;
    }


    public AtomicInteger getCountVal() {
        return countVal;
    }
}
