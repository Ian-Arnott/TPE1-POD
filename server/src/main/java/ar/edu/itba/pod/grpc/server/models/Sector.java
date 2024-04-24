package ar.edu.itba.pod.grpc.server.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Sector {
    private final String name;
    // una lista counters, los counters son representados con numeros enteros
    private final Map<Integer, Counter> counterMap;

    public Sector(String name) {
        this.name = name;
        this.counterMap = new ConcurrentHashMap<>();
    }


    public synchronized AtomicInteger addCounters(AtomicInteger lastCounterAdded, int counterAmount) {
        for (int i = 0; i < counterAmount; i++) {
            counterMap.put(lastCounterAdded.getAndIncrement(), new Counter());
        }
        return lastCounterAdded;
    }

    public Map<Integer, Counter> getCounterMap() {
        return counterMap;
    }

    // al mostrar los sectores en el servant de servicio de reservas,
    // hay que devolver un string con los counters
    // me fijo que la diferencia entre un counter y el siguiente sea < 1 y si lo es
    // ultimo counter = ese counter
    // si es < 1 entonces termino esa parte del string,
    // y hago que primer counter = ese counter
    // asi asta terminar de hacer los counters.
    // para el 2.2 asumir que se muestra una linea nueva de counters si para de ser contiguo
}
