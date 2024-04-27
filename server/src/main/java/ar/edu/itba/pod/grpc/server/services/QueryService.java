package ar.edu.itba.pod.grpc.server.services;

import airport.AdminAirportServiceGrpc;
import airport.AdminAirportServiceOuterClass.AddCountersRequest;
import airport.AdminAirportServiceOuterClass.AddCountersResponse;
import airport.AdminAirportServiceOuterClass.AddSectorRequest;
import airport.AdminAirportServiceOuterClass.ManifestRequest;
import airport.QueryServiceGrpc;
import airport.QueryServiceOuterClass;
import ar.edu.itba.pod.grpc.server.models.Sector;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryService extends QueryServiceGrpc.QueryServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);
    private static final AirportRepository repository = AirportRepository.getInstance();

    @Override
    public void queryCounters(QueryServiceOuterClass.QueryCountersRequest request, StreamObserver<QueryServiceOuterClass.QueryCounterResponse> responseObserver) {
        QueryServiceOuterClass.QueryCounterResponse.Builder response = QueryServiceOuterClass.QueryCounterResponse.newBuilder();
        if (!request.getSectorName().isEmpty()){
            response.addAllQueryList(repository.getCountersQuery(request.getSectorName())).build();
        } else {
            for (Sector sector : repository.getSectorMap().values()) {
                response.addAllQueryList(repository.getCountersQuery(sector.getName())).build();
            }
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryCheckIns(QueryServiceOuterClass.QueryCheckInsRequest request, StreamObserver<QueryServiceOuterClass.QueryCheckInsResponse> responseObserver) {
        super.queryCheckIns(request, responseObserver);
    }
}
