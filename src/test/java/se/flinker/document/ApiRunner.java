package se.flinker.document;

import static java.util.Collections.emptyMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import se.flinker.document.util.Fixtures;

public class ApiRunner {
    String endpoint = "http://localhost:9812/api/v1/html2pdf";
    String apiKey = "123456";
    
//    String endpoint = "https://documentservice.flinker.net/api/v1/html2pdf";
//    String apiKey = "206a551cfe9c447e9390c3ff43d5995b";

    @Test
    public void test() throws Exception {
        
        Path outputPath = Paths.get("target/generated.pdf");
        RestTemplate rest = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-docservice-api-key", apiKey);
        HttpEntity<Object> requestEntity = new HttpEntity<>(payload(), headers);
        Files.write(outputPath, rest.exchange(endpoint, HttpMethod.POST, requestEntity, byte[].class, emptyMap()).getBody(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        
        Runtime.getRuntime().exec(new String[] {"open", outputPath.toAbsolutePath().toString()});
    }
    
    private static Object payload() throws IOException {
        Map<String, String> payload = new HashMap<>();
        payload.put("html", Fixtures.load("svea.html"));
//        payload.put("html", Fixtures.load("flexbox.html"));
//        payload.put("colormode", "cmyk");
        return payload;
    }
}
