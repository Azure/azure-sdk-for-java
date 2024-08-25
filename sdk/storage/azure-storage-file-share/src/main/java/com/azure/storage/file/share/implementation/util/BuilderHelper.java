// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

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
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.BuilderUtils;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.credentials.CredentialValidator;
import com.azure.storage.common.policy.MetadataValidationPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import com.azure.storage.common.sas.CommonSasQueryParameters;
import com.azure.storage.file.share.models.ShareAudience;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

/**
 * This class provides helper methods for common builder patterns.
 */
public final class BuilderHelper {
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-storage-file-share.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    private static final Pattern IP_URL_PATTERN = Pattern
        .compile("(?:\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(?:localhost)");

    /**
     * Constructs a {@link HttpPipeline} from values passed from a builder.
     *
     * @param storageSharedKeyCredential {@link StorageSharedKeyCredential} if present.
     * @param tokenCredential {@link TokenCredential} if present.
     * @param azureSasCredential {@link AzureSasCredential} if present.
     * @param sasToken SAS token if present.
     * @param retryOptions Storage retry options to set in the retry policy.
     * @param coreRetryOptions Core retry options to set in the retry policy.
     * @param logOptions Logging options to set in the logging policy.
     * @param clientOptions Client options.
     * @param httpClient HttpClient to use in the builder.
     * @param perCallPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline per call.
     * @param perRetryPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline per retry.
     * @param configuration Configuration store contain environment settings.
     * @param audience {@link ShareAudience} used to determine the audience of the path.
     * @param logger {@link ClientLogger} used to log any exception.
     * @return A new {@link HttpPipeline} from the passed values.
     */
    public static HttpPipeline buildPipeline(StorageSharedKeyCredential storageSharedKeyCredential,
        TokenCredential tokenCredential, AzureSasCredential azureSasCredential, String sasToken, String endpoint,
        RequestRetryOptions retryOptions, RetryOptions coreRetryOptions, HttpLogOptions logOptions,
        ClientOptions clientOptions, HttpClient httpClient, List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies, Configuration configuration, ShareAudience audience,
        ClientLogger logger) {

        CredentialValidator.validateSingleCredentialIsPresent(
            storageSharedKeyCredential, tokenCredential, azureSasCredential, sasToken, logger);

        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(getUserAgentPolicy(configuration, logOptions, clientOptions));
        policies.add(new RequestIdPolicy());

        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(BuilderUtils.createRetryPolicy(retryOptions, coreRetryOptions, logger));

        policies.add(new AddDatePolicy());

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
            httpsValidation(tokenCredential, "bearer token", endpoint, logger);
            String scope = audience != null ? ((audience.toString().endsWith("/")
                ? audience + ".default" : audience + "/.default")) : Constants.STORAGE_SCOPE;
            credentialPolicy =  new BearerTokenAuthenticationPolicy(tokenCredential, scope);
        } else if (azureSasCredential != null) {
            credentialPolicy = new AzureSasCredentialPolicy(azureSasCredential, false);
        } else if (sasToken != null) {
            credentialPolicy = new AzureSasCredentialPolicy(new AzureSasCredential(sasToken), false);
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

    /**
     * Gets the default http log option for Storage File.
     *
     * @return the default http log options.
     */
    public static HttpLogOptions getDefaultHttpLogOptions() {
        HttpLogOptions defaultOptions = new HttpLogOptions();
        FileHeadersAndQueryParameters.getFileHeaders().forEach(defaultOptions::addAllowedHeaderName);
        FileHeadersAndQueryParameters.getFileQueryParameters().forEach(defaultOptions::addAllowedQueryParamName);
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
            .build();
    }

    /**
     * Extracts the account name from the passed Azure Storage URL.
     *
     * @param url Azure Storage URL.
     * @return the account name in the endpoint, or null if the URL doesn't match the expected formats.
     */
    public static String getAccountName(URL url) {
        if (IP_URL_PATTERN.matcher(url.getHost()).find()) {
            // URL is using an IP pattern of http://127.0.0.1:10000/accountName or http://localhost:10000/accountName
            String path = url.getPath();
            if (!CoreUtils.isNullOrEmpty(path) && path.charAt(0) == '/') {
                path = path.substring(1);
            }

            String[] pathPieces = path.split("/", 1);
            return (pathPieces.length == 1) ? pathPieces[0] : null;
        } else {
            // URL is using a pattern of http://accountName.blob.core.windows.net
            String host = url.getHost();

            if (CoreUtils.isNullOrEmpty(host)) {
                return null;
            }

            int accountNameIndex = host.indexOf('.');
            if (accountNameIndex == -1) {
                return host;
            } else {
                return host.substring(0, accountNameIndex);
            }
        }
    }

    /**
     * Validates that the client is properly configured to use https.
     *
     * @param objectToCheck The object to check for.
     * @param objectName The name of the object.
     * @param endpoint The endpoint for the client.
     * @param logger {@link ClientLogger} used to log any exception.
     */
    public static void httpsValidation(Object objectToCheck, String objectName, String endpoint, ClientLogger logger) {
        if (objectToCheck != null && !parseEndpoint(endpoint, logger).getScheme().equals(Constants.HTTPS)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Using a(n) " + objectName + " requires https"));
        }
    }

    /**
     * Parses the given endpoint url into a ShareUrlParts.
     *
     * @param endpoint The endpoint url to be parsed.
     * @param logger {@link ClientLogger}
     * @return A {@link ShareUrlParts} object containing all the components of a ShareURL.
     * @throws IllegalArgumentException If the endpoint is malformed.
     */
    public static ShareUrlParts parseEndpoint(String endpoint, ClientLogger logger) {
        Objects.requireNonNull(endpoint);
        try {
            URL url = new URL(endpoint);
            ShareUrlParts parts = new ShareUrlParts().setScheme(url.getProtocol());

            if (determineAuthorityIsIpStyle(url.getAuthority())) {
                // URL is using an IP pattern of http://127.0.0.1:10000/accountName/shareName
                // or http://localhost:10000/accountName/shareName
                String path = url.getPath();
                if (!CoreUtils.isNullOrEmpty(path) && path.charAt(0) == '/') {
                    path = path.substring(1);
                }

                String[] pathPieces = path.split("/", 2);
                parts.setAccountName(pathPieces[0]);

                if (pathPieces.length == 2) {
                    parts.setShareName(pathPieces[1]);
                }

                parts.setEndpoint(url.getProtocol() + "://" + url.getAuthority() + "/" + parts.getAccountName());
            } else {
                // URL is using a pattern of http://accountName.file.core.windows.net/shareName
                String host = url.getHost();

                String accountName = null;
                if (!CoreUtils.isNullOrEmpty(host)) {
                    int accountNameIndex = host.indexOf('.');
                    if (accountNameIndex == -1) {
                        accountName = host;
                    } else {
                        accountName = host.substring(0, accountNameIndex);
                    }
                }

                parts.setAccountName(accountName);

                String[] pathSegments = url.getPath().split("/", 2);
                if (pathSegments.length == 2 && !CoreUtils.isNullOrEmpty(pathSegments[1])) {
                    parts.setShareName(pathSegments[1]);
                }

                parts.setEndpoint(url.getProtocol() + "://" + url.getAuthority());
            }

            // Attempt to get the SAS token from the URL passed
            String sasToken = new CommonSasQueryParameters(
                SasImplUtils.parseQueryString(url.getQuery()), false).encode();
            if (!CoreUtils.isNullOrEmpty(sasToken)) {
                parts.setSasToken(sasToken);
            }

            return parts;
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage Share endpoint url is malformed.", ex));
        }
    }

    /**
     * Determines whether the passed authority is IP style, that is, it is of the format {@code <host>:<port>}.
     *
     * @param authority The authority of a URL.
     * @throws MalformedURLException If the authority is malformed.
     * @return Whether the authority is IP style.
     */
    public static boolean determineAuthorityIsIpStyle(String authority) throws MalformedURLException {
        return new URL("http://" +  authority).getPort() != -1;
    }

    /**
     * This class represents the components that make up an Azure Storage Share URL.
     */
    public static class ShareUrlParts {
        private String scheme;
        private String endpoint;
        private String accountName;
        private String shareName;
        private String sasToken;

        /**
         * Gets the URL scheme, ex. "https".
         *
         * @return the URL scheme.
         */
        public String getScheme() {
            return scheme;
        }

        /**
         * Sets the URL scheme, ex. "https".
         *
         * @param scheme The URL scheme.
         * @return the updated ShareUrlParts object.
         */
        public ShareUrlParts setScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        /**
         * Gets the endpoint for the share service based on the parsed URL.
         *
         * @return The endpoint for the share service.
         */
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * Sets the endpoint for the share service.
         *
         * @return the updated ShareUrlParts object.
         */
        public ShareUrlParts setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Gets the accountname, ex. "myaccountname".
         *
         * @return the account name.
         */
        public String getAccountName() {
            return accountName;
        }

        /**
         * Sets the account name.
         *
         * @param accountName The account name.
         * @return the updated ShareUrlParts object.
         */
        public ShareUrlParts setAccountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        /**
         * Gets the share name that will be used as part of the URL path.
         *
         * @return the share name.
         */
        public String getShareName() {
            return shareName;
        }

        /**
         * Sets the share name that will be used as part of the URL path.
         *
         * @param shareName The share nme.
         * @return the updated ShareUrlParts object.
         */
        ShareUrlParts setShareName(String shareName) {
            this.shareName = shareName;
            return this;
        }

        /**
         * Gets the sas token that will be used as part of the URL path.
         *
         * @return the sas token.
         */
        public String getSasToken() {
            return sasToken;
        }

        /**
         * Sets the sas token that will be used as part of the URL path.
         *
         * @param sasToken the sas token.
         * @return the updated ShareUrlParts object.
         */
        public ShareUrlParts setSasToken(String sasToken) {
            this.sasToken = sasToken;
            return this;
        }
    }

    private static Tracer createTracer(ClientOptions clientOptions) {
        TracingOptions tracingOptions = clientOptions == null ? null : clientOptions.getTracingOptions();
        return TracerProvider.getDefaultProvider()
            .createTracer(CLIENT_NAME, CLIENT_VERSION, STORAGE_TRACING_NAMESPACE_VALUE, tracingOptions);
    }
}
