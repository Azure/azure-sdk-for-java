// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.utils;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.*;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides helper methods for common builder patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public final class BuilderHelper {
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-communication-jobrouter.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final String CLIENT_NAME = PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
    private static final String CLIENT_VERSION = PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");

    /**
     * Constructs a {@link HttpPipeline} from values passed from a builder.
     *
     * @param tokenCredential {@link TokenCredential} if present.
     * @param azureSasCredential {@link AzureSasCredential} if present.
     * @param sasToken SAS token if present.
     * @param retryOptions Storage's retry options to set in the retry policy.
     * @param logOptions Logging options to set in the logging policy.
     * @param clientOptions Client options.
     * @param httpClient HttpClient to use in the builder.
     * @param perCallPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline per call.
     * @param perRetryPolicies Additional {@link HttpPipelinePolicy policies} to set in the pipeline per retry.
     * @param configuration Configuration store contain environment settings.
     * @param logger {@link ClientLogger} used to log any exception.
     * @return A new {@link HttpPipeline} from the passed values.
     */
    public static HttpPipeline buildPipeline(
        HttpPipelinePolicy credentialPolicy,
        RetryOptions retryOptions,
        HttpLogOptions logOptions, ClientOptions clientOptions, HttpClient httpClient,
        List<HttpPipelinePolicy> perCallPolicies, List<HttpPipelinePolicy> perRetryPolicies,
        Configuration configuration, ClientLogger logger) {

        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(getUserAgentPolicy(configuration, logOptions, clientOptions));
        policies.add(new RequestIdPolicy());

        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RetryPolicy(retryOptions));

        policies.add(new AddDatePolicy());

        // We need to place this policy right before the credential policy since headers may affect the string to sign
        // of the request.
        HttpHeaders headers = new HttpHeaders();
        clientOptions.getHeaders().forEach(header -> headers.set(header.getName(), header.getValue()));
        if (headers.getSize() > 0) {
            policies.add(new AddHeadersPolicy(headers));
        }

        if (credentialPolicy != null) {
            policies.add(credentialPolicy);
        }

        if (perRetryPolicies!= null) {
            policies.addAll(perRetryPolicies);
        }

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new HttpLoggingPolicy(logOptions));

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
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
        String applicationId = clientOptions.getApplicationId() != null ? clientOptions.getApplicationId()
            : logOptions.getApplicationId();
        return new UserAgentPolicy(applicationId, CLIENT_NAME, CLIENT_VERSION, configuration);
    }
}
