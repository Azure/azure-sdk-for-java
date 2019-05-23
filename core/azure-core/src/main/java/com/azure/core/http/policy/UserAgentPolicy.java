// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.util.ImplUtils;
import reactor.core.publisher.Mono;

/**
 * Pipeline policy that adds 'User-Agent' header to a request.
 *
 * Format for User-Agent policy is outlined in https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html under "Client
 * Library Usage Telemetry".
 */
public class UserAgentPolicy implements HttpPipelinePolicy {
    private static final String DEFAULT_USER_AGENT_HEADER = "azsdk-java";

    // From the design guidelines, the default user agent header format is:
    // azsdk-java-<client_lib>/<sdk_version> <platform_info>
    private static final String DEFAULT_USER_AGENT_FORMAT = DEFAULT_USER_AGENT_HEADER + "-%s/%s %s";

    // From the design guidelines, the custom user agent header format is:
    // <application_id> azsdk-java-<client_lib>/<sdk_version> <platform_info>
    private static final String CUSTOM_USER_AGENT_FORMAT = "%s " + DEFAULT_USER_AGENT_HEADER + "-%s/%s %s";

    // From the design guidelines, the platform info format is:
    // <language runtime>; <os name> <os version>
    private static final String PLATFORM_INFO_FORMAT = "%s; %s %s";

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
        if (userAgent != null) {
            this.userAgent = userAgent;
        } else {
            this.userAgent = DEFAULT_USER_AGENT_HEADER;
        }
    }

    /**
     * Creates a UserAgentPolicy with the {@code sdkName} and {@code sdkVersion} in the User-Agent header value.
     *
     * @param sdkName Name of the client library.
     * @param sdkVersion Version of the client library.
     */
    public UserAgentPolicy(String sdkName, String sdkVersion) {
        this(sdkName, sdkVersion, null);
    }

    /**
     * Creates a UserAgentPolicy with the {@code sdkName}, {@code sdkVersion}, and a potential {@code applicationId}
     * in the User-Agent header value.
     *
     * @param sdkName Name of the client library.
     * @param sdkVersion Version of the client library.
     * @param applicationId Optional application ID supplied by the consumer of the library. This will be prepended to
     *                      User-Agent string as specified by specification.
     */
    public UserAgentPolicy(String sdkName, String sdkVersion, String applicationId) {
        if (ImplUtils.isNullOrEmpty(applicationId)) {
            this.userAgent = String.format(DEFAULT_USER_AGENT_FORMAT, sdkName, sdkVersion, getPlatformInfo());
        } else {
            this.userAgent = String.format(CUSTOM_USER_AGENT_FORMAT, applicationId, sdkName, sdkVersion, getPlatformInfo());
        }
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String header = context.httpRequest().headers().value("User-Agent");
        if (header == null || DEFAULT_USER_AGENT_HEADER.equals(header)) {
            header = userAgent;
        } else {
            header = userAgent + " " + header;
        }
        context.httpRequest().headers().put("User-Agent", header);
        return next.process();
    }

    private static String getPlatformInfo() {
        String javaVersion = ConfigurationManager.getConfiguration().get("java.version");
        String osName = ConfigurationManager.getConfiguration().get("os.name");
        String osVersion = ConfigurationManager.getConfiguration().get("os.version");

        return String.format(PLATFORM_INFO_FORMAT, javaVersion, osName, osVersion);
    }
}
