package ar.edu.itba.pod.grpc.server.services;

import airport.AdminAirportServiceGrpc;
import airport.AdminAirportServiceOuterClass.*;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminAirportService extends AdminAirportServiceGrpc.AdminAirportServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(AdminAirportService.class);
    private static final AirportRepository repository = AirportRepository.getInstance();

    @Override
    public void addSector(AddSectorRequest request, StreamObserver<BoolValue> responseObserver) {
        boolean response = repository.addSector(request.getSectorName());
        responseObserver.onNext(BoolValue.of(response));
        responseObserver.onCompleted();
    }

    @Override
    public void addCounters(AddCountersRequest request, StreamObserver<AddCountersResponse> responseObserver) {
        int lastCounterAdded = repository.addCountersToSector(request.getSectorName(), request.getCounterCount());
        responseObserver.onNext(
            AddCountersResponse.newBuilder().setLastCounterAdded(lastCounterAdded).build()
        );
        responseObserver.onCompleted();
    }


    @Override
    public void manifest(ManifestRequest request, StreamObserver<Empty> responseObserver) {
        ManifestRequestModel requestModel = ManifestRequestModel.fromManifestRequest(request);

        repository.manifest(requestModel);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
