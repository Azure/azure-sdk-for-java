/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.FileDeleteFromComputeNodeHeaders;
import com.microsoft.azure.batch.protocol.models.FileDeleteFromComputeNodeOptions;
import com.microsoft.azure.batch.protocol.models.FileDeleteFromTaskHeaders;
import com.microsoft.azure.batch.protocol.models.FileDeleteFromTaskOptions;
import com.microsoft.azure.batch.protocol.models.FileGetFromComputeNodeHeaders;
import com.microsoft.azure.batch.protocol.models.FileGetFromComputeNodeOptions;
import com.microsoft.azure.batch.protocol.models.FileGetFromTaskHeaders;
import com.microsoft.azure.batch.protocol.models.FileGetFromTaskOptions;
import com.microsoft.azure.batch.protocol.models.FileGetNodeFilePropertiesFromComputeNodeHeaders;
import com.microsoft.azure.batch.protocol.models.FileGetNodeFilePropertiesFromComputeNodeOptions;
import com.microsoft.azure.batch.protocol.models.FileGetNodeFilePropertiesFromTaskHeaders;
import com.microsoft.azure.batch.protocol.models.FileGetNodeFilePropertiesFromTaskOptions;
import com.microsoft.azure.batch.protocol.models.FileListFromComputeNodeHeaders;
import com.microsoft.azure.batch.protocol.models.FileListFromComputeNodeNextOptions;
import com.microsoft.azure.batch.protocol.models.FileListFromComputeNodeOptions;
import com.microsoft.azure.batch.protocol.models.FileListFromTaskHeaders;
import com.microsoft.azure.batch.protocol.models.FileListFromTaskNextOptions;
import com.microsoft.azure.batch.protocol.models.FileListFromTaskOptions;
import com.microsoft.azure.batch.protocol.models.NodeFile;
import com.microsoft.azure.batch.protocol.models.PageImpl;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import java.io.InputStream;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in Files.
 */
