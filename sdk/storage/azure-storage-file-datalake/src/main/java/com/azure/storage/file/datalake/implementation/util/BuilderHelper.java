// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

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
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;
import com.azure.storage.common.policy.ScrubEtagPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class provides helper methods for common builder patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public final class BuilderHelper {
    private static final String DEFAULT_USER_AGENT_NAME = "azure-storage-file-datalake";
    private static final String DEFAULT_USER_AGENT_VERSION = "12.0.0-preview.5";

    /**
     * Constructs a {@link HttpPipeline} from values passed from a builder.
     *
     * @param credentialPolicySupplier Supplier for credentials in the pipeline.
     * @param retryOptions Retry options to set in the retry policy.
     * @param logOptions Logging options to set in the logging policy.
     * @param httpClient HttpClient to use in the builder.
     * @param additionalPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline.
     * @param configuration Configuration store contain environment settings.
     * @return A new {@link HttpPipeline} from the passed values.
     */
    public static HttpPipeline buildPipeline(Supplier<HttpPipelinePolicy> credentialPolicySupplier,
        RequestRetryOptions retryOptions, HttpLogOptions logOptions, HttpClient httpClient,
        List<HttpPipelinePolicy> additionalPolicies, Configuration configuration) {
        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(getUserAgentPolicy(configuration));
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
    public static String getEndpoint(BlobUrlParts parts) {
        if (ModelHelper.IP_V4_URL_PATTERN.matcher(parts.getHost()).find()) {
            return String.format("%s://%s/%s", parts.getScheme(), parts.getHost(), parts.getAccountName());
        } else {
            return String.format("%s://%s", parts.getScheme(), parts.getHost());
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

        return new UserAgentPolicy(getDefaultHttpLogOptions().getApplicationId(),
            DEFAULT_USER_AGENT_NAME, DEFAULT_USER_AGENT_VERSION, configuration);
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
