// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.search.models.EntityCategory;
import com.azure.search.models.KeyPhraseExtractionSkillLanguage;
import com.azure.search.models.OcrSkillLanguage;
import com.azure.search.models.SentimentSkillLanguage;
import com.azure.search.models.Skillset;
import com.azure.search.models.SplitSkillLanguage;
import com.azure.search.models.TextExtractionAlgorithm;
import com.azure.search.models.TextSplitMode;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;

public class SkillsetManagementSyncTests extends SkillsetManagementTestBase {
    private SearchServiceClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionImageAnalysisKeyPhrase() {
        Skillset expectedSkillset = createTestSkillsetImageAnalysisKeyPhrase();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionLanguageDetection() {
        Skillset expectedSkillset = createTestSkillsetLanguageDetection();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionMergeText() {
        Skillset expectedSkillset = createTestSkillsetMergeText();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionOcrEntity() {
        Skillset expectedSkillset = createTestSkillsetOcrEntity(null, null);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);

        List<EntityCategory> entityCategories = Arrays.asList(
            EntityCategory.LOCATION, EntityCategory.ORGANIZATION, EntityCategory.PERSON);

        expectedSkillset = createTestSkillsetOcrEntity(TextExtractionAlgorithm.PRINTED, entityCategories)
            .setName("testskillset1");
        actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionOcrHandwritingSentiment() {
        Skillset expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.PT,
            SentimentSkillLanguage.PT_PT, TextExtractionAlgorithm.PRINTED);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);

        expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.FI,
            SentimentSkillLanguage.FI, TextExtractionAlgorithm.PRINTED).setName("testskillset1");
        actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);

        expectedSkillset = createTestSkillsetOcrSentiment(OcrSkillLanguage.EN,
            SentimentSkillLanguage.EN, TextExtractionAlgorithm.HANDWRITTEN).setName("testskillset2");
        actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionOcrKeyPhrase() {
        Skillset expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.EN,
            KeyPhraseExtractionSkillLanguage.EN);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);

        expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.FR, KeyPhraseExtractionSkillLanguage.FR)
            .setName("testskillset1");
        actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);

        expectedSkillset = createTestSkillsetOcrKeyPhrase(OcrSkillLanguage.ES, KeyPhraseExtractionSkillLanguage.ES)
            .setName("testskillset2");
        actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionOcrShaper() {
        Skillset expectedSkillset = createTestSkillsetOcrShaper();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionOcrSplitText() {
        Skillset expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.EN,
            SplitSkillLanguage.EN, TextSplitMode.PAGES);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);


        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.FR,
            SplitSkillLanguage.FR, TextSplitMode.PAGES).setName("testskillset1");
        actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);

        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.FI,
            SplitSkillLanguage.FI, TextSplitMode.SENTENCES).setName("testskillset2");
        actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);
        client.deleteSkillset(expectedSkillset.getName());

        expectedSkillset = createTestSkillsetOcrSplitText(OcrSkillLanguage.DA,
            SplitSkillLanguage.DA, TextSplitMode.SENTENCES).setName("testskillset3");
        actualSkillset = client.createSkillset(expectedSkillset);
        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionWithCognitiveServicesDefault() {
        Skillset expectedSkillset = createSkillsetWithCognitiveServicesKey();

        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionWithOcrDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithOcrDefaultSettings(false);
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionWithImageAnalysisDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithImageAnalysisDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionWithKeyPhraseExtractionDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithKeyPhraseExtractionDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionWithMergeDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithMergeDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionWithEntityRecognitionDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithEntityRecognitionDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void getOcrSkillsetReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(false);
        client.createSkillset(expected);

        Skillset actual = client.getSkillset(expected.getName());

        assertSkillsetsEqual(expected, actual);
    }

    @Override
    public void getOcrSkillsetWithShouldDetectOrientationReturnsCorrectDefinition() {
        Skillset expected = createSkillsetWithOcrDefaultSettings(true);

        client.createSkillset(expected);

        Skillset actual = client.getSkillset(expected.getName());

        assertSkillsetsEqual(expected, actual);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionWithSentimentDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithSentimentDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void createSkillsetReturnsCorrectDefinitionWithSplitDefaultSettings() {
        Skillset expectedSkillset = createSkillsetWithSplitDefaultSettings();
        Skillset actualSkillset = client.createSkillset(expectedSkillset);

        assertSkillsetsEqual(expectedSkillset, actualSkillset);
    }

    @Override
    public void getSkillsetThrowsOnNotFound() {
        try {
            String skillsetName = "thisdoesnotexist";
            client.getSynonymMap(skillsetName);

            Assert.fail("Expected an exception to be thrown");
        }
        catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(),
                ((HttpResponseException) ex).getResponse().getStatusCode());
        }
    }

    @Override
    public void deleteSkillsetIsIdempotent() {
        Skillset skillset = createSkillsetWithOcrDefaultSettings(false);

        Response<Void> deleteResponse = client.deleteSkillsetWithResponse(skillset.getName(), null, null);
        Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());

        client.createSkillset(skillset);

        // Delete the same skillset twice
        deleteResponse = client.deleteSkillsetWithResponse(skillset.getName(), null, null);
        Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), deleteResponse.getStatusCode());

        deleteResponse = client.deleteSkillsetWithResponse(skillset.getName(), null, null);
        Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());
    }

}
