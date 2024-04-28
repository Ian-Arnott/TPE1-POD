package ar.edu.itba.pod.grpc.server.models;

import ar.edu.itba.pod.grpc.server.models.response.CheckedInInfo;
import jdk.dynalink.linker.LinkerServices;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Booking {
    private final String code;
    private final Flight flight;
    private final CheckedInInfo checkedInInfo;
    private final AtomicBoolean inQueue;

    public Booking(String code, Flight flight) {
        this.code = code;
        this.flight = flight;
        this.checkedInInfo = new CheckedInInfo();
        inQueue = new AtomicBoolean(false);
    }

    public boolean getCheckedIn() {
        return checkedInInfo.getCheckedIn();
    }

    public CheckedInInfo getCheckedInInfo() {
        return checkedInInfo;
    }

    public void checkIn() {
        checkedInInfo.setCheckedIn(true);
    }


    public String getCode() {
        return code;
    }

    public Flight getFlight() {
        return flight;
    }

    public AtomicBoolean getInQueue() {
        return inQueue;
    }

    public String getAirlineName() {
        return flight.getAirline().getName();
    }
    public BookingRecord toRecord() {
        return new BookingRecord(code, flight.getAirline().getName(), flight.getCode(), checkedInInfo.copy());
    }

    public record BookingRecord(String bookingCode, String airlineName, String flightCode, CheckedInInfo checkedInInfo) {}
}
