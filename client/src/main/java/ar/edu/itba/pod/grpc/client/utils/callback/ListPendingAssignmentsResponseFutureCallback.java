package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.CounterAssignmentServiceOuterClass;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ListPendingAssignmentsResponseFutureCallback extends AbstractFutureCallback<CounterAssignmentServiceOuterClass.ListPendingAssignmentsResponse> {
    private final String sectorName;

    public ListPendingAssignmentsResponseFutureCallback(Logger logger, CountDownLatch latch, String sectorName) {
        super(logger, latch);
        this.sectorName = sectorName;
    }

    @Override
    public void onSuccess(CounterAssignmentServiceOuterClass.ListPendingAssignmentsResponse result) {
        printOutput(result);
        getLatch().countDown();
    }

    public void printOutput(CounterAssignmentServiceOuterClass.ListPendingAssignmentsResponse res) {
        if (res.getItemsCount() == 0) {
            System.out.println("There are no pending assignments in sector " + sectorName + '.');
            return;
        }

        System.out.println("Counters  Airline          Flights");
        System.out.println("##########################################################");

        for (CounterAssignmentServiceOuterClass.ListPendingAssignmentsItem item : res.getItemsList()) {
            Integer countersAmount = item.getCountersAmount();
            String airline = item.getAirlineName();
            List<String> flights = item.getFlightsList();
            String flightString = String.join("|", flights);

            System.out.printf("%-10d%-17s%s%n", countersAmount, airline, flightString);
        }
    }
}
