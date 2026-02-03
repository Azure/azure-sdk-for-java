// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Contains tests for {@link UserAgentPolicy}.
 */
public class UserAgentPolicyTests {
    @ParameterizedTest(name = "{displayName} [{index}]")
    @MethodSource("userAgentAndExpectedSupplier")
    public void validateUserAgentPolicyHandling(UserAgentPolicy userAgentPolicy, String expected) throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new ValidationHttpClient(
                request -> assertEquals(expected, request.getHeaders().getValue(HttpHeaderName.USER_AGENT))))
            .addPolicy(userAgentPolicy)
            .build();

        try (Response<?> response
            = pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost"))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    /**
     * Tests that applying the {@link UserAgentPolicy} after a {@link HttpRetryPolicy} doesn't result in the
     * User-Agent header being applied multiple times.
     */
    @ParameterizedTest(name = "{displayName} [{index}]")
    @MethodSource("userAgentAndExpectedSupplier")
    public void userAgentPolicyAfterRetryPolicy(UserAgentPolicy userAgentPolicy, String expected) throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new RetryValidationHttpClient(
                request -> assertEquals(expected, request.getHeaders().getValue(HttpHeaderName.USER_AGENT))))
            .addPolicy(new HttpRetryPolicy(new HttpRetryOptions(5, Duration.ofMillis(10))))
            .addPolicy(userAgentPolicy)
            .build();

        try (Response<?> response
            = pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost"))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    /**
     * Tests that applying multiple {@link UserAgentPolicy} doesn't result in the User-Agent header being applied
     * multiple times.
     */
    @ParameterizedTest(name = "{displayName} [{index}]")
    @MethodSource("userAgentAndExpectedSupplier")
    public void multipleUserAgentPolicies(UserAgentPolicy userAgentPolicy, String expected) throws IOException {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new ValidationHttpClient(
                request -> assertEquals(expected, request.getHeaders().getValue(HttpHeaderName.USER_AGENT))))
            .addPolicy(userAgentPolicy)
            .addPolicy(userAgentPolicy)
            .build();

        try (Response<?> response
            = pipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost"))) {
            assertEquals(200, response.getStatusCode());
        }
    }

    private static Stream<Arguments> userAgentAndExpectedSupplier() {
        String defaultUserAgent = UserAgentPolicy.DEFAULT_USER_AGENT_HEADER;
        String sdkName = "sdkName";
        String sdkVersion = "sdkVersion";
        String baseUserAgent = String.format("%s-%s/%s", defaultUserAgent, sdkName, sdkVersion);
        String applicationId = "applicationId";
        String platformInfo = String.format("%s; %s; %s", Configuration.getGlobalConfiguration().get("java.version"),
            Configuration.getGlobalConfiguration().get("os.name"),
            Configuration.getGlobalConfiguration().get("os.version"));

        return Stream.of(
            // Tests using the default User-Agent
            Arguments.of(new UserAgentPolicy(), defaultUserAgent),

            // Tests using a simple custom User-Agent
            Arguments.of(new UserAgentPolicy("AutoRest-Java"), "AutoRest-Java"),

            // Tests using SDK name and version with platform information and without application ID
            Arguments.of(new UserAgentPolicy(new UserAgentOptions().setSdkName(sdkName).setSdkVersion(sdkVersion)),
                String.format("%s (%s)", baseUserAgent, platformInfo)),

            // Tests using SDK name and version with platform information and application ID
            Arguments.of(new UserAgentPolicy(
                new UserAgentOptions().setSdkName(sdkName).setSdkVersion(sdkVersion).setApplicationId(applicationId)),
                String.format("%s %s (%s)", applicationId, baseUserAgent, platformInfo)));
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
        public Response<BinaryData> send(HttpRequest request) {
            validator.accept(request);
            return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
        }
    }

    private static class RetryValidationHttpClient implements HttpClient {
        private final Consumer<HttpRequest> validator;
        private int retryCount = 0;

        RetryValidationHttpClient(Consumer<HttpRequest> validator) {
            this.validator = validator;
        }

        @Override
        public Response<BinaryData> send(HttpRequest request) {
            if (retryCount < 5) {
                retryCount++;
                throw CoreException.from(new IOException("Activating retry policy"));
            }

            validator.accept(request);
            return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
        }
    }
}
