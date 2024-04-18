package ar.edu.itba.pod.grpc.server;

import ar.edu.itba.pod.grpc.server.exeptions.GlobalExceptionHandlerInterceptor;
import ar.edu.itba.pod.grpc.server.services.AdminAirportService;
import ar.edu.itba.pod.grpc.server.services.CounterAssignmentService;
import io.grpc.BindableService;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Function;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private static final Function<BindableService, ServerServiceDefinition> handler =
            service -> ServerInterceptors.intercept(service, new GlobalExceptionHandlerInterceptor());

    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info(" Server Starting ...");
        int port = 50051;
        io.grpc.Server server = ServerBuilder.forPort(port)
                .addService(handler.apply(new AdminAirportService()))
                .addService(handler.apply(new CounterAssignmentService()))
                .build();
        server.start();
        logger.info("Server started, listening on " + port);
        server.awaitTermination();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            logger.info("Server shut down");
        }));
    }}
