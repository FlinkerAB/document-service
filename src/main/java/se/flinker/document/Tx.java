package se.flinker.document;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Tx {

    private String tx;

    @PostConstruct
    public void postConstruct() {
        tx = UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Tx [value=").append(tx).append("]")
            .append(" ---> ")
            .append(super.toString());
        return builder.toString();
    }
    
    public String tx() {
        return tx;
    }
}
