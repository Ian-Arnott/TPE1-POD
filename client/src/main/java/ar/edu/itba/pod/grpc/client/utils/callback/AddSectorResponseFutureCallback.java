package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.AdminAirportServiceOuterClass;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class AddSectorResponseFutureCallback extends AbstractFutureCallback<Empty>{

    private final String sectorName;

    public AddSectorResponseFutureCallback(Logger logger, CountDownLatch latch, String sectorName) {
        super(logger, latch);
        this.sectorName = sectorName;
    }

    @Override
    public void onSuccess(Empty empty) {
        System.out.println("Sector " + sectorName + " added successfully");
        getLatch().countDown();
    }
}
