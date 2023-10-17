// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.policy;

import com.generic.core.http.HttpHeaderName;
import com.generic.core.http.HttpPipelineNextPolicy;
import com.generic.core.http.models.HttpPipelineCallContext;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.policy.HttpPipelinePolicy;
import com.generic.core.http.policy.HttpPipelinePolicyImpl;
import com.generic.core.models.Context;
import com.generic.core.util.CoreUtils;
import com.generic.core.util.configuration.Configuration;

/**
 * Pipeline policy that adds "User-Agent" header to a request.
 * <p>
 * The format for the "User-Agent" string is outlined in
 * <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry policy</a>.
 */
public class UserAgentPolicy implements HttpPipelinePolicy {
    /**
     * Key for {@link Context} to add a value which will override the User-Agent supplied in this policy in an ad-hoc
     * manner.
     */
    public static final String OVERRIDE_USER_AGENT_CONTEXT_KEY = "Override-User-Agent";

    /**
     * Key for {@link Context} to add a value which will be appended to the User-Agent supplied in this policy in an
     * ad-hoc manner.
     */
    public static final String APPEND_USER_AGENT_CONTEXT_KEY = "Append-User-Agent";

    private final String userAgent;
    private final HttpPipelinePolicyImpl inner = new HttpPipelinePolicyImpl() {
        /**
         * Updates the "User-Agent" header with the value supplied in the policy.
         *
         * <p>The {@code context} will be checked for {@code Override-User-Agent} and {@code Append-User-Agent}.
         * {@code Override-User-Agent} will take precedence over the value supplied in the policy,
         * {@code Append-User-Agent} will be appended to the value supplied in the policy.</p>
         *
         * @param context request context
         */
        @Override
        protected void beforeSendingRequest(HttpPipelineCallContext context) {
            String overrideUserAgent = (String) context.getData(OVERRIDE_USER_AGENT_CONTEXT_KEY).orElse(null);
            String appendUserAgent = (String) context.getData(APPEND_USER_AGENT_CONTEXT_KEY).orElse(null);

            String userAgentValue;
            if (!CoreUtils.isNullOrEmpty(overrideUserAgent)) {
                userAgentValue = overrideUserAgent;
            } else if (!CoreUtils.isNullOrEmpty(appendUserAgent)) {
                userAgentValue = userAgent + " " + appendUserAgent;
            } else {
                userAgentValue = userAgent;
            }

            context.getHttpRequest().setHeader(HttpHeaderName.USER_AGENT, userAgentValue);
        }
    };

    /**
     * Creates a {@link UserAgentPolicy} with a default user agent string.
     */
    public UserAgentPolicy() {
        this(null);
    }

    /**
     * Creates a UserAgentPolicy with {@code userAgent} as the header value. If {@code userAgent} is {@code null}, then
     * the default user agent value is used.
     *
     * @param userAgent The user agent string to add to request headers.
     */
    public UserAgentPolicy(String userAgent) {
        // TODO: should a custom useragent string be allowed?
        if (userAgent != null) {
            this.userAgent = userAgent;
        } else {
            this.userAgent = UserAgentUtil.DEFAULT_USER_AGENT_HEADER;
        }
    }

    /**
     * Creates a UserAgentPolicy with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     *
     * <p>If the passed configuration contains true for AZURE_TELEMETRY_DISABLED the platform information won't be
     * included in the user agent.</p>
     *
     * @param applicationId User specified application Id.
     * @param sdkName Name of the client library.
     * @param sdkVersion Version of the client library.
     * @param configuration Configuration store that will be checked for {@link
     * Configuration#PROPERTY_AZURE_TELEMETRY_DISABLED}. If {@code null} is passed the {@link
     * Configuration#getGlobalConfiguration() global configuration} will be checked.
     */
    public UserAgentPolicy(String applicationId, String sdkName, String sdkVersion, Configuration configuration) {
        this.userAgent = UserAgentUtil.toUserAgentString(applicationId, sdkName, sdkVersion, configuration);
    }

    /**
     * Updates the "User-Agent" header with the value supplied in the policy synchronously.
     *
     * <p>The {@code context} will be checked for {@code Override-User-Agent} and {@code Append-User-Agent}.
     * {@code Override-User-Agent} will take precedence over the value supplied in the policy,
     * {@code Append-User-Agent} will be appended to the value supplied in the policy.</p>
     *
     * @param context request context
     * @param next The next policy to invoke.
     * @return A response.
     */
    @Override
    public HttpResponse process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return inner.process(context, next);
    }
}
