package ar.edu.itba.pod.grpc.server.services;

import airport.Models;
import airport.NotifyServiceGrpc;
import airport.NotifyServiceOuterClass;
import ar.edu.itba.pod.grpc.server.models.Airline;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

public class NotifyService extends NotifyServiceGrpc.NotifyServiceImplBase {
    private final static Logger logger = LoggerFactory.getLogger(NotifyService.class);
    private final AirportRepository repository;
    public NotifyService() {
        repository = AirportRepository.getInstance();

    }

    @Override
    public void notifyAirline(NotifyServiceOuterClass.NotifyRequest request, StreamObserver<NotifyServiceOuterClass.Notification> responseObserver) {
        repository.registerForNotifications(request.getAirlineName(), responseObserver);
    }

    @Override
    public void notifyRemoveAirline(NotifyServiceOuterClass.NotifyRequest request, StreamObserver<NotifyServiceOuterClass.NotificationResponse> responseObserver) {
        StreamObserver<NotifyServiceOuterClass.Notification> notificationStreamObserver =
                repository.unregisterForNotification(request.getAirlineName());
        notificationStreamObserver.onCompleted();
        responseObserver.onNext(NotifyServiceOuterClass.NotificationResponse.newBuilder()
                .setResponse(Models.SimpleStatusResponse.OK).build());
        responseObserver.onCompleted();
    }
}
