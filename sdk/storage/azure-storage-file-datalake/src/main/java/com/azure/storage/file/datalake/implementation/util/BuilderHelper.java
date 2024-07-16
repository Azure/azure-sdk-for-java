// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

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
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy;
import com.azure.storage.blob.implementation.util.ModelHelper;
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
import com.azure.storage.file.datalake.models.DataLakeAudience;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

/**
 * This class provides helper methods for common builder patterns.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class BuilderHelper {
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;
    private static final HttpHeaderName X_MS_UPN = HttpHeaderName.fromString("x-ms-upn");

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-storage-file-datalake.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    /**
     * Constructs a {@link HttpPipeline} from values passed from a builder.
     *
     * @param storageSharedKeyCredential {@link StorageSharedKeyCredential} if present.
     * @param tokenCredential {@link TokenCredential} if present.
     * @param azureSasCredential {@link AzureSasCredential} if present.
     * @param endpoint The endpoint for the client.
     * @param retryOptions Storage retry options to set in the retry policy.
     * @param coreRetryOptions Core retry options to set in the retry policy.
     * @param logOptions Logging options to set in the logging policy.
     * @param clientOptions Client options.
     * @param httpClient HttpClient to use in the builder.
     * @param perCallPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline per call.
     * @param perRetryPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline per retry.
     * @param configuration Configuration store contain environment settings.
     * @param audience {@link DataLakeAudience} used to determine the audience of the path.
     * @param logger {@link ClientLogger} used to log any exception.
     * @return A new {@link HttpPipeline} from the passed values.
     */
    public static HttpPipeline buildPipeline(
        StorageSharedKeyCredential storageSharedKeyCredential,
        TokenCredential tokenCredential, AzureSasCredential azureSasCredential, String endpoint,
        RequestRetryOptions retryOptions, RetryOptions coreRetryOptions,
        HttpLogOptions logOptions, ClientOptions clientOptions, HttpClient httpClient,
        List<HttpPipelinePolicy> perCallPolicies, List<HttpPipelinePolicy> perRetryPolicies,
        Configuration configuration, DataLakeAudience audience, ClientLogger logger) {

        CredentialValidator.validateSingleCredentialIsPresent(
            storageSharedKeyCredential, tokenCredential, azureSasCredential, null, logger);

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

        HttpPipelinePolicy credentialPolicy;
        if (storageSharedKeyCredential != null) {
            credentialPolicy =  new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential);
        } else if (tokenCredential != null) {
            // The endpoint scope for the BearerToken is the blob endpoint not dfs
            String scope = audience != null
                ? ((audience.toString().endsWith("/") ? audience + ".default" : audience + "/.default"))
                : Constants.STORAGE_SCOPE;
            credentialPolicy =  new StorageBearerTokenChallengeAuthorizationPolicy(tokenCredential, scope);
        } else if (azureSasCredential != null) {
            credentialPolicy = new AzureSasCredentialPolicy(azureSasCredential, false);
        } else {
            credentialPolicy =  null;
        }

        if (credentialPolicy != null) {
            policies.add(credentialPolicy);
        }

        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(getResponseValidationPolicy());

        policies.add(new HttpLoggingPolicy(logOptions));

        policies.add(new ScrubEtagPolicy());

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
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
        DataLakeHeadersAndQueryParameters.getDataLakeHeaders().forEach(defaultOptions::addAllowedHeaderName);
        DataLakeHeadersAndQueryParameters.getDataLakeQueryParameters().forEach(
            defaultOptions::addAllowedQueryParamName);
        return defaultOptions;
    }

    /**
     * Gets the endpoint for the data lake service based on the parsed URL.
     *
     * @param parts The {@link BlobUrlParts} from the parse URL.
     * @return The endpoint for the data lake service.
     */
    public static String getEndpoint(BlobUrlParts parts) throws MalformedURLException {
        if (ModelHelper.determineAuthorityIsIpStyle(parts.getHost())) {
            return String.format("%s://%s/%s", parts.getScheme(), parts.getHost(), parts.getAccountName());
        } else {
            return String.format("%s://%s", parts.getScheme(), parts.getHost());
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
        return new ResponseValidationPolicyBuilder()
            .addOptionalEcho(Constants.HeaderConstants.CLIENT_REQUEST_ID)
            .addOptionalEcho(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256)
            .build();
    }

    /**
     * Gets a BlobUserAgentModificationPolicy with the correct clientName and clientVersion.
     *
     * @return {@link BlobUserAgentModificationPolicy}
     */
    public static BlobUserAgentModificationPolicy getBlobUserAgentModificationPolicy() {
        return new BlobUserAgentModificationPolicy(CLIENT_NAME, CLIENT_VERSION);
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

    private static Tracer createTracer(ClientOptions clientOptions) {
        TracingOptions tracingOptions = clientOptions == null ? null : clientOptions.getTracingOptions();
        return TracerProvider.getDefaultProvider()
            .createTracer(CLIENT_NAME, CLIENT_VERSION, STORAGE_TRACING_NAMESPACE_VALUE, tracingOptions);
    }

    public static Context addUpnHeader(Supplier<Boolean> upnHeaderValue, Context context) {
        Boolean value = upnHeaderValue.get();
        if (value == null) {
            return context;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(X_MS_UPN, Boolean.toString(value));
        if (context == null) {
            return new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);
        } else {
            return context.addData(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers);
        }

    }
}
