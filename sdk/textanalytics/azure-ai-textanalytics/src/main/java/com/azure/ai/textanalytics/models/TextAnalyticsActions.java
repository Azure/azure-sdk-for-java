// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.util.Arrays;

/**
 * The {@link TextAnalyticsActions} model.
 */
@Fluent
public final class TextAnalyticsActions {
    private final ClientLogger logger = new ClientLogger(TextAnalyticsActions.class);

    private String displayName;
    private Iterable<RecognizeEntitiesAction> recognizeEntitiesActions;
    private Iterable<RecognizeLinkedEntitiesAction> recognizeLinkedEntitiesActions;
    private Iterable<RecognizePiiEntitiesAction> recognizePiiEntitiesActions;
    private Iterable<ExtractKeyPhrasesAction> extractKeyPhrasesActions;
    private Iterable<AnalyzeSentimentAction> analyzeSentimentActions;
    private Iterable<ExtractSummaryAction> extractSummaryActions;
    private Iterable<RecognizeCustomEntitiesAction> recognizeCustomEntitiesActions;
    private Iterable<ClassifyCustomSingleCategoryAction> classifyCustomSingleCategoryActions;
    private Iterable<ClassifyCustomMultiCategoriesAction> classifyCustomMultiCategoriesActions;

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
     *
     * @throws IllegalArgumentException if more than one {@link RecognizeEntitiesAction} action are passed in.
     * Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setRecognizeEntitiesActions(RecognizeEntitiesAction... recognizeEntitiesActions) {
        validateActionsNumber(recognizeEntitiesActions, RecognizeEntitiesAction.class.getName());
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
     *
     * @throws IllegalArgumentException if more than one {@link RecognizeLinkedEntitiesAction} action are passed in.
     * Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setRecognizeLinkedEntitiesActions(
        RecognizeLinkedEntitiesAction... recognizeLinkedEntitiesActions) {
        validateActionsNumber(recognizeLinkedEntitiesActions, RecognizeLinkedEntitiesAction.class.getName());
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
     *
     * @throws IllegalArgumentException if more than one {@link RecognizePiiEntitiesAction} action are passed in.
     * Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setRecognizePiiEntitiesActions(
        RecognizePiiEntitiesAction... recognizePiiEntitiesActions) {
        validateActionsNumber(recognizePiiEntitiesActions, RecognizePiiEntitiesAction.class.getName());
        this.recognizePiiEntitiesActions = recognizePiiEntitiesActions == null ? null
            : Arrays.asList(recognizePiiEntitiesActions);
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
     *
     * @throws IllegalArgumentException if more than one {@link ExtractKeyPhrasesAction} action are passed in.
     * Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setExtractKeyPhrasesActions(ExtractKeyPhrasesAction... extractKeyPhrasesActions) {
        validateActionsNumber(extractKeyPhrasesActions, ExtractKeyPhrasesAction.class.getName());
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
     *
     * @throws IllegalArgumentException if more than one {@link AnalyzeSentimentAction} action are passed in.
     * Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setAnalyzeSentimentActions(AnalyzeSentimentAction... analyzeSentimentActions) {
        validateActionsNumber(analyzeSentimentActions, AnalyzeSentimentAction.class.getName());
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
     *
     * @throws IllegalArgumentException if more than one {@link ExtractSummaryAction} action are passed in.
     * Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setExtractSummaryActions(ExtractSummaryAction... extractSummaryActions) {
        validateActionsNumber(extractSummaryActions, ExtractSummaryAction.class.getName());
        this.extractSummaryActions = extractSummaryActions == null ? null : Arrays.asList(extractSummaryActions);
        return this;
    }

    /**
     * Gets the list of {@link RecognizeCustomEntitiesAction} to be executed.
     *
     * @return the list of {@link RecognizeCustomEntitiesAction} to be executed.
     */
    public Iterable<RecognizeCustomEntitiesAction> getRecognizeCustomEntitiesActions() {
        return recognizeCustomEntitiesActions;
    }

    /**
     * Sets the list of {@link RecognizeCustomEntitiesAction} to be executed.
     *
     * @param recognizeCustomEntitiesActions The list of {@link RecognizeCustomEntitiesAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     *
     * @throws IllegalArgumentException if more than one {@link RecognizeCustomEntitiesAction} action are passed in.
     * Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setRecognizeCustomEntitiesActions(
        RecognizeCustomEntitiesAction... recognizeCustomEntitiesActions) {
        validateActionsNumber(recognizeCustomEntitiesActions, RecognizeCustomEntitiesAction.class.getName());
        this.recognizeCustomEntitiesActions = recognizeCustomEntitiesActions == null ? null
                                                  : Arrays.asList(recognizeCustomEntitiesActions);
        return this;
    }

    /**
     * Gets the list of {@link ClassifyCustomSingleCategoryAction} to be executed.
     *
     * @return the list of {@link ClassifyCustomSingleCategoryAction} to be executed.
     */
    public Iterable<ClassifyCustomSingleCategoryAction> getClassifyCustomSingleCategoryActions() {
        return classifyCustomSingleCategoryActions;
    }

    /**
     * Sets the list of {@link ClassifyCustomSingleCategoryAction} to be executed.
     *
     * @param classifyCustomSingleCategoryActions The list of
     * {@link ClassifyCustomSingleCategoryAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     *
     * @throws IllegalArgumentException if more than one {@link ClassifyCustomSingleCategoryAction}
     * action are passed in.
     * Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setClassifyCustomSingleCategoryActions(
        ClassifyCustomSingleCategoryAction... classifyCustomSingleCategoryActions) {
        validateActionsNumber(classifyCustomSingleCategoryActions,
            ClassifyCustomSingleCategoryAction.class.getName());
        this.classifyCustomSingleCategoryActions = classifyCustomSingleCategoryActions == null
            ? null : Arrays.asList(classifyCustomSingleCategoryActions);
        return this;
    }

    /**
     * Gets the list of {@link ClassifyCustomMultiCategoriesAction} to be executed.
     *
     * @return the list of {@link ClassifyCustomMultiCategoriesAction} to be executed.
     */
    public Iterable<ClassifyCustomMultiCategoriesAction> getClassifyCustomMultiCategoriesActions() {
        return classifyCustomMultiCategoriesActions;
    }

    /**
     * Sets the list of {@link ClassifyCustomMultiCategoriesAction} to be executed.
     *
     * @param classifyCustomMultiCategoriesActions The list of {@link ClassifyCustomMultiCategoriesAction} to
     * be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     *
     * @throws IllegalArgumentException if more than one {@link ClassifyCustomMultiCategoriesAction} action are
     * passed in. Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setClassifyCustomMultiCategoriesActions(
        ClassifyCustomMultiCategoriesAction... classifyCustomMultiCategoriesActions) {
        validateActionsNumber(classifyCustomMultiCategoriesActions,
            ClassifyCustomMultiCategoriesAction.class.getName());
        this.classifyCustomMultiCategoriesActions = classifyCustomMultiCategoriesActions == null
            ? null : Arrays.asList(classifyCustomMultiCategoriesActions);
        return this;
    }

    private void validateActionsNumber(Object[] actions, String actionType) {
        if (actions != null && actions.length > 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "Currently, the service can accept up to one %s. Multiple actions of the same type are not supported.",
                actionType)));
        }
    }
}
