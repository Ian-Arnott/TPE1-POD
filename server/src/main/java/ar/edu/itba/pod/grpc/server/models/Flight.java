package ar.edu.itba.pod.grpc.server.models;

import org.checkerframework.checker.units.qual.A;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Flight {
    private final String code;
    private final Airline airline;
    private final ConcurrentHashMap<String, Booking> bookings;
    private final AtomicBoolean checkedIn;
    private final AtomicBoolean checkingIn;
    private final AtomicBoolean pending;
    private String sectorName;
    private CounterRange counterRange;

    public Flight(String code, Airline airline) {
        this.code = code;
        this.airline = airline;
        this.bookings = new ConcurrentHashMap<>();
        this.checkedIn = new AtomicBoolean(false);
        this.checkingIn = new AtomicBoolean(false);
        this.pending = new AtomicBoolean(false);
        sectorName = null;
        counterRange = null;
    }

    public String getCode() {
        return code;
    }

    public Airline getAirline() {
        return airline;
    }

    public AtomicBoolean getCheckedIn() {
        return checkedIn;
    }

    public AtomicBoolean getCheckingIn() {
        return checkingIn;
    }

    public AtomicBoolean getPending() {
        return pending;
    }

    public ConcurrentHashMap<String, Booking> getBookings() {
        return bookings;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setCounterRange(CounterRange counterRange) {
        this.counterRange = counterRange;
    }

    public CounterRange getCounterRange() {
        return counterRange;
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(code, flight.code) && Objects.equals(airline, flight.airline) && Objects.equals(bookings, flight.bookings) && (checkedIn.get() == flight.checkedIn.get()) && (checkingIn == flight.checkingIn) && (pending == flight.pending) && Objects.equals(sectorName, flight.sectorName)  && Objects.equals(counterRange, flight.counterRange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, airline, bookings, checkedIn, checkingIn, pending, sectorName, counterRange);
    }
}
