// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.administration;

import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationAsyncClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceAdministrationClientBuilder;
import com.azure.ai.documentintelligence.models.AuthorizeCopyRequest;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.BuildDocumentClassifierRequest;
import com.azure.ai.documentintelligence.models.BuildDocumentModelRequest;
import com.azure.ai.documentintelligence.models.ClassifierDocumentTypeDetails;
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
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.HashMap;

/**
 * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient}
 */
public class DocumentModelAdminAsyncClientJavaDocCodeSnippets {
    private final DocumentIntelligenceAdministrationAsyncClient documentIntelligenceAdministrationAsyncClient =
        new DocumentIntelligenceAdministrationClientBuilder().buildAsyncClient();

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient} initialization
     */
    public void documentModelAdministrationAsyncClientInitialization() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.initialization
        DocumentIntelligenceAdministrationAsyncClient client = new DocumentIntelligenceAdministrationClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.initialization
    }

    public void documentModelAdministrationAsyncClientKeyCred() {
        // BEGIN: readme-sample-createDocumentModelAdministrationAsyncClient
        DocumentIntelligenceAdministrationAsyncClient documentIntelligenceAdministrationAsyncClient =
            new DocumentIntelligenceAdministrationClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildAsyncClient();
        // END: readme-sample-createDocumentModelAdministrationAsyncClient
    }

    /**
     * Code snippet for creating a {@link DocumentIntelligenceAdministrationAsyncClient} with pipeline
     */
    public void createDocumentModelAdministrationAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        DocumentIntelligenceAdministrationAsyncClient documentIntelligenceAdministrationAsyncClient =
            new DocumentIntelligenceAdministrationClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildAsyncClient();
        // END:  com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.pipeline.instantiation
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#beginBuildDocumentModel(BuildDocumentModelRequest)}
     */
    public void beginBuildModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginBuildDocumentModel#BuildDocumentModelRequest
        String blobContainerUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        documentIntelligenceAdministrationAsyncClient.beginBuildDocumentModel(
            new BuildDocumentModelRequest("modelID", DocumentBuildMode.TEMPLATE)
                .setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl)))
            // if polling operation completed, retrieve the final result.
            .flatMap(asyncPollResponse -> asyncPollResponse.getFinalResult())
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
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginBuildDocumentModel#BuildDocumentModelRequest
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#beginBuildClassifier(BinaryData, RequestOptions)}
     */
    public void beginBuildClassifier() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginBuildClassifier#BuildDocumentClassifierRequest
        String blobContainerUrl1040D = "{SAS_URL_of_your_container_in_blob_storage}";
        String blobContainerUrl1040A = "{SAS_URL_of_your_container_in_blob_storage}";
        HashMap<String, ClassifierDocumentTypeDetails> documentTypesDetailsMap = new HashMap<>();
        documentTypesDetailsMap.put("1040-D", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl1040D)
        ));
        documentTypesDetailsMap.put("1040-A", new ClassifierDocumentTypeDetails().setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl1040A)
        ));

        documentIntelligenceAdministrationAsyncClient.beginBuildClassifier(new BuildDocumentClassifierRequest("classifierID", documentTypesDetailsMap))
            // if polling operation completed, retrieve the final result.
            .flatMap(asyncPollResponse -> asyncPollResponse.getFinalResult())
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
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginBuildClassifier#BuildDocumentClassifierRequest
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#deleteModel(String)}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteModel#string
        String modelId = "{model_id}";
        documentIntelligenceAdministrationAsyncClient.deleteModel(modelId)
            .subscribe(ignored -> System.out.printf("Model ID: %s is deleted%n", modelId));
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteModel#string
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#deleteModelWithResponse(String, RequestOptions)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteModelWithResponse#string-RequestOptions
        String modelId = "{model_id}";
        documentIntelligenceAdministrationAsyncClient.deleteModelWithResponse(modelId, null)
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                System.out.printf("Model ID: %s is deleted.%n", modelId);
            });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteModelWithResponse#string-RequestOptions
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#authorizeModelCopy(AuthorizeCopyRequest)}
     */
    public void authorizeModelCopy() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.authorizeModelCopy
        String modelId = "my-copied-model";
        documentIntelligenceAdministrationAsyncClient.authorizeModelCopy(new AuthorizeCopyRequest(modelId))
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
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#authorizeModelCopyWithResponse(BinaryData, RequestOptions)}
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
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#getResourceInfo()}
     */
    public void getResourceInfo() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getResourceInfo
        documentIntelligenceAdministrationAsyncClient.getResourceInfo()
            .subscribe(resourceInfo -> {
                System.out.printf("Max number of models that can be build for this account: %d%n",
                    resourceInfo.getCustomDocumentModels().getLimit());
                System.out.printf("Current count of built document analysis models: %d%n",
                    resourceInfo.getCustomDocumentModels().getCount());
            });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getResourceInfo
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#getResourceInfoWithResponse(RequestOptions)}
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
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#beginComposeModel(BinaryData, RequestOptions)}
     */
    public void beginCreateComposedModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginComposeDocumentModel#ComposeDocumentModelRequest
        documentIntelligenceAdministrationAsyncClient.beginComposeModel(
            new ComposeDocumentModelRequest("composedModelID", "classifierId", null)
                .setDescription("my composed model description"))
            // if polling operation completed, retrieve the final result.
            .flatMap(asyncPollResponse -> asyncPollResponse.getFinalResult())
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
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginComposeDocumentModel#ComposeDocumentModelRequest
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#beginCopyModelTo(String, CopyAuthorization)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginCopyDocumentModelTo#AuthorizeCopyRequest
        String copyModelId = "copy-model";
        // Get authorization to copy the model to target resource
        documentIntelligenceAdministrationAsyncClient.authorizeModelCopy(new AuthorizeCopyRequest(copyModelId))
            // Start copy operation from the source client
            // The ID of the model that needs to be copied to the target resource
            .subscribe(copyAuthorization -> documentIntelligenceAdministrationAsyncClient.beginCopyModelTo(copyModelId,
                    copyAuthorization)
                .filter(pollResponse -> pollResponse.getStatus().isComplete())
                .flatMap(asyncPollResponse -> asyncPollResponse.getFinalResult())
                .subscribe(documentModel ->
                    System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
                        documentModel.getModelId(),
                        documentModel.getCreatedDateTime())));

        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.beginCopyDocumentModelTo#AuthorizeCopyRequest
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#listModels()}
     */
    public void listModels() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listModels
        documentIntelligenceAdministrationAsyncClient.listModels()
            .subscribe(documentModelInfo ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModelInfo.getModelId(),
                    documentModelInfo.getDescription(),
                    documentModelInfo.getCreatedDateTime()));
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listModels
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#getModel(String)}
     */
    public void getModel() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getModel#string
        String modelId = "{model_id}";
        documentIntelligenceAdministrationAsyncClient.getModel(modelId).subscribe(documentModel -> {
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
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#getModelWithResponse(String, RequestOptions)}
     */
    public void getModelWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getModelWithResponse#string-RequestOptions
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
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getModelWithResponse#string-RequestOptions
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#getModel(String)}
     */
    public void getOperation() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getOperation#string
        String operationId = "{operation_Id}";
        documentIntelligenceAdministrationAsyncClient.getOperation(operationId).subscribe(operationDetails -> {
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
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#getOperationWithResponse(String, RequestOptions)}
     */
    public void getOperationWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getOperationWithResponse#string-RequestOptions
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
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getOperationWithResponse#string-RequestOptions
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#listOperations()}
     */
    public void listOperations() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listOperations
        documentIntelligenceAdministrationAsyncClient.listOperations()
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
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#deleteClassifier(String)}
     */
    public void deleteClassifier() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteDocumentClassifier#string
        String classifierId = "{classifierId}";
        documentIntelligenceAdministrationAsyncClient.deleteClassifier(classifierId)
            .subscribe(ignored -> System.out.printf("Classifier ID: %s is deleted%n", classifierId));
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteDocumentClassifier#string
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#deleteClassifierWithResponse(String, RequestOptions)}
     */
    public void deleteClassifierWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteDocumentClassifierWithResponse#string-RequestOptions
        String classifierId = "{classifierId}";
        documentIntelligenceAdministrationAsyncClient.deleteClassifierWithResponse(classifierId, new RequestOptions())
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                System.out.printf("Classifier ID: %s is deleted.%n", classifierId);
            });
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.deleteDocumentClassifierWithResponse#string-RequestOptions
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#listClassifiers()}
     */
    public void listClassifiers() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listDocumentClassifiers
        documentIntelligenceAdministrationAsyncClient.listClassifiers()
            .subscribe(documentModelInfo ->
                System.out.printf("Classifier ID: %s, Classifier description: %s, Created on: %s.%n",
                    documentModelInfo.getClassifierId(),
                    documentModelInfo.getDescription(),
                    documentModelInfo.getCreatedDateTime()));
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.listDocumentClassifiers
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#getClassifier(String)}
     */
    public void getDocumentClassifier() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getDocumentClassifier#string
        String modelId = "{model_id}";
        documentIntelligenceAdministrationAsyncClient.getClassifier(modelId).subscribe(documentClassifier -> {
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
     * Code snippet for {@link DocumentIntelligenceAdministrationAsyncClient#getClassifierWithResponse(String, RequestOptions)}
     */
    public void getClassifierWithResponse() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getDocumentClassifierWithResponse#string-RequestOptions
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
        // END: com.azure.ai.documentintelligence.DocumentModelAdminAsyncClient.getDocumentClassifierWithResponse#string-RequestOptions
    }
}
