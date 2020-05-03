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

import java.util.concurrent.CountDownLatch;

import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.LABELED_MODEL_DATA;
import static com.azure.ai.formrecognizer.TestUtils.NULL_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.UNLABELED_MODEL_DATA;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedAccountProperties;
import static com.azure.ai.formrecognizer.TestUtils.getModelRawResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormTrainingClientTest extends FormTrainingClientTestBase {

    private FormTrainingClient client;
    private CountDownLatch countDownLatch;

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
     * Verifies that an exception is thrown for null model Id parameter.
     */
    @Test
    void getCustomModelNullModelId() {
        assertThrows(NullPointerException.class, () -> client.getCustomModel(null));
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
        beginTrainingUnlabeledResultRunner((unLabeledContainerSasUrl, useLabelFile) -> {
            CustomFormModel trainedUnlabeledModel = client.beginTraining(unLabeledContainerSasUrl, useLabelFile)
                .getFinalResult();
            Response<CustomFormModel> customModelWithResponse =
                client.getCustomModelWithResponse(trainedUnlabeledModel.getModelId(),
                    Context.NONE);
            assertEquals(customModelWithResponse.getStatusCode(), HttpResponseStatus.OK.code());
            validateCustomModel(trainedUnlabeledModel, customModelWithResponse.getValue());
        });
    }

    /**
     * Verifies unlabeled custom model info returned with response for a valid model Id.
     */
    @Test
    void getCustomModelUnlabeled() {
        beginTrainingUnlabeledResultRunner((unLabeledContainerSasUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller = client.beginTraining(unLabeledContainerSasUrl,
                useLabelFile);
            syncPoller.waitForCompletion();
            CustomFormModel trainedUnlabeledModel = syncPoller.getFinalResult();
            validateCustomModelData(syncPoller.getFinalResult(), getModelRawResponse(UNLABELED_MODEL_DATA), false);
        });
    }

    /**
     * Verifies labeled custom model info returned with response for a valid model Id.
     */
    @Test
    void getCustomModelLabeled() {
        beginTrainingLabeledResultRunner((labeledContainerSasUrl, useLabelFile) -> {
            CustomFormModel customFormModel = client.beginTraining(labeledContainerSasUrl, useLabelFile)
                .getFinalResult();
            validateCustomModel(customFormModel, client.getCustomModel(customFormModel.getModelId()));
        });
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
    void getModelInfos() {
        for (CustomFormModelInfo modelInfo : client.getModelInfos()) {
            assertTrue(modelInfo.getModelId() != null && modelInfo.getCreatedOn() != null
                && modelInfo.getLastUpdatedOn() != null && modelInfo.getStatus() != null);
        }
    }

    /**
     * Test for listing all models information with {@link Context}.
     */
    @Test
    void getModelInfosWithContext() {
        for (CustomFormModelInfo modelInfo : client.getModelInfos(Context.NONE)) {
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
            validateCustomModelData(syncPoller.getFinalResult(), getModelRawResponse(LABELED_MODEL_DATA), true);
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model Id and training set Url.
     */
    // @Test
    // void beginTrainingUnlabeledResult() {
    //     beginTrainingUnlabeledResultRunner((storageSASUrl, useLabelFile) -> {
    //         SyncPoller<OperationResult, CustomFormModel> syncPoller =
    //             client.beginTraining(storageSASUrl, useLabelFile);
    //         syncPoller.waitForCompletion();
    //         validateCustomModelData(syncPoller.getFinalResult(), getModelRawResponse(UNLABELED_MODEL_DATA), false);
    //     });
    // }
}
