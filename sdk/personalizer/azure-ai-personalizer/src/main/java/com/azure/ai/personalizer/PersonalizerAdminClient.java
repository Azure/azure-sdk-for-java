package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.models.*;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.StreamResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

public final class PersonalizerAdminClient {

    private PersonalizerAdminAsyncClient client;

    public PersonalizerAdminClient(PersonalizerAdminAsyncClient client) {
        this.client = client;
    }

    public Response<Evaluation> createEvaluation(EvaluationContract evaluationContract) {
        Mono<ResponseBase<EvaluationsCreateHeaders, Evaluation>> response = client.createEvaluation(evaluationContract);
        return response.block();
    }

    public Response<Evaluation> getEvaluation(String evaluationId) {
        Mono<Response<Evaluation>> response = client.getEvaluation(evaluationId);
        return response.block();
    }

    public void deleteEvaluation(String evaluationId) {
        Mono<Response<Void>> response = client.deleteEvaluation(evaluationId);
        response.block();
    }

    public List<Evaluation> getEvaluations() {
        Mono<Response<List<Evaluation>>> response = client.getEvaluations();
        return response.block().getValue();
    }

    public LogsProperties getLogsProperties() {
        Mono<Response<LogsProperties>> response = client.getLogsProperties();
        return response.block().getValue();
    }

    public void deleteLogs() {
        Mono<Response<Void>> response = client.deleteLogs();
        response.block();
    }

    public ServiceConfiguration updateProperties(ServiceConfiguration configuration) {
        Mono<Response<ServiceConfiguration>> updatedConfiguration = client.updateProperties(configuration);
        return updatedConfiguration.block().getValue();
    }

    public ServiceConfiguration getProperties(ServiceConfiguration configuration) {
        Mono<Response<ServiceConfiguration>> updatedConfiguration = client.getProperties(configuration);
        return updatedConfiguration.block().getValue();
    }

    public void applyEvaluation(PolicyReferenceContract policyReferenceContract) {
        Mono<Response<Void>> updatedConfiguration = client.applyEvaluation(policyReferenceContract);
        updatedConfiguration.block();
    }

    public ModelProperties getModelProperties() {
        Mono<Response<ModelProperties>> response = client.getModelProperties();
        return response.block().getValue();
    }

    public PolicyContract getPolicy() {
        Mono<Response<PolicyContract>> response = client.getPolicy();
        return response.block().getValue();
    }

    public PolicyContract updatePolicy(PolicyContract policy) {
        Mono<Response<PolicyContract>> response = client.updatePolicy(policy);
        return response.block().getValue();
    }

    public PolicyContract resetPolicy() {
        Mono<Response<PolicyContract>> response = client.resetPolicy();
        return response.block().getValue();
    }

    public StreamResponse exportModel(boolean isSigned) {
        Mono<StreamResponse> response = client.exportModel(isSigned);
        return response.block();
    }

    public void resetModel() {
        Mono<Response<Void>> response = client.resetModel();
        response.block();
    }

    public void importModel(Flux<ByteBuffer> body, long contentLength) {
        Mono<Response<Void>> response = client.importModel(body, contentLength);
        response.block();
    }
}
