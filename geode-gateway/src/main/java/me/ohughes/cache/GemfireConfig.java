package me.ohughes.cache;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

@Configuration
public class GemfireConfig implements EnvironmentPostProcessor {

    Logger log = LoggerFactory.getLogger(GemfireConfig.class);
    @Bean
    WebSessionIdResolver cookieSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("JSESSIONID"); // <1>
        resolver.addCookieInitializer((builder) -> builder.path("/")); // <2>
        resolver.addCookieInitializer((builder) -> builder.sameSite("Strict")); // <3>
        return resolver;
    }
    @Bean
    public ApiClient apiClient() throws IOException {
        ApiClient apiClient = ClientBuilder.standard(false).build();
//        apiClient.setHttpClient(apiClient.getHttpClient()
//                .newBuilder()
//                .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
//                .readTimeout(Duration.ZERO)
//                .pingInterval(1, TimeUnit.MINUTES)
//                .build());

        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient);
        return apiClient;
    }

    @Bean
    AppsV1Api appsV1Api(ApiClient client) {
        return new AppsV1Api(client);
    }

    @Bean
    CoreV1Api coreV1Api(ApiClient client) {
        return new CoreV1Api(client);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            log.info("Running locator lookup");
            ApiClient apiClient = ClientBuilder.standard(false).build();
            io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient);
            AppsV1Api appsV1Api = new AppsV1Api(apiClient);
            CoreV1Api coreV1Api = new CoreV1Api(apiClient);
            V1PodList locatorPods = coreV1Api.listNamespacedPod("geode", null, null, null, null, "role=locator", null, null, null, null, null);
//            List<String> locatorHosts = locatorPods
//                    .getItems()
//                    .stream()
//                    .flatMap(v1Pod -> {
//                        List<V1Container> containers = v1Pod
//                                .getSpec()
//                                .getContainers();
//                        return containers
//                                .stream()
//                                .flatMap(v1Container -> v1Container
//                                        .getPorts()
//                                        .stream()
//                                        .map(v1ContainerPort -> v1ContainerPort.getHostIP() + "[10334]"));
//                    })
//                    .collect(Collectors.toList());
            List<String> locatorHosts = locatorPods.getItems().stream().map(v1Pod -> v1Pod.getStatus().getPodIP() + "[10334]").collect(Collectors.toList());
            String locatorProperty = String.join(",", locatorHosts);
            environment.getPropertySources()
                    .addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, new MapPropertySource("gemfireLocators", Collections.singletonMap("spring.data.gemfire.locators", locatorProperty)));

        } catch (IOException | ApiException e) {
            log.warn("unable to configure locator IP using Kubernetes API");

        }
//        apiClient.setHttpClient(apiClient.getHttpClient()
//                .newBuilder()
//                .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
//                .readTimeout(Duration.ZERO)
//                .pingInterval(1, TimeUnit.MINUTES)
//                .build());

        PropertySource<?> system = environment.getPropertySources()
                .get(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);

    }
//    @Bean(name = GemFireHttpSessionConfiguration.DEFAULT_SESSION_REGION_NAME)
//    public SessionCacheTypeAwareRegionFactoryBean<Object, Session> sessionRegion(GemFireCache gemfireCache) {
//        DataSerializableSessionSerializer.register();
//
//        SessionCacheTypeAwareRegionFactoryBean<Object, Session> sessionRegion =
//                new SessionCacheTypeAwareRegionFactoryBean<>();
//
//        sessionRegion.setCache(gemfireCache);
//        sessionRegion.setClientRegionShortcut(DEFAULT_CLIENT_REGION_SHORTCUT);
//        sessionRegion.setPoolName(DEFAULT_POOL_NAME);
//        sessionRegion.setRegionName(DEFAULT_SESSION_REGION_NAME);
//        sessionRegion.setServerRegionShortcut(DEFAULT_SERVER_REGION_SHORTCUT);
//
//        return sessionRegion;
//    }
//
//    @Bean
//    public GemFireOperationsSessionRepository sessionRepository(
//            GemfireOperations gemfireOperations) {
//
//
//        return new GemFireOperationsSessionRepository(gemfireOperations);
//    }
}
