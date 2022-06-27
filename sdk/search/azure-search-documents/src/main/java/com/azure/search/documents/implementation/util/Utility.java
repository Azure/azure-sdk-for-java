// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

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
import com.azure.core.http.rest.Response;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.SearchIndexClientImpl;
import com.azure.search.documents.implementation.models.IndexBatch;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexDocumentsResult;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.core.util.FluxUtil.monoError;

public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

    private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();
    private static final HttpLogOptions DEFAULT_LOG_OPTIONS = Constants.DEFAULT_LOG_OPTIONS_SUPPLIER.get();
    private static final HttpHeaders HTTP_HEADERS = new HttpHeaders().set("return-client-request-id", "true");

    private static final DecimalFormat COORDINATE_FORMATTER = new DecimalFormat();

    /*
     * Representation of the Multi-Status HTTP response code.
     */
    private static final int MULTI_STATUS_CODE = 207;

    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-search-documents.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    public static HttpPipeline buildHttpPipeline(ClientOptions clientOptions, HttpLogOptions logOptions,
        Configuration configuration, RetryPolicy retryPolicy, RetryOptions retryOptions,
        AzureKeyCredential azureKeyCredential, TokenCredential tokenCredential,
        List<HttpPipelinePolicy> perCallPolicies, List<HttpPipelinePolicy> perRetryPolicies, HttpClient httpClient,
        ClientLogger logger) {
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ClientOptions buildClientOptions = (clientOptions == null) ? DEFAULT_CLIENT_OPTIONS : clientOptions;
        HttpLogOptions buildLogOptions = (logOptions == null) ? DEFAULT_LOG_OPTIONS : logOptions;

        String applicationId = CoreUtils.getApplicationId(buildClientOptions, buildLogOptions);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
        httpPipelinePolicies.add(new AddHeadersPolicy(HTTP_HEADERS));
        httpPipelinePolicies.add(new AddHeadersFromContextPolicy());
        httpPipelinePolicies.add(new UserAgentPolicy(applicationId, CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
        httpPipelinePolicies.add(new RequestIdPolicy());

        httpPipelinePolicies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(httpPipelinePolicies);
        httpPipelinePolicies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));

        httpPipelinePolicies.add(new AddDatePolicy());

        if (azureKeyCredential != null && tokenCredential != null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Builder has both AzureKeyCredential and "
                + "TokenCredential supplied. Only one may be supplied."));
        } else if (azureKeyCredential != null) {
            httpPipelinePolicies.add(new AzureKeyCredentialPolicy("api-key", azureKeyCredential));
        } else if (tokenCredential != null) {
            httpPipelinePolicies.add(new BearerTokenAuthenticationPolicy(tokenCredential,
                "https://search.azure.com/.default"));
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException("Builder doesn't have a credential "
                + "configured. Supply either an AzureKeyCredential or TokenCredential."));
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

    public static Mono<Response<IndexDocumentsResult>> indexDocumentsWithResponse(SearchIndexClientImpl restClient,
        List<com.azure.search.documents.implementation.models.IndexAction> actions, boolean throwOnAnyError,
        Context context, ClientLogger logger) {
        try {
            return restClient.getDocuments().indexWithResponseAsync(new IndexBatch(actions), null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .flatMap(response -> (response.getStatusCode() == MULTI_STATUS_CODE && throwOnAnyError)
                    ? Mono.error(new IndexBatchException(response.getValue()))
                    : Mono.just(response));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public static SearchIndexClientImpl buildRestClient(SearchServiceVersion serviceVersion, String endpoint,
        String indexName, HttpPipeline httpPipeline) {
        return new SearchIndexClientImpl(httpPipeline, endpoint, indexName, serviceVersion.getVersion());
    }

    public static synchronized String formatCoordinate(double coordinate) {
        return COORDINATE_FORMATTER.format(coordinate);
    }

    public static String readSynonymsFromFile(Path filePath) {
        try {
            return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    private Utility() {
    }
}
