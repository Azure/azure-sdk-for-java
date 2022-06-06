package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.*;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;

/**
 * Sync Client that supports job router operations.
 *
 * <p><strong>Instantiating a synchronous JobRouter Client</strong></p>
 *
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
public class RouterClient {

    private final ClientLogger logger = new ClientLogger(RouterClient.class);

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
     * Creates or updates classification policy.
     * @param id
     * @param classificationPolicy
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> upsertClassificationPolicy(String id, ClassificationPolicy classificationPolicy) {
        return this.upsertClassificationPolicyWithResponse(id, classificationPolicy, null);
    }

    /**
     * Creates or updates classification policy.
     * @param id
     * @param classificationPolicy
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> upsertClassificationPolicyWithResponse(String id, ClassificationPolicy classificationPolicy, Context context) {
        return this.client.upsertClassificationPolicyWithResponse(id, classificationPolicy, context).block();
    }

    /**
     * Retrieves an existing classification policy by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> getClassificationPolicy(String id) {
        return this.getClassificationPolicyWithResponse(id, null);
    }

    /**
     * Retrieves an existing classification policy by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> getClassificationPolicyWithResponse(String id, Context context) {
        return this.client.getClassificationPolicyWithResponse(id, context).block();
    }

    /**
     * Deletes a Classification Policy by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteClassificationPolicy(String id) {
        return this.deleteClassificationPolicyWithResponse(id, null);
    }

    /**
     * Deletes a Classification Policy by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteClassificationPolicyWithResponse(String id, Context context) {
        return this.client.deleteClassificationPolicyWithResponse(id, context).block();
    }

    /**
     * Retrieves existing classification policies.
     * @param maxPageSize
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedClassificationPolicy> listClassificationPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listClassificationPolicies(maxPageSize));
    }

    /**
     * Retrieves existing classification policies.
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedClassificationPolicy> listClassificationPolicies() {
        return new PagedIterable<>(this.client.listClassificationPolicies());
    }

    /**
     * Creates or updates a distribution policy.
     * @param id
     * @param distributionPolicy
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> upsertDistributionPolicy(String id, DistributionPolicy distributionPolicy) {
        return this.upsertDistributionPolicyWithResponse(id, distributionPolicy, null);
    }

    /**
     * Creates or updates a distribution policy.
     * @param id
     * @param distributionPolicy
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> upsertDistributionPolicyWithResponse(String id, DistributionPolicy distributionPolicy, Context context) {
        return this.client.upsertDistributionPolicyWithResponse(id, distributionPolicy, context).block();
    }

    /**
     * Retrieves an existing distribution policy by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> getDistributionPolicy(String id) {
        return this.getDistributionPolicyWithResponse(id, null);
    }

    /**
     * Retrieves an existing distribution policy by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> getDistributionPolicyWithResponse(String id, Context context) {
        return this.client.getDistributionPolicyWithResponse(id, context).block();
    }

    /**
     * Delete a distribution policy by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDistributionPolicy(String id) {
        return this.deleteDistributionPolicyWithResponse(id, null);
    }

    /**
     * Delete a distribution policy by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDistributionPolicyWithResponse(String id, Context context) {
        return this.client.deleteDistributionPolicyWithResponse(id, context).block();
    }

    /**
     * Retrieves existing distribution policies.
     * @param maxPageSize
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedDistributionPolicy> listDistributionPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listDistributionPolicies(maxPageSize));
    }

    /**
     * Retrieves existing distribution policies.
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedDistributionPolicy> listDistributionPolicies() {
        return new PagedIterable<>(this.client.listDistributionPolicies());
    }

    /**
     * Creates or updates a exception policy.
     * @param id
     * @param exceptionPolicy
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> upsertExceptionPolicy(String id, ExceptionPolicy exceptionPolicy) {
        return this.upsertExceptionPolicyWithResponse(id, exceptionPolicy, null);
    }

    /**
     * Creates or updates a exception policy.
     * @param id
     * @param exceptionPolicy
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> upsertExceptionPolicyWithResponse(String id, ExceptionPolicy exceptionPolicy, Context context) {
        return this.client.upsertExceptionPolicyWithResponse(id, exceptionPolicy, context).block();
    }

    /**
     * Retrieves an existing exception policy by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> getExceptionPolicy(String id) {
        return this.getExceptionPolicyWithResponse(id, null);
    }

    /**
     * Retrieves an existing exception policy by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> getExceptionPolicyWithResponse(String id, Context context) {
        return this.client.getExceptionPolicyWithResponse(id, context).block();
    }

    /**
     * Deletes a exception policy by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteExceptionPolicy(String id) {
        return this.deleteExceptionPolicyWithResponse(id, null);
    }

    /**
     * Deletes a exception policy by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteExceptionPolicyWithResponse(String id, Context context) {
        return this.client.deleteExceptionPolicyWithResponse(id, context).block();
    }

    /**
     * Retrieves existing exception policies.
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedExceptionPolicy> listExceptionPolicies() {
        return new PagedIterable<>(this.client.listExceptionPolicies());
    }

    /**
     * Retrieves existing exception policies.
     * @param maxPageSize
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedExceptionPolicy> listExceptionPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listExceptionPolicies(maxPageSize));
    }

    /**
     * Creates or updates a job to be routed.
     * @param id
     * @param routerJob
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> upsertJob(String id, RouterJob routerJob) {
        return this.upsertJobWithResponse(id, routerJob, null);
    }

    /**
     * Creates or updates a job to be routed.
     * @param id
     * @param routerJob
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> upsertJobWithResponse(String id, RouterJob routerJob, Context context) {
        return this.client.upsertJobWithResponse(id, routerJob, context).block();
    }

    /**
     * Retrieves an existing job by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> getJob(String id) {
        return this.getJobWithResponse(id, null);
    }

    /**
     * Retrieves an existing job by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> getJobWithResponse(String id, Context context) {
        return this.client.getJobWithResponse(id, context).block();
    }

    /**
     * Deletes an existing job by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteJob(String id) {
        return this.deleteJobWithResponse(id, null);
    }

    /**
     * Deletes an existing job by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteJobWithResponse(String id, Context context) {
        return this.client.deleteJobWithResponse(id, context).block();
    }

    /**
     * Updates an existing job by Id and forcing it to be reclassified.
     * @param id
     * @param reclassifyJobRequest
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> reclassifyJobAction(String id, Object reclassifyJobRequest) {
        return this.reclassifyJobActionWithResponse(id, reclassifyJobRequest, null);
    }

    /**
     * Updates an existing job by Id and forcing it to be reclassified.
     * @param id
     * @param reclassifyJobRequest
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> reclassifyJobActionWithResponse(String id, Object reclassifyJobRequest, Context context) {
        return this.client.reclassifyJobActionWithResponse(id, reclassifyJobRequest, context).block();
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     * @param id
     * @param note
     * @param dispositionCode
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> cancelJobAction(String id, String note, String dispositionCode) {
        return this.cancelJobActionWithResponse(id, note, dispositionCode, null);
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     * @param id
     * @param note
     * @param dispositionCode
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> cancelJobActionWithResponse(String id, String note, String dispositionCode, Context context) {
        return this.client.cancelJobActionWithResponse(id, note, dispositionCode, context).block();
    }

    /**
     * Completes an assigned job.
     * @param id
     * @param assignmentId
     * @param note
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> completeJobAction(String id, String assignmentId, String note) {
        return this.completeJobActionWithResponse(id, assignmentId, note, null);
    }

    /**
     * Completes an assigned job.
     * @param id
     * @param assignmentId
     * @param note
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> completeJobActionWithResponse(String id, String assignmentId, String note, Context context) {
        return this.client.completeJobActionWithResponse(id, assignmentId, note, context).block();
    }

    /**
     * Closes a completed job.
     * @param id
     * @param assignmentId
     * @param dispositionCode
     * @param closeTime
     * @param note
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> closeJobAction(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note) {
        return this.closeJobActionWithResponse(id, assignmentId, dispositionCode, closeTime, note, null);
    }

    /**
     * Closes a completed job.
     * @param id
     * @param assignmentId
     * @param dispositionCode
     * @param closeTime
     * @param note
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> closeJobActionWithResponse(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note, Context context) {
        return this.client.closeJobActionWithResponse(id, assignmentId, dispositionCode, closeTime, note, context).block();
    }

    /**
     * Retrieves list of jobs.
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedJob> listJobs() {
        return new PagedIterable<>(this.client.listJobs());
    }

    /**
     * Retrieves list of jobs based on filters.
     * @param jobStateSelector
     * @param queueId
     * @param channelId
     * @param maxPageSize
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedJob> listJobs(JobStateSelector jobStateSelector, String queueId, String channelId, Integer maxPageSize) {
        return new PagedIterable<>(this.client.listJobs(jobStateSelector, queueId, channelId, maxPageSize));
    }

    /**
     * Gets a job's position details
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobPositionDetails> getInQueuePosition(String id) {
        return this.getInQueuePositionWithResponse(id, null);
    }

    /**
     * Gets a job's position details
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobPositionDetails> getInQueuePositionWithResponse(String id, Context context) {
        return this.client.getInQueuePositionWithResponse(id, context).block();
    }

    /**
     * Accepts an offer to work on a job and returns a 409/Conflict if another agent accepted the job already.
     * @param offerId
     * @param workerId
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AcceptJobOfferResponse> acceptJobAction(String offerId, String workerId) {
        return this.acceptJobActionWithResponse(offerId, workerId, null);
    }

    /**
     * Accepts an offer to work on a job and returns a 409/Conflict if another agent accepted the job already.
     * @param offerId
     * @param workerId
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AcceptJobOfferResponse> acceptJobActionWithResponse(String offerId, String workerId, Context context) {
        return this.client.acceptJobActionWithResponse(offerId, workerId, context).block();
    }

    /**
     * Declines an offer to work on a job.
     * @param offerId
     * @param workerId
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> declineJobAction(String offerId, String workerId) {
        return this.declineJobActionWithResponse(offerId, workerId, null);
    }

    /**
     * Declines an offer to work on a job.
     * @param offerId
     * @param workerId
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> declineJobActionWithResponse(String offerId, String workerId, Context context) {
        return this.client.declineJobActionWithResponse(offerId, workerId, context).block();
    }

    /**
     * Creates or updates a queue.
     * @param id
     * @param jobQueue
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> upsertQueue(String id, JobQueue jobQueue) {
        return this.upsertQueueWithResponse(id, jobQueue, null);
    }

    /**
     * Creates or updates a queue.
     * @param id
     * @param jobQueue
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> upsertQueueWithResponse(String id, JobQueue jobQueue, Context context) {
        return this.client.upsertQueueWithResponse(id, jobQueue, context).block();
    }

    /**
     * Retrieves an existing queue by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> getQueue(String id) {
        return this.getQueueWithResponse(id, null);
    }

    /**
     * Retrieves an existing queue by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> getQueueWithResponse(String id, Context context) {
        return this.client.getQueueWithResponse(id, context).block();
    }

    /**
     * Deletes a queue by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteQueueWithResponse(String id) {
        return this.deleteQueueWithResponse(id, null);
    }

    /**
     * Deletes a queue by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteQueueWithResponse(String id, Context context) {
        return this.client.deleteQueueWithResponse(id, context).block();
    }

    /**
     * Retrieves existing queues.
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedQueue> listQueues() {
        return new PagedIterable<>(this.client.listQueues());
    }

    /**
     * Retrieves existing queues.
     * @param maxPageSize
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedQueue> listQueues(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listQueues(maxPageSize));
    }

    /**
     * Retrieves a queue's statistics by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueStatistics> getQueueStatistics(String id) {
        return this.getQueueStatisticsWithResponse(id, null);
    }

    /**
     * Retrieves a queue's statistics by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueStatistics> getQueueStatisticsWithResponse(String id, Context context) {
        return this.client.getQueueStatisticsWithResponse(id, context).block();
    }

    /**
     * Creates or updates a worker to process jobs.
     * @param id
     * @param routerWorker
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> upsertWorker(String id, RouterWorker routerWorker) {
        return this.upsertWorkerWithResponse(id, routerWorker, null);
    }

    /**
     * Creates or updates a worker to process jobs.
     * @param id
     * @param routerWorker
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> upsertWorkerWithResponse(String id, RouterWorker routerWorker, Context context) {
        return this.client.upsertWorkerWithResponse(id, routerWorker, context).block();
    }

    /**
     * Retrieves an existing worker by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> getWorker(String id) {
        return this.getWorkerWithResponse(id, null);
    }

    /**
     * Retrieves an existing worker by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> getWorkerWithResponse(String id, Context context) {
        return this.client.getWorkerWithResponse(id, context).block();
    }

    /**
     * Deletes a worker by Id.
     * @param id
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWorkerWithResponse(String id) {
        return this.deleteWorkerWithResponse(id, null);
    }

    /**
     * Deletes a worker by Id.
     * @param id
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWorkerWithResponse(String id, Context context) {
        return this.client.deleteWorkerWithResponse(id, context).block();
    }

    /**
     * Retrieves existing workers.
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedWorker> listWorkers() {
        return new PagedIterable<>(this.client.listWorkers());
    }

    /**
     * Retrieves existing workers.
     * @param workerStateSelector
     * @param channelId
     * @param queueId
     * @param hasCapacity
     * @param maxPageSize
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedWorker> listWorkers(WorkerStateSelector workerStateSelector, String channelId, String queueId, Boolean hasCapacity, Integer maxPageSize) {
        return new PagedIterable<>(this.client.listWorkers(workerStateSelector, channelId, queueId, hasCapacity, maxPageSize));
    }
}
