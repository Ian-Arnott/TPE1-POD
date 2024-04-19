package ar.edu.itba.pod.grpc.server.exeptions;

public class CounterIsNotFirstInRangeException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Counter is not first in range ";
    }
}
