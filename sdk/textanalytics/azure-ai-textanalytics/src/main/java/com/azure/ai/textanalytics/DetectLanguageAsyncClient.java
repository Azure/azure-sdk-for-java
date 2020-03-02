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
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedFlux;
import com.azure.ai.textanalytics.util.TextAnalyticsPagedResponse;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.azure.ai.textanalytics.Transforms.toBatchStatistics;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Helper class for managing detect language endpoint.
 */
class DetectLanguageAsyncClient {
    private final ClientLogger logger = new ClientLogger(DetectLanguageAsyncClient.class);
    private final TextAnalyticsClientImpl service;

    /**
     * Create a {@link DetectLanguageAsyncClient} that sends requests to the Text Analytics services's detect language
     * endpoint.
     *
     * @param service The proxy service used to perform REST calls.
     */
    DetectLanguageAsyncClient(TextAnalyticsClientImpl service) {
        this.service = service;
    }

    /**
     * Helper function that detects a batch of text inputs and returns {@link TextAnalyticsPagedFlux} that is a paged
     * flux contains {@link DetectLanguageResult}.
     *
     * @param textInputs A batch of input texts.
     * @param options The request options, such as the training model version and to show statistics boolean.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link DetectLanguageResult}.
     */
    TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatch(Iterable<DetectLanguageInput> textInputs,
        TextAnalyticsRequestOptions options) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");

        final List<LanguageInput> multiLanguageInputs = new ArrayList<>();
        textInputs.forEach(textDocumentInput -> multiLanguageInputs.add(new LanguageInput()
            .setId(textDocumentInput.getId())
            .setText(textDocumentInput.getText())
            .setCountryHint(textDocumentInput.getCountryHint())));

        try {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> withContext(context ->
                service.languagesWithRestResponseAsync(new LanguageBatchInput().setDocuments(multiLanguageInputs),
                    options == null ? null : options.getModelVersion(),
                    options == null ? null : options.showStatistics(), context)
                    .doOnSubscribe(ignoredValue -> logger.info("A batch of language input - {}", textInputs.toString()))
                    .doOnSuccess(response ->
                        logger.info("A batch of detected language output - {}", response.getValue()))
                    .doOnError(error -> logger.warning("Failed to detect language - {}", error))
                    .map(this::toTextAnalyticsPagedResponse))
                    .flux());
        } catch (RuntimeException ex) {
            return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) -> fluxError(logger, ex));
        }
    }

    /**
     * Helper function that detects a batch of text inputs with {@link Context} and returns
     * {@link TextAnalyticsPagedFlux} that is a paged flux contains {@link DetectLanguageResult}.
     *
     * @param textInputs A batch of input texts.
     * @param options The request options, such as the training model version and to show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The {@link TextAnalyticsPagedFlux} of {@link DetectLanguageResult}.
     */
    TextAnalyticsPagedFlux<DetectLanguageResult> detectLanguageBatchWithContext(
        Iterable<DetectLanguageInput> textInputs, TextAnalyticsRequestOptions options, Context context) {
        Objects.requireNonNull(textInputs, "'textInputs' cannot be null.");
        final List<LanguageInput> multiLanguageInputs = new ArrayList<>();
        textInputs.forEach(textDocumentInput -> multiLanguageInputs.add(new LanguageInput()
            .setId(textDocumentInput.getId())
            .setText(textDocumentInput.getText())
            .setCountryHint(textDocumentInput.getCountryHint())));

        return new TextAnalyticsPagedFlux<>(() -> (continuationToken, pageSize) ->
            service.languagesWithRestResponseAsync(
                new LanguageBatchInput().setDocuments(multiLanguageInputs),
                options == null ? null : options.getModelVersion(),
                options == null ? null : options.showStatistics(), context)
                .doOnSubscribe(ignoredValue -> logger.info("A batch of language input - {}", textInputs.toString()))
                .doOnSuccess(response -> logger.info("A batch of detected language output - {}", response.getValue()))
                .doOnError(error -> logger.warning("Failed to detect language - {}", error))
                .map(this::toTextAnalyticsPagedResponse)
                .flux());
    }

    /**
     * Helper method to convert the service response of {@link LanguageResult} to {@link TextAnalyticsPagedResponse}
     * of {@link DetectLanguageResult}.
     *
     * @param response the {@link SimpleResponse} of {@link LanguageResult} returned by the service.
     *
     * @return the {@link TextAnalyticsPagedResponse} of {@link DetectLanguageResult} to be returned by the SDK.
     */
    private TextAnalyticsPagedResponse<DetectLanguageResult> toTextAnalyticsPagedResponse(
        SimpleResponse<LanguageResult> response) {

        final LanguageResult languageResult = response.getValue();

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
            final String documentId = documentError.getId();

            detectLanguageResults.add(
                new DetectLanguageResult(documentId, null, error, null));
        }

        return new TextAnalyticsPagedResponse<>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            detectLanguageResults,
            null,
            languageResult.getModelVersion(),
            languageResult.getStatistics() == null ? null : toBatchStatistics(languageResult.getStatistics()));
    }
}
