package ar.edu.itba.pod.grpc.server.models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Counter {
    private final int num;
    private CounterRange counterRange;

    public Counter(int num) {
        this.num = num;
        counterRange = null;
    }

    public AtomicBoolean getIsFirstInRange() {
        if (counterRange == null) {
            return new AtomicBoolean(false);
        }
        return new AtomicBoolean(counterRange.getFirstCounter().equals(this));
    }

    public AtomicInteger getLastInRange() {
        if (counterRange == null) {
            return new AtomicInteger(0);
        }
        return new AtomicInteger(counterRange.getLastCounter().getNum());
    }


    public AtomicBoolean getIsCheckingIn() {
        return new AtomicBoolean(counterRange != null);
    }

    public int getNum() {
        return num;
    }

    public CounterRange getCounterRange() {
        return counterRange;
    }

    public void setCounterRange(CounterRange counterRange) {
        this.counterRange = counterRange;
    }

    public int addBookingToQueue(Booking booking) {
        return counterRange.addBookingToQueue(booking);
    }
    public Airline getAirline(){
        if (counterRange == null) {
            return null;
        }
        return counterRange.getAirline();
    }
    public ConcurrentLinkedQueue<Flight> getFlights() {
         if (counterRange == null) {
             return new ConcurrentLinkedQueue<>();
         }
         return counterRange.getFlights();
    }
    public int getQueueLength(){
        if (counterRange == null) {
            return 0;
        }
        return counterRange.getQueueLength();
    }

    public CounterRecord toRecord() {
        Airline airline = getAirline();
        if (airline == null) {
            return new CounterRecord(num, "", new ArrayList<>(), 0);
        }
        return new CounterRecord(num, airline.getName(), counterRange.getFlights().stream().map(Flight::getCode).toList(), counterRange.getQueueLength());
    }


    public record CounterRecord(int counterCode, String airlineName, List<String> flightCodes, int peopleInLine) {
    }

}
