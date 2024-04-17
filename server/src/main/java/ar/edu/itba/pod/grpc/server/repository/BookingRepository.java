package ar.edu.itba.pod.grpc.server.repository;

import airport.AdminAirportServiceOuterClass;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookingRepository {

    private static BookingRepository instance;
    private static AirportRepository airportRepository = AirportRepository.getInstance();
    private final Map<String, Map<String,String>> bookings;
    private final Map<String, String> flights;
    private BookingRepository() {
        this.bookings = new ConcurrentHashMap<>();
        this.flights = new ConcurrentHashMap<>();
    }

    public synchronized static BookingRepository getInstance() {
        if (instance == null) {
            instance = new BookingRepository();
        }
        return instance;
    }

    public AdminAirportServiceOuterClass.ManifestResponse manifest(String booking, String flight, String airline) {
        if (this.bookings.containsKey(booking)) {
            return AdminAirportServiceOuterClass.ManifestResponse
                    .newBuilder().setMessage("not added: Booking " + booking + " already exists").build();
        } else {
            if (this.flights.containsKey(flight)) {
                String flightAirline = this.flights.get(flight);
                if (!flightAirline.equals(airline)) {
                    return AdminAirportServiceOuterClass.ManifestResponse
                            .newBuilder().setMessage("not added: Flight " + flight + " belongs to a different airline").build();
                }
            } else {
                this.flights.put(flight, airline);
            }

            this.bookings.put(booking, new ConcurrentHashMap<>());
            this.bookings.get(booking).put(flight,airline);
            return AdminAirportServiceOuterClass.ManifestResponse
                    .newBuilder().setMessage("Booking " + booking + " added successfully").build();
        }
    }

}
