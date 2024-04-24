package ar.edu.itba.pod.grpc.server.models;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Sector {
    private final String name;
    private final Map<Integer, Counter> counterMap;
    private final Map<String, ConcurrentLinkedQueue<Flight>> pendingFlightMap;
    public Sector(String name) {
        this.name = name;
        this.counterMap = new ConcurrentSkipListMap<>();
        this.pendingFlightMap = new ConcurrentSkipListMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, ConcurrentLinkedQueue<Flight>> getPendingFlightMap() {
        return pendingFlightMap;
    }

    public void addCounters(int lastCounterAdded, int counterAmount) {
        for (int i = 0; i < counterAmount; i++) {
            counterMap.put(lastCounterAdded + i, new Counter(lastCounterAdded + i));
        }
    }

    public synchronized Map<Integer, Counter> getCounterMap() {
        return counterMap;
    }

    public void resolvePending(int counterAmount, int firstCounter) {
        for (Map.Entry<String, ConcurrentLinkedQueue<Flight>> entry : pendingFlightMap.entrySet()) {
            ConcurrentLinkedQueue<Flight> queue = entry.getValue();
            if (!queue.isEmpty()) {
                for (int i = 0; i < counterAmount; i++) {
                    Counter counter = counterMap.get(firstCounter + i);
                    if (i == 0)
                        counter.getIsFirstInRange().set(true);
                    counter.getIsFirstInRange().set(false);
                    counter.getLastInRange().set(firstCounter - 1 + counterAmount);
                    counter.getFlights().addAll(queue);
                }
                queue.clear();
                break;
            }
        }
    }
}
