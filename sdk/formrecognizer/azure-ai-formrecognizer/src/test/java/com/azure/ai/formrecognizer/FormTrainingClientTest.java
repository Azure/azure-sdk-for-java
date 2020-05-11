// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.NULL_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedAccountProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormTrainingClientTest extends FormTrainingClientTestBase {

    private FormTrainingClient client;

    private FormTrainingClient getFormTrainingClient(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());
        AzureKeyCredential credential = (getTestMode() == TestMode.PLAYBACK)
            ? new AzureKeyCredential(INVALID_KEY) : new AzureKeyCredential(getApiKey());
        builder.credential(credential);
        return builder.buildClient().getFormTrainingClient();
    }

    /**
     * Verifies that an exception is thrown for null model Id parameter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getCustomModelNullModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        assertThrows(NullPointerException.class, () -> client.getCustomModel(null));
    }

    /**
     * Verifies that an exception is thrown for invalid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getCustomModelInvalidModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            getCustomModelInvalidModelIdRunner(invalidId -> client.getCustomModel(invalidId)));
        assertTrue(exception.getMessage().equals(INVALID_MODEL_ID_ERROR));
    }

    /**
     * Verifies custom model info returned with response for a valid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getCustomModelWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingDataSasUrl, useLabelFile) -> {
            CustomFormModel trainedUnlabeledModel = client.beginTraining(trainingDataSasUrl, useLabelFile)
                .getFinalResult();
            Response<CustomFormModel> customModelWithResponse =
                client.getCustomModelWithResponse(trainedUnlabeledModel.getModelId(),
                    Context.NONE);
            assertEquals(customModelWithResponse.getStatusCode(), HttpResponseStatus.OK.code());
            validateCustomModelData(customModelWithResponse.getValue(), false);
        });
    }

    /**
     * Verifies unlabeled custom model info returned with response for a valid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getCustomModelUnlabeled(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingDataSASUri, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller = client.beginTraining(trainingDataSASUri,
                useLabelFile);
            syncPoller.waitForCompletion();
            CustomFormModel trainedUnlabeledModel = syncPoller.getFinalResult();
            validateCustomModelData(client.getCustomModel(trainedUnlabeledModel.getModelId()), false);
        });
    }

    /**
     * Verifies labeled custom model info returned with response for a valid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getCustomModelLabeled(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingDataSASUrl, useLabelFile) -> {
            CustomFormModel customFormModel = client.beginTraining(trainingDataSASUrl, useLabelFile)
                .getFinalResult();
            validateCustomModelData(client.getCustomModel(customFormModel.getModelId()), true);
        });
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void validGetAccountProperties(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        validateAccountProperties(getExpectedAccountProperties(), client.getAccountProperties());
    }

    /**
     * Verifies account properties returned with an Http Response for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void validGetAccountPropertiesWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        Response<AccountProperties> accountPropertiesResponse = client.getAccountPropertiesWithResponse(Context.NONE);
        assertEquals(accountPropertiesResponse.getStatusCode(), HttpResponseStatus.OK.code());
        validateAccountProperties(getExpectedAccountProperties(), accountPropertiesResponse.getValue());
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void deleteModelInvalidModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            client.deleteModel(INVALID_MODEL_ID));
        assertTrue(exception.getMessage().equals(INVALID_MODEL_ID_ERROR));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void deleteModelValidModelIdWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingDataSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingDataSASUrl, useLabelFile);
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getModelInfos(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        for (CustomFormModelInfo modelInfo : client.getModelInfos()) {
            assertTrue(modelInfo.getModelId() != null && modelInfo.getCreatedOn() != null
                && modelInfo.getLastUpdatedOn() != null && modelInfo.getStatus() != null);
        }
    }

    /**
     * Test for listing all models information with {@link Context}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getModelInfosWithContext(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        for (CustomFormModelInfo modelInfo : client.getModelInfos(Context.NONE)) {
            assertTrue(modelInfo.getModelId() != null && modelInfo.getCreatedOn() != null
                && modelInfo.getLastUpdatedOn() != null && modelInfo.getStatus() != null);
        }
    }

    /**
     * Verifies that an exception is thrown for null source url input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingNullInput(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.beginTraining(null, false));
        assertTrue(exception.getMessage().equals(NULL_SOURCE_URL_ERROR));
    }

    /**
     * Verifies the result of the training operation for a valid labeled model Id and training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingLabeledResult(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingDataSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingDataSASUrl, useLabelFile);
            syncPoller.waitForCompletion();
            validateCustomModelData(syncPoller.getFinalResult(), true);
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model Id and training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingUnlabeledResult(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingDataSASUrl, useLabelFile) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingDataSASUrl, useLabelFile);
            syncPoller.waitForCompletion();
            validateCustomModelData(syncPoller.getFinalResult(), false);
        });
    }
}
