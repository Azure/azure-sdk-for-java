// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training;

import com.azure.ai.formrecognizer.FormRecognizerAsyncClient;
import com.azure.ai.formrecognizer.FormRecognizerClientBuilder;
import com.azure.ai.formrecognizer.FormRecognizerServiceVersion;
import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.models.Model;
import com.azure.ai.formrecognizer.implementation.models.TrainRequest;
import com.azure.ai.formrecognizer.implementation.models.TrainSourceFilter;
import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.TrainingFileFilter;
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
     * Creates a new {@link FormRecognizerAsyncClient} object. The new {@link FormTrainingAsyncClient}
     * uses the same request policy pipeline as the {@link FormTrainingAsyncClient}.
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
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public FormRecognizerServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Create and train a custom model.
     * Models are trained using documents that are of the following content type -
     * 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff'.
     * Other type of content is ignored.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginTraining#string-boolean}
     *
     * @param trainingFilesUrl source URL parameter that is either an externally accessible Azure
     * storage blob container Uri (preferably a Shared Access Signature Uri).
     * @param useTrainingLabels Boolean to specify the use of labeled files for training the model.
     *
     * @return A {@link PollerFlux} that polls the training model operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<OperationResult, CustomFormModel> beginTraining(String trainingFilesUrl,
        boolean useTrainingLabels) {
        return beginTraining(trainingFilesUrl, useTrainingLabels, null, null);
    }

    /**
     * Create and train a custom model.
     * <p>Models are trained using documents that are of the following content type -
     * 'application/pdf', 'image/jpeg', 'image/png', 'image/tiff'.
     * Other type of content is ignored.
     * </p>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.beginTraining#string-boolean-trainingFileFilter-Duration}
     *
     * @param trainingFilesUrl an externally accessible Azure storage blob container Uri (preferably a
     * Shared Access Signature Uri).
     * @param useTrainingLabels Boolean to specify the use of labeled files for training the model.
     * @param trainingFileFilter Filter to apply to the documents in the source path for training.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it
     * has completed, has failed, or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<OperationResult, CustomFormModel> beginTraining(String trainingFilesUrl,
        boolean useTrainingLabels, TrainingFileFilter trainingFileFilter, Duration pollInterval) {
        final Duration interval = pollInterval != null ? pollInterval : DEFAULT_DURATION;
        return new PollerFlux<OperationResult, CustomFormModel>(
            interval,
            getTrainingActivationOperation(trainingFilesUrl,
                trainingFileFilter != null ? trainingFileFilter.isIncludeSubFolders() : false,
                trainingFileFilter != null ? trainingFileFilter.getPrefix() : null,
                useTrainingLabels),
            createTrainingPollOperation(),
            (activationResponse, context) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchTrainingModelResultOperation());
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CustomFormModel> getCustomModel(String modelId) {
        return getCustomModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get detailed information for a specified custom model id with Http response
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getCustomModelWithResponse#string}
     *
     * @param modelId The UUID string format model identifier.
     *
     * @return A {@link Response} containing the requested {@link CustomFormModel model}.
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
        Objects.requireNonNull(modelId, "'modelId' cannot be null");
        return service.getCustomModelWithResponseAsync(UUID.fromString(modelId), context, true)
            .map(response -> new SimpleResponse<>(response, toCustomFormModel(response.getValue())));
    }

    /**
     * Get account information for all custom models.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getAccountProperties}
     *
     * @return The account information.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccountProperties> getAccountProperties() {
        return getAccountPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get account information.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.getAccountPropertiesWithResponse}
     *
     * @return A {@link Response} containing the requested account information details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccountProperties>> getAccountPropertiesWithResponse() {
        try {
            return withContext(context -> getAccountPropertiesWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AccountProperties>> getAccountPropertiesWithResponse(Context context) {
        return service.getCustomModelsWithResponseAsync(context)
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
     * @return A {@link Mono} containing containing status code and HTTP headers
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
        Objects.requireNonNull(modelId, "'modelId' cannot be null");

        return service.deleteCustomModelWithResponseAsync(UUID.fromString(modelId), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * List information for all models.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.listCustomModels}
     *
     * @return {@link PagedFlux} of {@link CustomFormModelInfo}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CustomFormModelInfo> listCustomModels() {
        try {
            return new PagedFlux<>(() -> withContext(context -> listFirstPageModelInfo(context)),
                continuationToken -> withContext(context -> listNextPageModelInfo(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * List information for all models with taking {@link Context}.
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return {@link PagedFlux} of {@link CustomFormModelInfo}.
     */
    PagedFlux<CustomFormModelInfo> listCustomModels(Context context) {
        return new PagedFlux<>(() -> listFirstPageModelInfo(context),
            continuationToken -> listNextPageModelInfo(continuationToken, context));
    }

    private Mono<PagedResponse<CustomFormModelInfo>> listFirstPageModelInfo(Context context) {
        return service.listCustomModelsSinglePageAsync(context)
            .doOnRequest(ignoredValue -> logger.info("Listing information for all models"))
            .doOnSuccess(response -> logger.info("Listed all models"))
            .doOnError(error -> logger.warning("Failed to list all models information", error))
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
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                toCustomFormModelInfo(res.getValue()),
                res.getContinuationToken(),
                null));
    }

    private Function<PollingContext<OperationResult>, Mono<CustomFormModel>> fetchTrainingModelResultOperation() {
        return (pollingContext) -> {
            try {
                final UUID modelUid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                return service.getCustomModelWithResponseAsync(modelUid, true)
                    .map(modelSimpleResponse -> toCustomFormModel(modelSimpleResponse.getValue()));
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<PollResponse<OperationResult>>>
        createTrainingPollOperation() {
        return (pollingContext) -> {
            try {
                PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
                UUID modelUid = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                return service.getCustomModelWithResponseAsync(modelUid, true)
                    .flatMap(modelSimpleResponse ->
                        processTrainingModelResponse(modelSimpleResponse, operationResultPollResponse));
            } catch (HttpResponseException e) {
                logger.logExceptionAsError(e);
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> getTrainingActivationOperation(
        String fileSourceUrl, boolean includeSubFolders, String filePrefix, boolean useTrainingLabels) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(fileSourceUrl, "'fileSourceUrl' cannot be null.");
                TrainSourceFilter trainSourceFilter = new TrainSourceFilter().setIncludeSubFolders(includeSubFolders)
                    .setPrefix(filePrefix);
                TrainRequest serviceTrainRequest = new TrainRequest().setSource(fileSourceUrl).
                    setSourceFilter(trainSourceFilter).setUseLabelFile(useTrainingLabels);
                return service.trainCustomModelAsyncWithResponseAsync(serviceTrainRequest)
                    .map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getLocation())));
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private static Mono<PollResponse<OperationResult>> processTrainingModelResponse(
        SimpleResponse<Model> trainingModel,
        PollResponse<OperationResult> trainingModelOperationResponse) {
        LongRunningOperationStatus status;
        switch (trainingModel.getValue().getModelInfo().getStatus()) {
            case CREATING:
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            case READY:
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case INVALID:
                status = LongRunningOperationStatus.FAILED;
                break;
            default:
                status = LongRunningOperationStatus.fromString(
                    trainingModel.getValue().getModelInfo().getStatus().toString(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status, trainingModelOperationResponse.getValue()));
    }
}
