// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureSasCredentialPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.NullHttpClient;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class BuilderHelper {
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-data-tables.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    static HttpPipeline buildPipeline(
        TablesSharedKeyCredential tablesSharedKeyCredential,
        TokenCredential tokenCredential, AzureSasCredential azureSasCredential, String sasToken,
        String endpoint, RequestRetryOptions retryOptions, HttpLogOptions logOptions, ClientOptions clientOptions,
        HttpClient httpClient, List<HttpPipelinePolicy> perCallAdditionalPolicies,
        List<HttpPipelinePolicy> perRetryAdditionalPolicies, Configuration configuration, ClientLogger logger) {

        configuration = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;
        clientOptions = (clientOptions == null) ? new ClientOptions() : clientOptions;
        logOptions = (logOptions == null) ? new HttpLogOptions() : logOptions;

        validateSingleCredentialIsPresent(
            tablesSharedKeyCredential, tokenCredential, azureSasCredential, sasToken, logger);

        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(getUserAgentPolicy(configuration, clientOptions, logOptions));
        policies.add(new RequestIdPolicy());

        // Add Accept header so we don't get back XML.
        // Can be removed when this is fixed. https://github.com/Azure/autorest.modelerfour/issues/324
        policies.add(new AddHeadersPolicy(new HttpHeaders().put("Accept", "application/json")));

        // Add per call additional policies.
        policies.addAll(perCallAdditionalPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        // Add retry policy.
        policies.add(new RequestRetryPolicy(retryOptions));

        policies.add(new AddDatePolicy());
        HttpPipelinePolicy credentialPolicy;
        if (tablesSharedKeyCredential != null) {
            credentialPolicy = new TablesSharedKeyCredentialPolicy(tablesSharedKeyCredential);
        } else if (tokenCredential != null) {
            UrlBuilder endpointParts = UrlBuilder.parse(endpoint);
            if (!endpointParts.getScheme().equals(Constants.HTTPS)) {
                throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                    "HTTPS is required when using a %s credential.", tokenCredential.getClass().getName())));
            }
            credentialPolicy = new BearerTokenAuthenticationPolicy(tokenCredential, getBearerTokenScope(endpointParts));
        } else if (azureSasCredential != null) {
            credentialPolicy = new AzureSasCredentialPolicy(azureSasCredential, false);
        } else if (sasToken != null) {
            credentialPolicy = new AzureSasCredentialPolicy(new AzureSasCredential(sasToken), false);
        } else {
            credentialPolicy = null;
        }

        if (credentialPolicy != null) {
            policies.add(credentialPolicy);
        }

        // Add per retry additional policies.
        policies.addAll(perRetryAdditionalPolicies);
        HttpPolicyProviders.addAfterRetryPolicies(policies); //should this be between 3/4?

        policies.add(getResponseValidationPolicy());
        policies.add(new HttpLoggingPolicy(logOptions));

        //hm what is this and why here not 5?
        // vcolin7: Probably to log the actual ETag from the service before scrubbing it.
        policies.add(new ScrubEtagPolicy());

        //where is #7, transport policy

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    static HttpPipeline buildNullClientPipeline() {
        HttpPipelinePolicy[] policies = {
            new AddHeadersPolicy(new HttpHeaders().put("Accept", "application/json;odata=minimalmetadata"))
        };

        return new HttpPipelineBuilder()
            .policies(policies)
            .httpClient(new NullHttpClient())
            .build();
    }

    private static void validateSingleCredentialIsPresent(
        TablesSharedKeyCredential storageSharedKeyCredential,
        TokenCredential tokenCredential, AzureSasCredential azureSasCredential, String sasToken, ClientLogger logger) {
        List<Object> usedCredentials = Stream.of(
            storageSharedKeyCredential, tokenCredential, azureSasCredential, sasToken)
            .filter(Objects::nonNull).collect(Collectors.toList());
        if (usedCredentials.size() > 1) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Only one credential should be used. Credentials present: "
                    + usedCredentials.stream().map(c -> c instanceof String ? "sasToken" : c.getClass().getName())
                    .collect(Collectors.joining(","))
            ));
        }
    }

    /*
     * Creates a {@link UserAgentPolicy} using the default blob module name and version.
     *
     * @param configuration Configuration store used to determine whether telemetry information should be included.
     * @param clientOptions ClientOptions such as application ID and custom headers to set on a request.
     * @param logOptions The logging configuration to use when sending and receiving requests to and from the service.
     * @return The default {@link UserAgentPolicy} for the module.
     */
    private static UserAgentPolicy getUserAgentPolicy(Configuration configuration, ClientOptions clientOptions,
        HttpLogOptions logOptions) {

        String clientName = PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");
        String applicationId = null;

        if (!CoreUtils.isNullOrEmpty(clientOptions.getApplicationId())) {
            applicationId = clientOptions.getApplicationId();
        } else if (!CoreUtils.isNullOrEmpty(logOptions.getApplicationId())) {
            applicationId = logOptions.getApplicationId();
        }

        return new UserAgentPolicy(applicationId, clientName, clientVersion, configuration);
    }

    /**
     * @param endpoint The endpoint passed by the customer.
     * @return The bearer token scope for the primary endpoint for the account. It may be the same endpoint passed if it
     * is already a primary or it may have had "-secondary" stripped from the end of the account name.
     */
    private static String getBearerTokenScope(UrlBuilder endpoint) {
        String[] hostParts = endpoint.getHost().split("\\.");
        if (hostParts[0].endsWith("-secondary")) {
            hostParts[0] = hostParts[0].substring(0, hostParts[0].length() - 10); // Strip off the '-secondary' suffix
            endpoint.setHost(String.join(".", hostParts));
        }
        return String.format("%s/.default", endpoint.toString());
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
}
