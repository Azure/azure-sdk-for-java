// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration;

import com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.DocumentAnalysisServiceVersion;
import com.azure.ai.formrecognizer.administration.models.AccountProperties;
import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.administration.models.CreateComposedModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;
import com.azure.ai.formrecognizer.administration.models.ModelOperation;
import com.azure.ai.formrecognizer.administration.models.ModelOperationInfo;
import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.models.AuthorizeCopyRequest;
import com.azure.ai.formrecognizer.implementation.models.AzureBlobContentSource;
import com.azure.ai.formrecognizer.implementation.models.BuildDocumentModelRequest;
import com.azure.ai.formrecognizer.implementation.models.ComponentModelInfo;
import com.azure.ai.formrecognizer.implementation.models.ComposeDocumentModelRequest;
import com.azure.ai.formrecognizer.implementation.models.GetOperationResponse;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.implementation.util.Transforms;
import com.azure.ai.formrecognizer.implementation.util.Utility;
import com.azure.ai.formrecognizer.models.DocumentAnalysisException;
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
 * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.initialization}
 *
 * @see DocumentModelAdministrationClientBuilder
 * @see DocumentModelAdministrationAsyncClient
 */
@ServiceClient(builder = DocumentModelAdministrationClientBuilder.class, isAsync = true)
public final class DocumentModelAdministrationAsyncClient {

    private final ClientLogger logger = new ClientLogger(DocumentModelAdministrationAsyncClient.class);
    private final FormRecognizerClientImpl service;
    private final DocumentAnalysisServiceVersion serviceVersion;

    /**
     * Create a {@link DocumentModelAdministrationAsyncClient} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link DocumentModelAdministrationClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Form Recognizer supported by this client library.
     */
    DocumentModelAdministrationAsyncClient(FormRecognizerClientImpl service, DocumentAnalysisServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Creates a new {@link DocumentAnalysisAsyncClient} object. The new {@code DocumentTrainingAsyncClient}
     * uses the same request policy pipeline as the {@code DocumentTrainingAsyncClient}.
     *
     * @return A new {@link DocumentAnalysisAsyncClient} object.
     */
    public DocumentAnalysisAsyncClient getDocumentAnalysisAsyncClient() {
        return new DocumentAnalysisClientBuilder().endpoint(getEndpoint()).pipeline(getHttpPipeline())
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
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-String}
     *
     * @param trainingFilesUrl source URL parameter that is an externally accessible Azure
     * storage blob container Url (preferably a Shared Access Signature Url).
     * @param modelId unique model identifier. If not specified, a model ID will be created for you.
     *
     * @return A {@link PollerFlux} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link DocumentModel custom document analysis model}.
     * @throws DocumentAnalysisException If building a model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code trainingFilesUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, DocumentModel> beginBuildModel(String trainingFilesUrl,
                                                                              String modelId) {
        return beginBuildModel(trainingFilesUrl, modelId, null);
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
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginBuildModel#String-String-BuildModelOptions}
     *
     * @param trainingFilesUrl source URL parameter that is an externally accessible Azure
     * storage blob container Url (preferably a Shared Access Signature Url).
     * @param modelId unique model identifier. If not specified, a model ID will be created for you.
     * @param buildModelOptions The configurable {@link BuildModelOptions options} to pass when
     * building a custom document analysis model.
     *
     * @return A {@link PollerFlux} that polls the building model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link DocumentModel custom document analysis model}.
     * @throws DocumentAnalysisException If building a model fails with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code trainingFilesUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, DocumentModel> beginBuildModel(String trainingFilesUrl,
        String modelId, BuildModelOptions buildModelOptions) {
        return beginBuildModel(trainingFilesUrl, modelId, buildModelOptions, Context.NONE);
    }

    PollerFlux<DocumentOperationResult, DocumentModel> beginBuildModel(String trainingFilesUrl,
        String modelId, BuildModelOptions buildModelOptions, Context context) {

        buildModelOptions =  buildModelOptions == null ? new BuildModelOptions() : buildModelOptions;
        return new PollerFlux<DocumentOperationResult, DocumentModel>(
            DEFAULT_POLL_INTERVAL,
            buildModelActivationOperation(trainingFilesUrl, modelId, buildModelOptions, context),
            createModelPollOperation(context),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchModelResultOperation(context));
    }

