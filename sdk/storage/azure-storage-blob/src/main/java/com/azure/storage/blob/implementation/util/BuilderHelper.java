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
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.SessionMode;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.BuilderUtils;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.credentials.CredentialValidator;
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
     * Constructs a {@link HttpPipeline} from values passed from a builder, with optional session-based
     * authentication support.
     * <p>
     * When {@code sessionOptions} is non-null and the resolved session mode is not {@link SessionMode#NONE},
     * and a {@code tokenCredential} is present, a {@link SessionTokenCredentialPolicy} is added before the
     * bearer token policy. The session policy uses a separate bearer-only pipeline for CreateSession calls.
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
     * @param sessionOptions {@link SessionOptions} containing the session mode.
     *                       Pass {@code null} to disable session support.
     * @param containerName The container name for session scoping. Required when session is active.
     * @param serviceVersion The service version for session creation. Required when session is active.
     * @return A new {@link HttpPipeline} from the passed values.
     */
    public static HttpPipeline buildPipeline(StorageSharedKeyCredential storageSharedKeyCredential,
        TokenCredential tokenCredential, AzureSasCredential azureSasCredential, String sasToken, String endpoint,
        RequestRetryOptions retryOptions, RetryOptions coreRetryOptions, HttpLogOptions logOptions,
        ClientOptions clientOptions, HttpClient httpClient, List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies, Configuration configuration, BlobAudience audience,
        ClientLogger logger, SessionOptions sessionOptions, String accountName, BlobServiceVersion serviceVersion) {

        CredentialValidator.validateCredentialsNotAmbiguous(storageSharedKeyCredential, tokenCredential,
            azureSasCredential, sasToken, logger);

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

        if (storageSharedKeyCredential != null) {
            policies.add(new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential));
        }

        addSessionPolicyIfEnabled(policies, sessionOptions, tokenCredential, endpoint, clientOptions, httpClient,
            audience, logger, accountName, serviceVersion);

        if (tokenCredential != null) {
            httpsValidation(tokenCredential, "bearer token", endpoint, logger);
            String scope = audience != null
                ? ((audience.toString().endsWith("/") ? audience + ".default" : audience + "/.default"))
                : Constants.STORAGE_SCOPE;
            policies.add(new StorageBearerTokenChallengeAuthorizationPolicy(tokenCredential, scope));
        }

        if (azureSasCredential != null) {
            policies.add(new AzureSasCredentialPolicy(azureSasCredential, false));
        } else if (sasToken != null) {
            policies.add(new AzureSasCredentialPolicy(new AzureSasCredential(sasToken), false));
        }

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

    private static void addSessionPolicyIfEnabled(List<HttpPipelinePolicy> policies, SessionOptions sessionOptions,
        TokenCredential tokenCredential, String endpoint, ClientOptions clientOptions, HttpClient httpClient,
        BlobAudience audience, ClientLogger logger, String accountName, BlobServiceVersion serviceVersion) {

        if (sessionOptions == null || tokenCredential == null) {
            return;
        }

        SessionMode effectiveMode = resolveSessionMode(sessionOptions.getSessionMode(), tokenCredential);
        if (effectiveMode == SessionMode.NONE) {
            return;
        }

        String containerName = sessionOptions.getContainerName();
        validateSessionOptions(containerName, serviceVersion, effectiveMode, logger);

        List<HttpPipelinePolicy> bearerPolicies = new ArrayList<>(policies);
        httpsValidation(tokenCredential, "bearer token", endpoint, logger);
        String scope = audience != null
            ? ((audience.toString().endsWith("/") ? audience + ".default" : audience + "/.default"))
            : Constants.STORAGE_SCOPE;
        bearerPolicies.add(new StorageBearerTokenChallengeAuthorizationPolicy(tokenCredential, scope));

        HttpPipeline bearerPipeline
            = new HttpPipelineBuilder().policies(bearerPolicies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .clientOptions(clientOptions)
                .tracer(createTracer(clientOptions))
                .build();

        SessionTokenCredentialPolicy sessionPolicy
            = createSessionPolicy(bearerPipeline, endpoint, accountName, containerName, serviceVersion, effectiveMode);

        policies.add(sessionPolicy);
    }

    private static void validateSessionOptions(String containerName, BlobServiceVersion serviceVersion,
        SessionMode effectiveMode, ClientLogger logger) {
        if (CoreUtils.isNullOrEmpty(containerName)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("containerName must be set when using SessionMode." + effectiveMode));
        }
        if (serviceVersion == null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("serviceVersion must be set when using SessionMode." + effectiveMode));
        }
    }

    private static SessionTokenCredentialPolicy createSessionPolicy(HttpPipeline bearerPipeline, String endpoint,
        String accountName, String containerName, BlobServiceVersion serviceVersion, SessionMode effectiveMode) {
        BlobSessionClient sessionClient
            = new BlobSessionClient(bearerPipeline, endpoint, serviceVersion, accountName, containerName);
        return new SessionTokenCredentialPolicy(new StorageSessionCredentialCache(sessionClient), effectiveMode);
    }

    private static SessionMode resolveSessionMode(SessionMode sessionMode, TokenCredential tokenCredential) {
        return resolveSessionMode(sessionMode, tokenCredential != null);
    }

    /**
     * Wraps an existing pipeline with a per-container {@link SessionTokenCredentialPolicy}.
     * Used by {@link com.azure.storage.blob.BlobServiceClient#getBlobContainerClient(String)} to give each
     * container its own session credential cache while sharing all other policies.
     *
     * @param basePipeline The service-level pipeline (used as-is for CreateSession calls).
     * @param sessionOptions The session options containing mode and container name.
     * @param endpoint The storage account endpoint.
     * @param serviceVersion The blob service version.
     * @param accountName The storage account name.
     * @return A new pipeline with session support, or {@code basePipeline} unchanged if sessions are not applicable.
     */
    public static HttpPipeline wrapWithSessionPolicy(HttpPipeline basePipeline, SessionOptions sessionOptions,
        String endpoint, BlobServiceVersion serviceVersion, String accountName) {

        SessionMode sessionMode = sessionOptions != null ? sessionOptions.getSessionMode() : null;
        String containerName = sessionOptions != null ? sessionOptions.getContainerName() : null;

        // Detect whether the pipeline has bearer auth by scanning for the policy.
        boolean hasBearerAuth = false;
        int bearerIndex = -1;
        for (int i = 0; i < basePipeline.getPolicyCount(); i++) {
            if (basePipeline.getPolicy(i) instanceof StorageBearerTokenChallengeAuthorizationPolicy) {
                hasBearerAuth = true;
                bearerIndex = i;
                break;
            }
        }

        SessionMode effectiveMode = resolveSessionMode(sessionMode, hasBearerAuth);
        if (effectiveMode == SessionMode.NONE || !hasBearerAuth) {
            return basePipeline;
        }

        // The base pipeline (with bearer) serves as the bearer-only pipeline for CreateSession calls.
        BlobSessionClient sessionClient
            = new BlobSessionClient(basePipeline, endpoint, serviceVersion, accountName, containerName);
        SessionTokenCredentialPolicy sessionPolicy
            = new SessionTokenCredentialPolicy(new StorageSessionCredentialCache(sessionClient), effectiveMode);

        // Build a new pipeline with session policy inserted before the bearer policy.
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        for (int i = 0; i < basePipeline.getPolicyCount(); i++) {
            if (i == bearerIndex) {
                policies.add(sessionPolicy);
            }
            policies.add(basePipeline.getPolicy(i));
        }

        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(basePipeline.getHttpClient())
            .build();
    }

    private static SessionMode resolveSessionMode(SessionMode sessionMode, boolean hasBearerAuth) {
        if (sessionMode != null) {
            return sessionMode;
        }
        return hasBearerAuth ? SessionMode.AUTO : SessionMode.NONE;
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

    /**
     * Logs information about credential changes in builders.
     *
     * @param logger The logger to use.
     * @param newCredentialType The credential type being set.
     */
    public static void logCredentialChange(ClientLogger logger, String newCredentialType) {
        logger.info("Credential set to '{}' when it was previously configured.", newCredentialType);
    }
}
