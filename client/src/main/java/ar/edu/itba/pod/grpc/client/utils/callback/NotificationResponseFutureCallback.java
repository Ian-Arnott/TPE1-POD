package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.NotifyServiceOuterClass;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class NotificationResponseFutureCallback extends AbstractFutureCallback<NotifyServiceOuterClass.NotificationResponse> {

    private final String airlineName;

    public NotificationResponseFutureCallback(Logger logger, CountDownLatch latch, String airlineName) {
        super(logger,latch);
        this.airlineName = airlineName;
    }

    @Override
    public void onSuccess(NotifyServiceOuterClass.NotificationResponse result) {
        System.out.println(airlineName + " unregistered successfully for events");
        getLatch().countDown();
    }
}
