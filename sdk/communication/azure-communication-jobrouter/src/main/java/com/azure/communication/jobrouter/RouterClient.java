// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

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
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DeclineJobOfferResult;
import com.azure.communication.jobrouter.models.DistributionPolicy;
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
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.time.OffsetDateTime;

/**
 * Sync Client that supports job router operations.
 *
 * <p><strong>Instantiating a synchronous JobRouter Client</strong></p>
 * <!-- src_embed com.azure.communication.jobrouter.routerclient.instantiation -->
 * <pre>
 * &#47;&#47; Initialize the router client builder
 * final RouterClientBuilder builder = new RouterClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;;
 * &#47;&#47; Build the router client
 * RouterClient routerClient = builder.buildClient&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.communication.jobrouter.routerclient.instantiation -->
 *
 * <p>View {@link RouterClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see RouterClientBuilder
 */
@ServiceClient(builder = RouterClientBuilder.class, isAsync = false)
public final class RouterClient {

    private final RouterAsyncClient client;

    /**
     * Creates a RouterClient that sends requests to the job router service at {@code serviceEndpoint}. Each
     * service call goes through the {@code pipeline}.
     *
     * @param client The {@link RouterAsyncClient} that the client routes its request through.
     */
    RouterClient(RouterAsyncClient client) {
        this.client = client;
    }

