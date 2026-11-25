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
import com.azure.core.http.policy.HttpLogDetailLevel;
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
import com.azure.storage.blob.implementation.accesshelpers.SessionContextAccessor;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.SessionContext;
import com.azure.storage.blob.SessionContext.SessionMode;
import java.time.Clock;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.BuilderUtils;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.credentials.CredentialValidator;
import com.azure.storage.common.policy.DataLocalityPolicy;
import com.azure.storage.common.policy.ExpectContinueOptions;
import com.azure.storage.common.policy.MetadataValidationPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import com.azure.storage.common.policy.StorageContentValidationDecodingPolicy;
import com.azure.storage.common.policy.StorageContentValidationEncodingPolicy;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
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
     * @param expectContinueOptions {@link ExpectContinueOptions} used to determine when the HTTP header
     * {@code Expect: 100-continue} is applied to requests.
     * @param sessionContext {@link SessionContext} containing the session mode and optional acquisition configuration.
     * @param serviceVersion The service version for session creation; defaults to the latest version when null.
     * @return A new {@link HttpPipeline} from the passed values.
     */
    public static HttpPipeline buildPipeline(StorageSharedKeyCredential storageSharedKeyCredential,
        TokenCredential tokenCredential, AzureSasCredential azureSasCredential, String sasToken, String endpoint,
        RequestRetryOptions retryOptions, RetryOptions coreRetryOptions, HttpLogOptions logOptions,
        ClientOptions clientOptions, HttpClient httpClient, List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies, Configuration configuration, BlobAudience audience,
        ExpectContinueOptions expectContinueOptions, ClientLogger logger, SessionContext sessionContext,
        BlobServiceVersion serviceVersion) {

        CredentialValidator.validateCredentialsNotAmbiguous(storageSharedKeyCredential, tokenCredential,
            azureSasCredential, sasToken, logger);

        SessionMode sessionMode = SessionMode.DISABLED;
        if (tokenCredential != null && sessionContext != null) {
            sessionMode = ModelHelper.resolveSessionMode(sessionContext.getSessionMode());
        }
        boolean sessionsEnabled = sessionMode == SessionMode.ENABLED;

        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(getUserAgentPolicy(configuration, logOptions, clientOptions));
        policies.add(new RequestIdPolicy());

        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new DataLocalityPolicy());
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

        policies.add(new StorageContentValidationEncodingPolicy());
        policies.add(new StorageContentValidationDecodingPolicy());

        // Must be after the retry policy so it is evaluated on every attempt, and after content validation so the
        // Content-Length it reads is the encoded length actually sent. Before the credential policies.
        BuilderUtils.addExpectContinuePolicy(policies, expectContinueOptions, configuration);

        if (storageSharedKeyCredential != null) {
            policies.add(new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential));
        }

        // When sessions are enabled, a single SessionAuthenticationPolicy is added as the auth policy. It wraps the
        // bearer token policy and delegates to it for requests that aren't session-eligible. CreateSession requests
        // are sent through this same pipeline by the session acquirer, which is bound once the pipeline is built.
        SessionAcquirer sessionAcquirer = null;
        if (tokenCredential != null) {
            httpsValidation(tokenCredential, "bearer token", endpoint, logger);
            String scope = audience != null
                ? ((audience.toString().endsWith("/") ? audience + ".default" : audience + "/.default"))
                : Constants.STORAGE_SCOPE;
            StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy
                = new StorageBearerTokenChallengeAuthorizationPolicy(tokenCredential, scope);

            if (!sessionsEnabled) {
                policies.add(bearerPolicy);
            } else {
                sessionAcquirer = createSessionAcquirer(sessionContext, endpoint, serviceVersion, scope, logger);
                // The provider owns only the session cache, so every client sharing these options shares sessions.
                SessionProvider sessionProvider = SessionContextAccessor.getOrCreateSessionProvider(sessionContext,
                    SessionProvider.class, () -> new ContainerSessionProvider(Clock.systemUTC()));
                policies
                    .add(new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, sessionAcquirer, sessionMode));
            }
        }

        if (azureSasCredential != null) {
            policies.add(new AzureSasCredentialPolicy(azureSasCredential, false));
        } else if (sasToken != null) {
            policies.add(new AzureSasCredentialPolicy(new AzureSasCredential(sasToken), false));
        }

        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(getResponseValidationPolicy());

        HttpLoggingPolicy loggingPolicy = new HttpLoggingPolicy(logOptions);
        if (sessionsEnabled) {
            // Wrap existing HttpLoggingPolicies so they skip CreateSession requests, which contain session credentials.
            policies.replaceAll(
                policy -> policy instanceof HttpLoggingPolicy ? new SessionAwareLoggingPolicy(policy, null) : policy);
            // Add the SDK logger: use its configured options for normal requests and a redacted logger for CreateSession.
            policies.add(new SessionAwareLoggingPolicy(loggingPolicy, createSessionLoggingPolicy(logOptions)));
        } else {
            policies.add(loggingPolicy);
        }

        policies.add(new ScrubEtagPolicy());

        HttpPipeline pipeline = createPipeline(policies, httpClient, clientOptions);
        if (sessionAcquirer != null) {
            sessionAcquirer.bind(pipeline);
        }
        return pipeline;
    }

    private static String resolveSessionAccountName(String endpoint, String accountName) {
        if (!CoreUtils.isNullOrEmpty(accountName)) {
            return accountName;
        }

        BlobUrlParts endpointParts = BlobUrlParts.parse(endpoint);
        String accountNameFromHost = StorageImplUtils.getAccountNameFromKnownServiceHost(endpointParts.getHost(),
            Constants.UrlConstants.BLOB_URI_SUBDOMAIN, Constants.UrlConstants.DFS_URI_SUBDOMAIN);
        if (accountNameFromHost != null) {
            return accountNameFromHost;
        }

        throw new IllegalArgumentException(
            "The account name must be provided when sessions are used with a custom endpoint.");
    }

    /**
     * Creates the {@link SessionAcquirer} for a client. CreateSession is sent to the client's endpoint with the
     * client's credential, unless the {@link SessionContext} override the acquisition endpoint and credential.
     * Resolves the signing account once, preferring the configured account name over endpoint inference.
     */
    private static SessionAcquirer createSessionAcquirer(SessionContext sessionContext, String endpoint,
        BlobServiceVersion serviceVersion, String scope, ClientLogger logger) {
        String acquisitionEndpoint = SessionContextAccessor.getEndpoint(sessionContext);
        StorageBearerTokenChallengeAuthorizationPolicy acquisitionBearerPolicy = null;
        if (acquisitionEndpoint == null) {
            acquisitionEndpoint = endpoint;
        } else {
            TokenCredential credential = SessionContextAccessor.getCredential(sessionContext);
            httpsValidation(credential, "bearer token", acquisitionEndpoint, logger);
            acquisitionBearerPolicy = new StorageBearerTokenChallengeAuthorizationPolicy(credential, scope);
        }

        String sessionEndpoint = getSessionEndpoint(acquisitionEndpoint);
        String accountName = resolveSessionAccountName(sessionEndpoint, sessionContext.getAccountName());
        return new SessionAcquirer(sessionEndpoint, serviceVersion, accountName, acquisitionBearerPolicy);
    }

    /**
     * Removes resource and query components while retaining supported account paths. Recognized Data Lake endpoints
     * are normalized to the corresponding Blob endpoint because session-authenticated Data Lake reads use the Blob
     * pipeline.
     *
     * @param endpoint The Blob or Data Lake endpoint.
     * @return The Blob account endpoint.
     */
    public static String getSessionEndpoint(String endpoint) {
        try {
            BlobUrlParts parts = BlobUrlParts.parse(endpoint);
            String accountEndpoint = getEndpoint(parts);
            if (!StorageImplUtils.isServiceEndpoint(parts.getHost(), Constants.UrlConstants.DFS_URI_SUBDOMAIN)) {
                return accountEndpoint;
            }

            return StorageImplUtils.endpointToDesiredEndpoint(accountEndpoint,
                Constants.UrlConstants.BLOB_URI_SUBDOMAIN, Constants.UrlConstants.DFS_URI_SUBDOMAIN);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid session endpoint.", e);
        }
    }

    /**
     * Determines whether a pipeline policy provides session authentication.
     *
     * @param policy The pipeline policy to inspect. This was added to support the session authentication policy's internal delegation to a bearer token policy.
     * @return Whether the policy provides session authentication.
     */
    public static boolean isSessionAuthenticationPolicy(HttpPipelinePolicy policy) {
        return policy instanceof SessionAuthenticationPolicy;
    }

    private static HttpLoggingPolicy createSessionLoggingPolicy(HttpLogOptions logOptions) {
        HttpLogOptions sourceOptions = logOptions == null ? new HttpLogOptions() : logOptions;
        HttpLogDetailLevel logLevel = sourceOptions.getLogLevel();
        if (logLevel == HttpLogDetailLevel.BODY) {
            logLevel = HttpLogDetailLevel.BASIC;
        } else if (logLevel == HttpLogDetailLevel.BODY_AND_HEADERS) {
            logLevel = HttpLogDetailLevel.HEADERS;
        }

        // Custom loggers receive the raw response and could read the credential payload, so don't copy them.
        HttpLogOptions sessionLogOptions = new HttpLogOptions().setLogLevel(logLevel)
            .setAllowedHttpHeaderNames(new HashSet<>(sourceOptions.getAllowedHttpHeaderNames()))
            .setAllowedQueryParamNames(new HashSet<>(sourceOptions.getAllowedQueryParamNames()))
            .disableRedactedHeaderLogging(sourceOptions.isRedactedHeaderLoggingDisabled());
        return new HttpLoggingPolicy(sessionLogOptions);
    }

    /**
     * Creates an {@link HttpPipeline} from an ordered policy list, applying the transport, client options and tracer
     * configuration shared by every pipeline this helper builds.
     */
    private static HttpPipeline createPipeline(List<HttpPipelinePolicy> policies, HttpClient httpClient,
        ClientOptions clientOptions) {
        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .clientOptions(clientOptions)
            .tracer(createTracer(clientOptions))
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
        defaultOptions.addAllowedHttpHeaderName(HttpHeaderName.EXPECT);
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
