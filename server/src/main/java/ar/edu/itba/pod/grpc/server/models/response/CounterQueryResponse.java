package ar.edu.itba.pod.grpc.server.models.response;

import ar.edu.itba.pod.grpc.server.models.Counter;

import java.util.List;

public record CounterQueryResponse (String sectorName, List<Counter.CounterRecord> counterRecords) {
}
