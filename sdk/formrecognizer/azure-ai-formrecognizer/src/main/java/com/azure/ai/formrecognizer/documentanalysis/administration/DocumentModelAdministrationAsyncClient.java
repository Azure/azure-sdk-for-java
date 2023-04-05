// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisServiceVersion;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentClassifierOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ClassifierDocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ComposeDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelCopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelSummary;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationStatus;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationSummary;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ResourceDetails;
import com.azure.ai.formrecognizer.documentanalysis.implementation.DocumentClassifiersImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.DocumentModelsImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.MiscellaneousImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.AuthorizeCopyRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.BuildDocumentClassifierRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.BuildDocumentModelRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.ComposeDocumentModelRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms;
import com.azure.ai.formrecognizer.documentanalysis.implementation.util.Utility;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnalysisAudience;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
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
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Constants.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getAuthorizeCopyRequest;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getBuildDocumentClassifierRequest;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getBuildDocumentModelRequest;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getComposeDocumentModelRequest;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getInnerCopyAuthorization;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.toInnerDocTypes;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Utility.getComposeModelOptions;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides an asynchronous client that contains model management operations
 * that apply to Azure Form Recognizer.
 * Operations allowed by the client are creating, building of custom document analysis models, deleting models,
 * listing models, copying a custom-built model to another Form Recognizer account, composing models from
 * component models, getting operation information and getting resource details.
 *
 * <p><strong>Instantiating an asynchronous Document Model Administration Client</strong></p>
 * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.initialization -->
 * <pre>
 * DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient =
 *     new DocumentModelAdministrationClientBuilder&#40;&#41;.buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.initialization -->
 *
 * @see DocumentModelAdministrationClientBuilder
 * @see DocumentModelAdministrationAsyncClient
 */
@ServiceClient(builder = DocumentModelAdministrationClientBuilder.class, isAsync = true)
public final class DocumentModelAdministrationAsyncClient {
    private final ClientLogger logger = new ClientLogger(DocumentModelAdministrationAsyncClient.class);
    private final FormRecognizerClientImpl formRecognizerClientImpl;
    private final DocumentModelsImpl documentModelsImpl;
    private final MiscellaneousImpl miscellaneousImpl;
    private final DocumentClassifiersImpl documentClassifiersImpl;

    private final DocumentAnalysisServiceVersion serviceVersion;
    private final DocumentAnalysisAudience audience;

    /**
     * Create a {@link DocumentModelAdministrationAsyncClient} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link DocumentModelAdministrationClientBuilder#pipeline http pipeline}.
     *
     * @param formRecognizerClientImpl The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Form Recognizer supported by this client library.
     * @param audience ARM management audience associated with the given form recognizer resource.
     *
     */
    DocumentModelAdministrationAsyncClient(FormRecognizerClientImpl formRecognizerClientImpl, DocumentAnalysisServiceVersion serviceVersion,
                                           DocumentAnalysisAudience audience) {
        this.formRecognizerClientImpl = formRecognizerClientImpl;
        this.documentModelsImpl = formRecognizerClientImpl.getDocumentModels();
        this.miscellaneousImpl = formRecognizerClientImpl.getMiscellaneous();
        this.serviceVersion = serviceVersion;
        this.documentClassifiersImpl = formRecognizerClientImpl.getDocumentClassifiers();
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
        return formRecognizerClientImpl.getHttpPipeline();
    }

