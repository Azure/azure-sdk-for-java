// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.administration.models.AccountProperties;
import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.administration.models.CreateComposedModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.administration.models.ModelOperation;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.polling.AsyncPollResponse;

import java.util.Arrays;
import java.util.List;

/**
 * Code snippet for {@link DocumentModelAdministrationAsyncClient}
 */
public class DocumentModelAdminAsyncClientJavaDocCodeSnippets {
    private final DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
        new DocumentModelAdministrationClientBuilder().buildAsyncClient();

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient} initialization
     */
    public void formTrainingAsyncClientInInitialization() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.initialization
        DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
            new DocumentModelAdministrationClientBuilder().buildAsyncClient();
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.initialization
    }

    /**
     * Code snippet for creating a {@link DocumentModelAdministrationAsyncClient} with pipeline
     */
    public void createDocumentTrainingAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
            new DocumentModelAdministrationClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildAsyncClient();
        // END:  com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.pipeline.instantiation
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginBuildModel(String, String)}
     */
    public void beginBuildModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-String
        String trainingFilesUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        documentModelAdministrationAsyncClient.beginBuildModel(trainingFilesUrl, "model-name")
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {
                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
                documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
                    docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
                    });
                });
            });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-String
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginBuildModel(String, String, BuildModelOptions)}
     * with options
     */
    public void beginBuildModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-String-BuildModelOptions
        String trainingFilesUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        documentModelAdministrationAsyncClient.beginBuildModel(trainingFilesUrl,
                "model-name",
                new BuildModelOptions()
                    .setDescription("model desc")
                    .setPrefix("Invoice"))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {
                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Description: %s%n", documentModel.getDescription());
                System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
                documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
                    docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
                    });
                });
            });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-String-BuildModelOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteModel}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.deleteModel#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.deleteModel(modelId)
            .subscribe(ignored -> System.out.printf("Model ID: %s is deleted%n", modelId));
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.deleteModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#deleteModelWithResponse(String)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.deleteModelWithResponse#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.deleteModelWithResponse(modelId)
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                System.out.printf("Model ID: %s is deleted.%n", modelId);
            });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.deleteModelWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getCopyAuthorization(String)}
     */
    public void getCopyAuthorization() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorization#string
        String modelId = "my-copied-model";
        documentModelAdministrationAsyncClient.getCopyAuthorization(modelId)
            .subscribe(copyAuthorization ->
                System.out.printf("Copy Authorization for model id: %s, access token: %s, expiration time: %s, "
                        + "target resource ID; %s, target resource region: %s%n",
                    copyAuthorization.getTargetModelId(),
                    copyAuthorization.getAccessToken(),
                    copyAuthorization.getExpiresOn(),
                    copyAuthorization.getTargetResourceId(),
                    copyAuthorization.getTargetResourceRegion()
                ));
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorization#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getCopyAuthorizationWithResponse(String, CopyAuthorizationOptions)}
     */
    public void getCopyAuthorizationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorizationWithResponse#string-CopyAuthorizationOptions
        String modelId = "my-copied-model";
        documentModelAdministrationAsyncClient.getCopyAuthorizationWithResponse(modelId,
                new CopyAuthorizationOptions().setDescription("model desc"))
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
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorizationWithResponse#string-CopyAuthorizationOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getAccountProperties()}
     */
    public void getAccountProperties() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getAccountProperties
        documentModelAdministrationAsyncClient.getAccountProperties()
            .subscribe(accountProperties -> {
                System.out.printf("Max number of models that can be build for this account: %d%n",
                    accountProperties.getDocumentModelLimit());
                System.out.printf("Current count of built document analysis models: %d%n",
                    accountProperties.getDocumentModelCount());
            });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getAccountProperties
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getAccountPropertiesWithResponse()}
     */
    public void getAccountPropertiesWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getAccountPropertiesWithResponse
        documentModelAdministrationAsyncClient.getAccountPropertiesWithResponse()
            .subscribe(response -> {
                System.out.printf("Response Status Code: %d.", response.getStatusCode());
                AccountProperties accountProperties = response.getValue();
                System.out.printf("Max number of models that can be build for this account: %d%n",
                    accountProperties.getDocumentModelLimit());
                System.out.printf("Current count of built document analysis models: %d%n",
                    accountProperties.getDocumentModelCount());
            });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getAccountPropertiesWithResponse
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginCreateComposedModel(List, String)}
     */
    public void beginCreateComposedModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCreateComposedModel#list-String
        String modelId1 = "{model_Id_1}";
        String modelId2 = "{model_Id_2}";
        documentModelAdministrationAsyncClient.beginCreateComposedModel(Arrays.asList(modelId1, modelId2),
                "my-composed-model")
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {
                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
                documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
                    docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
                    });
                });
            });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCreateComposedModel#list-String
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginCreateComposedModel(List, String, CreateComposedModelOptions)}
     * with options
     */
    public void beginCreateComposedModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCreateComposedModel#list-String-createComposedModelOptions
        String modelId1 = "{model_Id_1}";
        String modelId2 = "{model_Id_2}";
        documentModelAdministrationAsyncClient.beginCreateComposedModel(Arrays.asList(modelId1, modelId2),
                "my-composed-model",
                new CreateComposedModelOptions().setDescription("model-desc"))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(documentModel -> {
                System.out.printf("Model ID: %s%n", documentModel.getModelId());
                System.out.printf("Model Description: %s%n", documentModel.getDescription());
                System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
                documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
                    docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                        System.out.printf("Field: %s", field);
                        System.out.printf("Field type: %s", documentFieldSchema.getType());
                        System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
                    });
                });
            });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCreateComposedModel#list-String-createComposedModelOptions
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#beginCopyModel(String, CopyAuthorization)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCopyModel#string-copyAuthorization
        String copyModelId = "copy-model";
        String targetModelId = "my-copied-model-id";
        // Get authorization to copy the model to target resource
        documentModelAdministrationAsyncClient.getCopyAuthorization(targetModelId)
            // Start copy operation from the source client
            // The ID of the model that needs to be copied to the target resource
            .subscribe(copyAuthorization -> documentModelAdministrationAsyncClient.beginCopyModel(copyModelId,
                    copyAuthorization)
                .filter(pollResponse -> pollResponse.getStatus().isComplete())
                .flatMap(AsyncPollResponse::getFinalResult)
                .subscribe(documentModel ->
                    System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
                        documentModel.getModelId(),
                        documentModel.getCreatedOn())));

        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCopyModel#string-copyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#listModels()}
     */
    public void listModels() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listModels
        documentModelAdministrationAsyncClient.listModels()
            .subscribe(documentModelInfo ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModelInfo.getModelId(),
                    documentModelInfo.getDescription(),
                    documentModelInfo.getCreatedOn()));
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listModels
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getModel(String)}
     */
    public void getModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getModel#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getModel(modelId).subscribe(documentModel -> {
            System.out.printf("Model ID: %s%n", documentModel.getModelId());
            System.out.printf("Model Description: %s%n", documentModel.getDescription());
            System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
            documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
                docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                    System.out.printf("Field: %s", field);
                    System.out.printf("Field type: %s", documentFieldSchema.getType());
                    System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
                });
            });
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getModelWithResponse(String)}
     */
    public void getModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getModelWithResponse#string
        String modelId = "{model_id}";
        documentModelAdministrationAsyncClient.getModelWithResponse(modelId).subscribe(response -> {
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            DocumentModel documentModel = response.getValue();
            System.out.printf("Model ID: %s%n", documentModel.getModelId());
            System.out.printf("Model Description: %s%n", documentModel.getDescription());
            System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
            documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
                docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                    System.out.printf("Field: %s", field);
                    System.out.printf("Field type: %s", documentFieldSchema.getType());
                    System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
                });
            });
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getModelWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getModel(String)}
     */
    public void getOperation() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getOperation#string
        String operationId = "{operation_Id}";
        documentModelAdministrationAsyncClient.getOperation(operationId).subscribe(modelOperation -> {
            System.out.printf("Operation ID: %s%n", modelOperation.getOperationId());
            System.out.printf("Operation Kind: %s%n", modelOperation.getKind());
            System.out.printf("Operation Status: %s%n", modelOperation.getStatus());
            System.out.printf("Model ID created with this operation: %s%n", modelOperation.getModelId());
            if (ModelOperationStatus.FAILED.equals(modelOperation.getStatus())) {
                System.out.printf("Operation fail error: %s%n", modelOperation.getError().getMessage());
            }
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getOperation#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#getOperationWithResponse(String)}
     */
    public void getOperationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getOperationWithResponse#string
        String operationId = "{operation_Id}";
        documentModelAdministrationAsyncClient.getOperationWithResponse(operationId).subscribe(response -> {
            System.out.printf("Response Status Code: %d.", response.getStatusCode());
            ModelOperation modelOperation = response.getValue();
            System.out.printf("Operation ID: %s%n", modelOperation.getOperationId());
            System.out.printf("Operation Kind: %s%n", modelOperation.getKind());
            System.out.printf("Operation Status: %s%n", modelOperation.getStatus());
            System.out.printf("Model ID created with this operation: %s%n", modelOperation.getModelId());
            if (ModelOperationStatus.FAILED.equals(modelOperation.getStatus())) {
                System.out.printf("Operation fail error: %s%n", modelOperation.getError().getMessage());
            }
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getOperationWithResponse#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationAsyncClient#listOperations()}
     */
    public void listOperations() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listOperations
        documentModelAdministrationAsyncClient.listOperations()
            .subscribe(modelOperation -> {
                System.out.printf("Operation ID: %s%n", modelOperation.getOperationId());
                System.out.printf("Operation Status: %s%n", modelOperation.getStatus());
                System.out.printf("Operation Created on: %s%n", modelOperation.getCreatedOn());
                System.out.printf("Operation Percent completed: %d%n", modelOperation.getPercentCompleted());
                System.out.printf("Operation Kind: %s%n", modelOperation.getKind());
                System.out.printf("Operation Last updated on: %s%n", modelOperation.getLastUpdatedOn());
                System.out.printf("Operation resource location: %s%n", modelOperation.getResourceLocation());
            });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listOperations
    }
}
