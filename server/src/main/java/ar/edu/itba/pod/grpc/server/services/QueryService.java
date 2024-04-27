package ar.edu.itba.pod.grpc.server.services;

import airport.AdminAirportServiceGrpc;
import airport.AdminAirportServiceOuterClass.AddCountersRequest;
import airport.AdminAirportServiceOuterClass.AddCountersResponse;
import airport.AdminAirportServiceOuterClass.AddSectorRequest;
import airport.AdminAirportServiceOuterClass.ManifestRequest;
import airport.QueryServiceGrpc;
import airport.QueryServiceOuterClass;
import ar.edu.itba.pod.grpc.server.models.Booking;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QueryService extends QueryServiceGrpc.QueryServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);
    private static final AirportRepository repository = AirportRepository.getInstance();

    @Override
    public void queryCounters(QueryServiceOuterClass.QueryCountersRequest request, StreamObserver<QueryServiceOuterClass.QueryCounterResponse> responseObserver) {
        QueryServiceOuterClass.QueryCounterResponse.Builder response = QueryServiceOuterClass.QueryCounterResponse.newBuilder();
        if (!request.getSectorName().isEmpty()){
            response.addAllQueryList(repository.getCountersQuery(request.getSectorName())).build();
        } else {
            for (String sectorName : repository.getSectorNames()) {
                response.addAllQueryList(repository.getCountersQuery(sectorName)).build();
            }
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCheckIns(QueryServiceOuterClass.QueryCheckInsRequest request, StreamObserver<QueryServiceOuterClass.QueryCheckInsResponse> responseObserver) {
        QueryServiceOuterClass.QueryCheckInsResponse.Builder response = QueryServiceOuterClass.QueryCheckInsResponse.newBuilder();
        List<Booking> bookingList = repository.getBookingsQuery(Optional.of(request.getSectorName()), Optional.of(request.getAirlineName()));
        List<QueryServiceOuterClass.QueryCheckInItem> list = new ArrayList<>();
        QueryServiceOuterClass.QueryCheckInItem.Builder item = QueryServiceOuterClass.QueryCheckInItem.newBuilder();
        for (Booking booking : bookingList) {
            item.setSectorName(booking.getCheckedInInfo().getSector().getName())
                    .setAirlineName(booking.getFlight().getAirline().getName())
                    .setFlightCode(booking.getFlight().getCode())
                    .setCounter(booking.getCheckedInInfo().getCounter().getNum())
                    .setBookingCode(booking.getCode());
            list.add(item.build());
            item.clear();
        }
        response.addAllQueryList(list);
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
