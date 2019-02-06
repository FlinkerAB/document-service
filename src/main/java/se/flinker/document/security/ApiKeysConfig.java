package se.flinker.document.security;

import static se.flinker.document.utils.LogUtil.debug;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "api")
public class ApiKeysConfig {
    private static final Logger log = LoggerFactory.getLogger(ApiKeysConfig.class);

    private final List<String> keys = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        debug(getClass().getSimpleName(), "contructed: " + keys.size(), log);
    }
    
    public List<String> getKeys() {
        return keys;
    }
}
