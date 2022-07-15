// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.DocumentAnalysisServiceVersion;
import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.ComposeModelOptions;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.administration.models.DocumentModelSummary;
import com.azure.ai.formrecognizer.administration.models.ModelOperationDetails;
import com.azure.ai.formrecognizer.administration.models.ModelOperationSummary;
import com.azure.ai.formrecognizer.administration.models.ResourceDetails;
import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.util.Transforms;
import com.azure.ai.formrecognizer.implementation.util.Utility;
import com.azure.ai.formrecognizer.models.DocumentAnalysisAudience;
import com.azure.ai.formrecognizer.models.DocumentModelOperationException;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.implementation.util.Constants.DEFAULT_POLL_INTERVAL;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides an asynchronous client that contains model management operations
 * that apply to Azure Form Recognizer.
 * Operations allowed by the client are creating, building of custom document analysis models, deleting models,
 * listing models, copying a custom-built model to another Form Recognizer account, composing models from
 * component models, getting operation information and getting account information.
 *
 * <p><strong>Instantiating an asynchronous Document Model Administration Client</strong></p>
 * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.initialization -->
 * <pre>
 * DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
 *     new DocumentModelAdministrationClientBuilder&#40;&#41;.buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.initialization -->
 *
 * @see DocumentModelAdministrationClientBuilder
 * @see DocumentModelAdministrationAsyncClient
 */
@ServiceClient(builder = DocumentModelAdministrationClientBuilder.class, isAsync = true)
public final class DocumentModelAdministrationAsyncClient {

    private final ClientLogger logger = new ClientLogger(DocumentModelAdministrationAsyncClient.class);
    private final FormRecognizerClientImpl service;
    private final DocumentAnalysisServiceVersion serviceVersion;
    private final DocumentAnalysisAudience audience;

    /**
     * Create a {@link DocumentModelAdministrationAsyncClient} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link DocumentModelAdministrationClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Form Recognizer supported by this client library.
     * @param audience ARM management audience associated with the given form recognizer resource.
     *
     */
    DocumentModelAdministrationAsyncClient(FormRecognizerClientImpl service, DocumentAnalysisServiceVersion serviceVersion,
        DocumentAnalysisAudience audience) {
        this.service = service;
        this.serviceVersion = serviceVersion;
        this.audience = audience;
    }

    /**
     * Creates a new {@link DocumentAnalysisAsyncClient} object. The new {@code DocumentTrainingAsyncClient}
     * uses the same request policy pipeline as the {@code DocumentTrainingAsyncClient}.
     *
     * @return A new {@link DocumentAnalysisAsyncClient} object.
     */
    public DocumentAnalysisAsyncClient getDocumentAnalysisAsyncClient() {
        return new DocumentAnalysisClientBuilder().endpoint(getEndpoint()).pipeline(getHttpPipeline())
            .audience(this.audience)
            .buildAsyncClient();
    }

    /**
     * Gets the pipeline the client is using.
     *
     * @return the pipeline the client is using.
     */
    HttpPipeline getHttpPipeline() {
        return service.getHttpPipeline();
    }

    /**
     * Gets the endpoint the client is using.
     *
     * @return the endpoint the client is using.
     */
    String getEndpoint() {
        return service.getEndpoint();
    }

    /**
     * Gets the audience the client is using.
     *
     * @return the audience the client is using.
     */
    DocumentAnalysisAudience getAudience() {
        return audience;
    }

