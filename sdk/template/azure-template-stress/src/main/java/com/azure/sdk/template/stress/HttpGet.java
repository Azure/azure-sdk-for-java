// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.template.stress;

import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.azure.core.http.okhttp.OkHttpAsyncClientProvider;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RedirectPolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Context;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.sdk.template.stress.util.TelemetryHelper;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Performance test for simple HTTP GET against test server.
 */
public class HttpGet extends ScenarioBase<StressOptions> {
    // there will be multiple instances of scenario
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(HttpGet.class);
    private static final ClientLogger LOGGER = new ClientLogger(HttpGet.class);
    private final HttpPipeline pipeline;
    private final URL url;

    /**
     * Creates an instance of performance test.
     * @param options stress test options
     */
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

    private void runInternal() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        // no need to handle exceptions here, they will be handled (and recorded) by the telemetry helper
        HttpResponse response = pipeline.sendSync(request, Context.NONE);
        response.buffer().close();
        response.close();
    }

    @Override
    public Mono<Void> runAsync() {
        return TELEMETRY_HELPER.instrumentRunAsync(runInternalAsync());
    }

    private Mono<Void> runInternalAsync() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, options.getServiceEndpoint());
        // no need to handle exceptions here, they will be handled (and recorded) by the telemetry helper
        return pipeline.send(request)
                .flatMapMany(response -> response.getBody()
                    .doFinally(i -> response.close()))
            .then();
    }

    private HttpPipelineBuilder getPipelineBuilder() {
        HttpLogOptions logOptions = new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.HEADERS);

        ArrayList<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy("java-stress-test"));
        policies.add(new RequestIdPolicy());
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(new RetryPolicy());
        policies.add(new AddDatePolicy());
        policies.add(new RedirectPolicy());
        policies.add(new HttpLoggingPolicy(logOptions));

        HttpClientOptions httpClientOptions = new HttpClientOptions()
            .setHttpClientProvider(getHttpClientProvider());
        return new HttpPipelineBuilder()
            .clientOptions(httpClientOptions)
            .policies(policies.toArray(new HttpPipelinePolicy[0]));
    }

    private Class<? extends HttpClientProvider> getHttpClientProvider() {
        switch (options.getHttpClient()) {
            case OKHTTP:
                return OkHttpAsyncClientProvider.class;
            case NETTY:
                return NettyAsyncHttpClientProvider.class;
            default:
                throw LOGGER.logThrowableAsError(new IllegalArgumentException("Unknown HTTP Client provider: " + options.getHttpClient()));
        }
    }
}
