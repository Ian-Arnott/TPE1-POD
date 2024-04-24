package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.CheckInServiceOuterClass;
import com.google.protobuf.Empty;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class PassengerCheckinFutureCallback extends AbstractFutureCallback<CheckInServiceOuterClass.PassengerCheckInResponse>{

    private final String booking;
    private final int firstCounter;
    private final String sectorName;

    public PassengerCheckinFutureCallback(Logger logger, CountDownLatch latch, String booking, int firstCounter, String sectorName) {
        super(logger, latch);
        this.booking = booking;
        this.firstCounter = firstCounter;
        this.sectorName = sectorName;
    }

    @Override
    public void onSuccess(CheckInServiceOuterClass.PassengerCheckInResponse result) {
        System.out.println("Booking " + booking + " for flight " + result.getFlight()
        + " from " + result.getAirline() + " is now waiting to check-in on counters (" +
                firstCounter + "-" + result.getLastCounter() + ") in Sector " + sectorName
        + " with " + result.getPeopleInLIne() + " people in line");
        getLatch().countDown();
    }
}
