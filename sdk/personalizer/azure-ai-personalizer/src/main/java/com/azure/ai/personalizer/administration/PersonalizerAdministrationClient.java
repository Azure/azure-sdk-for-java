// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.administration;

import com.azure.ai.personalizer.administration.models.CreateEvaluationOperationResult;
import com.azure.ai.personalizer.administration.models.PersonalizerEvaluation;
import com.azure.ai.personalizer.administration.models.PersonalizerEvaluationOptions;
import com.azure.ai.personalizer.administration.models.PersonalizerLogProperties;
import com.azure.ai.personalizer.administration.models.PersonalizerModelProperties;
import com.azure.ai.personalizer.administration.models.PersonalizerPolicy;
import com.azure.ai.personalizer.administration.models.PersonalizerServiceProperties;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Mono;

/**
 * This class provides a synchronous client that contains the operations that apply to Azure Personalizer.
 * Operations allowed by the client are viewing and editing the properties, policy, model, running evaluations.
 *
 * <p><strong>Instantiating a synchronous Personalizer Admin Client</strong></p>
 * <!-- src_embed com.azure.ai.personalizer.PersonalizerAdministrationClient.instantiation -->
 * <pre>
 * PersonalizerAdministrationClient adminClient = new PersonalizerAdministrationClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.personalizer.PersonalizerAdministrationClient.instantiation -->
 *
 * @see PersonalizerAdministrationClientBuilder
 */
@ServiceClient(builder = PersonalizerAdministrationClientBuilder.class, isAsync = false)
public final class PersonalizerAdministrationClient {

    private final PersonalizerAdministrationAsyncClient client;

    PersonalizerAdministrationClient(PersonalizerAdministrationAsyncClient client) {
        this.client = client;
    }

