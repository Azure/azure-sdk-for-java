// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizeReceiptsOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.models.CopyAuthorization;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.training.models.CustomFormModelStatus;
import com.azure.ai.formrecognizer.training.models.TrainingFileFilter;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static com.azure.ai.formrecognizer.FormRecognizerClientTestBase.EXPECTED_MODEL_ID_NOT_FOUND_ERROR_CODE;
import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.NULL_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormTrainingAsyncClientTest extends FormTrainingClientTestBase {

    static final String EXPECTED_COPY_REQUEST_INVALID_TARGET_RESOURCE_REGION = "Status code 400, \"{\"error\":{\"code\":\"1002\",\"message\":\"Copy request is invalid. Field 'TargetResourceRegion' must be a valid Azure region name.\"}}\"";
    private FormTrainingAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private FormTrainingAsyncClient getFormTrainingAsyncClient(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        return getFormTrainingClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    /**
     * Verifies the form recognizer async client is valid.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    void getFormRecognizerClientAndValidate(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        FormRecognizerAsyncClient formRecognizerClient = getFormTrainingAsyncClient(httpClient, serviceVersion)
            .getFormRecognizerAsyncClient();
        blankPdfDataRunner((data, dataLength) -> {
            SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
                formRecognizerClient.beginRecognizeReceipts(toFluxByteBuffer(data), dataLength,
                    new RecognizeReceiptsOptions()
                        .setContentType(FormContentType.APPLICATION_PDF)
                        .setPollInterval(durationTestMode))
                    .getSyncPoller();
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCustomModel(null)).verifyError();
    }

    /**
     * Verifies that an exception is thrown for invalid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getCustomModelInvalidModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        getCustomModelInvalidModelIdRunner(invalidModelId -> StepVerifier.create(client.getCustomModel(invalidModelId))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INVALID_MODEL_ID_ERROR)).verify());
    }

    /**
     * Verifies custom model info returned with response for a valid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getCustomModelWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel trainedModel = syncPoller.getFinalResult();

            StepVerifier.create(client.getCustomModelWithResponse(trainedModel.getModelId())).assertNext(customFormModelResponse -> {
                assertEquals(customFormModelResponse.getStatusCode(), HttpResponseStatus.OK.code());
                validateCustomModelData(syncPoller.getFinalResult(), false);
            });
        });
    }

    /**
     * Verifies unlabeled custom model info returned with response for a valid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getCustomModelUnlabeled(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller = client.beginTraining(
                trainingFilesUrl, useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel trainedUnlabeledModel = syncPoller.getFinalResult();
            StepVerifier.create(client.getCustomModel(trainedUnlabeledModel.getModelId()))
                .assertNext(customFormModel -> validateCustomModelData(syncPoller.getFinalResult(),
                    false));
        });
    }

    /**
     * Verifies labeled custom model info returned with response for a valid model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getCustomModelLabeled(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode))
                .getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel trainedLabeledModel = syncPoller.getFinalResult();
            StepVerifier.create(client.getCustomModel(trainedLabeledModel.getModelId()))
                .assertNext(customFormModel -> validateCustomModelData(syncPoller.getFinalResult(),
                    true));
        });
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void validGetAccountProperties(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAccountProperties())
            .assertNext(accountProperties -> validateAccountProperties(accountProperties))
            .verifyComplete();
    }

    /**
     * Verifies account properties returned with an Http Response for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void validGetAccountPropertiesWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAccountProperties())
            .assertNext(accountProperties -> validateAccountProperties(accountProperties))
            .verifyComplete();
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void deleteModelInvalidModelId(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.deleteModel(INVALID_MODEL_ID))
            .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals(INVALID_MODEL_ID_ERROR))
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void deleteModelValidModelIdWithResponse(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();

            StepVerifier.create(client.deleteModelWithResponse(createdModel.getModelId()))
                .assertNext(response -> assertEquals(response.getStatusCode(), HttpResponseStatus.NO_CONTENT.code()))
                .verifyComplete();

            StepVerifier.create(client.getCustomModelWithResponse(createdModel.getModelId()))
                .verifyErrorSatisfies(throwable -> {
                    assertEquals(HttpResponseException.class, throwable.getClass());
                    final FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation)
                        ((HttpResponseException) throwable).getValue();
                    assertEquals(EXPECTED_MODEL_ID_NOT_FOUND_ERROR_CODE, errorInformation.getErrorCode());
                });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void deleteModelValidModelIdWithResponseWithoutTrainingLabels(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, notUseTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller = client.beginTraining(trainingFilesUrl,
                notUseTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();

            StepVerifier.create(client.deleteModelWithResponse(createdModel.getModelId()))
                .assertNext(response -> assertEquals(response.getStatusCode(), HttpResponseStatus.NO_CONTENT.code()))
                .verifyComplete();

            StepVerifier.create(client.getCustomModelWithResponse(createdModel.getModelId()))
                .verifyErrorSatisfies(throwable -> {
                    assertEquals(HttpResponseException.class, throwable.getClass());
                    final FormRecognizerErrorInformation errorInformation = (FormRecognizerErrorInformation)
                        ((HttpResponseException) throwable).getValue();
                    assertEquals(EXPECTED_MODEL_ID_NOT_FOUND_ERROR_CODE, errorInformation.getErrorCode());
                });
        });
    }

    /**
     * Test for listing all models information.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void listCustomModels(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.listCustomModels())
            .thenConsumeWhile(customFormModelInfo -> customFormModelInfo.getModelId() != null && customFormModelInfo.getTrainingStartedOn() != null
                    && customFormModelInfo.getTrainingCompletedOn() != null && customFormModelInfo.getStatus() != null)
            .verifyComplete();
    }

    /**
     * Verifies that an exception is thrown for null source url input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingNullInput(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        NullPointerException thrown = assertThrows(
            NullPointerException.class,
            () -> client.beginTraining(null, false,
                new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());

        assertTrue(thrown.getMessage().equals(NULL_SOURCE_URL_ERROR));
    }

    /**
     * Verifies the result of the copy operation for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginCopy(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels, new TrainingOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyRunner((resourceId, resourceRegion) -> {
                Mono<CopyAuthorization> targetMono = client.getCopyAuthorization(resourceId, resourceRegion);
                CopyAuthorization target = targetMono.block();
                if (actualModel == null) {
                    assertTrue(false);
                    return;
                }

                PollerFlux<FormRecognizerOperationResult, CustomFormModelInfo> copyPoller =
                    client.beginCopyModel(actualModel.getModelId(), target, durationTestMode);
                CustomFormModelInfo copyModel = copyPoller.getSyncPoller().getFinalResult();
                assertNotNull(target.getModelId(), copyModel.getModelId());
                assertNotNull(actualModel.getTrainingStartedOn());
                assertNotNull(actualModel.getTrainingCompletedOn());
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels, new TrainingOptions()
                    .setPollInterval(durationTestMode))
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyInvalidRegionRunner((resourceId, resourceRegion) -> {
                Mono<CopyAuthorization> targetMono = client.getCopyAuthorization(resourceId, resourceRegion);
                CopyAuthorization target = targetMono.block();
                if (actualModel == null) {
                    assertTrue(false);
                    return;
                }
                PollerFlux<FormRecognizerOperationResult, CustomFormModelInfo> copyPoller = client.beginCopyModel(
                    actualModel.getModelId(), target);

                Exception thrown = assertThrows(HttpResponseException.class,
                    () -> copyPoller.getSyncPoller().getFinalResult());
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels,
                    new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyIncorrectRegionRunner((resourceId, resourceRegion) -> {
                Mono<CopyAuthorization> targetMono = client.getCopyAuthorization(resourceId, resourceRegion);
                CopyAuthorization target = targetMono.block();
                if (actualModel == null) {
                    assertTrue(false);
                    return;
                }
                FormRecognizerException formRecognizerException = assertThrows(FormRecognizerException.class,
                    () -> client.beginCopyModel(actualModel.getModelId(), target, durationTestMode)
                        .getSyncPoller().getFinalResult());
                FormRecognizerErrorInformation errorInformation = formRecognizerException.getErrorInformation().get(0);
                // TODO: Service bug https://github.com/Azure/azure-sdk-for-java/issues/12046
                // Should return resource resolve error instead locally returning Authorization error
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginCopyRunner((resourceId, resourceRegion) ->
            StepVerifier.create(client.getCopyAuthorization(resourceId, resourceRegion))
                .assertNext(copyAuthorization ->
                    validateCopyAuthorizationResult(resourceId, resourceRegion, copyAuthorization))
                .verifyComplete()
        );
    }

    /**
     * Verifies the training operation throws FormRecognizerException when an invalid status model is returned.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingInvalidModelStatus(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingInvalidModelStatusRunner((invalidTrainingFilesUrl, useTrainingLabels) -> {
            FormRecognizerException formRecognizerException = assertThrows(FormRecognizerException.class,
                () -> client.beginTraining(invalidTrainingFilesUrl, useTrainingLabels,
                    new TrainingOptions().setPollInterval(durationTestMode))
                    .getSyncPoller().getFinalResult());

            FormRecognizerErrorInformation errorInformation = formRecognizerException.getErrorInformation().get(0);
            assertEquals(EXPECTED_INVALID_MODEL_STATUS_ERROR_CODE, errorInformation.getErrorCode());
            assertTrue(formRecognizerException.getMessage().contains(EXPECTED_INVALID_MODEL_STATUS_MESSAGE));
            assertEquals(EXPECTED_INVALID_MODEL_ERROR, errorInformation.getMessage());
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels, new TrainingOptions()
                    .setPollInterval(durationTestMode)).getSyncPoller();
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels, new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingMultipageRunner(trainingFilesUrl -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                true, new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingMultipageRunner(trainingFilesUrl -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                false, new TrainingOptions().setPollInterval(durationTestMode)).getSyncPoller();
            trainingPoller.waitForCompletion();
            validateCustomModelData(trainingPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies the result of the training operation for a valid labeled model Id and include subfolder training set
     * Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginTrainingWithoutTrainingLabelsIncludeSubfolderWithPrefixName(HttpClient httpClient,
        FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels, new TrainingOptions()
                    .setPollInterval(durationTestMode)
                    .setTrainingFileFilter(new TrainingFileFilter().setSubfoldersIncluded(true)
                        .setPrefix(PREFIX_SUBFOLDER))).getSyncPoller();
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels, new TrainingOptions()
                    .setTrainingFileFilter(new TrainingFileFilter().setPrefix(PREFIX_SUBFOLDER))
                    .setPollInterval(durationTestMode)).getSyncPoller();
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingMultipageRunner(trainingFilesUrl -> {
            FormRecognizerException thrown = assertThrows(FormRecognizerException.class, () ->
                client.beginTraining(trainingFilesUrl, false, new TrainingOptions()
                        .setTrainingFileFilter(new TrainingFileFilter().setSubfoldersIncluded(true)
                                .setPrefix(INVALID_PREFIX_FILE_NAME))
                        .setPollInterval(durationTestMode)).getSyncPoller().getFinalResult());
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingMultipageRunner(trainingFilesUrl -> {
            FormRecognizerException thrown = assertThrows(FormRecognizerException.class, () ->
                client.beginTraining(trainingFilesUrl, false,
                    new TrainingOptions()
                        .setTrainingFileFilter(new TrainingFileFilter().setPrefix(INVALID_PREFIX_FILE_NAME))
                        .setPollInterval(durationTestMode)).getSyncPoller()
                    .getFinalResult());
            assertEquals(NO_VALID_BLOB_FOUND, thrown.getErrorInformation().get(0).getMessage());
        });
    }
}
