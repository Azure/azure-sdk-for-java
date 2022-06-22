// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.models.AcceptJobOfferResponse;
import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.jobrouter.implementation.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.implementation.models.JobPositionDetails;
import com.azure.communication.jobrouter.implementation.models.JobQueue;
import com.azure.communication.jobrouter.implementation.models.JobStateSelector;
import com.azure.communication.jobrouter.implementation.models.PagedClassificationPolicy;
import com.azure.communication.jobrouter.implementation.models.PagedDistributionPolicy;
import com.azure.communication.jobrouter.implementation.models.PagedExceptionPolicy;
import com.azure.communication.jobrouter.implementation.models.PagedJob;
import com.azure.communication.jobrouter.implementation.models.PagedQueue;
import com.azure.communication.jobrouter.implementation.models.PagedWorker;
import com.azure.communication.jobrouter.implementation.models.QueueStatistics;
import com.azure.communication.jobrouter.implementation.models.RouterJob;
import com.azure.communication.jobrouter.implementation.models.RouterWorker;
import com.azure.communication.jobrouter.implementation.models.WorkerStateSelector;
import com.azure.communication.jobrouter.implementation.convertors.ClassificationPolicyAdapter;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.implementation.convertors.ExceptionPolicyAdapter;
import com.azure.communication.jobrouter.models.UpdateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateExceptionPolicyOptions;
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
    public Void deleteClassificationPolicy(String id) {
        return this.client.deleteClassificationPolicy(id).block();
    }

    /**
     * Delete a classification policy by Id.
     *
     * @param id Id of the classification policy.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteClassificationPolicyWithResponse(String id, Context context) {
        return this.client.deleteClassificationPolicyWithResponse(id, context).block();
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
     * @param id Id of the distribution policy.
     * @param distributionPolicy Model of distribution policy properties to be patched.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DistributionPolicy createDistributionPolicy(String id, DistributionPolicy distributionPolicy) {
        return this.client.createDistributionPolicy(id, distributionPolicy).block();
    }

    /**
     * Create a distribution policy.
     *
     * @param id Id of the distribution policy.
     * @param distributionPolicy Model of distribution policy properties to be patched.
     * @param context The context to associate with this operation.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> createDistributionPolicyWithResponse(String id, DistributionPolicy distributionPolicy, Context context) {
        return this.client.upsertDistributionPolicyWithResponse(id, distributionPolicy, context).block();
    }

    /**
     * Update a distribution policy.
     *
     * @param id Id of the distribution policy.
     * @param distributionPolicy Model of distribution policy properties to be patched.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DistributionPolicy updateDistributionPolicy(String id, DistributionPolicy distributionPolicy) {
        return this.client.createDistributionPolicy(id, distributionPolicy).block();
    }

    /**
     * Update a distribution policy.
     *
     * @param id Id of the distribution policy.
     * @param distributionPolicy Model of distribution policy properties to be patched.
     * @param context The context to associate with this operation.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> updateDistributionPolicyWithResponse(String id, DistributionPolicy distributionPolicy, Context context) {
        return this.client.upsertDistributionPolicyWithResponse(id, distributionPolicy, context).block();
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
    public DistributionPolicy getDistributionPolicy(String id) {
        return this.client.getDistributionPolicy(id).block();
    }

    /**
     * Retrieves an existing distribution policy by Id.
     *
     * @param id Id of the distribution policy.
     * @param context The context to associate with this operation.
     * @return policy governing how jobs are distributed to workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> getDistributionPolicyWithResponse(String id, Context context) {
        return this.client.getDistributionPolicyWithResponse(id, context).block();
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
    public Void deleteDistributionPolicy(String id) {
        return this.client.deleteDistributionPolicy(id).block();
    }

    /**
     * Delete a distribution policy by Id.
     *
     * @param id Id of the distribution policy.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDistributionPolicyWithResponse(String id, Context context) {
        return this.client.deleteDistributionPolicyWithResponse(id, context).block();
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
     * @param id Id of the exception policy.
     * @param exceptionPolicy Model of exception policy properties to be patched.
     * @param context The context to associate with this operation.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> updateExceptionPolicyWithResponse(String id, ExceptionPolicy exceptionPolicy, Context context) {
        return this.client.upsertExceptionPolicyWithResponse(id, exceptionPolicy, context).block();
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
    public ExceptionPolicy getExceptionPolicy(String id) {
        return this.client.getExceptionPolicy(id).block();
    }

    /**
     * Retrieves an existing exception policy by Id.
     *
     * @param id Id of the exception policy to retrieve.
     * @param context The context to associate with this operation.
     * @return a policy that defines actions to execute when exception are triggered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> getExceptionPolicyWithResponse(String id, Context context) {
        return this.client.getExceptionPolicyWithResponse(id, context).block();
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
    public Void deleteExceptionPolicy(String id) {
        return this.client.deleteExceptionPolicy(id).block();
    }

    /**
     * Deletes a exception policy by Id.
     *
     * @param id Id of the exception policy to delete.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteExceptionPolicyWithResponse(String id, Context context) {
        return this.client.deleteExceptionPolicyWithResponse(id, context).block();
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
     * @param id Id of the job.
     * @param routerJob Model of job properties to be created or patched.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouterJob createJob(String id, RouterJob routerJob) {
        return this.client.createJob(id, routerJob).block();
    }

    /**
     * Create a job.
     *
     * @param id Id of the job.
     * @param routerJob Model of job properties to be created or patched.
     * @param context The context to associate with this operation.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> createJobWithResponse(String id, RouterJob routerJob, Context context) {
        return this.client.upsertJobWithResponse(id, routerJob, context).block();
    }

    /**
     * Update a job.
     *
     * @param id Id of the job.
     * @param routerJob Model of job properties to be created or patched.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouterJob updateJob(String id, RouterJob routerJob) {
        return this.client.createJob(id, routerJob).block();
    }

    /**
     * Update a job.
     *
     * @param id Id of the job.
     * @param routerJob Model of job properties to be created or patched.
     * @param context The context to associate with this operation.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> updateJobWithResponse(String id, RouterJob routerJob, Context context) {
        return this.client.upsertJobWithResponse(id, routerJob, context).block();
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
    public RouterJob getJob(String id) {
        return this.client.getJob(id).block();
    }

    /**
     * Retrieves an existing job by Id.
     *
     * @param id Id of the job to retrieve.
     * @param context The context to associate with this operation.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> getJobWithResponse(String id, Context context) {
        return this.client.getJobWithResponse(id, context).block();
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
    public Void deleteJob(String id) {
        return this.client.deleteJob(id).block();
    }

    /**
     * Deletes a job and all of its traces.
     *
     * @param id Id of the job.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteJobWithResponse(String id, Context context) {
        return this.client.deleteJobWithResponse(id, context).block();
    }

    /**
     * Reclassify a job.
     *
     * @param id Id of the job.
     * @param reclassifyJobRequest Request object for reclassifying a job.
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Object reclassifyJob(String id, Object reclassifyJobRequest) {
        return this.client.reclassifyJob(id, reclassifyJobRequest).block();
    }

    /**
     * Reclassify a job.
     *
     * @param id Id of the job.
     * @param reclassifyJobRequest Request object for reclassifying a job.
     * @param context The context to associate with this operation.
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> reclassifyJobWithResponse(String id, Object reclassifyJobRequest, Context context) {
        return this.client.reclassifyJobWithResponse(id, reclassifyJobRequest, context).block();
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     *
     * @param id Id of the job.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values. If not
     * provided, default value of "Cancelled" is set.
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Object cancelJob(String id, String note, String dispositionCode) {
        return this.client.cancelJob(id, note, dispositionCode).block();
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     *
     * @param id Id of the job.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values. If not
     * provided, default value of "Cancelled" is set.
     * @param context The context to associate with this operation.
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> cancelJobWithResponse(String id, String note, String dispositionCode, Context context) {
        return this.client.cancelJobWithResponse(id, note, dispositionCode, context).block();
    }

    /**
     * Completes an assigned job.
     *
     * @param id Id of the job.
     * @param assignmentId The assignment within the job to complete.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Object completeJob(String id, String assignmentId, String note) {
        return this.client.completeJob(id, assignmentId, note).block();
    }

    /**
     * Completes an assigned job.
     *
     * @param id Id of the job.
     * @param assignmentId The assignment within the job to complete.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param context The context to associate with this operation.
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> completeJobWithResponse(String id, String assignmentId, String note, Context context) {
        return this.client.completeJobWithResponse(id, assignmentId, note, context).block();
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
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Object closeJob(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note) {
        return this.client.closeJob(id, assignmentId, dispositionCode, closeTime, note).block();
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
     * @param context The context to associate with this operation.
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> closeJobWithResponse(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note, Context context) {
        return this.client.closeJobWithResponse(id, assignmentId, dispositionCode, closeTime, note, context).block();
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
     * @param id Id of the job.
     * @return a job's position details.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public JobPositionDetails getInQueuePosition(String id) {
        return this.client.getInQueuePosition(id).block();
    }

    /**
     * Gets a job's position details.
     *
     * @param id Id of the job.
     * @param context The context to associate with this operation.
     * @return a job's position details.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobPositionDetails> getInQueuePositionWithResponse(String id, Context context) {
        return this.client.getInQueuePositionWithResponse(id, context).block();
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
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Object declineJobOffer(String workerId, String offerId) {
        return this.client.declineJobOffer(workerId, offerId).block();
    }

    /**
     * Declines an offer to work on a job.
     *
     * @param workerId Id of the worker.
     * @param offerId Id of the offer.
     * @param context The context to associate with this operation.
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> declineJobOfferWithResponse(String workerId, String offerId, Context context) {
        return this.client.declineJobOfferWithResponse(workerId, offerId, context).block();
    }

    /**
     * Create a queue.
     *
     * @param id Id of the queue.
     * @param jobQueue Model of queue properties to be patched. See also: https://datatracker.ietf.org/doc/html/rfc7386.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public JobQueue createQueue(String id, JobQueue jobQueue) {
        return this.client.createQueue(id, jobQueue).block();
    }

    /**
     * Create a queue.
     *
     * @param id Id of the queue.
     * @param jobQueue Model of queue properties to be patched. See also: https://datatracker.ietf.org/doc/html/rfc7386.
     * @param context The context to associate with this operation.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> createQueueWithResponse(String id, JobQueue jobQueue, Context context) {
        return this.client.upsertQueueWithResponse(id, jobQueue, context).block();
    }

    /**
     * Update a queue.
     *
     * @param id Id of the queue.
     * @param jobQueue Model of queue properties to be patched. See also: https://datatracker.ietf.org/doc/html/rfc7386.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public JobQueue updateQueue(String id, JobQueue jobQueue) {
        return this.client.createQueue(id, jobQueue).block();
    }

    /**
     * Update a queue.
     *
     * @param id Id of the queue.
     * @param jobQueue Model of queue properties to be patched. See also: https://datatracker.ietf.org/doc/html/rfc7386.
     * @param context The context to associate with this operation.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> updateQueueWithResponse(String id, JobQueue jobQueue, Context context) {
        return this.client.upsertQueueWithResponse(id, jobQueue, context).block();
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
    public JobQueue getQueue(String id) {
        return this.client.getQueue(id).block();
    }

    /**
     * Retrieves an existing queue by Id.
     *
     * @param id Id of the queue to retrieve.
     * @param context The context to associate with this operation.
     * @return a queue that can contain jobs to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> getQueueWithResponse(String id, Context context) {
        return this.client.getQueueWithResponse(id, context).block();
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
    public Void deleteQueue(String id) {
        return this.client.deleteQueue(id).block();
    }

    /**
     * Deletes a queue by Id.
     *
     * @param id Id of the queue to delete.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteQueueWithResponse(String id, Context context) {
        return this.client.deleteQueueWithResponse(id, context).block();
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
     * @param id Id of the queue to retrieve statistics.
     * @return statistics for the queue.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public QueueStatistics getQueueStatistics(String id) {
        return this.client.getQueueStatistics(id).block();
    }

    /**
     * Retrieves a queue's statistics.
     *
     * @param id Id of the queue to retrieve statistics.
     * @param context The context to associate with this operation.
     * @return statistics for the queue.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueStatistics> getQueueStatisticsWithResponse(String id, Context context) {
        return this.client.getQueueStatisticsWithResponse(id, context).block();
    }

    /**
     * Create a worker.
     *
     * @param id Id of the worker.
     * @param routerWorker Model of worker properties to be patched.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouterWorker createWorker(String id, RouterWorker routerWorker) {
        return this.client.createWorker(id, routerWorker).block();
    }

    /**
     * Create a worker.
     *
     * @param id Id of the worker.
     * @param routerWorker Model of worker properties to be patched.
     * @param context The context to associate with this operation.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> createWorkerWithResponse(String id, RouterWorker routerWorker, Context context) {
        return this.client.upsertWorkerWithResponse(id, routerWorker, context).block();
    }

    /**
     * Update a worker.
     *
     * @param id Id of the worker.
     * @param routerWorker Model of worker properties to be patched.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouterWorker updateWorker(String id, RouterWorker routerWorker) {
        return this.client.createWorker(id, routerWorker).block();
    }

    /**
     * Update a worker.
     *
     * @param id Id of the worker.
     * @param routerWorker Model of worker properties to be patched.
     * @param context The context to associate with this operation.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> updateWorkerWithResponse(String id, RouterWorker routerWorker, Context context) {
        return this.client.upsertWorkerWithResponse(id, routerWorker, context).block();
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
    public RouterWorker getWorker(String id) {
        return this.client.getWorker(id).block();
    }

    /**
     * Retrieves an existing worker by Id.
     *
     * @param id Id of the worker to retrieve.
     * @param context The context to associate with this operation.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> getWorkerWithResponse(String id, Context context) {
        return this.client.getWorkerWithResponse(id, context).block();
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
    public Void deleteWorker(String id) {
        return this.client.deleteWorker(id).block();
    }

    /**
     * Deletes a worker and all of its traces.
     *
     * @param id Id of the worker to delete.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWorkerWithResponse(String id, Context context) {
        return this.client.deleteWorkerWithResponse(id, context).block();
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