    /**
     * Builds a custom document analysis model.
     * Models are built using documents that are of the following content type -
     * 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff', image/bmp.
     * Other type of content is ignored.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/build-training-data-set#upload-your-training-data">here</a>
     * for information on building your own administration data set.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-DocumentModelBuildMode -->
     * <pre>
     * String trainingFilesUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * documentModelAdministrationAsyncClient.beginBuildModel&#40;trainingFilesUrl,
     *         DocumentModelBuildMode.TEMPLATE
     *     &#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;documentModel -&gt; &#123;
     *         System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     *         documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *             docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *                 System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *                 System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-DocumentModelBuildMode -->
     *
     * @param trainingFilesUrl an Azure Storage blob container's SAS URI. A container URI (without SAS)
     * can be used if the container is public or has a managed identity configured. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentModelBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * @return A {@link PollerFlux} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link DocumentModelDetails custom document analysis model}.
     * @throws DocumentModelOperationException If building a model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code trainingFilesUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, DocumentModelDetails> beginBuildModel(String trainingFilesUrl,
                                                                                  DocumentModelBuildMode buildMode) {
        return beginBuildModel(trainingFilesUrl, buildMode, null);
    }

    /**
     * Builds a custom document analysis model.
     * Models are built using documents that are of the following content type -
     * 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff', image/bmp.
     * Other type of content is ignored.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/build-training-data-set#upload-your-training-data">here</a>
     * for information on building your own administration data set.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-DocumentModelBuildMode-BuildModelOptions -->
     * <pre>
     * String trainingFilesUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * String modelId = &quot;model-id&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * documentModelAdministrationAsyncClient.beginBuildModel&#40;trainingFilesUrl,
     *         DocumentModelBuildMode.TEMPLATE,
     *         new BuildModelOptions&#40;&#41;
     *             .setPrefix&#40;&quot;Invoice&quot;&#41;
     *             .setModelId&#40;modelId&#41;
     *             .setDescription&#40;&quot;model desc&quot;&#41;
     *             .setTags&#40;attrs&#41;&#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;documentModel -&gt; &#123;
     *         System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModel.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model assigned tags: %s%n&quot;, documentModel.getTags&#40;&#41;&#41;;
     *         documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *             docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *                 System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *                 System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-DocumentModelBuildMode-BuildModelOptions -->
     *
     * @param trainingFilesUrl an Azure Storage blob container's SAS URI. A container URI (without SAS)
     * can be used if the container is public or has a managed identity configured. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentModelBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * @param buildModelOptions The configurable {@link BuildModelOptions options} to pass when
     * building a custom document analysis model.
     * @return A {@link PollerFlux} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link DocumentModelDetails custom document analysis model}.
     * @throws DocumentModelOperationException If building a model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code trainingFilesUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, DocumentModelDetails> beginBuildModel(String trainingFilesUrl,
                                                                                  DocumentModelBuildMode buildMode,
                                                                                  BuildModelOptions buildModelOptions) {
        return beginBuildModel(trainingFilesUrl, buildMode, buildModelOptions, Context.NONE);
    }

    PollerFlux<DocumentOperationResult, DocumentModelDetails> beginBuildModel(String trainingFilesUrl,
                                                                           DocumentModelBuildMode buildMode,
                                                                           BuildModelOptions buildModelOptions,
                                                                           Context context) {

        buildModelOptions =  buildModelOptions == null ? new BuildModelOptions() : buildModelOptions;
        String modelId = buildModelOptions.getModelId();
        if (modelId == null) {
            modelId = Utility.generateRandomModelID();
        }
        return new PollerFlux<DocumentOperationResult, DocumentModelDetails>(
            DEFAULT_POLL_INTERVAL,
            buildModelActivationOperation(trainingFilesUrl, buildMode, modelId, buildModelOptions, context),
            createModelPollOperation(context),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchModelResultOperation(context));
    }

    /**
     * Get information about the current Form Recognizer resource.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getResourceDetails -->
     * <pre>
     * documentModelAdministrationAsyncClient.getResourceDetails&#40;&#41;
     *     .subscribe&#40;resourceInfo -&gt; &#123;
     *         System.out.printf&#40;&quot;Max number of models that can be build for this account: %d%n&quot;,
     *             resourceInfo.getDocumentModelLimit&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Current count of built document analysis models: %d%n&quot;,
     *             resourceInfo.getDocumentModelCount&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getResourceDetails -->
     *
     * @return The requested resource information details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ResourceDetails> getResourceDetails() {
        return getResourceDetailsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get the information about the current Form Recognizer resource with a Http response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getResourceDetailsWithResponse -->
     * <pre>
     * documentModelAdministrationAsyncClient.getResourceDetailsWithResponse&#40;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         ResourceDetails resourceDetails = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Max number of models that can be build for this account: %d%n&quot;,
     *             resourceDetails.getDocumentModelLimit&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Current count of built document analysis models: %d%n&quot;,
     *             resourceDetails.getDocumentModelCount&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getResourceDetailsWithResponse -->
     *
     * @return A {@link Response} containing the requested resource information details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ResourceDetails>> getResourceDetailsWithResponse() {
        try {
            return withContext(this::getResourceDetailsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ResourceDetails>> getResourceDetailsWithResponse(Context context) {
        return service.getInfoWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, Transforms.toAccountProperties(response.getValue())));
    }

    /**
     * Deletes the specified custom document analysis model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.deleteModel#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentModelAdministrationAsyncClient.deleteModel&#40;modelId&#41;
     *     .subscribe&#40;ignored -&gt; System.out.printf&#40;&quot;Model ID: %s is deleted%n&quot;, modelId&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.deleteModel#string -->
     *
     * @param modelId The unique model identifier.
     * @return An empty Mono.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteModel(String modelId) {
        return deleteModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified custom document analysis model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.deleteModelWithResponse#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentModelAdministrationAsyncClient.deleteModelWithResponse&#40;modelId&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model ID: %s is deleted.%n&quot;, modelId&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.deleteModelWithResponse#string -->
     *
     * @param modelId The unique model identifier.
     * @return A {@link Response} containing the status code and HTTP headers.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteModelWithResponse(String modelId) {
        try {
            return withContext(context -> deleteModelWithResponse(modelId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteModelWithResponse(String modelId, Context context) {
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                + " be null or empty"));
        }
        return service.deleteModelWithResponseAsync(modelId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Generate authorization for copying a custom document analysis model into the target Form Recognizer resource.
     * <p> This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link DocumentModelAdministrationAsyncClient#beginCopyModelTo(String, CopyAuthorization)}.
     * </p>
     *
     * @return The {@link CopyAuthorization} that could be used to authorize copying model between resources.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CopyAuthorization> getCopyAuthorization() {
        return getCopyAuthorizationWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Generate authorization for copying a custom document analysis model into the target Form Recognizer resource.
     * <p>This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link DocumentModelAdministrationAsyncClient#beginCopyModelTo(String, CopyAuthorization)}.
     * </p>
     *
     * @param copyAuthorizationOptions The configurable {@link CopyAuthorizationOptions options} to pass when
     * copying a model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorizationWithResponse#CopyAuthorizationOptions -->
     * <pre>
     * String modelId = &quot;my-copied-model&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * documentModelAdministrationAsyncClient.getCopyAuthorizationWithResponse&#40;
     *         new CopyAuthorizationOptions&#40;&#41;
     *             .setModelId&#40;modelId&#41;
     *             .setDescription&#40;&quot;model desc&quot;&#41;
     *             .setTags&#40;attrs&#41;&#41;
     *     .subscribe&#40;copyAuthorization -&gt;
     *         System.out.printf&#40;&quot;Copy Authorization response status: %s, for model id: %s, access token: %s, &quot;
     *                 + &quot;expiration time: %s, target resource ID; %s, target resource region: %s%n&quot;,
     *             copyAuthorization.getStatusCode&#40;&#41;,
     *             copyAuthorization.getValue&#40;&#41;.getTargetModelId&#40;&#41;,
     *             copyAuthorization.getValue&#40;&#41;.getAccessToken&#40;&#41;,
     *             copyAuthorization.getValue&#40;&#41;.getExpiresOn&#40;&#41;,
     *             copyAuthorization.getValue&#40;&#41;.getTargetResourceId&#40;&#41;,
     *             copyAuthorization.getValue&#40;&#41;.getTargetResourceRegion&#40;&#41;
     *         &#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorizationWithResponse#CopyAuthorizationOptions -->
     * @return The {@link CopyAuthorization} that could be used to authorize copying model between resources.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CopyAuthorization>> getCopyAuthorizationWithResponse(
        CopyAuthorizationOptions copyAuthorizationOptions) {
        try {
            return withContext(context -> getCopyAuthorizationWithResponse(copyAuthorizationOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CopyAuthorization>> getCopyAuthorizationWithResponse(
        CopyAuthorizationOptions copyAuthorizationOptions,
        Context context) {
        copyAuthorizationOptions = copyAuthorizationOptions == null
            ? new CopyAuthorizationOptions() : copyAuthorizationOptions;
        String modelId = copyAuthorizationOptions.getModelId();
        modelId = modelId == null ? Utility.generateRandomModelID() : modelId;

        AuthorizeCopyRequest authorizeCopyRequest
            = new AuthorizeCopyRequest()
            .setModelId(modelId)
            .setDescription(copyAuthorizationOptions.getDescription())
            .setTags(copyAuthorizationOptions.getTags());

        return service.authorizeCopyDocumentModelWithResponseAsync(authorizeCopyRequest, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, Transforms.toCopyAuthorization(response.getValue())));
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
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginComposeModel#list -->
     * <pre>
     * String modelId1 = &quot;&#123;model_Id_1&#125;&quot;;
     * String modelId2 = &quot;&#123;model_Id_2&#125;&quot;;
     * documentModelAdministrationAsyncClient.beginComposeModel&#40;Arrays.asList&#40;modelId1, modelId2&#41;
     *     &#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;documentModel -&gt; &#123;
     *         System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     *         documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *             docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *                 System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *                 System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginComposeModel#list -->
     *
     * @param componentModelIds The list of component models to compose.
     * @return A {@link PollerFlux} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the created {@link DocumentModelDetails composed model}.
     * @throws DocumentModelOperationException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code componentModelIds} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, DocumentModelDetails> beginComposeModel(
        List<String> componentModelIds) {
        return beginComposeModel(componentModelIds, null);
    }

    /**
     * Create a composed model from the provided list of existing models in the account.
     *
     * <p>This operations fails if the list consists of an invalid, non-existing model Id or duplicate IDs.
     * </p>
     *
     *  <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginComposeModel#list-composeModelOptions -->
     * <pre>
     * String modelId1 = &quot;&#123;model_Id_1&#125;&quot;;
     * String modelId2 = &quot;&#123;model_Id_2&#125;&quot;;
     * String modelId = &quot;my-composed-model&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * documentModelAdministrationAsyncClient.beginComposeModel&#40;Arrays.asList&#40;modelId1, modelId2&#41;,
     *         new ComposeModelOptions&#40;&#41;
     *             .setModelId&#40;modelId&#41;
     *             .setDescription&#40;&quot;model-desc&quot;&#41;
     *             .setTags&#40;attrs&#41;&#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;documentModel -&gt; &#123;
     *         System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModel.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model assigned tags: %s%n&quot;, documentModel.getTags&#40;&#41;&#41;;
     *         documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *             docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *                 System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *                 System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginComposeModel#list-composeModelOptions -->
     *
     * @param componentModelIds The list of component models to compose.
     * @param composeModelOptions The configurable {@link ComposeModelOptions options} to pass when
     * creating a composed model.
     * @return A {@link PollerFlux} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the copied model {@link DocumentModelDetails}.
     * @throws DocumentModelOperationException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code componentModelIds} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, DocumentModelDetails> beginComposeModel(List<String> componentModelIds,
                                                                                    ComposeModelOptions composeModelOptions) {
        return beginComposeModel(componentModelIds, composeModelOptions, Context.NONE);
    }

    PollerFlux<DocumentOperationResult, DocumentModelDetails> beginComposeModel(List<String> componentModelIds,
                                                                             ComposeModelOptions composeModelOptions, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(componentModelIds)) {
                throw logger.logExceptionAsError(new NullPointerException("'componentModelIds' cannot be null or empty"));
            }
            composeModelOptions =  composeModelOptions == null
                ? new ComposeModelOptions() : composeModelOptions;

            String modelId = composeModelOptions.getModelId();
            modelId = modelId == null ? Utility.generateRandomModelID() : modelId;

            composeModelOptions = getComposeModelOptions(composeModelOptions);

            final ComposeDocumentModelRequest composeRequest = new ComposeDocumentModelRequest()
                .setComponentModels(componentModelIds.stream()
                    .map(modelIdString -> new ComponentModelInfo().setModelId(modelIdString))
                    .collect(Collectors.toList()))
                .setModelId(modelId)
                .setDescription(composeModelOptions.getDescription())
                .setTags(composeModelOptions.getTags());

            return new PollerFlux<DocumentOperationResult, DocumentModelDetails>(
                DEFAULT_POLL_INTERVAL,
                Utility.activationOperation(() -> service.composeDocumentModelWithResponseAsync(composeRequest, context)
                    .map(response -> Transforms.toDocumentOperationResult(
                        response.getDeserializedHeaders().getOperationLocation())), logger),
                createModelPollOperation(context),
                (activationResponse, pollingContext)
                    -> Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchModelResultOperation(context));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Copy a custom model stored in this resource (the source) to the user specified target Form Recognizer resource.
     *
     * <p>This should be called with the source Form Recognizer resource (with the model that is intended to be copied).
     * The target parameter should be supplied from the target resource's output from
     * {@link DocumentModelAdministrationAsyncClient#getCopyAuthorization()} method.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCopyModelTo#string-copyAuthorization -->
     * <pre>
     * String copyModelId = &quot;copy-model&quot;;
     * &#47;&#47; Get authorization to copy the model to target resource
     * documentModelAdministrationAsyncClient.getCopyAuthorization&#40;&#41;
     *     &#47;&#47; Start copy operation from the source client
     *     &#47;&#47; The ID of the model that needs to be copied to the target resource
     *     .subscribe&#40;copyAuthorization -&gt; documentModelAdministrationAsyncClient.beginCopyModelTo&#40;copyModelId,
     *             copyAuthorization&#41;
     *         .filter&#40;pollResponse -&gt; pollResponse.getStatus&#40;&#41;.isComplete&#40;&#41;&#41;
     *         .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *         .subscribe&#40;documentModel -&gt;
     *             System.out.printf&#40;&quot;Copied model has model ID: %s, was created on: %s.%n,&quot;,
     *                 documentModel.getModelId&#40;&#41;,
     *                 documentModel.getCreatedOn&#40;&#41;&#41;&#41;&#41;;
     *
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCopyModelTo#string-copyAuthorization -->
     *
     * @param modelId Model identifier of the model to copy to target resource.
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link DocumentModelAdministrationAsyncClient#getCopyAuthorization()}
     * @return A {@link PollerFlux} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the copied model {@link DocumentModelDetails}.
     * @throws DocumentModelOperationException If copy operation fails and model with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code modelId} or {@code target} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, DocumentModelDetails> beginCopyModelTo(String modelId,
                                                                                   CopyAuthorization target) {
        return beginCopyModelTo(modelId, target, null);
    }

    PollerFlux<DocumentOperationResult, DocumentModelDetails> beginCopyModelTo(String modelId,
                                                                            CopyAuthorization target, Context context) {
        return new PollerFlux<DocumentOperationResult, DocumentModelDetails>(
            DEFAULT_POLL_INTERVAL,
            getCopyActivationOperation(modelId, target, context),
            createModelPollOperation(context),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchModelResultOperation(context));
    }

    /**
     * List information for each model on the Form Recognizer account that were built successfully.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listModels -->
     * <pre>
     * documentModelAdministrationAsyncClient.listModels&#40;&#41;
     *     .subscribe&#40;documentModelInfo -&gt;
     *         System.out.printf&#40;&quot;Model ID: %s, Model description: %s, Created on: %s.%n&quot;,
     *             documentModelInfo.getModelId&#40;&#41;,
     *             documentModelInfo.getDescription&#40;&#41;,
     *             documentModelInfo.getCreatedOn&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listModels -->
     *
     * @return {@link PagedFlux} of {@link DocumentModelSummary}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DocumentModelSummary> listModels() {
        try {
            return new PagedFlux<>(() -> withContext(this::listFirstPageModelInfo),
                continuationToken -> withContext(context -> listNextPageModelInfo(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * List information for each model on the Form Recognizer account that were built successfully with a Http response
     * and a specified {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listModels -->
     * <pre>
     * documentModelAdministrationAsyncClient.listModels&#40;&#41;
     *     .subscribe&#40;documentModelInfo -&gt;
     *         System.out.printf&#40;&quot;Model ID: %s, Model description: %s, Created on: %s.%n&quot;,
     *             documentModelInfo.getModelId&#40;&#41;,
     *             documentModelInfo.getDescription&#40;&#41;,
     *             documentModelInfo.getCreatedOn&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listModels -->
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedFlux} of {@link DocumentModelSummary}.
     */
    PagedFlux<DocumentModelSummary> listModels(Context context) {
        return new PagedFlux<>(() -> listFirstPageModelInfo(context),
            continuationToken -> listNextPageModelInfo(continuationToken, context));
    }

