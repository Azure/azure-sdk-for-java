// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.administration.models.AccountProperties;
import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.administration.models.CreateComposedModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentBuildMode;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;
import com.azure.ai.formrecognizer.administration.models.ModelOperation;
import com.azure.ai.formrecognizer.administration.models.ModelOperationInfo;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.models.DocumentModelOperationException;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;

/**
 * This class provides a synchronous client that contains model management the operations that apply
 * to Azure Form Recognizer.
 * Operations allowed by the client are creating, building of custom document analysis models, deleting models,
 * listing models, copying a custom-built model to another Form Recognizer account, composing models from
 * component models, getting operation information and getting account information.
 *
 * <p><strong>Instantiating a synchronous Document Model Administration Client</strong></p>
 * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.initialization -->
 * <pre>
 * DocumentModelAdministrationClient documentModelAdministrationClient =
 *     new DocumentModelAdministrationClientBuilder&#40;&#41;.buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.initialization -->
 *
 * @see DocumentModelAdministrationClientBuilder
 * @see DocumentModelAdministrationClient
 */
@ServiceClient(builder = DocumentModelAdministrationClientBuilder.class)
public final class DocumentModelAdministrationClient {

    private final DocumentModelAdministrationAsyncClient client;

    /**
     * Create a {@link DocumentModelAdministrationClient} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link DocumentModelAdministrationClientBuilder#pipeline http pipeline}.
     *
     * @param documentAnalysisTrainingAsyncClient The {@link DocumentModelAdministrationAsyncClient} that the client routes its request through.
     */
    DocumentModelAdministrationClient(DocumentModelAdministrationAsyncClient documentAnalysisTrainingAsyncClient) {
        this.client = documentAnalysisTrainingAsyncClient;
    }

    /**
     * Creates a new {@link DocumentAnalysisClient} object. The new {@link DocumentAnalysisClient}
     * uses the same request policy pipeline as the {@link DocumentAnalysisClient}.
     *
     * @return A new {@link DocumentAnalysisClient} object.
     */
    public DocumentAnalysisClient getDocumentAnalysisClient() {
        return new DocumentAnalysisClientBuilder().endpoint(client.getEndpoint()).pipeline(client.getHttpPipeline())
            .audience(client.getAudience())
            .buildClient();
    }