    /**
     * Gets the endpoint the client is using.
     *
     * @return the endpoint the client is using.
     */
    String getEndpoint() {
        return formRecognizerClientImpl.getEndpoint();
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
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode -->
     * <pre>
     * String blobContainerUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * documentModelAdministrationAsyncClient.beginBuildDocumentModel&#40;blobContainerUrl,
     *         DocumentModelBuildMode.TEMPLATE
     *     &#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;documentModel -&gt; &#123;
     *         System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     *         documentModel.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *             documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *                 System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *                 System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode -->
     *
     * @param blobContainerUrl an Azure Storage blob container's SAS URI. A container URI (without SAS)
     * can be used if the container is public or has a managed identity configured. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentModelBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * @return A {@link PollerFlux} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link DocumentModelDetails custom document analysis model}.
     * @throws HttpResponseException If building a model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code blobContainerUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<OperationResult, DocumentModelDetails> beginBuildDocumentModel(String blobContainerUrl,
                                                                                     DocumentModelBuildMode buildMode) {
        return beginBuildDocumentModel(blobContainerUrl, buildMode, null, null);
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
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode-String-Options -->
     * <pre>
     * String blobContainerUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * String modelId = &quot;model-id&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     * String prefix = &quot;Invoice&quot;;
     *
     * documentModelAdministrationAsyncClient.beginBuildDocumentModel&#40;blobContainerUrl,
     *         DocumentModelBuildMode.TEMPLATE,
     *         prefix,
     *         new BuildDocumentModelOptions&#40;&#41;
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
     *         documentModel.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *             documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *                 System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *                 System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode-BuildDocumentModelOptions -->
     *
     * @param blobContainerUrl an Azure Storage blob container's SAS URI. A container URI (without SAS)
     * can be used if the container is public or has a managed identity configured. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentModelBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * @param prefix case-sensitive prefix blob name prefix to filter documents for training.
     * @param buildDocumentModelOptions The configurable {@link BuildDocumentModelOptions options} to pass when
     * building a custom document analysis model.
     * @return A {@link PollerFlux} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link DocumentModelDetails custom document analysis model}.
     * @throws HttpResponseException If building a model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code blobContainerUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<OperationResult, DocumentModelDetails> beginBuildDocumentModel(String blobContainerUrl,
                                                                                     DocumentModelBuildMode buildMode,
                                                                                     String prefix,
                                                                                     BuildDocumentModelOptions buildDocumentModelOptions) {
        return beginBuildDocumentModel(blobContainerUrl, buildMode, prefix, null, buildDocumentModelOptions, Context.NONE);
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
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode-String -->
     * <pre>
     * String blobContainerUrl = &quot;&#123;SAS-URL-of-your-container-in-blob-storage&#125;&quot;;
     * String fileList = &quot;&quot;;
     * documentModelAdministrationAsyncClient.beginBuildDocumentModel&#40;blobContainerUrl,
     *         DocumentModelBuildMode.TEMPLATE, fileList
     *     &#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;documentModel -&gt; &#123;
     *         System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     *         documentModel.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *             documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *                 System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *                 System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentModel#String-BuildMode-String -->
     *
     * @param blobContainerUrl an Azure Storage blob container's SAS URI. A container URI (without SAS)
     * can be used if the container is public or has a managed identity configured. For more information on
     * setting up a training data set, see: <a href="https://aka.ms/azsdk/formrecognizer/buildcustommodel">here</a>.
     * @param buildMode the preferred technique for creating models. For faster training of models use
     * {@link DocumentModelBuildMode#TEMPLATE}. See <a href="https://aka.ms/azsdk/formrecognizer/buildmode">here</a>
     * for more information on building mode for custom documents.
     * @param fileList Path to a JSONL file within the container specifying the set of documents for training.
     * @return A {@link PollerFlux} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link DocumentModelDetails custom document analysis model}.
     * @throws HttpResponseException If building a model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code blobContainerUrl} and {@code fileList} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<OperationResult, DocumentModelDetails> beginBuildDocumentModel(String blobContainerUrl,
                                                                                     DocumentModelBuildMode buildMode,
                                                                                     String fileList) {
        Objects.requireNonNull(fileList, "'fileList' is required and cannot be null.");
        return beginBuildDocumentModel(blobContainerUrl, buildMode, null, fileList, null, Context.NONE);
    }

    PollerFlux<OperationResult, DocumentModelDetails> beginBuildDocumentModel(String blobContainerUrl,
                                                                              DocumentModelBuildMode buildMode,
                                                                              String prefix,
                                                                              String fileList,
                                                                              BuildDocumentModelOptions buildDocumentModelOptions,
                                                                              Context context) {

        buildDocumentModelOptions =  buildDocumentModelOptions == null
            ? new BuildDocumentModelOptions() : buildDocumentModelOptions;
        String modelId = buildDocumentModelOptions.getModelId();
        if (modelId == null) {
            modelId = Utility.generateRandomModelID();
        }
        return new PollerFlux<OperationResult, DocumentModelDetails>(
            DEFAULT_POLL_INTERVAL,
            buildModelActivationOperation(blobContainerUrl, buildMode, modelId, prefix, fileList, buildDocumentModelOptions, context),
            createModelPollOperation(context),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchModelResultOperation(context));
    }

