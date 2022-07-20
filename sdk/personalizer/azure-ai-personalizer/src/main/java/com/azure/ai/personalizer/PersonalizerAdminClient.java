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

public final class PersonalizerAdminClient {

    private PersonalizerClientV1Preview3Impl impl;

    public PersonalizerAdminClient(String endpoint, AzureKeyCredential keyCredential) {
    PersonalizerClientV1Preview3ImplBuilder builder = new PersonalizerClientV1Preview3ImplBuilder();
    HttpClient httpClient = HttpClient.createDefault();
    impl = builder
        .endpoint(endpoint)
        .httpClient(httpClient)
        .addPolicy(new AzureKeyCredentialPolicy(Constants.OCP_APIM_SUBSCRIPTION_KEY, keyCredential))
        .retryPolicy(new RetryPolicy())
        .httpLogOptions(new HttpLogOptions())
        .buildClient();
    }

    public Evaluation createEvaluation(EvaluationContract evaluationContract) {
        Mono<ResponseBase<EvaluationsCreateHeaders, Evaluation>> response = impl.getEvaluations().createWithResponseAsync(evaluationContract);
        return response.block().getValue();
    }

    public Evaluation getEvaluation(String evaluationId) {
        Mono<Response<Evaluation>> response = impl.getEvaluations().getWithResponseAsync(evaluationId);
        return response.block().getValue();
    }

    public void deleteEvaluation(String evaluationId) {
        Mono<Response<Void>> response = impl.getEvaluations().deleteWithResponseAsync(evaluationId);
        response.block();
    }

    public List<Evaluation> getEvaluations() {
        Mono<Response<List<Evaluation>>> response = impl.getEvaluations().listWithResponseAsync();
        return response.block().getValue();
    }

    public LogsProperties getLogsProperties() {
        Mono<Response<LogsProperties>> response = impl.getLogs().getPropertiesWithResponseAsync();
        return response.block().getValue();
    }

    public void deleteLogs() {
        Mono<Response<Void>> response = impl.getLogs().deleteWithResponseAsync();
        response.block();
    }

    public ServiceConfiguration updateProperties(ServiceConfiguration configuration) {
        Mono<Response<ServiceConfiguration>> updatedConfiguration = impl.getServiceConfigurations().updateWithResponseAsync(configuration);
        return updatedConfiguration.block().getValue();
    }

    public ServiceConfiguration getProperties(ServiceConfiguration configuration) {
        Mono<Response<ServiceConfiguration>> updatedConfiguration = impl.getServiceConfigurations().getWithResponseAsync();
        return updatedConfiguration.block().getValue();
    }

    public void applyEvaluation(PolicyReferenceContract policyReferenceContract) {
        Mono<Response<Void>> updatedConfiguration = impl.getServiceConfigurations().applyFromEvaluationWithResponseAsync(policyReferenceContract);
        updatedConfiguration.block();
    }

    public ModelProperties getModelProperties() {
        Mono<Response<ModelProperties>> response = impl.getModels().getPropertiesWithResponseAsync();
        return response.block().getValue();
    }

    public PolicyContract getPolicy() {
        Mono<Response<PolicyContract>> response = impl.getPolicies().getWithResponseAsync();
        return response.block().getValue();
    }

    public PolicyContract updatePolicy(PolicyContract policy) {
        Mono<Response<PolicyContract>> response = impl.getPolicies().updateWithResponseAsync(policy);
        return response.block().getValue();
    }

    public PolicyContract resetPolicy() {
        Mono<Response<PolicyContract>> response = impl.getPolicies().resetWithResponseAsync();
        return response.block().getValue();
    }

    public StreamResponse exportModel(boolean isSigned) {
        Mono<StreamResponse> response = impl.getModels().getWithResponseAsync(isSigned);
        return response.block();
    }

    public void resetModel() {
        Mono<Response<Void>> response = impl.getModels().resetWithResponseAsync();
        response.block();
    }

    public void importModel(Flux<ByteBuffer> body, long contentLength) {
        Mono<Response<Void>> response = impl.getModels().importMethodWithResponseAsync(body, contentLength);
        response.block();
    }
}
