// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Contains tests for {@link UserAgentPolicy}.
 */
public class UserAgentTests {
    private static final String USER_AGENT = "User-Agent";

    @ParameterizedTest(name = "{displayName} [{index}]")
    @MethodSource("userAgentAndExpectedSupplier")
    public void validateUserAgentPolicyHandling(UserAgentPolicy userAgentPolicy, String expected) {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new ValidationHttpClient(request ->
                assertEquals(expected, request.getHeaders().getValue(USER_AGENT))))
            .policies(userAgentPolicy)
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that applying the {@link UserAgentPolicy} after a {@link RetryPolicy} doesn't result in the User-Agent
     * header being applied multiple times.
     */
    @ParameterizedTest(name = "{displayName} [{index}]")
    @MethodSource("userAgentAndExpectedSupplier")
    public void userAgentPolicyAfterRetryPolicy(UserAgentPolicy userAgentPolicy, String expected) {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new RetryValidationHttpClient(request ->
                assertEquals(expected, request.getHeaders().getValue(USER_AGENT))))
            .policies(new RetryPolicy(new FixedDelay(5, Duration.ofMillis(10))))
            .policies(userAgentPolicy)
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that applying multiple {@link UserAgentPolicy} doesn't result in the User-Agent header being applied
     * multiple times.
     */
    @ParameterizedTest(name = "{displayName} [{index}]")
    @MethodSource("userAgentAndExpectedSupplier")
    public void multipleUserAgentPolicies(UserAgentPolicy userAgentPolicy, String expected) {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new ValidationHttpClient(request ->
                assertEquals(expected, request.getHeaders().getValue(USER_AGENT))))
            .policies(userAgentPolicy, userAgentPolicy)
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that passing a {@link Context} with a value set for {@link UserAgentPolicy#OVERRIDE_USER_AGENT_CONTEXT_KEY}
     * will use the value, and only the value, for the User-Agent header.
     */
    @ParameterizedTest(name = "{displayName} [{index}]")
    @MethodSource("userAgentAndExpectedSupplier")
    public void overrideUserAgentContext(UserAgentPolicy userAgentPolicy, String expected) {
        String overrideUserAgent = "overrideUserAgent";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new ValidationHttpClient(request ->
                assertEquals(overrideUserAgent, request.getHeaders().getValue(USER_AGENT))))
            .policies(userAgentPolicy)
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost"),
            new Context(UserAgentPolicy.OVERRIDE_USER_AGENT_CONTEXT_KEY, overrideUserAgent)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that passing a {@link Context} with a value set for {@link UserAgentPolicy#APPEND_USER_AGENT_CONTEXT_KEY}
     * will append the value to the User-Agent header.
     */
    @ParameterizedTest(name = "{displayName} [{index}]")
    @MethodSource("userAgentAndExpectedSupplier")
    public void appendUserAgentContext(UserAgentPolicy userAgentPolicy, String expected) {
        String appendUserAgent = "appendUserAgent";
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new ValidationHttpClient(request ->
                assertEquals(expected + " " + appendUserAgent, request.getHeaders().getValue(USER_AGENT))))
            .policies(userAgentPolicy)
            .build();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost"),
            new Context(UserAgentPolicy.APPEND_USER_AGENT_CONTEXT_KEY, appendUserAgent)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> userAgentAndExpectedSupplier() {
        String defaultUserAgent = "azsdk-java";
        String sdkName = "sdkName";
        String sdkVersion = "sdkVersion";
        String baseUserAgent = String.format("%s-%s/%s", defaultUserAgent, sdkName, sdkVersion);
        String applicationId = "applicationId";
        String platformInfo = String.format("%s; %s; %s",
            Configuration.getGlobalConfiguration().get("java.version"),
            Configuration.getGlobalConfiguration().get("os.name"),
            Configuration.getGlobalConfiguration().get("os.version"));

        Configuration enabledTelemetryConfiguration = new Configuration()
            .put(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED, "false");
        Configuration disabledTelemetryConfiguration = new Configuration()
            .put(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED, "true");

        return Stream.of(
            // Tests using the default User-Agent
            Arguments.of(new UserAgentPolicy(), defaultUserAgent),

            // Tests using a simple custom User-Agent
            Arguments.of(new UserAgentPolicy("AutoRest-Java"), "AutoRest-Java"),

            // Tests using SDK name and version without platform information or application ID
            Arguments.of(new UserAgentPolicy(null, sdkName, sdkVersion, disabledTelemetryConfiguration),
                baseUserAgent),
            Arguments.of(new UserAgentPolicy(sdkName, sdkVersion, disabledTelemetryConfiguration, () -> "1.0"),
                baseUserAgent),

            // Tests using SDK name and version with application ID and without platform information
            Arguments.of(new UserAgentPolicy("applicationId", "sdkName", "sdkVersion", disabledTelemetryConfiguration),
                String.format("%s %s", applicationId, baseUserAgent)),

            // Tests using SDK name and version with platform information and without application ID
            Arguments.of(new UserAgentPolicy(null, "sdkName", "sdkVersion", enabledTelemetryConfiguration),
                String.format("%s (%s)", baseUserAgent, platformInfo)),
            Arguments.of(new UserAgentPolicy(sdkName, sdkVersion, enabledTelemetryConfiguration, () -> "1.0"),
                String.format("%s (%s)", baseUserAgent, platformInfo)),

            // Tests using SDK name and version with platform information and application ID
            Arguments.of(new UserAgentPolicy("applicationId", "sdkName", "sdkVersion", enabledTelemetryConfiguration),
                String.format("%s %s (%s)", applicationId, baseUserAgent, platformInfo))
        );
    }

    /*
     * Simple helper class which implements {@link HttpClient} and applies a validation method to the request sent
     * by the client.
     */
    private static class ValidationHttpClient implements HttpClient {
        private final Consumer<HttpRequest> validator;

        ValidationHttpClient(Consumer<HttpRequest> validator) {
            this.validator = validator;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            validator.accept(request);
            return Mono.just(new MockHttpResponse(request, 200));
        }
    }

    private static class RetryValidationHttpClient implements HttpClient {
        private final Consumer<HttpRequest> validator;
        private int retryCount = 0;

        RetryValidationHttpClient(Consumer<HttpRequest> validator) {
            this.validator = validator;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (retryCount < 5) {
                retryCount++;
                return Mono.error(new RuntimeException("Activating retry policy"));
            }

            validator.accept(request);
            return Mono.just(new MockHttpResponse(request, 200));
        }
    }
}
