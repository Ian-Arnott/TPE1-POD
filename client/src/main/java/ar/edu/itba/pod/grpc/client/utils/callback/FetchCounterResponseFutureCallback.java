package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.CheckInServiceOuterClass;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FetchCounterResponseFutureCallback extends AbstractFutureCallback<CheckInServiceOuterClass.FetchCounterResponse> {
    public FetchCounterResponseFutureCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(CheckInServiceOuterClass.FetchCounterResponse result) {
        printOutput(result);
        getLatch().countDown();
    }

    public void printOutput(CheckInServiceOuterClass.FetchCounterResponse res) {
        if (res.getCountersCount() == 0) {
            System.out.println("Flight " + res.getFlightCode() + " from " + res.getAirlineName() +
                    " has no counters assigned");
            return;
        }

        StringBuilder counterString = new StringBuilder();
        List<Integer> counters = res.getCountersList();

        for (int i = 0; i < counters.size(); i++) {
            int start = counters.get(i);
            int end = start;

            while (i + 1 < counters.size() && counters.get(i + 1) == end + 1) {
                end = counters.get(i + 1);
                i++;
            }

            counterString.append("(").append(start).append("-").append(end).append(")");
        }

        String flight = res.getFlightCode();
        String airline = res.getAirlineName();
        String sector = res.getSectorName();
        int peopleAmount = res.getPeopleAmountInLine();
        String people = peopleAmount <= 0 ? "no" : Integer.toString(peopleAmount);

        System.out.println("Flight " + flight + " from " + airline + " is now checking in at counters " + counterString +
                " in Sector " + sector + " with " + people + " person(s) in line");
    }
}
