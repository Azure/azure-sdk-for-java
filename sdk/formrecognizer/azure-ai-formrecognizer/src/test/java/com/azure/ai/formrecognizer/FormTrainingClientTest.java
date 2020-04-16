// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

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

public class FormTrainingClientTest extends FormTrainingClientTestBase {

    private FormTrainingClient client;

    @Override
    protected void beforeTest() {
        FormRecognizerClient formRecognizerClient = clientSetup(httpPipeline ->
            new FormRecognizerClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(httpPipeline)
                .buildClient());
        client = formRecognizerClient.getFormTrainingClient();
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @Test
    void getCustomModelInvalidStatusModel() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            getCustomModelInvalidStatusModelRunner(invalidId -> client.getCustomModel(invalidId)));
        assertEquals(exception.getMessage(), INVALID_STATUS_MODEL_ERROR);
    }

    /**
     * Verifies that an exception is thrown for null model Id parameter.
     */
    @Test
    void getCustomModelNullModelId() {
        assertThrows(NullPointerException.class, () -> client.getCustomModel(null));
    }

    /**
     * Verifies custom model info returned for a valid model Id.
     */
    @Test
    void getCustomModelValidModelId() {
        getCustomModelValidModelIdRunner(validModelId ->
            validateCustomModel(getExpectedUnlabeledModel(), client.getCustomModel(validModelId)));
    }

    /**
     * Verifies that an exception is thrown for invalid model Id.
     */
    @Test
    void getCustomModelInvalidModelId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            getCustomModelInvalidModelIdRunner(invalidId -> client.getCustomModel(invalidId)));
        assertTrue(exception.getMessage().equals(INVALID_MODEL_ID_ERROR));
    }

    /**
     * Verifies custom model info returned with response for a valid model Id.
     */
    @Test
    void getCustomModelWithResponse() {
        getCustomModelWithResponseRunner(validModelId ->
            validateCustomModel(getExpectedLabeledModel(),
                client.getCustomModelWithResponse(validModelId, Context.NONE).getValue()));
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @Test
    void validGetAccountProperties() {
        validateAccountProperties(getExpectedAccountProperties(), client.getAccountProperties());
    }

    /**
     * Verifies account properties returned with an Http Response for a subscription account.
     */
    @Test
    void validGetAccountPropertiesWithResponse() {
        Response<AccountProperties> accountPropertiesResponse = client.getAccountPropertiesWithResponse(Context.NONE);
        assertEquals(accountPropertiesResponse.getStatusCode(), HttpResponseStatus.OK.code());
        validateAccountProperties(getExpectedAccountProperties(), accountPropertiesResponse.getValue());
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @Test
    void deleteModelInvalidModelId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.deleteModel(INVALID_MODEL_ID));
        assertTrue(exception.getMessage().equals(INVALID_MODEL_ID_ERROR));
    }

    @Test
    void deleteModelValidModelIdWithResponse() {
        beginTrainingLabeledResultRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(storageSASUrl, useLabelFile);
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();

            Response<Void> deleteModelWithResponse = client.deleteModelWithResponse(createdModel.getModelId(),
                Context.NONE);
            assertEquals(deleteModelWithResponse.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());

            ErrorResponseException exception = assertThrows(ErrorResponseException.class, () ->
                client.getCustomModelWithResponse(createdModel.getModelId(), Context.NONE));
            assertEquals(exception.getResponse().getStatusCode(), HttpResponseStatus.NOT_FOUND.code());
        });
    }

    /**
     * Test for listing all models information.
     */
    @Test
    void listModels() {
        for (CustomFormModelInfo modelInfo : client.listModels()) {
            assertTrue(modelInfo.getModelId() != null && modelInfo.getCreatedOn() != null
                && modelInfo.getLastUpdatedOn() != null && modelInfo.getStatus() != null);
        }
    }

    /**
     * Test for listing all models information with {@link Context}.
     */
    @Test
    void listModelsWithContext() {
        for (CustomFormModelInfo modelInfo : client.listModels(Context.NONE)) {
            assertTrue(modelInfo.getModelId() != null && modelInfo.getCreatedOn() != null
                && modelInfo.getLastUpdatedOn() != null && modelInfo.getStatus() != null);
        }
    }

    /**
     * Verifies that an exception is thrown for null source url input.
     */
    @Test
    void beginTrainingNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.beginTraining(null, false));
        assertTrue(exception.getMessage().equals(NULL_SOURCE_URL_ERROR));
    }

    /**
     * Verifies the result of the training operation for a valid labeled model Id and training set Url.
     */
    @Test
    void beginTrainingLabeledResult() {
        beginTrainingLabeledResultRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(storageSASUrl, useLabelFile);
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
                client.beginTraining(storageSASUrl, useLabelFile);
            syncPoller.waitForCompletion();
            validateCustomModel(getExpectedUnlabeledModel(), syncPoller.getFinalResult());
        });
    }
}
