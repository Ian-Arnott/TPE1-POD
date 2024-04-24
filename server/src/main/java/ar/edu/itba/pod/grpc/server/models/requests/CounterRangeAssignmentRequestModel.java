package ar.edu.itba.pod.grpc.server.models.requests;

import airport.CounterAssignmentServiceOuterClass;

import java.util.List;

public class CounterRangeAssignmentRequestModel {
    private final Integer countVal;
    private final String sectorName;
    private final List<String> flights;
    private final String airlineName;

    public CounterRangeAssignmentRequestModel(Integer countVal, String sectorName, List<String> flights, String airlineName) {
        if (countVal == null || countVal < 0)
            throw new IllegalArgumentException("countVal cannot be null or negative");
        if (sectorName == null || sectorName.isEmpty())
            throw new IllegalArgumentException("sectorName cannot be null or empty");
        if (flights == null || flights.isEmpty())
            throw new IllegalArgumentException("flights cannot be null or empty");
        if (airlineName == null || airlineName.isEmpty())
            throw new IllegalArgumentException("airline cannot be null or empty");
        this.countVal = countVal;
        this.sectorName = sectorName;
        this.flights = flights;
        this.airlineName = airlineName;

    }

    public String getAirlineName() {
        return airlineName;
    }

    public String getSectorName() {
        return sectorName;
    }

    public Integer getCountVal() {
        return countVal;
    }

    public List<String> getFlights() {
        return flights;
    }

    public static CounterRangeAssignmentRequestModel fromCounterRangAssignmentRequest(
            final CounterAssignmentServiceOuterClass.CounterRangeAssigmentRequest request) {
        return new CounterRangeAssignmentRequestModel(
                request.getCountVal(),
                request.getSectorName(),
                request.getFlightList().stream().toList(),
                request.getAirlineName()
        );
    }
}
