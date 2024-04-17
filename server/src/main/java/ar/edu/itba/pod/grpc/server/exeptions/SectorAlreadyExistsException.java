package ar.edu.itba.pod.grpc.server.exeptions;

public class SectorAlreadyExistsException extends RuntimeException {
    private final String sectorName;

    public SectorAlreadyExistsException(String sectorName) {
        this.sectorName = sectorName;
    }

    @Override
    public String getMessage() {
        return "Sector " + sectorName + " already exists";
    }
}
