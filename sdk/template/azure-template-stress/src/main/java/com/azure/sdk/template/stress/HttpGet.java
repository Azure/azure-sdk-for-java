// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.template.stress;


import com.azure.core.util.logging.ClientLogger;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.sdk.template.stress.util.TelemetryHelper;
import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.okhttp.OkHttpHttpClientProvider;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.http.pipeline.HttpPipelineBuilder;
import com.generic.core.http.pipeline.HttpPipelinePolicy;
import com.generic.core.http.policy.HttpLoggingPolicy;
import com.generic.core.http.policy.RetryPolicy;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Performance test for simple HTTP GET against test server.
 */
public class HttpGet extends ScenarioBase<StressOptions> {
    // there will be multiple instances of scenario
    private final static TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(HttpGet.class);
    private final static ClientLogger LOGGER = new ClientLogger(HttpGet.class);
    private final HttpPipeline pipeline;
    private final URL url;

    public HttpGet(StressOptions options) {
        super(options, TELEMETRY_HELPER);
        pipeline = getPipelineBuilder().build();
        try {
            url = new URL(options.getServiceEndpoint());
        } catch (MalformedURLException ex) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'url' must be a valid URL.", ex));
        }
    }

    @Override
    public void run() {
        TELEMETRY_HELPER.instrumentRun(this::runInternal);
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.error(new UnsupportedOperationException("Not implemented"));
    }

    private void runInternal() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        // no need to handle exceptions here, they will be handled (and recorded) by the telemetry helper
        try(HttpResponse response = pipeline.send(request)) {
            response.getBody().toBytes();
        }
    }

    private HttpPipelineBuilder getPipelineBuilder() {
        HttpLoggingPolicy.HttpLogOptions logOptions = new HttpLoggingPolicy.HttpLogOptions()
            .setLogLevel(HttpLoggingPolicy.HttpLogOptions.HttpLogDetailLevel.HEADERS);

        ArrayList<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new RetryPolicy());
        policies.add(new HttpLoggingPolicy(logOptions));

        HttpPipelineBuilder builder = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]));
        if (options.getHttpClient() == PerfStressOptions.HttpClientType.OKHTTP) {
            builder.httpClient(new OkHttpHttpClientProvider().createInstance());
        }
        return builder;
    }
}
