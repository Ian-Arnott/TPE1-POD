package ar.edu.itba.pod.grpc.server.models;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class Counter {
    private String airline;
    private ConcurrentSkipListSet<String> flights;
    private Integer numberOfPassengers;
    private AtomicBoolean isCheckingIn;


    // esta se rellena con data del booking repository con el rpc del 2.3
}
