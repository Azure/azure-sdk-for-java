// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextPiiEntitiesRecognitionInput;
import com.azure.ai.textanalytics.implementation.models.EntitiesResult;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.PiiDomain;
import com.azure.ai.textanalytics.implementation.models.PiiTaskParameters;
import com.azure.ai.textanalytics.implementation.models.StringIndexType;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
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
import static com.azure.ai.textanalytics.implementation.Utility.HTTP_REST_PROXY_SYNC_PROXY_ENABLE;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapToHttpResponseExceptionIfExists;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static com.azure.ai.textanalytics.implementation.Utility.toCategoriesFilter;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizePiiEntitiesResultCollectionResponseLanguageApi;
import static com.azure.ai.textanalytics.implementation.Utility.toRecognizePiiEntitiesResultCollectionResponseLegacyApi;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsException;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing recognize Personally Identifiable Information entity endpoint.
 */
class RecognizePiiEntityUtilClient {
    private static final ClientLogger LOGGER = new ClientLogger(RecognizePiiEntityUtilClient.class);
    private final TextAnalyticsClientImpl legacyService;
    private final MicrosoftCognitiveLanguageServiceTextAnalysisImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    RecognizePiiEntityUtilClient(TextAnalyticsClientImpl legacyService, TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = legacyService;
        this.service = null;
        this.serviceVersion = serviceVersion;
    }

