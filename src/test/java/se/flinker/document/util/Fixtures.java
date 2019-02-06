package se.flinker.document.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Fixtures {
    public static String load(String name) {
        return load(name, false);
    }
    
    public static String load(String name, boolean debug) {
        try {
            Path path = Paths.get("src/test/resources/fixtures", name);
            String data = Files.lines(path)
                    .map(String::trim)
                    .collect(Collectors.joining("\n"));
            if (debug) {
                System.err.printf("[FIXTURES LOAD DEBUG START]\n --> %s\n\n%s\n\n[FIXTURES LOAD DEBUG END]\n", path.toAbsolutePath(), data);
            }
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> T loadAs(String name, Class<T> clz) {
        try {
            Path path = Paths.get("src/test/resources/fixtures", name);
            String data = Files.lines(path)
                    .map(String::trim)
                    .collect(Collectors.joining());
            
            ObjectMapper mapper = new ObjectMapper();
            
            return mapper.readValue(data, clz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}