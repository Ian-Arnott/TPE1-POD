package ar.edu.itba.pod.grpc.server.models.response;

import ar.edu.itba.pod.grpc.server.models.Counter;
import ar.edu.itba.pod.grpc.server.models.Sector;

import java.util.concurrent.atomic.AtomicBoolean;

public class CheckedInInfo {
    private final AtomicBoolean checkedIn;
    private Sector sector;
    private Counter counter;

    public CheckedInInfo() {
        checkedIn = new AtomicBoolean(false);
    }

    public AtomicBoolean getCheckedIn() {
        return checkedIn;
    }

    public Sector getSector() {
        return sector;
    }

    public Counter getCounter() {
        return counter;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public void setCounter(Counter counter) {
        this.counter = counter;
    }
}