// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextKeyPhraseExtractionInput;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseResult;
import com.azure.ai.textanalytics.implementation.models.KeyPhraseTaskParameters;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageBatchInput;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
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
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapToHttpResponseExceptionIfExists;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.ai.textanalytics.implementation.Utility.toResultCollectionResponseLanguageApi;
import static com.azure.ai.textanalytics.implementation.Utility.toResultCollectionResponseLegacyApi;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsException;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing extract key phrase endpoint.
 */
class ExtractKeyPhraseUtilClient {
    private static final ClientLogger LOGGER = new ClientLogger(ExtractKeyPhraseUtilClient.class);
    private final TextAnalyticsClientImpl legacyService;
    private final MicrosoftCognitiveLanguageServiceTextAnalysisImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    ExtractKeyPhraseUtilClient(TextAnalyticsClientImpl legacyService, TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = legacyService;
        this.service = null;
        this.serviceVersion = serviceVersion;
    }

    ExtractKeyPhraseUtilClient(MicrosoftCognitiveLanguageServiceTextAnalysisImpl service,
        TextAnalyticsServiceVersion serviceVersion) {
        this.legacyService = null;
        this.service = service;
        this.serviceVersion = serviceVersion;
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
                                throw LOGGER.logExceptionAsError(toTextAnalyticsException(keyPhraseResult.getError()));
                            }
                            keyPhrasesCollection = new KeyPhrasesCollection(keyPhraseResult.getKeyPhrases(),
                                keyPhraseResult.getKeyPhrases().getWarnings());
                        }
                        return keyPhrasesCollection;
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
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
            return withContext(context -> getExtractedKeyPhrasesResponse(documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
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
        throwIfCallingNotAvailableFeatureInOptions(options);
        inputDocumentsValidation(documents);
        options = options == null ? new TextAnalyticsRequestOptions() : options;

        if (service != null) {
            return service.analyzeTextWithResponseAsync(
                new AnalyzeTextKeyPhraseExtractionInput()
                    .setParameters(
                        new KeyPhraseTaskParameters()
                            .setModelVersion(options.getModelVersion())
                            .setLoggingOptOut(options.isServiceLogsDisabled()))
                    .setAnalysisInput(
                        new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents))),
                options.isIncludeStatistics(),
                getNotNullContext(context)
                    .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
                .doOnSubscribe(ignoredValue -> LOGGER.info("A batch of documents with count - {}",
                    getDocumentCount(documents)))
                .doOnSuccess(response -> LOGGER.info("A batch of key phrases output - {}", response.getValue()))
                .doOnError(error -> LOGGER.warning("Failed to extract key phrases - {}", error))
                .map(Utility::toResultCollectionResponseLanguageApi)
                .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
        }

        return legacyService.keyPhrasesWithResponseAsync(
            new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
            options.getModelVersion(),
            options.isIncludeStatistics(),
            options.isServiceLogsDisabled(),
            getNotNullContext(context).addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> LOGGER.info("A batch of document with count - {}",
                getDocumentCount(documents)))
            .doOnSuccess(response -> LOGGER.info("A batch of key phrases output - {}", response.getValue()))
            .doOnError(error -> LOGGER.warning("Failed to extract key phrases - {}", error))
            .map(Utility::toResultCollectionResponseLegacyApi)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }

    /**
     * Call the service with REST response, convert to a {@link Response} which contains
     * {@link ExtractKeyPhrasesResultCollection} from a {@link SimpleResponse} of {@link KeyPhraseResult}.
     *
     * @param documents A list of documents to extract key phrases for.
     * @param options The {@link TextAnalyticsRequestOptions} request options.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains {@link ExtractKeyPhrasesResultCollection}.
     */
    Response<ExtractKeyPhrasesResultCollection> getExtractedKeyPhrasesResponseSync(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        throwIfCallingNotAvailableFeatureInOptions(options);
        inputDocumentsValidation(documents);
        options = options == null ? new TextAnalyticsRequestOptions() : options;
        context = enableSyncRestProxy(getNotNullContext(context))
            .addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE);
        try {
            return (service != null)
                ? toResultCollectionResponseLanguageApi(service.analyzeTextWithResponse(
                    new AnalyzeTextKeyPhraseExtractionInput()
                        .setParameters(
                            new KeyPhraseTaskParameters()
                                .setModelVersion(options.getModelVersion())
                                .setLoggingOptOut(options.isServiceLogsDisabled()))
                        .setAnalysisInput(
                            new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents))),
                    options.isIncludeStatistics(),
                    context))
                : toResultCollectionResponseLegacyApi(legacyService.keyPhrasesWithResponseSync(
                    new MultiLanguageBatchInput().setDocuments(toMultiLanguageInput(documents)),
                    options.getModelVersion(),
                    options.isIncludeStatistics(),
                    options.isServiceLogsDisabled(),
                    context));
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

    private Context enableSyncRestProxy(Context context) {
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }
}
