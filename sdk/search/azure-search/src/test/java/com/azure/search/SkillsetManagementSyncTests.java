// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.ConditionalSkill;
import com.azure.search.models.DefaultCognitiveServicesAccount;
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
import com.azure.search.models.RequestOptions;
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
import com.azure.search.test.AccessConditionTests;
import com.azure.search.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SkillsetManagementSyncTests extends SearchServiceTestBase {
    private static final String CONTEXT_VALUE = "/document";
    private static final String OCR_SKILLSET_NAME = "ocr-skillset";

    private SearchServiceClient client;

    // commonly used lambda definitions
    private BiFunction<Skillset, AccessOptions, Skillset> createOrUpdateSkillsetFunc =
        (Skillset skillset, AccessOptions ac) ->
            createSkillset(skillset, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<Skillset> newSkillsetFunc =
        () -> createSkillsetWithOcrDefaultSettings(false);

    private Function<Skillset, Skillset> mutateSkillsetFunc = this::mutateSkillsInSkillset;

    private BiConsumer<String, AccessOptions> deleteSkillsetFunc = (String name, AccessOptions ac) ->
        client.deleteSkillsetWithResponse(name, ac.getAccessCondition(), ac.getRequestOptions(), Context.NONE);

    private Skillset createSkillset(Skillset skillset, AccessCondition accessCondition, RequestOptions requestOptions) {
        return client.createOrUpdateSkillsetWithResponse(skillset, accessCondition, requestOptions, Context.NONE)
            .getValue();
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhrase() {
        Skillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhraseWithResponse() {
        Skillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();
        Response<Skillset> skillsetResponse = client.createSkillsetWithResponse(expectedSkillset,
            generateRequestOptions(), Context.NONE);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, skillsetResponse.getValue());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionLanguageDetection() {
        Skillset expectedSkillset = createTestSkillsetLanguageDetection();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionMergeText() {
        Skillset expectedSkillset = createTestSkillsetMergeText();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrEntity() {
        Skillset expectedSkillset = createTestSkillsetOcrEntity(null, null);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);

        List<EntityCategory> entityCategories = Arrays.asList(
            EntityCategory.LOCATION, EntityCategory.ORGANIZATION, EntityCategory.PERSON);

        expectedSkillset = createTestSkillsetOcrEntity(TextExtractionAlgorithm.PRINTED, entityCategories)
            .setName("testskillset1");
        actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentiment() {
        Skillset expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.PT,
            SentimentSkillLanguage.PT_PT, TextExtractionAlgorithm.PRINTED);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);

        expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.FI,
            SentimentSkillLanguage.FI, TextExtractionAlgorithm.PRINTED).setName("testskillset1");
        actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);

        expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.EN,
            SentimentSkillLanguage.EN, TextExtractionAlgorithm.HANDWRITTEN).setName("testskillset2");
        actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrKeyPhrase() {
        Skillset expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.EN,
            KeyPhraseExtractionSkillLanguage.EN);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);

        expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.FR, KeyPhraseExtractionSkillLanguage.FR)
            .setName("testskillset1");
        actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);

        expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.ES, KeyPhraseExtractionSkillLanguage.ES)
            .setName("testskillset2");
        actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrShaper() {
        Skillset expectedSkillset = createTestSkillsetOcrShaper();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrSplitText() {
        Skillset expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.EN,
            SplitSkillLanguage.EN, TextSplitMode.PAGES);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);


        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.FR,
            SplitSkillLanguage.FR, TextSplitMode.PAGES).setName("testskillset1");
        actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);

        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.FI,
            SplitSkillLanguage.FI, TextSplitMode.SENTENCES).setName("testskillset2");
        actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
        client.deleteSkillset(expectedSkillset.getName());

        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.DA,
            SplitSkillLanguage.DA, TextSplitMode.SENTENCES).setName("testskillset3");
        actualSkillset = client.createSkillset(expectedSkillset);
        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithCognitiveServicesDefault() {
        Skillset expectedSkillset = createSkillsetWithCognitiveServicesKey();

        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithOcrDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithOcrDefaultSettings(false);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithImageAnalysisDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithImageAnalysisDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithKeyPhraseExtractionDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithKeyPhraseExtractionDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithMergeDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithMergeDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithEntityRecognitionDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);

        Skillset actual = client.getSkillset(expected.getName());
        TestHelpers.assertSkillsetsEqual(expected, actual);
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinitionWithResponse() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);

        Skillset actual = client.getSkillsetWithResponse(expected.getName(), generateRequestOptions(), Context.NONE)
            .getValue();
        TestHelpers.assertSkillsetsEqual(expected, actual);
    }

    @Test
    public void getOcrSkillsetWithShouldDetectOrientationReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(true);

        client.createSkillset(expected);

        Skillset actual = client.getSkillset(expected.getName());

        TestHelpers.assertSkillsetsEqual(expected, actual);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSentimentDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithSentimentDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSplitDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithSplitDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        TestHelpers.assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Test
    public void createCustomSkillsetReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithCustomSkills();
        Skillset actual = client.createSkillset(expected);

        TestHelpers.assertSkillsetsEqual(expected, actual);
    }

    @Test
    public void getSkillsetThrowsOnNotFound() {
        assertHttpResponseException(
            () -> client.getSkillset("thisdoesnotexist"),
            HttpResponseStatus.NOT_FOUND,
            "No skillset with the name 'thisdoesnotexist' was found in service"
        );
    }

    @Test
    public void canCreateAndListSkillsets() {
        Skillset skillset1 = createSkillsetWithCognitiveServicesKey();
        Skillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        client.createSkillset(skillset1);
        client.createSkillset(skillset2);

        PagedIterable<Skillset> actual = client.listSkillsets();
        List<Skillset> result = actual.stream().collect(Collectors.toList());

        assertEquals(2, result.size());
        assertEquals(skillset1.getName(), result.get(0).getName());
        assertEquals(skillset2.getName(), result.get(1).getName());
    }

    @Test
    public void canListSkillsetsWithSelectedField() {
        Skillset skillset1 = createSkillsetWithCognitiveServicesKey();
        Skillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        client.createSkillset(skillset1);
        client.createSkillset(skillset2);

        PagedIterable<Skillset> selectedFieldListResponse = client.listSkillsets("name", generateRequestOptions(), Context.NONE);
        List<Skillset> result = selectedFieldListResponse.stream().collect(Collectors.toList());

        result.forEach(res -> {
            assertNotNull(res.getName());
            assertNull(res.getCognitiveServicesAccount());
            assertNull(res.getDescription());
            assertNull(res.getSkills());
            assertNull(res.getETag());
        });

        assertEquals(2, result.size());
        assertEquals(result.get(0).getName(), skillset1.getName());
        assertEquals(result.get(1).getName(), skillset2.getName());
    }

    @Test
    public void deleteSkillsetIsIdempotent() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(false);

        Response<Void> deleteResponse = client.deleteSkillsetWithResponse(skillset.getName(), new AccessCondition(),
            generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());

        client.createSkillset(skillset);

        // Delete the same skillset twice
        deleteResponse = client.deleteSkillsetWithResponse(skillset.getName(), new AccessCondition(),
            generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NO_CONTENT.code(), deleteResponse.getStatusCode());

        deleteResponse = client.deleteSkillsetWithResponse(skillset.getName(), new AccessCondition(),
            generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteSkillset() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);
        client.deleteSkillset(expected.getName());

        assertThrows(HttpResponseException.class, () -> client.getSkillset(expected.getName()));
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExist() {
        Skillset expected = createTestOcrSkillSet(1, TextExtractionAlgorithm.PRINTED);

        Skillset actual = client.createOrUpdateSkillset(expected);
        TestHelpers.assertSkillsetsEqual(expected, actual);
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistWithResponse() {
        Skillset expected = createTestOcrSkillSet(1, TextExtractionAlgorithm.PRINTED);

        Response<Skillset> createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(expected,
            new AccessCondition(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateUpdatesWhenSkillsetExists() {
        Skillset skillset = createTestOcrSkillSet(1, TextExtractionAlgorithm.HANDWRITTEN);

        Response<Skillset> createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(skillset,
            new AccessCondition(), generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());

        skillset = createTestOcrSkillSet(2, TextExtractionAlgorithm.PRINTED);
        createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(skillset, new AccessCondition(),
            generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.OK.code(), createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateUpdatesSkills() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(false);

        Skillset createdSkillset = client.createSkillset(skillset);

        // update skills
        createdSkillset.setSkills(getCreateOrUpdateSkills());

        TestHelpers.assertSkillsetsEqual(createdSkillset, client.createOrUpdateSkillset(createdSkillset));
    }


    @Test
    public void createOrUpdateUpdatesCognitiveService() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(false);

        Skillset createdSkillset = client.createSkillset(skillset);

        // update skills
        createdSkillset.setCognitiveServicesAccount(new DefaultCognitiveServicesAccount().setDescription("description"));

        TestHelpers.assertSkillsetsEqual(createdSkillset, client.createOrUpdateSkillset(createdSkillset));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionShaperWithNestedInputs() {
        Skillset expected = createSkillsetWithSharperSkillWithNestedInputs();
        Skillset actual = client.createSkillset(expected);

        TestHelpers.assertSkillsetsEqual(expected, actual);
    }

    // TODO (alzimmer): This test doesn't complete as expected, follow-up with a fix for it.
    //@Test
    public void createSkillsetThrowsExceptionWithNonShaperSkillWithNestedInputs() {
        List<InputFieldMappingEntry> inputs = this.createNestedInputFieldMappingEntry();
        List<OutputFieldMappingEntry> outputs = this.createOutputFieldMappingEntry();

        List<Skill> skills = new ArrayList<>();
        // Used for testing skill that shouldn't allow nested inputs
        skills.add(new WebApiSkill().setUri("https://contoso.example.org")
            .setDescription("Invalid skill with nested inputs")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        Skillset skillset = new Skillset()
            .setName("nested-skillset-with-nonsharperskill")
            .setDescription("Skillset for testing")
            .setSkills(skills);

        assertHttpResponseException(
            () -> client.createSkillset(skillset),
            HttpResponseStatus.BAD_REQUEST,
            "Skill '#1' is not allowed to have recursively defined inputs");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionConditional() {
        Skillset expected = createTestSkillsetConditional();
        Skillset actual = client.createSkillset(expected);

        TestHelpers.assertSkillsetsEqual(expected, actual);
    }

    @Test
    public void createOrUpdateSkillsetIfNotExistsFailsOnExistingResource() {
        AccessConditionTests.createOrUpdateIfNotExistsFailsOnExistingResource(createOrUpdateSkillsetFunc,
            newSkillsetFunc, mutateSkillsetFunc);
    }

    @Test
    public void createOrUpdateSkillsetIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests.createOrUpdateIfNotExistsSucceedsOnNoResource(createOrUpdateSkillsetFunc, newSkillsetFunc);
    }

    @Test
    public void createOrUpdateSkillsetIfExistsSucceedsOnExistingResource() {
        AccessConditionTests.updateIfExistsSucceedsOnExistingResource(newSkillsetFunc, createOrUpdateSkillsetFunc,
            mutateSkillsetFunc);
    }

    @Test
    public void createOrUpdateSkillsetIfExistsFailsOnNoResource() {
        AccessConditionTests.updateIfExistsFailsOnNoResource(newSkillsetFunc, createOrUpdateSkillsetFunc);
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedSucceedsWhenResourceUnchanged() {
        AccessConditionTests.updateIfNotChangedSucceedsWhenResourceUnchanged(newSkillsetFunc,
            createOrUpdateSkillsetFunc, mutateSkillsetFunc);
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedFailsWhenResourceChanged() {
        AccessConditionTests.updateIfNotChangedFailsWhenResourceChanged(newSkillsetFunc, createOrUpdateSkillsetFunc,
            mutateSkillsetFunc);
    }

    @Test
    public void deleteSkillsetIfNotChangedWorksOnlyOnCurrentResource() {
        AccessConditionTests.deleteIfNotChangedWorksOnlyOnCurrentResource(deleteSkillsetFunc, newSkillsetFunc,
            createOrUpdateSkillsetFunc, OCR_SKILLSET_NAME);
    }

    @Test
    public void deleteSkillsetIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests.deleteIfExistsWorksOnlyWhenResourceExists(deleteSkillsetFunc, createOrUpdateSkillsetFunc,
            newSkillsetFunc, OCR_SKILLSET_NAME);
    }

    private InputFieldMappingEntry simpleInputFieldMappingEntry(String name, String source) {
        return new InputFieldMappingEntry().setName(name).setSource(source);
    }

    private OutputFieldMappingEntry createOutputFieldMappingEntry(String name, String targetName) {
        return new OutputFieldMappingEntry().setName(name).setTargetName(targetName);
    }

    Skillset createTestSkillsetImageAnalysisKeyPhrase() {
        List<Skill> skills = new ArrayList<>();

        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString")
        );

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("description", "mydescription"));

        skills.add(new ImageAnalysisSkill()
            .setVisualFeatures(Arrays.asList(VisualFeature.values()))
            .setDetails(Arrays.asList(ImageDetail.values()))
            .setDefaultLanguageCode(ImageAnalysisSkillLanguage.EN)
            .setName("myimage")
            .setDescription("Tested image analysis skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mydescription/*/Tags/*"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"));
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
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/text"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("languageCode", "myLanguageCode"));

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
            simpleInputFieldMappingEntry("text", "/document/text"),
            simpleInputFieldMappingEntry("itemsToInsert", "/document/textitems"),
            simpleInputFieldMappingEntry("offsets", "/document/offsets"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("mergedText", "myMergedText"));

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
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill()
            .setTextExtractionAlgorithm(TextExtractionAlgorithm.PRINTED)
            .setDefaultLanguageCode(OcrSkillLanguage.EN)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("output", "myOutput"));
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
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

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
            .setCognitiveServicesAccount(new DefaultCognitiveServicesAccount());
    }

    Skillset createTestSkillsetConditional() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("condition", "= $(/document/language) == null"),
            simpleInputFieldMappingEntry("whenTrue", "= 'es'"),
            simpleInputFieldMappingEntry("whenFalse", "= $(/document/language)"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("output", "myLanguageCode"));

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
        return skillset.setSkills(Collections.singletonList(
            new KeyPhraseExtractionSkill()
                .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
                .setName("mykeyphrases")
                .setDescription("Tested Key Phrase skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(Collections
                    .singletonList(simpleInputFieldMappingEntry("text", "/document/mydescription/*/Tags/*")))
                .setOutputs(Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases")))
        ));
    }

    Skillset createTestSkillsetOcrEntity(TextExtractionAlgorithm algorithm, List<EntityCategory> categories) {
        List<Skill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill()
            .setTextExtractionAlgorithm(algorithm)
            .setDefaultLanguageCode(OcrSkillLanguage.EN)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("entities", "myEntities"));
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
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));
        skills.add(new OcrSkill()
            .setTextExtractionAlgorithm(algorithm)
            .setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("score", "mySentiment"));
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
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill()
            .setTextExtractionAlgorithm(TextExtractionAlgorithm.PRINTED)
            .setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"));
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
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill()
            .setTextExtractionAlgorithm(TextExtractionAlgorithm.PRINTED)
            .setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));
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

    Skillset createTestOcrSkillSet(int repeat, TextExtractionAlgorithm algorithm) {
        List<Skill> skills = new ArrayList<>();

        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        for (int i = 0; i < repeat; i++) {
            List<OutputFieldMappingEntry> outputs = Collections
                .singletonList(createOutputFieldMappingEntry("text", "mytext" + i));

            skills.add(new OcrSkill()
                .setDefaultLanguageCode(OcrSkillLanguage.EN)
                .setTextExtractionAlgorithm(algorithm)
                .setShouldDetectOrientation(false)
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

    Skillset createSkillsetWithOcrDefaultSettings(Boolean shouldDetectOrientation) {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

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
            .setName(SkillsetManagementSyncTests.OCR_SKILLSET_NAME)
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    Skillset createSkillsetWithImageAnalysisDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("description", "mydescription"));

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
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/myText"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"));

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
            simpleInputFieldMappingEntry("text", "/document/text"),
            simpleInputFieldMappingEntry("itemsToInsert", "/document/textitems"),
            simpleInputFieldMappingEntry("offsets", "/document/offsets"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("mergedText", "myMergedText"));

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
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("score", "mySentiment"));

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
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("entities", "myEntities"));

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
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));

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

        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));

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

    private List<InputFieldMappingEntry> createNestedInputFieldMappingEntry() {
        return Collections.singletonList(
            new InputFieldMappingEntry()
                .setName("doc")
                .setSourceContext("/document")
                .setInputs(Arrays.asList(
                    simpleInputFieldMappingEntry("text", "/document/content"),
                    simpleInputFieldMappingEntry("images", "/document/normalized_images/*")))
        );
    }

    private List<OutputFieldMappingEntry> createOutputFieldMappingEntry() {
        return Collections.singletonList(createOutputFieldMappingEntry("output", "myOutput"));
    }


    protected List<Skill> getCreateOrUpdateSkills() {
        return Collections.singletonList(new KeyPhraseExtractionSkill()
            .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
            .setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext")))
            .setOutputs(Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"))));
    }
}
