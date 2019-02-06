package se.flinker.document.controllers;

import static java.lang.String.format;
import static se.flinker.document.utils.LogUtil.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import se.flinker.document.exceptions.DocumentServiceAppRuntimeException;




@RestController
@RequestMapping("/ping")
public class PingController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(PingController.class);
    
    @GetMapping
    public DeferredResult<ResponseEntity<?>> ping2() {
        final String tx = tx();
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
        executor.execute(() -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            HttpStatus status = HttpStatus.OK;
            try {
                info(tx, "PING", log);
                result.setResult(new ResponseEntity<>(                        
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

    
}
