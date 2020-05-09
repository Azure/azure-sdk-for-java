// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexerSkillsetClient;
import com.azure.search.documents.models.ConditionalSkill;
import com.azure.search.documents.models.DefaultCognitiveServicesAccount;
import com.azure.search.documents.models.EntityCategory;
import com.azure.search.documents.models.EntityRecognitionSkill;
import com.azure.search.documents.models.EntityRecognitionSkillLanguage;
import com.azure.search.documents.models.ImageAnalysisSkill;
import com.azure.search.documents.models.ImageAnalysisSkillLanguage;
import com.azure.search.documents.models.ImageDetail;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.KeyPhraseExtractionSkill;
import com.azure.search.documents.models.KeyPhraseExtractionSkillLanguage;
import com.azure.search.documents.models.LanguageDetectionSkill;
import com.azure.search.documents.models.MergeSkill;
import com.azure.search.documents.models.OcrSkill;
import com.azure.search.documents.models.OcrSkillLanguage;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchIndexerSkill;
import com.azure.search.documents.models.SearchIndexerSkillset;
import com.azure.search.documents.models.SentimentSkill;
import com.azure.search.documents.models.SentimentSkillLanguage;
import com.azure.search.documents.models.ShaperSkill;
import com.azure.search.documents.models.SplitSkill;
import com.azure.search.documents.models.SplitSkillLanguage;
import com.azure.search.documents.models.TextExtractionAlgorithm;
import com.azure.search.documents.models.TextSplitMode;
import com.azure.search.documents.models.VisualFeature;
import com.azure.search.documents.models.WebApiSkill;
import com.azure.search.documents.test.AccessConditionTests;
import com.azure.search.documents.test.AccessOptions;
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

