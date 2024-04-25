package ar.edu.itba.pod.grpc.client;

import airport.CounterAssignmentServiceGrpc;
import airport.CounterAssignmentServiceOuterClass;
import ar.edu.itba.pod.grpc.client.utils.ClientArgs;
import ar.edu.itba.pod.grpc.client.utils.callback.*;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import ar.edu.itba.pod.grpc.client.utils.observers.PerformCheckInStreamObserver;
import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
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

    public static void main(String[] args) throws InterruptedException{
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
                latch = new CountDownLatch(1);

                ListenableFuture<CounterAssignmentServiceOuterClass.ListSectorsResponse> listenableFuture =
                        stub.listSectors(Empty.getDefaultInstance());
                Futures.addCallback(
                        listenableFuture,
                        new ListSectorsResponseFutureCallback(logger, latch),
                        Runnable::run
                );
            }
            case "listCounters" -> {
                final String counterFrom = argMap.get(ClientArgs.COUNTER_FROM.getValue());
                final String counterTo = argMap.get(ClientArgs.COUNTER_TO.getValue());
                final String sectorName = argMap.get(ClientArgs.SECTOR.getValue());
                checkNullArgs(counterFrom, "Counter Form Value Not Specified");
                checkNullArgs(counterTo, "Counter To Value Not Specified");
                checkNullArgs(sectorName, "Sector Name Not Specified");
                int counterFromInt = Integer.parseInt(counterFrom);
                int counterToInt = Integer.parseInt(counterTo);


                latch = new CountDownLatch(1);

                CounterAssignmentServiceOuterClass.ListCountersRequest request = CounterAssignmentServiceOuterClass.ListCountersRequest.newBuilder()
                        .setSectorName(sectorName)
                        .setFromVal(counterFromInt)
                        .setToVal(counterToInt)
                        .build();

                ListenableFuture<CounterAssignmentServiceOuterClass.ListCountersResponse> listenableFuture = stub.listCounters(request);
                Futures.addCallback(
                        listenableFuture,
                        new ListCountersResponseFutureCallback(logger, latch, sectorName, counterFromInt, counterToInt),
                        Runnable::run
                );
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

                CounterAssignmentServiceOuterClass.CounterRangeAssignmentRequest request = CounterAssignmentServiceOuterClass.CounterRangeAssignmentRequest.newBuilder()
                        .setCountVal(Integer.parseInt(countVal))
                        .setSectorName(sectorName)
                        .addAllFlight(flightList)
                        .setAirlineName(airline).build();
                ListenableFuture<CounterAssignmentServiceOuterClass.CounterRangeAssignmentResponse> listenableFuture = stub.counterRangeAssignment(request);
                Futures.addCallback(listenableFuture, new CounterRangeAssignmentResponseFutureCallback(logger,latch, sectorName, airline, flights), Runnable::run);
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
                CounterAssignmentServiceOuterClass.FreeCounterRangeRequest request = CounterAssignmentServiceOuterClass.FreeCounterRangeRequest.newBuilder()
                        .setSectorName(sectorName)
                        .setFromVal(value)
                        .setAirline(airline).build();
                ListenableFuture<CounterAssignmentServiceOuterClass.FreeCounterRangeResponse> listenableFuture = stub.freeCounterRange(request);
                Futures.addCallback(listenableFuture, new FreeCounterRangeResponseFutureCallback(logger,latch,sectorName, value),Runnable::run);
            }
            case "checkinCounters" -> {
                String fromVal = argMap.get(ClientArgs.COUNTER_FROM.getValue());
                String sectorName = argMap.get(ClientArgs.SECTOR.getValue());
                String airline = argMap.get(ClientArgs.AIRLINE.getValue());

                checkNullArgs(fromVal, "From Value Not Specified");
                checkNullArgs(sectorName, "Sector Name Not Specified");
                checkNullArgs(airline, "Airline Not Specified");

                latch = new CountDownLatch(1);
                int value = Integer.parseInt(fromVal);
                CounterAssignmentServiceOuterClass.PerformCounterCheckInRequest request = CounterAssignmentServiceOuterClass.PerformCounterCheckInRequest.newBuilder()
                        .setSectorName(sectorName)
                        .setAirlineName(airline)
                        .setFromVal(value)
                        .build();
                CounterAssignmentServiceGrpc.CounterAssignmentServiceStub serviceStub = CounterAssignmentServiceGrpc.newStub(channel);
                StreamObserver<CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse> observer = new PerformCheckInStreamObserver(latch);
                serviceStub.performCounterCheckIn(request,observer);
                latch.await();
            }
            case "listPendingAssignments" -> {
                String sectorName = argMap.get(ClientArgs.SECTOR.getValue());

                latch = new CountDownLatch(1);

                ListenableFuture<CounterAssignmentServiceOuterClass.ListPendingAssignmentsResponse> listenableFuture =
                        stub.listPendingAssignments(StringValue.of(sectorName));
                Futures.addCallback(
                        listenableFuture,
                        new ListPendingAssignmentsResponseFutureCallback(logger, latch),
                        Runnable::run
                );
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
