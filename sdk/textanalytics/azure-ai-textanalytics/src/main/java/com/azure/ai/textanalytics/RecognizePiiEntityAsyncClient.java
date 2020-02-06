// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentEntities;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.PiiEntity;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.Transforms.mapByIndex;
import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toMultiLanguageInput;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsError;
import static com.azure.ai.textanalytics.Transforms.toTextDocumentStatistics;

/**
 * Helper class for managing recognize PII entity endpoint.
 */
class RecognizePiiEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(RecognizePiiEntityAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@code RecognizePiiEntityAsyncClient} that sends requests to the Text Analytics services's
     * recognize PII entity endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    RecognizePiiEntityAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    Mono<Response<RecognizePiiEntitiesResult>> recognizePiiEntitiesWithResponse(String text, String language,
        Context context) {
        Objects.requireNonNull(text, "'text' cannot be null.");

        return recognizeBatchPiiEntitiesWithResponse(
            Collections.singletonList(new TextDocumentInput("0", text, language)), null, context)
            .map(Transforms::processSingleResponseErrorResult);
    }

    Mono<Response<DocumentResultCollection<RecognizePiiEntitiesResult>>> recognizePiiEntitiesWithResponse(
        List<String> textInputs, String language, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        return recognizeBatchPiiEntitiesWithResponse(documentInputs, null, context);
    }

    Mono<Response<DocumentResultCollection<RecognizePiiEntitiesResult>>> recognizeBatchPiiEntitiesWithResponse(
        List<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
            .setDocuments(toMultiLanguageInput(textInputs));
        return service.entitiesRecognitionPiiWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("Processing a batch of PII entities input"))
            .doOnSuccess(response -> logger.info("A batch of PII entities output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to recognize PII entities - {}", error))
            .map(response -> new SimpleResponse<>(response, toPiiDocumentResultCollection(response.getValue())));
    }

    /**
     * Helper method to convert the service response of {@link EntitiesResult} to {@link DocumentResultCollection}.
     *
     * @param entitiesResult the {@link EntitiesResult} returned by the service.
     *
     * @return the {@link DocumentResultCollection} of {@link RecognizePiiEntitiesResult} to be returned by the SDK.
     */
    private DocumentResultCollection<RecognizePiiEntitiesResult> toPiiDocumentResultCollection(
        final EntitiesResult entitiesResult) {
        List<RecognizePiiEntitiesResult> recognizePiiEntitiesResults = new ArrayList<>();
        for (DocumentEntities documentEntities : entitiesResult.getDocuments()) {
            recognizePiiEntitiesResults.add(new RecognizePiiEntitiesResult(documentEntities.getId(),
                documentEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentEntities.getStatistics()),
                null, documentEntities.getEntities().stream().map(entity ->
                new PiiEntity(entity.getText(), entity.getType(), entity.getSubtype(), entity.getOffset(),
                    entity.getLength(), entity.getScore())).collect(Collectors.toList())));
        }

        for (DocumentError documentError : entitiesResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error =
                toTextAnalyticsError(documentError.getError());
            recognizePiiEntitiesResults.add(new RecognizePiiEntitiesResult(documentError.getId(), null, error, null));
        }

        return new DocumentResultCollection<>(recognizePiiEntitiesResults,
            entitiesResult.getModelVersion(), entitiesResult.getStatistics() == null ? null
            : toBatchStatistics(entitiesResult.getStatistics()));
    }
}
