package ar.edu.itba.pod.grpc.server.repository;

import airport.NotifyServiceOuterClass;
import ar.edu.itba.pod.grpc.server.exeptions.SectorMapIsEmptyException;
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
import ar.edu.itba.pod.grpc.server.models.response.*;
import com.google.protobuf.StringValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class AirportRepository {
    private static AirportRepository instance;
    private final ConcurrentMap<String, Airline> airlines;
    private final ConcurrentMap<String, Flight> flightConcurrentMap;
    private final ConcurrentMap<String, Booking> bookingConcurrentMap;
    private final ConcurrentLinkedQueue<Booking> checkedInBookings;
    private final ConcurrentMap<String, Sector> sectorMap;

    private int lastCounterAdded;

    public AirportRepository() {
        sectorMap = new ConcurrentHashMap<>();

        this.airlines = new ConcurrentHashMap<>();
        this.flightConcurrentMap = new ConcurrentHashMap<>();
        this.bookingConcurrentMap = new ConcurrentHashMap<>();

        lastCounterAdded = 1;
        checkedInBookings = new ConcurrentLinkedQueue<>();
    }


    public synchronized Set<String> getSectorNames() {
        return sectorMap.keySet();
    }

    public synchronized static AirportRepository getInstance() {
        if (instance == null) {
            instance = new AirportRepository();
        }
        return instance;
    }

    public void addSector(String sectorName) {
        if (sectorMap.putIfAbsent(sectorName, new Sector(sectorName)) != null) {
            throw new SectorAlreadyExistsException(sectorName);
        }
    }


    public synchronized AddCountersResponseModel addCountersToSector(String sectorName, int counterAmount) {
        Sector sector = sectorMap.get(sectorName);
        if (sector == null) {
            throw new SectorDoesNotExistsException(sectorName);
        }
        if (counterAmount <= 0) {
            throw new NonPositiveCounterException(counterAmount);
        }

        sector.addCounters(lastCounterAdded, counterAmount);
        lastCounterAdded += counterAmount;

        List<PendingAssignment.PendingAssignmentRecord> list = sector.resolvePending();
        return new AddCountersResponseModel(lastCounterAdded, list);
    }

    public synchronized void manifest(ManifestRequestModel requestModel) {
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
                    0,
                    0,
                    requestModel.getCountVal(),
                    amountPendingAhead,
                    true
            );
            for (Flight flight : flightQueue) {
                flight.getPending().set(true);
            }
            PendingAssignment pendingAssignment = new PendingAssignment(
                    airline,
                    flightQueue,
                    requestModel.getCountVal()
            );
            pendingAssignments.add(pendingAssignment);
            return responseModel;
        } else {
            CounterRange counterRange = new CounterRange(availableCounters, airlines.get(requestModel.getAirlineName()), flightQueue);
            for (Flight flight : flightQueue) {
                if (flight.getPending().get())
                    flight.getPending().set(false);
                flight.getCheckingIn().set(true);
                flight.setSectorName(sector.getName());
                flight.setCounterRange(counterRange);
            }
            return new CounterRangeAssignmentResponseModel(
                    requestModel.getCountVal(),
                    counterRange.getLastCounter().getNum(),
                    0,
                    0,
                    false
            );
        }
    }

    public synchronized List<Counter.CounterRecord> getCounters(String sectorName, int from, int to){
        Sector sector = sectorMap.get(sectorName);
        if (to - from < 0) {
            throw new InvalidCounterRangeException(from, to);
        }

        if (sector == null) {
            throw new SectorDoesNotExistsException(sectorName);
        }

        Map<Integer, Counter> counterMap = sector.getCounterMap();
        List<Counter.CounterRecord> counters = new ArrayList<>();
        for (int i = from, j = 0; i <= to && j < counterMap.values().size(); i++, j++) {
            Counter counter = counterMap.get(i);
            if (counter != null) {
                counters.add(counter.toRecord());
            }
        }
        return counters;
    }

    public synchronized ConcurrentLinkedQueue<PendingAssignment> getPendingAssignments(String sectorName) {
        Sector sector = sectorMap.get(sectorName);
        if (sector == null)
            return new ConcurrentLinkedQueue<>();
        return sector.getPendingAssignments();
    }


    public synchronized FreeCounterRangeResponseModel freeCounterRange(FreeCounterRangeRequestModel requestModel) {
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
        responseModel.setPendingAssignments(sector.resolvePending());
        return responseModel;
    }

    public synchronized FetchCounterResponseModel fetchCounter(String bookingCode) {

        if (!bookingConcurrentMap.containsKey(bookingCode)) {
            throw new BookingDoesNotExistException(bookingCode);
        }

        Booking booking = bookingConcurrentMap.get(bookingCode);

        if (booking.getFlight().getCheckedIn().get() || booking.getFlight().getCounterRange() == null) {
            return new FetchCounterResponseModel(
                    booking.getFlight().getCode(),
                    booking.getFlight().getAirline().getName(), new ArrayList<>(), "", 0);
        }

        return new FetchCounterResponseModel(
                booking.getFlight().getCode(),
                booking.getFlight().getAirline().getName(),
                booking.getFlight().getCounterRange().getCounters(),
                booking.getFlight().getSectorName(),
                booking.getFlight().getCounterRange().getQueueLength()
        );
    }

    public synchronized List<Booking.BookingRecord> performCounterCheckIn(String sectorName, int fromVal, String airlineName) {
        Sector sector = sectorMap.get(sectorName);
        if (sector == null)
            throw new SectorDoesNotExistsException(sectorName);
        Counter counter = sector.getCounterMap().get(fromVal);

        if (counter == null)
            throw new CountersAreNotAssignedException();
        if (!counter.getIsCheckingIn().get())
            throw new CountersAreNotAssignedException();
        Airline airline = counter.getCounterRange().getAirline();
        if (!airline.getName().equals(airlineName))
            throw new CounterIsCheckingInOtherAirlineException();
        if (!counter.getIsFirstInRange().get())
            throw new CounterIsNotFirstInRangeException();

        CounterRange counterRange = counter.getCounterRange();
        List<Booking.BookingRecord> bookingList = new ArrayList<>();
        for (int i = fromVal; i <= counter.getLastInRange().get(); i++) {
            Booking booking = counterRange.performCheckIn();
            if (booking != null) {
                bookingList.add(booking.toRecord());
                booking.checkIn();
                booking.getCheckedInInfo().setSector(sector.getName());
                booking.getCheckedInInfo().setCounter(counter.getNum());
                checkedInBookings.add(booking);
            } else {
                bookingList.add(null);
            }
        }
        return bookingList;
    }

    public synchronized PassengerCheckInResponseModel passengerCheckIn(PassengerCheckInRequestModel requestModel) {
        Booking booking = bookingConcurrentMap.get(requestModel.getBooking());
        if (booking == null)
            throw new BookingDoesNotExistException(requestModel.getBooking());
        if (booking.getCheckedIn())
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

        int peopleInLine = counter.addBookingToQueue(booking);

        return new PassengerCheckInResponseModel(
                counter.getLastInRange().get(),
                peopleInLine,
                booking.getFlight().getCode(),
                booking.getFlight().getAirline().getName()
        );
    }

    public synchronized PassengerStatusResponseModel passengerStatus(String bookingCode) {

        if (!bookingConcurrentMap.containsKey(bookingCode)) {
            throw new BookingDoesNotExistException(bookingCode);
        }

        Booking booking = bookingConcurrentMap.get(bookingCode);

        if (booking.getFlight().getCounterRange() == null) {
            throw new CountersAreNotAssignedException();
        }

        if (booking.getCheckedIn()) {
            return new PassengerStatusResponseModel(
                    true,
                    true,
                    booking.getFlight().getCode(),
                    booking.getAirlineName(),
                    booking.getCheckedInInfo().getCounter(),
                    new ArrayList<>(),
                    booking.getFlight().getSectorName(),
                    0
            );
        }

        if (booking.getInQueue().get()) {
            return new PassengerStatusResponseModel(
                    true,
                    false,
                    booking.getFlight().getCode(),
                    booking.getAirlineName(),
                    0,
                    booking.getFlight().getCounterRange().getCounters(),
                    booking.getFlight().getSectorName(),
                    booking.getFlight().getCounterRange().getQueueLength()
            );
        }

        return new PassengerStatusResponseModel(
                false,
                false,
                booking.getFlight().getCode(),
                booking.getAirlineName(),
                0,
                booking.getFlight().getCounterRange().getCounters(),
                booking.getFlight().getSectorName(),
                0
        );
    }

    public ConcurrentLinkedQueue<PendingAssignment> listPendingAssignments(StringValue sectorName) {
        String sectorNameString = sectorName.getValue();

        if (!sectorMap.containsKey(sectorNameString)) throw new SectorDoesNotExistsException(sectorNameString);

        return sectorMap.get(sectorNameString).getPendingAssignments();
    }

    public synchronized void registerForNotifications(String airlineName) {
        Airline airline = airlines.get(airlineName);
        if (airline == null)
            throw new AirlineDoesNotExistException(airlineName);
    }

    public synchronized List<CounterQueryResponse> getCountersQuery(String sectorName) {
        boolean isEmpty = true;
        for (Sector sector : sectorMap.values()) {
            if (!sector.getCounterMap().isEmpty()) {
                isEmpty = false;
                break;
            }
        }
        if (isEmpty)
            throw new NoCountersAddedException();

        List<CounterQueryResponse> counterQueryResponses = new ArrayList<>();
        if (sectorName.isEmpty()) {
            for (Sector sector : sectorMap.values()) {
                 List<Counter.CounterRecord> counterRecords = sectorMap.get(sector.getName()).getCounterMap().values().stream().map(Counter::toRecord).toList();
                 counterQueryResponses.add(new CounterQueryResponse(sector.getName(), counterRecords));
            }
        } else {
            if (sectorMap.containsKey(sectorName)) {
                List<Counter.CounterRecord> counterRecords = sectorMap.get(sectorName).getCounterMap().values().stream().map(Counter::toRecord).toList();
                counterQueryResponses.add(new CounterQueryResponse(sectorName, counterRecords));
            } else {
                counterQueryResponses.add(new CounterQueryResponse(sectorName, new ArrayList<>()));
            }
        }
        return counterQueryResponses;
    }


    public synchronized List<Booking.BookingRecord> getBookingsQuery(String sectorName, String airlineName) {
        if (checkedInBookings.isEmpty())
            throw new NoBookingsCheckedInException();
        List<Booking.BookingRecord> bookingList = new ArrayList<>();
        List<Booking.BookingRecord> recordList = checkedInBookings.stream().map(Booking::toRecord).toList();
        if (sectorName.isEmpty() && airlineName.isEmpty()) {
            bookingList.addAll(recordList);
            return bookingList;
        }
        for  (Booking.BookingRecord booking : recordList) {
            if (!sectorName.isEmpty() && !airlineName.isEmpty())
                if (booking.airlineName().equals(airlineName) && booking.checkedInInfo().getSector().equals(sectorName))
                    bookingList.add(booking);
            if (!sectorName.isEmpty() && !airlineName.isEmpty())
                if (booking.checkedInInfo().getSector().equals(sectorName))
                    bookingList.add(booking);
            if (sectorName.isEmpty())
                if (booking.airlineName().equals(airlineName))
                    bookingList.add(booking);
        }
        return bookingList;
    }
}
