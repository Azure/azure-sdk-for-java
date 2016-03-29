/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.*;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseWithHeaders;
import okhttp3.ResponseBody;
import retrofit2.Call;

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

        ServiceResponseWithHeaders<CloudJob, JobGetHeaders> response = this._parentBatchClient.getProtocolLayer().getJobOperations().get(jobId, getJobOptions);
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

        ServiceResponseWithHeaders<PagedList<CloudJob>, JobListHeaders> response = this._parentBatchClient.getProtocolLayer().getJobOperations().list(jobListOptions);

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

        ServiceResponseWithHeaders<PagedList<CloudJob>, JobListFromJobScheduleHeaders> response = this._parentBatchClient.getProtocolLayer().getJobOperations().listFromJobSchedule(jobScheduleId, jobListOptions);

        return response.getBody();
    }

    public List<JobPreparationAndReleaseTaskExecutionInformation> listPreparationAndReleaseTaskStatus(String jobId) throws BatchErrorException, IOException {
        return listPreparationAndReleaseTaskStatus(jobId, null);
    }

    public List<JobPreparationAndReleaseTaskExecutionInformation> listPreparationAndReleaseTaskStatus(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobListPreparationAndReleaseTaskStatusOptions jobListOptions = new JobListPreparationAndReleaseTaskStatusOptions();

        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(jobListOptions);

        ServiceResponseWithHeaders<PagedList<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> response = this._parentBatchClient.getProtocolLayer().getJobOperations().listPreparationAndReleaseTaskStatus(jobId, jobListOptions);

        return response.getBody();
    }

    public void createJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        createJob(jobId, poolInfo, null);
    }

    public void createJob(String jobId, PoolInformation poolInfo, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobAddParameter param = new JobAddParameter();
        param.setId(jobId);
        param.setPoolInfo(poolInfo);

        createJob(param, additionalBehaviors);
    }

    public void createJob(JobAddParameter job) throws BatchErrorException, IOException {
        createJob(job, null);
    }

    public void createJob(JobAddParameter job, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobAddOptions options = new JobAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().getJobOperations().add(job, options);
    }

    public void deleteJob(String jobId) throws BatchErrorException, IOException {
        deleteJob(jobId, null);
    }

    public void deleteJob(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobDeleteOptions options = new JobDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().getJobOperations().delete(jobId, options);
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

        this._parentBatchClient.getProtocolLayer().getJobOperations().terminate(jobId, terminateReason, options);
    }

    public void enableJob(String jobId) throws BatchErrorException, IOException {
        enableJob(jobId, null);
    }

    public void enableJob(String jobId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobEnableOptions options = new JobEnableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().getJobOperations().enable(jobId, options);
    }

    public void disableJob(String jobId, DisableJobOption disableJobOption) throws BatchErrorException, IOException {
        disableJob(jobId, disableJobOption, null);
    }

    public void disableJob(String jobId, DisableJobOption disableJobOption, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobDisableOptions options = new JobDisableOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().getJobOperations().disable(jobId, disableJobOption, options);
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

        JobUpdateParameter param = new JobUpdateParameter();
        param.setPriority(priority);
        param.setPoolInfo(poolInfo);
        param.setConstraints(constraints);
        param.setMetadata(metadata);

        this._parentBatchClient.getProtocolLayer().getJobOperations().update(jobId, param, options);
    }

    public void patchJob(String jobId, PoolInformation poolInfo) throws BatchErrorException, IOException {
        patchJob(jobId, poolInfo, null, null, null, null);
    }

    public void patchJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, List<MetadataItem> metadata) throws BatchErrorException, IOException {
        patchJob(jobId, poolInfo, priority, constraints, metadata, null);
    }

    public void patchJob(String jobId, PoolInformation poolInfo, Integer priority, JobConstraints constraints, List<MetadataItem> metadata, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobPatchParameter param = new JobPatchParameter();
        param.setPriority(priority);
        param.setPoolInfo(poolInfo);
        param.setConstraints(constraints);
        param.setMetadata(metadata);

        patchJob(jobId, param, additionalBehaviors);
    }

    public void patchJob(String jobId, JobPatchParameter jobPatchParameter) throws BatchErrorException, IOException {
        patchJob(jobId, jobPatchParameter, null);
    }

    public void patchJob(String jobId, JobPatchParameter jobPatchParameter, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        JobPatchOptions options = new JobPatchOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().getJobOperations().patch(jobId, jobPatchParameter, options);
    }
}
