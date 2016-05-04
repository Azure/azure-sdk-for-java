/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.implementation.api.BatchErrorException;
import com.microsoft.azure.batch.protocol.implementation.api.CloudJobInner;
import com.microsoft.azure.batch.protocol.implementation.api.DisableJobOption;
import com.microsoft.azure.batch.protocol.implementation.api.JobAddOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobAddParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobConstraints;
import com.microsoft.azure.batch.protocol.implementation.api.JobDeleteOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobDisableOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobEnableOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobGetAllJobsLifetimeStatisticsHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobGetAllJobsLifetimeStatisticsOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobGetHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobGetOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobListFromJobScheduleHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobListFromJobScheduleOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobListHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobListOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobListPreparationAndReleaseTaskStatusHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobListPreparationAndReleaseTaskStatusOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobPatchOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobPatchParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobPreparationAndReleaseTaskExecutionInformationInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobStatisticsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobTerminateOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobUpdateOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.JobUpdateParameterInner;
import com.microsoft.azure.batch.protocol.implementation.api.MetadataItem;
import com.microsoft.azure.batch.protocol.implementation.api.PoolInformation;
import com.microsoft.rest.ServiceResponseWithHeaders;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class JobOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    public static final String SHA1_CERTIFICATE_ALGORITHM = "sha1";

    JobOperations(BatchClient batchClient, Collection<BatchClientBehavior> inheritedBehaviors) {
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

    public JobStatisticsInner getAllJobsLifetimeStatistics() throws BatchErrorException, IOException {
        return getAllJobsLifetimeStatistics(null);
    }

    public JobStatisticsInner getAllJobsLifetimeStatistics(Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobGetAllJobsLifetimeStatisticsOptionsInner options = new JobGetAllJobsLifetimeStatisticsOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<JobStatisticsInner, JobGetAllJobsLifetimeStatisticsHeadersInner> response = this._parentBatchClient.getProtocolLayer().jobs().getAllJobsLifetimeStatistics(options);

        return response.getBody();
    }

    public CloudJobInner getJob(String jobId) throws BatchErrorException, IOException {
        return getJob(jobId, null, null);
    }

    public CloudJobInner getJob(String jobId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getJob(jobId, detailLevel, null);
    }

    public CloudJobInner getJob(String jobId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobGetOptionsInner getJobOptions = new JobGetOptionsInner();

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(getJobOptions);

        ServiceResponseWithHeaders<CloudJobInner, JobGetHeadersInner> response = this._parentBatchClient.getProtocolLayer().jobs().get(jobId, getJobOptions);
        return response.getBody();
    }

    public List<CloudJobInner> listJobs() throws BatchErrorException, IOException {
        return listJobs(null, (Iterable<BatchClientBehavior>) null);
    }

    public List<CloudJobInner> listJobs(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobs(detailLevel, (Iterable<BatchClientBehavior>) null);
    }

    public List<CloudJobInner> listJobs(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListOptionsInner jobListOptions = new JobListOptionsInner();

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(jobListOptions);

        ServiceResponseWithHeaders<PagedList<CloudJobInner>, JobListHeadersInner> response = this._parentBatchClient.getProtocolLayer().jobs().list(jobListOptions);

        return response.getBody();
    }

    public List<CloudJobInner> listJobs(String jobScheduleId) throws BatchErrorException, IOException {
        return listJobs(jobScheduleId, null, null);
    }

    public List<CloudJobInner> listJobs(String jobScheduleId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobs(jobScheduleId, detailLevel, null);
    }

    public List<CloudJobInner> listJobs(String jobScheduleId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListFromJobScheduleOptionsInner jobListOptions = new JobListFromJobScheduleOptionsInner();

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(jobListOptions);

        ServiceResponseWithHeaders<PagedList<CloudJobInner>, JobListFromJobScheduleHeadersInner> response = this._parentBatchClient.getProtocolLayer().jobs().listFromJobSchedule(jobScheduleId, jobListOptions);

        return response.getBody();
    }

    public List<JobPreparationAndReleaseTaskExecutionInformationInner> listPreparationAndReleaseTaskStatus(String jobId) throws BatchErrorException, IOException {
        return listPreparationAndReleaseTaskStatus(jobId, null);
    }

    public List<JobPreparationAndReleaseTaskExecutionInformationInner> listPreparationAndReleaseTaskStatus(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListPreparationAndReleaseTaskStatusOptionsInner jobListOptions = new JobListPreparationAndReleaseTaskStatusOptionsInner();

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(jobListOptions);

        ServiceResponseWithHeaders<PagedList<JobPreparationAndReleaseTaskExecutionInformationInner>, JobListPreparationAndReleaseTaskStatusHeadersInner> response = this._parentBatchClient.getProtocolLayer().jobs().listPreparationAndReleaseTaskStatus(jobId, jobListOptions);

        return response.getBody();
    }

    public void createJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        createJob(jobId, poolInfo, null);
    }

    public void createJob(String jobId, PoolInformation poolInfo, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobAddParameterInner param = new JobAddParameterInner();
        param.setId(jobId);
        param.setPoolInfo(poolInfo);

        createJob(param, additionalBehaviors);
    }

    public void createJob(JobAddParameterInner job) throws BatchErrorException, IOException {
        createJob(job, null);
    }

    public void createJob(JobAddParameterInner job, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobAddOptionsInner options = new JobAddOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobs().add(job, options);
    }

    public void deleteJob(String jobId) throws BatchErrorException, IOException {
        deleteJob(jobId, null);
    }

    public void deleteJob(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobDeleteOptionsInner options = new JobDeleteOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobs().delete(jobId, options);
    }

    public void terminateJob(String jobId) throws BatchErrorException, IOException {
        terminateJob(jobId, null, null);
    }

    public void terminateJob(String jobId, String terminateReason) throws BatchErrorException, IOException {
        terminateJob(jobId, terminateReason, null);
    }

    public void terminateJob(String jobId, String terminateReason, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobTerminateOptionsInner options = new JobTerminateOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobs().terminate(jobId, terminateReason, options);
    }

    public void enableJob(String jobId) throws BatchErrorException, IOException {
        enableJob(jobId, null);
    }

    public void enableJob(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobEnableOptionsInner options = new JobEnableOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobs().enable(jobId, options);
    }

    public void disableJob(String jobId, DisableJobOption disableJobOption) throws BatchErrorException, IOException {
        disableJob(jobId, disableJobOption, null);
    }

    public void disableJob(String jobId, DisableJobOption disableJobOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobDisableOptionsInner options = new JobDisableOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobs().disable(jobId, disableJobOption, options);
    }

    public void updateJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        updateJob(jobId, poolInfo, null, null, null, null);
    }

    public void updateJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints) throws BatchErrorException, IOException {
        updateJob(jobId, poolInfo, priority, constraints, null, null);
    }

    public void updateJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        updateJob(jobId, poolInfo, priority, constraints, metadata, null);
    }

    public void updateJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobUpdateOptionsInner options = new JobUpdateOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobUpdateParameterInner param = new JobUpdateParameterInner();
        param.setPriority(priority);
        param.setPoolInfo(poolInfo);
        param.setConstraints(constraints);
        param.setMetadata(metadata);

        this._parentBatchClient.getProtocolLayer().jobs().update(jobId, param, options);
    }

    public void patchJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        patchJob(jobId, poolInfo, null, null, null, null);
    }

    public void patchJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchJob(jobId, poolInfo, priority, constraints, metadata, null);
    }

    public void patchJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobPatchParameterInner param = new JobPatchParameterInner();
        param.setPriority(priority);
        param.setPoolInfo(poolInfo);
        param.setConstraints(constraints);
        param.setMetadata(metadata);

        patchJob(jobId, param, additionalBehaviors);
    }

    public void patchJob(String jobId, JobPatchParameterInner jobPatchParameter) throws BatchErrorException, IOException {
        patchJob(jobId, jobPatchParameter, null);
    }

    public void patchJob(String jobId, JobPatchParameterInner jobPatchParameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobPatchOptionsInner options = new JobPatchOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobs().patch(jobId, jobPatchParameter, options);
    }
}
