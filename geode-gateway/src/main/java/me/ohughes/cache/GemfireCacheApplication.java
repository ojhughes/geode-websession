package me.ohughes.cache;

import org.apache.geode.cache.GemFireCache;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.GemfireOperations;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.session.Session;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.session.data.gemfire.GemFireOperationsSessionRepository;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.session.data.gemfire.config.annotation.web.http.support.SessionCacheTypeAwareRegionFactoryBean;
import org.springframework.session.data.gemfire.serialization.data.provider.DataSerializableSessionSerializer;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import static org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration.*;

@SpringBootApplication // <1>
@CacheServerApplication(name = "SpringSessionDataGeodeBootSampleServer", logLevel = "error") // <2>
@EnableSpringWebSession
public class GemfireCacheApplication {


    @Bean
    WebSessionIdResolver cookieSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("JSESSIONID"); // <1>
        resolver.addCookieInitializer((builder) -> builder.path("/")); // <2>
        resolver.addCookieInitializer((builder) -> builder.sameSite("Strict")); // <3>
        return resolver;
    }
    @Bean(name = GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME)
    public SessionCacheTypeAwareRegionFactoryBean<Object, Session> sessionRegion(GemFireCache gemfireCache) {
        DataSerializableSessionSerializer.register();

        SessionCacheTypeAwareRegionFactoryBean<Object, Session> sessionRegion =
                new SessionCacheTypeAwareRegionFactoryBean<>();

        sessionRegion.setCache(gemfireCache);
        sessionRegion.setClientRegionShortcut(DEFAULT_CLIENT_REGION_SHORTCUT);
        sessionRegion.setPoolName(DEFAULT_POOL_NAME);
        sessionRegion.setRegionName(DEFAULT_SESSION_REGION_NAME);
        sessionRegion.setServerRegionShortcut(DEFAULT_SERVER_REGION_SHORTCUT);

        return sessionRegion;
    }

    @Bean
    public GemFireOperationsSessionRepository sessionRepository(
             GemfireOperations gemfireOperations) {


        return new GemFireOperationsSessionRepository(gemfireOperations);
    }
    public static void main(String[] args) {

        new SpringApplicationBuilder(GemfireCacheApplication.class)
                .build()
                .run(args);
    }

    }
