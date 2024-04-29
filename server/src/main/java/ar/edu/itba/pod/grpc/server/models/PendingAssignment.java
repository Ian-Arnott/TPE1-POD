package ar.edu.itba.pod.grpc.server.models;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PendingAssignment {
    private final Airline airline;
    private final ConcurrentLinkedQueue<Flight> flights;
    private final AtomicInteger countVal;

    public PendingAssignment(Airline airline, ConcurrentLinkedQueue<Flight> flights, Integer countVal) {
        this.airline = airline;
        this.flights = flights;
        this.countVal = new AtomicInteger(countVal);
    }

    public ConcurrentLinkedQueue<Flight> getFlights() {
        return flights;
    }

    public AtomicInteger getCountVal() {
        return countVal;
    }

    public Airline getAirline() {
        return airline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PendingAssignment that = (PendingAssignment) o;
        return Objects.equals(airline, that.airline) && Objects.equals(flights, that.flights) && (countVal.get() == that.countVal.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(airline, flights, countVal);
    }

    public PendingAssignmentRecord toRecord(boolean isPending, int firstCounter) {
            return new PendingAssignmentRecord(
                    airline.getName(),
                    flights.stream().map(Flight::getCode).toList(),
                    countVal.get(),
                    isPending,
                    firstCounter
            );
    }
    public record PendingAssignmentRecord(
            String airlineName,
            List<String> flightCodes,
            int counterCount,
            boolean isPending,
            int firstCounter
    ){}
}