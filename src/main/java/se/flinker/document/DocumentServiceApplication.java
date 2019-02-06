package se.flinker.document;

import static java.lang.String.format;
import static se.flinker.document.utils.LogUtil.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import se.flinker.document.filters.RequestResponseFilter;
import se.flinker.document.utils.AppEnvironment;
import se.flinker.document.utils.LogUtil;


@SpringBootApplication
public class DocumentServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceApplication.class);

    public static String host;
    
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = SpringApplication.run(DocumentServiceApplication.class, args);
        
        host = ctx.getBeanFactory().resolveEmbeddedValue("${app.host}");
        ctx.getBean(LogUtil.class);
        
        AppEnvironment env = ctx.getBean(AppEnvironment.class);
        
        info("start-up", format("App Host: [%s:%s][isProduction: %s]", LogUtil.host, ctx.getEnvironment().resolvePlaceholders("${server.port}"), env.isProduction()), log);
    }
    
    @Bean("app-executor") 
    public AsyncTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(25);
        executor.setThreadNamePrefix("flinker-");
        executor.initialize();
        return executor;
    }
    
    @Bean
    public FilterRegistrationBean regRequestResponseFilter(RequestResponseFilter filter) {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(filter);
        bean.setOrder(0);
        bean.addUrlPatterns("/*");
        return bean;
    }
}
