// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.models.LinkedEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Objects;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsException;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing recognize linked entity endpoint.
 */
class RecognizeLinkedEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(RecognizeLinkedEntityAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@link RecognizeLinkedEntityAsyncClient} that sends requests to the Text Analytics services's recognize
     * linked entity endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    RecognizeLinkedEntityAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a {@link LinkedEntityCollection}.
     *
     * @param document A single document.
     * @param language The language code.
     *
     * @return The {@link Mono} of {@link LinkedEntityCollection}.
     */
    Mono<LinkedEntityCollection> recognizeLinkedEntities(String document, String language) {
        try {
            Objects.requireNonNull(document, "'document' cannot be null.");
            final TextDocumentInput textDocumentInput = new TextDocumentInput("0", document);
            textDocumentInput.setLanguage(language);
            return recognizeLinkedEntitiesBatch(Collections.singletonList(textDocumentInput), null)
                .map(resultCollectionResponse -> {
                    LinkedEntityCollection linkedEntityCollection = null;
                    // for each loop will have only one entry inside
                    for (RecognizeLinkedEntitiesResult entitiesResult : resultCollectionResponse.getValue()) {
                        if (entitiesResult.isError()) {
                            throw logger.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
                        }
                        linkedEntityCollection = new LinkedEntityCollection(entitiesResult.getEntities(),
                            entitiesResult.getEntities().getWarnings());
                    }
                    return linkedEntityCollection;
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a mono {@link Response}
     * which contains {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @param documents The list of documents to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return A mono {@link Response} that contains {@link RecognizeLinkedEntitiesResultCollection}.
     */
    Mono<Response<RecognizeLinkedEntitiesResultCollection>> recognizeLinkedEntitiesBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        try {
            inputDocumentsValidation(documents);
            return withContext(context -> getRecognizedLinkedEntitiesResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a mono {@link Response}
     * which contains {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @param documents The list of documents to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} that contains {@link RecognizeLinkedEntitiesResultCollection}.
     */
    Mono<Response<RecognizeLinkedEntitiesResultCollection>>
        recognizeLinkedEntitiesBatchWithContext(Iterable<TextDocumentInput> documents,
            TextAnalyticsRequestOptions options, Context context) {
        try {
            inputDocumentsValidation(documents);
            return getRecognizedLinkedEntitiesResponse(documents, options, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} which contains
     * {@link RecognizeLinkedEntitiesResultCollection} from a {@link SimpleResponse} of {@link EntityLinkingResult}.
     *
     * @param documents The list of documents to recognize linked entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A mono {@link Response} that contains {@link RecognizeLinkedEntitiesResultCollection}.
     */
    private Mono<Response<RecognizeLinkedEntitiesResultCollection>> getRecognizedLinkedEntitiesResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        options = options == null ? new TextAnalyticsRequestOptions() : options;
        return service.entitiesLinkingWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options.getModelVersion(),
            options.isIncludeStatistics(),
            options.isServiceLogsDisabled(),
            StringIndexType.UTF16CODE_UNIT,
            getNotNullContext(context).addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                   .doOnSubscribe(ignoredValue -> logger.info("A batch of documents with count - {}",
                       getDocumentCount(documents)))
                   .doOnSuccess(response -> logger.info("Recognized linked entities for a batch of documents - {}",
                       response.getValue()))
                   .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
                   .map(Utility::toRecognizeLinkedEntitiesResultCollectionResponse)
                   .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }
}
