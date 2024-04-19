package ar.edu.itba.pod.grpc.server.models;

import org.checkerframework.checker.units.qual.A;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Flight {
    private final String code;
    private final Airline airline;
    private final ConcurrentHashMap<String, Booking> bookings;

    // podria ser un status que tiene como posibles valores de un enum,
    // pero no es atomico el cambio de staus usando el enum.
    private AtomicBoolean checkedIn;
    private AtomicBoolean checkingIn;
    private AtomicBoolean pending;
    private String sectorName;
    public Flight(String code, Airline airline) {
        this.code = code;
        this.airline = airline;
        this.bookings = new ConcurrentHashMap<>();
        this.checkedIn = new AtomicBoolean(false);
        this.checkingIn = new AtomicBoolean(false);
        this.pending = new AtomicBoolean(false);
        sectorName = null;
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

    public void setCheckedIn(AtomicBoolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public void setCheckingIn(AtomicBoolean checkingIn) {
        this.checkingIn = checkingIn;
    }

    public void setPending(AtomicBoolean pending) {
        this.pending = pending;
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

    public synchronized boolean allBookingsCheckedIn() {
        for (Booking booking : bookings.values()) {
            if (!booking.getCheckedIn().get()) {
                return false;
            }
        }
        return true;
    }
}
