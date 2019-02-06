package se.flinker.document.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppEnvironment {

    private String host;
    private String env;

    @Autowired
    public AppEnvironment(@Value("${app.host}") String host, @Value("${app.environment}") String env) {
        this.host = host;
        this.env = env;
    }
    
    public boolean isTest() {
        return "test".equals(env);
    }
    public boolean isLocal() {
        return "local".equals(env);
    }
    public boolean isDemo() {
        return "demo".equals(env);
    }
    public boolean isProduction() {
        return "production".equals(env);
    }

    public String getName() {
        return host;
    }
}
