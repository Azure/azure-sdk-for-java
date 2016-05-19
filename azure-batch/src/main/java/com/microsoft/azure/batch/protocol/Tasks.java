/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.CloudTask;
import com.microsoft.azure.batch.protocol.models.CloudTaskListSubtasksResult;
import com.microsoft.azure.batch.protocol.models.PageImpl;
import com.microsoft.azure.batch.protocol.models.TaskAddCollectionHeaders;
import com.microsoft.azure.batch.protocol.models.TaskAddCollectionOptions;
import com.microsoft.azure.batch.protocol.models.TaskAddCollectionResult;
import com.microsoft.azure.batch.protocol.models.TaskAddHeaders;
import com.microsoft.azure.batch.protocol.models.TaskAddOptions;
import com.microsoft.azure.batch.protocol.models.TaskAddParameter;
import com.microsoft.azure.batch.protocol.models.TaskConstraints;
import com.microsoft.azure.batch.protocol.models.TaskDeleteHeaders;
import com.microsoft.azure.batch.protocol.models.TaskDeleteOptions;
import com.microsoft.azure.batch.protocol.models.TaskGetHeaders;
import com.microsoft.azure.batch.protocol.models.TaskGetOptions;
import com.microsoft.azure.batch.protocol.models.TaskListHeaders;
import com.microsoft.azure.batch.protocol.models.TaskListNextOptions;
import com.microsoft.azure.batch.protocol.models.TaskListOptions;
import com.microsoft.azure.batch.protocol.models.TaskListSubtasksHeaders;
import com.microsoft.azure.batch.protocol.models.TaskListSubtasksOptions;
import com.microsoft.azure.batch.protocol.models.TaskTerminateHeaders;
import com.microsoft.azure.batch.protocol.models.TaskTerminateOptions;
import com.microsoft.azure.batch.protocol.models.TaskUpdateHeaders;
import com.microsoft.azure.batch.protocol.models.TaskUpdateOptions;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import java.io.IOException;
import java.util.List;

/**
 * An instance of this class provides access to all the operations defined
 * in Tasks.
 */
