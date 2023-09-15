// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.converters.JobAdapter;
import com.azure.communication.jobrouter.implementation.converters.WorkerAdapter;
import com.azure.communication.jobrouter.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.jobrouter.implementation.models.RouterJobInternal;
import com.azure.communication.jobrouter.implementation.models.RouterWorkerInternal;
import com.azure.communication.jobrouter.models.AcceptJobOfferResult;
import com.azure.communication.jobrouter.models.CancelJobOptions;
import com.azure.communication.jobrouter.models.CloseJobOptions;
import com.azure.communication.jobrouter.models.CompleteJobOptions;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.CreateJobWithClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DeclineJobOfferOptions;
import com.azure.communication.jobrouter.models.ListJobsOptions;
import com.azure.communication.jobrouter.models.ListWorkersOptions;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterJobItem;
import com.azure.communication.jobrouter.models.RouterJobPositionDetails;
import com.azure.communication.jobrouter.models.RouterQueueStatistics;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.RouterWorkerItem;
import com.azure.communication.jobrouter.models.UnassignJobOptions;
import com.azure.communication.jobrouter.models.UnassignJobResult;
import com.azure.communication.jobrouter.models.UpdateJobOptions;
import com.azure.communication.jobrouter.models.UpdateWorkerOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Sync Client that supports job router operations.
 *
 * <p><strong>Instantiating a synchronous JobRouter Client</strong></p>
 * <!-- src_embed com.azure.communication.jobrouter.jobrouterclient.instantiation -->
 * <pre>
 * &#47;&#47; Initialize the jobrouter client builder
 * final JobRouterClientBuilder builder = new JobRouterClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;;
 * &#47;&#47; Build the jobrouter client
 * JobRouterClient jobRouterClient = builder.buildClient&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.communication.jobrouter.jobrouterclient.instantiation -->
 *
 * <p>View {@link JobRouterClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see JobRouterClientBuilder
 */
@ServiceClient(builder = JobRouterClientBuilder.class, isAsync = false)
public final class JobRouterClient {

    private final JobRouterAsyncClient client;

    /**
     * Creates a RouterClient that sends requests to the job router service at {@code serviceEndpoint}. Each
     * service call goes through the {@code pipeline}.
     *
     * @param client The {@link JobRouterAsyncClient} that the client routes its request through.
     */
    JobRouterClient(JobRouterAsyncClient client) {
        this.client = client;
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
     * Create a job with classification policy.
     *
     * @param createJobWithClassificationPolicyOptions Options to create a RouterJob.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouterJob createJobWithClassificationPolicy(CreateJobWithClassificationPolicyOptions createJobWithClassificationPolicyOptions) {
        return this.client.createJobWithClassificationPolicy(createJobWithClassificationPolicyOptions).block();
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
        RouterJobInternal routerJobInternal = JobAdapter.convertCreateJobOptionsToRouterJob(createJobOptions);
        return this.client.upsertJobWithResponse(createJobOptions.getId(), routerJobInternal, context).block();
    }

    /**
     * Create a job with classification policy.
     *
     * @param createJobWithClassificationPolicyOptions Options to create a RouterJob.
     * @param context The context to associate with this operation.
     * @return a unit of work to be routed.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> createJobWithClassificationPolicyWithResponse(CreateJobWithClassificationPolicyOptions createJobWithClassificationPolicyOptions, Context context) {
        RouterJobInternal routerJobInternal = JobAdapter.convertCreateJobWithClassificationPolicyOptionsToRouterJob(createJobWithClassificationPolicyOptions);
        return this.client.upsertJobWithResponse(createJobWithClassificationPolicyOptions.getId(), routerJobInternal, context).block();
    }

    /**
     * Update a job.
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
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
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
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
        RouterJobInternal routerJob = JobAdapter.convertUpdateJobOptionsToRouterJob(updateJobOptions);
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
     * @return void.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteJobWithResponse(String jobId, Context context) {
        return this.client.deleteJobWithResponse(jobId, context).block();
    }

    /**
     * Reclassify a job.
     *
     * @param jobId Id of the job.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void reclassifyJob(String jobId) {
        this.client.reclassifyJob(jobId).block();
    }

    /**
     * Reclassify a job.
     *
     * @param jobId Id of the job.
     * @param context The context to associate with this operation.
     * @return void.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> reclassifyJobWithResponse(String jobId, Context context) {
        return this.client.reclassifyJobWithResponse(jobId, context).block();
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     *
     * @param cancelJobOptions options object for cancelJob operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void cancelJob(CancelJobOptions cancelJobOptions) {
        this.client.cancelJob(cancelJobOptions).block();
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     *
     * @param cancelJobOptions options object for cancelJob operation.
     * @param context The context to associate with this operation.
     * @return void.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelJobWithResponse(CancelJobOptions cancelJobOptions, Context context) {
        return this.client.cancelJobWithResponse(
            cancelJobOptions.getJobId(),
            cancelJobOptions.getNote(),
            cancelJobOptions.getDispositionCode(),
            context).block();
    }

    /**
     * Completes an assigned job.
     *
     * @param completeJobOptions options object for completeJob.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void completeJob(CompleteJobOptions completeJobOptions) {
        this.client.completeJob(completeJobOptions).block();
    }

    /**
     * Completes an assigned job.
     *
     * @param jobId Id of the job.
     * @param assignmentId The assignment within the job to complete.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param context The context to associate with this operation.
     * @return void.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> completeJobWithResponse(String jobId, String assignmentId, String note, Context context) {
        return this.client.completeJobWithResponse(jobId, assignmentId, note, context).block();
    }

    /**
     * Closes a completed job.
     *
     * @param closeJobOptions Options object for close job operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void closeJob(CloseJobOptions closeJobOptions) {
        this.client.closeJob(closeJobOptions).block();
    }

    /**
     * Closes a completed job.
     *
     * @param closeJobOptions Options object for close job operation.
     * @param context The context to associate with this operation.
     * @return void.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> closeJobWithResponse(CloseJobOptions closeJobOptions, Context context) {
        return this.client.closeJobWithResponse(closeJobOptions, context).block();
    }

    /**
     * Unassigns a job from assigned worker.
     *
     * @param unassignJobOptions Options object for close job operation.
     * @return UnassignJobResult
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UnassignJobResult unassignJob(UnassignJobOptions unassignJobOptions) {
        return this.client.unassignJob(unassignJobOptions).block();
    }

    /**
     * Unassigns a job from assigned worker.
     *
     * @param unassignJobOptions Options object for close job operation.
     * @param context The context to associate with this operation.
     * @return void.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UnassignJobResult> unassignJobWithResponse(UnassignJobOptions unassignJobOptions, Context context) {
        return this.client.unassignJobWithResponse(unassignJobOptions, context).block();
    }

    /**
     * Retrieves list of jobs based on filter parameters.
     *
     * @return a paged collection of jobs.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<RouterJobItem> listJobs() {
        return new PagedIterable<>(this.client.listJobs());
    }

    /**
     * Retrieves list of jobs based on filter parameters.
     *
     * @param listJobsOptions options for listJobs.
     * @return a paged collection of jobs.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<RouterJobItem> listJobs(ListJobsOptions listJobsOptions) {
        return new PagedIterable<>(this.client.listJobs(listJobsOptions));
    }

    /**
     * Retrieves list of jobs based on filter parameters.
     *
     * @param listJobsOptions options for listJobs.
     * @param context Context for listJobs.
     * @return a paged collection of jobs.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<RouterJobItem> listJobs(ListJobsOptions listJobsOptions, Context context) {
        return new PagedIterable<>(this.client.listJobs(listJobsOptions, context));
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
    public RouterJobPositionDetails getQueuePosition(String jobId) {
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
    public Response<RouterJobPositionDetails> getQueuePositionWithResponse(String jobId, Context context) {
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
    public AcceptJobOfferResult acceptJobOffer(String workerId, String offerId) {
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
    public Response<AcceptJobOfferResult> acceptJobOfferWithResponse(String workerId, String offerId, Context context) {
        return this.client.acceptJobOfferWithResponse(workerId, offerId, context).block();
    }

    /**
     * Declines an offer to work on a job.
     *
     * @param options Options for declining the job offer.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void declineJobOffer(DeclineJobOfferOptions options) {
        this.client.declineJobOffer(options).block();
    }

    /**
     * Declines an offer to work on a job.
     *
     * @param options Options for declining the job offer.
     * @param context The context to associate with this operation.
     * @return void.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> declineJobOfferWithResponse(DeclineJobOfferOptions options, Context context) {
        return this.client.declineJobOfferWithResponse(options, context).block();
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
    public RouterQueueStatistics getQueueStatistics(String queueId) {
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
    public Response<RouterQueueStatistics> getQueueStatisticsWithResponse(String queueId, Context context) {
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
        RouterWorkerInternal routerWorker = WorkerAdapter.convertCreateWorkerOptionsToRouterWorker(createWorkerOptions);
        return this.client.upsertWorkerWithResponse(createWorkerOptions.getWorkerId(), routerWorker, context).block();
    }

    /**
     * Update a worker.
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
     *
     * @param updateWorkerOptions Container for inputs to update a worker.
     * @return an entity for jobs to be routed to.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouterWorker updateWorker(UpdateWorkerOptions updateWorkerOptions) {
        return this.client.updateWorker(updateWorkerOptions).block();
    }

    /**
     * Update a worker.
     * Follows https://www.rfc-editor.org/rfc/rfc7386.
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
        RouterWorkerInternal routerWorker = WorkerAdapter.convertUpdateWorkerOptionsToRouterWorker(updateWorkerOptions);
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
    public PagedIterable<RouterWorkerItem> listWorkers() {
        return new PagedIterable<>(this.client.listWorkers());
    }

    /**
     * Retrieves existing workers.
     *
     * @param listWorkersOptions options for listWorkers.
     * @return a paged collection of workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<RouterWorkerItem> listWorkers(ListWorkersOptions listWorkersOptions) {
        return new PagedIterable<>(this.client.listWorkers(listWorkersOptions));
    }

    /**
     * Retrieves existing workers.
     *
     * @param listWorkersOptions options for listWorkers.
     * @param context Context for listWorkers.
     * @return a paged collection of workers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<RouterWorkerItem> listWorkers(ListWorkersOptions listWorkersOptions, Context context) {
        return new PagedIterable<>(this.client.listWorkers(listWorkersOptions, context));
    }
}
