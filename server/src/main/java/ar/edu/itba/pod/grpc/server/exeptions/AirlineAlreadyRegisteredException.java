package ar.edu.itba.pod.grpc.server.exeptions;

public class AirlineAlreadyRegisteredException extends RuntimeException {
    private final String airlineName;

    public AirlineAlreadyRegisteredException(String airlineName) {
        this.airlineName = airlineName;
    }

    @Override
    public String getMessage() {
        return airlineName + " already registered for notifications.";
    }
}
