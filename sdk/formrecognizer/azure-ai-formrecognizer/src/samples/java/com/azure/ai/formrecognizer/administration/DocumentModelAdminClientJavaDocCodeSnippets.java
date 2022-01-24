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
import com.azure.ai.formrecognizer.administration.models.ModelOperationInfo;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Code snippet for {@link DocumentModelAdministrationClient}
 */
public class DocumentModelAdminClientJavaDocCodeSnippets {
    private final DocumentModelAdministrationClient documentModelAdministrationClient =
        new DocumentModelAdministrationClientBuilder().buildClient();
    private final DocumentModelAdministrationClient targetDocumentModelAdministrationClient =
        new DocumentModelAdministrationClientBuilder().buildClient();

    /**
     * Code snippet for {@link DocumentModelAdministrationClient} initialization
     */
    public void formTrainingClientInInitialization() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.initialization
        DocumentModelAdministrationClient documentModelAdministrationClient =
            new DocumentModelAdministrationClientBuilder().buildClient();
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.initialization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildModel(String, String)}
     */
    public void beginBuildModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#String-String
        String trainingFilesUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        DocumentModel documentModel =
            documentModelAdministrationClient.beginBuildModel(trainingFilesUrl, "my-model").getFinalResult();
        System.out.printf("Model ID: %s%n", documentModel.getModelId());
        System.out.printf("Model Created on: %s%n", documentModel.getCreatedOn());
        documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#String-String
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildModel(String, String, BuildModelOptions, Context)}
     * with options
     */
    public void beginBuildModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#string-String-BuildModelOptions-Context
        String trainingFilesUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        String prefix = "Invoice";

        DocumentModel documentModel = documentModelAdministrationClient.beginBuildModel(trainingFilesUrl, "my-model",
                new BuildModelOptions()
                    .setDescription("model desc")
                    .setPrefix(prefix), Context.NONE)
            .getFinalResult();

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
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#string-String-BuildModelOptions-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getAccountProperties()}
     */
    public void getAccountProperties() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getAccountProperties
        AccountProperties accountProperties = documentModelAdministrationClient.getAccountProperties();
        System.out.printf("Max number of models that can be build for this account: %d%n",
            accountProperties.getDocumentModelLimit());
        System.out.printf("Current count of built document analysis models: %d%n",
            accountProperties.getDocumentModelCount());
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getAccountProperties
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getAccountPropertiesWithResponse(Context)}
     */
    public void getAccountPropertiesWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getAccountPropertiesWithResponse#Context
        Response<AccountProperties> response =
            documentModelAdministrationClient.getAccountPropertiesWithResponse(Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        AccountProperties accountProperties = response.getValue();
        System.out.printf("Max number of models that can be build for this account: %d%n",
            accountProperties.getDocumentModelLimit());
        System.out.printf("Current count of built document analysis models: %d%n",
            accountProperties.getDocumentModelCount());
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getAccountPropertiesWithResponse#Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteModel(String)}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModel#string
        String modelId = "{model_id}";
        documentModelAdministrationClient.deleteModel(modelId);
        System.out.printf("Model ID: %s is deleted.%n", modelId);
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteModelWithResponse(String, Context)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModelWithResponse#string-Context
        String modelId = "{model_id}";
        Response<Void> response = documentModelAdministrationClient.deleteModelWithResponse(modelId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        System.out.printf("Model ID: %s is deleted.%n", modelId);
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModelWithResponse#string-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getCopyAuthorization(String)}
     */
    public void getCopyAuthorization() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getCopyAuthorization#string
        String modelId = "my-copied-model";
        CopyAuthorization copyAuthorization = documentModelAdministrationClient.getCopyAuthorization(modelId);
        System.out.printf("Copy Authorization for model id: %s, access token: %s, expiration time: %s, "
                + "target resource ID; %s, target resource region: %s%n",
            copyAuthorization.getTargetModelId(),
            copyAuthorization.getAccessToken(),
            copyAuthorization.getExpiresOn(),
            copyAuthorization.getTargetResourceId(),
            copyAuthorization.getTargetResourceRegion()
        );
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getCopyAuthorization#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getCopyAuthorizationWithResponse(String, CopyAuthorizationOptions, Context)}
     */
    public void getCopyAuthorizationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getCopyAuthorizationWithResponse#string-CopyAuthorizationOptions-Context
        String modelId = "my-copied-model";
        Response<CopyAuthorization> copyAuthorizationResponse =
            documentModelAdministrationClient.getCopyAuthorizationWithResponse(modelId,
                new CopyAuthorizationOptions().setDescription("model-desc"),
                Context.NONE);

        System.out.printf("Copy Authorization operation returned with status: %s",
            copyAuthorizationResponse.getStatusCode());
        CopyAuthorization copyAuthorization = copyAuthorizationResponse.getValue();
        System.out.printf("Copy Authorization for model id: %s, access token: %s, "
                + "expiration time: %s, target resource ID; %s, target resource region: %s%n",
            copyAuthorization.getTargetModelId(),
            copyAuthorization.getAccessToken(),
            copyAuthorization.getExpiresOn(),
            copyAuthorization.getTargetResourceId(),
            copyAuthorization.getTargetResourceRegion()
        );
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getCopyAuthorizationWithResponse#string-CopyAuthorizationOptions-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCreateComposedModel(List, String)}
     */
    public void beginCreateComposedModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCreateComposedModel#list-String
        String modelId1 = "{model_Id_1}";
        String modelId2 = "{model_Id_2}";
        String modelId = "my-composed-model";
        final DocumentModel documentModel
            = documentModelAdministrationClient.beginCreateComposedModel(Arrays.asList(modelId1, modelId2), modelId)
            .getFinalResult();

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
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCreateComposedModel#list-String
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCreateComposedModel(List, String, CreateComposedModelOptions, Context)}
     * with options
     */
    public void beginCreateComposedModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCreateComposedModel#list-String-CreateComposedModelOptions-Context
        String modelId1 = "{model_Id_1}";
        String modelId2 = "{model_Id_2}";
        String modelId = "my-composed-model";

        final DocumentModel documentModel =
            documentModelAdministrationClient.beginCreateComposedModel(Arrays.asList(modelId1, modelId2), modelId,
                    new CreateComposedModelOptions()
                        .setDescription("my composed model name"),
                    Context.NONE)
                .setPollInterval(Duration.ofSeconds(5))
                .getFinalResult();

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
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCreateComposedModel#list-String-CreateComposedModelOptions-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCopyModel(String, CopyAuthorization)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModel#string-copyAuthorization
        String copyModelId = "copy-model";
        String targetModelId = "my-copied-model-id";
        // Get authorization to copy the model to target resource
        CopyAuthorization copyAuthorization = documentModelAdministrationClient.getCopyAuthorization(copyModelId);
        // Start copy operation from the source client
        DocumentModel documentModel =
            documentModelAdministrationClient.beginCopyModel(copyModelId, copyAuthorization).getFinalResult();
        System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
            documentModel.getModelId(),
            documentModel.getCreatedOn());
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModel#string-copyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCopyModel(String, CopyAuthorization, Context)}
     */
    public void beginCopyOverload() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModel#string-copyAuthorization-Context
        String copyModelId = "copy-model";
        String targetModelId = "my-copied-model-id";
        // Get authorization to copy the model to target resource
        CopyAuthorization copyAuthorization = documentModelAdministrationClient.getCopyAuthorization(targetModelId);
        // Start copy operation from the source client
        DocumentModel documentModel =
            documentModelAdministrationClient.beginCopyModel(copyModelId, copyAuthorization, Context.NONE).getFinalResult();
        System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
            documentModel.getModelId(),
            documentModel.getCreatedOn());
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModel#string-copyAuthorization-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listModels()}
     */
    public void listModels() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listModels
        documentModelAdministrationClient.listModels()
            .forEach(documentModel ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModel.getModelId(),
                    documentModel.getDescription(),
                    documentModel.getCreatedOn())
            );
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listModels
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listModels(Context)}
     */
    public void listModelsWithContext() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listModels#Context
        documentModelAdministrationClient.listModels(Context.NONE)
            .forEach(documentModel ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModel.getModelId(),
                    documentModel.getDescription(),
                    documentModel.getCreatedOn())
            );
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listModels#Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getModel(String)}
     */
    public void getModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getModel#string
        String modelId = "{model_id}";
        DocumentModel documentModel = documentModelAdministrationClient.getModel(modelId);
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
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getModelWithResponse(String, Context)}
     */
    public void getModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getModelWithResponse#string-Context
        String modelId = "{model_id}";
        Response<DocumentModel> response = documentModelAdministrationClient.getModelWithResponse(modelId, Context.NONE);
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
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getModelWithResponse#string-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getOperation(String)}
     */
    public void getOperation() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperation#string
        String operationId = "{operation_Id}";
        ModelOperation modelOperation = documentModelAdministrationClient.getOperation(operationId);
        System.out.printf("Operation ID: %s%n", modelOperation.getOperationId());
        System.out.printf("Operation Kind: %s%n", modelOperation.getKind());
        System.out.printf("Operation Status: %s%n", modelOperation.getStatus());
        System.out.printf("Model ID created with this operation: %s%n", modelOperation.getModelId());
        if (ModelOperationStatus.FAILED.equals(modelOperation.getStatus())) {
            System.out.printf("Operation fail error: %s%n", modelOperation.getError().getMessage());
        }
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperation#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getOperationWithResponse(String, Context)}
     */
    public void getOperationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperationWithResponse#string-Context
        String operationId = "{operation_Id}";
        Response<ModelOperation> response =
            documentModelAdministrationClient.getOperationWithResponse(operationId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        ModelOperation modelOperation = response.getValue();
        System.out.printf("Operation ID: %s%n", modelOperation.getOperationId());
        System.out.printf("Operation Kind: %s%n", modelOperation.getKind());
        System.out.printf("Operation Status: %s%n", modelOperation.getStatus());
        System.out.printf("Model ID created with this operation: %s%n", modelOperation.getModelId());
        if (ModelOperationStatus.FAILED.equals(modelOperation.getStatus())) {
            System.out.printf("Operation fail error: %s%n", modelOperation.getError().getMessage());
        }
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperationWithResponse#string-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listOperations()}
     */
    public void listOperations() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations
        PagedIterable<ModelOperationInfo>
            modelOperationInfo = documentModelAdministrationClient.listOperations();
        modelOperationInfo.forEach(modelOperation -> {
            System.out.printf("Operation ID: %s%n", modelOperation.getOperationId());
            System.out.printf("Operation Status: %s%n", modelOperation.getStatus());
            System.out.printf("Operation Created on: %s%n", modelOperation.getCreatedOn());
            System.out.printf("Operation Percent completed: %d%n", modelOperation.getPercentCompleted());
            System.out.printf("Operation Kind: %s%n", modelOperation.getKind());
            System.out.printf("Operation Last updated on: %s%n", modelOperation.getLastUpdatedOn());
            System.out.printf("Operation resource location: %s%n", modelOperation.getResourceLocation());
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listOperations(Context)}
     */
    public void listOperationsWithContext() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations#Context
        PagedIterable<ModelOperationInfo>
            modelOperationInfo = documentModelAdministrationClient.listOperations(Context.NONE);
        modelOperationInfo.forEach(modelOperation -> {
            System.out.printf("Operation ID: %s%n", modelOperation.getOperationId());
            System.out.printf("Operation Status: %s%n", modelOperation.getStatus());
            System.out.printf("Operation Created on: %s%n", modelOperation.getCreatedOn());
            System.out.printf("Operation Percent completed: %d%n", modelOperation.getPercentCompleted());
            System.out.printf("Operation Kind: %s%n", modelOperation.getKind());
            System.out.printf("Operation Last updated on: %s%n", modelOperation.getLastUpdatedOn());
            System.out.printf("Operation resource location: %s%n", modelOperation.getResourceLocation());
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations#Context
    }
}
