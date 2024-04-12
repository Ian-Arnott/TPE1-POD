package ar.edu.itba.pod.grpc.client.utils.callback;

import com.google.protobuf.BoolValue;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class BoolValueFutureCallback extends AbstractFutureCallback<BoolValue> {

    private final AtomicInteger couldAdd;
    private final AtomicInteger couldNotAdd;

    public BoolValueFutureCallback(Logger logger, CountDownLatch latch, AtomicInteger couldAdd, AtomicInteger couldNotAdd) {
        super(logger, latch);
        this.couldAdd = couldAdd;
        this.couldNotAdd = couldNotAdd;
    }

    @Override
    public void onSuccess(BoolValue result) {
        if (result.getValue()) {
            couldAdd.incrementAndGet();
        }
        else {
            couldNotAdd.incrementAndGet();
        }
        getLatch().countDown();
    }
}
