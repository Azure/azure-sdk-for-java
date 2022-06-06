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
public final class RouterAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(RouterAsyncClient.class);

    private final JobRoutersImpl jobRouter;

    RouterAsyncClient(AzureCommunicationRoutingServiceImpl jobRouterService) {
        this.jobRouter = jobRouterService.getJobRouters();
    }

    /**
     * Creates or updates classification policy.
     *
     * @param id Id of the classification policy.
     * @param classificationPolicy Model of classification policy properties to be patched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a container for the rules that govern how jobs are classified.
     */
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

    /**
     * Retrieves an existing classification policy by Id.
     *
     * @param id Id of the classification policy.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a container for the rules that govern how jobs are classified.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
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
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of classification policies.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of classification policies.
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
     * Creates or updates a distribution policy.
     *
     * @param id Id of the distribution policy.
     * @param distributionPolicy Model of distribution policy properties to be patched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return policy governing how jobs are distributed to workers.
     */
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

    /**
     * Retrieves an existing distribution policy by Id.
     *
     * @param id Id of the distribution policy.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return policy governing how jobs are distributed to workers.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
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
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of distribution policies.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of distribution policies.
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
     * Creates or updates an exception policy.
     *
     * @param id Id of the exception policy.
     * @param exceptionPolicy Model of exception policy properties to be patched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a policy that defines actions to execute when exception are triggered.
     */
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

    /**
     * Retrieves an existing exception policy by Id.
     *
     * @param id Id of the exception policy to retrieve.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a policy that defines actions to execute when exception are triggered.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
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
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of exception policies.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of exception policies.
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
     * Upsert a job.
     *
     * @param id Id of the job.
     * @param routerJob Model of job properties to be created or patched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a unit of work to be routed.
     */
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

    /**
     * Retrieves an existing job by Id.
     *
     * @param id Id of the job to retrieve.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a unit of work to be routed.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
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
     * @param reclassifyJobRequest Request object for reclassifying a job.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
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

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     *
     * @param id Id of the job.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values. If not
     *     provided, default value of "Cancelled" is set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
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

    /**
     * Completes an assigned job.
     *
     * @param id Id of the job.
     * @param assignmentId The assignment within the job to complete.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
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

    /**
     * Closes a completed job.
     *
     * @param id Id of the job.
     * @param assignmentId The assignment within which the job is to be closed.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values.
     * @param closeTime If not provided, worker capacity is released immediately along with a JobClosedEvent
     *     notification. If provided, worker capacity is released along with a JobClosedEvent notification at a future
     *     time.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
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

    /**
     * Retrieves list of jobs based on filter parameters.
     *
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of jobs.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of jobs.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a job's position details.
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
     * @param offerId Id of the offer.
     * @param workerId Id of the worker.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response containing Id's for the worker, job, and assignment from an accepted offer.
     */
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

    /**
     * Declines an offer to work on a job.
     *
     * @param offerId Id of the offer.
     * @param workerId Id of the worker.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
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

    /**
     * Upsert a queue.
     *
     * @param id Id of the queue.
     * @param jobQueue Model of queue properties to be patched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a queue that can contain jobs to be routed.
     */
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

    /**
     * Retrieves an existing queue by Id.
     *
     * @param id Id of the queue to retrieve.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a queue that can contain jobs to be routed.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
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
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of queues.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of queues.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return statistics for the queue.
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
     * Upsert a worker.
     *
     * @param id Id of the worker.
     * @param routerWorker Model of worker properties to be patched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an entity for jobs to be routed to.
     */
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

    /**
     * Retrieves an existing worker by Id.
     *
     * @param id Id of the worker to retrieve.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an entity for jobs to be routed to.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
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
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of workers.
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
     *     `channelId` or for any channel if `channelId` not specified. If set to false, then will return all workers
     *     including workers without any capacity for jobs. Defaults to false.
     * @param maxPageSize Number of objects to return per page.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of workers.
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
