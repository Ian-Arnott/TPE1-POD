package ar.edu.itba.pod.grpc.server.repository;

import airport.AdminAirportServiceOuterClass;
import ar.edu.itba.pod.grpc.server.exeptions.SectorMapIsEmptyException;
import counter.CounterAssignmentServiceOuterClass;
import ar.edu.itba.pod.grpc.server.exeptions.NonPositiveCounterException;
import ar.edu.itba.pod.grpc.server.exeptions.SectorAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exeptions.SectorDoesNotExistsException;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import com.google.protobuf.Empty;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportRepository {
    private static AirportRepository instance;
    private static final BookingRepository bookingRepository = BookingRepository.getInstance();
    private final ConcurrentMap<String, Sector> sectorMap;
    private AtomicInteger lastCounterAdded;

    private AirportRepository() {
        sectorMap = new ConcurrentHashMap<>();
        lastCounterAdded = new AtomicInteger(1);
    }

    public synchronized static AirportRepository getInstance() {
        if (instance == null) {
            instance = new AirportRepository();
        }
        return instance;
    }

    public boolean addSector(String sectorName) {
        if (sectorMap.get(sectorName) != null) {
            throw new SectorAlreadyExistsException(sectorName);
        }
        Sector sector = sectorMap.put(sectorName, new Sector(sectorName));
        return sector == null;
    }


    public int addCountersToSector(String sectorName, int counterAmount) {
        if (sectorMap.get(sectorName) == null) {
            throw new SectorDoesNotExistsException(sectorName);
        }
        if (counterAmount <= 0) {
            throw new NonPositiveCounterException(counterAmount);
        }
        Sector sector = sectorMap.get(sectorName);

        lastCounterAdded = sector.addCounters(lastCounterAdded, counterAmount);
        return lastCounterAdded.get();
    }

    public AdminAirportServiceOuterClass.ManifestResponse manifest(ManifestRequestModel requestModel) {
        return bookingRepository.manifest(requestModel.getBooking(),requestModel.getFlight(),requestModel.getAirline());
    }

    public Map<String, Set<Integer>> listSectors() {
        if (sectorMap.isEmpty()) throw new SectorMapIsEmptyException();

        Map<String, Set<Integer>> res = new HashMap<>();

        sectorMap.forEach((sectorName, sector) -> res.put(sectorName, sector.getCounterMap().keySet()));

        return res;
    }
}
