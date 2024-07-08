// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisServiceVersion;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BlobContentSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BlobFileListContentSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentClassifierOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ClassifierDocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ComposeDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelCopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelSummary;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ResourceDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.models.ResponseError;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.NON_EXIST_MODEL_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DocumentModelAdminClientTest extends DocumentModelAdministrationClientTestBase {
    private DocumentModelAdministrationClient client;
    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .skipRequest((ignored1, ignored2) -> false)
            .assertSync()
            .build();
    }
    private DocumentModelAdministrationClient getDocumentModelAdministrationClient(HttpClient httpClient,
                                                                                   DocumentAnalysisServiceVersion serviceVersion) {
        return getDocumentModelAdminClientBuilder(
            buildSyncAssertingClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion
        )
            .buildClient();
    }

    /**
     * Verifies the form recognizer client is valid.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void getDocumentAnalysisClientAndValidate(HttpClient httpClient,
                                                     DocumentAnalysisServiceVersion serviceVersion) {
        DocumentAnalysisClient documentAnalysisClient = getDocumentModelAdministrationClient(httpClient, serviceVersion)
            .getDocumentAnalysisClient();
        blankPdfDataRunner((data, dataLength) -> {
            SyncPoller<OperationResult, AnalyzeResult> syncPoller =
                documentAnalysisClient.beginAnalyzeDocument("prebuilt-layout",
                    BinaryData.fromStream(data, dataLength))
                        .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            assertNotNull(syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies that an exception is thrown for invalid model ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void getModelNonExistingModelID(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
            client.getDocumentModel(NON_EXIST_MODEL_ID));
        final ResponseError responseError = (ResponseError) exception.getValue();
        assertEquals("NotFound", responseError.getCode());
    }

    /**
     * Verifies custom model info returned with response for a valid model ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void getModelWithResponse(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingDataSasUrl) -> {
            DocumentModelDetails documentModelDetails =
                client.beginBuildDocumentModel(trainingDataSasUrl, DocumentModelBuildMode.TEMPLATE)
                    .setPollInterval(durationTestMode).getFinalResult();
            Response<DocumentModelDetails> documentModelResponse =
                client.getDocumentModelWithResponse(documentModelDetails.getModelId(),
                    Context.NONE);
            client.deleteDocumentModel(documentModelDetails.getModelId());

            assertEquals(documentModelResponse.getStatusCode(), HttpResponseStatus.OK.code());
            validateDocumentModelData(documentModelResponse.getValue());
        });
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void validGetResourceDetails(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        validateResourceInfo(client.getResourceDetails());
    }

    /**
     * Verifies account properties returned with a Http Response for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void validGetResourceDetailsWithResponse(HttpClient httpClient,
                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        Response<ResourceDetails> resourceDetailsResponse = client.getResourceDetailsWithResponse(Context.NONE);
        assertEquals(resourceDetailsResponse.getStatusCode(), HttpResponseStatus.OK.code());
        validateResourceInfo(resourceDetailsResponse.getValue());
    }

    /**
     * Verifies that an exception is thrown for invalid status model ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void deleteModelNonExistingModelID(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
            client.deleteDocumentModel(NON_EXIST_MODEL_ID));
        final ResponseError responseError = (ResponseError) exception.getValue();
        assertEquals("NotFound", responseError.getCode());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void deleteModelValidModelIDWithResponse(HttpClient httpClient,
                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingDataSasUrl) -> {
            SyncPoller<OperationResult, DocumentModelDetails> syncPoller =
                client.beginBuildDocumentModel(trainingDataSasUrl, DocumentModelBuildMode.TEMPLATE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            DocumentModelDetails createdModel = syncPoller.getFinalResult();

            final Response<Void> deleteModelWithResponse
                = client.deleteDocumentModelWithResponse(createdModel.getModelId(), Context.NONE);

            assertEquals(deleteModelWithResponse.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
            final HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
                client.getDocumentModelWithResponse(createdModel.getModelId(), Context.NONE));
            final ResponseError responseError = (ResponseError) exception.getValue();
            assertEquals("NotFound", responseError.getCode());
        });
    }

    /**
     * Test for listing all models information.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void listModels(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        int pageCount = 0;
        for (PagedResponse<DocumentModelSummary> documentModelSummaryPagedResponse : client.listDocumentModels().iterableByPage()) {
            List<DocumentModelSummary> modelInfoList = documentModelSummaryPagedResponse.getValue();
            modelInfoList.forEach(documentModelSummary -> {
                assertNotNull(documentModelSummary.getModelId());
                assertNotNull(documentModelSummary.getCreatedOn());
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
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void listModelsWithContext(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        int pageCount = 0;
        for (PagedResponse<DocumentModelSummary> documentModelSummaryPagedResponse
            : client.listDocumentModels(Context.NONE).iterableByPage()) {
            List<DocumentModelSummary> modelInfoList = documentModelSummaryPagedResponse.getValue();
            modelInfoList.forEach(documentModelSummary -> {
                assertNotNull(documentModelSummary.getModelId());
                assertNotNull(documentModelSummary.getCreatedOn());
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
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void beginCopy(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<OperationResult, DocumentModelDetails> syncPoller =
                client.beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            DocumentModelDetails actualModel = syncPoller.getFinalResult();

            DocumentModelCopyAuthorization target =
                client.getCopyAuthorization();
            SyncPoller<OperationResult, DocumentModelDetails>
                copyPoller = client.beginCopyDocumentModelTo(actualModel.getModelId(), target)
                .setPollInterval(durationTestMode);
            DocumentModelDetails copiedModel = copyPoller.getFinalResult();

            Assertions.assertEquals(target.getTargetModelId(), copiedModel.getModelId());
            client.deleteDocumentModel(actualModel.getModelId());
            client.deleteDocumentModel(copiedModel.getModelId());
        });
    }

    /**
     * Verifies the result of the copy authorization for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void copyAuthorization(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        validateCopyAuthorizationResult(client.getCopyAuthorization());
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and JPG training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void beginBuildModelWithJPGTrainingSet(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller =
                client.beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();

            validateDocumentModelData(buildModelPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and multi-page PDF training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void beginBuildModelWithMultiPagePDFTrainingSet(HttpClient httpClient,
                                                           DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        multipageTrainingRunner(trainingFilesUrl -> {
            SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller =
                client.beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE)
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();

            validateDocumentModelData(buildModelPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model ID and include subfolder training set
     * Url with existing prefix name.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void beginBuildModelFailsWithInvalidPrefix(HttpClient httpClient,
                                                      DocumentAnalysisServiceVersion serviceVersion) {

        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
                client.beginBuildDocumentModel(trainingFilesUrl,
                        DocumentModelBuildMode.TEMPLATE,
                        "invalidPrefix",
                        null,
                        Context.NONE)
                    .setPollInterval(durationTestMode));

            final ResponseError responseError  = (ResponseError) exception.getValue();
            assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model ID and include subfolder training set
     * Url with non-existing prefix name.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void beginBuildModelIncludeSubfolderWithNonExistPrefixName(HttpClient httpClient,
                                                                      DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        multipageTrainingRunner(trainingFilesUrl -> {
            HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
                client.beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE, "subfolders", null, Context.NONE)
                    .setPollInterval(durationTestMode));

            final ResponseError responseError = (ResponseError) exception.getValue();
            assertEquals("InvalidRequest", responseError.getCode());
        });
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and multi-page PDF training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void beginBuildModelWithJsonLTrainingSet(HttpClient httpClient,
                                                           DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        selectionMarkTrainingRunner(trainingFilesUrl -> {
            SyncPoller<OperationResult, DocumentModelDetails> buildModelPoller =
                client.beginBuildDocumentModel(new BlobFileListContentSource(trainingFilesUrl, "filelist.jsonl"),
                        DocumentModelBuildMode.TEMPLATE)
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();

            validateDocumentModelData(buildModelPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the create composed model for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void beginCreateComposedModel(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<OperationResult, DocumentModelDetails> syncPoller1 =
                client.beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE, null,
                        new BuildDocumentModelOptions().setModelId("sync_component_model_1" + UUID.randomUUID()), Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller1.waitForCompletion();
            DocumentModelDetails createdModel1 = syncPoller1.getFinalResult();

            SyncPoller<OperationResult, DocumentModelDetails> syncPoller2 =
                client.beginBuildDocumentModel(trainingFilesUrl, DocumentModelBuildMode.TEMPLATE, null,
                        new BuildDocumentModelOptions().setModelId("sync_component_model_2" + UUID.randomUUID()), Context.NONE)
                    .setPollInterval(durationTestMode);
            syncPoller2.waitForCompletion();
            DocumentModelDetails createdModel2 = syncPoller2.getFinalResult();

            final List<String> modelIDList = Arrays.asList(createdModel1.getModelId(), createdModel2.getModelId());

            DocumentModelDetails composedModel =
                client.beginComposeDocumentModel(modelIDList,
                        new ComposeDocumentModelOptions().setModelId("sync_java_composed_model" + UUID.randomUUID())
                            .setDescription("test desc"),
                        Context.NONE)
                    .setPollInterval(durationTestMode)
                    .getFinalResult();

            assertNotNull(composedModel.getModelId());
            assertEquals("test desc", composedModel.getDescription());
            assertEquals(2, composedModel.getDocumentTypes().size());
            composedModel.getDocumentTypes().forEach((key, documentTypeDetails) -> {
                if (key.contains("sync_component_model_1") || key.contains("sync_component_model_2")) {
                    assert true;
                } else {
                    assert false;
                }
            });
            validateDocumentModelData(composedModel);

            client.deleteDocumentModel(createdModel1.getModelId());
            client.deleteDocumentModel(createdModel2.getModelId());
            client.deleteDocumentModel(composedModel.getModelId());
        });
    }

    /**
     * Verifies the result of the training operation for a classifier with a valid training data set.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void beginBuildClassifier(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap
                = new HashMap<String, ClassifierDocumentTypeDetails>();
            documentTypeDetailsMap.put("IRS-1040-A",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-A/train")
                ));
            documentTypeDetailsMap.put("IRS-1040-B",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-B/train")
                ));
            documentTypeDetailsMap.put("IRS-1040-C",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-C/train")
                ));
            documentTypeDetailsMap.put("IRS-1040-D",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-D/train")
                ));
            documentTypeDetailsMap.put("IRS-1040-E",
                new ClassifierDocumentTypeDetails(new BlobContentSource(trainingFilesUrl).setPrefix("IRS-1040-E/train")
                ));
            SyncPoller<OperationResult, DocumentClassifierDetails> buildModelPoller =
                client.beginBuildDocumentClassifier(documentTypeDetailsMap)
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();
            validateClassifierModelData(documentClassifierDetails);
            documentClassifierDetails.getDocumentTypes().forEach((s, classifierDocumentTypeDetails)
                -> assertNotNull(((BlobContentSource) classifierDocumentTypeDetails.getContentSource())
                .getContainerUrl()));
        });
    }

    /**
     * Verifies the result of the training operation for a classifier with a valid training data set with jsonL files.
     */
    @RecordWithoutRequestBody
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.documentanalysis.TestUtils#getTestParameters")
    public void beginBuildClassifierWithJsonL(HttpClient httpClient,
                                     DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        beginClassifierRunner((trainingFilesUrl) -> {
            Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap
                = new HashMap<String, ClassifierDocumentTypeDetails>();
            documentTypeDetailsMap.put("IRS-1040-A",
                new ClassifierDocumentTypeDetails(new BlobFileListContentSource(trainingFilesUrl, "IRS-1040-A.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-B",
                new ClassifierDocumentTypeDetails(new BlobFileListContentSource(trainingFilesUrl, "IRS-1040-B.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-C",
                new ClassifierDocumentTypeDetails(new BlobFileListContentSource(trainingFilesUrl, "IRS-1040-C.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-D",
                new ClassifierDocumentTypeDetails(new BlobFileListContentSource(trainingFilesUrl, "IRS-1040-D.jsonl")
                ));
            documentTypeDetailsMap.put("IRS-1040-E",
                new ClassifierDocumentTypeDetails(new BlobFileListContentSource(trainingFilesUrl, "IRS-1040-E.jsonl")
                ));
            SyncPoller<OperationResult, DocumentClassifierDetails> buildModelPoller =
                client.beginBuildDocumentClassifier(documentTypeDetailsMap,
                        new BuildDocumentClassifierOptions().setDescription("Json L classifier model"), Context.NONE)
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();
            DocumentClassifierDetails documentClassifierDetails = buildModelPoller.getFinalResult();

            documentClassifierDetails.getDocumentTypes().forEach((s, classifierDocumentTypeDetails)
                -> assertNotNull(((BlobFileListContentSource) classifierDocumentTypeDetails.getContentSource())
                .getContainerUrl()));

            validateClassifierModelData(documentClassifierDetails);
        });
    }
}
