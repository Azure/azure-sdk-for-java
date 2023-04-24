// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextDynamicClassificationInput;
import com.azure.ai.textanalytics.implementation.models.ClassificationType;
import com.azure.ai.textanalytics.implementation.models.DynamicClassificationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.ErrorResponseException;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.models.DynamicClassifyOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.DynamicClassifyDocumentResultCollection;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.implementation.Utility.enableSyncRestProxy;
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.getHttpResponseException;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static com.azure.ai.textanalytics.implementation.Utility.toDynamicClassifyDocumentResultCollectionResponse;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Helper class for managing dynamic classification endpoints.
 */
class DynamicClassifyUtilClient {
    private static final ClientLogger LOGGER = new ClientLogger(DynamicClassifyUtilClient.class);
    private final MicrosoftCognitiveLanguageServiceTextAnalysisImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    DynamicClassifyUtilClient(MicrosoftCognitiveLanguageServiceTextAnalysisImpl service,
        TextAnalyticsServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    Mono<Response<DynamicClassifyDocumentResultCollection>> dynamicClassifyBatch(
        Iterable<TextDocumentInput> documents, Iterable<String> categories,  DynamicClassifyOptions options) {
        try {
            return withContext(context -> getDynamicClassifyDocumentResultCollectionResponse(
                documents, categories, options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<DynamicClassifyDocumentResultCollection>> getDynamicClassifyDocumentResultCollectionResponse(
        Iterable<TextDocumentInput> documents, Iterable<String> categories, DynamicClassifyOptions options,
        Context context) {
        throwIfTargetServiceVersionFound(this.serviceVersion,
            Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1,
                TextAnalyticsServiceVersion.V2022_05_01),
            getUnsupportedServiceApiVersionMessage("Dynamic Classification", serviceVersion,
                TextAnalyticsServiceVersion.V2022_10_01_PREVIEW));
        inputDocumentsValidation(documents);
        options = getNotNullDynamicClassificationOptions(options);

        final com.azure.ai.textanalytics.models.ClassificationType finalClassificationType =
            options.getClassificationType();

        return service.analyzeTextWithResponseAsync(
            new AnalyzeTextDynamicClassificationInput()
                .setParameters(
                    new DynamicClassificationTaskParameters()
                        .setCategories(IterableStream.of(categories).stream().collect(Collectors.toList()))
                        .setClassificationType(finalClassificationType == null ? null
                            : ClassificationType.fromString(finalClassificationType.toString()))
                        .setModelVersion(options.getModelVersion())
                        .setLoggingOptOut(options.isServiceLogsDisabled()))
                .setAnalysisInput(new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents))),
                options.isIncludeStatistics(),
                getNotNullContext(context))

            .doOnSubscribe(ignoredValue ->
                LOGGER.info("A batch of documents with count - {}", getDocumentCount(documents)))
            .doOnSuccess(response ->
                LOGGER.info("Analyze dynamic classification for a batch of documents - {}", response))
            .doOnError(error -> LOGGER.warning("Failed to analyze dynamic classification - {}", error))
            .map(Utility::toDynamicClassifyDocumentResultCollectionResponse)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }

    Response<DynamicClassifyDocumentResultCollection> getResultCollectionResponseSync(
        Iterable<TextDocumentInput> documents, Iterable<String> categories, DynamicClassifyOptions options,
        Context context) {
        throwIfTargetServiceVersionFound(this.serviceVersion,
            Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1,
                TextAnalyticsServiceVersion.V2022_05_01),
            getUnsupportedServiceApiVersionMessage("Dynamic Classification", serviceVersion,
                TextAnalyticsServiceVersion.V2022_10_01_PREVIEW));
        inputDocumentsValidation(documents);
        options = getNotNullDynamicClassificationOptions(options);

        final com.azure.ai.textanalytics.models.ClassificationType finalClassificationType =
            options.getClassificationType();
        try {
            return toDynamicClassifyDocumentResultCollectionResponse(service.analyzeTextWithResponse(
                new AnalyzeTextDynamicClassificationInput()
                    .setParameters(
                        new DynamicClassificationTaskParameters()
                            .setCategories(IterableStream.of(categories).stream().collect(Collectors.toList()))
                            .setClassificationType(finalClassificationType == null ? null
                                : ClassificationType.fromString(finalClassificationType.toString()))
                            .setModelVersion(options.getModelVersion())
                            .setLoggingOptOut(options.isServiceLogsDisabled()))
                    .setAnalysisInput(new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents))),
                options.isIncludeStatistics(),
                enableSyncRestProxy(getNotNullContext(context))));
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    private DynamicClassifyOptions getNotNullDynamicClassificationOptions(
        DynamicClassifyOptions options) {
        return options == null ? new DynamicClassifyOptions() : options;
    }
}
