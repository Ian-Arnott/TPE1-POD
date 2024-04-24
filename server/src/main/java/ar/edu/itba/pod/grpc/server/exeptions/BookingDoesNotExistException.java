package ar.edu.itba.pod.grpc.server.exeptions;

public class BookingDoesNotExistException extends RuntimeException {

    private final String booking;
    public BookingDoesNotExistException(String booking) {
        this.booking = booking;
    }

    @Override
    public String getMessage() {
        return "Booking " + booking + " does not exist";
    }
}
