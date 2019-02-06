package se.flinker.document.security;

import static se.flinker.document.utils.LogUtil.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.flinker.document.exceptions.InvalidApikeyException;


@Service
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final ApiKeysConfig apiKeysConfig;
    
    @Autowired
    public AuthenticationService(ApiKeysConfig apiKeysConfig) {
        this.apiKeysConfig = apiKeysConfig;
    }
    public void authenticate(String apiKey, String tx) {
        if (!apiKeysConfig.getKeys().contains(apiKey)) {
            debug(tx, "! access denied !", log);
            throw new InvalidApikeyException("invalid apikey: " + apiKey, tx);
        }
        debug(tx, "* access granted *", log);
    }

}
