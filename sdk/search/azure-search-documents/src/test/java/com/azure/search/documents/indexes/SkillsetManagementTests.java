// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.indexes.models.ConditionalSkill;
import com.azure.search.documents.indexes.models.DefaultCognitiveServicesAccount;
import com.azure.search.documents.indexes.models.EntityCategory;
import com.azure.search.documents.indexes.models.EntityRecognitionSkill;
import com.azure.search.documents.indexes.models.EntityRecognitionSkillLanguage;
import com.azure.search.documents.indexes.models.EntityRecognitionSkillVersion;
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
import com.azure.search.documents.indexes.models.SentimentSkillVersion;
import com.azure.search.documents.indexes.models.ShaperSkill;
import com.azure.search.documents.indexes.models.SplitSkill;
import com.azure.search.documents.indexes.models.SplitSkillLanguage;
import com.azure.search.documents.indexes.models.TextSplitMode;
import com.azure.search.documents.indexes.models.VisualFeature;
import com.azure.search.documents.indexes.models.WebApiSkill;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.verifyHttpResponseError;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SkillsetManagementTests extends SearchTestBase {
    private static final String CONTEXT_VALUE = "/document";
    private static final String OCR_SKILLSET_NAME = "ocr-skillset";

    private final List<String> skillsetsToDelete = new ArrayList<>();

    private SearchIndexerClient client;
    private SearchIndexerAsyncClient asyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        // Disable `("$..source")` sanitizer
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.removeSanitizers("AZSDK3423");
        }
        client = getSearchIndexerClientBuilder(true).httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)).buildClient();
        asyncClient = getSearchIndexerClientBuilder(false).httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)).buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        for (String skillset : skillsetsToDelete) {
            client.deleteSkillset(skillset);
        }
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhraseSync() {
        createAndValidateSkillsetSync(createTestSkillsetImageAnalysisKeyPhrase());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhraseAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetImageAnalysisKeyPhrase());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhraseWithResponseSync() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();
        Response<SearchIndexerSkillset> skillsetResponse = client.createSkillsetWithResponse(expectedSkillset,
            Context.NONE);
        skillsetsToDelete.add(skillsetResponse.getValue().getName());

        assertObjectEquals(expectedSkillset, skillsetResponse.getValue(), true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhraseWithResponseAsync() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();

        StepVerifier.create(asyncClient.createSkillsetWithResponse(expectedSkillset))
            .assertNext(response -> {
                skillsetsToDelete.add(response.getValue().getName());
                assertObjectEquals(expectedSkillset, response.getValue(), true, "etag");
            })
            .verifyComplete();

    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionLanguageDetectionSync() {
        createAndValidateSkillsetSync(createTestSkillsetLanguageDetection());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionLanguageDetectionAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetLanguageDetection());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionMergeTextSync() {
        createAndValidateSkillsetSync(createTestSkillsetMergeText());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionMergeTextAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetMergeText());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrEntitySync() {
        createAndValidateSkillsetSync(createTestSkillsetOcrEntity(null));

        createAndValidateSkillsetSync(createTestSkillsetOcrEntity(Arrays.asList(EntityCategory.LOCATION,
            EntityCategory.ORGANIZATION, EntityCategory.PERSON)));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrEntityAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetOcrEntity(null));

        createAndValidateSkillsetAsync(createTestSkillsetOcrEntity(Arrays.asList(EntityCategory.LOCATION,
            EntityCategory.ORGANIZATION, EntityCategory.PERSON)));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentimentSync() {
        createAndValidateSkillsetSync(createTestSkillsetOcrSentiment(OcrSkillLanguage.PT,
            SentimentSkillLanguage.PT_PT));

        createAndValidateSkillsetSync(createTestSkillsetOcrSentiment(OcrSkillLanguage.FI, SentimentSkillLanguage.FI));

        createAndValidateSkillsetSync(createTestSkillsetOcrSentiment(OcrSkillLanguage.EN, SentimentSkillLanguage.EN));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentimentAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetOcrSentiment(OcrSkillLanguage.PT,
            SentimentSkillLanguage.PT_PT));

        createAndValidateSkillsetAsync(createTestSkillsetOcrSentiment(OcrSkillLanguage.FI, SentimentSkillLanguage.FI));

        createAndValidateSkillsetAsync(createTestSkillsetOcrSentiment(OcrSkillLanguage.EN, SentimentSkillLanguage.EN));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrKeyPhraseSync() {
        createAndValidateSkillsetSync(createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.EN,
            KeyPhraseExtractionSkillLanguage.EN));

        createAndValidateSkillsetSync(createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.FR,
            KeyPhraseExtractionSkillLanguage.FR));

        createAndValidateSkillsetSync(createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.ES,
            KeyPhraseExtractionSkillLanguage.ES));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrKeyPhraseAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.EN,
            KeyPhraseExtractionSkillLanguage.EN));

        createAndValidateSkillsetAsync(createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.FR,
            KeyPhraseExtractionSkillLanguage.FR));

        createAndValidateSkillsetAsync(createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.ES,
            KeyPhraseExtractionSkillLanguage.ES));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrShaperSync() {
        createAndValidateSkillsetSync(createTestSkillsetOcrShaper());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrShaperAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetOcrShaper());
    }

    @Test
    @Disabled("TODO: Service is not responding to api calls. 500 error thrown with little information.")
    public void createSkillsetReturnsCorrectDefinitionOcrSplitTextSync() {
        createAndValidateSkillsetSync(createTestSkillsetOcrSplitText(OcrSkillLanguage.EN,
            SplitSkillLanguage.EN, TextSplitMode.PAGES));

        createAndValidateSkillsetSync(createTestSkillsetOcrSplitText(OcrSkillLanguage.FR,
            SplitSkillLanguage.FR, TextSplitMode.PAGES));

        // not working
        createAndValidateSkillsetSync(createTestSkillsetOcrSplitText(OcrSkillLanguage.FI,
            SplitSkillLanguage.FI, TextSplitMode.SENTENCES));

        // not working
        createAndValidateSkillsetSync(createTestSkillsetOcrSplitText(OcrSkillLanguage.DA,
            SplitSkillLanguage.DA, TextSplitMode.SENTENCES));
    }

    @Test
    @Disabled("TODO: Service is not responding to api calls. 500 error thrown with little information.")
    public void createSkillsetReturnsCorrectDefinitionOcrSplitTextAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetOcrSplitText(OcrSkillLanguage.EN,
            SplitSkillLanguage.EN, TextSplitMode.PAGES));

        createAndValidateSkillsetAsync(createTestSkillsetOcrSplitText(OcrSkillLanguage.FR,
            SplitSkillLanguage.FR, TextSplitMode.PAGES));

        createAndValidateSkillsetAsync(createTestSkillsetOcrSplitText(OcrSkillLanguage.FI,
            SplitSkillLanguage.FI, TextSplitMode.SENTENCES));

        createAndValidateSkillsetAsync(createTestSkillsetOcrSplitText(OcrSkillLanguage.DA,
            SplitSkillLanguage.DA, TextSplitMode.SENTENCES));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithCognitiveServicesDefaultSync() {
        createAndValidateSkillsetSync(createSkillsetWithCognitiveServicesKey());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithCognitiveServicesDefaultAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithCognitiveServicesKey());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithOcrDefaultSettingsSync() {
        createAndValidateSkillsetSync(createSkillsetWithOcrDefaultSettings(false));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithOcrDefaultSettingsAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithOcrDefaultSettings(false));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithImageAnalysisDefaultSettingsSync() {
        createAndValidateSkillsetSync(createSkillsetWithImageAnalysisDefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithImageAnalysisDefaultSettingsAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithImageAnalysisDefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithKeyPhraseExtractionDefaultSettingsSync() {
        createAndValidateSkillsetSync(createSkillsetWithKeyPhraseExtractionDefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithKeyPhraseExtractionDefaultSettingsAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithKeyPhraseExtractionDefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithMergeDefaultSettingsSync() {
        createAndValidateSkillsetSync(createSkillsetWithMergeDefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithMergeDefaultSettingsAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithMergeDefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionDefaultSettingsSync() {
        createAndValidateSkillsetSync(createSkillsetWithEntityRecognitionDefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionDefaultSettingsAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithEntityRecognitionDefaultSettings());
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinitionSync() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        SearchIndexerSkillset actual = client.getSkillset(expected.getName());
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinitionAsync() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        asyncClient.createSkillset(expected).block();
        skillsetsToDelete.add(expected.getName());

        StepVerifier.create(asyncClient.getSkillset(expected.getName()))
            .assertNext(actual -> assertObjectEquals(expected, actual, true, "etag"))
            .verifyComplete();
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinitionWithResponseSync() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        SearchIndexerSkillset actual = client.getSkillsetWithResponse(expected.getName(), Context.NONE)
            .getValue();
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinitionWithResponseAsync() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        asyncClient.createSkillset(expected).block();
        skillsetsToDelete.add(expected.getName());

        StepVerifier.create(asyncClient.getSkillsetWithResponse(expected.getName(), Context.NONE))
            .assertNext(response -> assertObjectEquals(expected, response.getValue(), true, "etag"))
            .verifyComplete();
    }

    @Test
    public void getOcrSkillsetWithShouldDetectOrientationReturnsCorrectDefinitionSync() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(true);
        client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        SearchIndexerSkillset actual = client.getSkillset(expected.getName());
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getOcrSkillsetWithShouldDetectOrientationReturnsCorrectDefinitionAsync() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(true);
        asyncClient.createSkillset(expected).block();
        skillsetsToDelete.add(expected.getName());

        StepVerifier.create(asyncClient.getSkillset(expected.getName()))
            .assertNext(actual -> assertObjectEquals(expected, actual, true, "etag"))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSentimentDefaultSettingsSync() {
        createAndValidateSkillsetSync(createSkillsetWithSentimentDefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSentimentDefaultSettingsAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithSentimentDefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSplitDefaultSettingsSync() {
        createAndValidateSkillsetSync(createSkillsetWithSplitDefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSplitDefaultSettingsAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithSplitDefaultSettings());
    }

    @Test
    public void createCustomSkillsetReturnsCorrectDefinitionSync() {
        createAndValidateSkillsetSync(createSkillsetWithCustomSkills());
    }

    @Test
    public void createCustomSkillsetReturnsCorrectDefinitionAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithCustomSkills());
    }

    private void createAndValidateSkillsetSync(SearchIndexerSkillset expected) {
        SearchIndexerSkillset actual = client.createSkillset(expected);
        skillsetsToDelete.add(actual.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    private void createAndValidateSkillsetAsync(SearchIndexerSkillset expected) {
        StepVerifier.create(asyncClient.createSkillset(expected))
            .assertNext(actual -> {
                skillsetsToDelete.add(actual.getName());
                assertObjectEquals(expected, actual, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void getSkillsetThrowsOnNotFoundSync() {
        assertHttpResponseException(() -> client.getSkillset("thisdoesnotexist"), HttpURLConnection.HTTP_NOT_FOUND,
            "No skillset with the name 'thisdoesnotexist' was found in service");
    }

    @Test
    public void getSkillsetThrowsOnNotFoundAsync() {
        StepVerifier.create(asyncClient.getSkillset("thisdoesnotexist"))
            .verifyErrorSatisfies(throwable -> verifyHttpResponseError(throwable, HttpURLConnection.HTTP_NOT_FOUND,
                "No skillset with the name 'thisdoesnotexist' was found in service"));
    }

    @Test
    public void canCreateAndListSkillsetsSyncAndAsync() {
        SearchIndexerSkillset skillset1 = createSkillsetWithCognitiveServicesKey();
        SearchIndexerSkillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        client.createSkillset(skillset1);
        skillsetsToDelete.add(skillset1.getName());
        client.createSkillset(skillset2);
        skillsetsToDelete.add(skillset2.getName());

        Map<String, SearchIndexerSkillset> expectedSkillsets = new HashMap<>();
        expectedSkillsets.put(skillset1.getName(), skillset1);
        expectedSkillsets.put(skillset2.getName(), skillset2);

        Map<String, SearchIndexerSkillset> actualSkillsets = client.listSkillsets().stream()
            .collect(Collectors.toMap(SearchIndexerSkillset::getName, skillset -> skillset));

        compareMaps(expectedSkillsets, actualSkillsets,
            (expected, actual) -> assertObjectEquals(expected, actual, true));

        StepVerifier.create(asyncClient.listSkillsets().collectMap(SearchIndexerSkillset::getName))
            .assertNext(actualSkillsetsAsync -> compareMaps(expectedSkillsets, actualSkillsetsAsync,
                (expected, actual) -> assertObjectEquals(expected, actual, true)))
            .verifyComplete();
    }

    @Test
    public void canListSkillsetsWithSelectedFieldSyncAndAsync() {
        SearchIndexerSkillset skillset1 = createSkillsetWithCognitiveServicesKey();
        SearchIndexerSkillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        client.createSkillset(skillset1);
        skillsetsToDelete.add(skillset1.getName());
        client.createSkillset(skillset2);
        skillsetsToDelete.add(skillset2.getName());

        Set<String> expectedSkillsetNames = new HashSet<>(Arrays.asList(skillset1.getName(), skillset2.getName()));
        Set<String> actualSkillsetNames = client.listSkillsetNames(Context.NONE).stream()
            .collect(Collectors.toSet());

        assertEquals(expectedSkillsetNames.size(), actualSkillsetNames.size());
        assertTrue(actualSkillsetNames.containsAll(expectedSkillsetNames));

        StepVerifier.create(asyncClient.listSkillsetNames().collect(Collectors.toSet()))
            .assertNext(actualSkillsetNamesAsync -> {
                assertEquals(actualSkillsetNamesAsync.size(), actualSkillsetNames.size());
                assertTrue(actualSkillsetNamesAsync.containsAll(expectedSkillsetNames));
            })
            .verifyComplete();
    }

    @Test
    public void deleteSkillsetIsIdempotentSync() {
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
    public void deleteSkillsetIsIdempotentAsync() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(skillset, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();

        asyncClient.createSkillset(skillset).block();

        // Delete the same skillset twice
        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(skillset, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NO_CONTENT, response.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(skillset, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void canCreateAndDeleteSkillsetSync() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);
        client.deleteSkillset(expected.getName());

        assertThrows(HttpResponseException.class, () -> client.getSkillset(expected.getName()));
    }

    @Test
    public void canCreateAndDeleteSkillsetAsync() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        asyncClient.createSkillset(expected).block();
        asyncClient.deleteSkillset(expected.getName()).block();

        StepVerifier.create(asyncClient.getSkillset(expected.getName())).verifyError(HttpResponseException.class);
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistSync() {
        SearchIndexerSkillset expected = createTestOcrSkillset();
        SearchIndexerSkillset actual = client.createOrUpdateSkillset(expected);
        skillsetsToDelete.add(actual.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistAsync() {
        SearchIndexerSkillset expected = createTestOcrSkillset();

        StepVerifier.create(asyncClient.createOrUpdateSkillset(expected))
            .assertNext(actual -> {
                skillsetsToDelete.add(actual.getName());
                assertObjectEquals(expected, actual, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistWithResponseSync() {
        SearchIndexerSkillset expected = createTestOcrSkillset();
        Response<SearchIndexerSkillset> createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(expected,
            false, Context.NONE);
        skillsetsToDelete.add(createOrUpdateResponse.getValue().getName());

        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistWithResponseAsync() {
        SearchIndexerSkillset expected = createTestOcrSkillset();

        StepVerifier.create(asyncClient.createOrUpdateSkillsetWithResponse(expected, false))
            .assertNext(response -> {
                skillsetsToDelete.add(response.getValue().getName());
                assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode());
            })
            .verifyComplete();
    }

    @Test
    public void createOrUpdateUpdatesWhenSkillsetExistsSync() {
        SearchIndexerSkillset skillset = createTestOcrSkillset();
        Response<SearchIndexerSkillset> createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(skillset,
            false, Context.NONE);
        skillsetsToDelete.add(createOrUpdateResponse.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
        SearchIndexerSkillset updatedSkillset = createTestOcrSkillset(2, skillset.getName());
        createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(updatedSkillset, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_OK, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateUpdatesWhenSkillsetExistsAsync() {
        SearchIndexerSkillset skillset = createTestOcrSkillset();

        StepVerifier.create(asyncClient.createOrUpdateSkillsetWithResponse(skillset, false))
            .assertNext(response -> {
                skillsetsToDelete.add(response.getValue().getName());
                assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode());
            })
            .verifyComplete();

        StepVerifier.create(asyncClient.createOrUpdateSkillsetWithResponse(
                createTestOcrSkillset(2, skillset.getName()), false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateUpdatesSkillsSync() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);
        SearchIndexerSkillset createdSkillset = client.createSkillset(skillset);
        skillsetsToDelete.add(createdSkillset.getName());

        // update skills
        createdSkillset.setSkills(getCreateOrUpdateSkills());

        assertObjectEquals(createdSkillset, client.createOrUpdateSkillset(createdSkillset), true, "etag",
            "@odata.etag");
    }

    @Test
    public void createOrUpdateUpdatesSkillsAsync() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        SearchIndexerSkillset createdSkillset = asyncClient.createSkillset(skillset)
            .map(created -> {
                skillsetsToDelete.add(created.getName());
                return created.setSkills(getCreateOrUpdateSkills());
            })
            .block();

        StepVerifier.create(asyncClient.createOrUpdateSkillset(createdSkillset))
            .assertNext(updated -> assertObjectEquals(createdSkillset, updated, true, "etag", "@odata.etag"))
            .verifyComplete();
    }


    @Test
    public void createOrUpdateUpdatesCognitiveServiceSync() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);
        SearchIndexerSkillset createdSkillset = client.createSkillset(skillset);
        skillsetsToDelete.add(createdSkillset.getName());

        // update skills
        createdSkillset.setCognitiveServicesAccount(new DefaultCognitiveServicesAccount()
            .setDescription("description"));

        assertObjectEquals(createdSkillset, client.createOrUpdateSkillset(createdSkillset),
            true, "etag", "@odata.etag");
    }

    @Test
    public void createOrUpdateUpdatesCognitiveServiceAsync() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        SearchIndexerSkillset createdSkillset = asyncClient.createSkillset(skillset)
            .map(created -> {
                skillsetsToDelete.add(created.getName());
                return created.setCognitiveServicesAccount(new DefaultCognitiveServicesAccount()
                    .setDescription("description"));
            })
            .block();

        StepVerifier.create(asyncClient.createOrUpdateSkillset(createdSkillset))
            .assertNext(updated -> assertObjectEquals(createdSkillset, updated, true, "etag", "@odata.etag"))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionShaperWithNestedInputsSync() {
        SearchIndexerSkillset expected = createSkillsetWithSharperSkillWithNestedInputs();
        SearchIndexerSkillset actual = client.createSkillset(expected);
        skillsetsToDelete.add(actual.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionShaperWithNestedInputsAsync() {
        SearchIndexerSkillset expected = createSkillsetWithSharperSkillWithNestedInputs();
        StepVerifier.create(asyncClient.createSkillset(expected))
            .assertNext(actual -> {
                skillsetsToDelete.add(actual.getName());
                assertObjectEquals(expected, actual, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionConditionalSync() {
        SearchIndexerSkillset expected = createTestSkillsetConditional();
        SearchIndexerSkillset actual = client.createSkillset(expected);
        skillsetsToDelete.add(expected.getName());

        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionConditionalAsync() {
        SearchIndexerSkillset expected = createTestSkillsetConditional();
        StepVerifier.create(asyncClient.createSkillset(expected))
            .assertNext(actual -> {
                skillsetsToDelete.add(actual.getName());
                assertObjectEquals(expected, actual, true, "etag");
            })
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSkillsetIfNotExistsSucceedsOnNoResourceSync() {
        SearchIndexerSkillset created = client.createOrUpdateSkillsetWithResponse(
            createSkillsetWithOcrDefaultSettings(false), true, Context.NONE).getValue();
        skillsetsToDelete.add(created.getName());

        assertNotNull(created.getETag());
    }

    @Test
    public void createOrUpdateSkillsetIfNotExistsSucceedsOnNoResourceAsync() {
        StepVerifier.create(asyncClient.createOrUpdateSkillsetWithResponse(
                createSkillsetWithOcrDefaultSettings(false), true))
            .assertNext(response -> {
                skillsetsToDelete.add(response.getValue().getName());
                assertNotNull(response.getValue().getETag());
            })
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSkillsetIfExistsSucceedsOnExistingResourceSync() {
        SearchIndexerSkillset original = client.createOrUpdateSkillsetWithResponse(
            createSkillsetWithOcrDefaultSettings(false), false, Context.NONE).getValue();
        skillsetsToDelete.add(original.getName());

        SearchIndexerSkillset updated = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original),
            false, Context.NONE).getValue();

        validateETagUpdate(original.getETag(), updated.getETag());
    }

    @Test
    public void createOrUpdateSkillsetIfExistsSucceedsOnExistingResourceAsync() {
        Mono<Tuple2<String, String>> createAndUpdateMono =
            asyncClient.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), false)
                .flatMap(response -> {
                    SearchIndexerSkillset original = response.getValue();
                    skillsetsToDelete.add(original.getName());

                    return asyncClient.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), false)
                        .map(update -> Tuples.of(original.getETag(), update.getValue().getETag()));
                });

        StepVerifier.create(createAndUpdateMono)
            .assertNext(etags -> validateETagUpdate(etags.getT1(), etags.getT2()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedSucceedsWhenResourceUnchangedSync() {
        SearchIndexerSkillset original = client.createOrUpdateSkillsetWithResponse(
            createSkillsetWithOcrDefaultSettings(false), false, Context.NONE).getValue();
        skillsetsToDelete.add(original.getName());

        SearchIndexerSkillset updated = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original),
            true, Context.NONE).getValue();

        validateETagUpdate(original.getETag(), updated.getETag());
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedSucceedsWhenResourceUnchangedAsync() {
        Mono<Tuple2<String, String>> createAndUpdateMono =
            asyncClient.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), false)
                .flatMap(response -> {
                    SearchIndexerSkillset original = response.getValue();
                    skillsetsToDelete.add(original.getName());

                    return asyncClient.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), true)
                        .map(update -> Tuples.of(original.getETag(), update.getValue().getETag()));
                });

        StepVerifier.create(createAndUpdateMono)
            .assertNext(etags -> validateETagUpdate(etags.getT1(), etags.getT2()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedFailsWhenResourceChangedSyncAndAsync() {
        SearchIndexerSkillset original = client.createOrUpdateSkillsetWithResponse(
            createSkillsetWithOcrDefaultSettings(false), false, Context.NONE).getValue();
        skillsetsToDelete.add(original.getName());

        SearchIndexerSkillset updated = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original),
            true, Context.NONE).getValue();

        // Update and check the eTags were changed
        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.createOrUpdateSkillsetWithResponse(original, true, Context.NONE));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        StepVerifier.create(asyncClient.createOrUpdateSkillsetWithResponse(original, true))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException exAsync = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exAsync.getResponse().getStatusCode());
            });

        validateETagUpdate(original.getETag(), updated.getETag());
    }

    @Test
    public void deleteSkillsetIfNotChangedWorksOnlyOnCurrentResourceSync() {
        SearchIndexerSkillset stale = client.createOrUpdateSkillsetWithResponse(
                createSkillsetWithOcrDefaultSettings(false), true, Context.NONE)
            .getValue();

        SearchIndexerSkillset current = client.createOrUpdateSkillsetWithResponse(stale, true, Context.NONE)
            .getValue();

        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.deleteSkillsetWithResponse(stale, true, Context.NONE));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        assertDoesNotThrow(() -> client.deleteSkillsetWithResponse(current, true, Context.NONE));
    }

    @Test
    public void deleteSkillsetIfNotChangedWorksOnlyOnCurrentResourceAsync() {
        SearchIndexerSkillset stale = asyncClient.createOrUpdateSkillsetWithResponse(
                createSkillsetWithOcrDefaultSettings(false), true)
            .map(Response::getValue)
            .block();

        SearchIndexerSkillset current = asyncClient.createOrUpdateSkillsetWithResponse(stale, true)
            .map(Response::getValue)
            .block();

        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(stale, true))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
            });

        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(current, true))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void deleteSkillsetIfExistsWorksOnlyWhenResourceExistsSyncAndAsync() {
        SearchIndexerSkillset skillset = client.createOrUpdateSkillsetWithResponse(
                createSkillsetWithOcrDefaultSettings(false), false, Context.NONE)
            .getValue();

        client.deleteSkillsetWithResponse(skillset, true, Context.NONE);

        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.deleteSkillsetWithResponse(skillset, true, Context.NONE));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(skillset, true))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException exAsync = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exAsync.getResponse().getStatusCode());
            });
    }

    private static InputFieldMappingEntry simpleInputFieldMappingEntry(String name, String source) {
        return new InputFieldMappingEntry(name).setSource(source);
    }

    private static OutputFieldMappingEntry createOutputFieldMappingEntry(String name, String targetName) {
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

    static SearchIndexerSkillset mutateSkillsInSkillset(SearchIndexerSkillset skillset) {
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
        outputs = Collections.singletonList(createOutputFieldMappingEntry("namedEntities", "myEntities"));
        skills.add(new EntityRecognitionSkill(inputs, outputs, EntityRecognitionSkillVersion.V3)
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
        outputs = Collections.singletonList(createOutputFieldMappingEntry("confidenceScores", "mySentiment"));
        skills.add(new SentimentSkill(inputs, outputs, SentimentSkillVersion.V3)
            .setDefaultLanguageCode(sentimentLanguageCode)
            .setName("mysentiment")
            .setDescription("Tested Sentiment skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-sentiment-skillset", 48),
            skills).setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestSkillsetOcrKeyPhrase(OcrSkillLanguage ocrLanguageCode,
        KeyPhraseExtractionSkillLanguage keyPhraseLanguageCode) {
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

    SearchIndexerSkillset createTestOcrSkillset() {
        return createTestOcrSkillset(1, testResourceNamer.randomName("testskillset", 48));
    }

    SearchIndexerSkillset createTestOcrSkillset(int repeat, String name) {
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

        return new SearchIndexerSkillset(name, skills)
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
            testResourceNamer.randomName(SkillsetManagementTests.OCR_SKILLSET_NAME, 48), skills)
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
            .singletonList(createOutputFieldMappingEntry("confidenceScores", "mySentiment"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new SentimentSkill(inputs, outputs, SentimentSkillVersion.V3)
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
            .singletonList(createOutputFieldMappingEntry("namedEntities", "myEntities"));

        List<SearchIndexerSkill> skills = Collections.singletonList(
            new EntityRecognitionSkill(inputs, outputs, EntityRecognitionSkillVersion.V3)
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
        List<InputFieldMappingEntry> inputs = createNestedInputFieldMappingEntry();
        List<OutputFieldMappingEntry> outputs = createOutputFieldMappingEntry();

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

    private static List<InputFieldMappingEntry> createNestedInputFieldMappingEntry() {
        return Collections.singletonList(
            new InputFieldMappingEntry("doc")
                .setSourceContext("/document")
                .setInputs(Arrays.asList(
                    simpleInputFieldMappingEntry("text", "/document/content"),
                    simpleInputFieldMappingEntry("images", "/document/normalized_images/*")))
        );
    }

    private static List<OutputFieldMappingEntry> createOutputFieldMappingEntry() {
        return Collections.singletonList(createOutputFieldMappingEntry("output", "myOutput"));
    }

    private static List<SearchIndexerSkill> getCreateOrUpdateSkills() {
        return Collections.singletonList(new KeyPhraseExtractionSkill(
            Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext")),
            Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases")))
            .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
            .setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE));
    }
}
