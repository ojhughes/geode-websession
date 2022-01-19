package me.ohughes.cache;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

@Configuration
public class GeodeLocatorEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    Logger log = LoggerFactory.getLogger(GemfireConfig.class);
    private static final String KUBERNETES_SERVICE_HOST = "KUBERNETES_SERVICE_HOST";
    private static final String KUBERNETES_SERVICE_PORT = "KUBERNETES_SERVICE_PORT";
    private static final String SERVICE_HOST_SUFFIX = "_SERVICE_HOST";
    private static final String SERVICE_PORT_SUFFIX = "_SERVICE_PORT";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isKubernetesPlatform(environment)) {
            try {
                System.out.println("Running locator lookup");
                ApiClient apiClient = ClientBuilder.standard(false).build();
                io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient);
                CoreV1Api coreV1Api = new CoreV1Api(apiClient);
                V1PodList locatorPods = coreV1Api.listNamespacedPod("geode", null, null, null, null, "role=locator", null, null, null, null, null);
                List<String> locatorHosts = locatorPods.getItems().stream().map(v1Pod -> v1Pod.getStatus().getPodIP() + "[10334]").collect(Collectors.toList());
                String locatorProperty = String.join(",", locatorHosts);
                String cacheName = "cache-" + System.getenv("HOSTNAME");
                Map<String, Object> gemfireProperties = Map.of("spring.data.gemfire.locators", locatorProperty, "spring.data.gemfire.cache.name", cacheName);
                environment.getPropertySources()
                        .addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, new MapPropertySource("gemfireLocators", gemfireProperties));
                System.out.println("Gemfire Locators found: " + locatorProperty);

            } catch (IOException | ApiException e) {
                log.warn("unable to configure locator IP using Kubernetes API");
                throw new RuntimeException(e);

            }
        }
    }
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isKubernetesPlatform(ConfigurableEnvironment environment) {
        PropertySource<?> environmentPropertySource = environment.getPropertySources()
                .get(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        if (environmentPropertySource != null) {
            if (environmentPropertySource.containsProperty(KUBERNETES_SERVICE_HOST)
                    && environmentPropertySource.containsProperty(KUBERNETES_SERVICE_PORT)) {
                return true;
            }
            if (environmentPropertySource instanceof EnumerablePropertySource) {
                return isAutoDetected((EnumerablePropertySource<?>) environmentPropertySource);
            }
        }
        return false;
    }
    private boolean isAutoDetected(EnumerablePropertySource<?> environmentPropertySource) {
        for (String propertyName : environmentPropertySource.getPropertyNames()) {
            if (propertyName.endsWith(SERVICE_HOST_SUFFIX)) {
                String serviceName = propertyName.substring(0,
                        propertyName.length() - SERVICE_HOST_SUFFIX.length());
                if (environmentPropertySource.getProperty(serviceName + SERVICE_PORT_SUFFIX) != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
