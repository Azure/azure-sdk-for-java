// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.models.*;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.Context;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.List;

public final class PersonalizerAdminClient {

    private PersonalizerAdminAsyncClient client;

    public PersonalizerAdminClient(PersonalizerAdminAsyncClient client) {
        this.client = client;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Evaluation createEvaluation(EvaluationContract evaluationContract) {
        return createEvaluationWithResponse(evaluationContract, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Evaluation> createEvaluationWithResponse(EvaluationContract evaluationContract, Context context) {
        return client.createEvaluationWithResponse(evaluationContract, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Evaluation getEvaluation(String evaluationId) {
        return getEvaluationWithResponse(evaluationId, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Evaluation> getEvaluationWithResponse(String evaluationId, Context context) {
        return client.getEvaluationWithResponse(evaluationId, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteEvaluation(String evaluationId) {
        deleteEvaluationWithResponse(evaluationId, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteEvaluationWithResponse(String evaluationId, Context context) {
        return client.deleteEvaluationWithResponse(evaluationId, context).block();
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public List<Evaluation> getEvaluations() {
        return getEvaluationsWithResponse(Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Response<List<Evaluation>> getEvaluationsWithResponse(Context context) {
        return client.getEvaluationsWithResponse(context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsProperties getLogsProperties() {
        return getLogsPropertiesWithResponse(Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsProperties> getLogsPropertiesWithResponse(Context context) {
        return client.getLogsPropertiesWithResponse(context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteLogs() {
        deleteLogsWithResponse(Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteLogsWithResponse(Context context) {
        return client.deleteLogsWithResponse(context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceConfiguration updateProperties(ServiceConfiguration configuration) {
        return updatePropertiesWithResponse(configuration, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ServiceConfiguration> updatePropertiesWithResponse(ServiceConfiguration configuration, Context context) {
        return client.updatePropertiesWithResponse(configuration, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceConfiguration getProperties() {
        return getPropertiesWithResponse(Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ServiceConfiguration> getPropertiesWithResponse(Context context) {
        return client.getPropertiesWithResponse(context).block();
    }


    @ServiceMethod(returns = ReturnType.SINGLE)
    public void applyEvaluation(PolicyReferenceContract policyReferenceContract) {
        applyEvaluationWithResponse(policyReferenceContract, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> applyEvaluationWithResponse(PolicyReferenceContract policyReferenceContract, Context context) {
        return client.applyEvaluationWithResponse(policyReferenceContract, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public ModelProperties getModelProperties() {
        return getModelPropertiesWithResponse(Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ModelProperties> getModelPropertiesWithResponse(Context context) {
        return client.getModelPropertiesWithResponse(context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyContract getPolicy() {
        return getPolicyWithResponse(Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PolicyContract> getPolicyWithResponse(Context context) {
        return client.getPolicyWithResponse(context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyContract updatePolicy(PolicyContract policy) {
        return updatePolicyWithResponse(policy, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PolicyContract> updatePolicyWithResponse(PolicyContract policy, Context context) {
        return client.updatePolicyWithResponse(policy, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public PolicyContract resetPolicy() {
        return resetPolicyWithResponse(Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PolicyContract> resetPolicyWithResponse(Context context) {
        return client.resetPolicyWithResponse(context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Flux<ByteBuffer> exportModel(boolean isSigned) {
        return exportModelWithResponse(isSigned, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public StreamResponse exportModelWithResponse(boolean isSigned, Context context) {
        return client.exportModelWithResponse(isSigned, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public void resetModel() {
        resetModelWithResponse(Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resetModelWithResponse(Context context) {
        return client.resetModelWithResponse(context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public void importModel(Flux<ByteBuffer> body, long contentLength) {
        importModelWithResponse(body, contentLength, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> importModelWithResponse(Flux<ByteBuffer> body, long contentLength, Context context) {
        return client.importModelWithResponse(body, contentLength, context).block();
    }
}
