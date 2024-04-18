package ar.edu.itba.pod.grpc.server.exeptions;

public class FlightDoesNotExistsException extends RuntimeException {
    private final String flight;

    public FlightDoesNotExistsException(String flight) {
        this.flight = flight;
    }

    @Override
    public String getMessage() {
        return "Flight " + flight + " does not exists";
    }
}
