package ar.edu.itba.pod.grpc.server.repository;

import ar.edu.itba.pod.grpc.server.models.Sector;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AirportRepository {
    private static AirportRepository instance;
    // mapa de sectores
    private final ConcurrentMap<String, Sector> sectorConcurrentMap;

    private AirportRepository() {
        sectorConcurrentMap = new ConcurrentHashMap<>();
    }

    public static AirportRepository getInstance() {
        if (instance == null) {
            instance = new AirportRepository();
        }
        return instance;
    }
}
