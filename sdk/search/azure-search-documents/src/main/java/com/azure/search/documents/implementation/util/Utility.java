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
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.TypeReference;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.SearchIndexClientImpl;
import com.azure.search.documents.implementation.SearchIndexClientImplBuilder;
import com.azure.search.documents.implementation.converters.IndexDocumentsResultConverter;
import com.azure.search.documents.implementation.models.IndexBatch;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
    // Type reference that used across many places. Have one copy here to minimize the memory.
    public static final TypeReference<Map<String, Object>> MAP_STRING_OBJECT_TYPE_REFERENCE =
        new TypeReference<Map<String, Object>>() {
        };

    private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();
    private static final HttpLogOptions DEFAULT_LOG_OPTIONS = Constants.DEFAULT_LOG_OPTIONS_SUPPLIER.get();
    private static final HttpHeaders HTTP_HEADERS = new HttpHeaders().set("return-client-request-id", "true");

    private static final DecimalFormat COORDINATE_FORMATTER = new DecimalFormat();

    private static final JacksonAdapter DEFAULT_SERIALIZER_ADAPTER;

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

        JacksonAdapter adapter = new JacksonAdapter();

        UntypedObjectDeserializer defaultDeserializer = new UntypedObjectDeserializer(null, null);
        Iso8601DateDeserializer iso8601DateDeserializer = new Iso8601DateDeserializer(defaultDeserializer);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Object.class, iso8601DateDeserializer);

        adapter.serializer()
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .registerModule(Iso8601DateSerializer.getModule())
            .registerModule(module);

        DEFAULT_SERIALIZER_ADAPTER = adapter;
    }

    public static JacksonAdapter getDefaultSerializerAdapter() {
        return DEFAULT_SERIALIZER_ADAPTER;
    }

    public static <T> T convertValue(Object initialValue, Class<T> newValueType) throws IOException {
        return DEFAULT_SERIALIZER_ADAPTER.serializer().convertValue(initialValue, newValueType);
    }

    public static HttpPipeline buildHttpPipeline(ClientOptions clientOptions, HttpLogOptions logOptions,
        Configuration configuration, RetryPolicy retryPolicy, AzureKeyCredential azureKeyCredential,
        TokenCredential tokenCredential, List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies, HttpClient httpClient, ClientLogger logger) {
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
        httpPipelinePolicies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);

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
                    ? Mono.error(new IndexBatchException(IndexDocumentsResultConverter.map(response.getValue())))
                    : Mono.just(response).map(MappingUtils::mappingIndexDocumentResultResponse));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public static SearchIndexClientImpl buildRestClient(SearchServiceVersion serviceVersion, String endpoint,
        String indexName, HttpPipeline httpPipeline, SerializerAdapter adapter) {
        return new SearchIndexClientImplBuilder()
            .apiVersion(serviceVersion.getVersion())
            .endpoint(endpoint)
            .indexName(indexName)
            .pipeline(httpPipeline)
            .serializerAdapter(adapter)
            .buildClient();
    }

    public static synchronized String formatCoordinate(double coordinate) {
        return COORDINATE_FORMATTER.format(coordinate);
    }

    public static String readSynonymsFromFile(Path filePath) {
        try {
            return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new ClientLogger(Utility.class).logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    private Utility() {
    }
}
