// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http.policy;

import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * Pipeline policy that adds 'User-Agent' header to a request.
 *
 * Format for User-Agent policy is outlined in https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html under "Client
 * Library Usage Telemetry".
 */
public class UserAgentPolicy implements HttpPipelinePolicy {
    private static final String DEFAULT_USER_AGENT_HEADER = "azsdk-java";

    // From the design guidelines, the user agent header format is:
    // azsdk-java-<client_lib>/<sdk_version> <platform_info>
    private static final String USER_AGENT_FORMAT = DEFAULT_USER_AGENT_HEADER + "-%s/%s %s";

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
        String platformInfo = System.getProperty("java.version") + "; " + getOSInformation();
        this.userAgent = String.format(USER_AGENT_FORMAT, sdkName, sdkVersion, platformInfo);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String header = context.httpRequest().headers().value("User-Agent");
        if (header == null || DEFAULT_USER_AGENT_HEADER.equals(header)) {
            header = userAgent;
        } else {
            header = userAgent + " " + header;
        }
        context.httpRequest().headers().set("User-Agent", header);
        return next.process();
    }

    private static String getOSInformation() {
        return String.join(" ", System.getProperty("os.name"), System.getProperty("os.version"));
    }
}
