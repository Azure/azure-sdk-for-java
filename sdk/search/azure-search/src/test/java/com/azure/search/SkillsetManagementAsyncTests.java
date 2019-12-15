// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.DefaultCognitiveServicesAccount;
import com.azure.search.models.EntityCategory;
import com.azure.search.models.InputFieldMappingEntry;
import com.azure.search.models.KeyPhraseExtractionSkill;
import com.azure.search.models.KeyPhraseExtractionSkillLanguage;
import com.azure.search.models.OcrSkillLanguage;
import com.azure.search.models.OutputFieldMappingEntry;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SentimentSkillLanguage;
import com.azure.search.models.Skillset;
import com.azure.search.models.SplitSkillLanguage;
import com.azure.search.models.TextExtractionAlgorithm;
import com.azure.search.models.TextSplitMode;
import com.azure.search.test.AccessConditionAsyncTests;
import com.azure.search.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class SkillsetManagementAsyncTests extends SkillsetManagementTestBase {
    private SearchServiceAsyncClient client;

    // commonly used lambda definitions
    private BiFunction<Skillset,
        AccessOptions,
        Mono<Skillset>> createOrUpdateSkillsetAsyncFunc =
            (Skillset skillset, AccessOptions ac) ->
                createSkillset(skillset, ac.getAccessCondition(), ac.getRequestOptions());

    private Supplier<Skillset> newSkillsetFunc =
        () -> createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, false);

    private Function<Skillset, Skillset> mutateSkillsetFunc = this::mutateSkillsInSkillset;

    private BiFunction<String, AccessOptions, Mono<Void>> deleteSkillsetAsyncFunc =
        (String name, AccessOptions ac) ->
            deleteSkillset(name, ac.getAccessCondition(), ac.getRequestOptions());

    private Mono<Void> deleteSkillset(String skillsetName,
        AccessCondition accessCondition,
        RequestOptions requestOptions) {
        return client.deleteSkillsetWithResponse(skillsetName, accessCondition, requestOptions).flatMap(FluxUtil::toMono);
    }

    private Mono<Skillset> createSkillset(Skillset skillset,
        AccessCondition accessCondition,
        RequestOptions requestOptions) {
        return client.createOrUpdateSkillsetWithResponse(skillset, accessCondition, requestOptions)
            .map(Response::getValue);
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildAsyncClient();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhrase() {
        Skillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();

        StepVerifier
            .create(client.createSkillset(expectedSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhraseWithResponse() {
        Skillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();

        StepVerifier
            .create(client.createSkillsetWithResponse(expectedSkillset, generateRequestOptions()))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillset, actualSkillset.getValue()))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionLanguageDetection() {
        Skillset expectedSkillset = createTestSkillsetLanguageDetection();

        StepVerifier
            .create(client.createSkillset(expectedSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionMergeText() {
        Skillset expectedSkillset = createTestSkillsetMergeText();

        StepVerifier
            .create(client.createSkillset(expectedSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrEntity() {
        Skillset expectedSkillset = createTestSkillsetOcrEntity(null, null);
        StepVerifier
            .create(client.createSkillset(expectedSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillset, actualSkillset))
            .verifyComplete();

        List<EntityCategory> entityCategories = Arrays.asList(
            EntityCategory.LOCATION, EntityCategory.ORGANIZATION, EntityCategory.PERSON);

        Skillset expectedSkillsetWithPrintedExtraction = createTestSkillsetOcrEntity(TextExtractionAlgorithm.PRINTED,
            entityCategories).setName("testskillset1");
        StepVerifier
            .create(client.createSkillset(expectedSkillsetWithPrintedExtraction))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillsetWithPrintedExtraction, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentiment() {
        Skillset expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.PT,
            SentimentSkillLanguage.PT_PT, TextExtractionAlgorithm.PRINTED);
        StepVerifier
            .create(client.createSkillset(expectedSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillset, actualSkillset))
            .verifyComplete();

        Skillset expectedSkillsetWithPrintedExtraction = createTestSkillsetOcrSentiment(OcrSkillLanguage.FI,
            SentimentSkillLanguage.FI, TextExtractionAlgorithm.PRINTED).setName("testskillset1");
        StepVerifier
            .create(client.createSkillset(expectedSkillsetWithPrintedExtraction))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillsetWithPrintedExtraction, actualSkillset))
            .verifyComplete();

        Skillset expectedSkillsetWithHandWrittenExtraction = createTestSkillsetOcrSentiment(OcrSkillLanguage.EN,
            SentimentSkillLanguage.EN, TextExtractionAlgorithm.HANDWRITTEN).setName("testskillset2");
        StepVerifier
            .create(client.createSkillset(expectedSkillsetWithHandWrittenExtraction))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillsetWithHandWrittenExtraction, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrKeyPhrase() {
        Skillset expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.EN,
            KeyPhraseExtractionSkillLanguage.EN);
        StepVerifier
            .create(client.createSkillset(expectedSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillset, actualSkillset))
            .verifyComplete();

        Skillset expectedSkillsetWithFRLanguage = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.FR,
            KeyPhraseExtractionSkillLanguage.FR).setName("testskillset1");
        StepVerifier
            .create(client.createSkillset(expectedSkillsetWithFRLanguage))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillsetWithFRLanguage, actualSkillset))
            .verifyComplete();

        Skillset expectedSkillsetWithESLanguage = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.ES,
            KeyPhraseExtractionSkillLanguage.ES).setName("testskillset2");
        StepVerifier
            .create(client.createSkillset(expectedSkillsetWithESLanguage))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillsetWithESLanguage, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrShaper() {
        Skillset expectedSkillset = createTestSkillsetOcrShaper();

        StepVerifier
            .create(client.createSkillset(expectedSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionOcrSplitText() {
        Skillset expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.EN,
            SplitSkillLanguage.EN, TextSplitMode.PAGES);
        StepVerifier
            .create(client.createSkillset(expectedSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillset, actualSkillset))
            .verifyComplete();

        Skillset expectedSkillsetWithFRLanguage = createTestSkillsetOcrSplitText(OcrSkillLanguage.FR,
            SplitSkillLanguage.FR, TextSplitMode.PAGES).setName("testskillset1");
        StepVerifier
            .create(client.createSkillset(expectedSkillsetWithFRLanguage))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillsetWithFRLanguage, actualSkillset))
            .verifyComplete();

        Skillset expectedSkillsetWithFILanguage = createTestSkillsetOcrSplitText(OcrSkillLanguage.FI,
            SplitSkillLanguage.FI, TextSplitMode.SENTENCES).setName("testskillset2");
        StepVerifier
            .create(client.createSkillset(expectedSkillsetWithFILanguage))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillsetWithFILanguage, actualSkillset))
            .verifyComplete();
        client.deleteSkillset(expectedSkillsetWithFILanguage.getName()).block();

        Skillset expectedSkillsetWithDALanguage = createTestSkillsetOcrSplitText(OcrSkillLanguage.DA,
            SplitSkillLanguage.DA, TextSplitMode.SENTENCES).setName("testskillset3");
        StepVerifier
            .create(client.createSkillset(expectedSkillsetWithDALanguage))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillsetWithDALanguage, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithCognitiveServicesDefault() {
        Skillset expectedSkillset = createSkillsetWithCognitiveServicesKey();

        StepVerifier
            .create(client.createSkillset(expectedSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithOcrDefaultSettings() {
        Skillset expectedOcrSkillset = createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, false);

        StepVerifier
            .create(client.createSkillset(expectedOcrSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedOcrSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, false);
        client.createSkillset(expected).block();

        StepVerifier
            .create(client.getSkillset(expected.getName()))
            .assertNext(actual -> assertSkillsetsEqual(expected, actual))
            .verifyComplete();
    }

    @Test
    public void getOcrSkillsetReturnsCorrectDefinitionWithResponse() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, false);
        client.createSkillset(expected).block();

        StepVerifier
            .create(client.getSkillsetWithResponse(expected.getName(), generateRequestOptions()))
            .assertNext(actual -> assertSkillsetsEqual(expected, actual.getValue()))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithImageAnalysisDefaultSettings() {
        Skillset expectedImageAnalysisSkillset = createSkillsetWithImageAnalysisDefaultSettings();

        StepVerifier
            .create(client.createSkillset(expectedImageAnalysisSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedImageAnalysisSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithKeyPhraseExtractionDefaultSettings() {
        Skillset expectedKeyPhraseSkillset = createSkillsetWithKeyPhraseExtractionDefaultSettings();

        StepVerifier
            .create(client.createSkillset(expectedKeyPhraseSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedKeyPhraseSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithMergeDefaultSettings() {
        Skillset expectedMergeSkillset = createSkillsetWithMergeDefaultSettings();

        StepVerifier
            .create(client.createSkillset(expectedMergeSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedMergeSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionDefaultSettings() {
        Skillset expectedEntityRecognitionSkillset = createSkillsetWithEntityRecognitionDefaultSettings();

        StepVerifier
            .create(client.createSkillset(expectedEntityRecognitionSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedEntityRecognitionSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSentimentDefaultSettings() {
        Skillset expectedSentimentSkillset = createSkillsetWithSentimentDefaultSettings();

        StepVerifier
            .create(client.createSkillset(expectedSentimentSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSentimentSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void getOcrSkillsetWithShouldDetectOrientationReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, true);
        client.createSkillset(expected).block();

        StepVerifier
            .create(client.getSkillset(expected.getName()))
            .assertNext(actual -> assertSkillsetsEqual(expected, actual))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionWithSplitDefaultSettings() {
        Skillset expectedSplitSkillset = createSkillsetWithSplitDefaultSettings();

        StepVerifier
            .create(client.createSkillset(expectedSplitSkillset))
            .assertNext(actualSkillset -> assertSkillsetsEqual(expectedSplitSkillset, actualSkillset))
            .verifyComplete();
    }

    @Test
    public void createCustomSkillsetReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithCustomSkills();

        StepVerifier
            .create(client.createSkillset(expected))
            .assertNext(actual -> assertSkillsetsEqual(expected, actual))
            .verifyComplete();
    }

    @Test
    public void getSkillsetThrowsOnNotFound() {
        String skillsetName = "thisdoesnotexist";
        assertHttpResponseExceptionAsync(
            client.getSkillset(skillsetName),
            HttpResponseStatus.NOT_FOUND,
            "No skillset with the name 'thisdoesnotexist' was found in service"
        );
    }

    @Test
    public void canCreateAndListSkillsets() {
        Skillset skillset1 = createSkillsetWithCognitiveServicesKey();
        Skillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        client.createSkillset(skillset1).block();
        client.createSkillset(skillset2).block();

        PagedFlux<Skillset> listResponse = client.listSkillsets();

        StepVerifier
            .create(listResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(skillset1.getName(), result.get(0).getName());
                Assert.assertEquals(skillset2.getName(), result.get(1).getName());
            })
            .verifyComplete();
    }

    @Test
    public void canListSkillsetsWithSelectedField() {
        Skillset skillset1 = createSkillsetWithCognitiveServicesKey();
        Skillset skillset2 = createSkillsetWithEntityRecognitionDefaultSettings();

        client.createSkillset(skillset1).block();
        client.createSkillset(skillset2).block();

        PagedFlux<Skillset> listResponse = client.listSkillsets("name", generateRequestOptions());

        StepVerifier
            .create(listResponse.collectList())
            .assertNext(result ->
                result.forEach(res -> {
                    Assert.assertNotNull(res.getName());
                    Assert.assertNull(res.getCognitiveServicesAccount());
                    Assert.assertNull(res.getDescription());
                    Assert.assertNull(res.getSkills());
                    Assert.assertNull(res.getETag());
                })
            )
            .verifyComplete();

        StepVerifier
            .create(listResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(skillset1.getName(), result.get(0).getName());
                Assert.assertEquals(skillset2.getName(), result.get(1).getName());
            })
            .verifyComplete();
    }

    @Test
    public void deleteSkillsetIsIdempotent() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, false);

        StepVerifier
            .create(client.deleteSkillsetWithResponse(skillset.getName(), new AccessCondition(), generateRequestOptions()))
            .assertNext(deleteResponse ->
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode())
            )
            .verifyComplete();

        client.createSkillset(skillset).block();

        StepVerifier
            .create(client.deleteSkillsetWithResponse(skillset.getName(), new AccessCondition(), generateRequestOptions()))
            .assertNext(deleteResponse ->
                Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), deleteResponse.getStatusCode())
            )
            .verifyComplete();

        StepVerifier
            .create(client.deleteSkillsetWithResponse(skillset.getName(), new AccessCondition(), generateRequestOptions()))
            .assertNext(deleteResponse ->
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode())
            )
            .verifyComplete();
    }

    @Test
    public void canCreateAndDeleteSkillset() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, false);
        client.createSkillset(expected).block();
        client.deleteSkillset(expected.getName()).block();

        StepVerifier
            .create(client.skillsetExists(expected.getName()))
            .assertNext(Assert::assertFalse)
            .verifyComplete();
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExist() {
        Skillset expected = createTestOcrSkillSet(1, TextExtractionAlgorithm.PRINTED, false);

        StepVerifier
            .create(client.createOrUpdateSkillset(expected))
            .assertNext(res -> assertSkillsetsEqual(expected, res))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateCreatesWhenSkillsetDoesNotExistWithResponse() {
        Skillset expected = createTestOcrSkillSet(1, TextExtractionAlgorithm.PRINTED, false);

        StepVerifier
            .create(client.createOrUpdateSkillsetWithResponse(expected, new AccessCondition(),
                generateRequestOptions()))
            .assertNext(res -> Assert.assertEquals(HttpResponseStatus.CREATED.code(), res.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateUpdatesWhenSkillsetExists() {
        Skillset skillset = createTestOcrSkillSet(1, TextExtractionAlgorithm.HANDWRITTEN, false);
        StepVerifier
            .create(client.createOrUpdateSkillsetWithResponse(skillset, new AccessCondition(),
                generateRequestOptions()))
            .assertNext(res -> Assert.assertEquals(HttpResponseStatus.CREATED.code(), res.getStatusCode()))
            .verifyComplete();

        skillset = createTestOcrSkillSet(2, TextExtractionAlgorithm.PRINTED, false);
        StepVerifier
            .create(client.createOrUpdateSkillsetWithResponse(skillset, new AccessCondition(),
                generateRequestOptions()))
            .assertNext(res -> Assert.assertEquals(HttpResponseStatus.OK.code(), res.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void existsReturnsFalseForNonExistingSkillset() {
        StepVerifier
            .create(client.skillsetExists("nonexistent"))
            .assertNext(Assert::assertFalse)
            .verifyComplete();
    }

    @Test
    public void existsReturnsTrueForExistingSkillset() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, false);
        client.createSkillset(skillset).block();

        StepVerifier
            .create(client.skillsetExists(skillset.getName()))
            .assertNext(Assert::assertTrue)
            .verifyComplete();
    }

    @Test
    public void existsReturnsTrueForExistingSkillsetWithResponse() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, false);
        client.createSkillset(skillset).block();

        StepVerifier
            .create(client.skillsetExistsWithResponse(skillset.getName(), generateRequestOptions()))
            .assertNext(res -> Assert.assertTrue(res.getValue()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateUpdatesSkills() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, false);

        Skillset createdSkillset = client.createSkillset(skillset).block();
        assert createdSkillset != null;

        // update skills
        createdSkillset.setSkills(Collections.singletonList(
            new KeyPhraseExtractionSkill()
                .setDefaultLanguageCode(KeyPhraseExtractionSkillLanguage.EN)
                .setName("mykeyphrases")
                .setDescription("Tested Key Phrase skill")
                .setContext(CONTEXT_VALUE)
                .setInputs(Collections.singletonList(
                    new InputFieldMappingEntry()
                        .setName("text")
                        .setSource("/document/mytext")))
                .setOutputs(Collections.singletonList(
                    new OutputFieldMappingEntry()
                        .setName("keyPhrases")
                        .setTargetName("myKeyPhrases")))));

        StepVerifier
            .create(client.createOrUpdateSkillset(createdSkillset))
            .assertNext(res -> assertSkillsetsEqual(createdSkillset, res))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateUpdatesCognitiveService() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(OCR_SKILLSET_NAME, false);

        Skillset createdSkillset = client.createSkillset(skillset).block();
        assert createdSkillset != null;

        // update Cognitive Service
        createdSkillset.setCognitiveServicesAccount(new DefaultCognitiveServicesAccount().setDescription("description"));

        StepVerifier
            .create(client.createOrUpdateSkillset(createdSkillset))
            .assertNext(res -> assertSkillsetsEqual(createdSkillset, res))
            .verifyComplete();
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionShaperWithNestedInputs() {
        Skillset expected = createSkillsetWithSharperSkillWithNestedInputs();

        StepVerifier
            .create(client.createSkillset(expected))
            .assertNext(actual -> assertSkillsetsEqual(expected, actual))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSkillsetIfNotExistsFailsOnExistingResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsFailsOnExistingResourceAsync(
            createOrUpdateSkillsetAsyncFunc,
            newSkillsetFunc,
            mutateSkillsetFunc);
    }

    @Test
    public void createOrUpdateSkillsetIfNotExistsSucceedsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResourceAsync(
            createOrUpdateSkillsetAsyncFunc,
            newSkillsetFunc);
    }

    @Test
    public void createOrUpdateSkillsetIfExistsSucceedsOnExistingResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfExistsSucceedsOnExistingResourceAsync(
            newSkillsetFunc,
            createOrUpdateSkillsetAsyncFunc,
            mutateSkillsetFunc);
    }

    @Test
    public void createSkillsetThrowsExceptionWithNonShaperSkillWithNestedInputs() {
        Skillset skillset = createSkillsetWithNonSharperSkillWithNestedInputs();

        assertHttpResponseExceptionAsync(
            client.createSkillset(skillset),
            HttpResponseStatus.BAD_REQUEST,
            "Skill '#1' is not allowed to have recursively defined inputs"
        );
    }

    @Test
    public void createSkillsetReturnsCorrectDefinitionConditional() {
        Skillset expected = createTestSkillsetConditional();

        StepVerifier
            .create(client.createSkillset(expected))
            .assertNext(actual -> assertSkillsetsEqual(expected, actual))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSkillsetIfExistsFailsOnNoResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfExistsFailsOnNoResourceAsync(
            newSkillsetFunc,
            createOrUpdateSkillsetAsyncFunc);
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedSucceedsWhenResourceUnchanged() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfNotChangedSucceedsWhenResourceUnchangedAsync(
            newSkillsetFunc,
            createOrUpdateSkillsetAsyncFunc,
            mutateSkillsetFunc);
    }

    @Test
    public void createOrUpdateSkillsetIfNotChangedFailsWhenResourceChanged() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();
        act.updateIfNotChangedFailsWhenResourceChangedAsync(
            newSkillsetFunc,
            createOrUpdateSkillsetAsyncFunc,
            mutateSkillsetFunc);
    }

    @Test
    public void deleteSkillsetIfNotChangedWorksOnlyOnCurrentResource() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.deleteIfNotChangedWorksOnlyOnCurrentResourceAsync(
            deleteSkillsetAsyncFunc,
            newSkillsetFunc,
            createOrUpdateSkillsetAsyncFunc,
            mutateSkillsetFunc,
            OCR_SKILLSET_NAME);
    }

    @Test
    public void deleteSkillsetIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionAsyncTests act = new AccessConditionAsyncTests();

        act.deleteIfExistsWorksOnlyWhenResourceExistsAsync(
            deleteSkillsetAsyncFunc,
            createOrUpdateSkillsetAsyncFunc,
            newSkillsetFunc,
            OCR_SKILLSET_NAME);
    }
}
