// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.DocumentAnalysisServiceVersion;
import com.azure.ai.formrecognizer.administration.models.AccountProperties;
import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.CreateComposedModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;
import com.azure.ai.formrecognizer.administration.models.FormRecognizerError;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static com.azure.ai.formrecognizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.formrecognizer.TestUtils.NON_EXIST_MODEL_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DocumentModelAdministrationClientTest extends DocumentModelAdministrationClientTestBase {
    private DocumentModelAdministrationClient client;

    private DocumentModelAdministrationClient getDocumentModelAdministrationClient(HttpClient httpClient,
                                                                                   DocumentAnalysisServiceVersion serviceVersion) {
        return getDocumentModelAdminClientBuilder(httpClient, serviceVersion).buildClient();
    }

    /**
     * Verifies the form recognizer client is valid.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getDocumentAnalysisClientAndValidate(HttpClient httpClient,
                                                     DocumentAnalysisServiceVersion serviceVersion) {
        DocumentAnalysisClient documentAnalysisClient = getDocumentModelAdministrationClient(httpClient, serviceVersion)
            .getDocumentAnalysisClient();
        blankPdfDataRunner((data, dataLength) -> {
            SyncPoller<DocumentOperationResult, AnalyzeResult> syncPoller =
                documentAnalysisClient.beginAnalyzeDocument("prebuilt-layout", data, dataLength)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            assertNotNull(syncPoller.getFinalResult());
        });
    }

    /**
     * Verifies that an exception is thrown for null model ID parameter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getModelNullModelID(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        assertThrows(IllegalArgumentException.class, () -> client.getModel(null));
    }

    /**
     * Verifies that an exception is thrown for invalid model ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getModelNonExistingModelID(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
            client.getModel(NON_EXIST_MODEL_ID));
        final FormRecognizerError errorInformation =
            (FormRecognizerError) exception.getValue();
        assertEquals("ModelNotFound", errorInformation.getInnerError().getCode());
    }

    /**
     * Verifies custom model info returned with response for a valid model ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void getModelWithResponse(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingDataSasUrl) -> {
            DocumentModel documentModel =
                client.beginBuildModel(trainingDataSasUrl, null)
                    .setPollInterval(durationTestMode).getFinalResult();
            Response<DocumentModel> documentModelResponse =
                client.getModelWithResponse(documentModel.getModelId(),
                    Context.NONE);
            client.deleteModel(documentModel.getModelId());

            assertEquals(documentModelResponse.getStatusCode(), HttpResponseStatus.OK.code());
            validateDocumentModelData(documentModelResponse.getValue());
        });
    }

    /**
     * Verifies account properties returned for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void validGetAccountProperties(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        validateAccountProperties(client.getAccountProperties());
    }

    /**
     * Verifies account properties returned with an Http Response for a subscription account.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void validGetAccountPropertiesWithResponse(HttpClient httpClient,
                                                      DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        Response<AccountProperties> accountPropertiesResponse = client.getAccountPropertiesWithResponse(Context.NONE);
        assertEquals(accountPropertiesResponse.getStatusCode(), HttpResponseStatus.OK.code());
        validateAccountProperties(accountPropertiesResponse.getValue());
    }

    /**
     * Verifies that an exception is thrown for invalid status model ID.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void deleteModelNonExistingModelID(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
            client.deleteModel(NON_EXIST_MODEL_ID));
        final FormRecognizerError errorInformation =
            (FormRecognizerError) exception.getValue();
        assertEquals("ModelNotFound", errorInformation.getInnerError().getCode());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void deleteModelValidModelIDWithResponse(HttpClient httpClient,
                                                    DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingDataSasUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller =
                client.beginBuildModel(trainingDataSasUrl, null)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            DocumentModel createdModel = syncPoller.getFinalResult();

            final Response<Void> deleteModelWithResponse
                = client.deleteModelWithResponse(createdModel.getModelId(), Context.NONE);

            assertEquals(deleteModelWithResponse.getStatusCode(), HttpResponseStatus.NO_CONTENT.code());
            final HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
                client.getModelWithResponse(createdModel.getModelId(), Context.NONE));
            final FormRecognizerError errorInformation =
                (FormRecognizerError) exception.getValue();
            assertEquals("ModelNotFound", errorInformation.getInnerError().getCode());
        });
    }

    /**
     * Test for listing all models information.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void listModels(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        int pageCount = 0;
        for (PagedResponse<DocumentModelInfo> documentModelInfoPagedResponse : client.listModels().iterableByPage()) {
            List<DocumentModelInfo> modelInfoList = documentModelInfoPagedResponse.getValue();
            modelInfoList.forEach(documentModelInfo -> {
                assertNotNull(documentModelInfo.getModelId());
                assertNotNull(documentModelInfo.getCreatedOn());
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void listModelsWithContext(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        int pageCount = 0;
        for (PagedResponse<DocumentModelInfo> documentModelInfoPagedResponse
            : client.listModels(Context.NONE).iterableByPage()) {
            List<DocumentModelInfo> modelInfoList = documentModelInfoPagedResponse.getValue();
            modelInfoList.forEach(documentModelInfo -> {
                assertNotNull(documentModelInfo.getModelId());
                assertNotNull(documentModelInfo.getCreatedOn());
            });
            pageCount++;
            if (pageCount > 4) {
                // Stop after 4 pages since there can be large number of models.
                break;
            }
        }
    }

    /**
     * Verifies that an exception is thrown for null source url input.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void beginBuildModelNullInput(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        Exception exception = assertThrows(NullPointerException.class, () ->
            client.beginBuildModel(null, null));
        assertEquals("'trainingFilesUrl' cannot be null.", exception.getMessage());
    }

    /**
     * Verifies the result of the copy operation for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void beginCopy(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller =
                client.beginBuildModel(trainingFilesUrl, null)
                    .setPollInterval(durationTestMode);
            syncPoller.waitForCompletion();
            DocumentModel actualModel = syncPoller.getFinalResult();

            CopyAuthorization target =
                client.getCopyAuthorization(null);
            SyncPoller<DocumentOperationResult, DocumentModel>
                copyPoller = client.beginCopyModel(actualModel.getModelId(), target)
                .setPollInterval(durationTestMode);
            DocumentModel copiedModel = copyPoller.getFinalResult();

            Assertions.assertEquals(target.getTargetModelId(), copiedModel.getModelId());
            client.deleteModel(actualModel.getModelId());
            client.deleteModel(copiedModel.getModelId());
        });
    }

    /**
     * Verifies the result of the copy authorization for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    public void copyAuthorization(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        validateCopyAuthorizationResult(client.getCopyAuthorization(null));
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and JPG training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void beginBuildModelWithJPGTrainingSet(HttpClient httpClient,
                                                  DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller =
                client.beginBuildModel(trainingFilesUrl, null)
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();

            validateDocumentModelData(buildModelPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the training operation for a valid labeled model ID and multi-page PDF training set Url.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void beginBuildModelWithMultiPagePDFTrainingSet(HttpClient httpClient,
                                                           DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        multipageTrainingRunner(trainingFilesUrl -> {
            SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller =
                client.beginBuildModel(trainingFilesUrl, null)
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
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void beginBuildModelIncludeSubfolderWithPrefixName(HttpClient httpClient,
                                                              DocumentAnalysisServiceVersion serviceVersion) {
        // "innererror": {
        //     "code": "TrainingContentMissing",
        //         "message": "Training data is missing: Could not find any training data at the given path."
        // }
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> buildModelPoller =
                client.beginBuildModel(trainingFilesUrl, null,
                        new BuildModelOptions().setPrefix("subfolder"), Context.NONE)
                    .setPollInterval(durationTestMode);
            buildModelPoller.waitForCompletion();

            validateDocumentModelData(buildModelPoller.getFinalResult());
        });
    }

    /**
     * Verifies the result of the training operation for a valid unlabeled model ID and include subfolder training set
     * Url with non-existing prefix name.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void beginBuildModelIncludeSubfolderWithNonExistPrefixName(HttpClient httpClient,
                                                                      DocumentAnalysisServiceVersion serviceVersion) {
        // confirm
        // "code": "TrainingContentMissing",
        //     "message": "Training data is missing: Could not find any training data at the given path."
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        multipageTrainingRunner(trainingFilesUrl -> {
            HttpResponseException exception = assertThrows(HttpResponseException.class, () ->
                client.beginBuildModel(trainingFilesUrl, null,
                        new BuildModelOptions().setPrefix("subfolder"), Context.NONE)
                    .setPollInterval(durationTestMode));

            final FormRecognizerError errorInformation =
                (FormRecognizerError) exception.getValue();
            assertEquals("ModelNotFound", errorInformation.getInnerError().getCode());
        });
    }

    /**
     * Verifies the result of the create composed model for valid parameters.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.formrecognizer.TestUtils#getTestParameters")
    @Disabled
    public void beginCreateComposedModel(HttpClient httpClient, DocumentAnalysisServiceVersion serviceVersion) {
        // TODO: (https://github.com/Azure/azure-sdk-for-java-pr/issues/1353)
        client = getDocumentModelAdministrationClient(httpClient, serviceVersion);
        buildModelRunner((trainingFilesUrl) -> {
            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller1 =
                client.beginBuildModel(trainingFilesUrl, "sync_component_model_1")
                    .setPollInterval(durationTestMode);
            syncPoller1.waitForCompletion();
            DocumentModel createdModel1 = syncPoller1.getFinalResult();

            SyncPoller<DocumentOperationResult, DocumentModel> syncPoller2 =
                client.beginBuildModel(trainingFilesUrl, "sync_component_model_2")
                    .setPollInterval(durationTestMode);
            syncPoller2.waitForCompletion();
            DocumentModel createdModel2 = syncPoller2.getFinalResult();

            final List<String> modelIDList = Arrays.asList(createdModel1.getModelId(), createdModel2.getModelId());

            DocumentModel composedModel =
                client.beginCreateComposedModel(modelIDList,
                        "sync_java_composed_model",
                        new CreateComposedModelOptions().setDescription("test desc"),
                        Context.NONE)
                    .setPollInterval(durationTestMode)
                    .getFinalResult();

            assertNotNull(composedModel.getModelId());
            assertEquals("test desc", composedModel.getDescription());
            assertEquals(2, composedModel.getDocTypes().size());
            validateDocumentModelData(composedModel);

            client.deleteModel(createdModel1.getModelId());
            client.deleteModel(createdModel2.getModelId());
            client.deleteModel(composedModel.getModelId());
        });
    }
}
