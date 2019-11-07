// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

public abstract class SkillsetManagementTestBase extends SearchServiceTestBase {

    public abstract void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhrase();

    public abstract void createSkillsetReturnsCorrectDefinitionLanguageDetection();

    public abstract void createSkillsetReturnsCorrectDefinitionMergeText();

    public abstract void createSkillsetReturnsCorrectDefinitionOcrEntity();

    public abstract void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentiment();

    public abstract void createSkillsetReturnsCorrectDefinitionOcrKeyPhrase();

    public abstract void createSkillsetReturnsCorrectDefinitionOcrShaper();

    public abstract void createSkillsetReturnsCorrectDefinitionOcrSplitText();

    public abstract void createSkillsetReturnsCorrectDefinitionWithCognitiveServicesDefault();

    public abstract void createSkillsetReturnsCorrectDefinitionWithDefaultSettings();

    public abstract void createSkillsetThrowsExceptionWithInvalidLanguageSelection();

    public abstract void createSkillsetWithCognitiveServicesKey();

    public abstract void createSkillsetWithEntityRecognitionDefaultSettings();

    public abstract void createSkillsetWithImageAnalysisDefaultSettings();

    public abstract void createSkillsetWithKeyPhraseExtractionDefaultSettings();

    public abstract void createSkillsetWithMergeDefaultSettings();

    public abstract void createSkillsetWithOcrDefaultSettings();

    public abstract void createSkillsetWithSentimentDefaultSettings();

    public abstract void createSkillsetWithSplitDefaultSettings();
}
