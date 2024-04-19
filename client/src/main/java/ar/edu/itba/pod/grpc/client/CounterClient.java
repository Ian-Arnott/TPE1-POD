package ar.edu.itba.pod.grpc.client;

import airport.CounterAssignmentServiceGrpc;
import airport.CounterAssignmentServiceOuterClass.*;
import ar.edu.itba.pod.grpc.client.utils.ClientArgs;
import ar.edu.itba.pod.grpc.client.utils.callback.CounterRangeAssigmentResponseFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.callback.FreeCounterRangeResponseFutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.*;

public class CounterClient {
    private static final Logger logger = LoggerFactory.getLogger(AdminClient.class);
    private static CountDownLatch latch;

    public static void main(String[] args) {
        logger.info("Counter Client Starting ...");
        Map<String,String> argMap = parseArgs(args);
        final String serverAddress = argMap.get(ClientArgs.SERVER_ADDRESS.getValue());
        final String action = argMap.get(ClientArgs.ACTION.getValue());

        checkNullArgs(serverAddress, "Server Address Not Specified");
        checkNullArgs(action, "Action Not Specified");

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        CounterAssignmentServiceGrpc.CounterAssignmentServiceFutureStub stub =
                CounterAssignmentServiceGrpc.newFutureStub(channel);

        switch (action) {
            case "listSectors" -> {

            }
            case "assignCounters" -> {
                final String countVal = argMap.get(ClientArgs.COUNTER_COUNT.getValue());
                final String sectorName = argMap.get(ClientArgs.SECTOR.getValue());
                final String flights = argMap.get(ClientArgs.FLIGHTS.getValue());
                final String airline = argMap.get(ClientArgs.AIRLINE.getValue());
                checkNullArgs(countVal, "Count Value Not Specified");
                checkNullArgs(sectorName, "Sector Name Not Specified");
                checkNullArgs(flights, "Flights Not Specified");
                checkNullArgs(airline, "Airline Not Specified");
                latch = new CountDownLatch(1);

                List<String> flightList = Arrays.asList(flights.split("\\|"));

                CounterRangeAssigmentRequest request = CounterRangeAssigmentRequest.newBuilder()
                        .setCountVal(Integer.parseInt(countVal))
                        .setSectorName(sectorName)
                        .addAllFlight(flightList)
                        .setAirlineName(airline).build();
                ListenableFuture<CounterRangeAssigmentResponse> listenableFuture = stub.counterRangeAssigment(request);
                Futures.addCallback(listenableFuture, new CounterRangeAssigmentResponseFutureCallback(logger,latch, sectorName, airline, flights), Runnable::run);
            }
            case "freeCounters" -> {
                String fromVal = argMap.get(ClientArgs.COUNTER_FROM.getValue());
                String sectorName = argMap.get(ClientArgs.SECTOR.getValue());
                String airline = argMap.get(ClientArgs.AIRLINE.getValue());

                checkNullArgs(fromVal, "From Value Not Specified");
                checkNullArgs(sectorName, "Sector Name Not Specified");
                checkNullArgs(airline, "Airline Not Specified");
                latch = new CountDownLatch(1);


                int value = Integer.parseInt(fromVal);
                FreeCounterRangeRequest request = FreeCounterRangeRequest.newBuilder()
                        .setSectorName(sectorName)
                        .setFromVal(value)
                        .setAirline(airline).build();
                ListenableFuture<FreeCounterRangeResponse> listenableFuture = stub.freeCounterRange(request);
                Futures.addCallback(listenableFuture, new FreeCounterRangeResponseFutureCallback(logger,latch,sectorName,airline,value),Runnable::run);
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
