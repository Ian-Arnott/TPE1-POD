package ar.edu.itba.pod.grpc.server.exeptions;

public class SectorDoesNotExistsException extends RuntimeException {
    private final String sectorName;

    public SectorDoesNotExistsException(String sectorName) {
        this.sectorName = sectorName;
    }

    @Override
    public String getMessage() {
        return "Sector " + sectorName + " does not exists";
    }
}
