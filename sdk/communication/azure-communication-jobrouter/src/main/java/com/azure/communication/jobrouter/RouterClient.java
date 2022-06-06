package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.*;
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
     * Upsert a classification policy.
     *
     * @param id Id of the classification policy.
     * @param classificationPolicy Model of classification policy properties to be patched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a container for the rules that govern how jobs are classified.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> upsertClassificationPolicy(String id, ClassificationPolicy classificationPolicy) {
        return this.upsertClassificationPolicyWithResponse(id, classificationPolicy, null);
    }

    /**
     * Upsert a classification policy.
     *
     * @param id Id of the classification policy.
     * @param classificationPolicy Model of classification policy properties to be patched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a container for the rules that govern how jobs are classified.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> upsertClassificationPolicyWithResponse(String id, ClassificationPolicy classificationPolicy, Context context) {
        return this.client.upsertClassificationPolicyWithResponse(id, classificationPolicy, context).block();
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
    public Response<ClassificationPolicy> getClassificationPolicy(String id) {
        return this.getClassificationPolicyWithResponse(id, null);
    }

    /**
     * Retrieves an existing classification policy by Id.
     *
     * @param id Id of the classification policy.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a container for the rules that govern how jobs are classified.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> getClassificationPolicyWithResponse(String id, Context context) {
        return this.client.getClassificationPolicyWithResponse(id, context).block();
    }

    /**
     * Delete a classification policy by Id.
     *
     * @param id Id of the classification policy.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteClassificationPolicy(String id) {
        return this.deleteClassificationPolicyWithResponse(id, null);
    }

    /**
     * Delete a classification policy by Id.
     *
     * @param id Id of the classification policy.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteClassificationPolicyWithResponse(String id, Context context) {
        return this.client.deleteClassificationPolicyWithResponse(id, context).block();
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
    public PagedIterable<PagedClassificationPolicy> listClassificationPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listClassificationPolicies(maxPageSize));
    }

    /**
     * Retrieves existing classification policies.
     *
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of classification policies.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedClassificationPolicy> listClassificationPolicies() {
        return new PagedIterable<>(this.client.listClassificationPolicies());
    }

    /**
     * Upsert a distribution policy.
     *
     * @param id Id of the distribution policy.
     * @param distributionPolicy Model of distribution policy properties to be patched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return policy governing how jobs are distributed to workers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> upsertDistributionPolicy(String id, DistributionPolicy distributionPolicy) {
        return this.upsertDistributionPolicyWithResponse(id, distributionPolicy, null);
    }

    /**
     * Upsert a distribution policy.
     *
     * @param id Id of the distribution policy.
     * @param distributionPolicy Model of distribution policy properties to be patched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return policy governing how jobs are distributed to workers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> upsertDistributionPolicyWithResponse(String id, DistributionPolicy distributionPolicy, Context context) {
        return this.client.upsertDistributionPolicyWithResponse(id, distributionPolicy, context).block();
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
    public Response<DistributionPolicy> getDistributionPolicy(String id) {
        return this.getDistributionPolicyWithResponse(id, null);
    }

    /**
     * Retrieves an existing distribution policy by Id.
     *
     * @param id Id of the distribution policy.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return policy governing how jobs are distributed to workers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DistributionPolicy> getDistributionPolicyWithResponse(String id, Context context) {
        return this.client.getDistributionPolicyWithResponse(id, context).block();
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
    public Response<Void> deleteDistributionPolicy(String id) {
        return this.deleteDistributionPolicyWithResponse(id, null);
    }

    /**
     * Delete a distribution policy by Id.
     *
     * @param id Id of the distribution policy.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDistributionPolicyWithResponse(String id, Context context) {
        return this.client.deleteDistributionPolicyWithResponse(id, context).block();
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
    public PagedIterable<PagedDistributionPolicy> listDistributionPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listDistributionPolicies(maxPageSize));
    }

    /**
     * Retrieves existing distribution policies.
     *
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of distribution policies.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedDistributionPolicy> listDistributionPolicies() {
        return new PagedIterable<>(this.client.listDistributionPolicies());
    }

    /**
     * Upsert a exception policy.
     *
     * @param id Id of the exception policy.
     * @param exceptionPolicy Model of exception policy properties to be patched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a policy that defines actions to execute when exception are triggered.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> upsertExceptionPolicy(String id, ExceptionPolicy exceptionPolicy) {
        return this.upsertExceptionPolicyWithResponse(id, exceptionPolicy, null);
    }

    /**
     * Upsert a exception policy.
     *
     * @param id Id of the exception policy.
     * @param exceptionPolicy Model of exception policy properties to be patched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a policy that defines actions to execute when exception are triggered.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> upsertExceptionPolicyWithResponse(String id, ExceptionPolicy exceptionPolicy, Context context) {
        return this.client.upsertExceptionPolicyWithResponse(id, exceptionPolicy, context).block();
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
    public Response<ExceptionPolicy> getExceptionPolicy(String id) {
        return this.getExceptionPolicyWithResponse(id, null);
    }

    /**
     * Retrieves an existing exception policy by Id.
     *
     * @param id Id of the exception policy to retrieve.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a policy that defines actions to execute when exception are triggered.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExceptionPolicy> getExceptionPolicyWithResponse(String id, Context context) {
        return this.client.getExceptionPolicyWithResponse(id, context).block();
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
    public Response<Void> deleteExceptionPolicy(String id) {
        return this.deleteExceptionPolicyWithResponse(id, null);
    }

    /**
     * Deletes a exception policy by Id.
     *
     * @param id Id of the exception policy to delete.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteExceptionPolicyWithResponse(String id, Context context) {
        return this.client.deleteExceptionPolicyWithResponse(id, context).block();
    }

    /**
     * Retrieves existing exception policies.
     *
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of exception policies.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedExceptionPolicy> listExceptionPolicies() {
        return new PagedIterable<>(this.client.listExceptionPolicies());
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
    public PagedIterable<PagedExceptionPolicy> listExceptionPolicies(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listExceptionPolicies(maxPageSize));
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
    public Response<RouterJob> upsertJob(String id, RouterJob routerJob) {
        return this.upsertJobWithResponse(id, routerJob, null);
    }

    /**
     * Upsert a job.
     *
     * @param id Id of the job.
     * @param routerJob Model of job properties to be created or patched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a unit of work to be routed.
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
     *
     * @param id Id of the job to retrieve.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a unit of work to be routed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterJob> getJobWithResponse(String id, Context context) {
        return this.client.getJobWithResponse(id, context).block();
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
    public Response<Void> deleteJob(String id) {
        return this.deleteJobWithResponse(id, null);
    }

    /**
     * Deletes a job and all of its traces.
     *
     * @param id Id of the job.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> reclassifyJobAction(String id, Object reclassifyJobRequest) {
        return this.reclassifyJobActionWithResponse(id, reclassifyJobRequest, null);
    }

    /**
     * Reclassify a job.
     *
     * @param id Id of the job.
     * @param reclassifyJobRequest Request object for reclassifying a job.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> reclassifyJobActionWithResponse(String id, Object reclassifyJobRequest, Context context) {
        return this.client.reclassifyJobActionWithResponse(id, reclassifyJobRequest, context).block();
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
    public Response<Object> cancelJobAction(String id, String note, String dispositionCode) {
        return this.cancelJobActionWithResponse(id, note, dispositionCode, null);
    }

    /**
     * Submits request to cancel an existing job by Id while supplying free-form cancellation reason.
     *
     * @param id Id of the job.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param dispositionCode Indicates the outcome of the job, populate this field with your own custom values. If not
     *     provided, default value of "Cancelled" is set.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> cancelJobActionWithResponse(String id, String note, String dispositionCode, Context context) {
        return this.client.cancelJobActionWithResponse(id, note, dispositionCode, context).block();
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
    public Response<Object> completeJobAction(String id, String assignmentId, String note) {
        return this.completeJobActionWithResponse(id, assignmentId, note, null);
    }

    /**
     * Completes an assigned job.
     *
     * @param id Id of the job.
     * @param assignmentId The assignment within the job to complete.
     * @param note (Optional) A note that will be appended to the jobs' Notes collection with th current timestamp.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> completeJobActionWithResponse(String id, String assignmentId, String note, Context context) {
        return this.client.completeJobActionWithResponse(id, assignmentId, note, context).block();
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
    public Response<Object> closeJobAction(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note) {
        return this.closeJobActionWithResponse(id, assignmentId, dispositionCode, closeTime, note, null);
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
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> closeJobActionWithResponse(String id, String assignmentId, String dispositionCode, OffsetDateTime closeTime, String note, Context context) {
        return this.client.closeJobActionWithResponse(id, assignmentId, dispositionCode, closeTime, note, context).block();
    }

    /**
     * Retrieves list of jobs based on filter parameters.
     *
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of jobs.
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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of jobs.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedJob> listJobs(JobStateSelector jobStateSelector, String queueId, String channelId, Integer maxPageSize) {
        return new PagedIterable<>(this.client.listJobs(jobStateSelector, queueId, channelId, maxPageSize));
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
    public Response<JobPositionDetails> getInQueuePosition(String id) {
        return this.getInQueuePositionWithResponse(id, null);
    }

    /**
     * Gets a job's position details.
     *
     * @param id Id of the job.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a job's position details.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobPositionDetails> getInQueuePositionWithResponse(String id, Context context) {
        return this.client.getInQueuePositionWithResponse(id, context).block();
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
    public Response<AcceptJobOfferResponse> acceptJobAction(String offerId, String workerId) {
        return this.acceptJobActionWithResponse(offerId, workerId, null);
    }

    /**
     * Accepts an offer to work on a job and returns a 409/Conflict if another agent accepted the job already.
     *
     * @param offerId Id of the offer.
     * @param workerId Id of the worker.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response containing Id's for the worker, job, and assignment from an accepted offer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AcceptJobOfferResponse> acceptJobActionWithResponse(String offerId, String workerId, Context context) {
        return this.client.acceptJobActionWithResponse(offerId, workerId, context).block();
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
    public Response<Object> declineJobAction(String offerId, String workerId) {
        return this.declineJobActionWithResponse(offerId, workerId, null);
    }

    /**
     * Declines an offer to work on a job.
     *
     * @param offerId Id of the offer.
     * @param workerId Id of the worker.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Object> declineJobActionWithResponse(String offerId, String workerId, Context context) {
        return this.client.declineJobActionWithResponse(offerId, workerId, context).block();
    }

    /**
     * Upsert a queue.
     *
     * @param id Id of the queue.
     * @param jobQueue Model of queue properties to be patched. See also: https://datatracker.ietf.org/doc/html/rfc7386.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a queue that can contain jobs to be routed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> upsertQueue(String id, JobQueue jobQueue) {
        return this.upsertQueueWithResponse(id, jobQueue, null);
    }

    /**
     * Upsert a queue.
     *
     * @param id Id of the queue.
     * @param jobQueue Model of queue properties to be patched. See also: https://datatracker.ietf.org/doc/html/rfc7386.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a queue that can contain jobs to be routed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> upsertQueueWithResponse(String id, JobQueue jobQueue, Context context) {
        return this.client.upsertQueueWithResponse(id, jobQueue, context).block();
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
    public Response<JobQueue> getQueue(String id) {
        return this.getQueueWithResponse(id, null);
    }

    /**
     * Retrieves an existing queue by Id.
     *
     * @param id Id of the queue to retrieve.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a queue that can contain jobs to be routed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<JobQueue> getQueueWithResponse(String id, Context context) {
        return this.client.getQueueWithResponse(id, context).block();
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
    public Response<Void> deleteQueueWithResponse(String id) {
        return this.deleteQueueWithResponse(id, null);
    }

    /**
     * Deletes a queue by Id.
     *
     * @param id Id of the queue to delete.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteQueueWithResponse(String id, Context context) {
        return this.client.deleteQueueWithResponse(id, context).block();
    }

    /**
     * Retrieves existing queues.
     *
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of queues.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedQueue> listQueues() {
        return new PagedIterable<>(this.client.listQueues());
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
    public PagedIterable<PagedQueue> listQueues(Integer maxPageSize) {
        return new PagedIterable<>(this.client.listQueues(maxPageSize));
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
    public Response<QueueStatistics> getQueueStatistics(String id) {
        return this.getQueueStatisticsWithResponse(id, null);
    }

    /**
     * Retrieves a queue's statistics.
     *
     * @param id Id of the queue to retrieve statistics.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return statistics for the queue.
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
     * Upsert a worker.
     *
     * @param id Id of the worker.
     * @param routerWorker Model of worker properties to be patched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an entity for jobs to be routed to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> upsertWorkerWithResponse(String id, RouterWorker routerWorker, Context context) {
        return this.client.upsertWorkerWithResponse(id, routerWorker, context).block();
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
    public Response<RouterWorker> getWorker(String id) {
        return this.getWorkerWithResponse(id, null);
    }

    /**
     * Retrieves an existing worker by Id.
     *
     * @param id Id of the worker to retrieve.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return an entity for jobs to be routed to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouterWorker> getWorkerWithResponse(String id, Context context) {
        return this.client.getWorkerWithResponse(id, context).block();
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
    public Response<Void> deleteWorkerWithResponse(String id) {
        return this.deleteWorkerWithResponse(id, null);
    }

    /**
     * Deletes a worker and all of its traces.
     *
     * @param id Id of the worker to delete.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWorkerWithResponse(String id, Context context) {
        return this.client.deleteWorkerWithResponse(id, context).block();
    }

    /**
     * Retrieves existing workers.
     *
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of workers.
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
     *     `channelId` or for any channel if `channelId` not specified. If set to false, then will return all workers
     *     including workers without any capacity for jobs. Defaults to false.
     * @param maxPageSize Number of objects to return per page.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a paged collection of workers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PagedWorker> listWorkers(WorkerStateSelector workerStateSelector, String channelId, String queueId, Boolean hasCapacity, Integer maxPageSize) {
        return new PagedIterable<>(this.client.listWorkers(workerStateSelector, channelId, queueId, hasCapacity, maxPageSize));
    }
}
