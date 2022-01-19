package me.ohughes.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@Configuration
public class GemfireConfig {


    @Bean
    WebSessionIdResolver cookieSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("JSESSIONID"); // <1>
        resolver.addCookieInitializer((builder) -> builder.path("/")); // <2>
        resolver.addCookieInitializer((builder) -> builder.sameSite("Strict")); // <3>
        return resolver;
    }

}
