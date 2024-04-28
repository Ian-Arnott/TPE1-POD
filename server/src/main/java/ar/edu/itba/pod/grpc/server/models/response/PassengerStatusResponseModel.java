package ar.edu.itba.pod.grpc.server.models.response;

import java.util.List;

public class PassengerStatusResponseModel {
    boolean isCheckingIn;
    boolean isCheckedIn;
    String flightCode;
    String airlineName;
    int counterOfCheckIn;
    List<Integer> countersForCheckingIn;
    String sectorName;
    int peopleAmountInLine;


    public PassengerStatusResponseModel(boolean isCheckingIn, boolean isCheckedIn, String flightCode, String airlineName, int counterOfCheckIn, List<Integer> countersForCheckingIn, String sectorName, int peopleAmountInLine) {
        this.isCheckingIn = isCheckingIn;
        this.isCheckedIn = isCheckedIn;
        this.flightCode = flightCode;
        this.airlineName = airlineName;
        this.counterOfCheckIn = counterOfCheckIn;
        this.countersForCheckingIn = countersForCheckingIn;
        this.sectorName = sectorName;
        this.peopleAmountInLine = peopleAmountInLine;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public List<Integer> getCountersForCheckingIn() {
        return countersForCheckingIn;
    }

    public String getSectorName() {
        return sectorName;
    }

    public int getPeopleAmountInLine() {
        return peopleAmountInLine;
    }

    public boolean isCheckingIn() {
        return isCheckingIn;
    }

    public void setCheckingIn(boolean checkingIn) {
        isCheckingIn = checkingIn;
    }

    public boolean isCheckedIn() {
        return isCheckedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        isCheckedIn = checkedIn;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public int getCounterOfCheckIn() {
        return counterOfCheckIn;
    }

    public void setCounterOfCheckIn(int counterOfCheckIn) {
        this.counterOfCheckIn = counterOfCheckIn;
    }

    public void setCountersForCheckingIn(List<Integer> countersForCheckingIn) {
        this.countersForCheckingIn = countersForCheckingIn;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public void setPeopleAmountInLine(int peopleAmountInLine) {
        this.peopleAmountInLine = peopleAmountInLine;
    }
}
