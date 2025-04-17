package se.flinker.document.services;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static se.flinker.document.utils.InstallFontsHandler.downloadFont;
import static se.flinker.document.utils.InstallFontsHandler.extractFontFamily;
import static se.flinker.document.utils.InstallFontsHandler.extractFontUrl;
import static se.flinker.document.utils.InstallFontsHandler.fetchCssContent;
import static se.flinker.document.utils.LogUtil.debug;
import static se.flinker.document.utils.LogUtil.warn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.layout.font.FontProvider;

import se.flinker.document.exceptions.DocumentServiceAppRuntimeException;

@Service
public class Html2PdfService {
    private static final Logger log = LoggerFactory.getLogger(Html2PdfService.class);

    private String fontsDir;
    
    @Autowired
    public Html2PdfService(@Value("${app.fontsDir}") String fontsDir) {
        this.fontsDir = fontsDir;
    }
    
    public byte[] generatePdf(Map<String, String> payload, String tx) {
        try {
            String html = payload.get("html");
            ConverterProperties properties = createProperties(payload, tx);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(html, baos, properties);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new DocumentServiceAppRuntimeException("problems converting html to pdf", tx, e);
        }
    }
    
    private ConverterProperties createProperties(Map<String, String> payload, String tx) {
        ConverterProperties props = new ConverterProperties();
        props.setCharset("utf-8");

        if (nonNull(fontsDir) && fontsDir.trim().length() > 0) {
            try {
                FontProvider fontProvider = new DefaultFontProvider();
                Path path = Paths.get(fontsDir);
                if (Files.exists(path) && Files.isDirectory(path) && Files.list(path).count() > 0) {
                    debug(tx, "adding fonts dir: " + path.toAbsolutePath(), log);
                    fontProvider.addDirectory(fontsDir);
                    props.setFontProvider(fontProvider);
                }
            } catch (IOException e) {
                warn(tx, "Problems adding fonts directory: " + e.getMessage(), log);
            }
        }
        
        if (payload.containsKey("colormode")) {
            if ("cmyk".equalsIgnoreCase(payload.get("colormode"))) {
                props.setCssApplierFactory(new CMYKCssApplierFactory());
            }
        }
        
        return props;
    }

    public void installFonts(Map<String, String> payload, String tx) {
        String url = payload.get("url");
        String cssContent = fetchCssContent(url, tx);
        
        if (cssContent == null) {
            warn(tx, "Failed to fetch CSS from: " + url, log);
        } else {
            Pattern fontFacePattern = Pattern.compile("@font-face\\s*\\{(.*?)\\}", Pattern.DOTALL);
            Matcher fontFaceMatcher = fontFacePattern.matcher(cssContent);

            Set<String> fontFamilies = new HashSet<>();
            while (fontFaceMatcher.find()) {
                String fontFaceBlock = fontFaceMatcher.group(1);
                String fontFamily = extractFontFamily(fontFaceBlock);
                String fontUrl = extractFontUrl(fontFaceBlock);
                if (fontFamily != null && fontUrl != null) {
                    if (fontFamilies.contains(fontFamily)) {
                        debug(tx, format("[%s] - already downloaded", fontFamily), log);
                    } else {
                        String fileExtension = fontUrl.substring(fontUrl.lastIndexOf('.'));
                        String localFontPath = downloadFont(fontsDir, fontUrl, fontFamily + fileExtension, tx);
                        fontFamilies.add(fontFamily);
                        debug(tx, "font installed: " + localFontPath, log);
                    }
                }
            }
        }
    }

}
