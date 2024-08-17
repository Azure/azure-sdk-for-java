// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AuthorizeClassifierCopyRequest;
import com.azure.ai.documentintelligence.models.AuthorizeCopyRequest;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.AzureBlobFileListContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentClassifierRequest;
import com.azure.ai.documentintelligence.models.BuildDocumentModelRequest;
import com.azure.ai.documentintelligence.models.ClassifierCopyAuthorization;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
import com.azure.ai.documentintelligence.models.ComposeDocumentModelRequest;
import com.azure.ai.documentintelligence.models.CopyAuthorization;
import com.azure.ai.documentintelligence.models.DocumentBuildMode;
import com.azure.ai.documentintelligence.models.DocumentClassifierBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentClassifierCopyToOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentClassifierDetails;
import com.azure.ai.documentintelligence.models.DocumentModelBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelComposeOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelCopyToOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelDetails;
import com.azure.ai.documentintelligence.models.DocumentTypeDetails;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DocumentModelAdministrationAsyncClientTest extends DocumentAdministrationClientTestBase {
    private DocumentIntelligenceAdministrationAsyncClient client;

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest((ignored1, ignored2) -> false)
            .assertAsync()
            .build();
    }
    private DocumentIntelligenceAdministrationAsyncClient getModelAdminAsyncClient(HttpClient httpClient,
                                                                                   DocumentIntelligenceServiceVersion serviceVersion) {
        return getModelAdminClientBuilder(
            buildAsyncAssertingClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient()
                : httpClient),
            serviceVersion
        )
            .buildAsyncClient();
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void validGetResourceInfo(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getResourceInfo())
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
        StepVerifier.create(client.getResourceInfo())
            .assertNext(DocumentAdministrationClientTestBase::validateResourceInfo)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void deleteModelValidModelIdWithResponse(HttpClient httpClient,
                                                    DocumentIntelligenceServiceVersion serviceVersion) {

        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller1 =
                client.beginBuildDocumentModel(new BuildDocumentModelRequest("modelId" + UUID.randomUUID(), DocumentBuildMode.TEMPLATE).setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
                    .setPollInterval(durationTestMode).getSyncPoller();
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
        String modelId = "java_copy_model_test";
        StepVerifier.create(client.authorizeModelCopyWithResponse(BinaryData.fromObject(new AuthorizeCopyRequest(modelId)), null))
            .assertNext(response -> validateCopyAuthorizationResult(response.getValue().toObject(CopyAuthorization.class)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(client.deleteModel(modelId))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies the result of the create composed model for valid parameters.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginCreateComposedModel(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {

        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String composedModelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "composedModelId" + UUID.randomUUID();
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        Map<String, DocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
        documentTypeDetailsMap.put("IRS-1040-A",
            new DocumentTypeDetails());
        documentTypeDetailsMap.put("IRS-1040-B",
            new DocumentTypeDetails().setModelId("modelId" + UUID.randomUUID()));
        documentTypeDetailsMap.put("IRS-1040-C",
            new DocumentTypeDetails().setModelId("modelId" + UUID.randomUUID()));

        documentTypeDetailsMap.put("IRS-1040-D",
            new DocumentTypeDetails().setModelId("modelId" + UUID.randomUUID()));

        documentTypeDetailsMap.put("IRS-1040-E",
            new DocumentTypeDetails().setModelId("modelId" + UUID.randomUUID()));

        DocumentModelDetails composedModel = client.beginComposeModel(new ComposeDocumentModelRequest(composedModelId, classifierId, documentTypeDetailsMap).setDescription("test desc"))
            .setPollInterval(durationTestMode)
            .getSyncPoller().getFinalResult();

        assertNotNull(composedModel.getModelId());
        assertEquals("test desc", composedModel.getDescription());
        assertEquals(2, composedModel.getDocTypes().size());
        validateDocumentModelData(composedModel);

        client.deleteModel(composedModel.getModelId()).block();
    }

    /**
     * Verifies the result of building a document analysis model.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginBuildModel(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller1 =
                client.beginBuildDocumentModel(new BuildDocumentModelRequest(modelId, DocumentBuildMode.TEMPLATE).setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
                    .setPollInterval(durationTestMode).getSyncPoller();
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
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginCopy(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller1 =
                client.beginBuildDocumentModel(new BuildDocumentModelRequest(modelId, DocumentBuildMode.TEMPLATE).setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl)))
                    .setPollInterval(durationTestMode).getSyncPoller();
            syncPoller1.waitForCompletion();
            DocumentModelDetails actualModel = syncPoller1.getFinalResult();

            Mono<CopyAuthorization> targetMono = client.authorizeModelCopy(new AuthorizeCopyRequest("copyModelId" + UUID.randomUUID()));
            CopyAuthorization target = targetMono.block();
            if (actualModel == null) {
                fail();
                return;
            }

            PollerFlux<DocumentModelCopyToOperationDetails, DocumentModelDetails> copyPoller =
                client.beginCopyModelTo(actualModel.getModelId(), target).setPollInterval(durationTestMode);
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
    @Disabled
    public void listModels(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.listModels().byPage().take(4))
            .thenConsumeWhile(documentModelInfoPagedResponse -> {
                documentModelInfoPagedResponse.getValue()
                    .forEach(documentModelInfo -> {
                        assertNotNull(documentModelInfo.getModelId());
                        assertNotNull(documentModelInfo.getCreatedDateTime());
                    });
                return true;
            }).expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }


    /**
     * Test for listing all operations' information.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled
    public void listOperations(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {

        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        List<String> operationIdList = new ArrayList<>();
        StepVerifier.create(client.listOperations().byPage().take(4))
            .thenConsumeWhile(modelOperationInfoPagedResponse ->          {
                modelOperationInfoPagedResponse.getValue().forEach(modelOperationInfo -> {
                    operationIdList.add(modelOperationInfo.getOperationId());
                    assertTrue(modelOperationInfo.getOperationId() != null
                        && modelOperationInfo.getStatus() != null
                        && modelOperationInfo.getCreatedDateTime() != null
                        && modelOperationInfo.getLastUpdatedDateTime() != null
                        && modelOperationInfo.getResourceLocation() != null
                        && modelOperationInfo.getPercentCompleted() != null);
                });
                return true;
            })
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        if (!CoreUtils.isNullOrEmpty(operationIdList)) {
            operationIdList.forEach(operationId -> StepVerifier.create(client.getOperation(operationId))
                .assertNext(operationDetails -> {
                    assertNotNull(operationDetails.getOperationId());
                    assertNotNull(operationDetails.getCreatedDateTime());
                    if (operationDetails instanceof DocumentModelBuildOperationDetails) {
                        assertNotNull(((DocumentModelBuildOperationDetails) operationDetails).getResult());
                    } else if (operationDetails instanceof DocumentModelComposeOperationDetails) {
                        assertNotNull(((DocumentModelComposeOperationDetails) operationDetails).getResult());
                    } else if (operationDetails instanceof DocumentModelCopyToOperationDetails) {
                        assertNotNull(((DocumentModelCopyToOperationDetails) operationDetails).getResult());
                    }
                })
                .expectComplete()
                .verify(DEFAULT_TIMEOUT));
        }
    }

    @RecordWithoutRequestBody
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginBuildClassifier(HttpClient httpClient,
                                     DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A",
                new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-A/train")
                ));
            documentTypeDetailsMap.put("IRS-1040-B",
                new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-B/train")
                ));
            documentTypeDetailsMap.put("IRS-1040-C",
                new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-C/train")
                ));
            documentTypeDetailsMap.put("IRS-1040-D",
                new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-D/train")
                ));
            documentTypeDetailsMap.put("IRS-1040-E",
                new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-E/train")
                ));
            SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller =
                client.beginBuildClassifier(new BuildDocumentClassifierRequest(classifierId, documentTypeDetailsMap))
                    .setPollInterval(durationTestMode).getSyncPoller();

            DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();
            validateClassifierModelData(buildModelPoller.getFinalResult());
            assertNotNull(documentClassifierDetails.getDocTypes());
            documentClassifierDetails.getDocTypes().forEach((s, classifierDocumentTypeDetails)
                -> assertNotNull((classifierDocumentTypeDetails.getAzureBlobSource())
                .getContainerUrl()));
        });
    }

    /**
     * Verifies the result of the training operation for a classifier with a valid training data set with jsonL files.
     */
    @RecordWithoutRequestBody
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginBuildClassifierWithJsonL(HttpClient httpClient,
                                              DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A",
                new ClassifierDocumentTypeDetails().setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-A.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-B",
                new ClassifierDocumentTypeDetails().setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-B.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-C",
                new ClassifierDocumentTypeDetails().setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-C.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-D",
                new ClassifierDocumentTypeDetails().setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-D.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-E",
                new ClassifierDocumentTypeDetails().setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-E.jsonl")
                ));
            SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller =
                client.beginBuildClassifier(new BuildDocumentClassifierRequest(classifierId, documentTypeDetailsMap))
                    .setPollInterval(durationTestMode).getSyncPoller();
            DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();
            assertNotNull(documentClassifierDetails.getDocTypes());
            documentClassifierDetails.getDocTypes().forEach((s, classifierDocumentTypeDetails)
                -> assertNotNull((classifierDocumentTypeDetails.getAzureBlobFileListSource())
                .getContainerUrl()));

            validateClassifierModelData(buildModelPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the copy authorization for classifier with valid parameters.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("This test is disabled due to a service error requiring modelId instead of classifierId for copy authorization for classifiers.")
    public void copyAuthorizationClassifier(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String classifierId = "java_copy_classifier_test";
        StepVerifier.create(client.authorizeClassifierCopyWithResponse(BinaryData.fromObject(new AuthorizeClassifierCopyRequest(classifierId).setDescription("test description")), null))
            .assertNext(response -> validateClassifierCopyAuthorizationResult(response.getValue().toObject(ClassifierCopyAuthorization.class)))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        StepVerifier.create(client.deleteModel(classifierId))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * Verifies the result of the copy classifier operation for valid parameters.
     */
    @ParameterizedTest(name = TestUtils.DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("This test is disabled due to a service error requiring modelId instead of classifierId for copy authorization for classifiers.")
    public void beginCopyClassifier(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdminAsyncClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A",
                new ClassifierDocumentTypeDetails().setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-A.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-B",
                new ClassifierDocumentTypeDetails().setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-B.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-C",
                new ClassifierDocumentTypeDetails().setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-C.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-D",
                new ClassifierDocumentTypeDetails().setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-D.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-E",
                new ClassifierDocumentTypeDetails().setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "IRS-1040-E.jsonl")
                ));
            SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller =
                client.beginBuildClassifier(new BuildDocumentClassifierRequest(classifierId, documentTypeDetailsMap))
                    .setPollInterval(durationTestMode).getSyncPoller();
            DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();

            Mono<ClassifierCopyAuthorization> targetMono = client.authorizeClassifierCopy(new AuthorizeClassifierCopyRequest("copyClassifierId" + UUID.randomUUID()));
            ClassifierCopyAuthorization target = targetMono.block();
            if (documentClassifierDetails == null) {
                fail();
                return;
            }

            PollerFlux<DocumentClassifierCopyToOperationDetails, DocumentClassifierDetails> copyPoller =
                client.beginCopyClassifierTo(documentClassifierDetails.getClassifierId(), target).setPollInterval(durationTestMode);
            DocumentClassifierDetails copiedClassifier = copyPoller.getSyncPoller().getFinalResult();
            Assertions.assertEquals(target.getTargetClassifierId(), copiedClassifier.getClassifierId());

            client.deleteModel(documentClassifierDetails.getClassifierId()).block();
            client.deleteModel(copiedClassifier.getClassifierId()).block();
        });
    }

}
