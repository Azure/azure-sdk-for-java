// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class BuilderHelper {
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-data-tables.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    static HttpPipeline buildPipeline(TablesSharedKeyCredential tablesSharedKeyCredential,
        TokenCredential tokenCredential, SasTokenCredential sasTokenCredential,
        String endpoint, RequestRetryOptions retryOptions, HttpLogOptions logOptions,
        HttpClient httpClient, List<HttpPipelinePolicy> additionalPolicies,
        Configuration configuration, ClientLogger logger) {

        //1
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(getUserAgentPolicy(configuration));
        policies.add(new RequestIdPolicy());

        // Add Accept header so we don't get back XML.
        // Can be removed when this is fixed. https://github.com/Azure/autorest.modelerfour/issues/324
        policies.add(new AddHeadersPolicy(new HttpHeaders().put("Accept", "application/json")));

        //2
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RequestRetryPolicy(retryOptions));

        //3
        policies.add(new AddDatePolicy());
        HttpPipelinePolicy credentialPolicy;
        if (tablesSharedKeyCredential != null) {
            credentialPolicy = new TablesSharedKeyCredentialPolicy(tablesSharedKeyCredential);
        } else if (tokenCredential != null) {
            httpsValidation(tokenCredential, "bearer token", endpoint, logger);
            credentialPolicy = new BearerTokenAuthenticationPolicy(tokenCredential,
                String.format("%s/.default", getPrimaryEndpointForTokenAuth(endpoint)));
        } else if (sasTokenCredential != null) {
            credentialPolicy = new SasTokenCredentialPolicy(sasTokenCredential);
        } else {
            credentialPolicy = null;
        }

        if (credentialPolicy != null) {
            policies.add(credentialPolicy);
        }

        //4
        policies.addAll(additionalPolicies);
        HttpPolicyProviders.addAfterRetryPolicies(policies); //should this be between 3/4?

        //5
        policies.add(getResponseValidationPolicy());

        //6
        policies.add(new HttpLoggingPolicy(logOptions));

        //hm what is this and why here not 5?
        policies.add(new ScrubEtagPolicy());

        //where is #7, transport policy

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /*
     * Creates a {@link UserAgentPolicy} using the default blob module name and version.
     *
     * @param configuration Configuration store used to determine whether telemetry information should be included.
     * @return The default {@link UserAgentPolicy} for the module.
     */
    private static UserAgentPolicy getUserAgentPolicy(Configuration configuration) {
        configuration = (configuration == null) ? Configuration.NONE : configuration;

        String clientName = PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");
        return new UserAgentPolicy(getDefaultHttpLogOptions().getApplicationId(), clientName, clientVersion,
            configuration);
    }

    /**
     * @param endpoint The endpoint passed by the customer.
     * @return The primary endpoint for the account. It may be the same endpoint passed if it is already a primary or it
     * may have had "-secondary" stripped from the end of the account name.
     */
    private static String getPrimaryEndpointForTokenAuth(String endpoint) {
        String[] parts = endpoint.split("\\.");
        parts[0] = parts[0].endsWith("-secondary") ? parts[0].substring(0, parts[0].length() - "-secondary".length())
            : parts[0];
        return String.join(".", parts);
    }

    /**
     * Gets the default http log option for Storage Blob.
     *
     * @return the default http log options.
     */
    private static HttpLogOptions getDefaultHttpLogOptions() {
        HttpLogOptions defaultOptions = new HttpLogOptions();
        //BlobHeadersAndQueryParameters.getBlobHeaders().forEach(defaultOptions::addAllowedHeaderName);
        //BlobHeadersAndQueryParameters.getBlobQueryParameters().forEach(defaultOptions::addAllowedQueryParamName);
        return defaultOptions;
    }

    /*
     * Creates a {@link ResponseValidationPolicyBuilder.ResponseValidationPolicy} used to validate response data from
     * the service.
     *
     * @return The {@link ResponseValidationPolicyBuilder.ResponseValidationPolicy} for the module.
     */
    private static HttpPipelinePolicy getResponseValidationPolicy() {
        return new ResponseValidationPolicyBuilder()
            .addOptionalEcho(Constants.HeaderConstants.CLIENT_REQUEST_ID)
            .addOptionalEcho(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256)
            .build();
    }

    /**
     * Validates that the client is properly configured to use https.
     *
     * @param objectToCheck The object to check for.
     * @param objectName The name of the object.
     * @param endpoint The endpoint for the client.
     * @param logger the logger
     */
    private static void httpsValidation(Object objectToCheck, String objectName, String endpoint, ClientLogger logger) {
//        if (objectToCheck != null && !BlobUrlParts.parse(endpoint).getScheme().equals(Constants.HTTPS)) {
//            throw logger.logExceptionAsError(new IllegalArgumentException(
//                "Using a(n) " + objectName + " requires https"));
//        }
    }

}
