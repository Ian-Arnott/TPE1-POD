package ar.edu.itba.pod.grpc.server.models;

public class Booking {

    private final String code;
    private final Airline airline;
    private final Flight flight;

    public Booking(String code, Airline airline, Flight flight) {
        this.code = code;
        this.airline = airline;
        this.flight = flight;
    }
}
