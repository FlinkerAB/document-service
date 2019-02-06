package se.flinker.document.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.source.ByteArrayOutputStream;

import se.flinker.document.exceptions.DocumentServiceAppRuntimeException;

@Service
public class Html2PdfService {

    public byte[] generatePdf(Map<String, String> payload, String tx) {
        try {
            String html = payload.get("html");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ConverterProperties props = new ConverterProperties();
            props.setCharset("utf-8");
            HtmlConverter.convertToPdf(html, baos, props);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new DocumentServiceAppRuntimeException("problems converting html to pdf", tx, e);
        }
    }

}
