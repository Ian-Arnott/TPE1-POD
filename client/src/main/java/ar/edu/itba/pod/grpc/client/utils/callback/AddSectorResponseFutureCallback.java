package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.AdminAirportServiceOuterClass;
import com.google.protobuf.BoolValue;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class AddSectorResponseFutureCallback extends AbstractFutureCallback<BoolValue>{

    private final String sectorName;

    public AddSectorResponseFutureCallback(Logger logger, CountDownLatch latch, String sectorName) {
        super(logger, latch);
        this.sectorName = sectorName;
    }

    @Override
    public void onSuccess(BoolValue result) {
        if (result.getValue()) {
            System.out.println("Sector " + sectorName + " added successfully");
        } else {
            System.out.println("Sector " + sectorName + " already exists");
        }
        getLatch().countDown();
    }
}
