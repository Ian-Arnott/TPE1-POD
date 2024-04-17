package ar.edu.itba.pod.grpc.server.models.requests;

import airport.AdminAirportServiceOuterClass;

import java.util.jar.Manifest;

public class ManifestRequestModel {
    private final String booking;
    private final String flight;
    private final String airline;

    public ManifestRequestModel(final String booking, final String flight, final String airline) {
        if (booking == null || booking.isEmpty())
            throw new IllegalArgumentException("booking is null or empty");
        if (flight == null || flight.isEmpty())
            throw new IllegalArgumentException("flight is null or empty");
        if (airline == null || airline.isEmpty())
            throw new IllegalArgumentException("airline is null or empty");

        this.booking = booking;
        this.flight = flight;
        this.airline = airline;
    }

    public String getBooking() {
        return booking;
    }

    public String getFlight() {
        return flight;
    }

    public String getAirline() {
        return airline;
    }

    public static ManifestRequestModel fromManifestRequest(
            final AdminAirportServiceOuterClass.ManifestRequest manifest) {
        return new ManifestRequestModel(
                manifest.getBooking(),
                manifest.getFlight(),
                manifest.getAirline()
        );
    }
}
