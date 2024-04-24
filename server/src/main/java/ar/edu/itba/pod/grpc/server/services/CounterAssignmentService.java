package ar.edu.itba.pod.grpc.server.services;

import airport.CounterAssignmentServiceGrpc;
import airport.CounterAssignmentServiceOuterClass.*;
import ar.edu.itba.pod.grpc.server.models.requests.CounterRangeAssignmentRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.FreeCounterRangeRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.PerformCounterCheckInRequestModel;
import ar.edu.itba.pod.grpc.server.models.response.CounterRangeAssignmentResponseModel;
import ar.edu.itba.pod.grpc.server.models.response.FreeCounterRangeResponseModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounterAssignmentService extends CounterAssignmentServiceGrpc.CounterAssignmentServiceImplBase {
    private final static Logger logger = LoggerFactory.getLogger(AdminAirportService.class);
    private static final AirportRepository repository = AirportRepository.getInstance();

    @Override
    public void listSectors(Empty request, StreamObserver<ListSectorsResponse> responseObserver) {
        super.listSectors(request, responseObserver);
    }

    @Override
    public void counterRangeAssignment(CounterRangeAssignmentRequest request, StreamObserver<CounterRangeAssignmentResponse> responseObserver) {
        CounterRangeAssignmentRequestModel requestModel = CounterRangeAssignmentRequestModel.fromCounterRangAssignmentRequest(request);

        CounterRangeAssignmentResponseModel responseModel = repository.counterRangeAssignment(requestModel);
        responseObserver.onNext(CounterRangeAssignmentResponse.newBuilder()
                .setAmountCheckingIn(responseModel.getAmountCheckingIn())
                .setAmountPending(responseModel.getAmountPending())
                .setLastCheckingIn(responseModel.getLastCheckingIn())
                .setAmountPendingAhead(responseModel.getAmountPendingAhead()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void freeCounterRange(FreeCounterRangeRequest request, StreamObserver<FreeCounterRangeResponse> responseObserver) {
        FreeCounterRangeRequestModel requestModel = FreeCounterRangeRequestModel.fromFreeCounterRequest(request);

        FreeCounterRangeResponseModel responseModel = repository.freeCounterRange(requestModel);
        responseObserver.onNext(FreeCounterRangeResponse.newBuilder()
                .addAllFlights(responseModel.getFlights())
                .setFreedAmount(responseModel.getFreedAmount().get()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void performCounterCheckIn(PerformCounterCheckInRequest request, StreamObserver<PerformCounterCheckInResponse> responseObserver) {
        PerformCounterCheckInRequestModel requestModel = PerformCounterCheckInRequestModel.fromPerformCounterCheckInRequest(request);
        repository.performCounterCheckIn(requestModel, responseObserver);
    }
}
