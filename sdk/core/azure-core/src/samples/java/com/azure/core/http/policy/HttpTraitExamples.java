// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Context;
import com.azure.core.util.HttpClientOptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * This class demonstrates how to use HttpTrait to configure HTTP clients with custom policies.
 * HttpTrait provides a consistent interface for configuring HTTP-related settings across Azure SDK clients.
 */
public class HttpTraitExamples {

    /**
     * Example service client builder that implements HttpTrait.
     * This demonstrates the typical pattern used by Azure SDK client builders.
     */
    public static class ExampleServiceClientBuilder implements HttpTrait<ExampleServiceClientBuilder> {
        private HttpClient httpClient;
        private HttpPipeline pipeline;
        private final List<HttpPipelinePolicy> policies = new ArrayList<>();
        private RetryOptions retryOptions;
        private HttpLogOptions httpLogOptions;
        private ClientOptions clientOptions;
        private String endpoint;

        @Override
        public ExampleServiceClientBuilder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        @Override
        public ExampleServiceClientBuilder pipeline(HttpPipeline pipeline) {
            this.pipeline = pipeline;
            return this;
        }

        @Override
        public ExampleServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
            this.policies.add(pipelinePolicy);
            return this;
        }

        @Override
        public ExampleServiceClientBuilder retryOptions(RetryOptions retryOptions) {
            this.retryOptions = retryOptions;
            return this;
        }

        @Override
        public ExampleServiceClientBuilder httpLogOptions(HttpLogOptions logOptions) {
            this.httpLogOptions = logOptions;
            return this;
        }

        @Override
        public ExampleServiceClientBuilder clientOptions(ClientOptions clientOptions) {
            this.clientOptions = clientOptions;
            return this;
        }

        /**
         * Sets the service endpoint.
         */
        public ExampleServiceClientBuilder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Builds the service client.
         */
        public ExampleServiceClient build() {
            HttpPipeline finalPipeline = this.pipeline;
            
            if (finalPipeline == null) {
                // Build pipeline from individual components if not provided
                finalPipeline = buildDefaultPipeline();
            }
            
            return new ExampleServiceClient(finalPipeline, endpoint);
        }

        private HttpPipeline buildDefaultPipeline() {
            HttpPipelineBuilder builder = new HttpPipelineBuilder();
            
            // Set HTTP client
            if (httpClient != null) {
                builder.httpClient(httpClient);
            } else {
                builder.httpClient(createExampleHttpClient());
            }
            
            // Add custom policies
            List<HttpPipelinePolicy> allPolicies = new ArrayList<>();
            
            // Add custom policies first
            allPolicies.addAll(policies);
            
            // Add retry policy
            if (retryOptions != null) {
                allPolicies.add(new RetryPolicy(retryOptions));
            } else {
                allPolicies.add(new RetryPolicy());
            }
            
            // Add logging policy
            if (httpLogOptions != null) {
                allPolicies.add(new HttpLoggingPolicy(httpLogOptions));
            }
            
            return builder.policies(allPolicies.toArray(new HttpPipelinePolicy[0])).build();
        }
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
     * Example service client that uses the configured HTTP pipeline.
     */
    public static class ExampleServiceClient {
        private final HttpPipeline pipeline;
        private final String endpoint;

        ExampleServiceClient(HttpPipeline pipeline, String endpoint) {
            this.pipeline = pipeline;
            this.endpoint = endpoint;
        }

        public HttpPipeline getPipeline() {
            return pipeline;
        }

        public String getEndpoint() {
            return endpoint;
        }
    }

    /**
     * Example 1: Basic usage with a single custom policy.
     */
    public static ExampleServiceClient createClientWithBasicCustomPolicy() {
        return new ExampleServiceClientBuilder()
            .endpoint("https://example.service.azure.com")
            .addPolicy(new CustomPolicyExamples.ObservabilityLoggingPolicy("example-service"))
            .build();
    }

