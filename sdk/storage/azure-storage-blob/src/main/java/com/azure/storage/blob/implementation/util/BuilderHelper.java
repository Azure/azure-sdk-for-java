// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureSasCredentialPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.BuilderUtils;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.AuthenticationStrategy;
import com.azure.storage.common.policy.MetadataValidationPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;
import static com.azure.storage.common.implementation.BuilderUtils.determineAuthenticationStrategy;

/**
 * This class provides helper methods for common builder patterns.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class BuilderHelper {
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-storage-blob.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    /**
     * Constructs a {@link HttpPipeline} from values passed from a builder.
     *
     * @param storageSharedKeyCredential {@link StorageSharedKeyCredential} if present.
     * @param tokenCredential {@link TokenCredential} if present.
     * @param azureSasCredential {@link AzureSasCredential} if present.
     * @param sasToken SAS token if present.
     * @param endpoint The endpoint for the client.
     * @param retryOptions Storage's retry options to set in the retry policy.
     * @param coreRetryOptions Core's retry options to set in the retry policy.
     * @param logOptions Logging options to set in the logging policy.
     * @param clientOptions Client options.
     * @param httpClient HttpClient to use in the builder.
     * @param perCallPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline per call.
     * @param perRetryPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline per retry.
     * @param configuration Configuration store contain environment settings.
     * @param logger {@link ClientLogger} used to log any exception.
     * @param audience {@link BlobAudience} used to determine the audience of the blob.
     * @return A new {@link HttpPipeline} from the passed values.
     */
    public static HttpPipeline buildPipeline(StorageSharedKeyCredential storageSharedKeyCredential,
        TokenCredential tokenCredential, AzureSasCredential azureSasCredential, String sasToken, String endpoint,
        RequestRetryOptions retryOptions, RetryOptions coreRetryOptions, HttpLogOptions logOptions,
        ClientOptions clientOptions, HttpClient httpClient, List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies, Configuration configuration, BlobAudience audience,
        ClientLogger logger) {

        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(getUserAgentPolicy(configuration, logOptions, clientOptions));
        policies.add(new RequestIdPolicy());

        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(BuilderUtils.createRetryPolicy(retryOptions, coreRetryOptions, logger));

        policies.add(new AddDatePolicy());

        policies.add(new AddHeadersFromContextPolicy());

        // We need to place this policy right before the credential policy since headers may affect the string to sign
        // of the request.
        HttpHeaders headers = CoreUtils.createHttpHeadersFromClientOptions(clientOptions);
        if (headers != null) {
            policies.add(new AddHeadersPolicy(headers));
        }
        policies.add(new MetadataValidationPolicy());

        // Add authentication policies
        addAuthenticationPolicies(policies, storageSharedKeyCredential, tokenCredential, azureSasCredential, sasToken,
            endpoint, audience, logger);

        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(getResponseValidationPolicy());

        policies.add(new HttpLoggingPolicy(logOptions));

        policies.add(new ScrubEtagPolicy());

        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .clientOptions(clientOptions)
            .tracer(createTracer(clientOptions))
            .build();
    }

    /**
     * Adds authentication policies to the pipeline based on the determined authentication strategy.
     *
     * @param policies The list of policies to add authentication policies to.
     * @param storageSharedKeyCredential The storage shared key credential, if applicable.
     * @param tokenCredential The token credential, if applicable.
     * @param azureSasCredential The Azure SAS credential, if applicable.
     * @param sasToken The SAS token, if applicable.
     * @param endpoint The endpoint for the client.
     * @param audience The blob audience for token credential scope.
     * @param logger The logger for error reporting.
     */
    private static void addAuthenticationPolicies(List<HttpPipelinePolicy> policies,
        StorageSharedKeyCredential storageSharedKeyCredential, TokenCredential tokenCredential,
        AzureSasCredential azureSasCredential, String sasToken, String endpoint, BlobAudience audience,
        ClientLogger logger) {

        // Determine authentication strategy
        AuthenticationStrategy authStrategy = determineAuthenticationStrategy(storageSharedKeyCredential,
            tokenCredential, azureSasCredential, sasToken, logger);

        String scope = audience != null
            ? ((audience.toString().endsWith("/") ? audience + ".default" : audience + "/.default"))
            : Constants.STORAGE_SCOPE;

        switch (authStrategy) {
            case SHARED_KEY:
                if (storageSharedKeyCredential != null) {
                    policies.add(new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential));
                }
                break;

            case TOKEN:
                if (tokenCredential != null) {
                    httpsValidation(tokenCredential, "bearer token", endpoint, logger);
                    policies.add(new StorageBearerTokenChallengeAuthorizationPolicy(tokenCredential, scope));
                }
                break;

            case SAS:
                // Handle both SAS token and Azure SAS credential the same way
                if (azureSasCredential != null) {
                    policies.add(new AzureSasCredentialPolicy(azureSasCredential, false));
                } else if (sasToken != null) {
                    policies.add(new AzureSasCredentialPolicy(new AzureSasCredential(sasToken), false));
                }
                break;

            case TOKEN_WITH_SAS:
                // Add both token credential and SAS policies
                if (tokenCredential != null) {
                    httpsValidation(tokenCredential, "bearer token", endpoint, logger);
                    policies.add(new StorageBearerTokenChallengeAuthorizationPolicy(tokenCredential, scope));
                }
                if (azureSasCredential != null) {
                    policies.add(new AzureSasCredentialPolicy(azureSasCredential, false));
                } else if (sasToken != null) {
                    policies.add(new AzureSasCredentialPolicy(new AzureSasCredential(sasToken), false));
                }
                break;

            case ANONYMOUS:
            default:
                // No authentication policies added
                break;
        }
    }

    /**
     * Gets the default http log option for Storage Blob.
     *
     * @return the default http log options.
     */
    public static HttpLogOptions getDefaultHttpLogOptions() {
        HttpLogOptions defaultOptions = new HttpLogOptions();
        BlobHeadersAndQueryParameters.getBlobHeaders()
            .forEach(stringHeaderName -> defaultOptions
                .addAllowedHttpHeaderName(HttpHeaderName.fromString(stringHeaderName)));
        BlobHeadersAndQueryParameters.getBlobQueryParameters().forEach(defaultOptions::addAllowedQueryParamName);
        return defaultOptions;
    }

    /**
     * Gets the endpoint for the blob service based on the parsed URL.
     *
     * @param parts The {@link BlobUrlParts} from the parse URL.
     * @return The endpoint for the blob service.
     */
    public static String getEndpoint(BlobUrlParts parts) throws MalformedURLException {
        if (ModelHelper.determineAuthorityIsIpStyle(parts.getHost())) {
            return parts.getScheme() + "://" + parts.getHost() + "/" + parts.getAccountName();
        } else {
            return parts.getScheme() + "://" + parts.getHost();
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
            throw logger
                .logExceptionAsError(new IllegalArgumentException("Using a(n) " + objectName + " requires https"));
        }
    }

    /*
     * Creates a {@link UserAgentPolicy} using the default blob module name and version.
     *
     * @param configuration Configuration store used to determine whether telemetry information should be included.
     * @param logOptions Logging options to set in the logging policy.
     * @param clientOptions Client options.
     * @return The default {@link UserAgentPolicy} for the module.
     */
    private static UserAgentPolicy getUserAgentPolicy(Configuration configuration, HttpLogOptions logOptions,
        ClientOptions clientOptions) {
        configuration = (configuration == null) ? Configuration.NONE : configuration;
        String applicationId = CoreUtils.getApplicationId(clientOptions, logOptions);
        return new UserAgentPolicy(applicationId, CLIENT_NAME, CLIENT_VERSION, configuration);
    }

    /*
     * Creates a {@link ResponseValidationPolicyBuilder.ResponseValidationPolicy} used to validate response data from
     * the service.
     *
     * @return The {@link ResponseValidationPolicyBuilder.ResponseValidationPolicy} for the module.
     */
    private static HttpPipelinePolicy getResponseValidationPolicy() {
        return new ResponseValidationPolicyBuilder().addOptionalEcho(HttpHeaderName.X_MS_CLIENT_REQUEST_ID)
            .addOptionalEcho(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256_HEADER_NAME)
            .build();
    }

    public static Tracer createTracer(ClientOptions clientOptions) {
        TracingOptions tracingOptions = clientOptions == null ? null : clientOptions.getTracingOptions();
        return TracerProvider.getDefaultProvider()
            .createTracer(CLIENT_NAME, CLIENT_VERSION, STORAGE_TRACING_NAMESPACE_VALUE, tracingOptions);
    }
}
