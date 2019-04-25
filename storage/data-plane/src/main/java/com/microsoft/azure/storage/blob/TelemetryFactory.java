/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        String userAgentPrefix = telemetryOptions.userAgentPrefix() == null ?
                Constants.EMPTY_STRING : telemetryOptions.userAgentPrefix();
        this.userAgent = userAgentPrefix + ' ' +
                Constants.HeaderConstants.USER_AGENT_PREFIX + '/' + Constants.HeaderConstants.USER_AGENT_VERSION +
                String.format(Locale.ROOT, " (JavaJRE %s; %s %s)",
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
