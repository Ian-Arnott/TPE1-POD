package ar.edu.itba.pod.grpc.client.utils.callback;

import counter.CounterAssignmentServiceOuterClass;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public class ListSectorsResponseFutureCallback extends AbstractFutureCallback<CounterAssignmentServiceOuterClass.ListSectorsResponse> {
    public ListSectorsResponseFutureCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(CounterAssignmentServiceOuterClass.ListSectorsResponse result) {
        String response = "Sectors \tCounters\n####################\n";
        response += result.getSectorName() + "\t\t\t(" + result.getCounterRangeStart(1) + "-" + (result.getCounterRangeStart(1) + result.getCounterRangeSize(1)) + ")";
        System.out.println(response);
        getLatch().countDown();
    }
}
