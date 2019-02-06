package se.flinker.document.filters;

import static java.lang.String.format;
import static se.flinker.document.utils.LogUtil.debug;
import static se.flinker.document.utils.LogUtil.info;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import se.flinker.document.Tx;
import se.flinker.document.exceptions.InvalidApikeyException;
import se.flinker.document.security.AuthenticationService;



@Component
public class ApiKeyFilter extends OncePerRequestFilter implements Ordered {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

    @Autowired
    private Tx uid;
    @Autowired
    private AuthenticationService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws ServletException, IOException {
        try {
            String apiKey = req.getHeader("X-docservice-api-key");
            debug(uid.tx(), format("check api key: [%s]", apiKey), log);
            authService.authenticate(apiKey, uid.tx());
            chain.doFilter(req, resp);
        } catch (InvalidApikeyException e) {
            info(uid.tx(), format("[reqEnd][%s][exetime:0 ms]", e.httpStatus().value()), log);
            resp.sendError(e.httpStatus().value(), e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 15;
    }
}
