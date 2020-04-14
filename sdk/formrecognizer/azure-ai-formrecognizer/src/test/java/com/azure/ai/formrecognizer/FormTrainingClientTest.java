// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
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
import static com.azure.ai.formrecognizer.TestUtils.getExpectedSupervisedModel;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedUnsupervisedModel;
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

    @Test
    void getCustomModelInvalidStatusModel() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            getCustomModelInvalidStatusModelRunner(invalidId -> client.getCustomModel(invalidId)));
        assertEquals(exception.getMessage(), INVALID_STATUS_MODEL_ERROR);
    }

    @Test
    void getCustomModelNullModelId() {
        assertThrows(NullPointerException.class, () -> client.getCustomModel(null));
    }

    @Test
    void getCustomModelValidModelId() {
        getCustomModelValidModelIdRunner(validModelId ->
            validateCustomModel(getExpectedUnsupervisedModel(), client.getCustomModel(validModelId)));
    }

    @Test
    void getCustomModelInvalidModelId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            getCustomModelInvalidModelIdRunner(invalidId -> client.getCustomModel(invalidId)));
        assertTrue(exception.getMessage().equals(INVALID_MODEL_ID_ERROR));
    }

    @Test
    void getCustomModelWithResponse() {
        getCustomModelWithResponseRunner(validModelId ->
            validateCustomModel(getExpectedSupervisedModel(),
                client.getCustomModelWithResponse(validModelId, Context.NONE).getValue()));
    }

    @Test
    void validGetAccountProperties() {
        validateAccountProperties(getExpectedAccountProperties(), client.getAccountProperties());
    }

    @Test
    void validGetAccountPropertiesWithResponse() {
        Response<AccountProperties> accountPropertiesResponse = client.getAccountPropertiesWithResponse(Context.NONE);
        assertEquals(accountPropertiesResponse.getStatusCode(), HttpResponseStatus.OK.code());
        validateAccountProperties(getExpectedAccountProperties(), accountPropertiesResponse.getValue());
    }

    @Test
    void deleteModelInvalidModelId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.deleteModel(INVALID_MODEL_ID));
        assertTrue(exception.getMessage().equals(INVALID_MODEL_ID_ERROR));
    }

    @Test
    void deleteModelValidModelIdWithResponse() {
        // TODO: after List models API is merged.
        // list models select first and delete model Id check success response.s
    }

    @Test
    void beginTrainingNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.beginTraining(null, false));
        assertTrue(exception.getMessage().equals(NULL_SOURCE_URL_ERROR));
    }

    @Test
    void beginTrainingSupervisedResult() {
        beginTrainingSupervisedResultRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(storageSASUrl, useLabelFile);
            syncPoller.waitForCompletion();
            validateCustomModel(getExpectedSupervisedModel(), syncPoller.getFinalResult());
        });
    }

    @Test
    void beginTrainingUnsupervisedResult() {
        beginTrainingUnsupervisedResultRunner((storageSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(storageSASUrl, useLabelFile);
            syncPoller.waitForCompletion();
            validateCustomModel(getExpectedUnsupervisedModel(), syncPoller.getFinalResult());
        });
    }
}
