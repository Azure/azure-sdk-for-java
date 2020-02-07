// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
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
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides helper methods for common builder patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public final class BuilderHelper {
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-storage-blob.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    /**
     * Constructs a {@link HttpPipeline} from values passed from a builder.
     *
     * @param storageSharedKeyCredential {@link StorageSharedKeyCredential} if present.
     * @param tokenCredential {@link TokenCredential} if present.
     * @param sasTokenCredential {@link SasTokenCredential} if present.
     * @param endpoint The endpoint for the client.
     * @param retryOptions Retry options to set in the retry policy.
     * @param logOptions Logging options to set in the logging policy.
     * @param httpClient HttpClient to use in the builder.
     * @param additionalPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline.
     * @param configuration Configuration store contain environment settings.
     * @param logger {@link ClientLogger} used to log any exception.
     * @return A new {@link HttpPipeline} from the passed values.
     */
    public static HttpPipeline buildPipeline(StorageSharedKeyCredential storageSharedKeyCredential,
        TokenCredential tokenCredential, SasTokenCredential sasTokenCredential, String endpoint,
        RequestRetryOptions retryOptions, HttpLogOptions logOptions, HttpClient httpClient,
        List<HttpPipelinePolicy> additionalPolicies, Configuration configuration, ClientLogger logger) {
        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(getUserAgentPolicy(configuration));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        HttpPipelinePolicy credentialPolicy;
        if (storageSharedKeyCredential != null) {
            credentialPolicy =  new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential);
        } else if (tokenCredential != null) {
            httpsValidation(tokenCredential, "bearer token", endpoint, logger);
            credentialPolicy =  new BearerTokenAuthenticationPolicy(tokenCredential,
                String.format("%s/.default", endpoint));
        } else if (sasTokenCredential != null) {
            credentialPolicy =  new SasTokenCredentialPolicy(sasTokenCredential);
        } else {
            credentialPolicy =  null;
        }

        if (credentialPolicy != null) {
            policies.add(credentialPolicy);
        }

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RequestRetryPolicy(retryOptions));

        policies.addAll(additionalPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(getResponseValidationPolicy());

        policies.add(new HttpLoggingPolicy(logOptions));

        policies.add(new ScrubEtagPolicy());

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /**
     * Gets the default http log option for Storage Blob.
     *
     * @return the default http log options.
     */
    public static HttpLogOptions getDefaultHttpLogOptions() {
        HttpLogOptions defaultOptions = new HttpLogOptions();
        BlobHeadersAndQueryParameters.getBlobHeaders().forEach(defaultOptions::addAllowedHeaderName);
        BlobHeadersAndQueryParameters.getBlobQueryParameters().forEach(defaultOptions::addAllowedQueryParamName);
        return defaultOptions;
    }

    /**
     * Gets the endpoint for the blob service based on the parsed URL.
     *
     * @param parts The {@link BlobUrlParts} from the parse URL.
     * @return The endpoint for the blob service.
     */
    public static String getEndpoint(BlobUrlParts parts) {
        if (ModelHelper.IP_V4_URL_PATTERN.matcher(parts.getHost()).find()) {
            return String.format("%s://%s/%s", parts.getScheme(), parts.getHost(), parts.getAccountName());
        } else {
            return String.format("%s://%s", parts.getScheme(), parts.getHost());
        }
    }

    /**
     * Validates that the client is properly configured to use https.
     *
     * @param objectToCheck The object to check for.
     * @param objectName The name of the object.
     * @param endpoint The endpoint for the client.
     */
    public static void httpsValidation(Object objectToCheck, String objectName, String endpoint, ClientLogger logger) {
        if (objectToCheck != null && !BlobUrlParts.parse(endpoint).getScheme().equals(Constants.HTTPS)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Using a(n) " + objectName + " requires https"));
        }
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
