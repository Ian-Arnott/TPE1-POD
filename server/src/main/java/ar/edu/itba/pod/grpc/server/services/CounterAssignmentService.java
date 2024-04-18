package ar.edu.itba.pod.grpc.server.services;

import airport.CounterAssignmentServiceGrpc;
import airport.CounterAssignmentServiceOuterClass.*;
import ar.edu.itba.pod.grpc.server.models.requests.CounterRangeAssignmentRequestModel;
import ar.edu.itba.pod.grpc.server.models.response.CounterRangeAssignmentResponseModel;
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
    public void counterRangeAssigment(CounterRangeAssigmentRequest request, StreamObserver<CounterRangeAssigmentResponse> responseObserver) {
        CounterRangeAssignmentRequestModel requestModel = CounterRangeAssignmentRequestModel.fromCounterRangAssignmentRequest(request);

        CounterRangeAssignmentResponseModel responseModel = repository.counterRangeAssignment(requestModel);
        responseObserver.onNext(CounterRangeAssigmentResponse.newBuilder()
                .setAmountCheckingIn(responseModel.getAmountCheckingIn())
                .setAmountPending(responseModel.getAmountPending())
                .setLastCheckingIn(responseModel.getLastCheckingIn())
                .setAmountPendingAhead(responseModel.getAmountPendingAhead()).build());
        responseObserver.onCompleted();
    }
}
