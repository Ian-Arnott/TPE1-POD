package ar.edu.itba.pod.grpc.server.models.response;

import ar.edu.itba.pod.grpc.server.models.Counter;
import ar.edu.itba.pod.grpc.server.models.Sector;
import com.google.common.graph.AbstractNetwork;

import java.util.concurrent.atomic.AtomicBoolean;

public class CheckedInInfo {
    private boolean checkedIn;
    private String sector;
    private int counter;

    public CheckedInInfo() {
        checkedIn = false;
    }
    private CheckedInInfo(boolean checkedIn, String sectorName, int counter) {
        this.checkedIn = checkedIn;
        this.sector = sectorName;
        this.counter = counter;
    }

    public synchronized boolean getCheckedIn() {
        return checkedIn;
    }
    public synchronized void setCheckedIn(boolean val) {
        checkedIn = val;
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
    public synchronized CheckedInInfo copy() {
        return new CheckedInInfo(checkedIn, sector,  counter);
    }
}