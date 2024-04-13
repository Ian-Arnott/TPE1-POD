package ar.edu.itba.pod.grpc.server.services;

import airport.AdminAirportServiceGrpc;
import airport.AdminAirportServiceOuterClass.*;
import com.google.protobuf.BoolValue;
import io.grpc.stub.StreamObserver;

public class AdminAirportService extends AdminAirportServiceGrpc.AdminAirportServiceImplBase {

    @Override
    public void addSector(AddSectorRequest request, StreamObserver<BoolValue> responseObserver) {
        super.addSector(request, responseObserver);
    }

    @Override
    public void addCounters(AddCountersRequest request, StreamObserver<BoolValue> responseObserver) {
        super.addCounters(request, responseObserver);
    }

    @Override
    public StreamObserver<ManifestRequest> manifest(StreamObserver<BoolValue> responseObserver) {
        return super.manifest(responseObserver);
    }
}
