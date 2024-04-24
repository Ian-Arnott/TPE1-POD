package ar.edu.itba.pod.grpc.server.models.response;

import airport.CheckInServiceOuterClass;

import java.util.concurrent.atomic.AtomicInteger;

public class PassengerCheckInResponseModel {
    private final AtomicInteger lastCounter;
    private final AtomicInteger peopleInLine;
    private final String flight;
    private final String airline;

    public PassengerCheckInResponseModel(AtomicInteger lastCounter, AtomicInteger peopleInLine, String flight, String airline) {
        this.lastCounter = lastCounter;
        this.peopleInLine = peopleInLine;
        this.flight = flight;
        this.airline = airline;
    }

    public AtomicInteger getLastCounter() {
        return lastCounter;
    }

    public AtomicInteger getPeopleInLine() {
        return peopleInLine;
    }

    public String getFlight() {
        return flight;
    }

    public String getAirline() {
        return airline;
    }
}
