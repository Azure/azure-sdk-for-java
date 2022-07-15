// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.administration.models.ModelOperationDetails;
import com.azure.ai.formrecognizer.administration.models.ResourceDetails;
import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.ComposeModelOptions;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.administration.models.ModelOperationSummary;
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
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildModel(String, DocumentModelBuildMode)}
     */
    public void beginBuildModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#String-DocumentModelBuildMode
        String trainingFilesUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        DocumentModelDetails documentModelDetails
            = documentModelAdministrationClient.beginBuildModel(trainingFilesUrl,
                DocumentModelBuildMode.TEMPLATE)
            .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedOn());
        documentModelDetails.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#String-DocumentModelBuildMode
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildModel(String, DocumentModelBuildMode, BuildModelOptions, Context)}
     * with options
     */
    public void beginBuildModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#String-DocumentModelBuildMode-BuildModelOptions-Context
        String trainingFilesUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        String modelId = "custom-model-id";
        String prefix = "Invoice";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        DocumentModelDetails documentModelDetails
            = documentModelAdministrationClient.beginBuildModel(trainingFilesUrl,
                DocumentModelBuildMode.TEMPLATE,
                new BuildModelOptions()
                    .setModelId(modelId)
                    .setDescription("model desc")
                    .setPrefix(prefix)
                    .setTags(attrs),
                Context.NONE)
            .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedOn());
        System.out.printf("Model assigned tags: %s%n", documentModelDetails.getTags());
        documentModelDetails.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#String-DocumentModelBuildMode-BuildModelOptions-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getResourceDetails()}
     */
    public void getResourceInfo() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getResourceDetails
        ResourceDetails resourceDetails = documentModelAdministrationClient.getResourceDetails();
        System.out.printf("Max number of models that can be build for this account: %d%n",
            resourceDetails.getDocumentModelLimit());
        System.out.printf("Current count of built document analysis models: %d%n",
            resourceDetails.getDocumentModelCount());
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getResourceDetails
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getResourceDetailsWithResponse(Context)}
     */
    public void getResourceInfoWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getResourceDetailsWithResponse#Context
        Response<ResourceDetails> response =
            documentModelAdministrationClient.getResourceDetailsWithResponse(Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        ResourceDetails resourceDetails = response.getValue();
        System.out.printf("Max number of models that can be build for this account: %d%n",
            resourceDetails.getDocumentModelLimit());
        System.out.printf("Current count of built document analysis models: %d%n",
            resourceDetails.getDocumentModelCount());
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getResourceDetailsWithResponse#Context
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
     * Code snippet for {@link DocumentModelAdministrationClient#beginComposeModel(List)}
     */
    public void beginCreateComposedModel() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginComposeModel#list
        String modelId1 = "{custom-model-id_1}";
        String modelId2 = "{custom-model-id_2}";
        final DocumentModelDetails documentModelDetails
            = documentModelAdministrationClient.beginComposeModel(Arrays.asList(modelId1, modelId2))
            .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedOn());
        documentModelDetails.getDocTypes().forEach((key, docTypeInfo) -> {
            docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginComposeModel#list
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginComposeModel(List, ComposeModelOptions, Context)}
     * with options
     */
    public void beginCreateComposedModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginComposeModel#list-ComposeModelOptions-Context
        String modelId1 = "{custom-model-id_1}";
        String modelId2 = "{custom-model-id_2}";
        String modelId = "my-composed-model";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        final DocumentModelDetails documentModelDetails =
            documentModelAdministrationClient.beginComposeModel(Arrays.asList(modelId1, modelId2),
                    new ComposeModelOptions()
                        .setModelId(modelId)
                        .setDescription("my composed model desc")
                        .setTags(attrs),
                    Context.NONE)
                .setPollInterval(Duration.ofSeconds(5))
                .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedOn());
        System.out.printf("Model assigned tags: %s%n", documentModelDetails.getTags());
        documentModelDetails.getDocTypes().forEach((key, docTypeInfo) -> {
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
        DocumentModelDetails documentModelDetails =
            documentModelAdministrationClient.beginCopyModelTo(copyModelId, copyAuthorization).getFinalResult();
        System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
            documentModelDetails.getModelId(),
            documentModelDetails.getCreatedOn());
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
        DocumentModelDetails documentModelDetails =
            documentModelAdministrationClient.beginCopyModelTo(copyModelId, copyAuthorization, Context.NONE).getFinalResult();
        System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
            documentModelDetails.getModelId(),
            documentModelDetails.getCreatedOn());
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
        DocumentModelDetails documentModelDetails = documentModelAdministrationClient.getModel(modelId);
        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedOn());
        documentModelDetails.getDocTypes().forEach((key, docTypeInfo) -> {
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
        Response<DocumentModelDetails> response = documentModelAdministrationClient.getModelWithResponse(modelId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        DocumentModelDetails documentModelDetails = response.getValue();
        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedOn());
        documentModelDetails.getDocTypes().forEach((key, docTypeInfo) -> {
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
        ModelOperationDetails modelOperationDetails = documentModelAdministrationClient.getOperation(operationId);
        System.out.printf("Operation ID: %s%n", modelOperationDetails.getOperationId());
        System.out.printf("Operation Kind: %s%n", modelOperationDetails.getKind());
        System.out.printf("Operation Status: %s%n", modelOperationDetails.getStatus());
        System.out.printf("Model ID created with this operation: %s%n", modelOperationDetails.getModelId());
        if (ModelOperationStatus.FAILED.equals(modelOperationDetails.getStatus())) {
            System.out.printf("Operation fail error: %s%n", modelOperationDetails.getError().getMessage());
        }
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperation#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getOperationWithResponse(String, Context)}
     */
    public void getOperationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperationWithResponse#string-Context
        String operationId = "{operation-id}";
        Response<ModelOperationDetails> response =
            documentModelAdministrationClient.getOperationWithResponse(operationId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        ModelOperationDetails modelOperationDetails = response.getValue();
        System.out.printf("Operation ID: %s%n", modelOperationDetails.getOperationId());
        System.out.printf("Operation Kind: %s%n", modelOperationDetails.getKind());
        System.out.printf("Operation Status: %s%n", modelOperationDetails.getStatus());
        System.out.printf("Model ID created with this operation: %s%n", modelOperationDetails.getModelId());
        if (ModelOperationStatus.FAILED.equals(modelOperationDetails.getStatus())) {
            System.out.printf("Operation fail error: %s%n", modelOperationDetails.getError().getMessage());
        }
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperationWithResponse#string-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listOperations()}
     */
    public void listOperations() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations
        PagedIterable<ModelOperationSummary>
            modelOperationInfo = documentModelAdministrationClient.listOperations();
        modelOperationInfo.forEach(modelOperationSummary -> {
            System.out.printf("Operation ID: %s%n", modelOperationSummary.getOperationId());
            System.out.printf("Operation Status: %s%n", modelOperationSummary.getStatus());
            System.out.printf("Operation Created on: %s%n", modelOperationSummary.getCreatedOn());
            System.out.printf("Operation Percent completed: %d%n", modelOperationSummary.getPercentCompleted());
            System.out.printf("Operation Kind: %s%n", modelOperationSummary.getKind());
            System.out.printf("Operation Last updated on: %s%n", modelOperationSummary.getLastUpdatedOn());
            System.out.printf("Operation resource location: %s%n", modelOperationSummary.getResourceLocation());
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listOperations(Context)}
     */
    public void listOperationsWithContext() {
        // BEGIN: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations#Context
        PagedIterable<ModelOperationSummary>
            modelOperationInfo = documentModelAdministrationClient.listOperations(Context.NONE);
        modelOperationInfo.forEach(modelOperationSummary -> {
            System.out.printf("Operation ID: %s%n", modelOperationSummary.getOperationId());
            System.out.printf("Operation Status: %s%n", modelOperationSummary.getStatus());
            System.out.printf("Operation Created on: %s%n", modelOperationSummary.getCreatedOn());
            System.out.printf("Operation Percent completed: %d%n", modelOperationSummary.getPercentCompleted());
            System.out.printf("Operation Kind: %s%n", modelOperationSummary.getKind());
            System.out.printf("Operation Last updated on: %s%n", modelOperationSummary.getLastUpdatedOn());
            System.out.printf("Operation resource location: %s%n", modelOperationSummary.getResourceLocation());
        });
        // END: com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations#Context
    }
}
