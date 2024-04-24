package ar.edu.itba.pod.grpc.server.exeptions;

public class SectorMapIsEmptyException extends RuntimeException {

    @Override
    public String getMessage() {
        return "There are no sectors in the airport.";
    }
}
