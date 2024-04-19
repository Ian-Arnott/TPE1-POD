package ar.edu.itba.pod.grpc.server.repository;

import ar.edu.itba.pod.grpc.server.exeptions.*;
import ar.edu.itba.pod.grpc.server.models.Counter;
import ar.edu.itba.pod.grpc.server.models.Flight;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.models.requests.CounterRangeAssignmentRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.FreeCounterRangeRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import ar.edu.itba.pod.grpc.server.models.response.CounterRangeAssignmentResponseModel;
import ar.edu.itba.pod.grpc.server.models.response.FreeCounterRangeResponseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

    public void manifest(ManifestRequestModel requestModel) {
        bookingRepository.manifest(requestModel.getBooking(), requestModel.getFlight(), requestModel.getAirline());

    }

    public synchronized CounterRangeAssignmentResponseModel counterRangeAssignment(CounterRangeAssignmentRequestModel requestModel) {
        if (!sectorMap.containsKey(requestModel.getSectorName()))
            throw new SectorDoesNotExistsException(requestModel.getSectorName());

        ConcurrentLinkedQueue<Flight> flightQueue = new ConcurrentLinkedQueue<>();
        AtomicReference<Integer> numberOfPassengers = new AtomicReference<>(0);

        requestModel.getFlights().forEach(flightCode -> {
            if (!bookingRepository.getFlightConcurrentMap().containsKey(flightCode))
                throw new FlightDoesNotExistsException(flightCode);
            if (bookingRepository.flightDoesNotHasBookings(flightCode))
                throw new FlightDoesNotHaveBookingsException(flightCode);
            if (!requestModel.getAirlineName().equals(bookingRepository.getFlightAirline(flightCode)))
                throw new FlightExistsForOtherAirlineException(flightCode);
            if (bookingRepository.flightIsPending(flightCode))
                throw new FlightStatusException(flightCode, " is already pending");
            if (bookingRepository.flightIsCheckingIn(flightCode))
                throw new FlightStatusException(flightCode, " is already checking in");
            if (bookingRepository.flightCheckedIn(flightCode))
                throw new FlightStatusException(flightCode, " has already done check in");
            Flight flight = bookingRepository.getFlight(flightCode);
            flightQueue.add(flight);
            numberOfPassengers.updateAndGet(v -> v + flight.getBookings().size());
        });

        Map<Integer, Counter> counterMap = sectorMap.get(requestModel.getSectorName()).getCounterMap();
        List<Integer> availableCounters = getAvailableCounters(requestModel, counterMap);

        if (availableCounters.isEmpty()) {
            CounterRangeAssignmentResponseModel responseModel = new CounterRangeAssignmentResponseModel(
                    0,0,requestModel.getCountVal(),
                    bookingRepository.getPendingFlights(requestModel.getSectorName()));
            for (Flight flight : flightQueue) {
                flight.getPending().set(true);
                flight.setSectorName(requestModel.getSectorName());
            }
            return responseModel;
        } else {
            for (int available : availableCounters) {
                counterMap.get(available).getIsCheckingIn().set(true);
                counterMap.get(available).setAirline(requestModel.getAirlineName());
                counterMap.get(available).setFlights(flightQueue);
            }
            for (Flight flight : flightQueue) {
                if (flight.getPending().get())
                    flight.getPending().set(false);
                flight.getCheckingIn().set(true);
                flight.setSectorName(requestModel.getSectorName());
            }
            counterMap.get(availableCounters.getFirst()).getIsFirstInRange().set(true);
            counterMap.get(availableCounters.getFirst()).getLastInRange().set(availableCounters.getLast());
            return new CounterRangeAssignmentResponseModel(requestModel.getCountVal(), availableCounters.getLast()
                    , 0,0);
        }
    }

    private static List<Integer> getAvailableCounters(CounterRangeAssignmentRequestModel requestModel, Map<Integer, Counter> counterMap) {
        List<Integer> availableCounters = new ArrayList<>();
        for (Map.Entry<Integer, Counter> entry : counterMap.entrySet()) {
            if (!entry.getValue().getIsCheckingIn().get()) {
                System.out.println(entry);
                availableCounters.add(entry.getKey());
            } else {
                availableCounters.clear();
            }
            if (availableCounters.size() == requestModel.getCountVal()) {
                break;
            }
        }
        return availableCounters;
    }

    public FreeCounterRangeResponseModel freeCounterRange(FreeCounterRangeRequestModel requestModel) {
        if (!sectorMap.containsKey(requestModel.getSectorName()))
            throw new SectorDoesNotExistsException(requestModel.getSectorName());
        Sector sector = sectorMap.get(requestModel.getSectorName());
        if (!sector.getCounterMap().containsKey(requestModel.getFromVal()))
            throw new CountersAreNotAssignedException();
        if (!sector.getCounterMap().get(requestModel.getFromVal()).getIsCheckingIn().get())
            throw new CountersAreNotAssignedException();
        if (!sector.getCounterMap().get(requestModel.getFromVal()).getAirline().equals(requestModel.getAirline()))
            throw new CounterIsCheckingInOtherAirlineException();
        if (!sector.getCounterMap().get(requestModel.getFromVal()).getIsFirstInRange().get())
            throw new CounterIsNotFirstInRangeException();

        FreeCounterRangeResponseModel responseModel = new FreeCounterRangeResponseModel();
        for (int i = requestModel.getFromVal(); i <= sector.getCounterMap().get(requestModel.getFromVal()).getLastInRange().get(); i++) {
            sector.getCounterMap().get(i).getFlights().forEach(flight -> {
                if (!flight.bookingQueueEmpty())
                    throw new StillCheckingInBookingsException();
                flight.getCheckingIn().set(false);
                flight.getCheckedIn().set(true);
                responseModel.getFlights().add(flight.getCode());
            });
            responseModel.getFreedAmount().incrementAndGet();
        }
        return responseModel;
    }
}
