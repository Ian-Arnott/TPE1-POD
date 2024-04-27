package ar.edu.itba.pod.grpc.server.models.response;

import ar.edu.itba.pod.grpc.server.models.Counter;
import ar.edu.itba.pod.grpc.server.models.Sector;

import java.util.concurrent.atomic.AtomicBoolean;

public class CheckedInInfo {
    private final AtomicBoolean checkedIn;
    private String sector;
    private int counter;

    public CheckedInInfo() {
        checkedIn = new AtomicBoolean(false);
    }

    public AtomicBoolean getCheckedIn() {
        return checkedIn;
    }

    public String getSector() {
        return sector;
    }

    public int getCounter() {
        return counter;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}