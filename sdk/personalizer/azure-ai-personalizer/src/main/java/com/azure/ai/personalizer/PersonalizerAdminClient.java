// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.Evaluation;
import com.azure.ai.personalizer.models.EvaluationContract;
import com.azure.ai.personalizer.models.LogsProperties;
import com.azure.ai.personalizer.models.ModelProperties;
import com.azure.ai.personalizer.models.PolicyContract;
import com.azure.ai.personalizer.models.PolicyReferenceContract;
import com.azure.ai.personalizer.models.ServiceConfiguration;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

import java.util.List;

/**
 * Client to perform administrative operations on Personalizer instance in a synchronous manner.
 */
public final class PersonalizerAdminClient {

    private final PersonalizerAdminAsyncClient client;

    PersonalizerAdminClient(PersonalizerAdminAsyncClient client) {
        this.client = client;
    }

    /**
     * Submit a new Offline Evaluation job.
     * @param evaluationContract The Offline Evaluation job definition.
     * @return a counterfactual evaluation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Evaluation createEvaluation(EvaluationContract evaluationContract) {
        return createEvaluationWithResponse(evaluationContract, Context.NONE).getValue();
    }

    /**
     * Submit a new Offline Evaluation job.
     * @param evaluationContract The Offline Evaluation job definition.
     * @param context The context to associate with this operation.
     * @return a counterfactual evaluation along with {@link ResponseBase}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Evaluation> createEvaluationWithResponse(EvaluationContract evaluationContract, Context context) {
        return client.createEvaluationWithResponse(evaluationContract, context).block();
    }

    /**
     * Get the Offline Evaluation associated with the Id.
     * @param evaluationId Id of the Offline Evaluation.
     * @return the Offline Evaluation associated with the Id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Evaluation getEvaluation(String evaluationId) {
        return getEvaluationWithResponse(evaluationId, Context.NONE).getValue();
    }

    /**
     * Get the Offline Evaluation associated with the Id.
     * @param evaluationId Id of the Offline Evaluation.
     * @param context The context to associate with this operation.
     * @return the Offline Evaluation associated with the Id along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Evaluation> getEvaluationWithResponse(String evaluationId, Context context) {
        return client.getEvaluationWithResponse(evaluationId, context).block();
    }

    /**
     * Delete the Offline Evaluation associated with the Id.
     * @param evaluationId Id of the Offline Evaluation to delete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEvaluation(String evaluationId) {
        deleteEvaluationWithResponse(evaluationId, Context.NONE).getValue();
    }

    /**
     * Delete the Offline Evaluation associated with the Id.
     * @param evaluationId Id of the Offline Evaluation to delete.
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteEvaluationWithResponse(String evaluationId, Context context) {
        return client.deleteEvaluationWithResponse(evaluationId, context).block();
    }

    /**
     * List of all Offline Evaluations.
     * @return List Evaluations.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public List<Evaluation> getEvaluations() {
        return getEvaluationsWithResponse(Context.NONE).getValue();
    }

    /**
     * List of all Offline Evaluations.
     * @param context The context to associate with this operation.
     * @return List Evaluations along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Response<List<Evaluation>> getEvaluationsWithResponse(Context context) {
        return client.getEvaluationsWithResponse(context).block();
    }

    /**
     * Get properties of the Personalizer logs.
     * @return properties of the Personalizer logs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsProperties getLogsProperties() {
        return getLogsPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Get properties of the Personalizer logs.
     * @param context The context to associate with this operation.
     * @return properties of the Personalizer logs along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsProperties> getLogsPropertiesWithResponse(Context context) {
        return client.getLogsPropertiesWithResponse(context).block();
    }

    /**
     * Delete all logs of Rank and Reward calls stored by Personalizer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteLogs() {
        deleteLogsWithResponse(Context.NONE).getValue();
    }

    /**
     * Delete all logs of Rank and Reward calls stored by Personalizer.
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteLogsWithResponse(Context context) {
        return client.deleteLogsWithResponse(context).block();
    }

    /**
     * Update the Personalizer service configuration.
     * @param configuration The personalizer service configuration.
     * @return the configuration of the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceConfiguration updateProperties(ServiceConfiguration configuration) {
        return updatePropertiesWithResponse(configuration, Context.NONE).getValue();
    }

    /**
     * Update the Personalizer service configuration.
     * @param configuration The personalizer service configuration.
     * @param context The context to associate with this operation.
     * @return the configuration of the service along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ServiceConfiguration> updatePropertiesWithResponse(ServiceConfiguration configuration, Context context) {
        return client.updatePropertiesWithResponse(configuration, context).block();
    }

    /**
     * Get the current properties of the personalizer service.
     * @return The properties of the personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceConfiguration getProperties() {
        return getPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Get the current properties of the personalizer service.
     * @param context The context to associate with this operation.
     * @return The properties of the personalizer service along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ServiceConfiguration> getPropertiesWithResponse(Context context) {
        return client.getPropertiesWithResponse(context).block();
    }


    /**
     * Apply Learning Settings and model from a pre-existing Offline Evaluation, making them the current online Learning
     * Settings and model and replacing the previous ones.
     * @param policyReferenceContract Reference to the policy within the evaluation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void applyEvaluation(PolicyReferenceContract policyReferenceContract) {
        applyEvaluationWithResponse(policyReferenceContract, Context.NONE).getValue();
    }

    /**
     * Apply Learning Settings and model from a pre-existing Offline Evaluation, making them the current online Learning
     * Settings and model and replacing the previous ones.
     * @param policyReferenceContract Reference to the policy within the evaluation.
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> applyEvaluationWithResponse(PolicyReferenceContract policyReferenceContract, Context context) {
        return client.applyEvaluationWithResponse(policyReferenceContract, context).block();
    }

    /**
     * Get properties of the model file generated by Personalizer service.
     * @return properties of the model file generated by Personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ModelProperties getModelProperties() {
        return getModelPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Get properties of the model file generated by Personalizer service.
     * @param context The context to associate with this operation.
     * @return properties of the model file generated by Personalizer service along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ModelProperties> getModelPropertiesWithResponse(Context context) {
        return client.getModelPropertiesWithResponse(context).block();
    }

    /**
     * Get the Learning Settings currently used by the Personalizer service.
     * @return the Learning Settings currently used by the Personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyContract getPolicy() {
        return getPolicyWithResponse(Context.NONE).getValue();
    }

    /**
     * Get the Learning Settings currently used by the Personalizer service.
     * @param context The context to associate with this operation.
     * @return the Learning Settings currently used by the Personalizer service along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PolicyContract> getPolicyWithResponse(Context context) {
        return client.getPolicyWithResponse(context).block();
    }

    /**
     * Update the Learning Settings that the Personalizer service will use to train models.
     * @param policy The learning settings.
     * @return learning settings specifying how to train the model on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyContract updatePolicy(PolicyContract policy) {
        return updatePolicyWithResponse(policy, Context.NONE).getValue();
    }

    /**
     * Update the Learning Settings that the Personalizer service will use to train models.
     * @param policy The learning settings.
     * @param context The context to associate with this operation.
     * @return learning settings specifying how to train the model along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PolicyContract> updatePolicyWithResponse(PolicyContract policy, Context context) {
        return client.updatePolicyWithResponse(policy, context).block();
    }

    /**
     * Resets the learning settings of the Personalizer service to default.
     * @return the new learning settings on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyContract resetPolicy() {
        return resetPolicyWithResponse(Context.NONE).getValue();
    }

    /**
     * Resets the learning settings of the Personalizer service to default.
     * @param context The context to associate with this operation.
     * @return the new learning settings along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PolicyContract> resetPolicyWithResponse(Context context) {
        return client.resetPolicyWithResponse(context).block();
    }

    /**
     * Get the model file generated by Personalizer service.
     * @param isSigned True if requesting signed model zip archive, false otherwise.
     * @return the model file generated by Personalizer service.
     */

    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData exportModel(boolean isSigned) {
        return exportModelWithResponse(isSigned, Context.NONE);
    }

    /**
     * Get the model file generated by Personalizer service.
     * @param isSigned True if requesting signed model zip archive, false otherwise.
     * @param context The context to associate with this operation.
     * @return the model file generated by Personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData exportModelWithResponse(boolean isSigned, Context context) {
        return client.exportModelWithResponse(isSigned, context).block();
    }

    /**
     * Resets the model file generated by Personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void resetModel() {
        resetModelWithResponse(Context.NONE).getValue();
    }

    /**
     * Resets the model file generated by Personalizer service.
     * @param context The context to associate with this operation.
     * @return {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resetModelWithResponse(Context context) {
        return client.resetModelWithResponse(context).block();
    }

    /**
     * Import the digitally signed model file. The input file is obtained by calling the exportModel api.
     * @param signedModel The signed model file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void importModel(BinaryData signedModel) {
        importModelWithResponse(signedModel, Context.NONE).getValue();
    }

    /**
     * Import the digitally signed model file. The input file is obtained by calling the exportModel api.
     * @param signedModel The signed model file.
     * @param context The context to associate with this operation.
     * @return {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> importModelWithResponse(BinaryData signedModel, Context context) {
        return client.importModelWithResponse(signedModel, context).block();
    }
}
