// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.administration;

import com.azure.ai.documentintelligence.DocumentModelAdministrationAsyncClient;
import com.azure.ai.documentintelligence.DocumentModelAdministrationClientBuilder;
import com.azure.ai.documentintelligence.models.AuthorizeCopyRequest;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentClassifierRequest;
import com.azure.ai.documentintelligence.models.BuildDocumentModelRequest;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
import com.azure.ai.documentintelligence.models.ComponentDocumentModelDetails;
import com.azure.ai.documentintelligence.models.ComposeDocumentModelRequest;
import com.azure.ai.documentintelligence.models.CopyAuthorization;
import com.azure.ai.documentintelligence.models.DocumentBuildMode;
import com.azure.ai.documentintelligence.models.DocumentModelBuildOperationDetails;
import com.azure.ai.documentintelligence.models.OperationStatus;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Code snippet for {@link DocumentModelAdministrationAsyncClient}
 */
public class DocumentModelAdminAsyncClientJavaDocCodeSnippets {
    private final DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
        new DocumentModelAdministrationClientBuilder().buildAsyncClient();

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient} initialization
     */
    public void documentModelAdministrationAsyncClientInitialization() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.initialization
        // DocumentModelAdministrationAsyncClient client = new DocumentModelAdministrationClientBuilder()
        // .endpoint("{endpoint}")
        //    .credential(new DefaultAzureCredentialBuilder().build())
        //     .buildAsyncClient();
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.initialization
    }

    public void documentModelAdministrationAsyncClientKeyCred() {
        // BEGIN: readme-sample-createDocumentModelAdministrationAsyncClient
        DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
            new DocumentModelAdministrationClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildAsyncClient();
        // END: readme-sample-createDocumentModelAdministrationAsyncClient
    }

    /**
     * Code snippet for creating a {@link DocumentModelAdministrationAsyncClient} with pipeline
     */
    public void createDocumentModelAdministrationAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
            new DocumentModelAdministrationClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildAsyncClient();
        // END:  com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.pipeline.instantiation
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginBuildDocumentModel(BuildDocumentModelRequest)}
     */
    public void beginBuildModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode
        String blobContainerUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        documentModelAdministrationAsyncClient.beginBuildDocumentModel(
            new BuildDocumentModelRequest("modelID", DocumentBuildMode.TEMPLATE)
                .setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl)))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {
                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Created on: %s%n", documentModel.getCreatedDateTime());
                documentModel.getDocTypes().forEach((key, documentTypeDetails) -> {
                    documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
                    });
                });
            });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginBuildClassifier(BinaryData, RequestOptions)}
     */
    public void beginBuildClassifier() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginBuildClassifier#Map
        String blobContainerUrl1040D = "{SAS_URL_of_your_container_in_blob_storage}";
        String blobContainerUrl1040A = "{SAS_URL_of_your_container_in_blob_storage}";
        HashMap<String, ClassifierDocumentTypeDetails> documentTypesDetailsMap = new HashMap<>();
        documentTypesDetailsMap.put("1040-D", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl1040D)
        ));
        documentTypesDetailsMap.put("1040-A", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl1040A)
        ));

        documentModelAdministrationAsyncClient.beginBuildClassifier(new BuildDocumentClassifierRequest("classifierID", documentTypesDetailsMap))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(classifierDetails -> {
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
            });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginBuildClassifier#Map
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteModel(String)}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteModel#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.deleteModel(modelId)
            .subscribe(ignored -> System.out.printf("Model ID: %s is deleted%n", modelId));
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteModelWithResponse(String, RequestOptions)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteModelWithResponse#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.deleteModelWithResponse(modelId, null)
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                System.out.printf("Model ID: %s is deleted.%n", modelId);
            });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteModelWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#authorizeModelCopy(AuthorizeCopyRequest)}
     */
    public void authorizeModelCopy() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.authorizeModelCopy
        String modelId = "my-copied-model";
        documentModelAdministrationAsyncClient.authorizeModelCopy(new AuthorizeCopyRequest(modelId))
            .subscribe(copyAuthorization ->
                System.out.printf("Copy Authorization for model id: %s, access token: %s, expiration time: %s, "
                        + "target resource ID; %s, target resource region: %s%n",
                    copyAuthorization.getTargetModelId(),
                    copyAuthorization.getAccessToken(),
                    copyAuthorization.getExpirationDateTime(),
                    copyAuthorization.getTargetResourceId(),
                    copyAuthorization.getTargetResourceRegion()
                ));
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.authorizeModelCopy
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#authorizeModelCopyWithResponse(BinaryData, RequestOptions)}
     */
    public void authorizeModelCopyWithResponse() {
    // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.authorizeModelCopyWithResponse#Options
        /*
        String modelId = "my-copied-model";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        documentModelAdministrationAsyncClient.authorizeModelCopyWithResponse(
                new AuthorizeCopyRequest(modelId)
                    .setDescription("model desc")
                    .setTags(attrs), null)
            .subscribe(copyAuthorization ->
                System.out.printf("Copy Authorization response status: %s, for model id: %s, access token: %s, "
                        + "expiration time: %s, target resource ID; %s, target resource region: %s%n",
                    copyAuthorization.getStatusCode(),
                    copyAuthorization.getValue().getTargetModelId(),
                    copyAuthorization.getValue().getAccessToken(),
                    copyAuthorization.getValue().getExpirationDateTime(),
                    copyAuthorization.getValue().getTargetResourceId(),
                    copyAuthorization.getValue().getTargetResourceRegion()
                ));
         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.authorizeModelCopyWithResponse#Options
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getResourceInfo()}
     */
    public void getResourceInfo() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getResourceInfo
        documentModelAdministrationAsyncClient.getResourceInfo()
            .subscribe(resourceInfo -> {
                System.out.printf("Max number of models that can be build for this account: %d%n",
                    resourceInfo.getCustomDocumentModels().getLimit());
                System.out.printf("Current count of built document analysis models: %d%n",
                    resourceInfo.getCustomDocumentModels().getCount());
            });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getResourceInfo
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getResourceInfoWithResponse(RequestOptions)}
     */
    public void getResourceInfoWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getResourceInfoWithResponse
        /*documentModelAdministrationAsyncClient.getResourceInfoWithResponse(new RequestOptions())
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                ResourceDetails resourceDetails = response.getValue();
                System.out.printf("Max number of models that can be build for this account: %d%n",
                    resourceDetails.getCustomDocumentModelLimit());
                System.out.printf("Current count of built document analysis models: %d%n",
                    resourceDetails.getCustomDocumentModelCount());
            });
        */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getResourceInfoWithResponse
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginComposeModel(BinaryData, RequestOptions)}
     */
    public void beginCreateComposedModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginComposeDocumentModel#list
        String modelId1 = "{model_Id_1}";
        String modelId2 = "{model_Id_2}";
        documentModelAdministrationAsyncClient.beginComposeModel(
            new ComposeDocumentModelRequest("composedModelID", Arrays.asList(new ComponentDocumentModelDetails(modelId1), new ComponentDocumentModelDetails(modelId2))))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {
                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Created on: %s%n", documentModel.getCreatedDateTime());
                documentModel.getDocTypes().forEach((key, documentTypeDetails) -> {
                    documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
                    });
                });
            });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginComposeDocumentModel#list
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginCopyModelTo(String, CopyAuthorization)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginCopyDocumentModelTo#string-copyAuthorization
        String copyModelId = "copy-model";
        // Get authorization to copy the model to target resource
        documentModelAdministrationAsyncClient.authorizeModelCopy(new AuthorizeCopyRequest(copyModelId))
            // Start copy operation from the source client
            // The ID of the model that needs to be copied to the target resource
            .subscribe(copyAuthorization -> documentModelAdministrationAsyncClient.beginCopyModelTo(copyModelId,
                    copyAuthorization)
                .filter(pollResponse -> pollResponse.getStatus().isComplete())
                .flatMap(AsyncPollResponse::getFinalResult)
                .subscribe(documentModel ->
                    System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
                        documentModel.getModelId(),
                        documentModel.getCreatedDateTime())));

        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginCopyDocumentModelTo#string-copyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#listModels()}
     */
    public void listModels() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listModels
        documentModelAdministrationAsyncClient.listModels()
            .subscribe(documentModelInfo ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModelInfo.getModelId(),
                    documentModelInfo.getDescription(),
                    documentModelInfo.getCreatedDateTime()));
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listModels
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getModel(String)}
     */
    public void getModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getModel#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getModel(modelId).subscribe(documentModel -> {
            System.out.printf("Model ID: %s%n", documentModel.getModelId());
            System.out.printf("Model Description: %s%n", documentModel.getDescription());
            System.out.printf("Model Created on: %s%n", documentModel.getCreatedDateTime());
            documentModel.getDocTypes().forEach((key, documentTypeDetails) -> {
                documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                    System.out.printf("Field: %s", field);
                    System.out.printf("Field type: %s", documentFieldSchema.getType());
                    System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
                });
            });
        });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getModelWithResponse(String, RequestOptions)}
     */
    public void getModelWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getModelWithResponse#string
        /*String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getModelWithResponse(modelId, null)
            .subscribe(response -> {
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
        });

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getModelWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getModel(String)}
     */
    public void getOperation() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getOperation#string
        String operationId = "{operation_Id}";
        documentModelAdministrationAsyncClient.getOperation(operationId).subscribe(operationDetails -> {
            System.out.printf("Operation ID: %s%n", operationDetails.getOperationId());
            System.out.printf("Operation Status: %s%n", operationDetails.getStatus());
            System.out.printf("Model ID created with this operation: %s%n",
                ((DocumentModelBuildOperationDetails) operationDetails).getResult().getModelId());
            if (OperationStatus.FAILED.equals(operationDetails.getStatus())) {
                System.out.printf("Operation fail error: %s%n", operationDetails.getError().getMessage());
            }
        });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getOperation#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getOperationWithResponse(String, RequestOptions)}
     */
    public void getOperationWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getOperationWithResponse#string
        /*String operationId = "{operation_Id}";
        documentModelAdministrationAsyncClient.getOperationWithResponse(operationId, null)
            .subscribe(response -> {
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            OperationDetails operationDetails = response.getValue();
            System.out.printf("Operation ID: %s%n", operationDetails.getOperationId());
            System.out.printf("Operation Status: %s%n", operationDetails.getStatus());
            System.out.printf("Model ID created with this operation: %s%n",
                ((DocumentModelBuildOperationDetails) operationDetails).getResult().getModelId());
            if (OperationStatus.FAILED.equals(operationDetails.getStatus())) {
                System.out.printf("Operation fail error: %s%n", operationDetails.getError().getMessage());
            }
        });

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getOperationWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#listOperations()}
     */
    public void listOperations() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listOperations
        documentModelAdministrationAsyncClient.listOperations()
            .subscribe(modelOperationSummary -> {
                System.out.printf("Operation ID: %s%n", modelOperationSummary.getOperationId());
                System.out.printf("Operation Status: %s%n", modelOperationSummary.getStatus());
                System.out.printf("Operation Created on: %s%n", modelOperationSummary.getCreatedDateTime());
                System.out.printf("Operation Percent completed: %d%n", modelOperationSummary.getPercentCompleted());
                System.out.printf("Operation Last updated on: %s%n", modelOperationSummary.getLastUpdatedDateTime());
                System.out.printf("Operation resource location: %s%n", modelOperationSummary.getResourceLocation());
            });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listOperations
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteClassifier(String)}
     */
    public void deleteClassifier() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteDocumentClassifier#string
        String classifierId = "{classifierId}";
        documentModelAdministrationAsyncClient.deleteClassifier(classifierId)
            .subscribe(ignored -> System.out.printf("Classifier ID: %s is deleted%n", classifierId));
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteDocumentClassifier#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteClassifierWithResponse(String, RequestOptions)}
     */
    public void deleteClassifierWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteDocumentClassifierWithResponse#string
        String classifierId = "{classifierId}";
        documentModelAdministrationAsyncClient.deleteClassifierWithResponse(classifierId, null)
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                System.out.printf("Classifier ID: %s is deleted.%n", classifierId);
            });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteDocumentClassifierWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#listClassifiers()}
     */
    public void listClassifiers() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listDocumentClassifiers
        documentModelAdministrationAsyncClient.listClassifiers()
            .subscribe(documentModelInfo ->
                System.out.printf("Classifier ID: %s, Classifier description: %s, Created on: %s.%n",
                    documentModelInfo.getClassifierId(),
                    documentModelInfo.getDescription(),
                    documentModelInfo.getCreatedDateTime()));
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listDocumentClassifiers
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getClassifier(String)}
     */
    public void getDocumentClassifier() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getDocumentClassifier#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getClassifier(modelId).subscribe(documentClassifier -> {
            System.out.printf("Classifier ID: %s%n", documentClassifier.getClassifierId());
            System.out.printf("Classifier Description: %s%n", documentClassifier.getDescription());
            System.out.printf("Classifier Created on: %s%n", documentClassifier.getCreatedDateTime());
            documentClassifier.getDocTypes().forEach((key, documentTypeDetails) -> {
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
        });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getDocumentClassifier#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getClassifierWithResponse(String, RequestOptions)}
     */
    public void getClassifierWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getDocumentClassifierWithResponse#string
        /*String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getClassifierWithResponse(modelId, new RequestOptions())
            .subscribe(response -> {
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
        });

         */
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getDocumentClassifierWithResponse#string
    }
}
