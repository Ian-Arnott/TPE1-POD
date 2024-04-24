package ar.edu.itba.pod.grpc.server.exeptions;

public class BookingAlreadyInLineException extends RuntimeException {
    private final String booking;


    public BookingAlreadyInLineException(String booking) {
        this.booking = booking;
    }

    @Override
    public String getMessage() {
        return "Booking: " + booking + " is already in line";
    }
}
