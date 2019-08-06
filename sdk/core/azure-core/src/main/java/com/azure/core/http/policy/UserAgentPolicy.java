// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.configuration.BaseConfigurations;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
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
    private static final String USER_AGENT_FORMAT = DEFAULT_USER_AGENT_HEADER + "-%s/%s %s";

    // When the AZURE_TELEMETRY_DISABLED configuration is true remove the <platform_info> portion of the user agent.
    private static final String DISABLED_TELEMETRY_USER_AGENT_FORMAT = DEFAULT_USER_AGENT_HEADER + "-%s/%s";

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
     * If the passed configuration contains true for AZURE_TELEMETRY_DISABLED the platform information won't be included
     * in the user agent.
     *
     * @param sdkName Name of the client library.
     * @param sdkVersion Version of the client library.
     * @param configuration Configuration store that will be checked for the AZURE_TELEMETRY_DISABLED.
     */
    public UserAgentPolicy(String sdkName, String sdkVersion, Configuration configuration) {
        boolean telemetryDisabled = configuration.get(BaseConfigurations.AZURE_TELEMETRY_DISABLED, false);
        if (telemetryDisabled) {
            this.userAgent = String.format(DISABLED_TELEMETRY_USER_AGENT_FORMAT, sdkName, sdkVersion);
        } else {
            this.userAgent = String.format(USER_AGENT_FORMAT, sdkName, sdkVersion, getPlatformInfo());
        }
    }

    /**
     * Updates the User-Agent header with the value supplied in the policy.
     *
     * When the User-Agent header already has a value and it differs from the value used to create this policy the
     * User-Agent header is updated by prepending the value in this policy.
     * {@inheritDoc}
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String header = context.httpRequest().headers().value("User-Agent");
        if (header == null || header.startsWith(DEFAULT_USER_AGENT_HEADER)) {
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
