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

public class JobScheduleOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    JobScheduleOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    @Override
    public Collection<BatchClientBehavior> getCustomBehaviors() {
        return _customBehaviors;
    }

    @Override
    public void setCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        this._customBehaviors = behaviors;
    }

    public boolean existsJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        return existsJobSchedule(jobScheduleId, null);
    }

    public boolean existsJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleExistsOptions options = new JobScheduleExistsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Boolean, JobScheduleExistsHeaders> response = this._parentBatchClient.getProtocolLayer().jobSchedules().exists(jobScheduleId, options);

        return response.getBody();
    }

    public void deleteJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        deleteJobSchedule(jobScheduleId, null);
    }

    public void deleteJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleDeleteOptions options = new JobScheduleDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobSchedules().delete(jobScheduleId, options);
    }

    public CloudJobSchedule getJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        return getJobSchedule(jobScheduleId, null, null);
    }

    public CloudJobSchedule getJobSchedule(String jobScheduleId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getJobSchedule(jobScheduleId, detailLevel, null);
    }

    public CloudJobSchedule getJobSchedule(String jobScheduleId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleGetOptions options = new JobScheduleGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudJobSchedule, JobScheduleGetHeaders> response = this._parentBatchClient.getProtocolLayer().jobSchedules().get(jobScheduleId, options);

        return response.getBody();
    }

    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        patchJobSchedule(jobScheduleId, schedule, jobSpecification, null, null);
    }

    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchJobSchedule(jobScheduleId, schedule, jobSpecification, metadata, null);
    }

    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobSchedulePatchOptions options = new JobSchedulePatchOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobSchedulePatchParameter param = new JobSchedulePatchParameter()
                .withJobSpecification(jobSpecification)
                .withMetadata(metadata)
                .withSchedule(schedule);
        this._parentBatchClient.getProtocolLayer().jobSchedules().patch(jobScheduleId, param, options);
    }

    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        updateJobSchedule(jobScheduleId, schedule, jobSpecification, null, null);
    }

    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        updateJobSchedule(jobScheduleId, schedule, jobSpecification, metadata, null);
    }

    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleUpdateOptions options = new JobScheduleUpdateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobScheduleUpdateParameter param = new JobScheduleUpdateParameter()
                .withJobSpecification(jobSpecification)
                .withMetadata(metadata)
                .withSchedule(schedule);
        this._parentBatchClient.getProtocolLayer().jobSchedules().update(jobScheduleId, param, options);
    }

    public void disableJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        disableJobSchedule(jobScheduleId, null);
    }

    public void disableJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleDisableOptions options = new JobScheduleDisableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobSchedules().disable(jobScheduleId, options);
    }

    public void enableJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        enableJobSchedule(jobScheduleId, null);
    }

    public void enableJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleEnableOptions options = new JobScheduleEnableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobSchedules().enable(jobScheduleId, options);
    }

    public void terminateJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        terminateJobSchedule(jobScheduleId, null);
    }

    public void terminateJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleTerminateOptions options = new JobScheduleTerminateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobSchedules().terminate(jobScheduleId, options);
    }

    public void createJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        createJobSchedule(jobScheduleId, schedule, jobSpecification, null);
    }

    public void createJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleAddParameter param = new JobScheduleAddParameter()
                .withJobSpecification(jobSpecification)
                .withSchedule(schedule)
                .withId(jobScheduleId);
        createJobSchedule(param, additionalBehaviors);
    }

    public void createJobSchedule(JobScheduleAddParameter param) throws BatchErrorException, IOException {
        createJobSchedule(param, null);
    }

    public void createJobSchedule(JobScheduleAddParameter param, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleAddOptions options = new JobScheduleAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobSchedules().add(param, options);
    }

    public List<CloudJobSchedule> listJobSchedules() throws BatchErrorException, IOException {
        return listJobSchedules(null, null);
    }

    public List<CloudJobSchedule> listJobSchedules(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobSchedules(detailLevel, null);
    }

    public List<CloudJobSchedule> listJobSchedules(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleListOptions options = new JobScheduleListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<CloudJobSchedule>, JobScheduleListHeaders> response = this._parentBatchClient.getProtocolLayer().jobSchedules().list(options);

        return response.getBody();
    }

}
