// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management;

import com.azure.core.http.HttpRequest;
import com.azure.core.management.AzureEnvironment;

import java.util.Map;

/**
 * Defines a few utilities.
 */
public class Utils {

    private Utils() {
    }

    /**
     * Generates default scope for oauth2 from the specific request
     * @param request a http request
     * @param environment the azure environment with current request
     * @return the default scope
     */
    public static String getDefaultScopeFromRequest(HttpRequest request, AzureEnvironment environment) {
        return getDefaultScopeFromUrl(request.getUrl().toString().toLowerCase(), environment);
    }

    /**
     * Generates default scope for oauth2 from the specific request
     * @param url the url in lower case of a http request
     * @param environment the azure environment with current request
     * @return the default scope
     */
    public static String getDefaultScopeFromUrl(String url, AzureEnvironment environment) {
        String resource = environment.getManagementEndpoint();
        for (Map.Entry<String, String> endpoint : environment.endpoints().entrySet()) {
            if (url.contains(endpoint.getValue())) {
                if (endpoint.getKey().equals(AzureEnvironment.Endpoint.KEYVAULT.identifier())) {
                    resource = String.format("https://%s/", endpoint.getValue().replaceAll("^\\.*", ""));
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.GRAPH.identifier())) {
                    resource = environment.getGraphEndpoint();
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
    public static String removeTrailingSlash(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * Gets the subscription ID from rest client.
     * @param restClient the rest client
     * @return the subscription ID
     */
    public static String getSubscriptionIdFromRestClient(RestClient restClient) {
        if (restClient.getCredential() instanceof AzureTokenCredential) {
            return ((AzureTokenCredential) restClient.getCredential()).getDefaultSubscriptionId();
        }
        return null;
    }

    /**
     * Gets the tenant ID from rest client.
     * @param restClient the rest client
     * @return the tenant ID
     */
    public static String getTenantIdFromRestClient(RestClient restClient) {
        if (restClient.getCredential() instanceof AzureTokenCredential) {
            return ((AzureTokenCredential) restClient.getCredential()).getDomain();
        }
        return null;
    }
}
