package ar.edu.itba.pod.grpc.server.exeptions;

public class FlightExistsForOtherAirlineException extends RuntimeException{
    private final String flight;

    public FlightExistsForOtherAirlineException(String flight) {
        this.flight = flight;
    }

    @Override
    public String getMessage() {
        return "Flight " + flight + " already exists for another airline.";
    }
}
