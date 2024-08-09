// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.administration;

import com.azure.ai.personalizer.administration.models.CreateEvaluationOperationResult;
import com.azure.ai.personalizer.administration.models.EvaluationOperationException;
import com.azure.ai.personalizer.administration.models.EvaluationsCreateHeaders;
import com.azure.ai.personalizer.administration.models.PersonalizerEvaluation;
import com.azure.ai.personalizer.administration.models.PersonalizerEvaluationOptions;
import com.azure.ai.personalizer.administration.models.PersonalizerLogProperties;
import com.azure.ai.personalizer.administration.models.PersonalizerModelProperties;
import com.azure.ai.personalizer.administration.models.PersonalizerPolicy;
import com.azure.ai.personalizer.administration.models.PersonalizerPolicyReferenceOptions;
import com.azure.ai.personalizer.administration.models.PersonalizerServiceProperties;
import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3Impl;
import com.azure.ai.personalizer.implementation.util.ModelTransforms;
import com.azure.ai.personalizer.implementation.util.Transforms;
import com.azure.ai.personalizer.implementation.util.Utility;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
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
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.ai.personalizer.models.EvaluationJobStatus.COMPLETED;
import static com.azure.ai.personalizer.models.EvaluationJobStatus.FAILED;
import static com.azure.ai.personalizer.models.EvaluationJobStatus.NOT_SUBMITTED;
import static com.azure.ai.personalizer.models.EvaluationJobStatus.ONLINE_POLICY_RETAINED;
import static com.azure.ai.personalizer.models.EvaluationJobStatus.OPTIMAL_POLICY_APPLIED;
import static com.azure.ai.personalizer.models.EvaluationJobStatus.PENDING;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides an asynchronous client that contains the operations that apply to Azure Personalizer.
 * Operations allowed by the client are viewing and editing the properties, policy, model, running evaluations.
 *
 * <p><strong>Instantiating an asynchronous Personalizer Admin Client</strong></p>
 * <!-- src_embed com.azure.ai.personalizer.PersonalizerAdministrationAsyncClient.instantiation -->
 * <pre>
 * PersonalizerAdministrationAsyncClient adminClient = new PersonalizerAdministrationClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.personalizer.PersonalizerAdministrationAsyncClient.instantiation -->
 *
 * @see PersonalizerAdministrationClientBuilder
 */
@ServiceClient(builder = PersonalizerAdministrationClientBuilder.class, isAsync = true)
public final class PersonalizerAdministrationAsyncClient {

    private final PersonalizerClientV1Preview3Impl service;
    private final ClientLogger logger = new ClientLogger(PersonalizerAdministrationAsyncClient.class);

    PersonalizerAdministrationAsyncClient(PersonalizerClientV1Preview3Impl service) {
        this.service = service;
    }

