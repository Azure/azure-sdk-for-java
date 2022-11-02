// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImpl;
import com.azure.ai.textanalytics.implementation.Utility;
import com.azure.ai.textanalytics.implementation.models.AnalyzeTextDynamicClassificationInput;
import com.azure.ai.textanalytics.implementation.models.ClassificationType;
import com.azure.ai.textanalytics.implementation.models.DynamicClassificationTaskParameters;
import com.azure.ai.textanalytics.implementation.models.MultiLanguageAnalysisInput;
import com.azure.ai.textanalytics.models.DynamicClassificationOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.DynamicClassifyDocumentResultCollection;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.azure.ai.textanalytics.TextAnalyticsAsyncClient.COGNITIVE_TRACING_NAMESPACE_VALUE;
import static com.azure.ai.textanalytics.implementation.Utility.getDocumentCount;
import static com.azure.ai.textanalytics.implementation.Utility.getNotNullContext;
import static com.azure.ai.textanalytics.implementation.Utility.getUnsupportedServiceApiVersionMessage;
import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.throwIfTargetServiceVersionFound;
import static com.azure.ai.textanalytics.implementation.Utility.toMultiLanguageInput;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Helper class for managing dynamic classification endpoints.
 */
class DynamicClassificationAsyncClient {
    private final ClientLogger logger = new ClientLogger(DynamicClassificationAsyncClient.class);
    private final MicrosoftCognitiveLanguageServiceTextAnalysisImpl service;

    private final TextAnalyticsServiceVersion serviceVersion;

    DynamicClassificationAsyncClient(MicrosoftCognitiveLanguageServiceTextAnalysisImpl service,
                                     TextAnalyticsServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    Mono<Response<DynamicClassifyDocumentResultCollection>> dynamicClassifyBatch(
        Iterable<TextDocumentInput> documents, DynamicClassificationOptions options) {
        try {
            return withContext(context -> getDynamicClassifyDocumentResultCollectionResponse(
                documents, options, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DynamicClassifyDocumentResultCollection>> getDynamicClassifyDocumentResultCollectionResponse(
        Iterable<TextDocumentInput> documents, DynamicClassificationOptions options, Context context) {
        throwIfTargetServiceVersionFound(this.serviceVersion,
            Arrays.asList(TextAnalyticsServiceVersion.V3_0, TextAnalyticsServiceVersion.V3_1,
                TextAnalyticsServiceVersion.V2022_05_01),
            getUnsupportedServiceApiVersionMessage("Dynamic Classification", serviceVersion,
                TextAnalyticsServiceVersion.V2022_10_01_PREVIEW));
        inputDocumentsValidation(documents);
        options = getNotNullDynamicClassificationOptions(options);

        final com.azure.ai.textanalytics.models.ClassificationType finalClassificationType = options.getClassificationType();

        return service.analyzeTextWithResponseAsync(
            new AnalyzeTextDynamicClassificationInput()
                .setParameters(
                    new DynamicClassificationTaskParameters()
                        .setCategories(IterableStream.of(options.getCategories()).stream().collect(Collectors.toList()))
                        .setClassificationType(finalClassificationType == null ? null
                            : ClassificationType.fromString(finalClassificationType.toString()))
                        .setModelVersion(options.getModelVersion())
                        .setLoggingOptOut(options.isServiceLogsDisabled()))
                .setAnalysisInput(new MultiLanguageAnalysisInput().setDocuments(toMultiLanguageInput(documents))),
                options.isIncludeStatistics(),
                getNotNullContext(context).addData(AZ_TRACING_NAMESPACE_KEY, COGNITIVE_TRACING_NAMESPACE_VALUE))

            .doOnSubscribe(ignoredValue -> logger.info("A batch of documents with count - {}", getDocumentCount(documents)))
            .doOnSuccess(response -> logger.info("Analyze dynamic classification for a batch of documents - {}", response))
            .doOnError(error -> logger.warning("Failed to analyze dynamic classification - {}", error))
            .map(Utility::toDynamicClassifyDocumentResultCollectionResponse)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
    }

    private DynamicClassificationOptions getNotNullDynamicClassificationOptions(
        DynamicClassificationOptions options) {
        return options == null ? new DynamicClassificationOptions() : options;
    }
}
