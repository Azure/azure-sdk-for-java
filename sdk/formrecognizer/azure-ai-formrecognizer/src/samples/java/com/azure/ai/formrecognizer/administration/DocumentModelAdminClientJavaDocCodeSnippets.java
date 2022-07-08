// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.administration.models.ResourceInfo;
import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.ComposeModelOptions;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentBuildMode;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;
import com.azure.ai.formrecognizer.administration.models.ModelOperation;
import com.azure.ai.formrecognizer.administration.models.ModelOperationInfo;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.initialization
        DocumentModelAdministrationClient documentModelAdministrationClient =
            new DocumentModelAdministrationClientBuilder().buildClient();
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.initialization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildModel(com.azure.ai.formrecognizer.administration.models.ContentSource, DocumentBuildMode)}
     */
    public void beginBuildModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#String-DocumentBuildMode
        String trainingFilesUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        DocumentModelInfo documentModelInfo
            = documentModelAdministrationClient.beginBuildModel(trainingFilesUrl, DocumentBuildMode.TEMPLATE)
            .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
        System.out.printf("Model Created on: %s%n", documentModelInfo.getCreatedOn());
        documentModelInfo.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#String-DocumentBuildMode
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildModel(com.azure.ai.formrecognizer.administration.models.ContentSource, DocumentBuildMode, BuildModelOptions, Context)}
     * with options
     */
    public void beginBuildModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#string-DocumentBuildMode-BuildModelOptions-Context
        String trainingFilesUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        String modelId = "custom-model-id";
        String prefix = "Invoice";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        DocumentModelInfo documentModelInfo = documentModelAdministrationClient.beginBuildModel(trainingFilesUrl,
                DocumentBuildMode.TEMPLATE,
                new BuildModelOptions()
                    .setModelId(modelId)
                    .setDescription("model desc")
                    .setPrefix(prefix)
                    .setTags(attrs), Context.NONE)
            .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
        System.out.printf("Model Description: %s%n", documentModelInfo.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelInfo.getCreatedOn());
        System.out.printf("Model assigned tags: %s%n", documentModelInfo.getTags());
        documentModelInfo.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#string-DocumentBuildMode-BuildModelOptions-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getResourceInfo()}
     */
    public void getAccountProperties() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getResourceInfo
        ResourceInfo resourceInfo = documentModelAdministrationClient.getResourceInfo();
        System.out.printf("Max number of models that can be build for this account: %d%n",
            resourceInfo.getDocumentModelLimit());
        System.out.printf("Current count of built document analysis models: %d%n",
            resourceInfo.getDocumentModelCount());
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getResourceInfo
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getResourceInfoWithResponse(Context)}
     */
    public void getAccountPropertiesWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getResourceInfoWithResponse#Context
        Response<ResourceInfo> response =
            documentModelAdministrationClient.getResourceInfoWithResponse(Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        ResourceInfo resourceInfo = response.getValue();
        System.out.printf("Max number of models that can be build for this account: %d%n",
            resourceInfo.getDocumentModelLimit());
        System.out.printf("Current count of built document analysis models: %d%n",
            resourceInfo.getDocumentModelCount());
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getResourceInfoWithResponse#Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteModel(String)}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModel#string
        String modelId = "{custom-model-id}";
        documentModelAdministrationClient.deleteModel(modelId);
        System.out.printf("Model ID: %s is deleted.%n", modelId);
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteModelWithResponse(String, Context)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModelWithResponse#string-Context
        String modelId = "{custom-model-id}";
        Response<Void> response = documentModelAdministrationClient.deleteModelWithResponse(modelId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        System.out.printf("Model ID: %s is deleted.%n", modelId);
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModelWithResponse#string-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getCopyAuthorization()}
     */
    public void getCopyAuthorization() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getCopyAuthorization
        CopyAuthorization copyAuthorization = documentModelAdministrationClient.getCopyAuthorization();
        System.out.printf("Copy Authorization for model id: %s, access token: %s, expiration time: %s, "
                + "target resource ID; %s, target resource region: %s%n",
            copyAuthorization.getTargetModelId(),
            copyAuthorization.getAccessToken(),
            copyAuthorization.getExpiresOn(),
            copyAuthorization.getTargetResourceId(),
            copyAuthorization.getTargetResourceRegion()
        );
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getCopyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getCopyAuthorizationWithResponse(CopyAuthorizationOptions, Context)}
     */
    public void getCopyAuthorizationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getCopyAuthorizationWithResponse#CopyAuthorizationOptions-Context
        String modelId = "my-copied-model";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        Response<CopyAuthorization> copyAuthorizationResponse =
            documentModelAdministrationClient.getCopyAuthorizationWithResponse(
                new CopyAuthorizationOptions()
                    .setModelId(modelId)
                    .setDescription("model-desc")
                    .setTags(attrs),
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
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getCopyAuthorizationWithResponse#CopyAuthorizationOptions-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCreateComposedModel(List)}
     */
    public void beginCreateComposedModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginComposeModel#list
        String modelId1 = "{custom-model-id_1}";
        String modelId2 = "{custom-model-id_2}";
        final DocumentModelInfo documentModelInfo
            = documentModelAdministrationClient.beginCreateComposedModel(Arrays.asList(modelId1, modelId2))
            .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
        System.out.printf("Model Description: %s%n", documentModelInfo.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelInfo.getCreatedOn());
        documentModelInfo.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginComposeModel#list
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCreateComposedModel(List, ComposeModelOptions, Context)}
     * with options
     */
    public void beginCreateComposedModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginComposeModel#list-ComposeModelOptions-Context
        String modelId1 = "{custom-model-id_1}";
        String modelId2 = "{custom-model-id_2}";
        String modelId = "my-composed-model";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        final DocumentModelInfo documentModelInfo =
            documentModelAdministrationClient.beginCreateComposedModel(Arrays.asList(modelId1, modelId2),
                    new ComposeModelOptions()
                        .setModelId(modelId)
                        .setDescription("my composed model desc")
                        .setTags(attrs),
                    Context.NONE)
                .setPollInterval(Duration.ofSeconds(5))
                .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
        System.out.printf("Model Description: %s%n", documentModelInfo.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelInfo.getCreatedOn());
        System.out.printf("Model assigned tags: %s%n", documentModelInfo.getTags());
        documentModelInfo.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginComposeModel#list-ComposeModelOptions-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCopyModelTo(String, CopyAuthorization)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModelTo#string-copyAuthorization
        String copyModelId = "copy-model";
        // Get authorization to copy the model to target resource
        CopyAuthorization copyAuthorization = documentModelAdministrationClient.getCopyAuthorization();
        // Start copy operation from the source client
        DocumentModelInfo documentModelInfo =
            documentModelAdministrationClient.beginCopyModelTo(copyModelId, copyAuthorization).getFinalResult();
        System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
            documentModelInfo.getModelId(),
            documentModelInfo.getCreatedOn());
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModelTo#string-copyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCopyModelTo(String, CopyAuthorization, Context)}
     */
    public void beginCopyOverload() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModelTo#string-copyAuthorization-Context
        String copyModelId = "copy-model";
        // Get authorization to copy the model to target resource
        CopyAuthorization copyAuthorization = documentModelAdministrationClient.getCopyAuthorization();
        // Start copy operation from the source client
        DocumentModelInfo documentModelInfo =
            documentModelAdministrationClient.beginCopyModelTo(copyModelId, copyAuthorization, Context.NONE).getFinalResult();
        System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
            documentModelInfo.getModelId(),
            documentModelInfo.getCreatedOn());
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModelTo#string-copyAuthorization-Context
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
        String modelId = "{custom-model-id}";
        DocumentModelInfo documentModelInfo = documentModelAdministrationClient.getModel(modelId);
        System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
        System.out.printf("Model Description: %s%n", documentModelInfo.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelInfo.getCreatedOn());
        documentModelInfo.getDocTypes().forEach((key, docTypeInfo) -> {
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
        String modelId = "{custom-model-id}";
        Response<DocumentModelInfo> response = documentModelAdministrationClient.getModelWithResponse(modelId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        DocumentModelInfo documentModelInfo = response.getValue();
        System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
        System.out.printf("Model Description: %s%n", documentModelInfo.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelInfo.getCreatedOn());
        documentModelInfo.getDocTypes().forEach((key, docTypeInfo) -> {
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
        String operationId = "{operation-id}";
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
        String operationId = "{operation-id}";
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
