package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3Impl;
import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3ImplBuilder;
import com.azure.ai.personalizer.implementation.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.StreamResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

public final class PersonalizerAdminAsyncClient {

    private PersonalizerClientV1Preview3Impl service;

    public PersonalizerAdminAsyncClient(PersonalizerClientV1Preview3Impl service) {
    PersonalizerClientV1Preview3ImplBuilder builder = new PersonalizerClientV1Preview3ImplBuilder();
        this.service = service;
    }

    public Mono<ResponseBase<EvaluationsCreateHeaders, Evaluation>> createEvaluation(EvaluationContract evaluationContract) {
        return service.getEvaluations().createWithResponseAsync(evaluationContract);
    }

    public Mono<Response<Evaluation>> getEvaluation(String evaluationId) {
        return service.getEvaluations().getWithResponseAsync(evaluationId);
    }

    public Mono<Response<Void>> deleteEvaluation(String evaluationId) {
        return service.getEvaluations().deleteWithResponseAsync(evaluationId);
    }

    public Mono<Response<List<Evaluation>>> getEvaluations() {
        return service.getEvaluations().listWithResponseAsync();
    }

    public Mono<Response<LogsProperties>> getLogsProperties() {
        return service.getLogs().getPropertiesWithResponseAsync();
    }

    public Mono<Response<Void>> deleteLogs() {
        return service.getLogs().deleteWithResponseAsync();
    }

    public Mono<Response<ServiceConfiguration>> updateProperties(ServiceConfiguration configuration) {
        return service.getServiceConfigurations().updateWithResponseAsync(configuration);
    }

    public Mono<Response<ServiceConfiguration>> getProperties(ServiceConfiguration configuration) {
        return service.getServiceConfigurations().getWithResponseAsync();
    }

    public Mono<Response<Void>> applyEvaluation(PolicyReferenceContract policyReferenceContract) {
        return service.getServiceConfigurations().applyFromEvaluationWithResponseAsync(policyReferenceContract);
    }

    public Mono<Response<ModelProperties>> getModelProperties() {
        return service.getModels().getPropertiesWithResponseAsync();
    }

    public Mono<Response<PolicyContract>> getPolicy() {
        return service.getPolicies().getWithResponseAsync();
    }

    public Mono<Response<PolicyContract>> updatePolicy(PolicyContract policy) {
        return service.getPolicies().updateWithResponseAsync(policy);
    }

    public Mono<Response<PolicyContract>> resetPolicy() {
        return service.getPolicies().resetWithResponseAsync();
    }

    public Mono<StreamResponse> exportModel(boolean isSigned) {
        return service.getModels().getWithResponseAsync(isSigned);
    }

    public Mono<Response<Void>> resetModel() {
        return service.getModels().resetWithResponseAsync();
    }

    public Mono<Response<Void>> importModel(Flux<ByteBuffer> body, long contentLength) {
        return service.getModels().importMethodWithResponseAsync(body, contentLength);
    }
}
