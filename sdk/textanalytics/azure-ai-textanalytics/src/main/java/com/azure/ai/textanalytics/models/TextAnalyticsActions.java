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

    /**
     * Get the custom name for the actions.
     *
     * @return the custom name for the actions.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the custom name for the actions.
     *
     * @param displayName the custom name for the actions.
     *
     * @return the {@link TextAnalyticsActions} object itself.
     */
    public TextAnalyticsActions setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the list of {@link RecognizeEntitiesAction} to be executed.
     *
     * @return the list of {@link RecognizeEntitiesAction} to be executed.
     */
    public Iterable<RecognizeEntitiesAction> getRecognizeEntitiesActions() {
        return this.recognizeEntitiesActions;
    }

    /**
     * Set the list of {@link RecognizeEntitiesAction} to be executed.
     *
     * @param recognizeEntitiesActions the list of {@link RecognizeEntitiesAction} to be executed.
     *
     * @return the {@link TextAnalyticsActions} object itself.
     *
     * @throws IllegalArgumentException if more than one {@link RecognizeEntitiesAction} action are passed in.
     * Currently service v3.1 only accepts up to one action per type.
     */
    public TextAnalyticsActions setRecognizeEntitiesActions(RecognizeEntitiesAction... recognizeEntitiesActions) {
        validateActionsNumber(recognizeEntitiesActions, RecognizeEntitiesAction.class.getName());
        this.recognizeEntitiesActions = recognizeEntitiesActions == null ? null
            : Arrays.asList(recognizeEntitiesActions);
        return this;
    }

    /**
     * Get the list of {@link RecognizeLinkedEntitiesAction} to be executed.
     *
     * @return the list of {@link RecognizeLinkedEntitiesAction} to be executed.
     */
    public Iterable<RecognizeLinkedEntitiesAction> getRecognizeLinkedEntitiesActions() {
        return this.recognizeLinkedEntitiesActions;
    }

    /**
     * Set the list of {@link RecognizeLinkedEntitiesAction} to be executed.
     *
     * @param recognizeLinkedEntitiesActions the list of {@link RecognizeLinkedEntitiesAction} to be executed.
     *
     * @return the {@link TextAnalyticsActions} object itself.
     *
     * @throws IllegalArgumentException if more than one {@link RecognizeLinkedEntitiesAction} action are passed in.
     * Currently service v3.1 only accepts up to one action per type.
     */
    public TextAnalyticsActions setRecognizeLinkedEntitiesActions(
        RecognizeLinkedEntitiesAction... recognizeLinkedEntitiesActions) {
        validateActionsNumber(recognizeLinkedEntitiesActions, RecognizeLinkedEntitiesAction.class.getName());
        this.recognizeLinkedEntitiesActions = recognizeLinkedEntitiesActions == null ? null
            : Arrays.asList(recognizeLinkedEntitiesActions);
        return this;
    }

    /**
     * Get the list of {@link RecognizePiiEntitiesAction} to be executed.
     *
     * @return the list of {@link RecognizePiiEntitiesAction} to be executed.
     */
    public Iterable<RecognizePiiEntitiesAction> getRecognizePiiEntitiesActions() {
        return this.recognizePiiEntitiesActions;
    }

    /**
     * Set the list of {@link RecognizePiiEntitiesAction} to be executed.
     *
     * @param recognizePiiEntitiesActions the list of {@link RecognizePiiEntitiesAction} to be executed.
     *
     * @return the {@link TextAnalyticsActions} object itself.
     *
     * @throws IllegalArgumentException if more than one {@link RecognizePiiEntitiesAction} action are passed in.
     * Currently service v3.1 only accepts up to one action per type.
     */
    public TextAnalyticsActions setRecognizePiiEntitiesActions(
        RecognizePiiEntitiesAction... recognizePiiEntitiesActions) {
        validateActionsNumber(recognizePiiEntitiesActions, RecognizePiiEntitiesAction.class.getName());
        this.recognizePiiEntitiesActions = recognizePiiEntitiesActions == null ? null
            : Arrays.asList(recognizePiiEntitiesActions);
        return this;
    }

    /**
     * Get the list of {@link ExtractKeyPhrasesAction} to be executed.
     *
     * @return the list of {@link ExtractKeyPhrasesAction} to be executed.
     */
    public Iterable<ExtractKeyPhrasesAction> getExtractKeyPhrasesActions() {
        return this.extractKeyPhrasesActions;
    }

    /**
     * Set the list of {@link ExtractKeyPhrasesAction} to be executed.
     *
     * @param extractKeyPhrasesActions the list of {@link ExtractKeyPhrasesAction} to be executed.
     *
     * @return the {@link TextAnalyticsActions} object itself.
     *
     * @throws IllegalArgumentException if more than one {@link ExtractKeyPhrasesAction} action are passed in.
     * Currently service v3.1 only accepts up to one action per type.
     */
    public TextAnalyticsActions setExtractKeyPhrasesActions(ExtractKeyPhrasesAction... extractKeyPhrasesActions) {
        validateActionsNumber(extractKeyPhrasesActions, ExtractKeyPhrasesAction.class.getName());
        this.extractKeyPhrasesActions = extractKeyPhrasesActions == null ? null
            : Arrays.asList(extractKeyPhrasesActions);
        return this;
    }

    /**
     * Get the list of {@link AnalyzeSentimentAction} to be executed.
     *
     * @return the list of {@link AnalyzeSentimentAction} to be executed.
     */
    public Iterable<AnalyzeSentimentAction> getAnalyzeSentimentActions() {
        return this.analyzeSentimentActions;
    }

    /**
     * Set the list of {@link AnalyzeSentimentAction} to be executed.
     *
     * @param analyzeSentimentActions the list of {@link AnalyzeSentimentAction} to be executed.
     *
     * @return the {@link TextAnalyticsActions} object itself.
     *
     * @throws IllegalArgumentException if more than one {@link AnalyzeSentimentAction} action are passed in.
     * Currently service v3.1 only accepts up to one action per type.
     */
    public TextAnalyticsActions setAnalyzeSentimentActions(AnalyzeSentimentAction... analyzeSentimentActions) {
        validateActionsNumber(analyzeSentimentActions, AnalyzeSentimentAction.class.getName());
        this.analyzeSentimentActions = analyzeSentimentActions == null ? null : Arrays.asList(analyzeSentimentActions);
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
