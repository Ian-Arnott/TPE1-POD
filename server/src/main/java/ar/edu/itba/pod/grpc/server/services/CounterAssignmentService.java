package ar.edu.itba.pod.grpc.server.services;

import airport.CounterAssignmentServiceGrpc;
import airport.CounterAssignmentServiceOuterClass.*;
import ar.edu.itba.pod.grpc.server.models.*;
import airport.CounterAssignmentServiceOuterClass;
import ar.edu.itba.pod.grpc.server.models.requests.CounterRangeAssignmentRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.FreeCounterRangeRequestModel;
import ar.edu.itba.pod.grpc.server.models.requests.PerformCounterCheckInRequestModel;
import ar.edu.itba.pod.grpc.server.models.response.CounterRangeAssignmentResponseModel;
import ar.edu.itba.pod.grpc.server.models.response.FreeCounterRangeResponseModel;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class CounterAssignmentService extends CounterAssignmentServiceGrpc.CounterAssignmentServiceImplBase {
    private final static Logger logger = LoggerFactory.getLogger(CounterAssignmentService.class);
    private static final AirportRepository repository = AirportRepository.getInstance();
    private final NotifyService notifyService;

    public CounterAssignmentService() {
        notifyService = new NotifyService();
    }

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

        if (responseModel.getAmountPendingAhead() != 0) {
            notifyService.notifyPendingAssignment(
                    request.getAirlineName(),
                    request.getCountVal(),
                    request.getFlightList(),
                    request.getSectorName(),
                    responseModel.getAmountPendingAhead()
            );
        } else {
            notifyService.notifyAssignedRange(
                    request.getAirlineName(),
                    request.getCountVal(),
                    responseModel.getLastCheckingIn(),
                    request.getFlightList(),
                    request.getSectorName()
            );
        }
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
        List<PendingAssignment> pendingAssignments = repository.resolvePending(request.getSectorName());
        notifyService.notifyPendingAssignments(pendingAssignments, request.getSectorName());


        notifyService.notifyFreeCounterRange(request.getAirline(), responseModel.getFlights(), requestModel.getFromVal(), responseModel.getFreedAmount(), request.getSectorName());

        responseObserver.onNext(CounterAssignmentServiceOuterClass.FreeCounterRangeResponse.newBuilder()
                .addAllFlights(responseModel.getFlights())
                .setFreedAmount(responseModel.getFreedAmount().get()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void performCounterCheckIn(CounterAssignmentServiceOuterClass.PerformCounterCheckInRequest request, StreamObserver<CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse> responseObserver) {
        PerformCounterCheckInRequestModel requestModel = PerformCounterCheckInRequestModel.fromPerformCounterCheckInRequest(request);
        List<Booking> bookingList = repository.performCounterCheckIn(requestModel.getSectorName(), request.getFromVal(), request.getAirlineName());
        int i = request.getFromVal();
        for (Booking booking : bookingList) {
            if (booking == null) {
                responseObserver.onNext(CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse.newBuilder()
                        .setSuccessful(false).setCounter(i).build());
            } else {
                responseObserver.onNext(CounterAssignmentServiceOuterClass.PerformCounterCheckInResponse.newBuilder()
                        .setCounter(i)
                        .setBooking(booking.getCode()).setFlight(booking.getFlight().getCode())
                        .setSuccessful(true)
                        .build());
                notifyService.notifyPassengerCheckIn(booking, i, request.getSectorName());
            }
            i++;
        }
        responseObserver.onCompleted();
    }

    @Override
    public void listPendingAssignments(StringValue request, StreamObserver<CounterAssignmentServiceOuterClass.ListPendingAssignmentsResponse> responseObserver) {
        ConcurrentLinkedQueue<PendingAssignment> res = repository.listPendingAssignments(request);

        List<CounterAssignmentServiceOuterClass.ListPendingAssignmentsItem> listPendingAssignmentsRes = new ArrayList<>();
        res.forEach((pendingAssignment) -> listPendingAssignmentsRes.add(CounterAssignmentServiceOuterClass.ListPendingAssignmentsItem
                .newBuilder()
                .setCountersAmount(pendingAssignment.getCountVal().get())
                .setAirlineName(pendingAssignment.getAirlineName())
                .addAllFlights(pendingAssignment.getFlights().stream().map(Flight::getCode).toList())
                .build()));

        CounterAssignmentServiceOuterClass.ListPendingAssignmentsResponse listPendingAssignmentsResponse =
                CounterAssignmentServiceOuterClass.ListPendingAssignmentsResponse.newBuilder().addAllItems(listPendingAssignmentsRes).build();

        logger.info("SERVER - The listPendingAssignments action is finished.");
        responseObserver.onNext(listPendingAssignmentsResponse);
        responseObserver.onCompleted();
    }
}
