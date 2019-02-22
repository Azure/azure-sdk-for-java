// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.FileDeleteFromComputeNodeOptions;
import com.microsoft.azure.batch.protocol.models.FileDeleteFromTaskOptions;
import com.microsoft.azure.batch.protocol.models.FileGetFromComputeNodeOptions;
import com.microsoft.azure.batch.protocol.models.FileGetFromTaskOptions;
import com.microsoft.azure.batch.protocol.models.FileGetPropertiesFromComputeNodeHeaders;
import com.microsoft.azure.batch.protocol.models.FileGetPropertiesFromComputeNodeOptions;
import com.microsoft.azure.batch.protocol.models.FileGetPropertiesFromTaskHeaders;
import com.microsoft.azure.batch.protocol.models.FileGetPropertiesFromTaskOptions;
import com.microsoft.azure.batch.protocol.models.FileListFromComputeNodeOptions;
import com.microsoft.azure.batch.protocol.models.FileListFromTaskOptions;
import com.microsoft.azure.batch.protocol.models.FileProperties;
import com.microsoft.azure.batch.protocol.models.NodeFile;
import com.microsoft.rest.ServiceResponseWithHeaders;

import java.io.*;
import java.util.Collection;
import java.util.List;

/**
 * Performs file-related operations on an Azure Batch account.
 */
