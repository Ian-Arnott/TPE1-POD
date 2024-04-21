package ar.edu.itba.pod.grpc.client.utils.observers;

import airport.CounterAssignmentServiceOuterClass.*;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class PerformCheckInStreamObserver implements StreamObserver<PerformCounterCheckInResponse> {
    private final CountDownLatch latch;

    public PerformCheckInStreamObserver(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onNext(PerformCounterCheckInResponse value) {
        if (value.getSuccessful()) {
            System.out.println("Check-in successful for " + value.getBooking() +
                    " for flight " + value.getFlight() + " at counter " + value.getCounter());
        } else {
            System.out.println("Counter " + value.getCounter() + " is idle");
        }
    }

    @Override
    public void onError(Throwable t) {
        latch.countDown();
        System.out.println(t.getMessage());
    }

    @Override
    public void onCompleted() {
        latch.countDown();
    }
}
