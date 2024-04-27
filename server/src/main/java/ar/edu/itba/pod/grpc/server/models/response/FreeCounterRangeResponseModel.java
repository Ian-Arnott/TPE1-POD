package ar.edu.itba.pod.grpc.server.models.response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class FreeCounterRangeResponseModel {
    private final AtomicInteger freedAmount;
    private final ConcurrentLinkedQueue<String> flights;

    public FreeCounterRangeResponseModel() {
        freedAmount = new AtomicInteger(0);
        flights = new ConcurrentLinkedQueue<>();
    }

    public AtomicInteger getFreedAmount() {
        return freedAmount;
    }

    public ConcurrentLinkedQueue<String> getFlights() {
        return flights;
    }
}
