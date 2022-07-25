package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.convertors.ClassificationPolicyAdapter;
import com.azure.communication.jobrouter.implementation.convertors.DistributionPolicyAdapter;
import com.azure.communication.jobrouter.implementation.convertors.ExceptionPolicyAdapter;
import com.azure.communication.jobrouter.implementation.convertors.QueueAdapter;
import com.azure.communication.jobrouter.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.JobQueue;
import com.azure.communication.jobrouter.models.PagedClassificationPolicy;
import com.azure.communication.jobrouter.models.PagedDistributionPolicy;
import com.azure.communication.jobrouter.models.PagedExceptionPolicy;
import com.azure.communication.jobrouter.models.PagedQueue;
import com.azure.communication.jobrouter.models.UpdateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateQueueOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

@ServiceClient(builder = RouterAdministrationClientBuilder.class, isAsync = false)
public class RouterAdministrationClient {

    private RouterAdministrationAsyncClient client;
    RouterAdministrationClient(RouterAdministrationAsyncClient client) {
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
     * @param classificationPolicyId Id of the classification policy.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ClassificationPolicy getClassificationPolicy(String classificationPolicyId) {
        return this.client.getClassificationPolicy(classificationPolicyId).block();
    }

    /**
     * Retrieves an existing classification policy by Id.
     *
     * @param classificationPolicyId Id of the classification policy.
     * @param context The context to associate with this operation.
     * @return a container for the rules that govern how jobs are classified.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ClassificationPolicy> getClassificationPolicyWithResponse(String classificationPolicyId, Context context) {
        return this.client.getClassificationPolicyWithResponse(classificationPolicyId, context).block();
    }

    /**
     * Delete a classification policy by Id.
     *
     * @param classificationPolicyId Id of the classification policy.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteClassificationPolicy(String classificationPolicyId) {
        this.client.deleteClassificationPolicy(classificationPolicyId).block();
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
     * @param distributionPolicyId Id of the distribution policy.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDistributionPolicy(String distributionPolicyId) {
        this.client.deleteDistributionPolicy(distributionPolicyId).block();
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
}
