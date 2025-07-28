// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class contains examples of how to create custom HTTP pipeline policies for observability and other purposes.
 * These examples demonstrate common patterns for writing policies that can be used with Azure SDK clients.
 */
public class CustomPolicyExamples {
    private static final ClientLogger LOGGER = new ClientLogger(CustomPolicyExamples.class);

    /**
     * Example of a simple request/response logging policy for observability.
     * This policy logs basic information about each HTTP request and response.
     */
    public static class ObservabilityLoggingPolicy implements HttpPipelinePolicy {
        private final String policyName;

        public ObservabilityLoggingPolicy(String policyName) {
            this.policyName = policyName;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            HttpRequest request = context.getHttpRequest();
            Instant startTime = Instant.now();
            
            LOGGER.atInfo()
                .addKeyValue("policy", policyName)
                .addKeyValue("method", request.getHttpMethod())
                .addKeyValue("url", request.getUrl())
                .log("Sending request");

            return next.process()
                .doOnNext(response -> {
                    Duration duration = Duration.between(startTime, Instant.now());
                    LOGGER.atInfo()
                        .addKeyValue("policy", policyName)
                        .addKeyValue("statusCode", response.getStatusCode())
                        .addKeyValue("durationMs", duration.toMillis())
                        .log("Received response");
                })
                .doOnError(error -> {
                    Duration duration = Duration.between(startTime, Instant.now());
                    LOGGER.atError()
                        .addKeyValue("policy", policyName)
                        .addKeyValue("durationMs", duration.toMillis())
                        .addKeyValue("error", error.getMessage())
                        .log("Request failed");
                });
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            HttpRequest request = context.getHttpRequest();
            Instant startTime = Instant.now();
            
            LOGGER.atInfo()
                .addKeyValue("policy", policyName)
                .addKeyValue("method", request.getHttpMethod())
                .addKeyValue("url", request.getUrl())
                .log("Sending request");

            try {
                HttpResponse response = next.processSync();
                Duration duration = Duration.between(startTime, Instant.now());
                LOGGER.atInfo()
                    .addKeyValue("policy", policyName)
                    .addKeyValue("statusCode", response.getStatusCode())
                    .addKeyValue("durationMs", duration.toMillis())
                    .log("Received response");
                return response;
            } catch (Exception error) {
                Duration duration = Duration.between(startTime, Instant.now());
                LOGGER.atError()
                    .addKeyValue("policy", policyName)
                    .addKeyValue("durationMs", duration.toMillis())
                    .addKeyValue("error", error.getMessage())
                    .log("Request failed");
                throw error;
            }
        }
    }

    /**
     * Example of a metrics collection policy that tracks request counts and durations.
     * This demonstrates how to collect observability data for monitoring purposes.
     */
    public static class MetricsCollectionPolicy implements HttpPipelinePolicy {
        private final AtomicLong requestCount = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final String serviceName;

        public MetricsCollectionPolicy(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            long requestId = requestCount.incrementAndGet();
            Instant startTime = Instant.now();

            return next.process()
                .doOnNext(response -> recordMetrics(requestId, startTime, response.getStatusCode(), null))
                .doOnError(error -> recordMetrics(requestId, startTime, -1, error));
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            long requestId = requestCount.incrementAndGet();
            Instant startTime = Instant.now();

            try {
                HttpResponse response = next.processSync();
                recordMetrics(requestId, startTime, response.getStatusCode(), null);
                return response;
            } catch (Exception error) {
                recordMetrics(requestId, startTime, -1, error);
                throw error;
            }
        }

        private void recordMetrics(long requestId, Instant startTime, int statusCode, Throwable error) {
            Duration duration = Duration.between(startTime, Instant.now());
            totalDuration.addAndGet(duration.toMillis());

            LOGGER.atInfo()
                .addKeyValue("service", serviceName)
                .addKeyValue("requestId", requestId)
                .addKeyValue("statusCode", statusCode)
                .addKeyValue("durationMs", duration.toMillis())
                .addKeyValue("totalRequests", requestCount.get())
                .addKeyValue("avgDurationMs", totalDuration.get() / requestCount.get())
                .log("Request metrics");
        }

