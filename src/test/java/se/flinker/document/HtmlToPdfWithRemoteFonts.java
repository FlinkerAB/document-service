package se.flinker.document;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.styledxmlparser.resolver.font.BasicFontProvider;

import se.flinker.document.util.Fixtures;

public class HtmlToPdfWithRemoteFonts {

    public static void main(String[] args) throws IOException {
        String html = "<style>body { font-family: 'Poppins', sans-serif; }</style><h1>Hello Poppins!</h1>";
        String cssUrl = "https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap";
        String dest = "remote_fonts.pdf";

        Map<String, String> downloadedFonts = downloadAndRegisterFonts(cssUrl);

        ConverterProperties converterProperties = new ConverterProperties();
        
        FontProvider fontProvider = new BasicFontProvider();
        for (Map.Entry<String, String> entry : downloadedFonts.entrySet()) {
            fontProvider.addFont(entry.getValue());
        }
        converterProperties.setFontProvider(fontProvider);

        HtmlConverter.convertToPdf(html, new FileOutputStream(dest), converterProperties);
        System.out.println("PDF created: " + dest);
    }

    private static Map<String, String> downloadAndRegisterFonts(String cssUrl) throws IOException {
        Map<String, String> registeredFonts = new HashMap<>();

        // Step 1: Fetch the CSS content from the URL
        // This part requires an HTTP client library (e.g., Apache HttpClient, OkHttp)
        String cssContent = fetchCssContentFromDisc("poppins.css");
//        String cssContent = fetchCssContent(cssUrl);
        if (cssContent == null) {
            System.err.println("Failed to fetch CSS from: " + cssUrl);
            return registeredFonts;
        }

        // Step 2: Extract @font-face rules and font URLs
        Pattern fontFacePattern = Pattern.compile("@font-face\\s*\\{(.*?)\\}", Pattern.DOTALL);
        Matcher fontFaceMatcher = fontFacePattern.matcher(cssContent);

        Set<String> fontFamilys = new HashSet<>();
        while (fontFaceMatcher.find()) {
            String fontFaceBlock = fontFaceMatcher.group(1);
            String fontFamily = extractFontFamily(fontFaceBlock);
            String fontStyle = extractFontStyle(fontFaceBlock);
            String fontWeight = extractFontWeight(fontFaceBlock);
            String fontUrl = extractFontUrl(fontFaceBlock);

            if (fontFamily != null && fontUrl != null) {
                String fileExtension = fontUrl.substring(fontUrl.lastIndexOf('.')); // Extract the file extension
                String localFilename = format("%s_%s_%s%s", fontFamily, fontStyle, fontWeight, fileExtension);
                
                if (fontFamilys.contains(localFilename)) {
                    System.out.printf("[%s] - already downloaded%n", localFilename);
                } else {
                    
                    // Step 3: Download the font file
                    // This part also requires an HTTP client and file handling
//                    String localFontPath = downloadFont(fontUrl, fontFamily + fileExtension); // Use the extracted extension
                    String localFontPath = downloadFont(fontUrl, localFilename); // Use the extracted extension
                    
                    if (localFontPath != null) {
                        registeredFonts.put(localFilename, localFontPath);
                        System.out.println("Downloaded and will register: " + localFilename + " from " + fontUrl + " to " + localFontPath);
                        fontFamilys.add(localFilename);
                    } else {
                        System.err.println("Failed to download font for: " + localFilename + " from " + fontUrl);
                    }
                }
            }
        }

        return registeredFonts;
    }

    public static String fetchCssContentFromDisc(String fixtureName) {
        return Fixtures.load(fixtureName);
    }
    
    public static String fetchCssContent(String cssUrl) {
        StringBuilder content = new StringBuilder();
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(cssUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } else {
                System.err.println("Failed to fetch CSS. HTTP response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return content.toString();
    }


    private static String extractFontFamily(String fontFaceBlock) {
        Pattern fontFamilyPattern = Pattern.compile("font-family\\s*:\\s*['\"]?([^'\";]+)['\"]?");
        Matcher fontFamilyMatcher = fontFamilyPattern.matcher(fontFaceBlock);
        if (fontFamilyMatcher.find()) {
            return fontFamilyMatcher.group(1);
        }
        return null;
    }
    
    private static String extractFontStyle(String fontFaceBlock) {
        Pattern fontFamilyPattern = Pattern.compile("font-style\\s*:\\s*['\"]?([^'\";]+)['\"]?");
        Matcher fontFamilyMatcher = fontFamilyPattern.matcher(fontFaceBlock);
        if (fontFamilyMatcher.find()) {
            return fontFamilyMatcher.group(1);
        }
        return null;
    }
    
    private static String extractFontWeight(String fontFaceBlock) {
        Pattern fontFamilyPattern = Pattern.compile("font-weight\\s*:\\s*['\"]?([^'\";]+)['\"]?");
        Matcher fontFamilyMatcher = fontFamilyPattern.matcher(fontFaceBlock);
        if (fontFamilyMatcher.find()) {
            return fontFamilyMatcher.group(1);
        }
        return null;
    }

    private static String extractFontUrl(String fontFaceBlock) {
        Pattern urlPattern = Pattern.compile("url\\s*\\(['\"]?([^'\"]+)['\"]?\\)");
        Matcher urlMatcher = urlPattern.matcher(fontFaceBlock);
        if (urlMatcher.find()) {
            return urlMatcher.group(1);
        }
        return null;
    }

    private static String downloadFont(String fontUrl, String localFileName) {
        String fontsDir = "fonts"; // Directory to store fonts
        String localFilePath = fontsDir + "/" + localFileName;

        // Create the fonts directory if it doesn't exist
        java.io.File directory = new java.io.File(fontsDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        HttpURLConnection connection = null;
        java.io.InputStream inputStream = null;
        java.io.FileOutputStream outputStream = null;

        try {
            // Open connection to the font URL
            URL url = new URL(fontUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000);

            // Check if the response is OK
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                outputStream = new java.io.FileOutputStream(localFilePath);

                // Read from the input stream and write to the output stream
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                return localFilePath; // Return the local file path
            } else {
                System.err.println("Failed to download font. HTTP response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null; // Return null if the download fails
    }
}