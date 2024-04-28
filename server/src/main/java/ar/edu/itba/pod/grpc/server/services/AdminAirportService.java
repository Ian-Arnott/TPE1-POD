package ar.edu.itba.pod.grpc.server.services;

import airport.AdminAirportServiceGrpc;
import airport.AdminAirportServiceOuterClass.*;
import ar.edu.itba.pod.grpc.server.models.PendingAssignment;
import ar.edu.itba.pod.grpc.server.models.requests.ManifestRequestModel;
import ar.edu.itba.pod.grpc.server.models.response.AddCountersResponseModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AdminAirportService extends AdminAirportServiceGrpc.AdminAirportServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(AdminAirportService.class);
    private static final AirportRepository repository = AirportRepository.getInstance();
    private final NotifyService notifyService;

    public AdminAirportService() {
        notifyService = new NotifyService();
    }

    @Override
    public void addSector(AddSectorRequest request, StreamObserver<Empty> responseObserver) {
        repository.addSector(request.getSectorName());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void addCounters(AddCountersRequest request, StreamObserver<AddCountersResponse> responseObserver) {
        AddCountersResponseModel addCountersResponse = repository.addCountersToSector(request.getSectorName(), request.getCounterCount());

        notifyService.notifyPendingAssignments(addCountersResponse.pendingAssignmentList(), request.getSectorName());

        responseObserver.onNext(
            AddCountersResponse.newBuilder().setLastCounterAdded(addCountersResponse.lastCounterAdded()).build()
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
