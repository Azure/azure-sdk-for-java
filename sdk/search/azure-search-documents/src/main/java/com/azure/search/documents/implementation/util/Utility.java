// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.util;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
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
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.SearchIndexClientImpl;
import com.azure.search.documents.implementation.models.ErrorResponseException;
import com.azure.search.documents.implementation.models.IndexBatch;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.SearchAudience;
import com.azure.search.documents.models.SuggestOptions;
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
import java.util.function.Supplier;

import static com.azure.core.util.FluxUtil.monoError;

public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();
    private static final HttpLogOptions DEFAULT_LOG_OPTIONS = Constants.DEFAULT_LOG_OPTIONS_SUPPLIER.get();
    private static final HttpHeaders HTTP_HEADERS = new HttpHeaders().set("return-client-request-id", "true");

    private static final ThreadLocal<DecimalFormat> COORDINATE_FORMATTER = ThreadLocal.withInitial(DecimalFormat::new);

    /*
     * Representation of the Multi-Status HTTP response code.
     */
    private static final int MULTI_STATUS_CODE = 207;

    /*
     * Exception message to use if the document isn't found.
     */
    private static final String DOCUMENT_NOT_FOUND = "Document not found.";

    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-search-documents.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    public static HttpPipeline buildHttpPipeline(ClientOptions clientOptions,
        HttpLogOptions logOptions,
        Configuration configuration,
        RetryPolicy retryPolicy,
        RetryOptions retryOptions,
        AzureKeyCredential azureKeyCredential,
        TokenCredential tokenCredential,
        SearchAudience audience,
        List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies,
        HttpClient httpClient,
        ClientLogger logger) {
        Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

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
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Builder has both AzureKeyCredential and TokenCredential supplied. Only one may be supplied."));
        } else if (azureKeyCredential != null) {
            httpPipelinePolicies.add(new AzureKeyCredentialPolicy("api-key", azureKeyCredential));
        } else if (tokenCredential != null) {
            String audienceUrl = audience == null ? SearchAudience.AZURE_PUBLIC_CLOUD.toString() : audience.toString();
            httpPipelinePolicies.add(new BearerTokenAuthenticationPolicy(tokenCredential, audienceUrl + "/.default"));
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException("Builder doesn't have a credential "
                                                                          + "configured. Supply either an AzureKeyCredential or TokenCredential."));
        }

        httpPipelinePolicies.addAll(perRetryPolicies);
        HttpPolicyProviders.addAfterRetryPolicies(httpPipelinePolicies);

        HttpHeaders headers = CoreUtils.createHttpHeadersFromClientOptions(buildClientOptions);
        if (headers != null) {
            httpPipelinePolicies.add(new AddHeadersPolicy(headers));
        }

        httpPipelinePolicies.add(new HttpLoggingPolicy(buildLogOptions));

        return new HttpPipelineBuilder()
            .clientOptions(buildClientOptions)
            .httpClient(httpClient)
            .policies(httpPipelinePolicies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }

    public static Mono<Response<IndexDocumentsResult>> indexDocumentsWithResponseAsync(SearchIndexClientImpl restClient,
        List<com.azure.search.documents.implementation.models.IndexAction> actions,
        boolean throwOnAnyError,
        Context context,
        ClientLogger logger) {
        try {
            return restClient
                .getDocuments()
                .indexWithResponseAsync(new IndexBatch(actions), null, context)
                .onErrorMap(MappingUtils::exceptionMapper)
                .flatMap(response -> (response.getStatusCode() == MULTI_STATUS_CODE && throwOnAnyError) ? Mono.error(
                    new IndexBatchException(response.getValue())) : Mono.just(response));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public static Response<IndexDocumentsResult> indexDocumentsWithResponse(SearchIndexClientImpl restClient,
        List<com.azure.search.documents.implementation.models.IndexAction> actions, boolean throwOnAnyError,
        Context context, ClientLogger logger) {
        return executeRestCallWithExceptionHandling(() -> {
            Response<IndexDocumentsResult> response = restClient.getDocuments()
                .indexWithResponse(new IndexBatch(actions), null, context);
            if (response.getStatusCode() == MULTI_STATUS_CODE && throwOnAnyError) {
                throw logger.logExceptionAsError(new IndexBatchException(response.getValue()));
            }
            return response;
        }, logger);
    }

    public static SearchIndexClientImpl buildRestClient(SearchServiceVersion serviceVersion,
        String endpoint,
        String indexName,
        HttpPipeline httpPipeline) {
        return new SearchIndexClientImpl(httpPipeline, endpoint, indexName, serviceVersion.getVersion());
    }

    public static String formatCoordinate(double coordinate) {
        return COORDINATE_FORMATTER.get().format(coordinate);
    }

    public static String readSynonymsFromFile(Path filePath) {
        try {
            return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    public static <T> T executeRestCallWithExceptionHandling(Supplier<T> supplier, ClientLogger logger) {
        try {
            return supplier.get();
        } catch (com.azure.search.documents.indexes.implementation.models.ErrorResponseException exception) {
            throw logger.logExceptionAsError(new HttpResponseException(exception.getMessage(),
                exception.getResponse()));
        } catch (com.azure.search.documents.implementation.models.ErrorResponseException exception) {
            throw logger.logExceptionAsError(new HttpResponseException(exception.getMessage(),
                exception.getResponse()));
        } catch (RuntimeException ex) {
            throw logger.logExceptionAsError(ex);
        }
    }

    /**
     * Ensures that all suggest parameters are correctly set. This method should be used when {@link SuggestOptions} is
     * passed to the Search service.
     *
     * @param suggestOptions suggest parameters
     * @return SuggestOptions ensured suggest parameters
     */
    public static SuggestOptions ensureSuggestOptions(SuggestOptions suggestOptions) {
        if (suggestOptions == null) {
            return null;
        }

        return CoreUtils.isNullOrEmpty(suggestOptions.getSelect()) ? suggestOptions.setSelect("*") : suggestOptions;
    }

    /**
     * Converts the {@link Throwable} into a more descriptive exception type if the {@link SearchDocument} isn't found.
     *
     * @param throwable Throwable thrown during a API call.
     * @return The {@link Throwable} mapped to a more descriptive exception type if the {@link SearchDocument}
     * isn't found, otherwise the passed {@link Throwable} unmodified.
     */
    public static Throwable exceptionMapper(Throwable throwable) {
        if (!(throwable instanceof ErrorResponseException)) {
            return throwable;
        }

        return mapErrorResponseException((ErrorResponseException) throwable);
    }

    public static HttpResponseException mapErrorResponseException(ErrorResponseException exception) {
        if (exception.getResponse().getStatusCode() == 404) {
            return new ResourceNotFoundException(DOCUMENT_NOT_FOUND, exception.getResponse());
        }
        return new HttpResponseException(exception.getMessage(), exception.getResponse());
    }

    private Utility() {
    }
}
