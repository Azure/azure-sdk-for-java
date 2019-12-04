// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.ConditionalSkill;
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
import com.azure.search.models.WebApiSkill;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public abstract class SkillsetManagementTestBase extends SearchServiceTestBase {

    static final String CONTEXT_VALUE = "/document";

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
    public abstract void getOcrSkillsetReturnsCorrectDefinition();

    @Test
    public abstract void getOcrSkillsetWithShouldDetectOrientationReturnsCorrectDefinition();

    @Test
    public abstract void getSkillsetThrowsOnNotFound();

    @Test
    public abstract void canCreateAndListSkillsets();

    @Test
    public abstract void canListSkillsetsWithSelectedField();

    @Test
    public abstract void deleteSkillsetIsIdempotent();

    @Test
    public abstract void canCreateAndDeleteSkillset();

    @Test
    public abstract void createOrUpdateCreatesWhenSkillsetDoesNotExist();

    @Test
    public abstract void createOrUpdateUpdatesWhenSkillsetExists();

    @Test
    public abstract void existsReturnsFalseForNonExistingSkillset();

    @Test
    public abstract void existsReturnsTrueForExistingSkillset();

    @Test
    public abstract void createCustomSkillsetReturnsCorrectDefinition();

    @Test
    public abstract void createOrUpdateUpdatesSkills();

    @Test
    public abstract void createOrUpdateUpdatesCognitiveService();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionShaperWithNestedInputs();

    @Test
    public abstract void createSkillsetThrowsExceptionWithNonShaperSkillWithNestedInputs();

    @Test
    public abstract void createSkillsetReturnsCorrectDefinitionConditional();

    @Test
    public abstract void createOrUpdateSkillsetIfNotExistsFailsOnExistingResource();

    @Test
    public abstract void createOrUpdateSkillsetIfNotExistsSucceedsOnNoResource();

    @Test
    public abstract void createOrUpdateSkillsetIfExistsSucceedsOnExistingResource() throws NoSuchFieldException, IllegalAccessException;

    @Test
    public abstract void createOrUpdateSkillsetIfExistsFailsOnNoResource() throws NoSuchFieldException, IllegalAccessException;

    @Test
    public abstract void createOrUpdateSkillsetIfNotChangedSucceedsWhenResourceUnchanged() throws NoSuchFieldException, IllegalAccessException;

    @Test
    public abstract void createOrUpdateSkillsetIfNotChangedFailsWhenResourceChanged() throws NoSuchFieldException, IllegalAccessException;

    @Test
    public abstract void deleteSkillsetIfNotChangedWorksOnlyOnCurrentResource() throws NoSuchFieldException, IllegalAccessException;

    @Test
    public abstract void deleteSkillsetIfExistsWorksOnlyWhenResourceExists();

    void assertSkillsetsEqual(Skillset expected, Skillset actual) {
        expected.setETag("none");
        actual.setETag("none");
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    Skillset createTestSkillsetImageAnalysisKeyPhrase() {
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
            .setName("image-analysis-key-phrase-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    Skillset createTestSkillsetLanguageDetection() {
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
            .setName("language-detection-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    Skillset createTestSkillsetMergeText() {
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
            .setName("merge-text-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    Skillset createTestSkillsetOcrShaper() {
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
            .setName("ocr-shaper-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    Skillset createSkillsetWithCognitiveServicesKey() {
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
            .setName("cognitive-services-key-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills)
            .setCognitiveServices(new DefaultCognitiveServices());
    }

    Skillset createTestSkillsetConditional() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("condition")
                .setSource("= $(/document/language) == null"),
            new InputFieldMappingEntry()
                .setName("whenTrue")
                .setSource("= 'es'"),
            new InputFieldMappingEntry()
                .setName("whenFalse")
                .setSource("= $(/document/language)")
        );

        List<OutputFieldMappingEntry> outputs = Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("output")
                .setTargetName("myLanguageCode")
        );

        List<Skill> skills = Collections.singletonList(
            new ConditionalSkill()
                .setName("myconditional")
                .setDescription("Tested Conditional skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new Skillset()
            .setName("conditional-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    Skillset mutateSkillsInSkillset(Skillset skillset) {
        skillset.setSkills(Collections.singletonList(
            new KeyPhraseExtractionSkill()
                .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
                .setName("mykeyphrases")
                .setDescription("Tested Key Phrase skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(Collections.singletonList(
                    new InputFieldMappingEntry()
                        .setName("text")
                        .setSource("/document/mydescription/*/Tags/*")))
                .setOutputs(Collections.singletonList(
                    new OutputFieldMappingEntry()
                        .setName("keyPhrases")
                        .setTargetName("myKeyPhrases")
                ))
        ));
        return skillset;
    }

    Skillset createTestSkillsetOcrEntity(TextExtractionAlgorithm algorithm, List<EntityCategory> categories) {
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
            .setName("ocr-entity-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    Skillset createTestSkillsetOcrSentiment(OcrSkillLanguage ocrLanguageCode, SentimentSkillLanguage sentimentLanguageCode, TextExtractionAlgorithm algorithm) {
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
            .setName("ocr-sentiment-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    Skillset createTestSkillsetOcrKeyPhrase(OcrSkillLanguage ocrLanguageCode, KeyPhraseExtractionSkillLanguage keyPhraseLanguageCode) {
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
            .setName("ocr-key-phrase-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    Skillset createTestSkillsetOcrSplitText(OcrSkillLanguage ocrLanguageCode, SplitSkillLanguage splitLanguageCode, TextSplitMode textSplitMode) {
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
            .setName("ocr-split-text-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    Skillset createTestOcrSkillSet(int repeat, TextExtractionAlgorithm algorithm, boolean shouldDetectOrientation) {
        List<Skill> skills = new ArrayList<>();

        List<InputFieldMappingEntry> inputs = Arrays.asList(
            new InputFieldMappingEntry()
                .setName("url")
                .setSource("/document/url"),
            new InputFieldMappingEntry().setName("queryString")
                .setSource("/document/queryString")
        );

        for (int i = 0; i < repeat; i++) {
            List<OutputFieldMappingEntry> outputs = Collections.singletonList(
                new OutputFieldMappingEntry()
                    .setName("text")
                    .setTargetName("mytext" + i)
            );

            skills.add(new OcrSkill()
                .setDefaultLanguageCode(OcrSkillLanguage.EN)
                .setTextExtractionAlgorithm(algorithm)
                .setShouldDetectOrientation(shouldDetectOrientation)
                .setName("myocr-" + i)
                .setDescription("Tested OCR skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs));
        }

        return new Skillset()
            .setName("testskillset")
            .setDescription("Skillset for testing OCR")
            .setSkills(skills);
    }

    Skillset createSkillsetWithOcrDefaultSettings(String skillsetName, Boolean shouldDetectOrientation) {
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
                .setShouldDetectOrientation(shouldDetectOrientation)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new Skillset()
            .setName(skillsetName)
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    Skillset createSkillsetWithImageAnalysisDefaultSettings() {
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
            .setName("image-analysis-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    Skillset createSkillsetWithKeyPhraseExtractionDefaultSettings() {
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
            .setName("key-phrase-extraction-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    Skillset createSkillsetWithMergeDefaultSettings() {
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
            .setName("merge-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    Skillset createSkillsetWithSentimentDefaultSettings() {
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
            .setName("sentiment-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    Skillset createSkillsetWithEntityRecognitionDefaultSettings() {
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
            .setName("entity-recognition-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    Skillset createSkillsetWithSplitDefaultSettings() {
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
            .setName("split-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    Skillset createSkillsetWithCustomSkills() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", "foobar");

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

        Skill webApiSkill = new WebApiSkill()
            .setUri("https://indexer-e2e-webskill.azurewebsites.net/api/InvokeTextAnalyticsV3?code=foo")
            .setHttpMethod("POST")
            .setHttpHeaders(headers)
            .setInputs(inputs)
            .setOutputs(outputs)
            .setName("webapi-skill")
            .setDescription("Calls an Azure function, which in turn calls Bing Entity Search");

        return new Skillset()
            .setName("custom-skillset")
            .setDescription("Skillset for testing custom skillsets")
            .setSkills(Collections.singletonList(webApiSkill));
    }

    Skillset createSkillsetWithSharperSkillWithNestedInputs() {
        List<InputFieldMappingEntry> inputs = this.createNestedInputFieldMappingEntry();
        List<OutputFieldMappingEntry> outputs = this.createOutputFieldMappingEntry();

        List<Skill> skills = new ArrayList<>();
        skills.add(new ShaperSkill()
            .setName("myshaper")
            .setDescription("Tested Shaper skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs)
        );

        return new Skillset()
            .setName("nested-skillset-with-sharperskill")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    Skillset createSkillsetWithNonSharperSkillWithNestedInputs() {
        List<InputFieldMappingEntry> inputs = this.createNestedInputFieldMappingEntry();
        List<OutputFieldMappingEntry> outputs = this.createOutputFieldMappingEntry();

        List<Skill> skills = new ArrayList<>();
        // Used for testing skill that shouldn't allow nested inputs
        skills.add(new WebApiSkill()
            .setUri("https://contoso.example.org")
            .setDescription("Invalid skill with nested inputed")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs)
        );

        return new Skillset()
            .setName("nested-skillset-with-nonsharperskill")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    private List<InputFieldMappingEntry> createNestedInputFieldMappingEntry() {
        return Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("doc")
                .setSourceContext("/document")
                .setInputs(Arrays.asList(
                    new InputFieldMappingEntry()
                        .setName("text")
                        .setSource("/document/content"),
                    new InputFieldMappingEntry()
                        .setName("images")
                        .setSource("/document/normalized_images/*"))
                )
        );
    }

    private List<OutputFieldMappingEntry> createOutputFieldMappingEntry() {
        return Collections.singletonList(
            new OutputFieldMappingEntry()
                .setName("output")
                .setTargetName("myOutput")
        );
    }
}
