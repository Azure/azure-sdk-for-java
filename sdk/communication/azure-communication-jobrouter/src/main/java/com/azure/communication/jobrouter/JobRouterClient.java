package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.*;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static com.azure.core.util.FluxUtil.*;
import static com.azure.core.util.FluxUtil.pagedFluxError;

/**
 * Sync Client that supports chat operations.
 *
 * <p><strong>Instantiating a synchronous JobRouter Client</strong></p>
 *
 * <!-- src_embed com.azure.communication.jobrouter.jobrouterclient.instantiation -->
 * <pre>
 *
 * &#47;&#47; Initialize the job router client builder
 * final JobRouterClientBuilder builder = new JobRouterClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;credential&#41;;
 *
 * &#47;&#47; Build the job router client
 * JobRouterClient chatClient = builder.buildClient&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.communication.chat.chatclient.instantiation -->
 *
 * <p>View {@link JobRouterClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see JobRouterClientBuilder
 */
@ServiceClient(builder = JobRouterClientBuilder.class, isAsync = false)
public class JobRouterClient {

    private final ClientLogger logger = new ClientLogger(JobRouterClient.class);

    private final JobRouterAsyncClient client;

    /**
     * Creates a JobRouterClient that sends requests to the job router service at {@code serviceEndpoint}. Each
     * service call goes through the {@code pipeline}.
     *
     * @param client The {@link JobRouterAsyncClient} that the client routes its request through.
     */
    JobRouterClient(JobRouterAsyncClient client) {
        this.client = client;
    }

