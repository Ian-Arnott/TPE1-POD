package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.server.models.response.FreeCounterRangeResponseModel;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CounterRange {
    private final List<Counter> counters;
    private final ConcurrentLinkedQueue<Booking> bookingQueue;

    private final Airline airline;
    private final ConcurrentLinkedQueue<Flight> flights;

    public CounterRange(List<Counter> counters, Airline airline, ConcurrentLinkedQueue<Flight> flights) {
        this.counters = counters;
        this.airline = airline;
        this.flights = flights;
        bookingQueue = new ConcurrentLinkedQueue<>();
        for (Counter counter : counters) {
            counter.setCounterRange(this);
        }
    }

    public Counter getFirstCounter() {
        return counters.getFirst();
    }

    public Counter getLastCounter() {
        return counters.getLast();
    }

    public int getCounterCount() {
        return counters.size();
    }

    public Airline getAirline() {
        return airline;
    }

    public ConcurrentLinkedQueue<Flight> getFlights() {
        return flights;
    }

    public boolean hasBookings() {
        return !bookingQueue.isEmpty();
    }

    public FreeCounterRangeResponseModel free() {
        FreeCounterRangeResponseModel freeCounterRangeResponseModel = new FreeCounterRangeResponseModel();
        freeCounterRangeResponseModel.getFreedAmount().set(counters.size());
        for (Counter counter : counters) {
            counter.setCounterRange(null);
        }
        for (Flight flight : flights) {
            flight.getCheckingIn().set(false);
            flight.getCheckedIn().set(true);
            freeCounterRangeResponseModel.getFlights().add(flight.getCode());
        }
        return freeCounterRangeResponseModel;
    }

    public Booking performCheckIn() {
        if (bookingQueue.isEmpty()) {
            return null;
        }
        return bookingQueue.poll();
    }

    public int addBookingToQueue(Booking booking) {
        int len = bookingQueue.size();

        bookingQueue.add(booking);
        booking.getInQueue().set(true);
        return len;
    }
    public int getQueueLength() {
        return bookingQueue.size();
    }
}

