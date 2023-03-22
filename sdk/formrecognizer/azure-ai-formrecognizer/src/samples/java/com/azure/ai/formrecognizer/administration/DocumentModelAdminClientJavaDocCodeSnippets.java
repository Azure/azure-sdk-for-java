// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.AzureBlobContentSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentClassifierOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ClassifierDocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ComposeDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelCopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationStatus;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationSummary;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ResourceDetails;
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
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.initialization
        DocumentModelAdministrationClient documentModelAdministrationClient =
            new DocumentModelAdministrationClientBuilder().buildClient();
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.initialization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildDocumentModel(String, DocumentModelBuildMode)}
     */
    public void beginBuildModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#String-BuildMode
        String blobContainerUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        DocumentModelDetails documentModelDetails
            = documentModelAdministrationClient.beginBuildDocumentModel(blobContainerUrl,
                DocumentModelBuildMode.TEMPLATE)
            .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedOn());
        documentModelDetails.getDocumentTypes().forEach((key, documentTypeDetails) -> {
            documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#String-BuildMode
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildDocumentModel(String, DocumentModelBuildMode, String, BuildDocumentModelOptions, Context)}
     * with options
     */
    public void beginBuildModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#String-BuildMode-String-Options-Context
        String blobContainerUrl = "{SAS-URL-of-your-container-in-blob-storage}";
        String modelId = "custom-model-id";
        String prefix = "Invoice";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        DocumentModelDetails documentModelDetails
            = documentModelAdministrationClient.beginBuildDocumentModel(blobContainerUrl,
                DocumentModelBuildMode.TEMPLATE,
                prefix,
                new BuildDocumentModelOptions()
                    .setModelId(modelId)
                    .setDescription("model desc")
                    .setTags(attrs),
                Context.NONE)
            .getFinalResult();

        System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
        System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
        System.out.printf("Model Created on: %s%n", documentModelDetails.getCreatedOn());
        System.out.printf("Model assigned tags: %s%n", documentModelDetails.getTags());
        documentModelDetails.getDocumentTypes().forEach((key, documentTypeDetails) -> {
            documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentModel#String-BuildMode-String-Options-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getResourceDetails()}
     */
    public void getResourceInfo() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getResourceDetails
        ResourceDetails resourceDetails = documentModelAdministrationClient.getResourceDetails();
        System.out.printf("Max number of models that can be build for this account: %d%n",
            resourceDetails.getCustomDocumentModelLimit());
        System.out.printf("Current count of built document analysis models: %d%n",
            resourceDetails.getCustomDocumentModelCount());
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getResourceDetails
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getResourceDetailsWithResponse(Context)}
     */
    public void getResourceInfoWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getResourceDetailsWithResponse#Context
        Response<ResourceDetails> response =
            documentModelAdministrationClient.getResourceDetailsWithResponse(Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        ResourceDetails resourceDetails = response.getValue();
        System.out.printf("Max number of models that can be build for this account: %d%n",
            resourceDetails.getCustomDocumentModelLimit());
        System.out.printf("Current count of built document analysis models: %d%n",
            resourceDetails.getCustomDocumentModelCount());
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getResourceDetailsWithResponse#Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteDocumentModel(String)}
     */
    public void deleteModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentModel#string
        String modelId = "{custom-model-id}";
        documentModelAdministrationClient.deleteDocumentModel(modelId);
        System.out.printf("Model ID: %s is deleted.%n", modelId);
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteDocumentModelWithResponse(String, Context)}
     */
    public void deleteModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentModelWithResponse#string-Context
        String modelId = "{custom-model-id}";
        Response<Void> response
            = documentModelAdministrationClient.deleteDocumentModelWithResponse(modelId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        System.out.printf("Model ID: %s is deleted.%n", modelId);
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentModelWithResponse#string-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getCopyAuthorization()}
     */
    public void getCopyAuthorization() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getCopyAuthorization
        DocumentModelCopyAuthorization documentModelCopyAuthorization
            = documentModelAdministrationClient.getCopyAuthorization();
        System.out.printf("Copy Authorization for model id: %s, access token: %s, expiration time: %s, "
                + "target resource ID; %s, target resource region: %s%n",
            documentModelCopyAuthorization.getTargetModelId(),
            documentModelCopyAuthorization.getAccessToken(),
            documentModelCopyAuthorization.getExpiresOn(),
            documentModelCopyAuthorization.getTargetResourceId(),
            documentModelCopyAuthorization.getTargetResourceRegion()
        );
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getCopyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getCopyAuthorizationWithResponse(CopyAuthorizationOptions, Context)}
     */
    public void getCopyAuthorizationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getCopyAuthorizationWithResponse#Options-Context
        String modelId = "my-copied-model";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        Response<DocumentModelCopyAuthorization> copyAuthorizationResponse =
            documentModelAdministrationClient.getCopyAuthorizationWithResponse(
                new CopyAuthorizationOptions()
                    .setModelId(modelId)
                    .setDescription("model-desc")
                    .setTags(attrs),
                Context.NONE);

        System.out.printf("Copy Authorization operation returned with status: %s",
            copyAuthorizationResponse.getStatusCode());
        DocumentModelCopyAuthorization documentModelCopyAuthorization = copyAuthorizationResponse.getValue();
        System.out.printf("Copy Authorization for model id: %s, access token: %s, "
                + "expiration time: %s, target resource ID; %s, target resource region: %s%n",
            documentModelCopyAuthorization.getTargetModelId(),
            documentModelCopyAuthorization.getAccessToken(),
            documentModelCopyAuthorization.getExpiresOn(),
            documentModelCopyAuthorization.getTargetResourceId(),
            documentModelCopyAuthorization.getTargetResourceRegion()
        );
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getCopyAuthorizationWithResponse#Options-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginComposeDocumentModel(List)}
     */
    public void beginCreateComposedModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginComposeDocumentModel#list
        String modelId1 = "{custom-model-id_1}";
        String modelId2 = "{custom-model-id_2}";
        final DocumentModelDetails documentModelDetails
            = documentModelAdministrationClient.beginComposeDocumentModel(Arrays.asList(modelId1, modelId2))
            .getFinalResult();

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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginComposeDocumentModel#list
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginComposeDocumentModel(List, ComposeDocumentModelOptions, Context)}
     * with options
     */
    public void beginCreateComposedModelWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginComposeDocumentModel#list-Options-Context
        String modelId1 = "{custom-model-id_1}";
        String modelId2 = "{custom-model-id_2}";
        String modelId = "my-composed-model";
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("createdBy", "sample");

        final DocumentModelDetails documentModelDetails =
            documentModelAdministrationClient.beginComposeDocumentModel(Arrays.asList(modelId1, modelId2),
                    new ComposeDocumentModelOptions()
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
        documentModelDetails.getDocumentTypes().forEach((key, documentTypeDetails) -> {
            documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s", field);
                System.out.printf("Field type: %s", documentFieldSchema.getType());
                System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
            });
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginComposeDocumentModel#list-Options-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCopyDocumentModelTo(String, DocumentModelCopyAuthorization)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginCopyDocumentModelTo#string-copyAuthorization
        String copyModelId = "copy-model";
        // Get authorization to copy the model to target resource
        DocumentModelCopyAuthorization documentModelCopyAuthorization
            = documentModelAdministrationClient.getCopyAuthorization();
        // Start copy operation from the source client
        DocumentModelDetails documentModelDetails
            = documentModelAdministrationClient.beginCopyDocumentModelTo(copyModelId, documentModelCopyAuthorization)
                .getFinalResult();
        System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
            documentModelDetails.getModelId(),
            documentModelDetails.getCreatedOn());
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginCopyDocumentModelTo#string-copyAuthorization
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginCopyDocumentModelTo(String, DocumentModelCopyAuthorization, Context)}
     */
    public void beginCopyOverload() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginCopyDocumentModelTo#string-copyAuthorization-Context
        String copyModelId = "copy-model";
        // Get authorization to copy the model to target resource
        DocumentModelCopyAuthorization documentModelCopyAuthorization
            = documentModelAdministrationClient.getCopyAuthorization();
        // Start copy operation from the source client
        DocumentModelDetails documentModelDetails =
            documentModelAdministrationClient.beginCopyDocumentModelTo(copyModelId,
                    documentModelCopyAuthorization,
                    Context.NONE)
                .getFinalResult();
        System.out.printf("Copied model has model ID: %s, was created on: %s.%n,",
            documentModelDetails.getModelId(),
            documentModelDetails.getCreatedOn());
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginCopyDocumentModelTo#string-copyAuthorization-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listDocumentModels()}
     */
    public void listModels() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentModels
        documentModelAdministrationClient.listDocumentModels()
            .forEach(documentModel ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModel.getModelId(),
                    documentModel.getDescription(),
                    documentModel.getCreatedOn())
            );
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentModels
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listDocumentModels(Context)}
     */
    public void listModelsWithContext() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentModels#Context
        documentModelAdministrationClient.listDocumentModels(Context.NONE)
            .forEach(documentModel ->
                System.out.printf("Model ID: %s, Model description: %s, Created on: %s.%n",
                    documentModel.getModelId(),
                    documentModel.getDescription(),
                    documentModel.getCreatedOn())
            );
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentModels#Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getDocumentModel(String)}
     */
    public void getModel() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentModel#string
        String modelId = "{custom-model-id}";
        DocumentModelDetails documentModelDetails = documentModelAdministrationClient.getDocumentModel(modelId);
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentModel#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getDocumentModelWithResponse(String, Context)}
     */
    public void getModelWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentModelWithResponse#string-Context
        String modelId = "{custom-model-id}";
        Response<DocumentModelDetails> response
            = documentModelAdministrationClient.getDocumentModelWithResponse(modelId, Context.NONE);
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentModelWithResponse#string-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getOperation(String)}
     */
    public void getOperation() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getOperation#string
        String operationId = "{operation-id}";
        OperationDetails operationDetails
            = documentModelAdministrationClient.getOperation(operationId);
        System.out.printf("Operation ID: %s%n", operationDetails.getOperationId());
        System.out.printf("Operation Kind: %s%n", operationDetails.getKind());
        System.out.printf("Operation Status: %s%n", operationDetails.getStatus());
        System.out.printf("Model ID created with this operation: %s%n",
            ((DocumentModelBuildOperationDetails) operationDetails).getResult().getModelId());
        if (OperationStatus.FAILED.equals(operationDetails.getStatus())) {
            System.out.printf("Operation fail error: %s%n", operationDetails.getError().getMessage());
        }
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getOperation#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getOperationWithResponse(String, Context)}
     */
    public void getOperationWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getOperationWithResponse#string-Context
        String operationId = "{operation-id}";
        Response<OperationDetails> response =
            documentModelAdministrationClient.getOperationWithResponse(operationId, Context.NONE);
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getOperationWithResponse#string-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listOperations()}
     */
    public void listOperations() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listOperations
        PagedIterable<OperationSummary>
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listOperations
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listOperations(Context)}
     */
    public void listOperationsWithContext() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listOperations#Context
        PagedIterable<OperationSummary>
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
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listOperations#Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildDocumentClassifier(Map)}
     */
    public void beginBuildDocumentClassifier() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentClassifier#Map
        String blobContainerUrl1040D = "{SAS_URL_of_your_container_in_blob_storage}";
        String blobContainerUrl1040A = "{SAS_URL_of_your_container_in_blob_storage}";
        HashMap<String, ClassifierDocumentTypeDetails> docTypes = new HashMap<>();
        docTypes.put("1040-D", new ClassifierDocumentTypeDetails()
            .setAzureBlobSource(new AzureBlobContentSource().setContainerUrl(blobContainerUrl1040D)));
        docTypes.put("1040-D", new ClassifierDocumentTypeDetails()
            .setAzureBlobSource(new AzureBlobContentSource().setContainerUrl(blobContainerUrl1040A)));

        DocumentClassifierDetails classifierDetails
            = documentModelAdministrationClient.beginBuildDocumentClassifier(docTypes)
            .getFinalResult();

        System.out.printf("Classifier ID: %s%n", classifierDetails.getClassifierId());
        System.out.printf("Classifier description: %s%n", classifierDetails.getDescription());
        System.out.printf("Classifier created on: %s%n", classifierDetails.getCreatedOn());
        System.out.printf("Classifier expires on: %s%n", classifierDetails.getExpiresOn());
        classifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            System.out.printf("Blob Source container Url: %s", documentTypeDetails
                .getAzureBlobSource().getContainerUrl());
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentClassifier#Map
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#beginBuildDocumentClassifier(Map, BuildDocumentClassifierOptions, Context)}
     * with options
     */
    public void beginBuildDocumentClassifierWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentClassifier#Map-Options-Context
        String blobContainerUrl1040D = "{SAS_URL_of_your_container_in_blob_storage}";
        String blobContainerUrl1040A = "{SAS_URL_of_your_container_in_blob_storage}";
        HashMap<String, ClassifierDocumentTypeDetails> docTypes = new HashMap<>();
        docTypes.put("1040-D", new ClassifierDocumentTypeDetails()
            .setAzureBlobSource(new AzureBlobContentSource().setContainerUrl(blobContainerUrl1040D)));
        docTypes.put("1040-D", new ClassifierDocumentTypeDetails()
            .setAzureBlobSource(new AzureBlobContentSource().setContainerUrl(blobContainerUrl1040A)));

        DocumentClassifierDetails classifierDetails
            = documentModelAdministrationClient.beginBuildDocumentClassifier(docTypes,
                new BuildDocumentClassifierOptions()
                    .setClassifierId("classifierId")
                    .setDescription("classifier desc"),
                Context.NONE)
            .getFinalResult();

        System.out.printf("Classifier ID: %s%n", classifierDetails.getClassifierId());
        System.out.printf("Classifier description: %s%n", classifierDetails.getDescription());
        System.out.printf("Classifier created on: %s%n", classifierDetails.getCreatedOn());
        System.out.printf("Classifier expires on: %s%n", classifierDetails.getExpiresOn());
        classifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            System.out.printf("Blob Source container Url: %s", documentTypeDetails
                .getAzureBlobSource().getContainerUrl());
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.beginBuildDocumentClassifier#Map-Options-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteDocumentClassifier(String)}
     */
    public void deleteDocumentClassifier() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentClassifier#string
        String classifierId = "{classifierId}";
        documentModelAdministrationClient.deleteDocumentClassifier(classifierId);
        System.out.printf("Classifier ID: %s is deleted.%n", classifierId);
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentClassifier#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#deleteDocumentClassifierWithResponse(String, Context)}
     */
    public void deleteDocumentClassifierWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentClassifierWithResponse#string-Context
        String classifierId = "{classifierId}";
        Response<Void> response
            = documentModelAdministrationClient.deleteDocumentClassifierWithResponse(classifierId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        System.out.printf("Classifier ID: %s is deleted.%n", classifierId);
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.deleteDocumentClassifierWithResponse#string-Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listDocumentClassifiers()}
     */
    public void listDocumentClassifiers() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentClassifiers
        documentModelAdministrationClient.listDocumentClassifiers()
            .forEach(documentModel ->
                System.out.printf("Classifier ID: %s, Classifier description: %s, Created on: %s.%n",
                    documentModel.getClassifierId(),
                    documentModel.getDescription(),
                    documentModel.getCreatedOn())
            );
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentClassifiers
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#listDocumentClassifiers(Context)}
     */
    public void listDocumentClassifiersWithContext() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentClassifiers#Context
        documentModelAdministrationClient.listDocumentClassifiers(Context.NONE)
            .forEach(documentModel ->
                System.out.printf("Classifier ID: %s, Classifier description: %s, Created on: %s.%n",
                    documentModel.getClassifierId(),
                    documentModel.getDescription(),
                    documentModel.getCreatedOn())
            );
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.listDocumentClassifiers#Context
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getDocumentClassifier(String)}
     */
    public void getDocumentClassifier() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentClassifier#string
        String classifierId = "{classifierId}";
        DocumentClassifierDetails documentClassifierDetails
            = documentModelAdministrationClient.getDocumentClassifier(classifierId);
        System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());
        System.out.printf("Classifier Description: %s%n", documentClassifierDetails.getDescription());
        System.out.printf("Classifier Created on: %s%n", documentClassifierDetails.getCreatedOn());
        documentClassifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            System.out.printf("Blob Source container Url: %s", documentTypeDetails
                .getAzureBlobSource().getContainerUrl());
            System.out.printf("Blob File list Source container Url: %s", documentTypeDetails
                .getAzureBlobFileListSource().getContainerUrl());
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentClassifier#string
    }

    /**
     * Code snippet for {@link DocumentModelAdministrationClient#getDocumentModelWithResponse(String, Context)}
     */
    public void getDocumentClassifierWithResponse() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentClassifierWithResponse#string-Context
        String modelId = "{custom-model-id}";
        Response<DocumentClassifierDetails> response
            = documentModelAdministrationClient.getDocumentClassifierWithResponse(modelId, Context.NONE);
        System.out.printf("Response Status Code: %d.", response.getStatusCode());
        DocumentClassifierDetails documentClassifierDetails = response.getValue();
        System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());
        System.out.printf("Classifier Description: %s%n", documentClassifierDetails.getDescription());
        System.out.printf("Classifier Created on: %s%n", documentClassifierDetails.getCreatedOn());
        documentClassifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
            System.out.printf("Blob Source container Url: %s", documentTypeDetails
                .getAzureBlobSource().getContainerUrl());
            System.out.printf("Blob File list Source container Url: %s", documentTypeDetails
                .getAzureBlobFileListSource().getContainerUrl());
        });
        // END: com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.getDocumentClassifierWithResponse#string-Context
    }

}
