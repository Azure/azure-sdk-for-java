// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.Context;
import com.azure.storage.common.implementation.BuilderUtils;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.ExpectContinueMode;
import com.azure.storage.common.policy.ExpectContinueOptions;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.RetryPolicyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ExpectContinuePolicy} and {@link ExpectContinueOnThrottlePolicy}.
 */
public class ExpectContinueTests {
    private static final String CONTINUE = "100-continue";
    private static final String ENDPOINT = "https://account.blob.core.windows.net/container/blob";
    private static final byte[] BODY = new byte[1024];

    // Negligible backoff so the retry tests stay fast.
    private static final RequestRetryOptions FAST_RETRY_OPTIONS
        = new RequestRetryOptions(RetryPolicyType.FIXED, 4, (Integer) null, 1L, 5L, null);

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void policyAddsHeaderOnContentBody(boolean hasBody) throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.On), client);

        HttpRequest request = hasBody
            ? new HttpRequest(HttpMethod.PUT, new URL(ENDPOINT)).setBody("foo")
            : new HttpRequest(HttpMethod.PUT, new URL(ENDPOINT));
        pipeline.sendSync(request, Context.NONE);

        assertEquals(hasBody ? CONTINUE : null, client.expectHeaders.get(0));
    }

    @ParameterizedTest
    @CsvSource({ "1024, 2048, true", "2048, 1024, false", "1024, 1024, true" })
    public void policyRespectsThreshold(int threshold, int bodyLength, boolean expectHeader)
        throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline
            = pipeline(options(ExpectContinueMode.On).setContentLengthThreshold((long) threshold), client);

        pipeline.sendSync(requestWithBody(bodyLength), Context.NONE);

        assertEquals(expectHeader ? CONTINUE : null, client.expectHeaders.get(0));
    }

    @ParameterizedTest
    @ValueSource(ints = { 429, 500, 503 })
    public void throttlePolicyAddsHeaderOnlyAfterError(int errorStatusCode) throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(202, errorStatusCode, 202);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.ApplyOnThrottle), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertNull(client.expectHeaders.get(0));

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertNull(client.expectHeaders.get(1));

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertEquals(CONTINUE, client.expectHeaders.get(2));
    }

    @ParameterizedTest
    @CsvSource({ "1024, 2048, true", "2048, 1024, false", "1024, 1024, true" })
    public void throttlePolicyRespectsThreshold(int threshold, int bodyLength, boolean expectHeader)
        throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(429, 202);
        HttpPipeline pipeline
            = pipeline(options(ExpectContinueMode.ApplyOnThrottle).setContentLengthThreshold((long) threshold), client);

        pipeline.sendSync(requestWithBody(bodyLength), Context.NONE);
        assertNull(client.expectHeaders.get(0));

        pipeline.sendSync(requestWithBody(bodyLength), Context.NONE);
        assertEquals(expectHeader ? CONTINUE : null, client.expectHeaders.get(1));
    }

    @Test
    public void throttlePolicyRevertsAfterBackoff() throws Exception {
        Duration backoff = Duration.ofMillis(50);
        RecordingHttpClient client = new RecordingHttpClient(429, 202, 202);
        HttpPipeline pipeline
            = pipeline(options(ExpectContinueMode.ApplyOnThrottle).setThrottleInterval(backoff), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertNull(client.expectHeaders.get(0));

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertEquals(CONTINUE, client.expectHeaders.get(1));

        Thread.sleep(500);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        assertNull(client.expectHeaders.get(2));
    }

    @SyncAsyncTest
    public void appliesHeaderOnRetryAfterThrottling() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(503, 200);
        HttpPipeline pipeline = retryPipeline(options(ExpectContinueMode.ApplyOnThrottle), client);

        HttpResponse response = send(pipeline, requestWithBody());

        assertEquals(200, response.getStatusCode());
        assertEquals(2, client.expectHeaders.size());
        assertNull(client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
    }

    @SyncAsyncTest
    public void keepsApplyingHeaderAcrossMultipleRetries() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(503, 503, 429, 200);
        HttpPipeline pipeline = retryPipeline(options(ExpectContinueMode.ApplyOnThrottle), client);

        HttpResponse response = send(pipeline, requestWithBody());

        assertEquals(200, response.getStatusCode());
        assertEquals(4, client.expectHeaders.size());
        assertNull(client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
        assertEquals(CONTINUE, client.expectHeaders.get(2));
        assertEquals(CONTINUE, client.expectHeaders.get(3));
    }

    @SyncAsyncTest
    public void onModeAppliesHeaderOnEveryAttempt() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(503, 200);
        HttpPipeline pipeline = retryPipeline(options(ExpectContinueMode.On), client);

        send(pipeline, requestWithBody());

        assertEquals(2, client.expectHeaders.size());
        assertEquals(CONTINUE, client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
    }

    @SyncAsyncTest
    public void offModeDoesNotApplyHeader() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.Off), client);

        send(pipeline, requestWithBody());

        assertNull(client.expectHeaders.get(0));
    }

    @Test
    public void offModeAddsNoPolicy() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, options(ExpectContinueMode.Off));

        assertTrue(policies.isEmpty());
    }

    @Test
    public void defaultOptionsApplyOnThrottle() {
        ExpectContinueOptions options = new ExpectContinueOptions();

        assertEquals(ExpectContinueMode.ApplyOnThrottle, options.getMode());
        assertEquals(Duration.ofMinutes(1), options.getThrottleInterval());
        assertNull(options.getContentLengthThreshold());
    }

    @Test
    public void nullOptionsGetApplyOnThrottlePolicy() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, null);

        assertEquals(1, policies.size());
        assertTrue(policies.get(0) instanceof ExpectContinueOnThrottlePolicy);
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 304, 404, 412, 501 })
    public void applyOnThrottleIgnoresNonThrottlingResponses(int statusCode) throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(statusCode, 200);
        HttpPipeline pipeline = pipeline(options(ExpectContinueMode.ApplyOnThrottle), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertNull(client.expectHeaders.get(0));
        assertNull(client.expectHeaders.get(1));
    }

    @Test
    public void unknownContentLengthIsAlwaysEligible() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline
            = pipeline(options(ExpectContinueMode.On).setContentLengthThreshold(Long.MAX_VALUE), client);

        HttpRequest request = new HttpRequest(HttpMethod.PUT, new URL(ENDPOINT));
        request.setBody(BinaryData.fromStream(new ByteArrayInputStream(BODY)));
        assertNull(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

        pipeline.sendSync(request, Context.NONE);

        assertEquals(CONTINUE, client.expectHeaders.get(0));
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "TRUE" })
    public void configurationOptOutSuppressesHeader(String value) throws MalformedURLException {
        Configuration configuration = environmentConfiguration(
            new TestConfigurationSource().put(Constants.PROPERTY_AZURE_STORAGE_DISABLE_EXPECT_CONTINUE_HEADER, value));
        RecordingHttpClient client = new RecordingHttpClient(503, 200);
        HttpPipeline pipeline = buildPipeline(Arrays.asList(new ExpectContinuePolicy(null, configuration),
            new ExpectContinueOnThrottlePolicy(Duration.ofMinutes(1), null, configuration)), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertNull(client.expectHeaders.get(0));
        assertNull(client.expectHeaders.get(1));
    }

    @Test
    public void configurationOptOutDefaultsToEnabled() throws MalformedURLException {
        Configuration configuration = environmentConfiguration(new TestConfigurationSource());
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline = buildPipeline(Arrays.asList(new ExpectContinuePolicy(null, configuration)), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertEquals(CONTINUE, client.expectHeaders.get(0));
    }

    // The opt out is read with Configuration.get(String, T), which resolves against the environment configuration.
    private static Configuration environmentConfiguration(TestConfigurationSource environment) {
        return new ConfigurationBuilder(new TestConfigurationSource(), new TestConfigurationSource(), environment)
            .build();
    }

    private static ExpectContinueOptions options(ExpectContinueMode mode) {
        return new ExpectContinueOptions().setMode(mode);
    }

    private static HttpRequest requestWithBody() throws MalformedURLException {
        return requestWithBody(BODY.length);
    }

    private static HttpRequest requestWithBody(int bodyLength) throws MalformedURLException {
        return new HttpRequest(HttpMethod.PUT, new URL(ENDPOINT)).setBody(new byte[bodyLength]);
    }

    private static HttpPipeline pipeline(ExpectContinueOptions options, HttpClient client) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, options);
        return buildPipeline(policies, client);
    }

    private static HttpPipeline retryPipeline(ExpectContinueOptions options, HttpClient client) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new RequestRetryPolicy(FAST_RETRY_OPTIONS));
        BuilderUtils.addExpectContinuePolicy(policies, options);
        return buildPipeline(policies, client);
    }

    private static HttpPipeline buildPipeline(List<HttpPipelinePolicy> policies, HttpClient client) {
        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(client)
            .build();
    }

    private static HttpResponse send(HttpPipeline pipeline, HttpRequest request) {
        return SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE), () -> pipeline.send(request));
    }

    /**
     * Records the {@code Expect} header of every request it sees and replies with the given status codes in order,
     * repeating the last one once they are exhausted.
     */
    private static final class RecordingHttpClient extends NoOpHttpClient {
        private final List<String> expectHeaders = new CopyOnWriteArrayList<>();
        private final int[] statusCodes;
        private final AtomicInteger attempt = new AtomicInteger();

        RecordingHttpClient(int... statusCodes) {
            this.statusCodes = statusCodes;
        }

        private HttpResponse handle(HttpRequest request) {
            expectHeaders.add(request.getHeaders().getValue(HttpHeaderName.EXPECT));
            int index = Math.min(attempt.getAndIncrement(), statusCodes.length - 1);
            return new MockHttpResponse(request, statusCodes[index]);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.fromCallable(() -> handle(request));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            return handle(request);
        }
    }
}
