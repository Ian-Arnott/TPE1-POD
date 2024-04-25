package ar.edu.itba.pod.grpc.server.models;

import java.util.concurrent.atomic.AtomicBoolean;

public class Booking {
    private final String code;
    private final Flight flight;
    private final AtomicBoolean checkedIn;
    private final AtomicBoolean inQueue;

    public Booking(String code, Flight flight) {
        this.code = code;
        this.flight = flight;
        this.checkedIn = new AtomicBoolean(false);
        inQueue = new AtomicBoolean(false);
    }

    public AtomicBoolean getCheckedIn() {
        return checkedIn;
    }

    public void checkIn() {
        checkedIn.set(true);
    }


    public String getCode() {
        return code;
    }

    public Flight getFlight() {
        return flight;
    }

    public AtomicBoolean getInQueue() {
        return inQueue;
    }
}
