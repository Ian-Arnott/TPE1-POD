package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.QueryServiceOuterClass;
import ar.edu.itba.pod.grpc.client.utils.ClientUtils;
import org.slf4j.Logger;

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

    private void generateQueryFile(List<QueryServiceOuterClass.QueryCounterItem> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sector\tCounters\tAirline\tFlights\tPeople\n");
        stringBuilder.append("##########################################\n");
        for (QueryServiceOuterClass.QueryCounterItem item : list) {
            stringBuilder.append(item.getSectorName()).append("\t")
                    .append("(").append(item.getFirstCounter()).append("-")
                    .append(item.getLastCounter()).append(")").append("\t")
                    .append(item.getAirlineName()).append("\t")
                    .append(getFlightCodes(item.getFlightsList()))
                    .append("\t");
            if (item.getAirlineName().equals("-"))
                stringBuilder.append("-").append("\n");
            else stringBuilder.append(item.getPeople()).append("\n");
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
