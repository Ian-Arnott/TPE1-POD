package ar.edu.itba.pod.grpc.server.services;

import counter.CounterAssignmentServiceGrpc;
import counter.CounterAssignmentServiceOuterClass;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
}
