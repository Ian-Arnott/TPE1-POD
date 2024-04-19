package ar.edu.itba.pod.grpc.server.exeptions;

public class CounterIsCheckingInOtherAirlineException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Counter are checking in an other airline";
    }
}
