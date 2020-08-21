// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training;

import com.azure.ai.formrecognizer.FormRecognizerAsyncClient;
import com.azure.ai.formrecognizer.FormRecognizerClientBuilder;
import com.azure.ai.formrecognizer.FormRecognizerServiceVersion;
import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.implementation.models.CopyAuthorizationResult;
import com.azure.ai.formrecognizer.implementation.models.CopyOperationResult;
import com.azure.ai.formrecognizer.implementation.models.CopyRequest;
import com.azure.ai.formrecognizer.implementation.models.Model;
import com.azure.ai.formrecognizer.implementation.models.ModelStatus;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.implementation.models.TrainRequest;
import com.azure.ai.formrecognizer.implementation.models.TrainSourceFilter;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.training.models.AccountProperties;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.ai.formrecognizer.training.models.CopyAuthorization;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.training.models.CustomFormModelStatus;
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

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.implementation.Utility.parseModelId;
import static com.azure.ai.formrecognizer.training.CustomModelTransforms.DEFAULT_DURATION;
import static com.azure.ai.formrecognizer.training.CustomModelTransforms.toCustomFormModel;
import static com.azure.ai.formrecognizer.training.CustomModelTransforms.toCustomFormModelInfo;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides an asynchronous client that contains model management operations
 * that apply to Azure Form Recognizer.
 * Operations allowed by the client are, to creating, training of custom models, delete models, list models and get
 * subscription account information.
 *
 * <p><strong>Instantiating an asynchronous Form Training Client</strong></p>
 * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.initialization}
 *
 * @see FormTrainingClientBuilder
 * @see FormTrainingAsyncClient
 */
@ServiceClient(builder = FormTrainingClientBuilder.class, isAsync = true)
public final class FormTrainingAsyncClient {

    private final ClientLogger logger = new ClientLogger(FormTrainingAsyncClient.class);
    private final FormRecognizerClientImpl service;
    private final FormRecognizerServiceVersion serviceVersion;

