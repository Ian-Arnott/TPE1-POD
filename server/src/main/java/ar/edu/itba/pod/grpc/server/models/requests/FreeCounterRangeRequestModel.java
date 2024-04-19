package ar.edu.itba.pod.grpc.server.models.requests;

import airport.CounterAssignmentServiceOuterClass;

public class FreeCounterRangeRequestModel {
    private final String sectorName;
    private final int fromVal;
    private final String airline;


    public FreeCounterRangeRequestModel(String sectorName, int fromVal, String airline) {
        this.sectorName = sectorName;
        this.fromVal = fromVal;
        this.airline = airline;
    }

    public String getSectorName() {
        return sectorName;
    }

    public int getFromVal() {
        return fromVal;
    }

    public String getAirline() {
        return airline;
    }

    public static FreeCounterRangeRequestModel fromFreeCounterRequest(CounterAssignmentServiceOuterClass.FreeCounterRangeRequest request) {
        return new FreeCounterRangeRequestModel(
                request.getSectorName(),
                request.getFromVal(),
                request.getAirline());
    }
}
