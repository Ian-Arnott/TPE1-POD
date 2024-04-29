package ar.edu.itba.pod.grpc.server.models.response;

import ar.edu.itba.pod.grpc.server.models.PendingAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class FreeCounterRangeResponseModel {
    private final AtomicInteger freedAmount;
    private final ConcurrentLinkedQueue<String> flights;
    private List<PendingAssignment.PendingAssignmentRecord> pendingAssignments;

    public FreeCounterRangeResponseModel() {
        freedAmount = new AtomicInteger(0);
        flights = new ConcurrentLinkedQueue<>();
        pendingAssignments = new ArrayList<>();
    }

    public AtomicInteger getFreedAmount() {
        return freedAmount;
    }

    public ConcurrentLinkedQueue<String> getFlights() {
        return flights;
    }

    public List<PendingAssignment.PendingAssignmentRecord> getPendingAssignments() {
        return pendingAssignments;
    }

    public void setPendingAssignments(List<PendingAssignment.PendingAssignmentRecord> pendingAssignments) {
        this.pendingAssignments = pendingAssignments;
    }
}
