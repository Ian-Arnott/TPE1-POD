package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.AdminAirportServiceOuterClass;
import com.google.protobuf.Empty;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class ManifestResponseFutureCallback extends AbstractFutureCallback<Empty>{

    private final String booking;
    private final String flight;
    private final String airline;

    public ManifestResponseFutureCallback(Logger logger, CountDownLatch latch, String booking, String flight, String airline) {
        super(logger, latch);
        this.booking = booking;
        this.flight = flight;
        this.airline = airline;
    }

    @Override
    public void onSuccess(Empty result) {
        String response = String.format("Booking %s for %s %s added successfully",booking,flight,airline);
        System.out.println(response);
        getLatch().countDown();
    }
}
