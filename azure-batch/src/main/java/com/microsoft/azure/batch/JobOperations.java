/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

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
import com.microsoft.azure.batch.protocol.models.JobGetAllJobsLifetimeStatisticsHeaders;
import com.microsoft.azure.batch.protocol.models.JobGetAllJobsLifetimeStatisticsOptions;
import com.microsoft.azure.batch.protocol.models.JobGetHeaders;
import com.microsoft.azure.batch.protocol.models.JobGetOptions;
import com.microsoft.azure.batch.protocol.models.JobListFromJobScheduleHeaders;
import com.microsoft.azure.batch.protocol.models.JobListFromJobScheduleOptions;
import com.microsoft.azure.batch.protocol.models.JobListHeaders;
import com.microsoft.azure.batch.protocol.models.JobListOptions;
import com.microsoft.azure.batch.protocol.models.JobListPreparationAndReleaseTaskStatusHeaders;
import com.microsoft.azure.batch.protocol.models.JobListPreparationAndReleaseTaskStatusOptions;
import com.microsoft.azure.batch.protocol.models.JobPatchOptions;
import com.microsoft.azure.batch.protocol.models.JobPatchParameter;
import com.microsoft.azure.batch.protocol.models.JobPreparationAndReleaseTaskExecutionInformation;
import com.microsoft.azure.batch.protocol.models.JobStatistics;
import com.microsoft.azure.batch.protocol.models.JobTerminateOptions;
import com.microsoft.azure.batch.protocol.models.JobUpdateOptions;
import com.microsoft.azure.batch.protocol.models.JobUpdateParameter;
import com.microsoft.azure.batch.protocol.models.MetadataItem;
import com.microsoft.azure.batch.protocol.models.PoolInformation;
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

    public JobStatistics getAllJobsLifetimeStatistics() throws BatchErrorException, IOException {
        return getAllJobsLifetimeStatistics(null);
    }

    public JobStatistics getAllJobsLifetimeStatistics(Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobGetAllJobsLifetimeStatisticsOptions options = new JobGetAllJobsLifetimeStatisticsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<JobStatistics, JobGetAllJobsLifetimeStatisticsHeaders> response = this._parentBatchClient.getProtocolLayer().jobs().getAllJobsLifetimeStatistics(options);

        return response.getBody();
    }

    public CloudJob getJob(String jobId) throws BatchErrorException, IOException {
        return getJob(jobId, null, null);
    }

    public CloudJob getJob(String jobId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getJob(jobId, detailLevel, null);
    }

    public CloudJob getJob(String jobId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobGetOptions getJobOptions = new JobGetOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(getJobOptions);

        ServiceResponseWithHeaders<CloudJob, JobGetHeaders> response = this._parentBatchClient.getProtocolLayer().jobs().get(jobId, getJobOptions);
        return response.getBody();
    }

    public List<CloudJob> listJobs() throws BatchErrorException, IOException {
        return listJobs(null, (Iterable<BatchClientBehavior>) null);
    }

    public List<CloudJob> listJobs(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobs(detailLevel, (Iterable<BatchClientBehavior>) null);
    }

    public List<CloudJob> listJobs(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListOptions jobListOptions = new JobListOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(jobListOptions);

        ServiceResponseWithHeaders<PagedList<CloudJob>, JobListHeaders> response = this._parentBatchClient.getProtocolLayer().jobs().list(jobListOptions);

        return response.getBody();
    }

    public List<CloudJob> listJobs(String jobScheduleId) throws BatchErrorException, IOException {
        return listJobs(jobScheduleId, null, null);
    }

    public List<CloudJob> listJobs(String jobScheduleId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listJobs(jobScheduleId, detailLevel, null);
    }

    public List<CloudJob> listJobs(String jobScheduleId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListFromJobScheduleOptions jobListOptions = new JobListFromJobScheduleOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(jobListOptions);

        ServiceResponseWithHeaders<PagedList<CloudJob>, JobListFromJobScheduleHeaders> response = this._parentBatchClient.getProtocolLayer().jobs().listFromJobSchedule(jobScheduleId, jobListOptions);

        return response.getBody();
    }

    public List<JobPreparationAndReleaseTaskExecutionInformation> listPreparationAndReleaseTaskStatus(String jobId) throws BatchErrorException, IOException {
        return listPreparationAndReleaseTaskStatus(jobId, null);
    }

    public List<JobPreparationAndReleaseTaskExecutionInformation> listPreparationAndReleaseTaskStatus(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListPreparationAndReleaseTaskStatusOptions jobListOptions = new JobListPreparationAndReleaseTaskStatusOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(jobListOptions);

        ServiceResponseWithHeaders<PagedList<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> response = this._parentBatchClient.getProtocolLayer().jobs().listPreparationAndReleaseTaskStatus(jobId, jobListOptions);

        return response.getBody();
    }

    public void createJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        createJob(jobId, poolInfo, null);
    }

    public void createJob(String jobId, PoolInformation poolInfo, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobAddParameter param = new JobAddParameter()
                .withId(jobId)
                .withPoolInfo(poolInfo);

        createJob(param, additionalBehaviors);
    }

    public void createJob(JobAddParameter job) throws BatchErrorException, IOException {
        createJob(job, null);
    }

    public void createJob(JobAddParameter job, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobAddOptions options = new JobAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobs().add(job, options);
    }

    public void deleteJob(String jobId) throws BatchErrorException, IOException {
        deleteJob(jobId, null);
    }

    public void deleteJob(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobDeleteOptions options = new JobDeleteOptions();
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
        JobTerminateOptions options = new JobTerminateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobs().terminate(jobId, terminateReason, options);
    }

    public void enableJob(String jobId) throws BatchErrorException, IOException {
        enableJob(jobId, null);
    }

    public void enableJob(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobEnableOptions options = new JobEnableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobs().enable(jobId, options);
    }

    public void disableJob(String jobId, DisableJobOption disableJobOption) throws BatchErrorException, IOException {
        disableJob(jobId, disableJobOption, null);
    }

    public void disableJob(String jobId, DisableJobOption disableJobOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobDisableOptions options = new JobDisableOptions();
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
        JobUpdateOptions options = new JobUpdateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        JobUpdateParameter param = new JobUpdateParameter()
                .withPriority(priority)
                .withPoolInfo(poolInfo)
                .withConstraints(constraints)
                .withMetadata(metadata);

        this._parentBatchClient.getProtocolLayer().jobs().update(jobId, param, options);
    }

    public void patchJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        patchJob(jobId, poolInfo, null, null, null, null);
    }

    public void patchJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchJob(jobId, poolInfo, priority, constraints, metadata, null);
    }

    public void patchJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobPatchParameter param = new JobPatchParameter()
                .withPriority(priority)
                .withPoolInfo(poolInfo)
                .withConstraints(constraints)
                .withMetadata(metadata);

        patchJob(jobId, param, additionalBehaviors);
    }

    public void patchJob(String jobId, JobPatchParameter jobPatchParameter) throws BatchErrorException, IOException {
        patchJob(jobId, jobPatchParameter, null);
    }

    public void patchJob(String jobId, JobPatchParameter jobPatchParameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobPatchOptions options = new JobPatchOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().jobs().patch(jobId, jobPatchParameter, options);
    }
}
