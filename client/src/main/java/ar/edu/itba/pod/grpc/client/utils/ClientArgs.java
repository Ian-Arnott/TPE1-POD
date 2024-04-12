package ar.edu.itba.pod.grpc.client.utils;

public enum ClientArgs {
    SERVER_ADDRESS("serverAddress"),
    ACTION("action"),
    SECTOR("sector"),
    COUNTERS("counters"),
    IN_PATH("inPath"),;

    private final String value;
    ClientArgs(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
