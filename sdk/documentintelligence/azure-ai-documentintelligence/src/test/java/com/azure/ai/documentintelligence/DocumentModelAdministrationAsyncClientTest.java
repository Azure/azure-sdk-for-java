// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AuthorizeClassifierCopyOptions;
import com.azure.ai.documentintelligence.models.AuthorizeModelCopyOptions;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.AzureBlobFileListContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentClassifierOptions;
import com.azure.ai.documentintelligence.models.BuildDocumentModelOptions;
import com.azure.ai.documentintelligence.models.ClassifierCopyAuthorization;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
import com.azure.ai.documentintelligence.models.ComposeDocumentModelOptions;
import com.azure.ai.documentintelligence.models.DocumentBuildMode;
import com.azure.ai.documentintelligence.models.DocumentClassifierBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentClassifierCopyToOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentClassifierDetails;
import com.azure.ai.documentintelligence.models.DocumentModelBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelComposeOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelCopyToOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelDetails;
import com.azure.ai.documentintelligence.models.DocumentTypeDetails;
import com.azure.ai.documentintelligence.models.ModelCopyAuthorization;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.azure.ai.documentintelligence.TestUtils.DEFAULT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class DocumentModelAdministrationAsyncClientTest extends DocumentAdministrationClientTestBase {
    private DocumentIntelligenceAdministrationAsyncClient client;

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient).skipRequest((ignored1, ignored2) -> false)
            .assertAsync()
            .build();
    }

    private DocumentIntelligenceAdministrationAsyncClient getModelAdminAsyncClient(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        return getModelAdminClientBuilder(
            buildAsyncAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion).buildAsyncClient();
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void validGetResourceInfo(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getResourceDetails())
            .assertNext(DocumentAdministrationClientTestBase::validateResourceInfo)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies account properties returned with a Http Response for a subscription account.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void validgetResourceInfoWithResponse(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getResourceDetails())
            .assertNext(DocumentAdministrationClientTestBase::validateResourceInfo)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void deleteModelValidModelIdWithResponse(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {

        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller1 = client
                .beginBuildDocumentModel(
                    new BuildDocumentModelOptions("modelId" + UUID.randomUUID(), DocumentBuildMode.TEMPLATE)
                        .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller1.waitForCompletion();
            DocumentModelDetails createdModel = syncPoller1.getFinalResult();

            StepVerifier.create(client.deleteModelWithResponse(createdModel.getModelId(), null))
                .assertNext(response -> assertEquals(response.getStatusCode(), HttpResponseStatus.NO_CONTENT.code()))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        });
    }

    /**
     * Verifies the result of the copy authorization for valid parameters.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void copyAuthorization(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String modelId = "modelId" + UUID.randomUUID();
        try {
            buildModelRunner((trainingFilesUrl) -> {
                SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller1 = client
                    .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId, DocumentBuildMode.TEMPLATE)
                        .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
                syncPoller1.waitForCompletion();
                DocumentModelDetails createdModel = syncPoller1.getFinalResult();
            });

            StepVerifier
                .create(client.authorizeModelCopyWithResponse(
                    BinaryData.fromObject(new AuthorizeModelCopyOptions("copy_model_id" + UUID.randomUUID())), null))
                .assertNext(response -> validateCopyAuthorizationResult(
                    response.getValue().toObject(ModelCopyAuthorization.class)))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        } finally {
            StepVerifier.create(client.deleteModel(modelId)).expectComplete().verify(DEFAULT_TIMEOUT);
        }
    }

    /**
     * Verifies the result of the create composed model for valid parameters.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginCreateComposedModel(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String modelId1 = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId1" + UUID.randomUUID();
        String modelId2 = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId2" + UUID.randomUUID();
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        String composedModelId
            = interceptorManager.isPlaybackMode() ? "REDACTED" : "composedModelId" + UUID.randomUUID();

        try {
            buildModelRunner((trainingDataSasUrl) -> {
                SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller1 = client
                    .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId1, DocumentBuildMode.TEMPLATE)
                        .setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();

                LongRunningOperationStatus operationStatus1 = syncPoller1.poll().getStatus();
                while (!LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(operationStatus1)) {
                    operationStatus1 = syncPoller1.poll().getStatus();
                }

                DocumentModelDetails documentModelDetails1 = syncPoller1.getFinalResult();
                assertNotNull(documentModelDetails1);

                SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller2 = client
                    .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId2, DocumentBuildMode.TEMPLATE)
                        .setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();

                LongRunningOperationStatus operationStatus2 = syncPoller2.poll().getStatus();
                while (!LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(operationStatus2)) {
                    operationStatus2 = syncPoller2.poll().getStatus();
                }

                DocumentModelDetails documentModelDetails2 = syncPoller2.getFinalResult();
                assertNotNull(documentModelDetails2);
            });

            beginClassifierRunner((trainingFilesUrl) -> {
                Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsClassifierMap = new HashMap<>();
                documentTypeDetailsClassifierMap.put("IRS-1040-A", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-A/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-B", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-B/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-C", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-C/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-D", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-D/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-E", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-E/train")));

                SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> syncPoller = client
                    .beginBuildClassifier(
                        new BuildDocumentClassifierOptions(classifierId, documentTypeDetailsClassifierMap))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();

                LongRunningOperationStatus operationStatus = syncPoller.poll().getStatus();
                while (!LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(operationStatus)) {
                    operationStatus = syncPoller.poll().getStatus();
                }

                DocumentClassifierDetails documentClassifierDetails = syncPoller.getFinalResult();
                assertNotNull(documentClassifierDetails);
            });

            Map<String, DocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A", new DocumentTypeDetails().setModelId(modelId1));
            documentTypeDetailsMap.put("IRS-1040-B", new DocumentTypeDetails().setModelId(modelId2));

            SyncPoller<DocumentModelComposeOperationDetails, DocumentModelDetails> composePoller
                = client
                    .beginComposeModel(
                        new ComposeDocumentModelOptions(composedModelId, classifierId, documentTypeDetailsMap)
                            .setDescription("test desc"))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();

            LongRunningOperationStatus composeOperationStatus = composePoller.poll().getStatus();
            while (!LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(composeOperationStatus)) {
                composeOperationStatus = composePoller.poll().getStatus();
            }

            DocumentModelDetails composedModel = composePoller.getFinalResult();
            assertNotNull(composedModel.getModelId());
            assertEquals("test desc", composedModel.getDescription());
            assertEquals(2, composedModel.getDocumentTypes().size());
            validateDocumentModelData(composedModel);
        } finally {
            StepVerifier.create(client.deleteModel(modelId1)).expectComplete().verify(DEFAULT_TIMEOUT);

            StepVerifier.create(client.deleteModel(modelId2)).expectComplete().verify(DEFAULT_TIMEOUT);

            StepVerifier.create(client.deleteModel(composedModelId)).expectComplete().verify(DEFAULT_TIMEOUT);
        }
    }

    /**
     * Verifies the result of building a document analysis model.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginBuildModel(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller1 = client
                .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId, DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller1.waitForCompletion();
            DocumentModelDetails createdModel1 = syncPoller1.getFinalResult();

            validateDocumentModelData(createdModel1);
            client.deleteModel(createdModel1.getModelId()).block();
        });
    }

    /**
     * Verifies the result of the copy operation for valid parameters.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginCopy(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller1 = client
                .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId, DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
                .setPollInterval(durationTestMode)
                .getSyncPoller();
            syncPoller1.waitForCompletion();
            DocumentModelDetails actualModel = syncPoller1.getFinalResult();

            Mono<ModelCopyAuthorization> targetMono
                = client.authorizeModelCopy(new AuthorizeModelCopyOptions("copyModelId" + UUID.randomUUID()));
            ModelCopyAuthorization target = targetMono.block();
            if (actualModel == null) {
                fail();
                return;
            }

            PollerFlux<DocumentModelCopyToOperationDetails, DocumentModelDetails> copyPoller
                = client.beginCopyModelTo(actualModel.getModelId(), target).setPollInterval(durationTestMode);
            DocumentModelDetails copiedModel = copyPoller.getSyncPoller().getFinalResult();
            Assertions.assertEquals(target.getTargetModelId(), copiedModel.getModelId());

            client.deleteModel(actualModel.getModelId()).block();
            client.deleteModel(copiedModel.getModelId()).block();
        });
    }

    /**
     * Test for listing all models information.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void listModels(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.listModels().byPage().take(4)).thenConsumeWhile(documentModelInfoPagedResponse -> {
            documentModelInfoPagedResponse.getValue().forEach(documentModelInfo -> {
                assertNotNull(documentModelInfo.getModelId());
                assertNotNull(documentModelInfo.getCreatedOn());
            });
            return true;
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    /**
     * Test for listing all operations' information.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void listOperations(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {

        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        List<String> operationIdList = new ArrayList<>();
        StepVerifier.create(client.listOperations().byPage().take(2))
            .thenConsumeWhile(modelOperationInfoPagedResponse -> {
                modelOperationInfoPagedResponse.getValue().forEach(modelOperationInfo -> {
                    operationIdList.add(modelOperationInfo.getOperationId());
                    assertNotNull(modelOperationInfo.getOperationId());
                    assertNotNull(modelOperationInfo.getKind());
                    assertNotNull(modelOperationInfo.getStatus());
                    assertNotNull(modelOperationInfo.getCreatedOn());
                    assertNotNull(modelOperationInfo.getResourceLocation());
                });
                return true;
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        if (!CoreUtils.isNullOrEmpty(operationIdList)) {
            operationIdList.forEach(
                operationId -> StepVerifier.create(client.getOperation(operationId)).assertNext(operationDetails -> {
                    assertNotNull(operationDetails.getOperationId());
                    assertNotNull(operationDetails.getCreatedOn());
                }).expectComplete().verify(DEFAULT_TIMEOUT));
        }
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginBuildClassifier(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-A/train")));
            documentTypeDetailsMap.put("IRS-1040-B", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-B/train")));
            documentTypeDetailsMap.put("IRS-1040-C", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-C/train")));
            documentTypeDetailsMap.put("IRS-1040-D", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-D/train")));
            documentTypeDetailsMap.put("IRS-1040-E", new ClassifierDocumentTypeDetails()
                .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-E/train")));
            SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller
                = client.beginBuildClassifier(new BuildDocumentClassifierOptions(classifierId, documentTypeDetailsMap))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();

            DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();
            validateClassifierModelData(buildModelPoller.getFinalResult());
            assertNotNull(documentClassifierDetails.getDocumentTypes());
            documentClassifierDetails.getDocumentTypes()
                .forEach((s, classifierDocumentTypeDetails) -> assertNotNull(
                    (classifierDocumentTypeDetails.getAzureBlobSource()).getContainerUrl()));
        });
    }

    /**
     * Verifies the result of the training operation for a classifier with a valid training data set with jsonL files.
     */
    @RecordWithoutRequestBody
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginBuildClassifierWithJsonL(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A", new ClassifierDocumentTypeDetails()
                .setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-A.jsonl")));
            documentTypeDetailsMap.put("IRS-1040-B", new ClassifierDocumentTypeDetails()
                .setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-B.jsonl")));
            documentTypeDetailsMap.put("IRS-1040-C", new ClassifierDocumentTypeDetails()
                .setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-C.jsonl")));
            documentTypeDetailsMap.put("IRS-1040-D", new ClassifierDocumentTypeDetails()
                .setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-D.jsonl")));
            documentTypeDetailsMap.put("IRS-1040-E", new ClassifierDocumentTypeDetails()
                .setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-E.jsonl")));
            SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller
                = client.beginBuildClassifier(new BuildDocumentClassifierOptions(classifierId, documentTypeDetailsMap))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();
            DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();
            assertNotNull(documentClassifierDetails.getDocumentTypes());
            documentClassifierDetails.getDocumentTypes()
                .forEach((s, classifierDocumentTypeDetails) -> assertNotNull(
                    (classifierDocumentTypeDetails.getAzureBlobFileListSource()).getContainerUrl()));

            validateClassifierModelData(buildModelPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the copy authorization for classifier with valid parameters.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void copyAuthorizationClassifier(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        final DocumentClassifierDetails[] documentClassifierDetails = { null };

        try {
            beginClassifierRunner((trainingFilesUrl) -> {
                Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsClassifierMap = new HashMap<>();
                documentTypeDetailsClassifierMap.put("IRS-1040-A", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-A/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-B", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-B/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-C", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-C/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-D", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-D/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-E", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-E/train")));

                SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> syncPoller = client
                    .beginBuildClassifier(
                        new BuildDocumentClassifierOptions(classifierId, documentTypeDetailsClassifierMap))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();

                LongRunningOperationStatus operationStatus = syncPoller.poll().getStatus();
                while (!LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(operationStatus)) {
                    operationStatus = syncPoller.poll().getStatus();
                }

                documentClassifierDetails[0] = syncPoller.getFinalResult();
            });

            StepVerifier
                .create(client.authorizeClassifierCopyWithResponse(
                    BinaryData.fromObject(new AuthorizeClassifierCopyOptions("copy_classifier" + UUID.randomUUID())
                        .setDescription("test description")),
                    null))
                .assertNext(response -> validateClassifierCopyAuthorizationResult(
                    response.getValue().toObject(ClassifierCopyAuthorization.class)))
                .expectComplete()
                .verify(DEFAULT_TIMEOUT);
        } finally {
            StepVerifier.create(client.deleteClassifier(classifierId)).expectComplete().verify(DEFAULT_TIMEOUT);
        }
    }

    /**
     * Verifies the result of the copy classifier operation for valid parameters.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginCopyClassifier(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        final DocumentClassifierDetails[] documentClassifierDetails = { null };
        final DocumentClassifierDetails[] copiedClassifier = { null };

        try {
            beginClassifierRunner((trainingFilesUrl) -> {
                Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsClassifierMap = new HashMap<>();
                documentTypeDetailsClassifierMap.put("IRS-1040-A", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-A/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-B", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-B/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-C", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-C/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-D", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-D/train")));
                documentTypeDetailsClassifierMap.put("IRS-1040-E", new ClassifierDocumentTypeDetails()
                    .setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-E/train")));

                SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> syncPoller = client
                    .beginBuildClassifier(
                        new BuildDocumentClassifierOptions(classifierId, documentTypeDetailsClassifierMap))
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();

                LongRunningOperationStatus operationStatus = syncPoller.poll().getStatus();
                while (!LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(operationStatus)) {
                    operationStatus = syncPoller.poll().getStatus();
                }

                documentClassifierDetails[0] = syncPoller.getFinalResult();
            });

            final ClassifierCopyAuthorization[] copyAuthorization = new ClassifierCopyAuthorization[1];
            Mono<ClassifierCopyAuthorization> targetMono = client
                .authorizeClassifierCopy(new AuthorizeClassifierCopyOptions("copyClassifierId" + UUID.randomUUID()));
            StepVerifier.create(targetMono).assertNext(target -> {
                assertNotNull(target);
                copyAuthorization[0] = target;
            }).expectComplete().verify(DEFAULT_TIMEOUT);

            SyncPoller<DocumentClassifierCopyToOperationDetails, DocumentClassifierDetails> syncCopyPoller
                = client.beginCopyClassifierTo(documentClassifierDetails[0].getClassifierId(), copyAuthorization[0])
                    .setPollInterval(durationTestMode)
                    .getSyncPoller();

            LongRunningOperationStatus copyOperationStatus = syncCopyPoller.poll().getStatus();
            while (!LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(copyOperationStatus)) {
                copyOperationStatus = syncCopyPoller.poll().getStatus();
            }

            DocumentClassifierDetails classifierDetails = syncCopyPoller.getFinalResult();
            assertEquals(copyAuthorization[0].getTargetClassifierId(), classifierDetails.getClassifierId());
            copiedClassifier[0] = classifierDetails;
        } finally {
            StepVerifier.create(client.deleteClassifier(classifierId)).expectComplete().verify(DEFAULT_TIMEOUT);

            if (copiedClassifier[0] != null) {
                StepVerifier.create(client.deleteClassifier(copiedClassifier[0].getClassifierId()))
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);
            }
        }
    }
}
