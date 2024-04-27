package ar.edu.itba.pod.grpc.server.models.response;

import ar.edu.itba.pod.grpc.server.models.Counter;

import java.util.List;

public class FetchCounterResponseModel {
    String flightCode;
    String airlineName;
    List<Integer> counters;
    String sectorName;
    int peopleAmountInLine;

    public String getFlightCode() {
        return flightCode;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public List<Integer> getCounters() {
        return counters;
    }

    public String getSectorName() {
        return sectorName;
    }

    public int getPeopleAmountInLine() {
        return peopleAmountInLine;
    }

    public FetchCounterResponseModel(String flightCode, String airlineName, List<Integer> counters, String sectorName, int peopleAmountInLine) {
        this.flightCode = flightCode;
        this.airlineName = airlineName;
        this.counters = counters;
        this.sectorName = sectorName;
        this.peopleAmountInLine = peopleAmountInLine;
    }
}
