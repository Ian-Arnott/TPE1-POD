package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.CounterAssignmentServiceOuterClass;
import airport.QueryServiceOuterClass;
import ar.edu.itba.pod.grpc.client.utils.ClientUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryCountersFutureCallback extends AbstractFutureCallback<QueryServiceOuterClass.QueryCounterResponse>{
    private final String outPath;

    public QueryCountersFutureCallback(Logger logger, CountDownLatch latch, String outPath) {
        super(logger,latch);
        this.outPath = outPath;
    }

    @Override
    public void onSuccess(QueryServiceOuterClass.QueryCounterResponse result) {
        List<QueryServiceOuterClass.QueryCounterItem> list = result.getQueryListList();
        generateQueryFile(list);
        getLatch().countDown();
    }
    private boolean shouldAddToCurrentBlock(
            QueryServiceOuterClass.CounterItem item,
            List<QueryServiceOuterClass.CounterItem> currentBlock
    ) {
        if (item == null) return false;
        if (currentBlock.isEmpty()) return true;
        if (currentBlock.getLast().getCounterNum() + 1 != item.getCounterNum()) return false;

        QueryServiceOuterClass.CounterItem firstItem = currentBlock.getFirst();
        return firstItem.getAirlineName().equals(item.getAirlineName())
                && firstItem.getFlightCodesList().equals(item.getFlightCodesList());
    }
    private void generateQueryFile(List<QueryServiceOuterClass.QueryCounterItem> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sector\tCounters\tAirline\tFlights\tPeople\n");
        stringBuilder.append("##########################################\n");
        for (QueryServiceOuterClass.QueryCounterItem item : list) {
            Iterator<QueryServiceOuterClass.CounterItem> itemIterator = item.getCountersList().iterator();

            if (itemIterator.hasNext()) {
                QueryServiceOuterClass.CounterItem currentItem = itemIterator.next();
                List<QueryServiceOuterClass.CounterItem> currentBlock = new ArrayList<>();

                while (itemIterator.hasNext()) {
                    while (shouldAddToCurrentBlock(currentItem, currentBlock)) {
                        currentBlock.add(currentItem);
                        if (itemIterator.hasNext()) {
                            currentItem = itemIterator.next();
                        } else {
                            currentItem = null;
                        }
                    }
                    // print line
                    if (!currentBlock.isEmpty()) {
                        QueryServiceOuterClass.CounterItem firstItem = currentBlock.getFirst();
                        QueryServiceOuterClass.CounterItem lastItem = currentBlock.getLast();
                        stringBuilder.append(item.getSectorName()).append("\t");

                        stringBuilder.append("(")
                                .append(firstItem.getCounterNum())
                                .append("-")
                                .append(lastItem.getCounterNum())
                                .append(")\t");

                        String airlineName = firstItem.getAirlineName();

                        if (airlineName.isEmpty()) {
                            stringBuilder.append("-").append("\t").append("-").append("\t").append("-").append("\n");
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
            }
        }
        ClientUtils.createOutputFile(outPath, stringBuilder.toString());
    }

    private static String getFlightCodes(List<String> flightList) {
        StringBuilder flightString = new StringBuilder();
        AtomicInteger counter = new AtomicInteger();
        int flightsAmount = flightList.size();
        flightList.forEach(flight -> {
            flightString.append(flight);
            counter.getAndIncrement();
            if (counter.get() < flightsAmount)
                flightString.append("|");
        });
        return flightString.toString();
    }
}
