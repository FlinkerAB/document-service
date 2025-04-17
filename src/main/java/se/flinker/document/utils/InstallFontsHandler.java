package se.flinker.document.utils;

import static se.flinker.document.utils.LogUtil.error;
import static se.flinker.document.utils.LogUtil.warn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallFontsHandler {
    private static final Logger log = LoggerFactory.getLogger(InstallFontsHandler.class);

    public static String fetchCssContent(String cssUrl, String tx) {
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
                warn(tx, "Failed to fetch CSS. HTTP response code: " + responseCode, log);
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
                error(tx, "problems fetching css - " + e.getMessage(), e, log);
            }
        }

        return content.toString();
    }
    
    public static String downloadFont(String fontsDir, String fontUrl, String localFileName, String tx) {
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
                warn(tx, "Failed to download font. HTTP response code: " + responseCode, log);
            }
        } catch (Exception e) {
            error(tx, "problems downloading font - " + e.getMessage(), e, log);
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
                error(tx, "problems downloading font - " + e.getMessage(), e, log);
            }
        }

        return null; // Return null if the download fails
    }
    
    public static String extractFontFamily(String fontFaceBlock) {
        Pattern fontFamilyPattern = Pattern.compile("font-family\\s*:\\s*['\"]?([^'\";]+)['\"]?");
        Matcher fontFamilyMatcher = fontFamilyPattern.matcher(fontFaceBlock);
        if (fontFamilyMatcher.find()) {
            return fontFamilyMatcher.group(1);
        }
        return null;
    }

    public static String extractFontUrl(String fontFaceBlock) {
        Pattern urlPattern = Pattern.compile("url\\s*\\(['\"]?([^'\"]+)['\"]?\\)");
        Matcher urlMatcher = urlPattern.matcher(fontFaceBlock);
        if (urlMatcher.find()) {
            return urlMatcher.group(1);
        }
        return null;
    }
}
