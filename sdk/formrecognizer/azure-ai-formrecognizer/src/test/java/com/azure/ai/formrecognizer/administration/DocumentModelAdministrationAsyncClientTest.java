// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.DocumentAnalysisServiceVersion;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.CreateComposedModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.administration.models.FormRecognizerError;
import com.azure.ai.formrecognizer.implementation.util.Utility;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DocumentModelAdministrationAsyncClientTest extends DocumentModelAdministrationClientTestBase {
    private DocumentModelAdministrationAsyncClient client;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private DocumentModelAdministrationAsyncClient getDocumentModelAdminAsyncClient(HttpClient httpClient,
                                                                                    DocumentAnalysisServiceVersion serviceVersion) {
        return getDocumentModelAdminClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    /**
     * Verifies the document analysis async client is valid.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getDocumentAnalysisClientAndValidate(HttpClient httpClient,
                                                     DocumentAnalysisServiceVersion serviceVersion) {
        DocumentAnalysisAsyncClient documentAnalysisAsyncClient =
            getDocumentModelAdminAsyncClient(httpClient, serviceVersion)
                .getDocumentAnalysisAsyncClient();
        blankPdfDataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller =
                documentAnalysisAsyncClient.beginAnalyzeDocument("prebuilt-receipt", Utility.toFluxByteBuffer(data), dataLength)
                    .getSyncPoller();
            syncPoller.waitForCompletion();
            assertNotNull(syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void validGetAccountProperties(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAccountProperties())
            .assertNext(DocumentModelAdministrationClientTestBase::validateAccountProperties)
            .verifyComplete();
    }

    /**
     * Verifies account properties returned with an Http Response for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void validGetAccountPropertiesWithResponse(HttpClient httpClient,
                                                      DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getAccountProperties())
            .assertNext(DocumentModelAdministrationClientTestBase::validateAccountProperties)
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void deleteModelValidModelIdWithResponse(HttpClient httpClient,
                                                    DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller1 =
                client.beginBuildModel(trainingFilesUrl, null)
                    .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller1.waitForCompletion();
            DocumentModel createdModel = syncPoller1.getFinalResult();

            StepVerifier.create(client.deleteModelWithResponse(createdModel.getModelId()))
                .assertNext(response -> assertEquals(response.getStatusCode(), HttpResponseStatus.NO_CONTENT.code()))
                .verifyComplete();

            StepVerifier.create(client.getModelWithResponse(createdModel.getModelId()))
                .verifyErrorSatisfies(throwable -> {
                    assertEquals(HttpResponseException.class, throwable.getClass());
                    final FormRecognizerError errorInformation = (FormRecognizerError)
                        ((HttpResponseException) throwable).getValue();
                    assertEquals("ModelNotFound", errorInformation.getInnerError().getCode());
                });
        });
    }

    /**
     * Verifies the result of the copy authorization for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void copyAuthorization(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getCopyAuthorization("java_copy_model_test"))
            .assertNext(DocumentModelAdministrationClientTestBase::validateCopyAuthorizationResult)
            .verifyComplete();

        StepVerifier.create(client.deleteModel("java_copy_model_test")).verifyComplete();
    }

    /**
     * Verifies the result of the create composed model for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void beginCreateComposedModel(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller1 =
                client.beginBuildModel(trainingFilesUrl, "component_model_1")
                    .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller1.waitForCompletion();
            DocumentModel createdModel1 = syncPoller1.getFinalResult();

            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller2 =
                client.beginBuildModel(trainingFilesUrl, "component_model_2")
                    .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller2.waitForCompletion();
            DocumentModel createdModel2 = syncPoller2.getFinalResult();

            final List<String> modelIdList = Arrays.asList(createdModel1.getModelId(), createdModel2.getModelId());

            DocumentModel composedModel = client.beginCreateComposedModel(modelIdList, "java_composed_model",
                    new CreateComposedModelOptions().setDescription("test desc"))
                .setPollInterval(durationTestMode)
                .getSyncPoller().getFinalResult();

            assertNotNull(composedModel.getModelId());
            assertEquals("test desc", composedModel.getDescription());
            assertEquals(2, composedModel.getDocTypes().size());
            validateDocumentModelData(composedModel);

            client.deleteModel(createdModel1.getModelId()).block();
            client.deleteModel(createdModel2.getModelId()).block();
            client.deleteModel(composedModel.getModelId()).block();
        });
    }

    /**
     * Verifies the result of building a document analysis model.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginBuildModel(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller1 =
                client.beginBuildModel(trainingFilesUrl, null)
                    .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller1.waitForCompletion();
            DocumentModel createdModel1 = syncPoller1.getFinalResult();

            validateDocumentModelData(createdModel1);
            client.deleteModel(createdModel1.getModelId()).block();
        });
    }

    /**
     * Verifies the result of the copy operation for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginCopy(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller1 =
                client.beginBuildModel(trainingFilesUrl, null)
                    .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller1.waitForCompletion();
            DocumentModel actualModel = syncPoller1.getFinalResult();

            Mono<CopyAuthorization> targetMono = client.getCopyAuthorization(null);
            CopyAuthorization target = targetMono.block();
            if (actualModel == null) {
                fail();
                return;
            }

            PollerFlux<DocumentOperationResult, DocumentModel> copyPoller =
                client.beginCopyModel(actualModel.getModelId(), target).setPollInterval(durationTestMode);
            DocumentModel copiedModel = copyPoller.getSyncPoller().getFinalResult();
            Assertions.assertEquals(target.getTargetModelId(), copiedModel.getModelId());

            client.deleteModel(actualModel.getModelId()).block();
            client.deleteModel(copiedModel.getModelId()).block();
        });
    }

    /**
     * Test for listing all models information.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void listModels(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.listModels().byPage().take(4))
        .thenConsumeWhile(documentModelInfoPagedResponse -> {
            documentModelInfoPagedResponse.getValue()
                .forEach(documentModelInfo -> {
                    assertNotNull(documentModelInfo.getModelId());
                    assertNotNull(documentModelInfo.getCreatedOn());
                });
            return true;
        }).verifyComplete();
    }

    /**
     * Verifies that an exception is thrown for null model ID parameter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getModelNullModelId(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getModel(null)).verifyError();
    }

    /**
     * Verifies document model info returned with response for a valid model ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getModelWithResponse(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller1 =
                client.beginBuildModel(trainingFilesUrl, null)
                    .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller1.waitForCompletion();
            DocumentModel createdModel = syncPoller1.getFinalResult();

            StepVerifier.create(client.getModelWithResponse(createdModel.getModelId()))
                .assertNext(documentModelResponse -> {
                    assertEquals(documentModelResponse.getStatusCode(), HttpResponseStatus.OK.code());
                    validateDocumentModelData(documentModelResponse.getValue());
                });
        });
    }

    /**
     * Test for listing all operations' information.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void listOperations(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentModelAdminAsyncClient(httpClient, serviceVersion);
        List<String> operationIdList = new ArrayList<>();
        StepVerifier.create(client.listOperations().byPage().take(10))
            .thenConsumeWhile(modelOperationInfoPagedResponse ->          {
                modelOperationInfoPagedResponse.getValue().forEach(modelOperationInfo -> {
                    operationIdList.add(modelOperationInfo.getOperationId());
                    assertTrue(modelOperationInfo.getOperationId() != null
                        && modelOperationInfo.getStatus() != null
                        && modelOperationInfo.getCreatedOn() != null
                        && modelOperationInfo.getLastUpdatedOn() != null
                        && modelOperationInfo.getResourceLocation() != null
                        && modelOperationInfo.getPercentCompleted() != null);
                });
                return true;
            })
            .verifyComplete();

        if (!CoreUtils.isNullOrEmpty(operationIdList)) {
            operationIdList.forEach(operationId -> StepVerifier.create(client.getOperation(operationId))
                .assertNext(modelOperation -> {
                    assertNotNull(modelOperation.getModelId());
                    assertNotNull(modelOperation.getCreatedOn());
                })
                .verifyComplete());
        }
    }
}
