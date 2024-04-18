package ar.edu.itba.pod.grpc.server.repository;

import airport.AdminAirportServiceOuterClass;
import counter.CounterAssignmentServiceOuterClass;
import ar.edu.itba.pod.grpc.server.exeptions.NonPositiveCounterException;
import ar.edu.itba.pod.grpc.server.exeptions.SectorAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exeptions.SectorDoesNotExistsException;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import com.google.protobuf.Empty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public CounterAssignmentServiceOuterClass.ListSectorsResponse listSectors(Empty request) {
        if (sectorMap.isEmpty()) {
            // TODO: Implement
        }

        Map<String, List<int[]>> response = new HashMap<>();

        sectorMap.forEach((sectorName, sector) -> response.put(sectorName, sector.getCounterList()));

        return CounterAssignmentServiceOuterClass.ListSectorsResponse.newBuilder().setSectorName("A").setCounterRangeStart(1, 1).setCounterRangeSize(1, 3).build();
    }
}