public class FileOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private final BatchClient _parentBatchClient;

    FileOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    /**
     * Gets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @return A collection of {@link BatchClientBehavior} instances.
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return _customBehaviors;
    }

    /**
     * Sets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @param behaviors The collection of {@link BatchClientBehavior} instances.
     * @return The current instance.
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
        return this;
    }

    /**
     * Lists the files in the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job.
     * @param taskId The ID of the task.
     * @return A list of {@link NodeFile} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<NodeFile> listFilesFromTask(String jobId, String taskId) throws BatchErrorException, IOException {
        return listFilesFromTask(jobId, taskId, null, null, null);
    }

    /**
     * Lists the files in the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job.
     * @param taskId The ID of the task.
     * @param recursive If true, performs a recursive list of all files of the task. If false or null, returns only the files in the root task directory.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A list of {@link NodeFile} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<NodeFile> listFilesFromTask(String jobId, String taskId, Boolean recursive, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listFilesFromTask(jobId, taskId, recursive, detailLevel, null);
    }

    /**
     * Lists the files in the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job.
     * @param taskId The ID of the task.
     * @param recursive If true, performs a recursive list of all files of the task. If false or null, returns only the files in the root task directory.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link NodeFile} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<NodeFile> listFilesFromTask(String jobId, String taskId, Boolean recursive, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileListFromTaskOptions options = new FileListFromTaskOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this._parentBatchClient.protocolLayer().files().listFromTask(jobId, taskId, recursive, options);
    }

    /**
     * Lists files on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @return A list of {@link NodeFile} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<NodeFile> listFilesFromComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        return listFilesFromComputeNode(poolId, nodeId, null, null, null);
    }

    /**
     * Lists files on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param recursive If true, recursively lists all files on the compute node. If false or null, lists only the files in the compute node root directory.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A list of {@link NodeFile} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<NodeFile> listFilesFromComputeNode(String poolId, String nodeId, Boolean recursive, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listFilesFromComputeNode(poolId, nodeId, recursive, detailLevel, null);
    }

    /**
     * Lists files on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param recursive If true, recursively lists all files on the compute node. If false or null, lists only the files in the compute node root directory.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link NodeFile} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<NodeFile> listFilesFromComputeNode(String poolId, String nodeId, Boolean recursive, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileListFromComputeNodeOptions options = new FileListFromComputeNodeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this._parentBatchClient.protocolLayer().files().listFromComputeNode(poolId, nodeId, recursive, options);
    }

    /**
     * Deletes the specified file from the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file to delete.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteFileFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException {
        deleteFileFromTask(jobId, taskId, fileName, null, null);
    }

    /**
     * Deletes the specified file from the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file to delete.
     * @param recursive If the file-path parameter represents a directory instead of a file, you can set the recursive parameter to true to delete the directory and all of the files and subdirectories in it. If recursive is false or null, then the directory must be empty or deletion will fail.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteFileFromTask(String jobId, String taskId, String fileName, Boolean recursive) throws BatchErrorException, IOException {
        deleteFileFromTask(jobId, taskId, fileName, recursive, null);
    }

    /**
     * Deletes the specified file from the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file to delete.
     * @param recursive If the file-path parameter represents a directory instead of a file, you can set the recursive parameter to true to delete the directory and all of the files and subdirectories in it. If recursive is false or null, then the directory must be empty or deletion will fail.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteFileFromTask(String jobId, String taskId, String fileName, Boolean recursive, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileDeleteFromTaskOptions options = new FileDeleteFromTaskOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().files().deleteFromTask(jobId, taskId, fileName, recursive, options);
    }

    /**
     * Deletes the specified file from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param fileName The name of the file to delete.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteFileFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException {
        deleteFileFromComputeNode(poolId, nodeId, fileName, null, null);
    }

    /**
     * Deletes the specified file from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param fileName The name of the file to delete.
     * @param recursive If the file-path parameter represents a directory instead of a file, you can set the recursive parameter to true to delete the directory and all of the files and subdirectories in it. If recursive is false or null, then the directory must be empty or deletion will fail.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteFileFromComputeNode(String poolId, String nodeId, String fileName, Boolean recursive) throws BatchErrorException, IOException {
        deleteFileFromComputeNode(poolId, nodeId, fileName, recursive, null);
    }

    /**
     * Deletes the specified file from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param fileName The name of the file to delete.
     * @param recursive If the file-path parameter represents a directory instead of a file, you can set the recursive parameter to true to delete the directory and all of the files and subdirectories in it. If recursive is false or null, then the directory must be empty or deletion will fail.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void deleteFileFromComputeNode(String poolId, String nodeId, String fileName, Boolean recursive, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileDeleteFromComputeNodeOptions options = new FileDeleteFromComputeNodeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().files().deleteFromComputeNode(poolId, nodeId, fileName, recursive, options);
    }

    /**
     * Downloads the specified file from the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file to download.
     * @param outputStream A stream into which the file contents will be written.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void getFileFromTask(String jobId, String taskId, String fileName, OutputStream outputStream) throws BatchErrorException, IOException {
        getFileFromTask(jobId, taskId, fileName, null, outputStream);
    }

    /**
     * Downloads the specified file from the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file to download.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @param outputStream A stream into which the file contents will be written.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void getFileFromTask(String jobId, String taskId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors, OutputStream outputStream) throws BatchErrorException, IOException {
        FileGetFromTaskOptions options = new FileGetFromTaskOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().files().getFromTask(jobId, taskId, fileName, options, outputStream);
    }

    /**
     * Downloads the specified file from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param fileName The name of the file to download.
     * @param outputStream A stream into which the file contents will be written.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void getFileFromComputeNode(String poolId, String nodeId, String fileName, OutputStream outputStream) throws BatchErrorException, IOException {
        getFileFromComputeNode(poolId, nodeId, fileName, null, outputStream);
    }

    /**
     * Downloads the specified file from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param fileName The name of the file to download.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @param outputStream A stream into which the file contents will be written.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public void getFileFromComputeNode(String poolId, String nodeId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors, OutputStream outputStream) throws BatchErrorException, IOException {
        FileGetFromComputeNodeOptions options = new FileGetFromComputeNodeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().files().getFromComputeNode(poolId, nodeId, fileName, options, outputStream);
    }

    /**
     * Gets information about a file from the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file to retrieve.
     * @return A {@link FileProperties} instance containing information about the file.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public FileProperties getFilePropertiesFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException {
        return getFilePropertiesFromTask(jobId, taskId, fileName, null);
    }

    /**
     * Gets information about a file from the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file to retrieve.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link FileProperties} instance containing information about the file.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public FileProperties getFilePropertiesFromTask(String jobId, String taskId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetPropertiesFromTaskOptions options = new FileGetPropertiesFromTaskOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Void, FileGetPropertiesFromTaskHeaders> response = this._parentBatchClient.protocolLayer().files().
                getPropertiesFromTaskWithServiceResponseAsync(jobId, taskId, fileName, options).toBlocking().single();

        return new FileProperties()
                .withContentLength(response.headers().contentLength())
                .withContentType(response.headers().contentType())
                .withCreationTime(response.headers().ocpCreationTime())
                .withLastModified(response.headers().lastModified())
                .withFileMode(response.headers().ocpBatchFileMode());
    }

    /**
     * Gets information about a file on a compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node.
     * @param fileName The name of the file to retrieve.
     * @return A {@link FileProperties} instance containing information about the file.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public FileProperties getFilePropertiesFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException {
        return getFilePropertiesFromComputeNode(poolId, nodeId, fileName, null);
    }

    /**
     * Gets information about a file on a compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node.
     * @param fileName The name of the file to retrieve.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A {@link FileProperties} instance containing information about the file.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public FileProperties getFilePropertiesFromComputeNode(String poolId, String nodeId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetPropertiesFromComputeNodeOptions options = new FileGetPropertiesFromComputeNodeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Void, FileGetPropertiesFromComputeNodeHeaders> response = this._parentBatchClient.protocolLayer().files().
                getPropertiesFromComputeNodeWithServiceResponseAsync(poolId, nodeId, fileName, options).toBlocking().single();

        return new FileProperties()
                .withContentLength(response.headers().contentLength())
                .withContentType(response.headers().contentType())
                .withCreationTime(response.headers().ocpCreationTime())
                .withLastModified(response.headers().lastModified())
                .withFileMode(response.headers().ocpBatchFileMode());
    }
}
