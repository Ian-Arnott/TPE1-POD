package ar.edu.itba.pod.grpc.server.exeptions;

import com.google.rpc.Code;
import io.grpc.*;
import io.grpc.protobuf.StatusProto;

import java.util.Map;


public class GlobalExceptionHandlerInterceptor implements ServerInterceptor {

    @Override
    public <T, R> ServerCall.Listener<T> interceptCall(
            ServerCall<T, R> serverCall, Metadata headers, ServerCallHandler<T, R> serverCallHandler) {
        ServerCall.Listener<T> delegate = serverCallHandler.startCall(serverCall, headers);
        return new ExceptionHandler<>(delegate, serverCall, headers);
    }

    private static class ExceptionHandler<T, R> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<T> {

        private final ServerCall<T, R> delegate;
        private final Metadata headers;

        ExceptionHandler(ServerCall.Listener<T> listener, ServerCall<T, R> serverCall, Metadata headers) {
            super(listener);
            this.delegate = serverCall;
            this.headers = headers;
        }

        @Override
        public void onHalfClose() {
            try {
                super.onHalfClose();
            } catch (RuntimeException ex) {
                handleException(ex, delegate, headers);
            }
        }

        private static final Map<Class<? extends Throwable>, Code> errorCodesByException = Map.ofEntries(
                Map.entry(IllegalArgumentException.class, Code.INVALID_ARGUMENT),
                Map.entry(SectorAlreadyExistsException.class, Code.ALREADY_EXISTS),
                Map.entry(SectorDoesNotExistsException.class, Code.NOT_FOUND),
                Map.entry(NonPositiveCounterException.class, Code.INVALID_ARGUMENT),
                Map.entry(BookingAlreadyExistsException.class, Code.ALREADY_EXISTS),
                Map.entry(BookingDoesNotExistException.class, Code.NOT_FOUND),
                Map.entry(FlightExistsForOtherAirlineException.class, Code.ALREADY_EXISTS),
                Map.entry(FlightDoesNotExistsException.class, Code.NOT_FOUND),
                Map.entry(FlightDoesNotHaveBookingsException.class, Code.ABORTED),
                Map.entry(FlightStatusException.class, Code.ABORTED),
                Map.entry(CounterIsCheckingInOtherAirlineException.class, Code.ABORTED),
                Map.entry(CounterIsNotFirstInRangeException.class, Code.ABORTED),
                Map.entry(CountersAreNotAssignedException.class, Code.ABORTED),
                Map.entry(StillCheckingInBookingsException.class, Code.ABORTED),
                Map.entry(InvalidCounterRangeException.class, Code.INVALID_ARGUMENT)
        );

        private void handleException(RuntimeException exception, ServerCall<T, R> serverCall, Metadata headers) {
            Throwable error = exception;
            if (!errorCodesByException.containsKey(error.getClass())) {
                // Si la excepción vino "wrappeada" entonces necesitamos preguntar por la causa.
                error = error.getCause();
                if (error == null || !errorCodesByException.containsKey(error.getClass())) {
                    // Una excepción NO esperada.
                    serverCall.close(Status.UNKNOWN, headers);
                    return;
                }
            }
            // Una excepción esperada.
            com.google.rpc.Status rpcStatus = com.google.rpc.Status.newBuilder()
                    .setCode(errorCodesByException.get(error.getClass()).getNumber())
                    .setMessage(error.getMessage())
                    .build();
            StatusRuntimeException statusRuntimeException = StatusProto.toStatusRuntimeException(rpcStatus);
            Status newStatus = Status.fromThrowable(statusRuntimeException);
            serverCall.close(newStatus, headers);
        }
    }

}