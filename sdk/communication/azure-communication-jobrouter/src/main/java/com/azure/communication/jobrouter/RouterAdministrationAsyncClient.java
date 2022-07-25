package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.AzureCommunicationRoutingServiceImpl;
import com.azure.communication.jobrouter.implementation.JobRouterAdministrationsImpl;
import com.azure.communication.jobrouter.implementation.convertors.ClassificationPolicyAdapter;
import com.azure.communication.jobrouter.implementation.convertors.DistributionPolicyAdapter;
import com.azure.communication.jobrouter.implementation.convertors.ExceptionPolicyAdapter;
import com.azure.communication.jobrouter.implementation.convertors.QueueAdapter;
import com.azure.communication.jobrouter.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.ClassificationPolicyItem;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.DistributionPolicyItem;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.ExceptionPolicyItem;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.JobQueueItem;
import com.azure.communication.jobrouter.models.options.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.options.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.options.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.options.CreateQueueOptions;
import com.azure.communication.jobrouter.models.options.UpdateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.options.UpdateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.options.UpdateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.options.UpdateQueueOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Async Client that supports job router administration operations.
 *
 * <p><strong>Instantiating an asynchronous JobRouter Administration Client</strong></p>
 * <!-- src_embed com.azure.communication.jobrouter.routeradministrationasyncclient.instantiation -->
 * <pre>
 * &#47;&#47; Initialize the router administration client builder
 * final RouterAdministrationClientBuilder builder = new RouterAdministrationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;;
 * &#47;&#47; Build the router administration client
 * RouterAdministrationAsyncClient routerAdminAsyncClient = builder.buildAsyncClient&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.communication.jobrouter.routeradministrationasyncclient.instantiation -->
 *
 * <p>View {@link RouterAdministrationClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see RouterAdministrationClientBuilder
 */
@ServiceClient(builder = RouterAdministrationClientBuilder.class, isAsync = true)
public class RouterAdministrationAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(RouterAsyncClient.class);

    private final JobRouterAdministrationsImpl jobRouterAdmin;

    RouterAdministrationAsyncClient(AzureCommunicationRoutingServiceImpl jobRouterService) {
        this.jobRouterAdmin = jobRouterService.getJobRouterAdministrations();
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
            return jobRouterAdmin.upsertClassificationPolicyWithResponseAsync(id, classificationPolicy, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing classification policy by Id.
     *
     * @param classificationPolicyId Id of the classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ClassificationPolicy> getClassificationPolicy(String classificationPolicyId) {
        try {
            return withContext(context -> getClassificationPolicyWithResponse(classificationPolicyId, context)
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
     * @param classificationPolicyId Id of the classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ClassificationPolicy>> getClassificationPolicyWithResponse(String classificationPolicyId) {
        try {
            return withContext(context -> getClassificationPolicyWithResponse(classificationPolicyId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ClassificationPolicy>> getClassificationPolicyWithResponse(String id, Context context) {
        try {
            return jobRouterAdmin.getClassificationPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a Classification Policy by Id.
     *
     * @param classificationPolicyId Id of the classification policy.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteClassificationPolicy(String classificationPolicyId) {
        try {
            return withContext(context -> deleteClassificationPolicyWithResponse(classificationPolicyId, context)
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
     * @param classificationPolicyId Id of the classification policy.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteClassificationPolicyWithResponse(String classificationPolicyId) {
        try {
            return withContext(context -> deleteClassificationPolicyWithResponse(classificationPolicyId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteClassificationPolicyWithResponse(String id, Context context) {
        try {
            return jobRouterAdmin.deleteClassificationPolicyWithResponseAsync(id, context);
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
    public PagedFlux<ClassificationPolicyItem> listClassificationPolicies() {
        try {
            return jobRouterAdmin.listClassificationPoliciesAsync();
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
    public PagedFlux<ClassificationPolicyItem> listClassificationPolicies(Integer maxPageSize) {
        try {
            return jobRouterAdmin.listClassificationPoliciesAsync(maxPageSize);
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
            return jobRouterAdmin.upsertDistributionPolicyWithResponseAsync(id, distributionPolicy, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing distribution policy by Id.
     *
     * @param distributionPolicyId Id of the distribution policy.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DistributionPolicy> getDistributionPolicy(String distributionPolicyId) {
        try {
            return withContext(context -> getDistributionPolicyWithResponse(distributionPolicyId, context)
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
     * @param distributionPolicyId Id of the distribution policy.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DistributionPolicy>> getDistributionPolicyWithResponse(String distributionPolicyId) {
        try {
            return withContext(context -> getDistributionPolicyWithResponse(distributionPolicyId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<DistributionPolicy>> getDistributionPolicyWithResponse(String id, Context context) {
        try {
            return jobRouterAdmin.getDistributionPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Delete a distribution policy by Id.
     *
     * @param distributionPolicyId Id of the distribution policy.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDistributionPolicy(String distributionPolicyId) {
        try {
            return withContext(context -> deleteDistributionPolicyWithResponse(distributionPolicyId, context)
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
     * @param distributionPolicyId Id of the distribution policy.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDistributionPolicyWithResponse(String distributionPolicyId) {
        try {
            return withContext(context -> deleteDistributionPolicyWithResponse(distributionPolicyId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteDistributionPolicyWithResponse(String id, Context context) {
        try {
            return jobRouterAdmin.deleteDistributionPolicyWithResponseAsync(id, context);
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
    public PagedFlux<DistributionPolicyItem> listDistributionPolicies() {
        try {
            return jobRouterAdmin.listDistributionPoliciesAsync();
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
    public PagedFlux<DistributionPolicyItem> listDistributionPolicies(Integer maxPageSize) {
        try {
            return jobRouterAdmin.listDistributionPoliciesAsync(maxPageSize);
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
     * @param createExceptionPolicyOptions Create options for Exception Policy.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ExceptionPolicy>> createExceptionPolicyWithResponse(CreateExceptionPolicyOptions createExceptionPolicyOptions) {
        try {
            ExceptionPolicy exceptionPolicy = ExceptionPolicyAdapter.convertCreateOptionsToExceptionPolicy(createExceptionPolicyOptions);
            return withContext(context -> upsertExceptionPolicyWithResponse(createExceptionPolicyOptions.getId(), exceptionPolicy, context));
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
            return jobRouterAdmin.upsertExceptionPolicyWithResponseAsync(id, exceptionPolicy, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing exception policy by Id.
     *
     * @param exceptionPolicyId Id of the exception policy to retrieve.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ExceptionPolicy> getExceptionPolicy(String exceptionPolicyId) {
        try {
            return withContext(context -> getExceptionPolicyWithResponse(exceptionPolicyId, context)
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
     * @param exceptionPolicyId Id of the exception policy to retrieve.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ExceptionPolicy>> getExceptionPolicyWithResponse(String exceptionPolicyId) {
        try {
            return withContext(context -> getExceptionPolicyWithResponse(exceptionPolicyId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ExceptionPolicy>> getExceptionPolicyWithResponse(String id, Context context) {
        try {
            return jobRouterAdmin.getExceptionPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a exception policy by Id.
     *
     * @param exceptionPolicyId Id of the exception policy to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteExceptionPolicy(String exceptionPolicyId) {
        try {
            return withContext(context -> deleteExceptionPolicyWithResponse(exceptionPolicyId, context)
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
     * @param exceptionPolicyId Id of the exception policy to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteExceptionPolicyWithResponse(String exceptionPolicyId) {
        try {
            return withContext(context -> deleteExceptionPolicyWithResponse(exceptionPolicyId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteExceptionPolicyWithResponse(String id, Context context) {
        try {
            return jobRouterAdmin.deleteExceptionPolicyWithResponseAsync(id, context);
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
    public PagedFlux<ExceptionPolicyItem> listExceptionPolicies() {
        try {
            return jobRouterAdmin.listExceptionPoliciesAsync();
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
    public PagedFlux<ExceptionPolicyItem> listExceptionPolicies(Integer maxPageSize) {
        try {
            return jobRouterAdmin.listExceptionPoliciesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
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
            return jobRouterAdmin.upsertQueueWithResponseAsync(id, jobQueue, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Retrieves an existing queue by Id.
     *
     * @param queueId Id of the queue to retrieve.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<JobQueue> getQueue(String queueId) {
        try {
            return withContext(context -> getQueueWithResponse(queueId, context)
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
     * @param queueId Id of the queue to retrieve.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<JobQueue>> getQueueWithResponse(String queueId) {
        try {
            return withContext(context -> getQueueWithResponse(queueId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<JobQueue>> getQueueWithResponse(String id, Context context) {
        try {
            return jobRouterAdmin.getQueueWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes a queue by Id.
     *
     * @param queueId Id of the queue to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteQueue(String queueId) {
        try {
            return withContext(context -> deleteQueueWithResponse(queueId, context)
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
     * @param queueId Id of the queue to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteQueueWithResponse(String queueId) {
        try {
            return withContext(context -> deleteQueueWithResponse(queueId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteQueueWithResponse(String id, Context context) {
        try {
            return jobRouterAdmin.deleteQueueWithResponseAsync(id, context);
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
    public PagedFlux<JobQueueItem> listQueues() {
        try {
            return jobRouterAdmin.listQueuesAsync();
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
    public PagedFlux<JobQueueItem> listQueues(Integer maxPageSize) {
        try {
            return jobRouterAdmin.listQueuesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }
}
