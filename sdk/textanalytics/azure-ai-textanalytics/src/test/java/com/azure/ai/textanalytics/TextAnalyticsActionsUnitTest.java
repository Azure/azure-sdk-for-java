// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AnalyzeSentimentAction;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesAction;
import com.azure.ai.textanalytics.models.RecognizeEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesAction;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link TextAnalyticsActions}
 */
public class TextAnalyticsActionsUnitTest {
    private static final String DUPLICATE_ACTIONS_ERROR_MESSAGE =
        "Currently, the service can accept up to one %s. Multiple actions of the same type are not supported.";

    @Test
    public void duplicateRecognizeEntitiesActions() {
        TextAnalyticsActions actions = new TextAnalyticsActions();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> actions.setRecognizeEntitiesActions(new RecognizeEntitiesAction(), new RecognizeEntitiesAction()));
        assertEquals(String.format(DUPLICATE_ACTIONS_ERROR_MESSAGE, RecognizeEntitiesAction.class.getName()),
            exception.getMessage());
    }

    @Test
    public void duplicateRecognizeLinkedEntitiesActions() {
        TextAnalyticsActions actions = new TextAnalyticsActions();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> actions.setRecognizeLinkedEntitiesActions(new RecognizeLinkedEntitiesAction(),
                new RecognizeLinkedEntitiesAction()));
        assertEquals(String.format(DUPLICATE_ACTIONS_ERROR_MESSAGE, RecognizeLinkedEntitiesAction.class.getName()),
            exception.getMessage());
    }

    @Test
    public void duplicateRecognizePiiEntitiesActions() {
        TextAnalyticsActions actions = new TextAnalyticsActions();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> actions.setRecognizePiiEntitiesActions(new RecognizePiiEntitiesAction(),
                new RecognizePiiEntitiesAction()));
        assertEquals(String.format(DUPLICATE_ACTIONS_ERROR_MESSAGE, RecognizePiiEntitiesAction.class.getName()),
            exception.getMessage());
    }

    @Test
    public void duplicateExtractKeyPhrasesActions() {
        TextAnalyticsActions actions = new TextAnalyticsActions();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> actions.setExtractKeyPhrasesActions(new ExtractKeyPhrasesAction(), new ExtractKeyPhrasesAction()));
        assertEquals(String.format(DUPLICATE_ACTIONS_ERROR_MESSAGE, ExtractKeyPhrasesAction.class.getName()),
            exception.getMessage());
    }

    @Test
    public void duplicateAnalyzeSentimentActions() {
        TextAnalyticsActions actions = new TextAnalyticsActions();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> actions.setAnalyzeSentimentActions(new AnalyzeSentimentAction(), new AnalyzeSentimentAction()));
        assertEquals(String.format(DUPLICATE_ACTIONS_ERROR_MESSAGE, AnalyzeSentimentAction.class.getName()),
            exception.getMessage());
    }
}
