// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextEntityRecognitionInput;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.EntitiesTaskParameters;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.enableSyncRestProxy;
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapToHttpResponseExceptionIfExists;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeEntitiesResultCollectionResponseLanguageApi;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizeEntitiesResultCollectionResponseLegacyApi;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsException;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing recognize entity endpoint.
 */
class RecognizeEntityUtilClient {
    private static final ClientLogger LOGGER = new ClientLogger(RecognizeEntityUtilClient.class);
    private final TextAnalyticsClientImpl legacyService;
    private final MicrosoftCognitiveLanguageServiceTextAnalysisImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    RecognizeEntityUtilClient(TextAnalyticsClientImpl legacyService, TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = legacyService;
        this.service = null;
        this.serviceVersion = serviceVersion;
    }

    RecognizeEntityUtilClient(MicrosoftCognitiveLanguageServiceTextAnalysisImpl service,
        TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = null;
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a {@link Mono}
     * which contains {@link CategorizedEntityCollection}.
     *
     * @param document A single document.
     * @param language The language code.
     *
     * @return The {@link Mono} of {@link CategorizedEntityCollection}.
     */
    Mono<CategorizedEntityCollection> recognizeEntities(String document, String language) {
        try {
            Objects.requireNonNull(document, "'document' cannot be null.");
            final TextDocumentInput textDocumentInput = new TextDocumentInput("0", document);
            textDocumentInput.setLanguage(language);
            return recognizeEntitiesBatch(Collections.singletonList(textDocumentInput), null)
                .map(resultCollectionResponse -> {
                    CategorizedEntityCollection entityCollection = null;
                    // for each loop will have only one entry inside
                    for (RecognizeEntitiesResult entitiesResult : resultCollectionResponse.getValue()) {
                        if (entitiesResult.isError()) {
                            throw LOGGER.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
                        }
                        entityCollection = new CategorizedEntityCollection(entitiesResult.getEntities(),
                            entitiesResult.getEntities().getWarnings());
                    }
                    return entityCollection;
                });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Helper function for calling service with max overloaded parameters.
     *
     * @param documents The list of documents to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     *
     * @return A mono {@link Response} that contains {@link RecognizeEntitiesResultCollection}.
     */
    Mono<Response<RecognizeEntitiesResultCollection>> recognizeEntitiesBatch(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        try {
            return withContext(context -> getRecognizedEntitiesResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} that contains
     * {@link RecognizeEntitiesResultCollection} from a {@link SimpleResponse} of {@link EntitiesResult}.
     *
     * @param documents The list of documents to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} that contains {@link RecognizeEntitiesResultCollection}.
     */
    private Mono<Response<RecognizeEntitiesResultCollection>> getRecognizedEntitiesResponse(
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
                new AnalyzeTextEntityRecognitionInput()
                    .setParameters(
                        new EntitiesTaskParameters()
                            .setStringIndexType(finalStringIndexType)
                            .setModelVersion(finalModelVersion)
                            .setLoggingOptOut(finalLoggingOptOut))
                    .setAnalysisInput(new MultiLanguageAnalysisInput()
                        .setDocuments(toMultiLanguageInput(documents))),
                finalIncludeStatistics,
                finalContext)
                .doOnSubscribe(ignoredValue -> LOGGER.info("A batch of documents with count - {}",
                    getDocumentCount(documents)))
                .doOnSuccess(response -> LOGGER.info("Recognized entities for a batch of documents- {}",
                    response.getValue()))
                .doOnError(error -> LOGGER.warning("Failed to recognize entities - {}", error))
                .map(Utility::toRecognizeEntitiesResultCollectionResponseLanguageApi)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        }

        return legacyService.entitiesRecognitionGeneralWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            finalModelVersion,
            finalIncludeStatistics,
            finalLoggingOptOut,
            finalStringIndexType,
            finalContext)
            .doOnSubscribe(ignoredValue -> LOGGER.info("A batch of documents with count - {}",
                getDocumentCount(documents)))
            .doOnSuccess(response -> LOGGER.info("Recognized entities for a batch of documents- {}",
                response.getValue()))
            .doOnError(error -> LOGGER.warning("Failed to recognize entities - {}", error))
            .map(Utility::toRecognizeEntitiesResultCollectionResponseLegacyApi)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }

    /**
     * Call the service with REST response, convert to a {@link Response} that contains
     * {@link RecognizeEntitiesResultCollection} from a {@link SimpleResponse} of {@link EntitiesResult}.
     *
     * @param documents The list of documents to recognize entities for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains {@link RecognizeEntitiesResultCollection}.
     */
    Response<RecognizeEntitiesResultCollection> getRecognizedEntitiesResponseSync(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        throwIfCallingNotAvailableFeatureInOptions(options);
        inputDocumentsValidation(documents);
        options = options == null ? new TextAnalyticsRequestOptions() : options;
        final Context finalContext = enableSyncRestProxy(getNotNullContext(context))
            .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
        final StringIndexType finalStringIndexType = StringIndexType.UTF16CODE_UNIT;
        final String finalModelVersion = options.getModelVersion();
        final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
        final boolean finalIncludeStatistics = options.isIncludeStatistics();
        try {
            return (service != null)
                ? toRecognizeEntitiesResultCollectionResponseLanguageApi(service.analyzeTextWithResponse(
                    new AnalyzeTextEntityRecognitionInput()
                        .setParameters(
                            new EntitiesTaskParameters()
                                .setStringIndexType(finalStringIndexType)
                                .setModelVersion(finalModelVersion)
                                .setLoggingOptOut(finalLoggingOptOut))
                        .setAnalysisInput(new MultiLanguageAnalysisInput()
                            .setDocuments(toMultiLanguageInput(documents))),
                    finalIncludeStatistics,
                    finalContext))
                : toRecognizeEntitiesResultCollectionResponseLegacyApi(
                    legacyService.entitiesRecognitionGeneralWithResponseSync(
                        new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                        finalModelVersion,
                        finalIncludeStatistics,
                        finalLoggingOptOut,
                        finalStringIndexType,
                        finalContext));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError((HttpResponseException) mapToHttpResponseExceptionIfExists(ex));
        }
    }

    private void throwIfCallingNotAvailableFeatureInOptions(TextAnalyticsRequestOptions options) {
        if (options != null && options.isServiceLogsDisabled()) {
            throwIfTargetServiceVersionFound(this.serviceVersion, Arrays.asList(TextAnalyticsServiceVersion.V3_0),
                getUnsupportedServiceApiVersionMessage("TextAnalyticsRequestOptions.disableServiceLogs",
                    serviceVersion, TextAnalyticsServiceVersion.V3_1));
        }
    }
}
