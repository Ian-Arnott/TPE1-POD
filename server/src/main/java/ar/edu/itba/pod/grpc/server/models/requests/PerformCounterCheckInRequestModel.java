package ar.edu.itba.pod.grpc.server.models.requests;

import airport.CounterAssignmentServiceOuterClass;

import java.util.concurrent.atomic.AtomicInteger;

public class PerformCounterCheckInRequestModel {
    private final String sectorName;
    private final AtomicInteger fromVal;
    private final String airlineName;

    public PerformCounterCheckInRequestModel(String sectorName, AtomicInteger fromVal, String airlineName) {
        this.sectorName = sectorName;
        this.fromVal = fromVal;
        this.airlineName = airlineName;
    }

    public String getSectorName() {
        return sectorName;
    }

    public AtomicInteger getFromVal() {
        return fromVal;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public static PerformCounterCheckInRequestModel fromPerformCounterCheckInRequest(CounterAssignmentServiceOuterClass.PerformCounterCheckInRequest request) {
        return new PerformCounterCheckInRequestModel(request.getSectorName(), new AtomicInteger(request.getFromVal()), request.getAirlineName());
    }
}
