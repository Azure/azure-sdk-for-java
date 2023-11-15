// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.administration;

import com.azure.ai.documentintelligence.DocumentModelAdministrationClient;
import com.azure.ai.documentintelligence.DocumentModelAdministrationClientBuilder;
import com.azure.ai.documentintelligence.models.AuthorizeCopyRequest;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentClassifierRequest;
import com.azure.ai.documentintelligence.models.BuildDocumentModelRequest;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
import com.azure.ai.documentintelligence.models.CopyAuthorization;
import com.azure.ai.documentintelligence.models.DocumentBuildMode;
import com.azure.ai.documentintelligence.models.DocumentClassifierDetails;
import com.azure.ai.documentintelligence.models.DocumentModelBuildOperationDetails;
import com.azure.ai.documentintelligence.models.DocumentModelDetails;
import com.azure.ai.documentintelligence.models.OperationDetails;
import com.azure.ai.documentintelligence.models.OperationStatus;
import com.azure.ai.documentintelligence.models.ResourceDetails;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.HashMap;

/**
 * Code snippet for {@link DocumentModelAdministrationClient}
 */
public class DocumentModelAdminClientJavaDocCodeSnippets {
    private final DocumentModelAdministrationClient documentModelAdministrationClient =
        new DocumentModelAdministrationClientBuilder().buildClient();

