// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.CloudJobSchedule;
import com.microsoft.azure.batch.protocol.models.JobScheduleAddOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleAddParameter;
import com.microsoft.azure.batch.protocol.models.JobScheduleDeleteOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleDisableOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleEnableOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleExistsOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleGetOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleListOptions;
import com.microsoft.azure.batch.protocol.models.JobSchedulePatchOptions;
import com.microsoft.azure.batch.protocol.models.JobSchedulePatchParameter;
import com.microsoft.azure.batch.protocol.models.JobScheduleTerminateOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleUpdateOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleUpdateParameter;
import com.microsoft.azure.batch.protocol.models.JobSpecification;
import com.microsoft.azure.batch.protocol.models.MetadataItem;
import com.microsoft.azure.batch.protocol.models.Schedule;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Performs job schedule-related operations on an Azure Batch account.
 */
public class JobScheduleOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> customBehaviors;

    private final BatchClient parentBatchClient;

    JobScheduleOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
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
     * Checks whether the specified job schedule exists.
     *
     * @param jobScheduleId The ID of the job schedule which you want to check.
     * @return True if the specified job schedule exists; otherwise, false.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public boolean existsJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        return existsJobSchedule(jobScheduleId, null);
    }

    /**
     * Checks whether the specified job schedule exists.
     *
     * @param jobScheduleId The ID of the job schedule which you want to check.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return True if the specified job schedule exists; otherwise, false.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public boolean existsJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleExistsOptions options = new JobScheduleExistsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().jobSchedules().exists(jobScheduleId, options);
    }

    /**
     * Deletes the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        deleteJobSchedule(jobScheduleId, null);
    }

    /**
     * Deletes the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleDeleteOptions options = new JobScheduleDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobSchedules().delete(jobScheduleId, options);
    }

    /**
     * Gets the specified {@link CloudJobSchedule}.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @return A {@link CloudJobSchedule} containing information about the specified Azure Batch job schedule.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public CloudJobSchedule getJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        return getJobSchedule(jobScheduleId, null, null);
    }

    /**
     * Gets the specified {@link CloudJobSchedule}.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @return A {@link CloudJobSchedule} containing information about the specified Azure Batch job schedule.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public CloudJobSchedule getJobSchedule(String jobScheduleId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getJobSchedule(jobScheduleId, detailLevel, null);
    }

    /**
     * Gets the specified {@link CloudJobSchedule}.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param detailLevel A {@link DetailLevel} used for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link CloudJobSchedule} containing information about the specified Azure Batch job schedule.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public CloudJobSchedule getJobSchedule(String jobScheduleId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleGetOptions options = new JobScheduleGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().jobSchedules().get(jobScheduleId, options);
    }

    /**
     * Updates the specified job schedule.
     * This method only replaces the properties specified with non-null values.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param schedule The schedule according to which jobs will be created. If null, any existing schedule is left unchanged.
     * @param jobSpecification The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification. If null, the existing job specification is left unchanged.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        patchJobSchedule(jobScheduleId, schedule, jobSpecification, null, null);
    }

    /**
     * Updates the specified job schedule.
     * This method only replaces the properties specified with non-null values.
     *
     * @param jobScheduleId The ID of the job schedule. If null, any existing schedule is left unchanged.
     * @param schedule The schedule according to which jobs will be created.
     * @param jobSpecification The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification. If null, the existing job specification is left unchanged.
     * @param metadata A list of name-value pairs associated with the job schedule as metadata. If null, the existing metadata are left unchanged.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchJobSchedule(jobScheduleId, schedule, jobSpecification, metadata, null);
    }

    /**
     * Updates the specified job schedule.
     * This method only replaces the properties specified with non-null values.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param schedule The schedule according to which jobs will be created. If null, any existing schedule is left unchanged.
     * @param jobSpecification The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification. If null, the existing job specification is left unchanged.
     * @param metadata A list of name-value pairs associated with the job schedule as metadata. If null, the existing metadata are left unchanged.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobSchedulePatchOptions options = new JobSchedulePatchOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobSchedulePatchParameter param = new JobSchedulePatchParameter()
                .withJobSpecification(jobSpecification)
                .withMetadata(metadata)
                .withSchedule(schedule);
        this.parentBatchClient.protocolLayer().jobSchedules().patch(jobScheduleId, param, options);
    }

    /**
     * Updates the specified job schedule.
     * This method performs a full replace of all the updatable properties of the job schedule. For example, if the schedule parameter is null, then the Batch service removes the job schedule’s existing schedule and replaces it with the default schedule.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param schedule The schedule according to which jobs will be created. If null, it is equivalent to passing the default schedule: that is, a single job scheduled to run immediately.
     * @param jobSpecification  The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        updateJobSchedule(jobScheduleId, schedule, jobSpecification, null, null);
    }

    /**
     * Updates the specified job schedule.
     * This method performs a full replace of all the updatable properties of the job schedule. For example, if the schedule parameter is null, then the Batch service removes the job schedule’s existing schedule and replaces it with the default schedule.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param schedule The schedule according to which jobs will be created. If null, it is equivalent to passing the default schedule: that is, a single job scheduled to run immediately.
     * @param jobSpecification The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification.
     * @param metadata A list of name-value pairs associated with the job schedule as metadata. If null, it takes the default value of an empty list; in effect, any existing metadata is deleted.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        updateJobSchedule(jobScheduleId, schedule, jobSpecification, metadata, null);
    }

    /**
     * Updates the specified job schedule.
     * This method performs a full replace of all the updatable properties of the job schedule. For example, if the schedule parameter is null, then the Batch service removes the job schedule’s existing schedule and replaces it with the default schedule.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param schedule The schedule according to which jobs will be created. If null, it is equivalent to passing the default schedule: that is, a single job scheduled to run immediately.
     * @param jobSpecification The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification.
     * @param metadata A list of name-value pairs associated with the job schedule as metadata. If null, it takes the default value of an empty list; in effect, any existing metadata is deleted.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleUpdateOptions options = new JobScheduleUpdateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobScheduleUpdateParameter param = new JobScheduleUpdateParameter()
                .withJobSpecification(jobSpecification)
                .withMetadata(metadata)
                .withSchedule(schedule);
        this.parentBatchClient.protocolLayer().jobSchedules().update(jobScheduleId, param, options);
    }

    /**
     * Disables the specified job schedule. Disabled schedules do not create new jobs, but may be re-enabled later.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void disableJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        disableJobSchedule(jobScheduleId, null);
    }

    /**
     * Disables the specified job schedule. Disabled schedules do not create new jobs, but may be re-enabled later.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void disableJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleDisableOptions options = new JobScheduleDisableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobSchedules().disable(jobScheduleId, options);
    }

    /**
     * Enables the specified job schedule, allowing jobs to be created according to its {@link Schedule}.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void enableJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        enableJobSchedule(jobScheduleId, null);
    }

    /**
     * Enables the specified job schedule, allowing jobs to be created according to its {@link Schedule}.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void enableJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleEnableOptions options = new JobScheduleEnableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobSchedules().enable(jobScheduleId, options);
    }

    /**
     * Terminates the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void terminateJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        terminateJobSchedule(jobScheduleId, null);
    }

    /**
     * Terminates the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void terminateJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleTerminateOptions options = new JobScheduleTerminateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobSchedules().terminate(jobScheduleId, options);
    }

    /**
     * Adds a job schedule to the Batch account.
     *
     * @param jobScheduleId A string that uniquely identifies the job schedule within the account.
     * @param schedule The schedule according to which jobs will be created.
     * @param jobSpecification Details about the jobs to be created on this schedule.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void createJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        createJobSchedule(jobScheduleId, schedule, jobSpecification, null);
    }

    /**
     * Adds a job schedule to the Batch account.
     *
     * @param jobScheduleId A string that uniquely identifies the job schedule within the account.
     * @param schedule The schedule according to which jobs will be created.
     * @param jobSpecification Details about the jobs to be created on this schedule.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void createJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleAddParameter param = new JobScheduleAddParameter()
                .withJobSpecification(jobSpecification)
                .withSchedule(schedule)
                .withId(jobScheduleId);
        createJobSchedule(param, additionalBehaviors);
    }

    /**
     * Adds a job schedule to the Batch account.
     *
     * @param jobSchedule The job schedule to be added.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void createJobSchedule(JobScheduleAddParameter jobSchedule) throws BatchErrorException, IOException {
        createJobSchedule(jobSchedule, null);
    }

    /**
     * Adds a job schedule to the Batch account.
     *
     * @param jobSchedule The job schedule to be added.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void createJobSchedule(JobScheduleAddParameter jobSchedule, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleAddOptions options = new JobScheduleAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this.parentBatchClient.protocolLayer().jobSchedules().add(jobSchedule, options);
    }

    /**
     * Lists the {@link CloudJobSchedule job schedules} in the Batch account.
     *
     * @return A list of {@link CloudJobSchedule} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<CloudJobSchedule> listJobSchedules() throws BatchErrorException, IOException {
        return listJobSchedules(null, null);
    }

    /**
     * Lists the {@link CloudJobSchedule job schedules} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A list of {@link CloudJobSchedule} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<CloudJobSchedule> listJobSchedules(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobSchedules(detailLevel, null);
    }

    /**
     * Lists the {@link CloudJobSchedule job schedules} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link CloudJobSchedule} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<CloudJobSchedule> listJobSchedules(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleListOptions options = new JobScheduleListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().jobSchedules().list(options);
    }

}
