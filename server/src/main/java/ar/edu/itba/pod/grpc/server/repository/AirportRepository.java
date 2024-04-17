package ar.edu.itba.pod.grpc.server.repository;

import airport.AdminAirportServiceOuterClass;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AirportRepository {
    private static AirportRepository instance;
    private static final BookingRepository bookingRepository = BookingRepository.getInstance();
    private final ConcurrentMap<String, Sector> sectorConcurrentMap;
    private AtomicInteger lastCounterAdded;

    private AirportRepository() {
        sectorConcurrentMap = new ConcurrentHashMap<>();
        lastCounterAdded = new AtomicInteger(0);
    }

    public synchronized static AirportRepository getInstance() {
        if (instance == null) {
            instance = new AirportRepository();
        }
        return instance;
    }

    public AdminAirportServiceOuterClass.ManifestResponse manifest(ManifestRequestModel requestModel) {
        return bookingRepository.manifest(requestModel.getBooking(),requestModel.getFlight(),requestModel.getAirline());
    }
}
