// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.FluxUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.RemoteDependencyTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.RequestTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedDuration;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedTime;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class QuickPulseTestBase extends TestProxyTestBase {
    private static final String APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE = "https://monitor.azure.com//.default";

    HttpPipeline getHttpPipelineWithAuthentication() {
        if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
            TokenCredential credential
                = new DefaultAzureCredentialBuilder().managedIdentityClientId("AZURE_CLIENT_ID").build();
            return getHttpPipeline(
                new BearerTokenAuthenticationPolicy(credential, APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE));
        } else {
            return getHttpPipeline(new BearerTokenAuthenticationPolicy(new MockTokenCredential()));
        }
    }

    HttpPipeline getHttpPipeline(HttpPipelinePolicy... policies) {
        HttpClient httpClient;
        List<HttpPipelinePolicy> allPolicies = new ArrayList<>(Arrays.asList(policies));
        httpClient = HttpClient.createDefault();
        if (getTestMode() == TestMode.RECORD) {
            allPolicies.add(interceptorManager.getRecordPolicy());
        }

        if (getTestMode() == TestMode.PLAYBACK) {
            httpClient = interceptorManager.getPlaybackClient();
            interceptorManager.addMatchers(Arrays.asList(new BodilessMatcher(),
                new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("x-ms-qps-transmission-time"))));
        }
        return new HttpPipelineBuilder().httpClient(httpClient)
            .policies(allPolicies.toArray(new HttpPipelinePolicy[0]))
            .tracer(new NoopTracer())
            .build();
    }

    public static TelemetryItem createRequestTelemetry(String name, Date timestamp, long durationMillis,
        String responseCode, boolean success) {
        RequestTelemetryBuilder telemetryBuilder = RequestTelemetryBuilder.create();
        telemetryBuilder.addProperty("customProperty", "customValue");
        telemetryBuilder.setName(name);
        telemetryBuilder.setDuration(FormattedDuration.fromNanos(MILLISECONDS.toNanos(durationMillis)));
        telemetryBuilder.setResponseCode(responseCode);
        telemetryBuilder.setSuccess(success);
        telemetryBuilder.setUrl("foo");
        telemetryBuilder.setTime(FormattedTime.offSetDateTimeFromEpochMillis(timestamp.getTime()));
        return telemetryBuilder.build();
    }

    public static TelemetryItem createRemoteDependencyTelemetry(String name, String command, long durationMillis,
        boolean success) {
        RemoteDependencyTelemetryBuilder telemetryBuilder = RemoteDependencyTelemetryBuilder.create();
        telemetryBuilder.addProperty("customProperty", "customValue");
        telemetryBuilder.setName(name);
        telemetryBuilder.setData(command);
        telemetryBuilder.setDuration(FormattedDuration.fromNanos(MILLISECONDS.toNanos(durationMillis)));
        telemetryBuilder.setSuccess(success);
        return telemetryBuilder.build();
    }

    static class ValidationPolicy implements HttpPipelinePolicy {

        private final CountDownLatch countDown;
        private final String expectedRequestBody;

        ValidationPolicy(CountDownLatch countDown, String expectedRequestBody) {
            this.countDown = countDown;
            this.expectedRequestBody = expectedRequestBody;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            Mono<String> asyncString = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody())
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
            asyncString.subscribe(value -> {
                if (Pattern.matches(expectedRequestBody, value)) {
                    countDown.countDown();
                }
            });
            return next.process();
        }
    }
}