        public long getTotalRequests() {
            return requestCount.get();
        }

        public double getAverageDuration() {
            long count = requestCount.get();
            return count > 0 ? (double) totalDuration.get() / count : 0.0;
        }
    }

    /**
     * Example of a context-aware policy that demonstrates context propagation.
     * This policy shows how to read values from the context and add custom headers.
     */
    public static class ContextAwarePolicy implements HttpPipelinePolicy {
        private static final String CORRELATION_ID_KEY = "correlationId";
        private static final String USER_ID_KEY = "userId";
        
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            addContextHeaders(context);
            return next.process();
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            addContextHeaders(context);
            return next.processSync();
        }

        private void addContextHeaders(HttpPipelineCallContext context) {
            Context requestContext = context.getContext();
            
            // Read correlation ID from context and add as header
            requestContext.getData(CORRELATION_ID_KEY)
                .ifPresent(correlationId -> {
                    context.getHttpRequest().getHeaders()
                        .set(HttpHeaderName.fromString("X-Correlation-ID"), correlationId.toString());
                    LOGGER.atInfo()
                        .addKeyValue("correlationId", correlationId)
                        .log("Added correlation ID header");
                });

            // Read user ID from context and add as header
            requestContext.getData(USER_ID_KEY)
                .ifPresent(userId -> {
                    context.getHttpRequest().getHeaders()
                        .set(HttpHeaderName.fromString("X-User-ID"), userId.toString());
                    LOGGER.atInfo()
                        .addKeyValue("userId", userId)
                        .log("Added user ID header");
                });
        }
    }

    /**
     * Example of a retry-aware policy that demonstrates advanced policy patterns.
     * This policy tracks retry attempts and can modify behavior based on retry count.
     */
    public static class RetryAwarePolicy implements HttpPipelinePolicy {
        private static final String RETRY_COUNT_KEY = "retryCount";

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            // Get current retry count from context
            int retryCount = context.getContext()
                .getData(RETRY_COUNT_KEY)
                .map(count -> (Integer) count)
                .orElse(0);

            LOGGER.atInfo()
                .addKeyValue("retryAttempt", retryCount)
                .log("Processing request");

            // Add retry count as header for server-side observability
            context.getHttpRequest().getHeaders()
                .set(HttpHeaderName.fromString("X-Retry-Count"), String.valueOf(retryCount));

            return next.process();
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            // Get current retry count from context
            int retryCount = context.getContext()
                .getData(RETRY_COUNT_KEY)
                .map(count -> (Integer) count)
                .orElse(0);

            LOGGER.atInfo()
                .addKeyValue("retryAttempt", retryCount)
                .log("Processing request");

            // Add retry count as header for server-side observability
            context.getHttpRequest().getHeaders()
                .set(HttpHeaderName.fromString("X-Retry-Count"), String.valueOf(retryCount));

            return next.processSync();
        }
    }

    /**
     * Example demonstrating how to create an HTTP pipeline with custom policies.
     * Note: In real scenarios, you would use an actual HttpClient implementation.
     */
    public static HttpPipeline createCustomPipeline() {
        return new HttpPipelineBuilder()
            .httpClient(createExampleHttpClient())
            .policies(
                new ObservabilityLoggingPolicy("example-service"),
                new MetricsCollectionPolicy("example-service"),
                new ContextAwarePolicy(),
                new RetryAwarePolicy()
            )
            .build();
    }

    /**
     * Creates a simple HttpClient for demonstration purposes.
     * In real applications, you would use implementations from azure-core-http-netty or azure-core-http-okhttp.
     */
    private static HttpClient createExampleHttpClient() {
        return new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                // This is a no-op client for demonstration purposes
                return Mono.empty();
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                // This is a no-op client for demonstration purposes
                return null;
            }
        };
    }

    /**
     * Example of creating context with custom data for context propagation.
     */
    public static Context createContextWithData(String correlationId, String userId) {
        return Context.NONE
            .addData("correlationId", correlationId)
            .addData("userId", userId);
    }
}