// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;

import java.util.Locale;

/**
 * This is a factory which creates policies in an {@link HttpPipeline} for adding telemetry to a given HTTP request. In
 * most cases, it is sufficient to configure a {@link TelemetryOptions} object and set those as a field on a
 * {@link PipelineOptions} object to configure a default pipeline. The factory and policy must only be used directly
 * when creating a custom pipeline.
 */
public final class TelemetryFactory implements RequestPolicyFactory {

    private final String userAgent;

    /**
     * Creates a factory that can create telemetry policy objects which add telemetry information to the outgoing
     * HTTP requests.
     *
     * @param telemetryOptions
     *         {@link TelemetryOptions}
     */
    public TelemetryFactory(TelemetryOptions telemetryOptions) {
        telemetryOptions = telemetryOptions == null ? new TelemetryOptions() : telemetryOptions;
        String userAgentPrefix = telemetryOptions.userAgentPrefix() == null
                ? Constants.EMPTY_STRING : telemetryOptions.userAgentPrefix();
        this.userAgent = userAgentPrefix + ' '
                + Constants.HeaderConstants.USER_AGENT_PREFIX + '/' + Constants.HeaderConstants.USER_AGENT_VERSION
                + String.format(Locale.ROOT, " (JavaJRE %s; %s %s)",
                        System.getProperty("java.version"),
                        System.getProperty("os.name").replaceAll(" ", ""),
                        System.getProperty("os.version"));
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new TelemetryPolicy(next, options);
    }

    private final class TelemetryPolicy implements RequestPolicy {
        private final RequestPolicy nextPolicy;

        private TelemetryPolicy(RequestPolicy nextPolicy, RequestPolicyOptions options) {
            this.nextPolicy = nextPolicy;
        }

        public Single<HttpResponse> sendAsync(HttpRequest request) {
            request.headers().set(Constants.HeaderConstants.USER_AGENT, userAgent);
            return this.nextPolicy.sendAsync(request);
        }
    }
}
