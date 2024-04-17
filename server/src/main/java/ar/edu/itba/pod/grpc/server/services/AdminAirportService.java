package ar.edu.itba.pod.grpc.server.services;

import airport.AdminAirportServiceGrpc;
import airport.AdminAirportServiceOuterClass.*;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import com.google.protobuf.BoolValue;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminAirportService extends AdminAirportServiceGrpc.AdminAirportServiceImplBase {
    private final static Logger logger = LoggerFactory.getLogger(AdminAirportService.class);
    private static final AirportRepository repository = AirportRepository.getInstance();

    @Override
    public void addSector(AddSectorRequest request, StreamObserver<BoolValue> responseObserver) {
        super.addSector(request, responseObserver);
    }

    @Override
    public void addCounters(AddCountersRequest request, StreamObserver<AddCountersResponse> responseObserver) {
        super.addCounters(request, responseObserver);
    }

    @Override
    public void manifest(ManifestRequest request, StreamObserver<ManifestResponse> responseObserver) {
        ManifestRequestModel requestModel = ManifestRequestModel.fromManifestRequest(request);

        ManifestResponse manifestResponse = repository.manifest(requestModel);
        responseObserver.onNext(manifestResponse);
        responseObserver.onCompleted();
    }
}
