// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

public class UserAgentPolicy implements HttpPipelinePolicy {
    private static final String USER_AGENT_KEY = "User-Agent";
    private static final String SDK_NAME_KEY = "Sdk-Name";
    private static final String SDK_VERSION_KEY = "Sdk-Version";
    private static final String APPLICATION_ID_KEY = "Application-Id";

    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_VERSION = System.getProperty("os.version");
    private static final String JAVA_VERSION = System.getProperty("java.version");

    /*
     * The base User-Agent header format is azsdk-java-<client_lib>/<sdk_version>. Additional information such as the
     * application ID will be prepended and platform telemetry will be appended, a fully configured User-Agent header
     * format is <application_id> azsdk-java-<client_lib>/<sdk_version> <platform_info>.
     */
    private static final String DEFAULT_USER_AGENT_FORMAT = "azsdk-java-%s/%s";

    // From the design guidelines, the platform info format is:
    // <language runtime>; <os name> <os version>
    private static final String PLATFORM_INFO_FORMAT = "%s; %s %s";

    private final String defaultSdkName = this.getClass().getPackage().getName();
    private final String defaultSdkVersion = this.getClass().getPackage().getSpecificationVersion();

    private final HttpLogOptions httpLogOptions;
    private final Configuration configuration;

    private static final UserAgentPolicy defaultUserAgentPolicy = new UserAgentPolicy(null, null);

    public static UserAgentPolicy getDefaultUserAgentPolicy() {
        return defaultUserAgentPolicy;
    }

    public UserAgentPolicy(HttpLogOptions httpLogOptions, Configuration configuration) {
        if (httpLogOptions == null) {
            this.httpLogOptions = new HttpLogOptions();
        } else {
            this.httpLogOptions = httpLogOptions;
        }

        if (configuration == null) {
            this.configuration = Configuration.getGlobalConfiguration();
        } else {
            this.configuration = configuration;
        }
    }

    protected String buildUserAgent(String applicationId, String sdkName, String sdkVersion) {
            StringBuilder userAgentBuilder = new StringBuilder();

            // Only add the application ID if it is present as it is optional.
            if (applicationId != null) {
                userAgentBuilder.append(applicationId).append(" ");
            }

            // Add the required default User-Agent string.
            userAgentBuilder.append(String.format(DEFAULT_USER_AGENT_FORMAT, sdkName, sdkVersion));

            // Only add the platform telemetry if it is allowed as it is optional.
            if (!telemetryDisabled()) {
                String platformInfo = String.format(PLATFORM_INFO_FORMAT, JAVA_VERSION, OS_NAME, OS_VERSION);
                userAgentBuilder.append(" ")
                        .append("(")
                        .append(platformInfo)
                        .append(")");
            }

            return userAgentBuilder.toString();
    }

    private boolean telemetryDisabled() {
        return configuration.get(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED, false);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next){
        String userAgent = context.getHttpRequest().getHeaders().getValue(USER_AGENT_KEY);
        if (!CoreUtils.isNullOrEmpty(userAgent)) {
            return next.process();
        }

        userAgent = context.getData(USER_AGENT_KEY).orElse("").toString();
        if (!CoreUtils.isNullOrEmpty(userAgent)) {
            context.getHttpRequest().setHeader(USER_AGENT_KEY, userAgent);
            return next.process();
        }

        String sdkName = context.getData(SDK_NAME_KEY).orElse("").toString();
        if (CoreUtils.isNullOrEmpty(sdkName)) {
            sdkName = defaultSdkName;
        }

        String sdkVersion = context.getData(SDK_VERSION_KEY).orElse("").toString();
        if (CoreUtils.isNullOrEmpty(sdkVersion)) {
            sdkVersion = defaultSdkVersion;
        }

        String applicationId = context.getData(APPLICATION_ID_KEY).orElse("").toString();
        if (CoreUtils.isNullOrEmpty(applicationId)) {
            applicationId = httpLogOptions.getApplicationId();
        }

        context.getHttpRequest().setHeader(USER_AGENT_KEY, buildUserAgent(applicationId, sdkName, sdkVersion));
        return next.process();
    }
}
