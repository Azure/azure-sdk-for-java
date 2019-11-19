// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.ServiceVersion;
import com.azure.core.util.Configuration;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Pipeline policy that adds "User-Agent" header to a request.
 *
 * The format for the "User-Agent" string is outlined in
 * <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry policy</a>.
 */
public class UserAgentPolicy implements HttpPipelinePolicy {
    private static final String DEFAULT_USER_AGENT_HEADER = "azsdk-java";

    // From the design guidelines, the default user agent header format is:
    // azsdk-java-<client_lib>/<sdk_version> <platform_info>
    private static final String USER_AGENT_FORMAT = DEFAULT_USER_AGENT_HEADER + "-%s/%s (%s)";

    // From the design guidelines, the application id user agent header format is:
    // AzCopy/10.0.4-Preview azsdk-java-<client_lib>/<sdk_version> <platform_info>
    private static final String APPLICATION_ID_USER_AGENT_FORMAT = "%s " + USER_AGENT_FORMAT;

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
     * @param applicationId User specified application Id.
     * @param sdkName Name of the client library.
     * @param sdkVersion Version of the client library.
     * @param version {@link ServiceVersion} of the service to be used when making requests.
     * @param configuration Configuration store that will be checked for the AZURE_TELEMETRY_DISABLED.
     * @throws NullPointerException if {@code configuration} is {@code null}.
     */
    public UserAgentPolicy(String applicationId, String sdkName, String sdkVersion, Configuration configuration,
                           ServiceVersion version) {
        Objects.requireNonNull(configuration, "'configuration' cannot be null.");
        boolean telemetryDisabled = configuration.get(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED, false);
        if (telemetryDisabled) {
            this.userAgent = String.format(DISABLED_TELEMETRY_USER_AGENT_FORMAT, sdkName, sdkVersion,
                version.getVersion());
        } else {
            if (applicationId == null) {
                this.userAgent = String.format(USER_AGENT_FORMAT, sdkName, sdkVersion, getPlatformInfo(),
                    version.getVersion());
            } else {
                this.userAgent = String.format(APPLICATION_ID_USER_AGENT_FORMAT, applicationId, sdkName, sdkVersion,
                    getPlatformInfo(), version.getVersion());
            }
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
     * @param version {@link ServiceVersion} of the service to be used when making requests.
     * @param configuration Configuration store that will be checked for the AZURE_TELEMETRY_DISABLED.
     * @throws NullPointerException if {@code configuration} is {@code null}.
     */
    public UserAgentPolicy(String sdkName, String sdkVersion, Configuration configuration, ServiceVersion version) {
        Objects.requireNonNull(configuration, "'configuration' cannot be null.");
        boolean telemetryDisabled = configuration.get(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED, false);
        if (telemetryDisabled) {
            this.userAgent = String.format(DISABLED_TELEMETRY_USER_AGENT_FORMAT, sdkName, sdkVersion,
                version.getVersion());
        } else {
            this.userAgent = String.format(USER_AGENT_FORMAT, sdkName, sdkVersion, getPlatformInfo(),
                version.getVersion());
        }
    }

    /**
     * Updates the "User-Agent" header with the value supplied in the policy.
     *
     * When the User-Agent header already has a value and it differs from the value used to create this policy the
     * User-Agent header is updated by prepending the value in this policy.
     * {@inheritDoc}
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String header = context.getHttpRequest().getHeaders().getValue("User-Agent");
        if (header == null || header.contains(DEFAULT_USER_AGENT_HEADER)) {
            header = userAgent;
        } else {
            header = userAgent + " " + header;
        }
        context.getHttpRequest().getHeaders().put("User-Agent", header);
        return next.process();
    }

    private static String getPlatformInfo() {
        String javaVersion = Configuration.getGlobalConfiguration().get("java.version");
        String osName = Configuration.getGlobalConfiguration().get("os.name");
        String osVersion = Configuration.getGlobalConfiguration().get("os.version");

        return String.format(PLATFORM_INFO_FORMAT, javaVersion, osName, osVersion);
    }
}
