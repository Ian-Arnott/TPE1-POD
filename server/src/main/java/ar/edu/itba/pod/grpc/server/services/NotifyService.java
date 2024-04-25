package ar.edu.itba.pod.grpc.server.services;

import airport.NotifyServiceGrpc;
import airport.NotifyServiceOuterClass;
import ar.edu.itba.pod.grpc.server.repository.AirportRepository;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifyService extends NotifyServiceGrpc.NotifyServiceImplBase {
    private final static Logger logger = LoggerFactory.getLogger(NotifyService.class);
    private final static AirportRepository repository = AirportRepository.getInstance();

    @Override
    public void notifyAirline(NotifyServiceOuterClass.NotifyRequest request, StreamObserver<NotifyServiceOuterClass.Notification> responseObserver) {
        repository.registerForNotifications(request.getAirlineName(), responseObserver);
    }

    @Override
    public void notifyRemoveAirline(NotifyServiceOuterClass.NotifyRequest request, StreamObserver<NotifyServiceOuterClass.NotificationResponse> responseObserver) {
        StreamObserver<NotifyServiceOuterClass.Notification> notificationStreamObserver =
                repository.unregisterForNotification(request.getAirlineName());
        notificationStreamObserver.onCompleted();
        responseObserver.onNext(NotifyServiceOuterClass.NotificationResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
