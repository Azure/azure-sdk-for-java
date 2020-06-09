// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CopyAuthorization;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.CustomFormModelStatus;
import com.azure.ai.formrecognizer.models.ErrorInformation;
import com.azure.ai.formrecognizer.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.formrecognizer.FormTrainingAsyncClientTest.EXPECTED_COPY_REQUEST_INVALID_TARGET_RESOURCE_REGION;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.NULL_SOURCE_URL_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormTrainingClientTest extends FormTrainingClientTestBase {
    private FormTrainingClient client;

    private FormTrainingClient getFormTrainingClient(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        return getFormTrainingClientBuilder(httpClient, serviceVersion).buildClient();
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
        beginTrainingUnlabeledRunner((trainingDataSasUrl, useTrainingLabels) -> {
            CustomFormModel trainedUnlabeledModel = client.beginTraining(trainingDataSasUrl, useTrainingLabels)
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
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels);
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
        beginTrainingLabeledRunner((trainingDataSASUrl, useTrainingLabels) -> {
            CustomFormModel customFormModel = client.beginTraining(trainingDataSASUrl, useTrainingLabels)
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
        validateAccountProperties(client.getAccountProperties());
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
        validateAccountProperties(accountPropertiesResponse.getValue());
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
        beginTrainingLabeledRunner((trainingDataSASUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingDataSASUrl, useTrainingLabels);
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
    public void listCustomModels(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        for (CustomFormModelInfo modelInfo : client.listCustomModels()) {
            assertTrue(modelInfo.getModelId() != null && modelInfo.getRequestedOn() != null
                && modelInfo.getCompletedOn() != null && modelInfo.getStatus() != null);
        }
    }

    /**
     * Test for listing all models information with {@link Context}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void listCustomModelsWithContext(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        for (CustomFormModelInfo modelInfo : client.listCustomModels(Context.NONE)) {
            assertTrue(modelInfo.getModelId() != null && modelInfo.getRequestedOn() != null
                && modelInfo.getCompletedOn() != null && modelInfo.getStatus() != null);
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
        assertEquals(exception.getMessage(), NULL_SOURCE_URL_ERROR);
    }

    /**
     * Verifies the result of the training operation for a valid labeled model Id and training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingLabeledResult(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingDataSASUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingDataSASUrl, useTrainingLabels);
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
        beginTrainingUnlabeledRunner((trainingDataSASUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingDataSASUrl, useTrainingLabels);
            syncPoller.waitForCompletion();
            validateCustomModelData(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies the result of the copy operation for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginCopy(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels);
            syncPoller.waitForCompletion();
            CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyRunner((resourceId, resourceRegion) -> {
                CopyAuthorization target =
                    client.getCopyAuthorization(resourceId, resourceRegion);
                SyncPoller<OperationResult,
                    CustomFormModelInfo> copyPoller = client.beginCopyModel(actualModel.getModelId(), target);
                CustomFormModelInfo copyModel = copyPoller.getFinalResult();
                assertEquals(target.getModelId(), copyModel.getModelId());
                assertNotNull(actualModel.getRequestedOn());
                assertNotNull(actualModel.getCompletedOn());
                assertEquals(CustomFormModelStatus.READY, copyModel.getStatus());
            });
        });
    }

    /**
     * Verifies the Invalid region ErrorResponseException is thrown for invalid region input to copy operation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginCopyInvalidRegion(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels);
            syncPoller.waitForCompletion();
            final CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyInvalidRegionRunner((resourceId, resourceRegion) -> {
                final CopyAuthorization target = client.getCopyAuthorization(resourceId, resourceRegion);
                Exception thrown = assertThrows(ErrorResponseException.class,
                    () -> client.beginCopyModel(actualModel.getModelId(), target));
                assertEquals(EXPECTED_COPY_REQUEST_INVALID_TARGET_RESOURCE_REGION, thrown.getMessage());
            });
        });
    }

    /**
     * Verifies {@link FormRecognizerException} is thrown for invalid region input to copy operation.
     */
    @SuppressWarnings("unchecked")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginCopyIncorrectRegion(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels);
            syncPoller.waitForCompletion();
            CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyIncorrectRegionRunner((resourceId, resourceRegion) -> {
                final CopyAuthorization target = client.getCopyAuthorization(resourceId, resourceRegion);
                FormRecognizerException formRecognizerException = assertThrows(FormRecognizerException.class,
                    () -> client.beginCopyModel(actualModel.getModelId(), target).getFinalResult());
                ErrorInformation errorInformation = formRecognizerException.getErrorInformation().get(0);
                assertEquals(RESOURCE_RESOLVER_ERROR, errorInformation.getCode());
                assertTrue(formRecognizerException.getMessage().startsWith(COPY_OPERATION_FAILED_STATUS_MESSAGE));
            });
        });
    }

    /**
     * Verifies the result of the copy authorization for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void copyAuthorization(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginCopyRunner((resourceId, resourceRegion) -> validateCopyAuthorizationResult(resourceId, resourceRegion,
            client.getCopyAuthorization(resourceId, resourceRegion)));
    }

    /**
     * Verifies the training operation throws FormRecognizerException when an invalid status model is returned.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingInvalidModelStatus(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingInvalidModelStatusRunner((invalidTrainingFilesUrl, useTrainingLabels) -> {
            FormRecognizerException formRecognizerException = assertThrows(FormRecognizerException.class,
                () -> client.beginTraining(invalidTrainingFilesUrl, useTrainingLabels).getFinalResult());
            ErrorInformation errorInformation = formRecognizerException.getErrorInformation().get(0);
            assertEquals(EXPECTED_INVALID_MODEL_STATUS_ERROR_CODE, errorInformation.getCode());
            assertEquals(EXPECTED_INVALID_MODEL_ERROR, errorInformation.getMessage());
            assertTrue(formRecognizerException.getMessage().contains(EXPECTED_INVALID_STATUS_EXCEPTION_MESSAGE));
        });
    }
}
