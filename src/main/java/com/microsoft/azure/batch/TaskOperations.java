/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.*;
import com.microsoft.rest.ServiceResponseWithHeaders;
import com.sun.javafx.tk.Toolkit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TaskOperations implements IInheritedBehaviors {
    TaskOperations(BatchClient batchClient, Collection<BatchClientBehavior> customBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, customBehaviors);
    }

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    @Override
    public Collection<BatchClientBehavior> getCustomBehaviors() {
        return _customBehaviors;
    }

    @Override
    public void setCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
    }

    public void createTask(String jobId, TaskAddParameter taskToAdd) throws BatchErrorException, IOException {
        createTask(jobId, taskToAdd, null);
    }

    public void createTask(String jobId, TaskAddParameter taskToAdd, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskAddOptions options = new TaskAddOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().getTaskOperations().add(jobId, taskToAdd, options);
    }

    public void createTasks(String jobId, List<TaskAddParameter> taskList) throws BatchErrorException, IOException {
        createTasks(jobId, taskList, null);
    }

    public void createTasks(String jobId, List<TaskAddParameter> taskList, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        final int MAX_TASKS_PER_REQUEST = 100;

        TaskAddCollectionOptions options = new TaskAddCollectionOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        List<TaskAddParameter> pendingList = new ArrayList<>(taskList);

        while (!pendingList.isEmpty()) {
            List<TaskAddParameter> currentList = pendingList.subList(0, MAX_TASKS_PER_REQUEST - 1);
            pendingList.removeAll(currentList);

            ServiceResponseWithHeaders<TaskAddCollectionResult, TaskAddCollectionHeaders> response = this._parentBatchClient.getProtocolLayer().getTaskOperations().addCollection(jobId, currentList, options);
            if (response.getBody() != null && response.getBody().getValue() != null) {
                List<TaskAddResult> failures = new ArrayList<>();

                for (TaskAddResult result : response.getBody().getValue()) {
                    if (result.getError() != null){
                        if (result.getStatus() == TaskAddStatus.SERVERERROR) {
                            for (TaskAddParameter addParameter : taskList) {
                                if (addParameter.getId() == result.getTaskId()) {
                                    pendingList.add(addParameter);
                                    break;
                                }
                            }
                        }
                        else if (result.getStatus() == TaskAddStatus.CLIENTERROR && result.getError().getCode() != BatchErrorCodeStrings.TaskExists) {
                            failures.add(result);
                        }
                    }
                }

                if (!failures.isEmpty()) {
                    throw new CreateTasksTerminatedException("At least one task failed to be added.", failures, pendingList);
                }
            }
        }
    }

    public List<CloudTask> listTasks(String jobId) throws BatchErrorException, IOException {
        return listTasks(jobId, null, null);
    }

    public List<CloudTask> listTasks(String jobId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listTasks(jobId, detailLevel, null);
    }

    public List<CloudTask> listTasks(String jobId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskListOptions options = new TaskListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<CloudTask>, TaskListHeaders> response = this._parentBatchClient.getProtocolLayer().getTaskOperations().list(jobId, options);

        return response.getBody();
    }

    public List<SubtaskInformation> listSubtasks(String jobId, String taskId) throws BatchErrorException, IOException {
        return listSubtasks(jobId, taskId, null, null);
    }

    public List<SubtaskInformation> listSubtasks(String jobId, String taskId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listSubtasks(jobId, taskId, detailLevel, null);
    }

    public List<SubtaskInformation> listSubtasks(String jobId, String taskId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskListSubtasksOptions options = new TaskListSubtasksOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudTaskListSubtasksResult, TaskListSubtasksHeaders> response = this._parentBatchClient.getProtocolLayer().getTaskOperations().listSubtasks(jobId, taskId, options);

        if (response.getBody() != null) {
            return response.getBody().getValue();
        }
        else {
            return null;
        }
    }

    public void deleteTask(String jobId, String taskId) throws BatchErrorException, IOException {
        deleteTask(jobId, taskId, null);
    }

    public void deleteTask(String jobId, String taskId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskDeleteOptions options = new TaskDeleteOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().getTaskOperations().delete(jobId, taskId, options);
    }

    public CloudTask getTask(String jobId, String taskId) throws BatchErrorException, IOException {
        return getTask(jobId, taskId, null, null);
    }

    public CloudTask getTask(String jobId, String taskId, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return getTask(jobId, taskId, detailLevel, null);
    }

    public CloudTask getTask(String jobId, String taskId, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskGetOptions options = new TaskGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<CloudTask, TaskGetHeaders> response = this._parentBatchClient.getProtocolLayer().getTaskOperations().get(jobId, taskId, options);

        return response.getBody();
    }

    public void updateTask(String jobId, String taskId, TaskConstraints constraints) throws BatchErrorException, IOException {
        updateTask(jobId, taskId, constraints, null);
    }

    public void updateTask(String jobId, String taskId, TaskConstraints constraints, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskUpdateOptions options = new TaskUpdateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().getTaskOperations().update(jobId, taskId, constraints, options);
    }

    public void terminateTask(String jobId, String taskId) throws BatchErrorException, IOException {
        terminateTask(jobId, taskId, null);
    }

    public void terminateTask(String jobId, String taskId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        TaskTerminateOptions options = new TaskTerminateOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().getTaskOperations().terminate(jobId, taskId, options);
    }
}
