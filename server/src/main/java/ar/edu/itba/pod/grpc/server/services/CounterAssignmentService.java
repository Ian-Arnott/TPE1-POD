package ar.edu.itba.pod.grpc.server.services;

import airport.CounterAssignmentServiceGrpc;
import airport.CounterAssignmentServiceOuterClass.*;
import ar.edu.itba.pod.grpc.server.models.Airline;
import ar.edu.itba.pod.grpc.server.models.Counter;
import ar.edu.itba.pod.grpc.server.models.Flight;
import airport.CounterAssignmentServiceOuterClass;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class CounterAssignmentService extends CounterAssignmentServiceGrpc.CounterAssignmentServiceImplBase {
    private final static Logger logger = LoggerFactory.getLogger(CounterAssignmentService.class);
    private static final AirportRepository repository = AirportRepository.getInstance();

    @Override
    public void listSectors(Empty request, StreamObserver<CounterAssignmentServiceOuterClass.ListSectorsResponse> responseObserver) {
        Map<String, Set<Integer>> res = repository.listSectors();

        List<CounterAssignmentServiceOuterClass.ListSectorsItem> listSectorsRes = new ArrayList<>();
        res.forEach((sectorName, countersSet) -> listSectorsRes.add(CounterAssignmentServiceOuterClass.ListSectorsItem
                .newBuilder()
                .setSectorName(sectorName)
                .addAllCounters(countersSet)
                .build()));

        CounterAssignmentServiceOuterClass.ListSectorsResponse listSectorsResponse =
                CounterAssignmentServiceOuterClass.ListSectorsResponse.newBuilder().addAllItems(listSectorsRes).build();

        logger.info("SERVER - The listSectors action is finished.");
        responseObserver.onNext(listSectorsResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void listCounters(ListCountersRequest request, StreamObserver<ListCountersResponse> responseObserver) {
        List<Counter> counters = repository.getCounters(request.getSectorName(), request.getFromVal(), request.getToVal());
        ListCountersResponse.Builder listCountersResonseBuilder = ListCountersResponse.newBuilder();
        for (Counter counter : counters) {
            ListCounterItem.Builder listCounterItem = ListCounterItem.newBuilder().setCounterNum(counter.getNum());
            Airline airline = counter.getAirline();
            if (airline != null) {
                listCounterItem.setAirlineName(airline.getName())
                        .addAllFlightCodes(counter.getFlights().stream().map(Flight::getCode).collect(Collectors.toList()))
                        .setPeople(counter.getQueueLength());
            }
            listCountersResonseBuilder.addItems(
                    listCounterItem.build()
            );
        }
        responseObserver.onNext(listCountersResonseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void counterRangeAssignment(CounterRangeAssignmentRequest request, StreamObserver<CounterRangeAssignmentResponse> responseObserver) {
        CounterRangeAssignmentRequestModel requestModel = CounterRangeAssignmentRequestModel.fromCounterRangAssignmentRequest(request);

        CounterRangeAssignmentResponseModel responseModel = repository.counterRangeAssignment(requestModel);
        responseObserver.onNext(CounterAssignmentServiceOuterClass.CounterRangeAssignmentResponse.newBuilder()
                .setAmountCheckingIn(responseModel.getAmountCheckingIn())
                .setAmountPending(responseModel.getAmountPending())
                .setLastCheckingIn(responseModel.getLastCheckingIn())
                .setAmountPendingAhead(responseModel.getAmountPendingAhead()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void freeCounterRange(CounterAssignmentServiceOuterClass.FreeCounterRangeRequest request, StreamObserver<FreeCounterRangeResponse> responseObserver) {
        FreeCounterRangeRequestModel requestModel = FreeCounterRangeRequestModel.fromFreeCounterRequest(request);

        FreeCounterRangeResponseModel responseModel = repository.freeCounterRange(requestModel);
        responseObserver.onNext(CounterAssignmentServiceOuterClass.FreeCounterRangeResponse.newBuilder()
                .addAllFlights(responseModel.getFlights())
                .setFreedAmount(responseModel.getFreedAmount().get()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void performCounterCheckIn(CounterAssignmentServiceOuterClass.PerformCounterCheckInRequest request, StreamObserver<CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse> responseObserver) {
        PerformCounterCheckInRequestModel requestModel = PerformCounterCheckInRequestModel.fromPerformCounterCheckInRequest(request);
        repository.performCounterCheckIn(requestModel, responseObserver);
    }
}