public interface Files {
    /**
     * Deletes the specified task file from the compute node where the task ran.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to delete.
     * @param fileName The path to the task file that you want to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, FileDeleteFromTaskHeaders> deleteFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes the specified task file from the compute node where the task ran.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to delete.
     * @param fileName The path to the task file that you want to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteFromTaskAsync(String jobId, String taskId, String fileName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Deletes the specified task file from the compute node where the task ran.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to delete.
     * @param fileName The path to the task file that you want to delete.
     * @param recursive Whether to delete children of a directory. If the fileName parameter represents a directory instead of a file, you can set Recursive to true to delete the directory and all of the files and subdirectories in it. If Recursive is false then the directory must be empty or deletion will fail.
     * @param fileDeleteFromTaskOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, FileDeleteFromTaskHeaders> deleteFromTask(String jobId, String taskId, String fileName, Boolean recursive, FileDeleteFromTaskOptions fileDeleteFromTaskOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes the specified task file from the compute node where the task ran.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to delete.
     * @param fileName The path to the task file that you want to delete.
     * @param recursive Whether to delete children of a directory. If the fileName parameter represents a directory instead of a file, you can set Recursive to true to delete the directory and all of the files and subdirectories in it. If Recursive is false then the directory must be empty or deletion will fail.
     * @param fileDeleteFromTaskOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteFromTaskAsync(String jobId, String taskId, String fileName, Boolean recursive, FileDeleteFromTaskOptions fileDeleteFromTaskOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Returns the content of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to retrieve.
     * @param fileName The path to the task file that you want to get the content of.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<InputStream, FileGetFromTaskHeaders> getFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Returns the content of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to retrieve.
     * @param fileName The path to the task file that you want to get the content of.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getFromTaskAsync(String jobId, String taskId, String fileName, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException;
    /**
     * Returns the content of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to retrieve.
     * @param fileName The path to the task file that you want to get the content of.
     * @param fileGetFromTaskOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<InputStream, FileGetFromTaskHeaders> getFromTask(String jobId, String taskId, String fileName, FileGetFromTaskOptions fileGetFromTaskOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Returns the content of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to retrieve.
     * @param fileName The path to the task file that you want to get the content of.
     * @param fileGetFromTaskOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getFromTaskAsync(String jobId, String taskId, String fileName, FileGetFromTaskOptions fileGetFromTaskOptions, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets the properties of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to get the properties of.
     * @param fileName The path to the task file that you want to get the properties of.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromTaskHeaders> getNodeFilePropertiesFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets the properties of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to get the properties of.
     * @param fileName The path to the task file that you want to get the properties of.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getNodeFilePropertiesFromTaskAsync(String jobId, String taskId, String fileName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets the properties of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to get the properties of.
     * @param fileName The path to the task file that you want to get the properties of.
     * @param fileGetNodeFilePropertiesFromTaskOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromTaskHeaders> getNodeFilePropertiesFromTask(String jobId, String taskId, String fileName, FileGetNodeFilePropertiesFromTaskOptions fileGetNodeFilePropertiesFromTaskOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets the properties of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to get the properties of.
     * @param fileName The path to the task file that you want to get the properties of.
     * @param fileGetNodeFilePropertiesFromTaskOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getNodeFilePropertiesFromTaskAsync(String jobId, String taskId, String fileName, FileGetNodeFilePropertiesFromTaskOptions fileGetNodeFilePropertiesFromTaskOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Deletes the specified task file from the compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node from which you want to delete the file.
     * @param fileName The path to the file that you want to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, FileDeleteFromComputeNodeHeaders> deleteFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes the specified task file from the compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node from which you want to delete the file.
     * @param fileName The path to the file that you want to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteFromComputeNodeAsync(String poolId, String nodeId, String fileName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Deletes the specified task file from the compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node from which you want to delete the file.
     * @param fileName The path to the file that you want to delete.
     * @param recursive Whether to delete children of a directory. If the fileName parameter represents a directory instead of a file, you can set Recursive to true to delete the directory and all of the files and subdirectories in it. If Recursive is false then the directory must be empty or deletion will fail.
     * @param fileDeleteFromComputeNodeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, FileDeleteFromComputeNodeHeaders> deleteFromComputeNode(String poolId, String nodeId, String fileName, Boolean recursive, FileDeleteFromComputeNodeOptions fileDeleteFromComputeNodeOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes the specified task file from the compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node from which you want to delete the file.
     * @param fileName The path to the file that you want to delete.
     * @param recursive Whether to delete children of a directory. If the fileName parameter represents a directory instead of a file, you can set Recursive to true to delete the directory and all of the files and subdirectories in it. If Recursive is false then the directory must be empty or deletion will fail.
     * @param fileDeleteFromComputeNodeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteFromComputeNodeAsync(String poolId, String nodeId, String fileName, Boolean recursive, FileDeleteFromComputeNodeOptions fileDeleteFromComputeNodeOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Returns the content of the specified task file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the task file that you want to get the content of.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<InputStream, FileGetFromComputeNodeHeaders> getFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Returns the content of the specified task file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the task file that you want to get the content of.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getFromComputeNodeAsync(String poolId, String nodeId, String fileName, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException;
    /**
     * Returns the content of the specified task file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the task file that you want to get the content of.
     * @param fileGetFromComputeNodeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<InputStream, FileGetFromComputeNodeHeaders> getFromComputeNode(String poolId, String nodeId, String fileName, FileGetFromComputeNodeOptions fileGetFromComputeNodeOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Returns the content of the specified task file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the task file that you want to get the content of.
     * @param fileGetFromComputeNodeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getFromComputeNodeAsync(String poolId, String nodeId, String fileName, FileGetFromComputeNodeOptions fileGetFromComputeNodeOptions, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets the properties of the specified compute node file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the compute node file that you want to get the properties of.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromComputeNodeHeaders> getNodeFilePropertiesFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets the properties of the specified compute node file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the compute node file that you want to get the properties of.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getNodeFilePropertiesFromComputeNodeAsync(String poolId, String nodeId, String fileName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets the properties of the specified compute node file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the compute node file that you want to get the properties of.
     * @param fileGetNodeFilePropertiesFromComputeNodeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromComputeNodeHeaders> getNodeFilePropertiesFromComputeNode(String poolId, String nodeId, String fileName, FileGetNodeFilePropertiesFromComputeNodeOptions fileGetNodeFilePropertiesFromComputeNodeOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets the properties of the specified compute node file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the compute node file that you want to get the properties of.
     * @param fileGetNodeFilePropertiesFromComputeNodeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getNodeFilePropertiesFromComputeNodeAsync(String poolId, String nodeId, String fileName, FileGetNodeFilePropertiesFromComputeNodeOptions fileGetNodeFilePropertiesFromComputeNodeOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose files you want to list.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFile&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<NodeFile>, FileListFromTaskHeaders> listFromTask(final String jobId, final String taskId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose files you want to list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listFromTaskAsync(final String jobId, final String taskId, final ListOperationCallback<NodeFile> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose files you want to list.
     * @param recursive Whether to list children of a directory.
     * @param fileListFromTaskOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFile&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<NodeFile>, FileListFromTaskHeaders> listFromTask(final String jobId, final String taskId, final Boolean recursive, final FileListFromTaskOptions fileListFromTaskOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose files you want to list.
     * @param recursive Whether to list children of a directory.
     * @param fileListFromTaskOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listFromTaskAsync(final String jobId, final String taskId, final Boolean recursive, final FileListFromTaskOptions fileListFromTaskOptions, final ListOperationCallback<NodeFile> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node whose files you want to list.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFile&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<NodeFile>, FileListFromComputeNodeHeaders> listFromComputeNode(final String poolId, final String nodeId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node whose files you want to list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listFromComputeNodeAsync(final String poolId, final String nodeId, final ListOperationCallback<NodeFile> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node whose files you want to list.
     * @param recursive Whether to list children of a directory.
     * @param fileListFromComputeNodeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFile&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<NodeFile>, FileListFromComputeNodeHeaders> listFromComputeNode(final String poolId, final String nodeId, final Boolean recursive, final FileListFromComputeNodeOptions fileListFromComputeNodeOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node whose files you want to list.
     * @param recursive Whether to list children of a directory.
     * @param fileListFromComputeNodeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listFromComputeNodeAsync(final String poolId, final String nodeId, final Boolean recursive, final FileListFromComputeNodeOptions fileListFromComputeNodeOptions, final ListOperationCallback<NodeFile> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFile&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<NodeFile>, FileListFromTaskHeaders> listFromTaskNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listFromTaskNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<NodeFile> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param fileListFromTaskNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFile&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<NodeFile>, FileListFromTaskHeaders> listFromTaskNext(final String nextPageLink, final FileListFromTaskNextOptions fileListFromTaskNextOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param fileListFromTaskNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listFromTaskNextAsync(final String nextPageLink, final FileListFromTaskNextOptions fileListFromTaskNextOptions, final ServiceCall serviceCall, final ListOperationCallback<NodeFile> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFile&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<NodeFile>, FileListFromComputeNodeHeaders> listFromComputeNodeNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listFromComputeNodeNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<NodeFile> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param fileListFromComputeNodeNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFile&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<NodeFile>, FileListFromComputeNodeHeaders> listFromComputeNodeNext(final String nextPageLink, final FileListFromComputeNodeNextOptions fileListFromComputeNodeNextOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param fileListFromComputeNodeNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listFromComputeNodeNextAsync(final String nextPageLink, final FileListFromComputeNodeNextOptions fileListFromComputeNodeNextOptions, final ServiceCall serviceCall, final ListOperationCallback<NodeFile> serviceCallback) throws IllegalArgumentException;

}
