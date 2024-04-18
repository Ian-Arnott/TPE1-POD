package ar.edu.itba.pod.grpc.server.exeptions;

public class FlightDoesNotHaveBookingsException extends RuntimeException {
    private final String flight;

    public FlightDoesNotHaveBookingsException(String flight) {
        this.flight = flight;
    }

    @Override
    public String getMessage() {
        return "Flight " + flight + " does not have bookings";
    }
}
