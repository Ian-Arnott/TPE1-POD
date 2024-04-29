package ar.edu.itba.pod.grpc.server.models.response;

import ar.edu.itba.pod.grpc.server.models.PendingAssignment;

import java.util.List;

public record AddCountersResponseModel(int lastCounterAdded, List<PendingAssignment.PendingAssignmentRecord> pendingAssignmentList) {
}
