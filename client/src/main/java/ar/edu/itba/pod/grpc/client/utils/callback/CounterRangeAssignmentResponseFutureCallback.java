package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.CounterAssignmentServiceOuterClass;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class CounterRangeAssignmentResponseFutureCallback extends AbstractFutureCallback<CounterAssignmentServiceOuterClass.CounterRangeAssignmentResponse> {

    private final String sectorName;
    private final String airline;
    private final String flights;

    public CounterRangeAssignmentResponseFutureCallback(Logger logger, CountDownLatch latch, String sectorName, String airline, String flights) {
        super(logger,latch);
        this.sectorName = sectorName;
        this.airline = airline;
        this.flights = flights;
    }

    @Override
    public void onSuccess(CounterAssignmentServiceOuterClass.CounterRangeAssignmentResponse result) {
        String response;
        if (result.getAmountPending() == 0) {
            response = String.format("%d counters (%d-%d) in Sector %s are now checking in passengers from %s %s flights",
                    result.getAmountCheckingIn(),
                    result.getLastCheckingIn() - result.getAmountCheckingIn() + 1,
                    result.getLastCheckingIn(),
                    sectorName,
                    airline,
                    flights);
        } else {
            response = String.format("%d counters in Sector %s is pending with %d others pending ahead",
                    result.getAmountPending(),
                    sectorName,
                    result.getAmountPendingAhead());
        }
        System.out.println(response);
        getLatch().countDown();
    }
}
