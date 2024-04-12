package ar.edu.itba.pod.grpc.client.utils.callback;

import com.google.common.util.concurrent.FutureCallback;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

public abstract class AbstractFutureCallback<V> implements FutureCallback<V> {

    private final Logger logger;
    private final CountDownLatch latch;

    protected AbstractFutureCallback(Logger logger, CountDownLatch latch) {
        if (logger == null || latch == null) {
            throw new IllegalArgumentException("Must provide a non-null Latch and Logger");
        }
        this.logger = logger;
        this.latch = latch;
    }

    @Override
    public void onSuccess(V result) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void onFailure(Throwable t) {
        latch.countDown();
        logger.error(t.getMessage());
    }

    public Logger getLogger() {
        return logger;
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
