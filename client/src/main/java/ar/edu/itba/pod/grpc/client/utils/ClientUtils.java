package ar.edu.itba.pod.grpc.client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.*;

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

    public static List<String[]> getCSVData(String path) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("File %s not found", path));
        }
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';').build();

        try (CSVReader reader = new CSVReaderBuilder(fileReader)
                .withSkipLines(1).withCSVParser(parser).build()) {
            return reader.readAll();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error reading CSV file: %s", path));
        }
    }
}