    /**
     * Submit a new Offline Evaluation job.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param evaluationOptions The Offline Evaluation job definition.
     * @return A {@link PollerFlux} that polls the counterfactual evaluation until it has completed, has failed, or has
     * been cancelled. The completed operation returns the completed {@link PersonalizerEvaluation evaluation with results}.
     * @throws EvaluationOperationException If the evaluation does not complete successfully.
     * @throws NullPointerException thrown if evaluationOptions is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<CreateEvaluationOperationResult, PersonalizerEvaluation> beginCreateEvaluation(PersonalizerEvaluationOptions evaluationOptions) {
        return beginCreateEvaluation(evaluationOptions, Context.NONE);
    }

    /**
     * Get the Offline Evaluation associated with the Id.
     *
     * @param evaluationId Id of the Offline Evaluation.
     * @return the Offline Evaluation associated with the Id on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if the evaluationId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerEvaluation> getEvaluation(String evaluationId) {
        return getEvaluationWithResponse(evaluationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get the Offline Evaluation associated with the Id.
     *
     * @param evaluationId Id of the Offline Evaluation.
     * @return the Offline Evaluation associated with the Id along with {@link Response} on successful completion of
     * {@link Mono}.
     * @throws IllegalArgumentException thrown if the evaluationId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerEvaluation>> getEvaluationWithResponse(String evaluationId) {
        try {
            return withContext(context -> getEvaluationWithResponse(evaluationId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete the Offline Evaluation associated with the Id.
     *
     * @param evaluationId Id of the Offline Evaluation to delete.
     * @return the completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if the evaluationId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEvaluation(String evaluationId) {
        return deleteEvaluationWithResponse(evaluationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Delete the Offline Evaluation associated with the Id.
     *
     * @param evaluationId Id of the Offline Evaluation to delete.
     * @return the {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if the evaluationId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEvaluationWithResponse(String evaluationId) {
        try {
            return withContext(context -> deleteEvaluationWithResponse(evaluationId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get properties of the Personalizer logs.
     *
     * @return properties of the Personalizer logs on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerLogProperties> getLogsProperties() {
        return getLogsPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get properties of the Personalizer logs.
     *
     * @return properties of the Personalizer logs along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerLogProperties>> getLogsPropertiesWithResponse() {
        try {
            return withContext(context -> getLogsPropertiesWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PersonalizerLogProperties>> getLogsPropertiesWithResponse(Context context) {
        return service.getLogs().getPropertiesWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Delete all logs of Rank and Reward calls stored by Personalizer.
     *
     * @return the completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteLogs() {
        return deleteLogsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Delete all logs of Rank and Reward calls stored by Personalizer.
     *
     * @return the {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteLogsWithResponse() {
        try {
            return withContext(context -> deleteLogsWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteLogsWithResponse(Context context) {
        return service.getLogs().deleteWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Update the Personalizer service serviceProperties.
     *
     * @param serviceProperties The personalizer service serviceProperties.
     * @return the serviceProperties of the service with the completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if the serviceProperties is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerServiceProperties> updateProperties(PersonalizerServiceProperties serviceProperties) {
        return updatePropertiesWithResponse(serviceProperties).flatMap(FluxUtil::toMono);
    }

    /**
     * Update the Personalizer service serviceProperties.
     *
     * @param serviceProperties The personalizer service serviceProperties.
     * @return the serviceProperties of the service along with {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if the serviceProperties is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerServiceProperties>> updatePropertiesWithResponse(PersonalizerServiceProperties serviceProperties) {
        try {
            return withContext(context -> updatePropertiesWithResponse(serviceProperties, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Update the Personalizer service serviceProperties.
     *
     * @param serviceProperties The personalizer service serviceProperties.
     * @param context The context to associate with this operation.
     * @return the serviceProperties of the service along with {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if the serviceProperties is null.
     */
    Mono<Response<PersonalizerServiceProperties>> updatePropertiesWithResponse(PersonalizerServiceProperties serviceProperties, Context context) {
        if (serviceProperties == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'serviceProperties' is required and cannot be null"));
        }
        return service.getServiceConfigurations().updateWithResponseAsync(serviceProperties, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Get the current properties of the personalizer service.
     *
     * @return The properties of the personalizer service on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerServiceProperties> getServiceProperties() {
        return getServicePropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get the current properties of the personalizer service.
     *
     * @return The properties of the personalizer service along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerServiceProperties>> getServicePropertiesWithResponse() {
        try {
            return withContext(context -> getServicePropertiesWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get the current properties of the personalizer service.
     *
     * @param context The context to associate with this operation.
     * @return The properties of the personalizer service along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    Mono<Response<PersonalizerServiceProperties>> getServicePropertiesWithResponse(Context context) {
        return service.getServiceConfigurations().getWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Apply Learning Settings and model from a pre-existing Offline Evaluation, making them the current online Learning
     * Settings and model and replacing the previous ones.
     *
     * @param evaluationId EvaluationId of the evaluation.
     * @param policyName PolicyName of the policy within the evaluation.
     * @return the completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if evaluationId or policyName are null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> applyEvaluation(String evaluationId, String policyName) {
        return applyEvaluationWithResponse(evaluationId, policyName).flatMap(FluxUtil::toMono);
    }

    /**
     * Apply Learning Settings and model from a pre-existing Offline Evaluation, making them the current online Learning
     * Settings and model and replacing the previous ones.
     *
     * @param evaluationId EvaluationId of the evaluation.
     * @param policyName PolicyName of the policy within the evaluation.
     * @return the {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if evaluationId or policyName are null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> applyEvaluationWithResponse(String evaluationId, String policyName) {
        try {
            return withContext(context -> applyEvaluationWithResponse(evaluationId, policyName, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> applyEvaluationWithResponse(String evaluationId, String policyName, Context context) {
        if (CoreUtils.isNullOrEmpty(evaluationId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'evaluationId' is required and cannot be null or empty."));
        }
        if (CoreUtils.isNullOrEmpty(policyName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'policyName' is required and cannot be null or empty."));
        }

        PersonalizerPolicyReferenceOptions options = new PersonalizerPolicyReferenceOptions()
            .setEvaluationId(evaluationId)
            .setPolicyName(policyName);
        return service.getServiceConfigurations().applyFromEvaluationWithResponseAsync(options, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Get properties of the model file generated by Personalizer service.
     *
     * @return properties of the model file generated by Personalizer service on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerModelProperties> getModelProperties() {
        return getModelPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get properties of the model file generated by Personalizer service.
     *
     * @return properties of the model file generated by Personalizer service along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerModelProperties>> getModelPropertiesWithResponse() {
        try {
            return withContext(context -> getModelPropertiesWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get properties of the model file generated by Personalizer service.
     *
     * @param context The context to associate with this operation.
     * @return properties of the model file generated by Personalizer service along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    Mono<Response<PersonalizerModelProperties>> getModelPropertiesWithResponse(Context context) {
        return service.getModels().getPropertiesWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Get the Learning Settings currently used by the Personalizer service.
     *
     * @return the Learning Settings currently used by the Personalizer service on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerPolicy> getPolicy() {
        return getPolicyWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get the Learning Settings currently used by the Personalizer service.
     *
     * @return the Learning Settings currently used by the Personalizer service along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerPolicy>> getPolicyWithResponse() {
        try {
            return withContext(context -> getPolicyWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get the Learning Settings currently used by the Personalizer service.
     *
     * @param context The context to associate with this operation.
     * @return the Learning Settings currently used by the Personalizer service along with {@link Response} on successful completion of {@link Mono}.
     */
    Mono<Response<PersonalizerPolicy>> getPolicyWithResponse(Context context) {
        return service.getPolicies().getWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Update the Learning Settings that the Personalizer service will use to train models.
     *
     * @param policy The learning settings.
     * @return learning settings specifying how to train the model on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if policy is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerPolicy> updatePolicy(PersonalizerPolicy policy) {
        return updatePolicyWithResponse(policy).flatMap(FluxUtil::toMono);
    }

    /**
     * Update the Learning Settings that the Personalizer service will use to train models.
     *
     * @param policy The learning settings.
     * @return learning settings specifying how to train the model along with {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if policy is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerPolicy>> updatePolicyWithResponse(PersonalizerPolicy policy) {
        try {
            return withContext(context -> updatePolicyWithResponse(policy, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PersonalizerPolicy>> updatePolicyWithResponse(PersonalizerPolicy policy, Context context) {
        if (policy == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'policy' is required and cannot be null"));
        }
        return service.getPolicies().updateWithResponseAsync(policy, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Resets the learning settings of the Personalizer service to default.
     *
     * @return the new learning settings on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerPolicy> resetPolicy() {
        return resetPolicyWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Resets the learning settings of the Personalizer service to default.
     *
     * @return the new learning settings along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerPolicy>> resetPolicyWithResponse() {
        try {
            return withContext(context -> resetPolicyWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resets the learning settings of the Personalizer service to default.
     *
     * @param context The context to associate with this operation.
     * @return the new learning settings along with {@link Response} on successful completion of {@link Mono}.
     */
    Mono<Response<PersonalizerPolicy>> resetPolicyWithResponse(Context context) {
        return service.getPolicies().resetWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Get the model file generated by Personalizer service.
     *
     * @param isSigned True if requesting signed model zip archive, false otherwise.
     * @return the model file generated by Personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> exportModelWithResponse(boolean isSigned) {
        try {
            return exportModelWithResponse(isSigned, Context.NONE);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get the model file generated by Personalizer service.
     *
     * @param isSigned True if requesting signed model zip archive, false otherwise.
     * @param context The context to associate with this operation.
     * @return the model file generated by Personalizer service.
     */
    Mono<Response<BinaryData>> exportModelWithResponse(boolean isSigned, Context context) {
        return service.getModels().getWithResponseAsync(isSigned, context);
    }

    /**
     * Resets the model file generated by Personalizer service.
     *
     * @return completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resetModel() {
        return resetModelWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Resets the model file generated by Personalizer service.
     *
     * @return {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resetModelWithResponse() {
        try {
            return withContext(context -> resetModelWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Import the digitally signed model file. The input file is obtained by calling the exportModel api.
     *
     * @param signedModel The signed model file.
     * @return completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> importModel(BinaryData signedModel) {
        return importModelWithResponse(signedModel).flatMap(FluxUtil::toMono);
    }

    /**
     * Import the digitally signed model file. The input file is obtained by calling the exportModel api.
     *
     * @param signedModel The signed model file.
     * @return {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> importModelWithResponse(BinaryData signedModel) {
        try {
            return withContext(context -> importModelWithResponse(signedModel, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> resetModelWithResponse(Context context) {
        return service.getModels().resetWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    Mono<Response<Void>> importModelWithResponse(BinaryData signedModel, Context context) {
        return service.getModels().importMethodWithResponseAsync(signedModel, signedModel.getLength(), context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Get list of evaluations with paging.
     *
     * @return {@link PagedFlux} of {@link PersonalizerEvaluation}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PersonalizerEvaluation> listEvaluations() {
        return new PagedFlux<>(() -> listEvaluationsSinglePageAsync(Context.NONE), null);
    }

    PagedFlux<PersonalizerEvaluation> listEvaluations(Context context) {
        return new PagedFlux<>(() -> listEvaluationsSinglePageAsync(context), null);
    }

    Mono<PagedResponse<PersonalizerEvaluation>> listEvaluationsSinglePageAsync(Context context) {
        // return the service call wrapped in PagedResponseBase
        return service.getEvaluations().listWithResponseAsync(context)
            .map(
                res -> new PagedResponseBase<>(
                    res.getRequest(),
                    res.getStatusCode(),
                    res.getHeaders(),
                    res.getValue(),
                    "",
                    null));
    }

    Mono<Response<PersonalizerEvaluation>> getEvaluationWithResponse(String evaluationId, Context context) {
        if (CoreUtils.isNullOrEmpty(evaluationId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'evaluationId' is required and cannot be null or empty"));
        }
        return service.getEvaluations().getWithResponseAsync(evaluationId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    Mono<Response<Void>> deleteEvaluationWithResponse(String evaluationId, Context context) {
        if (CoreUtils.isNullOrEmpty(evaluationId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'evaluationId' is required and cannot be null or empty"));
        }
        return service.getEvaluations().deleteWithResponseAsync(evaluationId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    Function<PollingContext<CreateEvaluationOperationResult>, Mono<PollResponse<CreateEvaluationOperationResult>>> createEvaluationPollOperation(Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<CreateEvaluationOperationResult> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                String evaluationId = operationResultPollResponse.getValue().getEvaluationId();
                return service.getEvaluations().getAsync(evaluationId, context)
                    .flatMap(evaluationResponse ->
                        processRunEvaluationResponse(evaluationResponse, operationResultPollResponse))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (HttpResponseException ex) {
                return monoError(logger, ex);
            }
        };
    }

    Mono<PollResponse<CreateEvaluationOperationResult>> processRunEvaluationResponse(
        PersonalizerEvaluation getOperationResponse,
        PollResponse<CreateEvaluationOperationResult> evaluationOperationResponse) {
        LongRunningOperationStatus status;
        if (PENDING.equals(getOperationResponse.getStatus()) || NOT_SUBMITTED.equals(getOperationResponse.getStatus())) {
            status = LongRunningOperationStatus.IN_PROGRESS;
        } else if (COMPLETED.equals(getOperationResponse.getStatus()) || OPTIMAL_POLICY_APPLIED.equals(getOperationResponse.getStatus()) || ONLINE_POLICY_RETAINED.equals(getOperationResponse.getStatus())) {
            status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (FAILED.equals(getOperationResponse.getStatus())) {
            throw logger.logExceptionAsError(
                ModelTransforms.toEvaluationFailedException(getOperationResponse.getStatus()));
        } else {
            status = LongRunningOperationStatus.fromString(
                getOperationResponse.getStatus().toString(), true);
        }
        return Mono.just(new PollResponse<>(status,
            evaluationOperationResponse.getValue()));
    }

    Function<PollingContext<CreateEvaluationOperationResult>, Mono<CreateEvaluationOperationResult>> createEvaluationInternal(
        PersonalizerEvaluationOptions evaluationOptions, Context context) {
        return (pollingContext) -> createEvaluationWithResponse(evaluationOptions, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> ModelTransforms.toCreateEvaluationOperationResult(Utility.parseResultId(response.getDeserializedHeaders().getLocation())));
    }

    Mono<ResponseBase<EvaluationsCreateHeaders, PersonalizerEvaluation>> createEvaluationWithResponse(
        PersonalizerEvaluationOptions evaluationOptions, Context context) {
        Objects.requireNonNull(evaluationOptions, "'evaluationOptions' is required and can not be null.");
        return service.getEvaluations().createWithResponseAsync(evaluationOptions, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> response);
    }

    PollerFlux<CreateEvaluationOperationResult, PersonalizerEvaluation> beginCreateEvaluation(
        PersonalizerEvaluationOptions evaluationOptions,
        Context context) {
        return new PollerFlux<CreateEvaluationOperationResult, PersonalizerEvaluation>(
            Duration.ofMinutes(1),
            createEvaluationInternal(evaluationOptions, context),
            createEvaluationPollOperation(context),
            cancelEvaluationOperation(context),
            fetchEvaluationResultOperation(context));
    }

    private BiFunction<PollingContext<CreateEvaluationOperationResult>, PollResponse<CreateEvaluationOperationResult>, Mono<CreateEvaluationOperationResult>> cancelEvaluationOperation(Context context) {
        return (pollingContext, activationResponse) -> {
            String evaluationId = activationResponse.getValue().getEvaluationId();
            return deleteEvaluationWithResponse(evaluationId, context)
                .thenReturn(ModelTransforms.toCreateEvaluationOperationResult(evaluationId));
        };
    }

    Function<PollingContext<CreateEvaluationOperationResult>, Mono<PersonalizerEvaluation>> fetchEvaluationResultOperation(
        Context context) {
        return (pollingContext) -> {
            try {
                final String evaluationId = pollingContext.getLatestResponse().getValue().getEvaluationId();
                return service.getEvaluations().getAsync(evaluationId, context)
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }
}
