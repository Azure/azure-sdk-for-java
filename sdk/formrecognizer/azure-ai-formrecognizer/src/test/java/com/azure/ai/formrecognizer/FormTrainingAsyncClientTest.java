// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.util.polling.SyncPoller;
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
import static com.azure.ai.formrecognizer.TestUtils.getExpectedSupervisedModel;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedUnsupervisedModel;
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

    @Test
    void getCustomModelInvalidStatusModel() {
        getCustomModelInvalidStatusModelRunner(invalidId -> StepVerifier.create(client.getCustomModel(invalidId))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INVALID_STATUS_MODEL_ERROR)).verify());
    }

    @Test
    void getCustomModelNullModelId() {
        StepVerifier.create(client.getCustomModel(null)).verifyError();
    }

    @Test
    void getCustomModelValidModelId() {
        getCustomModelValidModelIdRunner(validModelId -> StepVerifier.create(client.getCustomModel(validModelId))
            .assertNext(customFormModel ->
                validateCustomModel(getExpectedUnsupervisedModel(), customFormModel)));
    }

    @Test
    void getCustomModelInvalidModelId() {
        getCustomModelInvalidModelIdRunner(invalidModelId -> StepVerifier.create(client.getCustomModel(invalidModelId))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INVALID_MODEL_ID_ERROR)).verify());
    }

    @Test
    void getCustomModelWithResponse() {
        getCustomModelWithResponseRunner(validModelId ->
            StepVerifier.create(client.getCustomFormModelWithResponse(validModelId))
                .assertNext(customFormModel ->
                    validateCustomModel(getExpectedSupervisedModel(), customFormModel.getValue()))
                .verifyComplete());
    }

    @Test
    void validGetAccountProperties() {
        StepVerifier.create(client.getAccountProperties())
            .assertNext(accountProperties -> validateAccountProperties(getExpectedAccountProperties(), accountProperties))
            .verifyComplete();
    }

    @Test
    void validGetAccountPropertiesWithResponse() {
        StepVerifier.create(client.getAccountProperties())
            .assertNext(accountProperties ->
                validateAccountProperties(getExpectedAccountProperties(), accountProperties))
            .verifyComplete();
    }

    @Test
    void deleteModelInvalidModelId() {
        StepVerifier.create(client.deleteModel(INVALID_MODEL_ID))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INVALID_MODEL_ID_ERROR))
            .verify();
    }

    @Test
    void deleteModelValidModelIdWithResponse() {
        // TODO: after List models API is merged.
        // list models select first and delete model Id check success response.
    }

    @Test
    void beginTrainingNullInput() {
        NullPointerException thrown = assertThrows(
            NullPointerException.class,
            () -> client.beginTraining(null, false).getSyncPoller().getFinalResult());

        assertTrue(thrown.getMessage().equals(NULL_SOURCE_URL_ERROR));
    }

    @Test
    void beginTrainingSupervisedResult() {
        beginTrainingSupervisedResultRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(storageSASUrl, useLabelFile).getSyncPoller();
            syncPoller.waitForCompletion();
            validateCustomModel(getExpectedSupervisedModel(), syncPoller.getFinalResult());
        });
    }

    @Test
    void beginTrainingUnsupervisedResult() {
        beginTrainingUnsupervisedResultRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(storageSASUrl, useLabelFile).getSyncPoller();
            syncPoller.waitForCompletion();
            validateCustomModel(getExpectedUnsupervisedModel(), syncPoller.getFinalResult());
        });
    }
}
