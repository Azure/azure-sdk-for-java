// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.ServiceVersion;
import com.azure.core.util.UserAgentUtil;
import reactor.core.publisher.Mono;

/**
 * The {@code UserAgentPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy is
 * used to add a "User-Agent" header to each {@code HttpRequest}.
 *
 * <p>This class is useful when you need to add a specific "User-Agent" header for all requests in a pipeline.
 * It ensures that the "User-Agent" header is set correctly for each request. The "User-Agent" header is used to
 * provide the server with information about the software used by the client.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code UserAgentPolicy} is created with a "User-Agent" header value of "MyApp/1.0".
 * Once added to the pipeline, requests will have their "User-Agent" header set to "MyApp/1.0" by the
 * {@code UserAgentPolicy}.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.UserAgentPolicy.constructor -->
 * <pre>
 * UserAgentPolicy userAgentPolicy = new UserAgentPolicy&#40;&quot;MyApp&#47;1.0&quot;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.UserAgentPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 * @see com.azure.core.http.HttpHeaderName
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
    private final HttpPipelineSyncPolicy inner = new HttpPipelineSyncPolicy() {
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
     * Creates a UserAgentPolicy with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     *
     * <p>If the passed configuration contains true for AZURE_TELEMETRY_DISABLED the platform information won't be
     * included in the user agent.</p>
     *
     * @param sdkName Name of the client library.
     * @param sdkVersion Version of the client library.
     * @param version {@link ServiceVersion} of the service to be used when making requests.
     * @param configuration Configuration store that will be checked for {@link
     * Configuration#PROPERTY_AZURE_TELEMETRY_DISABLED}. If {@code null} is passed the {@link
     * Configuration#getGlobalConfiguration() global configuration} will be checked.
     * @deprecated Use {@link UserAgentPolicy#UserAgentPolicy(String, String, String, Configuration)} instead.
     */
    @Deprecated
    public UserAgentPolicy(String sdkName, String sdkVersion, Configuration configuration, ServiceVersion version) {
        this.userAgent = UserAgentUtil.toUserAgentString(null, sdkName, sdkVersion, configuration);
    }

    /**
     * Updates the "User-Agent" header with the value supplied in the policy.
     *
     * <p>The {@code context} will be checked for {@code Override-User-Agent} and {@code Append-User-Agent}.
     * {@code Override-User-Agent} will take precedence over the value supplied in the policy,
     * {@code Append-User-Agent} will be appended to the value supplied in the policy.</p>
     *
     * @param context request context
     * @param next The next policy to invoke.
     * @return A publisher that initiates the request upon subscription and emits a response on completion.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return inner.process(context, next);
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
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return inner.processSync(context, next);
    }
}
