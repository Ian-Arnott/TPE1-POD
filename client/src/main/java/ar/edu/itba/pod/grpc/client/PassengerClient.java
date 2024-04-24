package ar.edu.itba.pod.grpc.client;

import airport.AdminAirportServiceGrpc;
import airport.AdminAirportServiceOuterClass.AddCountersRequest;
import airport.AdminAirportServiceOuterClass.AddCountersResponse;
import airport.AdminAirportServiceOuterClass.AddSectorRequest;
import airport.AdminAirportServiceOuterClass.ManifestRequest;
import airport.CheckInServiceGrpc;
import airport.CheckInServiceOuterClass;
import ar.edu.itba.pod.grpc.client.utils.ClientArgs;
import ar.edu.itba.pod.grpc.client.utils.callback.AddCountersResponseFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.callback.AddSectorResponseFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.callback.ManifestResponseFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.callback.PassengerCheckinFutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.*;

public class PassengerClient {
    private static final Logger logger = LoggerFactory.getLogger(PassengerClient.class);
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

        CheckInServiceGrpc.CheckInServiceFutureStub stub = CheckInServiceGrpc
                .newFutureStub(channel);


        switch (action) {
            case "fetchCounter" -> {
            }
            case "passengerCheckin" -> {
                String counterNumber = argMap.get(ClientArgs.COUNTER.getValue());
                String sectorName = argMap.get(ClientArgs.SECTOR.getValue());
                String booking = argMap.get(ClientArgs.BOOKING.getValue());

                checkNullArgs(counterNumber, "Counter Number Not Specified");
                checkNullArgs(sectorName, "Sector Name Not Specified");
                checkNullArgs(booking, "Booking Not Specified");

                latch = new CountDownLatch(1);

                int value = Integer.parseInt(counterNumber);
                CheckInServiceOuterClass.PassengerCheckInRequest request = CheckInServiceOuterClass.PassengerCheckInRequest.newBuilder()
                        .setBooking(booking).setSectorName(sectorName).setFirstCounter(value).build();
                ListenableFuture<CheckInServiceOuterClass.PassengerCheckInResponse> listenableFuture = stub.passengerCheckIn(request);
                Futures.addCallback(listenableFuture, new PassengerCheckinFutureCallback(logger, latch, booking,value, sectorName), Runnable::run);
            }
            case "passengerStatus" -> {
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
