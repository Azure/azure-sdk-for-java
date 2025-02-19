// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.policy;

import com.azure.v2.core.utils.CoreUtils;
import com.azure.v2.core.utils.UserAgentUtil;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePosition;
import io.clientcore.core.utils.Context;
import io.clientcore.core.utils.configuration.Configuration;

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
 * <!-- end com.azure.core.http.policy.UserAgentPolicy.constructor -->
 *
 * @see com.azure.v2.core.http.policy
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 * @see HttpHeaderName
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
     * @param configuration Configuration store that will be checked configurations.
     */
    public UserAgentPolicy(String applicationId, String sdkName, String sdkVersion, Configuration configuration) {
        this.userAgent = UserAgentUtil.toUserAgentString(applicationId, sdkName, sdkVersion, configuration);
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        final Context context = httpRequest.getRequestOptions().getContext();
        String overrideUserAgent = (String) context.get(OVERRIDE_USER_AGENT_CONTEXT_KEY);
        String appendUserAgent = (String) context.get(APPEND_USER_AGENT_CONTEXT_KEY);

        String userAgentValue;
        if (!CoreUtils.isNullOrEmpty(overrideUserAgent)) {
            userAgentValue = overrideUserAgent;
        } else if (!CoreUtils.isNullOrEmpty(appendUserAgent)) {
            userAgentValue = userAgent + " " + appendUserAgent;
        } else {
            userAgentValue = userAgent;
        }

        httpRequest.getHeaders().add(HttpHeaderName.USER_AGENT, userAgentValue);

        return next.process();
    }

    @Override
    public final HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.BEFORE_REDIRECT;
    }
}
