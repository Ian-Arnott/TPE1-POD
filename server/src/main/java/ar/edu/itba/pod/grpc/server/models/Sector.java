package ar.edu.itba.pod.grpc.server.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

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

    public synchronized void resolvePending() {
        if (!pendingAssignments.isEmpty()) {
            PendingAssignment front = pendingAssignments.peek();
            PendingAssignment pendingAssignment;
            List<Counter> counters = null;

            while (front != null) {
                pendingAssignment = pendingAssignments.poll();
                int countVal = pendingAssignment.getCountVal().get();
                counters = getAvailableCounters(countVal, this.counterMap);
                if (counters == null || counters.size() < countVal) {
                    break;
                }
                CounterRange counterRange = new CounterRange(counters,pendingAssignment.getFlights().peek().getAirline(), pendingAssignment.getFlights());
                for (Flight flight : pendingAssignment.getFlights()) {
                    if (flight.getPending().get())
                        flight.getPending().set(false);
                    flight.getCheckingIn().set(true);
                }
                front = pendingAssignments.peek();
            }
        }
    }

    public List<Counter> getAvailableCounters(int countVal, Map<Integer, Counter> counterMap) {
        List<Counter> availableCounters = new ArrayList<>();
        for (Map.Entry<Integer, Counter> entry : counterMap.entrySet()) {
            if (!entry.getValue().getIsCheckingIn().get()) {
                availableCounters.add(entry.getValue());
            } else {
                availableCounters.clear();
            }
            if (availableCounters.size() == countVal) {
                break;
            }
        }
        return availableCounters;
    }
}
