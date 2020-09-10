// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizeReceiptsOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.models.AccountProperties;
import com.azure.ai.formrecognizer.training.models.CopyAuthorization;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.training.models.CustomFormModelStatus;
import com.azure.ai.formrecognizer.training.models.TrainingFileFilter;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.EXPECTED_MODEL_ID_NOT_FOUND_ERROR_CODE;
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
     * Verifies the form recognizer client is valid.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getFormRecognizerClientAndValidate(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        FormRecognizerClient formRecognizerClient = getFormTrainingClient(httpClient, serviceVersion)
            .getFormRecognizerClient();
        blankPdfDataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                formRecognizerClient.beginRecognizeReceipts(data, dataLength,
                    new RecognizeReceiptsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF)
                        .setPollInterval(durationTestMode),
                    Context.NONE);
            syncPoller.waitForCompletion();
            validateBlankPdfResultData(syncPoller.getFinalResult());
        });
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
        assertEquals(INVALID_MODEL_ID_ERROR, exception.getMessage());
    }

    /**
     * Verifies custom model info returned with response for a valid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getCustomModelWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingDataSasUrl, useTrainingLabels) -> {
            CustomFormModel trainedUnlabeledModel = client.beginTraining(trainingDataSasUrl, useTrainingLabels,
                new TrainingOptions().setPollInterval(durationTestMode), Context.NONE).getFinalResult();
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
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
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
            CustomFormModel customFormModel = client.beginTraining(trainingDataSASUrl, useTrainingLabels,
                new TrainingOptions().setPollInterval(durationTestMode), Context.NONE).getFinalResult();
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
        assertEquals(INVALID_MODEL_ID_ERROR, exception.getMessage());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void deleteModelValidModelIdWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingDataSASUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller = client.beginTraining(trainingDataSASUrl,
                useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();

            final Response<Void> deleteModelWithResponse = client.deleteModelWithResponse(createdModel.getModelId(),
                Context.NONE);
            assertEquals(deleteModelWithResponse.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
            final HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
                client.getCustomModelWithResponse(createdModel.getModelId(), Context.NONE));
            final FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation) exception.getValue();
            assertEquals(EXPECTED_MODEL_ID_NOT_FOUND_ERROR_CODE, errorInformation.getErrorCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void deleteModelValidModelIdWithResponseWithoutTrainingLabels(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingDataSASUrl, notUseTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller = client.beginTraining(trainingDataSASUrl,
                notUseTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();

            final Response<Void> deleteModelWithResponse = client.deleteModelWithResponse(createdModel.getModelId(),
                Context.NONE);
            assertEquals(deleteModelWithResponse.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
            final HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
                client.getCustomModelWithResponse(createdModel.getModelId(), Context.NONE));
            final FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation) exception.getValue();
            assertEquals(EXPECTED_MODEL_ID_NOT_FOUND_ERROR_CODE, errorInformation.getErrorCode());
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
            assertTrue(modelInfo.getModelId() != null && modelInfo.getTrainingStartedOn() != null
                && modelInfo.getTrainingCompletedOn() != null && modelInfo.getStatus() != null);
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
            assertTrue(modelInfo.getModelId() != null && modelInfo.getTrainingStartedOn() != null
                && modelInfo.getTrainingCompletedOn() != null && modelInfo.getStatus() != null);
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
     * Verifies the result of the copy operation for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginCopy(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels,
                    new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            syncPoller.waitForCompletion();
            CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyRunner((resourceId, resourceRegion) -> {
                CopyAuthorization target =
                    client.getCopyAuthorization(resourceId, resourceRegion);
                SyncPoller<FormRecognizerOperationResult,
                    CustomFormModelInfo> copyPoller = client.beginCopyModel(actualModel.getModelId(), target,
                    durationTestMode, Context.NONE);
                CustomFormModelInfo copyModel = copyPoller.getFinalResult();
                assertEquals(target.getModelId(), copyModel.getModelId());
                assertNotNull(actualModel.getTrainingStartedOn());
                assertNotNull(actualModel.getTrainingCompletedOn());
                assertEquals(CustomFormModelStatus.READY, copyModel.getStatus());
            });
        });
    }

    /**
     * Verifies the Invalid region HttpResponseException is thrown for invalid region input to copy operation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginCopyInvalidRegion(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels,
                new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            syncPoller.waitForCompletion();
            final CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyInvalidRegionRunner((resourceId, resourceRegion) -> {
                final CopyAuthorization target = client.getCopyAuthorization(resourceId, resourceRegion);
                Exception thrown = assertThrows(HttpResponseException.class,
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
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels,
                    new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            syncPoller.waitForCompletion();
            CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyIncorrectRegionRunner((resourceId, resourceRegion) -> {
                final CopyAuthorization target = client.getCopyAuthorization(resourceId, resourceRegion);
                FormRecognizerException formRecognizerException = assertThrows(FormRecognizerException.class,
                    () -> client.beginCopyModel(actualModel.getModelId(), target, durationTestMode, Context.NONE)
                        .getFinalResult());
                FormRecognizerErrorInformation errorInformation = formRecognizerException.getErrorInformation().get(0);
                // TODO: Service bug https://github.com/Azure/azure-sdk-for-java/issues/12046
                // assertEquals(RESOURCE_RESOLVER_ERROR, errorInformation.getCode());
                // assertTrue(formRecognizerException.getMessage().startsWith(COPY_OPERATION_FAILED_STATUS_MESSAGE));
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
                () -> client.beginTraining(invalidTrainingFilesUrl, useTrainingLabels,
                    new TrainingOptions().setPollInterval(durationTestMode), Context.NONE)
                    .getFinalResult());
            FormRecognizerErrorInformation errorInformation = formRecognizerException.getErrorInformation().get(0);
            assertEquals(EXPECTED_INVALID_MODEL_STATUS_ERROR_CODE, errorInformation.getErrorCode());
            assertEquals(EXPECTED_INVALID_MODEL_ERROR, errorInformation.getMessage());
            assertTrue(formRecognizerException.getMessage().contains(EXPECTED_INVALID_MODEL_STATUS_MESSAGE));
            assertTrue(formRecognizerException.getMessage().contains(EXPECTED_INVALID_STATUS_EXCEPTION_MESSAGE));
        });
    }

    /**
     * Verifies the result of the training operation for a valid labeled model Id and JPG training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingWithTrainingLabelsForJPGTrainingSet(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            trainingPoller.waitForCompletion();
            validateCustomModelData(trainingPoller.getFinalResult(), true);
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model Id and JPG training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingWithoutTrainingLabelsForJPGTrainingSet(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            trainingPoller.waitForCompletion();
            validateCustomModelData(trainingPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies the result of the training operation for a valid labeled model Id and multi-page PDF training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingWithTrainingLabelsForMultiPagePDFTrainingSet(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingMultipageRunner(trainingFilesUrl -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                true, new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            trainingPoller.waitForCompletion();
            validateCustomModelData(trainingPoller.getFinalResult(), true);
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model Id and multi-page PDF training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingWithoutTrainingLabelsForMultiPagePDFTrainingSet(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingMultipageRunner(trainingFilesUrl -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                false, new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            trainingPoller.waitForCompletion();
            validateCustomModelData(trainingPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model Id and include subfolder training set
     * Url with existing prefix name.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingWithoutTrainingLabelsIncludeSubfolderWithPrefixName(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels,
                new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            trainingPoller.waitForCompletion();
            validateCustomModelData(trainingPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model ID and exclude subfolder training set
     * URL with existing prefix name.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingWithoutTrainingLabelsExcludeSubfolderWithPrefixName(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels,
                new TrainingOptions().setPollInterval(durationTestMode), Context.NONE);
            trainingPoller.waitForCompletion();
            validateCustomModelData(trainingPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model Id and include subfolder training set
     * Url with non-existing prefix name.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingWithoutTrainingLabelsIncludeSubfolderWithNonExistPrefixName(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingMultipageRunner(trainingFilesUrl -> {
            FormRecognizerException thrown = assertThrows(FormRecognizerException.class, () ->
                client.beginTraining(trainingFilesUrl, false,
                    new TrainingOptions()
                        .setPollInterval(durationTestMode)
                        .setTrainingFileFilter(new TrainingFileFilter()
                            .setSubfoldersIncluded(true)
                            .setPrefix(INVALID_PREFIX_FILE_NAME)),
                    Context.NONE).getFinalResult());
            assertEquals(NO_VALID_BLOB_FOUND, thrown.getErrorInformation().get(0).getMessage());
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model Id and exclude subfolder training set
     * Url with non-existing prefix name.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingWithoutTrainingLabelsExcludeSubfolderWithNonExistPrefixName(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingClient(httpClient, serviceVersion);
        beginTrainingMultipageRunner(trainingFilesUrl -> {
            FormRecognizerException thrown = assertThrows(FormRecognizerException.class, () ->
                client.beginTraining(trainingFilesUrl, false,
                    new TrainingOptions()
                        .setTrainingFileFilter(new TrainingFileFilter().setPrefix(INVALID_PREFIX_FILE_NAME))
                        .setPollInterval(durationTestMode), Context.NONE)
                    .getFinalResult());
            assertEquals(NO_VALID_BLOB_FOUND, thrown.getErrorInformation().get(0).getMessage());
        });
    }
}
