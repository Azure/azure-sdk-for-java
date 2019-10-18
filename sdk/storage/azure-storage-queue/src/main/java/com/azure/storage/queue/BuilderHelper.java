// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

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
import com.azure.storage.common.Utility;
<<<<<<< HEAD
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
=======
import com.azure.storage.common.StorageSharedKeyCredential;
>>>>>>> 44657b9dd642e20e7149dfed9d24619368acc277
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * This class provides helper methods for common builder patterns.
 */
final class BuilderHelper {
    private static final String DEFAULT_USER_AGENT_NAME = "azure-storage-queue";
    private static final String DEFAULT_USER_AGENT_VERSION = "12.0.0-preview.5";

    private static final Pattern IP_URL_PATTERN = Pattern
        .compile("(?:\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(?:localhost)");

    /**
     * Parses the passed {@code connectionString} for values to configure on the builder.
     *
     * @param connectionString Connection string from the service account.
     * @param accountNameSetter Callback to set the account name on the builder.
     * @param credentialSetter Callback to set the {@link StorageSharedKeyCredential} of the builder.
     * @param endpointSetter Callback to set the endpoint of the builder.
     * @param logger {@link ClientLogger} used to log any exceptions.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     * @throws IllegalArgumentException If {@code connectionString} doesn't contain 'AccountName' or 'AccountKey'.
     */
    static void configureConnectionString(String connectionString, Consumer<String> accountNameSetter,
                                          Consumer<StorageSharedKeyCredential> credentialSetter,
                                          Consumer<String> endpointSetter, ClientLogger logger) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");

        Map<String, String> connectionStringPieces = Utility.parseConnectionString(connectionString);

        String accountName = connectionStringPieces.get(Constants.ConnectionStringConstants.ACCOUNT_NAME);
        String accountKey = connectionStringPieces.get(Constants.ConnectionStringConstants.ACCOUNT_KEY);

        if (ImplUtils.isNullOrEmpty(accountName) || ImplUtils.isNullOrEmpty(accountKey)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'connectionString' must contain 'AccountName' and 'AccountKey'."));
        }

        String endpointProtocol = connectionStringPieces.get(Constants.ConnectionStringConstants.ENDPOINT_PROTOCOL);
        String endpointSuffix = connectionStringPieces.get(Constants.ConnectionStringConstants.ENDPOINT_SUFFIX);

        if (!ImplUtils.isNullOrEmpty(endpointProtocol) && !ImplUtils.isNullOrEmpty(endpointSuffix)) {
            endpointSetter.accept(String.format("%s://%s.queue.%s", endpointProtocol, accountName,
                endpointSuffix.replaceFirst("^\\.", "")));
        }

        accountNameSetter.accept(accountName);
        credentialSetter.accept(new StorageSharedKeyCredential(accountName, accountKey));
    }

    static void parseEndpoint(String endpoint, Consumer<String> endpointSetter, Consumer<String> accountNameSetter,
        Consumer<String> queueNameSetter, Consumer<String> sasTokenSetter, ClientLogger logger) {
        Objects.requireNonNull(endpoint);
        try {
            URL url = new URL(endpoint);
            endpointSetter.accept(url.getProtocol() + "://" + url.getAuthority());

            if (IP_URL_PATTERN.matcher(url.getHost()).find()) {
                // URL is using an IP pattern of http://127.0.0.1:10000/accountName/queueName
                // or http://localhost:10000/accountName/queueName
                String path = url.getPath();
                if (!ImplUtils.isNullOrEmpty(path) && path.charAt(0) == '/') {
                    path = path.substring(1);
                }

                String[] pathPieces = path.split("/", 2);
                accountNameSetter.accept(pathPieces[0]);

                if (queueNameSetter != null && pathPieces.length == 2) {
                    queueNameSetter.accept(pathPieces[1]);
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

                accountNameSetter.accept(accountName);

                String[] pathSegments = url.getPath().split("/", 2);
                if (queueNameSetter != null && pathSegments.length == 2 && !ImplUtils.isNullOrEmpty(pathSegments[1])) {
                    queueNameSetter.accept(pathSegments[1]);
                }
            }

            // Attempt to get the SAS token from the URL passed
            String sasToken = new QueueServiceSasQueryParameters(
                Utility.parseQueryStringSplitValues(url.getQuery()), false).encode();
            if (!ImplUtils.isNullOrEmpty(sasToken)) {
                sasTokenSetter.accept(sasToken);
            }
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
    static HttpPipeline buildPipeline(Supplier<HttpPipelinePolicy> credentialPolicySupplier,
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
}
