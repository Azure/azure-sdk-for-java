// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.implementation.util;

import com.azure.ai.personalizer.models.PersonalizerAudience;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility method class.
 */
public final class Utility {
    static final String DEFAULT_SCOPE = "/.default";
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties(Constants.PERSONALIZER_PROPERTIES);
        CLIENT_NAME = properties.getOrDefault(Constants.NAME, "UnknownName");
        CLIENT_VERSION = properties.getOrDefault(Constants.VERSION, "UnknownVersion");
    }

    private Utility() {
    }

    /**
     * Creates a Flux of ByteBuffer, with each ByteBuffer wrapping bytes read from the given
     * InputStream.
     *
     * @param inputStream InputStream to back the Flux
     * @return Flux of ByteBuffer backed by the InputStream
     * @throws NullPointerException If {@code inputStream} is null.
     */
    public static Flux<ByteBuffer> toFluxByteBuffer(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "'inputStream' is required and cannot be null.");
        return FluxUtil
            .toFluxByteBuffer(inputStream)
            .cache()
            .map(ByteBuffer::duplicate);
    }

    public static String parseResultId(String operationLocation) {

        if (!CoreUtils.isNullOrEmpty(operationLocation)) {
            int lastIndex = operationLocation.lastIndexOf('/');
            int firstIndex = operationLocation.indexOf('?');
            if (firstIndex != -1 && lastIndex != -1) {
                return operationLocation.substring(operationLocation.lastIndexOf('/') + 1,
                    operationLocation.indexOf('?'));
            } else if (firstIndex == -1) {
                return operationLocation.substring(operationLocation.lastIndexOf('/') + 1);
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse operation header for result Id from: " + operationLocation));
    }


    public static HttpPipeline buildHttpPipeline(ClientOptions clientOptions, HttpLogOptions logOptions,
                                                 Configuration configuration, RetryPolicy retryPolicy,
                                                 RetryOptions retryOptions, AzureKeyCredential azureKeyCredential,
                                                 TokenCredential tokenCredential, PersonalizerAudience audience,
                                                 List<HttpPipelinePolicy> perCallPolicies,
                                                 List<HttpPipelinePolicy> perRetryPolicies, HttpClient httpClient) {

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ClientOptions buildClientOptions = (clientOptions == null) ? Constants.DEFAULT_CLIENT_OPTIONS : clientOptions;
        HttpLogOptions buildLogOptions = (logOptions == null) ? Constants.DEFAULT_LOG_OPTIONS : logOptions;

        String applicationId = CoreUtils.getApplicationId(buildClientOptions, buildLogOptions);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
        httpPipelinePolicies.add(new AddHeadersPolicy(Constants.DEFAULT_HTTP_HEADERS));
        httpPipelinePolicies.add(new AddHeadersFromContextPolicy());
        httpPipelinePolicies.add(new UserAgentPolicy(applicationId, CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
        httpPipelinePolicies.add(new RequestIdPolicy());

        httpPipelinePolicies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(httpPipelinePolicies);
        httpPipelinePolicies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));

        httpPipelinePolicies.add(new AddDatePolicy());

        // Authentications
        if (tokenCredential != null) {
            if (audience == null) {
                audience = PersonalizerAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD;
            }
            httpPipelinePolicies.add(new BearerTokenAuthenticationPolicy(tokenCredential,
                audience + DEFAULT_SCOPE));
        } else if (azureKeyCredential != null) {
            httpPipelinePolicies.add(new AzureKeyCredentialPolicy(Constants.OCP_APIM_SUBSCRIPTION_KEY,
                azureKeyCredential));
        } else {
            // Throw exception that azureKeyCredential and tokenCredential cannot be null
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }
        httpPipelinePolicies.addAll(perRetryPolicies);
        HttpPolicyProviders.addAfterRetryPolicies(httpPipelinePolicies);

        HttpHeaders headers = new HttpHeaders();
        buildClientOptions.getHeaders().forEach(header -> headers.set(header.getName(), header.getValue()));
        if (headers.getSize() > 0) {
            httpPipelinePolicies.add(new AddHeadersPolicy(headers));
        }

        httpPipelinePolicies.add(new HttpLoggingPolicy(buildLogOptions));

        return new HttpPipelineBuilder()
            .clientOptions(buildClientOptions)
            .httpClient(httpClient)
            .policies(httpPipelinePolicies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }
}