    /**
     * Get account information of the Form Recognizer account.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getAccountProperties}
     *
     * @return The requested account information details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccountProperties> getAccountProperties() {
        return getAccountPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get account information of the Form Recognizer account with a Http response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getAccountPropertiesWithResponse}
     *
     * @return A {@link Response} containing the requested account information details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccountProperties>> getAccountPropertiesWithResponse() {
        try {
            return withContext(this::getAccountPropertiesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AccountProperties>> getAccountPropertiesWithResponse(Context context) {
        return service.getInfoWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, Transforms.toAccountProperties(response.getValue())));
    }

    /**
     * Deletes the specified custom document analysis model.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.deleteModel#string}
     *
     * @param modelId The unique model identifier.
     *
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
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.deleteModelWithResponse#string}
     *
     * @param modelId The unique model identifier.
     *
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
     * the target parameter into {@link DocumentModelAdministrationAsyncClient#beginCopyModel(String, CopyAuthorization)}.
     * </p>
     *
     * @param modelId A unique ID for your copied model. If not specified, a model ID will be created for you.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorization#string}
     *
     * @return The {@link CopyAuthorization} that could be used to authorize copying model between resources.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CopyAuthorization> getCopyAuthorization(String modelId) {
        return getCopyAuthorizationWithResponse(modelId, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Generate authorization for copying a custom document analysis model into the target Form Recognizer resource.
     * <p>This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link DocumentModelAdministrationAsyncClient#beginCopyModel(String, CopyAuthorization)}.
     * </p>
     *
     * @param modelId A unique ID for your copied model. If not specified, a model ID will be created for you.
     * @param copyAuthorizationOptions The configurable {@link CopyAuthorizationOptions options} to pass when
     * copying a model.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getCopyAuthorizationWithResponse#string-CopyAuthorizationOptions}
     *
     * @return The {@link CopyAuthorization} that could be used to authorize copying model between resources.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CopyAuthorization>> getCopyAuthorizationWithResponse(String modelId,
        CopyAuthorizationOptions copyAuthorizationOptions) {
        try {
            return withContext(context -> getCopyAuthorizationWithResponse(modelId, copyAuthorizationOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CopyAuthorization>> getCopyAuthorizationWithResponse(String modelId,
        CopyAuthorizationOptions copyAuthorizationOptions, Context context) {
        copyAuthorizationOptions = copyAuthorizationOptions == null
            ? new CopyAuthorizationOptions() : copyAuthorizationOptions;
        modelId = modelId == null ? Utility.generateRandomModelID() : modelId;
        AuthorizeCopyRequest authorizeCopyRequest = new AuthorizeCopyRequest().setModelId(modelId).setDescription(
            copyAuthorizationOptions.getDescription());

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
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCreateComposedModel#list-String}
     *
     * @param modelIDs The list of component models to compose.
     * @param modelId The unique model identifier for the composed model.
     *
     * @return A {@link PollerFlux} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the created {@link DocumentModel composed model}.
     * @throws DocumentAnalysisException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code modelIDs} or {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, DocumentModel> beginCreateComposedModel(
        List<String> modelIDs, String modelId) {
        return beginCreateComposedModel(modelIDs, modelId, null, null);
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
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCreateComposedModel#list-String-createComposedModelOptions}
     *
     * @param modelIDs The list of component models to compose.
     * @param modelId The unique model identifier for the composed model.
     * @param createComposedModelOptions The configurable {@link CreateComposedModelOptions options} to pass when
     * creating a composed model.
     *
     * @return A {@link PollerFlux} that polls the create composed model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the copied model {@link DocumentModel}.
     * @throws DocumentAnalysisException If create composed model operation fails and model with
     * {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If the list of {@code modelIDs} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, DocumentModel> beginCreateComposedModel(List<String> modelIDs,
        String modelId, CreateComposedModelOptions createComposedModelOptions) {
        return beginCreateComposedModel(modelIDs, modelId, createComposedModelOptions, Context.NONE);
    }

    PollerFlux<DocumentOperationResult, DocumentModel> beginCreateComposedModel(List<String> modelIDs,
        String modelId, CreateComposedModelOptions createComposedModelOptions, Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(modelIDs)) {
                throw logger.logExceptionAsError(new NullPointerException("'modelIDs' cannot be null or empty"));
            } else if (CoreUtils.isNullOrEmpty(modelId)) {
                throw logger.logExceptionAsError(new NullPointerException("'modelId' cannot be null or empty"));
            }
            createComposedModelOptions = getCreateComposeModelOptions(createComposedModelOptions);

            final ComposeDocumentModelRequest composeRequest = new ComposeDocumentModelRequest()
                .setComponentModels(modelIDs.stream()
                    .map(modelIdString -> new ComponentModelInfo().setModelId(modelIdString))
                    .collect(Collectors.toList()))
                .setModelId(modelId)
                .setDescription(createComposedModelOptions.getDescription());

            return new PollerFlux<DocumentOperationResult, DocumentModel>(
                DEFAULT_POLL_INTERVAL,
                Utility.activationOperation(() -> service.composeDocumentModelWithResponseAsync(composeRequest, context)
                    .map(response -> Transforms.toFormRecognizerOperationResult(
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
     * {@link DocumentModelAdministrationAsyncClient#getCopyAuthorization(String)} method.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.beginCopyModel#string-copyAuthorization}
     *
     * @param modelId Model identifier of the model to copy to target resource.
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link DocumentModelAdministrationAsyncClient#getCopyAuthorization(String)}
     *
     * @return A {@link PollerFlux} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the copied model {@link DocumentModel}.
     * @throws DocumentAnalysisException If copy operation fails and model with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code modelId} or {@code target} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, DocumentModel> beginCopyModel(String modelId,
        CopyAuthorization target) {
        return beginCopyModel(modelId, target, null);
    }

    PollerFlux<DocumentOperationResult, DocumentModel> beginCopyModel(String modelId,
        CopyAuthorization target, Context context) {
        return new PollerFlux<DocumentOperationResult, DocumentModel>(
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
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listModels}
     *
     * @return {@link PagedFlux} of {@link DocumentModelInfo}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DocumentModelInfo> listModels() {
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
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listModels}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedFlux} of {@link DocumentModelInfo}.
     */
    PagedFlux<DocumentModelInfo> listModels(Context context) {
        return new PagedFlux<>(() -> listFirstPageModelInfo(context),
            continuationToken -> listNextPageModelInfo(continuationToken, context));
    }

