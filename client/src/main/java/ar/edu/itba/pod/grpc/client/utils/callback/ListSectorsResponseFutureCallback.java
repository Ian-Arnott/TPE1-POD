package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.CounterAssignmentServiceOuterClass;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class ListSectorsResponseFutureCallback extends AbstractFutureCallback<CounterAssignmentServiceOuterClass.ListSectorsResponse> {
    public ListSectorsResponseFutureCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(CounterAssignmentServiceOuterClass.ListSectorsResponse result) {
        String response = "Sectors \tCounters\n####################";
        System.out.println(response);
        getLatch().countDown();
    }
}
