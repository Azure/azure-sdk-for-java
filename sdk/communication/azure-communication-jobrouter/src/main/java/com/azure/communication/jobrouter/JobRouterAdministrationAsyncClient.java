// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.AzureCommunicationServicesImpl;
import com.azure.communication.jobrouter.implementation.JobRouterAdministrationsImpl;
import com.azure.communication.jobrouter.implementation.accesshelpers.ClassificationPolicyConstructorProxy;
import com.azure.communication.jobrouter.implementation.accesshelpers.DistributionPolicyConstructorProxy;
import com.azure.communication.jobrouter.implementation.accesshelpers.ExceptionPolicyConstructorProxy;
import com.azure.communication.jobrouter.implementation.accesshelpers.RouterQueueConstructorProxy;
import com.azure.communication.jobrouter.implementation.converters.ClassificationPolicyAdapter;
import com.azure.communication.jobrouter.implementation.converters.DistributionPolicyAdapter;
import com.azure.communication.jobrouter.implementation.converters.ExceptionPolicyAdapter;
import com.azure.communication.jobrouter.implementation.converters.QueueAdapter;
import com.azure.communication.jobrouter.implementation.models.ClassificationPolicyInternal;
import com.azure.communication.jobrouter.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.jobrouter.implementation.models.DistributionPolicyInternal;
import com.azure.communication.jobrouter.implementation.models.ExceptionPolicyInternal;
import com.azure.communication.jobrouter.implementation.models.RouterQueueInternal;
import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.ClassificationPolicyItem;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.DistributionPolicyItem;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.ExceptionPolicyItem;
import com.azure.communication.jobrouter.models.ListClassificationPoliciesOptions;
import com.azure.communication.jobrouter.models.ListDistributionPoliciesOptions;
import com.azure.communication.jobrouter.models.ListExceptionPoliciesOptions;
import com.azure.communication.jobrouter.models.ListQueuesOptions;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterQueueItem;
import com.azure.communication.jobrouter.models.UpdateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateQueueOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
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
 * <!-- src_embed com.azure.communication.jobrouter.jobrouteradministrationasyncclient.instantiation -->
 * <pre>
 * &#47;&#47; Initialize the jobrouter administration client builder
 * final JobRouterAdministrationClientBuilder builder = new JobRouterAdministrationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;;
 * &#47;&#47; Build the jobrouter administration client
 * JobRouterAdministrationAsyncClient jobrouterAdministrationClient = builder.buildAsyncClient&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.communication.jobrouter.jobrouteradministrationasyncclient.instantiation -->
 *
 * <p>View {@link JobRouterAdministrationClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see JobRouterAdministrationClientBuilder
 */
@ServiceClient(builder = JobRouterAdministrationClientBuilder.class, isAsync = true)
public final class JobRouterAdministrationAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(JobRouterAdministrationAsyncClient.class);

    private final JobRouterAdministrationsImpl jobRouterAdmin;

    JobRouterAdministrationAsyncClient(AzureCommunicationServicesImpl jobRouterService) {
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
            ClassificationPolicyInternal classificationPolicy = ClassificationPolicyAdapter.convertCreateOptionsToClassificationPolicy(createClassificationPolicyOptions);
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
            ClassificationPolicyInternal classificationPolicy = ClassificationPolicyAdapter.convertCreateOptionsToClassificationPolicy(createClassificationPolicyOptions);
            return withContext(context -> upsertClassificationPolicyWithResponse(createClassificationPolicyOptions.getId(), classificationPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates a classification policy.
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
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
            ClassificationPolicyInternal classificationPolicy = ClassificationPolicyAdapter.convertUpdateOptionsToClassificationPolicy(updateClassificationPolicyOptions);
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
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
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
            ClassificationPolicyInternal classificationPolicy = ClassificationPolicyAdapter.convertUpdateOptionsToClassificationPolicy(updateClassificationPolicyOptions);
            return withContext(context -> upsertClassificationPolicyWithResponse(updateClassificationPolicyOptions.getId(), classificationPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ClassificationPolicy>> upsertClassificationPolicyWithResponse(String id, ClassificationPolicyInternal classificationPolicy, Context context) {
        try {
            return jobRouterAdmin.upsertClassificationPolicyWithResponseAsync(id, classificationPolicy, context)
                .map(response -> new SimpleResponse<>(response, ClassificationPolicyConstructorProxy.create(response.getValue())));
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
            return jobRouterAdmin.getClassificationPolicyWithResponseAsync(id, context)
                .map(response -> new SimpleResponse<>(response, ClassificationPolicyConstructorProxy.create(response.getValue())));
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
            return ClassificationPolicyAdapter.convertPagedFluxToPublic(
                jobRouterAdmin.listClassificationPoliciesAsync(null));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing classification policies.
     *
     * @param listClassificationPoliciesOptions options for listClassificationPolicies.
     * @return a paged collection of classification policies.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ClassificationPolicyItem> listClassificationPolicies(ListClassificationPoliciesOptions listClassificationPoliciesOptions) {
        try {
            return ClassificationPolicyAdapter.convertPagedFluxToPublic(
                jobRouterAdmin.listClassificationPoliciesAsync(listClassificationPoliciesOptions.getMaxPageSize()));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing classification policies.
     *
     * @param listClassificationPoliciesOptions options for listClassificationPolicies.
     * @param context Context for listClassificationPolicies.
     * @return a paged collection of classification policies.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    PagedFlux<ClassificationPolicyItem> listClassificationPolicies(ListClassificationPoliciesOptions listClassificationPoliciesOptions, Context context) {
        try {
            return ClassificationPolicyAdapter.convertPagedFluxToPublic(
                jobRouterAdmin.listClassificationPoliciesAsync(listClassificationPoliciesOptions.getMaxPageSize(), context));
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
            DistributionPolicyInternal distributionPolicy = DistributionPolicyAdapter.convertCreateOptionsToDistributionPolicy(createDistributionPolicyOptions);
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
            DistributionPolicyInternal distributionPolicy = DistributionPolicyAdapter.convertCreateOptionsToDistributionPolicy(createDistributionPolicyOptions);
            return withContext(context -> upsertDistributionPolicyWithResponse(createDistributionPolicyOptions.getId(), distributionPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates a distribution policy.
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
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
            DistributionPolicyInternal distributionPolicy = DistributionPolicyAdapter.convertUpdateOptionsToClassificationPolicy(updateDistributionPolicyOptions);
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
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
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
            DistributionPolicyInternal distributionPolicy = DistributionPolicyAdapter.convertUpdateOptionsToClassificationPolicy(updateDistributionPolicyOptions);
            return withContext(context -> upsertDistributionPolicyWithResponse(updateDistributionPolicyOptions.getId(), distributionPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<DistributionPolicy>> upsertDistributionPolicyWithResponse(String id, DistributionPolicyInternal distributionPolicy, Context context) {
        try {
            return jobRouterAdmin.upsertDistributionPolicyWithResponseAsync(id, distributionPolicy, context)
                .map(response -> new SimpleResponse<>(response, DistributionPolicyConstructorProxy.create(response.getValue())));
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
            return jobRouterAdmin.getDistributionPolicyWithResponseAsync(id, context)
                .map(response -> new SimpleResponse<>(response, DistributionPolicyConstructorProxy.create(response.getValue())));
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DistributionPolicyItem> listDistributionPolicies() {
        try {
            return DistributionPolicyAdapter.convertPagedFluxToPublic(jobRouterAdmin.listDistributionPoliciesAsync(null));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing distribution policies.
     *
     * @param listDistributionPoliciesOptions list options.
     * @return a paged collection of distribution policies.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DistributionPolicyItem> listDistributionPolicies(ListDistributionPoliciesOptions listDistributionPoliciesOptions) {
        try {
            return DistributionPolicyAdapter.convertPagedFluxToPublic(
                jobRouterAdmin.listDistributionPoliciesAsync(listDistributionPoliciesOptions.getMaxPageSize()));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing distribution policies.
     *
     * @param listDistributionPoliciesOptions list options.
     * @param context Context for listDistributionPolicies.
     * @return a paged collection of distribution policies.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    PagedFlux<DistributionPolicyItem> listDistributionPolicies(ListDistributionPoliciesOptions listDistributionPoliciesOptions, Context context) {
        try {
            return DistributionPolicyAdapter.convertPagedFluxToPublic(
                jobRouterAdmin.listDistributionPoliciesAsync(listDistributionPoliciesOptions.getMaxPageSize(), context));
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
            ExceptionPolicyInternal exceptionPolicy = ExceptionPolicyAdapter.convertCreateOptionsToExceptionPolicy(createExceptionPolicyOptions);
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
            ExceptionPolicyInternal exceptionPolicy = ExceptionPolicyAdapter.convertCreateOptionsToExceptionPolicy(createExceptionPolicyOptions);
            return withContext(context -> upsertExceptionPolicyWithResponse(createExceptionPolicyOptions.getId(), exceptionPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Updates an exception policy.
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
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
            ExceptionPolicyInternal exceptionPolicy = ExceptionPolicyAdapter.convertUpdateOptionsToExceptionPolicy(updateExceptionPolicyOptions);
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
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
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
            ExceptionPolicyInternal exceptionPolicy = ExceptionPolicyAdapter.convertUpdateOptionsToExceptionPolicy(updateExceptionPolicyOptions);
            return withContext(context -> upsertExceptionPolicyWithResponse(updateExceptionPolicyOptions.getId(), exceptionPolicy, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ExceptionPolicy>> upsertExceptionPolicyWithResponse(String id, ExceptionPolicyInternal exceptionPolicy, Context context) {
        try {
            return jobRouterAdmin.upsertExceptionPolicyWithResponseAsync(id, exceptionPolicy, context)
                .map(response -> new SimpleResponse<>(response, ExceptionPolicyConstructorProxy.create(response.getValue())));
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
            return jobRouterAdmin.getExceptionPolicyWithResponseAsync(id, context)
                .map(response -> new SimpleResponse<>(response, ExceptionPolicyConstructorProxy.create(response.getValue())));
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
            return ExceptionPolicyAdapter.convertPagedFluxToPublic(jobRouterAdmin.listExceptionPoliciesAsync(null));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing exception policies.
     *
     * @param listExceptionPoliciesOptions options for listExceptionPolicies.
     * @return a paged collection of exception policies.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ExceptionPolicyItem> listExceptionPolicies(ListExceptionPoliciesOptions listExceptionPoliciesOptions) {
        try {
            return ExceptionPolicyAdapter.convertPagedFluxToPublic(
                jobRouterAdmin.listExceptionPoliciesAsync(listExceptionPoliciesOptions.getMaxPageSize()));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing exception policies.
     *
     * @param listExceptionPoliciesOptions options for listExceptionPolicies.
     * @param context Context for listExceptionPolicies.
     * @return a paged collection of exception policies.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    PagedFlux<ExceptionPolicyItem> listExceptionPolicies(ListExceptionPoliciesOptions listExceptionPoliciesOptions, Context context) {
        try {
            return ExceptionPolicyAdapter.convertPagedFluxToPublic(
                jobRouterAdmin.listExceptionPoliciesAsync(listExceptionPoliciesOptions.getMaxPageSize(), context));
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
    public Mono<RouterQueue> createQueue(CreateQueueOptions createQueueOptions) {
        try {
            RouterQueueInternal queue = QueueAdapter.convertCreateQueueOptionsToRouterQueue(createQueueOptions);
            return withContext(context -> upsertQueueWithResponse(createQueueOptions.getQueueId(), queue, context)
                .flatMap(
                    (Response<RouterQueue> res) -> {
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
    public Mono<Response<RouterQueue>> createQueueWithResponse(CreateQueueOptions createQueueOptions) {
        try {
            RouterQueueInternal queue = QueueAdapter.convertCreateQueueOptionsToRouterQueue(createQueueOptions);
            return withContext(context -> upsertQueueWithResponse(createQueueOptions.getQueueId(), queue, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Update a queue.
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
     *
     * @param updateQueueOptions Container for inputs to update a queue.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouterQueue> updateQueue(UpdateQueueOptions updateQueueOptions) {
        try {
            RouterQueueInternal queue = QueueAdapter.convertUpdateQueueOptionsToRouterQueue(updateQueueOptions);
            return withContext(context -> upsertQueueWithResponse(updateQueueOptions.getQueueId(), queue, context)
                .flatMap(
                    (Response<RouterQueue> res) -> {
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
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
     *
     * @param updateQueueOptions Container for inputs to update a queue.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouterQueue>> updateQueueWithResponse(UpdateQueueOptions updateQueueOptions) {
        try {
            RouterQueueInternal queue = QueueAdapter.convertUpdateQueueOptionsToRouterQueue(updateQueueOptions);
            return withContext(context -> upsertQueueWithResponse(updateQueueOptions.getQueueId(), queue, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<RouterQueue>> upsertQueueWithResponse(String id, RouterQueueInternal queue, Context context) {
        try {
            return jobRouterAdmin.upsertQueueWithResponseAsync(id, queue, context)
                .map(response -> new SimpleResponse<>(response, RouterQueueConstructorProxy.create(response.getValue())));
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
    public Mono<RouterQueue> getQueue(String queueId) {
        try {
            return withContext(context -> getQueueWithResponse(queueId, context)
                .flatMap(
                    (Response<RouterQueue> res) -> {
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
    public Mono<Response<RouterQueue>> getQueueWithResponse(String queueId) {
        try {
            return withContext(context -> getQueueWithResponse(queueId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<RouterQueue>> getQueueWithResponse(String id, Context context) {
        try {
            return jobRouterAdmin.getQueueWithResponseAsync(id, context)
                .map(response -> new SimpleResponse<>(response, RouterQueueConstructorProxy.create(response.getValue())));
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
    public PagedFlux<RouterQueueItem> listQueues() {
        try {
            return QueueAdapter.convertPagedFluxToPublic(jobRouterAdmin.listQueuesAsync(null));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing queues.
     *
     * @param listQueuesOptions options for listQueues.
     * @return a paged collection of queues.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<RouterQueueItem> listQueues(ListQueuesOptions listQueuesOptions) {
        try {
            return QueueAdapter.convertPagedFluxToPublic(jobRouterAdmin.listQueuesAsync(listQueuesOptions.getMaxPageSize()));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    /**
     * Retrieves existing queues.
     *
     * @param listQueuesOptions options for listQueues.
     * @param context context for listQueues.
     * @return a paged collection of queues.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    PagedFlux<RouterQueueItem> listQueues(ListQueuesOptions listQueuesOptions, Context context) {
        try {
            return QueueAdapter.convertPagedFluxToPublic(jobRouterAdmin.listQueuesAsync(listQueuesOptions.getMaxPageSize(), context));
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }
}
