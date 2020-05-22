// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.CopyAuthorization;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
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

import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.NULL_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.getExpectedAccountProperties;
import static com.azure.ai.formrecognizer.models.CustomFormModelStatus.READY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormTrainingAsyncClientTest extends FormTrainingClientTestBase {

    static final String EXPECTED_COPY_REQUEST_INVALID_FIELD_TARGET_RESOURCE_REGION = "Status code 400, \"{\"error\":{\"code\":\"1002\",\"message\":\"Copy request is invalid. Field 'TargetResourceRegion' must be a valid Azure region name.\"}}\"";
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
        FormTrainingClientBuilder builder = new FormTrainingClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());
        AzureKeyCredential credential = (getTestMode() == TestMode.PLAYBACK)
            ? new AzureKeyCredential(INVALID_KEY) : new AzureKeyCredential(getApiKey());
        builder.credential(credential);
        return builder.buildAsyncClient();
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
            SyncPoller<OperationResult, CustomFormModel> syncPoller = client.beginTraining(trainingFilesUrl,
                useTrainingLabels).getSyncPoller();
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
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels).getSyncPoller();
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
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels).getSyncPoller();
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
            .assertNext(accountProperties -> validateAccountProperties(getExpectedAccountProperties(),
                accountProperties))
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
            .assertNext(accountProperties ->
                validateAccountProperties(getExpectedAccountProperties(), accountProperties))
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
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels).getSyncPoller();
            syncPoller.waitForCompletion();
            CustomFormModel createdModel = syncPoller.getFinalResult();

            StepVerifier.create(client.deleteModelWithResponse(createdModel.getModelId()))
                .assertNext(response ->
                    assertEquals(response.getStatusCode(), HttpResponseStatus.NO_CONTENT.code()))
                .verifyComplete();

            StepVerifier.create(client.getCustomModelWithResponse(createdModel.getModelId()))
                .verifyErrorSatisfies(throwable ->
                    assertTrue(throwable.getMessage().contains("404")));
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
            .thenConsumeWhile(customFormModelInfo ->
                customFormModelInfo.getModelId() != null && customFormModelInfo.getRequestedOn() != null
                    && customFormModelInfo.getCompletedOn() != null && customFormModelInfo.getStatus() != null)
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
            () -> client.beginTraining(null, false).getSyncPoller().getFinalResult());

        assertTrue(thrown.getMessage().equals(NULL_SOURCE_URL_ERROR));
    }

    /**
     * Verifies the result of the training operation for a valid labeled model Id and training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
     public void beginTrainingLabeledResult(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingLabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels).getSyncPoller();
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
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels).getSyncPoller();
            syncPoller.waitForCompletion();
            validateCustomModelData(syncPoller.getFinalResult(), false);
        });
    }

    /**
     * Verifies the result of the copy operation for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    void beginCopy(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels).getSyncPoller();
            syncPoller.waitForCompletion();
            final CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyRunner((resourceId, resourceRegion) -> {
                final Mono<CopyAuthorization> target =
                    client.getCopyAuthorization(resourceId, resourceRegion);
                final PollerFlux<OperationResult,
                    CustomFormModelInfo> copyPoller = client.beginCopyModel(actualModel.getModelId(), target.block());
                final CustomFormModelInfo copyModel = copyPoller.getSyncPoller().getFinalResult();
                assertEquals(actualModel.getModelId(), copyModel.getModelId());
                assertNotNull(actualModel.getCreatedOn());
                // assertEquals(actualModel.getCreatedOn(), copyModel.getCreatedOn());
                // Expected :2020-05-22T21:30:32Z
                // Actual   :2020-05-22T21:30:58.511074200Z
                // assertEquals(actualModel.getLastUpdatedOn(), copyModel.getLastUpdatedOn());
                // Expected :2020-05-22T21:30:44Z
                // Actual   :2020-05-22T21:30:58.511074900Z
                assertNotNull(actualModel.getLastUpdatedOn());
                assertEquals(READY, copyModel.getStatus());
            });
        });
    }

    /**
     * Verifies the Invalid region ErrorResponseException is thrown for invalid region input to copy operation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    void beginCopyInvalidRegion(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginTrainingUnlabeledRunner((trainingFilesUrl, useTrainingLabels) -> {
            SyncPoller<OperationResult, CustomFormModel> syncPoller =
                client.beginTraining(trainingFilesUrl, useTrainingLabels).getSyncPoller();
            syncPoller.waitForCompletion();
            final CustomFormModel actualModel = syncPoller.getFinalResult();

            beginCopyInvalidRegionRunner((resourceId, resourceRegion) -> {
                final Mono<CopyAuthorization> target =
                    client.getCopyAuthorization(resourceId, resourceRegion);
                final PollerFlux<OperationResult,
                    CustomFormModelInfo> copyPoller = client.beginCopyModel(actualModel.getModelId(), target.block());

                Exception thrown = assertThrows(ErrorResponseException.class,
                    () -> copyPoller.getSyncPoller().getFinalResult());
                assertEquals(EXPECTED_COPY_REQUEST_INVALID_FIELD_TARGET_RESOURCE_REGION, thrown.getMessage());
            });
        });
    }

    /**
     * Verifies the result of the copy authorization for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    void copyAuthorization(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion) {
        client = getFormTrainingAsyncClient(httpClient, serviceVersion);
        beginCopyRunner((resourceId, resourceRegion) ->
            StepVerifier.create(client.getCopyAuthorization(resourceId, resourceRegion))
                .assertNext(copyAuthorization ->
                    validateCopyAuthorizationResult(resourceId, resourceRegion, copyAuthorization))
                .verifyComplete()
        );
    }
}