    /**
     * Create a classification policy.
     *
     * @param createClassificationPolicyOptions Container for inputs to create a classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ClassificationPolicy createClassificationPolicy(CreateClassificationPolicyOptions createClassificationPolicyOptions) {
        return this.client.createClassificationPolicy(createClassificationPolicyOptions).block();
    }

    /**
     * Create a classification policy.
     *
     * @param createClassificationPolicyOptions Container for inputs to create a classification policy.
     * @param context The context to associate with this operation.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> createClassificationPolicyWithResponse(CreateClassificationPolicyOptions createClassificationPolicyOptions, Context context) {
        ClassificationPolicy classificationPolicy = ClassificationPolicyAdapter.convertCreateOptionsToClassificationPolicy(createClassificationPolicyOptions);
        return this.client.upsertClassificationPolicyWithResponse(createClassificationPolicyOptions.getId(), classificationPolicy, context).block();
    }

    /**
     * Update a classification policy.
     *
     * @param updateClassificationPolicyOptions Request options to update classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ClassificationPolicy updateClassificationPolicy(UpdateClassificationPolicyOptions updateClassificationPolicyOptions) {
        return this.client.updateClassificationPolicy(updateClassificationPolicyOptions).block();
    }

    /**
     * Update a classification policy.
     *
     * @param updateClassificationPolicyOptions Request options to update classification policy.
     * @param context The context to associate with this operation.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> updateClassificationPolicyWithResponse(UpdateClassificationPolicyOptions updateClassificationPolicyOptions, Context context) {
        ClassificationPolicy classificationPolicy = ClassificationPolicyAdapter.convertUpdateOptionsToClassificationPolicy(updateClassificationPolicyOptions);
        return this.client.upsertClassificationPolicyWithResponse(updateClassificationPolicyOptions.getId(), classificationPolicy, context).block();
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
    public ClassificationPolicy getClassificationPolicy(String id) {
        return this.client.getClassificationPolicy(id).block();
    }

    /**
     * Retrieves an existing classification policy by Id.
     *
     * @param id Id of the classification policy.
     * @param context The context to associate with this operation.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> getClassificationPolicyWithResponse(String id, Context context) {
        return this.client.getClassificationPolicyWithResponse(id, context).block();
    }

    /**
     * Delete a classification policy by Id.
     *
     * @param id Id of the classification policy.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteClassificationPolicy(String id) {
        this.client.deleteClassificationPolicy(id).block();
    }

    /**
     * Delete a classification policy by Id.
     *
     * @param classificationPolicyId Id of the classification policy.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteClassificationPolicyWithResponse(String classificationPolicyId, Context context) {
        return this.client.deleteClassificationPolicyWithResponse(classificationPolicyId, context).block();
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
    public PagedIterable<PagedClassificationPolicy> listClassificationPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listClassificationPolicies(maxPageSize));
    }

    /**
     * Retrieves existing classification policies.
     *
     * @return a paged collection of classification policies.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedClassificationPolicy> listClassificationPolicies() {
        return new PagedIterable<>(this.client.listClassificationPolicies());
    }

    /**
     * Create a distribution policy.
     *
     * @param createDistributionPolicyOptions Container for inputs to create a distribution policy.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DistributionPolicy createDistributionPolicy(CreateDistributionPolicyOptions createDistributionPolicyOptions) {
        return this.client.createDistributionPolicy(createDistributionPolicyOptions).block();
    }

    /**
     * Create a distribution policy.
     *
     * @param createDistributionPolicyOptions Container for inputs to create a distribution policy.
     * @param context The context to associate with this operation.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> createDistributionPolicyWithResponse(CreateDistributionPolicyOptions createDistributionPolicyOptions, Context context) {
        DistributionPolicy distributionPolicy = DistributionPolicyAdapter.convertCreateOptionsToDistributionPolicy(createDistributionPolicyOptions);
        return this.client.upsertDistributionPolicyWithResponse(createDistributionPolicyOptions.getId(), distributionPolicy, context).block();
    }

    /**
     * Update a distribution policy.
     *
     * @param updateDistributionPolicyOptions Request options to update distribution policy.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DistributionPolicy updateDistributionPolicy(UpdateDistributionPolicyOptions updateDistributionPolicyOptions) {
        return this.client.updateDistributionPolicy(updateDistributionPolicyOptions).block();
    }

    /**
     * Update a distribution policy.
     *
     * @param updateDistributionPolicyOptions Request options to update distribution policy.
     * @param context The context to associate with this operation.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> updateDistributionPolicyWithResponse(UpdateDistributionPolicyOptions updateDistributionPolicyOptions, Context context) {
        DistributionPolicy distributionPolicy = DistributionPolicyAdapter.convertUpdateOptionsToClassificationPolicy(updateDistributionPolicyOptions);
        return this.client.upsertDistributionPolicyWithResponse(updateDistributionPolicyOptions.getId(), distributionPolicy, context).block();
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
    public DistributionPolicy getDistributionPolicy(String distributionPolicyId) {
        return this.client.getDistributionPolicy(distributionPolicyId).block();
    }

    /**
     * Retrieves an existing distribution policy by Id.
     *
     * @param distributionPolicyId Id of the distribution policy.
     * @param context The context to associate with this operation.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> getDistributionPolicyWithResponse(String distributionPolicyId, Context context) {
        return this.client.getDistributionPolicyWithResponse(distributionPolicyId, context).block();
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
    public void deleteDistributionPolicy(String id) {
        this.client.deleteDistributionPolicy(id).block();
    }

    /**
     * Delete a distribution policy by Id.
     *
     * @param distributionPolicyId Id of the distribution policy.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDistributionPolicyWithResponse(String distributionPolicyId, Context context) {
        return this.client.deleteDistributionPolicyWithResponse(distributionPolicyId, context).block();
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
    public PagedIterable<PagedDistributionPolicy> listDistributionPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listDistributionPolicies(maxPageSize));
    }

    /**
     * Retrieves existing distribution policies.
     *
     * @return a paged collection of distribution policies.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedDistributionPolicy> listDistributionPolicies() {
        return new PagedIterable<>(this.client.listDistributionPolicies());
    }

    /**
     * Create an exception policy.
     *
     * @param createExceptionPolicyOptions Create options for Exception Policy.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ExceptionPolicy createExceptionPolicy(CreateExceptionPolicyOptions createExceptionPolicyOptions) {
        return this.client.createExceptionPolicy(createExceptionPolicyOptions).block();
    }

    /**
     * Create an exception policy.
     *
     * @param createExceptionPolicyOptions Create options for Exception Policy.
     * @param context The context to associate with this operation.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> createExceptionPolicyWithResponse(CreateExceptionPolicyOptions createExceptionPolicyOptions, Context context) {
        ExceptionPolicy exceptionPolicy = ExceptionPolicyAdapter.convertCreateOptionsToExceptionPolicy(createExceptionPolicyOptions);
        return this.client.upsertExceptionPolicyWithResponse(createExceptionPolicyOptions.getId(), exceptionPolicy, context).block();
    }

    /**
     * Update an exception policy.
     *
     * @param updateExceptionPolicyOptions Options to update ExceptionPolicy.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ExceptionPolicy updateExceptionPolicy(UpdateExceptionPolicyOptions updateExceptionPolicyOptions) {
        return this.client.updateExceptionPolicy(updateExceptionPolicyOptions).block();
    }

    /**
     * Update an exception policy.
     *
     * @param exceptionPolicyId Id of the exception policy.
     * @param exceptionPolicy Model of exception policy properties to be patched.
     * @param context The context to associate with this operation.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> updateExceptionPolicyWithResponse(String exceptionPolicyId, ExceptionPolicy exceptionPolicy, Context context) {
        return this.client.upsertExceptionPolicyWithResponse(exceptionPolicyId, exceptionPolicy, context).block();
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
    public ExceptionPolicy getExceptionPolicy(String exceptionPolicyId) {
        return this.client.getExceptionPolicy(exceptionPolicyId).block();
    }

    /**
     * Retrieves an existing exception policy by Id.
     *
     * @param exceptionPolicyId Id of the exception policy to retrieve.
     * @param context The context to associate with this operation.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> getExceptionPolicyWithResponse(String exceptionPolicyId, Context context) {
        return this.client.getExceptionPolicyWithResponse(exceptionPolicyId, context).block();
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
    public void deleteExceptionPolicy(String exceptionPolicyId) {
        this.client.deleteExceptionPolicy(exceptionPolicyId).block();
    }

    /**
     * Deletes a exception policy by Id.
     *
     * @param exceptionPolicyId Id of the exception policy to delete.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteExceptionPolicyWithResponse(String exceptionPolicyId, Context context) {
        return this.client.deleteExceptionPolicyWithResponse(exceptionPolicyId, context).block();
    }

    /**
     * Retrieves existing exception policies.
     *
     * @return a paged collection of exception policies.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedExceptionPolicy> listExceptionPolicies() {
        return new PagedIterable<>(this.client.listExceptionPolicies());
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
    public PagedIterable<PagedExceptionPolicy> listExceptionPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listExceptionPolicies(maxPageSize));
    }

    /**
     * Create a job.
     *
     * @param createJobOptions Options to create a RouterJob.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouterJob createJob(CreateJobOptions createJobOptions) {
        return this.client.createJob(createJobOptions).block();
    }

    /**
     * Create a job.
     *
     * @param createJobOptions Options to create a RouterJob.
     * @param context The context to associate with this operation.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> createJobWithResponse(CreateJobOptions createJobOptions, Context context) {
        RouterJob routerJob = JobAdapter.convertCreateJobOptionsToRouterJob(createJobOptions);
        return this.client.upsertJobWithResponse(createJobOptions.getId(), routerJob, context).block();
    }

    /**
     * Update a job.
     *
     * @param updateJobOptions Options to update RouterJob.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouterJob updateJob(UpdateJobOptions updateJobOptions) {
        return this.client.updateJob(updateJobOptions).block();
    }

    /**
     * Update a job.
     *
     * @param updateJobOptions Options to update RouterJob.
     * @param context The context to associate with this operation.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> updateJobWithResponse(UpdateJobOptions updateJobOptions, Context context) {
        RouterJob routerJob = JobAdapter.convertUpdateJobOptionsToRouterJob(updateJobOptions);
        return this.client.upsertJobWithResponse(updateJobOptions.getId(), routerJob, context).block();
    }

    /**
     * Retrieves an existing job by Id.
     *
     * @param jobId Id of the job to retrieve.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouterJob getJob(String jobId) {
        return this.client.getJob(jobId).block();
    }

    /**
     * Retrieves an existing job by Id.
     *
     * @param jobId Id of the job to retrieve.
     * @param context The context to associate with this operation.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> getJobWithResponse(String jobId, Context context) {
        return this.client.getJobWithResponse(jobId, context).block();
    }

    /**
     * Deletes a job and all of its traces.
     *
     * @param jobId Id of the job.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteJob(String jobId) {
        this.client.deleteJob(jobId).block();
    }

    /**
     * Deletes a job and all of its traces.
     *
     * @param jobId Id of the job.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteJobWithResponse(String jobId, Context context) {
        this.client.deleteJobWithResponse(jobId, context).block();
    }

    /**
     * Reclassify a job.
     *
     * @param jobId Id of the job.
     * @return ReclassifyJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReclassifyJobResult reclassifyJob(String jobId) {
        return this.client.reclassifyJob(jobId).block();
    }

    /**
     * Reclassify a job.
     *
     * @param jobId Id of the job.
     * @param context The context to associate with this operation.
     * @return ReclassifyJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ReclassifyJobResult> reclassifyJobWithResponse(String jobId, Context context) {
        return this.client.reclassifyJobWithResponse(jobId, context).block();
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     *
     * @param jobId Id of the job.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values. If not
     * provided, default value of "Cancelled" is set.
     * @return CancelJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CancelJobResult cancelJob(String jobId, String note, String dispositionCode) {
        return this.client.cancelJob(jobId, note, dispositionCode).block();
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     *
     * @param jobId Id of the job.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values. If not
     * provided, default value of "Cancelled" is set.
     * @param context The context to associate with this operation.
     * @return CancelJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CancelJobResult> cancelJobWithResponse(String jobId, String note, String dispositionCode, Context context) {
        return this.client.cancelJobWithResponse(jobId, note, dispositionCode, context).block();
    }

    /**
     * Completes an assigned job.
     *
     * @param jobId Id of the job.
     * @param assignmentId The assignment within the job to complete.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @return CompleteJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CompleteJobResult completeJob(String jobId, String assignmentId, String note) {
        return this.client.completeJob(jobId, assignmentId, note).block();
    }

    /**
     * Completes an assigned job.
     *
     * @param jobId Id of the job.
     * @param assignmentId The assignment within the job to complete.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param context The context to associate with this operation.
     * @return CompleteJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CompleteJobResult> completeJobWithResponse(String jobId, String assignmentId, String note, Context context) {
        return this.client.completeJobWithResponse(jobId, assignmentId, note, context).block();
    }

    /**
     * Closes a completed job.
     *
     * @param jobId Id of the job.
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
    public CloseJobResult closeJob(String jobId, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note) {
        return this.client.closeJob(jobId, assignmentId, dispositionCode, closeTime, note).block();
    }

    /**
     * Closes a completed job.
     *
     * @param jobId Id of the job.
     * @param assignmentId The assignment within which the job is to be closed.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values.
     * @param closeTime If not provided, worker capacity is released immediately along with a JobClosedEvent
     * notification. If provided, worker capacity is released along with a JobClosedEvent notification at a future
     * time.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param context The context to associate with this operation.
     * @return CloseJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CloseJobResult> closeJobWithResponse(String jobId, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note, Context context) {
        return this.client.closeJobWithResponse(jobId, assignmentId, dispositionCode, closeTime, note, context).block();
    }

    /**
     * Retrieves list of jobs based on filter parameters.
     *
     * @return a paged collection of jobs.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedJob> listJobs() {
        return new PagedIterable<>(this.client.listJobs());
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
    public PagedIterable<PagedJob> listJobs(JobStateSelector jobStateSelector, String queueId, String channelId, Integer maxPageSize) {
        return new PagedIterable<>(this.client.listJobs(jobStateSelector, queueId, channelId, maxPageSize));
    }

    /**
     * Gets a job's position details.
     *
     * @param jobId Id of the job.
     * @return a job's position details.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public JobPositionDetails getQueuePosition(String jobId) {
        return this.client.getQueuePosition(jobId).block();
    }

    /**
     * Gets a job's position details.
     *
     * @param jobId Id of the job.
     * @param context The context to associate with this operation.
     * @return a job's position details.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobPositionDetails> getQueuePositionWithResponse(String jobId, Context context) {
        return this.client.getQueuePositionWithResponse(jobId, context).block();
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
    public AcceptJobOfferResponse acceptJobOffer(String workerId, String offerId) {
        return this.client.acceptJobOffer(workerId, offerId).block();
    }

    /**
     * Accepts an offer to work on a job and returns a 409/Conflict if another agent accepted the job already.
     *
     * @param workerId Id of the worker.
     * @param offerId Id of the offer.
     * @param context The context to associate with this operation.
     * @return response containing Id's for the worker, job, and assignment from an accepted offer.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AcceptJobOfferResponse> acceptJobOfferWithResponse(String workerId, String offerId, Context context) {
        return this.client.acceptJobOfferWithResponse(workerId, offerId, context).block();
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
    public DeclineJobOfferResult declineJobOffer(String workerId, String offerId) {
        return this.client.declineJobOffer(workerId, offerId).block();
    }

    /**
     * Declines an offer to work on a job.
     *
     * @param workerId Id of the worker.
     * @param offerId Id of the offer.
     * @param context The context to associate with this operation.
     * @return DeclineJobOfferResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeclineJobOfferResult> declineJobOfferWithResponse(String workerId, String offerId, Context context) {
        return this.client.declineJobOfferWithResponse(workerId, offerId, context).block();
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
    public JobQueue createQueue(CreateQueueOptions createQueueOptions) {
        return this.client.createQueue(createQueueOptions).block();
    }

    /**
     * Create a queue.
     *
     * @param createQueueOptions Container for inputs to create a queue.
     * @param context The context to associate with this operation.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> createQueueWithResponse(CreateQueueOptions createQueueOptions, Context context) {
        JobQueue jobQueue = QueueAdapter.convertCreateQueueOptionsToJobQueue(createQueueOptions);
        return this.client.upsertQueueWithResponse(createQueueOptions.getQueueId(), jobQueue, context).block();
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
    public JobQueue updateQueue(UpdateQueueOptions updateQueueOptions) {
        return this.client.updateQueue(updateQueueOptions).block();
    }

    /**
     * Update a queue.
     *
     * @param updateQueueOptions Container for inputs to update a queue.
     * @param context The context to associate with this operation.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> updateQueueWithResponse(UpdateQueueOptions updateQueueOptions, Context context) {
        JobQueue jobQueue = QueueAdapter.convertUpdateQueueOptionsToJobQueue(updateQueueOptions);
        return this.client.upsertQueueWithResponse(updateQueueOptions.getQueueId(), jobQueue, context).block();
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
    public JobQueue getQueue(String queueId) {
        return this.client.getQueue(queueId).block();
    }

    /**
     * Retrieves an existing queue by Id.
     *
     * @param queueId Id of the queue to retrieve.
     * @param context The context to associate with this operation.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> getQueueWithResponse(String queueId, Context context) {
        return this.client.getQueueWithResponse(queueId, context).block();
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
    public void deleteQueue(String queueId) {
        this.client.deleteQueue(queueId).block();
    }

    /**
     * Deletes a queue by Id.
     *
     * @param queueId Id of the queue to delete.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteQueueWithResponse(String queueId, Context context) {
        return this.client.deleteQueueWithResponse(queueId, context).block();
    }

    /**
     * Retrieves existing queues.
     *
     * @return a paged collection of queues.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedQueue> listQueues() {
        return new PagedIterable<>(this.client.listQueues());
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
    public PagedIterable<PagedQueue> listQueues(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listQueues(maxPageSize));
    }

    /**
     * Retrieves a queue's statistics.
     *
     * @param queueId Id of the queue to retrieve statistics.
     * @return statistics for the queue.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueStatistics getQueueStatistics(String queueId) {
        return this.client.getQueueStatistics(queueId).block();
    }

    /**
     * Retrieves a queue's statistics.
     *
     * @param queueId Id of the queue to retrieve statistics.
     * @param context The context to associate with this operation.
     * @return statistics for the queue.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueStatistics> getQueueStatisticsWithResponse(String queueId, Context context) {
        return this.client.getQueueStatisticsWithResponse(queueId, context).block();
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
    public RouterWorker createWorker(CreateWorkerOptions createWorkerOptions) {
        return this.client.createWorker(createWorkerOptions).block();
    }

    /**
     * Create a worker.
     *
     * @param createWorkerOptions Container for inputs to create a worker.
     * @param context The context to associate with this operation.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> createWorkerWithResponse(CreateWorkerOptions createWorkerOptions, Context context) {
        RouterWorker routerWorker = WorkerAdapter.convertCreateWorkerOptionsToRouterWorker(createWorkerOptions);
        return this.client.upsertWorkerWithResponse(createWorkerOptions.getWorkerId(), routerWorker, context).block();
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
    public RouterWorker updateWorkerOptions(UpdateWorkerOptions updateWorkerOptions) {
        return this.client.updateWorker(updateWorkerOptions).block();
    }

    /**
     * Update a worker.
     *
     * @param updateWorkerOptions Container for inputs to update a worker.
     * @param context The context to associate with this operation.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> updateWorkerWithResponse(UpdateWorkerOptions updateWorkerOptions, Context context) {
        RouterWorker routerWorker = WorkerAdapter.convertUpdateWorkerOptionsToRouterWorker(updateWorkerOptions);
        return this.client.upsertWorkerWithResponse(updateWorkerOptions.getWorkerId(), routerWorker, context).block();
    }

    /**
     * Retrieves an existing worker by Id.
     *
     * @param workerId Id of the worker to retrieve.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouterWorker getWorker(String workerId) {
        return this.client.getWorker(workerId).block();
    }

    /**
     * Retrieves an existing worker by Id.
     *
     * @param workerId Id of the worker to retrieve.
     * @param context The context to associate with this operation.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> getWorkerWithResponse(String workerId, Context context) {
        return this.client.getWorkerWithResponse(workerId, context).block();
    }

    /**
     * Deletes a worker and all of its traces.
     *
     * @param workerId Id of the worker to delete.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteWorker(String workerId) {
        this.client.deleteWorker(workerId).block();
    }

    /**
     * Deletes a worker and all of its traces.
     *
     * @param workerId Id of the worker to delete.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWorkerWithResponse(String workerId, Context context) {
        return this.client.deleteWorkerWithResponse(workerId, context).block();
    }

    /**
     * Retrieves existing workers.
     *
     * @return a paged collection of workers.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedWorker> listWorkers() {
        return new PagedIterable<>(this.client.listWorkers());
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
    public PagedIterable<PagedWorker> listWorkers(WorkerStateSelector workerStateSelector, String channelId, String queueId, Boolean hasCapacity, Integer maxPageSize) {
        return new PagedIterable<>(this.client.listWorkers(workerStateSelector, channelId, queueId, hasCapacity, maxPageSize));
    }
}
