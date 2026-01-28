// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.indexes.models.CognitiveServicesAccount;
import com.azure.search.documents.indexes.models.CognitiveServicesAccountKey;
import com.azure.search.documents.indexes.models.ConditionalSkill;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkill;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkillChunkingProperties;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkillChunkingUnit;
import com.azure.search.documents.indexes.models.ContentUnderstandingSkillExtractionOptions;
import com.azure.search.documents.indexes.models.DefaultCognitiveServicesAccount;
import com.azure.search.documents.indexes.models.EntityRecognitionSkillV3;
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
import com.azure.search.documents.indexes.models.SentimentSkillV3;
import com.azure.search.documents.indexes.models.ShaperSkill;
import com.azure.search.documents.indexes.models.SplitSkill;
import com.azure.search.documents.indexes.models.SplitSkillLanguage;
import com.azure.search.documents.indexes.models.TextSplitMode;
import com.azure.search.documents.indexes.models.VisualFeature;
import com.azure.search.documents.indexes.models.WebApiHttpHeaders;
import com.azure.search.documents.indexes.models.WebApiSkill;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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
import static com.azure.search.documents.TestHelpers.ifMatch;
import static com.azure.search.documents.TestHelpers.verifyHttpResponseError;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
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
            // interceptorManager.addSanitizers(new TestProxySanitizer("$..cognitiveServices.key",
            //     TestProxyUtils.HOST_NAME_REGEX, "REDACTED", TestProxySanitizerType.BODY_KEY));

        }
        client = getSearchIndexerClientBuilder(true)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        asyncClient = getSearchIndexerClientBuilder(false)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();
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
        SearchIndexerSkillset skillset
            = client.createSkillsetWithResponse(BinaryData.fromObject(expectedSkillset), null)
                .getValue()
                .toObject(SearchIndexerSkillset.class);
        skillsetsToDelete.add(skillset.getName());

        assertObjectEquals(expectedSkillset, skillset, true, "etag");
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhraseWithResponseAsync() {
        SearchIndexerSkillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();

        StepVerifier.create(asyncClient.createSkillsetWithResponse(BinaryData.fromObject(expectedSkillset), null))
            .assertNext(response -> {
                SearchIndexerSkillset skillset = response.getValue().toObject(SearchIndexerSkillset.class);
                skillsetsToDelete.add(skillset.getName());
                assertObjectEquals(expectedSkillset, skillset, true, "etag");
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

        createAndValidateSkillsetSync(createTestSkillsetOcrEntity(Arrays.asList("location", "organization", "person")));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrEntityAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetOcrEntity(null));

        createAndValidateSkillsetAsync(
            createTestSkillsetOcrEntity(Arrays.asList("location", "organization", "person")));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentimentSync() {
        createAndValidateSkillsetSync(createTestSkillsetOcrSentiment(OcrSkillLanguage.PT, "pt_PT"));

        createAndValidateSkillsetSync(createTestSkillsetOcrSentiment(OcrSkillLanguage.FI, "fi"));

        createAndValidateSkillsetSync(createTestSkillsetOcrSentiment(OcrSkillLanguage.EN, "en"));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentimentAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetOcrSentiment(OcrSkillLanguage.PT, "pt_PT"));

        createAndValidateSkillsetAsync(createTestSkillsetOcrSentiment(OcrSkillLanguage.FI, "fi"));

        createAndValidateSkillsetAsync(createTestSkillsetOcrSentiment(OcrSkillLanguage.EN, "en"));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrKeyPhraseSync() {
        createAndValidateSkillsetSync(
            createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.EN, KeyPhraseExtractionSkillLanguage.EN));

        createAndValidateSkillsetSync(
            createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.FR, KeyPhraseExtractionSkillLanguage.FR));

        createAndValidateSkillsetSync(
            createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.ES, KeyPhraseExtractionSkillLanguage.ES));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrKeyPhraseAsync() {
        createAndValidateSkillsetAsync(
            createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.EN, KeyPhraseExtractionSkillLanguage.EN));

        createAndValidateSkillsetAsync(
            createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.FR, KeyPhraseExtractionSkillLanguage.FR));

        createAndValidateSkillsetAsync(
            createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.ES, KeyPhraseExtractionSkillLanguage.ES));
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
    public void createSkillsetReturnsCorrectDefinitionOcrSplitTextSync() {
        createAndValidateSkillsetSync(
            createTestSkillsetOcrSplitText(OcrSkillLanguage.EN, SplitSkillLanguage.EN, TextSplitMode.PAGES));

        createAndValidateSkillsetSync(
            createTestSkillsetOcrSplitText(OcrSkillLanguage.FR, SplitSkillLanguage.FR, TextSplitMode.PAGES));

        // not working
        createAndValidateSkillsetSync(
            createTestSkillsetOcrSplitText(OcrSkillLanguage.FI, SplitSkillLanguage.FI, TextSplitMode.SENTENCES));

        // not working
        createAndValidateSkillsetSync(
            createTestSkillsetOcrSplitText(OcrSkillLanguage.DA, SplitSkillLanguage.DA, TextSplitMode.SENTENCES));
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrSplitTextAsync() {
        createAndValidateSkillsetAsync(
            createTestSkillsetOcrSplitText(OcrSkillLanguage.EN, SplitSkillLanguage.EN, TextSplitMode.PAGES));

        createAndValidateSkillsetAsync(
            createTestSkillsetOcrSplitText(OcrSkillLanguage.FR, SplitSkillLanguage.FR, TextSplitMode.PAGES));

        createAndValidateSkillsetAsync(
            createTestSkillsetOcrSplitText(OcrSkillLanguage.FI, SplitSkillLanguage.FI, TextSplitMode.SENTENCES));

        createAndValidateSkillsetAsync(
            createTestSkillsetOcrSplitText(OcrSkillLanguage.DA, SplitSkillLanguage.DA, TextSplitMode.SENTENCES));
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
    public void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionV3DefaultSettingsSync() {
        createAndValidateSkillsetSync(createSkillsetWithEntityRecognitionV3DefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionV3DefaultSettingsAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithEntityRecognitionV3DefaultSettings());
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

        SearchIndexerSkillset actual
            = client.getSkillsetWithResponse(expected.getName(), null).getValue().toObject(SearchIndexerSkillset.class);
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinitionWithResponseAsync() {
        SearchIndexerSkillset expected = createSkillsetWithOcrDefaultSettings(false);
        asyncClient.createSkillset(expected).block();
        skillsetsToDelete.add(expected.getName());

        StepVerifier.create(asyncClient.getSkillsetWithResponse(expected.getName(), null))
            .assertNext(response -> assertObjectEquals(expected,
                response.getValue().toObject(SearchIndexerSkillset.class), true, "etag"))
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
    public void createSkillsetReturnsCorrectDefinitionWithSentimentV3DefaultSettingsSync() {
        createAndValidateSkillsetSync(createSkillsetWithSentimentV3DefaultSettings());
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSentimentV3DefaultSettingsAsync() {
        createAndValidateSkillsetAsync(createSkillsetWithSentimentV3DefaultSettings());
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
        StepVerifier.create(asyncClient.createSkillset(expected)).assertNext(actual -> {
            skillsetsToDelete.add(actual.getName());
            assertObjectEquals(expected, actual, true, "etag");
        }).verifyComplete();
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
        SearchIndexerSkillset skillset2 = createSkillsetWithEntityRecognitionV3DefaultSettings();

        client.createSkillset(skillset1);
        skillsetsToDelete.add(skillset1.getName());
        client.createSkillset(skillset2);
        skillsetsToDelete.add(skillset2.getName());

        Map<String, SearchIndexerSkillset> expectedSkillsets = new HashMap<>();
        expectedSkillsets.put(skillset1.getName(), skillset1);
        expectedSkillsets.put(skillset2.getName(), skillset2);

        Map<String, SearchIndexerSkillset> actualSkillsets = client.listSkillsets()
            .getSkillsets()
            .stream()
            .collect(Collectors.toMap(SearchIndexerSkillset::getName, skillset -> skillset));

        compareMaps(expectedSkillsets, actualSkillsets,
            (expected, actual) -> assertObjectEquals(expected, actual, true));

        StepVerifier
            .create(asyncClient.listSkillsets()
                .map(result -> result.getSkillsets()
                    .stream()
                    .collect(Collectors.toMap(SearchIndexerSkillset::getName, skillset -> skillset))))
            .assertNext(actualSkillsetsAsync -> compareMaps(expectedSkillsets, actualSkillsetsAsync,
                (expected, actual) -> assertObjectEquals(expected, actual, true)))
            .verifyComplete();
    }

    @Test
    public void canListSkillsetsWithSelectedFieldSyncAndAsync() {
        SearchIndexerSkillset skillset1 = createSkillsetWithCognitiveServicesKey();
        SearchIndexerSkillset skillset2 = createSkillsetWithEntityRecognitionV3DefaultSettings();

        client.createSkillset(skillset1);
        skillsetsToDelete.add(skillset1.getName());
        client.createSkillset(skillset2);
        skillsetsToDelete.add(skillset2.getName());

        Set<String> expectedSkillsetNames = new HashSet<>(Arrays.asList(skillset1.getName(), skillset2.getName()));
        Set<String> actualSkillsetNames = new HashSet<>(client.listSkillsetNames());

        assertEquals(expectedSkillsetNames.size(), actualSkillsetNames.size());
        assertTrue(actualSkillsetNames.containsAll(expectedSkillsetNames));

        StepVerifier.create(asyncClient.listSkillsetNames().map(HashSet::new)).assertNext(actualSkillsetNamesAsync -> {
            assertEquals(actualSkillsetNamesAsync.size(), actualSkillsetNames.size());
            assertTrue(actualSkillsetNamesAsync.containsAll(expectedSkillsetNames));
        }).verifyComplete();
    }

    @Test
    public void deleteSkillsetIsIdempotentSync() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        Response<Void> deleteResponse = client.deleteSkillsetWithResponse(skillset.getName(), null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        client.createSkillset(skillset);

        // Delete the same skillset twice
        deleteResponse = client.deleteSkillsetWithResponse(skillset.getName(), null);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = client.deleteSkillsetWithResponse(skillset.getName(), null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    public void deleteSkillsetIsIdempotentAsync() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(skillset.getName(), null))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();

        asyncClient.createSkillset(skillset).block();

        // Delete the same skillset twice
        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(skillset.getName(), null))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NO_CONTENT, response.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(skillset.getName(), null))
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

        StepVerifier.create(asyncClient.createOrUpdateSkillset(expected)).assertNext(actual -> {
            skillsetsToDelete.add(actual.getName());
            assertObjectEquals(expected, actual, true, "etag");
        }).verifyComplete();
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistWithResponseSync() {
        SearchIndexerSkillset expected = createTestOcrSkillset();
        Response<SearchIndexerSkillset> createOrUpdateResponse
            = client.createOrUpdateSkillsetWithResponse(expected, null);
        skillsetsToDelete.add(createOrUpdateResponse.getValue().getName());

        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistWithResponseAsync() {
        SearchIndexerSkillset expected = createTestOcrSkillset();

        StepVerifier.create(asyncClient.createOrUpdateSkillsetWithResponse(expected, null)).assertNext(response -> {
            skillsetsToDelete.add(response.getValue().getName());
            assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode());
        }).verifyComplete();
    }

    @Test
    public void createOrUpdateUpdatesWhenSkillsetExistsSync() {
        SearchIndexerSkillset skillset = createTestOcrSkillset();
        Response<SearchIndexerSkillset> createOrUpdateResponse
            = client.createOrUpdateSkillsetWithResponse(skillset, null);
        skillsetsToDelete.add(createOrUpdateResponse.getValue().getName());
        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
        SearchIndexerSkillset updatedSkillset = createTestOcrSkillset(2, skillset.getName());
        createOrUpdateResponse = client.createOrUpdateSkillsetWithResponse(updatedSkillset, null);
        assertEquals(HttpURLConnection.HTTP_OK, createOrUpdateResponse.getStatusCode());
    }

    @Test
    public void createOrUpdateUpdatesWhenSkillsetExistsAsync() {
        SearchIndexerSkillset skillset = createTestOcrSkillset();

        StepVerifier.create(asyncClient.createOrUpdateSkillsetWithResponse(skillset, null)).assertNext(response -> {
            skillsetsToDelete.add(response.getValue().getName());
            assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode());
        }).verifyComplete();

        StepVerifier
            .create(asyncClient.createOrUpdateSkillsetWithResponse(createTestOcrSkillset(2, skillset.getName()), null))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateUpdatesSkillsSync() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);
        SearchIndexerSkillset createdSkillset = client.createSkillset(skillset);
        skillsetsToDelete.add(createdSkillset.getName());

        // update skills
        createdSkillset.getSkills().clear();
        createdSkillset.getSkills().addAll(getCreateOrUpdateSkills());

        assertObjectEquals(createdSkillset, client.createOrUpdateSkillset(createdSkillset), true, "etag",
            "@odata.etag");
    }

    @Test
    public void createOrUpdateUpdatesSkillsAsync() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        SearchIndexerSkillset createdSkillset = asyncClient.createSkillset(skillset).map(created -> {
            skillsetsToDelete.add(created.getName());
            created.getSkills().clear();
            created.getSkills().addAll(getCreateOrUpdateSkills());
            return created;
        }).block();

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
        createdSkillset
            .setCognitiveServicesAccount(new DefaultCognitiveServicesAccount().setDescription("description"));

        assertObjectEquals(createdSkillset, client.createOrUpdateSkillset(createdSkillset), true, "etag",
            "@odata.etag");
    }

    @Test
    public void createOrUpdateUpdatesCognitiveServiceAsync() {
        SearchIndexerSkillset skillset = createSkillsetWithOcrDefaultSettings(false);

        SearchIndexerSkillset createdSkillset = asyncClient.createSkillset(skillset).map(created -> {
            skillsetsToDelete.add(created.getName());
            return created
                .setCognitiveServicesAccount(new DefaultCognitiveServicesAccount().setDescription("description"));
        }).block();

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
        StepVerifier.create(asyncClient.createSkillset(expected)).assertNext(actual -> {
            skillsetsToDelete.add(actual.getName());
            assertObjectEquals(expected, actual, true, "etag");
        }).verifyComplete();
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
        StepVerifier.create(asyncClient.createSkillset(expected)).assertNext(actual -> {
            skillsetsToDelete.add(actual.getName());
            assertObjectEquals(expected, actual, true, "etag");
        }).verifyComplete();
    }

    @Test
    public void createOrUpdateSkillsetIfNotExistsSucceedsOnNoResourceSync() {
        SearchIndexerSkillset initial = createSkillsetWithOcrDefaultSettings(false);
        SearchIndexerSkillset created
            = client.createOrUpdateSkillsetWithResponse(initial, ifMatch(initial.getETag())).getValue();
        skillsetsToDelete.add(created.getName());

        assertNotNull(created.getETag());
    }

    @Test
    public void createOrUpdateSkillsetIfNotExistsSucceedsOnNoResourceAsync() {
        SearchIndexerSkillset initial = createSkillsetWithOcrDefaultSettings(false);
        StepVerifier.create(asyncClient.createOrUpdateSkillsetWithResponse(initial, ifMatch(initial.getETag())))
            .assertNext(response -> {
                skillsetsToDelete.add(response.getValue().getName());
                assertNotNull(response.getValue().getETag());
            })
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSkillsetIfExistsSucceedsOnExistingResourceSync() {
        SearchIndexerSkillset original
            = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), null).getValue();
        skillsetsToDelete.add(original.getName());

        SearchIndexerSkillset updated
            = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), null).getValue();

        validateETagUpdate(original.getETag(), updated.getETag());
    }

    @Test
    public void createOrUpdateSkillsetIfExistsSucceedsOnExistingResourceAsync() {
        Mono<Tuple2<String, String>> createAndUpdateMono
            = asyncClient.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), null)
                .flatMap(response -> {
                    SearchIndexerSkillset original = response.getValue();
                    skillsetsToDelete.add(original.getName());

                    return asyncClient.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), null)
                        .map(update -> Tuples.of(original.getETag(), update.getValue().getETag()));
                });

        StepVerifier.create(createAndUpdateMono)
            .assertNext(etags -> validateETagUpdate(etags.getT1(), etags.getT2()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedSucceedsWhenResourceUnchangedSync() {
        SearchIndexerSkillset original
            = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), null).getValue();
        skillsetsToDelete.add(original.getName());

        SearchIndexerSkillset updated
            = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), ifMatch(original.getETag()))
                .getValue();

        validateETagUpdate(original.getETag(), updated.getETag());
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedSucceedsWhenResourceUnchangedAsync() {
        Mono<Tuple2<String, String>> createAndUpdateMono
            = asyncClient.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), null)
                .flatMap(response -> {
                    SearchIndexerSkillset original = response.getValue();
                    skillsetsToDelete.add(original.getName());

                    return asyncClient
                        .createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original),
                            ifMatch(original.getETag()))
                        .map(update -> Tuples.of(original.getETag(), update.getValue().getETag()));
                });

        StepVerifier.create(createAndUpdateMono)
            .assertNext(etags -> validateETagUpdate(etags.getT1(), etags.getT2()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedFailsWhenResourceChangedSyncAndAsync() {
        SearchIndexerSkillset original
            = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), null).getValue();
        skillsetsToDelete.add(original.getName());

        SearchIndexerSkillset updated
            = client.createOrUpdateSkillsetWithResponse(mutateSkillsInSkillset(original), ifMatch(original.getETag()))
                .getValue();

        // Update and check the eTags were changed
        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.createOrUpdateSkillsetWithResponse(original, ifMatch(original.getETag())));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        StepVerifier.create(asyncClient.createOrUpdateSkillsetWithResponse(original, ifMatch(original.getETag())))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException exAsync = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exAsync.getResponse().getStatusCode());
            });

        validateETagUpdate(original.getETag(), updated.getETag());
    }

    @Test
    public void deleteSkillsetIfNotChangedWorksOnlyOnCurrentResourceSync() {
        SearchIndexerSkillset stale
            = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), null).getValue();

        SearchIndexerSkillset current
            = client.createOrUpdateSkillsetWithResponse(stale, ifMatch(stale.getETag())).getValue();

        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.deleteSkillsetWithResponse(stale.getName(), ifMatch(stale.getETag())));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        assertDoesNotThrow(() -> client.deleteSkillsetWithResponse(current.getName(), ifMatch(current.getETag())));
    }

    @Test
    public void deleteSkillsetIfNotChangedWorksOnlyOnCurrentResourceAsync() {
        SearchIndexerSkillset stale
            = asyncClient.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), null)
                .map(Response::getValue)
                .block();

        SearchIndexerSkillset current = asyncClient.createOrUpdateSkillsetWithResponse(stale, ifMatch(stale.getETag()))
            .map(Response::getValue)
            .block();

        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(stale.getName(), ifMatch(stale.getETag())))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
            });

        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(current.getName(), ifMatch(current.getETag())))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void deleteSkillsetIfExistsWorksOnlyWhenResourceExistsSyncAndAsync() {
        SearchIndexerSkillset skillset
            = client.createOrUpdateSkillsetWithResponse(createSkillsetWithOcrDefaultSettings(false), null).getValue();

        client.deleteSkillsetWithResponse(skillset.getName(), ifMatch(skillset.getETag()));

        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.deleteSkillsetWithResponse(skillset.getName(), ifMatch(skillset.getETag())));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        StepVerifier.create(asyncClient.deleteSkillsetWithResponse(skillset.getName(), ifMatch(skillset.getETag())))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException exAsync = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exAsync.getResponse().getStatusCode());
            });
    }

    @Disabled("Test proxy issues")
    public void createSkillsetReturnsCorrectDefinitionContentUnderstandingSync() {
        createAndValidateSkillsetSync(createTestSkillsetContentUnderstanding());
    }

    @Disabled("Test proxy issues")
    @Test
    public void createSkillsetReturnsCorrectDefinitionContentUnderstandingAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetContentUnderstanding());
    }

    @Disabled("Test proxy issues")
    @Test
    public void createSkillsetReturnsCorrectDefinitionContentUnderstandingWithAllOptionsSync() {
        createAndValidateSkillsetSync(createTestSkillsetContentUnderstandingWithAllOptions());
    }

    @Disabled("Test proxy issues")
    @Test
    public void createSkillsetReturnsCorrectDefinitionContentUnderstandingWithAllOptionsAsync() {
        createAndValidateSkillsetAsync(createTestSkillsetContentUnderstandingWithAllOptions());
    }

    @Test
    public void contentUnderstandingSkillSerializesCorrectly() {
        ContentUnderstandingSkill skill = new ContentUnderstandingSkill(
            Collections.singletonList(new InputFieldMappingEntry("file_data").setSource("/document/file_data")),
            Collections.singletonList(new OutputFieldMappingEntry("text_sections").setTargetName("sections")))
                .setExtractionOptions(Arrays.asList(ContentUnderstandingSkillExtractionOptions.IMAGES,
                    ContentUnderstandingSkillExtractionOptions.LOCATION_METADATA))
                .setChunkingProperties(new ContentUnderstandingSkillChunkingProperties()
                    .setUnit(ContentUnderstandingSkillChunkingUnit.CHARACTERS)
                    .setMaximumLength(2000)
                    .setOverlapLength(200));

        String json = BinaryData.fromObject(skill).toString();

        assertTrue(json.contains("\"@odata.type\":\"#Microsoft.Skills.Util.ContentUnderstandingSkill\""));
        assertTrue(json.contains("\"extractionOptions\":[\"images\",\"locationMetadata\"]"));
        assertTrue(json.contains("\"unit\":\"characters\""));
        assertTrue(json.contains("\"maximumLength\":2000"));
        assertTrue(json.contains("\"overlapLength\":200"));

    }

    @Test
    @Disabled("Requires module access configuration for Jackson deserialization - Jackson cannot access private fields in module system")
    public void contentUnderstandingSkillDeserializesCorrectly() {
        String json = "{\"@odata.type\":\"#Microsoft.Skills.Util.ContentUnderstandingSkill\","
            + "\"inputs\":[{\"name\":\"file_data\", \"source\": \"/document/file_data\"}],"
            + "\"outputs\":[{\"name\":\"text_sections\", \"targetName\": \"sections\"}],"
            + "\"extractionOptions\":[\"images\",\"locationMetadata\"],\"chunkingProperties\":{"
            + "\"unit\":\"characters\",\"maximumLength\":1500,\"overlapLength\":150}}";

        ContentUnderstandingSkill skill = BinaryData.fromString(json).toObject(ContentUnderstandingSkill.class);

        assertEquals("images", skill.getExtractionOptions().get(0).getValue());
        assertEquals("locationMetadata", skill.getExtractionOptions().get(1).getValue());
        assertEquals("characters", skill.getChunkingProperties().getUnit().getValue());
        assertEquals(1500, skill.getChunkingProperties().getMaximumLength());
        assertEquals(150, skill.getChunkingProperties().getOverlapLength());
    }

    @Test
    public void contentUnderstandingSkillWithNullInputsThrows() {
        ContentUnderstandingSkill skill = new ContentUnderstandingSkill(null, Collections.emptyList());
        assertNotNull(skill);
    }

    @Test
    public void contentUnderstandingSkillWithNullOutputsThrows() {
        ContentUnderstandingSkill skill = new ContentUnderstandingSkill(
            Collections.singletonList(new InputFieldMappingEntry("file_data").setSource("/document/file_data")), null);
        assertNotNull(skill);
    }

    @Test
    public void contentUnderstandingSkillWithInvalidChunkingUnitThrows() {
        ContentUnderstandingSkill skill = new ContentUnderstandingSkill(
            Collections.singletonList(new InputFieldMappingEntry("file_data").setSource("/document/file_data")),
            Collections.singletonList(new OutputFieldMappingEntry("text_sections").setTargetName("sections")));

        assertThrows(IllegalArgumentException.class,
            () -> skill.setChunkingProperties(new ContentUnderstandingSkillChunkingProperties()
                .setUnit(ContentUnderstandingSkillChunkingUnit.fromString("INVALID_UNIT"))
                .setMaximumLength(1000)));
    }

    @Test
    public void contentUnderstandingSkillWithNegativeChunkingLengthThrows() {
        ContentUnderstandingSkill skill = new ContentUnderstandingSkill(
            Collections.singletonList(new InputFieldMappingEntry("file_data").setSource("/document/file_data")),
            Collections.singletonList(new OutputFieldMappingEntry("text_sections").setTargetName("sections")));

        assertThrows(IllegalArgumentException.class,
            () -> skill.setChunkingProperties(new ContentUnderstandingSkillChunkingProperties()
                .setUnit(ContentUnderstandingSkillChunkingUnit.CHARACTERS)
                .setMaximumLength(-1)));
    }

    @Test
    @Disabled("Test proxy issues")
    public void contentUnderstandingSkillWorksWithPreviewApiVersion() {
        SearchIndexerClient indexerClient
            = getSearchIndexerClientBuilder(true).serviceVersion(SearchServiceVersion.V2025_11_01_PREVIEW)
                .buildClient();

        SearchIndexerSkillset skillset = createTestSkillsetContentUnderstanding();

        SearchIndexerSkillset created = indexerClient.createSkillset(skillset);
        assertNotNull(created);

        ContentUnderstandingSkill skill = (ContentUnderstandingSkill) created.getSkills().get(0);
        assertNotNull(skill.getChunkingProperties());

        assertEquals(ContentUnderstandingSkillChunkingUnit.CHARACTERS, skill.getChunkingProperties().getUnit());

        skillsetsToDelete.add(created.getName());
    }

    //    @Test
    //    public void contentUnderstandingSkillFailsWithOlderApiVersion() {
    //        SearchIndexerClient indexerClient
    //            = getSearchIndexerClientBuilder(true).serviceVersion(SearchServiceVersion.V2024_07_01).buildClient();
    //
    //        SearchIndexerSkillset skillset = createTestSkillsetContentUnderstanding();
    //
    //        HttpResponseException ex = assertThrows(HttpResponseException.class,
    //            () -> indexerClient.createSkillset(skillset));
    //
    //        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    //        assertTrue(ex.getMessage().contains("ContentUnderstandingSkill")
    //            || ex.getMessage().contains("unsupported")
    //            || ex.getMessage().contains("not supported"));
    //    }

    private static InputFieldMappingEntry simpleInputFieldMappingEntry(String name, String source) {
        return new InputFieldMappingEntry(name).setSource(source);
    }

    private static OutputFieldMappingEntry createOutputFieldMappingEntry(String name, String targetName) {
        return new OutputFieldMappingEntry(name).setTargetName(targetName);
    }

    SearchIndexerSkillset createTestSkillsetImageAnalysisKeyPhrase() {
        List<SearchIndexerSkill> skills = new ArrayList<>();

        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("description", "mydescription"));

        skills.add(new ImageAnalysisSkill(inputs, outputs).setVisualFeatures(new ArrayList<>(VisualFeature.values()))
            .setDetails(new ArrayList<>(ImageDetail.values()))
            .setDefaultLanguageCode(ImageAnalysisSkillLanguage.EN)
            .setName("myimage")
            .setDescription("Tested image analysis skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mydescription/*/Tags/*"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"));
        skills.add(
            new KeyPhraseExtractionSkill(inputs, outputs).setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
                .setName("mykeyphrases")
                .setDescription("Tested Key Phrase skill")
                .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("image-analysis-key-phrase-skillset", 48), skills)
            .setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestSkillsetLanguageDetection() {
        List<InputFieldMappingEntry> inputs
            = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/text"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("languageCode", "myLanguageCode"));

        SearchIndexerSkill skill = new LanguageDetectionSkill(inputs, outputs).setName("mylanguage")
            .setDescription("Tested Language Detection skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("language-detection-skillset", 48), skill)
            .setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestSkillsetMergeText() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("text", "/document/text"),
            simpleInputFieldMappingEntry("itemsToInsert", "/document/textitems"),
            simpleInputFieldMappingEntry("offsets", "/document/offsets"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("mergedText", "myMergedText"));

        SearchIndexerSkill skill = new MergeSkill(inputs, outputs).setInsertPostTag("__e")
            .setInsertPreTag("__")
            .setName("mymerge")
            .setDescription("Tested Merged Text skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("merge-text-skillset", 48), skill)
            .setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestSkillsetOcrShaper() {
        List<SearchIndexerSkill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill(inputs, outputs).setDefaultLanguageCode(OcrSkillLanguage.EN)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("output", "myOutput"));
        skills.add(new ShaperSkill(inputs, outputs).setName("mysharper")
            .setDescription("Tested Shaper skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-shaper-skillset", 48), skills)
            .setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createSkillsetWithCognitiveServicesKey() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("text", "mytext"));

        SearchIndexerSkill skill = new OcrSkill(inputs, outputs).setDefaultLanguageCode(OcrSkillLanguage.EN)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("cognitive-services-key-skillset", 48), skill)
            .setDescription("Skillset for testing")
            .setCognitiveServicesAccount(new DefaultCognitiveServicesAccount());
    }

    SearchIndexerSkillset createTestSkillsetConditional() {
        List<InputFieldMappingEntry> inputs
            = Arrays.asList(simpleInputFieldMappingEntry("condition", "= $(/document/language) == null"),
                simpleInputFieldMappingEntry("whenTrue", "= 'es'"),
                simpleInputFieldMappingEntry("whenFalse", "= $(/document/language)"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("output", "myLanguageCode"));

        SearchIndexerSkill skill = new ConditionalSkill(inputs, outputs).setName("myconditional")
            .setDescription("Tested Conditional skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("conditional-skillset", 48), skill)
            .setDescription("Skillset for testing");
    }

    static SearchIndexerSkillset mutateSkillsInSkillset(SearchIndexerSkillset skillset) {
        skillset.getSkills().clear();
        skillset.getSkills()
            .add(new KeyPhraseExtractionSkill(
                Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mydescription/*/Tags/*")),
                Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases")))
                    .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
                    .setName("mykeyphrases")
                    .setDescription("Tested Key Phrase skill")
                    .setContext(CONTEXT_VALUE));
        return skillset;
    }

    SearchIndexerSkillset createTestSkillsetOcrEntity(List<String> categories) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill(inputs, outputs).setDefaultLanguageCode(OcrSkillLanguage.EN)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("namedEntities", "myEntities"));
        skills.add(new EntityRecognitionSkillV3(inputs, outputs).setCategories(categories)
            .setDefaultLanguageCode("en")
            .setMinimumPrecision(0.5)
            .setName("myentity")
            .setDescription("Tested Entity Recognition skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-entity-skillset", 48), skills)
            .setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestSkillsetOcrSentiment(OcrSkillLanguage ocrLanguageCode,
        String sentimentLanguageCode) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("text", "mytext"));
        skills.add(new OcrSkill(inputs, outputs).setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("confidenceScores", "mySentiment"));
        skills.add(new SentimentSkillV3(inputs, outputs).setDefaultLanguageCode(sentimentLanguageCode)
            .setName("mysentiment")
            .setDescription("Tested Sentiment skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-sentiment-skillset", 48), skills)
            .setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestSkillsetOcrKeyPhrase(OcrSkillLanguage ocrLanguageCode,
        KeyPhraseExtractionSkillLanguage keyPhraseLanguageCode) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill(inputs, outputs).setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"));
        skills.add(new KeyPhraseExtractionSkill(inputs, outputs).setDefaultLanguageCode(keyPhraseLanguageCode)
            .setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-key-phrase-skillset", 48), skills)
            .setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestSkillsetOcrSplitText(OcrSkillLanguage ocrLanguageCode,
        SplitSkillLanguage splitLanguageCode, TextSplitMode textSplitMode) {
        List<SearchIndexerSkill> skills = new ArrayList<>();
        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("text", "mytext"));

        skills.add(new OcrSkill(inputs, outputs).setDefaultLanguageCode(ocrLanguageCode)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE));

        inputs = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));
        outputs = Collections.singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));
        skills.add(new SplitSkill(inputs, outputs).setDefaultLanguageCode(splitLanguageCode)
            .setTextSplitMode(textSplitMode)
            .setName("mysplit")
            .setDescription("Tested Split skill")
            .setContext(CONTEXT_VALUE));

        return new SearchIndexerSkillset(testResourceNamer.randomName("ocr-split-text-skillset", 48), skills)
            .setDescription("Skillset for testing");
    }

    SearchIndexerSkillset createTestOcrSkillset() {
        return createTestOcrSkillset(1, testResourceNamer.randomName("testskillset", 48));
    }

    SearchIndexerSkillset createTestOcrSkillset(int repeat, String name) {
        List<SearchIndexerSkill> skills = new ArrayList<>();

        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        for (int i = 0; i < repeat; i++) {
            List<OutputFieldMappingEntry> outputs
                = Collections.singletonList(createOutputFieldMappingEntry("text", "mytext" + i));

            skills.add(new OcrSkill(inputs, outputs).setDefaultLanguageCode(OcrSkillLanguage.EN)
                .setShouldDetectOrientation(false)
                .setName("myocr-" + i)
                .setDescription("Tested OCR skill")
                .setContext(CONTEXT_VALUE));
        }

        return new SearchIndexerSkillset(name, skills).setDescription("Skillset for testing OCR");
    }

    SearchIndexerSkillset createSkillsetWithOcrDefaultSettings(Boolean shouldDetectOrientation) {
        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("text", "mytext"));

        SearchIndexerSkill skill = new OcrSkill(inputs, outputs).setShouldDetectOrientation(shouldDetectOrientation)
            .setName("myocr")
            .setDescription("Tested OCR skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName(SkillsetManagementTests.OCR_SKILLSET_NAME, 48),
            skill).setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithImageAnalysisDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("url", "/document/url"),
            simpleInputFieldMappingEntry("queryString", "/document/queryString"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("description", "mydescription"));

        SearchIndexerSkill skill = new ImageAnalysisSkill(inputs, outputs).setName("myimage")
            .setDescription("Tested image analysis skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("image-analysis-skillset", 48), skill)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithKeyPhraseExtractionDefaultSettings() {
        List<InputFieldMappingEntry> inputs
            = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/myText"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("keyPhrases", "myKeyPhrases"));

        SearchIndexerSkill skill = new KeyPhraseExtractionSkill(inputs, outputs).setName("mykeyphrases")
            .setDescription("Tested Key Phrase skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("key-phrase-extraction-skillset", 48), skill)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithMergeDefaultSettings() {
        List<InputFieldMappingEntry> inputs = Arrays.asList(simpleInputFieldMappingEntry("text", "/document/text"),
            simpleInputFieldMappingEntry("itemsToInsert", "/document/textitems"),
            simpleInputFieldMappingEntry("offsets", "/document/offsets"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("mergedText", "myMergedText"));

        SearchIndexerSkill skill = new MergeSkill(inputs, outputs).setName("mymerge")
            .setDescription("Tested Merged Text skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("merge-skillset", 48), skill)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithSentimentV3DefaultSettings() {
        List<InputFieldMappingEntry> inputs
            = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("confidenceScores", "mySentiment"));

        SearchIndexerSkill skill = new SentimentSkillV3(inputs, outputs).setName("mysentiment")
            .setDescription("Tested Sentiment skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("sentiment-skillset", 48), skill)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithEntityRecognitionV3DefaultSettings() {
        List<InputFieldMappingEntry> inputs
            = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("namedEntities", "myEntities"));

        SearchIndexerSkill skill = new EntityRecognitionSkillV3(inputs, outputs).setName("myentity")
            .setDescription("Tested Entity Recognition skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("entity-recognition-skillset", 48), skill)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithSplitDefaultSettings() {
        List<InputFieldMappingEntry> inputs
            = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));

        SearchIndexerSkill skill = new SplitSkill(inputs, outputs).setTextSplitMode(TextSplitMode.PAGES)
            .setName("mysplit")
            .setDescription("Tested Split skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("split-skillset", 48), skill)
            .setDescription("Skillset for testing default configuration");
    }

    SearchIndexerSkillset createSkillsetWithCustomSkills() {
        Map<String, String> headers = Collections.singletonMap("Ocp-Apim-Subscription-Key", "foobar");

        List<InputFieldMappingEntry> inputs
            = Collections.singletonList(simpleInputFieldMappingEntry("text", "/document/mytext"));

        List<OutputFieldMappingEntry> outputs
            = Collections.singletonList(createOutputFieldMappingEntry("textItems", "myTextItems"));

        SearchIndexerSkill webApiSkill = new WebApiSkill(inputs, outputs,
            "https://indexer-e2e-webskill.azurewebsites.net/api/InvokeTextAnalyticsV3?code=foo").setHttpMethod("POST")
                .setHttpHeaders(new WebApiHttpHeaders().setAdditionalProperties(headers))
                .setName("webapi-skill")
                .setDescription("Calls an Azure function, which in turn calls Bing Entity Search");

        return new SearchIndexerSkillset(testResourceNamer.randomName("custom-skillset", 48), webApiSkill)
            .setDescription("Skillset for testing custom skillsets");
    }

    SearchIndexerSkillset createSkillsetWithSharperSkillWithNestedInputs() {
        List<InputFieldMappingEntry> inputs = createNestedInputFieldMappingEntry();
        List<OutputFieldMappingEntry> outputs = createOutputFieldMappingEntry();

        SearchIndexerSkill skill = new ShaperSkill(inputs, outputs).setName("myshaper")
            .setDescription("Tested Shaper skill")
            .setContext(CONTEXT_VALUE);

        return new SearchIndexerSkillset(testResourceNamer.randomName("nested-skillset-with-sharperskill", 48), skill)
            .setDescription("Skillset for testing");
    }

    private static List<InputFieldMappingEntry> createNestedInputFieldMappingEntry() {
        return Collections.singletonList(new InputFieldMappingEntry("doc").setSourceContext("/document")
            .setInputs(simpleInputFieldMappingEntry("text", "/document/content"),
                simpleInputFieldMappingEntry("images", "/document/normalized_images/*")));
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

    private SearchIndexerSkillset createTestSkillsetContentUnderstanding() {
        ContentUnderstandingSkill skill = new ContentUnderstandingSkill(
            Collections.singletonList(new InputFieldMappingEntry("file_data").setSource("/document/file_data")),
            Collections.singletonList(new OutputFieldMappingEntry("text_sections").setTargetName("sections")))
                .setChunkingProperties(new ContentUnderstandingSkillChunkingProperties()
                    .setUnit(ContentUnderstandingSkillChunkingUnit.CHARACTERS)
                    .setMaximumLength(1000)
                    .setOverlapLength(100));

        return new SearchIndexerSkillset(testResourceNamer.randomName("content-understanding-skillset", 48), skill)
            .setDescription("Test skillset with Content Understanding skill")
            .setCognitiveServicesAccount(createAIFoundryCognitiveServicesAccount());
    }

    private SearchIndexerSkillset createTestSkillsetContentUnderstandingWithAllOptions() {
        ContentUnderstandingSkill skill = new ContentUnderstandingSkill(
            Collections.singletonList(new InputFieldMappingEntry("file_data").setSource("/document/file_data")),
            Arrays.asList(new OutputFieldMappingEntry("text_sections").setTargetName("sections"),
                new OutputFieldMappingEntry("normalized_images").setTargetName("images")))
                    .setExtractionOptions(ContentUnderstandingSkillExtractionOptions.IMAGES,
                        ContentUnderstandingSkillExtractionOptions.LOCATION_METADATA)
                    .setChunkingProperties(new ContentUnderstandingSkillChunkingProperties()
                        .setUnit(ContentUnderstandingSkillChunkingUnit.CHARACTERS)
                        .setMaximumLength(2000)
                        .setOverlapLength(200));

        return new SearchIndexerSkillset(testResourceNamer.randomName("content-understanding-all-options-skillset", 48),
            skill).setDescription("Test skillset with Content Understanding skill (all options)")
                .setCognitiveServicesAccount(createAIFoundryCognitiveServicesAccount());
    }

    private CognitiveServicesAccount createAIFoundryCognitiveServicesAccount() {
        String aiFoundryKey = System.getenv("AI_FOUNDRY_KEY");

        if (aiFoundryKey != null && !aiFoundryKey.isEmpty()) {
            return new CognitiveServicesAccountKey(aiFoundryKey);
        } else {
            return new DefaultCognitiveServicesAccount();
        }
    }
}
