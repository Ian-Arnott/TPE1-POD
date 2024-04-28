package ar.edu.itba.pod.grpc.server.services;

import airport.CounterAssignmentServiceOuterClass;
import airport.QueryServiceGrpc;
import airport.QueryServiceOuterClass;
import ar.edu.itba.pod.grpc.server.models.Airline;
import ar.edu.itba.pod.grpc.server.models.Booking;
import ar.edu.itba.pod.grpc.server.models.Counter;
import ar.edu.itba.pod.grpc.server.models.Flight;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QueryService extends QueryServiceGrpc.QueryServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);
    private static final AirportRepository repository = AirportRepository.getInstance();

    @Override
    public void queryCounters(QueryServiceOuterClass.QueryCountersRequest request, StreamObserver<QueryServiceOuterClass.QueryCounterResponse> responseObserver) {
        QueryServiceOuterClass.QueryCounterResponse.Builder response = QueryServiceOuterClass.QueryCounterResponse.newBuilder();
        if (!request.getSectorName().isEmpty()){
            QueryServiceOuterClass.QueryCounterItem item = getQueryCounterItems(request.getSectorName());
            response.addQueryList(item);
        } else {
            for (String sectorName : repository.getSectorNames()) {
                QueryServiceOuterClass.QueryCounterItem item = getQueryCounterItems(sectorName);
                response.addQueryList(item);
            }
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private static QueryServiceOuterClass.QueryCounterItem getQueryCounterItems(String sectorName) {
        Collection<Counter> counters = repository.getCountersQuery(sectorName);
        QueryServiceOuterClass.QueryCounterItem.Builder item = QueryServiceOuterClass.QueryCounterItem.newBuilder();
        item.setSectorName(sectorName);
        QueryServiceOuterClass.CounterItem.Builder counterItemBuilder = QueryServiceOuterClass.CounterItem.newBuilder();
        for (Counter counter : counters) {
            counterItemBuilder.setCounterNum(counter.getNum());
            Airline airline = counter.getAirline();
            if (airline != null) {
                counterItemBuilder.setAirlineName(airline.getName())
                        .addAllFlightCodes(counter.getFlights().stream().map(Flight::getCode).collect(Collectors.toList()))
                        .setPeople(counter.getQueueLength());
            }
            item.addCounters(counterItemBuilder.build());
            counterItemBuilder.clear();
        }
        return item.build();
    }

    @Override
    public void queryCheckIns(QueryServiceOuterClass.QueryCheckInsRequest request, StreamObserver<QueryServiceOuterClass.QueryCheckInsResponse> responseObserver) {
        QueryServiceOuterClass.QueryCheckInsResponse.Builder response = QueryServiceOuterClass.QueryCheckInsResponse.newBuilder();
        List<Booking> bookingList = repository.getBookingsQuery(request.getSectorName(), request.getAirlineName());
        List<QueryServiceOuterClass.QueryCheckInItem> list = new ArrayList<>();
        QueryServiceOuterClass.QueryCheckInItem.Builder item = QueryServiceOuterClass.QueryCheckInItem.newBuilder();
        for (Booking booking : bookingList) {
            item.setSectorName(booking.getCheckedInInfo().getSector())
                    .setAirlineName(booking.getFlight().getAirline().getName())
                    .setFlightCode(booking.getFlight().getCode())
                    .setCounter(booking.getCheckedInInfo().getCounter())
                    .setBookingCode(booking.getCode());
            list.add(item.build());
            item.clear();
        }
        response.addAllQueryList(list);
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
