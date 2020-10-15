// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.CloudJob;
import com.microsoft.azure.batch.protocol.models.DisableJobOption;
import com.microsoft.azure.batch.protocol.models.JobAddOptions;
import com.microsoft.azure.batch.protocol.models.JobAddParameter;
import com.microsoft.azure.batch.protocol.models.JobConstraints;
import com.microsoft.azure.batch.protocol.models.JobDeleteOptions;
import com.microsoft.azure.batch.protocol.models.JobDisableOptions;
import com.microsoft.azure.batch.protocol.models.JobEnableOptions;
import com.microsoft.azure.batch.protocol.models.JobExecutionInformation;
import com.microsoft.azure.batch.protocol.models.JobGetAllLifetimeStatisticsOptions;
import com.microsoft.azure.batch.protocol.models.JobGetOptions;
import com.microsoft.azure.batch.protocol.models.JobGetTaskCountsOptions;
import com.microsoft.azure.batch.protocol.models.JobListFromJobScheduleOptions;
import com.microsoft.azure.batch.protocol.models.JobListOptions;
import com.microsoft.azure.batch.protocol.models.JobListPreparationAndReleaseTaskStatusOptions;
import com.microsoft.azure.batch.protocol.models.JobPatchOptions;
import com.microsoft.azure.batch.protocol.models.JobPatchParameter;
import com.microsoft.azure.batch.protocol.models.JobPreparationAndReleaseTaskExecutionInformation;
import com.microsoft.azure.batch.protocol.models.JobPreparationTask;
import com.microsoft.azure.batch.protocol.models.JobReleaseTask;
import com.microsoft.azure.batch.protocol.models.JobStatistics;
import com.microsoft.azure.batch.protocol.models.JobTerminateOptions;
import com.microsoft.azure.batch.protocol.models.JobUpdateOptions;
import com.microsoft.azure.batch.protocol.models.JobUpdateParameter;
import com.microsoft.azure.batch.protocol.models.MetadataItem;
import com.microsoft.azure.batch.protocol.models.OnAllTasksComplete;
import com.microsoft.azure.batch.protocol.models.PoolInformation;
import com.microsoft.azure.batch.protocol.models.TaskCounts;
import com.microsoft.azure.batch.protocol.models.TaskCountsResult;
import com.microsoft.azure.batch.protocol.models.TaskSlotCounts;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Performs job-related operations on an Azure Batch account.
 */
