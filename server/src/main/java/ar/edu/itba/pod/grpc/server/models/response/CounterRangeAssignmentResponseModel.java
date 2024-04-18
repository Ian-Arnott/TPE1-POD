package ar.edu.itba.pod.grpc.server.models.response;

public class CounterRangeAssignmentResponseModel {
    private final int amountCheckingIn;
    private final int lastCheckingIn;
    private final int amountPending;
    private final int amountPendingAhead;


    public CounterRangeAssignmentResponseModel(int amountCheckingIn, int lastCheckingIn, int amountPending, int amountPendingAhead) {
        this.amountCheckingIn = amountCheckingIn;
        this.lastCheckingIn = lastCheckingIn;
        this.amountPending = amountPending;
        this.amountPendingAhead = amountPendingAhead;
    }

    public int getAmountCheckingIn() {
        return amountCheckingIn;
    }

    public int getLastCheckingIn() {
        return lastCheckingIn;
    }

    public int getAmountPending() {
        return amountPending;
    }

    public int getAmountPendingAhead() {
        return amountPendingAhead;
    }
}
