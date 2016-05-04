/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.implementation.api.BatchErrorException;
import com.microsoft.azure.batch.protocol.implementation.api.CloudJobScheduleInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleAddOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleAddParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleDeleteOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleDisableOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleEnableOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleExistsHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleExistsOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleGetHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleGetOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleListHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleListOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobSchedulePatchOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobSchedulePatchParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleTerminateOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleUpdateOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobScheduleUpdateParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobSpecification;
import com.microsoft.azure.batch.protocol.implementation.api.MetadataItem;
import com.microsoft.azure.batch.protocol.implementation.api.Schedule;
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
        JobScheduleExistsOptionsInner options = new JobScheduleExistsOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Boolean, JobScheduleExistsHeadersInner> response = this._parentBatchClient.getProtocolLayer().jobSchedules().exists(jobScheduleId, options);

        return response.getBody();
    }

    public void deleteJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        deleteJobSchedule(jobScheduleId, null);
    }

    public void deleteJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleDeleteOptionsInner options = new JobScheduleDeleteOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobSchedules().delete(jobScheduleId, options);
    }

    public CloudJobScheduleInner getJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        return getJobSchedule(jobScheduleId, null, null);
    }

    public CloudJobScheduleInner getJobSchedule(String jobScheduleId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getJobSchedule(jobScheduleId, detailLevel, null);
    }

    public CloudJobScheduleInner getJobSchedule(String jobScheduleId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleGetOptionsInner options = new JobScheduleGetOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudJobScheduleInner, JobScheduleGetHeadersInner> response = this._parentBatchClient.getProtocolLayer().jobSchedules().get(jobScheduleId, options);

        return response.getBody();
    }

    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        patchJobSchedule(jobScheduleId, schedule, jobSpecification, null, null);
    }

    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchJobSchedule(jobScheduleId, schedule, jobSpecification, metadata, null);
    }

    public void patchJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobSchedulePatchOptionsInner options = new JobSchedulePatchOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobSchedulePatchParameterInner param = new JobSchedulePatchParameterInner();
        param.setJobSpecification(jobSpecification);
        param.setMetadata(metadata);
        param.setSchedule(schedule);
        this._parentBatchClient.getProtocolLayer().jobSchedules().patch(jobScheduleId, param, options);
    }

    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        updateJobSchedule(jobScheduleId, schedule, jobSpecification, null, null);
    }

    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        updateJobSchedule(jobScheduleId, schedule, jobSpecification, metadata, null);
    }

    public void updateJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleUpdateOptionsInner options = new JobScheduleUpdateOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobScheduleUpdateParameterInner param = new JobScheduleUpdateParameterInner();
        param.setJobSpecification(jobSpecification);
        param.setMetadata(metadata);
        param.setSchedule(schedule);
        this._parentBatchClient.getProtocolLayer().jobSchedules().update(jobScheduleId, param, options);
    }

    public void disableJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        disableJobSchedule(jobScheduleId, null);
    }

    public void disableJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleDisableOptionsInner options = new JobScheduleDisableOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobSchedules().disable(jobScheduleId, options);
    }

    public void enableJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        enableJobSchedule(jobScheduleId, null);
    }

    public void enableJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleEnableOptionsInner options = new JobScheduleEnableOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobSchedules().enable(jobScheduleId, options);
    }

    public void terminateJobSchedule(String jobScheduleId) throws BatchErrorException, IOException {
        terminateJobSchedule(jobScheduleId, null);
    }

    public void terminateJobSchedule(String jobScheduleId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleTerminateOptionsInner options = new JobScheduleTerminateOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobSchedules().terminate(jobScheduleId, options);
    }

    public void createJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification) throws BatchErrorException, IOException {
        createJobSchedule(jobScheduleId, schedule, jobSpecification, null);
    }

    public void createJobSchedule(String jobScheduleId, Schedule schedule, JobSpecification jobSpecification, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleAddParameterInner param = new JobScheduleAddParameterInner();
        param.setJobSpecification(jobSpecification);
        param.setSchedule(schedule);
        param.setId(jobScheduleId);
        createJobSchedule(param, additionalBehaviors);
    }

    public void createJobSchedule(JobScheduleAddParameterInner param) throws BatchErrorException, IOException {
        createJobSchedule(param, null);
    }

    public void createJobSchedule(JobScheduleAddParameterInner param, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleAddOptionsInner options = new JobScheduleAddOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobSchedules().add(param, options);
    }

    public List<CloudJobScheduleInner> listJobSchedules() throws BatchErrorException, IOException {
        return listJobSchedules(null, null);
    }

    public List<CloudJobScheduleInner> listJobSchedules(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobSchedules(detailLevel, null);
    }

    public List<CloudJobScheduleInner> listJobSchedules(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobScheduleListOptionsInner options = new JobScheduleListOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<CloudJobScheduleInner>, JobScheduleListHeadersInner> response = this._parentBatchClient.getProtocolLayer().jobSchedules().list(options);

        return response.getBody();
    }

}