public class JobOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> customBehaviors;

    private final BatchClient parentBatchClient;

    JobOperations(BatchClient batchClient, Collection<BatchClientBehavior> inheritedBehaviors) {
        parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.inheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    /**
     * Gets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @return A collection of {@link BatchClientBehavior} instances.
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return customBehaviors;
    }

    /**
     * Sets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @param behaviors The collection of {@link BatchClientBehavior} instances.
     * @return The current instance.
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        customBehaviors = behaviors;
        return this;
    }

    /**
     * Gets lifetime summary statistics for all of the jobs in the current account.
     *
     * @return The aggregated job statistics.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public JobStatistics getAllJobsLifetimeStatistics() throws BatchErrorException, IOException {
        return getAllJobsLifetimeStatistics(null);
    }

    /**
     * Gets lifetime summary statistics for all of the jobs in the current account.
     *
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return The aggregated job statistics.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public JobStatistics getAllJobsLifetimeStatistics(Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobGetAllLifetimeStatisticsOptions options = new JobGetAllLifetimeStatisticsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().jobs().getAllLifetimeStatistics(options);
    }

    /**
     * Gets the specified {@link CloudJob}.
     *
     * @param jobId The ID of the job to get.
     * @return A {@link CloudJob} containing information about the specified Azure Batch job.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public CloudJob getJob(String jobId) throws BatchErrorException, IOException {
        return getJob(jobId, null, null);
    }

    /**
     * Gets the specified {@link CloudJob}.
     *
     * @param jobId The ID of the job to get.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @return A {@link CloudJob} containing information about the specified Azure Batch job.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public CloudJob getJob(String jobId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getJob(jobId, detailLevel, null);
    }

    /**
     * Gets the specified {@link CloudJob}.
     *
     * @param jobId The ID of the job to get.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link CloudJob} containing information about the specified Azure Batch job.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public CloudJob getJob(String jobId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobGetOptions getJobOptions = new JobGetOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(getJobOptions);

        return this.parentBatchClient.protocolLayer().jobs().get(jobId, getJobOptions);
    }

    /**
     * Lists the {@link CloudJob jobs} in the Batch account.
     *
     * @return A list of {@link CloudJob} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<CloudJob> listJobs() throws BatchErrorException, IOException {
        return listJobs(null, (Iterable<BatchClientBehavior>) null);
    }

    /**
     * Lists the {@link CloudJob jobs} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A list of {@link CloudJob} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<CloudJob> listJobs(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobs(detailLevel, null);
    }

    /**
     * Lists the {@link CloudJob jobs} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link CloudJob} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<CloudJob> listJobs(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListOptions jobListOptions = new JobListOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(jobListOptions);

        return this.parentBatchClient.protocolLayer().jobs().list(jobListOptions);
    }

    /**
     * Lists the {@link CloudJob jobs} created under the specified job schedule.
     *
     * @param jobScheduleId The ID of job schedule.
     * @return A list of {@link CloudJob} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<CloudJob> listJobs(String jobScheduleId) throws BatchErrorException, IOException {
        return listJobs(jobScheduleId, null, null);
    }

    /**
     * Lists the {@link CloudJob jobs} created under the specified job schedule.
     *
     * @param jobScheduleId The ID of job schedule.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A list of {@link CloudJob} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<CloudJob> listJobs(String jobScheduleId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobs(jobScheduleId, detailLevel, null);
    }

    /**
     * Lists the {@link CloudJob jobs} created under the specified jobSchedule.
     *
     * @param jobScheduleId The ID of jobSchedule.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link CloudJob} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<CloudJob> listJobs(String jobScheduleId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListFromJobScheduleOptions jobListOptions = new JobListFromJobScheduleOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(jobListOptions);

        return this.parentBatchClient.protocolLayer().jobs().listFromJobSchedule(jobScheduleId, jobListOptions);
    }

    /**
     * Lists the status of {@link JobPreparationTask} and {@link JobReleaseTask} tasks for the specified job.
     *
     * @param jobId The ID of the job.
     * @return A list of {@link JobPreparationAndReleaseTaskExecutionInformation} instances.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<JobPreparationAndReleaseTaskExecutionInformation> listPreparationAndReleaseTaskStatus(String jobId) throws BatchErrorException, IOException {
        return listPreparationAndReleaseTaskStatus(jobId, null);
    }

    /**
     * Lists the status of {@link JobPreparationTask} and {@link JobReleaseTask} tasks for the specified job.
     *
     * @param jobId The ID of the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link JobPreparationAndReleaseTaskExecutionInformation} instances.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<JobPreparationAndReleaseTaskExecutionInformation> listPreparationAndReleaseTaskStatus(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListPreparationAndReleaseTaskStatusOptions jobListOptions = new JobListPreparationAndReleaseTaskStatusOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(jobListOptions);

        return this.parentBatchClient.protocolLayer().jobs().listPreparationAndReleaseTaskStatus(jobId, jobListOptions);
    }

    /**
     * Adds a job to the Batch account.
     *
     * @param jobId The ID of the job to be added.
     * @param poolInfo Specifies how a job should be assigned to a pool.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void createJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        createJob(jobId, poolInfo, null);
    }

    /**
     * Adds a job to the Batch account.
     *
     * @param jobId The ID of the job to be added.
     * @param poolInfo Specifies how a job should be assigned to a pool.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void createJob(String jobId, PoolInformation poolInfo, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobAddParameter param = new JobAddParameter()
                .withId(jobId)
                .withPoolInfo(poolInfo);

        createJob(param, additionalBehaviors);
    }

    /**
     * Adds a job to the Batch account.
     *
     * @param job The job to be added.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void createJob(JobAddParameter job) throws BatchErrorException, IOException {
        createJob(job, null);
    }

    /**
     * Adds a job to the Batch account.
     *
     * @param job The job to be added.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void createJob(JobAddParameter job, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobAddOptions options = new JobAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobs().add(job, options);
    }

    /**
     * Deletes the specified job.
     *
     * @param jobId The ID of the job.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteJob(String jobId) throws BatchErrorException, IOException {
        deleteJob(jobId, null);
    }

    /**
     * Deletes the specified job.
     *
     * @param jobId The ID of the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteJob(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobDeleteOptions options = new JobDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobs().delete(jobId, options);
    }

    /**
     * Terminates the specified job, marking it as completed.
     *
     * @param jobId The ID of the job.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void terminateJob(String jobId) throws BatchErrorException, IOException {
        terminateJob(jobId, null, null);
    }

    /**
     * Terminates the specified job, marking it as completed.
     *
     * @param jobId The ID of the job.
     * @param terminateReason The message to describe the reason the job has terminated. This text will appear when you call {@link JobExecutionInformation#terminateReason()}.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void terminateJob(String jobId, String terminateReason) throws BatchErrorException, IOException {
        terminateJob(jobId, terminateReason, null);
    }

    /**
     * Terminates the specified job, marking it as completed.
     *
     * @param jobId The ID of the job.
     * @param terminateReason The message to describe the reason the job has terminated. This text will appear when you call {@link JobExecutionInformation#terminateReason()}.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void terminateJob(String jobId, String terminateReason, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobTerminateOptions options = new JobTerminateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobs().terminate(jobId, terminateReason, options);
    }

    /**
     * Enables the specified job, allowing new tasks to run.
     *
     * @param jobId The ID of the job.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void enableJob(String jobId) throws BatchErrorException, IOException {
        enableJob(jobId, null);
    }

    /**
     * Enables the specified job, allowing new tasks to run.
     *
     * @param jobId The ID of the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void enableJob(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobEnableOptions options = new JobEnableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobs().enable(jobId, options);
    }

    /**
     * Disables the specified job. Disabled jobs do not run new tasks, but may be re-enabled later.
     *
     * @param jobId The ID of the job.
     * @param disableJobOption Specifies what to do with running tasks associated with the job.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void disableJob(String jobId, DisableJobOption disableJobOption) throws BatchErrorException, IOException {
        disableJob(jobId, disableJobOption, null);
    }

    /**
     * Disables the specified job. Disabled jobs do not run new tasks, but may be re-enabled later.
     *
     * @param jobId The ID of the job.
     * @param disableJobOption Specifies what to do with running tasks associated with the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void disableJob(String jobId, DisableJobOption disableJobOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobDisableOptions options = new JobDisableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobs().disable(jobId, disableJobOption, options);
    }

    /**
     * Updates the specified job.
     * This method performs a full replace of all updatable properties of the job. For example, if the constraints parameter is null, then the Batch service removes the job's existing constraints and replaces them with the default constraints.
     *
     * @param jobId The ID of the job.
     * @param poolInfo The pool on which the Batch service runs the job's tasks. You may change the pool for a job only when the job is disabled. If you specify an autoPoolSpecification specification in the poolInfo, only the keepAlive property can be updated, and then only if the auto pool has a poolLifetimeOption of job.
     * @param priority The priority of the job. Priority values can range from -1000 to 1000, with -1000 being the lowest priority and 1000 being the highest priority. If null, it is set to the default value 0.
     * @param constraints The execution constraints for the job. If null, the constraints are cleared.
     * @param onAllTasksComplete Specifies an action the Batch service should take when all tasks in the job are in the completed state.
     * @param metadata A list of name-value pairs associated with the job as metadata. If null, it takes the default value of an empty list; in effect, any existing metadata is deleted.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void updateJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, OnAllTasksComplete onAllTasksComplete,
                          List<MetadataItem> metadata) throws BatchErrorException, IOException {
        updateJob(jobId, poolInfo, priority, constraints, onAllTasksComplete, metadata, null);
    }

    /**
     * Updates the specified job.
     * This method performs a full replace of all updatable properties of the job. For example, if the constraints parameter is null, then the Batch service removes the job's existing constraints and replaces them with the default constraints.
     *
     * @param jobId The ID of the job.
     * @param poolInfo The pool on which the Batch service runs the job's tasks. You may change the pool for a job only when the job is disabled. If you specify an autoPoolSpecification specification in the poolInfo, only the keepAlive property can be updated, and then only if the auto pool has a poolLifetimeOption of job.
     * @param priority The priority of the job. Priority values can range from -1000 to 1000, with -1000 being the lowest priority and 1000 being the highest priority. If null, it is set to the default value 0.
     * @param constraints The execution constraints for the job. If null, the constraints are cleared.
     * @param onAllTasksComplete Specifies an action the Batch service should take when all tasks in the job are in the completed state.
     * @param metadata A list of name-value pairs associated with the job as metadata. If null, it takes the default value of an empty list; in effect, any existing metadata is deleted.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void updateJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, OnAllTasksComplete onAllTasksComplete,
                          List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobUpdateOptions options = new JobUpdateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobUpdateParameter param = new JobUpdateParameter()
                .withPriority(priority)
                .withPoolInfo(poolInfo)
                .withConstraints(constraints)
                .withOnAllTasksComplete(onAllTasksComplete)
                .withMetadata(metadata);

        this.parentBatchClient.protocolLayer().jobs().update(jobId, param, options);
    }

    /**
     * Updates the specified job.
     * This method only replaces the properties specified with non-null values.
     *
     * @param jobId The ID of the job.
     * @param poolInfo The pool on which the Batch service runs the job's tasks. You may change the pool for a job only when the job is disabled. If you specify an autoPoolSpecification specification in the poolInfo, only the keepAlive property can be updated, and then only if the auto pool has a poolLifetimeOption of job. If null, the job continues to run on its current pool.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void patchJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        patchJob(jobId, poolInfo, null, null, null, null, null);
    }

    /**
     * Updates the specified job.
     * This method only replaces the properties specified with non-null values.
     *
     * @param jobId The ID of the job.
     * @param onAllTasksComplete Specifies an action the Batch service should take when all tasks in the job are in the completed state.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void patchJob(String jobId, OnAllTasksComplete onAllTasksComplete) throws BatchErrorException, IOException {
        patchJob(jobId, null, null, null, onAllTasksComplete, null, null);
    }

    /**
     * Updates the specified job.
     * This method only replaces the properties specified with non-null values.
     *
     * @param jobId The ID of the job.
     * @param poolInfo The pool on which the Batch service runs the job's tasks. You may change the pool for a job only when the job is disabled. If you specify an autoPoolSpecification specification in the poolInfo, only the keepAlive property can be updated, and then only if the auto pool has a poolLifetimeOption of job. If null, the job continues to run on its current pool.
     * @param priority The priority of the job. Priority values can range from -1000 to 1000, with -1000 being the lowest priority and 1000 being the highest priority. If null, the priority of the job is left unchanged.
     * @param constraints The execution constraints for the job. If null, the existing execution constraints are left unchanged.
     * @param onAllTasksComplete Specifies an action the Batch service should take when all tasks in the job are in the completed state.
     * @param metadata A list of name-value pairs associated with the job as metadata. If null, the existing job metadata is left unchanged.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void patchJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, OnAllTasksComplete onAllTasksComplete,
                         List<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchJob(jobId, poolInfo, priority, constraints, onAllTasksComplete, metadata, null);
    }

    /**
     * Updates the specified job.
     * This method only replaces the properties specified with non-null values.
     *
     * @param jobId The ID of the job.
     * @param poolInfo The pool on which the Batch service runs the job's tasks. You may change the pool for a job only when the job is disabled. If you specify an autoPoolSpecification specification in the poolInfo, only the keepAlive property can be updated, and then only if the auto pool has a poolLifetimeOption of job. If null, the job continues to run on its current pool.
     * @param priority The priority of the job. Priority values can range from -1000 to 1000, with -1000 being the lowest priority and 1000 being the highest priority. If null, the priority of the job is left unchanged.
     * @param constraints The execution constraints for the job. If null, the existing execution constraints are left unchanged.
     * @param onAllTasksComplete Specifies an action the Batch service should take when all tasks in the job are in the completed state.
     * @param metadata A list of name-value pairs associated with the job as metadata. If null, the existing job metadata is left unchanged.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void patchJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, OnAllTasksComplete onAllTasksComplete,
                         List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobPatchParameter param = new JobPatchParameter()
                .withPriority(priority)
                .withPoolInfo(poolInfo)
                .withConstraints(constraints)
                .withOnAllTasksComplete(onAllTasksComplete)
                .withMetadata(metadata);

        patchJob(jobId, param, additionalBehaviors);
    }

    /**
     * Updates the specified job.
     * This method only replaces the properties specified with non-null values.
     *
     * @param jobId The ID of the job.
     * @param jobPatchParameter The set of changes to be made to a job.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void patchJob(String jobId, JobPatchParameter jobPatchParameter) throws BatchErrorException, IOException {
        patchJob(jobId, jobPatchParameter, null);
    }

    /**
     * Updates the specified job.
     * This method only replaces the properties specified with non-null values.
     *
     * @param jobId The ID of the job.
     * @param jobPatchParameter The parameter to update the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void patchJob(String jobId, JobPatchParameter jobPatchParameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobPatchOptions options = new JobPatchOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobs().patch(jobId, jobPatchParameter, options);
    }

    /**
     * Gets the task counts for the specified job.
     * Task counts provide a count of the tasks by active, running or completed task state, and a count of tasks which succeeded or failed. Tasks in the preparing state are counted as running.
     *
     * @param jobId The ID of the job.
     * @throws BatchErrorException thrown if the request is rejected by server
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     * @return the TaskCounts object if successful.
     */
    public TaskCounts getTaskCounts(String jobId) throws BatchErrorException, IOException {
        return getTaskCounts(jobId, null);
    }

    /**
     * Gets the task counts for the specified job.
     * Task counts provide a count of the tasks by active, running or completed task state, and a count of tasks which succeeded or failed. Tasks in the preparing state are counted as running.
     *
     * @param jobId The ID of the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException thrown if the request is rejected by server
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     * @return the TaskCounts object if successful.
     */
    public TaskCounts getTaskCounts(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        return getTaskCountsResult(jobId, additionalBehaviors).taskCounts();

    }

    /**
     * Gets the task slot counts for the specified job.
     * Task slot counts provide a count of the tasks by active, running or completed task state, and a count of tasks which succeeded or failed. Tasks in the preparing state are counted as running.
     *
     * @param jobId The ID of the job.
     * @throws BatchErrorException thrown if the request is rejected by server
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     * @return the TaskSlotCounts object if successful.
     */
    public TaskSlotCounts getTaskSlotCounts(String jobId) throws BatchErrorException, IOException {
        return getTaskSlotCounts(jobId, null);
    }

    /**
     * Gets the task slot counts for the specified job.
     * Task slot counts provide a count of the tasks by active, running or completed task state, and a count of tasks which succeeded or failed. Tasks in the preparing state are counted as running.
     *
     * @param jobId The ID of the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException thrown if the request is rejected by server
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     * @return the TaskSlotCounts object if successful.
     */
    public TaskSlotCounts getTaskSlotCounts(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        return getTaskCountsResult(jobId, additionalBehaviors).taskSlotCounts();

    }

    /**
     * Gets the task counts result for the specified job.
     * The result includes both task counts and task slot counts. Each counts object provides a count of the tasks by active, running or completed task state, and a count of tasks which succeeded or failed. Tasks in the preparing state are counted as running.
     *
     * @param jobId The ID of the job.
     * @throws BatchErrorException thrown if the request is rejected by server
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     * @return the TaskCountsResult object if successful.
     */
    public TaskCountsResult getTaskCountsResult(String jobId)
    throws BatchErrorException, IOException {
        return getTaskCountsResult(jobId, null);
    }

    /**
     * Gets the task counts result for the specified job.
     * The result includes both task counts and task slot counts. Each counts object provides a count of the tasks by active, running or completed task state, and a count of tasks which succeeded or failed. Tasks in the preparing state are counted as running.
     *
     * @param jobId The ID of the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException thrown if the request is rejected by server
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     * @return the TaskCountsResult object if successful.
     */
    public TaskCountsResult getTaskCountsResult(
        String jobId,
        Iterable<BatchClientBehavior> additionalBehaviors
    ) throws BatchErrorException, IOException {
        JobGetTaskCountsOptions options = new JobGetTaskCountsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);
        return this.parentBatchClient.protocolLayer().jobs().getTaskCounts(jobId, options);
    }
}
