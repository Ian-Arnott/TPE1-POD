package ar.edu.itba.pod.grpc.server.exeptions;

public class StillCheckingInBookingsException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Bookings are still checking in";
    }
}
