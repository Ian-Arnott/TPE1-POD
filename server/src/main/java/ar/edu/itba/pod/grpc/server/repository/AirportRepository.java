package ar.edu.itba.pod.grpc.server.repository;

import ar.edu.itba.pod.grpc.server.models.Sector;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportRepository {
    private static AirportRepository instance;
    // mapa de sectores
    private final ConcurrentMap<String, Sector> sectorConcurrentMap;
    private AtomicInteger lastCounterAdded;

    private AirportRepository() {
        sectorConcurrentMap = new ConcurrentHashMap<>();
        lastCounterAdded = new AtomicInteger(0);
    }

    public static AirportRepository getInstance() {
        if (instance == null) {
            instance = new AirportRepository();
        }
        return instance;
    }
}