    // Classification policies
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> upsertClassificationPolicy(String id, ClassificationPolicy classificationPolicy) {
        return this.upsertClassificationPolicyWithResponse(id, classificationPolicy, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> upsertClassificationPolicyWithResponse(String id, ClassificationPolicy classificationPolicy, Context context) {
        return this.client.upsertClassificationPolicyWithResponse(id, classificationPolicy, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> getClassificationPolicy(String id) {
        return this.getClassificationPolicyWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> getClassificationPolicyWithResponse(String id, Context context) {
        return this.client.getClassificationPolicyWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteClassificationPolicy(String id) {
        return this.deleteClassificationPolicyWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteClassificationPolicyWithResponse(String id, Context context) {
        return this.client.deleteClassificationPolicyWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedClassificationPolicy> listClassificationPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listClassificationPolicies(maxPageSize));
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedClassificationPolicy> listClassificationPolicies() {
        return new PagedIterable<>(this.client.listClassificationPolicies());
    }

    // Distribution Policies
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> upsertDistributionPolicy(String id, DistributionPolicy distributionPolicy) {
        return this.upsertDistributionPolicyWithResponse(id, distributionPolicy, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> upsertDistributionPolicyWithResponse(String id, DistributionPolicy distributionPolicy, Context context) {
        return this.client.upsertDistributionPolicyWithResponse(id, distributionPolicy, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> getDistributionPolicy(String id) {
        return this.getDistributionPolicyWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> getDistributionPolicyWithResponse(String id, Context context) {
        return this.client.getDistributionPolicyWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDistributionPolicy(String id) {
        return this.deleteDistributionPolicyWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDistributionPolicyWithResponse(String id, Context context) {
        return this.client.deleteDistributionPolicyWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedDistributionPolicy> listDistributionPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listDistributionPolicies(maxPageSize));
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedDistributionPolicy> listDistributionPolicies() {
        return new PagedIterable<>(this.client.listDistributionPolicies());
    }

    // Exception Policies
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> upsertExceptionPolicy(String id, ExceptionPolicy exceptionPolicy) {
        return this.upsertExceptionPolicyWithResponse(id, exceptionPolicy, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> upsertExceptionPolicyWithResponse(String id, ExceptionPolicy exceptionPolicy, Context context) {
        return this.client.upsertExceptionPolicyWithResponse(id, exceptionPolicy, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> getExceptionPolicy(String id) {
        return this.getExceptionPolicyWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> getExceptionPolicyWithResponse(String id, Context context) {
        return this.client.getExceptionPolicyWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteExceptionPolicy(String id) {
        return this.deleteExceptionPolicyWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteExceptionPolicyWithResponse(String id, Context context) {
        return this.client.deleteExceptionPolicyWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedExceptionPolicy> listExceptionPolicies() {
        return new PagedIterable<>(this.client.listExceptionPolicies());
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedExceptionPolicy> listExceptionPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listExceptionPolicies(maxPageSize));
    }

    // Jobs
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> upsertJob(String id, RouterJob routerJob) {
        return this.upsertJobWithResponse(id, routerJob, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> upsertJobWithResponse(String id, RouterJob routerJob, Context context) {
        return this.client.upsertJobWithResponse(id, routerJob, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> getJob(String id) {
        return this.getJobWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> getJobWithResponse(String id, Context context) {
        return this.client.getJobWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteJob(String id) {
        return this.deleteJobWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteJobWithResponse(String id, Context context) {
        return this.client.deleteJobWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> reclassifyJobAction(String id, Object reclassifyJobRequest) {
        return this.reclassifyJobActionWithResponse(id, reclassifyJobRequest, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> reclassifyJobActionWithResponse(String id, Object reclassifyJobRequest, Context context) {
        return this.client.reclassifyJobActionWithResponse(id, reclassifyJobRequest, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> cancelJobAction(String id, String note, String dispositionCode) {
        return this.cancelJobActionWithResponse(id, note, dispositionCode, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> cancelJobActionWithResponse(String id, String note, String dispositionCode, Context context) {
        return this.client.cancelJobActionWithResponse(id, note, dispositionCode, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> completeJobAction(String id, String assignmentId, String note) {
        return this.completeJobActionWithResponse(id, assignmentId, note, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> completeJobActionWithResponse(String id, String assignmentId, String note, Context context) {
        return this.client.completeJobActionWithResponse(id, assignmentId, note, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> closeJobAction(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note) {
        return this.closeJobActionWithResponse(id, assignmentId, dispositionCode, closeTime, note, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> closeJobActionWithResponse(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note, Context context) {
        return this.client.closeJobActionWithResponse(id, assignmentId, dispositionCode, closeTime, note, context).block();
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedJob> listJobs() {
        return new PagedIterable<>(this.client.listJobs());
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedJob> listJobs(JobStateSelector jobStateSelector, String queueId, String channelId, Integer maxPageSize) {
        return new PagedIterable<>(this.client.listJobs(jobStateSelector, queueId, channelId, maxPageSize));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobPositionDetails> getInQueuePosition(String id) {
        return this.getInQueuePositionWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobPositionDetails> getInQueuePositionWithResponse(String id, Context context) {
        return this.client.getInQueuePositionWithResponse(id, context).block();
    }

    // Offers
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AcceptJobOfferResponse> acceptJobAction(String offerId, String workerId) {
        return this.acceptJobActionWithResponse(offerId, workerId, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AcceptJobOfferResponse> acceptJobActionWithResponse(String offerId, String workerId, Context context) {
        return this.client.acceptJobActionWithResponse(offerId, workerId, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> declineJobAction(String offerId, String workerId) {
        return this.declineJobActionWithResponse(offerId, workerId, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> declineJobActionWithResponse(String offerId, String workerId, Context context) {
        return this.client.declineJobActionWithResponse(offerId, workerId, context).block();
    }

    // Queues
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> upsertQueue(String id, JobQueue jobQueue) {
        return this.upsertQueueWithResponse(id, jobQueue, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> upsertQueueWithResponse(String id, JobQueue jobQueue, Context context) {
        return this.client.upsertQueueWithResponse(id, jobQueue, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> getQueue(String id) {
        return this.getQueueWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> getQueueWithResponse(String id, Context context) {
        return this.client.getQueueWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteQueueWithResponse(String id) {
        return this.deleteQueueWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteQueueWithResponse(String id, Context context) {
        return this.client.deleteQueueWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedQueue> listQueues() {
        return new PagedIterable<>(this.client.listQueues());
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedQueue> listQueues(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listQueues(maxPageSize));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueStatistics> getQueueStatistics(String id) {
        return this.getQueueStatisticsWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<QueueStatistics> getQueueStatisticsWithResponse(String id, Context context) {
        return this.client.getQueueStatisticsWithResponse(id, context).block();
    }

    // Workers
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> upsertWorker(String id, RouterWorker routerWorker) {
        return this.upsertWorkerWithResponse(id, routerWorker, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> upsertWorkerWithResponse(String id, RouterWorker routerWorker, Context context) {
        return this.client.upsertWorkerWithResponse(id, routerWorker, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> getWorker(String id) {
        return this.getWorkerWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> getWorkerWithResponse(String id, Context context) {
        return this.client.getWorkerWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWorkerWithResponse(String id) {
        return this.deleteWorkerWithResponse(id, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWorkerWithResponse(String id, Context context) {
        return this.client.deleteWorkerWithResponse(id, context).block();
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedWorker> listWorkers() {
        return new PagedIterable<>(this.client.listWorkers());
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedWorker> listWorkers(WorkerStateSelector workerStateSelector, String channelId, String queueId, Boolean hasCapacity, Integer maxPageSize) {
        return new PagedIterable<>(this.client.listWorkers(workerStateSelector, channelId, queueId, hasCapacity, maxPageSize));
    }
}