    /**
     * Code snippet for {@link DocumentModelAdministrationClient} initialization
     */
    public void documentModelAdministrationClientInInitialization() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.initialization
        DocumentModelAdministrationClient client = new DocumentModelAdministrationClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.initialization
    }

    /**
     * Code snippet for getting sync DocumentModelAdministration client using the AzureKeyCredential authentication.
     */
    public void documentModelAdministrationClientKeyCred() {
        // BEGIN: readme-sample-createDocumentModelAdministrationClient
        DocumentModelAdministrationClient client =
            new DocumentModelAdministrationClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildClient();
        // END: readme-sample-createDocumentModelAdministrationClient
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildDocumentModel(BinaryData, RequestOptions)}
     */
    public void beginBuildModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.beginBuildDocumentModel#BuildDocumentModelRequest
        String blobContainerUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        DocumentModelDetails documentModelDetails
            = documentModelAdministrationClient.beginBuildDocumentModel(
                new BuildDocumentModelRequest("modelID", DocumentBuildMode.TEMPLATE)
                    .setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl)))
            .getFinalResult().getResult();

        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedDateTime());
        documentModelDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.beginBuildDocumentModel#BuildDocumentModelRequest
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getResourceInfo()}
     */
    public void getResourceInfo() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.getResourceInfo
        ResourceDetails resourceDetails = documentModelAdministrationClient.getResourceInfo();
        System.out.printf("Max number of models that can be build for this account: %d%n",
            resourceDetails.getCustomDocumentModels().getLimit());
        System.out.printf("Current count of built document analysis models: %d%n",
            resourceDetails.getCustomDocumentModels().getCount());
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.getResourceInfo
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getResourceInfoWithResponse(RequestOptions)}
     */
    public void getResourceInfoWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.getResourceInfoWithResponse#RequestOptions
        /*Response<ResourceDetails> response =
            documentModelAdministrationClient.getResourceInfoWithResponse(new RequestOptions());
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        ResourceDetails resourceDetails = response.getValue();
        System.out.printf("Max number of models that can be build for this account: %d%n",
            resourceDetails.getCustomDocumentModels().getLimit());
        System.out.printf("Current count of built document analysis models: %d%n",
            resourceDetails.getCustomDocumentModels().getCount());

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.getResourceInfoWithResponse#RequestOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteModel(String)}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.deleteDocumentModel#string
        String modelId = "{custom-model-id}";
        documentModelAdministrationClient.deleteModel(modelId);
        System.out.printf("Model ID: %s is deleted.%n", modelId);
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.deleteDocumentModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteModelWithResponse(String, RequestOptions)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.deleteDocumentModelWithResponse#string-RequestOptions
        String modelId = "{custom-model-id}";
        Response<Void> response
            = documentModelAdministrationClient.deleteModelWithResponse(modelId, new RequestOptions());
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        System.out.printf("Model ID: %s is deleted.%n", modelId);
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.deleteDocumentModelWithResponse#string-RequestOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#authorizeModelCopy(AuthorizeCopyRequest)}
     */
    public void getCopyAuthorization() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.getCopyAuthorization#AuthorizeCopyRequest
        CopyAuthorization documentModelCopyAuthorization
            = documentModelAdministrationClient.authorizeModelCopy(new AuthorizeCopyRequest("copyModelID"));
        System.out.printf("Copy Authorization for model id: %s, access token: %s, expiration time: %s, "
                + "target resource ID; %s, target resource region: %s%n",
            documentModelCopyAuthorization.getTargetModelId(),
            documentModelCopyAuthorization.getAccessToken(),
            documentModelCopyAuthorization.getExpirationDateTime(),
            documentModelCopyAuthorization.getTargetResourceId(),
            documentModelCopyAuthorization.getTargetResourceRegion()
        );
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.getCopyAuthorization#AuthorizeCopyRequest
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#authorizeModelCopyWithResponse(BinaryData, RequestOptions)}
     */
    public void getCopyAuthorizationWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.getCopyAuthorizationWithResponse#AuthorizeCopyRequest-RequestOptions
        /*String modelId = "my-copied-model";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        Response<CopyAuthorization> copyAuthorizationResponse =
            documentModelAdministrationClient.authorizeModelCopyWithResponse(
                new AuthorizeCopyRequest(modelId)
                    .setDescription("model-desc")
                    .setTags(attrs), new RequestOptions());

        System.out.printf("Copy Authorization operation returned with status: %s",
            copyAuthorizationResponse.getStatusCode());
        DocumentModelCopyAuthorization documentModelCopyAuthorization = copyAuthorizationResponse.getValue();
        System.out.printf("Copy Authorization for model id: %s, access token: %s, "
                + "expiration time: %s, target resource ID; %s, target resource region: %s%n",
            documentModelCopyAuthorization.getTargetModelId(),
            documentModelCopyAuthorization.getAccessToken(),
            documentModelCopyAuthorization.getExpirationDateTime(),
            documentModelCopyAuthorization.getTargetResourceId(),
            documentModelCopyAuthorization.getTargetResourceRegion()
        );

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.getCopyAuthorizationWithResponse#AuthorizeCopyRequest-RequestOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginComposeModel(BinaryData, RequestOptions)}
     */
    public void beginCreateComposedModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.beginComposeDocumentModel#list-RequestOptions
        /*String modelId1 = "{custom-model-id_1}";
        String modelId2 = "{custom-model-id_2}";
        final DocumentModelDetails documentModelDetails
            = documentModelAdministrationClient.beginComposeModel(new ComposeDocumentModelRequest("composedModelID", Arrays.asList(new ComponentDocumentModelDetails(modelId1), new ComponentDocumentModelDetails(modelId2))), new RequestOptions())
            .getFinalResult().getAnalyzeResult();

        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedDateTime());
        documentModelDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
            });
        });

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.beginComposeDocumentModel#list-RequestOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCopyModelTo(String, CopyAuthorization)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.beginCopyDocumentModelTo#string-copyAuthorization
        String copyModelId = "copy-model";
        // Get authorization to copy the model to target resource
        CopyAuthorization documentModelCopyAuthorization
            = documentModelAdministrationClient.authorizeModelCopy(new AuthorizeCopyRequest(copyModelId));
        // Start copy operation from the source client
        DocumentModelDetails documentModelDetails
            = documentModelAdministrationClient.beginCopyModelTo(copyModelId, documentModelCopyAuthorization)
            .getFinalResult().getResult();
        System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
            documentModelDetails.getModelId(),
            documentModelDetails.getCreatedDateTime());
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.beginCopyDocumentModelTo#string-copyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listModels()}
     */
    public void listModels() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.listModels
        documentModelAdministrationClient.listModels()
            .forEach(documentModel ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModel.getModelId(),
                    documentModel.getDescription(),
                    documentModel.getCreatedDateTime())
            );
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.listModels
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listModels(RequestOptions)}
     */
    public void listModelsWithContext() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.listModels#RequestOptions
        /*documentModelAdministrationClient.listModels(new RequestOptions())
            .forEach(documentModel ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModel.getModelId(),
                    documentModel.getDescription(),
                    documentModel.getCreatedDateTime())
            );

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.listModels#RequestOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getModel(String)}
     */
    public void getModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.getModel#string
        String modelId = "{custom-model-id}";
        DocumentModelDetails documentModelDetails = documentModelAdministrationClient.getModel(modelId);
        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedDateTime());
        documentModelDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.getModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getModelWithResponse(String, RequestOptions)}
     */
    public void getModelWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.getModelWithResponse#string-RequestOptions
        String modelId = "{custom-model-id}";
        /*Response<DocumentModelDetails> response
            = documentModelAdministrationClient.getModelWithResponse(modelId, new RequestOptions());
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        DocumentModelDetails documentModelDetails = response.getValue();
        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedDateTime());
        documentModelDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
            });
        });

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.getModelWithResponse#string-RequestOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getOperation(String)}
     */
    public void getOperation() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.getOperation#string
        String operationId = "{operation-id}";
        OperationDetails operationDetails
            = documentModelAdministrationClient.getOperation(operationId);
        System.out.printf("Operation ID: %s%n", operationDetails.getOperationId());
        System.out.printf("Operation Status: %s%n", operationDetails.getStatus());
        System.out.printf("Model ID created with this operation: %s%n",
            ((DocumentModelBuildOperationDetails) operationDetails).getResult().getModelId());
        if (OperationStatus.FAILED.equals(operationDetails.getStatus())) {
            System.out.printf("Operation fail error: %s%n", operationDetails.getError().getMessage());
        }
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.getOperation#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getOperationWithResponse(String, RequestOptions)}
     */
    public void getOperationWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.getOperationWithResponse#string-RequestOptions
        /*String operationId = "{operation-id}";
        Response<OperationDetails> response =
            documentModelAdministrationClient.getOperationWithResponse(operationId, new RequestOptions());
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        OperationDetails operationDetails = response.getValue();
        System.out.printf("Operation ID: %s%n", operationDetails.getOperationId());
        System.out.printf("Operation Status: %s%n", operationDetails.getStatus());
        System.out.printf("Model ID created with this operation: %s%n",
            ((DocumentModelBuildOperationDetails) operationDetails).getResult().getModelId());
        if (OperationStatus.FAILED.equals(operationDetails.getStatus())) {
            System.out.printf("Operation fail error: %s%n", operationDetails.getError().getMessage());
        }

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.getOperationWithResponse#string-RequestOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listOperations()}
     */
    public void listOperations() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.listOperations
        PagedIterable<OperationDetails>
            modelOperationInfo = documentModelAdministrationClient.listOperations();
        modelOperationInfo.forEach(modelOperationSummary -> {
            System.out.printf("Operation ID: %s%n", modelOperationSummary.getOperationId());
            System.out.printf("Operation Status: %s%n", modelOperationSummary.getStatus());
            System.out.printf("Operation Created on: %s%n", modelOperationSummary.getCreatedDateTime());
            System.out.printf("Operation Percent completed: %d%n", modelOperationSummary.getPercentCompleted());
            System.out.printf("Operation Last updated on: %s%n", modelOperationSummary.getLastUpdatedDateTime());
            System.out.printf("Operation resource location: %s%n", modelOperationSummary.getResourceLocation());
        });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.listOperations
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listOperations(RequestOptions)}
     */
    public void listOperationsWithContext() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.listOperations#RequestOptions
        /*PagedIterable<OperationDetails>
            modelOperationInfo = documentModelAdministrationClient.listOperations(new RequestOptions());
        modelOperationInfo.forEach(modelOperationSummary -> {
            System.out.printf("Operation ID: %s%n", modelOperationSummary.getOperationId());
            System.out.printf("Operation Status: %s%n", modelOperationSummary.getStatus());
            System.out.printf("Operation Created on: %s%n", modelOperationSummary.getCreatedDateTime());
            System.out.printf("Operation Percent completed: %d%n", modelOperationSummary.getPercentCompleted());
            System.out.printf("Operation Last updated on: %s%n", modelOperationSummary.getLastUpdatedDateTime());
            System.out.printf("Operation resource location: %s%n", modelOperationSummary.getResourceLocation());
        });

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.listOperations#RequestOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildClassifier(BuildDocumentClassifierRequest)}
     */
    public void beginBuildClassifier() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.beginBuildClassifier#BuildDocumentClassifierRequest
        String blobContainerUrl1040D = "{SAS_URL_of_your_container_in_blob_storage}";
        String blobContainerUrl1040A = "{SAS_URL_of_your_container_in_blob_storage}";
        HashMap<String, ClassifierDocumentTypeDetails> documentTypes = new HashMap<>();
        documentTypes.put("1040-D", new ClassifierDocumentTypeDetails()
            .setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl1040D)
            ));
        documentTypes.put("1040-A", new ClassifierDocumentTypeDetails()
            .setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl1040A)
            ));

        DocumentClassifierDetails classifierDetails
            = documentModelAdministrationClient.beginBuildClassifier(
                new BuildDocumentClassifierRequest("classifierID", documentTypes))
            .getFinalResult().getResult();

        System.out.printf("Classifier ID: %s%n", classifierDetails.getClassifierId());
        System.out.printf("Classifier description: %s%n", classifierDetails.getDescription());
        System.out.printf("Classifier created on: %s%n", classifierDetails.getCreatedDateTime());
        System.out.printf("Classifier expires on: %s%n", classifierDetails.getExpirationDateTime());
        classifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            if (documentTypeDetails.getAzureBlobSource() != null) {
                System.out.printf("Blob Source container Url: %s", (documentTypeDetails
                    .getAzureBlobSource()).getContainerUrl());
            }
        });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.beginBuildClassifier#BuildDocumentClassifierRequest
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteClassifier(String)}
     */
    public void deleteDocumentClassifier() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.deleteDocumentClassifier#string
        String classifierId = "{classifierId}";
        documentModelAdministrationClient.deleteClassifier(classifierId);
        System.out.printf("Classifier ID: %s is deleted.%n", classifierId);
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.deleteDocumentClassifier#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteClassifierWithResponse(String, RequestOptions)}
     */
    public void deleteDocumentClassifierWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.deleteDocumentClassifierWithResponse#string-Context
        String classifierId = "{classifierId}";
        Response<Void> response
            = documentModelAdministrationClient.deleteClassifierWithResponse(classifierId, new RequestOptions());
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        System.out.printf("Classifier ID: %s is deleted.%n", classifierId);
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.deleteDocumentClassifierWithResponse#string-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listClassifiers()}
     */
    public void listDocumentClassifiers() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.listDocumentClassifiers
        documentModelAdministrationClient.listClassifiers()
            .forEach(documentModel ->
                System.out.printf("Classifier ID: %s, Classifier description: %s, Created on: %s.%n",
                    documentModel.getClassifierId(),
                    documentModel.getDescription(),
                    documentModel.getCreatedDateTime())
            );
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.listDocumentClassifiers
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getClassifier(String)}
     */
    public void getDocumentClassifier() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.getDocumentClassifier#string
        String classifierId = "{classifierId}";
        DocumentClassifierDetails documentClassifierDetails
            = documentModelAdministrationClient.getClassifier(classifierId);
        System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());
        System.out.printf("Classifier Description: %s%n", documentClassifierDetails.getDescription());
        System.out.printf("Classifier Created on: %s%n", documentClassifierDetails.getCreatedDateTime());
        documentClassifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            if (documentTypeDetails.getAzureBlobSource() != null) {
                System.out.printf("Blob Source container Url: %s", (documentTypeDetails
                    .getAzureBlobSource()).getContainerUrl());
            }
            if (documentTypeDetails.getAzureBlobFileListSource() != null) {
                System.out.printf("Blob File List Source container Url: %s",
                    (documentTypeDetails
                        .getAzureBlobFileListSource()).getContainerUrl());
            }
        });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.getDocumentClassifier#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getClassifierWithResponse(String, RequestOptions)}
     */
    public void getDocumentClassifierWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminClient.getDocumentClassifierWithResponse#string-RequestOptions
        /*String modelId = "{custom-model-id}";
        Response<DocumentClassifierDetails> response
            = documentModelAdministrationClient.getClassifierWithResponse(modelId, new RequestOptions());
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        DocumentClassifierDetails documentClassifierDetails = response.getValue();
        System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());
        System.out.printf("Classifier Description: %s%n", documentClassifierDetails.getDescription());
        System.out.printf("Classifier Created on: %s%n", documentClassifierDetails.getCreatedDateTime());
        documentClassifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            if (documentTypeDetails.getContentSource() instanceof AzureBlobContentSource) {
                System.out.printf("Blob Source container Url: %s", ((AzureBlobContentSource) documentTypeDetails
                    .getContentSource()).getContainerUrl());
            }
            if (documentTypeDetails.getContentSource() instanceof BlobFileListContentSource) {
                System.out.printf("Blob File List Source container Url: %s",
                    ((BlobFileListContentSource) documentTypeDetails
                        .getContentSource()).getContainerUrl());
            }
        });

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminClient.getDocumentClassifierWithResponse#string-RequestOptions
    }

}
