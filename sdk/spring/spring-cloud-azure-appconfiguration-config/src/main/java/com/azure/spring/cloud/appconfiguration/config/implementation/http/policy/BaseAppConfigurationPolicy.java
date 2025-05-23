// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.http.policy;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.PUSH_REFRESH;

import org.springframework.util.StringUtils;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants;

import reactor.core.publisher.Mono;

/**
 * HttpPipelinePolicy for connecting to Azure App Configuration.
 */
public final class BaseAppConfigurationPolicy implements HttpPipelinePolicy {

    /**
     * Library Package name
     */
    private static final String PACKAGE_NAME = BaseAppConfigurationPolicy.class.getPackage().getImplementationTitle();

    /**
     * Format of User Agent
     */
    private static final String USER_AGENT = String.format("%s/%s", StringUtils.replace(PACKAGE_NAME, " ", ""),
        BaseAppConfigurationPolicy.class.getPackage().getImplementationVersion());

    private final TracingInfo tracingInfo;

    /**
     * App Configuration Http Pipeline Policy
     * @param tracingInfo Usage info for provider
     */
    public BaseAppConfigurationPolicy(TracingInfo tracingInfo) {
        this.tracingInfo = tracingInfo;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        Boolean watchRequests = (Boolean) context.getData("refresh").orElse(false);
        Boolean pushRefresh = (Boolean) context.getData(PUSH_REFRESH).orElse(false);
        FeatureFlagTracing ffTracing = (FeatureFlagTracing) context.getData("FeatureFlagTracing").orElse(null);
        HttpHeaders headers = context.getHttpRequest().getHeaders();
        String sdkUserAgent = headers.get(HttpHeaderName.USER_AGENT).getValue();
        headers.set(HttpHeaderName.USER_AGENT, USER_AGENT + " " + sdkUserAgent);
        headers.set(HttpHeaderName.fromString(AppConfigurationConstants.CORRELATION_CONTEXT),
            tracingInfo.getValue(watchRequests, pushRefresh, ffTracing));

        return next.process();
    }

}
