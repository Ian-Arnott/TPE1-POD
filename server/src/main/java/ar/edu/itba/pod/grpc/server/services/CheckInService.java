package ar.edu.itba.pod.grpc.server.services;

import airport.CheckInServiceGrpc;
import airport.CheckInServiceOuterClass;
import ar.edu.itba.pod.grpc.server.models.requests.PassengerCheckInRequestModel;
import ar.edu.itba.pod.grpc.server.models.response.FetchCounterResponseModel;
import ar.edu.itba.pod.grpc.server.models.response.PassengerCheckInResponseModel;
import ar.edu.itba.pod.grpc.server.models.response.PassengerStatusResponseModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckInService extends CheckInServiceGrpc.CheckInServiceImplBase {
    private static final AirportRepository repository = AirportRepository.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(CheckInService.class);
    private final NotifyService notifyService;

    public CheckInService() {
        notifyService = new NotifyService();
    }

    @Override
    public void fetchCounter(StringValue request, StreamObserver<CheckInServiceOuterClass.FetchCounterResponse> responseObserver) {
        FetchCounterResponseModel res = repository.fetchCounter(request.getValue());

        CheckInServiceOuterClass.FetchCounterResponse fetchCounterResponse =
                CheckInServiceOuterClass.FetchCounterResponse.newBuilder()
                        .setFlightCode(res.getFlightCode())
                        .setAirlineName(res.getAirlineName())
                        .addAllCounters(res.getCounters())
                        .setSectorName(res.getSectorName())
                        .setPeopleAmountInLine(res.getPeopleAmountInLine()).build();

        responseObserver.onNext(fetchCounterResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void passengerCheckIn(CheckInServiceOuterClass.PassengerCheckInRequest request, StreamObserver<CheckInServiceOuterClass.PassengerCheckInResponse> responseObserver) {
        PassengerCheckInRequestModel requestModel = PassengerCheckInRequestModel.fromCheckInRequest(request);

        PassengerCheckInResponseModel responseModel = repository.passengerCheckIn(requestModel);
            notifyService.notifyBookingInQueue(
                    responseModel.airline(),
                    request.getBooking(),
                    responseModel.flight(),
                    responseModel.peopleInLine(),
                    request.getFirstCounter(),
                    responseModel.lastCounter(),
                    requestModel.getSectorName()
            );

        responseObserver.onNext(CheckInServiceOuterClass.PassengerCheckInResponse.newBuilder().
                setAirline(responseModel.airline()).
                setFlight(responseModel.flight())
                .setLastCounter(responseModel.lastCounter()).setPeopleInLIne(responseModel.peopleInLine()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void passengerStatus(StringValue request, StreamObserver<CheckInServiceOuterClass.PassengerStatusResponse> responseObserver) {
        PassengerStatusResponseModel res = repository.passengerStatus(request.getValue());

        CheckInServiceOuterClass.PassengerStatusResponse passengerStatusResponse =
                CheckInServiceOuterClass.PassengerStatusResponse.newBuilder()
                        .setIsCheckingIn(res.isCheckingIn())
                        .setIsCheckedIn(res.isCheckedIn())
                        .setFlightCode(res.getFlightCode())
                        .setAirlineName(res.getAirlineName())
                        .setCounterOfCheckIn(res.getCounterOfCheckIn())
                        .addAllCountersForCheckingIn(res.getCountersForCheckingIn())
                        .setSectorName(res.getSectorName())
                        .setPeopleAmountInLine(res.getPeopleAmountInLine())
                        .build();

        responseObserver.onNext(passengerStatusResponse);
        responseObserver.onCompleted();
    }
}
