package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.QueryServiceOuterClass;
import ar.edu.itba.pod.grpc.client.utils.ClientUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class QueryCheckInsFutureCallback extends AbstractFutureCallback<QueryServiceOuterClass.QueryCheckInsResponse>{
    private final String outPath;

    public QueryCheckInsFutureCallback(Logger logger, CountDownLatch latch, String outPath) {
        super(logger,latch);
        this.outPath = outPath;
    }

    @Override
    public void onSuccess(QueryServiceOuterClass.QueryCheckInsResponse result) {
        List<QueryServiceOuterClass.QueryCheckInItem> list = result.getQueryListList();
        generateQueryFile(list);
        getLatch().countDown();
    }

    private void generateQueryFile(List<QueryServiceOuterClass.QueryCheckInItem> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sector\tCounter\tAirline\tFlight\tBooking\n");
        stringBuilder.append("####################################################\n");
        for (QueryServiceOuterClass.QueryCheckInItem item : list) {
            stringBuilder.append(item.getSectorName()).append("\t")
                    .append(item.getCounter()).append("\t")
                    .append(item.getAirlineName()).append("\t")
                    .append(item.getFlightCode()).append("\t")
                    .append(item.getBookingCode()).append("\n");
        }
        ClientUtils.createOutputFile(outPath, stringBuilder.toString());
    }
}
