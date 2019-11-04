// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.implementation.util;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;

import com.azure.storage.queue.sas.QueueServiceSasQueryParameters;
import com.azure.storage.queue.QueueServiceVersion;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * This class provides helper methods for common builder patterns.
 */
public final class BuilderHelper {
    private static final String DEFAULT_USER_AGENT_NAME = "azure-storage-queue";
    // {x-version-update-start;com.azure:azure-storage-queue;current}
    private static final String DEFAULT_USER_AGENT_VERSION = "12.1.0-preview.1";
    // {x-version-update-end}

    private static final Pattern IP_URL_PATTERN = Pattern
        .compile("(?:\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(?:localhost)");

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
            QueueUrlParts parts = new QueueUrlParts();

            parts.setEndpoint(url.getProtocol() + "://" + url.getAuthority());

            if (IP_URL_PATTERN.matcher(url.getHost()).find()) {
                // URL is using an IP pattern of http://127.0.0.1:10000/accountName/queueName
                // or http://localhost:10000/accountName/queueName
                String path = url.getPath();
                if (!ImplUtils.isNullOrEmpty(path) && path.charAt(0) == '/') {
                    path = path.substring(1);
                }

                String[] pathPieces = path.split("/", 2);
                parts.setAccountName(pathPieces[0]);

                if (pathPieces.length == 2) {
                    parts.setQueueName(pathPieces[1]);
                }
            } else {
                // URL is using a pattern of http://accountName.blob.core.windows.net/queueName
                String host = url.getHost();

                String accountName = null;
                if (!ImplUtils.isNullOrEmpty(host)) {
                    int accountNameIndex = host.indexOf('.');
                    if (accountNameIndex == -1) {
                        accountName = host;
                    } else {
                        accountName = host.substring(0, accountNameIndex);
                    }
                }

                parts.setAccountName(accountName);

                String[] pathSegments = url.getPath().split("/", 2);
                if (pathSegments.length == 2 && !ImplUtils.isNullOrEmpty(pathSegments[1])) {
                    parts.setQueueName(pathSegments[1]);
                }
            }

            // Attempt to get the SAS token from the URL passed
            String sasToken = new QueueServiceSasQueryParameters(
                StorageImplUtils.parseQueryStringSplitValues(url.getQuery()), false).encode();
            if (!ImplUtils.isNullOrEmpty(sasToken)) {
                parts.setQueueName(sasToken);
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
     * @param credentialPolicySupplier Supplier for credentials in the pipeline.
     * @param retryOptions Retry options to set in the retry policy.
     * @param logOptions Logging options to set in the logging policy.
     * @param httpClient HttpClient to use in the builder.
     * @param additionalPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline.
     * @param configuration Configuration store contain environment settings.
     * @param serviceVersion {@link QueueServiceVersion} of the service to be used when making requests.
     * @return A new {@link HttpPipeline} from the passed values.
     */
    public static HttpPipeline buildPipeline(Supplier<HttpPipelinePolicy> credentialPolicySupplier,
        RequestRetryOptions retryOptions, HttpLogOptions logOptions, HttpClient httpClient,
        List<HttpPipelinePolicy> additionalPolicies, Configuration configuration, QueueServiceVersion serviceVersion) {

        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(getUserAgentPolicy(configuration, serviceVersion));
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        HttpPipelinePolicy credentialPolicy = credentialPolicySupplier.get();
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
     * @param version {@link QueueServiceVersion} of the service to be used when making requests.
     * @return The default {@link UserAgentPolicy} for the module.
     */
    private static UserAgentPolicy getUserAgentPolicy(Configuration configuration, QueueServiceVersion version) {
        configuration = (configuration == null) ? Configuration.NONE : configuration;

        return new UserAgentPolicy(DEFAULT_USER_AGENT_NAME, DEFAULT_USER_AGENT_VERSION, configuration, version);
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

    public static class QueueUrlParts {
        private String endpoint;
        private String accountName;
        private String queueName;
        private String sasToken;

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