    /**
     * Submit a new Offline Evaluation job.
     *
     * @param evaluationOptions The Offline Evaluation job definition.
     * @return a {@link SyncPoller} that will return counterfactual evaluation when polled.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<CreateEvaluationOperationResult, PersonalizerEvaluation> beginCreateEvaluation(PersonalizerEvaluationOptions evaluationOptions) {
        return beginCreateEvaluation(evaluationOptions, Context.NONE);
    }

    /**
     * Submit a new Offline Evaluation job.
     *
     * @param evaluationOptions The Offline Evaluation job definition.
     * @param context The context to associate with this operation.
     * @return a {@link SyncPoller} that will return counterfactual evaluation when polled.
     * @throws NullPointerException thrown if evaluationOptions is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<CreateEvaluationOperationResult, PersonalizerEvaluation> beginCreateEvaluation(PersonalizerEvaluationOptions evaluationOptions, Context context) {
        return client
            .beginCreateEvaluation(evaluationOptions, context)
            .getSyncPoller();
    }

    /**
     * Get the Offline Evaluation associated with the Id.
     *
     * @param evaluationId Id of the Offline Evaluation.
     * @return the Offline Evaluation associated with the Id.
     * @throws IllegalArgumentException thrown if the evaluationId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerEvaluation getEvaluation(String evaluationId) {
        return getEvaluationWithResponse(evaluationId, Context.NONE).getValue();
    }

    /**
     * Get the Offline Evaluation associated with the Id.
     *
     * @param evaluationId Id of the Offline Evaluation.
     * @param context The context to associate with this operation.
     * @return the Offline Evaluation associated with the Id along with {@link Response}.
     * @throws IllegalArgumentException thrown if the evaluationId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerEvaluation> getEvaluationWithResponse(String evaluationId, Context context) {
        return client.getEvaluationWithResponse(evaluationId, context).block();
    }

    /**
     * Delete the Offline Evaluation associated with the Id.
     *
     * @param evaluationId Id of the Offline Evaluation to delete.
     * @throws IllegalArgumentException thrown if the evaluationId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEvaluation(String evaluationId) {
        deleteEvaluationWithResponse(evaluationId, Context.NONE).getValue();
    }

    /**
     * Delete the Offline Evaluation associated with the Id.
     *
     * @param evaluationId Id of the Offline Evaluation to delete.
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     * @throws IllegalArgumentException thrown if the evaluationId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteEvaluationWithResponse(String evaluationId, Context context) {
        return client.deleteEvaluationWithResponse(evaluationId, context).block();
    }

    /**
     * List of Offline Evaluations with paging.
     *
     * @return {@link PagedIterable} of {@link PersonalizerEvaluation}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PersonalizerEvaluation> listEvaluations() {
        return listEvaluations(Context.NONE);
    }

    /**
     * List of Offline Evaluations with paging.
     *
     * @param context The context to associate with this operation.
     * @return {@link PagedIterable} of {@link PersonalizerEvaluation}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PersonalizerEvaluation> listEvaluations(Context context) {
        return new PagedIterable<>(client.listEvaluations(context));
    }

    /**
     * Get properties of the Personalizer logs.
     *
     * @return properties of the Personalizer logs.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerLogProperties getLogsProperties() {
        return getLogsPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Get properties of the Personalizer logs.
     *
     * @param context The context to associate with this operation.
     * @return properties of the Personalizer logs along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerLogProperties> getLogsPropertiesWithResponse(Context context) {
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
     *
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteLogsWithResponse(Context context) {
        return client.deleteLogsWithResponse(context).block();
    }

    /**
     * Update the Personalizer service serviceProperties.
     *
     * @param serviceProperties The personalizer service serviceProperties.
     * @return the serviceProperties of the service.
     * @throws IllegalArgumentException thrown if the serviceProperties is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerServiceProperties updateProperties(PersonalizerServiceProperties serviceProperties) {
        return updatePropertiesWithResponse(serviceProperties, Context.NONE).getValue();
    }

    /**
     * Update the Personalizer service serviceProperties.
     *
     * @param serviceProperties The personalizer service serviceProperties.
     * @param context The context to associate with this operation.
     * @return the serviceProperties of the service along with {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if the serviceProperties is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerServiceProperties> updatePropertiesWithResponse(PersonalizerServiceProperties serviceProperties, Context context) {
        return client.updatePropertiesWithResponse(serviceProperties, context).block();
    }

    /**
     * Get the current properties of the personalizer service.
     *
     * @return The properties of the personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerServiceProperties getServiceProperties() {
        return getServicePropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Get the current properties of the personalizer service.
     *
     * @param context The context to associate with this operation.
     * @return The properties of the personalizer service along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerServiceProperties> getServicePropertiesWithResponse(Context context) {
        return client.getServicePropertiesWithResponse(context).block();
    }


    /**
     * Apply Learning Settings and model from a pre-existing Offline Evaluation, making them the current online Learning
     * Settings and model and replacing the previous ones.
     *
     * @param evaluationId EvaluationId of the evaluation.
     * @param policyName PolicyName of the policy within the evaluation.
     * @throws IllegalArgumentException thrown if evaluationId or policyName are null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void applyEvaluation(String evaluationId, String policyName) {
        applyEvaluationWithResponse(evaluationId, policyName, Context.NONE).getValue();
    }

    /**
     * Apply Learning Settings and model from a pre-existing Offline Evaluation, making them the current online Learning
     * Settings and model and replacing the previous ones.
     *
     * @param evaluationId EvaluationId of the evaluation.
     * @param policyName PolicyName of the policy within the evaluation.
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     * @throws IllegalArgumentException thrown if evaluationId or policyName are null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> applyEvaluationWithResponse(String evaluationId, String policyName, Context context) {
        return client.applyEvaluationWithResponse(evaluationId, policyName, context).block();
    }

    /**
     * Get properties of the model file generated by Personalizer service.
     *
     * @return properties of the model file generated by Personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerModelProperties getModelProperties() {
        return getModelPropertiesWithResponse(Context.NONE).getValue();
    }

    /**
     * Get properties of the model file generated by Personalizer service.
     *
     * @param context The context to associate with this operation.
     * @return properties of the model file generated by Personalizer service along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerModelProperties> getModelPropertiesWithResponse(Context context) {
        return client.getModelPropertiesWithResponse(context).block();
    }

    /**
     * Get the Learning Settings currently used by the Personalizer service.
     *
     * @return the Learning Settings currently used by the Personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerPolicy getPolicy() {
        return getPolicyWithResponse(Context.NONE).getValue();
    }

    /**
     * Get the Learning Settings currently used by the Personalizer service.
     *
     * @param context The context to associate with this operation.
     * @return the Learning Settings currently used by the Personalizer service along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerPolicy> getPolicyWithResponse(Context context) {
        return client.getPolicyWithResponse(context).block();
    }

    /**
     * Update the Learning Settings that the Personalizer service will use to train models.
     *
     * @param policy The learning settings.
     * @return learning settings specifying how to train the model on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if the policy is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerPolicy updatePolicy(PersonalizerPolicy policy) {
        return updatePolicyWithResponse(policy, Context.NONE).getValue();
    }

    /**
     * Update the Learning Settings that the Personalizer service will use to train models.
     *
     * @param policy The learning settings.
     * @param context The context to associate with this operation.
     * @return learning settings specifying how to train the model along with {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException thrown if the policy is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerPolicy> updatePolicyWithResponse(PersonalizerPolicy policy, Context context) {
        return client.updatePolicyWithResponse(policy, context).block();
    }

    /**
     * Resets the learning settings of the Personalizer service to default.
     *
     * @return the new learning settings on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerPolicy resetPolicy() {
        return resetPolicyWithResponse(Context.NONE).getValue();
    }

    /**
     * Resets the learning settings of the Personalizer service to default.
     *
     * @param context The context to associate with this operation.
     * @return the new learning settings along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerPolicy> resetPolicyWithResponse(Context context) {
        return client.resetPolicyWithResponse(context).block();
    }

    /**
     * Get the model file generated by Personalizer service.
     *
     * @param isSigned True if requesting signed model zip archive, false otherwise.
     * @return the model file generated by Personalizer service.
     */

    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData exportModel(boolean isSigned) {
        return exportModelWithResponse(isSigned, Context.NONE).getValue();
    }

    /**
     * Get the model file generated by Personalizer service.
     *
     * @param isSigned True if requesting signed model zip archive, false otherwise.
     * @param context The context to associate with this operation.
     * @return the model file generated by Personalizer service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> exportModelWithResponse(boolean isSigned, Context context) {
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
     *
     * @param context The context to associate with this operation.
     * @return {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resetModelWithResponse(Context context) {
        return client.resetModelWithResponse(context).block();
    }

    /**
     * Import the digitally signed model file. The input file is obtained by calling the exportModel api.
     *
     * @param signedModel The signed model file.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void importModel(BinaryData signedModel) {
        importModelWithResponse(signedModel, Context.NONE).getValue();
    }

    /**
     * Import the digitally signed model file. The input file is obtained by calling the exportModel api.
     *
     * @param signedModel The signed model file.
     * @param context The context to associate with this operation.
     * @return {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> importModelWithResponse(BinaryData signedModel, Context context) {
        return client.importModelWithResponse(signedModel, context).block();
    }
}
