// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

import com.microsoft.agentserver.api.HealthApi;
import com.microsoft.agentserver.api.ResponsesApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Spring Boot adapter service for the Azure AI Foundry Agent Server API.
 * <p>
 * Provides static factory methods to build and run a Spring Boot application
 * hosting the given {@link ResponsesApi} implementation, mirroring the
 * {@code JerseyAgentServerAdaptorService} API.
 *
 * <h3>Example usage</h3>
 * <pre>{@code
 * SpringAgentServerAdaptorService.run(myResponsesApi);
 * }</pre>
 *
 * <h3>Custom port</h3>
 * <pre>{@code
 * SpringAgentServerAdaptorService.run("8088", myResponsesApi);
 * }</pre>
 */
public class SpringAgentServerAdaptorService {

    private static final boolean LOG_REQUESTS =
        Boolean.parseBoolean(System.getenv().getOrDefault("CA_LOG_REQUESTS", "false"));

    /**
     * Builds and starts a Spring Boot application hosting the given {@link ResponsesApi}
     * on the default port (8088).
     *
     * @param responsesApi the ResponsesApi instance to be served
     * @return the running Spring application context
     */
    public static ConfigurableApplicationContext run(ResponsesApi responsesApi) {
        return run("8088", responsesApi);
    }

    /**
     * Builds and starts a Spring Boot application hosting the given {@link ResponsesApi}
     * on the specified port.
     *
     * @param port         the port to listen on (e.g. "8088")
     * @param responsesApi the ResponsesApi instance to be served
     * @return the running Spring application context
     */
    public static ConfigurableApplicationContext run(String port, ResponsesApi responsesApi) {
        return run(port, responsesApi, new HealthApi() {
        });
    }

    /**
     * Builds and starts a Spring Boot application hosting the given {@link ResponsesApi}
     * and {@link HealthApi} on the specified port.
     *
     * @param port         the port to listen on (e.g. "8088")
     * @param responsesApi the ResponsesApi instance to be served
     * @param healthApi    the HealthApi instance for health probes
     * @return the running Spring application context
     */
    public static ConfigurableApplicationContext run(String port, ResponsesApi responsesApi, HealthApi healthApi) {
        SpringApplication app = new SpringApplication(AgentServerAutoConfiguration.class);
        app.setDefaultProperties(java.util.Map.of(
            "server.port", port,
            "server.address", "0.0.0.0",
            "spring.mvc.throw-exception-if-no-handler-found", "true"
        ));

        // Register the ResponsesApi and HealthApi beans programmatically
        app.addInitializers(context -> {
            context.getBeanFactory().registerSingleton("responsesApi", responsesApi);
            context.getBeanFactory().registerSingleton("healthApi", healthApi);
        });

        return app.run();
    }

    /**
     * Spring Boot auto-configuration that registers all controllers, filters,
     * and exception handlers for the agent server API.
     */
    @SpringBootApplication
    @Configuration
    static class AgentServerAutoConfiguration {

        @Bean
        public ResponsesController responsesController(ResponsesApi responsesApi) {
            return new ResponsesController(responsesApi);
        }

        @Bean
        public HealthController healthController(HealthApi healthApi) {
            return new HealthController(healthApi);
        }

        @Bean
        public GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }

        @Bean
        public ObjectMapperConfig objectMapperConfig() {
            return new ObjectMapperConfig();
        }

        @Bean
        public StreamRoutingFilter streamRoutingFilter() {
            return new StreamRoutingFilter();
        }

        @Bean
        public SseHeaderFilter sseHeaderFilter() {
            return new SseHeaderFilter();
        }

        @Bean
        public FilterRegistrationBean<Filters.CorsFilter> corsFilter() {
            FilterRegistrationBean<Filters.CorsFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new Filters.CorsFilter());
            registration.addUrlPatterns("/*");
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
            return registration;
        }

        @Bean
        public FilterRegistrationBean<Filters.RequestLoggingFilter> requestLoggingFilter() {
            FilterRegistrationBean<Filters.RequestLoggingFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new Filters.RequestLoggingFilter());
            registration.addUrlPatterns("/*");
            registration.setEnabled(LOG_REQUESTS);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 3);
            return registration;
        }

        @Bean
        public FilterRegistrationBean<Filters.ResponseLoggingFilter> responseLoggingFilter() {
            FilterRegistrationBean<Filters.ResponseLoggingFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new Filters.ResponseLoggingFilter());
            registration.addUrlPatterns("/*");
            registration.setEnabled(LOG_REQUESTS);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 4);
            return registration;
        }
    }
}

