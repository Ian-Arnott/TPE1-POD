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
    public Airline(String name) {
        this.name = name;
        this.bookings = new ConcurrentHashMap<>();
        this.flights = new ConcurrentHashMap<>();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airline airline = (Airline) o;
        return Objects.equals(name, airline.name) && Objects.equals(bookings, airline.bookings) && Objects.equals(flights, airline.flights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bookings, flights);
    }

}
