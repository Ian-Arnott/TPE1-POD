package ar.edu.itba.pod.grpc.server.models;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Sector {
    private final String name;
    private final Map<Integer, Counter> counterMap;
    private final ConcurrentLinkedQueue<PendingAssignment> pendingAssignments;
    public Sector(String name) {
        this.name = name;
        this.counterMap = new ConcurrentSkipListMap<>();
        this.pendingAssignments = new ConcurrentLinkedQueue<>();
    }

    public String getName() {
        return name;
    }

    public ConcurrentLinkedQueue<PendingAssignment> getPendingAssignments() {
        return pendingAssignments;
    }

    public void addCounters(int lastCounterAdded, int counterAmount) {
        for (int i = 0; i < counterAmount; i++) {
            counterMap.put(lastCounterAdded + i, new Counter(lastCounterAdded + i));
        }
    }

    public synchronized Map<Integer, Counter> getCounterMap() {
        return counterMap;
    }

    public synchronized void resolvePending(int counterAmount, int firstCounter) {
        if (!pendingAssignments.isEmpty()) {
            PendingAssignment front = pendingAssignments.peek();
            PendingAssignment pendingAssignment;
            while (front != null && front.getCountVal().get() <= counterAmount) {
                pendingAssignment = pendingAssignments.poll();
                List<Counter> counters = new ArrayList<>();
                counterAmount -= getCountersFromVal(counters, firstCounter, pendingAssignment.getCountVal().get());
                CounterRange counterRange = new CounterRange(counters,pendingAssignment.getFlights().peek().getAirline(), pendingAssignment.getFlights());
                front = pendingAssignments.peek();
            }
        }
    }

    private int getCountersFromVal(List<Counter> counters, int firstCounter, int countVal) {
        for (int i = firstCounter; i <= firstCounter + countVal -1; i++) {
            counters.add(counterMap.get(i));
        }
        return counters.size();
    }
}