    /**
     * Example 2: Adding multiple custom policies with specific configurations.
     */
    public static ExampleServiceClient createClientWithMultipleCustomPolicies() {
        return new ExampleServiceClientBuilder()
            .endpoint("https://example.service.azure.com")
            .addPolicy(new CustomPolicyExamples.ObservabilityLoggingPolicy("example-service"))
            .addPolicy(new CustomPolicyExamples.MetricsCollectionPolicy("example-service"))
            .addPolicy(new CustomPolicyExamples.ContextAwarePolicy())
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions().setMaxRetries(3)
                .setBaseDelay(Duration.ofSeconds(1)).setMaxDelay(Duration.ofMinutes(1))))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.HEADERS))
            .build();
    }

    /**
     * Example 3: Using HttpClientOptions for advanced configuration.
     */
    public static ExampleServiceClient createClientWithHttpClientOptions() {
        HttpClientOptions options = new HttpClientOptions()
            .setConnectTimeout(Duration.ofSeconds(30))
            .setWriteTimeout(Duration.ofMinutes(2))
            .setResponseTimeout(Duration.ofMinutes(2))
            .setReadTimeout(Duration.ofMinutes(2));

        return new ExampleServiceClientBuilder()
            .endpoint("https://example.service.azure.com")
            .addPolicy(new CustomPolicyExamples.ObservabilityLoggingPolicy("example-service"))
            .clientOptions(options)
            .build();
    }

    /**
     * Example 4: Providing a pre-built HTTP pipeline (advanced usage).
     * When a pipeline is provided, other HttpTrait settings are ignored.
     */
    public static ExampleServiceClient createClientWithCustomPipeline() {
        HttpPipeline customPipeline = new HttpPipelineBuilder()
            .httpClient(createExampleHttpClient())
            .policies(
                new CustomPolicyExamples.ObservabilityLoggingPolicy("custom-pipeline"),
                new CustomPolicyExamples.MetricsCollectionPolicy("custom-pipeline"),
                new RetryPolicy(),
                new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            )
            .build();

        return new ExampleServiceClientBuilder()
            .endpoint("https://example.service.azure.com")
            .pipeline(customPipeline)
            .build();
    }

    /**
     * Example 5: Creating a client with a custom HttpClient.
     */
    public static ExampleServiceClient createClientWithCustomHttpClient() {
        HttpClient customHttpClient = createExampleHttpClient();

        return new ExampleServiceClientBuilder()
            .endpoint("https://example.service.azure.com")
            .httpClient(customHttpClient)
            .addPolicy(new CustomPolicyExamples.ObservabilityLoggingPolicy("example-service"))
            .retryOptions(new RetryOptions(new FixedDelayOptions(3, Duration.ofSeconds(2))))
            .build();
    }

    /**
     * Example 6: Fluent configuration for complex scenarios.
     */
    public static ExampleServiceClient createFullyConfiguredClient() {
        return new ExampleServiceClientBuilder()
            .endpoint("https://example.service.azure.com")
            // Add observability policies
            .addPolicy(new CustomPolicyExamples.ObservabilityLoggingPolicy("production-service"))
            .addPolicy(new CustomPolicyExamples.MetricsCollectionPolicy("production-service"))
            .addPolicy(new CustomPolicyExamples.ContextAwarePolicy())
            .addPolicy(new CustomPolicyExamples.RetryAwarePolicy())
            // Configure retry behavior
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions().setMaxRetries(5)
                .setBaseDelay(Duration.ofSeconds(1)).setMaxDelay(Duration.ofMinutes(2))))
            // Configure detailed logging for troubleshooting
            .httpLogOptions(new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            // Configure timeouts
            .clientOptions(new HttpClientOptions()
                .setConnectTimeout(Duration.ofSeconds(30))
                .setResponseTimeout(Duration.ofMinutes(5)))
            .build();
    }
}