package ar.edu.itba.pod.grpc.server.repository;

import ar.edu.itba.pod.grpc.server.exeptions.BookingAlreadyExistsException;
import ar.edu.itba.pod.grpc.server.exeptions.FlightExistsForOtherAirlineException;
import ar.edu.itba.pod.grpc.server.models.Airline;
import ar.edu.itba.pod.grpc.server.models.Booking;
import ar.edu.itba.pod.grpc.server.models.Flight;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BookingRepository {

    private static BookingRepository instance;
    private static AirportRepository airportRepository = AirportRepository.getInstance();

    private final ConcurrentMap<String, Airline> airlines;
    private final ConcurrentMap<String, Flight> flightConcurrentMap;
    private final ConcurrentMap<String, Booking> bookingConcurrentMap;

    private BookingRepository() {
        this.airlines = new ConcurrentHashMap<>();
        this.flightConcurrentMap = new ConcurrentHashMap<>();
        this.bookingConcurrentMap = new ConcurrentHashMap<>();
    }

    public synchronized static BookingRepository getInstance() {
        if (instance == null) {
            instance = new BookingRepository();
        }
        return instance;
    }

    public synchronized void manifest(String booking, String flight, String airline) {
        if (this.bookingConcurrentMap.containsKey(booking)) {
            throw new BookingAlreadyExistsException(booking, flight, airline);
        }

        Airline existingAirline = airlines.getOrDefault(airline, new Airline(airline));
        Flight existingFlight = flightConcurrentMap.get(flight);

        if (existingFlight != null) {
            if (!existingFlight.getAirline().getCode().equals(airline)) {
                throw new FlightExistsForOtherAirlineException(flight);
            }
        } else {
            existingFlight = new Flight(flight, existingAirline);
            existingAirline.getFlights().put(flight, existingFlight);
        }
        Booking newBooking = new Booking(booking, existingAirline, existingFlight);
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
        return flightConcurrentMap.get(flight).getAirline().getCode();
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

    public Flight getFlight(String flight) {
        return flightConcurrentMap.get(flight);
    }

    public ConcurrentMap<String, Flight> getFlightConcurrentMap() {
        return flightConcurrentMap;
    }

    public boolean bookingExist(String booking) {
        return bookingConcurrentMap.containsKey(booking);
    }

    public Booking getBooking(String booking) {
        return bookingConcurrentMap.get(booking);
    }
}