    /**
     * Get detailed information for a specified model ID.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getModel#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentModelAdministrationAsyncClient.getModel&#40;modelId&#41;.subscribe&#40;documentModel -&gt; &#123;
     *     System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModel.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     *     documentModel.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *         docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *             System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getModel#string -->
     *
     * @param modelId The unique model identifier.
     * @return The detailed information for the specified model.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentModelDetails> getModel(String modelId) {
        return getModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get detailed information for a specified model ID with Http response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getModelWithResponse#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentModelAdministrationAsyncClient.getModelWithResponse&#40;modelId&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *     DocumentModelDetails documentModelDetails = response.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModelDetails.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     *     documentModelDetails.getDocTypes&#40;&#41;.forEach&#40;&#40;key, docTypeInfo&#41; -&gt; &#123;
     *         docTypeInfo.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *             System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Field confidence: %.2f&quot;, docTypeInfo.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getModelWithResponse#string -->
     *
     * @param modelId The unique model identifier.
     * @return A {@link Response} containing the requested {@link DocumentModelDetails model}.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentModelDetails>> getModelWithResponse(String modelId) {
        try {
            return withContext(context -> getModelWithResponse(modelId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentModelDetails>> getModelWithResponse(String modelId, Context context) {
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                + " be null or empty"));
        }
        return service.getModelWithResponseAsync(modelId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, Transforms.toDocumentModel(response.getValue())));
    }

    /**
     * Get detailed operation information for the specified ID.
     * <p> This operations fails if the operation ID used is past 24 hours.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getOperation#string -->
     * <pre>
     * String operationId = &quot;&#123;operation_Id&#125;&quot;;
     * documentModelAdministrationAsyncClient.getOperation&#40;operationId&#41;.subscribe&#40;modelOperationDetails -&gt; &#123;
     *     System.out.printf&#40;&quot;Operation ID: %s%n&quot;, modelOperationDetails.getOperationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, modelOperationDetails.getKind&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Status: %s%n&quot;, modelOperationDetails.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model ID created with this operation: %s%n&quot;, modelOperationDetails.getModelId&#40;&#41;&#41;;
     *     if &#40;ModelOperationStatus.FAILED.equals&#40;modelOperationDetails.getStatus&#40;&#41;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Operation fail error: %s%n&quot;, modelOperationDetails.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getOperation#string -->
     *
     * @param operationId Unique operation ID.
     * @return detailed operation information for the specified ID.
     * @throws IllegalArgumentException If {@code operationId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ModelOperationDetails> getOperation(String operationId) {
        return getOperationWithResponse(operationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get detailed operation information for the specified ID with Http response.
     * <p> This operations fails if the operation ID used is past 24 hours.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getOperationWithResponse#string -->
     * <pre>
     * String operationId = &quot;&#123;operation_Id&#125;&quot;;
     * documentModelAdministrationAsyncClient.getOperationWithResponse&#40;operationId&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *     ModelOperationDetails modelOperationDetails = response.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Operation ID: %s%n&quot;, modelOperationDetails.getOperationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, modelOperationDetails.getKind&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Status: %s%n&quot;, modelOperationDetails.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model ID created with this operation: %s%n&quot;, modelOperationDetails.getModelId&#40;&#41;&#41;;
     *     if &#40;ModelOperationStatus.FAILED.equals&#40;modelOperationDetails.getStatus&#40;&#41;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Operation fail error: %s%n&quot;, modelOperationDetails.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getOperationWithResponse#string -->
     *
     * @param operationId Unique operation ID.
     * @return A {@link Response} containing the requested {@link ModelOperationDetails}.
     * @throws IllegalArgumentException If {@code operationId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ModelOperationDetails>> getOperationWithResponse(String operationId) {
        try {
            return withContext(context -> getOperationWithResponse(operationId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ModelOperationDetails>> getOperationWithResponse(String operationId, Context context) {
        if (CoreUtils.isNullOrEmpty(operationId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'operationId' is required and cannot"
                + " be null or empty"));
        }
        return service.getOperationWithResponseAsync(operationId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, Transforms.toModelOperation(response.getValue())));
    }

    /**
     * List information for each model operation on the Form Recognizer account in the past 24 hours.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listOperations -->
     * <pre>
     * documentModelAdministrationAsyncClient.listOperations&#40;&#41;
     *     .subscribe&#40;modelOperationSummary -&gt; &#123;
     *         System.out.printf&#40;&quot;Operation ID: %s%n&quot;, modelOperationSummary.getOperationId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Operation Status: %s%n&quot;, modelOperationSummary.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Operation Created on: %s%n&quot;, modelOperationSummary.getCreatedOn&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Operation Percent completed: %d%n&quot;, modelOperationSummary.getPercentCompleted&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, modelOperationSummary.getKind&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Operation Last updated on: %s%n&quot;, modelOperationSummary.getLastUpdatedOn&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Operation resource location: %s%n&quot;, modelOperationSummary.getResourceLocation&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listOperations -->
     *
     * @return {@link PagedFlux} of {@link ModelOperationDetails}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ModelOperationSummary> listOperations() {
        try {
            return new PagedFlux<>(() -> withContext(this::listFirstPageOperationInfo),
                continuationToken -> withContext(context -> listNextPageOperationInfo(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<ModelOperationSummary> listOperations(Context context) {
        return new PagedFlux<>(() -> listFirstPageOperationInfo(context),
            continuationToken -> listNextPageOperationInfo(continuationToken, context));
    }

    private Function<PollingContext<DocumentOperationResult>, Mono<DocumentModelDetails>>
        fetchModelResultOperation(Context context) {
        return (pollingContext) -> {
            try {
                final String modelId = pollingContext.getLatestResponse().getValue().getResultId();
                return service.getOperationAsync(modelId, context)
                    .map(modelSimpleResponse -> Transforms.toDocumentModel(modelSimpleResponse.getResult()))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<DocumentOperationResult>, Mono<PollResponse<DocumentOperationResult>>>
        createModelPollOperation(Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<DocumentOperationResult> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                String modelId = operationResultPollResponse.getValue().getResultId();
                return service.getOperationAsync(modelId, context)
                    .flatMap(modelSimpleResponse ->
                        processBuildingModelResponse(modelSimpleResponse, operationResultPollResponse))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            }  catch (HttpResponseException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<DocumentOperationResult>, Mono<DocumentOperationResult>>
        buildModelActivationOperation(
        String trainingFilesUrl, DocumentModelBuildMode buildMode, String modelId,
        BuildModelOptions buildModelOptions, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(trainingFilesUrl, "'trainingFilesUrl' cannot be null.");
                BuildDocumentModelRequest buildDocumentModelRequest = new BuildDocumentModelRequest()
                    .setModelId(modelId)
                    .setBuildMode(com.azure.ai.formrecognizer.implementation.models.DocumentBuildMode
                        .fromString(buildMode.toString()))
                    .setAzureBlobSource(new AzureBlobContentSource()
                        .setContainerUrl(trainingFilesUrl)
                        .setPrefix(buildModelOptions.getPrefix()))
                    .setDescription(buildModelOptions.getDescription())
                    .setTags(buildModelOptions.getTags());

                return service.buildDocumentModelWithResponseAsync(buildDocumentModelRequest, context)
                    .map(response ->
                        Transforms.toDocumentOperationResult(
                            response.getDeserializedHeaders().getOperationLocation()))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Mono<PollResponse<DocumentOperationResult>> processBuildingModelResponse(
        GetOperationResponse getOperationResponse,
        PollResponse<DocumentOperationResult> trainingModelOperationResponse) {
        LongRunningOperationStatus status;
        switch (getOperationResponse.getStatus()) {
            case NOT_STARTED:
            case RUNNING:
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            case SUCCEEDED:
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case FAILED:
                throw logger.logExceptionAsError(
                    Transforms.toDocumentModelOperationException(getOperationResponse.getError()));
            case CANCELED:
            default:
                status = LongRunningOperationStatus.fromString(
                    getOperationResponse.getStatus().toString(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status,
            trainingModelOperationResponse.getValue()));
    }

    private Function<PollingContext<DocumentOperationResult>, Mono<DocumentOperationResult>>
        getCopyActivationOperation(
        String modelId, CopyAuthorization target, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(modelId, "'modelId' cannot be null.");
                Objects.requireNonNull(target, "'target' cannot be null.");
                com.azure.ai.formrecognizer.implementation.models.CopyAuthorization copyRequest
                    = new com.azure.ai.formrecognizer.implementation.models.CopyAuthorization()
                    .setTargetModelLocation(target.getTargetModelLocation())
                    .setTargetResourceId(target.getTargetResourceId())
                    .setTargetResourceRegion(target.getTargetResourceRegion())
                    .setTargetModelId(target.getTargetModelId())
                    .setAccessToken(target.getAccessToken())
                    .setExpirationDateTime(target.getExpiresOn());
                return service.copyDocumentModelToWithResponseAsync(modelId, copyRequest, context)
                    .map(response ->
                        Transforms.toDocumentOperationResult(
                            response.getDeserializedHeaders().getOperationLocation()))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Mono<PagedResponse<DocumentModelSummary>> listFirstPageModelInfo(Context context) {
        return service.getModelsSinglePageAsync(context)
            .doOnRequest(ignoredValue -> logger.info("Listing information for all models"))
            .doOnSuccess(response -> logger.info("Listed all models"))
            .doOnError(error -> logger.warning("Failed to list all models information", error))
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                Transforms.toDocumentModelInfo(res.getValue()),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<DocumentModelSummary>> listNextPageModelInfo(String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        return service.getModelsNextSinglePageAsync(nextPageLink, context)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                Transforms.toDocumentModelInfo(res.getValue()),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<ModelOperationSummary>> listFirstPageOperationInfo(Context context) {
        return service.getOperationsSinglePageAsync(context)
            .doOnRequest(ignoredValue -> logger.info("Listing information for all operations"))
            .doOnSuccess(response -> logger.info("Listed all operations"))
            .doOnError(error -> logger.warning("Failed to list all operations information", error))
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                Transforms.toModelOperationInfo(res.getValue()),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<ModelOperationSummary>> listNextPageOperationInfo(String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        return service.getOperationsNextSinglePageAsync(nextPageLink, context)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                Transforms.toModelOperationInfo(res.getValue()),
                res.getContinuationToken(),
                null));
    }

    private static ComposeModelOptions
        getComposeModelOptions(ComposeModelOptions userProvidedOptions) {
        return userProvidedOptions == null ? new ComposeModelOptions() : userProvidedOptions;
    }
}
