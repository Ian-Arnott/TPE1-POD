package ar.edu.itba.pod.grpc.server.models;

import java.util.concurrent.ConcurrentHashMap;

public class Flight {
    private final String code;
    private final Airline airline;
    private final ConcurrentHashMap<String, Booking> bookings;

    public Flight(String code, Airline airline) {
        this.code = code;
        this.airline = airline;
        this.bookings = new ConcurrentHashMap<>();
    }

    public String getCode() {
        return code;
    }

    public Airline getAirline() {
        return airline;
    }

    public ConcurrentHashMap<String, Booking> getBookings() {
        return bookings;
    }
}
