// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ComposeModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentOperationStatus;
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
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.initialization
        DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
            new DocumentModelAdministrationClientBuilder().buildAsyncClient();
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.initialization
    }

    /**
     * Code snippet for creating a {@link DocumentModelAdministrationAsyncClient} with pipeline
     */
    public void createDocumentModelAdministrationAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
            new DocumentModelAdministrationClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildAsyncClient();
        // END:  com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.pipeline.instantiation
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginBuildModel(String, DocumentModelBuildMode)}
     */
    public void beginBuildModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-DocumentModelBuildMode
        String blobContainerUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        documentModelAdministrationAsyncClient.beginBuildModel(blobContainerUrl,
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-DocumentModelBuildMode
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginBuildModel(String, DocumentModelBuildMode, String, BuildModelOptions)}
     * with options
     */
    public void beginBuildModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-DocumentModelBuildMode-String-BuildModelOptions
        String blobContainerUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        String modelId = "model-id";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");
        String prefix = "Invoice";

        documentModelAdministrationAsyncClient.beginBuildModel(blobContainerUrl,
                DocumentModelBuildMode.TEMPLATE,
                prefix,
                new BuildModelOptions()
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-DocumentModelBuildMode-String-BuildModelOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteModel}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.deleteModel#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.deleteModel(modelId)
            .subscribe(ignored -> System.out.printf("Model ID: %s is deleted%n", modelId));
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.deleteModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteModelWithResponse(String)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.deleteModelWithResponse#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.deleteModelWithResponse(modelId)
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                System.out.printf("Model ID: %s is deleted.%n", modelId);
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.deleteModelWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getCopyAuthorization()}
     */
    public void getCopyAuthorization() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorization
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getCopyAuthorizationWithResponse(CopyAuthorizationOptions)}
     */
    public void getCopyAuthorizationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorizationWithResponse#CopyAuthorizationOptions
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorizationWithResponse#CopyAuthorizationOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getResourceDetails()}
     */
    public void getResourceInfo() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getResourceDetails
        documentModelAdministrationAsyncClient.getResourceDetails()
            .subscribe(resourceInfo -> {
                System.out.printf("Max number of models that can be build for this account: %d%n",
                    resourceInfo.getCustomDocumentModelLimit());
                System.out.printf("Current count of built document analysis models: %d%n",
                    resourceInfo.getCustomDocumentModelCount());
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getResourceDetails
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getResourceDetailsWithResponse()}
     */
    public void getResourceInfoWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getResourceDetailsWithResponse
        documentModelAdministrationAsyncClient.getResourceDetailsWithResponse()
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                ResourceDetails resourceDetails = response.getValue();
                System.out.printf("Max number of models that can be build for this account: %d%n",
                    resourceDetails.getCustomDocumentModelLimit());
                System.out.printf("Current count of built document analysis models: %d%n",
                    resourceDetails.getCustomDocumentModelCount());
            });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getResourceDetailsWithResponse
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginComposeModel(List)}
     */
    public void beginCreateComposedModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.beginComposeModel#list
        String modelId1 = "{model_Id_1}";
        String modelId2 = "{model_Id_2}";
        documentModelAdministrationAsyncClient.beginComposeModel(Arrays.asList(modelId1, modelId2)
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.beginComposeModel#list
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginComposeModel(List, ComposeModelOptions)}
     * with options
     */
    public void beginCreateComposedModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.beginComposeModel#list-composeModelOptions
        String modelId1 = "{model_Id_1}";
        String modelId2 = "{model_Id_2}";
        String modelId = "my-composed-model";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        documentModelAdministrationAsyncClient.beginComposeModel(Arrays.asList(modelId1, modelId2),
                new ComposeModelOptions()
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.beginComposeModel#list-composeModelOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginCopyModelTo(String, CopyAuthorization)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.beginCopyModelTo#string-copyAuthorization
        String copyModelId = "copy-model";
        // Get authorization to copy the model to target resource
        documentModelAdministrationAsyncClient.getCopyAuthorization()
            // Start copy operation from the source client
            // The ID of the model that needs to be copied to the target resource
            .subscribe(copyAuthorization -> documentModelAdministrationAsyncClient.beginCopyModelTo(copyModelId,
                    copyAuthorization)
                .filter(pollResponse -> pollResponse.getStatus().isComplete())
                .flatMap(AsyncPollResponse::getFinalResult)
                .subscribe(documentModel ->
                    System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
                        documentModel.getModelId(),
                        documentModel.getCreatedOn())));

        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.beginCopyModelTo#string-copyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#listModels()}
     */
    public void listModels() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.listModels
        documentModelAdministrationAsyncClient.listModels()
            .subscribe(documentModelInfo ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModelInfo.getModelId(),
                    documentModelInfo.getDescription(),
                    documentModelInfo.getCreatedOn()));
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.listModels
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getModel(String)}
     */
    public void getModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getModel#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getModel(modelId).subscribe(documentModel -> {
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getModelWithResponse(String)}
     */
    public void getModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getModelWithResponse#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getModelWithResponse(modelId).subscribe(response -> {
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getModelWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getModel(String)}
     */
    public void getOperation() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getOperation#string
        String operationId = "{operation_Id}";
        documentModelAdministrationAsyncClient.getOperation(operationId).subscribe(modelOperationDetails -> {
            System.out.printf("Operation ID: %s%n", modelOperationDetails.getOperationId());
            System.out.printf("Operation Kind: %s%n", modelOperationDetails.getKind());
            System.out.printf("Operation Status: %s%n", modelOperationDetails.getStatus());
            System.out.printf("Model ID created with this operation: %s%n",
                modelOperationDetails.getResult().getModelId());
            if (DocumentOperationStatus.FAILED.equals(modelOperationDetails.getStatus())) {
                System.out.printf("Operation fail error: %s%n", modelOperationDetails.getError().getMessage());
            }
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getOperation#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getOperationWithResponse(String)}
     */
    public void getOperationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getOperationWithResponse#string
        String operationId = "{operation_Id}";
        documentModelAdministrationAsyncClient.getOperationWithResponse(operationId).subscribe(response -> {
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            DocumentModelOperationDetails documentModelOperationDetails = response.getValue();
            System.out.printf("Operation ID: %s%n", documentModelOperationDetails.getOperationId());
            System.out.printf("Operation Kind: %s%n", documentModelOperationDetails.getKind());
            System.out.printf("Operation Status: %s%n", documentModelOperationDetails.getStatus());
            System.out.printf("Model ID created with this operation: %s%n",
                documentModelOperationDetails.getResult().getModelId());
            if (DocumentOperationStatus.FAILED.equals(documentModelOperationDetails.getStatus())) {
                System.out.printf("Operation fail error: %s%n", documentModelOperationDetails.getError().getMessage());
            }
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.getOperationWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#listOperations()}
     */
    public void listOperations() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.listOperations
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient.listOperations
    }
}
