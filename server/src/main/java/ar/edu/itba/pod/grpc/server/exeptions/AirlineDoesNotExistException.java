package ar.edu.itba.pod.grpc.server.exeptions;

public class AirlineDoesNotExistException extends RuntimeException {

    private final String airline;
    public AirlineDoesNotExistException(String airline) {
        this.airline = airline;
    }

    @Override
    public String getMessage() {
        return "Airline " + airline + " does not exist";
    }
}
