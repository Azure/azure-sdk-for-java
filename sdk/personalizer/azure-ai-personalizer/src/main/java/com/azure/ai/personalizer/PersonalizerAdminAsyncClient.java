// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3Impl;
import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3ImplBuilder;
import com.azure.ai.personalizer.implementation.models.Evaluation;
import com.azure.ai.personalizer.implementation.models.EvaluationContract;
import com.azure.ai.personalizer.implementation.models.LogsProperties;
import com.azure.ai.personalizer.implementation.models.ModelProperties;
import com.azure.ai.personalizer.implementation.models.PolicyContract;
import com.azure.ai.personalizer.implementation.models.PolicyReferenceContract;
import com.azure.ai.personalizer.implementation.models.ServiceConfiguration;
import com.azure.ai.personalizer.implementation.util.Transforms;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

public final class PersonalizerAdminAsyncClient {

    private PersonalizerClientV1Preview3Impl service;
    private final ClientLogger logger = new ClientLogger(PersonalizerAdminAsyncClient.class);

    public PersonalizerAdminAsyncClient(PersonalizerClientV1Preview3Impl service) {
    PersonalizerClientV1Preview3ImplBuilder builder = new PersonalizerClientV1Preview3ImplBuilder();
        this.service = service;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Evaluation> createEvaluation(EvaluationContract evaluationContract) {
        return createEvaluationWithResponse(evaluationContract).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Evaluation>> createEvaluationWithResponse(EvaluationContract evaluationContract) {
        try {
            return withContext(context -> createEvaluationWithResponse(evaluationContract, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Evaluation>> createEvaluationWithResponse(EvaluationContract evaluationContract, Context context) {
        if (evaluationContract == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'evaluationContract' is required and cannot be null"));
        }
        return service.getEvaluations().createWithResponseAsync(evaluationContract, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Evaluation> getEvaluation(String evaluationId) {
        return getEvaluationWithResponse(evaluationId).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Evaluation>> getEvaluationWithResponse(String evaluationId) {
        try {
            return withContext(context -> getEvaluationWithResponse(evaluationId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Evaluation>> getEvaluationWithResponse(String evaluationId, Context context) {
        if (CoreUtils.isNullOrEmpty(evaluationId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'evaluationId' is required and cannot be null or empty"));
        }
        return service.getEvaluations().getWithResponseAsync(evaluationId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEvaluation(String evaluationId) {
        return deleteEvaluationWithResponse(evaluationId).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEvaluationWithResponse(String evaluationId) {
        try {
            return withContext(context -> deleteEvaluationWithResponse(evaluationId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteEvaluationWithResponse(String evaluationId, Context context) {
        if (CoreUtils.isNullOrEmpty(evaluationId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'evaluationId' is required and cannot be null or empty"));
        }
        return service.getEvaluations().deleteWithResponseAsync(evaluationId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<List<Evaluation>> getEvaluations() {
        return getEvaluationsWithResponse().flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Response<List<Evaluation>>> getEvaluationsWithResponse() {
        try {
            return withContext(context -> getEvaluationsWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<List<Evaluation>>> getEvaluationsWithResponse(Context context) {
        return service.getEvaluations().listWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LogsProperties> getLogsProperties() {
        return getLogsPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LogsProperties>> getLogsPropertiesWithResponse() {
        try {
            return withContext(context -> getLogsPropertiesWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<LogsProperties>> getLogsPropertiesWithResponse(Context context) {
        return service.getLogs().getPropertiesWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteLogs() {
        return deleteLogsWithResponse().flatMap(FluxUtil::toMono);
    }

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

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ServiceConfiguration> updateProperties(ServiceConfiguration configuration) {
        return updatePropertiesWithResponse(configuration).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ServiceConfiguration>> updatePropertiesWithResponse(ServiceConfiguration configuration) {
        try {
            return withContext(context -> updatePropertiesWithResponse(configuration, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ServiceConfiguration>> updatePropertiesWithResponse(ServiceConfiguration configuration, Context context) {
        if (configuration == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'configuration' is required and cannot be null"));
        }
        return service.getServiceConfigurations().updateWithResponseAsync(configuration, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ServiceConfiguration> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ServiceConfiguration>> getPropertiesWithResponse() {
        try {
            return withContext(context -> getPropertiesWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ServiceConfiguration>> getPropertiesWithResponse(Context context) {
        return service.getServiceConfigurations().getWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> applyEvaluation(PolicyReferenceContract policyReferenceContract) {
        return applyEvaluationWithResponse(policyReferenceContract).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> applyEvaluationWithResponse(PolicyReferenceContract policyReferenceContract) {
        try {
            return withContext(context -> applyEvaluationWithResponse(policyReferenceContract, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> applyEvaluationWithResponse(PolicyReferenceContract policyReferenceContract, Context context) {
        if (policyReferenceContract == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'policyReferenceContract' is required and cannot be null"));
        }
        return service.getServiceConfigurations().applyFromEvaluationWithResponseAsync(policyReferenceContract, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ModelProperties> getModelProperties() {
        return getModelPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ModelProperties>> getModelPropertiesWithResponse() {
        try {
            return withContext(context -> getModelPropertiesWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ModelProperties>> getModelPropertiesWithResponse(Context context) {
        return service.getModels().getPropertiesWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PolicyContract> getPolicy() {
        return getPolicyWithResponse().flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PolicyContract>> getPolicyWithResponse() {
        try {
            return withContext(context -> getPolicyWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PolicyContract>> getPolicyWithResponse(Context context) {
        return service.getPolicies().getWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PolicyContract> updatePolicy(PolicyContract policy) {
        return updatePolicyWithResponse(policy).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PolicyContract>> updatePolicyWithResponse(PolicyContract policy) {
        try {
            return withContext(context -> updatePolicyWithResponse(policy, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PolicyContract>> updatePolicyWithResponse(PolicyContract policy, Context context) {
        if (policy == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'policy' is required and cannot be null"));
        }
        return service.getPolicies().updateWithResponseAsync(policy, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PolicyContract> resetPolicy() {
        return resetPolicyWithResponse().flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PolicyContract>> resetPolicyWithResponse() {
        try {
            return withContext(context -> resetPolicyWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PolicyContract>> resetPolicyWithResponse(Context context) {
        return service.getPolicies().resetWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<StreamResponse> exportModelWithResponse(boolean isSigned) {
        try {
            return withContext(context -> exportModelWithResponse(isSigned, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<StreamResponse> exportModelWithResponse(boolean isSigned, Context context) {
        return service.getModels().getWithResponseAsync(isSigned, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resetModel() {
        return resetModelWithResponse().flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resetModelWithResponse() {
        try {
            return withContext(context -> resetModelWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> resetModelWithResponse(Context context) {
        return service.getModels().resetWithResponseAsync(context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }


    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> importModel(Flux<ByteBuffer> model, long length) {
        return importModelWithResponse(model, length).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> importModelWithResponse(Flux<ByteBuffer> model, long length) {
        try {
            return withContext(context -> importModelWithResponse(model, length, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> importModelWithResponse(Flux<ByteBuffer> model, long length, Context context) {
        return service.getModels().importMethodWithResponseAsync(model, length, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }
}
