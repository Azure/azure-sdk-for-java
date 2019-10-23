// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

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
import com.azure.core.util.Configuration;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageAllowedHeadersAndQueries;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;

import com.azure.storage.common.policy.ScrubEtagPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This class provides helper methods for common builder patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public final class BuilderHelper {
    private static final String DEFAULT_USER_AGENT_NAME = "azure-storage-blob";
    // {x-version-update-start;com.azure:azure-storage-blob;current}
    private static final String DEFAULT_USER_AGENT_VERSION = "12.0.0-preview.5";
    // {x-version-update-end}

    /**
     * Constructs a {@link HttpPipeline} from values passed from a builder.
     *
     * @param credentialPolicySupplier Supplier for credentials in the pipeline.
     * @param retryOptions Retry options to set in the retry policy.
     * @param logOptions Logging options to set in the logging policy.
     * @param httpClient HttpClient to use in the builder.
     * @param additionalPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline.
     * @param configuration Configuration store contain environment settings.
     * @param serviceVersion {@link BlobServiceVersion} of the service to be used when making requests.
     * @return A new {@link HttpPipeline} from the passed values.
     */
    public static HttpPipeline buildPipeline(Supplier<HttpPipelinePolicy> credentialPolicySupplier,
        RequestRetryOptions retryOptions, HttpLogOptions logOptions, HttpClient httpClient,
        List<HttpPipelinePolicy> additionalPolicies, Configuration configuration, BlobServiceVersion serviceVersion) {
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

        // Prepare load options for logging policy.
        loadLogOptions(logOptions);

        policies.add(new HttpLoggingPolicy(logOptions));

        policies.add(new ScrubEtagPolicy());

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /**
     * Sets the allowed headers and queries to logOptions.
     * @param logOptions the log options for headers and queries.
     */
    private static void loadLogOptions(final HttpLogOptions logOptions) {
        Objects.requireNonNull(logOptions);

        logOptions.setAllowedHeaderNames(StorageAllowedHeadersAndQueries.BlobHeadersAndQueries.getBlobHeaders());

        logOptions.setAllowedQueryParamNames(StorageAllowedHeadersAndQueries.BlobHeadersAndQueries.getBlobQueries());
    }

    /*
     * Creates a {@link UserAgentPolicy} using the default blob module name and version.
     *
     * @param configuration Configuration store used to determine whether telemetry information should be included.
     * @param version {@link BlobServiceVersion} of the service to be used when making requests.
     * @return The default {@link UserAgentPolicy} for the module.
     */
    private static UserAgentPolicy getUserAgentPolicy(Configuration configuration, BlobServiceVersion serviceVersion) {
        configuration = (configuration == null) ? Configuration.NONE : configuration;

        return new UserAgentPolicy(DEFAULT_USER_AGENT_NAME, DEFAULT_USER_AGENT_VERSION, configuration, serviceVersion);
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
