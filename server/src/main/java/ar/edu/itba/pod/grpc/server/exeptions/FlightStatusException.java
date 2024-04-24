package ar.edu.itba.pod.grpc.server.exeptions;

public class FlightStatusException extends RuntimeException {
    private final String flight;
    private final String status;

    public FlightStatusException(String flight, String status) {
        this.flight = flight;
        this.status = status;
    }

    @Override
    public String getMessage() {
        return "Flight " + flight + status;
    }
}
