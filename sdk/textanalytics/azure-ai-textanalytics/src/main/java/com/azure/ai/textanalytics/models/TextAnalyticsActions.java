// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;

/**
 * The {@link TextAnalyticsActions} model.
 */
@Fluent
public final class TextAnalyticsActions {
    private String displayName;
    private Iterable<RecognizeEntitiesAction> recognizeEntitiesActions;
    private Iterable<RecognizeLinkedEntitiesAction> recognizeLinkedEntitiesActions;
    private Iterable<RecognizePiiEntitiesAction> recognizePiiEntitiesActions;
    private Iterable<AnalyzeHealthcareEntitiesAction> analyzeHealthcareEntitiesActions;
    private Iterable<ExtractKeyPhrasesAction> extractKeyPhrasesActions;
    private Iterable<AnalyzeSentimentAction> analyzeSentimentActions;
    private Iterable<ExtractSummaryAction> extractSummaryActions;
    private Iterable<RecognizeCustomEntitiesAction> recognizeCustomEntitiesActions;
    private Iterable<SingleCategoryClassifyAction> singleCategoryClassifyActions;
    private Iterable<MultiCategoryClassifyAction> multiCategoryClassifyActions;

    /**
     * Gets the custom name for the actions.
     *
     * @return the custom name for the actions.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the custom name for the actions.
     *
     * @param displayName The custom name for the actions.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Gets the list of {@link RecognizeEntitiesAction} to be executed.
     *
     * @return The list of {@link RecognizeEntitiesAction} to be executed.
     */
    public Iterable<RecognizeEntitiesAction> getRecognizeEntitiesActions() {
        return this.recognizeEntitiesActions;
    }

    /**
     * Sets the list of {@link RecognizeEntitiesAction} to be executed.
     *
     * @param recognizeEntitiesActions The list of {@link RecognizeEntitiesAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setRecognizeEntitiesActions(RecognizeEntitiesAction... recognizeEntitiesActions) {
        this.recognizeEntitiesActions = recognizeEntitiesActions == null ? null
            : Arrays.asList(recognizeEntitiesActions);
        return this;
    }

    /**
     * Gets the list of {@link RecognizeLinkedEntitiesAction} to be executed.
     *
     * @return The list of {@link RecognizeLinkedEntitiesAction} to be executed.
     */
    public Iterable<RecognizeLinkedEntitiesAction> getRecognizeLinkedEntitiesActions() {
        return this.recognizeLinkedEntitiesActions;
    }

    /**
     * Sets the list of {@link RecognizeLinkedEntitiesAction} to be executed.
     *
     * @param recognizeLinkedEntitiesActions The list of {@link RecognizeLinkedEntitiesAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setRecognizeLinkedEntitiesActions(
        RecognizeLinkedEntitiesAction... recognizeLinkedEntitiesActions) {
        this.recognizeLinkedEntitiesActions = recognizeLinkedEntitiesActions == null ? null
            : Arrays.asList(recognizeLinkedEntitiesActions);
        return this;
    }

    /**
     * Gets the list of {@link RecognizePiiEntitiesAction} to be executed.
     *
     * @return The list of {@link RecognizePiiEntitiesAction} to be executed.
     */
    public Iterable<RecognizePiiEntitiesAction> getRecognizePiiEntitiesActions() {
        return this.recognizePiiEntitiesActions;
    }

    /**
     * Sets the list of {@link RecognizePiiEntitiesAction} to be executed.
     *
     * @param recognizePiiEntitiesActions The list of {@link RecognizePiiEntitiesAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setRecognizePiiEntitiesActions(
        RecognizePiiEntitiesAction... recognizePiiEntitiesActions) {
        this.recognizePiiEntitiesActions = recognizePiiEntitiesActions == null ? null
            : Arrays.asList(recognizePiiEntitiesActions);
        return this;
    }

    /**
     * Gets the list of {@link AnalyzeHealthcareEntitiesAction} to be executed.
     *
     * @return The list of {@link AnalyzeHealthcareEntitiesAction} to be executed.
     */
    public Iterable<AnalyzeHealthcareEntitiesAction> getAnalyzeHealthcareEntitiesActions() {
        return this.analyzeHealthcareEntitiesActions;
    }

    /**
     * Sets the list of {@link AnalyzeHealthcareEntitiesAction} to be executed.
     *
     * @param analyzeHealthcareEntitiesActions The list of {@link AnalyzeHealthcareEntitiesAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setAnalyzeHealthcareEntitiesActions(
        AnalyzeHealthcareEntitiesAction... analyzeHealthcareEntitiesActions) {
        this.analyzeHealthcareEntitiesActions = analyzeHealthcareEntitiesActions == null ? null
                                               : Arrays.asList(analyzeHealthcareEntitiesActions);
        return this;
    }

    /**
     * Gets the list of {@link ExtractKeyPhrasesAction} to be executed.
     *
     * @return The list of {@link ExtractKeyPhrasesAction} to be executed.
     */
    public Iterable<ExtractKeyPhrasesAction> getExtractKeyPhrasesActions() {
        return this.extractKeyPhrasesActions;
    }

