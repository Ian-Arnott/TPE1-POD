package ar.edu.itba.pod.grpc.client.utils;

public enum ClientArgs {
    SERVER_ADDRESS("serverAddress"),
    ACTION("action"),
    SECTOR("sector"),
    COUNTERS("counters"),
    IN_PATH("inPath"),
    COUNTER_FROM("counterFrom"),
    COUNTER_TO("counterTo"),
    FLIGHTS("flights"),
    AIRLINE("airline"),
    COUNTER("counter"),
    BOOKING("booking"),
    COUNTER_COUNT("counterCount"),
    OUT_PATH("outPath");

    private final String value;
    ClientArgs(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
