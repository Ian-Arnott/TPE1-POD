package ar.edu.itba.pod.grpc.client.utils.observers;

import airport.NotifyServiceOuterClass;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class NotificationStreamObserver implements StreamObserver<NotifyServiceOuterClass.Notification> {
    private final CountDownLatch latch;

    public NotificationStreamObserver(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onNext(NotifyServiceOuterClass.Notification value) {
        System.out.println(value.getMessage());
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