    /**
     * Create a {@link FormTrainingClient} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link FormTrainingClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Form Recognizer supported by this client library.
     */
    FormTrainingAsyncClient(FormRecognizerClientImpl service, FormRecognizerServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Creates a new {@link FormRecognizerAsyncClient} object. The new {@code FormTrainingAsyncClient}
     * uses the same request policy pipeline as the {@code FormTrainingAsyncClient}.
     *
     * @return A new {@link FormRecognizerAsyncClient} object.
     */
    public FormRecognizerAsyncClient getFormRecognizerAsyncClient() {
        return new FormRecognizerClientBuilder().endpoint(getEndpoint()).pipeline(getHttpPipeline()).buildAsyncClient();
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
     * Create and train a custom model.
     * Models are trained using documents that are of the following content type -
     * 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff'.
     * Other type of content is ignored.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/build-training-data-set#upload-your-training-data">here</a>
     * for information on building your own training data set.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginTraining#string-boolean}
     *
     * @param trainingFilesUrl source URL parameter that is an externally accessible Azure
     * storage blob container Uri (preferably a Shared Access Signature Uri).
     * @param useTrainingLabels boolean to specify the use of labeled files for training the model.
     *
     * @return A {@link PollerFlux} that polls the training model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link CustomFormModel custom form model}.
     * @throws FormRecognizerException If training fails and a model with {@link ModelStatus#INVALID} is created.
     * @throws NullPointerException If {@code trainingFilesUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<FormRecognizerOperationResult, CustomFormModel> beginTraining(String trainingFilesUrl,
        boolean useTrainingLabels) {
        return beginTraining(trainingFilesUrl, useTrainingLabels, null);
    }

    /**
     * Create and train a custom model.
     * <p>Models are trained using documents that are of the following content type -
     * 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff'.Other type of content is ignored.
     * </p>
     * See <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/build-training-data-set#upload-your-training-data">here</a>
     * for information on building your own training data set.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginTraining#string-boolean-TrainingOptions}
     *
     * @param trainingFilesUrl an externally accessible Azure storage blob container Uri (preferably a
     * Shared Access Signature Uri).
     * @param useTrainingLabels boolean to specify the use of labeled files for training the model.
     * @param trainingOptions The additional configurable {@link TrainingOptions options}
     * that may be passed when training a model.
     *
     * @return A {@link PollerFlux} that polls the training model operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the trained {@link CustomFormModel custom form model}.
     * @throws FormRecognizerException If training fails and model with {@link ModelStatus#INVALID} is created.
     * @throws NullPointerException If {@code trainingFilesUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<FormRecognizerOperationResult, CustomFormModel> beginTraining(String trainingFilesUrl,
        boolean useTrainingLabels, TrainingOptions trainingOptions) {
        return beginTraining(trainingFilesUrl, useTrainingLabels, trainingOptions,
            Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, CustomFormModel> beginTraining(String trainingFilesUrl,
        boolean useTrainingLabels,
        TrainingOptions trainingOptions, Context context) {
        trainingOptions =  trainingOptions == null ? new TrainingOptions() : trainingOptions;
        return new PollerFlux<FormRecognizerOperationResult, CustomFormModel>(
            trainingOptions.getPollInterval(),
            getTrainingActivationOperation(trainingFilesUrl,
                trainingOptions.getTrainingFileFilter() != null
                    ? trainingOptions.getTrainingFileFilter().isSubfoldersIncluded() : false,
                trainingOptions.getTrainingFileFilter() != null
                    ? trainingOptions.getTrainingFileFilter().getPrefix() : null,
                useTrainingLabels, context),
            createTrainingPollOperation(context),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchTrainingModelResultOperation(context));
    }

    /**
     * Get detailed information for a specified custom model id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCustomModel#string}
     *
     * @param modelId The UUID string format model identifier.
     *
     * @return The detailed information for the specified model.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CustomFormModel> getCustomModel(String modelId) {
        return getCustomModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get detailed information for a specified custom model id with Http response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCustomModelWithResponse#string}
     *
     * @param modelId The UUID string format model identifier.
     *
     * @return A {@link Response} containing the requested {@link CustomFormModel model}.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CustomFormModel>> getCustomModelWithResponse(String modelId) {
        try {
            return withContext(context -> getCustomModelWithResponse(modelId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CustomFormModel>> getCustomModelWithResponse(String modelId, Context context) {
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                + " be null or empty"));
        }
        return service.getCustomModelWithResponseAsync(UUID.fromString(modelId), true, context)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)
            .map(response -> new SimpleResponse<>(response, toCustomFormModel(response.getValue())));
    }

    /**
     * Get account information of the form recognizer account.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getAccountProperties}
     *
     * @return The requested account information details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccountProperties> getAccountProperties() {
        return getAccountPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get account information of the form recognizer account with an Http response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getAccountPropertiesWithResponse}
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
        return service.getCustomModelsWithResponseAsync(context)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)
            .map(response -> new SimpleResponse<>(response,
                new AccountProperties(response.getValue().getSummary().getCount(),
                    response.getValue().getSummary().getLimit())));
    }

    /**
     * Deletes the specified custom model.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.deleteModel#string}
     *
     * @param modelId The UUID string format model identifier.
     *
     * @return An empty Mono.
     * @throws IllegalArgumentException If {@code modelId} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteModel(String modelId) {
        return deleteModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified custom model.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.deleteModelWithResponse#string}
     *
     * @param modelId The UUID string format model identifier.
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
        return service.deleteCustomModelWithResponseAsync(UUID.fromString(modelId), context)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * List information for each model on the form recognizer account.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.listCustomModels}
     *
     * @return {@link PagedFlux} of {@link CustomFormModelInfo}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CustomFormModelInfo> listCustomModels() {
        try {
            return new PagedFlux<>(() -> withContext(this::listFirstPageModelInfo),
                continuationToken -> withContext(context -> listNextPageModelInfo(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * List information for each model on the form recognizer account with an Http response and a specified
     * {@link Context}.
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedFlux} of {@link CustomFormModelInfo}.
     */
    PagedFlux<CustomFormModelInfo> listCustomModels(Context context) {
        return new PagedFlux<>(() -> listFirstPageModelInfo(context),
            continuationToken -> listNextPageModelInfo(continuationToken, context));
    }

    /**
     * Copy a custom model stored in this resource (the source) to the user specified target Form Recognizer resource.
     *
     * <p>This should be called with the source Form Recognizer resource (with the model that is intended to be copied).
     * The target parameter should be supplied from the target resource's output from
     * {@link FormTrainingAsyncClient#getCopyAuthorization(String, String)} method.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginCopyModel#string-copyAuthorization}
     *
     * @param modelId Model identifier of the model to copy to the target Form Recognizer resource
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link FormTrainingAsyncClient#getCopyAuthorization(String, String)}
     *
     * @return A {@link PollerFlux} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the copied model {@link CustomFormModelInfo}.
     * @throws FormRecognizerException If copy operation fails and model with {@link OperationStatus#FAILED} is created.
     * @throws NullPointerException If {@code modelId}, {@code target} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<FormRecognizerOperationResult, CustomFormModelInfo> beginCopyModel(String modelId,
        CopyAuthorization target) {
        return beginCopyModel(modelId, target, null);
    }

    /**
     * Copy a custom model stored in this resource (the source) to the user specified target Form Recognizer resource.
     *
     * <p>This should be called with the source Form Recognizer resource (with the model that is intended to be copied).
     * The target parameter should be supplied from the target resource's output from
     * {@link FormTrainingAsyncClient#getCopyAuthorization(String, String)} method.
     * </p>
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginCopyModel#string-copyAuthorization-Duration}
     *
     * @param modelId Model identifier of the model to copy to the target Form Recognizer resource
     * @param target the copy authorization to the target Form Recognizer resource. The copy authorization can be
     * generated from the target resource's call to {@link FormTrainingAsyncClient#getCopyAuthorization(String, String)}
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link PollerFlux} that polls the copy model operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns the copied model {@link CustomFormModelInfo}.
     * @throws FormRecognizerException If copy operation fails and model with {@link OperationStatus#FAILED}
     * is created.
     * @throws NullPointerException If {@code modelId}, {@code target} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<FormRecognizerOperationResult, CustomFormModelInfo> beginCopyModel(String modelId,
        CopyAuthorization target, Duration pollInterval) {
        return beginCopyModel(modelId, target, pollInterval, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, CustomFormModelInfo> beginCopyModel(String modelId,
        CopyAuthorization target, Duration pollInterval, Context context) {
        final Duration interval = pollInterval != null ? pollInterval : DEFAULT_DURATION;
        return new PollerFlux<FormRecognizerOperationResult, CustomFormModelInfo>(
            interval,
            getCopyActivationOperation(modelId, target, context),
            createCopyPollOperation(modelId, context),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchCopyModelResultOperation(modelId, target.getModelId(), context));
    }

    /**
     * Generate authorization for copying a custom model into the target Form Recognizer resource.
     *
     * @param resourceId Azure Resource Id of the target Form Recognizer resource where the model will be copied to.
     * @param resourceRegion Location of the target Form Recognizer resource. A valid Azure region name supported
     * by Cognitive Services.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCopyAuthorization#string-string}
     *
     * @return The {@link CopyAuthorization} that could be used to authorize copying model between resources.
     * @throws NullPointerException If {@code resourceId}, {@code resourceRegion} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CopyAuthorization> getCopyAuthorization(String resourceId, String resourceRegion) {
        return getCopyAuthorizationWithResponse(resourceId, resourceRegion).flatMap(FluxUtil::toMono);
    }

    /**
     * Generate authorization for copying a custom model into the target Form Recognizer resource.
     * This should be called by the target resource (where the model will be copied to) and the output can be passed as
     * the target parameter into {@link FormTrainingAsyncClient#beginCopyModel(String, CopyAuthorization)}.
     *
     * @param resourceId Azure Resource Id of the target Form Recognizer resource where the model will be copied to.
     * @param resourceRegion Location of the target Form Recognizer resource. A valid Azure region name supported by
     * Cognitive Services.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCopyAuthorizationWithResponse#string-string}
     *
     * @return A {@link Response} containing the {@link CopyAuthorization} that could be used to authorize copying
     * model between resources.
     * @throws NullPointerException If {@code resourceId}, {@code resourceRegion} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CopyAuthorization>> getCopyAuthorizationWithResponse(String resourceId,
        String resourceRegion) {
        try {
            return withContext(context -> getCopyAuthorizationWithResponse(resourceId, resourceRegion, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CopyAuthorization>> getCopyAuthorizationWithResponse(String resourceId, String resourceRegion,
        Context context) {
        Objects.requireNonNull(resourceId, "'resourceId' cannot be null");
        Objects.requireNonNull(resourceRegion, "'resourceRegion' cannot be null");
        return service.generateModelCopyAuthorizationWithResponseAsync(context)
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)
            .map(response -> {
                CopyAuthorizationResult copyAuthorizationResult = response.getValue();
                return new SimpleResponse<>(response, new CopyAuthorization(copyAuthorizationResult.getModelId(),
                    copyAuthorizationResult.getAccessToken(), resourceId, resourceRegion,
                    copyAuthorizationResult.getExpirationDateTimeTicks()));
            });
    }

    private Mono<PagedResponse<CustomFormModelInfo>> listFirstPageModelInfo(Context context) {
        return service.listCustomModelsSinglePageAsync(context)
            .doOnRequest(ignoredValue -> logger.info("Listing information for all models"))
            .doOnSuccess(response -> logger.info("Listed all models"))
            .doOnError(error -> logger.warning("Failed to list all models information", error))
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                toCustomFormModelInfo(res.getValue()),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<CustomFormModelInfo>> listNextPageModelInfo(String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        return service.listCustomModelsNextSinglePageAsync(nextPageLink, context)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                toCustomFormModelInfo(res.getValue()),
                res.getContinuationToken(),
                null));
    }

    private Function<PollingContext<FormRecognizerOperationResult>, Mono<CustomFormModelInfo>>
        fetchCopyModelResultOperation(
        String modelId, String copyModelId, Context context) {
        return (pollingContext) -> {
            try {
                final UUID resultUid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                Objects.requireNonNull(modelId, "'modelId' cannot be null.");
                return service.getCustomModelCopyResultWithResponseAsync(UUID.fromString(modelId), resultUid, context)
                    .map(modelSimpleResponse -> {
                        CopyOperationResult copyOperationResult = modelSimpleResponse.getValue();
                        return new CustomFormModelInfo(copyModelId,
                            copyOperationResult.getStatus() == OperationStatus.SUCCEEDED
                                ? CustomFormModelStatus.READY
                                : CustomFormModelStatus.fromString(copyOperationResult.getStatus().toString()),
                            copyOperationResult.getCreatedDateTime(),
                            copyOperationResult.getLastUpdatedDateTime());
                    })
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<FormRecognizerOperationResult>, Mono<PollResponse<FormRecognizerOperationResult>>>
        createCopyPollOperation(String modelId, Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<FormRecognizerOperationResult> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                UUID targetId = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                return service.getCustomModelCopyResultWithResponseAsync(UUID.fromString(modelId), targetId, context)
                    .flatMap(modelSimpleResponse ->
                        processCopyModelResponse(modelSimpleResponse, operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (HttpResponseException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<FormRecognizerOperationResult>, Mono<FormRecognizerOperationResult>>
        getCopyActivationOperation(
        String modelId, CopyAuthorization target, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(modelId, "'modelId' cannot be null.");
                Objects.requireNonNull(target, "'target' cannot be null.");
                CopyRequest copyRequest = new CopyRequest()
                    .setTargetResourceId(target.getResourceId())
                    .setTargetResourceRegion(target.getResourceRegion())
                    .setCopyAuthorization(new CopyAuthorizationResult()
                        .setModelId(target.getModelId())
                        .setAccessToken(target.getAccessToken())
                        .setExpirationDateTimeTicks(target.getExpiresOn().toEpochSecond()));
                return service.copyCustomModelWithResponseAsync(UUID.fromString(modelId), copyRequest, context)
                    .map(response ->
                        new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Mono<PollResponse<FormRecognizerOperationResult>> processCopyModelResponse(
        Response<CopyOperationResult> copyModel,
        PollResponse<FormRecognizerOperationResult> copyModelOperationResponse) {
        LongRunningOperationStatus status;
        switch (copyModel.getValue().getStatus()) {
            case NOT_STARTED:
            case RUNNING:
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            case SUCCEEDED:
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case FAILED:
                throw logger.logExceptionAsError(new FormRecognizerException("Copy operation failed",
                    copyModel.getValue().getCopyResult().getErrors().stream()
                        .map(errorInformation ->
                            new FormRecognizerErrorInformation(errorInformation.getCode(),
                                errorInformation.getMessage()))
                        .collect(Collectors.toList())));
            default:
                status = LongRunningOperationStatus.fromString(copyModel.getValue().getStatus().toString(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status, copyModelOperationResponse.getValue()));
    }

    private Function<PollingContext<FormRecognizerOperationResult>, Mono<CustomFormModel>>
        fetchTrainingModelResultOperation(Context context) {
        return (pollingContext) -> {
            try {
                final UUID modelUid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                return service.getCustomModelWithResponseAsync(modelUid, true, context)
                    .map(modelSimpleResponse -> toCustomFormModel(modelSimpleResponse.getValue()))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<FormRecognizerOperationResult>, Mono<PollResponse<FormRecognizerOperationResult>>>
        createTrainingPollOperation(Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<FormRecognizerOperationResult> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                UUID modelUid = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                return service.getCustomModelWithResponseAsync(modelUid, true, context)
                    .flatMap(modelSimpleResponse ->
                        processTrainingModelResponse(modelSimpleResponse, operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            }  catch (HttpResponseException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<FormRecognizerOperationResult>, Mono<FormRecognizerOperationResult>>
        getTrainingActivationOperation(
        String trainingFilesUrl, boolean includeSubfolders, String filePrefix, boolean useTrainingLabels,
        Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(trainingFilesUrl, "'trainingFilesUrl' cannot be null.");
                TrainSourceFilter trainSourceFilter = new TrainSourceFilter().setIncludeSubFolders(includeSubfolders)
                    .setPrefix(filePrefix);
                TrainRequest serviceTrainRequest = new TrainRequest().setSource(trainingFilesUrl).
                    setSourceFilter(trainSourceFilter).setUseLabelFile(useTrainingLabels);
                return service.trainCustomModelAsyncWithResponseAsync(serviceTrainRequest, context)
                    .map(response -> new FormRecognizerOperationResult(
                        parseModelId(response.getDeserializedHeaders().getLocation())))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Mono<PollResponse<FormRecognizerOperationResult>> processTrainingModelResponse(
        Response<Model> trainingModel,
        PollResponse<FormRecognizerOperationResult> trainingModelOperationResponse) {
        LongRunningOperationStatus status;
        switch (trainingModel.getValue().getModelInfo().getStatus()) {
            case CREATING:
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            case READY:
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case INVALID:
                throw logger.logExceptionAsError(new FormRecognizerException(String.format("Invalid model created"
                    + " with model Id %s", trainingModel.getValue().getModelInfo().getModelId()),
                    trainingModel.getValue().getTrainResult().getErrors().stream()
                        .map(errorInformation ->
                            new FormRecognizerErrorInformation(errorInformation.getCode(),
                                errorInformation.getMessage()))
                        .collect(Collectors.toList())));
            default:
                status = LongRunningOperationStatus.fromString(
                    trainingModel.getValue().getModelInfo().getStatus().toString(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status, trainingModelOperationResponse.getValue()));
    }
}
