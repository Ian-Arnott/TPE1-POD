package ar.edu.itba.pod.grpc.server;

import ar.edu.itba.pod.grpc.server.exeptions.*;
import ar.edu.itba.pod.grpc.server.models.requests.CounterRangeAssignmentRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.FreeCounterRangeRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.PassengerCheckInRequestModel;
import ar.edu.itba.pod.grpc.server.models.response.AddCountersResponseModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    void passengerCheckinTest() {
        instance.addSector("C");
        instance.addCountersToSector("C", 3);
        instance.manifest(new ManifestRequestModel("ABC124", "AC987", "AirCanada"));

        instance.manifest(new ManifestRequestModel("ABC123", "AC987", "AirCanada"));
        instance.manifest(new ManifestRequestModel("ABC122", "AC987", "AirCanada"));
        instance.manifest(new ManifestRequestModel("ABC121", "AC987", "AirCanada"));
        Assertions.assertDoesNotThrow(
                () -> instance.fetchCounter("ABC123")
        );
        instance.counterRangeAssignment(new CounterRangeAssignmentRequestModel(2, "C", "AC987".lines().toList(),"AirCanada"));
        Assertions.assertDoesNotThrow(
                () -> instance.passengerCheckIn(new PassengerCheckInRequestModel(new AtomicInteger(1),"C","ABC123"))
        );
        Assertions.assertDoesNotThrow(
                () -> instance.passengerStatus("ABC123")
        );
    }

    @Test
    void freeCounterRangeTest() {
        instance.addSector("C");
        instance.manifest(new ManifestRequestModel("ABC124", "AC987", "AirCanada"));
        instance.addCountersToSector("C", 3);

        instance.counterRangeAssignment(new CounterRangeAssignmentRequestModel(1, "C", "AC987".lines().toList(),"AirCanada"));
        Assertions.assertDoesNotThrow(
                ()->instance.freeCounterRange(new FreeCounterRangeRequestModel("C",1,"AirCanada"))
        );
    }

    @Test
    void queryCountersTest() {
        instance.addSector("C");
        instance.manifest(new ManifestRequestModel("ABC124", "AC987", "AirCanada"));

        try {
            instance.getCountersQuery("C");
        } catch (NoCountersAddedException e ) {
            Assertions.assertTrue(true);
        }
        instance.addCountersToSector("C", 3);
        Assertions.assertDoesNotThrow(()-> instance.getCountersQuery("C"));

    }

    @Test
    void bookingQueryTest() {
        instance.addSector("C");
        instance.addCountersToSector("C", 3);
        instance.manifest(new ManifestRequestModel("ABC123", "AC987", "AirCanada"));

        try {
            instance.getBookingsQuery("C","");
        } catch (NoBookingsCheckedInException e) {
            Assertions.assertTrue(true);
        }

        instance.counterRangeAssignment(new CounterRangeAssignmentRequestModel(1, "C", "AC987".lines().toList(),"AirCanada"));
        instance.passengerCheckIn(new PassengerCheckInRequestModel(new AtomicInteger(1),"C","ABC123"));
        instance.performCounterCheckIn("C",1,"AirCanada");

        Assertions.assertDoesNotThrow(()-> instance.getBookingsQuery("C",""));
    }
}