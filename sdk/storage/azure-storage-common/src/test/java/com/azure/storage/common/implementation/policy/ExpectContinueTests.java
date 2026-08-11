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
import com.azure.storage.common.policy.Request100ContinueMode;
import com.azure.storage.common.policy.Request100ContinueOptions;
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

    /*
     * Retry with a negligible backoff so the retry tests stay fast.
     */
    private static final RequestRetryOptions FAST_RETRY_OPTIONS
        = new RequestRetryOptions(RetryPolicyType.FIXED, 4, (Integer) null, 1L, 5L, null);

    @SyncAsyncTest
    public void alwaysModeAppliesHeader() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline = pipeline(options(Request100ContinueMode.ALWAYS), client);

        send(pipeline, requestWithBody());

        assertEquals(1, client.expectHeaders.size());
        assertEquals(CONTINUE, client.expectHeaders.get(0));
    }

    @SyncAsyncTest
    public void alwaysModeDoesNotApplyHeaderWithoutBody() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline = pipeline(options(Request100ContinueMode.ALWAYS), client);

        send(pipeline, new HttpRequest(HttpMethod.GET, new URL(ENDPOINT)));

        assertNull(client.expectHeaders.get(0));
    }

    @SyncAsyncTest
    public void neverModeDoesNotApplyHeader() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline = pipeline(options(Request100ContinueMode.NEVER), client);

        send(pipeline, requestWithBody());

        assertNull(client.expectHeaders.get(0));
    }

    @Test
    public void neverModeAddsNoPolicy() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, options(Request100ContinueMode.NEVER));

        assertTrue(policies.isEmpty());
    }

    @Test
    public void defaultOptionsAreAuto() {
        Request100ContinueOptions options = new Request100ContinueOptions();

        assertEquals(Request100ContinueMode.AUTO, options.getMode());
        assertEquals(Duration.ofMinutes(1), options.getAutoInterval());
        assertNull(options.getContentLengthThreshold());
    }

    @Test
    public void nullOptionsGetAutoPolicy() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, null);

        assertEquals(1, policies.size());
        assertTrue(policies.get(0) instanceof ExpectContinueOnThrottlePolicy);
    }

    @ParameterizedTest
    @CsvSource({ "512, true", "1024, true", "1025, false", "2048, false" })
    public void contentLengthThresholdIsHonored(long threshold, boolean expectHeader) throws MalformedURLException {
        // BODY is 1024 bytes, so it is eligible when the threshold is at or below that.
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline
            = pipeline(options(Request100ContinueMode.ALWAYS).setContentLengthThreshold(threshold), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertEquals(expectHeader ? CONTINUE : null, client.expectHeaders.get(0));
    }

    @ParameterizedTest
    @CsvSource({ "512, true", "1024, true", "1025, false", "2048, false" })
    public void autoModeContentLengthThresholdIsHonored(long threshold, boolean expectHeader)
        throws MalformedURLException {
        // The threshold applies in auto mode too, so a throttled service does not cause tiny requests to pay for the
        // additional round trip.
        RecordingHttpClient client = new RecordingHttpClient(503, 200);
        HttpPipeline pipeline
            = pipeline(options(Request100ContinueMode.AUTO).setContentLengthThreshold(threshold), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertNull(client.expectHeaders.get(0));
        assertEquals(expectHeader ? CONTINUE : null, client.expectHeaders.get(1));
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

    @Test
    public void unknownContentLengthIsAlwaysEligible() throws MalformedURLException {
        // A body whose length cannot be determined ahead of time is never measured against the threshold, as doing so
        // would require buffering it.
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline
            = pipeline(options(Request100ContinueMode.ALWAYS).setContentLengthThreshold(Long.MAX_VALUE), client);

        HttpRequest request = new HttpRequest(HttpMethod.PUT, new URL(ENDPOINT));
        request.setBody(BinaryData.fromStream(new ByteArrayInputStream(BODY)));
        assertNull(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

        pipeline.sendSync(request, Context.NONE);

        assertEquals(CONTINUE, client.expectHeaders.get(0));
    }

    @SyncAsyncTest
    public void autoModeDoesNotApplyHeaderBeforeThrottling() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(200);
        HttpPipeline pipeline = pipeline(options(Request100ContinueMode.AUTO), client);

        send(pipeline, requestWithBody());
        send(pipeline, requestWithBody());

        assertNull(client.expectHeaders.get(0));
        assertNull(client.expectHeaders.get(1));
    }

    @ParameterizedTest
    @ValueSource(ints = { 429, 500, 503 })
    public void autoModeAppliesHeaderAfterThrottlingResponse(int statusCode) throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(statusCode, 200);
        HttpPipeline pipeline = pipeline(options(Request100ContinueMode.AUTO), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertNull(client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 304, 404, 412, 501 })
    public void autoModeIgnoresNonThrottlingResponses(int statusCode) throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(statusCode, 200);
        HttpPipeline pipeline = pipeline(options(Request100ContinueMode.AUTO), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertNull(client.expectHeaders.get(0));
        assertNull(client.expectHeaders.get(1));
    }

    @Test
    public void autoModeStopsApplyingHeaderAfterIntervalElapses() throws Exception {
        RecordingHttpClient client = new RecordingHttpClient(503, 200);
        HttpPipeline pipeline
            = pipeline(options(Request100ContinueMode.AUTO).setAutoInterval(Duration.ofMillis(50)), client);

        pipeline.sendSync(requestWithBody(), Context.NONE);
        pipeline.sendSync(requestWithBody(), Context.NONE);

        // Sleeping well past the interval, so the window can only be closed by the time the third request is sent.
        Thread.sleep(500);
        pipeline.sendSync(requestWithBody(), Context.NONE);

        assertNull(client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
        assertNull(client.expectHeaders.get(2));
    }

    /**
     * The policy is placed after the retry policy so that a throttling response takes effect on the very next attempt,
     * rather than only on a later call.
     */
    @SyncAsyncTest
    public void autoModeAppliesHeaderOnRetryAfterThrottling() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(503, 200);
        HttpPipeline pipeline = retryPipeline(options(Request100ContinueMode.AUTO), client);

        HttpResponse response = send(pipeline, requestWithBody());

        assertEquals(200, response.getStatusCode());
        assertEquals(2, client.expectHeaders.size());
        assertNull(client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
    }

    @SyncAsyncTest
    public void autoModeKeepsApplyingHeaderAcrossMultipleRetries() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(503, 503, 429, 200);
        HttpPipeline pipeline = retryPipeline(options(Request100ContinueMode.AUTO), client);

        HttpResponse response = send(pipeline, requestWithBody());

        assertEquals(200, response.getStatusCode());
        assertEquals(4, client.expectHeaders.size());
        assertNull(client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
        assertEquals(CONTINUE, client.expectHeaders.get(2));
        assertEquals(CONTINUE, client.expectHeaders.get(3));
    }

    @SyncAsyncTest
    public void alwaysModeAppliesHeaderOnEveryAttempt() throws MalformedURLException {
        RecordingHttpClient client = new RecordingHttpClient(503, 200);
        HttpPipeline pipeline = retryPipeline(options(Request100ContinueMode.ALWAYS), client);

        send(pipeline, requestWithBody());

        assertEquals(2, client.expectHeaders.size());
        assertEquals(CONTINUE, client.expectHeaders.get(0));
        assertEquals(CONTINUE, client.expectHeaders.get(1));
    }

    /*
     * The opt out is read with the legacy Configuration.get(String, T), which resolves against the environment
     * configuration rather than the explicit source.
     */
    private static Configuration environmentConfiguration(TestConfigurationSource environment) {
        return new ConfigurationBuilder(new TestConfigurationSource(), new TestConfigurationSource(), environment)
            .build();
    }

    private static Request100ContinueOptions options(Request100ContinueMode mode) {
        return new Request100ContinueOptions().setMode(mode);
    }

    private static HttpRequest requestWithBody() throws MalformedURLException {
        return new HttpRequest(HttpMethod.PUT, new URL(ENDPOINT)).setBody(BODY);
    }

    private static HttpPipeline pipeline(Request100ContinueOptions options, HttpClient client) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        BuilderUtils.addExpectContinuePolicy(policies, options);
        return buildPipeline(policies, client);
    }

    private static HttpPipeline retryPipeline(Request100ContinueOptions options, HttpClient client) {
        // Mirrors the production ordering, where the policy sits below the retry policy.
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
