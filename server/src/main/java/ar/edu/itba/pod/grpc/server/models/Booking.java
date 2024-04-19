package ar.edu.itba.pod.grpc.server.models;

import java.util.concurrent.atomic.AtomicBoolean;

public class Booking {

    private final String code;
    private final Airline airline;
    private final Flight flight;
    private AtomicBoolean checkedIn;

    public Booking(String code, Airline airline, Flight flight) {
        this.code = code;
        this.airline = airline;
        this.flight = flight;
        this.checkedIn = new AtomicBoolean(false);
    }

    public AtomicBoolean getCheckedIn() {
        return checkedIn;
    }

    public void checkIn() {
        checkedIn.set(true);
    }


}
