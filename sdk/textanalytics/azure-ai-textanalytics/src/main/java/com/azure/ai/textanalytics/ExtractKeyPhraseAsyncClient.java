// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentKeyPhrases;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.azure.ai.textanalytics.Transforms.mapByIndex;
import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toMultiLanguageInput;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsError;
import static com.azure.ai.textanalytics.Transforms.toTextDocumentStatistics;

/**
 * Helper class for managing extract keyphrase endpoint.
 */
class ExtractKeyPhraseAsyncClient {
    private final ClientLogger logger = new ClientLogger(ExtractKeyPhraseAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@code ExtractKeyPhraseAsyncClient} that sends requests to the Text Analytics services's extract
     * keyphrase endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    ExtractKeyPhraseAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    Mono<PagedResponse<String>> extractKeyPhrasesWithResponse(String text, String language, Context context) {
        Objects.requireNonNull(text, "'text' cannot be null.");

        return extractKeyPhrasesBatchWithResponse(
            Collections.singletonList(new TextDocumentInput("0", text, language)), null, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                Transforms.processSingleResponseErrorResult(response).getValue().getKeyPhrases(),
                null,
                null
            ));
    }

    PagedFlux<String> extractKeyPhrases(String text, String language, Context context) {
        return new PagedFlux<>(() -> extractKeyPhrasesWithResponse(text, language, context));
    }

    Mono<Response<DocumentResultCollection<ExtractKeyPhraseResult>>> extractKeyPhrasesWithResponse(
        List<String> textInputs, String language,  TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        return extractKeyPhrasesBatchWithResponse(documentInputs, options, context);
    }

    Mono<Response<DocumentResultCollection<ExtractKeyPhraseResult>>> extractKeyPhrasesBatchWithResponse(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
            .setDocuments(toMultiLanguageInput(textInputs));
        return service.keyPhrasesWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of key phrases input - {}", textInputs.toString()))
            .doOnSuccess(response -> logger.info("A batch of key phrases output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to extract key phrases - {}", error))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    /**
     * Helper method to convert the service response of {@link KeyPhraseResult} to {@link DocumentResultCollection}.
     *
     * @param keyPhraseResult the {@link KeyPhraseResult} returned by the service.
     *
     * @return the {@link DocumentResultCollection} of {@link KeyPhraseResult} to be returned by the SDK.
     */
    private DocumentResultCollection<ExtractKeyPhraseResult> toDocumentResultCollection(
        final KeyPhraseResult keyPhraseResult) {
        List<ExtractKeyPhraseResult> keyPhraseResultList = new ArrayList<>();
        for (DocumentKeyPhrases documentKeyPhrases : keyPhraseResult.getDocuments()) {
            keyPhraseResultList.add(new ExtractKeyPhraseResult(documentKeyPhrases.getId(),
                documentKeyPhrases.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentKeyPhrases.getStatistics()), null,
                documentKeyPhrases.getKeyPhrases()));
        }

        for (DocumentError documentError : keyPhraseResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error =
                toTextAnalyticsError(documentError.getError());
            keyPhraseResultList.add(new ExtractKeyPhraseResult(documentError.getId(), null, error, null));
        }

        return new DocumentResultCollection<>(keyPhraseResultList,
            keyPhraseResult.getModelVersion(), keyPhraseResult.getStatistics() == null ? null
            : toBatchStatistics(keyPhraseResult.getStatistics()));
    }
}
