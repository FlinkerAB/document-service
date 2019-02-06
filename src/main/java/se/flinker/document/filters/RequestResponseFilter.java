package se.flinker.document.filters;

import static java.lang.String.format;
import static se.flinker.document.utils.LogUtil.info;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import se.flinker.document.Tx;



@Component
public class RequestResponseFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger log = LoggerFactory
            .getLogger(RequestResponseFilter.class);

    @Autowired
    private Tx uid;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
            HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
            req = logStart(req);
            req.setAttribute("flinker-tx", uid.tx());
            chain.doFilter(req, resp);
    }

    private HttpServletRequest logStart(HttpServletRequest req) {
        if (shouldLog(req)) {
            boolean logBody = shouldLogBody(req);
            req = logBody ? new ResettableStreamHttpServletRequest(req) : req;
            info(this.uid.tx(),
                    format("[reqStart][%s %s%s]%s", req.getMethod(), req
                            .getRequestURI(), req.getQueryString() == null ? ""
                            : "?" + req.getQueryString(), logBody ? readBody((ResettableStreamHttpServletRequest) req)
                            : ""), log);
        }
        return req;
    }

    private String readBody(ResettableStreamHttpServletRequest req) {
        try {
            String body = IOUtils.toString(req.getReader());
            req.resetInputStream();
            return format("[body: %s]", body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldLogBody(HttpServletRequest req) {
        if (req.getRequestURI().contains("html2pdf")) {
            return false;
        }
        if ("PUT".equals(req.getMethod()) || "POST".equals(req.getMethod())) {
            return true;
        }
        return false;
    }

    private boolean shouldLog(HttpServletRequest req) {
        return !"/health".equals(req.getRequestURI());
    }

    
    private static class ResettableStreamHttpServletRequest extends
            HttpServletRequestWrapper {

        private byte[] rawData;
        private HttpServletRequest request;
        private ResettableServletInputStream servletStream;

        public ResettableStreamHttpServletRequest(HttpServletRequest request) {
            super(request);
            this.request = request;
            this.servletStream = new ResettableServletInputStream();
        }

        public void resetInputStream() {
            servletStream.stream = new ByteArrayInputStream(rawData);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (rawData == null) {
                rawData = IOUtils.toByteArray(this.request.getReader());
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
            return servletStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (rawData == null) {
                rawData = IOUtils.toByteArray(this.request.getReader());
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
            return new BufferedReader(new InputStreamReader(servletStream));
        }

        private class ResettableServletInputStream extends ServletInputStream {

            private InputStream stream;

            @Override
            public int read() throws IOException {
                return stream.read();
            }

            @Override
            public boolean isFinished() {
                try {
                    return stream.available() < 1;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 15;
    }
}
