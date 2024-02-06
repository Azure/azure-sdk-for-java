// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;

/**
 * The {@code TextAnalyticsActions} model.
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
    private Iterable<RecognizeCustomEntitiesAction> recognizeCustomEntitiesActions;
    private Iterable<SingleLabelClassifyAction> singleLabelClassifyActions;
    private Iterable<MultiLabelClassifyAction> multiLabelClassifyActions;
    private Iterable<AbstractiveSummaryAction> abstractiveSummaryActions;
    private Iterable<ExtractiveSummaryAction> extractiveSummaryActions;

    /**
     * Constructs a {@code TextAnalyticsActions} model.
     */
    public TextAnalyticsActions() {
    }

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
     * @return The {@code TextAnalyticsActions} object itself.
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
     * @return The {@code TextAnalyticsActions} object itself.
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
     * @return The {@code TextAnalyticsActions} object itself.
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
     * @return The {@code TextAnalyticsActions} object itself.
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
     * @return The {@code TextAnalyticsActions} object itself.
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
     * @return The {@code TextAnalyticsActions} object itself.
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
     * @return The {@code TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setAnalyzeSentimentActions(AnalyzeSentimentAction... analyzeSentimentActions) {
        this.analyzeSentimentActions = analyzeSentimentActions == null ? null : Arrays.asList(analyzeSentimentActions);
        return this;
    }

    /**
     * Gets the list of {@link RecognizeCustomEntitiesAction} to be executed.
     * See the service documentation for regional support of custom entities recognition:
     * <a href="https://aka.ms/azsdk/textanalytics/customentityrecognition">Custom NER</a>
     *
     * @return the list of {@link RecognizeCustomEntitiesAction} to be executed.
     */
    public Iterable<RecognizeCustomEntitiesAction> getRecognizeCustomEntitiesActions() {
        return recognizeCustomEntitiesActions;
    }

    /**
     * Sets the list of {@link RecognizeCustomEntitiesAction} to be executed.
     * See the service documentation for regional support of custom entities recognition:
     * <a href="https://aka.ms/azsdk/textanalytics/customentityrecognition">Custom NER</a>
     *
     * @param recognizeCustomEntitiesActions The list of {@link RecognizeCustomEntitiesAction} to be executed.
     *
     * @return The {@code TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setRecognizeCustomEntitiesActions(
        RecognizeCustomEntitiesAction... recognizeCustomEntitiesActions) {
        this.recognizeCustomEntitiesActions = recognizeCustomEntitiesActions == null ? null
            : Arrays.asList(recognizeCustomEntitiesActions);
        return this;
    }

    /**
     * Gets the list of {@link SingleLabelClassifyAction} to be executed.
     * See the service documentation for regional support of custom single classification:
     * <a href="https://aka.ms/azsdk/textanalytics/customfunctionalities">Custom NER</a>
     *
     * @return the list of {@link SingleLabelClassifyAction} to be executed.
     */
    public Iterable<SingleLabelClassifyAction> getSingleLabelClassifyActions() {
        return singleLabelClassifyActions;
    }

    /**
     * Sets the list of {@link SingleLabelClassifyAction} to be executed.
     * See the service documentation for regional support of custom single classification:
     * <a href="https://aka.ms/azsdk/textanalytics/customfunctionalities">Custom NER</a>
     *
     * @param singleLabelClassifyActions The list of
     * {@link SingleLabelClassifyAction} to be executed.
     *
     * @return The {@code TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setSingleLabelClassifyActions(SingleLabelClassifyAction... singleLabelClassifyActions) {
        this.singleLabelClassifyActions = singleLabelClassifyActions == null ? null
            : Arrays.asList(singleLabelClassifyActions);
        return this;
    }

    /**
     * Gets the list of {@link MultiLabelClassifyAction} to be executed.
     * See the service documentation for regional support of custom multi classification:
     * <a href="https://aka.ms/azsdk/textanalytics/customfunctionalities">Custom NER</a>
     *
     * @return the list of {@link MultiLabelClassifyAction} to be executed.
     */
    public Iterable<MultiLabelClassifyAction> getMultiLabelClassifyActions() {
        return multiLabelClassifyActions;
    }

    /**
     * Sets the list of {@link MultiLabelClassifyAction} to be executed.
     * See the service documentation for regional support of custom multi classification:
     * <a href="https://aka.ms/azsdk/textanalytics/customfunctionalities">Custom NER</a>
     *
     * @param multiLabelClassifyActions The list of {@link MultiLabelClassifyAction} to
     * be executed.
     *
     * @return The {@code TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setMultiLabelClassifyActions(MultiLabelClassifyAction... multiLabelClassifyActions) {
        this.multiLabelClassifyActions = multiLabelClassifyActions == null ? null
            : Arrays.asList(multiLabelClassifyActions);
        return this;
    }

    /**
     * Gets the list of {@link AbstractiveSummaryAction} to be executed.
     *
     * @return the list of {@link AbstractiveSummaryAction} to be executed.
     */
    public Iterable<AbstractiveSummaryAction> getAbstractiveSummaryActions() {
        return abstractiveSummaryActions;
    }

    /**
     * Sets the list of {@link AbstractiveSummaryAction} to be executed.
     *
     * @param abstractiveSummaryActions The list of {@link AbstractiveSummaryAction} to be executed.
     *
     * @return The {@code TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setAbstractiveSummaryActions(AbstractiveSummaryAction... abstractiveSummaryActions) {
        this.abstractiveSummaryActions = abstractiveSummaryActions == null ? null
            : Arrays.asList(abstractiveSummaryActions);
        return this;
    }

    /**
     * Gets the list of {@link ExtractiveSummaryAction} to be executed.
     *
     * @return the list of {@link ExtractiveSummaryAction} to be executed.
     */
    public Iterable<ExtractiveSummaryAction> getExtractiveSummaryActions() {
        return extractiveSummaryActions;
    }

    /**
     * Sets the list of {@link ExtractiveSummaryAction} to be executed.
     *
     * @param extractiveSummaryActions The list of {@link ExtractiveSummaryAction} to be executed.
     *
     * @return The {@code TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setExtractiveSummaryActions(ExtractiveSummaryAction... extractiveSummaryActions) {
        this.extractiveSummaryActions = extractiveSummaryActions == null ? null
            : Arrays.asList(extractiveSummaryActions);
        return this;
    }
}
