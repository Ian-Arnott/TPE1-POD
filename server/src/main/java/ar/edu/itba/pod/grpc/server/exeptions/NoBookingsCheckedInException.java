package ar.edu.itba.pod.grpc.server.exeptions;

public class NoBookingsCheckedInException extends RuntimeException {
    @Override
    public String getMessage() {
        return "No bookings have checked in";
    }
}
