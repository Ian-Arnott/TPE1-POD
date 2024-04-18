package ar.edu.itba.pod.grpc.server.services;

import counter.CounterAssignmentServiceGrpc;
import counter.CounterAssignmentServiceOuterClass;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounterAssignmentService extends CounterAssignmentServiceGrpc.CounterAssignmentServiceImplBase {
    private final static Logger logger = LoggerFactory.getLogger(CounterAssignmentService.class);
    private static final AirportRepository repository = AirportRepository.getInstance();

    @Override
    public void listSectors(Empty request, StreamObserver<CounterAssignmentServiceOuterClass.ListSectorsResponse> responseObserver) {
        CounterAssignmentServiceOuterClass.ListSectorsResponse listSectorsResponse = repository.listSectors(request);
        responseObserver.onNext(listSectorsResponse);
        responseObserver.onCompleted();
    }
}
