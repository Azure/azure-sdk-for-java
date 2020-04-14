// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_STATUS_MODEL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.NULL_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedAccountProperties;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedLabeledModel;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedUnlabeledModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormTrainingAsyncClientTest extends FormTrainingClientTestBase {

    private FormTrainingAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @Override
    protected void beforeTest() {
        FormRecognizerAsyncClient formRecognizerAsyncClient = clientSetup(httpPipeline ->
            new FormRecognizerClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(httpPipeline)
                .buildAsyncClient());
        client = formRecognizerAsyncClient.getFormTrainingAsyncClient();
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @Test
    void getCustomModelInvalidStatusModel() {
        getCustomModelInvalidStatusModelRunner(invalidId -> StepVerifier.create(client.getCustomModel(invalidId))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INVALID_STATUS_MODEL_ERROR)).verify());
    }

    /**
     * Verifies that an exception is thrown for null model Id parameter.
     */
    @Test
    void getCustomModelNullModelId() {
        StepVerifier.create(client.getCustomModel(null)).verifyError();
    }

    /**
     * Verifies custom model info returned for a valid model Id.
     */
    @Test
    void getCustomModelValidModelId() {
        getCustomModelValidModelIdRunner(validModelId -> StepVerifier.create(client.getCustomModel(validModelId))
            .assertNext(customFormModel ->
                validateCustomModel(getExpectedUnlabeledModel(), customFormModel)));
    }

    /**
     * Verifies that an exception is thrown for invalid model Id.
     */
    @Test
    void getCustomModelInvalidModelId() {
        getCustomModelInvalidModelIdRunner(invalidModelId -> StepVerifier.create(client.getCustomModel(invalidModelId))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INVALID_MODEL_ID_ERROR)).verify());
    }

    /**
     * Verifies custom model info returned with response for a valid model Id.
     */
    @Test
    void getCustomModelWithResponse() {
        getCustomModelWithResponseRunner(validModelId ->
            StepVerifier.create(client.getCustomModelWithResponse(validModelId))
                .assertNext(customFormModel ->
                    validateCustomModel(getExpectedLabeledModel(), customFormModel.getValue()))
                .verifyComplete());
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @Test
    void validGetAccountProperties() {
        StepVerifier.create(client.getAccountProperties())
            .assertNext(accountProperties -> validateAccountProperties(getExpectedAccountProperties(),
                accountProperties))
            .verifyComplete();
    }

    /**
     * Verifies account properties returned with an Http Response for a subscription account.
     */
    @Test
    void validGetAccountPropertiesWithResponse() {
        StepVerifier.create(client.getAccountProperties())
            .assertNext(accountProperties ->
                validateAccountProperties(getExpectedAccountProperties(), accountProperties))
            .verifyComplete();
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @Test
    void deleteModelInvalidModelId() {
        StepVerifier.create(client.deleteModel(INVALID_MODEL_ID))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INVALID_MODEL_ID_ERROR))
            .verify();
    }

    @Test
    void deleteModelValidModelIdWithResponse() {
        beginTrainingLabeledResultRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(storageSASUrl, useLabelFile).getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();

            StepVerifier.create(client.deleteModelWithResponse(createdModel.getModelId()))
                .assertNext(response ->
                    assertEquals(response.getStatusCode(), HttpResponseStatus.NO_CONTENT.code()))
                .verifyComplete();

            StepVerifier.create(client.getCustomModelWithResponse(createdModel.getModelId()))
                .verifyErrorSatisfies(throwable ->
                    throwable.getMessage().contains(HttpResponseStatus.NOT_FOUND.toString()));
        });
    }

    /**
     * Test for listing all models information.
     */
    @Test
    void listModels() {
        StepVerifier.create(client.listModels())
            .thenConsumeWhile(customFormModelInfo ->
                customFormModelInfo.getModelId() != null && customFormModelInfo.getCreatedOn() != null
                    && customFormModelInfo.getLastUpdatedOn() != null && customFormModelInfo.getStatus() != null)
            .verifyComplete();
    }

    /**
     * Test for listing all models information with {@link Context}.
     */
    @Test
    void listModelsWithContext() {
        StepVerifier.create(client.listModels(Context.NONE))
            .thenConsumeWhile(modelInfo ->
                modelInfo.getModelId() != null && modelInfo.getCreatedOn() != null
                    && modelInfo.getLastUpdatedOn() != null && modelInfo.getStatus() != null)
            .verifyComplete();
    }

    /**
     * Verifies that an exception is thrown for null source url input.
     */
    @Test
    void beginTrainingNullInput() {
        NullPointerException thrown = assertThrows(
            NullPointerException.class,
            () -> client.beginTraining(null, false).getSyncPoller().getFinalResult());

        assertTrue(thrown.getMessage().equals(NULL_SOURCE_URL_ERROR));
    }

    /**
     * Verifies the result of the training operation for a valid labeled model Id and training set Url.
     */
    @Test
    void beginTrainingLabeledResult() {
        beginTrainingLabeledResultRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(storageSASUrl, useLabelFile).getSyncPoller();
            syncPoller.waitForCompletion();
            validateCustomModel(getExpectedLabeledModel(), syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model Id and training set Url.
     */
    @Test
    void beginTrainingUnlabeledResult() {
        beginTrainingUnlabeledResultRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(storageSASUrl, useLabelFile).getSyncPoller();
            syncPoller.waitForCompletion();
            validateCustomModel(getExpectedUnlabeledModel(), syncPoller.getFinalResult());
        });
    }
}
