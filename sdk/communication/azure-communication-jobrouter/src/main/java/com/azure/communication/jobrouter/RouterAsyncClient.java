// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.AzureCommunicationRoutingServiceImpl;
import com.azure.communication.jobrouter.implementation.JobRoutersImpl;
import com.azure.communication.jobrouter.implementation.convertors.DistributionPolicyAdapter;
import com.azure.communication.jobrouter.implementation.convertors.QueueAdapter;
import com.azure.communication.jobrouter.implementation.convertors.JobAdapter;
import com.azure.communication.jobrouter.implementation.convertors.WorkerAdapter;
import com.azure.communication.jobrouter.models.AcceptJobOfferResponse;
import com.azure.communication.jobrouter.models.CancelJobResult;
import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.jobrouter.models.CloseJobResult;
import com.azure.communication.jobrouter.models.CompleteJobResult;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DeclineJobOfferResult;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.JobPositionDetails;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.JobStateSelector;
import com.azure.communication.jobrouter.implementation.models.PagedClassificationPolicy;
import com.azure.communication.jobrouter.implementation.models.PagedDistributionPolicy;
import com.azure.communication.jobrouter.implementation.models.PagedExceptionPolicy;
import com.azure.communication.jobrouter.implementation.models.PagedJob;
import com.azure.communication.jobrouter.implementation.models.PagedQueue;
import com.azure.communication.jobrouter.implementation.models.PagedWorker;
import com.azure.communication.jobrouter.models.QueueStatistics;
import com.azure.communication.jobrouter.models.ReclassifyJobResult;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.UpdateQueueOptions;
import com.azure.communication.jobrouter.models.UpdateWorkerOptions;
import com.azure.communication.jobrouter.models.WorkerStateSelector;
import com.azure.communication.jobrouter.implementation.convertors.ClassificationPolicyAdapter;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.implementation.convertors.ExceptionPolicyAdapter;
import com.azure.communication.jobrouter.models.UpdateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateJobOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;


/**
 * Async Client that supports job router operations.
 *
 * <p><strong>Instantiating an asynchronous job router Client</strong></p>
 * <!-- src_embed com.azure.communication.jobrouter.routerasyncclient.instantiation -->
 * <pre>
 * &#47;&#47; Initialize the router client builder
 * final RouterClientBuilder builder = new RouterClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;;
 * &#47;&#47; Build the router client
 * RouterAsyncClient routerAsyncClient = builder.buildAsyncClient&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.communication.jobrouter.routerasyncclient.instantiation -->
 *
 * <p>View {@link RouterClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see RouterClientBuilder
 */
