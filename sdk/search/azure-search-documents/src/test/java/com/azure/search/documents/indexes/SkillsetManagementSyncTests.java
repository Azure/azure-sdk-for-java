// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.indexes.models.ConditionalSkill;
import com.azure.search.documents.indexes.models.DefaultCognitiveServicesAccount;
import com.azure.search.documents.indexes.models.EntityCategory;
import com.azure.search.documents.indexes.models.EntityRecognitionSkill;
import com.azure.search.documents.indexes.models.EntityRecognitionSkillLanguage;
import com.azure.search.documents.indexes.models.ImageAnalysisSkill;
import com.azure.search.documents.indexes.models.ImageAnalysisSkillLanguage;
import com.azure.search.documents.indexes.models.ImageDetail;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.KeyPhraseExtractionSkill;
import com.azure.search.documents.indexes.models.KeyPhraseExtractionSkillLanguage;
import com.azure.search.documents.indexes.models.LanguageDetectionSkill;
import com.azure.search.documents.indexes.models.MergeSkill;
import com.azure.search.documents.indexes.models.OcrSkill;
import com.azure.search.documents.indexes.models.OcrSkillLanguage;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SearchIndexerSkill;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SentimentSkill;
import com.azure.search.documents.indexes.models.SentimentSkillLanguage;
import com.azure.search.documents.indexes.models.ShaperSkill;
import com.azure.search.documents.indexes.models.SplitSkill;
import com.azure.search.documents.indexes.models.SplitSkillLanguage;
import com.azure.search.documents.indexes.models.TextSplitMode;
import com.azure.search.documents.indexes.models.VisualFeature;
import com.azure.search.documents.indexes.models.WebApiSkill;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class SkillsetManagementSyncTests extends SearchTestBase {
    private static final String CONTEXT_VALUE = "/document";
    private static final String OCR_SKILLSET_NAME = "ocr-skillset";

    private final List<String> skillsetsToDelete = new ArrayList<>();

    private SearchIndexerClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchIndexerClientBuilder().buildClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        for (String skillset : skillsetsToDelete) {
            client.deleteSkillset(skillset);
        }
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhrase() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhraseWithResponse() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();
        Response<SearchIndexerSkillset> skillsetResponse = client.createSkillsetWithResponse(expectedSkillset,
            Context.NONE);
        skillsetsToDelete.add(skillsetResponse.getValue().getName());

        assertObjectEquals(expectedSkillset, skillsetResponse.getValue(), true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionLanguageDetection() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetLanguageDetection();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionMergeText() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetMergeText();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrEntity() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetOcrEntity(null);
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrEntity(Arrays.asList(EntityCategory.LOCATION,
            EntityCategory.ORGANIZATION, EntityCategory.PERSON));
        actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentiment() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.PT,
            SentimentSkillLanguage.PT_PT);
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.FI,
            SentimentSkillLanguage.FI);
        actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.EN,
            SentimentSkillLanguage.EN);
        actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrKeyPhrase() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.EN,
            KeyPhraseExtractionSkillLanguage.EN);
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.FR, KeyPhraseExtractionSkillLanguage.FR);
        actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.ES, KeyPhraseExtractionSkillLanguage.ES);
        actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrShaper() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetOcrShaper();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrSplitText() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.EN,
            SplitSkillLanguage.EN, TextSplitMode.PAGES);
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");


        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.FR,
            SplitSkillLanguage.FR, TextSplitMode.PAGES);
        actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.FI,
            SplitSkillLanguage.FI, TextSplitMode.SENTENCES);
        actualSkillset = client.createSkillset(expectedSkillset);
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
        client.deleteSkillset(expectedSkillset.getName());

        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.DA,
            SplitSkillLanguage.DA, TextSplitMode.SENTENCES);
        actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithCognitiveServicesDefault() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithCognitiveServicesKey();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithOcrDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithOcrDefaultSettings(false);
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithImageAnalysisDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithImageAnalysisDefaultSettings();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithKeyPhraseExtractionDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithKeyPhraseExtractionDefaultSettings();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithMergeDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithMergeDefaultSettings();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithEntityRecognitionDefaultSettings();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinition() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        SearchIndexerSkillset actual = client.getSkillset(expected.getName());
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinitionWithResponse() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        SearchIndexerSkillset actual = client.getSkillsetWithResponse(expected.getName(), Context.NONE)
            .getValue();
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getOcrSkillsetWithShouldDetectOrientationReturnsCorrectDefinition() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(true);
        client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        SearchIndexerSkillset actual = client.getSkillset(expected.getName());
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSentimentDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithSentimentDefaultSettings();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSplitDefaultSettings() {
        SearchIndexerSkillset expectedSkillset = createSkillsetWithSplitDefaultSettings();
        SearchIndexerSkillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createCustomSkillsetReturnsCorrectDefinition() {
        SearchIndexerSkillset expected = createSkillsetWithCustomSkills();
        SearchIndexerSkillset actual = client.createSkillset(expected);
        skillsetsToDelete.add(actual.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getSkillsetThrowsOnNotFound() {
        assertHttpResponseException(
            () -> client.getSkillset("thisdoesnotexist"),
            HttpURLConnection.HTTP_NOT_FOUND,
            "No skillset with the name 'thisdoesnotexist' was found in service"
        );
    }

    @Test
    public void canCreateAndListSkillsets() {
        SearchIndexerSkillset skillset1 = createSkillsetWithCognitiveServicesKey();
        SearchIndexerSkillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        client.createSkillset(skillset1);
        skillsetsToDelete.add(skillset1.getName());
        client.createSkillset(skillset2);
        skillsetsToDelete.add(skillset2.getName());

        Iterator<SearchIndexerSkillset> actual = client.listSkillsets().iterator();

        assertObjectEquals(skillset1, actual.next(), true);
        assertObjectEquals(skillset2, actual.next(), true);
        assertFalse(actual.hasNext());
    }

    @Test
    public void canListSkillsetsWithSelectedField() {
        SearchIndexerSkillset skillset1 = createSkillsetWithCognitiveServicesKey();
        SearchIndexerSkillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        client.createSkillset(skillset1);
        skillsetsToDelete.add(skillset1.getName());
        client.createSkillset(skillset2);
        skillsetsToDelete.add(skillset2.getName());

        PagedIterable<String> selectedFieldListResponse = client.listSkillsetNames(Context.NONE);
        List<String> result = selectedFieldListResponse.stream().collect(Collectors.toList());

        result.forEach(Assertions::assertNotNull);

        assertEquals(2, result.size());
        assertEquals(result.get(0), skillset1.getName());
        assertEquals(result.get(1), skillset2.getName());
    }

    @Test
    public void deleteSkillsetIsIdempotent() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        Response<Void> deleteResponse = client.deleteSkillsetWithResponse(skillset, false,
            Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        client.createSkillset(skillset);

        // Delete the same skillset twice
        deleteResponse = client.deleteSkillsetWithResponse(skillset, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = client.deleteSkillsetWithResponse(skillset, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteSkillset() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);
        client.deleteSkillset(expected.getName());

        assertThrows(HttpResponseException.class, () -> client.getSkillset(expected.getName()));
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExist() {
        SearchIndexerSkillset expected = createTestOcrSkillSet(1);
        SearchIndexerSkillset actual = client.createOrUpdateSkillset(expected);
        skillsetsToDelete.add(actual.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistWithResponse() {
        SearchIndexerSkillset expected = createTestOcrSkillSet(1);
        Response<SearchIndexerSkillset> createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(expected,
            false, Context.NONE);
        skillsetsToDelete.add(createOrUpdateResponse.getValue().getName());

        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateUpdatesWhenSkillsetExists() throws Exception {
        SearchIndexerSkillset skillset = createTestOcrSkillSet(1);
        Response<SearchIndexerSkillset> createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(skillset, false,
            Context.NONE);
        skillsetsToDelete.add(createOrUpdateResponse.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
        SearchIndexerSkillset updatedSkillset = createTestOcrSkillSet(2);
        Field skillsetName = updatedSkillset.getClass().getDeclaredField("name");
        skillsetName.setAccessible(true);
        skillsetName.set(updatedSkillset, skillset.getName());
        createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(skillset, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_OK, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateUpdatesSkills() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);
        SearchIndexerSkillset createdSkillset = client.createSkillset(skillset);
        skillsetsToDelete.add(createdSkillset.getName());

        // update skills
        createdSkillset.setSkills(getCreateOrUpdateSkills());

        assertObjectEquals(createdSkillset, client.createOrUpdateSkillset(createdSkillset), true, "etag", "@odata.etag");
    }


    @Test
    public void createOrUpdateUpdatesCognitiveService() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);
        SearchIndexerSkillset createdSkillset = client.createSkillset(skillset);
        skillsetsToDelete.add(createdSkillset.getName());

        // update skills
        createdSkillset.setCognitiveServicesAccount(new DefaultCognitiveServicesAccount().setDescription("description"));

        assertObjectEquals(createdSkillset, client.createOrUpdateSkillset(createdSkillset),
            true, "etag", "@odata.etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionShaperWithNestedInputs() {
        SearchIndexerSkillset expected = createSkillsetWithSharperSkillWithNestedInputs();
        SearchIndexerSkillset actual = client.createSkillset(expected);
        skillsetsToDelete.add(actual.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    // TODO (alzimmer): This test doesn't complete as expected, follow-up with a fix for it.
    //@Test
    public void createSkillsetThrowsExceptionWithNonShaperSkillWithNestedInputs() {
        List<InputFieldMappingEntry> inputs = this.createNestedInputFieldMappingEntry();
        List<OutputFieldMappingEntry> outputs = this.createOutputFieldMappingEntry();

        List<SearchIndexerSkill> skills = new ArrayList<>();
        // Used for testing skill that shouldn't allow nested inputs
        skills.add(new WebApiSkill(inputs, outputs, "https://contoso.example.org")
            .setDescription("Invalid skill with nested inputs")
            .setContext(CONTEXT_VALUE));

        SearchIndexerSkillset skillset = new SearchIndexerSkillset("nested-skillset-with-nonsharperskill")
            .setDescription("Skillset for testing")
            .setSkills(skills);

        assertHttpResponseException(
            () -> client.createSkillset(skillset),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Skill '#1' is not allowed to have recursively defined inputs");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionConditional() {
        SearchIndexerSkillset expected = createTestSkillsetConditional();
        SearchIndexerSkillset actual = client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createOrUpdateSkillsetIfNotExistsSucceedsOnNoResource() {
        SearchIndexerSkillset created = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), true,
            Context.NONE).getValue();
        skillsetsToDelete.add(created.getName());

        assertFalse(CoreUtils.isNullOrEmpty(created.getETag()));
    }

    @Test
    public void createOrUpdateSkillsetIfExistsSucceedsOnExistingResource() {
        SearchIndexerSkillset original = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false),
            false, Context.NONE).getValue();
        String originalETag = original.getETag();
        skillsetsToDelete.add(original.getName());

        SearchIndexerSkillset updated = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), false,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedSucceedsWhenResourceUnchanged() {
        SearchIndexerSkillset original = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false),
            false, Context.NONE).getValue();
        String originalETag = original.getETag();
        skillsetsToDelete.add(original.getName());

        SearchIndexerSkillset updated = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), true,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedFailsWhenResourceChanged() {
        SearchIndexerSkillset original = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false),
            false, Context.NONE).getValue();
        String originalETag = original.getETag();
        skillsetsToDelete.add(original.getName());

        SearchIndexerSkillset updated = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), true,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        // Update and check the eTags were changed
        try {
            client.createOrUpdateSkillsetWithResponse(original, true, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void deleteSkillsetIfNotChangedWorksOnlyOnCurrentResource() {
        SearchIndexerSkillset stale = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false),
            true, Context.NONE).getValue();

        SearchIndexerSkillset current = client.createOrUpdateSkillsetWithResponse(stale, true, Context.NONE)
            .getValue();

        try {
            client.deleteSkillsetWithResponse(stale, true, Context.NONE);
            fail("deleteFunc should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        client.deleteSkillsetWithResponse(current, true, Context.NONE);
    }

    @Test
    public void deleteSkillsetIfExistsWorksOnlyWhenResourceExists() {
        SearchIndexerSkillset skillset = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false),
            false, Context.NONE).getValue();

        client.deleteSkillsetWithResponse(skillset, true, Context.NONE);

        try {
            client.deleteSkillsetWithResponse(skillset, true, Context.NONE);
            fail("deleteFunc should have failed due to non existent resource.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
    }

    private InputFieldMappingEntry simpleInputFieldMappingEntry(String name, String source) {
        return new InputFieldMappingEntry(name).setSource(source);
    }

    private OutputFieldMappingEntry createOutputFieldMappingEntry(String name, String targetName) {
        return new OutputFieldMappingEntry(name).setTargetName(targetName);
    }

    SearchIndexerSkillset createTestSkillsetImageAnalysisKeyPhrase() {
        List<SearchIndexerSkill> skills = new ArrayList<>();

        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString")
        );

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("description", "mydescription"));

        skills.add(new ImageAnalysisSkill(inputs, outputs)
            .setVisualFeatures(new ArrayList<>(VisualFeature.values()))
            .setDetails(new ArrayList<>(ImageDetail.values()))
            .setDefaultLanguageCode(ImageAnalysisSkillLanguage.EN)
            .setName("myimage")
            .setDescription("Tested image analysis skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mydescription/*/Tags/*"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"));
        skills.add(new KeyPhraseExtractionSkill(inputs, outputs)
            .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
            .setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(
            testResourceNamer.randomName("image-analysis-key-phrase-skillset", 48))
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset createTestSkillsetLanguageDetection() {
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/text"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("languageCode", "myLanguageCode"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new LanguageDetectionSkill(inputs, outputs)
                .setName("mylanguage")
                .setDescription("Tested Language Detection skill")
                .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("language-detection-skillset", 48))
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
            new MergeSkill(inputs, outputs)
                .setInsertPostTag("__e")
                .setInsertPreTag("__")
                .setName("mymerge")
                .setDescription("Tested Merged Text skill")
                .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("merge-text-skillset", 48))
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

        skills.add(new OcrSkill(inputs, outputs)
            .setDefaultLanguageCode(OcrSkillLanguage.EN)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("output", "myOutput"));
        skills.add(new ShaperSkill(inputs, outputs)
            .setName("mysharper")
            .setDescription("Tested Shaper skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-shaper-skillset", 48))
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
            new OcrSkill(inputs, outputs)
                .setDefaultLanguageCode(OcrSkillLanguage.EN)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(
            testResourceNamer.randomName("cognitive-services-key-skillset", 48), skills)
            .setDescription("Skillset for testing")
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
            new ConditionalSkill(inputs, outputs)
                .setName("myconditional")
                .setDescription("Tested Conditional skill")
                .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(testResourceNamer.randomName("conditional-skillset", 48))
            .setDescription("Skillset for testing")
            .setSkills(skills);
    }

    SearchIndexerSkillset mutateSkillsInSkillset(SearchIndexerSkillset skillset) {
        return skillset.setSkills(new KeyPhraseExtractionSkill(Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mydescription/*/Tags/*")),
            Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases")))
            .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
            .setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE));
    }

    SearchIndexerSkillset createTestSkillsetOcrEntity(List<EntityCategory> categories) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill(inputs, outputs)
            .setDefaultLanguageCode(OcrSkillLanguage.EN)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("entities", "myEntities"));
        skills.add(new EntityRecognitionSkill(inputs, outputs)
            .setCategories(categories)
            .setDefaultLanguageCode(EntityRecognitionSkillLanguage.EN)
            .setMinimumPrecision(0.5)
            .setName("myentity")
            .setDescription("Tested Entity Recognition skill")
            .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-entity-skillset", 48), skills)
            .setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestSkillsetOcrSentiment(OcrSkillLanguage ocrLanguageCode,
        SentimentSkillLanguage sentimentLanguageCode) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));
        skills.add(new OcrSkill(inputs, outputs)
            .setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("score", "mySentiment"));
        skills.add(new SentimentSkill(inputs, outputs)
            .setDefaultLanguageCode(sentimentLanguageCode)
            .setName("mysentiment")
            .setDescription("Tested Sentiment skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-sentiment-skillset", 48),
            skills).setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestSkillsetOcrKeyPhrase(OcrSkillLanguage ocrLanguageCode, KeyPhraseExtractionSkillLanguage keyPhraseLanguageCode) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill(inputs, outputs)
            .setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"));
        skills.add(new KeyPhraseExtractionSkill(inputs, outputs)
            .setDefaultLanguageCode(keyPhraseLanguageCode)
            .setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-key-phrase-skillset", 48),
            skills).setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestSkillsetOcrSplitText(OcrSkillLanguage ocrLanguageCode,
        SplitSkillLanguage splitLanguageCode, TextSplitMode textSplitMode) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill(inputs, outputs)
            .setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));
        skills.add(new SplitSkill(inputs, outputs)
            .setDefaultLanguageCode(splitLanguageCode)
            .setTextSplitMode(textSplitMode)
            .setName("mysplit")
            .setDescription("Tested Split skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-split-text-skillset", 48),
            skills).setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestOcrSkillSet(int repeat) {
        List<SearchIndexerSkill> skills = new ArrayList<>();

        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        for (int i = 0; i < repeat; i++) {
            List<OutputFieldMappingEntry> outputs = Collections
                .singletonList(createOutputFieldMappingEntry("text", "mytext" + i));

            skills.add(new OcrSkill(inputs, outputs)
                .setDefaultLanguageCode(OcrSkillLanguage.EN)
                .setShouldDetectOrientation(false)
                .setName("myocr-" + i)
                .setDescription("Tested OCR skill")
                .setContext(CONTEXT_VALUE));
        }

        return new SearchIndexerSkillset(testResourceNamer.randomName("testskillset", 48), skills)
            .setDescription("Skillset for testing OCR");
    }

    SearchIndexerSkillset createSkillsetWithOcrDefaultSettings(Boolean shouldDetectOrientation) {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("text", "mytext"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new OcrSkill(inputs, outputs)
                .setShouldDetectOrientation(shouldDetectOrientation)
                .setName("myocr")
                .setDescription("Tested OCR skill")
                .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(
            testResourceNamer.randomName(SkillsetManagementSyncTests.OCR_SKILLSET_NAME, 48), skills)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithImageAnalysisDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("description", "mydescription"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new ImageAnalysisSkill(inputs, outputs)
                .setName("myimage")
                .setDescription("Tested image analysis skill")
                .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(testResourceNamer.randomName("image-analysis-skillset", 48), skills)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithKeyPhraseExtractionDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/myText"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new KeyPhraseExtractionSkill(inputs, outputs)
                .setName("mykeyphrases")
                .setDescription("Tested Key Phrase skill")
                .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(testResourceNamer.randomName("key-phrase-extraction-skillset", 48),
            skills).setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithMergeDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(
            simpleInputFieldMappingEntry("text", "/document/text"),
            simpleInputFieldMappingEntry("itemsToInsert", "/document/textitems"),
            simpleInputFieldMappingEntry("offsets", "/document/offsets"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("mergedText", "myMergedText"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new MergeSkill(inputs, outputs)
                .setName("mymerge")
                .setDescription("Tested Merged Text skill")
                .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(testResourceNamer.randomName("merge-skillset", 48), skills)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithSentimentDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("score", "mySentiment"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new SentimentSkill(inputs, outputs)
                .setName("mysentiment")
                .setDescription("Tested Sentiment skill")
                .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(testResourceNamer.randomName("sentiment-skillset", 48), skills)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithEntityRecognitionDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("entities", "myEntities"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new EntityRecognitionSkill(inputs, outputs)
                .setName("myentity")
                .setDescription("Tested Entity Recognition skill")
                .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(testResourceNamer.randomName("entity-recognition-skillset", 48),
            skills).setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithSplitDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new SplitSkill(inputs, outputs)
                .setTextSplitMode(TextSplitMode.PAGES)
                .setName("mysplit")
                .setDescription("Tested Split skill")
                .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(testResourceNamer.randomName("split-skillset", 48), skills)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithCustomSkills() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", "foobar");

        List<InputFieldMappingEntry> inputs = Collections
            .singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs = Collections
            .singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));

        SearchIndexerSkill webApiSkill = new WebApiSkill(inputs, outputs,
            "https://indexer-e2e-webskill.azurewebsites.net/api/InvokeTextAnalyticsV3?code=foo")
            .setHttpMethod("POST")
            .setHttpHeaders(headers)
            .setName("webapi-skill")
            .setDescription("Calls an Azure function, which in turn calls Bing Entity Search");

        return new SearchIndexerSkillset(testResourceNamer.randomName("custom-skillset", 48),
            Collections.singletonList(webApiSkill))
            .setDescription("Skillset for testing custom skillsets");
    }

    SearchIndexerSkillset createSkillsetWithSharperSkillWithNestedInputs() {
        List<InputFieldMappingEntry> inputs = this.createNestedInputFieldMappingEntry();
        List<OutputFieldMappingEntry> outputs = this.createOutputFieldMappingEntry();

        List<SearchIndexerSkill> skills = new ArrayList<>();
        skills.add(new ShaperSkill(inputs, outputs)
            .setName("myshaper")
            .setDescription("Tested Shaper skill")
            .setContext(CONTEXT_VALUE)
        );

        return new SearchIndexerSkillset(
            testResourceNamer.randomName("nested-skillset-with-sharperskill", 48), skills)
            .setDescription("Skillset for testing");
    }

    private List<InputFieldMappingEntry> createNestedInputFieldMappingEntry() {
        return Collections.singletonList(
            new InputFieldMappingEntry("doc")
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
        return Collections.singletonList(new KeyPhraseExtractionSkill(
            Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext")),
            Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases")))
            .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
            .setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE));
    }
}
