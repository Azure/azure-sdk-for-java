// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.models.Model;
import com.azure.ai.formrecognizer.implementation.models.TrainRequest;
import com.azure.ai.formrecognizer.implementation.models.TrainSourceFilter;
import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
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

import static com.azure.ai.formrecognizer.CustomModelTransforms.toCustomFormModel;
import static com.azure.ai.formrecognizer.implementation.Utility.parseModelId;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides an asynchronous client that contains model management operations
 * that apply to Azure Form Recognizer.
 * Operations allowed by the client are, to create/tracin custom models. delete models, list models.
 *
 * @see FormRecognizerClientBuilder
 */
public class FormTrainingAsyncClient {

    private final ClientLogger logger = new ClientLogger(FormTrainingAsyncClient.class);
    private final FormRecognizerClientImpl service;
    private final FormRecognizerServiceVersion serviceVersion;

    /**
     * Create a {@link FormTrainingClient} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link FormRecognizerClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Form Recognizer supported by this client library.
     */
    FormTrainingAsyncClient(FormRecognizerClientImpl service, FormRecognizerServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
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
     * @param fileSourceUrl source URL parameter that is either an externally accessible Azure
     * storage blob container Uri (preferably a Shared Access Signature Uri).
     * @param useLabelFile Boolean to specify the use of labeled files for training the model.
     *
     * @return A {@link PollerFlux} that polls the training model operation until it has completed, has failed, or has
     * been cancelled.
     */
    public PollerFlux<OperationResult, CustomFormModel> beginTraining(String fileSourceUrl, boolean useLabelFile) {
        return beginTraining(fileSourceUrl, useLabelFile, false, null, null);
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
     * @param fileSourceUrl source URL parameter that is either an externally accessible Azure
     * storage blob container Uri (preferably a Shared Access Signature Uri).
     * @param useLabelFile Boolean to specify the use of labeled files for training the model.
     * @param includeSubFolders to indicate if sub folders within the set of prefix folders will
     * also need to be included when searching for content to be preprocessed.
     * @param filePrefix A case-sensitive prefix string to filter documents in the source path
     * for training. For example, when using a Azure storage blob Uri, use the prefix to restrict
     * sub folders for training.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it
     * has completed, has failed, or has been cancelled.
     */
    public PollerFlux<OperationResult, CustomFormModel> beginTraining(String fileSourceUrl,
        boolean useLabelFile, boolean includeSubFolders, String filePrefix, Duration pollInterval) {
        Objects.requireNonNull(fileSourceUrl, "'fileSourceUrl' cannot be null.");
        Objects.requireNonNull(useLabelFile, "'useLabelFile' cannot be null.");
        final Duration interval = pollInterval != null ? pollInterval : Duration.ofSeconds(5);

        return new PollerFlux<OperationResult, CustomFormModel>(
            interval,
            getTrainingActivationOperation(fileSourceUrl, includeSubFolders, filePrefix, useLabelFile),
            createTrainingPollOperation(),
            (activationResponse, context) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchTrainingModelResultOperation());
    }

    /**
     * Get detailed information for a specified custom model id.
     *
     * @param modelId Model identifier.
     *
     * @return The detailed information for the specified model.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CustomFormModel> getCustomModel(String modelId) {
        return getCustomFormModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get detailed information for a specified custom model id with Http response
     *
     * @param modelId Model identifier.
     *
     * @return A {@link Response} containing the requested {@link CustomFormModel model}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CustomFormModel>> getCustomFormModelWithResponse(String modelId) {
        try {
            return withContext(context -> getCustomFormModelWithResponse(modelId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<CustomFormModel>> getCustomFormModelWithResponse(String modelId, Context context) {
        Objects.requireNonNull(modelId, "'modelId' cannot be null");
        return service.getCustomModelWithResponseAsync(UUID.fromString(modelId), context, true)
            .map(response -> new SimpleResponse<>(response, toCustomFormModel(response.getValue())));
    }

    /**
     * Get account information for all custom models.
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

    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<AccountProperties>> getAccountPropertiesWithResponse(Context context) {
        return service.getCustomModelsWithResponseAsync(context)
            .map(response -> new SimpleResponse<>(response,
            new AccountProperties(response.getValue().getSummary().getCount(),
                response.getValue().getSummary().getLimit())));
    }

    /**
     * Deletes the specified custom model.
     *
     * @param modelId The modelIdentifier.
     * @return An empty Mono.
     */
    public Mono<Void> deleteModel(String modelId) {
        return deleteModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the specified custom model.
     *
     * @param modelId The modelIdentifier
     *
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
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

    // list models
    // TODO (shawn) : #9976

    private Function<PollingContext<OperationResult>, Mono<CustomFormModel>> fetchTrainingModelResultOperation() {
        return (pollingContext) -> {
            final UUID modelUid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
            return service.getCustomModelWithResponseAsync(modelUid, true)
                .map(modelSimpleResponse -> toCustomFormModel(modelSimpleResponse.getValue()));
        };
    }

    private Function<PollingContext<OperationResult>, Mono<PollResponse<OperationResult>>>
        createTrainingPollOperation() {
        return (pollingContext) -> {
            PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
            try {
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
        String sourcePath, boolean includeSubFolders, String filePrefix, boolean useLabelFile) {

        TrainSourceFilter trainSourceFilter = new TrainSourceFilter().setIncludeSubFolders(includeSubFolders)
            .setPrefix(filePrefix);
        TrainRequest serviceTrainRequest = new TrainRequest().setSource(sourcePath).
            setSourceFilter(trainSourceFilter).setUseLabelFile(useLabelFile);
        return (pollingContext) -> {
            try {
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
