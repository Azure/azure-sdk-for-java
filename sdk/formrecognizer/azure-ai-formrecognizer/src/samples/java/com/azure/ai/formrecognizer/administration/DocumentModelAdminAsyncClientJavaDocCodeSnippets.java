// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ComposeDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelCopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationStatus;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ResourceDetails;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.polling.AsyncPollResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.initialization
        DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
            new DocumentModelAdministrationClientBuilder().buildAsyncClient();
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.initialization
    }

    /**
     * Code snippet for creating a {@link DocumentModelAdministrationAsyncClient} with pipeline
     */
    public void createDocumentModelAdministrationAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
            new DocumentModelAdministrationClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildAsyncClient();
        // END:  com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.pipeline.instantiation
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginBuildDocumentModel(String, DocumentModelBuildMode)}
     */
    public void beginBuildModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode
        String blobContainerUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        documentModelAdministrationAsyncClient.beginBuildDocumentModel(blobContainerUrl,
                DocumentModelBuildMode.TEMPLATE
            )
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {
                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
                documentModel.getDocumentTypes().forEach((key, documentTypeDetails) -> {
                    documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
                    });
                });
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginBuildDocumentModel(String, DocumentModelBuildMode, String, BuildDocumentModelOptions)}
     * with options
     */
    public void beginBuildModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode-String-Options
        String blobContainerUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        String modelId = "model-id";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");
        String prefix = "Invoice";

        documentModelAdministrationAsyncClient.beginBuildDocumentModel(blobContainerUrl,
                DocumentModelBuildMode.TEMPLATE,
                prefix,
                new BuildDocumentModelOptions()
                    .setModelId(modelId)
                    .setDescription("model desc")
                    .setTags(attrs))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {
                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Description: %s%n", documentModel.getDescription());
                System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
                System.out.printf("Model assigned tags: %s%n", documentModel.getTags());
                documentModel.getDocumentTypes().forEach((key, documentTypeDetails) -> {
                    documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
                    });
                });
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode-String-Options
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteDocumentModel}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentModel#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.deleteDocumentModel(modelId)
            .subscribe(ignored -> System.out.printf("Model ID: %s is deleted%n", modelId));
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteDocumentModelWithResponse(String)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentModelWithResponse#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.deleteDocumentModelWithResponse(modelId)
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                System.out.printf("Model ID: %s is deleted.%n", modelId);
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentModelWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getCopyAuthorization()}
     */
    public void getCopyAuthorization() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getCopyAuthorization
        String modelId = "my-copied-model";
        documentModelAdministrationAsyncClient.getCopyAuthorization()
            .subscribe(copyAuthorization ->
                System.out.printf("Copy Authorization for model id: %s, access token: %s, expiration time: %s, "
                        + "target resource ID; %s, target resource region: %s%n",
                    copyAuthorization.getTargetModelId(),
                    copyAuthorization.getAccessToken(),
                    copyAuthorization.getExpiresOn(),
                    copyAuthorization.getTargetResourceId(),
                    copyAuthorization.getTargetResourceRegion()
                ));
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getCopyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getCopyAuthorizationWithResponse(CopyAuthorizationOptions)}
     */
    public void getCopyAuthorizationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getCopyAuthorizationWithResponse#Options
        String modelId = "my-copied-model";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        documentModelAdministrationAsyncClient.getCopyAuthorizationWithResponse(
                new CopyAuthorizationOptions()
                    .setModelId(modelId)
                    .setDescription("model desc")
                    .setTags(attrs))
            .subscribe(copyAuthorization ->
                System.out.printf("Copy Authorization response status: %s, for model id: %s, access token: %s, "
                        + "expiration time: %s, target resource ID; %s, target resource region: %s%n",
                    copyAuthorization.getStatusCode(),
                    copyAuthorization.getValue().getTargetModelId(),
                    copyAuthorization.getValue().getAccessToken(),
                    copyAuthorization.getValue().getExpiresOn(),
                    copyAuthorization.getValue().getTargetResourceId(),
                    copyAuthorization.getValue().getTargetResourceRegion()
                ));
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getCopyAuthorizationWithResponse#Options
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getResourceDetails()}
     */
    public void getResourceInfo() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getResourceDetails
        documentModelAdministrationAsyncClient.getResourceDetails()
            .subscribe(resourceInfo -> {
                System.out.printf("Max number of models that can be build for this account: %d%n",
                    resourceInfo.getCustomDocumentModelLimit());
                System.out.printf("Current count of built document analysis models: %d%n",
                    resourceInfo.getCustomDocumentModelCount());
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getResourceDetails
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getResourceDetailsWithResponse()}
     */
    public void getResourceInfoWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getResourceDetailsWithResponse
        documentModelAdministrationAsyncClient.getResourceDetailsWithResponse()
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                ResourceDetails resourceDetails = response.getValue();
                System.out.printf("Max number of models that can be build for this account: %d%n",
                    resourceDetails.getCustomDocumentModelLimit());
                System.out.printf("Current count of built document analysis models: %d%n",
                    resourceDetails.getCustomDocumentModelCount());
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getResourceDetailsWithResponse
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginComposeDocumentModel(List)}
     */
    public void beginCreateComposedModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginComposeDocumentModel#list
        String modelId1 = "{model_Id_1}";
        String modelId2 = "{model_Id_2}";
        documentModelAdministrationAsyncClient.beginComposeDocumentModel(Arrays.asList(modelId1, modelId2)
            )
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {
                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
                documentModel.getDocumentTypes().forEach((key, documentTypeDetails) -> {
                    documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
                    });
                });
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginComposeDocumentModel#list
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginComposeDocumentModel(List, ComposeDocumentModelOptions)}
     * with options
     */
    public void beginCreateComposedModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginComposeDocumentModel#list-Options
        String modelId1 = "{model_Id_1}";
        String modelId2 = "{model_Id_2}";
        String modelId = "my-composed-model";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        documentModelAdministrationAsyncClient.beginComposeDocumentModel(Arrays.asList(modelId1, modelId2),
                new ComposeDocumentModelOptions()
                    .setModelId(modelId)
                    .setDescription("model-desc")
                    .setTags(attrs))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {
                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Description: %s%n", documentModel.getDescription());
                System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
                System.out.printf("Model assigned tags: %s%n", documentModel.getTags());
                documentModel.getDocumentTypes().forEach((key, documentTypeDetails) -> {
                    documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
                    });
                });
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginComposeDocumentModel#list-Options
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginCopyDocumentModelTo(String, DocumentModelCopyAuthorization)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginCopyDocumentModelTo#string-copyAuthorization
        String copyModelId = "copy-model";
        // Get authorization to copy the model to target resource
        documentModelAdministrationAsyncClient.getCopyAuthorization()
            // Start copy operation from the source client
            // The ID of the model that needs to be copied to the target resource
            .subscribe(copyAuthorization -> documentModelAdministrationAsyncClient.beginCopyDocumentModelTo(copyModelId,
                    copyAuthorization)
                .filter(pollResponse -> pollResponse.getStatus().isComplete())
                .flatMap(AsyncPollResponse::getFinalResult)
                .subscribe(documentModel ->
                    System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
                        documentModel.getModelId(),
                        documentModel.getCreatedOn())));

        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginCopyDocumentModelTo#string-copyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#listDocumentModels()}
     */
    public void listModels() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listDocumentModels
        documentModelAdministrationAsyncClient.listDocumentModels()
            .subscribe(documentModelInfo ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModelInfo.getModelId(),
                    documentModelInfo.getDescription(),
                    documentModelInfo.getCreatedOn()));
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listDocumentModels
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getDocumentModel(String)}
     */
    public void getModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentModel#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getDocumentModel(modelId).subscribe(documentModel -> {
            System.out.printf("Model ID: %s%n", documentModel.getModelId());
            System.out.printf("Model Description: %s%n", documentModel.getDescription());
            System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
            documentModel.getDocumentTypes().forEach((key, documentTypeDetails) -> {
                documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                    System.out.printf("Field: %s", field);
                    System.out.printf("Field type: %s", documentFieldSchema.getType());
                    System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
                });
            });
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getDocumentModelWithResponse(String)}
     */
    public void getModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentModelWithResponse#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getDocumentModelWithResponse(modelId).subscribe(response -> {
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            DocumentModelDetails documentModelDetails = response.getValue();
            System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
            System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
            System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedOn());
            documentModelDetails.getDocumentTypes().forEach((key, documentTypeDetails) -> {
                documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                    System.out.printf("Field: %s", field);
                    System.out.printf("Field type: %s", documentFieldSchema.getType());
                    System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
                });
            });
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentModelWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getDocumentModel(String)}
     */
    public void getOperation() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getOperation#string
        String operationId = "{operation_Id}";
        documentModelAdministrationAsyncClient.getOperation(operationId).subscribe(operationDetails -> {
            System.out.printf("Operation ID: %s%n", operationDetails.getOperationId());
            System.out.printf("Operation Kind: %s%n", operationDetails.getKind());
            System.out.printf("Operation Status: %s%n", operationDetails.getStatus());
            System.out.printf("Model ID created with this operation: %s%n",
                ((DocumentModelBuildOperationDetails) operationDetails).getResult().getModelId());
            if (OperationStatus.FAILED.equals(operationDetails.getStatus())) {
                System.out.printf("Operation fail error: %s%n", operationDetails.getError().getMessage());
            }
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getOperation#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getOperationWithResponse(String)}
     */
    public void getOperationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getOperationWithResponse#string
        String operationId = "{operation_Id}";
        documentModelAdministrationAsyncClient.getOperationWithResponse(operationId).subscribe(response -> {
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            OperationDetails operationDetails = response.getValue();
            System.out.printf("Operation ID: %s%n", operationDetails.getOperationId());
            System.out.printf("Operation Kind: %s%n", operationDetails.getKind());
            System.out.printf("Operation Status: %s%n", operationDetails.getStatus());
            System.out.printf("Model ID created with this operation: %s%n",
                ((DocumentModelBuildOperationDetails) operationDetails).getResult().getModelId());
            if (OperationStatus.FAILED.equals(operationDetails.getStatus())) {
                System.out.printf("Operation fail error: %s%n", operationDetails.getError().getMessage());
            }
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getOperationWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#listOperations()}
     */
    public void listOperations() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listOperations
        documentModelAdministrationAsyncClient.listOperations()
            .subscribe(modelOperationSummary -> {
                System.out.printf("Operation ID: %s%n", modelOperationSummary.getOperationId());
                System.out.printf("Operation Status: %s%n", modelOperationSummary.getStatus());
                System.out.printf("Operation Created on: %s%n", modelOperationSummary.getCreatedOn());
                System.out.printf("Operation Percent completed: %d%n", modelOperationSummary.getPercentCompleted());
                System.out.printf("Operation Kind: %s%n", modelOperationSummary.getKind());
                System.out.printf("Operation Last updated on: %s%n", modelOperationSummary.getLastUpdatedOn());
                System.out.printf("Operation resource location: %s%n", modelOperationSummary.getResourceLocation());
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listOperations
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteDocumentClassifier(String)}
     */
    public void deleteClassifier() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentClassifier#string
        String classifierId = "{classifierId}";
        documentModelAdministrationAsyncClient.deleteDocumentClassifier(classifierId)
            .subscribe(ignored -> System.out.printf("Classifier ID: %s is deleted%n", classifierId));
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentClassifier#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteDocumentClassifierWithResponse(String)}
     */
    public void deleteClassifierWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentClassifierWithResponse#string
        String classifierId = "{classifierId}";
        documentModelAdministrationAsyncClient.deleteDocumentClassifierWithResponse(classifierId)
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                System.out.printf("Classifier ID: %s is deleted.%n", classifierId);
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentClassifierWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#listDocumentClassifiers()}
     */
    public void listClassifiers() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listDocumentClassifiers
        documentModelAdministrationAsyncClient.listDocumentClassifiers()
            .subscribe(documentModelInfo ->
                System.out.printf("Classifier ID: %s, Classifier description: %s, Created on: %s.%n",
                    documentModelInfo.getClassifierId(),
                    documentModelInfo.getDescription(),
                    documentModelInfo.getCreatedOn()));
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listDocumentClassifiers
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getDocumentClassifier(String)}
     */
    public void getDocumentClassifier() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentClassifier#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getDocumentClassifier(modelId).subscribe(documentClassifier -> {
            System.out.printf("Classifier ID: %s%n", documentClassifier.getClassifierId());
            System.out.printf("Classifier Description: %s%n", documentClassifier.getDescription());
            System.out.printf("Classifier Created on: %s%n", documentClassifier.getCreatedOn());
            documentClassifier.getDocTypes().forEach((key, documentTypeDetails) -> {
                System.out.printf("Blob Source container Url: %s", documentTypeDetails.getAzureBlobSource()
                    .getContainerUrl());
                System.out.printf("Blob File list Source container Url: %s", documentTypeDetails.
                    getAzureBlobFileListSource().getContainerUrl());
            });
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentClassifier#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getDocumentClassifierWithResponse(String)}
     */
    public void getClassifierWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentClassifierWithResponse#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getDocumentClassifierWithResponse(modelId).subscribe(response -> {
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            DocumentClassifierDetails documentClassifierDetails = response.getValue();
            System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());
            System.out.printf("Classifier Description: %s%n", documentClassifierDetails.getDescription());
            System.out.printf("Classifier Created on: %s%n", documentClassifierDetails.getCreatedOn());
            documentClassifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
                System.out.printf("Blob Source container Url: %s", documentTypeDetails.
                    getAzureBlobSource().getContainerUrl());
                System.out.printf("Blob File list Source container Url: %s", documentTypeDetails.
                    getAzureBlobFileListSource().getContainerUrl());
            });
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentClassifierWithResponse#string
    }


}
