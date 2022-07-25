// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.convertors.JobAdapter;
import com.azure.communication.jobrouter.implementation.convertors.WorkerAdapter;
import com.azure.communication.jobrouter.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.jobrouter.models.AcceptJobOfferResult;
import com.azure.communication.jobrouter.models.CancelJobResult;
import com.azure.communication.jobrouter.models.CloseJobResult;
import com.azure.communication.jobrouter.models.CompleteJobResult;
import com.azure.communication.jobrouter.models.DeclineJobOfferResult;
import com.azure.communication.jobrouter.models.JobPositionDetails;
import com.azure.communication.jobrouter.models.JobStateSelector;
import com.azure.communication.jobrouter.models.QueueStatistics;
import com.azure.communication.jobrouter.models.ReclassifyJobResult;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterJobItem;
import com.azure.communication.jobrouter.models.RouterWorker;
import com.azure.communication.jobrouter.models.RouterWorkerItem;
import com.azure.communication.jobrouter.models.WorkerStateSelector;
import com.azure.communication.jobrouter.models.options.CloseJobOptions;
import com.azure.communication.jobrouter.models.options.CreateJobOptions;
import com.azure.communication.jobrouter.models.options.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.options.UpdateJobOptions;
import com.azure.communication.jobrouter.models.options.UpdateWorkerOptions;
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
        return this.client.deleteJobWithResponse(jobId, context).block();
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
     * @param closeJobOptions Options object for close job operation.
     * @return CloseJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CloseJobResult closeJob(CloseJobOptions closeJobOptions) {
        return this.client.closeJob(closeJobOptions).block();
    }

    /**
     * Closes a completed job.
     *
     * @param closeJobOptions Options object for close job operation.
     * @param context The context to associate with this operation.
     * @return CloseJobResult.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CloseJobResult> closeJobWithResponse(CloseJobOptions closeJobOptions, Context context) {
        return this.client.closeJobWithResponse(closeJobOptions, context).block();
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
     * @param jobStateSelector (Optional) If specified, filter jobs by status.
     * @param queueId (Optional) If specified, filter jobs by queue.
     * @param channelId (Optional) If specified, filter jobs by channel.
     * @param classificationPolicyId (Optional) If specified, filter jobs by classificationPolicyId.
     * @param maxPageSize Number of objects to return per page.
     * @return a paged collection of jobs.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<RouterJobItem> listJobs(JobStateSelector jobStateSelector, String queueId, String channelId, String classificationPolicyId, Integer maxPageSize) {
        return new PagedIterable<>(this.client.listJobs(jobStateSelector, queueId, channelId, classificationPolicyId, maxPageSize));
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
    public PagedIterable<RouterWorkerItem> listWorkers(WorkerStateSelector workerStateSelector, String channelId, String queueId, Boolean hasCapacity, Integer maxPageSize) {
        return new PagedIterable<>(this.client.listWorkers(workerStateSelector, channelId, queueId, hasCapacity, maxPageSize));
    }
}
