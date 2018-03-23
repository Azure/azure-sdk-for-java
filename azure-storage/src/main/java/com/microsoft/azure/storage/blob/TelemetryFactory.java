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

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;

import java.util.Locale;

/**
 * Used to add telemetry to outgoing HTTP requests.
 */
public final class TelemetryFactory implements RequestPolicyFactory {

    private final String userAgent;

    /**
     * Creates a factory that can create telemetry policy objects which add telemetry information to the outgoing
     * HTTP requests.
     *
     * @param telemetryOptions
     *      A {@link TelemetryOptions} object which configures the behavior for the telemetry policies produced by this
     *      factory.
     */
    public TelemetryFactory(TelemetryOptions telemetryOptions) {
        String userAgentPrefix = telemetryOptions.getUserAgentPrefix() == null ?
                Constants.EMPTY_STRING : telemetryOptions.getUserAgentPrefix();
        this.userAgent = userAgentPrefix + ' ' +
                Constants.HeaderConstants.USER_AGENT_PREFIX + '/' + Constants.HeaderConstants.USER_AGENT_VERSION +
                String.format(Locale.US, "(JavaJRE %s; %s %s)",
                    System.getProperty("java.version"),
                    System.getProperty("os.name").replaceAll(" ", ""),
                    System.getProperty("os.version"));
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

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new TelemetryPolicy(next, options);
    }
}