    /**
     * Get information about the current Form Recognizer resource.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getResourceDetails -->
     * <pre>
     * documentModelAdministrationAsyncClient.getResourceDetails&#40;&#41;
     *     .subscribe&#40;resourceInfo -&gt; &#123;
     *         System.out.printf&#40;&quot;Max number of models that can be build for this account: %d%n&quot;,
     *             resourceInfo.getCustomDocumentModelLimit&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Current count of built document analysis models: %d%n&quot;,
     *             resourceInfo.getCustomDocumentModelCount&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getResourceDetails -->
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
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getResourceDetailsWithResponse -->
     * <pre>
     * documentModelAdministrationAsyncClient.getResourceDetailsWithResponse&#40;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         ResourceDetails resourceDetails = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Max number of models that can be build for this account: %d%n&quot;,
     *             resourceDetails.getCustomDocumentModelLimit&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Current count of built document analysis models: %d%n&quot;,
     *             resourceDetails.getCustomDocumentModelCount&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getResourceDetailsWithResponse -->
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
        return miscellaneousImpl.getResourceInfoWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, Transforms.toAccountProperties(response.getValue())));
    }

    /**
     * Deletes the specified custom document analysis model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentModel#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentModelAdministrationAsyncClient.deleteDocumentModel&#40;modelId&#41;
     *     .subscribe&#40;ignored -&gt; System.out.printf&#40;&quot;Model ID: %s is deleted%n&quot;, modelId&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentModel#string -->
     *
     * @param modelId The unique model identifier.
     * @return An empty Mono.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDocumentModel(String modelId) {
        return deleteDocumentModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified custom document analysis model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentModelWithResponse#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentModelAdministrationAsyncClient.deleteDocumentModelWithResponse&#40;modelId&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model ID: %s is deleted.%n&quot;, modelId&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentModelWithResponse#string -->
     *
     * @param modelId The unique model identifier.
     * @return A {@link Response} containing the status code and HTTP headers.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDocumentModelWithResponse(String modelId) {
        try {
            return withContext(context -> deleteDocumentModelWithResponse(modelId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteDocumentModelWithResponse(String modelId, Context context) {
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                + " be null or empty"));
        }
        return documentModelsImpl.deleteModelWithResponseAsync(modelId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Generate authorization for copying a custom document analysis model into the target Form Recognizer resource.
     * <p> This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link DocumentModelAdministrationAsyncClient#beginCopyDocumentModelTo(String, DocumentModelCopyAuthorization)}.
     * </p>
     *
     * @return The {@link DocumentModelCopyAuthorization} that could be used to authorize copying model between resources.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentModelCopyAuthorization> getCopyAuthorization() {
        return getCopyAuthorizationWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Generate authorization for copying a custom document analysis model into the target Form Recognizer resource.
     * <p>This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link DocumentModelAdministrationAsyncClient#beginCopyDocumentModelTo(String, DocumentModelCopyAuthorization)}.
     * </p>
     *
     * @param copyAuthorizationOptions The configurable {@link CopyAuthorizationOptions options} to pass when
     * copying a model.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getCopyAuthorizationWithResponse#Options -->
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
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getCopyAuthorizationWithResponse#Options -->
     * @return The {@link DocumentModelCopyAuthorization} that could be used to authorize copying model between resources.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentModelCopyAuthorization>> getCopyAuthorizationWithResponse(
        CopyAuthorizationOptions copyAuthorizationOptions) {
        try {
            return withContext(context -> getCopyAuthorizationWithResponse(copyAuthorizationOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentModelCopyAuthorization>> getCopyAuthorizationWithResponse(
        CopyAuthorizationOptions copyAuthorizationOptions,
        Context context) {
        copyAuthorizationOptions = copyAuthorizationOptions == null
            ? new CopyAuthorizationOptions() : copyAuthorizationOptions;
        String modelId = copyAuthorizationOptions.getModelId();
        modelId = modelId == null ? Utility.generateRandomModelID() : modelId;

        AuthorizeCopyRequest authorizeCopyRequest =
            getAuthorizeCopyRequest(copyAuthorizationOptions, modelId);

        return documentModelsImpl.authorizeModelCopyWithResponseAsync(authorizeCopyRequest, context)
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
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginComposeDocumentModel#list -->
     * <pre>
     * String modelId1 = &quot;&#123;model_Id_1&#125;&quot;;
     * String modelId2 = &quot;&#123;model_Id_2&#125;&quot;;
     * documentModelAdministrationAsyncClient.beginComposeDocumentModel&#40;Arrays.asList&#40;modelId1, modelId2&#41;
     *     &#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;documentModel -&gt; &#123;
     *         System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     *         documentModel.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *             documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *                 System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *                 System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginComposeDocumentModel#list -->
     *
     * @param componentModelIds The list of component models to compose.
     * @return A {@link PollerFlux} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the created {@link DocumentModelDetails composed model}.
     * @throws HttpResponseException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code componentModelIds} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<OperationResult, DocumentModelDetails> beginComposeDocumentModel(
        List<String> componentModelIds) {
        return beginComposeDocumentModel(componentModelIds, null);
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
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginComposeDocumentModel#list-Options -->
     * <pre>
     * String modelId1 = &quot;&#123;model_Id_1&#125;&quot;;
     * String modelId2 = &quot;&#123;model_Id_2&#125;&quot;;
     * String modelId = &quot;my-composed-model&quot;;
     * Map&lt;String, String&gt; attrs = new HashMap&lt;String, String&gt;&#40;&#41;;
     * attrs.put&#40;&quot;createdBy&quot;, &quot;sample&quot;&#41;;
     *
     * documentModelAdministrationAsyncClient.beginComposeDocumentModel&#40;Arrays.asList&#40;modelId1, modelId2&#41;,
     *         new ComposeDocumentModelOptions&#40;&#41;
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
     *         documentModel.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *             documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *                 System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *                 System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginComposeDocumentModel#list-Options -->
     *
     * @param componentModelIds The list of component models to compose.
     * @param composeDocumentModelOptions The configurable {@link ComposeDocumentModelOptions options} to pass when
     * creating a composed model.
     * @return A {@link PollerFlux} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the copied model {@link DocumentModelDetails}.
     * @throws HttpResponseException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code componentModelIds} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<OperationResult, DocumentModelDetails> beginComposeDocumentModel(List<String> componentModelIds,
                                                                                       ComposeDocumentModelOptions composeDocumentModelOptions) {
        return beginComposeDocumentModel(componentModelIds, composeDocumentModelOptions, Context.NONE);
    }

    PollerFlux<OperationResult, DocumentModelDetails> beginComposeDocumentModel(List<String> componentModelIds,
                                                                                ComposeDocumentModelOptions composeDocumentModelOptions, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(componentModelIds)) {
                throw logger.logExceptionAsError(new NullPointerException("'componentModelIds' cannot be null or empty"));
            }
            String modelId = composeDocumentModelOptions.getModelId();
            modelId = modelId == null ? Utility.generateRandomModelID() : modelId;

            composeDocumentModelOptions = getComposeModelOptions(composeDocumentModelOptions);

            final ComposeDocumentModelRequest composeRequest = getComposeDocumentModelRequest(componentModelIds, composeDocumentModelOptions, modelId);

            return new PollerFlux<OperationResult, DocumentModelDetails>(
                DEFAULT_POLL_INTERVAL,
                Utility.activationOperation(() -> documentModelsImpl.composeModelWithResponseAsync(composeRequest, context)
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
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginCopyDocumentModelTo#string-copyAuthorization -->
     * <pre>
     * String copyModelId = &quot;copy-model&quot;;
     * &#47;&#47; Get authorization to copy the model to target resource
     * documentModelAdministrationAsyncClient.getCopyAuthorization&#40;&#41;
     *     &#47;&#47; Start copy operation from the source client
     *     &#47;&#47; The ID of the model that needs to be copied to the target resource
     *     .subscribe&#40;copyAuthorization -&gt; documentModelAdministrationAsyncClient.beginCopyDocumentModelTo&#40;copyModelId,
     *             copyAuthorization&#41;
     *         .filter&#40;pollResponse -&gt; pollResponse.getStatus&#40;&#41;.isComplete&#40;&#41;&#41;
     *         .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *         .subscribe&#40;documentModel -&gt;
     *             System.out.printf&#40;&quot;Copied model has model ID: %s, was created on: %s.%n,&quot;,
     *                 documentModel.getModelId&#40;&#41;,
     *                 documentModel.getCreatedOn&#40;&#41;&#41;&#41;&#41;;
     *
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginCopyDocumentModelTo#string-copyAuthorization -->
     *
     * @param sourceModelId Model identifier of the source model to copy to target resource.
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link DocumentModelAdministrationAsyncClient#getCopyAuthorization()}
     * @return A {@link PollerFlux} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the copied model {@link DocumentModelDetails}.
     * @throws HttpResponseException If copy operation fails and model with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code modelId} or {@code target} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<OperationResult, DocumentModelDetails> beginCopyDocumentModelTo(String sourceModelId,
                                                                                      DocumentModelCopyAuthorization target) {
        return beginCopyDocumentModelTo(sourceModelId, target, null);
    }

    PollerFlux<OperationResult, DocumentModelDetails> beginCopyDocumentModelTo(String sourceModelId,
                                                                               DocumentModelCopyAuthorization target, Context context) {
        return new PollerFlux<OperationResult, DocumentModelDetails>(
            DEFAULT_POLL_INTERVAL,
            getCopyActivationOperation(sourceModelId, target, context),
            createModelPollOperation(context),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchModelResultOperation(context));
    }

    /**
     * List information for each model on the Form Recognizer account that were built successfully.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listDocumentModels -->
     * <pre>
     * documentModelAdministrationAsyncClient.listDocumentModels&#40;&#41;
     *     .subscribe&#40;documentModelInfo -&gt;
     *         System.out.printf&#40;&quot;Model ID: %s, Model description: %s, Created on: %s.%n&quot;,
     *             documentModelInfo.getModelId&#40;&#41;,
     *             documentModelInfo.getDescription&#40;&#41;,
     *             documentModelInfo.getCreatedOn&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listDocumentModels -->
     *
     * @return {@link PagedFlux} of {@link DocumentModelSummary}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DocumentModelSummary> listDocumentModels() {
        try {
            return new PagedFlux<>(() -> withContext(this::listFirstPageModelInfo),
                continuationToken -> withContext(context -> listNextPageModelInfo(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }


    /**
     * Get detailed information for a specified model ID.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentModel#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentModelAdministrationAsyncClient.getDocumentModel&#40;modelId&#41;.subscribe&#40;documentModel -&gt; &#123;
     *     System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModel.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
     *     documentModel.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *         documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *             System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentModel#string -->
     *
     * @param modelId The unique model identifier.
     * @return The detailed information for the specified model.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentModelDetails> getDocumentModel(String modelId) {
        return getDocumentModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get detailed information for a specified model ID with Http response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentModelWithResponse#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentModelAdministrationAsyncClient.getDocumentModelWithResponse&#40;modelId&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *     DocumentModelDetails documentModelDetails = response.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModelDetails.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     *     documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *         documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *             System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentModelWithResponse#string -->
     *
     * @param modelId The unique model identifier.
     * @return A {@link Response} containing the requested {@link DocumentModelDetails model}.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentModelDetails>> getDocumentModelWithResponse(String modelId) {
        try {
            return withContext(context -> getDocumentModelWithResponse(modelId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentModelDetails>> getDocumentModelWithResponse(String modelId, Context context) {
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                + " be null or empty"));
        }
        return documentModelsImpl.getModelWithResponseAsync(modelId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, Transforms.toDocumentModelDetails(response.getValue())));
    }

    /**
     * Get detailed operation information for the specified ID.
     * <p> This operations fails if the operation ID used is past 24 hours.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getOperation#string -->
     * <pre>
     * String operationId = &quot;&#123;operation_Id&#125;&quot;;
     * documentModelAdministrationAsyncClient.getOperation&#40;operationId&#41;.subscribe&#40;operationDetails -&gt; &#123;
     *     System.out.printf&#40;&quot;Operation ID: %s%n&quot;, operationDetails.getOperationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, operationDetails.getKind&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Status: %s%n&quot;, operationDetails.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model ID created with this operation: %s%n&quot;,
     *         &#40;&#40;DocumentModelBuildOperationDetails&#41; operationDetails&#41;.getResult&#40;&#41;.getModelId&#40;&#41;&#41;;
     *     if &#40;OperationStatus.FAILED.equals&#40;operationDetails.getStatus&#40;&#41;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Operation fail error: %s%n&quot;, operationDetails.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getOperation#string -->
     *
     * @param operationId Unique operation ID.
     * @return detailed operation information for the specified ID.
     * @throws IllegalArgumentException If {@code operationId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<OperationDetails> getOperation(String operationId) {
        return getOperationWithResponse(operationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get detailed operation information for the specified ID with Http response.
     * <p> This operations fails if the operation ID used is past 24 hours.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getOperationWithResponse#string -->
     * <pre>
     * String operationId = &quot;&#123;operation_Id&#125;&quot;;
     * documentModelAdministrationAsyncClient.getOperationWithResponse&#40;operationId&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *     OperationDetails operationDetails = response.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Operation ID: %s%n&quot;, operationDetails.getOperationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Kind: %s%n&quot;, operationDetails.getKind&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Operation Status: %s%n&quot;, operationDetails.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model ID created with this operation: %s%n&quot;,
     *         &#40;&#40;DocumentModelBuildOperationDetails&#41; operationDetails&#41;.getResult&#40;&#41;.getModelId&#40;&#41;&#41;;
     *     if &#40;OperationStatus.FAILED.equals&#40;operationDetails.getStatus&#40;&#41;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Operation fail error: %s%n&quot;, operationDetails.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getOperationWithResponse#string -->
     *
     * @param operationId Unique operation ID.
     * @return A {@link Response} containing the requested {@link OperationDetails}.
     * @throws IllegalArgumentException If {@code operationId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<OperationDetails>> getOperationWithResponse(String operationId) {
        try {
            return withContext(context -> getOperationWithResponse(operationId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<OperationDetails>> getOperationWithResponse(String operationId, Context context) {
        if (CoreUtils.isNullOrEmpty(operationId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'operationId' is required and cannot"
                + " be null or empty"));
        }
        return miscellaneousImpl.getOperationWithResponseAsync(operationId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, Transforms.toOperationDetails(response.getValue())));
    }

    /**
     * List information for each model operation on the Form Recognizer account in the past 24 hours.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listOperations -->
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
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listOperations -->
     *
     * @return {@link PagedFlux} of {@link OperationDetails}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<OperationSummary> listOperations() {
        try {
            return new PagedFlux<>(() -> withContext(this::listFirstPageOperationInfo),
                continuationToken -> withContext(context -> listNextPageOperationInfo(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Deletes the specified document classifier.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentClassifier#string -->
     * <pre>
     * String classifierId = &quot;&#123;classifierId&#125;&quot;;
     * documentModelAdministrationAsyncClient.deleteDocumentClassifier&#40;classifierId&#41;
     *     .subscribe&#40;ignored -&gt; System.out.printf&#40;&quot;Classifier ID: %s is deleted%n&quot;, classifierId&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentClassifier#string -->
     *
     * @param classifierId The unique document classifier identifier.
     * @return An empty Mono.
     * @throws IllegalArgumentException If {@code classifierId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDocumentClassifier(String classifierId) {
        return deleteDocumentModelWithResponse(classifierId).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified document classifier.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentClassifierWithResponse#string -->
     * <pre>
     * String classifierId = &quot;&#123;classifierId&#125;&quot;;
     * documentModelAdministrationAsyncClient.deleteDocumentClassifierWithResponse&#40;classifierId&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Classifier ID: %s is deleted.%n&quot;, classifierId&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.deleteDocumentClassifierWithResponse#string -->
     *
     * @param classifierId The unique document classifier identifier.
     * @return A {@link Response} containing the status code and HTTP headers.
     * @throws IllegalArgumentException If {@code classifierId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDocumentClassifierWithResponse(String classifierId) {
        try {
            return withContext(context -> deleteDocumentModelWithResponse(classifierId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List information for each document classifier on the Form Recognizer account that were built successfully.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listDocumentClassifiers -->
     * <pre>
     * documentModelAdministrationAsyncClient.listDocumentClassifiers&#40;&#41;
     *     .subscribe&#40;documentModelInfo -&gt;
     *         System.out.printf&#40;&quot;Classifier ID: %s, Classifier description: %s, Created on: %s.%n&quot;,
     *             documentModelInfo.getClassifierId&#40;&#41;,
     *             documentModelInfo.getDescription&#40;&#41;,
     *             documentModelInfo.getCreatedOn&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.listDocumentClassifiers -->
     *
     * @return {@link PagedFlux} of {@link DocumentClassifierDetails document classifiers} on the Form Recognizer account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DocumentClassifierDetails> listDocumentClassifiers() {
        try {
            return new PagedFlux<>(() -> withContext(this::listFirstPageClassifiers),
                continuationToken -> withContext(context -> listNextPageClassifiers(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    private Mono<PagedResponse<DocumentClassifierDetails>> listFirstPageClassifiers(Context context) {
        return documentClassifiersImpl.listClassifiersSinglePageAsync(context)
            .doOnRequest(ignoredValue -> logger.info("Listing information for all models"))
            .doOnSuccess(response -> logger.info("Listed all models"))
            .doOnError(error -> logger.warning("Failed to list all models information", error))
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().stream()
                    .map(documentClassifierDetails -> Transforms.fromInnerDocumentClassifierDetails(documentClassifierDetails))
                    .collect(Collectors.toList()),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<DocumentClassifierDetails>> listNextPageClassifiers(String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        return documentClassifiersImpl.listClassifiersNextSinglePageAsync(nextPageLink, context)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().stream()
                    .map(documentClassifierDetails -> Transforms.fromInnerDocumentClassifierDetails(documentClassifierDetails))
                    .collect(Collectors.toList()),
                res.getContinuationToken(),
                null));
    }

    /**
     * Get detailed information for a document classifier by its ID.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentClassifier#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentModelAdministrationAsyncClient.getDocumentClassifier&#40;modelId&#41;.subscribe&#40;documentClassifier -&gt; &#123;
     *     System.out.printf&#40;&quot;Classifier ID: %s%n&quot;, documentClassifier.getClassifierId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Classifier Description: %s%n&quot;, documentClassifier.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Classifier Created on: %s%n&quot;, documentClassifier.getCreatedOn&#40;&#41;&#41;;
     *     documentClassifier.getDocTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Blob Source container Url: %s&quot;, documentTypeDetails.getAzureBlobSource&#40;&#41;
     *             .getContainerUrl&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Blob File list Source container Url: %s&quot;, documentTypeDetails.
     *             getAzureBlobFileListSource&#40;&#41;.getContainerUrl&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentClassifier#string -->
     *
     * @param classifierId The unique document classifier identifier.
     * @return The detailed information for the specified document classifier ID.
     * @throws IllegalArgumentException If {@code classifierId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentClassifierDetails> getDocumentClassifier(String classifierId) {
        return getDocumentClassifierWithResponse(classifierId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get detailed information for a specified model ID with Http response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentModelWithResponse#string -->
     * <pre>
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentModelAdministrationAsyncClient.getDocumentModelWithResponse&#40;modelId&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Response Status Code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *     DocumentModelDetails documentModelDetails = response.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModelDetails.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Model Created on: %s%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
     *     documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *         documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
     *             System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.getDocumentModelWithResponse#string -->
     *
     * @param classifierId The unique document classifier identifier.
     * @return A {@link Response} containing the requested {@link DocumentClassifierDetails model}.
     * @throws IllegalArgumentException If {@code classifierId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentClassifierDetails>> getDocumentClassifierWithResponse(String classifierId) {
        try {
            return withContext(context -> getDocumentClassifierWithResponse(classifierId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private Mono<Response<DocumentClassifierDetails>> getDocumentClassifierWithResponse(String classifierId, Context context) {
        if (CoreUtils.isNullOrEmpty(classifierId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'classifierId' is required and cannot"
                + " be null or empty"));
        }
        return documentClassifiersImpl.getClassifierWithResponseAsync(classifierId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, Transforms.fromInnerDocumentClassifierDetails(response.getValue())));
    }

    /**
     * Builds a custom classifier document model.
     * <p>Classifier models can identify multiple documents or multiple instances of a single document. For that,
     * you need at least five documents for each class and two classes of documents.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentClassifier#Map -->
     * <pre>
     * String blobContainerUrl1040D = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
     * String blobContainerUrl1040A = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
     * HashMap&lt;String, ClassifierDocumentTypeDetails&gt; docTypes = new HashMap&lt;&gt;&#40;&#41;;
     * docTypes.put&#40;&quot;1040-D&quot;, new ClassifierDocumentTypeDetails&#40;&#41;
     *     .setAzureBlobSource&#40;new AzureBlobContentSource&#40;blobContainerUrl1040D&#41;&#41;&#41;;
     * docTypes.put&#40;&quot;1040-D&quot;, new ClassifierDocumentTypeDetails&#40;&#41;
     *     .setAzureBlobSource&#40;new AzureBlobContentSource&#40;blobContainerUrl1040A&#41;&#41;&#41;;
     *
     * documentModelAdministrationAsyncClient.beginBuildDocumentClassifier&#40;docTypes&#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;classifierDetails -&gt; &#123;
     *         System.out.printf&#40;&quot;Classifier ID: %s%n&quot;, classifierDetails.getClassifierId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Classifier description: %s%n&quot;, classifierDetails.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Classifier created on: %s%n&quot;, classifierDetails.getCreatedOn&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Classifier expires on: %s%n&quot;, classifierDetails.getExpiresOn&#40;&#41;&#41;;
     *         classifierDetails.getDocTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Blob Source container Url: %s&quot;, documentTypeDetails
     *                 .getAzureBlobSource&#40;&#41;.getContainerUrl&#40;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentClassifier#Map -->
     *
     * @param docTypes List of document types to classify against.
     * @return A {@link PollerFlux} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link DocumentClassifierDetails custom document classifier model}.
     * @throws HttpResponseException If building a model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code docTypes} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<OperationResult, DocumentClassifierDetails> beginBuildDocumentClassifier(Map<String, ClassifierDocumentTypeDetails> docTypes) {
        return beginBuildDocumentClassifier(docTypes, null, null);
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
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentClassifier#Map-Options -->
     * <pre>
     * String blobContainerUrl1040D = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
     * String blobContainerUrl1040A = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
     * HashMap&lt;String, ClassifierDocumentTypeDetails&gt; docTypes = new HashMap&lt;&gt;&#40;&#41;;
     * docTypes.put&#40;&quot;1040-D&quot;, new ClassifierDocumentTypeDetails&#40;&#41;
     *     .setAzureBlobSource&#40;new AzureBlobContentSource&#40;blobContainerUrl1040D&#41;&#41;&#41;;
     * docTypes.put&#40;&quot;1040-D&quot;, new ClassifierDocumentTypeDetails&#40;&#41;
     *     .setAzureBlobSource&#40;new AzureBlobContentSource&#40;blobContainerUrl1040A&#41;&#41;&#41;;
     *
     * documentModelAdministrationAsyncClient.beginBuildDocumentClassifier&#40;docTypes,
     *         new BuildDocumentClassifierOptions&#40;&#41;
     *             .setClassifierId&#40;&quot;classifierId&quot;&#41;
     *             .setDescription&#40;&quot;classifier desc&quot;&#41;&#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;classifierDetails -&gt; &#123;
     *         System.out.printf&#40;&quot;Classifier ID: %s%n&quot;, classifierDetails.getClassifierId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Classifier description: %s%n&quot;, classifierDetails.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Classifier created on: %s%n&quot;, classifierDetails.getCreatedOn&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Classifier expires on: %s%n&quot;, classifierDetails.getExpiresOn&#40;&#41;&#41;;
     *         classifierDetails.getDocTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Blob Source container Url: %s&quot;, documentTypeDetails
     *                 .getAzureBlobSource&#40;&#41;.getContainerUrl&#40;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminAsyncClient.beginBuildDocumentClassifier#Map-Options -->
     *
     * @param docTypes List of document types to classify against.
     * @param buildDocumentClassifierOptions The configurable {@link BuildDocumentClassifierOptions options} to pass when
     * building a custom classifier document model.
     * @return A {@link SyncPoller} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the built {@link DocumentClassifierDetails custom document classifier model}.
     * @throws HttpResponseException If building the model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code docTypes} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<OperationResult, DocumentClassifierDetails> beginBuildDocumentClassifier(
        Map<String, ClassifierDocumentTypeDetails> docTypes, BuildDocumentClassifierOptions buildDocumentClassifierOptions) {
        return beginBuildDocumentClassifier(docTypes, buildDocumentClassifierOptions, Context.NONE);
    }

    PollerFlux<OperationResult, DocumentClassifierDetails> beginBuildDocumentClassifier(Map<String, ClassifierDocumentTypeDetails> docTypes, BuildDocumentClassifierOptions buildDocumentClassifierOptions,
                                                                              Context context) {

        buildDocumentClassifierOptions =  buildDocumentClassifierOptions == null
            ? new BuildDocumentClassifierOptions() : buildDocumentClassifierOptions;
        String classifierId = buildDocumentClassifierOptions.getClassifierId();
        if (classifierId == null) {
            classifierId = Utility.generateRandomModelID();
        }
        return new PollerFlux<OperationResult, DocumentClassifierDetails>(
            DEFAULT_POLL_INTERVAL,
            buildClassifierActivationOperation(classifierId, docTypes, buildDocumentClassifierOptions, context),
            createModelPollOperation(context),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchClassifierResultOperation(context));
    }

    private Function<PollingContext<OperationResult>, Mono<DocumentClassifierDetails>>
        fetchClassifierResultOperation(Context context) {
        return (pollingContext) -> {
            try {
                final String classifierId = pollingContext.getLatestResponse().getValue().getOperationId();
                return miscellaneousImpl.getOperationWithResponseAsync(classifierId, context)
                    .map(classifierResponse ->
                        Transforms.toDocumentClassifierFromOperationId(classifierResponse.getValue()));
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }
    private Function<PollingContext<OperationResult>, Mono<DocumentModelDetails>>
        fetchModelResultOperation(Context context) {
        return (pollingContext) -> {
            try {
                final String modelId = pollingContext.getLatestResponse().getValue().getOperationId();
                return miscellaneousImpl.getOperationWithResponseAsync(modelId, context)
                    .map(modelSimpleResponse -> Transforms.toDocumentModelFromOperationId(modelSimpleResponse.getValue()))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<PollResponse<OperationResult>>>
        createModelPollOperation(Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<OperationResult> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                String modelId = operationResultPollResponse.getValue().getOperationId();
                return miscellaneousImpl.getOperationAsync(modelId, context)
                    .flatMap(modelSimpleResponse ->
                        processBuildingModelResponse(modelSimpleResponse, operationResultPollResponse))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            }  catch (HttpResponseException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>>
        buildModelActivationOperation(
        String blobContainerUrl, DocumentModelBuildMode buildMode, String modelId,
        String prefix, String fileList, BuildDocumentModelOptions buildDocumentModelOptions, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(blobContainerUrl, "'blobContainerUrl' cannot be null.");
                BuildDocumentModelRequest buildDocumentModelRequest =
                    getBuildDocumentModelRequest(blobContainerUrl, buildMode, modelId, prefix, fileList,
                        buildDocumentModelOptions);

                return documentModelsImpl.buildModelWithResponseAsync(buildDocumentModelRequest, context)
                    .map(response ->
                        Transforms.toDocumentOperationResult(
                            response.getDeserializedHeaders().getOperationLocation()))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>>
        buildClassifierActivationOperation(String classifierId, Map<String, ClassifierDocumentTypeDetails> docTypes,
                                       BuildDocumentClassifierOptions buildDocumentClassifierOptions, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(docTypes, "'docTypes' cannot be null.");
                BuildDocumentClassifierRequest buildDocumentModelRequest =
                    getBuildDocumentClassifierRequest(classifierId, buildDocumentClassifierOptions.getDescription(), toInnerDocTypes(docTypes));


                return documentClassifiersImpl.buildClassifierWithResponseAsync(buildDocumentModelRequest, context)
                    .map(response ->
                        Transforms.toDocumentOperationResult(
                            response.getDeserializedHeaders().getOperationLocation()))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Mono<PollResponse<OperationResult>> processBuildingModelResponse(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.OperationDetails getOperationResponse,
        PollResponse<OperationResult> trainingModelOperationResponse) {
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
                    Transforms.mapResponseErrorToHttpResponseException(getOperationResponse.getError()));
            case CANCELED:
            default:
                status = LongRunningOperationStatus.fromString(
                    getOperationResponse.getStatus().toString(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status,
            trainingModelOperationResponse.getValue()));
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>>
        getCopyActivationOperation(
        String modelId, DocumentModelCopyAuthorization target, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(modelId, "'modelId' cannot be null.");
                Objects.requireNonNull(target, "'target' cannot be null.");
                com.azure.ai.formrecognizer.documentanalysis.implementation.models.CopyAuthorization copyRequest
                    = getInnerCopyAuthorization(target);

                return documentModelsImpl.copyModelToWithResponseAsync(modelId, copyRequest, context)
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
        return documentModelsImpl.listModelsSinglePageAsync(context)
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
        return documentModelsImpl.listModelsNextSinglePageAsync(nextPageLink, context)
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

    private Mono<PagedResponse<OperationSummary>> listFirstPageOperationInfo(Context context) {
        return miscellaneousImpl.listOperationsSinglePageAsync(context)
            .doOnRequest(ignoredValue -> logger.info("Listing information for all operations"))
            .doOnSuccess(response -> logger.info("Listed all operations"))
            .doOnError(error -> logger.warning("Failed to list all operations information", error))
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                Transforms.toOperationSummary(res.getValue()),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<OperationSummary>> listNextPageOperationInfo(String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        return miscellaneousImpl.listOperationsNextSinglePageAsync(nextPageLink, context)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                Transforms.toOperationSummary(res.getValue()),
                res.getContinuationToken(),
                null));
    }
}
