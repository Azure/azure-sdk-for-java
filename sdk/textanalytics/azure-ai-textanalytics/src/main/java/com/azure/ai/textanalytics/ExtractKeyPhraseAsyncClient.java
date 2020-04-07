// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentKeyPhrases;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toMultiLanguageInput;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsError;
import static com.azure.ai.textanalytics.Transforms.toTextDocumentStatistics;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing extract key phrase endpoint.
 */
class ExtractKeyPhraseAsyncClient {
    private final ClientLogger logger = new ClientLogger(ExtractKeyPhraseAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@link ExtractKeyPhraseAsyncClient} that sends requests to the Text Analytics services's extract
     * keyphrase endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    ExtractKeyPhraseAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains extracted key phrases.
     *
     * @param document A document.
     * @param language The language code.
     *
     * @return The {@link TextAnalyticsPagedFlux} of extracted key phrases strings.
     */
    TextAnalyticsPagedFlux<String> extractKeyPhrasesSingleText(String document, String language) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        return new TextAnalyticsPagedFlux<>(() ->
            (continuationToken, pageSize) -> extractKeyPhrases(
                Collections.singletonList(new TextDocumentInput("0", document, language)), null).byPage()
                .map(resOfResult -> {
                    Iterator<ExtractKeyPhraseResult> iterator = resOfResult.getValue().iterator();
                    // Collection will never empty
                    if (!iterator.hasNext()) {
                        throw logger.logExceptionAsError(new IllegalStateException(
                            "An empty collection returned which is an unexpected error."));
                    }

                    final ExtractKeyPhraseResult keyPhraseResult = iterator.next();
                    if (keyPhraseResult.isError()) {
                        throw logger.logExceptionAsError(
                            Transforms.toTextAnalyticsException(keyPhraseResult.getError()));
                    }

                    return new TextAnalyticsPagedResponse<>(
                        resOfResult.getRequest(), resOfResult.getStatusCode(), resOfResult.getHeaders(),
                        keyPhraseResult.getKeyPhrases().stream().collect(Collectors.toList()),
                        null, resOfResult.getModelVersion(), resOfResult.getStatistics());
                }));
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link ExtractKeyPhraseResult}.
     *
     * @param documents A list of documents to extract key phrases for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link ExtractKeyPhraseResult}.
     */
    TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrases(Iterable<TextDocumentInput> documents,
        TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        final Iterator<TextDocumentInput> iterator = documents.iterator();
        if (!iterator.hasNext()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'documents' cannot be empty."));
        }

        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                getExtractedKeyPhrasesResponseInPage(documents, options, context)).flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() ->
                (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that a returns {@link TextAnalyticsPagedFlux}
     * which is a paged flux that contains {@link ExtractKeyPhraseResult}.
     *
     * @param documents A list of documents to extract key phrases for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link ExtractKeyPhraseResult}.
     */
    TextAnalyticsPagedFlux<ExtractKeyPhraseResult> extractKeyPhrasesBatchWithContext(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(documents, "'documents' cannot be null.");
        final Iterator<TextDocumentInput> iterator = documents.iterator();
        if (!iterator.hasNext()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'documents' cannot be empty."));
        }

        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            getExtractedKeyPhrasesResponseInPage(documents, options, context).flux());
    }

    /**
     * Helper method to convert the service response of {@link KeyPhraseResult} to {@link TextAnalyticsPagedResponse}
     * of {@link ExtractKeyPhraseResult}.
     *
     * @param response the {@link SimpleResponse} returned by the service.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link ExtractKeyPhraseResult} to be returned by the SDK.
     */
    private TextAnalyticsPagedResponse<ExtractKeyPhraseResult> toTextAnalyticsPagedResponse(
        final SimpleResponse<KeyPhraseResult> response) {

        final KeyPhraseResult keyPhraseResult = response.getValue();

        final List<ExtractKeyPhraseResult> keyPhraseResultList = new ArrayList<>();
        for (DocumentKeyPhrases documentKeyPhrases : keyPhraseResult.getDocuments()) {
            final String documentId = documentKeyPhrases.getId();
            keyPhraseResultList.add(new ExtractKeyPhraseResult(
                documentId,
                documentKeyPhrases.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentKeyPhrases.getStatistics()), null,
                new IterableStream<>(documentKeyPhrases.getKeyPhrases())));
        }

        for (DocumentError documentError : keyPhraseResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error =
                toTextAnalyticsError(documentError.getError());

            final String documentId = documentError.getId();

            keyPhraseResultList.add(new ExtractKeyPhraseResult(
                documentId, null, error, null));
        }

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            keyPhraseResultList,
            null,
            keyPhraseResult.getModelVersion(), keyPhraseResult.getStatistics() == null ? null
            : toBatchStatistics(keyPhraseResult.getStatistics()));
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link TextAnalyticsPagedResponse} of
     * {@link ExtractKeyPhraseResult} from a {@link SimpleResponse} of {@link KeyPhraseResult}.
     *
     * @param documents A list of documents to extract key phrases for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Mono} of {@link TextAnalyticsPagedResponse} of {@link ExtractKeyPhraseResult}.
     */
    private Mono<TextAnalyticsPagedResponse<ExtractKeyPhraseResult>> getExtractedKeyPhrasesResponseInPage(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        return service.keyPhrasesWithRestResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.isIncludeStatistics(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of document - {}", documents.toString()))
            .doOnSuccess(response -> logger.info("A batch of key phrases output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to extract key phrases - {}", error))
            .map(this::toTextAnalyticsPagedResponse);
    }
}
