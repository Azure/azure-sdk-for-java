// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.implementation.util;

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
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import com.azure.storage.common.implementation.policy.SasTokenCredentialPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;
import com.azure.storage.common.sas.CommonSasQueryParameters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides helper methods for common builder patterns.
 */
public final class BuilderHelper {
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-storage-queue.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    /**
     * Determines whether or not the passed authority is IP style, that is it is of the format
     * {@code <host>:<port>}.
     *
     * @param authority The authority of a URL.
     * @throws MalformedURLException If the authority is malformed.
     * @return Whether the authority is IP style.
     */
    public static boolean determineAuthorityIsIpStyle(String authority) throws MalformedURLException {
        return new URL("http://" +  authority).getPort() != -1;
    }

    /**
     * Parse the endpoint for the account name, queue name, and SAS token query parameters.
     *
     * @param endpoint Endpoint to parse.
     * @param logger {@link ClientLogger} used to log any exception.
     * @return The parsed endpoint as a {@link QueueUrlParts}.
     */
    public static QueueUrlParts parseEndpoint(String endpoint, ClientLogger logger) {
        Objects.requireNonNull(endpoint);
        try {
            URL url = new URL(endpoint);
            QueueUrlParts parts = new QueueUrlParts().setScheme(url.getProtocol());

            if (determineAuthorityIsIpStyle(url.getAuthority())) {
                // URL is using an IP pattern of http://127.0.0.1:10000/accountName/queueName
                // or http://localhost:10000/accountName/queueName
                String path = url.getPath();
                if (!CoreUtils.isNullOrEmpty(path) && path.charAt(0) == '/') {
                    path = path.substring(1);
                }

                String[] pathPieces = path.split("/", 2);
                parts.setAccountName(pathPieces[0]);

                if (pathPieces.length == 2) {
                    parts.setQueueName(pathPieces[1]);
                }

                parts.setEndpoint(String.format("%s://%s/%s", url.getProtocol(), url.getAuthority(),
                    parts.getAccountName()));
            } else {
                // URL is using a pattern of http://accountName.queue.core.windows.net/queueName
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
                    parts.setQueueName(pathSegments[1]);
                }

                parts.setEndpoint(String.format("%s://%s", url.getProtocol(), url.getAuthority()));
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
                new IllegalArgumentException("The Azure Storage Queue endpoint url is malformed.", ex));
        }
    }

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

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RequestRetryPolicy(retryOptions));

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
     * Gets the default http log option for Storage Queue.
     *
     * @return the default http log options.
     */
    public static HttpLogOptions getDefaultHttpLogOptions() {
        HttpLogOptions defaultOptions = new HttpLogOptions();
        QueueHeadersAndQueryParameters.getQueueHeaders().forEach(defaultOptions::addAllowedHeaderName);
        QueueHeadersAndQueryParameters.getQueueQueryParameters().forEach(defaultOptions::addAllowedQueryParamName);
        return defaultOptions;
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
            .build();
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


    public static class QueueUrlParts {
        private String scheme;
        private String endpoint;
        private String accountName;
        private String queueName;
        private String sasToken;

        public String getScheme() {
            return scheme;
        }

        public QueueUrlParts setScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public QueueUrlParts setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public String getAccountName() {
            return accountName;
        }

        public QueueUrlParts setAccountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public String getQueueName() {
            return queueName;
        }

        QueueUrlParts setQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public String getSasToken() {
            return sasToken;
        }

        public QueueUrlParts setSasToken(String sasToken) {
            this.sasToken = sasToken;
            return this;
        }
    }
}
