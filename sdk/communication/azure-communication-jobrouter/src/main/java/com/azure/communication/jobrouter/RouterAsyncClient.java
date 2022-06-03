package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.AzureCommunicationRoutingServiceImpl;
import com.azure.communication.jobrouter.implementation.JobRoutersImpl;
import com.azure.communication.jobrouter.models.*;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static com.azure.core.util.FluxUtil.*;

/**
 * Async Client that supports job router operations.
 *
 * <p><strong>Instantiating an asynchronous job router Client</strong></p>
 *
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
public class RouterAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(RouterAsyncClient.class);

    private JobRoutersImpl jobRouter;

    RouterAsyncClient(AzureCommunicationRoutingServiceImpl jobRouterService) {
        this.jobRouter = jobRouterService.getJobRouters();
    }

    // Classification policies
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ClassificationPolicy>> upsertClassificationPolicyWithResponse(String id, ClassificationPolicy classificationPolicy) {
        try {
            return withContext(context -> upsertClassificationPolicyWithResponse(id, classificationPolicy, context));
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
            return jobRouter.deleteDistributionPolicyWithResponseAsync(id, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedClassificationPolicy> listClassificationPolicies() {
        try {
            return jobRouter.listClassificationPoliciesAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedClassificationPolicy> listClassificationPolicies(Integer maxPageSize) {
        try {
            return jobRouter.listClassificationPoliciesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    // Distribution Policies

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DistributionPolicy>> upsertDistributionPolicyWithResponse(String id, DistributionPolicy distributionPolicy) {
        try {
            return withContext(context -> upsertDistributionPolicyWithResponse(id, distributionPolicy, context));
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

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedDistributionPolicy> listDistributionPolicies() {
        try {
            return jobRouter.listDistributionPoliciesAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedDistributionPolicy> listDistributionPolicies(Integer maxPageSize) {
        try {
            return jobRouter.listDistributionPoliciesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    // Exception Policies
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ExceptionPolicy>> upsertExceptionPolicyWithResponse(String id, ExceptionPolicy exceptionPolicy) {
        try {
            return withContext(context -> upsertExceptionPolicyWithResponse(id, exceptionPolicy, context));
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

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedExceptionPolicy> listExceptionPolicies() {
        try {
            return jobRouter.listExceptionPoliciesAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedExceptionPolicy> listExceptionPolicies(Integer maxPageSize) {
        try {
            return jobRouter.listExceptionPoliciesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    // Jobs
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouterJob>> upsertJobWithResponse(String id, RouterJob routerJob) {
        try {
            return withContext(context -> upsertJobWithResponse(id, routerJob, context));
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

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Object>> reclassifyJobActionWithResponse(String id, Object reclassifyJobRequest) {
        try {
            return withContext(context -> reclassifyJobActionWithResponse(id, reclassifyJobRequest, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Object>> reclassifyJobActionWithResponse(String id, Object reclassifyJobRequest, Context context) {
        try {
            return jobRouter.reclassifyJobActionWithResponseAsync(id, reclassifyJobRequest, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Object>> cancelJobActionWithResponse(String id, String note, String dispositionCode) {
        try {
            return withContext(context -> cancelJobActionWithResponse(id, note, dispositionCode, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Object>> cancelJobActionWithResponse(String id, String note, String dispositionCode, Context context) {
        try {
            return jobRouter.cancelJobActionWithResponseAsync(id, note, dispositionCode, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Object>> completeJobActionWithResponse(String id, String assignmentId, String note) {
        try {
            return withContext(context -> completeJobActionWithResponse(id, assignmentId, note, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Object>> completeJobActionWithResponse(String id, String assignmentId, String note, Context context) {
        try {
            return jobRouter.completeJobActionWithResponseAsync(id, assignmentId, note, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Object>> closeJobActionWithResponse(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note) {
        try {
            return withContext(context -> closeJobActionWithResponse(id, assignmentId, dispositionCode, closeTime, note, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Object>> closeJobActionWithResponse(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note, Context context) {
        try {
            return jobRouter.closeJobActionWithResponseAsync(id, assignmentId, dispositionCode, closeTime, note, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedJob> listJobs() {
        try {
            return jobRouter.listJobsAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedJob> listJobs(JobStateSelector jobStateSelector, String queueId, String channelId, Integer maxPageSize) {
        try {
            return jobRouter.listJobsAsync(jobStateSelector, queueId, channelId, maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

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

    // Offers
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcceptJobOfferResponse>> acceptJobActionWithResponse(String offerId, String workerId) {
        try {
            return withContext(context -> acceptJobActionWithResponse(offerId, workerId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<AcceptJobOfferResponse>> acceptJobActionWithResponse(String offerId, String workerId, Context context) {
        try {
            return jobRouter.acceptJobActionWithResponseAsync(offerId, workerId, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Object>> declineJobActionWithResponse(String offerId, String workerId) {
        try {
            return withContext(context -> declineJobActionWithResponse(offerId, workerId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Object>> declineJobActionWithResponse(String offerId, String workerId, Context context) {
        try {
            return jobRouter.declineJobActionWithResponseAsync(offerId, workerId, context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    // Queues
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<JobQueue>> upsertQueueWithResponse(String id, JobQueue jobQueue) {
        try {
            return withContext(context -> upsertQueueWithResponse(id, jobQueue, context));
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

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedQueue> listQueues() {
        try {
            return jobRouter.listQueuesAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedQueue> listQueues(Integer maxPageSize) {
        try {
            return jobRouter.listQueuesAsync(maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

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

    // Workers
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouterWorker>> upsertWorkerWithResponse(String id, RouterWorker routerWorker) {
        try {
            return withContext(context -> upsertWorkerWithResponse(id, routerWorker, context));
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

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedWorker> listWorkers() {
        try {
            return jobRouter.listWorkersAsync();
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PagedWorker> listWorkers(WorkerStateSelector workerStateSelector, String channelId, String queueId, Boolean hasCapacity, Integer maxPageSize) {
        try {
            return jobRouter.listWorkersAsync(workerStateSelector, channelId, queueId, hasCapacity, maxPageSize);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }
}
