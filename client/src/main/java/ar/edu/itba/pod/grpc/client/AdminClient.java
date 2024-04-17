package ar.edu.itba.pod.grpc.client;

import airport.AdminAirportServiceGrpc;
import ar.edu.itba.pod.grpc.client.utils.ClientArgs;
import ar.edu.itba.pod.grpc.client.utils.callback.AddCountersResponseFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.callback.AddSectorResponseFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.callback.BoolValueFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.callback.ManifestResponseFutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import airport.AdminAirportServiceOuterClass.*;

import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.*;

public class AdminClient {
    private static final Logger logger = LoggerFactory.getLogger(AdminClient.class);
    private static CountDownLatch latch;

    public static void main(String[] args) throws InterruptedException {
        logger.info("Admin Client Starting ...");
        Map<String,String> argMap = parseArgs(args);
        final String serverAddress = argMap.get(ClientArgs.SERVER_ADDRESS.getValue());
        final String action = argMap.get(ClientArgs.ACTION.getValue());

        checkNullArgs(serverAddress, "Server Address Not Specified");
        checkNullArgs(action, "Action Not Specified");

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        AdminAirportServiceGrpc.AdminAirportServiceFutureStub stub = AdminAirportServiceGrpc
                .newFutureStub(channel);

        final AtomicInteger couldAdd = new AtomicInteger(0);
        final AtomicInteger couldNotAdd = new AtomicInteger(0);

        switch (action) {
            case "addSector" -> {
                final String sectorName = argMap.get(ClientArgs.SECTOR.getValue());
                checkNullArgs(sectorName, "Sector Name Not Specified");
                latch = new CountDownLatch(1);
                AddSectorRequest addSectorRequest = AddSectorRequest.newBuilder()
                        .setSectorName(sectorName)
                        .build();
                ListenableFuture<BoolValue> listenableFuture = stub.addSector(addSectorRequest);
                Futures.addCallback(listenableFuture, new AddSectorResponseFutureCallback(logger, latch, sectorName), Runnable::run);
            }
            case "addCounters" -> {
                final String sectorName = argMap.get(ClientArgs.SECTOR.getValue());
                final String countersArg = argMap.get(ClientArgs.COUNTERS.getValue());
                checkNullArgs(sectorName, "Sector Name Not Specified");
                checkNullArgs(countersArg, "Counters Amount Not Specified");
                final int counters = Integer.parseInt(countersArg);
                latch = new CountDownLatch(1);
                AddCountersRequest countersRequest = AddCountersRequest.newBuilder()
                        .setSectorName(sectorName)
                        .setCounterCount(counters)
                        .build();
                ListenableFuture<AddCountersResponse> listenableFuture = stub.addCounters(countersRequest);
                Futures.addCallback(listenableFuture, new AddCountersResponseFutureCallback(logger, latch, sectorName, counters), Runnable::run);
            }
            case "manifest" -> {
                final String inPath = argMap.get(ClientArgs.IN_PATH.getValue());
                checkNullArgs(inPath, "Input Path Not Specified");

                List<String[]> csvData = getCSVData(inPath);
                latch = new CountDownLatch(csvData.size());
                for (String[] data : csvData) {
                    ListenableFuture<ManifestResponse> listenableFuture;
                    ManifestRequest manifestRequest = ManifestRequest.newBuilder().setBooking(data[0])
                            .setFlight(data[1]).setAirline(data[2]).build();
                    listenableFuture = stub.manifest(manifestRequest);
                    Futures.addCallback(listenableFuture,
                            new ManifestResponseFutureCallback(logger,latch,data[0],data[1],data[2]), Runnable::run);
                }
            }
        }

        try {
            logger.info("Waiting for response...");
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error(e.getMessage());
        }
    }
}
