package se.flinker.document.controllers;

import static java.lang.String.format;
import static se.flinker.document.utils.LogUtil.info;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import se.flinker.document.exceptions.DocumentServiceAppRuntimeException;
import se.flinker.document.services.Html2PdfService;


@RestController
@RequestMapping("/api/v1/html2pdf")
public class Html2PdfController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(Html2PdfController.class);
    private Html2PdfService srv;

    @Autowired
    public Html2PdfController(Html2PdfService srv) {
        this.srv = srv;
    }
    
    @PostMapping(produces = "application/pdf")
    public DeferredResult<ResponseEntity<?>> html2pdf(@RequestBody Map<String, String> payload) {
        final String tx = tx();
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
        executor.execute(() -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            HttpStatus status = HttpStatus.OK;
            try {
                info(tx, "html2pdf", log);
                result.setResult(new ResponseEntity<>(
                        srv.generatePdf(payload, tx),
                        status));
            } catch (Exception e) {
                result.setErrorResult(e);
                if (e instanceof DocumentServiceAppRuntimeException) {
                    status = ((DocumentServiceAppRuntimeException) e).httpStatus();
                } else {
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                }
            } finally {
                stopWatch.stop();
                info(tx, format("[reqEnd][%s][exetime:%s ms]", status.value(), stopWatch.getTotalTimeMillis()), log);
            }
        });
        return result;
    }
    
    @PostMapping("/install-fonts")
    public DeferredResult<ResponseEntity<?>> installFonts(@RequestBody Map<String, String> payload) {
        final String tx = tx();
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
        executor.execute(() -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            HttpStatus status = HttpStatus.NO_CONTENT;
            try {
                info(tx, "install-fonts", log);
                srv.installFonts(payload, tx);
                result.setResult(new ResponseEntity<>(status));
            } catch (Exception e) {
                result.setErrorResult(e);
                if (e instanceof DocumentServiceAppRuntimeException) {
                    status = ((DocumentServiceAppRuntimeException) e).httpStatus();
                } else {
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                }
            } finally {
                stopWatch.stop();
                info(tx, format("[reqEnd][%s][exetime:%s ms]", status.value(), stopWatch.getTotalTimeMillis()), log);
            }
        });
        return result;
    }
}