    /**
     * Builds a custom document analysis model.
     * <p>Models are built using documents that are of the following content
     * type - 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff', image/bmp.
     * Other type of content is ignored.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#String-DocumentBuildMode -->
     * <pre>
     * String trainingFilesUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * DocumentModel documentModel
     *     = documentModelAdministrationClient.beginBuildModel&#40;trainingFilesUrl, DocumentBuildMode.TEMPLATE&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     * documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *     docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#String-DocumentBuildMode -->
     *
     * @param trainingFilesUrl an Azure Storage blob container's SAS URI. A container URI (without SAS)
     * can be used if the container is public or has a managed identity configured. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * @return A {@link SyncPoller} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link DocumentModel custom document analysis model}.
     * @throws DocumentModelOperationException If building model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code trainingFilesUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, DocumentModel> beginBuildModel(
        String trainingFilesUrl, DocumentBuildMode buildMode) {
        return beginBuildModel(trainingFilesUrl, buildMode, null, Context.NONE);
    }

    /**
     * Builds a custom document analysis model.
     * <p>Models are built using documents that are of the following content
     * type - 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff', image/bmp.
     * Other type of content is ignored.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#string-DocumentBuildMode-BuildModelOptions-Context -->
     * <pre>
     * String trainingFilesUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * String modelId = &quot;custom-model-id&quot;;
     * String prefix = &quot;Invoice&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * DocumentModel documentModel = documentModelAdministrationClient.beginBuildModel&#40;trainingFilesUrl,
     *         DocumentBuildMode.TEMPLATE,
     *         new BuildModelOptions&#40;&#41;
     *             .setModelId&#40;modelId&#41;
     *             .setDescription&#40;&quot;model desc&quot;&#41;
     *             .setPrefix&#40;prefix&#41;
     *             .setTags&#40;attrs&#41;, Context.NONE&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModel.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model assigned tags: %s%n&quot;, documentModel.getTags&#40;&#41;&#41;;
     * documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *     docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginBuildModel#string-DocumentBuildMode-BuildModelOptions-Context -->
     *
     * @param trainingFilesUrl an Azure Storage blob container's SAS URI. A container URI (without SAS)
     * can be used if the container is public or has a managed identity configured. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * @param buildModelOptions The configurable {@link BuildModelOptions options} to pass when
     * building a custom document analysis model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the built {@link DocumentModel custom document analysis model}.
     * @throws DocumentModelOperationException If building the model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code trainingFilesUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, DocumentModel> beginBuildModel(
        String trainingFilesUrl, DocumentBuildMode buildMode,
        BuildModelOptions buildModelOptions,
        Context context) {
        return client.beginBuildModel(trainingFilesUrl, buildMode, buildModelOptions, context)
            .getSyncPoller();
    }

    /**
     * Get account information of the Form Recognizer account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getAccountProperties -->
     * <pre>
     * AccountProperties accountProperties = documentModelAdministrationClient.getAccountProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Max number of models that can be build for this account: %d%n&quot;,
     *     accountProperties.getDocumentModelLimit&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Current count of built document analysis models: %d%n&quot;,
     *     accountProperties.getDocumentModelCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getAccountProperties -->
     *
     * @return The requested account information of the Form Recognizer account.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccountProperties getAccountProperties() {
        return getAccountPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Get account information of the Form Recognizer account with an Http response and a
     * specified {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getAccountPropertiesWithResponse#Context -->
     * <pre>
     * Response&lt;AccountProperties&gt; response =
     *     documentModelAdministrationClient.getAccountPropertiesWithResponse&#40;Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * AccountProperties accountProperties = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Max number of models that can be build for this account: %d%n&quot;,
     *     accountProperties.getDocumentModelLimit&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Current count of built document analysis models: %d%n&quot;,
     *     accountProperties.getDocumentModelCount&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getAccountPropertiesWithResponse#Context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The requested account information of the Form Recognizer account.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccountProperties> getAccountPropertiesWithResponse(Context context) {
        return client.getAccountPropertiesWithResponse(context).block();
    }

    /**
     * Deletes the specified custom document analysis model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModel#string -->
     * <pre>
     * String modelId = &quot;&#123;custom-model-id&#125;&quot;;
     * documentModelAdministrationClient.deleteModel&#40;modelId&#41;;
     * System.out.printf&#40;&quot;Model ID: %s is deleted.%n&quot;, modelId&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModel#string -->
     *
     * @param modelId The unique model identifier.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteModel(String modelId) {
        deleteModelWithResponse(modelId, Context.NONE);
    }

    /**
     * Deletes the specified custom document analysis model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModelWithResponse#string-Context -->
     * <pre>
     * String modelId = &quot;&#123;custom-model-id&#125;&quot;;
     * Response&lt;Void&gt; response = documentModelAdministrationClient.deleteModelWithResponse&#40;modelId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model ID: %s is deleted.%n&quot;, modelId&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.deleteModelWithResponse#string-Context -->
     *
     * @param modelId The unique model identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.

     * @return A {@link Response} containing containing status code and HTTP headers.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteModelWithResponse(String modelId, Context context) {
        return client.deleteModelWithResponse(modelId, context).block();
    }

    /**
     * Generate authorization for copying a custom model into the target Form Recognizer resource.
     * <p> This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link DocumentModelAdministrationClient#beginCopyModelTo(String, CopyAuthorization)}.
     * </p>
     *
     * @return The {@link CopyAuthorization}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CopyAuthorization getCopyAuthorization() {
        return getCopyAuthorizationWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Generate authorization for copying a custom model into the target Form Recognizer resource.
     * <p> This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link DocumentModelAdministrationClient#beginCopyModelTo(String, CopyAuthorization)}.
     * </p>
     *
     * @param copyAuthorizationOptions The configurable {@link CopyAuthorizationOptions options} to pass when
     * copying a model.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getCopyAuthorizationWithResponse#CopyAuthorizationOptions-Context -->
     * <pre>
     * String modelId = &quot;my-copied-model&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * Response&lt;CopyAuthorization&gt; copyAuthorizationResponse =
     *     documentModelAdministrationClient.getCopyAuthorizationWithResponse&#40;
     *         new CopyAuthorizationOptions&#40;&#41;
     *             .setModelId&#40;modelId&#41;
     *             .setDescription&#40;&quot;model-desc&quot;&#41;
     *             .setTags&#40;attrs&#41;,
     *         Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;Copy Authorization operation returned with status: %s&quot;,
     *     copyAuthorizationResponse.getStatusCode&#40;&#41;&#41;;
     * CopyAuthorization copyAuthorization = copyAuthorizationResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Copy Authorization for model id: %s, access token: %s, &quot;
     *         + &quot;expiration time: %s, target resource ID; %s, target resource region: %s%n&quot;,
     *     copyAuthorization.getTargetModelId&#40;&#41;,
     *     copyAuthorization.getAccessToken&#40;&#41;,
     *     copyAuthorization.getExpiresOn&#40;&#41;,
     *     copyAuthorization.getTargetResourceId&#40;&#41;,
     *     copyAuthorization.getTargetResourceRegion&#40;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getCopyAuthorizationWithResponse#CopyAuthorizationOptions-Context -->
     *
     * @return A {@link Response} containing the {@link CopyAuthorization}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CopyAuthorization> getCopyAuthorizationWithResponse(
        CopyAuthorizationOptions copyAuthorizationOptions,
        Context context) {
        return client.getCopyAuthorizationWithResponse(copyAuthorizationOptions, context).block();
    }

    /**
     * Create a composed model from the provided list of existing models in the account.
     *
     * <p>This operations fails if the list consists of an invalid, non-existing model Id or duplicate IDs.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCreateComposedModel#list -->
     * <pre>
     * String modelId1 = &quot;&#123;custom-model-id_1&#125;&quot;;
     * String modelId2 = &quot;&#123;custom-model-id_2&#125;&quot;;
     * final DocumentModel documentModel
     *     = documentModelAdministrationClient.beginCreateComposedModel&#40;Arrays.asList&#40;modelId1, modelId2&#41;&#41;
     *     .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModel.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     * documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *     docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCreateComposedModel#list -->
     *
     * @param componentModelIDs The list of models IDs to form the composed model.
     * @return A {@link SyncPoller} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the {@link DocumentModel composed model}.
     * @throws DocumentModelOperationException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code modelIDs} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, DocumentModel> beginCreateComposedModel(
        List<String> componentModelIDs) {
        return beginCreateComposedModel(componentModelIDs, null, Context.NONE);
    }

    /**
     * Create a composed model from the provided list of existing models in the account.
     *
     * <p>This operations fails if the list consists of an invalid, non-existing model Id or duplicate IDs.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCreateComposedModel#list-CreateComposedModelOptions-Context -->
     * <pre>
     * String modelId1 = &quot;&#123;custom-model-id_1&#125;&quot;;
     * String modelId2 = &quot;&#123;custom-model-id_2&#125;&quot;;
     * String modelId = &quot;my-composed-model&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * final DocumentModel documentModel =
     *     documentModelAdministrationClient.beginCreateComposedModel&#40;Arrays.asList&#40;modelId1, modelId2&#41;,
     *             new CreateComposedModelOptions&#40;&#41;
     *                 .setModelId&#40;modelId&#41;
     *                 .setDescription&#40;&quot;my composed model desc&quot;&#41;
     *                 .setTags&#40;attrs&#41;,
     *             Context.NONE&#41;
     *         .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *         .getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModel.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model assigned tags: %s%n&quot;, documentModel.getTags&#40;&#41;&#41;;
     * documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *     docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCreateComposedModel#list-CreateComposedModelOptions-Context -->
     *
     * @param componentModelIDs The list of models IDs to form the composed model.
     * @param createComposedModelOptions The configurable {@link CreateComposedModelOptions options} to pass when
     * creating a composed model.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the {@link DocumentModel composed model}.
     * @throws DocumentModelOperationException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code modelIDs} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, DocumentModel> beginCreateComposedModel(
        List<String> componentModelIDs, CreateComposedModelOptions createComposedModelOptions,
        Context context) {
        return client.beginCreateComposedModel(componentModelIDs, createComposedModelOptions, context).getSyncPoller();
    }

    /**
     * Copy a custom model stored in this resource (the source) to the user specified target Form Recognizer resource.
     *
     * <p>This should be called with the source Form Recognizer resource (with the model that is intended to be copied).
     * The target parameter should be supplied from the target resource's output from
     * {@link DocumentModelAdministrationClient#getCopyAuthorization()} method.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModelTo#string-copyAuthorization -->
     * <pre>
     * String copyModelId = &quot;copy-model&quot;;
     * &#47;&#47; Get authorization to copy the model to target resource
     * CopyAuthorization copyAuthorization = documentModelAdministrationClient.getCopyAuthorization&#40;&#41;;
     * &#47;&#47; Start copy operation from the source client
     * DocumentModel documentModel =
     *     documentModelAdministrationClient.beginCopyModelTo&#40;copyModelId, copyAuthorization&#41;.getFinalResult&#40;&#41;;
     * System.out.printf&#40;&quot;Copied model has model ID: %s, was created on: %s.%n,&quot;,
     *     documentModel.getModelId&#40;&#41;,
     *     documentModel.getCreatedOn&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModelTo#string-copyAuthorization -->
     *
     * @param modelId Model identifier of the model to copy to target resource.
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link DocumentModelAdministrationClient#getCopyAuthorization()}
     *
     * @return A {@link SyncPoller} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, DocumentModel> beginCopyModelTo(String modelId,
        CopyAuthorization target) {
        return beginCopyModelTo(modelId, target, Context.NONE);
    }

    /**
     * Copy a custom model stored in this resource (the source) to the user specified target Form Recognizer resource.
     *
     * <p>This should be called with the source Form Recognizer resource (with the model that is intended to be copied).
     * The target parameter should be supplied from the target resource's output from
     * {@link DocumentModelAdministrationClient#getCopyAuthorization()} method.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModelTo#string-copyAuthorization-Context -->
     * <pre>
     * String copyModelId = &quot;copy-model&quot;;
     * &#47;&#47; Get authorization to copy the model to target resource
     * CopyAuthorization copyAuthorization = documentModelAdministrationClient.getCopyAuthorization&#40;&#41;;
     * &#47;&#47; Start copy operation from the source client
     * DocumentModel documentModel =
     *     documentModelAdministrationClient.beginCopyModelTo&#40;copyModelId, copyAuthorization, Context.NONE&#41;.getFinalResult&#40;&#41;;
     * System.out.printf&#40;&quot;Copied model has model ID: %s, was created on: %s.%n,&quot;,
     *     documentModel.getModelId&#40;&#41;,
     *     documentModel.getCreatedOn&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.beginCopyModelTo#string-copyAuthorization-Context -->
     *
     * @param modelId Model identifier of the model to copy to target resource.
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link DocumentModelAdministrationClient#getCopyAuthorization()}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, DocumentModel> beginCopyModelTo(String modelId,
        CopyAuthorization target, Context context) {
        return client.beginCopyModelTo(modelId, target, context).getSyncPoller();
    }

    /**
     * List information for each model on the Form Recognizer account that were built successfully.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listModels -->
     * <pre>
     * documentModelAdministrationClient.listModels&#40;&#41;
     *     .forEach&#40;documentModel -&gt;
     *         System.out.printf&#40;&quot;Model ID: %s, Model description: %s, Created on: %s.%n&quot;,
     *             documentModel.getModelId&#40;&#41;,
     *             documentModel.getDescription&#40;&#41;,
     *             documentModel.getCreatedOn&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listModels -->
     *
     * @return {@link PagedIterable} of {@link DocumentModelInfo} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DocumentModelInfo> listModels() {
        return new PagedIterable<>(client.listModels(Context.NONE));
    }

    /**
     * List information for each model on the Form Recognizer account that were built successfully with a Http
     * response and a specified {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listModels#Context -->
     * <pre>
     * documentModelAdministrationClient.listModels&#40;Context.NONE&#41;
     *     .forEach&#40;documentModel -&gt;
     *         System.out.printf&#40;&quot;Model ID: %s, Model description: %s, Created on: %s.%n&quot;,
     *             documentModel.getModelId&#40;&#41;,
     *             documentModel.getDescription&#40;&#41;,
     *             documentModel.getCreatedOn&#40;&#41;&#41;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listModels#Context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedIterable} of {@link DocumentModelInfo} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DocumentModelInfo> listModels(Context context) {
        return new PagedIterable<>(client.listModels(context));
    }

    /**
     * Get detailed information for a specified model ID.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getModel#string -->
     * <pre>
     * String modelId = &quot;&#123;custom-model-id&#125;&quot;;
     * DocumentModel documentModel = documentModelAdministrationClient.getModel&#40;modelId&#41;;
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModel.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     * documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *     docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getModel#string -->
     *
     * @param modelId The unique model identifier.
     *
     * @return The detailed information for the specified model.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentModel getModel(String modelId) {
        return getModelWithResponse(modelId, Context.NONE).getValue();
    }

    /**
     * Get detailed information for a specified model ID.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getModelWithResponse#string-Context -->
     * <pre>
     * String modelId = &quot;&#123;custom-model-id&#125;&quot;;
     * Response&lt;DocumentModel&gt; response = documentModelAdministrationClient.getModelWithResponse&#40;modelId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * DocumentModel documentModel = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModel.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     * documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *     docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *         System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getModelWithResponse#string-Context -->
     *
     * @param modelId The unique model identifier.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The detailed information for the specified model.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DocumentModel> getModelWithResponse(String modelId, Context context) {
        return client.getModelWithResponse(modelId, context).block();
    }

    /**
     * Get detailed operation information for the specified ID with Http response.
     * <p> This operations fails if the operation ID used is past 24 hours.</p>
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperation#string -->
     * <pre>
     * String operationId = &quot;&#123;operation-id&#125;&quot;;
     * ModelOperation modelOperation = documentModelAdministrationClient.getOperation&#40;operationId&#41;;
     * System.out.printf&#40;&quot;Operation ID: %s%n&quot;, modelOperation.getOperationId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, modelOperation.getKind&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Operation Status: %s%n&quot;, modelOperation.getStatus&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model ID created with this operation: %s%n&quot;, modelOperation.getModelId&#40;&#41;&#41;;
     * if &#40;ModelOperationStatus.FAILED.equals&#40;modelOperation.getStatus&#40;&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Operation fail error: %s%n&quot;, modelOperation.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperation#string -->
     *
     * @param operationId Unique operation ID.
     *
     * @return The detailed information for the specified operation.
     * @throws IllegalArgumentException If {@code operationId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ModelOperation getOperation(String operationId) {
        return getOperationWithResponse(operationId, Context.NONE).getValue();
    }

    /**
     * Get detailed operation information for the specified ID with Http response.
     * <p> This operations fails if the operation ID used is past 24 hours.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperationWithResponse#string-Context -->
     * <pre>
     * String operationId = &quot;&#123;operation-id&#125;&quot;;
     * Response&lt;ModelOperation&gt; response =
     *     documentModelAdministrationClient.getOperationWithResponse&#40;operationId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * ModelOperation modelOperation = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Operation ID: %s%n&quot;, modelOperation.getOperationId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, modelOperation.getKind&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Operation Status: %s%n&quot;, modelOperation.getStatus&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Model ID created with this operation: %s%n&quot;, modelOperation.getModelId&#40;&#41;&#41;;
     * if &#40;ModelOperationStatus.FAILED.equals&#40;modelOperation.getStatus&#40;&#41;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Operation fail error: %s%n&quot;, modelOperation.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.getOperationWithResponse#string-Context -->
     *
     * @param operationId Unique operation ID.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The detailed information for the specified operation.
     * @throws IllegalArgumentException If {@code operationId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ModelOperation> getOperationWithResponse(String operationId, Context context) {
        return client.getOperationWithResponse(operationId, context).block();
    }

    /**
     * List information for each model operation on the Form Recognizer account in the past 24 hours.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations -->
     * <pre>
     * PagedIterable&lt;ModelOperationInfo&gt;
     *     modelOperationInfo = documentModelAdministrationClient.listOperations&#40;&#41;;
     * modelOperationInfo.forEach&#40;modelOperation -&gt; &#123;
     *     System.out.printf&#40;&quot;Operation ID: %s%n&quot;, modelOperation.getOperationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Status: %s%n&quot;, modelOperation.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Created on: %s%n&quot;, modelOperation.getCreatedOn&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Percent completed: %d%n&quot;, modelOperation.getPercentCompleted&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, modelOperation.getKind&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Last updated on: %s%n&quot;, modelOperation.getLastUpdatedOn&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation resource location: %s%n&quot;, modelOperation.getResourceLocation&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations -->
     *
     * @return {@link PagedIterable} of {@link ModelOperationInfo} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ModelOperationInfo> listOperations() {
        return new PagedIterable<>(client.listOperations(Context.NONE));
    }

    /**
     * List information for each operation on the Form Recognizer account in the past 24 hours with an HTTP response and
     * a specified {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations#Context -->
     * <pre>
     * PagedIterable&lt;ModelOperationInfo&gt;
     *     modelOperationInfo = documentModelAdministrationClient.listOperations&#40;Context.NONE&#41;;
     * modelOperationInfo.forEach&#40;modelOperation -&gt; &#123;
     *     System.out.printf&#40;&quot;Operation ID: %s%n&quot;, modelOperation.getOperationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Status: %s%n&quot;, modelOperation.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Created on: %s%n&quot;, modelOperation.getCreatedOn&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Percent completed: %d%n&quot;, modelOperation.getPercentCompleted&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, modelOperation.getKind&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Last updated on: %s%n&quot;, modelOperation.getLastUpdatedOn&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation resource location: %s%n&quot;, modelOperation.getResourceLocation&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient.listOperations#Context -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedIterable} of {@link ModelOperationInfo} custom form model information.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ModelOperationInfo> listOperations(Context context) {
        return new PagedIterable<>(client.listOperations(context));
    }
}