    RecognizePiiEntityUtilClient(MicrosoftCognitiveLanguageServiceTextAnalysisImpl service,
        TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = null;
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Helper function for calling service with max overloaded parameters that returns a {@link Mono}
     * which contains {@link PiiEntityCollection}.
     *
     * @param document A single document.
     * @param language The language code.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return The {@link Mono} of {@link PiiEntityCollection}.
     */
    Mono<PiiEntityCollection> recognizePiiEntities(String document, String language,
        RecognizePiiEntitiesOptions options) {
        try {
            throwIfTargetServiceVersionFound(this.serviceVersion,
                Arrays.asList(TextAnalyticsServiceVersion.V3_0),
                getUnsupportedServiceApiVersionMessage("recognizePiiEntitiesBatch", serviceVersion,
                    TextAnalyticsServiceVersion.V3_1));
            Objects.requireNonNull(document, "'document' cannot be null.");
            return recognizePiiEntitiesBatch(
                Collections.singletonList(new TextDocumentInput("0", document).setLanguage(language)), options)
                .map(resultCollectionResponse -> {
                    PiiEntityCollection entityCollection = null;
                    // for each loop will have only one entry inside
                    for (RecognizePiiEntitiesResult entitiesResult : resultCollectionResponse.getValue()) {
                        if (entitiesResult.isError()) {
                            throw LOGGER.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
                        }
                        entityCollection = new PiiEntityCollection(entitiesResult.getEntities(),
                            entitiesResult.getEntities().getRedactedText(),
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
     * @param documents The list of documents to recognize Personally Identifiable Information entities for.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return A mono {@link Response} that contains {@link RecognizePiiEntitiesResultCollection}.
     */
    Mono<Response<RecognizePiiEntitiesResultCollection>> recognizePiiEntitiesBatch(
        Iterable<TextDocumentInput> documents, RecognizePiiEntitiesOptions options) {
        try {
            return withContext(context -> getRecognizePiiEntitiesResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Call the service with REST response, convert to a {@link Mono} of {@link Response} that contains
     * {@link RecognizePiiEntitiesResultCollection} from a {@link SimpleResponse} of {@link EntitiesResult}.
     *
     * @param documents The list of documents to recognize Personally Identifiable Information entities for.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A mono {@link Response} that contains {@link RecognizePiiEntitiesResultCollection}.
     *
     */
    private Mono<Response<RecognizePiiEntitiesResultCollection>> getRecognizePiiEntitiesResponse(
        Iterable<TextDocumentInput> documents, RecognizePiiEntitiesOptions options, Context context) {
        throwIfTargetServiceVersionFound(this.serviceVersion,
            Arrays.asList(TextAnalyticsServiceVersion.V3_0),
            getUnsupportedServiceApiVersionMessage("recognizePiiEntitiesBatch", serviceVersion,
                TextAnalyticsServiceVersion.V3_1));
        inputDocumentsValidation(documents);
        options = options == null ? new RecognizePiiEntitiesOptions() : options;
        final Context finalContext = getNotNullContext(context)
            .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
        final StringIndexType finalStringIndexType = StringIndexType.UTF16CODE_UNIT;
        final String finalModelVersion = options.getModelVersion();
        final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
        final boolean finalIncludeStatistics = options.isIncludeStatistics();

        final String finalDomainFilter = options.getDomainFilter() != null
            ? options.getDomainFilter().toString() : null;
        if (service != null) {
            return service.analyzeTextWithResponseAsync(
                new AnalyzeTextPiiEntitiesRecognitionInput()
                    .setParameters(
                        new PiiTaskParameters()
                            .setDomain(PiiDomain.fromString(finalDomainFilter))
                            .setPiiCategories(
                                toCategoriesFilter(options.getCategoriesFilter()))
                            .setStringIndexType(finalStringIndexType)
                            .setModelVersion(finalModelVersion)
                            .setLoggingOptOut(finalLoggingOptOut))
                    .setAnalysisInput(new MultiLanguageAnalysisInput()
                        .setDocuments(toMultiLanguageInput(documents))),
                finalIncludeStatistics,
                finalContext)
                .doOnSubscribe(ignoredValue -> LOGGER.info(
                    "Start recognizing Personally Identifiable Information entities for a batch of documents."))
                .doOnSuccess(response -> LOGGER.info("Successfully recognized Personally Identifiable Information "
                    + "entities for a batch of documents."))
                .doOnError(error -> LOGGER.warning(
                    "Failed to recognize Personally Identifiable Information entities - {}", error))
                .map(Utility::toRecognizePiiEntitiesResultCollectionResponseLanguageApi)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        }

        return legacyService.entitiesRecognitionPiiWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            finalModelVersion,
            finalIncludeStatistics,
            finalLoggingOptOut,
            finalDomainFilter,
            finalStringIndexType,
            toCategoriesFilter(options.getCategoriesFilter()),
            finalContext)
            .doOnSubscribe(ignoredValue -> LOGGER.info(
                "Start recognizing Personally Identifiable Information entities for a batch of documents."))
            .doOnSuccess(response -> LOGGER.info(
                "Successfully recognized Personally Identifiable Information entities for a batch of documents."))
            .doOnError(error ->
                LOGGER.warning("Failed to recognize Personally Identifiable Information entities - {}", error))
            .map(Utility::toRecognizePiiEntitiesResultCollectionResponseLegacyApi)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }

    /**
     * Call the service with REST response, convert to a {@link Response} that contains
     * {@link RecognizePiiEntitiesResultCollection} from a {@link SimpleResponse} of {@link EntitiesResult}.
     *
     * @param documents The list of documents to recognize Personally Identifiable Information entities for.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains {@link RecognizePiiEntitiesResultCollection}.
     *
     */
    Response<RecognizePiiEntitiesResultCollection> getRecognizePiiEntitiesResponseSync(
        Iterable<TextDocumentInput> documents, RecognizePiiEntitiesOptions options, Context context) {
        throwIfTargetServiceVersionFound(this.serviceVersion,
            Arrays.asList(TextAnalyticsServiceVersion.V3_0),
            getUnsupportedServiceApiVersionMessage("recognizePiiEntitiesBatch", serviceVersion,
                TextAnalyticsServiceVersion.V3_1));
        inputDocumentsValidation(documents);
        options = options == null ? new RecognizePiiEntitiesOptions() : options;
        final Context finalContext = enableSyncRestProxy(getNotNullContext(context))
            .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
        final StringIndexType finalStringIndexType = StringIndexType.UTF16CODE_UNIT;
        final String finalModelVersion = options.getModelVersion();
        final boolean finalLoggingOptOut = options.isServiceLogsDisabled();
        final boolean finalIncludeStatistics = options.isIncludeStatistics();

        final String finalDomainFilter = options.getDomainFilter() != null
            ? options.getDomainFilter().toString() : null;
        try {
            return (service != null)
                ? toRecognizePiiEntitiesResultCollectionResponseLanguageApi(
                    service.analyzeTextWithResponse(
                        new AnalyzeTextPiiEntitiesRecognitionInput()
                            .setParameters(
                                new PiiTaskParameters()
                                    .setDomain(PiiDomain.fromString(finalDomainFilter))
                                    .setPiiCategories(
                                        toCategoriesFilter(options.getCategoriesFilter()))
                                    .setStringIndexType(finalStringIndexType)
                                    .setModelVersion(finalModelVersion)
                                    .setLoggingOptOut(finalLoggingOptOut))
                            .setAnalysisInput(new MultiLanguageAnalysisInput()
                                .setDocuments(toMultiLanguageInput(documents))),
                        finalIncludeStatistics,
                        finalContext))
                : toRecognizePiiEntitiesResultCollectionResponseLegacyApi(
                    legacyService.entitiesRecognitionPiiWithResponseSync(
                        new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                        finalModelVersion,
                        finalIncludeStatistics,
                        finalLoggingOptOut,
                        finalDomainFilter,
                        finalStringIndexType,
                        toCategoriesFilter(options.getCategoriesFilter()),
                        finalContext));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError((HttpResponseException) mapToHttpResponseExceptionIfExists(ex));
        }
    }

    private Context enableSyncRestProxy(Context context) {
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }
}
