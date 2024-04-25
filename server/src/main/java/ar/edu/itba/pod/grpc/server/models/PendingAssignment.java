package ar.edu.itba.pod.grpc.server.models;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PendingAssignment {
    private final ConcurrentLinkedQueue<Flight> flights;
    private final String airlineName;
    private final AtomicInteger countVal;

    public PendingAssignment(ConcurrentLinkedQueue<Flight> flights, String airlineName, Integer countVal) {
        this.flights = flights;
        this.airlineName = airlineName;
        this.countVal = new AtomicInteger(countVal);
    }

    public ConcurrentLinkedQueue<Flight> getFlights() {
        return flights;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public AtomicInteger getCountVal() {
        return countVal;
    }
}
