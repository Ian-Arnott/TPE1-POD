package ar.edu.itba.pod.grpc.server.models.response;

import java.util.concurrent.atomic.AtomicInteger;

public record PassengerCheckInResponseModel(
        int lastCounter,
        int peopleInLine,
        String flight,
        String airline
) {
}
