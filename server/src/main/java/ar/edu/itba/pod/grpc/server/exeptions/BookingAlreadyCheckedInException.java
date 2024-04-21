package ar.edu.itba.pod.grpc.server.exeptions;

public class BookingAlreadyCheckedInException extends RuntimeException{
    private final String booking;


    public BookingAlreadyCheckedInException(String booking) {
        this.booking = booking;
    }

    @Override
    public String getMessage() {
        return "Booking " + booking + " already checked in";
    }
}
