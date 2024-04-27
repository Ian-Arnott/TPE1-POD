package ar.edu.itba.pod.grpc.server.repository;

import airport.NotifyServiceOuterClass;
import ar.edu.itba.pod.grpc.server.exeptions.SectorMapIsEmptyException;
import airport.CounterAssignmentServiceOuterClass;
import ar.edu.itba.pod.grpc.server.exeptions.NonPositiveCounterException;
import ar.edu.itba.pod.grpc.server.exeptions.SectorAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exeptions.SectorDoesNotExistsException;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;

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
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

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

        sector.resolvePending();
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

        Airline airline = airlines.get(requestModel.getAirlineName());
        if (airline == null) {
            throw new AirlineDoesNotExistException(requestModel.getAirlineName());
        }

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

        Sector sector = sectorMap.get(requestModel.getSectorName());
        Map<Integer, Counter> counterMap = sector.getCounterMap();
        List<Counter> availableCounters = sector.getAvailableCounters(requestModel.getCountVal(), counterMap);

        if (availableCounters.isEmpty() || availableCounters.size()!=requestModel.getCountVal()) {
            ConcurrentLinkedQueue<PendingAssignment> pendingAssignments = sector.getPendingAssignments();
            int amountPendingAhead = pendingAssignments.size();
            CounterRangeAssignmentResponseModel responseModel = new CounterRangeAssignmentResponseModel(
                    0,0,requestModel.getCountVal(),
                    amountPendingAhead);
            for (Flight flight : flightQueue) {
                flight.getPending().set(true);
            }
            PendingAssignment pendingAssignment = new PendingAssignment(airline, flightQueue, requestModel.getAirlineName(), requestModel.getCountVal());
            pendingAssignments.add(pendingAssignment);
            if (airline.isShouldNotify())
                airline.notifyPendingAssignment(pendingAssignment, sector.getName(),amountPendingAhead);
            return responseModel;
        } else {
            CounterRange counterRange = new CounterRange(availableCounters, airlines.get(requestModel.getAirlineName()), flightQueue);
            for (Flight flight : flightQueue) {
                if (flight.getPending().get())
                    flight.getPending().set(false);
                flight.getCheckingIn().set(true);
            }
            if (airline.isShouldNotify())
                airline.notifyAssignedRange(counterRange, sector.getName());
            return new CounterRangeAssignmentResponseModel(requestModel.getCountVal(), counterRange.getLastCounter().getNum()
                    , 0,0);
        }
    }

    public List<Counter> getCounters(String sectorName, int from, int to){
        Sector sector = sectorMap.get(sectorName);
        if (to - from < 0) {
            throw new InvalidCounterRangeException(from, to);
        }

        if (sector == null) {
            throw new SectorDoesNotExistsException(sectorName);
        }

        Map<Integer, Counter> counterMap = sector.getCounterMap();
        List<Counter> counters = new ArrayList<>();
        for (int i = from, j = 0; i <= to && j < counterMap.values().size(); i++, j++) {
            Counter counter = counterMap.get(i);
            if (counter != null) {
                counters.add(counter);
            }
        }
        return counters;
    }

    public FreeCounterRangeResponseModel freeCounterRange(FreeCounterRangeRequestModel requestModel) {
        if (!sectorMap.containsKey(requestModel.getSectorName()))
            throw new SectorDoesNotExistsException(requestModel.getSectorName());
        Sector sector = sectorMap.get(requestModel.getSectorName());

        Counter counter = sector.getCounterMap().get(requestModel.getFromVal());
        if (counter == null)
            throw new CountersAreNotAssignedException();
        if (!counter.getIsCheckingIn().get())
            throw new CountersAreNotAssignedException();
        CounterRange counterRange = counter.getCounterRange();
        Airline airline = counter.getAirline();

        if (!airline.getName().equals(requestModel.getAirline()))
            throw new CounterIsCheckingInOtherAirlineException();
        if (!counter.getIsFirstInRange().get())
            throw new CounterIsNotFirstInRangeException();
        if (counter.getCounterRange().hasBookings())
            throw new StillCheckingInBookingsException();

        FreeCounterRangeResponseModel responseModel = counterRange.free();
        if (airline.isShouldNotify())
            airline.notifyFreeCounterRange(responseModel.getFlights(), requestModel.getFromVal(), responseModel.getFreedAmount(), sector.getName());
        sector.resolvePending();
        return responseModel;
    }

    public synchronized void performCounterCheckIn(PerformCounterCheckInRequestModel requestModel, StreamObserver<CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse> responseObserver) {
        Sector sector = sectorMap.get(requestModel.getSectorName());
        if (!sectorMap.containsKey(requestModel.getSectorName()))
            throw new SectorDoesNotExistsException(requestModel.getSectorName());
        Counter counter = sector.getCounterMap().get(requestModel.getFromVal().get());

        if (counter == null)
            throw new CountersAreNotAssignedException();
        if (!counter.getIsCheckingIn().get())
            throw new CountersAreNotAssignedException();
        if (!counter.getCounterRange().getAirline().getName().equals(requestModel.getAirlineName()))
            throw new CounterIsCheckingInOtherAirlineException();
        if (!counter.getIsFirstInRange().get())
            throw new CounterIsNotFirstInRangeException();

        CounterRange counterRange = counter.getCounterRange();
        Airline airline = counter.getAirline();
        for (int i = requestModel.getFromVal().get(); i <= counter.getLastInRange().get(); i++) {
            Booking booking = counterRange.performCheckIn();
            if (booking == null) {
                responseObserver.onNext(CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse.newBuilder()
                        .setSuccessful(false).setCounter(i).build());
            } else {
                booking.getCheckedIn().set(true);
                responseObserver.onNext(CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse.newBuilder()
                        .setCounter(i)
                        .setBooking(booking.getCode()).setFlight(booking.getFlight().getCode())
                        .setSuccessful(true)
                        .build());
                if (airline.isShouldNotify())
                    airline.notifyPassengerCheckIn(booking,i,sector.getName());
            }
            if (i == counter.getLastInRange().get()) {
                responseObserver.onCompleted();
            }
        }

    }

    public synchronized PassengerCheckInResponseModel passengerCheckIn(PassengerCheckInRequestModel requestModel) {
        Booking booking = bookingConcurrentMap.get(requestModel.getBooking());
        if (booking == null)
            throw new BookingDoesNotExistException(requestModel.getBooking());
        if (booking.getCheckedIn().get())
            throw new BookingAlreadyCheckedInException(booking.getCode());
        if (!sectorMap.containsKey(requestModel.getSectorName()))
            throw new SectorDoesNotExistsException(requestModel.getSectorName());
        Counter counter = sectorMap.get(requestModel.getSectorName()).getCounterMap().get(requestModel.getFirstCounter().get());
        if (counter == null)
            throw new CountersAreNotAssignedException();
        if (!counter.getIsFirstInRange().get())
            throw new CounterIsNotFirstInRangeException();
        if (booking.getInQueue().get())
            throw new BookingAlreadyInLineException(booking.getCode());

        // asumir que el airline esta bien??
        Airline airline = counter.getAirline();
        int peopleInLine = counter.addBookingToQueue(booking);

        PassengerCheckInResponseModel responseModel = new PassengerCheckInResponseModel(
                counter.getLastInRange(),
                new AtomicInteger(peopleInLine),
                booking.getFlight().getCode(),
                booking.getFlight().getAirline().getName()
        );
        if (airline.isShouldNotify())
            airline.notifyBookingInQueue(booking, peopleInLine,counter, requestModel.getSectorName());
        return responseModel;
    }

    public ConcurrentLinkedQueue<PendingAssignment> listPendingAssignments(StringValue sectorName) {
        String sectorNameString = sectorName.getValue();

        if (!sectorMap.containsKey(sectorNameString)) throw new SectorDoesNotExistsException(sectorNameString);

        return sectorMap.get(sectorNameString).getPendingAssignments();
    }

    public void registerForNotifications(String airlineName, StreamObserver<NotifyServiceOuterClass.Notification> responseObserver) {
        Airline airline = airlines.get(airlineName);
        if (airline == null)
            throw new AirlineDoesNotExistException(airlineName);
        airline.registerForNotifications(responseObserver);
        airline.notifyRegistered();
    }

    public StreamObserver<NotifyServiceOuterClass.Notification> unregisterForNotification(String airlineName) {
        Airline airline = airlines.get(airlineName);
        if (airline == null)
            throw new AirlineDoesNotExistException(airlineName);
        return airline.unregisterForNotifications();
    }
}
