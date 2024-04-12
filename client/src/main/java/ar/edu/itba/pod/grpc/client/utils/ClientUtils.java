package ar.edu.itba.pod.grpc.client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class ClientUtils {
    private static final Logger logger = LoggerFactory.getLogger(ClientUtils.class);

    public static Map<String,String> parseArgs(String[] args) {
        Map<String,String> map = new HashMap<>();
        for (String arg : args) {
            String[] split = arg.substring(2).split("=");
            if (split.length == 2) {
                map.put(split[0], split[1]);
            }
        }
        return map;
    }

    public static void checkNullArgs(String arg, String msg) {
        if (arg == null) {
            logger.error(msg);
            System.exit(1);
        }
    }
}
