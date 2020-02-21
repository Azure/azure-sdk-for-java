// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentLinkedEntities;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.LinkedEntity;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.LinkedEntityMatch;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
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
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.Transforms.mapByIndex;
import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.ai.textanalytics.Transforms.toMultiLanguageInput;
import static com.azure.ai.textanalytics.Transforms.toTextAnalyticsError;
import static com.azure.ai.textanalytics.Transforms.toTextDocumentStatistics;

/**
 * Helper class for managing recognize linked entity endpoint.
 */
class RecognizeLinkedEntityAsyncClient {
    private final ClientLogger logger = new ClientLogger(RecognizeLinkedEntityAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@code RecognizeLinkedEntityAsyncClient} that sends requests to the Text Analytics services's recognize
     * linked entity endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    RecognizeLinkedEntityAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    Mono<PagedResponse<com.azure.ai.textanalytics.models.LinkedEntity>> recognizeLinkedEntitiesWithResponse(
        String text, String language, Context context) {
        Objects.requireNonNull(text, "'text' cannot be null.");

        return recognizeLinkedEntitiesBatchWithResponse(
            Collections.singletonList(new TextDocumentInput("0", text, language)), null, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                Transforms.processSingleResponseErrorResult(response).getValue().getEntities(),
                null,
                null
            ));
    }

    PagedFlux<com.azure.ai.textanalytics.models.LinkedEntity> recognizeLinkedEntities(
        String text, String language, Context context) {
        return new PagedFlux<>(() -> recognizeLinkedEntitiesWithResponse(text, language, context));
    }

    Mono<Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>>> recognizeLinkedEntitiesWithResponse(
        List<String> textInputs, String language, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        List<TextDocumentInput> documentInputs = mapByIndex(textInputs, (index, value) ->
            new TextDocumentInput(index, value, language));
        return recognizeLinkedEntitiesBatchWithResponse(documentInputs, options, context);
    }

    Mono<Response<DocumentResultCollection<RecognizeLinkedEntitiesResult>>> recognizeLinkedEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        final MultiLanguageBatchInput batchInput = new MultiLanguageBatchInput()
            .setDocuments(toMultiLanguageInput(textInputs));
        return service.entitiesLinkingWithRestResponseAsync(
            batchInput,
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of linked entities input - {}", textInputs.toString()))
            .doOnSuccess(response -> logger.info("A batch of linked entities output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }


    private List<com.azure.ai.textanalytics.models.LinkedEntity> mapLinkedEntity(List<LinkedEntity> linkedEntities) {
        List<com.azure.ai.textanalytics.models.LinkedEntity> linkedEntitiesList = new ArrayList<>();
        for (LinkedEntity linkedEntity : linkedEntities) {
            linkedEntitiesList.add(new com.azure.ai.textanalytics.models.LinkedEntity(linkedEntity.getName(),
                linkedEntity.getMatches().stream().map(match ->
                    new LinkedEntityMatch(match.getText(), match.getScore(), match.getLength(),
                        match.getOffset())).collect(Collectors.toList()), linkedEntity.getLanguage(),
                linkedEntity.getId(), linkedEntity.getUrl(), linkedEntity.getDataSource()));
        }
        return linkedEntitiesList;
    }

    /**
     * Helper method to convert the service response of {@link EntityLinkingResult} to {@link DocumentResultCollection}.
     *
     * @param entityLinkingResult the {@link EntityLinkingResult} returned by the service.
     *
     * @return the {@link DocumentResultCollection} of {@link RecognizeLinkedEntitiesResult} to be returned by the SDK.
     */
    private DocumentResultCollection<RecognizeLinkedEntitiesResult> toDocumentResultCollection(
        final EntityLinkingResult entityLinkingResult) {
        List<RecognizeLinkedEntitiesResult> linkedEntitiesResults = new ArrayList<>();
        for (DocumentLinkedEntities documentLinkedEntities : entityLinkingResult.getDocuments()) {
            linkedEntitiesResults.add(new RecognizeLinkedEntitiesResult(documentLinkedEntities.getId(),
                documentLinkedEntities.getStatistics() == null ? null
                    : toTextDocumentStatistics(documentLinkedEntities.getStatistics()),
                null, mapLinkedEntity(documentLinkedEntities.getEntities())));
        }
        for (DocumentError documentError : entityLinkingResult.getErrors()) {
            final com.azure.ai.textanalytics.models.TextAnalyticsError error =
                toTextAnalyticsError(documentError.getError());
            linkedEntitiesResults.add(new RecognizeLinkedEntitiesResult(documentError.getId(), null, error, null));
        }

        return new DocumentResultCollection<>(linkedEntitiesResults,
            entityLinkingResult.getModelVersion(), entityLinkingResult.getStatistics() == null ? null
            : toBatchStatistics(entityLinkingResult.getStatistics()));
    }
}