    /**
     * Get detailed information for a specified model ID.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getModel#string}
     *
     * @param modelId The unique model identifier.
     *
     * @return The detailed information for the specified model.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentModel> getModel(String modelId) {
        return getModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get detailed information for a specified model ID with Http response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getModelWithResponse#string}
     *
     * @param modelId The unique model identifier.
     *
     * @return A {@link Response} containing the requested {@link DocumentModel model}.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DocumentModel>> getModelWithResponse(String modelId) {
        try {
            return withContext(context -> getModelWithResponse(modelId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DocumentModel>> getModelWithResponse(String modelId, Context context) {
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
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getOperation#string}
     *
     * @param operationId Unique operation ID.
     *
     * @return detailed operation information for the specified ID.
     * @throws IllegalArgumentException If {@code operationId} is null or empty.
     * @throws HttpResponseException If the {@code operationId} is past 24 hours.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ModelOperation> getOperation(String operationId) {
        return getOperationWithResponse(operationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get detailed operation information for the specified ID with Http response.
     * <p> This operations fails if the operation ID used is past 24 hours.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.getOperationWithResponse#string}
     *
     * @param operationId Unique operation ID.
     *
     * @return A {@link Response} containing the requested {@link ModelOperation}.
     * @throws IllegalArgumentException If {@code operationId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ModelOperation>> getOperationWithResponse(String operationId) {
        try {
            return withContext(context -> getOperationWithResponse(operationId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ModelOperation>> getOperationWithResponse(String operationId, Context context) {
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
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listOperations}
     *
     * @return {@link PagedFlux} of {@link ModelOperationInfo}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ModelOperationInfo> listOperations() {
        try {
            return new PagedFlux<>(() -> withContext(this::listFirstPageOperationInfo),
                continuationToken -> withContext(context -> listNextPageOperationInfo(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * List information for each operation on the Form Recognizer account with a Http response and a specified
     * {@link Context}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient.listOperations}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedFlux} of {@link ModelOperationInfo}.
     */
    PagedFlux<ModelOperationInfo> listOperations(Context context) {
        return new PagedFlux<>(() -> listFirstPageOperationInfo(context),
            continuationToken -> listNextPageOperationInfo(continuationToken, context));
    }

    private Function<PollingContext<DocumentOperationResult>, Mono<DocumentModel>>
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
        String trainingFilesUrl, String modelId, BuildModelOptions buildModelOptions, Context context) {
        if (modelId == null) {
            modelId = Utility.generateRandomModelID();
        }
        String finalModelId = modelId;
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(trainingFilesUrl, "'trainingFilesUrl' cannot be null.");
                BuildDocumentModelRequest buildDocumentModelRequest = new BuildDocumentModelRequest()
                    .setModelId(finalModelId)
                    .setAzureBlobSource(new AzureBlobContentSource()
                        .setContainerUrl(trainingFilesUrl)
                        .setPrefix(buildModelOptions.getPrefix()))
                    .setDescription(buildModelOptions.getDescription());

                return service.buildDocumentModelWithResponseAsync(buildDocumentModelRequest, context)
                    .map(response ->
                        Transforms.toFormRecognizerOperationResult(
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
                // TODO (Revisit error logic https://github.com/Azure/azure-sdk-for-java-pr/issues/1337)
                throw logger.logExceptionAsError(
                    Transforms.toDocumentAnalysisException(getOperationResponse.getError()));
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
                        Transforms.toFormRecognizerOperationResult(
                            response.getDeserializedHeaders().getOperationLocation()))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Mono<PagedResponse<DocumentModelInfo>> listFirstPageModelInfo(Context context) {
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

    private Mono<PagedResponse<DocumentModelInfo>> listNextPageModelInfo(String nextPageLink, Context context) {
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

    private Mono<PagedResponse<ModelOperationInfo>> listFirstPageOperationInfo(Context context) {
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

    private Mono<PagedResponse<ModelOperationInfo>> listNextPageOperationInfo(String nextPageLink, Context context) {
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

    private static CreateComposedModelOptions
        getCreateComposeModelOptions(CreateComposedModelOptions userProvidedOptions) {
        return userProvidedOptions == null ? new CreateComposedModelOptions() : userProvidedOptions;
    }
}
