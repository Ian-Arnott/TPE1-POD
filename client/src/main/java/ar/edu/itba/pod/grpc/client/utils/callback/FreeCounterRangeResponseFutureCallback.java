package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.CounterAssignmentServiceOuterClass;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class FreeCounterRangeResponseFutureCallback extends AbstractFutureCallback<CounterAssignmentServiceOuterClass.FreeCounterRangeResponse> {

    private final String sectorName;
    private final String airline;
    private final int fromVal;

    public FreeCounterRangeResponseFutureCallback(Logger logger, CountDownLatch latch, String sectorName, String airline, int fromVal) {
        super(logger,latch);
        this.sectorName = sectorName;
        this.airline = airline;
        this.fromVal = fromVal;
    }

    @Override
    public void onSuccess(CounterAssignmentServiceOuterClass.FreeCounterRangeResponse result) {
        String response;
        StringBuilder flights = new StringBuilder();
        AtomicInteger counter = new AtomicInteger(0);
        result.getFlightsList().forEach(flight -> {
            flights.append(flight);
            if (counter.get() != flights.length()-1)
                flights.append("|");
            counter.getAndIncrement();
        });
        response = String.format("Ended check-in for flights %s on %d counters (%d-%d) in Sector %s",
                flights.toString(),
                result.getFreedAmount(),
                fromVal,
                fromVal - 1 + result.getFreedAmount(),
                sectorName);
        System.out.println(response);
        getLatch().countDown();
    }
}
