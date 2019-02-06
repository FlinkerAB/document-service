package se.flinker.document.consumer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.springframework.web.client.RestTemplate;

public class Html2PdfConsumer {

    public static void main(String[] args) throws Exception {
        Path path = Paths.get(System.getProperty("user.home"),".html2pdf");
        path.toFile().createNewFile();
        
        Properties props = new Properties();
        props.load(Files.newBufferedReader(path));
        
        String endpoint = read("service endpoint", props.getProperty("endpoint", "https://pdfservice.flinker.net/api/v1/html2pdf"));
        String input = read("input file", props.getProperty("inputFile"));
        String output = read("output file", props.getProperty("outputFile"));
        
        props.setProperty("endpoint", endpoint);
        props.setProperty("inputFile", input);
        props.setProperty("outputFile", output);
        props.store(Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING), "");
        
        Path outputPath = Paths.get(output);
        RestTemplate rest = new RestTemplate();
        Files.write(outputPath, rest.postForObject(endpoint, payload(input), byte[].class), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Runtime.getRuntime().exec(new String[] {"open", outputPath.toAbsolutePath().toString()});
    }

    private static Object payload(String file) throws IOException {
        Map<String, String> payload = new HashMap<>();
        Path path = Paths.get(file);
        payload.put("html", new String(Files.readAllBytes(path), "utf-8"));
        return payload;
    }
    
    private static Scanner scanner = new Scanner(System.in);
    private static String read(String msg, String defaultValue) {
        String format = "%s";
        if (defaultValue != null) {
            format += " [" + defaultValue + "]";
        }
        format += " > ";
        System.out.printf(format, msg, defaultValue);
        String input = scanner.nextLine();
        if (input.trim().length() == 0 && defaultValue != null) {
            return defaultValue;
        }
        return input;
    }
}
