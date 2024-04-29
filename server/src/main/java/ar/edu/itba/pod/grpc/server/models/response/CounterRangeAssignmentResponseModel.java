package ar.edu.itba.pod.grpc.server.models.response;

public record CounterRangeAssignmentResponseModel(
        int amountCheckingIn,
        int lastCheckingIn,
        int amountPending,
        int amountPendingAhead,
        boolean isPending
) {
}
