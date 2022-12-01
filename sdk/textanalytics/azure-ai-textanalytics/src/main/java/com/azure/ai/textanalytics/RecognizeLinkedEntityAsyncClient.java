// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextEntityLinkingInput;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingResult;
import com.azure.ai.textanalytics.implementation.models.EntityLinkingTaskParameters;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
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
    private final TextAnalyticsClientImpl legacyService;
    private final MicrosoftCognitiveLanguageServiceTextAnalysisImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    RecognizeLinkedEntityAsyncClient(TextAnalyticsClientImpl legacyService,
        TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = legacyService;
        this.service = null;
        this.serviceVersion = serviceVersion;
    }

    RecognizeLinkedEntityAsyncClient(MicrosoftCognitiveLanguageServiceTextAnalysisImpl service,
        TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = null;
        this.service = service;
        this.serviceVersion = serviceVersion;
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
        throwIfCallingNotAvailableFeatureInOptions(options);
        inputDocumentsValidation(documents);
        options = options == null ? new TextAnalyticsRequestOptions() : options;
        final Context finalContext = getNotNullContext(context)
            .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
        final StringIndexType finalStringIndexType = StringIndexType.UTF16CODE_UNIT;
        final String finalModelVersion = options.getModelVersion();
        final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
        final boolean finalIncludeStatistics = options.isIncludeStatistics();
        if (service != null) {
            return service.analyzeTextWithResponseAsync(
                new AnalyzeTextEntityLinkingInput()
                    .setParameters(
                        new EntityLinkingTaskParameters()
                            .setStringIndexType(finalStringIndexType)
                            .setModelVersion(finalModelVersion)
                            .setLoggingOptOut(finalLoggingOptOut))
                    .setAnalysisInput(
                        new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents))),
                finalIncludeStatistics,
                finalContext)
                .doOnSubscribe(ignoredValue -> logger.info("A batch of documents with count - {}",
                    getDocumentCount(documents)))
                .doOnSuccess(response -> logger.info("Recognized linked entities for a batch of documents - {}",
                    response.getValue()))
                .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
                .map(Utility::toRecognizeLinkedEntitiesResultCollectionResponseLanguageApi)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        }

        return legacyService.entitiesLinkingWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            finalModelVersion,
            finalIncludeStatistics,
            finalLoggingOptOut,
            finalStringIndexType,
            finalContext)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents with count - {}",
                getDocumentCount(documents)))
            .doOnSuccess(response -> logger.info("Recognized linked entities for a batch of documents - {}",
                response.getValue()))
            .doOnError(error -> logger.warning("Failed to recognize linked entities - {}", error))
            .map(Utility::toRecognizeLinkedEntitiesResultCollectionResponseLegacyApi)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }

    private void throwIfCallingNotAvailableFeatureInOptions(TextAnalyticsRequestOptions options) {
        if (options != null && options.isServiceLogsDisabled()) {
            throwIfTargetServiceVersionFound(this.serviceVersion, Arrays.asList(TextAnalyticsServiceVersion.V3_0),
                getUnsupportedServiceApiVersionMessage("TextAnalyticsRequestOptions.disableServiceLogs",
                    serviceVersion, TextAnalyticsServiceVersion.V3_1));
        }
    }
}
