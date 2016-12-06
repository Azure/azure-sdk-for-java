/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.*;
import com.microsoft.rest.ServiceResponseWithHeaders;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Performs job related operations on an Azure Batch account.
 */
public class JobOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    public static final String SHA1_CERTIFICATE_ALGORITHM = "sha1";

    JobOperations(BatchClient batchClient, Collection<BatchClientBehavior> inheritedBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    /**
     * Gets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @return A list of BatchClientBehavior
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return _customBehaviors;
    }

    /**
     * Sets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @param behaviors The collection of BatchClientBehavior classes
     * @return The current instance
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
        return this;
    }

    /**
     * Gets lifetime summary statistics for all of the jobs in the current account.
     *
     * @return The aggregated job statistics.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public JobStatistics getAllJobsLifetimeStatistics() throws BatchErrorException, IOException {
        return getAllJobsLifetimeStatistics(null);
    }

    /**
     * Gets lifetime summary statistics for all of the jobs in the current account.
     *
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return The aggregated job statistics.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public JobStatistics getAllJobsLifetimeStatistics(Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobGetAllJobsLifetimeStatisticsOptions options = new JobGetAllJobsLifetimeStatisticsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<JobStatistics, JobGetAllJobsLifetimeStatisticsHeaders> response = this._parentBatchClient.protocolLayer().jobs().getAllJobsLifetimeStatistics(options);

        return response.getBody();
    }

    /**
     * Gets the specified {@link CloudJob}.
     *
     * @param jobId The ID of the job to get.
     * @return A {@link CloudJob} containing information about the specified Azure Batch job.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudJob getJob(String jobId) throws BatchErrorException, IOException {
        return getJob(jobId, null, null);
    }

    /**
     * Gets the specified {@link CloudJob}.
     *
     * @param jobId The ID of the job to get.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A {@link CloudJob} containing information about the specified Azure Batch job.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudJob getJob(String jobId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getJob(jobId, detailLevel, null);
    }

    /**
     * Gets the specified {@link CloudJob}.
     *
     * @param jobId The ID of the job to get.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link CloudJob} containing information about the specified Azure Batch job.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudJob getJob(String jobId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobGetOptions getJobOptions = new JobGetOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(getJobOptions);

        ServiceResponseWithHeaders<CloudJob, JobGetHeaders> response = this._parentBatchClient.protocolLayer().jobs().get(jobId, getJobOptions);
        return response.getBody();
    }

    /**
     * Enumerates the {@link CloudJob jobs} in the Batch account.
     *
     * @return A collection of {@link CloudJob jobs}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudJob> listJobs() throws BatchErrorException, IOException {
        return listJobs(null, (Iterable<BatchClientBehavior>) null);
    }

    /**
     * Enumerates the {@link CloudJob jobs} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A collection of {@link CloudJob jobs}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudJob> listJobs(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobs(detailLevel, (Iterable<BatchClientBehavior>) null);
    }

    /**
     * Enumerates the {@link CloudJob jobs} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link CloudJob jobs}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudJob> listJobs(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListOptions jobListOptions = new JobListOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(jobListOptions);

        ServiceResponseWithHeaders<PagedList<CloudJob>, JobListHeaders> response = this._parentBatchClient.protocolLayer().jobs().list(jobListOptions);

        return response.getBody();
    }

    /**
     * Enumerates the {@link CloudJob jobs} in the specified job schedule.
     *
     * @param jobScheduleId The ID of job schedule
     * @return A collection of {@link CloudJob jobs}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudJob> listJobs(String jobScheduleId) throws BatchErrorException, IOException {
        return listJobs(jobScheduleId, null, null);
    }

    /**
     * Enumerates the {@link CloudJob jobs} in the specified job schedule.
     *
     * @param jobScheduleId The ID of job schedule
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A collection of {@link CloudJob jobs}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudJob> listJobs(String jobScheduleId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobs(jobScheduleId, detailLevel, null);
    }

    /**
     * Enumerates the {@link CloudJob jobs} in the specified jobSchedule.
     *
     * @param jobScheduleId The ID of jobSchedule
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link CloudJob jobs}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudJob> listJobs(String jobScheduleId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListFromJobScheduleOptions jobListOptions = new JobListFromJobScheduleOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(jobListOptions);

        ServiceResponseWithHeaders<PagedList<CloudJob>, JobListFromJobScheduleHeaders> response = this._parentBatchClient.protocolLayer().jobs().listFromJobSchedule(jobScheduleId, jobListOptions);

        return response.getBody();
    }

    /**
     * Enumerates the status of {@link JobPreparationTask} and {@link JobReleaseTask} tasks for the specified job.
     *
     * @param jobId The ID of the job.
     * @return A collection of {@link JobPreparationAndReleaseTaskExecutionInformation} object.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<JobPreparationAndReleaseTaskExecutionInformation> listPreparationAndReleaseTaskStatus(String jobId) throws BatchErrorException, IOException {
        return listPreparationAndReleaseTaskStatus(jobId, null);
    }

    /**
     * Enumerates the status of {@link JobPreparationTask} and {@link JobReleaseTask} tasks for the specified job.
     *
     * @param jobId The ID of the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link JobPreparationAndReleaseTaskExecutionInformation} object.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<JobPreparationAndReleaseTaskExecutionInformation> listPreparationAndReleaseTaskStatus(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListPreparationAndReleaseTaskStatusOptions jobListOptions = new JobListPreparationAndReleaseTaskStatusOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(jobListOptions);

        ServiceResponseWithHeaders<PagedList<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> response = this._parentBatchClient.protocolLayer().jobs().listPreparationAndReleaseTaskStatus(jobId, jobListOptions);

        return response.getBody();
    }

    /**
     * Add a job to the specified pool.
     *
     * @param jobId The Id of the job.
     * @param poolInfo the information about the pool the job will run on.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        createJob(jobId, poolInfo, null);
    }

    /**
     * Add a job to the specified pool.
     *
     * @param jobId The Id of the job.
     * @param poolInfo The information about the pool the job will run on.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createJob(String jobId, PoolInformation poolInfo, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobAddParameter param = new JobAddParameter()
                .withId(jobId)
                .withPoolInfo(poolInfo);

        createJob(param, additionalBehaviors);
    }

    /**
     * Add a job.
     *
     * @param job The parameter to add a job.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createJob(JobAddParameter job) throws BatchErrorException, IOException {
        createJob(job, null);
    }

    /**
     * Add a job.
     *
     * @param job The parameter to add a job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createJob(JobAddParameter job, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobAddOptions options = new JobAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobs().add(job, options);
    }

    /**
     * Deletes the specified job.
     *
     * @param jobId The ID of the job.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deleteJob(String jobId) throws BatchErrorException, IOException {
        deleteJob(jobId, null);
    }

    /**
     * Deletes the specified job.
     *
     * @param jobId The ID of the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deleteJob(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobDeleteOptions options = new JobDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobs().delete(jobId, options);
    }

    /**
     * Terminates the specified job, marking it as completed.
     *
     * @param jobId The ID of the job.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void terminateJob(String jobId) throws BatchErrorException, IOException {
        terminateJob(jobId, null, null);
    }

    /**
     * Terminates the specified job, marking it as completed.
     *
     * @param jobId The ID of the job.
     * @param terminateReason the text you want to appear as the job's terminate reason.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void terminateJob(String jobId, String terminateReason) throws BatchErrorException, IOException {
        terminateJob(jobId, terminateReason, null);
    }

    /**
     * Terminates the specified job, marking it as completed.
     *
     * @param jobId The ID of the job.
     * @param terminateReason The text you want to appear as the job's terminate reason.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void terminateJob(String jobId, String terminateReason, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobTerminateOptions options = new JobTerminateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobs().terminate(jobId, terminateReason, options);
    }

    /**
     * Enables the specified job, allowing new tasks to run.
     *
     * @param jobId The ID of the job.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void enableJob(String jobId) throws BatchErrorException, IOException {
        enableJob(jobId, null);
    }

    /**
     * Enables the specified job, allowing new tasks to run.
     *
     * @param jobId The ID of the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void enableJob(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobEnableOptions options = new JobEnableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobs().enable(jobId, options);
    }

    /**
     * Disables the specified job.  Disabled jobs do not run new tasks, but may be re-enabled later.
     *
     * @param jobId The ID of the job.
     * @param disableJobOption Specifies what to do with active tasks associated with the job.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void disableJob(String jobId, DisableJobOption disableJobOption) throws BatchErrorException, IOException {
        disableJob(jobId, disableJobOption, null);
    }

    /**
     * Disables the specified job.  Disabled jobs do not run new tasks, but may be re-enabled later.
     *
     * @param jobId The ID of the job.
     * @param disableJobOption Specifies what to do with active tasks associated with the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void disableJob(String jobId, DisableJobOption disableJobOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobDisableOptions options = new JobDisableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobs().disable(jobId, disableJobOption, options);
    }

    /**
     * Updates the specified job.
     *
     * @param jobId The ID of the job.
     * @param poolInfo The pool on which the Batch service runs the job's tasks. You may change the pool for a job only when the job is disabled. If you specify an autoPoolSpecification specification in the poolInfo, only the keepAlive property can be updated, and then only if the auto pool has a poolLifetimeOption of job.
     * @param priority The priority of the job. Priority values can range from -1000 to 1000, with -1000 being the lowest priority and 1000 being the highest priority. If omitted, it is set to the default value 0.
     * @param constraints The execution constraints for the job. If omitted, the constraints are cleared.
     * @param onAllTasksComplete Specifies an action the Batch service should take when all tasks in the job are in the completed state.
     * @param metadata A list of name-value pairs associated with the job as metadata. If omitted, it takes the default value of an empty list; in effect, any existing metadata is deleted.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updateJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, OnAllTasksComplete onAllTasksComplete,
                          List<MetadataItem> metadata) throws BatchErrorException, IOException {
        updateJob(jobId, poolInfo, priority, constraints, onAllTasksComplete, metadata, null);
    }

    /**
     * Updates the specified job.
     *
     * @param jobId The ID of the job.
     * @param poolInfo The pool on which the Batch service runs the job's tasks. You may change the pool for a job only when the job is disabled. If you specify an autoPoolSpecification specification in the poolInfo, only the keepAlive property can be updated, and then only if the auto pool has a poolLifetimeOption of job.
     * @param priority The priority of the job. Priority values can range from -1000 to 1000, with -1000 being the lowest priority and 1000 being the highest priority. If omitted, it is set to the default value 0.
     * @param constraints The execution constraints for the job. If omitted, the constraints are cleared.
     * @param onAllTasksComplete Specifies an action the Batch service should take when all tasks in the job are in the completed state.
     * @param metadata A list of name-value pairs associated with the job as metadata. If omitted, it takes the default value of an empty list; in effect, any existing metadata is deleted.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
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

        this._parentBatchClient.protocolLayer().jobs().update(jobId, param, options);
    }

    /**
     * Updates the specified job.
     *
     * @param jobId The ID of the job.
     * @param poolInfo The pool on which the Batch service runs the job's tasks. You may change the pool for a job only when the job is disabled. If you specify an autoPoolSpecification specification in the poolInfo, only the keepAlive property can be updated, and then only if the auto pool has a poolLifetimeOption of job. If omitted, the job continues to run on its current pool.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void patchJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        patchJob(jobId, poolInfo, null, null, null, null, null);
    }

    /**
     * Updates the specified job.
     *
     * @param jobId The ID of the job.
     * @param onAllTasksComplete Specifies an action the Batch service should take when all tasks in the job are in the completed state.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void patchJob(String jobId, OnAllTasksComplete onAllTasksComplete) throws BatchErrorException, IOException {
        patchJob(jobId, null, null, null, onAllTasksComplete, null, null);
    }

    /**
     * Updates the specified job.
     *
     * @param jobId The ID of the job.
     * @param poolInfo The pool on which the Batch service runs the job's tasks. You may change the pool for a job only when the job is disabled. If you specify an autoPoolSpecification specification in the poolInfo, only the keepAlive property can be updated, and then only if the auto pool has a poolLifetimeOption of job. If omitted, the job continues to run on its current pool.
     * @param priority The priority of the job. Priority values can range from -1000 to 1000, with -1000 being the lowest priority and 1000 being the highest priority. If omitted, the priority of the job is left unchanged.
     * @param constraints The execution constraints for the job. If omitted, the existing execution constraints are left unchanged.
     * @param onAllTasksComplete Specifies an action the Batch service should take when all tasks in the job are in the completed state.
     * @param metadata A list of name-value pairs associated with the job as metadata. If omitted, the existing job metadata is left unchanged.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void patchJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, OnAllTasksComplete onAllTasksComplete,
                         List<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchJob(jobId, poolInfo, priority, constraints, onAllTasksComplete, metadata, null);
    }

    /**
     * Updates the specified job.
     *
     * @param jobId The ID of the job.
     * @param poolInfo The pool on which the Batch service runs the job's tasks. You may change the pool for a job only when the job is disabled. If you specify an autoPoolSpecification specification in the poolInfo, only the keepAlive property can be updated, and then only if the auto pool has a poolLifetimeOption of job. If omitted, the job continues to run on its current pool.
     * @param priority The priority of the job. Priority values can range from -1000 to 1000, with -1000 being the lowest priority and 1000 being the highest priority. If omitted, the priority of the job is left unchanged.
     * @param constraints The execution constraints for the job. If omitted, the existing execution constraints are left unchanged.
     * @param onAllTasksComplete Specifies an action the Batch service should take when all tasks in the job are in the completed state.
     * @param metadata A list of name-value pairs associated with the job as metadata. If omitted, the existing job metadata is left unchanged.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
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
     *
     * @param jobId The ID of the job.
     * @param jobPatchParameter The parameter to update the job.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void patchJob(String jobId, JobPatchParameter jobPatchParameter) throws BatchErrorException, IOException {
        patchJob(jobId, jobPatchParameter, null);
    }

    /**
     * Updates the specified job.
     *
     * @param jobId The ID of the job.
     * @param jobPatchParameter The parameter to update the job.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void patchJob(String jobId, JobPatchParameter jobPatchParameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobPatchOptions options = new JobPatchOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobs().patch(jobId, jobPatchParameter, options);
    }
}
