package ar.edu.itba.pod.grpc.server;

import ar.edu.itba.pod.grpc.server.exeptions.*;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import ar.edu.itba.pod.grpc.server.models.response.AddCountersResponseModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;

class AirportRepositoryTest {
    private static AirportRepository instance;

    @BeforeEach
    public void init() {
        instance = new AirportRepository();
    }
    @Test
    void addSectorTest() {
        Assertions.assertDoesNotThrow(() -> instance.addSector("C"));
        Assertions.assertThrows(SectorAlreadyExistsException.class, () -> instance.addSector("C"));
    }
    @Test
    void addCountersTest() {
        instance.addSector("C");

        Assertions.assertThrows(SectorDoesNotExistsException.class, () -> instance.addCountersToSector("D", 1));
        Assertions.assertThrows(NonPositiveCounterException.class, () -> instance.addCountersToSector("C", 0));
        Assertions.assertEquals(
                new AddCountersResponseModel(2, new ArrayList<>()),
                instance.addCountersToSector("C", 1)
        );
    }
    @Test
    void manifestTest() {
        Assertions.assertDoesNotThrow(
                () -> instance.manifest(new ManifestRequestModel("ABC123", "AC987", "AirCanada"))
        );
        Assertions.assertThrows(
                BookingAlreadyExistsException.class,
                () -> instance.manifest(new ManifestRequestModel("ABC123", "AC987", "AmericanAirlines"))
        );
        Assertions.assertThrows(
                FlightExistsForOtherAirlineException.class,
                () -> instance.manifest(new ManifestRequestModel("XYZ234", "AC987", "AmericanAirlines"))
        );
    }
}