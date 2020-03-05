// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.models.DocumentError;
import com.azure.ai.textanalytics.implementation.models.DocumentLanguage;
import com.azure.ai.textanalytics.implementation.models.LanguageBatchInput;
import com.azure.ai.textanalytics.implementation.models.LanguageInput;
import com.azure.ai.textanalytics.implementation.models.LanguageResult;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentResultCollection;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Helper class for managing detect language endpoint.
 */
class DetectLanguageAsyncClient {
    private final ClientLogger logger = new ClientLogger(DetectLanguageAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@code DetectLanguageAsyncClient} that sends requests to the Text Analytics services's detect language
     * endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    DetectLanguageAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    Mono<Response<DocumentResultCollection<DetectLanguageResult>>> detectLanguageBatchWithResponse(
        Iterable<DetectLanguageInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        final List<LanguageInput> multiLanguageInputs = new ArrayList<>();
        for (DetectLanguageInput textDocumentInput : textInputs) {
            multiLanguageInputs.add(new LanguageInput()
                .setId(textDocumentInput.getId())
                .setText(textDocumentInput.getText())
                .setCountryHint(textDocumentInput.getCountryHint()));
        }

        return service.languagesWithRestResponseAsync(new LanguageBatchInput().setDocuments(multiLanguageInputs),
            options == null ? null : options.getModelVersion(),
            options == null ? null : options.showStatistics(), context)
            .doOnSubscribe(ignoredValue -> logger.info("A batch of language input - {}", textInputs.toString()))
            .doOnSuccess(response -> logger.info("A batch of detected language output - {}", response.getValue()))
            .doOnError(error -> logger.warning("Failed to detect language - {}", error))
            .map(response -> new SimpleResponse<>(response, toDocumentResultCollection(response.getValue())));
    }

    /**
     * Helper method to convert the service response of {@link LanguageResult} to {@link DocumentResultCollection}.
     *
     * @param languageResult the {@link LanguageResult} returned by the service.
     *
     * @return the {@link DocumentResultCollection} of {@link DetectLanguageResult} to be returned by the SDK.
     */
    private static DocumentResultCollection<DetectLanguageResult> toDocumentResultCollection(
        final LanguageResult languageResult) {

        final List<DetectLanguageResult> detectLanguageResults = new ArrayList<>();
        for (DocumentLanguage documentLanguage : languageResult.getDocuments()) {
            DetectedLanguage primaryLanguage = null;
            List<com.azure.ai.textanalytics.implementation.models.DetectedLanguage> detectedLanguages =
                documentLanguage.getDetectedLanguages();
            if (detectedLanguages.size() >= 1) {
                detectedLanguages.sort(
                    Comparator.comparing(com.azure.ai.textanalytics.implementation.models.DetectedLanguage::getScore));
                com.azure.ai.textanalytics.implementation.models.DetectedLanguage detectedLanguageResult =
                    detectedLanguages.get(0);
                primaryLanguage = new DetectedLanguage(detectedLanguageResult.getName(),
                    detectedLanguageResult.getIso6391Name(), detectedLanguageResult.getScore());
            }
            detectLanguageResults.add(new DetectLanguageResult(documentLanguage.getId(),
                documentLanguage.getStatistics() == null
                    ? null : Transforms.toTextDocumentStatistics(documentLanguage.getStatistics()),
                null,
                primaryLanguage));
        }

        for (DocumentError documentError : languageResult.getErrors()) {
            com.azure.ai.textanalytics.models.TextAnalyticsError error =
                Transforms.toTextAnalyticsError(documentError.getError());
            detectLanguageResults.add(
                new DetectLanguageResult(documentError.getId(), null, error, null));
        }

        return new DocumentResultCollection<>(detectLanguageResults, languageResult.getModelVersion(),
            languageResult.getStatistics() == null ? null
                : Transforms.toBatchStatistics(languageResult.getStatistics()));
    }
}
