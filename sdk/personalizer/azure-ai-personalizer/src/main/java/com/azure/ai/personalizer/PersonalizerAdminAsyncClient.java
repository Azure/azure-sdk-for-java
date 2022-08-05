// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3Impl;
import com.azure.ai.personalizer.implementation.util.Transforms;
import com.azure.ai.personalizer.models.PersonalizerEvaluation;
import com.azure.ai.personalizer.models.PersonalizerEvaluationOptions;
import com.azure.ai.personalizer.models.PersonalizerLogProperties;
import com.azure.ai.personalizer.models.PersonalizerModelProperties;
import com.azure.ai.personalizer.models.PersonalizerPolicy;
import com.azure.ai.personalizer.models.PersonalizerPolicyReferenceOptions;
import com.azure.ai.personalizer.models.PersonalizerServiceProperties;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Client to perform administrative operations on Personalizer instance in an asynchronous manner.
 */
public final class PersonalizerAdminAsyncClient {

    private final PersonalizerClientV1Preview3Impl service;
    private final ClientLogger logger = new ClientLogger(PersonalizerAdminAsyncClient.class);

    PersonalizerAdminAsyncClient(PersonalizerClientV1Preview3Impl service) {
        this.service = service;
    }

    /**
     * Submit a new Offline Evaluation job.
     * @param evaluationOptions The Offline Evaluation job definition.
     * @return a counterfactual evaluation on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerEvaluation> createEvaluation(PersonalizerEvaluationOptions evaluationOptions) {
        return createEvaluationWithResponse(evaluationOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Submit a new Offline Evaluation job.
     * @param evaluationOptions The Offline Evaluation job definition.
     * @return a counterfactual evaluation along with {@link ResponseBase} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerEvaluation>> createEvaluationWithResponse(PersonalizerEvaluationOptions evaluationOptions) {
        try {
            return withContext(context -> createEvaluationWithResponse(evaluationOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Submit a new Offline Evaluation job.
     * @param evaluationOptions The Offline Evaluation job definition.
     * @param context The context to associate with this operation.
     * @return a counterfactual evaluation along with {@link ResponseBase} on successful completion of {@link Mono}.
     */
    Mono<Response<PersonalizerEvaluation>> createEvaluationWithResponse(PersonalizerEvaluationOptions evaluationOptions, Context context) {
        Objects.requireNonNull(evaluationOptions, "'evaluationContract' is required and can not be null.");
        return service.getEvaluations().createWithResponseAsync(evaluationOptions, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Get the Offline Evaluation associated with the Id.
     * @param evaluationId Id of the Offline Evaluation.
     * @return the Offline Evaluation associated with the Id on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerEvaluation> getEvaluation(String evaluationId) {
        return getEvaluationWithResponse(evaluationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get the Offline Evaluation associated with the Id.
     * @param evaluationId Id of the Offline Evaluation.
     * @return the Offline Evaluation associated with the Id along with {@link Response} on successful completion of
     * {@link Mono}.
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
     * Get the Offline Evaluation associated with the Id.
     * @param evaluationId Id of the Offline Evaluation.
     * @param context The context to associate with this operation.
     * @return the Offline Evaluation associated with the Id along with {@link Response} on successful completion of
     * {@link Mono}.
     */
    Mono<Response<PersonalizerEvaluation>> getEvaluationWithResponse(String evaluationId, Context context) {
        if (CoreUtils.isNullOrEmpty(evaluationId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'evaluationId' is required and cannot be null or empty"));
        }
        return service.getEvaluations().getWithResponseAsync(evaluationId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Delete the Offline Evaluation associated with the Id.
     * @param evaluationId Id of the Offline Evaluation to delete.
     * @return the completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEvaluation(String evaluationId) {
        return deleteEvaluationWithResponse(evaluationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Delete the Offline Evaluation associated with the Id.
     * @param evaluationId Id of the Offline Evaluation to delete.
     * @return the {@link Response} on successful completion of {@link Mono}.
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
     * Delete the Offline Evaluation associated with the Id.
     * @param evaluationId Id of the Offline Evaluation to delete.
     * @param context The context to associate with this operation.
     * @return the {@link Response} on successful completion of {@link Mono}.
     */
    Mono<Response<Void>> deleteEvaluationWithResponse(String evaluationId, Context context) {
        if (CoreUtils.isNullOrEmpty(evaluationId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'evaluationId' is required and cannot be null or empty"));
        }
        return service.getEvaluations().deleteWithResponseAsync(evaluationId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * List of all Offline Evaluations.
     * @return List Evaluations on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<List<PersonalizerEvaluation>> getEvaluations() {
        return getEvaluationsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * List of all Offline Evaluations.
     * @return List Evaluations along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Response<List<PersonalizerEvaluation>>> getEvaluationsWithResponse() {
        try {
            return withContext(context -> getEvaluationsWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List of all Offline Evaluations.
     * @param context The context to associate with this operation.
     * @return List Evaluations along with {@link Response} on successful completion of {@link Mono}.
     */
    Mono<Response<List<PersonalizerEvaluation>>> getEvaluationsWithResponse(Context context) {
        return service.getEvaluations().listWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Get properties of the Personalizer logs.
     * @return properties of the Personalizer logs on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerLogProperties> getLogsProperties() {
        return getLogsPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get properties of the Personalizer logs.
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

    /**
     * Get properties of the Personalizer logs.
     * @param context The context to associate with this operation.
     * @return properties of the Personalizer logs along with {@link Response} on successful completion of {@link Mono}.
     */
    Mono<Response<PersonalizerLogProperties>> getLogsPropertiesWithResponse(Context context) {
        return service.getLogs().getPropertiesWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Delete all logs of Rank and Reward calls stored by Personalizer.
     * @return the completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteLogs() {
        return deleteLogsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Delete all logs of Rank and Reward calls stored by Personalizer.
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

    /**
     * Delete all logs of Rank and Reward calls stored by Personalizer.
     * @param context The context to associate with this operation.
     * @return the {@link Response} on successful completion of {@link Mono}.
     */
    Mono<Response<Void>> deleteLogsWithResponse(Context context) {
        return service.getLogs().deleteWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Update the Personalizer service configuration.
     * @param configuration The personalizer service configuration.
     * @return the configuration of the service with the completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerServiceProperties> updateProperties(PersonalizerServiceProperties configuration) {
        return updatePropertiesWithResponse(configuration).flatMap(FluxUtil::toMono);
    }

    /**
     * Update the Personalizer service configuration.
     * @param configuration The personalizer service configuration.
     * @return the configuration of the service along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerServiceProperties>> updatePropertiesWithResponse(PersonalizerServiceProperties configuration) {
        try {
            return withContext(context -> updatePropertiesWithResponse(configuration, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Update the Personalizer service configuration.
     * @param configuration The personalizer service configuration.
     * @param context The context to associate with this operation.
     * @return the configuration of the service along with {@link Response} on successful completion of {@link Mono}.
     */
    Mono<Response<PersonalizerServiceProperties>> updatePropertiesWithResponse(PersonalizerServiceProperties configuration, Context context) {
        if (configuration == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'configuration' is required and cannot be null"));
        }
        return service.getServiceConfigurations().updateWithResponseAsync(configuration, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Get the current properties of the personalizer service.
     * @return The properties of the personalizer service on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerServiceProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get the current properties of the personalizer service.
     * @return The properties of the personalizer service along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerServiceProperties>> getPropertiesWithResponse() {
        try {
            return withContext(context -> getPropertiesWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get the current properties of the personalizer service.
     * @param context The context to associate with this operation.
     * @return The properties of the personalizer service along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    Mono<Response<PersonalizerServiceProperties>> getPropertiesWithResponse(Context context) {
        return service.getServiceConfigurations().getWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Apply Learning Settings and model from a pre-existing Offline Evaluation, making them the current online Learning
     * Settings and model and replacing the previous ones.
     * @param policyReferenceOptions Reference to the policy within the evaluation.
     * @return the completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> applyEvaluation(PersonalizerPolicyReferenceOptions policyReferenceOptions) {
        return applyEvaluationWithResponse(policyReferenceOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Apply Learning Settings and model from a pre-existing Offline Evaluation, making them the current online Learning
     * Settings and model and replacing the previous ones.
     * @param policyReferenceOptions Reference to the policy within the evaluation.
     * @return the {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> applyEvaluationWithResponse(PersonalizerPolicyReferenceOptions policyReferenceOptions) {
        try {
            return withContext(context -> applyEvaluationWithResponse(policyReferenceOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Apply Learning Settings and model from a pre-existing Offline Evaluation, making them the current online Learning
     * Settings and model and replacing the previous ones.
     * @param policyReferenceOptions Reference to the policy within the evaluation.
     * @param context The context to associate with this operation.
     * @return the {@link Response} on successful completion of {@link Mono}.
     */
    Mono<Response<Void>> applyEvaluationWithResponse(PersonalizerPolicyReferenceOptions policyReferenceOptions, Context context) {
        if (policyReferenceOptions == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'policyReferenceContract' is required and cannot be null"));
        }
        return service.getServiceConfigurations().applyFromEvaluationWithResponseAsync(policyReferenceOptions, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Get properties of the model file generated by Personalizer service.
     * @return properties of the model file generated by Personalizer service on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerModelProperties> getModelProperties() {
        return getModelPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get properties of the model file generated by Personalizer service.
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
     * @return the Learning Settings currently used by the Personalizer service on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerPolicy> getPolicy() {
        return getPolicyWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Get the Learning Settings currently used by the Personalizer service.
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
     * @param policy The learning settings.
     * @return learning settings specifying how to train the model on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerPolicy> updatePolicy(PersonalizerPolicy policy) {
        return updatePolicyWithResponse(policy).flatMap(FluxUtil::toMono);
    }

    /**
     * Update the Learning Settings that the Personalizer service will use to train models.
     * @param policy The learning settings.
     * @return learning settings specifying how to train the model along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerPolicy>> updatePolicyWithResponse(PersonalizerPolicy policy) {
        try {
            return withContext(context -> updatePolicyWithResponse(policy, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Update the Learning Settings that the Personalizer service will use to train models.
     * @param policy The learning settings.
     * @param context The context to associate with this operation.
     * @return learning settings specifying how to train the model along with {@link Response} on successful completion of {@link Mono}.
     */
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
     * @return the new learning settings on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerPolicy> resetPolicy() {
        return resetPolicyWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Resets the learning settings of the Personalizer service to default.
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
     * @param isSigned True if requesting signed model zip archive, false otherwise.
     * @return the model file generated by Personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<BinaryData> exportModelWithResponse(boolean isSigned) {
        try {
            return exportModelWithResponse(isSigned, Context.NONE);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get the model file generated by Personalizer service.
     * @param isSigned True if requesting signed model zip archive, false otherwise.
     * @param context The context to associate with this operation.
     * @return the model file generated by Personalizer service.
     */
    Mono<BinaryData> exportModelWithResponse(boolean isSigned, Context context) {
        return BinaryData.fromFlux(service.getModels().getWithResponseAsync(isSigned, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .block().getValue());
    }

    /**
     * Resets the model file generated by Personalizer service.
     * @return completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resetModel() {
        return resetModelWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Resets the model file generated by Personalizer service.
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
     * Resets the model file generated by Personalizer service.
     * @param context The context to associate with this operation.
     * @return {@link Response} on successful completion of {@link Mono}.
     */
    Mono<Response<Void>> resetModelWithResponse(Context context) {
        return service.getModels().resetWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Import the digitally signed model file. The input file is obtained by calling the exportModel api.
     * @param signedModel The signed model file.
     * @return completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> importModel(BinaryData signedModel) {
        return importModelWithResponse(signedModel).flatMap(FluxUtil::toMono);
    }

    /**
     * Import the digitally signed model file. The input file is obtained by calling the exportModel api.
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

    /**
     * Import the digitally signed model file. The input file is obtained by calling the exportModel api.
     * @param signedModel The signed model file.
     * @param context The context to associate with this operation.
     * @return {@link Response} on successful completion of {@link Mono}.
     */
    Mono<Response<Void>> importModelWithResponse(BinaryData signedModel, Context context) {
        return service.getModels().importMethodWithResponseAsync(signedModel, signedModel.getLength(), context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }
}
