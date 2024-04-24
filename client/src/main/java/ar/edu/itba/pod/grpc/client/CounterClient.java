package ar.edu.itba.pod.grpc.client;

import airport.CounterAssignmentServiceGrpc;
import airport.CounterAssignmentServiceOuterClass;
import ar.edu.itba.pod.grpc.client.utils.ClientArgs;
import ar.edu.itba.pod.grpc.client.utils.callback.ListSectorsResponseFutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import ar.edu.itba.pod.grpc.client.utils.callback.CounterRangeAssigmentResponseFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.callback.FreeCounterRangeResponseFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.observers.PerformCheckInStreamObserver;
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

//                for (String[] data : csvData) {
//                    ListenableFuture<AdminAirportServiceOuterClass.ManifestResponse> listenableFuture;
//                    AdminAirportServiceOuterClass.ManifestRequest manifestRequest = AdminAirportServiceOuterClass.ManifestRequest.newBuilder().setBooking(data[0])
//                            .setFlight(data[1]).setAirline(data[2]).build();
//                    listenableFuture = stub.manifest(manifestRequest);
//                    Futures.addCallback(listenableFuture,
//                            new ManifestResponseFutureCallback(logger,latch,data[0],data[1],data[2]), Runnable::run);
//                }
                ListenableFuture<CounterAssignmentServiceOuterClass.ListSectorsResponse> listenableFuture =
                        stub.listSectors(Empty.getDefaultInstance());
                Futures.addCallback(
                        listenableFuture,
                        new ListSectorsResponseFutureCallback(logger, latch),
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

                CounterAssignmentServiceOuterClass.CounterRangeAssigmentRequest request = CounterAssignmentServiceOuterClass.CounterRangeAssigmentRequest.newBuilder()
                        .setCountVal(Integer.parseInt(countVal))
                        .setSectorName(sectorName)
                        .addAllFlight(flightList)
                        .setAirlineName(airline).build();
                ListenableFuture<CounterAssignmentServiceOuterClass.CounterRangeAssigmentResponse> listenableFuture = stub.counterRangeAssigment(request);
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
