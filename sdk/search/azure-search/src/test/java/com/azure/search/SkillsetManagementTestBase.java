// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.DefaultCognitiveServices;
import com.azure.search.models.EntityCategory;
import com.azure.search.models.EntityRecognitionSkill;
import com.azure.search.models.EntityRecognitionSkillLanguage;
import com.azure.search.models.ImageAnalysisSkill;
import com.azure.search.models.ImageAnalysisSkillLanguage;
import com.azure.search.models.ImageDetail;
import com.azure.search.models.InputFieldMappingEntry;
import com.azure.search.models.KeyPhraseExtractionSkill;
import com.azure.search.models.KeyPhraseExtractionSkillLanguage;
import com.azure.search.models.LanguageDetectionSkill;
import com.azure.search.models.MergeSkill;
import com.azure.search.models.OcrSkill;
import com.azure.search.models.OcrSkillLanguage;
import com.azure.search.models.OutputFieldMappingEntry;
import com.azure.search.models.SentimentSkill;
import com.azure.search.models.SentimentSkillLanguage;
import com.azure.search.models.ShaperSkill;
import com.azure.search.models.Skill;
import com.azure.search.models.Skillset;
import com.azure.search.models.SplitSkill;
import com.azure.search.models.SplitSkillLanguage;
import com.azure.search.models.TextExtractionAlgorithm;
import com.azure.search.models.TextSplitMode;
import com.azure.search.models.VisualFeature;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public abstract class SkillsetManagementTestBase extends SearchServiceTestBase {

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhrase();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionLanguageDetection();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionMergeText();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionOcrEntity();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentiment();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionOcrKeyPhrase();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionOcrShaper();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionOcrSplitText();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionWithCognitiveServicesDefault();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionWithOcrDefaultSettings();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionWithImageAnalysisDefaultSettings();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionWithKeyPhraseExtractionDefaultSettings();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionWithMergeDefaultSettings();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionDefaultSettings();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionWithSentimentDefaultSettings();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionWithSplitDefaultSettings();

    @Test
    public abstract void deleteSkillsetIsIdempotent();

    protected void assertSkillsetsEqual(Skillset expected, Skillset actual) {
        expected.setETag("none");
        actual.setETag("none");
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    static final String CONTEXT_VALUE = "/document";

    protected Skillset createTestSkillsetImageAnalysisKeyPhrase() {
        List<Skill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry()
                .setName("queryString")
                .setSource("/document/queryString")
        );
        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("description")
                .setTargetName("mydescription"));
        skills.add(new ImageAnalysisSkill()
            .setVisualFeatures(Arrays.asList(VisualFeature.values()))
            .setDetails(Arrays.asList(ImageDetail.values()))
            .setDefaultLanguageCode(ImageAnalysisSkillLanguage.EN)
            .setName("myimage")
            .setDescription("Tested image analysis skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/mydescription/*/Tags/*")
        );
        outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("keyPhrases")
                .setTargetName("myKeyPhrases")
        );
        skills.add(new KeyPhraseExtractionSkill()
            .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
            .setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    protected Skillset createTestSkillsetLanguageDetection() {
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/text")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("languageCode")
                .setTargetName("myLanguageCode")
        );

        List<Skill> skills = Collections.singletonList(
            new LanguageDetectionSkill()
                .setName("mylanguage")
                .setDescription("Tested Language Detection skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs));

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    protected Skillset createTestSkillsetMergeText() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/text"),
            new InputFieldMappingEntry()
                .setName("itemsToInsert")
                .setSource("/document/textitems"),
            new InputFieldMappingEntry()
                .setName("offsets")
                .setSource("/document/offsets")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("mergedText")
                .setTargetName("myMergedText")
        );

        List<Skill> skills = Collections.singletonList(
            new MergeSkill()
                .setInsertPostTag("__e")
                .setInsertPreTag("__")
                .setName("mymerge")
                .setDescription("Tested Merged Text skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs));

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    protected Skillset createTestSkillsetOcrShaper() {
        List<Skill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry().setName("queryString")
                .setSource("/document/queryString")
        );
        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext")
        );
        skills.add(new OcrSkill()
            .setTextExtractionAlgorithm(TextExtractionAlgorithm.PRINTED)
            .setDefaultLanguageCode(OcrSkillLanguage.EN)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/mytext")
        );
        outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("output")
                .setTargetName("myOutput")
        );
        skills.add(new ShaperSkill()
            .setName("mysharper")
            .setDescription("Tested Shaper skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    protected Skillset createSkillsetWithCognitiveServicesKey() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry().setName("queryString")
                .setSource("/document/queryString")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext")
        );

        List<Skill> skills = Collections.singletonList(
            new OcrSkill()
                .setTextExtractionAlgorithm(TextExtractionAlgorithm.PRINTED)
                .setDefaultLanguageCode(OcrSkillLanguage.EN)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing")
            .setSkills(skills)
            .setCognitiveServices(new DefaultCognitiveServices());
    }

    protected Skillset createTestSkillsetOcrEntity(TextExtractionAlgorithm algorithm, List<EntityCategory> categories) {
        List<Skill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry().setName("queryString")
                .setSource("/document/queryString")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext")
        );
        skills.add(new OcrSkill()
            .setTextExtractionAlgorithm(algorithm)
            .setDefaultLanguageCode(OcrSkillLanguage.EN)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/mytext")
        );
        outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("entities")
                .setTargetName("myEntities")
        );
        skills.add(new EntityRecognitionSkill()
            .setCategories(categories)
            .setDefaultLanguageCode(EntityRecognitionSkillLanguage.EN)
            .setMinimumPrecision(0.5)
            .setName("myentity")
            .setDescription("Tested Entity Recognition skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs)
        );

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    protected Skillset createTestSkillsetOcrSentiment(OcrSkillLanguage ocrLanguageCode, SentimentSkillLanguage sentimentLanguageCode, TextExtractionAlgorithm algorithm) {
        List<Skill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry().setName("queryString")
                .setSource("/document/queryString")
        );
        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext")
        );
        skills.add(new OcrSkill()
            .setTextExtractionAlgorithm(algorithm)
            .setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/mytext")
        );
        outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("score")
                .setTargetName("mySentiment")
        );
        skills.add(new SentimentSkill()
            .setDefaultLanguageCode(sentimentLanguageCode)
            .setName("mysentiment")
            .setDescription("Tested Sentiment skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    protected Skillset createTestSkillsetOcrKeyPhrase(OcrSkillLanguage ocrLanguageCode, KeyPhraseExtractionSkillLanguage keyPhraseLanguageCode) {
        List<Skill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry().setName("queryString")
                .setSource("/document/queryString")
        );
        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext")
        );
        skills.add(new OcrSkill()
            .setTextExtractionAlgorithm(TextExtractionAlgorithm.PRINTED)
            .setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/mytext")
        );
        outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("keyPhrases")
                .setTargetName("myKeyPhrases")
        );
        skills.add(new KeyPhraseExtractionSkill()
            .setDefaultLanguageCode(keyPhraseLanguageCode)
            .setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    protected Skillset createTestSkillsetOcrSplitText(OcrSkillLanguage ocrLanguageCode, SplitSkillLanguage splitLanguageCode, TextSplitMode textSplitMode) {
        List<Skill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry().setName("queryString")
                .setSource("/document/queryString")
        );
        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext")
        );
        skills.add(new OcrSkill()
            .setTextExtractionAlgorithm(TextExtractionAlgorithm.PRINTED)
            .setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/mytext")
        );
        outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("textItems")
                .setTargetName("myTextItems")
        );
        skills.add(new SplitSkill()
            .setDefaultLanguageCode(splitLanguageCode)
            .setTextSplitMode(textSplitMode)
            .setName("mysplit")
            .setDescription("Tested Split skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    protected Skillset createSkillsetWithOcrDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry().setName("queryString")
                .setSource("/document/queryString")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("text")
                .setTargetName("mytext")
        );

        List<Skill> skills = Collections.singletonList(
            new OcrSkill()
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    protected Skillset createSkillsetWithImageAnalysisDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry().setName("queryString")
                .setSource("/document/queryString")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
             new OutputFieldMappingEntry()
                 .setName("description")
                 .setTargetName("mydescription")
        );

        List<Skill> skills = Collections.singletonList(
            new ImageAnalysisSkill()
                .setName("myimage")
                .setDescription("Tested image analysis skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    protected Skillset createSkillsetWithKeyPhraseExtractionDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/myText")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("keyPhrases")
                .setTargetName("myKeyPhrases")
        );

        List<Skill> skills = Collections.singletonList(
            new KeyPhraseExtractionSkill()
                .setName("mykeyphrases")
                .setDescription("Tested Key Phrase skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    protected Skillset createSkillsetWithMergeDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/text"),
            new InputFieldMappingEntry()
                .setName("itemsToInsert")
                .setSource("/document/textitems"),
            new InputFieldMappingEntry()
                .setName("offsets")
                .setSource("/document/offsets")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("mergedText")
                .setTargetName("myMergedText")
        );

        List<Skill> skills = Collections.singletonList(
            new MergeSkill()
                .setName("mymerge")
                .setDescription("Tested Merged Text skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    protected Skillset createSkillsetWithSentimentDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/mytext")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("score")
                .setTargetName("mySentiment")
        );

        List<Skill> skills = Collections.singletonList(
            new SentimentSkill()
                .setName("mysentiment")
                .setDescription("Tested Sentiment skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    protected Skillset createSkillsetWithEntityRecognitionDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/mytext")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("entities")
                .setTargetName("myEntities")
        );

        List<Skill> skills = Collections.singletonList(
            new EntityRecognitionSkill()
                .setName("myentity")
                .setDescription("Tested Entity Recognition skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    protected Skillset createSkillsetWithSplitDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("text")
                .setSource("/document/mytext")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("textItems")
                .setTargetName("myTextItems")
        );

        List<Skill> skills = Collections.singletonList(
            new SplitSkill()
                .setTextSplitMode(TextSplitMode.PAGES)
                .setName("mysplit")
                .setDescription("Tested Split skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }
}
