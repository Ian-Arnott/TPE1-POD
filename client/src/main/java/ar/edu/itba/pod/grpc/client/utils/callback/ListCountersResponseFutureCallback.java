package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.CounterAssignmentServiceOuterClass;
import jdk.jfr.FlightRecorder;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ListCountersResponseFutureCallback extends AbstractFutureCallback<CounterAssignmentServiceOuterClass.ListCountersResponse>{

    private String sectorName;
    private int counterFrom;
    private int counterTo;
    public ListCountersResponseFutureCallback(
            Logger logger,
            CountDownLatch latch,
            String sectorName,
            int counterFrom,
            int counterTo
    ) {
        super(logger, latch);
        this.sectorName = sectorName;
        this.counterFrom = counterFrom;
        this.counterTo = counterTo;
    }

    private boolean shouldAddToCurrentBlock(
            CounterAssignmentServiceOuterClass.ListCounterItem item,
            List<CounterAssignmentServiceOuterClass.ListCounterItem> currentBlock
    ) {
        if (item == null) return true;
        if (currentBlock.isEmpty()) return true;

        CounterAssignmentServiceOuterClass.ListCounterItem firstItem = currentBlock.getFirst();
        return firstItem.getAirlineName().equals(item.getAirlineName())
                && firstItem.getFlightCodesList().equals(item.getFlightCodesList());
    }

    @Override
    public void onSuccess(CounterAssignmentServiceOuterClass.ListCountersResponse response) {
        List<CounterAssignmentServiceOuterClass.ListCounterItem> items = response.getItemsList();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Counters\tAirline\tFlights\tPeople\n");
        stringBuilder.append("##################################\n");
        Iterator<CounterAssignmentServiceOuterClass.ListCounterItem> itemIterator = items.iterator();

        CounterAssignmentServiceOuterClass.ListCounterItem currentItem = null;
        List<CounterAssignmentServiceOuterClass.ListCounterItem> currentBlock = new ArrayList<>();

        int currentVal = counterFrom;
        while (itemIterator.hasNext()) {
            while (itemIterator.hasNext() && shouldAddToCurrentBlock(currentItem, currentBlock)) {
                currentItem = itemIterator.next();
                currentVal++;
                currentBlock.add(currentItem);
            }
            // print line
            if (!currentBlock.isEmpty()) {
                CounterAssignmentServiceOuterClass.ListCounterItem firstItem = currentBlock.getFirst();
                stringBuilder.append("(")
                        .append(firstItem.getCounterNum())
                        .append("-")
                        .append(currentVal - 1)
                        .append(")\t");

                String airlineName = firstItem.getAirlineName();

                if (airlineName.isEmpty()) {
                    stringBuilder.append("-").append("\t").append("-").append("\t").append("-").append("\t");
                } else {
                    stringBuilder.append(firstItem.getAirlineName()).append("\t");
                    int flight_i = 0;
                    for (String f : firstItem.getFlightCodesList()) {
                        if (flight_i != 0) {
                            stringBuilder.append("|");
                        }
                        stringBuilder.append(f);
                        flight_i++;
                    }
                    stringBuilder.append("\t").append(firstItem.getPeople()).append("\n");
                }
                currentBlock.clear();
            }
        }
        // print last empty line if needed
        System.out.print(stringBuilder);

        getLatch().countDown();
    }
}
