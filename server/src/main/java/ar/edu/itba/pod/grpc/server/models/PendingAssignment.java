package ar.edu.itba.pod.grpc.server.models;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PendingAssignment {
    private final Airline airline;
    private final ConcurrentLinkedQueue<Flight> flights;
    private final String airlineName;
    private final AtomicInteger countVal;

    public PendingAssignment(Airline airline, ConcurrentLinkedQueue<Flight> flights, String airlineName, Integer countVal) {
        this.airline = airline;
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

    public Airline getAirline() {
        return airline;
    }

    // public void notifyAssignedPending(CounterRange counterRange, String sectorName) {
    //     if (airline.isShouldNotify())
    //         airline.notifyAssignedRange(counterRange, sectorName);
    // }

    // public void notifyChange(AtomicInteger countVal, ConcurrentLinkedQueue<Flight> flightConcurrentLinkedQueue, AtomicInteger pos, String sectorName, int pendingAmount) {
    //     if (airline.isShouldNotify())
    //         airline.notifyPendingChange(countVal, flightConcurrentLinkedQueue, pos,sectorName,pendingAmount);
    // }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PendingAssignment that = (PendingAssignment) o;
        return Objects.equals(airline, that.airline) && Objects.equals(flights, that.flights) && Objects.equals(airlineName, that.airlineName) && (countVal.get() == that.countVal.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(airline, flights, airlineName, countVal);
    }
}
