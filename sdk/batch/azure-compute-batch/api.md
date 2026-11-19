```java
maven {
    parent : com.azure:azure-client-sdk-parent:1.7.0
    properties : com.azure:azure-compute-batch:1.0.0
    name : Microsoft Azure client library for Batch Client
    description : This package contains the Microsoft Azure Batch data plane client library.
    dependencies {
        // compile scope
        com.azure:azure-core 1.61.0
        com.azure:azure-core-http-netty 1.17.0
        com.azure:azure-json 1.5.1
    }
}
module com.azure.compute.batch {
    requires transitive com.azure.core;
    exports com.azure.compute.batch;
    exports com.azure.compute.batch.models;
    opens com.azure.compute.batch.models to com.azure.core;
}
package com.azure.compute.batch {
    @ServiceClient(builder = BatchClientBuilder, isAsync = true)
    public final class BatchAsyncClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        @Generated public Mono<BatchApplication> getApplication(String applicationId)
        @Generated public Mono<BatchApplication> getApplication(String applicationId, BatchApplicationGetOptions options)
        @Generated public Mono<Response<BinaryData>> getApplicationWithResponse(String applicationId, RequestOptions requestOptions)
        public PollerFlux<BatchNode, BatchNode> beginDeallocateNode(String poolId, String nodeId)
        public PollerFlux<BatchNode, BatchNode> beginDeallocateNode(String poolId, String nodeId, BatchNodeDeallocateOptions options)
        public PollerFlux<BatchJob, BatchJob> beginDeleteJob(String jobId)
        public PollerFlux<BatchJob, BatchJob> beginDeleteJob(String jobId, BatchJobDeleteOptions options, RequestConditions requestConditions)
        public PollerFlux<BatchJobSchedule, BatchJobSchedule> beginDeleteJobSchedule(String jobScheduleId)
        public PollerFlux<BatchJobSchedule, BatchJobSchedule> beginDeleteJobSchedule(String jobScheduleId, BatchJobScheduleDeleteOptions options, RequestConditions requestConditions)
        public PollerFlux<BatchPool, BatchPool> beginDeletePool(String poolId)
        public PollerFlux<BatchPool, BatchPool> beginDeletePool(String poolId, BatchPoolDeleteOptions options, RequestConditions requestConditions)
        public PollerFlux<BatchJob, BatchJob> beginDisableJob(String jobId, BatchJobDisableParameters parameters)
        public PollerFlux<BatchJob, BatchJob> beginDisableJob(String jobId, BatchJobDisableParameters parameters, BatchJobDisableOptions options, RequestConditions requestConditions)
        public PollerFlux<BatchJob, BatchJob> beginEnableJob(String jobId)
        public PollerFlux<BatchJob, BatchJob> beginEnableJob(String jobId, BatchJobEnableOptions options, RequestConditions requestConditions)
        public PollerFlux<BatchNode, BatchNode> beginRebootNode(String poolId, String nodeId)
        public PollerFlux<BatchNode, BatchNode> beginRebootNode(String poolId, String nodeId, BatchNodeRebootOptions options)
        public PollerFlux<BatchNode, BatchNode> beginReimageNode(String poolId, String nodeId)
        public PollerFlux<BatchNode, BatchNode> beginReimageNode(String poolId, String nodeId, BatchNodeReimageOptions options)
        public PollerFlux<BatchPool, BatchPool> beginRemoveNodes(String poolId, BatchNodeRemoveParameters parameters)
        public PollerFlux<BatchPool, BatchPool> beginRemoveNodes(String poolId, BatchNodeRemoveParameters parameters, BatchNodesRemoveOptions options, RequestConditions requestConditions)
        public PollerFlux<BatchPool, BatchPool> beginResizePool(String poolId, BatchPoolResizeParameters parameters)
        public PollerFlux<BatchPool, BatchPool> beginResizePool(String poolId, BatchPoolResizeParameters parameters, BatchPoolResizeOptions options, RequestConditions requestConditions)
        public PollerFlux<BatchNode, BatchNode> beginStartNode(String poolId, String nodeId)
        public PollerFlux<BatchNode, BatchNode> beginStartNode(String poolId, String nodeId, BatchNodeStartOptions options)
        public PollerFlux<BatchPool, BatchPool> beginStopPoolResize(String poolId)
        public PollerFlux<BatchPool, BatchPool> beginStopPoolResize(String poolId, BatchPoolResizeStopOptions options, RequestConditions requestConditions)
        public PollerFlux<BatchJob, BatchJob> beginTerminateJob(String jobId)
        public PollerFlux<BatchJob, BatchJob> beginTerminateJob(String jobId, BatchJobTerminateOptions options, RequestConditions requestConditions)
        public PollerFlux<BatchJobSchedule, BatchJobSchedule> beginTerminateJobSchedule(String jobScheduleId)
        public PollerFlux<BatchJobSchedule, BatchJobSchedule> beginTerminateJobSchedule(String jobScheduleId, BatchJobScheduleTerminateOptions options, RequestConditions requestConditions)
        @Generated public Mono<Void> createJob(BatchJobCreateParameters job)
        @Generated public Mono<Void> createJob(BatchJobCreateParameters job, BatchJobCreateOptions options)
        @Generated public Mono<Void> createJobSchedule(BatchJobScheduleCreateParameters jobSchedule)
        @Generated public Mono<Void> createJobSchedule(BatchJobScheduleCreateParameters jobSchedule, BatchJobScheduleCreateOptions options)
        @Generated public Mono<Response<Void>> createJobScheduleWithResponse(BinaryData jobSchedule, RequestOptions requestOptions)
        @Generated public Mono<Response<Void>> createJobWithResponse(BinaryData job, RequestOptions requestOptions)
        @Generated public Mono<Void> createNodeUser(String poolId, String nodeId, BatchNodeUserCreateParameters user)
        @Generated public Mono<Void> createNodeUser(String poolId, String nodeId, BatchNodeUserCreateParameters user, BatchNodeUserCreateOptions options)
        @Generated public Mono<Response<Void>> createNodeUserWithResponse(String poolId, String nodeId, BinaryData user, RequestOptions requestOptions)
        @Generated public Mono<Void> createPool(BatchPoolCreateParameters pool)
        @Generated public Mono<Void> createPool(BatchPoolCreateParameters pool, BatchPoolCreateOptions options)
        @Generated public Mono<Response<Void>> createPoolWithResponse(BinaryData pool, RequestOptions requestOptions)
        @Generated public Mono<Void> createTask(String jobId, BatchTaskCreateParameters task)
        @Generated public Mono<Void> createTask(String jobId, BatchTaskCreateParameters task, BatchTaskCreateOptions options)
        @Generated public Mono<BatchCreateTaskCollectionResult> createTaskCollection(String jobId, BatchTaskGroup taskCollection)
        @Generated public Mono<BatchCreateTaskCollectionResult> createTaskCollection(String jobId, BatchTaskGroup taskCollection, BatchTaskCollectionCreateOptions options)
        @Generated public Mono<Response<BinaryData>> createTaskCollectionWithResponse(String jobId, BinaryData taskCollection, RequestOptions requestOptions)
        public Mono<Void> createTasks(String jobId, List<BatchTaskCreateParameters> taskList)
        public Mono<Void> createTasks(String jobId, List<BatchTaskCreateParameters> taskList, BatchTaskBulkCreateOptions batchTaskBulkCreateOptions)
        @Generated public Mono<Response<Void>> createTaskWithResponse(String jobId, BinaryData task, RequestOptions requestOptions)
        public Mono<Response<Void>> deallocateNodeWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        public Mono<Response<Void>> deleteJobScheduleWithResponse(String jobScheduleId, RequestOptions requestOptions)
        public Mono<Response<Void>> deleteJobWithResponse(String jobId, RequestOptions requestOptions)
        @Generated public Mono<Void> deleteNodeFile(String poolId, String nodeId, String filePath)
        @Generated public Mono<Void> deleteNodeFile(String poolId, String nodeId, String filePath, BatchNodeFileDeleteOptions options)
        @Generated public Mono<Response<Void>> deleteNodeFileWithResponse(String poolId, String nodeId, String filePath, RequestOptions requestOptions)
        @Generated public Mono<Void> deleteNodeUser(String poolId, String nodeId, String username)
        @Generated public Mono<Void> deleteNodeUser(String poolId, String nodeId, String username, BatchNodeUserDeleteOptions options)
        @Generated public Mono<Response<Void>> deleteNodeUserWithResponse(String poolId, String nodeId, String username, RequestOptions requestOptions)
        public Mono<Response<Void>> deletePoolWithResponse(String poolId, RequestOptions requestOptions)
        @Generated public Mono<Void> deleteTask(String jobId, String taskId)
        @Generated public Mono<Void> deleteTask(String jobId, String taskId, BatchTaskDeleteOptions options, RequestConditions requestConditions)
        @Generated public Mono<Void> deleteTaskFile(String jobId, String taskId, String filePath)
        @Generated public Mono<Void> deleteTaskFile(String jobId, String taskId, String filePath, BatchTaskFileDeleteOptions options)
        @Generated public Mono<Response<Void>> deleteTaskFileWithResponse(String jobId, String taskId, String filePath, RequestOptions requestOptions)
        @Generated public Mono<Response<Void>> deleteTaskWithResponse(String jobId, String taskId, RequestOptions requestOptions)
        @Generated public Mono<Void> disableJobSchedule(String jobScheduleId)
        @Generated public Mono<Void> disableJobSchedule(String jobScheduleId, BatchJobScheduleDisableOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Void>> disableJobScheduleWithResponse(String jobScheduleId, RequestOptions requestOptions)
        public Mono<Response<Void>> disableJobWithResponse(String jobId, BinaryData parameters, RequestOptions requestOptions)
        @Generated public Mono<Void> disableNodeScheduling(String poolId, String nodeId)
        @Generated public Mono<Void> disableNodeScheduling(String poolId, String nodeId, BatchNodeSchedulingDisableOptions options)
        @Generated public Mono<Response<Void>> disableNodeSchedulingWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public Mono<Void> disablePoolAutoScale(String poolId)
        @Generated public Mono<Void> disablePoolAutoScale(String poolId, BatchPoolDisableAutoScaleOptions options)
        @Generated public Mono<Response<Void>> disablePoolAutoScaleWithResponse(String poolId, RequestOptions requestOptions)
        @Generated public Mono<Void> enableJobSchedule(String jobScheduleId)
        @Generated public Mono<Void> enableJobSchedule(String jobScheduleId, BatchJobScheduleEnableOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Void>> enableJobScheduleWithResponse(String jobScheduleId, RequestOptions requestOptions)
        public Mono<Response<Void>> enableJobWithResponse(String jobId, RequestOptions requestOptions)
        @Generated public Mono<Void> enableNodeScheduling(String poolId, String nodeId)
        @Generated public Mono<Void> enableNodeScheduling(String poolId, String nodeId, BatchNodeSchedulingEnableOptions options)
        @Generated public Mono<Response<Void>> enableNodeSchedulingWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public Mono<Void> enablePoolAutoScale(String poolId, BatchPoolAutoScaleEnableParameters parameters)
        @Generated public Mono<Void> enablePoolAutoScale(String poolId, BatchPoolAutoScaleEnableParameters parameters, BatchPoolAutoScaleEnableOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Void>> enablePoolAutoScaleWithResponse(String poolId, BinaryData parameters, RequestOptions requestOptions)
        @Generated public Mono<AutoScaleRun> evaluatePoolAutoScale(String poolId, BatchPoolEvaluateAutoScaleParameters parameters)
        @Generated public Mono<AutoScaleRun> evaluatePoolAutoScale(String poolId, BatchPoolEvaluateAutoScaleParameters parameters, BatchPoolEvaluateAutoScaleOptions options)
        @Generated public Mono<Response<BinaryData>> evaluatePoolAutoScaleWithResponse(String poolId, BinaryData parameters, RequestOptions requestOptions)
        @Generated public Mono<BatchJob> getJob(String jobId)
        @Generated public Mono<BatchJob> getJob(String jobId, BatchJobGetOptions options, RequestConditions requestConditions)
        @Generated public Mono<BatchJobSchedule> getJobSchedule(String jobScheduleId)
        @Generated public Mono<BatchJobSchedule> getJobSchedule(String jobScheduleId, BatchJobScheduleGetOptions options, RequestConditions requestConditions)
        @Generated public Mono<Boolean> jobScheduleExists(String jobScheduleId)
        @Generated public Mono<Boolean> jobScheduleExists(String jobScheduleId, BatchJobScheduleExistsOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Boolean>> jobScheduleExistsWithResponse(String jobScheduleId, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getJobScheduleWithResponse(String jobScheduleId, RequestOptions requestOptions)
        @Generated public Mono<BatchTaskCountsResult> getJobTaskCounts(String jobId)
        @Generated public Mono<BatchTaskCountsResult> getJobTaskCounts(String jobId, BatchJobTaskCountsGetOptions options)
        @Generated public Mono<Response<BinaryData>> getJobTaskCountsWithResponse(String jobId, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getJobWithResponse(String jobId, RequestOptions requestOptions)
        @Generated public PagedFlux<BatchApplication> listApplications()
        @Generated public PagedFlux<BinaryData> listApplications(RequestOptions requestOptions)
        @Generated public PagedFlux<BatchApplication> listApplications(BatchApplicationsListOptions options)
        @Generated public PagedFlux<BatchJobPreparationAndReleaseTaskStatus> listJobPreparationAndReleaseTaskStatus(String jobId)
        @Generated public PagedFlux<BinaryData> listJobPreparationAndReleaseTaskStatus(String jobId, RequestOptions requestOptions)
        @Generated public PagedFlux<BatchJobPreparationAndReleaseTaskStatus> listJobPreparationAndReleaseTaskStatus(String jobId, BatchJobPreparationAndReleaseTaskStatusListOptions options)
        @Generated public PagedFlux<BatchJob> listJobs()
        @Generated public PagedFlux<BinaryData> listJobs(RequestOptions requestOptions)
        @Generated public PagedFlux<BatchJob> listJobs(BatchJobsListOptions options)
        @Generated public PagedFlux<BatchJobSchedule> listJobSchedules()
        @Generated public PagedFlux<BinaryData> listJobSchedules(RequestOptions requestOptions)
        @Generated public PagedFlux<BatchJobSchedule> listJobSchedules(BatchJobSchedulesListOptions options)
        @Generated public PagedFlux<BatchJob> listJobsFromSchedule(String jobScheduleId)
        @Generated public PagedFlux<BinaryData> listJobsFromSchedule(String jobScheduleId, RequestOptions requestOptions)
        @Generated public PagedFlux<BatchJob> listJobsFromSchedule(String jobScheduleId, BatchJobsFromScheduleListOptions options)
        @Generated public PagedFlux<BatchNodeVMExtension> listNodeExtensions(String poolId, String nodeId)
        @Generated public PagedFlux<BinaryData> listNodeExtensions(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public PagedFlux<BatchNodeVMExtension> listNodeExtensions(String poolId, String nodeId, BatchNodeExtensionsListOptions options)
        @Generated public PagedFlux<BatchNodeFile> listNodeFiles(String poolId, String nodeId)
        @Generated public PagedFlux<BinaryData> listNodeFiles(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public PagedFlux<BatchNodeFile> listNodeFiles(String poolId, String nodeId, BatchNodeFilesListOptions options)
        @Generated public PagedFlux<BatchNode> listNodes(String poolId)
        @Generated public PagedFlux<BinaryData> listNodes(String poolId, RequestOptions requestOptions)
        @Generated public PagedFlux<BatchNode> listNodes(String poolId, BatchNodesListOptions options)
        @Generated public PagedFlux<BatchPoolNodeCounts> listPoolNodeCounts()
        @Generated public PagedFlux<BinaryData> listPoolNodeCounts(RequestOptions requestOptions)
        @Generated public PagedFlux<BatchPoolNodeCounts> listPoolNodeCounts(BatchPoolNodeCountsListOptions options)
        @Generated public PagedFlux<BatchPool> listPools()
        @Generated public PagedFlux<BinaryData> listPools(RequestOptions requestOptions)
        @Generated public PagedFlux<BatchPool> listPools(BatchPoolsListOptions options)
        @Generated public PagedFlux<BatchSubtask> listSubTasks(String jobId, String taskId)
        @Generated public PagedFlux<BinaryData> listSubTasks(String jobId, String taskId, RequestOptions requestOptions)
        @Generated public PagedFlux<BatchSubtask> listSubTasks(String jobId, String taskId, BatchSubTasksListOptions options)
        @Generated public PagedFlux<BatchSupportedImage> listSupportedImages()
        @Generated public PagedFlux<BinaryData> listSupportedImages(RequestOptions requestOptions)
        @Generated public PagedFlux<BatchSupportedImage> listSupportedImages(SupportedBatchImagesListOptions options)
        @Generated public PagedFlux<BatchNodeFile> listTaskFiles(String jobId, String taskId)
        @Generated public PagedFlux<BinaryData> listTaskFiles(String jobId, String taskId, RequestOptions requestOptions)
        @Generated public PagedFlux<BatchNodeFile> listTaskFiles(String jobId, String taskId, BatchTaskFilesListOptions options)
        @Generated public PagedFlux<BatchTask> listTasks(String jobId)
        @Generated public PagedFlux<BinaryData> listTasks(String jobId, RequestOptions requestOptions)
        @Generated public PagedFlux<BatchTask> listTasks(String jobId, BatchTasksListOptions options)
        @Generated public Mono<BatchNode> getNode(String poolId, String nodeId)
        @Generated public Mono<BatchNode> getNode(String poolId, String nodeId, BatchNodeGetOptions options)
        @Generated public Mono<BatchNodeVMExtension> getNodeExtension(String poolId, String nodeId, String extensionName)
        @Generated public Mono<BatchNodeVMExtension> getNodeExtension(String poolId, String nodeId, String extensionName, BatchNodeExtensionGetOptions options)
        @Generated public Mono<Response<BinaryData>> getNodeExtensionWithResponse(String poolId, String nodeId, String extensionName, RequestOptions requestOptions)
        @Generated public Mono<BinaryData> getNodeFile(String poolId, String nodeId, String filePath)
        @Generated public Mono<BinaryData> getNodeFile(String poolId, String nodeId, String filePath, BatchNodeFileGetOptions options)
        public Mono<BatchFileProperties> getNodeFileProperties(String poolId, String nodeId, String filePath)
        public Mono<BatchFileProperties> getNodeFileProperties(String poolId, String nodeId, String filePath, BatchNodeFilePropertiesGetOptions options)
        @Generated public Mono<Response<Void>> getNodeFilePropertiesWithResponse(String poolId, String nodeId, String filePath, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getNodeFileWithResponse(String poolId, String nodeId, String filePath, RequestOptions requestOptions)
        @Generated public Mono<BatchNodeRemoteLoginSettings> getNodeRemoteLoginSettings(String poolId, String nodeId)
        @Generated public Mono<BatchNodeRemoteLoginSettings> getNodeRemoteLoginSettings(String poolId, String nodeId, BatchNodeRemoteLoginSettingsGetOptions options)
        @Generated public Mono<Response<BinaryData>> getNodeRemoteLoginSettingsWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getNodeWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public Mono<BatchPool> getPool(String poolId)
        @Generated public Mono<BatchPool> getPool(String poolId, BatchPoolGetOptions options, RequestConditions requestConditions)
        @Generated public Mono<Boolean> poolExists(String poolId)
        @Generated public Mono<Boolean> poolExists(String poolId, BatchPoolExistsOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Boolean>> poolExistsWithResponse(String poolId, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getPoolWithResponse(String poolId, RequestOptions requestOptions)
        @Generated public Mono<Void> reactivateTask(String jobId, String taskId)
        @Generated public Mono<Void> reactivateTask(String jobId, String taskId, BatchTaskReactivateOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Void>> reactivateTaskWithResponse(String jobId, String taskId, RequestOptions requestOptions)
        public Mono<Response<Void>> rebootNodeWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        public Mono<Response<Void>> reimageNodeWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        public Mono<Response<Void>> removeNodesWithResponse(String poolId, BinaryData parameters, RequestOptions requestOptions)
        @Generated public Mono<Void> replaceJob(String jobId, BatchJob job)
        @Generated public Mono<Void> replaceJob(String jobId, BatchJob job, BatchJobReplaceOptions options, RequestConditions requestConditions)
        @Generated public Mono<Void> replaceJobSchedule(String jobScheduleId, BatchJobSchedule jobSchedule)
        @Generated public Mono<Void> replaceJobSchedule(String jobScheduleId, BatchJobSchedule jobSchedule, BatchJobScheduleReplaceOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Void>> replaceJobScheduleWithResponse(String jobScheduleId, BinaryData jobSchedule, RequestOptions requestOptions)
        @Generated public Mono<Response<Void>> replaceJobWithResponse(String jobId, BinaryData job, RequestOptions requestOptions)
        @Generated public Mono<Void> replaceNodeUser(String poolId, String nodeId, String username, BatchNodeUserUpdateParameters parameters)
        @Generated public Mono<Void> replaceNodeUser(String poolId, String nodeId, String username, BatchNodeUserUpdateParameters parameters, BatchNodeUserReplaceOptions options)
        @Generated public Mono<Response<Void>> replaceNodeUserWithResponse(String poolId, String nodeId, String username, BinaryData parameters, RequestOptions requestOptions)
        @Generated public Mono<Void> replacePoolProperties(String poolId, BatchPoolReplaceParameters pool)
        @Generated public Mono<Void> replacePoolProperties(String poolId, BatchPoolReplaceParameters pool, BatchPoolPropertiesReplaceOptions options)
        @Generated public Mono<Response<Void>> replacePoolPropertiesWithResponse(String poolId, BinaryData pool, RequestOptions requestOptions)
        @Generated public Mono<Void> replaceTask(String jobId, String taskId, BatchTask task)
        @Generated public Mono<Void> replaceTask(String jobId, String taskId, BatchTask task, BatchTaskReplaceOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Void>> replaceTaskWithResponse(String jobId, String taskId, BinaryData task, RequestOptions requestOptions)
        public Mono<Response<Void>> resizePoolWithResponse(String poolId, BinaryData parameters, RequestOptions requestOptions)
        public Mono<Response<Void>> startNodeWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        public Mono<Response<Void>> stopPoolResizeWithResponse(String poolId, RequestOptions requestOptions)
        @Generated public Mono<BatchTask> getTask(String jobId, String taskId)
        @Generated public Mono<BatchTask> getTask(String jobId, String taskId, BatchTaskGetOptions options, RequestConditions requestConditions)
        @Generated public Mono<BinaryData> getTaskFile(String jobId, String taskId, String filePath)
        @Generated public Mono<BinaryData> getTaskFile(String jobId, String taskId, String filePath, BatchTaskFileGetOptions options)
        public Mono<BatchFileProperties> getTaskFileProperties(String jobId, String taskId, String filePath)
        public Mono<BatchFileProperties> getTaskFileProperties(String jobId, String taskId, String filePath, BatchTaskFilePropertiesGetOptions options)
        @Generated public Mono<Response<Void>> getTaskFilePropertiesWithResponse(String jobId, String taskId, String filePath, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getTaskFileWithResponse(String jobId, String taskId, String filePath, RequestOptions requestOptions)
        @Generated public Mono<Response<BinaryData>> getTaskWithResponse(String jobId, String taskId, RequestOptions requestOptions)
        public Mono<Response<Void>> terminateJobScheduleWithResponse(String jobScheduleId, RequestOptions requestOptions)
        public Mono<Response<Void>> terminateJobWithResponse(String jobId, RequestOptions requestOptions)
        @Generated public Mono<Void> terminateTask(String jobId, String taskId)
        @Generated public Mono<Void> terminateTask(String jobId, String taskId, BatchTaskTerminateOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Void>> terminateTaskWithResponse(String jobId, String taskId, RequestOptions requestOptions)
        @Generated public Mono<Void> updateJob(String jobId, BatchJobUpdateParameters job)
        @Generated public Mono<Void> updateJob(String jobId, BatchJobUpdateParameters job, BatchJobUpdateOptions options, RequestConditions requestConditions)
        @Generated public Mono<Void> updateJobSchedule(String jobScheduleId, BatchJobScheduleUpdateParameters jobSchedule)
        @Generated public Mono<Void> updateJobSchedule(String jobScheduleId, BatchJobScheduleUpdateParameters jobSchedule, BatchJobScheduleUpdateOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Void>> updateJobScheduleWithResponse(String jobScheduleId, BinaryData jobSchedule, RequestOptions requestOptions)
        @Generated public Mono<Response<Void>> updateJobWithResponse(String jobId, BinaryData job, RequestOptions requestOptions)
        @Generated public Mono<Void> updatePool(String poolId, BatchPoolUpdateParameters pool)
        @Generated public Mono<Void> updatePool(String poolId, BatchPoolUpdateParameters pool, BatchPoolUpdateOptions options, RequestConditions requestConditions)
        @Generated public Mono<Response<Void>> updatePoolWithResponse(String poolId, BinaryData pool, RequestOptions requestOptions)
        @Generated public Mono<UploadBatchServiceLogsResult> uploadNodeLogs(String poolId, String nodeId, UploadBatchServiceLogsParameters parameters)
        @Generated public Mono<UploadBatchServiceLogsResult> uploadNodeLogs(String poolId, String nodeId, UploadBatchServiceLogsParameters parameters, BatchNodeLogsUploadOptions options)
        @Generated public Mono<Response<BinaryData>> uploadNodeLogsWithResponse(String poolId, String nodeId, BinaryData parameters, RequestOptions requestOptions)
    }
    @ServiceClient(builder = BatchClientBuilder)
    public final class BatchClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        @Generated public BatchApplication getApplication(String applicationId)
        @Generated public BatchApplication getApplication(String applicationId, BatchApplicationGetOptions options)
        @Generated public Response<BinaryData> getApplicationWithResponse(String applicationId, RequestOptions requestOptions)
        public SyncPoller<BatchNode, BatchNode> beginDeallocateNode(String poolId, String nodeId)
        public SyncPoller<BatchNode, BatchNode> beginDeallocateNode(String poolId, String nodeId, BatchNodeDeallocateOptions options)
        public SyncPoller<BatchJob, BatchJob> beginDeleteJob(String jobId)
        public SyncPoller<BatchJob, BatchJob> beginDeleteJob(String jobId, BatchJobDeleteOptions options, RequestConditions requestConditions)
        public SyncPoller<BatchJobSchedule, BatchJobSchedule> beginDeleteJobSchedule(String jobScheduleId)
        public SyncPoller<BatchJobSchedule, BatchJobSchedule> beginDeleteJobSchedule(String jobScheduleId, BatchJobScheduleDeleteOptions options, RequestConditions requestConditions)
        public SyncPoller<BatchPool, BatchPool> beginDeletePool(String poolId)
        public SyncPoller<BatchPool, BatchPool> beginDeletePool(String poolId, BatchPoolDeleteOptions options, RequestConditions requestConditions)
        public SyncPoller<BatchJob, BatchJob> beginDisableJob(String jobId, BatchJobDisableParameters parameters)
        public SyncPoller<BatchJob, BatchJob> beginDisableJob(String jobId, BatchJobDisableParameters parameters, BatchJobDisableOptions options, RequestConditions requestConditions)
        public SyncPoller<BatchJob, BatchJob> beginEnableJob(String jobId)
        public SyncPoller<BatchJob, BatchJob> beginEnableJob(String jobId, BatchJobEnableOptions options, RequestConditions requestConditions)
        public SyncPoller<BatchNode, BatchNode> beginRebootNode(String poolId, String nodeId)
        public SyncPoller<BatchNode, BatchNode> beginRebootNode(String poolId, String nodeId, BatchNodeRebootOptions options)
        public SyncPoller<BatchNode, BatchNode> beginReimageNode(String poolId, String nodeId)
        public SyncPoller<BatchNode, BatchNode> beginReimageNode(String poolId, String nodeId, BatchNodeReimageOptions options)
        public SyncPoller<BatchPool, BatchPool> beginRemoveNodes(String poolId, BatchNodeRemoveParameters parameters)
        public SyncPoller<BatchPool, BatchPool> beginRemoveNodes(String poolId, BatchNodeRemoveParameters parameters, BatchNodesRemoveOptions options, RequestConditions requestConditions)
        public SyncPoller<BatchPool, BatchPool> beginResizePool(String poolId, BatchPoolResizeParameters parameters)
        public SyncPoller<BatchPool, BatchPool> beginResizePool(String poolId, BatchPoolResizeParameters parameters, BatchPoolResizeOptions options, RequestConditions requestConditions)
        public SyncPoller<BatchNode, BatchNode> beginStartNode(String poolId, String nodeId)
        public SyncPoller<BatchNode, BatchNode> beginStartNode(String poolId, String nodeId, BatchNodeStartOptions options)
        public SyncPoller<BatchPool, BatchPool> beginStopPoolResize(String poolId)
        public SyncPoller<BatchPool, BatchPool> beginStopPoolResize(String poolId, BatchPoolResizeStopOptions options, RequestConditions requestConditions)
        public SyncPoller<BatchJob, BatchJob> beginTerminateJob(String jobId)
        public SyncPoller<BatchJob, BatchJob> beginTerminateJob(String jobId, BatchJobTerminateOptions options, RequestConditions requestConditions)
        public SyncPoller<BatchJobSchedule, BatchJobSchedule> beginTerminateJobSchedule(String jobScheduleId)
        public SyncPoller<BatchJobSchedule, BatchJobSchedule> beginTerminateJobSchedule(String jobScheduleId, BatchJobScheduleTerminateOptions options, RequestConditions requestConditions)
        @Generated public void createJob(BatchJobCreateParameters job)
        @Generated public void createJob(BatchJobCreateParameters job, BatchJobCreateOptions options)
        @Generated public void createJobSchedule(BatchJobScheduleCreateParameters jobSchedule)
        @Generated public void createJobSchedule(BatchJobScheduleCreateParameters jobSchedule, BatchJobScheduleCreateOptions options)
        @Generated public Response<Void> createJobScheduleWithResponse(BinaryData jobSchedule, RequestOptions requestOptions)
        @Generated public Response<Void> createJobWithResponse(BinaryData job, RequestOptions requestOptions)
        @Generated public void createNodeUser(String poolId, String nodeId, BatchNodeUserCreateParameters user)
        @Generated public void createNodeUser(String poolId, String nodeId, BatchNodeUserCreateParameters user, BatchNodeUserCreateOptions options)
        @Generated public Response<Void> createNodeUserWithResponse(String poolId, String nodeId, BinaryData user, RequestOptions requestOptions)
        @Generated public void createPool(BatchPoolCreateParameters pool)
        @Generated public void createPool(BatchPoolCreateParameters pool, BatchPoolCreateOptions options)
        @Generated public Response<Void> createPoolWithResponse(BinaryData pool, RequestOptions requestOptions)
        @Generated public void createTask(String jobId, BatchTaskCreateParameters task)
        @Generated public void createTask(String jobId, BatchTaskCreateParameters task, BatchTaskCreateOptions options)
        @Generated public BatchCreateTaskCollectionResult createTaskCollection(String jobId, BatchTaskGroup taskCollection)
        @Generated public BatchCreateTaskCollectionResult createTaskCollection(String jobId, BatchTaskGroup taskCollection, BatchTaskCollectionCreateOptions options)
        @Generated public Response<BinaryData> createTaskCollectionWithResponse(String jobId, BinaryData taskCollection, RequestOptions requestOptions)
        public void createTasks(String jobId, List<BatchTaskCreateParameters> taskList)
        public void createTasks(String jobId, List<BatchTaskCreateParameters> taskList, BatchTaskBulkCreateOptions batchTaskBulkCreateOptions)
        @Generated public Response<Void> createTaskWithResponse(String jobId, BinaryData task, RequestOptions requestOptions)
        @Generated public void deleteNodeFile(String poolId, String nodeId, String filePath)
        @Generated public void deleteNodeFile(String poolId, String nodeId, String filePath, BatchNodeFileDeleteOptions options)
        @Generated public Response<Void> deleteNodeFileWithResponse(String poolId, String nodeId, String filePath, RequestOptions requestOptions)
        @Generated public void deleteNodeUser(String poolId, String nodeId, String username)
        @Generated public void deleteNodeUser(String poolId, String nodeId, String username, BatchNodeUserDeleteOptions options)
        @Generated public Response<Void> deleteNodeUserWithResponse(String poolId, String nodeId, String username, RequestOptions requestOptions)
        @Generated public void deleteTask(String jobId, String taskId)
        @Generated public void deleteTask(String jobId, String taskId, BatchTaskDeleteOptions options, RequestConditions requestConditions)
        @Generated public void deleteTaskFile(String jobId, String taskId, String filePath)
        @Generated public void deleteTaskFile(String jobId, String taskId, String filePath, BatchTaskFileDeleteOptions options)
        @Generated public Response<Void> deleteTaskFileWithResponse(String jobId, String taskId, String filePath, RequestOptions requestOptions)
        @Generated public Response<Void> deleteTaskWithResponse(String jobId, String taskId, RequestOptions requestOptions)
        @Generated public void disableJobSchedule(String jobScheduleId)
        @Generated public void disableJobSchedule(String jobScheduleId, BatchJobScheduleDisableOptions options, RequestConditions requestConditions)
        @Generated public Response<Void> disableJobScheduleWithResponse(String jobScheduleId, RequestOptions requestOptions)
        @Generated public void disableNodeScheduling(String poolId, String nodeId)
        @Generated public void disableNodeScheduling(String poolId, String nodeId, BatchNodeSchedulingDisableOptions options)
        @Generated public Response<Void> disableNodeSchedulingWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public void disablePoolAutoScale(String poolId)
        @Generated public void disablePoolAutoScale(String poolId, BatchPoolDisableAutoScaleOptions options)
        @Generated public Response<Void> disablePoolAutoScaleWithResponse(String poolId, RequestOptions requestOptions)
        @Generated public void enableJobSchedule(String jobScheduleId)
        @Generated public void enableJobSchedule(String jobScheduleId, BatchJobScheduleEnableOptions options, RequestConditions requestConditions)
        @Generated public Response<Void> enableJobScheduleWithResponse(String jobScheduleId, RequestOptions requestOptions)
        @Generated public void enableNodeScheduling(String poolId, String nodeId)
        @Generated public void enableNodeScheduling(String poolId, String nodeId, BatchNodeSchedulingEnableOptions options)
        @Generated public Response<Void> enableNodeSchedulingWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public void enablePoolAutoScale(String poolId, BatchPoolAutoScaleEnableParameters parameters)
        @Generated public void enablePoolAutoScale(String poolId, BatchPoolAutoScaleEnableParameters parameters, BatchPoolAutoScaleEnableOptions options, RequestConditions requestConditions)
        @Generated public Response<Void> enablePoolAutoScaleWithResponse(String poolId, BinaryData parameters, RequestOptions requestOptions)
        @Generated public AutoScaleRun evaluatePoolAutoScale(String poolId, BatchPoolEvaluateAutoScaleParameters parameters)
        @Generated public AutoScaleRun evaluatePoolAutoScale(String poolId, BatchPoolEvaluateAutoScaleParameters parameters, BatchPoolEvaluateAutoScaleOptions options)
        @Generated public Response<BinaryData> evaluatePoolAutoScaleWithResponse(String poolId, BinaryData parameters, RequestOptions requestOptions)
        @Generated public BatchJob getJob(String jobId)
        @Generated public BatchJob getJob(String jobId, BatchJobGetOptions options, RequestConditions requestConditions)
        @Generated public BatchJobSchedule getJobSchedule(String jobScheduleId)
        @Generated public BatchJobSchedule getJobSchedule(String jobScheduleId, BatchJobScheduleGetOptions options, RequestConditions requestConditions)
        @Generated public boolean jobScheduleExists(String jobScheduleId)
        @Generated public boolean jobScheduleExists(String jobScheduleId, BatchJobScheduleExistsOptions options, RequestConditions requestConditions)
        @Generated public Response<Boolean> jobScheduleExistsWithResponse(String jobScheduleId, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getJobScheduleWithResponse(String jobScheduleId, RequestOptions requestOptions)
        @Generated public BatchTaskCountsResult getJobTaskCounts(String jobId)
        @Generated public BatchTaskCountsResult getJobTaskCounts(String jobId, BatchJobTaskCountsGetOptions options)
        @Generated public Response<BinaryData> getJobTaskCountsWithResponse(String jobId, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getJobWithResponse(String jobId, RequestOptions requestOptions)
        @Generated public PagedIterable<BatchApplication> listApplications()
        @Generated public PagedIterable<BinaryData> listApplications(RequestOptions requestOptions)
        @Generated public PagedIterable<BatchApplication> listApplications(BatchApplicationsListOptions options)
        @Generated public PagedIterable<BatchJobPreparationAndReleaseTaskStatus> listJobPreparationAndReleaseTaskStatus(String jobId)
        @Generated public PagedIterable<BinaryData> listJobPreparationAndReleaseTaskStatus(String jobId, RequestOptions requestOptions)
        @Generated public PagedIterable<BatchJobPreparationAndReleaseTaskStatus> listJobPreparationAndReleaseTaskStatus(String jobId, BatchJobPreparationAndReleaseTaskStatusListOptions options)
        @Generated public PagedIterable<BatchJob> listJobs()
        @Generated public PagedIterable<BinaryData> listJobs(RequestOptions requestOptions)
        @Generated public PagedIterable<BatchJob> listJobs(BatchJobsListOptions options)
        @Generated public PagedIterable<BatchJobSchedule> listJobSchedules()
        @Generated public PagedIterable<BinaryData> listJobSchedules(RequestOptions requestOptions)
        @Generated public PagedIterable<BatchJobSchedule> listJobSchedules(BatchJobSchedulesListOptions options)
        @Generated public PagedIterable<BatchJob> listJobsFromSchedule(String jobScheduleId)
        @Generated public PagedIterable<BinaryData> listJobsFromSchedule(String jobScheduleId, RequestOptions requestOptions)
        @Generated public PagedIterable<BatchJob> listJobsFromSchedule(String jobScheduleId, BatchJobsFromScheduleListOptions options)
        @Generated public PagedIterable<BatchNodeVMExtension> listNodeExtensions(String poolId, String nodeId)
        @Generated public PagedIterable<BinaryData> listNodeExtensions(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public PagedIterable<BatchNodeVMExtension> listNodeExtensions(String poolId, String nodeId, BatchNodeExtensionsListOptions options)
        @Generated public PagedIterable<BatchNodeFile> listNodeFiles(String poolId, String nodeId)
        @Generated public PagedIterable<BinaryData> listNodeFiles(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public PagedIterable<BatchNodeFile> listNodeFiles(String poolId, String nodeId, BatchNodeFilesListOptions options)
        @Generated public PagedIterable<BatchNode> listNodes(String poolId)
        @Generated public PagedIterable<BinaryData> listNodes(String poolId, RequestOptions requestOptions)
        @Generated public PagedIterable<BatchNode> listNodes(String poolId, BatchNodesListOptions options)
        @Generated public PagedIterable<BatchPoolNodeCounts> listPoolNodeCounts()
        @Generated public PagedIterable<BinaryData> listPoolNodeCounts(RequestOptions requestOptions)
        @Generated public PagedIterable<BatchPoolNodeCounts> listPoolNodeCounts(BatchPoolNodeCountsListOptions options)
        @Generated public PagedIterable<BatchPool> listPools()
        @Generated public PagedIterable<BinaryData> listPools(RequestOptions requestOptions)
        @Generated public PagedIterable<BatchPool> listPools(BatchPoolsListOptions options)
        @Generated public PagedIterable<BatchSubtask> listSubTasks(String jobId, String taskId)
        @Generated public PagedIterable<BinaryData> listSubTasks(String jobId, String taskId, RequestOptions requestOptions)
        @Generated public PagedIterable<BatchSubtask> listSubTasks(String jobId, String taskId, BatchSubTasksListOptions options)
        @Generated public PagedIterable<BatchSupportedImage> listSupportedImages()
        @Generated public PagedIterable<BinaryData> listSupportedImages(RequestOptions requestOptions)
        @Generated public PagedIterable<BatchSupportedImage> listSupportedImages(SupportedBatchImagesListOptions options)
        @Generated public PagedIterable<BatchNodeFile> listTaskFiles(String jobId, String taskId)
        @Generated public PagedIterable<BinaryData> listTaskFiles(String jobId, String taskId, RequestOptions requestOptions)
        @Generated public PagedIterable<BatchNodeFile> listTaskFiles(String jobId, String taskId, BatchTaskFilesListOptions options)
        @Generated public PagedIterable<BatchTask> listTasks(String jobId)
        @Generated public PagedIterable<BinaryData> listTasks(String jobId, RequestOptions requestOptions)
        @Generated public PagedIterable<BatchTask> listTasks(String jobId, BatchTasksListOptions options)
        @Generated public BatchNode getNode(String poolId, String nodeId)
        @Generated public BatchNode getNode(String poolId, String nodeId, BatchNodeGetOptions options)
        @Generated public BatchNodeVMExtension getNodeExtension(String poolId, String nodeId, String extensionName)
        @Generated public BatchNodeVMExtension getNodeExtension(String poolId, String nodeId, String extensionName, BatchNodeExtensionGetOptions options)
        @Generated public Response<BinaryData> getNodeExtensionWithResponse(String poolId, String nodeId, String extensionName, RequestOptions requestOptions)
        @Generated public BinaryData getNodeFile(String poolId, String nodeId, String filePath)
        @Generated public BinaryData getNodeFile(String poolId, String nodeId, String filePath, BatchNodeFileGetOptions options)
        public BatchFileProperties getNodeFileProperties(String poolId, String nodeId, String filePath)
        public BatchFileProperties getNodeFileProperties(String poolId, String nodeId, String filePath, BatchNodeFilePropertiesGetOptions options)
        @Generated public Response<Void> getNodeFilePropertiesWithResponse(String poolId, String nodeId, String filePath, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getNodeFileWithResponse(String poolId, String nodeId, String filePath, RequestOptions requestOptions)
        @Generated public BatchNodeRemoteLoginSettings getNodeRemoteLoginSettings(String poolId, String nodeId)
        @Generated public BatchNodeRemoteLoginSettings getNodeRemoteLoginSettings(String poolId, String nodeId, BatchNodeRemoteLoginSettingsGetOptions options)
        @Generated public Response<BinaryData> getNodeRemoteLoginSettingsWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getNodeWithResponse(String poolId, String nodeId, RequestOptions requestOptions)
        @Generated public BatchPool getPool(String poolId)
        @Generated public BatchPool getPool(String poolId, BatchPoolGetOptions options, RequestConditions requestConditions)
        @Generated public boolean poolExists(String poolId)
        @Generated public boolean poolExists(String poolId, BatchPoolExistsOptions options, RequestConditions requestConditions)
        @Generated public Response<Boolean> poolExistsWithResponse(String poolId, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getPoolWithResponse(String poolId, RequestOptions requestOptions)
        @Generated public void reactivateTask(String jobId, String taskId)
        @Generated public void reactivateTask(String jobId, String taskId, BatchTaskReactivateOptions options, RequestConditions requestConditions)
        @Generated public Response<Void> reactivateTaskWithResponse(String jobId, String taskId, RequestOptions requestOptions)
        @Generated public void replaceJob(String jobId, BatchJob job)
        @Generated public void replaceJob(String jobId, BatchJob job, BatchJobReplaceOptions options, RequestConditions requestConditions)
        @Generated public void replaceJobSchedule(String jobScheduleId, BatchJobSchedule jobSchedule)
        @Generated public void replaceJobSchedule(String jobScheduleId, BatchJobSchedule jobSchedule, BatchJobScheduleReplaceOptions options, RequestConditions requestConditions)
        @Generated public Response<Void> replaceJobScheduleWithResponse(String jobScheduleId, BinaryData jobSchedule, RequestOptions requestOptions)
        @Generated public Response<Void> replaceJobWithResponse(String jobId, BinaryData job, RequestOptions requestOptions)
        @Generated public void replaceNodeUser(String poolId, String nodeId, String username, BatchNodeUserUpdateParameters parameters)
        @Generated public void replaceNodeUser(String poolId, String nodeId, String username, BatchNodeUserUpdateParameters parameters, BatchNodeUserReplaceOptions options)
        @Generated public Response<Void> replaceNodeUserWithResponse(String poolId, String nodeId, String username, BinaryData parameters, RequestOptions requestOptions)
        @Generated public void replacePoolProperties(String poolId, BatchPoolReplaceParameters pool)
        @Generated public void replacePoolProperties(String poolId, BatchPoolReplaceParameters pool, BatchPoolPropertiesReplaceOptions options)
        @Generated public Response<Void> replacePoolPropertiesWithResponse(String poolId, BinaryData pool, RequestOptions requestOptions)
        @Generated public void replaceTask(String jobId, String taskId, BatchTask task)
        @Generated public void replaceTask(String jobId, String taskId, BatchTask task, BatchTaskReplaceOptions options, RequestConditions requestConditions)
        @Generated public Response<Void> replaceTaskWithResponse(String jobId, String taskId, BinaryData task, RequestOptions requestOptions)
        @Generated public BatchTask getTask(String jobId, String taskId)
        @Generated public BatchTask getTask(String jobId, String taskId, BatchTaskGetOptions options, RequestConditions requestConditions)
        @Generated public BinaryData getTaskFile(String jobId, String taskId, String filePath)
        @Generated public BinaryData getTaskFile(String jobId, String taskId, String filePath, BatchTaskFileGetOptions options)
        public BatchFileProperties getTaskFileProperties(String jobId, String taskId, String filePath)
        public BatchFileProperties getTaskFileProperties(String jobId, String taskId, String filePath, BatchTaskFilePropertiesGetOptions options)
        @Generated public Response<Void> getTaskFilePropertiesWithResponse(String jobId, String taskId, String filePath, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getTaskFileWithResponse(String jobId, String taskId, String filePath, RequestOptions requestOptions)
        @Generated public Response<BinaryData> getTaskWithResponse(String jobId, String taskId, RequestOptions requestOptions)
        @Generated public void terminateTask(String jobId, String taskId)
        @Generated public void terminateTask(String jobId, String taskId, BatchTaskTerminateOptions options, RequestConditions requestConditions)
        @Generated public Response<Void> terminateTaskWithResponse(String jobId, String taskId, RequestOptions requestOptions)
        @Generated public void updateJob(String jobId, BatchJobUpdateParameters job)
        @Generated public void updateJob(String jobId, BatchJobUpdateParameters job, BatchJobUpdateOptions options, RequestConditions requestConditions)
        @Generated public void updateJobSchedule(String jobScheduleId, BatchJobScheduleUpdateParameters jobSchedule)
        @Generated public void updateJobSchedule(String jobScheduleId, BatchJobScheduleUpdateParameters jobSchedule, BatchJobScheduleUpdateOptions options, RequestConditions requestConditions)
        @Generated public Response<Void> updateJobScheduleWithResponse(String jobScheduleId, BinaryData jobSchedule, RequestOptions requestOptions)
        @Generated public Response<Void> updateJobWithResponse(String jobId, BinaryData job, RequestOptions requestOptions)
        @Generated public void updatePool(String poolId, BatchPoolUpdateParameters pool)
        @Generated public void updatePool(String poolId, BatchPoolUpdateParameters pool, BatchPoolUpdateOptions options, RequestConditions requestConditions)
        @Generated public Response<Void> updatePoolWithResponse(String poolId, BinaryData pool, RequestOptions requestOptions)
        @Generated public UploadBatchServiceLogsResult uploadNodeLogs(String poolId, String nodeId, UploadBatchServiceLogsParameters parameters)
        @Generated public UploadBatchServiceLogsResult uploadNodeLogs(String poolId, String nodeId, UploadBatchServiceLogsParameters parameters, BatchNodeLogsUploadOptions options)
        @Generated public Response<BinaryData> uploadNodeLogsWithResponse(String poolId, String nodeId, BinaryData parameters, RequestOptions requestOptions)
    }
    @ServiceClientBuilder(serviceClients = { BatchClient, BatchAsyncClient })
    public final class BatchClientBuilder implements HttpTrait<BatchClientBuilder> , ConfigurationTrait<BatchClientBuilder> , TokenCredentialTrait<BatchClientBuilder> , EndpointTrait<BatchClientBuilder> , AzureNamedKeyCredentialTrait<BatchClientBuilder> {
        @Generated public BatchClientBuilder()
        @Generated @Override public BatchClientBuilder addPolicy(HttpPipelinePolicy customPolicy)
        @Generated @Override public BatchClientBuilder clientOptions(ClientOptions clientOptions)
        @Generated @Override public BatchClientBuilder configuration(Configuration configuration)
        @Generated @Override public BatchClientBuilder credential(TokenCredential tokenCredential)
        @Override public BatchClientBuilder credential(AzureNamedKeyCredential azureNamedKeyCredential)
        @Generated @Override public BatchClientBuilder endpoint(String endpoint)
        @Generated @Override public BatchClientBuilder httpClient(HttpClient httpClient)
        @Generated @Override public BatchClientBuilder httpLogOptions(HttpLogOptions httpLogOptions)
        @Generated @Override public BatchClientBuilder pipeline(HttpPipeline pipeline)
        @Generated @Override public BatchClientBuilder retryOptions(RetryOptions retryOptions)
        @Generated public BatchClientBuilder retryPolicy(RetryPolicy retryPolicy)
        @Generated public BatchClientBuilder serviceVersion(BatchServiceVersion serviceVersion)
        @Generated public BatchAsyncClient buildAsyncClient()
        @Generated public BatchClient buildClient()
    }
    public enum BatchServiceVersion implements ServiceVersion {
        V2025_06_01("2025-06-01"),
        V2026_10_01("2026-10-01");
        public static BatchServiceVersion getLatest() // returns V2026_10_01
        @Override public String getVersion()
    }
}
package com.azure.compute.batch.models {
    public final class AllocationState extends ExpandableStringEnum<AllocationState> {
        @Generated public static final AllocationState STEADY = fromString("steady");
        @Generated public static final AllocationState RESIZING = fromString("resizing");
        @Generated public static final AllocationState STOPPING = fromString("stopping");
        @Deprecated @Generated public AllocationState()
        @Generated public static AllocationState fromString(String name)
        @Generated public static Collection<AllocationState> values()
    }
    @Immutable
    public final class AutoScaleRun implements JsonSerializable<AutoScaleRun> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public AutoScaleRunError getError()
        @Generated public String getResults()
        @Generated public OffsetDateTime getTimestamp()
    }
    @Immutable
    public final class AutoScaleRunError implements JsonSerializable<AutoScaleRunError> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getCode()
        @Generated public String getMessage()
        @Generated public List<NameValuePair> getValues()
    }
    public final class AutoUserScope extends ExpandableStringEnum<AutoUserScope> {
        @Generated public static final AutoUserScope TASK = fromString("task");
        @Generated public static final AutoUserScope POOL = fromString("pool");
        @Deprecated @Generated public AutoUserScope()
        @Generated public static AutoUserScope fromString(String name)
        @Generated public static Collection<AutoUserScope> values()
    }
    @Fluent
    public final class AutoUserSpecification implements JsonSerializable<AutoUserSpecification> {
        @Generated public AutoUserSpecification()
        @Generated public ElevationLevel getElevationLevel()
        @Generated public AutoUserSpecification setElevationLevel(ElevationLevel elevationLevel)
        @Generated public AutoUserScope getScope()
        @Generated public AutoUserSpecification setScope(AutoUserScope scope)
    }
    @Fluent
    public final class AutomaticOsUpgradePolicy implements JsonSerializable<AutomaticOsUpgradePolicy> {
        @Generated public AutomaticOsUpgradePolicy()
        @Generated public Boolean isDisableAutomaticRollback()
        @Generated public AutomaticOsUpgradePolicy setDisableAutomaticRollback(Boolean disableAutomaticRollback)
        @Generated public Boolean isEnableAutomaticOsUpgrade()
        @Generated public AutomaticOsUpgradePolicy setEnableAutomaticOsUpgrade(Boolean enableAutomaticOsUpgrade)
        @Generated public Boolean isOsRollingUpgradeDeferral()
        @Generated public AutomaticOsUpgradePolicy setOsRollingUpgradeDeferral(Boolean osRollingUpgradeDeferral)
        @Generated public Boolean isUseRollingUpgradePolicy()
        @Generated public AutomaticOsUpgradePolicy setUseRollingUpgradePolicy(Boolean useRollingUpgradePolicy)
    }
    @Fluent
    public final class AzureBlobFileSystemConfiguration implements JsonSerializable<AzureBlobFileSystemConfiguration> {
        @Generated public AzureBlobFileSystemConfiguration(String accountName, String containerName, String relativeMountPath)
        public AzureBlobFileSystemConfiguration(String accountName, String containerName, Path relativeMountPath)
        @Generated public String getAccountKey()
        @Generated public AzureBlobFileSystemConfiguration setAccountKey(String accountKey)
        @Generated public String getAccountName()
        @Generated public String getBlobfuseOptions()
        @Generated public AzureBlobFileSystemConfiguration setBlobfuseOptions(String blobfuseOptions)
        @Generated public String getContainerName()
        @Generated public BatchNodeIdentityReference getIdentityReference()
        @Generated public AzureBlobFileSystemConfiguration setIdentityReference(BatchNodeIdentityReference identityReference)
        @Generated public String getRelativeMountPath()
        @Generated public String getSasKey()
        @Generated public AzureBlobFileSystemConfiguration setSasKey(String sasKey)
    }
    @Fluent
    public final class AzureFileShareConfiguration implements JsonSerializable<AzureFileShareConfiguration> {
        @Generated public AzureFileShareConfiguration(String accountName, String accountKey, String azureFileUrl, String relativeMountPath)
        public AzureFileShareConfiguration(String accountName, String accountKey, String azureFileUrl, Path relativeMountPath)
        @Generated public String getAccountKey()
        @Generated public String getAccountName()
        @Generated public String getAzureFileUrl()
        @Generated public String getMountOptions()
        @Generated public AzureFileShareConfiguration setMountOptions(String mountOptions)
        @Generated public String getRelativeMountPath()
    }
    @Immutable
    public final class BatchAffinityDetails implements JsonSerializable<BatchAffinityDetails> {
        @Generated public BatchAffinityDetails(String affinityId)
        @Generated public String getAffinityId()
    }
    public final class BatchAllTasksCompleteMode extends ExpandableStringEnum<BatchAllTasksCompleteMode> {
        @Generated public static final BatchAllTasksCompleteMode NO_ACTION = fromString("noaction");
        @Generated public static final BatchAllTasksCompleteMode TERMINATE_JOB = fromString("terminatejob");
        @Deprecated @Generated public BatchAllTasksCompleteMode()
        @Generated public static BatchAllTasksCompleteMode fromString(String name)
        @Generated public static Collection<BatchAllTasksCompleteMode> values()
    }
    @Immutable
    public final class BatchApplication implements JsonSerializable<BatchApplication> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getDisplayName()
        @Generated public String getId()
        @Generated public List<String> getVersions()
    }
    @Fluent
    public final class BatchApplicationGetOptions {
        @Generated public BatchApplicationGetOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchApplicationGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchApplicationPackageReference implements JsonSerializable<BatchApplicationPackageReference> {
        @Generated public BatchApplicationPackageReference(String applicationId)
        @Generated public String getApplicationId()
        @Generated public String getVersion()
        @Generated public BatchApplicationPackageReference setVersion(String version)
    }
    @Fluent
    public final class BatchApplicationsListOptions {
        @Generated public BatchApplicationsListOptions()
        @Generated public Integer getMaxPageSize()
        @Generated public BatchApplicationsListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public Duration getTimeout()
        @Generated public BatchApplicationsListOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchAutoPoolSpecification implements JsonSerializable<BatchAutoPoolSpecification> {
        @Generated public BatchAutoPoolSpecification(BatchPoolLifetimeOption poolLifetimeOption)
        @Generated public String getAutoPoolIdPrefix()
        @Generated public BatchAutoPoolSpecification setAutoPoolIdPrefix(String autoPoolIdPrefix)
        @Generated public Boolean isKeepAlive()
        @Generated public BatchAutoPoolSpecification setKeepAlive(Boolean keepAlive)
        @Generated public BatchPoolSpecification getPool()
        @Generated public BatchAutoPoolSpecification setPool(BatchPoolSpecification pool)
        @Generated public BatchPoolLifetimeOption getPoolLifetimeOption()
    }
    public final class BatchCachingType extends ExpandableStringEnum<BatchCachingType> {
        @Generated public static final BatchCachingType NONE = fromString("none");
        @Generated public static final BatchCachingType READ_ONLY = fromString("readonly");
        @Generated public static final BatchCachingType READ_WRITE = fromString("readwrite");
        @Deprecated @Generated public BatchCachingType()
        @Generated public static BatchCachingType fromString(String name)
        @Generated public static Collection<BatchCachingType> values()
    }
    @Fluent
    public final class BatchContainerConfiguration implements JsonSerializable<BatchContainerConfiguration> {
        @Generated public BatchContainerConfiguration(BatchContainerType type)
        @Generated public List<String> getContainerImageNames()
        @Generated public BatchContainerConfiguration setContainerImageNames(List<String> containerImageNames)
        @Generated public List<ContainerRegistryReference> getContainerRegistries()
        @Generated public BatchContainerConfiguration setContainerRegistries(List<ContainerRegistryReference> containerRegistries)
        @Generated public BatchContainerType getType()
    }
    public final class BatchContainerType extends ExpandableStringEnum<BatchContainerType> {
        @Generated public static final BatchContainerType DOCKER_COMPATIBLE = fromString("dockerCompatible");
        @Generated public static final BatchContainerType CRI_COMPATIBLE = fromString("criCompatible");
        @Deprecated @Generated public BatchContainerType()
        @Generated public static BatchContainerType fromString(String name)
        @Generated public static Collection<BatchContainerType> values()
    }
    @Immutable
    public final class BatchCreateTaskCollectionResult implements JsonSerializable<BatchCreateTaskCollectionResult> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<BatchTaskCreateResult> getValues()
    }
    @Fluent
    public final class BatchDiffDiskSettings implements JsonSerializable<BatchDiffDiskSettings> {
        @Generated public BatchDiffDiskSettings()
        @Generated public DiffDiskPlacement getPlacement()
        @Generated public BatchDiffDiskSettings setPlacement(DiffDiskPlacement placement)
    }
    @Immutable
    public final class BatchError implements JsonSerializable<BatchError> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getCode()
        public static BatchError fromException(HttpResponseException err)
        @Generated public BatchErrorMessage getMessage()
        @Generated public List<BatchErrorDetail> getValues()
    }
    @Immutable
    public final class BatchErrorDetail implements JsonSerializable<BatchErrorDetail> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getKey()
        @Generated public String getValue()
    }
    public final class BatchErrorException extends HttpResponseException {
        public BatchErrorException(String message, HttpResponse response)
        public BatchErrorException(String message, HttpResponse response, BatchError value)
        @Override public BatchError getValue()
    }
    @Immutable
    public final class BatchErrorMessage implements JsonSerializable<BatchErrorMessage> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getLang()
        @Generated public String getValue()
    }
    public final class BatchErrorSourceCategory extends ExpandableStringEnum<BatchErrorSourceCategory> {
        @Generated public static final BatchErrorSourceCategory USER_ERROR = fromString("usererror");
        @Generated public static final BatchErrorSourceCategory SERVER_ERROR = fromString("servererror");
        @Deprecated @Generated public BatchErrorSourceCategory()
        @Generated public static BatchErrorSourceCategory fromString(String name)
        @Generated public static Collection<BatchErrorSourceCategory> values()
    }
    public final class BatchFileProperties {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        public long getContentLength()
        public boolean isDirectory()
        public static BatchFileProperties fromHeaders(HttpHeaders headers)
        public String getOcpBatchFileMode()
        public String getOcpBatchFileUrl()
        public OffsetDateTime getOcpCreationTime()
    }
    @Fluent
    public final class BatchInboundNatPool implements JsonSerializable<BatchInboundNatPool> {
        @Generated public BatchInboundNatPool(String name, InboundEndpointProtocol protocol, int backendPort, int frontendPortRangeStart, int frontendPortRangeEnd)
        @Generated public int getBackendPort()
        @Generated public int getFrontendPortRangeEnd()
        @Generated public int getFrontendPortRangeStart()
        @Generated public String getName()
        @Generated public List<NetworkSecurityGroupRule> getNetworkSecurityGroupRules()
        @Generated public BatchInboundNatPool setNetworkSecurityGroupRules(List<NetworkSecurityGroupRule> networkSecurityGroupRules)
        @Generated public InboundEndpointProtocol getProtocol()
    }
    @Fluent
    public final class BatchJob implements JsonSerializable<BatchJob> {
        @Generated public BatchJob(BatchPoolDetails poolDetails)
        @Generated public Boolean isAllowTaskPreemption()
        @Generated public BatchJob setAllowTaskPreemption(Boolean allowTaskPreemption)
        @Generated public BatchAllTasksCompleteMode getAllTasksCompleteMode()
        @Generated public BatchJob setAllTasksCompleteMode(BatchAllTasksCompleteMode allTasksCompleteMode)
        @Generated public List<EnvironmentSetting> getCommonEnvironmentSettings()
        @Generated public BatchJobConstraints getConstraints()
        @Generated public BatchJob setConstraints(BatchJobConstraints constraints)
        @Generated public OffsetDateTime getCreationTime()
        @Generated public String getDisplayName()
        @Generated public String getETag()
        @Generated public BatchJobExecutionDetails getExecutionInfo()
        @Generated public String getId()
        @Generated public BatchJobManagerTask getJobManagerTask()
        @Generated public BatchJobPreparationTask getJobPreparationTask()
        @Generated public BatchJobReleaseTask getJobReleaseTask()
        @Generated public BatchJobStatistics getJobStatistics()
        @Generated public OffsetDateTime getLastModified()
        @Generated public Integer getMaxParallelTasks()
        @Generated public BatchJob setMaxParallelTasks(Integer maxParallelTasks)
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchJob setMetadata(List<BatchMetadataItem> metadata)
        @Generated public BatchJobNetworkConfiguration getNetworkConfiguration()
        @Generated public BatchPoolDetails getPoolDetails()
        @Generated public BatchJobState getPreviousState()
        @Generated public OffsetDateTime getPreviousStateTransitionTime()
        @Generated public Integer getPriority()
        @Generated public BatchJob setPriority(Integer priority)
        @Generated public BatchJobState getState()
        @Generated public OffsetDateTime getStateTransitionTime()
        @Generated public BatchTaskFailureMode getTaskFailureMode()
        @Generated public String getUrl()
        @Generated public Boolean isUsesTaskDependencies()
    }
    public final class BatchJobActionKind extends ExpandableStringEnum<BatchJobActionKind> {
        @Generated public static final BatchJobActionKind NONE = fromString("none");
        @Generated public static final BatchJobActionKind DISABLE = fromString("disable");
        @Generated public static final BatchJobActionKind TERMINATE = fromString("terminate");
        @Deprecated @Generated public BatchJobActionKind()
        @Generated public static BatchJobActionKind fromString(String name)
        @Generated public static Collection<BatchJobActionKind> values()
    }
    @Fluent
    public final class BatchJobConstraints implements JsonSerializable<BatchJobConstraints> {
        @Generated public BatchJobConstraints()
        @Generated public Integer getMaxTaskRetryCount()
        @Generated public BatchJobConstraints setMaxTaskRetryCount(Integer maxTaskRetryCount)
        @Generated public Duration getMaxWallClockTime()
        @Generated public BatchJobConstraints setMaxWallClockTime(Duration maxWallClockTime)
    }
    @Fluent
    public final class BatchJobCreateOptions {
        @Generated public BatchJobCreateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobCreateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobCreateParameters implements JsonSerializable<BatchJobCreateParameters> {
        @Generated public BatchJobCreateParameters(String id, BatchPoolDetails poolDetails)
        @Generated public Boolean isAllowTaskPreemption()
        @Generated public BatchJobCreateParameters setAllowTaskPreemption(Boolean allowTaskPreemption)
        @Generated public BatchAllTasksCompleteMode getAllTasksCompleteMode()
        @Generated public BatchJobCreateParameters setAllTasksCompleteMode(BatchAllTasksCompleteMode allTasksCompleteMode)
        @Generated public List<EnvironmentSetting> getCommonEnvironmentSettings()
        @Generated public BatchJobCreateParameters setCommonEnvironmentSettings(List<EnvironmentSetting> commonEnvironmentSettings)
        @Generated public BatchJobConstraints getConstraints()
        @Generated public BatchJobCreateParameters setConstraints(BatchJobConstraints constraints)
        @Generated public String getDisplayName()
        @Generated public BatchJobCreateParameters setDisplayName(String displayName)
        @Generated public String getId()
        @Generated public BatchJobManagerTask getJobManagerTask()
        @Generated public BatchJobCreateParameters setJobManagerTask(BatchJobManagerTask jobManagerTask)
        @Generated public BatchJobPreparationTask getJobPreparationTask()
        @Generated public BatchJobCreateParameters setJobPreparationTask(BatchJobPreparationTask jobPreparationTask)
        @Generated public BatchJobReleaseTask getJobReleaseTask()
        @Generated public BatchJobCreateParameters setJobReleaseTask(BatchJobReleaseTask jobReleaseTask)
        @Generated public Integer getMaxParallelTasks()
        @Generated public BatchJobCreateParameters setMaxParallelTasks(Integer maxParallelTasks)
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchJobCreateParameters setMetadata(List<BatchMetadataItem> metadata)
        @Generated public BatchJobNetworkConfiguration getNetworkConfiguration()
        @Generated public BatchJobCreateParameters setNetworkConfiguration(BatchJobNetworkConfiguration networkConfiguration)
        @Generated public BatchPoolDetails getPoolDetails()
        @Generated public Integer getPriority()
        @Generated public BatchJobCreateParameters setPriority(Integer priority)
        @Generated public BatchTaskFailureMode getTaskFailureMode()
        @Generated public BatchJobCreateParameters setTaskFailureMode(BatchTaskFailureMode taskFailureMode)
        @Generated public Boolean isUsesTaskDependencies()
        @Generated public BatchJobCreateParameters setUsesTaskDependencies(Boolean usesTaskDependencies)
    }
    public final class BatchJobDefaultOrder extends ExpandableStringEnum<BatchJobDefaultOrder> {
        @Generated public static final BatchJobDefaultOrder NONE = fromString("none");
        @Generated public static final BatchJobDefaultOrder CREATION_TIME = fromString("creationtime");
        @Deprecated @Generated public BatchJobDefaultOrder()
        @Generated public static BatchJobDefaultOrder fromString(String name)
        @Generated public static Collection<BatchJobDefaultOrder> values()
    }
    @Fluent
    public final class BatchJobDeleteOptions {
        @Generated public BatchJobDeleteOptions()
        @Generated public Boolean isForce()
        @Generated public BatchJobDeleteOptions setForce(Boolean force)
        @Generated public Duration getTimeout()
        @Generated public BatchJobDeleteOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobDisableOptions {
        @Generated public BatchJobDisableOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobDisableOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchJobDisableParameters implements JsonSerializable<BatchJobDisableParameters> {
        @Generated public BatchJobDisableParameters(DisableBatchJobOption disableTasks)
        @Generated public DisableBatchJobOption getDisableTasks()
    }
    @Fluent
    public final class BatchJobEnableOptions {
        @Generated public BatchJobEnableOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobEnableOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchJobExecutionDetails implements JsonSerializable<BatchJobExecutionDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public OffsetDateTime getEndTime()
        @Generated public String getPoolId()
        @Generated public BatchJobSchedulingError getSchedulingError()
        @Generated public OffsetDateTime getStartTime()
        @Generated public String getTerminationReason()
    }
    @Fluent
    public final class BatchJobGetOptions {
        @Generated public BatchJobGetOptions()
        @Generated public List<String> getExpand()
        @Generated public BatchJobGetOptions setExpand(List<String> expand)
        @Generated public List<String> getSelect()
        @Generated public BatchJobGetOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchJobGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobManagerTask implements JsonSerializable<BatchJobManagerTask> {
        @Generated public BatchJobManagerTask(String id, String commandLine)
        @Generated public Boolean isAllowLowPriorityNode()
        @Generated public BatchJobManagerTask setAllowLowPriorityNode(Boolean allowLowPriorityNode)
        @Generated public List<BatchApplicationPackageReference> getApplicationPackageReferences()
        @Generated public BatchJobManagerTask setApplicationPackageReferences(List<BatchApplicationPackageReference> applicationPackageReferences)
        @Generated public String getCommandLine()
        @Generated public BatchTaskConstraints getConstraints()
        @Generated public BatchJobManagerTask setConstraints(BatchTaskConstraints constraints)
        @Generated public BatchTaskContainerSettings getContainerSettings()
        @Generated public BatchJobManagerTask setContainerSettings(BatchTaskContainerSettings containerSettings)
        @Generated public String getDisplayName()
        @Generated public BatchJobManagerTask setDisplayName(String displayName)
        @Generated public List<EnvironmentSetting> getEnvironmentSettings()
        @Generated public BatchJobManagerTask setEnvironmentSettings(List<EnvironmentSetting> environmentSettings)
        @Generated public String getId()
        @Generated public Boolean isKillJobOnCompletion()
        @Generated public BatchJobManagerTask setKillJobOnCompletion(Boolean killJobOnCompletion)
        @Generated public List<OutputFile> getOutputFiles()
        @Generated public BatchJobManagerTask setOutputFiles(List<OutputFile> outputFiles)
        @Generated public Integer getRequiredSlots()
        @Generated public BatchJobManagerTask setRequiredSlots(Integer requiredSlots)
        @Generated public List<ResourceFile> getResourceFiles()
        @Generated public BatchJobManagerTask setResourceFiles(List<ResourceFile> resourceFiles)
        @Generated public Boolean isRunExclusive()
        @Generated public BatchJobManagerTask setRunExclusive(Boolean runExclusive)
        @Generated public UserIdentity getUserIdentity()
        @Generated public BatchJobManagerTask setUserIdentity(UserIdentity userIdentity)
    }
    @Fluent
    public final class BatchJobNetworkConfiguration implements JsonSerializable<BatchJobNetworkConfiguration> {
        @Generated public BatchJobNetworkConfiguration(String subnetId)
        @Generated public Boolean isSkipWithdrawFromVnet()
        @Generated public BatchJobNetworkConfiguration setSkipWithdrawFromVnet(Boolean skipWithdrawFromVnet)
        @Generated public String getSubnetId()
    }
    @Immutable
    public final class BatchJobPreparationAndReleaseTaskStatus implements JsonSerializable<BatchJobPreparationAndReleaseTaskStatus> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchJobPreparationTaskExecutionDetails getJobPreparationTaskExecutionInfo()
        @Generated public BatchJobReleaseTaskExecutionDetails getJobReleaseTaskExecutionInfo()
        @Generated public String getNodeId()
        @Generated public String getNodeUrl()
        @Generated public String getPoolId()
    }
    @Fluent
    public final class BatchJobPreparationAndReleaseTaskStatusListOptions {
        @Generated public BatchJobPreparationAndReleaseTaskStatusListOptions()
        @Generated public String getFilter()
        @Generated public BatchJobPreparationAndReleaseTaskStatusListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public BatchJobPreparationAndReleaseTaskStatusListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public List<String> getSelect()
        @Generated public BatchJobPreparationAndReleaseTaskStatusListOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchJobPreparationAndReleaseTaskStatusListOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobPreparationTask implements JsonSerializable<BatchJobPreparationTask> {
        @Generated public BatchJobPreparationTask(String commandLine)
        @Generated public String getCommandLine()
        @Generated public BatchTaskConstraints getConstraints()
        @Generated public BatchJobPreparationTask setConstraints(BatchTaskConstraints constraints)
        @Generated public BatchTaskContainerSettings getContainerSettings()
        @Generated public BatchJobPreparationTask setContainerSettings(BatchTaskContainerSettings containerSettings)
        @Generated public List<EnvironmentSetting> getEnvironmentSettings()
        @Generated public BatchJobPreparationTask setEnvironmentSettings(List<EnvironmentSetting> environmentSettings)
        @Generated public String getId()
        @Generated public BatchJobPreparationTask setId(String id)
        @Generated public Boolean isRerunOnNodeRebootAfterSuccess()
        @Generated public BatchJobPreparationTask setRerunOnNodeRebootAfterSuccess(Boolean rerunOnNodeRebootAfterSuccess)
        @Generated public List<ResourceFile> getResourceFiles()
        @Generated public BatchJobPreparationTask setResourceFiles(List<ResourceFile> resourceFiles)
        @Generated public UserIdentity getUserIdentity()
        @Generated public BatchJobPreparationTask setUserIdentity(UserIdentity userIdentity)
        @Generated public Boolean isWaitForSuccess()
        @Generated public BatchJobPreparationTask setWaitForSuccess(Boolean waitForSuccess)
    }
    @Immutable
    public final class BatchJobPreparationTaskExecutionDetails implements JsonSerializable<BatchJobPreparationTaskExecutionDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchTaskContainerExecutionDetails getContainerInfo()
        @Generated public OffsetDateTime getEndTime()
        @Generated public Integer getExitCode()
        @Generated public BatchTaskFailureDetails getFailureInfo()
        @Generated public OffsetDateTime getLastRetryTime()
        @Generated public BatchTaskExecutionResult getResult()
        @Generated public int getRetryCount()
        @Generated public OffsetDateTime getStartTime()
        @Generated public BatchJobPreparationTaskState getState()
        @Generated public String getTaskRootDirectory()
        @Generated public String getTaskRootDirectoryUrl()
    }
    public final class BatchJobPreparationTaskState extends ExpandableStringEnum<BatchJobPreparationTaskState> {
        @Generated public static final BatchJobPreparationTaskState RUNNING = fromString("running");
        @Generated public static final BatchJobPreparationTaskState COMPLETED = fromString("completed");
        @Deprecated @Generated public BatchJobPreparationTaskState()
        @Generated public static BatchJobPreparationTaskState fromString(String name)
        @Generated public static Collection<BatchJobPreparationTaskState> values()
    }
    @Fluent
    public final class BatchJobReleaseTask implements JsonSerializable<BatchJobReleaseTask> {
        @Generated public BatchJobReleaseTask(String commandLine)
        @Generated public String getCommandLine()
        @Generated public BatchTaskContainerSettings getContainerSettings()
        @Generated public BatchJobReleaseTask setContainerSettings(BatchTaskContainerSettings containerSettings)
        @Generated public List<EnvironmentSetting> getEnvironmentSettings()
        @Generated public BatchJobReleaseTask setEnvironmentSettings(List<EnvironmentSetting> environmentSettings)
        @Generated public String getId()
        @Generated public BatchJobReleaseTask setId(String id)
        @Generated public Duration getMaxWallClockTime()
        @Generated public BatchJobReleaseTask setMaxWallClockTime(Duration maxWallClockTime)
        @Generated public List<ResourceFile> getResourceFiles()
        @Generated public BatchJobReleaseTask setResourceFiles(List<ResourceFile> resourceFiles)
        @Generated public Duration getRetentionTime()
        @Generated public BatchJobReleaseTask setRetentionTime(Duration retentionTime)
        @Generated public UserIdentity getUserIdentity()
        @Generated public BatchJobReleaseTask setUserIdentity(UserIdentity userIdentity)
    }
    @Immutable
    public final class BatchJobReleaseTaskExecutionDetails implements JsonSerializable<BatchJobReleaseTaskExecutionDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchTaskContainerExecutionDetails getContainerInfo()
        @Generated public OffsetDateTime getEndTime()
        @Generated public Integer getExitCode()
        @Generated public BatchTaskFailureDetails getFailureInfo()
        @Generated public BatchTaskExecutionResult getResult()
        @Generated public OffsetDateTime getStartTime()
        @Generated public BatchJobReleaseTaskState getState()
        @Generated public String getTaskRootDirectory()
        @Generated public String getTaskRootDirectoryUrl()
    }
    public final class BatchJobReleaseTaskState extends ExpandableStringEnum<BatchJobReleaseTaskState> {
        @Generated public static final BatchJobReleaseTaskState RUNNING = fromString("running");
        @Generated public static final BatchJobReleaseTaskState COMPLETED = fromString("completed");
        @Deprecated @Generated public BatchJobReleaseTaskState()
        @Generated public static BatchJobReleaseTaskState fromString(String name)
        @Generated public static Collection<BatchJobReleaseTaskState> values()
    }
    @Fluent
    public final class BatchJobReplaceOptions {
        @Generated public BatchJobReplaceOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobReplaceOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobSchedule implements JsonSerializable<BatchJobSchedule> {
        @Generated public BatchJobSchedule(BatchJobSpecification jobSpecification)
        @Generated public OffsetDateTime getCreationTime()
        @Generated public String getDisplayName()
        @Generated public String getETag()
        @Generated public BatchJobScheduleExecutionDetails getExecutionDetails()
        @Generated public String getId()
        @Generated public BatchJobScheduleStatistics getJobScheduleStatistics()
        @Generated public BatchJobSpecification getJobSpecification()
        @Generated public OffsetDateTime getLastModified()
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchJobSchedule setMetadata(List<BatchMetadataItem> metadata)
        @Generated public BatchJobScheduleState getPreviousState()
        @Generated public OffsetDateTime getPreviousStateTransitionTime()
        @Generated public BatchJobScheduleConfiguration getSchedule()
        @Generated public BatchJobSchedule setSchedule(BatchJobScheduleConfiguration schedule)
        @Generated public BatchJobScheduleState getState()
        @Generated public OffsetDateTime getStateTransitionTime()
        @Generated public String getUrl()
    }
    @Fluent
    public final class BatchJobScheduleConfiguration implements JsonSerializable<BatchJobScheduleConfiguration> {
        @Generated public BatchJobScheduleConfiguration()
        @Generated public OffsetDateTime getDoNotRunAfter()
        @Generated public BatchJobScheduleConfiguration setDoNotRunAfter(OffsetDateTime doNotRunAfter)
        @Generated public OffsetDateTime getDoNotRunUntil()
        @Generated public BatchJobScheduleConfiguration setDoNotRunUntil(OffsetDateTime doNotRunUntil)
        @Generated public Duration getRecurrenceInterval()
        @Generated public BatchJobScheduleConfiguration setRecurrenceInterval(Duration recurrenceInterval)
        @Generated public Duration getStartWindow()
        @Generated public BatchJobScheduleConfiguration setStartWindow(Duration startWindow)
    }
    @Fluent
    public final class BatchJobScheduleCreateOptions {
        @Generated public BatchJobScheduleCreateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobScheduleCreateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobScheduleCreateParameters implements JsonSerializable<BatchJobScheduleCreateParameters> {
        @Generated public BatchJobScheduleCreateParameters(String id, BatchJobScheduleConfiguration schedule, BatchJobSpecification jobSpecification)
        @Generated public String getDisplayName()
        @Generated public BatchJobScheduleCreateParameters setDisplayName(String displayName)
        @Generated public String getId()
        @Generated public BatchJobSpecification getJobSpecification()
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchJobScheduleCreateParameters setMetadata(List<BatchMetadataItem> metadata)
        @Generated public BatchJobScheduleConfiguration getSchedule()
    }
    @Fluent
    public final class BatchJobScheduleDeleteOptions {
        @Generated public BatchJobScheduleDeleteOptions()
        @Generated public Boolean isForce()
        @Generated public BatchJobScheduleDeleteOptions setForce(Boolean force)
        @Generated public Duration getTimeout()
        @Generated public BatchJobScheduleDeleteOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobScheduleDisableOptions {
        @Generated public BatchJobScheduleDisableOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobScheduleDisableOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobScheduleEnableOptions {
        @Generated public BatchJobScheduleEnableOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobScheduleEnableOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchJobScheduleExecutionDetails implements JsonSerializable<BatchJobScheduleExecutionDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public OffsetDateTime getEndTime()
        @Generated public OffsetDateTime getNextRunTime()
        @Generated public RecentBatchJob getRecentJob()
    }
    @Fluent
    public final class BatchJobScheduleExistsOptions {
        @Generated public BatchJobScheduleExistsOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobScheduleExistsOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobScheduleGetOptions {
        @Generated public BatchJobScheduleGetOptions()
        @Generated public List<String> getExpand()
        @Generated public BatchJobScheduleGetOptions setExpand(List<String> expand)
        @Generated public List<String> getSelect()
        @Generated public BatchJobScheduleGetOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchJobScheduleGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobScheduleReplaceOptions {
        @Generated public BatchJobScheduleReplaceOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobScheduleReplaceOptions setTimeout(Duration timeout)
    }
    public final class BatchJobScheduleState extends ExpandableStringEnum<BatchJobScheduleState> {
        @Generated public static final BatchJobScheduleState ACTIVE = fromString("active");
        @Generated public static final BatchJobScheduleState COMPLETED = fromString("completed");
        @Generated public static final BatchJobScheduleState DISABLED = fromString("disabled");
        @Generated public static final BatchJobScheduleState TERMINATING = fromString("terminating");
        @Generated public static final BatchJobScheduleState DELETING = fromString("deleting");
        @Deprecated @Generated public BatchJobScheduleState()
        @Generated public static BatchJobScheduleState fromString(String name)
        @Generated public static Collection<BatchJobScheduleState> values()
    }
    @Immutable
    public final class BatchJobScheduleStatistics implements JsonSerializable<BatchJobScheduleStatistics> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public long getFailedTasksCount()
        @Generated public Duration getKernelCpuTime()
        @Generated public OffsetDateTime getLastUpdateTime()
        @Generated public double getReadIoGiB()
        @Generated public long getReadIops()
        @Generated public OffsetDateTime getStartTime()
        @Generated public long getSucceededTasksCount()
        @Generated public long getTaskRetriesCount()
        @Generated public String getUrl()
        @Generated public Duration getUserCpuTime()
        @Generated public Duration getWaitTime()
        @Generated public Duration getWallClockTime()
        @Generated public double getWriteIoGiB()
        @Generated public long getWriteIops()
    }
    @Fluent
    public final class BatchJobScheduleTerminateOptions {
        @Generated public BatchJobScheduleTerminateOptions()
        @Generated public Boolean isForce()
        @Generated public BatchJobScheduleTerminateOptions setForce(Boolean force)
        @Generated public Duration getTimeout()
        @Generated public BatchJobScheduleTerminateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobScheduleUpdateOptions {
        @Generated public BatchJobScheduleUpdateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobScheduleUpdateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobScheduleUpdateParameters implements JsonSerializable<BatchJobScheduleUpdateParameters> {
        @Generated public BatchJobScheduleUpdateParameters()
        @Generated public BatchJobSpecification getJobSpecification()
        @Generated public BatchJobScheduleUpdateParameters setJobSpecification(BatchJobSpecification jobSpecification)
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchJobScheduleUpdateParameters setMetadata(List<BatchMetadataItem> metadata)
        @Generated public BatchJobScheduleConfiguration getSchedule()
        @Generated public BatchJobScheduleUpdateParameters setSchedule(BatchJobScheduleConfiguration schedule)
    }
    @Fluent
    public final class BatchJobSchedulesListOptions {
        @Generated public BatchJobSchedulesListOptions()
        @Generated public List<String> getExpand()
        @Generated public BatchJobSchedulesListOptions setExpand(List<String> expand)
        @Generated public String getFilter()
        @Generated public BatchJobSchedulesListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public BatchJobSchedulesListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public List<String> getSelect()
        @Generated public BatchJobSchedulesListOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchJobSchedulesListOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchJobSchedulingError implements JsonSerializable<BatchJobSchedulingError> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchErrorSourceCategory getCategory()
        @Generated public String getCode()
        @Generated public List<NameValuePair> getDetails()
        @Generated public String getMessage()
    }
    @Fluent
    public final class BatchJobSpecification implements JsonSerializable<BatchJobSpecification> {
        @Generated public BatchJobSpecification(BatchPoolDetails poolDetails)
        @Generated public Boolean isAllowTaskPreemption()
        @Generated public BatchJobSpecification setAllowTaskPreemption(Boolean allowTaskPreemption)
        @Generated public BatchAllTasksCompleteMode getAllTasksCompleteMode()
        @Generated public BatchJobSpecification setAllTasksCompleteMode(BatchAllTasksCompleteMode allTasksCompleteMode)
        @Generated public List<EnvironmentSetting> getCommonEnvironmentSettings()
        @Generated public BatchJobSpecification setCommonEnvironmentSettings(List<EnvironmentSetting> commonEnvironmentSettings)
        @Generated public BatchJobConstraints getConstraints()
        @Generated public BatchJobSpecification setConstraints(BatchJobConstraints constraints)
        @Generated public String getDisplayName()
        @Generated public BatchJobSpecification setDisplayName(String displayName)
        @Generated public BatchJobManagerTask getJobManagerTask()
        @Generated public BatchJobSpecification setJobManagerTask(BatchJobManagerTask jobManagerTask)
        @Generated public BatchJobPreparationTask getJobPreparationTask()
        @Generated public BatchJobSpecification setJobPreparationTask(BatchJobPreparationTask jobPreparationTask)
        @Generated public BatchJobReleaseTask getJobReleaseTask()
        @Generated public BatchJobSpecification setJobReleaseTask(BatchJobReleaseTask jobReleaseTask)
        @Generated public Integer getMaxParallelTasks()
        @Generated public BatchJobSpecification setMaxParallelTasks(Integer maxParallelTasks)
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchJobSpecification setMetadata(List<BatchMetadataItem> metadata)
        @Generated public BatchJobNetworkConfiguration getNetworkConfiguration()
        @Generated public BatchJobSpecification setNetworkConfiguration(BatchJobNetworkConfiguration networkConfiguration)
        @Generated public BatchPoolDetails getPoolDetails()
        @Generated public Integer getPriority()
        @Generated public BatchJobSpecification setPriority(Integer priority)
        @Generated public BatchTaskFailureMode getTaskFailureMode()
        @Generated public BatchJobSpecification setTaskFailureMode(BatchTaskFailureMode taskFailureMode)
        @Generated public Boolean isUsesTaskDependencies()
        @Generated public BatchJobSpecification setUsesTaskDependencies(Boolean usesTaskDependencies)
    }
    public final class BatchJobState extends ExpandableStringEnum<BatchJobState> {
        @Generated public static final BatchJobState ACTIVE = fromString("active");
        @Generated public static final BatchJobState DISABLING = fromString("disabling");
        @Generated public static final BatchJobState DISABLED = fromString("disabled");
        @Generated public static final BatchJobState ENABLING = fromString("enabling");
        @Generated public static final BatchJobState TERMINATING = fromString("terminating");
        @Generated public static final BatchJobState COMPLETED = fromString("completed");
        @Generated public static final BatchJobState DELETING = fromString("deleting");
        @Deprecated @Generated public BatchJobState()
        @Generated public static BatchJobState fromString(String name)
        @Generated public static Collection<BatchJobState> values()
    }
    @Immutable
    public final class BatchJobStatistics implements JsonSerializable<BatchJobStatistics> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public long getFailedTasksCount()
        @Generated public Duration getKernelCpuTime()
        @Generated public OffsetDateTime getLastUpdateTime()
        @Generated public double getReadIoGiB()
        @Generated public long getReadIops()
        @Generated public OffsetDateTime getStartTime()
        @Generated public long getSucceededTasksCount()
        @Generated public long getTaskRetriesCount()
        @Generated public String getUrl()
        @Generated public Duration getUserCpuTime()
        @Generated public Duration getWaitTime()
        @Generated public Duration getWallClockTime()
        @Generated public double getWriteIoGiB()
        @Generated public long getWriteIops()
    }
    @Fluent
    public final class BatchJobTaskCountsGetOptions {
        @Generated public BatchJobTaskCountsGetOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobTaskCountsGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobTerminateOptions {
        @Generated public BatchJobTerminateOptions()
        @Generated public Boolean isForce()
        @Generated public BatchJobTerminateOptions setForce(Boolean force)
        @Generated public BatchJobTerminateParameters getParameters()
        @Generated public BatchJobTerminateOptions setParameters(BatchJobTerminateParameters parameters)
        @Generated public Duration getTimeout()
        @Generated public BatchJobTerminateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobTerminateParameters implements JsonSerializable<BatchJobTerminateParameters> {
        @Generated public BatchJobTerminateParameters()
        @Generated public String getTerminationReason()
        @Generated public BatchJobTerminateParameters setTerminationReason(String terminationReason)
    }
    @Fluent
    public final class BatchJobUpdateOptions {
        @Generated public BatchJobUpdateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchJobUpdateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobUpdateParameters implements JsonSerializable<BatchJobUpdateParameters> {
        @Generated public BatchJobUpdateParameters()
        @Generated public Boolean isAllowTaskPreemption()
        @Generated public BatchJobUpdateParameters setAllowTaskPreemption(Boolean allowTaskPreemption)
        @Generated public BatchAllTasksCompleteMode getAllTasksCompleteMode()
        @Generated public BatchJobUpdateParameters setAllTasksCompleteMode(BatchAllTasksCompleteMode allTasksCompleteMode)
        @Generated public BatchJobConstraints getConstraints()
        @Generated public BatchJobUpdateParameters setConstraints(BatchJobConstraints constraints)
        @Generated public Integer getMaxParallelTasks()
        @Generated public BatchJobUpdateParameters setMaxParallelTasks(Integer maxParallelTasks)
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchJobUpdateParameters setMetadata(List<BatchMetadataItem> metadata)
        @Generated public BatchJobNetworkConfiguration getNetworkConfiguration()
        @Generated public BatchJobUpdateParameters setNetworkConfiguration(BatchJobNetworkConfiguration networkConfiguration)
        @Generated public BatchPoolDetails getPoolInfo()
        @Generated public BatchJobUpdateParameters setPoolInfo(BatchPoolDetails poolInfo)
        @Generated public Integer getPriority()
        @Generated public BatchJobUpdateParameters setPriority(Integer priority)
    }
    @Fluent
    public final class BatchJobsFromScheduleListOptions {
        @Generated public BatchJobsFromScheduleListOptions()
        @Generated public List<String> getExpand()
        @Generated public BatchJobsFromScheduleListOptions setExpand(List<String> expand)
        @Generated public String getFilter()
        @Generated public BatchJobsFromScheduleListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public BatchJobsFromScheduleListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public List<String> getSelect()
        @Generated public BatchJobsFromScheduleListOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchJobsFromScheduleListOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchJobsListOptions {
        @Generated public BatchJobsListOptions()
        @Generated public List<String> getExpand()
        @Generated public BatchJobsListOptions setExpand(List<String> expand)
        @Generated public String getFilter()
        @Generated public BatchJobsListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public BatchJobsListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public List<String> getSelect()
        @Generated public BatchJobsListOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchJobsListOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchMetadataItem implements JsonSerializable<BatchMetadataItem> {
        @Generated public BatchMetadataItem(String name, String value)
        @Generated public String getName()
        @Generated public String getValue()
    }
    @Immutable
    public final class BatchNode implements JsonSerializable<BatchNode> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getAffinityId()
        @Generated public OffsetDateTime getAllocationTime()
        @Generated public Boolean isDedicated()
        @Generated public BatchNodeEndpointConfiguration getEndpointConfiguration()
        @Generated public List<BatchNodeError> getErrors()
        @Generated public String getId()
        @Generated public String getIpAddress()
        @Generated public String getIpv6Address()
        @Generated public OffsetDateTime getLastBootTime()
        @Generated public BatchNodeAgentDetails getNodeAgentInfo()
        @Generated public String getPublicIpAddress()
        @Generated public String getPublicIpv6Address()
        @Generated public List<BatchTaskDetails> getRecentTasks()
        @Generated public Integer getRunningTasksCount()
        @Generated public Integer getRunningTaskSlotsCount()
        @Generated public SchedulingState getSchedulingState()
        @Generated public BatchStartTask getStartTask()
        @Generated public BatchStartTaskDetails getStartTaskInfo()
        @Generated public BatchNodeState getState()
        @Generated public OffsetDateTime getStateTransitionTime()
        @Generated public int getTotalTasksRun()
        @Generated public Integer getTotalTasksSucceeded()
        @Generated public String getUrl()
        @Generated public VirtualMachineDetails getVirtualMachineDetails()
        @Generated public String getVmSize()
    }
    @Immutable
    public final class BatchNodeAgentDetails implements JsonSerializable<BatchNodeAgentDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public OffsetDateTime getLastUpdateTime()
        @Generated public String getVersion()
    }
    @Immutable
    public final class BatchNodeCounts implements JsonSerializable<BatchNodeCounts> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public int getCreating()
        @Generated public int getDeallocated()
        @Generated public int getDeallocating()
        @Generated public int getIdle()
        @Generated public int getLeavingPool()
        @Generated public int getOffline()
        @Generated public int getPreempted()
        @Generated public int getRebooting()
        @Generated public int getReimaging()
        @Generated public int getRunning()
        @Generated public int getStarting()
        @Generated public int getStartTaskFailed()
        @Generated public int getTotal()
        @Generated public int getUnknown()
        @Generated public int getUnusable()
        @Generated public int getUpgradingOs()
        @Generated public int getWaitingForStartTask()
    }
    public final class BatchNodeDeallocateOption extends ExpandableStringEnum<BatchNodeDeallocateOption> {
        @Generated public static final BatchNodeDeallocateOption REQUEUE = fromString("requeue");
        @Generated public static final BatchNodeDeallocateOption TERMINATE = fromString("terminate");
        @Generated public static final BatchNodeDeallocateOption TASK_COMPLETION = fromString("taskcompletion");
        @Deprecated @Generated public BatchNodeDeallocateOption()
        @Generated public static BatchNodeDeallocateOption fromString(String name)
        @Generated public static Collection<BatchNodeDeallocateOption> values()
    }
    @Fluent
    public final class BatchNodeDeallocateOptions {
        @Generated public BatchNodeDeallocateOptions()
        @Generated public BatchNodeDeallocateParameters getParameters()
        @Generated public BatchNodeDeallocateOptions setParameters(BatchNodeDeallocateParameters parameters)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeDeallocateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeDeallocateParameters implements JsonSerializable<BatchNodeDeallocateParameters> {
        @Generated public BatchNodeDeallocateParameters()
        @Generated public BatchNodeDeallocateOption getNodeDeallocateOption()
        @Generated public BatchNodeDeallocateParameters setNodeDeallocateOption(BatchNodeDeallocateOption nodeDeallocateOption)
    }
    public final class BatchNodeDeallocationOption extends ExpandableStringEnum<BatchNodeDeallocationOption> {
        @Generated public static final BatchNodeDeallocationOption REQUEUE = fromString("requeue");
        @Generated public static final BatchNodeDeallocationOption TERMINATE = fromString("terminate");
        @Generated public static final BatchNodeDeallocationOption TASK_COMPLETION = fromString("taskcompletion");
        @Deprecated @Generated public BatchNodeDeallocationOption()
        @Generated public static BatchNodeDeallocationOption fromString(String name)
        @Generated public static Collection<BatchNodeDeallocationOption> values()
    }
    @Immutable
    public final class BatchNodeDetails implements JsonSerializable<BatchNodeDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getAffinityId()
        @Generated public String getNodeId()
        @Generated public String getNodeUrl()
        @Generated public String getPoolId()
        @Generated public String getTaskRootDirectory()
        @Generated public String getTaskRootDirectoryUrl()
    }
    public final class BatchNodeDisableSchedulingOption extends ExpandableStringEnum<BatchNodeDisableSchedulingOption> {
        @Generated public static final BatchNodeDisableSchedulingOption REQUEUE = fromString("requeue");
        @Generated public static final BatchNodeDisableSchedulingOption TERMINATE = fromString("terminate");
        @Generated public static final BatchNodeDisableSchedulingOption TASK_COMPLETION = fromString("taskcompletion");
        @Deprecated @Generated public BatchNodeDisableSchedulingOption()
        @Generated public static BatchNodeDisableSchedulingOption fromString(String name)
        @Generated public static Collection<BatchNodeDisableSchedulingOption> values()
    }
    @Immutable
    public final class BatchNodeEndpointConfiguration implements JsonSerializable<BatchNodeEndpointConfiguration> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public List<InboundEndpoint> getInboundEndpoints()
    }
    @Immutable
    public final class BatchNodeError implements JsonSerializable<BatchNodeError> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getCode()
        @Generated public List<NameValuePair> getErrorDetails()
        @Generated public String getMessage()
    }
    @Fluent
    public final class BatchNodeExtensionGetOptions {
        @Generated public BatchNodeExtensionGetOptions()
        @Generated public List<String> getSelect()
        @Generated public BatchNodeExtensionGetOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeExtensionGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeExtensionsListOptions {
        @Generated public BatchNodeExtensionsListOptions()
        @Generated public Integer getMaxPageSize()
        @Generated public BatchNodeExtensionsListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public List<String> getSelect()
        @Generated public BatchNodeExtensionsListOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeExtensionsListOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchNodeFile implements JsonSerializable<BatchNodeFile> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Boolean isDirectory()
        @Generated public String getName()
        @Generated public FileProperties getProperties()
        @Generated public String getUrl()
    }
    @Fluent
    public final class BatchNodeFileDeleteOptions {
        @Generated public BatchNodeFileDeleteOptions()
        @Generated public Boolean isRecursive()
        @Generated public BatchNodeFileDeleteOptions setRecursive(Boolean recursive)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeFileDeleteOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeFileGetOptions {
        @Generated public BatchNodeFileGetOptions()
        @Generated public OffsetDateTime getIfModifiedSince()
        @Generated public BatchNodeFileGetOptions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Generated public OffsetDateTime getIfUnmodifiedSince()
        @Generated public BatchNodeFileGetOptions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
        @Generated public String getOcpRange()
        @Generated public BatchNodeFileGetOptions setOcpRange(String ocpRange)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeFileGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeFilePropertiesGetOptions {
        @Generated public BatchNodeFilePropertiesGetOptions()
        @Generated public OffsetDateTime getIfModifiedSince()
        @Generated public BatchNodeFilePropertiesGetOptions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Generated public OffsetDateTime getIfUnmodifiedSince()
        @Generated public BatchNodeFilePropertiesGetOptions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeFilePropertiesGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeFilesListOptions {
        @Generated public BatchNodeFilesListOptions()
        @Generated public String getFilter()
        @Generated public BatchNodeFilesListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public BatchNodeFilesListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public Boolean isRecursive()
        @Generated public BatchNodeFilesListOptions setRecursive(Boolean recursive)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeFilesListOptions setTimeout(Duration timeout)
    }
    public final class BatchNodeFillType extends ExpandableStringEnum<BatchNodeFillType> {
        @Generated public static final BatchNodeFillType SPREAD = fromString("spread");
        @Generated public static final BatchNodeFillType PACK = fromString("pack");
        @Deprecated @Generated public BatchNodeFillType()
        @Generated public static BatchNodeFillType fromString(String name)
        @Generated public static Collection<BatchNodeFillType> values()
    }
    @Fluent
    public final class BatchNodeGetOptions {
        @Generated public BatchNodeGetOptions()
        @Generated public List<String> getSelect()
        @Generated public BatchNodeGetOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeIdentityReference implements JsonSerializable<BatchNodeIdentityReference> {
        @Generated public BatchNodeIdentityReference()
        @Generated public String getResourceId()
        @Generated public BatchNodeIdentityReference setResourceId(String resourceId)
    }
    @Fluent
    public final class BatchNodeLogsUploadOptions {
        @Generated public BatchNodeLogsUploadOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchNodeLogsUploadOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodePlacementConfiguration implements JsonSerializable<BatchNodePlacementConfiguration> {
        @Generated public BatchNodePlacementConfiguration()
        @Generated public BatchNodePlacementPolicyType getPolicy()
        @Generated public BatchNodePlacementConfiguration setPolicy(BatchNodePlacementPolicyType policy)
    }
    public final class BatchNodePlacementPolicyType extends ExpandableStringEnum<BatchNodePlacementPolicyType> {
        @Generated public static final BatchNodePlacementPolicyType REGIONAL = fromString("regional");
        @Generated public static final BatchNodePlacementPolicyType ZONAL = fromString("zonal");
        @Deprecated @Generated public BatchNodePlacementPolicyType()
        @Generated public static BatchNodePlacementPolicyType fromString(String name)
        @Generated public static Collection<BatchNodePlacementPolicyType> values()
    }
    public final class BatchNodeRebootKind extends ExpandableStringEnum<BatchNodeRebootKind> {
        @Generated public static final BatchNodeRebootKind REQUEUE = fromString("requeue");
        @Generated public static final BatchNodeRebootKind TERMINATE = fromString("terminate");
        @Generated public static final BatchNodeRebootKind TASK_COMPLETION = fromString("taskcompletion");
        @Deprecated @Generated public BatchNodeRebootKind()
        @Generated public static BatchNodeRebootKind fromString(String name)
        @Generated public static Collection<BatchNodeRebootKind> values()
    }
    @Fluent
    public final class BatchNodeRebootOptions {
        @Generated public BatchNodeRebootOptions()
        @Generated public BatchNodeRebootParameters getParameters()
        @Generated public BatchNodeRebootOptions setParameters(BatchNodeRebootParameters parameters)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeRebootOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeRebootParameters implements JsonSerializable<BatchNodeRebootParameters> {
        @Generated public BatchNodeRebootParameters()
        @Generated public BatchNodeRebootKind getNodeRebootKind()
        @Generated public BatchNodeRebootParameters setNodeRebootKind(BatchNodeRebootKind nodeRebootKind)
    }
    public final class BatchNodeReimageOption extends ExpandableStringEnum<BatchNodeReimageOption> {
        @Generated public static final BatchNodeReimageOption REQUEUE = fromString("requeue");
        @Generated public static final BatchNodeReimageOption TERMINATE = fromString("terminate");
        @Generated public static final BatchNodeReimageOption TASK_COMPLETION = fromString("taskcompletion");
        @Deprecated @Generated public BatchNodeReimageOption()
        @Generated public static BatchNodeReimageOption fromString(String name)
        @Generated public static Collection<BatchNodeReimageOption> values()
    }
    @Fluent
    public final class BatchNodeReimageOptions {
        @Generated public BatchNodeReimageOptions()
        @Generated public BatchNodeReimageParameters getParameters()
        @Generated public BatchNodeReimageOptions setParameters(BatchNodeReimageParameters parameters)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeReimageOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeReimageParameters implements JsonSerializable<BatchNodeReimageParameters> {
        @Generated public BatchNodeReimageParameters()
        @Generated public BatchNodeReimageOption getNodeReimageOption()
        @Generated public BatchNodeReimageParameters setNodeReimageOption(BatchNodeReimageOption nodeReimageOption)
    }
    @Immutable
    public final class BatchNodeRemoteLoginSettings implements JsonSerializable<BatchNodeRemoteLoginSettings> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getIpv6RemoteLoginIpAddress()
        @Generated public Integer getIpv6RemoteLoginPort()
        @Generated public String getRemoteLoginIpAddress()
        @Generated public int getRemoteLoginPort()
    }
    @Fluent
    public final class BatchNodeRemoteLoginSettingsGetOptions {
        @Generated public BatchNodeRemoteLoginSettingsGetOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchNodeRemoteLoginSettingsGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeRemoveParameters implements JsonSerializable<BatchNodeRemoveParameters> {
        @Generated public BatchNodeRemoveParameters(List<String> nodeIds)
        @Generated public BatchNodeDeallocationOption getNodeDeallocationOption()
        @Generated public BatchNodeRemoveParameters setNodeDeallocationOption(BatchNodeDeallocationOption nodeDeallocationOption)
        @Generated public List<String> getNodeIds()
        @Generated public Duration getResizeTimeout()
        @Generated public BatchNodeRemoveParameters setResizeTimeout(Duration resizeTimeout)
    }
    @Fluent
    public final class BatchNodeSchedulingDisableOptions {
        @Generated public BatchNodeSchedulingDisableOptions()
        @Generated public BatchNodeSchedulingDisableParameters getParameters()
        @Generated public BatchNodeSchedulingDisableOptions setParameters(BatchNodeSchedulingDisableParameters parameters)
        @Generated public Duration getTimeout()
        @Generated public BatchNodeSchedulingDisableOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeSchedulingDisableParameters implements JsonSerializable<BatchNodeSchedulingDisableParameters> {
        @Generated public BatchNodeSchedulingDisableParameters()
        @Generated public BatchNodeDisableSchedulingOption getNodeDisableSchedulingOption()
        @Generated public BatchNodeSchedulingDisableParameters setNodeDisableSchedulingOption(BatchNodeDisableSchedulingOption nodeDisableSchedulingOption)
    }
    @Fluent
    public final class BatchNodeSchedulingEnableOptions {
        @Generated public BatchNodeSchedulingEnableOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchNodeSchedulingEnableOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeStartOptions {
        @Generated public BatchNodeStartOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchNodeStartOptions setTimeout(Duration timeout)
    }
    public final class BatchNodeState extends ExpandableStringEnum<BatchNodeState> {
        @Generated public static final BatchNodeState IDLE = fromString("idle");
        @Generated public static final BatchNodeState REBOOTING = fromString("rebooting");
        @Generated public static final BatchNodeState REIMAGING = fromString("reimaging");
        @Generated public static final BatchNodeState RUNNING = fromString("running");
        @Generated public static final BatchNodeState UNUSABLE = fromString("unusable");
        @Generated public static final BatchNodeState CREATING = fromString("creating");
        @Generated public static final BatchNodeState STARTING = fromString("starting");
        @Generated public static final BatchNodeState WAITING_FOR_START_TASK = fromString("waitingforstarttask");
        @Generated public static final BatchNodeState START_TASK_FAILED = fromString("starttaskfailed");
        @Generated public static final BatchNodeState UNKNOWN = fromString("unknown");
        @Generated public static final BatchNodeState LEAVING_POOL = fromString("leavingpool");
        @Generated public static final BatchNodeState OFFLINE = fromString("offline");
        @Generated public static final BatchNodeState PREEMPTED = fromString("preempted");
        @Generated public static final BatchNodeState UPGRADING_OS = fromString("upgradingos");
        @Generated public static final BatchNodeState DEALLOCATED = fromString("deallocated");
        @Generated public static final BatchNodeState DEALLOCATING = fromString("deallocating");
        @Deprecated @Generated public BatchNodeState()
        @Generated public static BatchNodeState fromString(String name)
        @Generated public static Collection<BatchNodeState> values()
    }
    @Fluent
    public final class BatchNodeUserCreateOptions {
        @Generated public BatchNodeUserCreateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchNodeUserCreateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeUserCreateParameters implements JsonSerializable<BatchNodeUserCreateParameters> {
        @Generated public BatchNodeUserCreateParameters(String name)
        @Generated public Boolean isAdmin()
        @Generated public OffsetDateTime getExpiryTime()
        @Generated public BatchNodeUserCreateParameters setExpiryTime(OffsetDateTime expiryTime)
        @Generated public BatchNodeUserCreateParameters setIsAdmin(Boolean isAdmin)
        @Generated public String getName()
        @Generated public String getPassword()
        @Generated public BatchNodeUserCreateParameters setPassword(String password)
        @Generated public String getSshPublicKey()
        @Generated public BatchNodeUserCreateParameters setSshPublicKey(String sshPublicKey)
    }
    @Fluent
    public final class BatchNodeUserDeleteOptions {
        @Generated public BatchNodeUserDeleteOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchNodeUserDeleteOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeUserReplaceOptions {
        @Generated public BatchNodeUserReplaceOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchNodeUserReplaceOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodeUserUpdateParameters implements JsonSerializable<BatchNodeUserUpdateParameters> {
        @Generated public BatchNodeUserUpdateParameters()
        @Generated public OffsetDateTime getExpiryTime()
        @Generated public BatchNodeUserUpdateParameters setExpiryTime(OffsetDateTime expiryTime)
        @Generated public String getPassword()
        @Generated public BatchNodeUserUpdateParameters setPassword(String password)
        @Generated public String getSshPublicKey()
        @Generated public BatchNodeUserUpdateParameters setSshPublicKey(String sshPublicKey)
    }
    @Immutable
    public final class BatchNodeVMExtension implements JsonSerializable<BatchNodeVMExtension> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public VirtualMachineExtensionInstanceView getInstanceView()
        @Generated public String getProvisioningState()
        @Generated public VirtualMachineExtension getVmExtension()
    }
    @Fluent
    public final class BatchNodesListOptions {
        @Generated public BatchNodesListOptions()
        @Generated public String getFilter()
        @Generated public BatchNodesListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public BatchNodesListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public List<String> getSelect()
        @Generated public BatchNodesListOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchNodesListOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchNodesRemoveOptions {
        @Generated public BatchNodesRemoveOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchNodesRemoveOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchOsDisk implements JsonSerializable<BatchOsDisk> {
        @Generated public BatchOsDisk()
        @Generated public BatchCachingType getCaching()
        @Generated public BatchOsDisk setCaching(BatchCachingType caching)
        @Generated public Integer getDiskSizeGB()
        @Generated public BatchOsDisk setDiskSizeGB(Integer diskSizeGB)
        @Generated public BatchDiffDiskSettings getEphemeralOSDiskSettings()
        @Generated public BatchOsDisk setEphemeralOSDiskSettings(BatchDiffDiskSettings ephemeralOSDiskSettings)
        @Generated public ManagedDisk getManagedDisk()
        @Generated public BatchOsDisk setManagedDisk(ManagedDisk managedDisk)
        @Generated public Boolean isWriteAcceleratorEnabled()
        @Generated public BatchOsDisk setWriteAcceleratorEnabled(Boolean writeAcceleratorEnabled)
    }
    @Immutable
    public final class BatchPool implements JsonSerializable<BatchPool> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public AllocationState getAllocationState()
        @Generated public OffsetDateTime getAllocationStateTransitionTime()
        @Generated public List<BatchApplicationPackageReference> getApplicationPackageReferences()
        @Generated public Duration getAutoScaleEvaluationInterval()
        @Generated public String getAutoScaleFormula()
        @Generated public AutoScaleRun getAutoScaleRun()
        @Generated public OffsetDateTime getCreationTime()
        @Generated public int getCurrentDedicatedNodes()
        @Generated public int getCurrentLowPriorityNodes()
        @Generated public String getDisplayName()
        @Generated public Boolean isEnableAutoScale()
        @Generated public Boolean isEnableInterNodeCommunication()
        @Generated public String getETag()
        @Generated public String getId()
        @Generated public BatchPoolIdentity getIdentity()
        @Generated public OffsetDateTime getLastModified()
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public List<MountConfiguration> getMountConfiguration()
        @Generated public NetworkConfiguration getNetworkConfiguration()
        @Generated public BatchPoolStatistics getPoolStatistics()
        @Generated public List<ResizeError> getResizeErrors()
        @Generated public Duration getResizeTimeout()
        @Generated public BatchStartTask getStartTask()
        @Generated public BatchPoolState getState()
        @Generated public OffsetDateTime getStateTransitionTime()
        @Generated public Integer getTargetDedicatedNodes()
        @Generated public Integer getTargetLowPriorityNodes()
        @Generated public BatchTaskSchedulingPolicy getTaskSchedulingPolicy()
        @Generated public Integer getTaskSlotsPerNode()
        @Generated public UpgradePolicy getUpgradePolicy()
        @Generated public String getUrl()
        @Generated public List<UserAccount> getUserAccounts()
        @Generated public VirtualMachineConfiguration getVirtualMachineConfiguration()
        @Generated public String getVmSize()
        @Generated public VirtualMachineSkuProfile getVmSkuProfile()
    }
    @Fluent
    public final class BatchPoolAutoScaleEnableOptions {
        @Generated public BatchPoolAutoScaleEnableOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchPoolAutoScaleEnableOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchPoolAutoScaleEnableParameters implements JsonSerializable<BatchPoolAutoScaleEnableParameters> {
        @Generated public BatchPoolAutoScaleEnableParameters()
        @Generated public Duration getAutoScaleEvaluationInterval()
        @Generated public BatchPoolAutoScaleEnableParameters setAutoScaleEvaluationInterval(Duration autoScaleEvaluationInterval)
        @Generated public String getAutoScaleFormula()
        @Generated public BatchPoolAutoScaleEnableParameters setAutoScaleFormula(String autoScaleFormula)
    }
    @Fluent
    public final class BatchPoolCreateOptions {
        @Generated public BatchPoolCreateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchPoolCreateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchPoolCreateParameters implements JsonSerializable<BatchPoolCreateParameters> {
        @Generated public BatchPoolCreateParameters(String id, String vmSize)
        @Generated public List<BatchApplicationPackageReference> getApplicationPackageReferences()
        @Generated public BatchPoolCreateParameters setApplicationPackageReferences(List<BatchApplicationPackageReference> applicationPackageReferences)
        @Generated public Duration getAutoScaleEvaluationInterval()
        @Generated public BatchPoolCreateParameters setAutoScaleEvaluationInterval(Duration autoScaleEvaluationInterval)
        @Generated public String getAutoScaleFormula()
        @Generated public BatchPoolCreateParameters setAutoScaleFormula(String autoScaleFormula)
        @Generated public String getDisplayName()
        @Generated public BatchPoolCreateParameters setDisplayName(String displayName)
        @Generated public Boolean isEnableAutoScale()
        @Generated public BatchPoolCreateParameters setEnableAutoScale(Boolean enableAutoScale)
        @Generated public Boolean isEnableInterNodeCommunication()
        @Generated public BatchPoolCreateParameters setEnableInterNodeCommunication(Boolean enableInterNodeCommunication)
        @Generated public String getId()
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchPoolCreateParameters setMetadata(List<BatchMetadataItem> metadata)
        @Generated public List<MountConfiguration> getMountConfiguration()
        @Generated public BatchPoolCreateParameters setMountConfiguration(List<MountConfiguration> mountConfiguration)
        @Generated public NetworkConfiguration getNetworkConfiguration()
        @Generated public BatchPoolCreateParameters setNetworkConfiguration(NetworkConfiguration networkConfiguration)
        @Generated public Duration getResizeTimeout()
        @Generated public BatchPoolCreateParameters setResizeTimeout(Duration resizeTimeout)
        @Generated public BatchStartTask getStartTask()
        @Generated public BatchPoolCreateParameters setStartTask(BatchStartTask startTask)
        @Generated public Integer getTargetDedicatedNodes()
        @Generated public BatchPoolCreateParameters setTargetDedicatedNodes(Integer targetDedicatedNodes)
        @Generated public Integer getTargetLowPriorityNodes()
        @Generated public BatchPoolCreateParameters setTargetLowPriorityNodes(Integer targetLowPriorityNodes)
        @Generated public BatchTaskSchedulingPolicy getTaskSchedulingPolicy()
        @Generated public BatchPoolCreateParameters setTaskSchedulingPolicy(BatchTaskSchedulingPolicy taskSchedulingPolicy)
        @Generated public Integer getTaskSlotsPerNode()
        @Generated public BatchPoolCreateParameters setTaskSlotsPerNode(Integer taskSlotsPerNode)
        @Generated public UpgradePolicy getUpgradePolicy()
        @Generated public BatchPoolCreateParameters setUpgradePolicy(UpgradePolicy upgradePolicy)
        @Generated public List<UserAccount> getUserAccounts()
        @Generated public BatchPoolCreateParameters setUserAccounts(List<UserAccount> userAccounts)
        @Generated public VirtualMachineConfiguration getVirtualMachineConfiguration()
        @Generated public BatchPoolCreateParameters setVirtualMachineConfiguration(VirtualMachineConfiguration virtualMachineConfiguration)
        @Generated public String getVmSize()
        @Generated public VirtualMachineSkuProfile getVmSkuProfile()
        @Generated public BatchPoolCreateParameters setVmSkuProfile(VirtualMachineSkuProfile vmSkuProfile)
    }
    @Fluent
    public final class BatchPoolDeleteOptions {
        @Generated public BatchPoolDeleteOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchPoolDeleteOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchPoolDetails implements JsonSerializable<BatchPoolDetails> {
        @Generated public BatchPoolDetails()
        @Generated public BatchAutoPoolSpecification getAutoPoolSpecification()
        @Generated public BatchPoolDetails setAutoPoolSpecification(BatchAutoPoolSpecification autoPoolSpecification)
        @Generated public String getPoolId()
        @Generated public BatchPoolDetails setPoolId(String poolId)
    }
    @Fluent
    public final class BatchPoolDisableAutoScaleOptions {
        @Generated public BatchPoolDisableAutoScaleOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchPoolDisableAutoScaleOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchPoolEndpointConfiguration implements JsonSerializable<BatchPoolEndpointConfiguration> {
        @Generated public BatchPoolEndpointConfiguration(List<BatchInboundNatPool> inboundNatPools)
        @Generated public List<BatchInboundNatPool> getInboundNatPools()
    }
    @Fluent
    public final class BatchPoolEvaluateAutoScaleOptions {
        @Generated public BatchPoolEvaluateAutoScaleOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchPoolEvaluateAutoScaleOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchPoolEvaluateAutoScaleParameters implements JsonSerializable<BatchPoolEvaluateAutoScaleParameters> {
        @Generated public BatchPoolEvaluateAutoScaleParameters(String autoScaleFormula)
        @Generated public String getAutoScaleFormula()
    }
    @Fluent
    public final class BatchPoolExistsOptions {
        @Generated public BatchPoolExistsOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchPoolExistsOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchPoolGetOptions {
        @Generated public BatchPoolGetOptions()
        @Generated public List<String> getExpand()
        @Generated public BatchPoolGetOptions setExpand(List<String> expand)
        @Generated public List<String> getSelect()
        @Generated public BatchPoolGetOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchPoolGetOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchPoolIdentity implements JsonSerializable<BatchPoolIdentity> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchPoolIdentityType getType()
        @Generated public List<BatchUserAssignedIdentity> getUserAssignedIdentities()
    }
    @Fluent
    public final class BatchPoolIdentityReference implements JsonSerializable<BatchPoolIdentityReference> {
        @Generated public BatchPoolIdentityReference()
        @Generated public String getResourceId()
        @Generated public BatchPoolIdentityReference setResourceId(String resourceId)
    }
    public final class BatchPoolIdentityType extends ExpandableStringEnum<BatchPoolIdentityType> {
        @Generated public static final BatchPoolIdentityType USER_ASSIGNED = fromString("UserAssigned");
        @Generated public static final BatchPoolIdentityType NONE = fromString("None");
        @Deprecated @Generated public BatchPoolIdentityType()
        @Generated public static BatchPoolIdentityType fromString(String name)
        @Generated public static Collection<BatchPoolIdentityType> values()
    }
    public final class BatchPoolLifetimeOption extends ExpandableStringEnum<BatchPoolLifetimeOption> {
        @Generated public static final BatchPoolLifetimeOption JOB_SCHEDULE = fromString("jobschedule");
        @Generated public static final BatchPoolLifetimeOption JOB = fromString("job");
        @Deprecated @Generated public BatchPoolLifetimeOption()
        @Generated public static BatchPoolLifetimeOption fromString(String name)
        @Generated public static Collection<BatchPoolLifetimeOption> values()
    }
    @Immutable
    public final class BatchPoolNodeCounts implements JsonSerializable<BatchPoolNodeCounts> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchNodeCounts getDedicated()
        @Generated public BatchNodeCounts getLowPriority()
        @Generated public String getPoolId()
    }
    @Fluent
    public final class BatchPoolNodeCountsListOptions {
        @Generated public BatchPoolNodeCountsListOptions()
        @Generated public String getFilter()
        @Generated public BatchPoolNodeCountsListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public BatchPoolNodeCountsListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public Duration getTimeout()
        @Generated public BatchPoolNodeCountsListOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchPoolPropertiesReplaceOptions {
        @Generated public BatchPoolPropertiesReplaceOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchPoolPropertiesReplaceOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchPoolReplaceParameters implements JsonSerializable<BatchPoolReplaceParameters> {
        @Generated public BatchPoolReplaceParameters(List<BatchApplicationPackageReference> applicationPackageReferences, List<BatchMetadataItem> metadata)
        @Generated public List<BatchApplicationPackageReference> getApplicationPackageReferences()
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchStartTask getStartTask()
        @Generated public BatchPoolReplaceParameters setStartTask(BatchStartTask startTask)
    }
    @Fluent
    public final class BatchPoolResizeOptions {
        @Generated public BatchPoolResizeOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchPoolResizeOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchPoolResizeParameters implements JsonSerializable<BatchPoolResizeParameters> {
        @Generated public BatchPoolResizeParameters()
        @Generated public BatchNodeDeallocationOption getNodeDeallocationOption()
        @Generated public BatchPoolResizeParameters setNodeDeallocationOption(BatchNodeDeallocationOption nodeDeallocationOption)
        @Generated public Duration getResizeTimeout()
        @Generated public BatchPoolResizeParameters setResizeTimeout(Duration resizeTimeout)
        @Generated public Integer getTargetDedicatedNodes()
        @Generated public BatchPoolResizeParameters setTargetDedicatedNodes(Integer targetDedicatedNodes)
        @Generated public Integer getTargetLowPriorityNodes()
        @Generated public BatchPoolResizeParameters setTargetLowPriorityNodes(Integer targetLowPriorityNodes)
    }
    @Fluent
    public final class BatchPoolResizeStopOptions {
        @Generated public BatchPoolResizeStopOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchPoolResizeStopOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchPoolResourceStatistics implements JsonSerializable<BatchPoolResourceStatistics> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public double getAvgCpuPercentage()
        @Generated public double getAvgDiskGiB()
        @Generated public double getAvgMemoryGiB()
        @Generated public double getDiskReadGiB()
        @Generated public long getDiskReadIops()
        @Generated public double getDiskWriteGiB()
        @Generated public long getDiskWriteIops()
        @Generated public OffsetDateTime getLastUpdateTime()
        @Generated public double getNetworkReadGiB()
        @Generated public double getNetworkWriteGiB()
        @Generated public double getPeakDiskGiB()
        @Generated public double getPeakMemoryGiB()
        @Generated public OffsetDateTime getStartTime()
    }
    @Fluent
    public final class BatchPoolSpecification implements JsonSerializable<BatchPoolSpecification> {
        @Generated public BatchPoolSpecification(String vmSize)
        @Generated public List<BatchApplicationPackageReference> getApplicationPackageReferences()
        @Generated public BatchPoolSpecification setApplicationPackageReferences(List<BatchApplicationPackageReference> applicationPackageReferences)
        @Generated public Duration getAutoScaleEvaluationInterval()
        @Generated public BatchPoolSpecification setAutoScaleEvaluationInterval(Duration autoScaleEvaluationInterval)
        @Generated public String getAutoScaleFormula()
        @Generated public BatchPoolSpecification setAutoScaleFormula(String autoScaleFormula)
        @Generated public String getDisplayName()
        @Generated public BatchPoolSpecification setDisplayName(String displayName)
        @Generated public Boolean isEnableAutoScale()
        @Generated public BatchPoolSpecification setEnableAutoScale(Boolean enableAutoScale)
        @Generated public Boolean isEnableInterNodeCommunication()
        @Generated public BatchPoolSpecification setEnableInterNodeCommunication(Boolean enableInterNodeCommunication)
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchPoolSpecification setMetadata(List<BatchMetadataItem> metadata)
        @Generated public List<MountConfiguration> getMountConfiguration()
        @Generated public BatchPoolSpecification setMountConfiguration(List<MountConfiguration> mountConfiguration)
        @Generated public NetworkConfiguration getNetworkConfiguration()
        @Generated public BatchPoolSpecification setNetworkConfiguration(NetworkConfiguration networkConfiguration)
        @Generated public Duration getResizeTimeout()
        @Generated public BatchPoolSpecification setResizeTimeout(Duration resizeTimeout)
        @Generated public BatchStartTask getStartTask()
        @Generated public BatchPoolSpecification setStartTask(BatchStartTask startTask)
        @Generated public Integer getTargetDedicatedNodes()
        @Generated public BatchPoolSpecification setTargetDedicatedNodes(Integer targetDedicatedNodes)
        @Generated public Integer getTargetLowPriorityNodes()
        @Generated public BatchPoolSpecification setTargetLowPriorityNodes(Integer targetLowPriorityNodes)
        @Generated public BatchTaskSchedulingPolicy getTaskSchedulingPolicy()
        @Generated public BatchPoolSpecification setTaskSchedulingPolicy(BatchTaskSchedulingPolicy taskSchedulingPolicy)
        @Generated public Integer getTaskSlotsPerNode()
        @Generated public BatchPoolSpecification setTaskSlotsPerNode(Integer taskSlotsPerNode)
        @Generated public UpgradePolicy getUpgradePolicy()
        @Generated public BatchPoolSpecification setUpgradePolicy(UpgradePolicy upgradePolicy)
        @Generated public List<UserAccount> getUserAccounts()
        @Generated public BatchPoolSpecification setUserAccounts(List<UserAccount> userAccounts)
        @Generated public VirtualMachineConfiguration getVirtualMachineConfiguration()
        @Generated public BatchPoolSpecification setVirtualMachineConfiguration(VirtualMachineConfiguration virtualMachineConfiguration)
        @Generated public String getVmSize()
    }
    public final class BatchPoolState extends ExpandableStringEnum<BatchPoolState> {
        @Generated public static final BatchPoolState ACTIVE = fromString("active");
        @Generated public static final BatchPoolState DELETING = fromString("deleting");
        @Deprecated @Generated public BatchPoolState()
        @Generated public static BatchPoolState fromString(String name)
        @Generated public static Collection<BatchPoolState> values()
    }
    @Immutable
    public final class BatchPoolStatistics implements JsonSerializable<BatchPoolStatistics> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public OffsetDateTime getLastUpdateTime()
        @Generated public BatchPoolResourceStatistics getResourceStatistics()
        @Generated public OffsetDateTime getStartTime()
        @Generated public String getUrl()
    }
    @Fluent
    public final class BatchPoolUpdateOptions {
        @Generated public BatchPoolUpdateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchPoolUpdateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchPoolUpdateParameters implements JsonSerializable<BatchPoolUpdateParameters> {
        @Generated public BatchPoolUpdateParameters()
        @Generated public List<BatchApplicationPackageReference> getApplicationPackageReferences()
        @Generated public BatchPoolUpdateParameters setApplicationPackageReferences(List<BatchApplicationPackageReference> applicationPackageReferences)
        @Generated public String getDisplayName()
        @Generated public BatchPoolUpdateParameters setDisplayName(String displayName)
        @Generated public Boolean isEnableInterNodeCommunication()
        @Generated public BatchPoolUpdateParameters setEnableInterNodeCommunication(Boolean enableInterNodeCommunication)
        @Generated public List<BatchMetadataItem> getMetadata()
        @Generated public BatchPoolUpdateParameters setMetadata(List<BatchMetadataItem> metadata)
        @Generated public List<MountConfiguration> getMountConfiguration()
        @Generated public BatchPoolUpdateParameters setMountConfiguration(List<MountConfiguration> mountConfiguration)
        @Generated public NetworkConfiguration getNetworkConfiguration()
        @Generated public BatchPoolUpdateParameters setNetworkConfiguration(NetworkConfiguration networkConfiguration)
        @Generated public BatchStartTask getStartTask()
        @Generated public BatchPoolUpdateParameters setStartTask(BatchStartTask startTask)
        @Generated public BatchTaskSchedulingPolicy getTaskSchedulingPolicy()
        @Generated public BatchPoolUpdateParameters setTaskSchedulingPolicy(BatchTaskSchedulingPolicy taskSchedulingPolicy)
        @Generated public Integer getTaskSlotsPerNode()
        @Generated public BatchPoolUpdateParameters setTaskSlotsPerNode(Integer taskSlotsPerNode)
        @Generated public UpgradePolicy getUpgradePolicy()
        @Generated public BatchPoolUpdateParameters setUpgradePolicy(UpgradePolicy upgradePolicy)
        @Generated public List<UserAccount> getUserAccounts()
        @Generated public BatchPoolUpdateParameters setUserAccounts(List<UserAccount> userAccounts)
        @Generated public VirtualMachineConfiguration getVirtualMachineConfiguration()
        @Generated public BatchPoolUpdateParameters setVirtualMachineConfiguration(VirtualMachineConfiguration virtualMachineConfiguration)
        @Generated public String getVmSize()
        @Generated public BatchPoolUpdateParameters setVmSize(String vmSize)
        @Generated public VirtualMachineSkuProfile getVmSkuProfile()
        @Generated public BatchPoolUpdateParameters setVmSkuProfile(VirtualMachineSkuProfile vmSkuProfile)
    }
    @Fluent
    public final class BatchPoolsListOptions {
        @Generated public BatchPoolsListOptions()
        @Generated public List<String> getExpand()
        @Generated public BatchPoolsListOptions setExpand(List<String> expand)
        @Generated public String getFilter()
        @Generated public BatchPoolsListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public BatchPoolsListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public List<String> getSelect()
        @Generated public BatchPoolsListOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchPoolsListOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchPublicIpAddressConfiguration implements JsonSerializable<BatchPublicIpAddressConfiguration> {
        @Generated public BatchPublicIpAddressConfiguration()
        @Generated public List<String> getIpAddressIds()
        @Generated public BatchPublicIpAddressConfiguration setIpAddressIds(List<String> ipAddressIds)
        @Generated public IpAddressProvisioningType getIpAddressProvisioningType()
        @Generated public BatchPublicIpAddressConfiguration setIpAddressProvisioningType(IpAddressProvisioningType ipAddressProvisioningType)
        @Generated public List<IPFamily> getIpFamilies()
        @Generated public BatchPublicIpAddressConfiguration setIpFamilies(List<IPFamily> ipFamilies)
        @Generated public List<IPTag> getIpTags()
        @Generated public BatchPublicIpAddressConfiguration setIpTags(List<IPTag> ipTags)
    }
    @Fluent
    public final class BatchStartTask implements JsonSerializable<BatchStartTask> {
        @Generated public BatchStartTask(String commandLine)
        @Generated public String getCommandLine()
        @Generated public BatchTaskContainerSettings getContainerSettings()
        @Generated public BatchStartTask setContainerSettings(BatchTaskContainerSettings containerSettings)
        @Generated public List<EnvironmentSetting> getEnvironmentSettings()
        @Generated public BatchStartTask setEnvironmentSettings(List<EnvironmentSetting> environmentSettings)
        @Generated public Integer getMaxTaskRetryCount()
        @Generated public BatchStartTask setMaxTaskRetryCount(Integer maxTaskRetryCount)
        @Generated public List<ResourceFile> getResourceFiles()
        @Generated public BatchStartTask setResourceFiles(List<ResourceFile> resourceFiles)
        @Generated public UserIdentity getUserIdentity()
        @Generated public BatchStartTask setUserIdentity(UserIdentity userIdentity)
        @Generated public Boolean isWaitForSuccess()
        @Generated public BatchStartTask setWaitForSuccess(Boolean waitForSuccess)
    }
    @Immutable
    public final class BatchStartTaskDetails implements JsonSerializable<BatchStartTaskDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchTaskContainerExecutionDetails getContainerInfo()
        @Generated public OffsetDateTime getEndTime()
        @Generated public Integer getExitCode()
        @Generated public BatchTaskFailureDetails getFailureInfo()
        @Generated public OffsetDateTime getLastRetryTime()
        @Generated public BatchTaskExecutionResult getResult()
        @Generated public int getRetryCount()
        @Generated public OffsetDateTime getStartTime()
        @Generated public BatchStartTaskState getState()
    }
    public final class BatchStartTaskState extends ExpandableStringEnum<BatchStartTaskState> {
        @Generated public static final BatchStartTaskState RUNNING = fromString("running");
        @Generated public static final BatchStartTaskState COMPLETED = fromString("completed");
        @Deprecated @Generated public BatchStartTaskState()
        @Generated public static BatchStartTaskState fromString(String name)
        @Generated public static Collection<BatchStartTaskState> values()
    }
    @Fluent
    public final class BatchSubTasksListOptions {
        @Generated public BatchSubTasksListOptions()
        @Generated public List<String> getSelect()
        @Generated public BatchSubTasksListOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchSubTasksListOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchSubtask implements JsonSerializable<BatchSubtask> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchTaskContainerExecutionDetails getContainerInfo()
        @Generated public OffsetDateTime getEndTime()
        @Generated public Integer getExitCode()
        @Generated public BatchTaskFailureDetails getFailureInfo()
        @Generated public Integer getId()
        @Generated public BatchNodeDetails getNodeInfo()
        @Generated public BatchSubtaskState getPreviousState()
        @Generated public OffsetDateTime getPreviousStateTransitionTime()
        @Generated public BatchTaskExecutionResult getResult()
        @Generated public OffsetDateTime getStartTime()
        @Generated public BatchSubtaskState getState()
        @Generated public OffsetDateTime getStateTransitionTime()
    }
    public final class BatchSubtaskState extends ExpandableStringEnum<BatchSubtaskState> {
        @Generated public static final BatchSubtaskState PREPARING = fromString("preparing");
        @Generated public static final BatchSubtaskState RUNNING = fromString("running");
        @Generated public static final BatchSubtaskState COMPLETED = fromString("completed");
        @Deprecated @Generated public BatchSubtaskState()
        @Generated public static BatchSubtaskState fromString(String name)
        @Generated public static Collection<BatchSubtaskState> values()
    }
    @Immutable
    public final class BatchSupportedImage implements JsonSerializable<BatchSupportedImage> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public OffsetDateTime getBatchSupportEndOfLife()
        @Generated public List<String> getCapabilities()
        @Generated public BatchVmImageReference getImageReference()
        @Generated public String getNodeAgentSkuId()
        @Generated public OsType getOsType()
        @Generated public ImageVerificationType getVerificationType()
    }
    @Fluent
    public final class BatchTask implements JsonSerializable<BatchTask> {
        @Generated public BatchTask()
        @Generated public BatchAffinityDetails getAffinityInfo()
        @Generated public List<BatchApplicationPackageReference> getApplicationPackageReferences()
        @Generated public String getCommandLine()
        @Generated public BatchTaskConstraints getConstraints()
        @Generated public BatchTask setConstraints(BatchTaskConstraints constraints)
        @Generated public BatchTaskContainerSettings getContainerSettings()
        @Generated public OffsetDateTime getCreationTime()
        @Generated public BatchTaskDependencies getDependsOn()
        @Generated public String getDisplayName()
        @Generated public List<EnvironmentSetting> getEnvironmentSettings()
        @Generated public String getETag()
        @Generated public BatchTaskExecutionDetails getExecutionInfo()
        @Generated public ExitConditions getExitConditions()
        @Generated public String getId()
        @Generated public OffsetDateTime getLastModified()
        @Generated public MultiInstanceSettings getMultiInstanceSettings()
        @Generated public BatchNodeDetails getNodeInfo()
        @Generated public List<OutputFile> getOutputFiles()
        @Generated public BatchTaskState getPreviousState()
        @Generated public OffsetDateTime getPreviousStateTransitionTime()
        @Generated public Integer getRequiredSlots()
        @Generated public List<ResourceFile> getResourceFiles()
        @Generated public BatchTaskState getState()
        @Generated public OffsetDateTime getStateTransitionTime()
        @Generated public BatchTaskStatistics getTaskStatistics()
        @Generated public String getUrl()
        @Generated public UserIdentity getUserIdentity()
    }
    public final class BatchTaskAddStatus extends ExpandableStringEnum<BatchTaskAddStatus> {
        @Generated public static final BatchTaskAddStatus SUCCESS = fromString("success");
        @Generated public static final BatchTaskAddStatus CLIENT_ERROR = fromString("clienterror");
        @Generated public static final BatchTaskAddStatus SERVER_ERROR = fromString("servererror");
        @Deprecated @Generated public BatchTaskAddStatus()
        @Generated public static BatchTaskAddStatus fromString(String name)
        @Generated public static Collection<BatchTaskAddStatus> values()
    }
    public class BatchTaskBulkCreateOptions {
        public BatchTaskBulkCreateOptions()
        public Integer getMaxConcurrency()
        public BatchTaskBulkCreateOptions setMaxConcurrency(Integer maxConcurrency)
    }
    @Fluent
    public final class BatchTaskCollectionCreateOptions {
        @Generated public BatchTaskCollectionCreateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchTaskCollectionCreateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchTaskConstraints implements JsonSerializable<BatchTaskConstraints> {
        @Generated public BatchTaskConstraints()
        @Generated public Integer getMaxTaskRetryCount()
        @Generated public BatchTaskConstraints setMaxTaskRetryCount(Integer maxTaskRetryCount)
        @Generated public Duration getMaxWallClockTime()
        @Generated public BatchTaskConstraints setMaxWallClockTime(Duration maxWallClockTime)
        @Generated public Duration getRetentionTime()
        @Generated public BatchTaskConstraints setRetentionTime(Duration retentionTime)
    }
    @Immutable
    public final class BatchTaskContainerExecutionDetails implements JsonSerializable<BatchTaskContainerExecutionDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getContainerId()
        @Generated public String getError()
        @Generated public String getState()
    }
    @Fluent
    public final class BatchTaskContainerSettings implements JsonSerializable<BatchTaskContainerSettings> {
        @Generated public BatchTaskContainerSettings(String imageName)
        @Generated public List<ContainerHostBatchBindMountEntry> getContainerHostBatchBindMounts()
        @Generated public BatchTaskContainerSettings setContainerHostBatchBindMounts(List<ContainerHostBatchBindMountEntry> containerHostBatchBindMounts)
        @Generated public String getContainerRunOptions()
        @Generated public BatchTaskContainerSettings setContainerRunOptions(String containerRunOptions)
        @Generated public String getImageName()
        @Generated public ContainerRegistryReference getRegistry()
        @Generated public BatchTaskContainerSettings setRegistry(ContainerRegistryReference registry)
        @Generated public ContainerWorkingDirectory getWorkingDirectory()
        @Generated public BatchTaskContainerSettings setWorkingDirectory(ContainerWorkingDirectory workingDirectory)
    }
    @Immutable
    public final class BatchTaskCounts implements JsonSerializable<BatchTaskCounts> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public int getActive()
        @Generated public int getCompleted()
        @Generated public int getFailed()
        @Generated public int getRunning()
        @Generated public int getSucceeded()
    }
    @Immutable
    public final class BatchTaskCountsResult implements JsonSerializable<BatchTaskCountsResult> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchTaskCounts getTaskCounts()
        @Generated public BatchTaskSlotCounts getTaskSlotCounts()
    }
    @Fluent
    public final class BatchTaskCreateOptions {
        @Generated public BatchTaskCreateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchTaskCreateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchTaskCreateParameters implements JsonSerializable<BatchTaskCreateParameters> {
        @Generated public BatchTaskCreateParameters(String id, String commandLine)
        @Generated public BatchAffinityDetails getAffinityInfo()
        @Generated public BatchTaskCreateParameters setAffinityInfo(BatchAffinityDetails affinityInfo)
        @Generated public List<BatchApplicationPackageReference> getApplicationPackageReferences()
        @Generated public BatchTaskCreateParameters setApplicationPackageReferences(List<BatchApplicationPackageReference> applicationPackageReferences)
        @Generated public String getCommandLine()
        @Generated public BatchTaskConstraints getConstraints()
        @Generated public BatchTaskCreateParameters setConstraints(BatchTaskConstraints constraints)
        @Generated public BatchTaskContainerSettings getContainerSettings()
        @Generated public BatchTaskCreateParameters setContainerSettings(BatchTaskContainerSettings containerSettings)
        @Generated public BatchTaskDependencies getDependsOn()
        @Generated public BatchTaskCreateParameters setDependsOn(BatchTaskDependencies dependsOn)
        @Generated public String getDisplayName()
        @Generated public BatchTaskCreateParameters setDisplayName(String displayName)
        @Generated public List<EnvironmentSetting> getEnvironmentSettings()
        @Generated public BatchTaskCreateParameters setEnvironmentSettings(List<EnvironmentSetting> environmentSettings)
        @Generated public ExitConditions getExitConditions()
        @Generated public BatchTaskCreateParameters setExitConditions(ExitConditions exitConditions)
        @Generated public String getId()
        @Generated public MultiInstanceSettings getMultiInstanceSettings()
        @Generated public BatchTaskCreateParameters setMultiInstanceSettings(MultiInstanceSettings multiInstanceSettings)
        @Generated public List<OutputFile> getOutputFiles()
        @Generated public BatchTaskCreateParameters setOutputFiles(List<OutputFile> outputFiles)
        @Generated public Integer getRequiredSlots()
        @Generated public BatchTaskCreateParameters setRequiredSlots(Integer requiredSlots)
        @Generated public List<ResourceFile> getResourceFiles()
        @Generated public BatchTaskCreateParameters setResourceFiles(List<ResourceFile> resourceFiles)
        @Generated public UserIdentity getUserIdentity()
        @Generated public BatchTaskCreateParameters setUserIdentity(UserIdentity userIdentity)
    }
    @Immutable
    public final class BatchTaskCreateResult implements JsonSerializable<BatchTaskCreateResult> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchError getError()
        @Generated public String getETag()
        @Generated public OffsetDateTime getLastModified()
        @Generated public String getLocation()
        @Generated public BatchTaskAddStatus getStatus()
        @Generated public String getTaskId()
    }
    @Fluent
    public final class BatchTaskDeleteOptions {
        @Generated public BatchTaskDeleteOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchTaskDeleteOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchTaskDependencies implements JsonSerializable<BatchTaskDependencies> {
        @Generated public BatchTaskDependencies()
        @Generated public List<BatchTaskIdRange> getTaskIdRanges()
        @Generated public BatchTaskDependencies setTaskIdRanges(List<BatchTaskIdRange> taskIdRanges)
        @Generated public List<String> getTaskIds()
        @Generated public BatchTaskDependencies setTaskIds(List<String> taskIds)
    }
    @Immutable
    public final class BatchTaskDetails implements JsonSerializable<BatchTaskDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchTaskExecutionDetails getExecutionInfo()
        @Generated public String getJobId()
        @Generated public Integer getSubtaskId()
        @Generated public String getTaskId()
        @Generated public BatchTaskState getTaskState()
        @Generated public String getTaskUrl()
    }
    @Immutable
    public final class BatchTaskExecutionDetails implements JsonSerializable<BatchTaskExecutionDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchTaskContainerExecutionDetails getContainerInfo()
        @Generated public OffsetDateTime getEndTime()
        @Generated public Integer getExitCode()
        @Generated public BatchTaskFailureDetails getFailureInfo()
        @Generated public OffsetDateTime getLastRequeueTime()
        @Generated public OffsetDateTime getLastRetryTime()
        @Generated public int getRequeueCount()
        @Generated public BatchTaskExecutionResult getResult()
        @Generated public int getRetryCount()
        @Generated public OffsetDateTime getStartTime()
    }
    public final class BatchTaskExecutionResult extends ExpandableStringEnum<BatchTaskExecutionResult> {
        @Generated public static final BatchTaskExecutionResult SUCCESS = fromString("success");
        @Generated public static final BatchTaskExecutionResult FAILURE = fromString("failure");
        @Deprecated @Generated public BatchTaskExecutionResult()
        @Generated public static BatchTaskExecutionResult fromString(String name)
        @Generated public static Collection<BatchTaskExecutionResult> values()
    }
    @Immutable
    public final class BatchTaskFailureDetails implements JsonSerializable<BatchTaskFailureDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchErrorSourceCategory getCategory()
        @Generated public String getCode()
        @Generated public List<NameValuePair> getDetails()
        @Generated public String getMessage()
    }
    public final class BatchTaskFailureMode extends ExpandableStringEnum<BatchTaskFailureMode> {
        @Generated public static final BatchTaskFailureMode NO_ACTION = fromString("noaction");
        @Generated public static final BatchTaskFailureMode PERFORM_EXIT_OPTIONS_JOB_ACTION = fromString("performexitoptionsjobaction");
        @Deprecated @Generated public BatchTaskFailureMode()
        @Generated public static BatchTaskFailureMode fromString(String name)
        @Generated public static Collection<BatchTaskFailureMode> values()
    }
    @Fluent
    public final class BatchTaskFileDeleteOptions {
        @Generated public BatchTaskFileDeleteOptions()
        @Generated public Boolean isRecursive()
        @Generated public BatchTaskFileDeleteOptions setRecursive(Boolean recursive)
        @Generated public Duration getTimeout()
        @Generated public BatchTaskFileDeleteOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchTaskFileGetOptions {
        @Generated public BatchTaskFileGetOptions()
        @Generated public OffsetDateTime getIfModifiedSince()
        @Generated public BatchTaskFileGetOptions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Generated public OffsetDateTime getIfUnmodifiedSince()
        @Generated public BatchTaskFileGetOptions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
        @Generated public String getOcpRange()
        @Generated public BatchTaskFileGetOptions setOcpRange(String ocpRange)
        @Generated public Duration getTimeout()
        @Generated public BatchTaskFileGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchTaskFilePropertiesGetOptions {
        @Generated public BatchTaskFilePropertiesGetOptions()
        @Generated public OffsetDateTime getIfModifiedSince()
        @Generated public BatchTaskFilePropertiesGetOptions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Generated public OffsetDateTime getIfUnmodifiedSince()
        @Generated public BatchTaskFilePropertiesGetOptions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
        @Generated public Duration getTimeout()
        @Generated public BatchTaskFilePropertiesGetOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchTaskFilesListOptions {
        @Generated public BatchTaskFilesListOptions()
        @Generated public String getFilter()
        @Generated public BatchTaskFilesListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public BatchTaskFilesListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public Boolean isRecursive()
        @Generated public BatchTaskFilesListOptions setRecursive(Boolean recursive)
        @Generated public Duration getTimeout()
        @Generated public BatchTaskFilesListOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchTaskGetOptions {
        @Generated public BatchTaskGetOptions()
        @Generated public List<String> getExpand()
        @Generated public BatchTaskGetOptions setExpand(List<String> expand)
        @Generated public List<String> getSelect()
        @Generated public BatchTaskGetOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchTaskGetOptions setTimeout(Duration timeout)
    }
    @Immutable
    public final class BatchTaskGroup implements JsonSerializable<BatchTaskGroup> {
        @Generated public BatchTaskGroup(List<BatchTaskCreateParameters> values)
        @Generated public List<BatchTaskCreateParameters> getValues()
    }
    @Immutable
    public final class BatchTaskIdRange implements JsonSerializable<BatchTaskIdRange> {
        @Generated public BatchTaskIdRange(int start, int end)
        @Generated public int getEnd()
        @Generated public int getStart()
    }
    @Fluent
    public final class BatchTaskReactivateOptions {
        @Generated public BatchTaskReactivateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchTaskReactivateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchTaskReplaceOptions {
        @Generated public BatchTaskReplaceOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchTaskReplaceOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchTaskSchedulingPolicy implements JsonSerializable<BatchTaskSchedulingPolicy> {
        @Generated public BatchTaskSchedulingPolicy(BatchNodeFillType nodeFillType)
        @Generated public BatchJobDefaultOrder getJobDefaultOrder()
        @Generated public BatchTaskSchedulingPolicy setJobDefaultOrder(BatchJobDefaultOrder jobDefaultOrder)
        @Generated public BatchNodeFillType getNodeFillType()
    }
    @Immutable
    public final class BatchTaskSlotCounts implements JsonSerializable<BatchTaskSlotCounts> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public int getActive()
        @Generated public int getCompleted()
        @Generated public int getFailed()
        @Generated public int getRunning()
        @Generated public int getSucceeded()
    }
    public final class BatchTaskState extends ExpandableStringEnum<BatchTaskState> {
        @Generated public static final BatchTaskState ACTIVE = fromString("active");
        @Generated public static final BatchTaskState PREPARING = fromString("preparing");
        @Generated public static final BatchTaskState RUNNING = fromString("running");
        @Generated public static final BatchTaskState COMPLETED = fromString("completed");
        @Deprecated @Generated public BatchTaskState()
        @Generated public static BatchTaskState fromString(String name)
        @Generated public static Collection<BatchTaskState> values()
    }
    @Immutable
    public final class BatchTaskStatistics implements JsonSerializable<BatchTaskStatistics> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public Duration getKernelCpuTime()
        @Generated public OffsetDateTime getLastUpdateTime()
        @Generated public double getReadIoGiB()
        @Generated public long getReadIops()
        @Generated public OffsetDateTime getStartTime()
        @Generated public String getUrl()
        @Generated public Duration getUserCpuTime()
        @Generated public Duration getWaitTime()
        @Generated public Duration getWallClockTime()
        @Generated public double getWriteIoGiB()
        @Generated public long getWriteIops()
    }
    @Fluent
    public final class BatchTaskTerminateOptions {
        @Generated public BatchTaskTerminateOptions()
        @Generated public Duration getTimeout()
        @Generated public BatchTaskTerminateOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchTasksListOptions {
        @Generated public BatchTasksListOptions()
        @Generated public List<String> getExpand()
        @Generated public BatchTasksListOptions setExpand(List<String> expand)
        @Generated public String getFilter()
        @Generated public BatchTasksListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public BatchTasksListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public List<String> getSelect()
        @Generated public BatchTasksListOptions setSelect(List<String> select)
        @Generated public Duration getTimeout()
        @Generated public BatchTasksListOptions setTimeout(Duration timeout)
    }
    @Fluent
    public final class BatchUefiSettings implements JsonSerializable<BatchUefiSettings> {
        @Generated public BatchUefiSettings()
        @Generated public Boolean isSecureBootEnabled()
        @Generated public BatchUefiSettings setSecureBootEnabled(Boolean secureBootEnabled)
        @Generated public Boolean isVTpmEnabled()
        @Generated public BatchUefiSettings setVTpmEnabled(Boolean vTpmEnabled)
    }
    @Immutable
    public final class BatchUserAssignedIdentity implements JsonSerializable<BatchUserAssignedIdentity> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getClientId()
        @Generated public String getPrincipalId()
        @Generated public String getResourceId()
    }
    @Fluent
    public final class BatchVmDiskSecurityProfile implements JsonSerializable<BatchVmDiskSecurityProfile> {
        @Generated public BatchVmDiskSecurityProfile()
        @Generated public SecurityEncryptionTypes getSecurityEncryptionType()
        @Generated public BatchVmDiskSecurityProfile setSecurityEncryptionType(SecurityEncryptionTypes securityEncryptionType)
    }
    @Fluent
    public final class BatchVmImageReference implements JsonSerializable<BatchVmImageReference> {
        @Generated public BatchVmImageReference()
        @Generated public String getCommunityGalleryImageId()
        @Generated public BatchVmImageReference setCommunityGalleryImageId(String communityGalleryImageId)
        @Generated public String getExactVersion()
        @Generated public String getOffer()
        @Generated public BatchVmImageReference setOffer(String offer)
        @Generated public String getPublisher()
        @Generated public BatchVmImageReference setPublisher(String publisher)
        @Generated public String getSharedGalleryImageId()
        @Generated public BatchVmImageReference setSharedGalleryImageId(String sharedGalleryImageId)
        @Generated public String getSku()
        @Generated public BatchVmImageReference setSku(String sku)
        @Generated public String getVersion()
        @Generated public BatchVmImageReference setVersion(String version)
        @Generated public String getVirtualMachineImageId()
        @Generated public BatchVmImageReference setVirtualMachineImageId(String virtualMachineImageId)
    }
    @Fluent
    public final class CifsMountConfiguration implements JsonSerializable<CifsMountConfiguration> {
        @Generated public CifsMountConfiguration(String username, String source, String relativeMountPath, String password)
        public CifsMountConfiguration(String username, String source, Path relativeMountPath, String password)
        @Generated public String getMountOptions()
        @Generated public CifsMountConfiguration setMountOptions(String mountOptions)
        @Generated public String getPassword()
        @Generated public String getRelativeMountPath()
        @Generated public String getSource()
        @Generated public String getUsername()
    }
    @Fluent
    public final class ContainerHostBatchBindMountEntry implements JsonSerializable<ContainerHostBatchBindMountEntry> {
        @Generated public ContainerHostBatchBindMountEntry()
        @Generated public ContainerHostBatchBindMountEntry setIsReadOnly(Boolean isReadOnly)
        @Generated public Boolean isReadOnly()
        @Generated public ContainerHostDataPath getSource()
        @Generated public ContainerHostBatchBindMountEntry setSource(ContainerHostDataPath source)
    }
    public final class ContainerHostDataPath extends ExpandableStringEnum<ContainerHostDataPath> {
        @Generated public static final ContainerHostDataPath SHARED = fromString("Shared");
        @Generated public static final ContainerHostDataPath STARTUP = fromString("Startup");
        @Generated public static final ContainerHostDataPath VFS_MOUNTS = fromString("VfsMounts");
        @Generated public static final ContainerHostDataPath TASK = fromString("Task");
        @Generated public static final ContainerHostDataPath JOB_PREP = fromString("JobPrep");
        @Generated public static final ContainerHostDataPath APPLICATIONS = fromString("Applications");
        @Deprecated @Generated public ContainerHostDataPath()
        @Generated public static ContainerHostDataPath fromString(String name)
        @Generated public static Collection<ContainerHostDataPath> values()
    }
    @Fluent
    public final class ContainerRegistryReference implements JsonSerializable<ContainerRegistryReference> {
        @Generated public ContainerRegistryReference()
        @Generated public BatchNodeIdentityReference getIdentityReference()
        @Generated public ContainerRegistryReference setIdentityReference(BatchNodeIdentityReference identityReference)
        @Generated public String getPassword()
        @Generated public ContainerRegistryReference setPassword(String password)
        @Generated public String getRegistryServer()
        @Generated public ContainerRegistryReference setRegistryServer(String registryServer)
        @Generated public String getUsername()
        @Generated public ContainerRegistryReference setUsername(String username)
    }
    public final class ContainerWorkingDirectory extends ExpandableStringEnum<ContainerWorkingDirectory> {
        @Generated public static final ContainerWorkingDirectory TASK_WORKING_DIRECTORY = fromString("taskWorkingDirectory");
        @Generated public static final ContainerWorkingDirectory CONTAINER_IMAGE_DEFAULT = fromString("containerImageDefault");
        @Deprecated @Generated public ContainerWorkingDirectory()
        @Generated public static ContainerWorkingDirectory fromString(String name)
        @Generated public static Collection<ContainerWorkingDirectory> values()
    }
    public class CreateTasksErrorException extends AzureException {
        public CreateTasksErrorException(String message, List<BatchTaskCreateResult> failureTaskList, List<BatchTaskCreateParameters> pendingTaskList)
        public List<BatchTaskCreateResult> getFailureTaskList()
        public List<BatchTaskCreateParameters> getPendingTaskList()
    }
    @Fluent
    public final class DataDisk implements JsonSerializable<DataDisk> {
        @Generated public DataDisk(int logicalUnitNumber, int diskSizeGb)
        @Generated public BatchCachingType getCaching()
        @Generated public DataDisk setCaching(BatchCachingType caching)
        @Generated public int getDiskSizeGb()
        @Generated public int getLogicalUnitNumber()
        @Generated public ManagedDisk getManagedDisk()
        @Generated public DataDisk setManagedDisk(ManagedDisk managedDisk)
    }
    public final class DependencyAction extends ExpandableStringEnum<DependencyAction> {
        @Generated public static final DependencyAction SATISFY = fromString("satisfy");
        @Generated public static final DependencyAction BLOCK = fromString("block");
        @Deprecated @Generated public DependencyAction()
        @Generated public static DependencyAction fromString(String name)
        @Generated public static Collection<DependencyAction> values()
    }
    public final class DiffDiskPlacement extends ExpandableStringEnum<DiffDiskPlacement> {
        @Generated public static final DiffDiskPlacement CACHE_DISK = fromString("cachedisk");
        @Deprecated @Generated public DiffDiskPlacement()
        @Generated public static DiffDiskPlacement fromString(String name)
        @Generated public static Collection<DiffDiskPlacement> values()
    }
    public final class DisableBatchJobOption extends ExpandableStringEnum<DisableBatchJobOption> {
        @Generated public static final DisableBatchJobOption REQUEUE = fromString("requeue");
        @Generated public static final DisableBatchJobOption TERMINATE = fromString("terminate");
        @Generated public static final DisableBatchJobOption WAIT = fromString("wait");
        @Deprecated @Generated public DisableBatchJobOption()
        @Generated public static DisableBatchJobOption fromString(String name)
        @Generated public static Collection<DisableBatchJobOption> values()
    }
    @Fluent
    public final class DiskCustomerManagedKey implements JsonSerializable<DiskCustomerManagedKey> {
        @Generated public DiskCustomerManagedKey()
        @Generated public BatchPoolIdentityReference getIdentityReference()
        @Generated public DiskCustomerManagedKey setIdentityReference(BatchPoolIdentityReference identityReference)
        @Generated public String getKeyUrl()
        @Generated public DiskCustomerManagedKey setKeyUrl(String keyUrl)
        @Generated public Boolean isRotationToLatestKeyVersionEnabled()
        @Generated public DiskCustomerManagedKey setRotationToLatestKeyVersionEnabled(Boolean rotationToLatestKeyVersionEnabled)
    }
    @Fluent
    public final class DiskEncryptionConfiguration implements JsonSerializable<DiskEncryptionConfiguration> {
        @Generated public DiskEncryptionConfiguration()
        @Generated public DiskCustomerManagedKey getCustomerManagedKey()
        @Generated public DiskEncryptionConfiguration setCustomerManagedKey(DiskCustomerManagedKey customerManagedKey)
        @Generated public List<DiskEncryptionTarget> getTargets()
        @Generated public DiskEncryptionConfiguration setTargets(List<DiskEncryptionTarget> targets)
    }
    @Fluent
    public final class DiskEncryptionSetParameters implements JsonSerializable<DiskEncryptionSetParameters> {
        @Generated public DiskEncryptionSetParameters()
        @Generated public String getId()
        @Generated public DiskEncryptionSetParameters setId(String id)
    }
    public final class DiskEncryptionTarget extends ExpandableStringEnum<DiskEncryptionTarget> {
        @Generated public static final DiskEncryptionTarget OS_DISK = fromString("osdisk");
        @Generated public static final DiskEncryptionTarget TEMPORARY_DISK = fromString("temporarydisk");
        @Deprecated @Generated public DiskEncryptionTarget()
        @Generated public static DiskEncryptionTarget fromString(String name)
        @Generated public static Collection<DiskEncryptionTarget> values()
    }
    public final class DynamicVNetAssignmentScope extends ExpandableStringEnum<DynamicVNetAssignmentScope> {
        @Generated public static final DynamicVNetAssignmentScope NONE = fromString("none");
        @Generated public static final DynamicVNetAssignmentScope JOB = fromString("job");
        @Deprecated @Generated public DynamicVNetAssignmentScope()
        @Generated public static DynamicVNetAssignmentScope fromString(String name)
        @Generated public static Collection<DynamicVNetAssignmentScope> values()
    }
    public final class ElevationLevel extends ExpandableStringEnum<ElevationLevel> {
        @Generated public static final ElevationLevel NON_ADMIN = fromString("nonadmin");
        @Generated public static final ElevationLevel ADMIN = fromString("admin");
        @Deprecated @Generated public ElevationLevel()
        @Generated public static ElevationLevel fromString(String name)
        @Generated public static Collection<ElevationLevel> values()
    }
    @Fluent
    public final class EnvironmentSetting implements JsonSerializable<EnvironmentSetting> {
        @Generated public EnvironmentSetting(String name)
        @Generated public String getName()
        @Generated public String getValue()
        @Generated public EnvironmentSetting setValue(String value)
    }
    @Immutable
    public final class ExitCodeMapping implements JsonSerializable<ExitCodeMapping> {
        @Generated public ExitCodeMapping(int code, ExitOptions exitOptions)
        @Generated public int getCode()
        @Generated public ExitOptions getExitOptions()
    }
    @Immutable
    public final class ExitCodeRangeMapping implements JsonSerializable<ExitCodeRangeMapping> {
        @Generated public ExitCodeRangeMapping(int start, int end, ExitOptions exitOptions)
        @Generated public int getEnd()
        @Generated public ExitOptions getExitOptions()
        @Generated public int getStart()
    }
    @Fluent
    public final class ExitConditions implements JsonSerializable<ExitConditions> {
        @Generated public ExitConditions()
        @Generated public ExitOptions getDefaultProperty()
        @Generated public ExitConditions setDefaultProperty(ExitOptions defaultProperty)
        @Generated public List<ExitCodeRangeMapping> getExitCodeRanges()
        @Generated public ExitConditions setExitCodeRanges(List<ExitCodeRangeMapping> exitCodeRanges)
        @Generated public List<ExitCodeMapping> getExitCodes()
        @Generated public ExitConditions setExitCodes(List<ExitCodeMapping> exitCodes)
        @Generated public ExitOptions getFileUploadError()
        @Generated public ExitConditions setFileUploadError(ExitOptions fileUploadError)
        @Generated public ExitOptions getPreProcessingError()
        @Generated public ExitConditions setPreProcessingError(ExitOptions preProcessingError)
    }
    @Fluent
    public final class ExitOptions implements JsonSerializable<ExitOptions> {
        @Generated public ExitOptions()
        @Generated public DependencyAction getDependencyAction()
        @Generated public ExitOptions setDependencyAction(DependencyAction dependencyAction)
        @Generated public BatchJobActionKind getJobAction()
        @Generated public ExitOptions setJobAction(BatchJobActionKind jobAction)
    }
    @Immutable
    public final class FileProperties implements JsonSerializable<FileProperties> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public long getContentLength()
        @Generated public String getContentType()
        @Generated public OffsetDateTime getCreationTime()
        @Generated public String getFileMode()
        @Generated public OffsetDateTime getLastModified()
    }
    @Fluent
    public final class HardwareProfile implements JsonSerializable<HardwareProfile> {
        @Generated public HardwareProfile()
        @Generated public VMSizeProperties getVmSizeProperties()
        @Generated public HardwareProfile setVmSizeProperties(VMSizeProperties vmSizeProperties)
    }
    @Fluent
    public final class HostEndpointSettings implements JsonSerializable<HostEndpointSettings> {
        @Generated public HostEndpointSettings()
        @Generated public String getInVmAccessControlProfileReferenceId()
        @Generated public HostEndpointSettings setInVmAccessControlProfileReferenceId(String inVmAccessControlProfileReferenceId)
        @Generated public HostEndpointSettingsModeTypes getMode()
        @Generated public HostEndpointSettings setMode(HostEndpointSettingsModeTypes mode)
    }
    public final class HostEndpointSettingsModeTypes extends ExpandableStringEnum<HostEndpointSettingsModeTypes> {
        @Generated public static final HostEndpointSettingsModeTypes AUDIT = fromString("Audit");
        @Generated public static final HostEndpointSettingsModeTypes ENFORCE = fromString("Enforce");
        @Deprecated @Generated public HostEndpointSettingsModeTypes()
        @Generated public static HostEndpointSettingsModeTypes fromString(String name)
        @Generated public static Collection<HostEndpointSettingsModeTypes> values()
    }
    public final class IPFamily extends ExpandableStringEnum<IPFamily> {
        @Generated public static final IPFamily IPV4 = fromString("IPv4");
        @Generated public static final IPFamily IPV6 = fromString("IPv6");
        @Deprecated @Generated public IPFamily()
        @Generated public static IPFamily fromString(String name)
        @Generated public static Collection<IPFamily> values()
    }
    @Fluent
    public final class IPTag implements JsonSerializable<IPTag> {
        @Generated public IPTag()
        @Generated public String getIpTagType()
        @Generated public IPTag setIpTagType(String ipTagType)
        @Generated public String getTag()
        @Generated public IPTag setTag(String tag)
    }
    public final class ImageVerificationType extends ExpandableStringEnum<ImageVerificationType> {
        @Generated public static final ImageVerificationType VERIFIED = fromString("verified");
        @Generated public static final ImageVerificationType UNVERIFIED = fromString("unverified");
        @Deprecated @Generated public ImageVerificationType()
        @Generated public static ImageVerificationType fromString(String name)
        @Generated public static Collection<ImageVerificationType> values()
    }
    @Immutable
    public final class InboundEndpoint implements JsonSerializable<InboundEndpoint> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public int getBackendPort()
        @Generated public int getFrontendPort()
        @Generated public String getName()
        @Generated public InboundEndpointProtocol getProtocol()
        @Generated public String getPublicFQDN()
        @Generated public String getPublicIpAddress()
    }
    public final class InboundEndpointProtocol extends ExpandableStringEnum<InboundEndpointProtocol> {
        @Generated public static final InboundEndpointProtocol TCP = fromString("tcp");
        @Generated public static final InboundEndpointProtocol UDP = fromString("udp");
        @Deprecated @Generated public InboundEndpointProtocol()
        @Generated public static InboundEndpointProtocol fromString(String name)
        @Generated public static Collection<InboundEndpointProtocol> values()
    }
    @Immutable
    public final class InstanceViewStatus implements JsonSerializable<InstanceViewStatus> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getCode()
        @Generated public String getDisplayStatus()
        @Generated public StatusLevelTypes getLevel()
        @Generated public String getMessage()
        @Generated public OffsetDateTime getTime()
    }
    public final class IpAddressProvisioningType extends ExpandableStringEnum<IpAddressProvisioningType> {
        @Generated public static final IpAddressProvisioningType BATCH_MANAGED = fromString("batchmanaged");
        @Generated public static final IpAddressProvisioningType USER_MANAGED = fromString("usermanaged");
        @Generated public static final IpAddressProvisioningType NO_PUBLIC_IP_ADDRESSES = fromString("nopublicipaddresses");
        @Deprecated @Generated public IpAddressProvisioningType()
        @Generated public static IpAddressProvisioningType fromString(String name)
        @Generated public static Collection<IpAddressProvisioningType> values()
    }
    @Fluent
    public final class LinuxUserConfiguration implements JsonSerializable<LinuxUserConfiguration> {
        @Generated public LinuxUserConfiguration()
        @Generated public Integer getGid()
        @Generated public LinuxUserConfiguration setGid(Integer gid)
        @Generated public String getSshPrivateKey()
        @Generated public LinuxUserConfiguration setSshPrivateKey(String sshPrivateKey)
        @Generated public Integer getUid()
        @Generated public LinuxUserConfiguration setUid(Integer uid)
    }
    public final class LoginMode extends ExpandableStringEnum<LoginMode> {
        @Generated public static final LoginMode BATCH = fromString("batch");
        @Generated public static final LoginMode INTERACTIVE = fromString("interactive");
        @Deprecated @Generated public LoginMode()
        @Generated public static LoginMode fromString(String name)
        @Generated public static Collection<LoginMode> values()
    }
    @Fluent
    public final class ManagedDisk implements JsonSerializable<ManagedDisk> {
        @Generated public ManagedDisk()
        @Generated public DiskEncryptionSetParameters getDiskEncryptionSet()
        @Generated public ManagedDisk setDiskEncryptionSet(DiskEncryptionSetParameters diskEncryptionSet)
        @Generated public BatchVmDiskSecurityProfile getSecurityProfile()
        @Generated public ManagedDisk setSecurityProfile(BatchVmDiskSecurityProfile securityProfile)
        @Generated public StorageAccountType getStorageAccountType()
        @Generated public ManagedDisk setStorageAccountType(StorageAccountType storageAccountType)
    }
    @Fluent
    public final class MaxInstancePercentPerZonePolicy implements JsonSerializable<MaxInstancePercentPerZonePolicy> {
        @Generated public MaxInstancePercentPerZonePolicy(int value)
        @Generated public Boolean isEnabled()
        @Generated public MaxInstancePercentPerZonePolicy setEnabled(Boolean enabled)
        @Generated public int getValue()
    }
    @Fluent
    public final class MountConfiguration implements JsonSerializable<MountConfiguration> {
        @Generated public MountConfiguration()
        @Generated public AzureBlobFileSystemConfiguration getAzureBlobFileSystemConfiguration()
        @Generated public MountConfiguration setAzureBlobFileSystemConfiguration(AzureBlobFileSystemConfiguration azureBlobFileSystemConfiguration)
        @Generated public AzureFileShareConfiguration getAzureFileShareConfiguration()
        @Generated public MountConfiguration setAzureFileShareConfiguration(AzureFileShareConfiguration azureFileShareConfiguration)
        @Generated public CifsMountConfiguration getCifsMountConfiguration()
        @Generated public MountConfiguration setCifsMountConfiguration(CifsMountConfiguration cifsMountConfiguration)
        @Generated public NfsMountConfiguration getNfsMountConfiguration()
        @Generated public MountConfiguration setNfsMountConfiguration(NfsMountConfiguration nfsMountConfiguration)
    }
    @Fluent
    public final class MultiInstanceSettings implements JsonSerializable<MultiInstanceSettings> {
        @Generated public MultiInstanceSettings(String coordinationCommandLine)
        @Generated public List<ResourceFile> getCommonResourceFiles()
        @Generated public MultiInstanceSettings setCommonResourceFiles(List<ResourceFile> commonResourceFiles)
        @Generated public String getCoordinationCommandLine()
        @Generated public Integer getNumberOfInstances()
        @Generated public MultiInstanceSettings setNumberOfInstances(Integer numberOfInstances)
    }
    @Immutable
    public final class NameValuePair implements JsonSerializable<NameValuePair> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getName()
        @Generated public String getValue()
    }
    @Fluent
    public final class NetworkConfiguration implements JsonSerializable<NetworkConfiguration> {
        @Generated public NetworkConfiguration()
        @Generated public DynamicVNetAssignmentScope getDynamicVnetAssignmentScope()
        @Generated public NetworkConfiguration setDynamicVnetAssignmentScope(DynamicVNetAssignmentScope dynamicVnetAssignmentScope)
        @Generated public Boolean isEnableAcceleratedNetworking()
        @Generated public NetworkConfiguration setEnableAcceleratedNetworking(Boolean enableAcceleratedNetworking)
        @Generated public BatchPoolEndpointConfiguration getEndpointConfiguration()
        @Generated public NetworkConfiguration setEndpointConfiguration(BatchPoolEndpointConfiguration endpointConfiguration)
        @Generated public BatchPublicIpAddressConfiguration getPublicIpAddressConfiguration()
        @Generated public NetworkConfiguration setPublicIpAddressConfiguration(BatchPublicIpAddressConfiguration publicIpAddressConfiguration)
        @Generated public String getSubnetId()
        @Generated public NetworkConfiguration setSubnetId(String subnetId)
    }
    @Fluent
    public final class NetworkSecurityGroupRule implements JsonSerializable<NetworkSecurityGroupRule> {
        @Generated public NetworkSecurityGroupRule(int priority, NetworkSecurityGroupRuleAccess access, String sourceAddressPrefix)
        @Generated public NetworkSecurityGroupRuleAccess getAccess()
        @Generated public int getPriority()
        @Generated public String getSourceAddressPrefix()
        @Generated public List<String> getSourcePortRanges()
        @Generated public NetworkSecurityGroupRule setSourcePortRanges(List<String> sourcePortRanges)
    }
    public final class NetworkSecurityGroupRuleAccess extends ExpandableStringEnum<NetworkSecurityGroupRuleAccess> {
        @Generated public static final NetworkSecurityGroupRuleAccess ALLOW = fromString("allow");
        @Generated public static final NetworkSecurityGroupRuleAccess DENY = fromString("deny");
        @Deprecated @Generated public NetworkSecurityGroupRuleAccess()
        @Generated public static NetworkSecurityGroupRuleAccess fromString(String name)
        @Generated public static Collection<NetworkSecurityGroupRuleAccess> values()
    }
    @Fluent
    public final class NfsMountConfiguration implements JsonSerializable<NfsMountConfiguration> {
        @Generated public NfsMountConfiguration(String source, String relativeMountPath)
        public NfsMountConfiguration(String source, Path relativeMountPath)
        @Generated public String getMountOptions()
        @Generated public NfsMountConfiguration setMountOptions(String mountOptions)
        @Generated public String getRelativeMountPath()
        @Generated public String getSource()
    }
    public final class OsType extends ExpandableStringEnum<OsType> {
        @Generated public static final OsType LINUX = fromString("linux");
        @Generated public static final OsType WINDOWS = fromString("windows");
        @Deprecated @Generated public OsType()
        @Generated public static OsType fromString(String name)
        @Generated public static Collection<OsType> values()
    }
    @Immutable
    public final class OutputFile implements JsonSerializable<OutputFile> {
        @Generated public OutputFile(String filePattern, OutputFileDestination destination, OutputFileUploadConfig uploadOptions)
        @Generated public OutputFileDestination getDestination()
        @Generated public String getFilePattern()
        @Generated public OutputFileUploadConfig getUploadOptions()
    }
    @Fluent
    public final class OutputFileBlobContainerDestination implements JsonSerializable<OutputFileBlobContainerDestination> {
        @Generated public OutputFileBlobContainerDestination(String containerUrl)
        @Generated public String getContainerUrl()
        @Generated public BatchNodeIdentityReference getIdentityReference()
        @Generated public OutputFileBlobContainerDestination setIdentityReference(BatchNodeIdentityReference identityReference)
        @Generated public String getPath()
        @Generated public OutputFileBlobContainerDestination setPath(String path)
        @Generated public List<OutputFileUploadHeader> getUploadHeaders()
        @Generated public OutputFileBlobContainerDestination setUploadHeaders(List<OutputFileUploadHeader> uploadHeaders)
    }
    @Fluent
    public final class OutputFileDestination implements JsonSerializable<OutputFileDestination> {
        @Generated public OutputFileDestination()
        @Generated public OutputFileBlobContainerDestination getContainer()
        @Generated public OutputFileDestination setContainer(OutputFileBlobContainerDestination container)
    }
    public final class OutputFileUploadCondition extends ExpandableStringEnum<OutputFileUploadCondition> {
        @Generated public static final OutputFileUploadCondition TASK_SUCCESS = fromString("tasksuccess");
        @Generated public static final OutputFileUploadCondition TASK_FAILURE = fromString("taskfailure");
        @Generated public static final OutputFileUploadCondition TASK_COMPLETION = fromString("taskcompletion");
        @Deprecated @Generated public OutputFileUploadCondition()
        @Generated public static OutputFileUploadCondition fromString(String name)
        @Generated public static Collection<OutputFileUploadCondition> values()
    }
    @Immutable
    public final class OutputFileUploadConfig implements JsonSerializable<OutputFileUploadConfig> {
        @Generated public OutputFileUploadConfig(OutputFileUploadCondition uploadCondition)
        @Generated public OutputFileUploadCondition getUploadCondition()
    }
    @Fluent
    public final class OutputFileUploadHeader implements JsonSerializable<OutputFileUploadHeader> {
        @Generated public OutputFileUploadHeader(String name)
        @Generated public String getName()
        @Generated public String getValue()
        @Generated public OutputFileUploadHeader setValue(String value)
    }
    @Immutable
    public final class Placement implements JsonSerializable<Placement> {
        @Generated public Placement(ZonePlacementPolicyType zonePlacementPolicy)
        @Generated public ZonePlacementPolicyType getZonePlacementPolicy()
    }
    @Fluent
    public final class ProxyAgentSettings implements JsonSerializable<ProxyAgentSettings> {
        @Generated public ProxyAgentSettings()
        @Generated public Boolean isEnabled()
        @Generated public ProxyAgentSettings setEnabled(Boolean enabled)
        @Generated public HostEndpointSettings getImds()
        @Generated public ProxyAgentSettings setImds(HostEndpointSettings imds)
        @Generated public HostEndpointSettings getWireServer()
        @Generated public ProxyAgentSettings setWireServer(HostEndpointSettings wireServer)
    }
    @Immutable
    public final class RecentBatchJob implements JsonSerializable<RecentBatchJob> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getId()
        @Generated public String getUrl()
    }
    @Immutable
    public final class ResiliencyPolicy implements JsonSerializable<ResiliencyPolicy> {
        @Generated public ResiliencyPolicy(ZoneAllocationPolicy zoneAllocationPolicy)
        @Generated public ZoneAllocationPolicy getZoneAllocationPolicy()
    }
    @Immutable
    public final class ResizeError implements JsonSerializable<ResizeError> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getCode()
        @Generated public String getMessage()
        @Generated public List<NameValuePair> getValues()
    }
    @Fluent
    public final class ResourceFile implements JsonSerializable<ResourceFile> {
        @Generated public ResourceFile()
        @Generated public String getAutoStorageContainerName()
        @Generated public ResourceFile setAutoStorageContainerName(String autoStorageContainerName)
        @Generated public String getBlobPrefix()
        @Generated public ResourceFile setBlobPrefix(String blobPrefix)
        @Generated public String getFileMode()
        @Generated public ResourceFile setFileMode(String fileMode)
        @Generated public String getFilePath()
        @Generated public ResourceFile setFilePath(String filePath)
        public ResourceFile setFilePath(Path filePath)
        @Generated public String getHttpUrl()
        @Generated public ResourceFile setHttpUrl(String httpUrl)
        @Generated public BatchNodeIdentityReference getIdentityReference()
        @Generated public ResourceFile setIdentityReference(BatchNodeIdentityReference identityReference)
        @Generated public String getStorageContainerUrl()
        @Generated public ResourceFile setStorageContainerUrl(String storageContainerUrl)
    }
    @Fluent
    public final class RollingUpgradePolicy implements JsonSerializable<RollingUpgradePolicy> {
        @Generated public RollingUpgradePolicy()
        @Generated public Boolean isEnableCrossZoneUpgrade()
        @Generated public RollingUpgradePolicy setEnableCrossZoneUpgrade(Boolean enableCrossZoneUpgrade)
        @Generated public Integer getMaxBatchInstancePercent()
        @Generated public RollingUpgradePolicy setMaxBatchInstancePercent(Integer maxBatchInstancePercent)
        @Generated public Integer getMaxUnhealthyInstancePercent()
        @Generated public RollingUpgradePolicy setMaxUnhealthyInstancePercent(Integer maxUnhealthyInstancePercent)
        @Generated public Integer getMaxUnhealthyUpgradedInstancePercent()
        @Generated public RollingUpgradePolicy setMaxUnhealthyUpgradedInstancePercent(Integer maxUnhealthyUpgradedInstancePercent)
        @Generated public Duration getPauseTimeBetweenBatches()
        @Generated public RollingUpgradePolicy setPauseTimeBetweenBatches(Duration pauseTimeBetweenBatches)
        @Generated public Boolean isPrioritizeUnhealthyInstances()
        @Generated public RollingUpgradePolicy setPrioritizeUnhealthyInstances(Boolean prioritizeUnhealthyInstances)
        @Generated public Boolean isRollbackFailedInstancesOnPolicyBreach()
        @Generated public RollingUpgradePolicy setRollbackFailedInstancesOnPolicyBreach(Boolean rollbackFailedInstancesOnPolicyBreach)
    }
    public final class SchedulingState extends ExpandableStringEnum<SchedulingState> {
        @Generated public static final SchedulingState ENABLED = fromString("enabled");
        @Generated public static final SchedulingState DISABLED = fromString("disabled");
        @Deprecated @Generated public SchedulingState()
        @Generated public static SchedulingState fromString(String name)
        @Generated public static Collection<SchedulingState> values()
    }
    public final class SecurityEncryptionTypes extends ExpandableStringEnum<SecurityEncryptionTypes> {
        @Generated public static final SecurityEncryptionTypes NON_PERSISTED_TPM = fromString("NonPersistedTPM");
        @Generated public static final SecurityEncryptionTypes VMGUEST_STATE_ONLY = fromString("VMGuestStateOnly");
        @Generated public static final SecurityEncryptionTypes DISK_WITH_VMGUEST_STATE = fromString("DiskWithVMGuestState");
        @Deprecated @Generated public SecurityEncryptionTypes()
        @Generated public static SecurityEncryptionTypes fromString(String name)
        @Generated public static Collection<SecurityEncryptionTypes> values()
    }
    @Fluent
    public final class SecurityProfile implements JsonSerializable<SecurityProfile> {
        @Generated public SecurityProfile()
        @Generated public Boolean isEncryptionAtHost()
        @Generated public SecurityProfile setEncryptionAtHost(Boolean encryptionAtHost)
        @Generated public ProxyAgentSettings getProxyAgentSettings()
        @Generated public SecurityProfile setProxyAgentSettings(ProxyAgentSettings proxyAgentSettings)
        @Generated public SecurityTypes getSecurityType()
        @Generated public SecurityProfile setSecurityType(SecurityTypes securityType)
        @Generated public BatchUefiSettings getUefiSettings()
        @Generated public SecurityProfile setUefiSettings(BatchUefiSettings uefiSettings)
    }
    public final class SecurityTypes extends ExpandableStringEnum<SecurityTypes> {
        @Generated public static final SecurityTypes TRUSTED_LAUNCH = fromString("trustedLaunch");
        @Generated public static final SecurityTypes CONFIDENTIAL_VM = fromString("confidentialvm");
        @Deprecated @Generated public SecurityTypes()
        @Generated public static SecurityTypes fromString(String name)
        @Generated public static Collection<SecurityTypes> values()
    }
    @Immutable
    public final class ServiceArtifactReference implements JsonSerializable<ServiceArtifactReference> {
        @Generated public ServiceArtifactReference(String id)
        @Generated public String getId()
    }
    public final class StatusLevelTypes extends ExpandableStringEnum<StatusLevelTypes> {
        @Generated public static final StatusLevelTypes ERROR = fromString("Error");
        @Generated public static final StatusLevelTypes INFO = fromString("Info");
        @Generated public static final StatusLevelTypes WARNING = fromString("Warning");
        @Deprecated @Generated public StatusLevelTypes()
        @Generated public static StatusLevelTypes fromString(String name)
        @Generated public static Collection<StatusLevelTypes> values()
    }
    public final class StorageAccountType extends ExpandableStringEnum<StorageAccountType> {
        @Generated public static final StorageAccountType STANDARD_LRS = fromString("standard_lrs");
        @Generated public static final StorageAccountType PREMIUM_LRS = fromString("premium_lrs");
        @Generated public static final StorageAccountType STANDARD_SSDLRS = fromString("standardssd_lrs");
        @Deprecated @Generated public StorageAccountType()
        @Generated public static StorageAccountType fromString(String name)
        @Generated public static Collection<StorageAccountType> values()
    }
    @Fluent
    public final class SupportedBatchImagesListOptions {
        @Generated public SupportedBatchImagesListOptions()
        @Generated public String getFilter()
        @Generated public SupportedBatchImagesListOptions setFilter(String filter)
        @Generated public Integer getMaxPageSize()
        @Generated public SupportedBatchImagesListOptions setMaxPageSize(Integer maxPageSize)
        @Generated public Duration getTimeout()
        @Generated public SupportedBatchImagesListOptions setTimeout(Duration timeout)
    }
    public final class UpgradeMode extends ExpandableStringEnum<UpgradeMode> {
        @Generated public static final UpgradeMode AUTOMATIC = fromString("automatic");
        @Generated public static final UpgradeMode MANUAL = fromString("manual");
        @Generated public static final UpgradeMode ROLLING = fromString("rolling");
        @Deprecated @Generated public UpgradeMode()
        @Generated public static UpgradeMode fromString(String name)
        @Generated public static Collection<UpgradeMode> values()
    }
    @Fluent
    public final class UpgradePolicy implements JsonSerializable<UpgradePolicy> {
        @Generated public UpgradePolicy(UpgradeMode mode)
        @Generated public AutomaticOsUpgradePolicy getAutomaticOsUpgradePolicy()
        @Generated public UpgradePolicy setAutomaticOsUpgradePolicy(AutomaticOsUpgradePolicy automaticOsUpgradePolicy)
        @Generated public UpgradeMode getMode()
        @Generated public RollingUpgradePolicy getRollingUpgradePolicy()
        @Generated public UpgradePolicy setRollingUpgradePolicy(RollingUpgradePolicy rollingUpgradePolicy)
    }
    @Fluent
    public final class UploadBatchServiceLogsParameters implements JsonSerializable<UploadBatchServiceLogsParameters> {
        @Generated public UploadBatchServiceLogsParameters(String containerUrl, OffsetDateTime startTime)
        @Generated public String getContainerUrl()
        @Generated public OffsetDateTime getEndTime()
        @Generated public UploadBatchServiceLogsParameters setEndTime(OffsetDateTime endTime)
        @Generated public BatchNodeIdentityReference getIdentityReference()
        @Generated public UploadBatchServiceLogsParameters setIdentityReference(BatchNodeIdentityReference identityReference)
        @Generated public OffsetDateTime getStartTime()
    }
    @Immutable
    public final class UploadBatchServiceLogsResult implements JsonSerializable<UploadBatchServiceLogsResult> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public int getNumberOfFilesUploaded()
        @Generated public String getVirtualDirectoryName()
    }
    @Fluent
    public final class UserAccount implements JsonSerializable<UserAccount> {
        @Generated public UserAccount(String name, String password)
        @Generated public ElevationLevel getElevationLevel()
        @Generated public UserAccount setElevationLevel(ElevationLevel elevationLevel)
        @Generated public LinuxUserConfiguration getLinuxUserConfiguration()
        @Generated public UserAccount setLinuxUserConfiguration(LinuxUserConfiguration linuxUserConfiguration)
        @Generated public String getName()
        @Generated public String getPassword()
        @Generated public WindowsUserConfiguration getWindowsUserConfiguration()
        @Generated public UserAccount setWindowsUserConfiguration(WindowsUserConfiguration windowsUserConfiguration)
    }
    @Fluent
    public final class UserIdentity implements JsonSerializable<UserIdentity> {
        @Generated public UserIdentity()
        @Generated public AutoUserSpecification getAutoUser()
        @Generated public UserIdentity setAutoUser(AutoUserSpecification autoUser)
        @Generated public String getUsername()
        @Generated public UserIdentity setUsername(String username)
    }
    @Fluent
    public final class VMSizeProperties implements JsonSerializable<VMSizeProperties> {
        @Generated public VMSizeProperties()
        @Generated public Integer getVCPUsAvailable()
        @Generated public VMSizeProperties setVCPUsAvailable(Integer vCPUsAvailable)
        @Generated public Integer getVCPUsPerCore()
        @Generated public VMSizeProperties setVCPUsPerCore(Integer vCPUsPerCore)
    }
    @Fluent
    public final class VirtualMachineConfiguration implements JsonSerializable<VirtualMachineConfiguration> {
        @Generated public VirtualMachineConfiguration(BatchVmImageReference imageReference, String nodeAgentSkuId)
        @Generated public BatchContainerConfiguration getContainerConfiguration()
        @Generated public VirtualMachineConfiguration setContainerConfiguration(BatchContainerConfiguration containerConfiguration)
        @Generated public List<DataDisk> getDataDisks()
        @Generated public VirtualMachineConfiguration setDataDisks(List<DataDisk> dataDisks)
        @Generated public DiskEncryptionConfiguration getDiskEncryptionConfiguration()
        @Generated public VirtualMachineConfiguration setDiskEncryptionConfiguration(DiskEncryptionConfiguration diskEncryptionConfiguration)
        @Generated public List<VirtualMachineExtension> getExtensions()
        @Generated public VirtualMachineConfiguration setExtensions(List<VirtualMachineExtension> extensions)
        @Generated public HardwareProfile getHardwareProfile()
        @Generated public VirtualMachineConfiguration setHardwareProfile(HardwareProfile hardwareProfile)
        @Generated public BatchVmImageReference getImageReference()
        @Generated public String getLicenseType()
        @Generated public VirtualMachineConfiguration setLicenseType(String licenseType)
        @Generated public String getNodeAgentSkuId()
        @Generated public BatchNodePlacementConfiguration getNodePlacementConfiguration()
        @Generated public VirtualMachineConfiguration setNodePlacementConfiguration(BatchNodePlacementConfiguration nodePlacementConfiguration)
        @Generated public BatchOsDisk getOsDisk()
        @Generated public VirtualMachineConfiguration setOsDisk(BatchOsDisk osDisk)
        @Generated public Placement getPlacement()
        @Generated public VirtualMachineConfiguration setPlacement(Placement placement)
        @Generated public ResiliencyPolicy getResiliencyPolicy()
        @Generated public VirtualMachineConfiguration setResiliencyPolicy(ResiliencyPolicy resiliencyPolicy)
        @Generated public SecurityProfile getSecurityProfile()
        @Generated public VirtualMachineConfiguration setSecurityProfile(SecurityProfile securityProfile)
        @Generated public ServiceArtifactReference getServiceArtifactReference()
        @Generated public VirtualMachineConfiguration setServiceArtifactReference(ServiceArtifactReference serviceArtifactReference)
        @Generated public WindowsConfiguration getWindowsConfiguration()
        @Generated public VirtualMachineConfiguration setWindowsConfiguration(WindowsConfiguration windowsConfiguration)
    }
    @Immutable
    public final class VirtualMachineDetails implements JsonSerializable<VirtualMachineDetails> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public BatchVmImageReference getImageReference()
        @Generated public String getScaleSetVmResourceId()
    }
    @Fluent
    public final class VirtualMachineExtension implements JsonSerializable<VirtualMachineExtension> {
        @Generated public VirtualMachineExtension(String name, String publisher, String type)
        @Generated public Boolean isAutoUpgradeMinorVersion()
        @Generated public VirtualMachineExtension setAutoUpgradeMinorVersion(Boolean autoUpgradeMinorVersion)
        @Generated public Boolean isEnableAutomaticUpgrade()
        @Generated public VirtualMachineExtension setEnableAutomaticUpgrade(Boolean enableAutomaticUpgrade)
        @Generated public String getName()
        @Generated public Map<String, String> getProtectedSettings()
        @Generated public VirtualMachineExtension setProtectedSettings(Map<String, String> protectedSettings)
        @Generated public List<String> getProvisionAfterExtensions()
        @Generated public VirtualMachineExtension setProvisionAfterExtensions(List<String> provisionAfterExtensions)
        @Generated public String getPublisher()
        @Generated public Map<String, String> getSettings()
        @Generated public VirtualMachineExtension setSettings(Map<String, String> settings)
        @Generated public String getType()
        @Generated public String getTypeHandlerVersion()
        @Generated public VirtualMachineExtension setTypeHandlerVersion(String typeHandlerVersion)
    }
    @Immutable
    public final class VirtualMachineExtensionInstanceView implements JsonSerializable<VirtualMachineExtensionInstanceView> {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Generated public String getName()
        @Generated public List<InstanceViewStatus> getStatuses()
        @Generated public List<InstanceViewStatus> getSubStatuses()
    }
    public final class VirtualMachineSkuAllocationStrategy extends ExpandableStringEnum<VirtualMachineSkuAllocationStrategy> {
        @Generated public static final VirtualMachineSkuAllocationStrategy LOWEST_PRICE = fromString("lowestPrice");
        @Generated public static final VirtualMachineSkuAllocationStrategy CAPACITY_OPTIMIZED = fromString("capacityOptimized");
        @Generated public static final VirtualMachineSkuAllocationStrategy PRIORITIZED = fromString("prioritized");
        @Deprecated @Generated public VirtualMachineSkuAllocationStrategy()
        @Generated public static VirtualMachineSkuAllocationStrategy fromString(String name)
        @Generated public static Collection<VirtualMachineSkuAllocationStrategy> values()
    }
    @Fluent
    public final class VirtualMachineSkuProfile implements JsonSerializable<VirtualMachineSkuProfile> {
        @Generated public VirtualMachineSkuProfile()
        @Generated public VirtualMachineSkuAllocationStrategy getAllocationStrategy()
        @Generated public VirtualMachineSkuProfile setAllocationStrategy(VirtualMachineSkuAllocationStrategy allocationStrategy)
        @Generated public List<VirtualMachineSkuProfileVmSize> getVmSizes()
        @Generated public VirtualMachineSkuProfile setVmSizes(List<VirtualMachineSkuProfileVmSize> vmSizes)
    }
    @Fluent
    public final class VirtualMachineSkuProfileVmSize implements JsonSerializable<VirtualMachineSkuProfileVmSize> {
        @Generated public VirtualMachineSkuProfileVmSize(String name)
        @Generated public String getName()
        @Generated public Integer getRank()
        @Generated public VirtualMachineSkuProfileVmSize setRank(Integer rank)
    }
    @Fluent
    public final class WindowsConfiguration implements JsonSerializable<WindowsConfiguration> {
        @Generated public WindowsConfiguration()
        @Generated public Boolean isEnableAutomaticUpdates()
        @Generated public WindowsConfiguration setEnableAutomaticUpdates(Boolean enableAutomaticUpdates)
    }
    @Fluent
    public final class WindowsUserConfiguration implements JsonSerializable<WindowsUserConfiguration> {
        @Generated public WindowsUserConfiguration()
        @Generated public LoginMode getLoginMode()
        @Generated public WindowsUserConfiguration setLoginMode(LoginMode loginMode)
    }
    @Fluent
    public final class ZoneAllocationPolicy implements JsonSerializable<ZoneAllocationPolicy> {
        @Generated public ZoneAllocationPolicy()
        @Generated public MaxInstancePercentPerZonePolicy getMaxInstancePercentPerZonePolicy()
        @Generated public ZoneAllocationPolicy setMaxInstancePercentPerZonePolicy(MaxInstancePercentPerZonePolicy maxInstancePercentPerZonePolicy)
        @Generated public Integer getMaxZoneCount()
        @Generated public ZoneAllocationPolicy setMaxZoneCount(Integer maxZoneCount)
    }
    public final class ZonePlacementPolicyType extends ExpandableStringEnum<ZonePlacementPolicyType> {
        @Generated public static final ZonePlacementPolicyType AUTO = fromString("Auto");
        @Deprecated @Generated public ZonePlacementPolicyType()
        @Generated public static ZonePlacementPolicyType fromString(String name)
        @Generated public static Collection<ZonePlacementPolicyType> values()
    }
}
```
