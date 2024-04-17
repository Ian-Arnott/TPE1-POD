package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.AdminAirportServiceOuterClass;
import com.google.protobuf.BoolValue;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class AddCountersResponseFutureCallback extends AbstractFutureCallback<AdminAirportServiceOuterClass.AddCountersResponse>{

    private final String sectorName;
    private final int counterAmount;

    public AddCountersResponseFutureCallback(Logger logger, CountDownLatch latch, String sectorName, int counterAmount) {
        super(logger, latch);
        this.sectorName = sectorName;
        this.counterAmount = counterAmount;
    }

    @Override
    public void onSuccess(AdminAirportServiceOuterClass.AddCountersResponse result) {
        int first = result.getLastCounterAdded() - counterAmount;
        int last = result.getLastCounterAdded() - 1;
        String resp = counterAmount +
                " new counters (" +
                first +
                "-" +
                last +
                ") in Sector "+
                sectorName +
                " added successfully";
        System.out.println(resp);
        getLatch().countDown();
    }
}
