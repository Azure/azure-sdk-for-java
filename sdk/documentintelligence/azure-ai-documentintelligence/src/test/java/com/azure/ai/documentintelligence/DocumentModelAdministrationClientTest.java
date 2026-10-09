// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AuthorizeModelCopyOptions;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.AzureBlobFileListContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentClassifierOptions;
import com.azure.ai.documentintelligence.models.BuildDocumentModelOptions;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
import com.azure.ai.documentintelligence.models.ComposeDocumentModelOptions;
import com.azure.ai.documentintelligence.models.DocumentBuildMode;
import com.azure.ai.documentintelligence.models.DocumentClassifierBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentClassifierDetails;
import com.azure.ai.documentintelligence.models.DocumentModelBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelCopyToOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelDetails;
import com.azure.ai.documentintelligence.models.DocumentTypeDetails;
import com.azure.ai.documentintelligence.models.ModelCopyAuthorization;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.azure.ai.documentintelligence.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DocumentModelAdministrationClientTest extends DocumentAdministrationClientTestBase {
    private DocumentIntelligenceAdministrationClient client;
    private final List<String> modelIdsToDelete = new ArrayList<>();

    private DocumentIntelligenceAdministrationClient getModelAdministrationClient(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        return getModelAdminClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, serviceVersion)
                .buildClient();
    }

    @AfterEach
    public void cleanupModels() {
        for (String modelId : modelIdsToDelete) {
            client.deleteModel(modelId);
        }
    }

    /**
     * Verifies custom model info returned with response for a valid model ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void getModelWithResponse(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        String trainingDataSasUrl = getTrainingFilesContainerUrl();
        DocumentModelDetails documentModelDetails = client
            .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId, DocumentBuildMode.TEMPLATE)
                .setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
            .setPollInterval(durationTestMode)
            .getFinalResult();
        modelIdsToDelete.add(documentModelDetails.getModelId());
        Response<BinaryData> documentModelResponse
            = client.getModelWithResponse(documentModelDetails.getModelId(), null);

        assertEquals(documentModelResponse.getStatusCode(), HttpResponseStatus.OK.code());
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void validGetResourceDetails(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        validateResourceInfo(client.getResourceDetails());
    }

    /**
     * Verifies account properties returned with a Http Response for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void validGetResourceDetailsWithResponse(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        Response<BinaryData> resourceDetailsResponse = client.getResourceDetailsWithResponse(null);
        assertEquals(resourceDetailsResponse.getStatusCode(), HttpResponseStatus.OK.code());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void deleteModelValidModelIDWithResponse(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String trainingDataSasUrl = getTrainingFilesContainerUrl();
        SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller
            = client
                .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId, DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        DocumentModelDetails createdModel = syncPoller.getFinalResult();

        final Response<Void> deleteModelWithResponse = client.deleteModelWithResponse(createdModel.getModelId(), null);

        assertEquals(deleteModelWithResponse.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
    }

    /**
     * Test for listing all models information.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void listModels(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        int pageCount = 0;
        for (PagedResponse<DocumentModelDetails> documentModelDetailsPagedResponse : client.listModels()
            .iterableByPage()) {
            List<DocumentModelDetails> modelInfoList = documentModelDetailsPagedResponse.getValue();
            modelInfoList.forEach(DocumentModelDetails -> {
                assertNotNull(DocumentModelDetails.getModelId());
                assertNotNull(DocumentModelDetails.getCreatedOn());
            });
            pageCount++;
            if (pageCount > 4) {
                // Stop after 4 pages since there can be large number of models.
                break;
            }
        }
    }

    /**
     * Test for listing all models information with {@link Context}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void listModelsWithContext(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        int pageCount = 0;
        for (PagedResponse<BinaryData> documentModelDetailsPagedResponse : client.listModels(null).iterableByPage()) {
            List<BinaryData> modelInfoList = documentModelDetailsPagedResponse.getValue();
            modelInfoList.forEach(modelInfo -> {
                DocumentModelDetails modelDetails = modelInfo.toObject(DocumentModelDetails.class);
                assertNotNull(modelDetails.getModelId());
                assertNotNull(modelDetails.getCreatedOn());
            });
            pageCount++;
            if (pageCount > 4) {
                // Stop after 4 pages since there can be large number of models.
                break;
            }
        }
    }

    /**
     * Verifies the result of the copy operation for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginCopy(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        String trainingDataSasUrl = getTrainingFilesContainerUrl();
        SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller
            = client
                .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId, DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                .setPollInterval(durationTestMode);
        syncPoller.waitForCompletion();
        DocumentModelDetails actualModel = syncPoller.getFinalResult();
        modelIdsToDelete.add(actualModel.getModelId());

        ModelCopyAuthorization target = client.authorizeModelCopy(
            new AuthorizeModelCopyOptions("copyModelId" + UUID.randomUUID()).setTags(actualModel.getTags())
                .setDescription(actualModel.getDescription()));

        SyncPoller<DocumentModelCopyToOperationDetails, DocumentModelDetails> copyPoller
            = client.beginCopyModelTo(actualModel.getModelId(), target).setPollInterval(durationTestMode);
        DocumentModelDetails copiedModel = copyPoller.getFinalResult();
        modelIdsToDelete.add(copiedModel.getModelId());

        Assertions.assertEquals(target.getTargetModelId(), copiedModel.getModelId());
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and JPG training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginBuildModelWithJPGTrainingSet(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        String trainingDataSasUrl = getTrainingFilesContainerUrl();
        SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildModelPoller
            = client
                .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId, DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                .setPollInterval(durationTestMode);
        buildModelPoller.waitForCompletion();

        validateDocumentModelData(buildModelPoller.getFinalResult());
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and multi-page PDF training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginBuildModelWithMultiPagePDFTrainingSet(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String trainingDataSasUrl = getMultipageTrainingSasUri();
        SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildModelPoller
            = client
                .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId, DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                .setPollInterval(durationTestMode);
        buildModelPoller.waitForCompletion();

        validateDocumentModelData(buildModelPoller.getFinalResult());
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and multi-page PDF training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginBuildModelWithJsonLTrainingSet(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        String trainingFilesUrl = getSelectionMarkTrainingSasUri();
        SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildModelPoller = client
            .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId, DocumentBuildMode.TEMPLATE)
                .setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "filelist.jsonl")))
            .setPollInterval(durationTestMode);
        buildModelPoller.waitForCompletion();

        validateDocumentModelData(buildModelPoller.getFinalResult());
    }

    /**
     * Verifies the result of the creation of a composed model for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginCreateComposedModel(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String modelId1 = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId1" + UUID.randomUUID();
        String modelId2 = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId2" + UUID.randomUUID();
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();

        try {
            String trainingDataSasUrl = getTrainingFilesContainerUrl();
            client
                .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId1, DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                .setPollInterval(durationTestMode)
                .getFinalResult();

            client
                .beginBuildDocumentModel(new BuildDocumentModelOptions(modelId2, DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                .setPollInterval(durationTestMode)
                .getFinalResult();

            String trainingFilesUrl = getClassifierTrainingFilesContainerUrl();
            Map<String, ClassifierDocumentTypeDetails> documentTypes = new HashMap<>();
            documentTypes.put("IRS-1040-A", createBlobContentSource(trainingFilesUrl, "IRS-1040-A/train"));
            documentTypes.put("IRS-1040-B", createBlobContentSource(trainingFilesUrl, "IRS-1040-B/train"));
            documentTypes.put("IRS-1040-C", createBlobContentSource(trainingFilesUrl, "IRS-1040-C/train"));
            documentTypes.put("IRS-1040-D", createBlobContentSource(trainingFilesUrl, "IRS-1040-D/train"));
            documentTypes.put("IRS-1040-E", createBlobContentSource(trainingFilesUrl, "IRS-1040-E/train"));
            SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildClassifier
                = client.beginBuildClassifier(new BuildDocumentClassifierOptions(classifierId, documentTypes))
                    .setPollInterval(durationTestMode);
            buildClassifier.getFinalResult();

            String composedModelId
                = interceptorManager.isPlaybackMode() ? "REDACTED" : "composedModelId" + UUID.randomUUID();
            Map<String, DocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
            documentTypeDetailsMap.put("IRS-1040-A", new DocumentTypeDetails().setModelId(modelId1));
            documentTypeDetailsMap.put("IRS-1040-B", new DocumentTypeDetails().setModelId(modelId2));

            DocumentModelDetails composedModel
                = client
                    .beginComposeModel(
                        new ComposeDocumentModelOptions(composedModelId, classifierId, documentTypeDetailsMap)
                            .setDescription("test desc"))
                    .setPollInterval(durationTestMode)
                    .getFinalResult();

            assertNotNull(composedModel.getModelId());
            assertEquals("test desc", composedModel.getDescription());
            assertEquals(2, composedModel.getDocumentTypes().size());
            validateDocumentModelData(composedModel);

            client.deleteModel(composedModel.getModelId());
        } finally {
            client.deleteModel(modelId1);
            client.deleteModel(modelId2);
        }
    }

    /**
     * Verifies the result of the training operation for a classifier with a valid training data set.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginBuildClassifier(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        String trainingFilesUrl = getClassifierTrainingFilesContainerUrl();
        Map<String, ClassifierDocumentTypeDetails> documentTypes = new HashMap<>();
        documentTypes.put("IRS-1040-A", createBlobContentSource(trainingFilesUrl, "IRS-1040-A/train"));
        documentTypes.put("IRS-1040-B", createBlobContentSource(trainingFilesUrl, "IRS-1040-B/train"));
        documentTypes.put("IRS-1040-C", createBlobContentSource(trainingFilesUrl, "IRS-1040-C/train"));
        documentTypes.put("IRS-1040-D", createBlobContentSource(trainingFilesUrl, "IRS-1040-D/train"));
        documentTypes.put("IRS-1040-E", createBlobContentSource(trainingFilesUrl, "IRS-1040-E/train"));
        SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller
            = client.beginBuildClassifier(new BuildDocumentClassifierOptions(classifierId, documentTypes))
                .setPollInterval(durationTestMode);
        buildModelPoller.waitForCompletion();
        DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();
        validateClassifierModelData(documentClassifierDetails);
        documentClassifierDetails.getDocumentTypes()
            .forEach((s, classifierDocumentTypeDetails) -> assertNotNull(
                classifierDocumentTypeDetails.getAzureBlobSource().getContainerUrl()));
    }

    /**
     * Verifies the result of the training operation for a classifier with a valid training data set with jsonL files.
     */
    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void beginBuildClassifierWithJsonL(HttpClient httpClient,
        DocumentIntelligenceServiceVersion serviceVersion) {
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String trainingFilesUrl = getClassifierTrainingFilesContainerUrl();
        Map<String, ClassifierDocumentTypeDetails> documentTypes = new HashMap<>();
        documentTypes.put("IRS-1040-A", createBlobFileListContentSource(trainingFilesUrl, "IRS-1040-A.jsonl"));
        documentTypes.put("IRS-1040-B", createBlobFileListContentSource(trainingFilesUrl, "IRS-1040-B.jsonl"));
        documentTypes.put("IRS-1040-C", createBlobFileListContentSource(trainingFilesUrl, "IRS-1040-C.jsonl"));
        documentTypes.put("IRS-1040-D", createBlobFileListContentSource(trainingFilesUrl, "IRS-1040-D.jsonl"));
        documentTypes.put("IRS-1040-E", createBlobFileListContentSource(trainingFilesUrl, "IRS-1040-E.jsonl"));
        SyncPoller<DocumentClassifierBuildOperationDetails, DocumentClassifierDetails> buildModelPoller
            = client.beginBuildClassifier(new BuildDocumentClassifierOptions(classifierId, documentTypes))
                .setPollInterval(durationTestMode);
        buildModelPoller.waitForCompletion();
        DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();

        documentClassifierDetails.getDocumentTypes()
            .forEach((s, classifierDocumentTypeDetails) -> assertNotNull(
                classifierDocumentTypeDetails.getAzureBlobFileListSource().getContainerUrl()));

        validateClassifierModelData(documentClassifierDetails);
    }
}
