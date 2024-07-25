// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.http.policy;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.USER_AGENT_TYPE;

import org.springframework.util.StringUtils;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.spring.cloud.appconfiguration.config.implementation.RequestTracingConstants;

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
    public static final String USER_AGENT = String.format("%s/%s", StringUtils.replace(PACKAGE_NAME, " ", ""),
        BaseAppConfigurationPolicy.class.getPackage().getImplementationVersion());

    static Boolean watchRequests = false;

    final TracingInfo tracingInfo;

    /**
     * App Configuration Http Pipeline Policy
     * @param tracingInfo Usage info for provider
     */
    public BaseAppConfigurationPolicy(TracingInfo tracingInfo) {
        this.tracingInfo = tracingInfo;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String sdkUserAgent = context.getHttpRequest().getHeaders().get(USER_AGENT_TYPE).getValue();
        context.getHttpRequest().getHeaders().set(USER_AGENT_TYPE, USER_AGENT + " " + sdkUserAgent);
        context.getHttpRequest().getHeaders().set(RequestTracingConstants.CORRELATION_CONTEXT_HEADER.toString(),
            tracingInfo.getValue(watchRequests));

        return next.process();
    }

    /**
     * @param watchRequests the watchRequests to set
     */
    public static void setWatchRequests(Boolean watchRequests) {
        BaseAppConfigurationPolicy.watchRequests = watchRequests;
    }

}
