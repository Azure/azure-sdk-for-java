// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.pipline.policies;

import org.springframework.util.StringUtils;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.spring.cloud.config.HostType;
import com.azure.spring.cloud.config.RequestTracingConstants;
import com.azure.spring.cloud.config.RequestType;

import reactor.core.publisher.Mono;

/**
 * HttpPipelinePolicy for connecting to Azure App Configuration.
 */
public final class BaseAppConfigurationPolicy implements HttpPipelinePolicy {

    private static final String PACKAGE_NAME = BaseAppConfigurationPolicy.class.getPackage().getImplementationTitle();

    public static final String USER_AGENT = String.format("%s/%s", StringUtils.replace(PACKAGE_NAME, " ", ""),
        BaseAppConfigurationPolicy.class.getPackage().getImplementationVersion());
    
    public static final String USER_AGENT_TYPE = "User-Agent";

    static Boolean watchRequests = false;

    static Boolean isDev = false;

    static Boolean isKeyVaultConfigured = false;

    /**
     * 
     * Checks if Azure App Configuration Tracing is disabled, and if not gets tracing information.
     *
     * @param request The http request that will be traced, used to check operation being run.
     * @return String of the value for the correlation-context header.
     */
    private static String getTracingInfo(HttpRequest request) {
        String track = System.getenv(RequestTracingConstants.REQUEST_TRACING_DISABLED_ENVIRONMENT_VARIABLE.toString());
        if (track != null && track.equalsIgnoreCase("false")) {
            return "";
        }
        String requestTypeValue = RequestType.WATCH.toString();
        requestTypeValue = watchRequests ? RequestType.WATCH.toString() : RequestType.STARTUP.toString();
        
        if (requestTypeValue.equals(RequestType.WATCH.toString())) {
            watchRequests = true;
        }
        String tracingInfo = RequestTracingConstants.REQUEST_TYPE_KEY.toString() + "=" + requestTypeValue;
        String hostType = getHostType();

        if (!hostType.isEmpty()) {
            tracingInfo += "," + RequestTracingConstants.HOST_TYPE_KEY + "=" + getHostType();
        }

        return tracingInfo;

    }

    private static String getEnvInfo() {
        String envInfo = "";
        
        envInfo = buildEnvTracingInfo(envInfo, isDev, "Dev");
        envInfo = buildEnvTracingInfo(envInfo, isKeyVaultConfigured, "ConfigureKeyVault");

        return envInfo;
    }

    private static String buildEnvTracingInfo(String envInfo, Boolean check, String checkString) {
        if (check) {
            if (envInfo.length() > 0) {
                envInfo += ",";
            }
            envInfo += checkString;
        }
        return envInfo;
    }

    /**
     * Gets the current host machines type; Azure Function, Azure Web App, Kubernetes, or Empty.
     *
     * @return String of Host Type
     */
    private static String getHostType() {
        HostType hostType = HostType.UNIDENTIFIED;

        if (System.getenv(RequestTracingConstants.AZURE_FUNCTIONS_ENVIRONMENT_VARIABLE.toString()) != null) {
            hostType = HostType.AZURE_FUNCTION;
        } else if (System.getenv(RequestTracingConstants.AZURE_WEB_APP_ENVIRONMENT_VARIABLE.toString()) != null) {
            hostType = HostType.AZURE_WEB_APP;
        } else if (System.getenv(RequestTracingConstants.KUBERNETES_ENVIRONMENT_VARIABLE.toString()) != null) {
            hostType = HostType.KUBERNETES;
        }

        return hostType.toString();

    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String sdkUserAgent = context.getHttpRequest().getHeaders().get(USER_AGENT_TYPE).getValue();
        context.getHttpRequest().getHeaders().set(USER_AGENT_TYPE, USER_AGENT + " " + sdkUserAgent);
        context.getHttpRequest().getHeaders().set(RequestTracingConstants.CORRELATION_CONTEXT_HEADER.toString(),
            getTracingInfo(context.getHttpRequest()));

        if (isDev || isKeyVaultConfigured) {
            context.getHttpRequest().getHeaders().set("Env", getEnvInfo());
        }
        return next.process();
    }

    /**
     * @param isDev the isDev to set
     */
    public static void setIsDev(Boolean isDev) {
        BaseAppConfigurationPolicy.isDev = isDev;
    }

    /**
     * @param isKeyVaultConfigured the isKeyVaultConfigured to set
     */
    public static void setIsKeyVaultConfigured(Boolean isKeyVaultConfigured) {
        BaseAppConfigurationPolicy.isKeyVaultConfigured = isKeyVaultConfigured;
    }

    /**
     * @param watchRequests the watchRequests to set
     */
    public static void setWatchRequests(Boolean watchRequests) {
        BaseAppConfigurationPolicy.watchRequests = watchRequests;
    }

}