import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SkillsetManagementSyncTests extends SearchServiceTestBase {
    private static final String CONTEXT_VALUE = "/document";
    private static final String OCR_SKILLSET_NAME = "ocr-skillset";

    private SearchIndexerSkillsetClient skillsetClient;

    // commonly used lambda definitions
    private BiFunction<SearchIndexerSkillset, AccessOptions, SearchIndexerSkillset> createOrUpdateSkillsetFunc =
        (SearchIndexerSkillset skillset, AccessOptions ac) ->
            createSkillset(skillset, ac.getOnlyIfUnchanged(), ac.getRequestOptions());

    private Supplier<SearchIndexerSkillset> newSkillsetFunc =
        () -> createSkillsetWithOcrDefaultSettings(false);

    private Function<SearchIndexerSkillset, SearchIndexerSkillset> mutateSkillsetFunc = this::mutateSkillsInSkillset;

    private BiConsumer<SearchIndexerSkillset, AccessOptions> deleteSkillsetFunc = (SearchIndexerSkillset skillset,
        AccessOptions ac) ->
        skillsetClient.deleteWithResponse(skillset, ac.getOnlyIfUnchanged(), ac.getRequestOptions(),
            Context.NONE);

    private SearchIndexerSkillset createSkillset(SearchIndexerSkillset skillset, Boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        return skillsetClient.createOrUpdateWithResponse(skillset, onlyIfUnchanged, requestOptions, Context.NONE)
            .getValue();
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        skillsetClient = getSearchServiceClientBuilder().buildClient().getSkillsetClient();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhrase() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhraseWithResponse() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();
        Response<SearchIndexerSkillset> skillsetResponse = skillsetClient.createWithResponse(expectedSkillset,
            generateRequestOptions(), Context.NONE);

        assertObjectEquals(expectedSkillset, skillsetResponse.getValue(), true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionLanguageDetection() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetLanguageDetection();
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionMergeText() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetMergeText();
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrEntity() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetOcrEntity(null, null);
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        List<EntityCategory> entityCategories = Arrays.asList(
            EntityCategory.LOCATION, EntityCategory.ORGANIZATION, EntityCategory.PERSON);

        expectedSkillset = createTestSkillsetOcrEntity(TextExtractionAlgorithm.PRINTED, entityCategories)
            .setName("testskillset1");
        actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentiment() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.PT,
            SentimentSkillLanguage.PT_PT, TextExtractionAlgorithm.PRINTED);
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.FI,
            SentimentSkillLanguage.FI, TextExtractionAlgorithm.PRINTED).setName("testskillset1");
        actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.EN,
            SentimentSkillLanguage.EN, TextExtractionAlgorithm.HANDWRITTEN).setName("testskillset2");
        actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrKeyPhrase() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.EN,
            KeyPhraseExtractionSkillLanguage.EN);
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.FR, KeyPhraseExtractionSkillLanguage.FR)
            .setName("testskillset1");
        actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.ES, KeyPhraseExtractionSkillLanguage.ES)
            .setName("testskillset2");
        actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrShaper() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetOcrShaper();
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrSplitText() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.EN,
            SplitSkillLanguage.EN, TextSplitMode.PAGES);
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");


        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.FR,
            SplitSkillLanguage.FR, TextSplitMode.PAGES).setName("testskillset1");
        actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.FI,
            SplitSkillLanguage.FI, TextSplitMode.SENTENCES).setName("testskillset2");
        actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
        skillsetClient.delete(expectedSkillset.getName());

        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.DA,
            SplitSkillLanguage.DA, TextSplitMode.SENTENCES).setName("testskillset3");
        actualSkillset = skillsetClient.create(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithCognitiveServicesDefault() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithCognitiveServicesKey();

        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithOcrDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithOcrDefaultSettings(false);
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithImageAnalysisDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithImageAnalysisDefaultSettings();
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithKeyPhraseExtractionDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithKeyPhraseExtractionDefaultSettings();
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithMergeDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithMergeDefaultSettings();
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithEntityRecognitionDefaultSettings();
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinition() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        skillsetClient.create(expected);

        SearchIndexerSkillset actual = skillsetClient.getSkillset(expected.getName());
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinitionWithResponse() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        skillsetClient.create(expected);

        SearchIndexerSkillset actual = skillsetClient.getSkillsetWithResponse(expected.getName(), generateRequestOptions(), Context.NONE)
            .getValue();
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getOcrSkillsetWithShouldDetectOrientationReturnsCorrectDefinition() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(true);

        skillsetClient.create(expected);

        SearchIndexerSkillset actual = skillsetClient.getSkillset(expected.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSentimentDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithSentimentDefaultSettings();
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSplitDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithSplitDefaultSettings();
        SearchIndexerSkillset actualSkillset = skillsetClient.create(expectedSkillset);

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createCustomSkillsetReturnsCorrectDefinition() {
        SearchIndexerSkillset expected = createSkillsetWithCustomSkills();
        SearchIndexerSkillset actual = skillsetClient.create(expected);

        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getSkillsetThrowsOnNotFound() {
        assertHttpResponseException(
            () -> skillsetClient.getSkillset("thisdoesnotexist"),
            HttpResponseStatus.NOT_FOUND,
            "No skillset with the name 'thisdoesnotexist' was found in service"
        );
    }

    @Test
    public void canCreateAndListSkillsets() {
        SearchIndexerSkillset skillset1 = createSkillsetWithCognitiveServicesKey();
        SearchIndexerSkillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        skillsetClient.create(skillset1);
        skillsetClient.create(skillset2);

        PagedIterable<SearchIndexerSkillset> actual = skillsetClient.listSkillsets();
        List<SearchIndexerSkillset> result = actual.stream().collect(Collectors.toList());

        assertEquals(2, result.size());
        assertEquals(skillset1.getName(), result.get(0).getName());
        assertEquals(skillset2.getName(), result.get(1).getName());
    }

    @Test
    public void canListSkillsetsWithSelectedField() {
        SearchIndexerSkillset skillset1 = createSkillsetWithCognitiveServicesKey();
        SearchIndexerSkillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        skillsetClient.create(skillset1);
        skillsetClient.create(skillset2);

        PagedIterable<SearchIndexerSkillset> selectedFieldListResponse =
            skillsetClient.listSkillsetNames(generateRequestOptions(), Context.NONE);
        List<SearchIndexerSkillset> result = selectedFieldListResponse.stream().collect(Collectors.toList());

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
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        Response<Void> deleteResponse = skillsetClient.deleteWithResponse(skillset, false,
            generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());

        skillsetClient.create(skillset);

        // Delete the same skillset twice
        deleteResponse = skillsetClient.deleteWithResponse(skillset, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NO_CONTENT.code(), deleteResponse.getStatusCode());

        deleteResponse = skillsetClient.deleteWithResponse(skillset, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteSkillset() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        skillsetClient.create(expected);
        skillsetClient.delete(expected.getName());

        assertThrows(HttpResponseException.class, () -> skillsetClient.getSkillset(expected.getName()));
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExist() {
        SearchIndexerSkillset expected = createTestOcrSkillSet(1, TextExtractionAlgorithm.PRINTED);

        SearchIndexerSkillset actual = skillsetClient.createOrUpdate(expected);
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistWithResponse() {
        SearchIndexerSkillset expected = createTestOcrSkillSet(1, TextExtractionAlgorithm.PRINTED);

        Response<SearchIndexerSkillset> createOrUpdateResponse = skillsetClient.createOrUpdateWithResponse(expected,
            false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateUpdatesWhenSkillsetExists() {
        SearchIndexerSkillset skillset = createTestOcrSkillSet(1, TextExtractionAlgorithm.HANDWRITTEN);

        Response<SearchIndexerSkillset> createOrUpdateResponse = skillsetClient.createOrUpdateWithResponse(
            skillset, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());

        skillset = createTestOcrSkillSet(2, TextExtractionAlgorithm.PRINTED);
        createOrUpdateResponse = skillsetClient.createOrUpdateWithResponse(skillset, false,
            generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.OK.code(), createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateUpdatesSkills() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        SearchIndexerSkillset createdSkillset = skillsetClient.create(skillset);

        // update skills
        createdSkillset.setSkills(getCreateOrUpdateSkills());

        assertObjectEquals(createdSkillset, skillsetClient.createOrUpdate(createdSkillset), true, "etag", "@odata.etag");
    }


    @Test
    public void createOrUpdateUpdatesCognitiveService() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        SearchIndexerSkillset createdSkillset = skillsetClient.create(skillset);

        // update skills
        createdSkillset.setCognitiveServicesAccount(new DefaultCognitiveServicesAccount().setDescription("description"));

        assertObjectEquals(createdSkillset, skillsetClient.createOrUpdate(createdSkillset),
            true, "etag", "@odata.etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionShaperWithNestedInputs() {
        SearchIndexerSkillset expected = createSkillsetWithSharperSkillWithNestedInputs();
        SearchIndexerSkillset actual = skillsetClient.create(expected);

        assertObjectEquals(expected, actual, true, "etag");
    }

    // TODO (alzimmer): This test doesn't complete as expected, follow-up with a fix for it.
    //@Test
    public void createSkillsetThrowsExceptionWithNonShaperSkillWithNestedInputs() {
        List<InputFieldMappingEntry> inputs = this.createNestedInputFieldMappingEntry();
        List<OutputFieldMappingEntry> outputs = this.createOutputFieldMappingEntry();

        List<SearchIndexerSkill> skills = new ArrayList<>();
        // Used for testing skill that shouldn't allow nested inputs
        skills.add(new WebApiSkill().setUri("https://contoso.example.org")
            .setDescription("Invalid skill with nested inputs")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs));

        SearchIndexerSkillset skillset = new SearchIndexerSkillset()
            .setName("nested-skillset-with-nonsharperskill")
            .setDescription("Skillset for testing")
            .setSkills(skills);

        assertHttpResponseException(
            () -> skillsetClient.create(skillset),
            HttpResponseStatus.BAD_REQUEST,
            "SearchIndexerSkill '#1' is not allowed to have recursively defined inputs");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionConditional() {
        SearchIndexerSkillset expected = createTestSkillsetConditional();
        SearchIndexerSkillset actual = skillsetClient.create(expected);

        assertObjectEquals(expected, actual, true, "etag");
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
            newSkillsetFunc);
    }

    private InputFieldMappingEntry simpleInputFieldMappingEntry(String name, String source) {
        return new InputFieldMappingEntry().setName(name).setSource(source);
    }

    private OutputFieldMappingEntry createOutputFieldMappingEntry(String name, String targetName) {
        return new OutputFieldMappingEntry().setName(name).setTargetName(targetName);
    }

    SearchIndexerSkillset createTestSkillsetImageAnalysisKeyPhrase() {
        List<SearchIndexerSkill> skills = new ArrayList<>();

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

        return new SearchIndexerSkillset()
            .setName("image-analysis-key-phrase-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset createTestSkillsetLanguageDetection() {
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/text"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("languageCode", "myLanguageCode"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new LanguageDetectionSkill()
                .setName("mylanguage")
                .setDescription("Tested Language Detection skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs));

        return new SearchIndexerSkillset()
            .setName("language-detection-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset createTestSkillsetMergeText() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("text", "/document/text"),
            simpleInputFieldMappingEntry("itemsToInsert", "/document/textitems"),
            simpleInputFieldMappingEntry("offsets", "/document/offsets"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("mergedText", "myMergedText"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new MergeSkill()
                .setInsertPostTag("__e")
                .setInsertPreTag("__")
                .setName("mymerge")
                .setDescription("Tested Merged Text skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs));

        return new SearchIndexerSkillset()
            .setName("merge-text-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset createTestSkillsetOcrShaper() {
        List<SearchIndexerSkill> skills = new ArrayList<>();
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

        return new SearchIndexerSkillset()
            .setName("ocr-shaper-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset createSkillsetWithCognitiveServicesKey() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new OcrSkill()
                .setTextExtractionAlgorithm(TextExtractionAlgorithm.PRINTED)
                .setDefaultLanguageCode(OcrSkillLanguage.EN)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new SearchIndexerSkillset()
            .setName("cognitive-services-key-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills)
            .setCognitiveServicesAccount(new DefaultCognitiveServicesAccount());
    }

    SearchIndexerSkillset createTestSkillsetConditional() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("condition", "= $(/document/language) == null"),
            simpleInputFieldMappingEntry("whenTrue", "= 'es'"),
            simpleInputFieldMappingEntry("whenFalse", "= $(/document/language)"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("output", "myLanguageCode"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new ConditionalSkill()
                .setName("myconditional")
                .setDescription("Tested Conditional skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new SearchIndexerSkillset()
            .setName("conditional-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset mutateSkillsInSkillset(SearchIndexerSkillset skillset) {
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

    SearchIndexerSkillset createTestSkillsetOcrEntity(TextExtractionAlgorithm algorithm, List<EntityCategory> categories) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
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

        return new SearchIndexerSkillset()
            .setName("ocr-entity-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset createTestSkillsetOcrSentiment(OcrSkillLanguage ocrLanguageCode, SentimentSkillLanguage sentimentLanguageCode, TextExtractionAlgorithm algorithm) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
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

        return new SearchIndexerSkillset()
            .setName("ocr-sentiment-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset createTestSkillsetOcrKeyPhrase(OcrSkillLanguage ocrLanguageCode, KeyPhraseExtractionSkillLanguage keyPhraseLanguageCode) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
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

        return new SearchIndexerSkillset()
            .setName("ocr-key-phrase-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset createTestSkillsetOcrSplitText(OcrSkillLanguage ocrLanguageCode,
        SplitSkillLanguage splitLanguageCode, TextSplitMode textSplitMode) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
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

        return new SearchIndexerSkillset()
            .setName("ocr-split-text-skillset")
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset createTestOcrSkillSet(int repeat, TextExtractionAlgorithm algorithm) {
        List<SearchIndexerSkill> skills = new ArrayList<>();

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

        return new SearchIndexerSkillset()
            .setName("testskillset")
            .setDescription("Skillset for testing OCR")
            .setSkills(skills);
    }

    SearchIndexerSkillset createSkillsetWithOcrDefaultSettings(Boolean shouldDetectOrientation) {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new OcrSkill()
                .setShouldDetectOrientation(shouldDetectOrientation)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new SearchIndexerSkillset()
            .setName(SkillsetManagementSyncTests.OCR_SKILLSET_NAME)
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    SearchIndexerSkillset createSkillsetWithImageAnalysisDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("description", "mydescription"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new ImageAnalysisSkill()
                .setName("myimage")
                .setDescription("Tested image analysis skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new SearchIndexerSkillset()
            .setName("image-analysis-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    SearchIndexerSkillset createSkillsetWithKeyPhraseExtractionDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/myText"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new KeyPhraseExtractionSkill()
                .setName("mykeyphrases")
                .setDescription("Tested Key Phrase skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new SearchIndexerSkillset()
            .setName("key-phrase-extraction-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    SearchIndexerSkillset createSkillsetWithMergeDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("text", "/document/text"),
            simpleInputFieldMappingEntry("itemsToInsert", "/document/textitems"),
            simpleInputFieldMappingEntry("offsets", "/document/offsets"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("mergedText", "myMergedText"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new MergeSkill()
                .setName("mymerge")
                .setDescription("Tested Merged Text skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new SearchIndexerSkillset()
            .setName("merge-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    SearchIndexerSkillset createSkillsetWithSentimentDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("score", "mySentiment"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new SentimentSkill()
                .setName("mysentiment")
                .setDescription("Tested Sentiment skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new SearchIndexerSkillset()
            .setName("sentiment-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    SearchIndexerSkillset createSkillsetWithEntityRecognitionDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("entities", "myEntities"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new EntityRecognitionSkill()
                .setName("myentity")
                .setDescription("Tested Entity Recognition skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new SearchIndexerSkillset()
            .setName("entity-recognition-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    SearchIndexerSkillset createSkillsetWithSplitDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new SplitSkill()
                .setTextSplitMode(TextSplitMode.PAGES)
                .setName("mysplit")
                .setDescription("Tested Split skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(inputs)
                .setOutputs(outputs)
        );

        return new SearchIndexerSkillset()
            .setName("split-skillset")
            .setDescription("Skillset for testing default configuration")
            .setSkills(skills);
    }

    SearchIndexerSkillset createSkillsetWithCustomSkills() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", "foobar");

        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));

        SearchIndexerSkill webApiSkill = new WebApiSkill()
            .setUri("https://indexer-e2e-webskill.azurewebsites.net/api/InvokeTextAnalyticsV3?code=foo")
            .setHttpMethod("POST")
            .setHttpHeaders(headers)
            .setInputs(inputs)
            .setOutputs(outputs)
            .setName("webapi-skill")
            .setDescription("Calls an Azure function, which in turn calls Bing Entity Search");

        return new SearchIndexerSkillset()
            .setName("custom-skillset")
            .setDescription("Skillset for testing custom skillsets")
            .setSkills(Collections.singletonList(webApiSkill));
    }

    SearchIndexerSkillset createSkillsetWithSharperSkillWithNestedInputs() {
        List<InputFieldMappingEntry> inputs = this.createNestedInputFieldMappingEntry();
        List<OutputFieldMappingEntry> outputs = this.createOutputFieldMappingEntry();

        List<SearchIndexerSkill> skills = new ArrayList<>();
        skills.add(new ShaperSkill()
            .setName("myshaper")
            .setDescription("Tested Shaper skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(inputs)
            .setOutputs(outputs)
        );

        return new SearchIndexerSkillset()
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


    protected List<SearchIndexerSkill> getCreateOrUpdateSkills() {
        return Collections.singletonList(new KeyPhraseExtractionSkill()
            .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
            .setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE)
            .setInputs(Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext")))
            .setOutputs(Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"))));
    }
}
