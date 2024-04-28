package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.CheckInServiceOuterClass;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PassengerStatusResponseFutureCallback extends AbstractFutureCallback<CheckInServiceOuterClass.PassengerStatusResponse> {
    private final String bookingCode;

    public PassengerStatusResponseFutureCallback(Logger logger, CountDownLatch latch, String bookingCode) {
        super(logger, latch);
        this.bookingCode = bookingCode;
    }

    @Override
    public void onSuccess(CheckInServiceOuterClass.PassengerStatusResponse result) {
        printOutput(result);
        getLatch().countDown();
    }

    public void printOutput(CheckInServiceOuterClass.PassengerStatusResponse res) {
        if (res.getIsCheckedIn()) {
            System.out.println("Booking " + bookingCode + " for flight " + res.getFlightCode() + " from " +
                    res.getAirlineName() + " checked in at counter " + res.getCounterOfCheckIn() + " in Sector " +
                    res.getSectorName());
            return;
        }

        StringBuilder counterString = new StringBuilder();
        List<Integer> counters = res.getCountersForCheckingInList();

        for (int i = 0; i < counters.size(); i++) {
            int start = counters.get(i);
            int end = start;

            while (i + 1 < counters.size() && counters.get(i + 1) == end + 1) {
                end = counters.get(i + 1);
                i++;
            }

            counterString.append("(").append(start).append("-").append(end).append(")");
        }

        if (res.getIsCheckingIn()) {
            System.out.println("Booking " + bookingCode + " for flight " + res.getFlightCode() + " from " +
                    res.getAirlineName() + " is now waiting to check-in on counters " + counterString +
                    " in Sector " + res.getSectorName() + " with " + res.getPeopleAmountInLine() + " people in line");
            return;
        }

        System.out.println("Booking " + bookingCode + " for flight " + res.getFlightCode() + " from " +
                res.getAirlineName() + " can check-in on counters " + counterString + " in Sector " +
                res.getSectorName());
    }
}
