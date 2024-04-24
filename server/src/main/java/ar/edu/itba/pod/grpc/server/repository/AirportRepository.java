package ar.edu.itba.pod.grpc.server.repository;

import airport.AdminAirportServiceOuterClass;
import ar.edu.itba.pod.grpc.server.exeptions.SectorMapIsEmptyException;
import airport.CounterAssignmentServiceOuterClass;
import ar.edu.itba.pod.grpc.server.exeptions.NonPositiveCounterException;
import ar.edu.itba.pod.grpc.server.exeptions.SectorAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exeptions.SectorDoesNotExistsException;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import com.google.protobuf.Empty;

import java.util.*;
import ar.edu.itba.pod.grpc.server.exeptions.*;
import ar.edu.itba.pod.grpc.server.models.*;
import ar.edu.itba.pod.grpc.server.models.Booking;
import ar.edu.itba.pod.grpc.server.models.Counter;
import ar.edu.itba.pod.grpc.server.models.Flight;
import ar.edu.itba.pod.grpc.server.models.requests.*;
import ar.edu.itba.pod.grpc.server.models.response.CounterRangeAssignmentResponseModel;
import ar.edu.itba.pod.grpc.server.models.response.FreeCounterRangeResponseModel;
import ar.edu.itba.pod.grpc.server.models.response.PassengerCheckInResponseModel;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AirportRepository {
    private static AirportRepository instance;
    private final ConcurrentMap<String, Airline> airlines;
    private final ConcurrentMap<String, Flight> flightConcurrentMap;
    private final ConcurrentMap<String, Booking> bookingConcurrentMap;
    private final ConcurrentMap<String, Sector> sectorMap;
    private final String counterLock = "counter lock";

    private int lastCounterAdded;

    private AirportRepository() {
        sectorMap = new ConcurrentHashMap<>();

        this.airlines = new ConcurrentHashMap<>();
        this.flightConcurrentMap = new ConcurrentHashMap<>();
        this.bookingConcurrentMap = new ConcurrentHashMap<>();

        lastCounterAdded = 1;
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

        synchronized (counterLock) {
            sector.addCounters(lastCounterAdded, counterAmount);
            lastCounterAdded += counterAmount;
        }

        sector.resolvePending(counterAmount, lastCounterAdded + 1 - counterAmount);
        return lastCounterAdded;
    }

    public void manifest(ManifestRequestModel requestModel) {
        String booking = requestModel.getBooking();
        String flight = requestModel.getFlight();
        String airline = requestModel.getAirline();

        if (this.bookingConcurrentMap.containsKey(booking)) {
            throw new BookingAlreadyExistsException(booking, flight, airline);
        }

        Airline existingAirline = airlines.getOrDefault(airline, new Airline(airline));
        Flight existingFlight = flightConcurrentMap.get(flight);

        if (existingFlight != null) {
            if (!existingFlight.getAirline().getName().equals(airline)) {
                throw new FlightExistsForOtherAirlineException(flight);
            }
        } else {
            existingFlight = new Flight(flight, existingAirline);
            existingAirline.getFlights().put(flight, existingFlight);
        }
        Booking newBooking = new Booking(booking, existingFlight);
        existingAirline.getBookings().put(booking, newBooking);

        existingFlight.getBookings().put(booking, newBooking);

        flightConcurrentMap.putIfAbsent(flight, existingFlight);
        airlines.putIfAbsent(airline, existingAirline);
        bookingConcurrentMap.put(booking, newBooking);

    }

    public boolean flightDoesNotHasBookings(String flight) {
        return flightConcurrentMap.get(flight).getBookings().isEmpty();
    }
    public String getFlightAirline(String flight) {
        return flightConcurrentMap.get(flight).getAirline().getName();
    }

    public boolean flightIsPending(String flight) {
        return flightConcurrentMap.get(flight).getPending().get();
    }
    public boolean flightIsCheckingIn(String flight) {
        return flightConcurrentMap.get(flight).getCheckingIn().get();
    }

    public boolean flightCheckedIn(String flight) {
        return flightConcurrentMap.get(flight).getCheckedIn().get();
    }

    public Map<String, Set<Integer>> listSectors() {
        if (sectorMap.isEmpty()) throw new SectorMapIsEmptyException();

        Map<String, Set<Integer>> res = new HashMap<>();

        sectorMap.forEach((sectorName, sector) -> res.put(sectorName, sector.getCounterMap().keySet()));

        return res;
    }

    public synchronized CounterRangeAssignmentResponseModel counterRangeAssignment(CounterRangeAssignmentRequestModel requestModel) {
        if (!sectorMap.containsKey(requestModel.getSectorName()))
            throw new SectorDoesNotExistsException(requestModel.getSectorName());

        ConcurrentLinkedQueue<Flight> flightQueue = new ConcurrentLinkedQueue<>();
        AtomicReference<Integer> numberOfPassengers = new AtomicReference<>(0);

        requestModel.getFlights().forEach(flightCode -> {
            if (!flightConcurrentMap.containsKey(flightCode))
                throw new FlightDoesNotExistsException(flightCode);
            if (flightDoesNotHasBookings(flightCode))
                throw new FlightDoesNotHaveBookingsException(flightCode);
            if (!requestModel.getAirlineName().equals(getFlightAirline(flightCode)))
                throw new FlightExistsForOtherAirlineException(flightCode);
            if (flightIsPending(flightCode))
                throw new FlightStatusException(flightCode, " is already pending");
            if (flightIsCheckingIn(flightCode))
                throw new FlightStatusException(flightCode, " is already checking in");
            if (flightCheckedIn(flightCode))
                throw new FlightStatusException(flightCode, " has already done check in");
            Flight flight = flightConcurrentMap.get(flightCode);
            flightQueue.add(flight);
            numberOfPassengers.updateAndGet(v -> v + flight.getBookings().size());
        });

        Map<Integer, Counter> counterMap = sectorMap.get(requestModel.getSectorName()).getCounterMap();
        List<Integer> availableCounters = getAvailableCounters(requestModel, counterMap);

        if (availableCounters.isEmpty()) {
            CounterRangeAssignmentResponseModel responseModel = new CounterRangeAssignmentResponseModel(
                    0,0,requestModel.getCountVal(),
                    sectorMap.get(requestModel.getSectorName()).getPendingFlightMap().get(requestModel.getAirlineName()).size());
            for (Flight flight : flightQueue) {
                flight.getPending().set(true);
                flight.setSectorName(requestModel.getSectorName());
                sectorMap.get(requestModel.getSectorName()).getPendingFlightMap().get(requestModel.getAirlineName()).add(flight);
            }
            return responseModel;
        } else {
            for (int available : availableCounters) {
                counterMap.get(available).getIsCheckingIn().set(true);
                counterMap.get(available).setAirline(airlines.get(requestModel.getAirlineName()));
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

    public List<Counter> getCounters(String sectorName, int from, int to){
        Sector sector = sectorMap.get(sectorName);
        Map<Integer, Counter> counterMap = sector.getCounterMap();
        List<Counter> counters = new ArrayList<>();
        for (int i = from; i <= to ; i++) {
            counters.add(counterMap.get(i));
        }
        return counters;
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
        if (!sector.getCounterMap().get(requestModel.getFromVal()).getAirline().getName().equals(requestModel.getAirline()))
            throw new CounterIsCheckingInOtherAirlineException();
        if (!sector.getCounterMap().get(requestModel.getFromVal()).getIsFirstInRange().get())
            throw new CounterIsNotFirstInRangeException();

        FreeCounterRangeResponseModel responseModel = new FreeCounterRangeResponseModel();
        int lastInRange = sector.getCounterMap().get(requestModel.getFromVal()).getLastInRange().get();
        for (int i = requestModel.getFromVal(); i <= lastInRange; i++) {
            Counter counter = sector.getCounterMap().get(i);
            if (!counter.bookingQueueEmpty())
                throw new StillCheckingInBookingsException();
            counter.getFlights().forEach(flight -> {
                flight.getCheckingIn().set(false);
                flight.getCheckedIn().set(true);
                responseModel.getFlights().add(flight.getCode());
            });
            counter.getIsCheckingIn().set(false);
            counter.getIsFirstInRange().set(false);
            counter.getLastInRange().set(0);
            responseModel.getFreedAmount().incrementAndGet();
        }
        sector.resolvePending(responseModel.getFreedAmount().get(), requestModel.getFromVal());
        return responseModel;
    }

    public synchronized void performCounterCheckIn(PerformCounterCheckInRequestModel requestModel, StreamObserver<CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse> responseObserver) {
        if (!sectorMap.containsKey(requestModel.getSectorName()))
            throw new SectorDoesNotExistsException(requestModel.getSectorName());
        Map<Integer,Counter> counterMap = sectorMap.get(requestModel.getSectorName()).getCounterMap();
        if (!counterMap.containsKey(requestModel.getFromVal().get()))
            throw new CountersAreNotAssignedException();
        Counter counter = counterMap.get(requestModel.getFromVal().get());
        if (!counter.getIsCheckingIn().get())
            throw new CountersAreNotAssignedException();
        if (!counter.getAirline().getName().equals(requestModel.getAirlineName()))
            throw new CounterIsCheckingInOtherAirlineException();
        if (!counter.getIsFirstInRange().get())
            throw new CounterIsNotFirstInRangeException();
        for (int i = requestModel.getFromVal().get(); i <= counter.getLastInRange().get(); i++) {
            Counter auxCounter = counterMap.get(i);
            Booking booking;
            if (!auxCounter.bookingQueueEmpty()) {
                booking = auxCounter.getBookingQueue().poll();
                booking.getCheckedIn().set(true);
                responseObserver.onNext(CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse.newBuilder()
                        .setCounter(i)
                        .setBooking(booking.getCode()).setFlight(booking.getFlight().getCode())
                        .setSuccessful(true)
                        .build());
            } else {
                responseObserver.onNext(CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse.newBuilder()
                        .setSuccessful(false).setCounter(i).build());
            }
            if (i == counter.getLastInRange().get()) {
                responseObserver.onCompleted();
            }
        }

    }

    public synchronized PassengerCheckInResponseModel passengerCheckIn(PassengerCheckInRequestModel requestModel) {
        if (!bookingConcurrentMap.containsKey(requestModel.getBooking()))
            throw new BookingDoesNotExistException(requestModel.getBooking());
        Booking booking = bookingConcurrentMap.get(requestModel.getBooking());
        if (booking.getCheckedIn().get())
            throw new BookingAlreadyCheckedInException(booking.getCode());
        if (!sectorMap.containsKey(requestModel.getSectorName()))
            throw new SectorDoesNotExistsException(requestModel.getSectorName());
        Map<Integer, Counter> counterMap = sectorMap.get(requestModel.getSectorName()).getCounterMap();
        if (!counterMap.containsKey(requestModel.getFirstCounter().get()))
            throw new CountersAreNotAssignedException();
        if (!counterMap.get(requestModel.getFirstCounter().get()).getIsFirstInRange().get())
            throw new CounterIsNotFirstInRangeException();
        int lastCounter = counterMap.get(requestModel.getFirstCounter().get()).getLastInRange().get();
        Random random = new Random();
        int counter = random.nextInt(lastCounter - requestModel.getFirstCounter().get() + 1) + requestModel.getFirstCounter().get();
        AtomicInteger peopleInLine =  new AtomicInteger();
        for (int i = requestModel.getFirstCounter().get(); i <= lastCounter; i++) {
            ConcurrentLinkedQueue<Booking> bookingQueue = counterMap.get(i).getBookingQueue();
            if (bookingQueue.contains(booking))
                throw new BookingAlreadyInLineException(booking.getCode());
            peopleInLine.addAndGet(bookingQueue.size());
        }
        counterMap.get(counter).getBookingQueue().add(booking);
        return new PassengerCheckInResponseModel(new AtomicInteger(lastCounter),peopleInLine,booking.getFlight().getCode(),booking.getFlight().getAirline().getName());
    }
}
