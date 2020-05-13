// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
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
import com.azure.search.documents.models.SearchErrorException;
import com.azure.search.documents.models.SentimentSkill;
import com.azure.search.documents.models.SentimentSkillLanguage;
import com.azure.search.documents.models.ShaperSkill;
import com.azure.search.documents.models.Skill;
import com.azure.search.documents.models.Skillset;
import com.azure.search.documents.models.SplitSkill;
import com.azure.search.documents.models.SplitSkillLanguage;
import com.azure.search.documents.models.TextExtractionAlgorithm;
import com.azure.search.documents.models.TextSplitMode;
import com.azure.search.documents.models.VisualFeature;
import com.azure.search.documents.models.WebApiSkill;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.generateRequestOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class SkillsetManagementSyncTests extends SearchTestBase {
    private static final String CONTEXT_VALUE = "/document";
    private static final String OCR_SKILLSET_NAME = "ocr-skillset";

    private final List<String> skillsetsToDelete = new ArrayList<>();

    private SearchServiceClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
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
        Skillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhraseWithResponse() {
        Skillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();
        Response<Skillset> skillsetResponse = client.createSkillsetWithResponse(expectedSkillset,
            generateRequestOptions(), Context.NONE);
        skillsetsToDelete.add(skillsetResponse.getValue().getName());

        assertObjectEquals(expectedSkillset, skillsetResponse.getValue(), true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionLanguageDetection() {
        Skillset expectedSkillset = createTestSkillsetLanguageDetection();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionMergeText() {
        Skillset expectedSkillset = createTestSkillsetMergeText();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrEntity() {
        Skillset expectedSkillset = createTestSkillsetOcrEntity(null, null);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        List<EntityCategory> entityCategories = Arrays.asList(
            EntityCategory.LOCATION, EntityCategory.ORGANIZATION, EntityCategory.PERSON);

        expectedSkillset = createTestSkillsetOcrEntity(TextExtractionAlgorithm.PRINTED, entityCategories);
        actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentiment() {
        Skillset expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.PT,
            SentimentSkillLanguage.PT_PT, TextExtractionAlgorithm.PRINTED);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.FI,
            SentimentSkillLanguage.FI, TextExtractionAlgorithm.PRINTED);
        actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");

        expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.EN,
            SentimentSkillLanguage.EN, TextExtractionAlgorithm.HANDWRITTEN);
        actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());
        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrKeyPhrase() {
        Skillset expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.EN,
            KeyPhraseExtractionSkillLanguage.EN);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
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
        Skillset expectedSkillset = createTestSkillsetOcrShaper();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrSplitText() {
        Skillset expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.EN,
            SplitSkillLanguage.EN, TextSplitMode.PAGES);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
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
        Skillset expectedSkillset = createSkillsetWithCognitiveServicesKey();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithOcrDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithOcrDefaultSettings(false);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithImageAnalysisDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithImageAnalysisDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithKeyPhraseExtractionDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithKeyPhraseExtractionDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithMergeDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithMergeDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithEntityRecognitionDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        Skillset actual = client.getSkillset(expected.getName());
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinitionWithResponse() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        Skillset actual = client.getSkillsetWithResponse(expected.getName(), generateRequestOptions(), Context.NONE)
            .getValue();
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getOcrSkillsetWithShouldDetectOrientationReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(true);
        client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        Skillset actual = client.getSkillset(expected.getName());
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSentimentDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithSentimentDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSplitDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithSplitDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        skillsetsToDelete.add(actualSkillset.getName());

        assertObjectEquals(expectedSkillset, actualSkillset, true, "etag");
    }

    @Test
    public void createCustomSkillsetReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithCustomSkills();
        Skillset actual = client.createSkillset(expected);
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
        Skillset skillset1 = createSkillsetWithCognitiveServicesKey();
        Skillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        client.createSkillset(skillset1);
        skillsetsToDelete.add(skillset1.getName());
        client.createSkillset(skillset2);
        skillsetsToDelete.add(skillset2.getName());

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
        skillsetsToDelete.add(skillset1.getName());
        client.createSkillset(skillset2);
        skillsetsToDelete.add(skillset2.getName());

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

        Response<Void> deleteResponse = client.deleteSkillsetWithResponse(skillset, false,
            generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        client.createSkillset(skillset);

        // Delete the same skillset twice
        deleteResponse = client.deleteSkillsetWithResponse(skillset, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = client.deleteSkillsetWithResponse(skillset, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
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
        skillsetsToDelete.add(actual.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistWithResponse() {
        Skillset expected = createTestOcrSkillSet(1, TextExtractionAlgorithm.PRINTED);
        Response<Skillset> createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(expected,
            false, generateRequestOptions(), Context.NONE);
        skillsetsToDelete.add(createOrUpdateResponse.getValue().getName());

        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateUpdatesWhenSkillsetExists() {
        Skillset skillset = createTestOcrSkillSet(1, TextExtractionAlgorithm.HANDWRITTEN);
        Response<Skillset> createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(skillset, false,
            generateRequestOptions(), Context.NONE);
        skillsetsToDelete.add(createOrUpdateResponse.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());

        skillset = createTestOcrSkillSet(2, TextExtractionAlgorithm.PRINTED).setName(skillset.getName());
        createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(skillset, false, generateRequestOptions(),
            Context.NONE);
        assertEquals(HttpURLConnection.HTTP_OK, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateUpdatesSkills() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(false);
        Skillset createdSkillset = client.createSkillset(skillset);
        skillsetsToDelete.add(createdSkillset.getName());

        // update skills
        createdSkillset.setSkills(getCreateOrUpdateSkills());

        assertObjectEquals(createdSkillset, client.createOrUpdateSkillset(createdSkillset), true, "etag", "@odata.etag");
    }


    @Test
    public void createOrUpdateUpdatesCognitiveService() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(false);
        Skillset createdSkillset = client.createSkillset(skillset);
        skillsetsToDelete.add(createdSkillset.getName());

        // update skills
        createdSkillset.setCognitiveServicesAccount(new DefaultCognitiveServicesAccount().setDescription("description"));

        assertObjectEquals(createdSkillset, client.createOrUpdateSkillset(createdSkillset),
            true, "etag", "@odata.etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionShaperWithNestedInputs() {
        Skillset expected = createSkillsetWithSharperSkillWithNestedInputs();
        Skillset actual = client.createSkillset(expected);
        skillsetsToDelete.add(actual.getName());

        assertObjectEquals(expected, actual, true, "etag");
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
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Skill '#1' is not allowed to have recursively defined inputs");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionConditional() {
        Skillset expected = createTestSkillsetConditional();
        Skillset actual = client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createOrUpdateSkillsetIfNotExistsSucceedsOnNoResource() {
        Skillset created = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), true,
            null, Context.NONE).getValue();
        skillsetsToDelete.add(created.getName());

        assertFalse(CoreUtils.isNullOrEmpty(created.getETag()));
    }

    @Test
    public void createOrUpdateSkillsetIfExistsSucceedsOnExistingResource() {
        Skillset original = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false),
            false, null, Context.NONE).getValue();
        String originalETag = original.getETag();
        skillsetsToDelete.add(original.getName());

        Skillset updated = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), false, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedSucceedsWhenResourceUnchanged() {
        Skillset original = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false),
            false, null, Context.NONE).getValue();
        String originalETag = original.getETag();
        skillsetsToDelete.add(original.getName());

        Skillset updated = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), true, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedFailsWhenResourceChanged() {
        Skillset original = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false),
            false, null, Context.NONE).getValue();
        String originalETag = original.getETag();
        skillsetsToDelete.add(original.getName());

        Skillset updated = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), true, null,
            Context.NONE).getValue();
        String updatedETag = updated.getETag();

        // Update and check the eTags were changed
        try {
            client.createOrUpdateSkillsetWithResponse(original, true, null, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void deleteSkillsetIfNotChangedWorksOnlyOnCurrentResource() {
        Skillset stale = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false),
            true, null, Context.NONE).getValue();

        Skillset current = client.createOrUpdateSkillsetWithResponse(stale, true, null, Context.NONE)
            .getValue();

        try {
            client.deleteSkillsetWithResponse(stale, true, null, Context.NONE);
            fail("deleteFunc should have failed due to precondition.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        client.deleteSkillsetWithResponse(current, true, null, Context.NONE);
    }

    @Test
    public void deleteSkillsetIfExistsWorksOnlyWhenResourceExists() {
        Skillset skillset = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false),
            false, null, Context.NONE).getValue();

        client.deleteSkillsetWithResponse(skillset, true, null, Context.NONE);

        try {
            client.deleteSkillsetWithResponse(skillset, true, null, Context.NONE);
            fail("deleteFunc should have failed due to non existent resource.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
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
            .setName(testResourceNamer.randomName("image-analysis-key-phrase-skillset", 48))
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
            .setName(testResourceNamer.randomName("language-detection-skillset", 48))
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
            .setName(testResourceNamer.randomName("merge-text-skillset", 48))
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
            .setName(testResourceNamer.randomName("ocr-shaper-skillset", 48))
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
            .setName(testResourceNamer.randomName("cognitive-services-key-skillset", 48))
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
            .setName(testResourceNamer.randomName("conditional-skillset", 48))
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
            .setName(testResourceNamer.randomName("ocr-entity-skillset", 48))
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
            .setName(testResourceNamer.randomName("ocr-sentiment-skillset", 48))
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
            .setName(testResourceNamer.randomName("ocr-key-phrase-skillset", 48))
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
            .setName(testResourceNamer.randomName("ocr-split-text-skillset", 48))
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
            .setName(testResourceNamer.randomName("testskillset", 48))
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
            .setName(testResourceNamer.randomName(SkillsetManagementSyncTests.OCR_SKILLSET_NAME, 48))
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
            .setName(testResourceNamer.randomName("image-analysis-skillset", 48))
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
            .setName(testResourceNamer.randomName("key-phrase-extraction-skillset", 48))
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
            .setName(testResourceNamer.randomName("merge-skillset", 48))
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
            .setName(testResourceNamer.randomName("sentiment-skillset", 48))
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
            .setName(testResourceNamer.randomName("entity-recognition-skillset", 48))
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
            .setName(testResourceNamer.randomName("split-skillset", 48))
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
            .setName(testResourceNamer.randomName("custom-skillset", 48))
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
            .setName(testResourceNamer.randomName("nested-skillset-with-sharperskill", 48))
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
