package me.ohughes.cache;

import org.apache.geode.cache.GemFireCache;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.GemfireOperations;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
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
@EnableManager(bindAddress = "0.0.0.0")
@EnableLocator(host = "0.0.0.0")
@EnableSpringWebSession
public class GemfireCacheApplication {

    public static void main(String[] args) {

        new SpringApplicationBuilder(GemfireCacheApplication.class)
                .build()
                .run(args);
    }

    }
