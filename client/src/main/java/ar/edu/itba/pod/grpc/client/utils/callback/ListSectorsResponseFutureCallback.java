package ar.edu.itba.pod.grpc.client.utils.callback;

import airport.CounterAssignmentServiceOuterClass;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ListSectorsResponseFutureCallback extends AbstractFutureCallback<CounterAssignmentServiceOuterClass.ListSectorsResponse> {
    public ListSectorsResponseFutureCallback(Logger logger, CountDownLatch latch) {
        super(logger, latch);
    }

    @Override
    public void onSuccess(CounterAssignmentServiceOuterClass.ListSectorsResponse result) {
        printOutput(result);
        getLatch().countDown();
    }

    public static void printOutput(CounterAssignmentServiceOuterClass.ListSectorsResponse res) {
        System.out.println("Sectors   Counters");
        System.out.println("###################");

        for (CounterAssignmentServiceOuterClass.ListSectorsItem item : res.getItemsList()) {
            String sector = item.getSectorName();
            List<Integer> counters = item.getCountersList();

            StringBuilder counterString = new StringBuilder();
            if (counters.isEmpty()) {
                counterString.append("-");
            } else {
                for (int i = 0; i < counters.size(); i++) {
                    int start = counters.get(i);
                    int end = start;

                    while (i + 1 < counters.size() && counters.get(i + 1) == end + 1) {
                        end = counters.get(i + 1);
                        i++;
                    }

                    counterString.append("(").append(start).append("-").append(end).append(")");
                }
            }

            System.out.printf("%-10s%s%n", sector, counterString);
        }
    }
}
