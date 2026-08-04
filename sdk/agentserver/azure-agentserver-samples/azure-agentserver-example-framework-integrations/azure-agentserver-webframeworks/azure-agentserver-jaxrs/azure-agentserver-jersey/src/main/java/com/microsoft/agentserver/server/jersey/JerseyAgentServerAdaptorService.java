package com.microsoft.agentserver.server.jersey;

import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.api.jaxrs.ApiExceptionMapper;
import com.microsoft.agentserver.api.jaxrs.HealthResource;
import com.microsoft.agentserver.api.jaxrs.InboundRequestLoggingFilter;
import com.microsoft.agentserver.api.jaxrs.ObjectMapperProvider;
import com.microsoft.agentserver.api.jaxrs.PlatformHeaderResponseFilter;
import com.microsoft.agentserver.api.jaxrs.ResponsesResource;
import com.microsoft.agentserver.api.jaxrs.RoutingFilter;
import com.microsoft.agentserver.api.jaxrs.SseResponseFilter;
import jakarta.enterprise.context.ApplicationScoped;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.inject.hk2.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.media.sse.internal.SseBinder;
import org.glassfish.jersey.media.sse.internal.SseEventSinkValueParamProvider;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class JerseyAgentServerAdaptorService {

    private static final Boolean LOG_REQUESTS = Boolean.parseBoolean(System.getenv().getOrDefault("CA_LOG_REQUESTS", "false"));

    public static class AgentBinder extends AbstractBinder {
        private final ResponsesApi responsesApi;

        public AgentBinder(ResponsesApi responsesApi) {
            this.responsesApi = responsesApi;
        }

        @Override
        protected void configure() {
            bind(new ResponsesResource(responsesApi)).to(ResponsesResource.class).in(ApplicationScoped.class);
        }
    }

    /**
     * Builds and returns an HttpServer hosting the given UntypedAgent at the default http://0.0.0.0:8088.
     *
     * @param res The ResponsesApi instance to be served.
     * @return An instance of HttpServer hosting the agent service.
     */
    public static HttpServer buildAgent(ResponsesApi res) {
        return getHttpServer("http://0.0.0.0:8088", new AgentBinder(res));
    }

    /**
     * Builds and returns an HttpServer hosting the given UntypedAgent at the specified base URI.
     *
     * @param baseUri The base URI where the agent service will be hosted i.e "http://0.0.0.0:8088".
     * @param res     The ResponsesApi instance to be served.
     * @return An instance of HttpServer hosting the agent service.
     */
    public static HttpServer buildAgent(String baseUri, ResponsesApi res) {
        return getHttpServer(baseUri, new AgentBinder(res));
    }

    private static HttpServer getHttpServer(String baseUri, AgentBinder agentBinder) {
        ResourceConfig rc = new ResourceConfig()
            .register(agentBinder)
            .register(ResponsesResource.class)
            .register(new SseBinder())
            .register(SseEventSinkValueParamProvider.class)
            .register(HealthResource.Liveness.class)
            .register(HealthResource.Readiness.class)
            .register(JacksonFeature.class)
            .register(ObjectMapperProvider.class)
            .register(Filters.CorsFilter.class)
            .register(SseFeature.class)
            .register(RoutingFilter.class)
            .register(SseResponseFilter.class)
            .register(PlatformHeaderResponseFilter.class)
            .register(InboundRequestLoggingFilter.class)
            .register(ApiExceptionMapper.class)
            .register(ExceptionMappers.NotFoundExceptionMapper.class)
            .register(ExceptionMappers.GenericExceptionMapper.class);

        if (LOG_REQUESTS) {
            rc = rc
                .register(Filters.RequestLoggingFilter.class)
                .register(Filters.ResponseLoggingFilter.class);
        }

        return build(URI.create(baseUri), rc);
    }

    public static HttpServer build(URI uri, ResourceConfig rc) {
        return GrizzlyHttpServerFactory.createHttpServer(uri, rc);
    }
}
