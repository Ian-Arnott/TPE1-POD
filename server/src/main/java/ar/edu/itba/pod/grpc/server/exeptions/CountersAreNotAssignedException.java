package ar.edu.itba.pod.grpc.server.exeptions;

public class CountersAreNotAssignedException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Counters are not assigned";
    }
}
