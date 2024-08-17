// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AuthorizeCopyRequest;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.AzureBlobFileListContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentClassifierRequest;
import com.azure.ai.documentintelligence.models.BuildDocumentModelRequest;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
import com.azure.ai.documentintelligence.models.ComposeDocumentModelRequest;
import com.azure.ai.documentintelligence.models.CopyAuthorization;
import com.azure.ai.documentintelligence.models.DocumentBuildMode;
import com.azure.ai.documentintelligence.models.DocumentClassifierBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentClassifierDetails;
import com.azure.ai.documentintelligence.models.DocumentModelBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelCopyToOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelDetails;
import com.azure.ai.documentintelligence.models.DocumentTypeDetails;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.azure.ai.documentintelligence.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DocumentModelAdministrationClientTest extends DocumentAdministrationClientTestBase {
    private DocumentIntelligenceAdministrationClient client;

    private DocumentIntelligenceAdministrationClient getModelAdministrationClient(HttpClient httpClient,
                                                                                  DocumentIntelligenceServiceVersion serviceVersion) {
        return getModelAdminClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient,
            serviceVersion
        )
            .buildClient();
    }


    /**
     * Verifies custom model info returned with response for a valid model ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void getModelWithResponse(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        buildModelRunner((trainingDataSasUrl) -> {
            DocumentModelDetails documentModelDetails =
                client.beginBuildDocumentModel(new BuildDocumentModelRequest(modelId, DocumentBuildMode.TEMPLATE).setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                    .setPollInterval(durationTestMode).getFinalResult();
            Response<BinaryData> documentModelResponse =
                client.getModelWithResponse(documentModelDetails.getModelId(), null);
            client.deleteModel(documentModelDetails.getModelId());

            assertEquals(documentModelResponse.getStatusCode(), HttpResponseStatus.OK.code());
        });
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void validGetResourceDetails(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        validateResourceInfo(client.getResourceInfo());
    }

    /**
     * Verifies account properties returned with a Http Response for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void validGetResourceDetailsWithResponse(HttpClient httpClient,
                                                    DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        Response<BinaryData> resourceDetailsResponse = client.getResourceInfoWithResponse(null);
        assertEquals(resourceDetailsResponse.getStatusCode(), HttpResponseStatus.OK.code());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void deleteModelValidModelIDWithResponse(HttpClient httpClient,
                                                    DocumentIntelligenceServiceVersion serviceVersion) {
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        client = getModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingDataSasUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller =
                client.beginBuildDocumentModel(new BuildDocumentModelRequest(modelId, DocumentBuildMode.TEMPLATE).setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            DocumentModelDetails createdModel = syncPoller.getFinalResult();

            final Response<Void> deleteModelWithResponse
                = client.deleteModelWithResponse(createdModel.getModelId(), null);

            assertEquals(deleteModelWithResponse.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
        });
    }

    /**
     * Test for listing all models information.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    public void listModels(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        int pageCount = 0;
        for (PagedResponse<DocumentModelDetails> documentModelDetailsPagedResponse : client.listModels().iterableByPage()) {
            List<DocumentModelDetails> modelInfoList = documentModelDetailsPagedResponse.getValue();
            modelInfoList.forEach(DocumentModelDetails -> {
                assertNotNull(DocumentModelDetails.getModelId());
                assertNotNull(DocumentModelDetails.getCreatedDateTime());
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
        for (PagedResponse<BinaryData> documentModelDetailsPagedResponse
            : client.listModels(null).iterableByPage()) {
            List<BinaryData> modelInfoList = documentModelDetailsPagedResponse.getValue();
            modelInfoList.forEach(modelInfo -> {
                assertNotNull(modelInfo.toObject(DocumentModelDetails.class).getModelId());
                assertNotNull(modelInfo.toObject(DocumentModelDetails.class).getCreatedDateTime());
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
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginCopy(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        buildModelRunner((trainingDataSasUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> syncPoller =
                client.beginBuildDocumentModel(new BuildDocumentModelRequest(modelId, DocumentBuildMode.TEMPLATE).setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            DocumentModelDetails actualModel = syncPoller.getFinalResult();

            CopyAuthorization target =
                client.authorizeModelCopy(new AuthorizeCopyRequest("copyModelId" + UUID.randomUUID()).setTags(actualModel.getTags()).setDescription(actualModel.getDescription()));

            SyncPoller<DocumentModelCopyToOperationDetails, DocumentModelDetails>
                copyPoller = client.beginCopyModelTo(actualModel.getModelId(), target)
                .setPollInterval(durationTestMode);
            DocumentModelDetails copiedModel = copyPoller.getFinalResult();

            Assertions.assertEquals(target.getTargetModelId(), copiedModel.getModelId());
            client.deleteModel(actualModel.getModelId());
            client.deleteModel(copiedModel.getModelId());
        });
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and JPG training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginBuildModelWithJPGTrainingSet(HttpClient httpClient,
                                                  DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        buildModelRunner((trainingDataSasUrl) -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildModelPoller =
                client.beginBuildDocumentModel(new BuildDocumentModelRequest(modelId, DocumentBuildMode.TEMPLATE).setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();

            validateDocumentModelData(buildModelPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and multi-page PDF training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginBuildModelWithMultiPagePDFTrainingSet(HttpClient httpClient,
                                                           DocumentIntelligenceServiceVersion serviceVersion) {
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        client = getModelAdministrationClient(httpClient, serviceVersion);
        multipageTrainingRunner(trainingDataSasUrl -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildModelPoller =
                client.beginBuildDocumentModel(new BuildDocumentModelRequest(modelId, DocumentBuildMode.TEMPLATE).setAzureBlobSource(new AzureBlobContentSource(trainingDataSasUrl)))
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();

            validateDocumentModelData(buildModelPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and multi-page PDF training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginBuildModelWithJsonLTrainingSet(HttpClient httpClient,
                                                           DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String modelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "modelId" + UUID.randomUUID();
        selectionMarkTrainingRunner(trainingFilesUrl -> {
            SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildModelPoller =
                client.beginBuildDocumentModel(new BuildDocumentModelRequest(modelId,
                    DocumentBuildMode.TEMPLATE).setAzureBlobFileListSource(new AzureBlobFileListContentSource(trainingFilesUrl, "filelist.jsonl")))
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();

            validateDocumentModelData(buildModelPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the create composed model for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginCreateComposedModel(HttpClient httpClient, DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String composedModelId = interceptorManager.isPlaybackMode() ? "REDACTED" : "composedModelId" + UUID.randomUUID();
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        Map<String, DocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
        documentTypeDetailsMap.put("IRS-1040-A",
            new DocumentTypeDetails().setModelId("modelId" + UUID.randomUUID()));
        documentTypeDetailsMap.put("IRS-1040-B",
            new DocumentTypeDetails().setModelId("modelId" + UUID.randomUUID()));
        documentTypeDetailsMap.put("IRS-1040-C",
            new DocumentTypeDetails().setModelId("modelId" + UUID.randomUUID()));

        documentTypeDetailsMap.put("IRS-1040-D",
            new DocumentTypeDetails().setModelId("modelId" + UUID.randomUUID()));

        documentTypeDetailsMap.put("IRS-1040-E",
            new DocumentTypeDetails().setModelId("modelId" + UUID.randomUUID()));

        DocumentModelDetails composedModel = client.beginComposeModel(new ComposeDocumentModelRequest(composedModelId, classifierId, documentTypeDetailsMap).setDescription("test desc"))
            .setPollInterval(durationTestMode).getFinalResult();

        assertNotNull(composedModel.getModelId());
        assertEquals("test desc", composedModel.getDescription());
        assertEquals(2, composedModel.getDocTypes().size());
        validateDocumentModelData(composedModel);

        client.deleteModel(composedModel.getModelId());
    }

    /**
     * Verifies the result of the training operation for a classifier with a valid training data set.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginBuildClassifier(HttpClient httpClient,
                                                  DocumentIntelligenceServiceVersion serviceVersion) {
        client = getModelAdministrationClient(httpClient, serviceVersion);
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap
                = new HashMap<String, ClassifierDocumentTypeDetails>();
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
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();
            validateClassifierModelData(documentClassifierDetails);
            documentClassifierDetails.getDocTypes().forEach((s, classifierDocumentTypeDetails)
                -> assertNotNull(classifierDocumentTypeDetails.getAzureBlobSource().getContainerUrl()));
        });
    }

    /**
     * Verifies the result of the training operation for a classifier with a valid training data set with jsonL files.
     */
    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.documentintelligence.TestUtils#getTestParameters")
    @Disabled("https://github.com/Azure/azure-sdk-for-java/issues/41027")
    public void beginBuildClassifierWithJsonL(HttpClient httpClient,
                                     DocumentIntelligenceServiceVersion serviceVersion) {
        String classifierId = interceptorManager.isPlaybackMode() ? "REDACTED" : "classifierId" + UUID.randomUUID();
        client = getModelAdministrationClient(httpClient, serviceVersion);
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap
                = new HashMap<String, ClassifierDocumentTypeDetails>();
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
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();

            documentClassifierDetails.getDocTypes().forEach((s, classifierDocumentTypeDetails)
                -> assertNotNull(classifierDocumentTypeDetails.getAzureBlobFileListSource()
                .getContainerUrl()));

            validateClassifierModelData(documentClassifierDetails);
        });
    }
}
