// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.clients.HttpClients;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class demonstrates various patterns for context propagation in Azure SDK HTTP pipelines.
 * Context propagation is essential for observability, correlation, and passing custom data through the pipeline.
 */
public class ContextPropagationExamples {
    private static final ClientLogger LOGGER = new ClientLogger(ContextPropagationExamples.class);

    // Common context keys used in examples
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String USER_ID_KEY = "userId";
    public static final String REQUEST_START_TIME_KEY = "requestStartTime";
    public static final String TRACE_ID_KEY = "traceId";

    /**
     * Policy that demonstrates reading values from context and propagating them as HTTP headers.
     */
    public static class ContextToHeaderPolicy implements HttpPipelinePolicy {
        
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            propagateContextToHeaders(context);
            return next.process();
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            propagateContextToHeaders(context);
            return next.processSync();
        }

        private void propagateContextToHeaders(HttpPipelineCallContext context) {
            Context requestContext = context.getContext();
            HttpRequest request = context.getHttpRequest();

            // Propagate correlation ID
            requestContext.getData(CORRELATION_ID_KEY)
                .ifPresent(correlationId -> {
                    request.getHeaders().set(HttpHeaderName.fromString("X-Correlation-ID"), correlationId.toString());
                    LOGGER.atInfo()
                        .addKeyValue("correlationId", correlationId)
                        .log("Propagated correlation ID to header");
                });

            // Propagate user ID for authentication/authorization context
            requestContext.getData(USER_ID_KEY)
                .ifPresent(userId -> {
                    request.getHeaders().set(HttpHeaderName.fromString("X-User-ID"), userId.toString());
                    LOGGER.atInfo()
                        .addKeyValue("userId", userId)
                        .log("Propagated user ID to header");
                });

            // Propagate trace ID for distributed tracing
            requestContext.getData(TRACE_ID_KEY)
                .ifPresent(traceId -> {
                    request.getHeaders().set(HttpHeaderName.fromString("X-Trace-ID"), traceId.toString());
                    LOGGER.atInfo()
                        .addKeyValue("traceId", traceId)
                        .log("Propagated trace ID to header");
                });
        }
    }

    /**
     * Policy that enriches context with additional data during request processing.
     */
    public static class ContextEnrichmentPolicy implements HttpPipelinePolicy {

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            // Enrich context with request start time
            Context enrichedContext = context.getContext()
                .addData(REQUEST_START_TIME_KEY, System.currentTimeMillis());
            
            // Update the context in the pipeline call context
            HttpPipelineCallContext enrichedCallContext = context.setContext(enrichedContext);
            
            return next.process();
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            // Enrich context with request start time
            Context enrichedContext = context.getContext()
                .addData(REQUEST_START_TIME_KEY, System.currentTimeMillis());
            
            // Update the context in the pipeline call context
            context.setContext(enrichedContext);
            
            return next.processSync();
        }
    }

    /**
     * Policy that demonstrates reading context data to calculate metrics.
     */
    public static class ContextMetricsPolicy implements HttpPipelinePolicy {

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process()
                .doOnNext(response -> calculateAndLogMetrics(context, response.getStatusCode(), null))
                .doOnError(error -> calculateAndLogMetrics(context, -1, error));
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            try {
                HttpResponse response = next.processSync();
                calculateAndLogMetrics(context, response.getStatusCode(), null);
                return response;
            } catch (Exception error) {
                calculateAndLogMetrics(context, -1, error);
                throw error;
            }
        }

        private void calculateAndLogMetrics(HttpPipelineCallContext context, int statusCode, Throwable error) {
            Context requestContext = context.getContext();
            
            // Calculate request duration using start time from context
            requestContext.getData(REQUEST_START_TIME_KEY)
                .ifPresent(startTime -> {
                    long duration = System.currentTimeMillis() - (Long) startTime;
                    
                    LOGGER.atInfo()
                        .addKeyValue("requestDuration", duration)
                        .addKeyValue("statusCode", statusCode)
                        .addKeyValue("method", context.getHttpRequest().getHttpMethod())
                        .addKeyValue("url", context.getHttpRequest().getUrl())
                        .log("Request metrics calculated from context");
                });

            // Log correlation information for request tracking
            requestContext.getData(CORRELATION_ID_KEY)
                .ifPresent(correlationId -> {
                    LOGGER.atInfo()
                        .addKeyValue("correlationId", correlationId)
                        .addKeyValue("statusCode", statusCode)
                        .log("Request completed with correlation ID");
                });
        }
    }

    /**
     * Example 1: Basic context creation and usage.
     */
    public static void basicContextUsage() {
        // Create context with correlation ID
        String correlationId = UUID.randomUUID().toString();
        Context context = Context.NONE.addData(CORRELATION_ID_KEY, correlationId);

        // Create pipeline with context-aware policies
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(HttpClients.createDefault())
            .policies(new ContextToHeaderPolicy())
            .build();

        // Create a sample request
        try {
            HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://httpbin.org/get"));
            
            // Send request with context - the correlation ID will be added as a header
            Mono<HttpResponse> response = pipeline.send(request, context);
            
            LOGGER.info("Request sent with correlation ID: {}", correlationId);
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid URL", e);
        }
    }

    /**
     * Example 2: Context propagation with multiple values.
     */
    public static void multiValueContextPropagation() {
        // Create context with multiple values
        Context context = Context.NONE
            .addData(CORRELATION_ID_KEY, UUID.randomUUID().toString())
            .addData(USER_ID_KEY, "user123")
            .addData(TRACE_ID_KEY, "trace-" + System.currentTimeMillis());

        // Create pipeline with multiple context-aware policies
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(HttpClients.createDefault())
            .policies(
                new ContextEnrichmentPolicy(),
                new ContextToHeaderPolicy(),
                new ContextMetricsPolicy()
            )
            .build();

        try {
            HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://httpbin.org/get"));
            
            // Send request with enriched context
            Mono<HttpResponse> response = pipeline.send(request, context);
            
            LOGGER.info("Request sent with enriched context containing multiple values");
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid URL", e);
        }
    }

    /**
     * Example 3: Async context propagation pattern.
     * This demonstrates how context flows through async operations.
     */
    public static Mono<String> asyncContextPropagationExample() {
        // Initial context creation
        String correlationId = UUID.randomUUID().toString();
        Context initialContext = Context.NONE
            .addData(CORRELATION_ID_KEY, correlationId)
            .addData(USER_ID_KEY, "async-user");

        return processWithAsyncContext(initialContext)
            .flatMap(result -> {
                // Context is automatically propagated through the Mono chain
                LOGGER.info("Async processing completed for correlation ID: {}", correlationId);
                return Mono.just("Async operation completed: " + result);
            });
    }

    private static Mono<String> processWithAsyncContext(Context context) {
        // Create pipeline
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(HttpClients.createDefault())
            .policies(
                new ContextToHeaderPolicy(),
                new ContextMetricsPolicy()
            )
            .build();

        try {
            HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://httpbin.org/headers"));
            
            // Send request and process response
            return pipeline.send(request, context)
                .map(response -> {
                    // Context data is available throughout the async chain
                    context.getData(CORRELATION_ID_KEY)
                        .ifPresent(corrId -> LOGGER.info("Processing response for correlation ID: {}", corrId));
                    
                    return "Response status: " + response.getStatusCode();
                });
        } catch (MalformedURLException e) {
            return Mono.error(e);
        }
    }

    /**
     * Example 4: Context propagation in synchronous scenarios.
     */
    public static void synchronousContextExample() {
        // Create context for sync operation
        Context context = Context.NONE
            .addData(CORRELATION_ID_KEY, "sync-" + System.currentTimeMillis())
            .addData(USER_ID_KEY, "sync-user");

        // Create pipeline
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(HttpClients.createDefault())
            .policies(
                new ContextEnrichmentPolicy(),
                new ContextToHeaderPolicy(),
                new ContextMetricsPolicy()
            )
            .build();

        try {
            HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://httpbin.org/get"));
            
            // Send synchronous request with context
            HttpResponse response = pipeline.sendSync(request, context);
            
            LOGGER.info("Synchronous request completed with status: {}", response.getStatusCode());
        } catch (Exception e) {
            LOGGER.error("Synchronous request failed", e);
        }
    }

    /**
     * Example 5: Context propagation in concurrent scenarios.
     */
    public static CompletableFuture<Void> concurrentContextExample() {
        // Create base context
        Context baseContext = Context.NONE
            .addData(USER_ID_KEY, "concurrent-user");

        // Create multiple requests with different correlation IDs but same base context
        CompletableFuture<Void> future1 = processRequestAsync(baseContext, "request-1");
        CompletableFuture<Void> future2 = processRequestAsync(baseContext, "request-2");
        CompletableFuture<Void> future3 = processRequestAsync(baseContext, "request-3");

        // Wait for all requests to complete
        return CompletableFuture.allOf(future1, future2, future3)
            .thenRun(() -> LOGGER.info("All concurrent requests completed"));
    }

    private static CompletableFuture<Void> processRequestAsync(Context baseContext, String requestId) {
        // Each request gets its own correlation ID while inheriting base context
        Context requestContext = baseContext.addData(CORRELATION_ID_KEY, requestId);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(HttpClients.createDefault())
            .policies(new ContextToHeaderPolicy())
            .build();

        try {
            HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://httpbin.org/delay/1"));
            
            return pipeline.send(request, requestContext)
                .doOnNext(response -> LOGGER.info("Request {} completed with status: {}", requestId, response.getStatusCode()))
                .doOnError(error -> LOGGER.error("Request {} failed", requestId, error))
                .then()
                .toFuture();
        } catch (MalformedURLException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Utility method to create a context with common observability data.
     */
    public static Context createObservabilityContext(String operationName, String userId) {
        return Context.NONE
            .addData(CORRELATION_ID_KEY, UUID.randomUUID().toString())
            .addData(USER_ID_KEY, userId)
            .addData(TRACE_ID_KEY, operationName + "-" + System.currentTimeMillis())
            .addData("operationName", operationName);
    }
}