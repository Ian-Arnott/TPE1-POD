package ar.edu.itba.pod.grpc.server.repository;

import ar.edu.itba.pod.grpc.server.exeptions.*;
import ar.edu.itba.pod.grpc.server.models.Counter;
import ar.edu.itba.pod.grpc.server.models.requests.CounterRangeAssignmentRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.FreeCounterRangeRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.PassengerCheckInRequestModel;
import ar.edu.itba.pod.grpc.server.models.response.AddCountersResponseModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
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
    void listSectorsTest() {
        Assertions.assertThrows(SectorMapIsEmptyException.class, () -> instance.listSectors());

        instance.addSector("A");
        instance.addCountersToSector("A", 1);
        instance.addSector("C");
        instance.addCountersToSector("C", 3);
        instance.addSector("D");
        instance.addCountersToSector("D", 2);
        instance.addSector("Z");
        instance.addCountersToSector("C", 2);

        Map<String, Set<Integer>> resMap = new HashMap<>();
        resMap.put("A", new HashSet<>(List.of(new Integer[]{1})));
        resMap.put("C", new HashSet<>(List.of(new Integer[]{2, 3, 4, 7, 8})));
        resMap.put("D", new HashSet<>(List.of(new Integer[]{5, 6})));
        resMap.put("Z", new HashSet<>());

        Assertions.assertEquals(resMap, instance.listSectors());
    }
    @Test
    void listCountersTest() {
        instance.addSector("C");
        instance.addCountersToSector("C", 4);
        instance.manifest(new ManifestRequestModel("XYZ234", "AA123", "AmericanAirlines"));
        instance.manifest(new ManifestRequestModel("XYZ235", "AA124", "AmericanAirlines"));
        instance.manifest(new ManifestRequestModel("XYZ236", "AA125", "AmericanAirlines"));
        instance.manifest(new ManifestRequestModel("XYZ237", "AA123", "AmericanAirlines"));
        instance.manifest(new ManifestRequestModel("XYZ238", "AA124", "AmericanAirlines"));
        instance.manifest(new ManifestRequestModel("XYZ239", "AA125", "AmericanAirlines"));
        instance.counterRangeAssignment(new CounterRangeAssignmentRequestModel(3, "C", new ArrayList<>(List.of(new String[]{"AA123", "AA124", "AA125"})), "AmericanAirlines"));
        instance.passengerCheckIn(new PassengerCheckInRequestModel(new AtomicInteger(1), "C", "XYZ234"));
        instance.passengerCheckIn(new PassengerCheckInRequestModel(new AtomicInteger(1), "C", "XYZ235"));
        instance.passengerCheckIn(new PassengerCheckInRequestModel(new AtomicInteger(1), "C", "XYZ236"));
        instance.passengerCheckIn(new PassengerCheckInRequestModel(new AtomicInteger(1), "C", "XYZ237"));
        instance.passengerCheckIn(new PassengerCheckInRequestModel(new AtomicInteger(1), "C", "XYZ238"));
        instance.passengerCheckIn(new PassengerCheckInRequestModel(new AtomicInteger(1), "C", "XYZ239"));

        List<Counter.CounterRecord> resList = new ArrayList<>();
        resList.add(new Counter.CounterRecord(2, "AmericanAirlines", new ArrayList<>(List.of(new String[]{"AA123", "AA124", "AA125"})), 6));
        resList.add(new Counter.CounterRecord(3, "AmericanAirlines", new ArrayList<>(List.of(new String[]{"AA123", "AA124", "AA125"})), 6));
        resList.add(new Counter.CounterRecord(4, "", new ArrayList<>(), 0));

        Assertions.assertThrows(SectorDoesNotExistsException.class, () -> instance.getCounters("A", 1, 1));
        Assertions.assertThrows(InvalidCounterRangeException.class, () -> instance.getCounters("C", 1, 0));
        Assertions.assertEquals(resList, instance.getCounters("C", 2, 5));
    }
    @Test
    void passengerCheckInTest() {
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