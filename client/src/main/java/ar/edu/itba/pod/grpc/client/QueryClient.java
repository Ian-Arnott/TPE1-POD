package ar.edu.itba.pod.grpc.client;

import airport.QueryServiceGrpc;
import airport.QueryServiceOuterClass;
import ar.edu.itba.pod.grpc.client.utils.ClientArgs;
import ar.edu.itba.pod.grpc.client.utils.ClientUtils;
import ar.edu.itba.pod.grpc.client.utils.callback.QueryCheckInsFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.callback.QueryCountersFutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class QueryClient {
    private static final Logger logger = LoggerFactory.getLogger(QueryClient.class);
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException{
        logger.info("Query Client Starting...");
        Map<String,String> argMap = ClientUtils.parseArgs(args);

        final String serverAddress = argMap.get(ClientArgs.SERVER_ADDRESS.getValue());
        final String action = argMap.get(ClientArgs.ACTION.getValue());
        final String outPath = argMap.get(ClientArgs.OUT_PATH.getValue());

        ClientUtils.checkNullArgs(serverAddress, "Server address not specified");
        ClientUtils.checkNullArgs(action, "Action not specified");
        ClientUtils.checkNullArgs(outPath, "Output path not specified");

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        QueryServiceGrpc.QueryServiceFutureStub stub = QueryServiceGrpc.newFutureStub(channel);

        switch (action) {
            case "queryCounters" -> {
                logger.info("Counters Status Query");
                final String sectorName = argMap.get(ClientArgs.SECTOR.getValue());
                ListenableFuture<QueryServiceOuterClass.QueryCounterResponse> listenableFuture;
                if (sectorName!=null) {
                    listenableFuture = stub.queryCounters(QueryServiceOuterClass.QueryCountersRequest.newBuilder().setSectorName(sectorName).build());
                } else
                    listenableFuture = stub.queryCounters(QueryServiceOuterClass.QueryCountersRequest.newBuilder().build());
                Futures.addCallback(listenableFuture, new QueryCountersFutureCallback(logger,latch,outPath), Runnable::run);
            }
            case "checkins" -> {
                logger.info("Checkins Status Query");
                final String sectorName = argMap.get(ClientArgs.SECTOR.getValue());
                final String airlineName = argMap.get(ClientArgs.AIRLINE.getValue());
                ListenableFuture<QueryServiceOuterClass.QueryCheckInsResponse> listenableFuture;
                if (sectorName!=null && airlineName!=null) {
                    listenableFuture = stub.queryCheckIns(QueryServiceOuterClass.QueryCheckInsRequest.newBuilder().setSectorName(sectorName).setAirlineName(airlineName).build());
                } else if (sectorName == null && airlineName != null) {
                    listenableFuture = stub.queryCheckIns(QueryServiceOuterClass.QueryCheckInsRequest.newBuilder().setAirlineName(airlineName).build());
                } else if (sectorName != null && airlineName == null) {
                    listenableFuture = stub.queryCheckIns(QueryServiceOuterClass.QueryCheckInsRequest.newBuilder().setSectorName(sectorName).build());
                } else {
                    listenableFuture = stub.queryCheckIns(QueryServiceOuterClass.QueryCheckInsRequest.newBuilder().build());
                }
                Futures.addCallback(listenableFuture, new QueryCheckInsFutureCallback(logger,latch,outPath), Runnable::run);
            }
        }
        try {
            logger.info("Query Client Stopping...");
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
