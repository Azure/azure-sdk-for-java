// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentKeyPhrases;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.toBatchStatistics;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsError;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsException;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsWarning;
import static com.azure.ai.textanalytics.implementation.Utility.toTextDocumentStatistics;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing extract key phrase endpoint.
 */
class ExtractKeyPhraseAsyncClient {
    private final ClientLogger logger = new ClientLogger(ExtractKeyPhraseAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create an {@link ExtractKeyPhraseAsyncClient} that sends requests to the Text Analytics services's extract
     * keyphrase endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    ExtractKeyPhraseAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a {@link KeyPhrasesCollection}.
     *
     * @param document A document.
     * @param language The language code.
     *
     * @return The {@link Mono} of {@link KeyPhrasesCollection} extracted key phrases strings.
     */
    Mono<KeyPhrasesCollection> extractKeyPhrasesSingleText(String document, String language) {
        try {
            Objects.requireNonNull(document, "'document' cannot be null.");
            final TextDocumentInput textDocumentInput = new TextDocumentInput("0", document);
            textDocumentInput.setLanguage(language);
            return extractKeyPhrasesWithResponse(Collections.singletonList(textDocumentInput), null)
                    .map(resultCollectionResponse -> {
                        KeyPhrasesCollection keyPhrasesCollection = null;
                        // for each loop will have only one entry inside
                        for (ExtractKeyPhraseResult keyPhraseResult : resultCollectionResponse.getValue()) {
                            if (keyPhraseResult.isError()) {
                                throw logger.logExceptionAsError(toTextAnalyticsException(keyPhraseResult.getError()));
                            }
                            keyPhrasesCollection = new KeyPhrasesCollection(keyPhraseResult.getKeyPhrases(),
                                keyPhraseResult.getKeyPhrases().getWarnings());
                        }
                        return keyPhrasesCollection;
                    });

        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters with {@link Response}.
     *
     * @param documents A list of documents to extract key phrases for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return A mono {@link Response} that contains {@link ExtractKeyPhrasesResultCollection}.
     */
    Mono<Response<ExtractKeyPhrasesResultCollection>> extractKeyPhrasesWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        try {
            inputDocumentsValidation(documents);
            return withContext(context -> getExtractedKeyPhrasesResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a {@link Response}
     * which contains {@link ExtractKeyPhrasesResultCollection}.
     *
     * @param documents A list of documents to extract key phrases for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} which contains {@link ExtractKeyPhrasesResultCollection}.
     */
    Mono<Response<ExtractKeyPhrasesResultCollection>> extractKeyPhrasesBatchWithContext(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            return getExtractedKeyPhrasesResponse(documents, options, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper method to convert the service response of {@link KeyPhraseResult} to {@link Response}
     * which contains {@link ExtractKeyPhrasesResultCollection}.
     *
     * @param response the {@link Response} returned by the service.
     *
     * @return A {@link Response} which contains {@link ExtractKeyPhrasesResultCollection}.
     */
    private Response<ExtractKeyPhrasesResultCollection> toExtractKeyPhrasesResultCollectionResponse(
        final Response<KeyPhraseResult> response) {
        final KeyPhraseResult keyPhraseResult = response.getValue();
        // List of documents results
        final List<ExtractKeyPhraseResult> keyPhraseResultList = new ArrayList<>();
        for (DocumentKeyPhrases documentKeyPhrases : keyPhraseResult.getDocuments()) {
            final String documentId = documentKeyPhrases.getId();
            keyPhraseResultList.add(new ExtractKeyPhraseResult(
                documentId,
                documentKeyPhrases.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentKeyPhrases.getStatistics()), null,
                new KeyPhrasesCollection(
                    new IterableStream<>(documentKeyPhrases.getKeyPhrases()),
                    new IterableStream<>(documentKeyPhrases.getWarnings().stream().map(
                        warning -> toTextAnalyticsWarning(warning)).collect(Collectors.toList())))));
        }
        // Document errors
        for (DocumentError documentError : keyPhraseResult.getErrors()) {
            keyPhraseResultList.add(new ExtractKeyPhraseResult(documentError.getId(), null,
                toTextAnalyticsError(documentError.getError()), null));
        }

        return new SimpleResponse<>(response,
            new ExtractKeyPhrasesResultCollection(keyPhraseResultList, keyPhraseResult.getModelVersion(),
                keyPhraseResult.getStatistics() == null ? null
                    : toBatchStatistics(keyPhraseResult.getStatistics())));
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} which contains
     * {@link ExtractKeyPhrasesResultCollection} from a {@link SimpleResponse} of {@link KeyPhraseResult}.
     *
     * @param documents A list of documents to extract key phrases for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} that contains {@link ExtractKeyPhrasesResultCollection}.
     */
    private Mono<Response<ExtractKeyPhrasesResultCollection>> getExtractedKeyPhrasesResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        options = options == null ? new TextAnalyticsRequestOptions() : options;
        return service.keyPhrasesWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options.getModelVersion(),
            options.isIncludeStatistics(),
            options.isServiceLogsDisabled(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("A batch of document with count - {}",
                getDocumentCount(documents)))
            .doOnSuccess(response -> logger.info("A batch of key phrases output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to extract key phrases - {}", error))
            .map(this::toExtractKeyPhrasesResultCollectionResponse)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }
}
