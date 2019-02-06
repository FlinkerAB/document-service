package se.flinker.document.utils;

import static java.lang.String.format;

import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LogUtil {
    private final static ObjectMapper mapper = new ObjectMapper();
    
    @Value("${app.host}")
    public static String host = "n/a";

    @Autowired
    public LogUtil(@Value("${app.host}") String appHost) {
        LogUtil.host = appHost;
    }
    
    public static void info(String txId, String msg, Logger log) {
        if (log.isInfoEnabled()) {
            log.info("[{}][{}] - {}",
                    LogUtil.host, 
                    txId, 
                    msg);
        }
    }
    
    public static void debug(String txId, String msg, Logger log) {
        if (log.isDebugEnabled()) {
            log.debug("[{}][{}] - {}",
                    LogUtil.host, 
                    txId, 
                    msg);
        }
    }
    
    public static void error(String txId, String msg, Throwable ex, Logger log) {
        if (log.isErrorEnabled()) {
            log.error(format("[%s][%s] - %s",
                    LogUtil.host, 
                    txId, 
                    msg), ex);
        }
    }
    
    public static void warn(String txId, String msg, Logger log) {
        if (log.isWarnEnabled()) {
            log.warn("[{}][{}] - {}",
                    LogUtil.host, 
                    txId, 
                    msg);
        }
    }
    
    public static void debugObjectAsJson(String txId, Object obj, Logger log) {
        if (log.isDebugEnabled()) {
            try {
                log.debug("[{}][{}] - {}",
                        host, 
                        txId, 
                        mapper.writeValueAsString(obj));
            } catch (JsonProcessingException e) {
                debug(txId, "problems writing debugObjectAsJson: " + e.getMessage(), log);
            }
        }
    }
    
    public static String listOfObjectsToString(List<?> lst) {
        StringJoiner j = new StringJoiner(", ");
        lst.forEach(item -> {j.add(item.toString());});
        return j.toString();
    }
}
