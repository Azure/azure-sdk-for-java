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
    private Iterable<RecognizeCustomEntitiesAction> RecognizeCustomEntitiesActions;
    private Iterable<ClassifyCustomCategoryAction> classifyCustomCategoryActions;
    private Iterable<ClassifyCustomCategoriesAction> classifyCustomCategoriesActions;

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
        return RecognizeCustomEntitiesActions;
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
        this.RecognizeCustomEntitiesActions = recognizeCustomEntitiesActions == null ? null :
                                                  Arrays.asList(recognizeCustomEntitiesActions);
        return this;
    }

    /**
     * Gets the list of {@link ClassifyCustomCategoryAction} to be executed.
     *
     * @return the list of {@link ClassifyCustomCategoryAction} to be executed.
     */
    public Iterable<ClassifyCustomCategoryAction> getClassifyCustomCategoryActions() {
        return classifyCustomCategoryActions;
    }

    /**
     * Sets the list of {@link ClassifyCustomCategoryAction} to be executed.
     *
     * @param classifyCustomCategoryActions The list of {@link ClassifyCustomCategoryAction} to be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     *
     * @throws IllegalArgumentException if more than one {@link ClassifyCustomCategoryAction} action are passed in.
     * Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setClassifyCustomCategoryActions(
        ClassifyCustomCategoryAction... classifyCustomCategoryActions) {
        validateActionsNumber(classifyCustomCategoryActions, ClassifyCustomCategoryAction.class.getName());
        this.classifyCustomCategoryActions = classifyCustomCategoryActions == null ? null :
                                                  Arrays.asList(classifyCustomCategoryActions);
        return this;
    }

    /**
     * Gets the list of {@link ClassifyCustomCategoriesAction} to be executed.
     *
     * @return the list of {@link ClassifyCustomCategoriesAction} to be executed.
     */
    public Iterable<ClassifyCustomCategoriesAction> getClassifyCustomCategoriesActions() {
        return classifyCustomCategoriesActions;
    }

    /**
     * Sets the list of {@link ClassifyCustomCategoriesAction} to be executed.
     *
     * @param classifyCustomCategoriesActions The list of {@link ClassifyCustomCategoriesAction} to
     * be executed.
     *
     * @return The {@link TextAnalyticsActions} object itself.
     *
     * @throws IllegalArgumentException if more than one {@link ClassifyCustomCategoriesAction} action are
     * passed in. Currently service v3.1 and up only accepts up to one action per type.
     */
    public TextAnalyticsActions setClassifyCustomCategoriesActions(
        ClassifyCustomCategoriesAction... classifyCustomCategoriesActions) {
        validateActionsNumber(classifyCustomCategoriesActions,
            ClassifyCustomCategoriesAction.class.getName());
        this.classifyCustomCategoriesActions = classifyCustomCategoriesActions == null ? null :
                                                            Arrays.asList(classifyCustomCategoriesActions);
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
