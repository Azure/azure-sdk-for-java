// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation;

import com.azure.core.experimental.http.policy.ArmChallengeAuthenticationPolicy;
import com.azure.core.http.HttpRequest;

import java.util.Locale;
import java.util.Map;

/**
 * Defines a few utilities. This is a temporary impl class to support {@link ArmChallengeAuthenticationPolicy} until it
 * moves to azure-resource-manager package.
 */
public final class ARMScopeHelper {
    private ARMScopeHelper() {
    }

    /**
     * Generates default scope for oauth2 from the specific request
     * @param request a http request
     * @param environment the azure environment with current request
     * @return the default scope
     */
    public static String getDefaultScopeFromRequest(HttpRequest request, AzureEnvironment environment) {
        return getDefaultScopeFromUrl(request.getUrl().toString().toLowerCase(Locale.US), environment);
    }

    /**
     * Generates default scope for oauth2 from the specific request
     * @param url the url in lower case of a http request
     * @param environment the azure environment with current request
     * @return the default scope
     */
    static String getDefaultScopeFromUrl(String url, AzureEnvironment environment) {
        String resource = environment.getManagementEndpoint();
        for (Map.Entry<String, String> endpoint : environment.getEndpoints().entrySet()) {
            if (url.contains(endpoint.getValue())) {
                if (endpoint.getKey().equals(AzureEnvironment.Endpoint.KEYVAULT.identifier())) {
                    resource = String.format("https://%s/", endpoint.getValue().replaceAll("^\\.*", ""));
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.GRAPH.identifier())) {
                    resource = environment.getGraphEndpoint();
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.MICROSOFT_GRAPH.identifier())) {
                    resource = environment.getMicrosoftGraphEndpoint();
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.LOG_ANALYTICS.identifier())) {
                    resource = environment.getLogAnalyticsEndpoint();
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.APPLICATION_INSIGHTS.identifier())) {
                    resource = environment.getApplicationInsightsEndpoint();
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.DATA_LAKE_STORE.identifier())
                    || endpoint.getKey().equals(AzureEnvironment.Endpoint.DATA_LAKE_ANALYTICS.identifier())) {
                    resource = environment.getDataLakeEndpointResourceId();
                    break;
                }
            }
        }
        return removeTrailingSlash(resource) + "/.default";
    }

    /**
     * Removes the trailing slash of the string.
     * @param s the string
     * @return the string without trailing slash
     */
    private static String removeTrailingSlash(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }
}

