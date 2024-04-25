package ar.edu.itba.pod.grpc.server.exeptions;

public class InvalidCounterRangeException extends RuntimeException {
    private final int from;
    private final int to;

    public InvalidCounterRangeException(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String getMessage() {
        return "(" + from + "-" + to + ") is not a valid range";
    }
}
