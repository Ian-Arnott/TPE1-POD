package ar.edu.itba.pod.grpc.server.exeptions;

public class NoCountersAddedException extends RuntimeException {
    @Override
    public String getMessage() {
        return "No counters were added to the airport";
    }
}