public interface Tasks {
    /**
     * Adds a task to the specified job.
     *
     * @param jobId The id of the job to which the task is to be added.
     * @param task The task to be added.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, TaskAddHeaders> add(String jobId, TaskAddParameter task) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Adds a task to the specified job.
     *
     * @param jobId The id of the job to which the task is to be added.
     * @param task The task to be added.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall addAsync(String jobId, TaskAddParameter task, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Adds a task to the specified job.
     *
     * @param jobId The id of the job to which the task is to be added.
     * @param task The task to be added.
     * @param taskAddOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, TaskAddHeaders> add(String jobId, TaskAddParameter task, TaskAddOptions taskAddOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Adds a task to the specified job.
     *
     * @param jobId The id of the job to which the task is to be added.
     * @param task The task to be added.
     * @param taskAddOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall addAsync(String jobId, TaskAddParameter task, TaskAddOptions taskAddOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param jobId The id of the job.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudTask&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<CloudTask>, TaskListHeaders> list(final String jobId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param jobId The id of the job.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final String jobId, final ListOperationCallback<CloudTask> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param jobId The id of the job.
     * @param taskListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudTask&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<CloudTask>, TaskListHeaders> list(final String jobId, final TaskListOptions taskListOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param jobId The id of the job.
     * @param taskListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final String jobId, final TaskListOptions taskListOptions, final ListOperationCallback<CloudTask> serviceCallback) throws IllegalArgumentException;

    /**
     * Adds a collection of tasks to the specified job.
     *
     * @param jobId The id of the job to which the task collection is to be added.
     * @param value The collection of tasks to add.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TaskAddCollectionResult object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<TaskAddCollectionResult, TaskAddCollectionHeaders> addCollection(String jobId, List<TaskAddParameter> value) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Adds a collection of tasks to the specified job.
     *
     * @param jobId The id of the job to which the task collection is to be added.
     * @param value The collection of tasks to add.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall addCollectionAsync(String jobId, List<TaskAddParameter> value, final ServiceCallback<TaskAddCollectionResult> serviceCallback) throws IllegalArgumentException;
    /**
     * Adds a collection of tasks to the specified job.
     *
     * @param jobId The id of the job to which the task collection is to be added.
     * @param value The collection of tasks to add.
     * @param taskAddCollectionOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TaskAddCollectionResult object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<TaskAddCollectionResult, TaskAddCollectionHeaders> addCollection(String jobId, List<TaskAddParameter> value, TaskAddCollectionOptions taskAddCollectionOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Adds a collection of tasks to the specified job.
     *
     * @param jobId The id of the job to which the task collection is to be added.
     * @param value The collection of tasks to add.
     * @param taskAddCollectionOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall addCollectionAsync(String jobId, List<TaskAddParameter> value, TaskAddCollectionOptions taskAddCollectionOptions, final ServiceCallback<TaskAddCollectionResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Deletes a task from the specified job.
     *
     * @param jobId The id of the job from which to delete the task.
     * @param taskId The id of the task to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, TaskDeleteHeaders> delete(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes a task from the specified job.
     *
     * @param jobId The id of the job from which to delete the task.
     * @param taskId The id of the task to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String jobId, String taskId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Deletes a task from the specified job.
     *
     * @param jobId The id of the job from which to delete the task.
     * @param taskId The id of the task to delete.
     * @param taskDeleteOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, TaskDeleteHeaders> delete(String jobId, String taskId, TaskDeleteOptions taskDeleteOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes a task from the specified job.
     *
     * @param jobId The id of the job from which to delete the task.
     * @param taskId The id of the task to delete.
     * @param taskDeleteOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String jobId, String taskId, TaskDeleteOptions taskDeleteOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets information about the specified task.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task to get information about.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudTask object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<CloudTask, TaskGetHeaders> get(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets information about the specified task.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task to get information about.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String jobId, String taskId, final ServiceCallback<CloudTask> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets information about the specified task.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task to get information about.
     * @param taskGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudTask object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<CloudTask, TaskGetHeaders> get(String jobId, String taskId, TaskGetOptions taskGetOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets information about the specified task.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task to get information about.
     * @param taskGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String jobId, String taskId, TaskGetOptions taskGetOptions, final ServiceCallback<CloudTask> serviceCallback) throws IllegalArgumentException;

    /**
     * Updates the properties of the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to update.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, TaskUpdateHeaders> update(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the properties of the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to update.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall updateAsync(String jobId, String taskId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Updates the properties of the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to update.
     * @param constraints Constraints that apply to this task. If omitted, the task is given the default constraints.
     * @param taskUpdateOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, TaskUpdateHeaders> update(String jobId, String taskId, TaskConstraints constraints, TaskUpdateOptions taskUpdateOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the properties of the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to update.
     * @param constraints Constraints that apply to this task. If omitted, the task is given the default constraints.
     * @param taskUpdateOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall updateAsync(String jobId, String taskId, TaskConstraints constraints, TaskUpdateOptions taskUpdateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all of the subtasks that are associated with the specified multi-instance task.
     *
     * @param jobId The id of the job.
     * @param taskId The id of the task.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudTaskListSubtasksResult object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<CloudTaskListSubtasksResult, TaskListSubtasksHeaders> listSubtasks(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the subtasks that are associated with the specified multi-instance task.
     *
     * @param jobId The id of the job.
     * @param taskId The id of the task.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listSubtasksAsync(String jobId, String taskId, final ServiceCallback<CloudTaskListSubtasksResult> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the subtasks that are associated with the specified multi-instance task.
     *
     * @param jobId The id of the job.
     * @param taskId The id of the task.
     * @param taskListSubtasksOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudTaskListSubtasksResult object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<CloudTaskListSubtasksResult, TaskListSubtasksHeaders> listSubtasks(String jobId, String taskId, TaskListSubtasksOptions taskListSubtasksOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the subtasks that are associated with the specified multi-instance task.
     *
     * @param jobId The id of the job.
     * @param taskId The id of the task.
     * @param taskListSubtasksOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listSubtasksAsync(String jobId, String taskId, TaskListSubtasksOptions taskListSubtasksOptions, final ServiceCallback<CloudTaskListSubtasksResult> serviceCallback) throws IllegalArgumentException;

    /**
     * Terminates the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to terminate.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, TaskTerminateHeaders> terminate(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Terminates the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to terminate.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall terminateAsync(String jobId, String taskId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Terminates the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to terminate.
     * @param taskTerminateOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, TaskTerminateHeaders> terminate(String jobId, String taskId, TaskTerminateOptions taskTerminateOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Terminates the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to terminate.
     * @param taskTerminateOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall terminateAsync(String jobId, String taskId, TaskTerminateOptions taskTerminateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudTask&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<CloudTask> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param taskListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudTask&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> listNext(final String nextPageLink, final TaskListNextOptions taskListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param taskListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final TaskListNextOptions taskListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<CloudTask> serviceCallback) throws IllegalArgumentException;

}
