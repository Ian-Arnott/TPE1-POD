package ar.edu.itba.pod.grpc.server.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

    public synchronized List<PendingAssignment> resolvePending() {
        List<PendingAssignment> list = new ArrayList<>();
        if (!pendingAssignments.isEmpty()) {
            AtomicReference<List<Counter>> counters = new AtomicReference<>();
            AtomicInteger countVal = new AtomicInteger();
            AtomicBoolean pendingsChanged = new AtomicBoolean(false);
            pendingAssignments.forEach(pendingAssignment -> {
                countVal.set(pendingAssignment.getCountVal().get());
                counters.set(getAvailableCounters(countVal.get(), counterMap));
                if (counters.get() != null && counters.get().size() == countVal.get()) {
                    CounterRange counterRange = new CounterRange(counters.get(),pendingAssignment.getAirline(), pendingAssignment.getFlights());
                    for (Flight flight : pendingAssignment.getFlights()) {
                        if (flight.getPending().get())
                            flight.getPending().set(false);
                        flight.getCheckingIn().set(true);
                        flight.setSectorName(name);
                        flight.setCounterRange(counterRange);
                    }
                    list.add(pendingAssignment);
                    // pendingAssignment.notifyAssignedPending(counterRange, name);
                    pendingAssignments.remove(pendingAssignment);
                    pendingsChanged.set(true);
                }
            });
            // if (pendingsChanged.get())
            //     notifyChangedPending();
        }
        return list;
    }

    // private void notifyChangedPending() {
    //     AtomicInteger pos = new AtomicInteger(1);
    //     int pendingAmount = pendingAssignments.size();
    //     pendingAssignments.forEach(pendingAssignment -> {
    //         pendingAssignment.notifyChange(pendingAssignment.getCountVal(),pendingAssignment.getFlights(),pos, name, pendingAmount);
    //     });
    // }

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
