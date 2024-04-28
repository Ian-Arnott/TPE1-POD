package ar.edu.itba.pod.grpc.server.exeptions;

public class AirlineNotRegisteredForNotificationsException extends RuntimeException {
    private final String airlineName;

    public AirlineNotRegisteredForNotificationsException(String airlineName) {
        this.airlineName = airlineName;
    }

    @Override
    public String getMessage() {
        return airlineName + " has not registered to receive notifications";
    }

}
