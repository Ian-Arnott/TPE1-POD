package ar.edu.itba.pod.grpc.server.services;

import airport.CounterAssignmentServiceOuterClass;
import airport.QueryServiceGrpc;
import airport.QueryServiceOuterClass;
import ar.edu.itba.pod.grpc.server.models.Airline;
import ar.edu.itba.pod.grpc.server.models.Booking;
import ar.edu.itba.pod.grpc.server.models.Counter;
import ar.edu.itba.pod.grpc.server.models.Flight;
import ar.edu.itba.pod.grpc.server.models.response.CounterQueryResponse;
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

        List<CounterQueryResponse> counterQueryResponses = repository.getCountersQuery(request.getSectorName());
        QueryServiceOuterClass.QueryCounterItem.Builder item = QueryServiceOuterClass.QueryCounterItem.newBuilder();
        QueryServiceOuterClass.CounterItem.Builder counterItemBuilder = QueryServiceOuterClass.CounterItem.newBuilder();
        for (CounterQueryResponse counterQueryResponse : counterQueryResponses) {
            if (!counterQueryResponse.counterRecords().isEmpty()) {
                item.setSectorName(counterQueryResponse.sectorName());
                for (Counter.CounterRecord counterRecord : counterQueryResponse.counterRecords()) {
                    counterItemBuilder.setCounterNum(counterRecord.counterCode());
                    String airlineName = counterRecord.airlineName();
                    if (!airlineName.isEmpty()) {
                        counterItemBuilder.setAirlineName(airlineName)
                                .addAllFlightCodes(counterRecord.flightCodes())
                                .setPeople(counterRecord.peopleInLine());
                    }
                    item.addCounters(counterItemBuilder.build());
                    counterItemBuilder.clear();
                }
            }
            response.addQueryList(item);
            item.clear();
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCheckIns(QueryServiceOuterClass.QueryCheckInsRequest request, StreamObserver<QueryServiceOuterClass.QueryCheckInsResponse> responseObserver) {
        QueryServiceOuterClass.QueryCheckInsResponse.Builder response = QueryServiceOuterClass.QueryCheckInsResponse.newBuilder();
        List<Booking.BookingRecord> bookingList = repository.getBookingsQuery(request.getSectorName(), request.getAirlineName());
        List<QueryServiceOuterClass.QueryCheckInItem> list = new ArrayList<>();
        QueryServiceOuterClass.QueryCheckInItem.Builder item = QueryServiceOuterClass.QueryCheckInItem.newBuilder();
        for (Booking.BookingRecord booking : bookingList) {
            item.setSectorName(booking.checkedInInfo().getSector())
                    .setAirlineName(booking.airlineName())
                    .setFlightCode(booking.flightCode())
                    .setCounter(booking.checkedInInfo().getCounter())
                    .setBookingCode(booking.bookingCode());
            list.add(item.build());
            item.clear();
        }
        response.addAllQueryList(list);
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
