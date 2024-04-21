package ar.edu.itba.pod.grpc.server.services;

import airport.CheckInServiceGrpc;
import airport.CheckInServiceOuterClass;
import ar.edu.itba.pod.grpc.server.models.requests.PassengerCheckInRequestModel;
import ar.edu.itba.pod.grpc.server.models.response.PassengerCheckInResponseModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckInService extends CheckInServiceGrpc.CheckInServiceImplBase {
    private static final AirportRepository repository = AirportRepository.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(CheckInService.class);

    @Override
    public void passengerCheckIn(CheckInServiceOuterClass.PassengerCheckInRequest request, StreamObserver<CheckInServiceOuterClass.PassengerCheckInResponse> responseObserver) {
        PassengerCheckInRequestModel requestModel = PassengerCheckInRequestModel.fromCheckInRequest(request);

        PassengerCheckInResponseModel responseModel = repository.passengerCheckIn(requestModel);
        responseObserver.onNext(CheckInServiceOuterClass.PassengerCheckInResponse.newBuilder().
                setAirline(responseModel.getAirline()).
                setFlight(responseModel.getFlight())
                .setLastCounter(responseModel.getLastCounter().get()).setPeopleInLIne(responseModel.getPeopleInLine().get()).build());
        responseObserver.onCompleted();
    }
}
