package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.AdminAirportServiceOuterClass;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class ManifestResponseFutureCallback extends AbstractFutureCallback<AdminAirportServiceOuterClass.ManifestResponse>{

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
    public void onSuccess(AdminAirportServiceOuterClass.ManifestResponse result) {
        String response = String.format("Booking %s for %s %s %s",booking,flight,airline, result.getMessage());
        System.out.println(response);
        getLatch().countDown();
    }

    @Override
    public void onFailure(Throwable t) {
        System.out.println("Booking " + booking + " for flight " + flight + " failed");
        super.onFailure(t);
    }
}