@ServiceClient(builder = RouterClientBuilder.class, isAsync = true)
public final class RouterAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(RouterAsyncClient.class);

    private final JobRoutersImpl jobRouter;

    RouterAsyncClient(AzureCommunicationRoutingServiceImpl jobRouterService) {
        this.jobRouter = jobRouterService.getJobRouters();
    }

    /**
     * Creates a classification policy.
     *
     * @param createClassificationPolicyOptions Container for inputs to create a classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ClassificationPolicy> createClassificationPolicy(CreateClassificationPolicyOptions createClassificationPolicyOptions) {
        try {
            ClassificationPolicy classificationPolicy = ClassificationPolicyAdapter.convertCreateOptionsToClassificationPolicy(createClassificationPolicyOptions);
            return withContext(context -> upsertClassificationPolicyWithResponse(createClassificationPolicyOptions.getId(), classificationPolicy, context)
                .flatMap(
                    (Response<ClassificationPolicy> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a classification policy.
     *
     * @param createClassificationPolicyOptions Container for inputs to create a classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ClassificationPolicy>> createClassificationPolicyWithResponse(CreateClassificationPolicyOptions createClassificationPolicyOptions) {
        try {
            ClassificationPolicy classificationPolicy = ClassificationPolicyAdapter.convertCreateOptionsToClassificationPolicy(createClassificationPolicyOptions);
            return withContext(context -> upsertClassificationPolicyWithResponse(createClassificationPolicyOptions.getId(), classificationPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates a classification policy.
     *
     * @param updateClassificationPolicyOptions Request options to update classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ClassificationPolicy> updateClassificationPolicy(UpdateClassificationPolicyOptions updateClassificationPolicyOptions) {
        try {
            ClassificationPolicy classificationPolicy = ClassificationPolicyAdapter.convertUpdateOptionsToClassificationPolicy(updateClassificationPolicyOptions);
            return withContext(context -> upsertClassificationPolicyWithResponse(updateClassificationPolicyOptions.getId(), classificationPolicy, context)
                .flatMap(
                    (Response<ClassificationPolicy> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates a classification policy.
     *
     * @param updateClassificationPolicyOptions Request options to update classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ClassificationPolicy>> updateClassificationPolicyWithResponse(UpdateClassificationPolicyOptions updateClassificationPolicyOptions) {
        try {
            ClassificationPolicy classificationPolicy = ClassificationPolicyAdapter.convertUpdateOptionsToClassificationPolicy(updateClassificationPolicyOptions);
            return withContext(context -> upsertClassificationPolicyWithResponse(updateClassificationPolicyOptions.getId(), classificationPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ClassificationPolicy>> upsertClassificationPolicyWithResponse(String id, ClassificationPolicy classificationPolicy, Context context) {
        try {
            return jobRouter.upsertClassificationPolicyWithResponseAsync(id, classificationPolicy, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing classification policy by Id.
     *
     * @param id Id of the classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ClassificationPolicy> getClassificationPolicy(String id) {
        try {
            return withContext(context -> getClassificationPolicyWithResponse(id, context)
                .flatMap(
                    (Response<ClassificationPolicy> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing classification policy by Id.
     *
     * @param id Id of the classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ClassificationPolicy>> getClassificationPolicyWithResponse(String id) {
        try {
            return withContext(context -> getClassificationPolicyWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ClassificationPolicy>> getClassificationPolicyWithResponse(String id, Context context) {
        try {
            return jobRouter.getClassificationPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a Classification Policy by Id.
     *
     * @param id Id of the classification policy.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteClassificationPolicy(String id) {
        try {
            return withContext(context -> deleteClassificationPolicyWithResponse(id, context)
                .flatMap(
                    (Response<Void> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a Classification Policy by Id.
     *
     * @param id Id of the classification policy.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteClassificationPolicyWithResponse(String id) {
        try {
            return withContext(context -> deleteClassificationPolicyWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteClassificationPolicyWithResponse(String id, Context context) {
        try {
            return jobRouter.deleteClassificationPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing classification policies.
     *
     * @return a paged collection of classification policies.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedClassificationPolicy> listClassificationPolicies() {
        try {
            return jobRouter.listClassificationPoliciesAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing classification policies.
     *
     * @param maxPageSize Maximum page size.
     * @return a paged collection of classification policies.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedClassificationPolicy> listClassificationPolicies(Integer maxPageSize) {
        try {
            return jobRouter.listClassificationPoliciesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Creates a distribution policy.
     *
     * @param createDistributionPolicyOptions Container for inputs to create a distribution policy.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DistributionPolicy> createDistributionPolicy(CreateDistributionPolicyOptions createDistributionPolicyOptions) {
        try {
            DistributionPolicy distributionPolicy = DistributionPolicyAdapter.convertCreateOptionsToDistributionPolicy(createDistributionPolicyOptions);
            return withContext(context -> upsertDistributionPolicyWithResponse(createDistributionPolicyOptions.getId(), distributionPolicy, context)
                .flatMap(
                    (Response<DistributionPolicy> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a distribution policy.
     *
     * @param createDistributionPolicyOptions Container for inputs to create a distribution policy.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DistributionPolicy>> createDistributionPolicyWithResponse(CreateDistributionPolicyOptions createDistributionPolicyOptions) {
        try {
            DistributionPolicy distributionPolicy = DistributionPolicyAdapter.convertCreateOptionsToDistributionPolicy(createDistributionPolicyOptions);
            return withContext(context -> upsertDistributionPolicyWithResponse(createDistributionPolicyOptions.getId(), distributionPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates a distribution policy.
     *
     * @param updateDistributionPolicyOptions Request options to update distribution policy.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DistributionPolicy> updateDistributionPolicy(UpdateDistributionPolicyOptions updateDistributionPolicyOptions) {
        try {
            DistributionPolicy distributionPolicy = DistributionPolicyAdapter.convertUpdateOptionsToClassificationPolicy(updateDistributionPolicyOptions);
            return withContext(context -> upsertDistributionPolicyWithResponse(updateDistributionPolicyOptions.getId(), distributionPolicy, context)
                .flatMap(
                    (Response<DistributionPolicy> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates a distribution policy.
     *
     * @param updateDistributionPolicyOptions Request options to update distribution policy.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DistributionPolicy>> updateDistributionPolicyWithResponse(UpdateDistributionPolicyOptions updateDistributionPolicyOptions) {
        try {
            DistributionPolicy distributionPolicy = DistributionPolicyAdapter.convertUpdateOptionsToClassificationPolicy(updateDistributionPolicyOptions);
            return withContext(context -> upsertDistributionPolicyWithResponse(updateDistributionPolicyOptions.getId(), distributionPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<DistributionPolicy>> upsertDistributionPolicyWithResponse(String id, DistributionPolicy distributionPolicy, Context context) {
        try {
            return jobRouter.upsertDistributionPolicyWithResponseAsync(id, distributionPolicy, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing distribution policy by Id.
     *
     * @param id Id of the distribution policy.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DistributionPolicy> getDistributionPolicy(String id) {
        try {
            return withContext(context -> getDistributionPolicyWithResponse(id, context)
                .flatMap(
                    (Response<DistributionPolicy> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing distribution policy by Id.
     *
     * @param id Id of the distribution policy.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DistributionPolicy>> getDistributionPolicyWithResponse(String id) {
        try {
            return withContext(context -> getDistributionPolicyWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<DistributionPolicy>> getDistributionPolicyWithResponse(String id, Context context) {
        try {
            return jobRouter.getDistributionPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Delete a distribution policy by Id.
     *
     * @param id Id of the distribution policy.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDistributionPolicy(String id) {
        try {
            return withContext(context -> deleteDistributionPolicyWithResponse(id, context)
                .flatMap(
                    (Response<Void> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Delete a distribution policy by Id.
     *
     * @param id Id of the distribution policy.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDistributionPolicyWithResponse(String id) {
        try {
            return withContext(context -> deleteDistributionPolicyWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteDistributionPolicyWithResponse(String id, Context context) {
        try {
            return jobRouter.deleteDistributionPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing distribution policies.
     *
     * @return a paged collection of distribution policies.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedDistributionPolicy> listDistributionPolicies() {
        try {
            return jobRouter.listDistributionPoliciesAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing distribution policies.
     *
     * @param maxPageSize Maximum page size.
     * @return a paged collection of distribution policies.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedDistributionPolicy> listDistributionPolicies(Integer maxPageSize) {
        try {
            return jobRouter.listDistributionPoliciesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Creates an exception policy.
     *
     * @param createExceptionPolicyOptions Create options for Exception Policy.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ExceptionPolicy> createExceptionPolicy(CreateExceptionPolicyOptions createExceptionPolicyOptions) {
        try {
            ExceptionPolicy exceptionPolicy = ExceptionPolicyAdapter.convertCreateOptionsToExceptionPolicy(createExceptionPolicyOptions);
            return withContext(context -> upsertExceptionPolicyWithResponse(createExceptionPolicyOptions.getId(), exceptionPolicy, context)
                .flatMap(
                    (Response<ExceptionPolicy> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates an exception policy.
     *
     * @param id Id of the exception policy.
     * @param exceptionPolicy Model of exception policy properties to be patched.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ExceptionPolicy>> createExceptionPolicyWithResponse(String id, ExceptionPolicy exceptionPolicy) {
        try {
            return withContext(context -> upsertExceptionPolicyWithResponse(id, exceptionPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates an exception policy.
     *
     * @param updateExceptionPolicyOptions Options to update ExceptionPolicy.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ExceptionPolicy> updateExceptionPolicy(UpdateExceptionPolicyOptions updateExceptionPolicyOptions) {
        try {
            ExceptionPolicy exceptionPolicy = ExceptionPolicyAdapter.convertUpdateOptionsToExceptionPolicy(updateExceptionPolicyOptions);
            return withContext(context -> upsertExceptionPolicyWithResponse(updateExceptionPolicyOptions.getId(), exceptionPolicy, context)
                .flatMap(
                    (Response<ExceptionPolicy> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates an exception policy.
     *
     * @param updateExceptionPolicyOptions Options to update ExceptionPolicy.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ExceptionPolicy>> updateExceptionPolicyWithResponse(UpdateExceptionPolicyOptions updateExceptionPolicyOptions) {
        try {
            ExceptionPolicy exceptionPolicy = ExceptionPolicyAdapter.convertUpdateOptionsToExceptionPolicy(updateExceptionPolicyOptions);
            return withContext(context -> upsertExceptionPolicyWithResponse(updateExceptionPolicyOptions.getId(), exceptionPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ExceptionPolicy>> upsertExceptionPolicyWithResponse(String id, ExceptionPolicy exceptionPolicy, Context context) {
        try {
            return jobRouter.upsertExceptionPolicyWithResponseAsync(id, exceptionPolicy, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing exception policy by Id.
     *
     * @param id Id of the exception policy to retrieve.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ExceptionPolicy> getExceptionPolicy(String id) {
        try {
            return withContext(context -> getExceptionPolicyWithResponse(id, context)
                .flatMap(
                    (Response<ExceptionPolicy> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing exception policy by Id.
     *
     * @param id Id of the exception policy to retrieve.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ExceptionPolicy>> getExceptionPolicyWithResponse(String id) {
        try {
            return withContext(context -> getExceptionPolicyWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ExceptionPolicy>> getExceptionPolicyWithResponse(String id, Context context) {
        try {
            return jobRouter.getExceptionPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a exception policy by Id.
     *
     * @param id Id of the exception policy to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteExceptionPolicy(String id) {
        try {
            return withContext(context -> deleteExceptionPolicyWithResponse(id, context)
                .flatMap(
                    (Response<Void> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a exception policy by Id.
     *
     * @param id Id of the exception policy to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteExceptionPolicyWithResponse(String id) {
        try {
            return withContext(context -> deleteExceptionPolicyWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteExceptionPolicyWithResponse(String id, Context context) {
        try {
            return jobRouter.deleteExceptionPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing exception policies.
     *
     * @return a paged collection of exception policies.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedExceptionPolicy> listExceptionPolicies() {
        try {
            return jobRouter.listExceptionPoliciesAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing exception policies.
     *
     * @param maxPageSize Maximum Number of objects to return per page.
     * @return a paged collection of exception policies.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedExceptionPolicy> listExceptionPolicies(Integer maxPageSize) {
        try {
            return jobRouter.listExceptionPoliciesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Create a job.
     *
     * @param createJobOptions Options to create RouterJob.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouterJob> createJob(CreateJobOptions createJobOptions) {
        try {
            RouterJob routerJob = JobAdapter.convertCreateJobOptionsToRouterJob(createJobOptions);
            return withContext(context -> upsertJobWithResponse(createJobOptions.getId(), routerJob, context)
                .flatMap(
                    (Response<RouterJob> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Create a job.
     *
     * @param createJobOptions Options to create RouterJob.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouterJob>> createJobWithResponse(CreateJobOptions createJobOptions) {
        try {
            RouterJob routerJob = JobAdapter.convertCreateJobOptionsToRouterJob(createJobOptions);
            return withContext(context -> upsertJobWithResponse(createJobOptions.getId(), routerJob, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates a job.
     *
     * @param updateJobOptions Options to update RouterJob.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouterJob> updateJob(UpdateJobOptions updateJobOptions) {
        try {
            RouterJob routerJob = JobAdapter.convertUpdateJobOptionsToRouterJob(updateJobOptions);
            return withContext(context -> upsertJobWithResponse(updateJobOptions.getId(), routerJob, context)
                .flatMap(
                    (Response<RouterJob> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates a job.
     *
     * @param updateJobOptions Options to update RouterJob.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouterJob>> updateJobWithResponse(UpdateJobOptions updateJobOptions) {
        try {
            RouterJob routerJob = JobAdapter.convertUpdateJobOptionsToRouterJob(updateJobOptions);
            return withContext(context -> upsertJobWithResponse(updateJobOptions.getId(), routerJob, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<RouterJob>> upsertJobWithResponse(String id, RouterJob routerJob, Context context) {
        try {
            return jobRouter.upsertJobWithResponseAsync(id, routerJob, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing job by Id.
     *
     * @param id Id of the job to retrieve.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouterJob> getJob(String id) {
        try {
            return withContext(context -> getJobWithResponse(id, context)
                .flatMap(
                    (Response<RouterJob> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing job by Id.
     *
     * @param id Id of the job to retrieve.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouterJob>> getJobWithResponse(String id) {
        try {
            return withContext(context -> getJobWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<RouterJob>> getJobWithResponse(String id, Context context) {
        try {
            return jobRouter.getJobWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a job and all of its traces.
     *
     * @param id Id of the job.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteJob(String id) {
        try {
            return withContext(context -> deleteJobWithResponse(id, context)
                .flatMap(
                    (Response<Void> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a job and all of its traces.
     *
     * @param id Id of the job.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteJobWithResponse(String id) {
        try {
            return withContext(context -> deleteJobWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteJobWithResponse(String id, Context context) {
        try {
            return jobRouter.deleteJobWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Reclassify a job.
     *
     * @param id Id of the job.
     * @return ReclassifyJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ReclassifyJobResult> reclassifyJob(String id) {
        try {
            return withContext(context -> reclassifyJobWithResponse(id, context)
                .flatMap(
                    (Response<ReclassifyJobResult> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Reclassify a job.
     *
     * @param id Id of the job.
     * @return ReclassifyJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ReclassifyJobResult>> reclassifyJobWithResponse(String id) {
        try {
            return withContext(context -> reclassifyJobWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ReclassifyJobResult>> reclassifyJobWithResponse(String id, Context context) {
        try {
            return jobRouter.reclassifyJobActionWithResponseAsync(id, null, context)
                .map(result -> new SimpleResponse<ReclassifyJobResult>(
                    result.getRequest(), result.getStatusCode(), result.getHeaders(), new ReclassifyJobResult(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     *
     * @param id Id of the job.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values. If not
     * provided, default value of "Cancelled" is set.
     * @return CancelJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CancelJobResult> cancelJob(String id, String note, String dispositionCode) {
        try {
            return withContext(context -> cancelJobWithResponse(id, note, dispositionCode, context)
                .flatMap(
                    (Response<CancelJobResult> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     *
     * @param id Id of the job.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values. If not
     * provided, default value of "Cancelled" is set.
     * @return CancelJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CancelJobResult>> cancelJobWithResponse(String id, String note, String dispositionCode) {
        try {
            return withContext(context -> cancelJobWithResponse(id, note, dispositionCode, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<CancelJobResult>> cancelJobWithResponse(String id, String note, String dispositionCode, Context context) {
        try {
            return jobRouter.cancelJobActionWithResponseAsync(id, note, dispositionCode, context)
                .map(result -> new SimpleResponse<CancelJobResult>(
                    result.getRequest(), result.getStatusCode(), result.getHeaders(), new CancelJobResult(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Completes an assigned job.
     *
     * @param id Id of the job.
     * @param assignmentId The assignment within the job to complete.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @return CompleteJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CompleteJobResult> completeJob(String id, String assignmentId, String note) {
        try {
            return withContext(context -> completeJobWithResponse(id, assignmentId, note, context)
                .flatMap(
                    (Response<CompleteJobResult> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Completes an assigned job.
     *
     * @param id Id of the job.
     * @param assignmentId The assignment within the job to complete.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @return CompleteJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CompleteJobResult>> completeJobWithResponse(String id, String assignmentId, String note) {
        try {
            return withContext(context -> completeJobWithResponse(id, assignmentId, note, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<CompleteJobResult>> completeJobWithResponse(String id, String assignmentId, String note, Context context) {
        try {
            return jobRouter.completeJobActionWithResponseAsync(id, assignmentId, note, context)
                .map(result -> new SimpleResponse<CompleteJobResult>(
                    result.getRequest(), result.getStatusCode(), result.getHeaders(), new CompleteJobResult(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Closes a completed job.
     *
     * @param id Id of the job.
     * @param assignmentId The assignment within which the job is to be closed.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values.
     * @param closeTime If not provided, worker capacity is released immediately along with a JobClosedEvent
     * notification. If provided, worker capacity is released along with a JobClosedEvent notification at a future
     * time.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @return CloseJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CloseJobResult> closeJob(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note) {
        try {
            return withContext(context -> closeJobWithResponse(id, assignmentId, dispositionCode, closeTime, note, context)
                .flatMap(
                    (Response<CloseJobResult> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Closes a completed job.
     *
     * @param id Id of the job.
     * @param assignmentId The assignment within which the job is to be closed.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values.
     * @param closeTime If not provided, worker capacity is released immediately along with a JobClosedEvent
     * notification. If provided, worker capacity is released along with a JobClosedEvent notification at a future
     * time.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @return CloseJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CloseJobResult>> closeJobWithResponse(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note) {
        try {
            return withContext(context -> closeJobWithResponse(id, assignmentId, dispositionCode, closeTime, note, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<CloseJobResult>> closeJobWithResponse(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note, Context context) {
        try {
            return jobRouter.closeJobActionWithResponseAsync(id, assignmentId, dispositionCode, closeTime, note, context)
                .map(result -> new SimpleResponse<CloseJobResult>(
                    result.getRequest(), result.getStatusCode(), result.getHeaders(), new CloseJobResult(result.getValue())
                ));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves list of jobs based on filter parameters.
     *
     * @return a paged collection of jobs.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedJob> listJobs() {
        try {
            return jobRouter.listJobsAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves list of jobs based on filter parameters.
     *
     * @param jobStateSelector (Optional) If specified, filter jobs by status.
     * @param queueId (Optional) If specified, filter jobs by queue.
     * @param channelId (Optional) If specified, filter jobs by channel.
     * @param maxPageSize Number of objects to return per page.
     * @return a paged collection of jobs.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedJob> listJobs(JobStateSelector jobStateSelector, String queueId, String channelId, Integer maxPageSize) {
        try {
            return jobRouter.listJobsAsync(jobStateSelector, queueId, channelId, maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Gets a job's position details.
     *
     * @param id Id of the job.
     * @return a job's position details.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<JobPositionDetails> getInQueuePosition(String id) {
        try {
            return withContext(context -> getInQueuePositionWithResponse(id, context)
                .flatMap(
                    (Response<JobPositionDetails> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Gets a job's position details.
     *
     * @param id Id of the job.
     * @return a job's position details.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<JobPositionDetails>> getInQueuePositionWithResponse(String id) {
        try {
            return withContext(context -> getInQueuePositionWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<JobPositionDetails>> getInQueuePositionWithResponse(String id, Context context) {
        try {
            return jobRouter.getInQueuePositionWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Accepts an offer to work on a job and returns a 409/Conflict if another agent accepted the job already.
     *
     * @param workerId Id of the worker.
     * @param offerId Id of the offer.
     * @return response containing Id's for the worker, job, and assignment from an accepted offer.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcceptJobOfferResponse> acceptJobOffer(String workerId, String offerId) {
        try {
            return withContext(context -> acceptJobOfferWithResponse(workerId, offerId, context)
                .flatMap(
                    (Response<AcceptJobOfferResponse> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Accepts an offer to work on a job and returns a 409/Conflict if another agent accepted the job already.
     *
     * @param workerId Id of the worker.
     * @param offerId Id of the offer.
     * @return response containing Id's for the worker, job, and assignment from an accepted offer.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcceptJobOfferResponse>> acceptJobOfferWithResponse(String workerId, String offerId) {
        try {
            return withContext(context -> acceptJobOfferWithResponse(workerId, offerId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<AcceptJobOfferResponse>> acceptJobOfferWithResponse(String workerId, String offerId, Context context) {
        try {
            return jobRouter.acceptJobActionWithResponseAsync(workerId, offerId, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Declines an offer to work on a job.
     *
     * @param workerId Id of the worker.
     * @param offerId Id of the offer.
     * @return DeclineJobOfferResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeclineJobOfferResult> declineJobOffer(String workerId, String offerId) {
        try {
            return withContext(context -> declineJobOfferWithResponse(workerId, offerId, context)
                .flatMap(
                    (Response<DeclineJobOfferResult> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Declines an offer to work on a job.
     *
     * @param workerId Id of the worker.
     * @param offerId Id of the offer.
     * @return DeclineJobOfferResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DeclineJobOfferResult>> declineJobOfferWithResponse(String workerId, String offerId) {
        try {
            return withContext(context -> declineJobOfferWithResponse(workerId, offerId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<DeclineJobOfferResult>> declineJobOfferWithResponse(String workerId, String offerId, Context context) {
        try {
            return jobRouter.declineJobActionWithResponseAsync(workerId, offerId, context)
                .map(result -> new SimpleResponse<DeclineJobOfferResult>(
                    result.getRequest(), result.getStatusCode(), result.getHeaders(), new DeclineJobOfferResult(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Create a queue.
     *
     * @param createQueueOptions Container for inputs to create a queue.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<JobQueue> createQueue(CreateQueueOptions createQueueOptions) {
        try {
            JobQueue jobQueue = QueueAdapter.convertCreateQueueOptionsToJobQueue(createQueueOptions);
            return withContext(context -> upsertQueueWithResponse(createQueueOptions.getQueueId(), jobQueue, context)
                .flatMap(
                    (Response<JobQueue> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Create a queue.
     *
     * @param createQueueOptions Container for inputs to create a queue.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<JobQueue>> createQueueWithResponse(CreateQueueOptions createQueueOptions) {
        try {
            JobQueue jobQueue = QueueAdapter.convertCreateQueueOptionsToJobQueue(createQueueOptions);
            return withContext(context -> upsertQueueWithResponse(createQueueOptions.getQueueId(), jobQueue, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Update a queue.
     *
     * @param updateQueueOptions Container for inputs to update a queue.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<JobQueue> updateQueue(UpdateQueueOptions updateQueueOptions) {
        try {
            JobQueue jobQueue = QueueAdapter.convertUpdateQueueOptionsToJobQueue(updateQueueOptions);
            return withContext(context -> upsertQueueWithResponse(updateQueueOptions.getQueueId(), jobQueue, context)
                .flatMap(
                    (Response<JobQueue> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Update a queue.
     *
     * @param updateQueueOptions Container for inputs to update a queue.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<JobQueue>> updateQueueWithResponse(UpdateQueueOptions updateQueueOptions) {
        try {
            JobQueue jobQueue = QueueAdapter.convertUpdateQueueOptionsToJobQueue(updateQueueOptions);
            return withContext(context -> upsertQueueWithResponse(updateQueueOptions.getQueueId(), jobQueue, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<JobQueue>> upsertQueueWithResponse(String id, JobQueue jobQueue, Context context) {
        try {
            return jobRouter.upsertQueueWithResponseAsync(id, jobQueue, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing queue by Id.
     *
     * @param id Id of the queue to retrieve.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<JobQueue> getQueue(String id) {
        try {
            return withContext(context -> getQueueWithResponse(id, context)
                .flatMap(
                    (Response<JobQueue> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing queue by Id.
     *
     * @param id Id of the queue to retrieve.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<JobQueue>> getQueueWithResponse(String id) {
        try {
            return withContext(context -> getQueueWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<JobQueue>> getQueueWithResponse(String id, Context context) {
        try {
            return jobRouter.getQueueWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a queue by Id.
     *
     * @param id Id of the queue to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteQueue(String id) {
        try {
            return withContext(context -> deleteQueueWithResponse(id, context)
                .flatMap(
                    (Response<Void> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a queue by Id.
     *
     * @param id Id of the queue to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteQueueWithResponse(String id) {
        try {
            return withContext(context -> deleteQueueWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteQueueWithResponse(String id, Context context) {
        try {
            return jobRouter.deleteQueueWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing queues.
     *
     * @return a paged collection of queues.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedQueue> listQueues() {
        try {
            return jobRouter.listQueuesAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing queues.
     *
     * @param maxPageSize Number of objects to return per page.
     * @return a paged collection of queues.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedQueue> listQueues(Integer maxPageSize) {
        try {
            return jobRouter.listQueuesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves a queue's statistics.
     *
     * @param id Id of the queue to retrieve statistics.
     * @return statistics for the queue.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<QueueStatistics> getQueueStatistics(String id) {
        try {
            return withContext(context -> getQueueStatisticsWithResponse(id, context)
                .flatMap(
                    (Response<QueueStatistics> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves a queue's statistics.
     *
     * @param id Id of the queue to retrieve statistics.
     * @return statistics for the queue.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<QueueStatistics>> getQueueStatisticsWithResponse(String id) {
        try {
            return withContext(context -> getQueueStatisticsWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<QueueStatistics>> getQueueStatisticsWithResponse(String id, Context context) {
        try {
            return jobRouter.getQueueStatisticsWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Create a worker.
     *
     * @param createWorkerOptions Container for inputs to create a worker.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouterWorker> createWorker(CreateWorkerOptions createWorkerOptions) {
        try {
            RouterWorker routerWorker = WorkerAdapter.convertCreateWorkerOptionsToRouterWorker(createWorkerOptions);
            return withContext(context -> upsertWorkerWithResponse(createWorkerOptions.getWorkerId(), routerWorker, context)
                .flatMap(
                    (Response<RouterWorker> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Create a worker.
     *
     * @param createWorkerOptions Container for inputs to create a worker.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouterWorker>> createWorkerWithResponse(CreateWorkerOptions createWorkerOptions) {
        try {
            RouterWorker routerWorker = WorkerAdapter.convertCreateWorkerOptionsToRouterWorker(createWorkerOptions);
            return withContext(context -> upsertWorkerWithResponse(createWorkerOptions.getWorkerId(), routerWorker, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Update a worker.
     *
     * @param updateWorkerOptions Container for inputs to update a worker.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouterWorker> updateWorker(UpdateWorkerOptions updateWorkerOptions) {
        try {
            RouterWorker routerWorker = WorkerAdapter.convertUpdateWorkerOptionsToRouterWorker(updateWorkerOptions);
            return withContext(context -> upsertWorkerWithResponse(updateWorkerOptions.getWorkerId(), routerWorker, context)
                .flatMap(
                    (Response<RouterWorker> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Update a worker.
     *
     * @param updateWorkerOptions Container for inputs to update a worker.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouterWorker>> updateWorkerWithResponse(UpdateWorkerOptions updateWorkerOptions) {
        try {
            RouterWorker routerWorker = WorkerAdapter.convertUpdateWorkerOptionsToRouterWorker(updateWorkerOptions);
            return withContext(context -> upsertWorkerWithResponse(updateWorkerOptions.getWorkerId(), routerWorker, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<RouterWorker>> upsertWorkerWithResponse(String id, RouterWorker routerWorker, Context context) {
        try {
            return jobRouter.upsertWorkerWithResponseAsync(id, routerWorker, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing worker by Id.
     *
     * @param id Id of the worker to retrieve.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouterWorker> getWorker(String id) {
        try {
            return withContext(context -> getWorkerWithResponse(id, context)
                .flatMap(
                    (Response<RouterWorker> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing worker by Id.
     *
     * @param id Id of the worker to retrieve.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouterWorker>> getWorkerWithResponse(String id) {
        try {
            return withContext(context -> getWorkerWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<RouterWorker>> getWorkerWithResponse(String id, Context context) {
        try {
            return jobRouter.getWorkerWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a worker and all of its traces.
     *
     * @param id Id of the worker to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteWorker(String id) {
        try {
            return withContext(context -> deleteWorkerWithResponse(id, context)
                .flatMap(
                    (Response<Void> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        } else {
                            return Mono.empty();
                        }
                    }));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a worker and all of its traces.
     *
     * @param id Id of the worker to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWorkerWithResponse(String id) {
        try {
            return withContext(context -> deleteWorkerWithResponse(id, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteWorkerWithResponse(String id, Context context) {
        try {
            return jobRouter.deleteWorkerWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing workers.
     *
     * @return a paged collection of workers.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedWorker> listWorkers() {
        try {
            return jobRouter.listWorkersAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing workers.
     *
     * @param workerStateSelector (Optional) If specified, select workers by worker status.
     * @param channelId (Optional) If specified, select workers who have a channel configuration with this channel.
     * @param queueId (Optional) If specified, select workers who are assigned to this queue.
     * @param hasCapacity (Optional) If set to true, select only workers who have capacity for the channel specified by
     * `channelId` or for any channel if `channelId` not specified. If set to false, then will return all workers
     * including workers without any capacity for jobs. Defaults to false.
     * @param maxPageSize Number of objects to return per page.
     * @return a paged collection of workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedWorker> listWorkers(WorkerStateSelector workerStateSelector, String channelId, String queueId, Boolean hasCapacity, Integer maxPageSize) {
        try {
            return jobRouter.listWorkersAsync(workerStateSelector, channelId, queueId, hasCapacity, maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }
}