    /**
     * Sets the list of {@link ExtractKeyPhrasesAction} to be executed.
     *
     * @param extractKeyPhrasesActions The list of {@link ExtractKeyPhrasesAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setExtractKeyPhrasesActions(ExtractKeyPhrasesAction... extractKeyPhrasesActions) {
        this.extractKeyPhrasesActions = extractKeyPhrasesActions == null ? null
            : Arrays.asList(extractKeyPhrasesActions);
        return this;
    }

    /**
     * Gets the list of {@link AnalyzeSentimentAction} to be executed.
     *
     * @return The list of {@link AnalyzeSentimentAction} to be executed.
     */
    public Iterable<AnalyzeSentimentAction> getAnalyzeSentimentActions() {
        return this.analyzeSentimentActions;
    }

    /**
     * Sets the list of {@link AnalyzeSentimentAction} to be executed.
     *
     * @param analyzeSentimentActions The list of {@link AnalyzeSentimentAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setAnalyzeSentimentActions(AnalyzeSentimentAction... analyzeSentimentActions) {
        this.analyzeSentimentActions = analyzeSentimentActions == null ? null : Arrays.asList(analyzeSentimentActions);
        return this;
    }

    /**
     * Gets the list of {@link ExtractSummaryAction} to be executed.
     *
     * @return the list of {@link ExtractSummaryAction} to be executed.
     */
    public Iterable<ExtractSummaryAction> getExtractSummaryActions() {
        return extractSummaryActions;
    }

    /**
     * Sets the list of {@link ExtractSummaryAction} to be executed.
     *
     * @param extractSummaryActions The list of {@link ExtractSummaryAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setExtractSummaryActions(ExtractSummaryAction... extractSummaryActions) {
        this.extractSummaryActions = extractSummaryActions == null ? null : Arrays.asList(extractSummaryActions);
        return this;
    }

    /**
     * Gets the list of {@link RecognizeCustomEntitiesAction} to be executed.
     *
     * See the service documentation for regional support of custom entities recognition:
     * https://aka.ms/azsdk/textanalytics/customentityrecognition
     *
     * @return the list of {@link RecognizeCustomEntitiesAction} to be executed.
     */
    public Iterable<RecognizeCustomEntitiesAction> getRecognizeCustomEntitiesActions() {
        return recognizeCustomEntitiesActions;
    }

    /**
     * Sets the list of {@link RecognizeCustomEntitiesAction} to be executed.
     *
     * See the service documentation for regional support of custom entities recognition:
     * https://aka.ms/azsdk/textanalytics/customentityrecognition
     *
     * @param recognizeCustomEntitiesActions The list of {@link RecognizeCustomEntitiesAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setRecognizeCustomEntitiesActions(
        RecognizeCustomEntitiesAction... recognizeCustomEntitiesActions) {
        this.recognizeCustomEntitiesActions = recognizeCustomEntitiesActions == null ? null
                                                  : Arrays.asList(recognizeCustomEntitiesActions);
        return this;
    }

    /**
     * Gets the list of {@link SingleCategoryClassifyAction} to be executed.
     *
     * See the service documentation for regional support of custom single classification:
     * https://aka.ms/azsdk/textanalytics/customfunctionalities
     *
     * @return the list of {@link SingleCategoryClassifyAction} to be executed.
     */
    public Iterable<SingleCategoryClassifyAction> getSingleCategoryClassifyActions() {
        return singleCategoryClassifyActions;
    }

    /**
     * Sets the list of {@link SingleCategoryClassifyAction} to be executed.
     *
     * See the service documentation for regional support of custom single classification:
     * https://aka.ms/azsdk/textanalytics/customfunctionalities
     *
     * @param singleCategoryClassifyActions The list of
     * {@link SingleCategoryClassifyAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setSingleCategoryClassifyActions(
        SingleCategoryClassifyAction... singleCategoryClassifyActions) {
        this.singleCategoryClassifyActions = singleCategoryClassifyActions == null
                                                 ? null : Arrays.asList(singleCategoryClassifyActions);
        return this;
    }

    /**
     * Gets the list of {@link MultiCategoryClassifyAction} to be executed.
     *
     * See the service documentation for regional support of custom multi classification:
     * https://aka.ms/azsdk/textanalytics/customfunctionalities
     *
     * @return the list of {@link MultiCategoryClassifyAction} to be executed.
     */
    public Iterable<MultiCategoryClassifyAction> getMultiCategoryClassifyActions() {
        return multiCategoryClassifyActions;
    }

    /**
     * Sets the list of {@link MultiCategoryClassifyAction} to be executed.
     *
     * See the service documentation for regional support of custom multi classification:
     * https://aka.ms/azsdk/textanalytics/customfunctionalities
     *
     * @param multiCategoryClassifyActions The list of {@link MultiCategoryClassifyAction} to
     * be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setMultiCategoryClassifyActions(
        MultiCategoryClassifyAction... multiCategoryClassifyActions) {
        this.multiCategoryClassifyActions = multiCategoryClassifyActions == null
                                                ? null : Arrays.asList(multiCategoryClassifyActions);
        return this;
    }
}
