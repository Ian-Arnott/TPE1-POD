package ar.edu.itba.pod.grpc.server.exeptions;

public class BookingAlreadyExistsException extends RuntimeException{
    private final String booking;
    private final String flight;
    private final String airline;

    public BookingAlreadyExistsException(String booking, String flight, String airline) {
        this.booking = booking;
        this.flight = flight;
        this.airline = airline;
    }

    @Override
    public String getMessage() {
        return "Booking " + booking + " for " + airline + " " + flight + " already exists";
    }
}
