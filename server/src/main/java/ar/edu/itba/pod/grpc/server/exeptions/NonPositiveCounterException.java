package ar.edu.itba.pod.grpc.server.exeptions;

public class NonPositiveCounterException extends RuntimeException {
    private final int counter;

    public NonPositiveCounterException(int counter) {
        this.counter = counter;
    }

    @Override
    public String getMessage() {
        return "Counter amount " + counter + " is not positive";
    }
}
