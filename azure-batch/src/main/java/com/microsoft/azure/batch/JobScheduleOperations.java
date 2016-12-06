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
 * Performs job schedule related operations on an Azure Batch account.
 */
public class JobScheduleOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    JobScheduleOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
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
     * Checks existence of the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule which you want to check.
     * @return True if specified job schedule exists, otherwise, return false.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public boolean existsJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        return existsJobSchedule(jobScheduleId, null);
    }

    /**
     * Checks existence of the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule which you want to check.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return True if specified job schedule exists, otherwise, return false.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public boolean existsJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleExistsOptions options = new JobScheduleExistsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Boolean, JobScheduleExistsHeaders> response = this._parentBatchClient.protocolLayer().jobSchedules().exists(jobScheduleId, options);

        return response.getBody();
    }

    /**
     * Deletes the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deleteJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        deleteJobSchedule(jobScheduleId, null);
    }

    /**
     * Deletes the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void deleteJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleDeleteOptions options = new JobScheduleDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobSchedules().delete(jobScheduleId, options);
    }

    /**
     * Gets the specified {@link CloudJobSchedule}.
     *
     * @param jobScheduleId The ID of the job schedule
     * @return A {@link CloudJobSchedule} containing information about the specified Azure Batch job schedule.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudJobSchedule getJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        return getJobSchedule(jobScheduleId, null, null);
    }

    /**
     * Gets the specified {@link CloudJobSchedule}.
     *
     * @param jobScheduleId The ID of the job schedule
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A {@link CloudJobSchedule} containing information about the specified Azure Batch job schedule.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudJobSchedule getJobSchedule(String jobScheduleId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getJobSchedule(jobScheduleId, detailLevel, null);
    }

    /**
     * Gets the specified {@link CloudJobSchedule}.
     *
     * @param jobScheduleId The ID of the job schedule
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link CloudJobSchedule} containing information about the specified Azure Batch job schedule.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public CloudJobSchedule getJobSchedule(String jobScheduleId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleGetOptions options = new JobScheduleGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudJobSchedule, JobScheduleGetHeaders> response = this._parentBatchClient.protocolLayer().jobSchedules().get(jobScheduleId, options);

        return response.getBody();
    }

    /**
     * Updates the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param schedule The schedule according to which jobs will be created.
     * @param jobSpecification The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        patchJobSchedule(jobScheduleId, schedule, jobSpecification, null, null);
    }

    /**
     * Updates the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param schedule The schedule according to which jobs will be created.
     * @param jobSpecification The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification.
     * @param metadata A list of name-value pairs associated with the job schedule as metadata. If omitted, the existing metadata are left unchanged.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchJobSchedule(jobScheduleId, schedule, jobSpecification, metadata, null);
    }

    /**
     * Updates the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule.
     * @param schedule The schedule according to which jobs will be created.
     * @param jobSpecification The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification.
     * @param metadata A list of name-value pairs associated with the job schedule as metadata. If omitted, the existing metadata are left unchanged.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobSchedulePatchOptions options = new JobSchedulePatchOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobSchedulePatchParameter param = new JobSchedulePatchParameter()
                .withJobSpecification(jobSpecification)
                .withMetadata(metadata)
                .withSchedule(schedule);
        this._parentBatchClient.protocolLayer().jobSchedules().patch(jobScheduleId, param, options);
    }

    /**
     * Updates the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule
     * @param schedule The schedule according to which jobs will be created. If you do not specify this element, it is equivalent to passing the default schedule: that is, a single job scheduled to run immediately.
     * @param jobSpecification  The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        updateJobSchedule(jobScheduleId, schedule, jobSpecification, null, null);
    }

    /**
     * Updates the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule
     * @param schedule The schedule according to which jobs will be created. If you do not specify this element, it is equivalent to passing the default schedule: that is, a single job scheduled to run immediately.
     * @param jobSpecification The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification.
     * @param metadata A list of name-value pairs associated with the job schedule as metadata. If you do not specify this element, it takes the default value of an empty list; in effect, any existing metadata is deleted.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        updateJobSchedule(jobScheduleId, schedule, jobSpecification, metadata, null);
    }

    /**
     * Updates the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule
     * @param schedule The schedule according to which jobs will be created. If you do not specify this element, it is equivalent to passing the default schedule: that is, a single job scheduled to run immediately.
     * @param jobSpecification The details of the jobs to be created on this schedule. Updates affect only jobs that are started after the update has taken place. Any currently active job continues with the older specification.
     * @param metadata A list of name-value pairs associated with the job schedule as metadata. If you do not specify this element, it takes the default value of an empty list; in effect, any existing metadata is deleted.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleUpdateOptions options = new JobScheduleUpdateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobScheduleUpdateParameter param = new JobScheduleUpdateParameter()
                .withJobSpecification(jobSpecification)
                .withMetadata(metadata)
                .withSchedule(schedule);
        this._parentBatchClient.protocolLayer().jobSchedules().update(jobScheduleId, param, options);
    }

    /**
     * Disables the specified job schedule.  Disabled schedules do not create new jobs, but may be re-enabled later.
     *
     * @param jobScheduleId The ID of the job schedule
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void disableJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        disableJobSchedule(jobScheduleId, null);
    }

    /**
     * Disables the specified job schedule.  Disabled schedules do not create new jobs, but may be re-enabled later.
     *
     * @param jobScheduleId The ID of the job schedule
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void disableJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleDisableOptions options = new JobScheduleDisableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobSchedules().disable(jobScheduleId, options);
    }

    /**
     * Enables the specified job schedule, allowing jobs to be created according to its {@link Schedule}
     *
     * @param jobScheduleId The ID of the job schedule
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void enableJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        enableJobSchedule(jobScheduleId, null);
    }

    /**
     * Enables the specified job schedule, allowing jobs to be created according to its {@link Schedule}
     *
     * @param jobScheduleId The ID of the job schedule
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void enableJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleEnableOptions options = new JobScheduleEnableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobSchedules().enable(jobScheduleId, options);
    }

    /**
     * Terminates the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void terminateJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        terminateJobSchedule(jobScheduleId, null);
    }

    /**
     * Terminates the specified job schedule.
     *
     * @param jobScheduleId The ID of the job schedule
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void terminateJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleTerminateOptions options = new JobScheduleTerminateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobSchedules().terminate(jobScheduleId, options);
    }

    /**
     * Add a job schedule to the Batch account.
     *
     * @param jobScheduleId a string that uniquely identifies the schedule within the account.
     * @param schedule the schedule according to which jobs will be created.
     * @param jobSpecification the details of the jobs to be created on this schedule.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        createJobSchedule(jobScheduleId, schedule, jobSpecification, null);
    }

    /**
     * Add a job schedule to the Batch account.
     *
     * @param jobScheduleId A string that uniquely identifies the schedule within the account.
     * @param schedule The schedule according to which jobs will be created.
     * @param jobSpecification The details of the jobs to be created on this schedule.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleAddParameter param = new JobScheduleAddParameter()
                .withJobSpecification(jobSpecification)
                .withSchedule(schedule)
                .withId(jobScheduleId);
        createJobSchedule(param, additionalBehaviors);
    }

    /**
     * Add a job schedule to the Batch account.
     *
     * @param param The parameter to add a job schedule
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createJobSchedule(JobScheduleAddParameter param) throws BatchErrorException, IOException {
        createJobSchedule(param, null);
    }

    /**
     * Add a job schedule to the Batch account.
     *
     * @param param The parameter to add a job schedule
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public void createJobSchedule(JobScheduleAddParameter param, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleAddOptions options = new JobScheduleAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().jobSchedules().add(param, options);
    }

    /**
     * Enumerates the {@link CloudJobSchedule job schedules} in the Batch account.
     *
     * @return A collection of {@link CloudJobSchedule job schedules}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudJobSchedule> listJobSchedules() throws BatchErrorException, IOException {
        return listJobSchedules(null, null);
    }

    /**
     * Enumerates the {@link CloudJobSchedule job schedules} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A collection of {@link CloudJobSchedule job schedules}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudJobSchedule> listJobSchedules(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobSchedules(detailLevel, null);
    }

    /**
     * Enumerates the {@link CloudJobSchedule job schedules} in the Batch account.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link CloudJobSchedule job schedules}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<CloudJobSchedule> listJobSchedules(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleListOptions options = new JobScheduleListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<CloudJobSchedule>, JobScheduleListHeaders> response = this._parentBatchClient.protocolLayer().jobSchedules().list(options);

        return response.getBody();
    }

}
