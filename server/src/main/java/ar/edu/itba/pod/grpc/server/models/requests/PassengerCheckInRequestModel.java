package ar.edu.itba.pod.grpc.server.models.requests;

import airport.CheckInServiceOuterClass;

import java.util.concurrent.atomic.AtomicInteger;

public class PassengerCheckInRequestModel {
    private final AtomicInteger firstCounter;
    private final String sectorName;
    private final String booking;

    public PassengerCheckInRequestModel(AtomicInteger firstCounter, String sectorName, String booking) {
        this.firstCounter = firstCounter;
        this.sectorName = sectorName;
        this.booking = booking;
    }

    public static PassengerCheckInRequestModel fromCheckInRequest(CheckInServiceOuterClass.PassengerCheckInRequest request) {
        return new PassengerCheckInRequestModel(new AtomicInteger(request.getFirstCounter()),
                request.getSectorName(), request.getBooking());
    }

    public AtomicInteger getFirstCounter() {
        return firstCounter;
    }

    public String getSectorName() {
        return sectorName;
    }

    public String getBooking() {
        return booking;
    }
}
