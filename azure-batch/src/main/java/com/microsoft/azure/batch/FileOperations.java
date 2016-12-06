/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.FileDeleteFromComputeNodeOptions;
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
import com.microsoft.azure.batch.protocol.models.FileListFromComputeNodeOptions;
import com.microsoft.azure.batch.protocol.models.FileListFromTaskHeaders;
import com.microsoft.azure.batch.protocol.models.FileListFromTaskOptions;
import com.microsoft.azure.batch.protocol.models.FileProperties;
import com.microsoft.azure.batch.protocol.models.NodeFile;
import com.microsoft.rest.ServiceResponseWithHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * Performs file related operations on an Azure Batch account.
 */
public class FileOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    FileOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    /**
     * Gets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @return A list of BatchClientBehavior
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return _customBehaviors;
    }

    /**
     * Sets a list of behaviors that modify or customize requests to the Batch service.
     *
     * @param behaviors The collection of BatchClientBehavior classes
     * @return The current instance
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
        return this;
    }

    /**
     * Enumerates the files in the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job.
     * @param taskId The ID of the task.
     * @return A collection of {@link NodeFile}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<NodeFile> listFilesFromTask(String jobId, String taskId) throws BatchErrorException, IOException {
        return listFilesFromTask(jobId, taskId, null, null, null);
    }

    /**
     * Enumerates the files in the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job.
     * @param taskId The ID of the task.
     * @param recursive If true, performs a recursive list of all files of the task. If false, returns only the files in the root task directory.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A collection of {@link NodeFile}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<NodeFile> listFilesFromTask(String jobId, String taskId, Boolean recursive, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listFilesFromTask(jobId, taskId, recursive, detailLevel, null);
    }

    /**
     * Enumerates the files in the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job.
     * @param taskId The ID of the task.
     * @param recursive If true, performs a recursive list of all files of the task. If false, returns only the files in the root task directory.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link NodeFile}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<NodeFile> listFilesFromTask(String jobId, String taskId, Boolean recursive, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileListFromTaskOptions options = new FileListFromTaskOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<NodeFile>, FileListFromTaskHeaders> response = this._parentBatchClient.protocolLayer().files().listFromTask(jobId, taskId, recursive, options);

        return response.getBody();
    }

    /**
     * Enumerates files on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @return A collection of {@link NodeFile}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<NodeFile> listFilesFromComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        return listFilesFromComputeNode(poolId, nodeId, null, null, null);
    }

    /**
     * Enumerates files on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param recursive If true, recursively enumerates all files on the compute node. If false, enumerates only the files in the compute node root directory.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A collection of {@link NodeFile}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<NodeFile> listFilesFromComputeNode(String poolId, String nodeId, Boolean recursive, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listFilesFromComputeNode(poolId, nodeId, recursive, detailLevel, null);
    }

    /**
     * Enumerates files on the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param recursive If true, recursively enumerates all files on the compute node. If false, enumerates only the files in the compute node root directory.
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A collection of {@link NodeFile}
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public List<NodeFile> listFilesFromComputeNode(String poolId, String nodeId, Boolean recursive, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileListFromComputeNodeOptions options = new FileListFromComputeNodeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<NodeFile>, FileListFromComputeNodeHeaders> response = this._parentBatchClient.protocolLayer().files().listFromComputeNode(poolId, nodeId, recursive, options);

        return response.getBody();
    }

    /**
     * Deletes the specified file from the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file to delete.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
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
     * @param recursive If the file-path parameter represents a directory instead of a file, you can set the optional recursive parameter to true to delete the directory and all of the files and subdirectories in it. If recursive is false then the directory must be empty or deletion will fail.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
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
     * @param recursive If the file-path parameter represents a directory instead of a file, you can set the optional recursive parameter to true to delete the directory and all of the files and subdirectories in it. If recursive is false then the directory must be empty or deletion will fail.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
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
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
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
     * @param recursive If the file-path parameter represents a directory instead of a file, you can set the optional recursive parameter to true to delete the directory and all of the files and subdirectories in it. If recursive is false then the directory must be empty or deletion will fail.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
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
     * @param recursive If the file-path parameter represents a directory instead of a file, you can set the optional recursive parameter to true to delete the directory and all of the files and subdirectories in it. If recursive is false then the directory must be empty or deletion will fail.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
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
     * @return A stream into which the file contents will be written.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public InputStream getFileFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException {
        return getFileFromTask(jobId, taskId, fileName, null);
    }

    /**
     * Downloads the specified file from the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file to download.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A stream into which the file contents will be written.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public InputStream getFileFromTask(String jobId, String taskId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetFromTaskOptions options = new FileGetFromTaskOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<InputStream, FileGetFromTaskHeaders> response = this._parentBatchClient.protocolLayer().files().getFromTask(jobId, taskId, fileName, options);

        return response.getBody();
    }

    /**
     * Downloads the specified file from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param fileName The name of the file to download.
     * @return A stream into which the file contents will be written.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public InputStream getFileFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException {
        return getFileFromComputeNode(poolId, nodeId, fileName, null);
    }

    /**
     * Downloads the specified file from the specified compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId The ID of the compute node.
     * @param fileName The name of the file to download.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A stream into which the file contents will be written.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public InputStream getFileFromComputeNode(String poolId, String nodeId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetFromComputeNodeOptions options = new FileGetFromComputeNodeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<InputStream, FileGetFromComputeNodeHeaders> response = this._parentBatchClient.protocolLayer().files().getFromComputeNode(poolId, nodeId, fileName, options);

        return response.getBody();
    }

    /**
     * Gets information about a file from the specified task's directory on its compute node.
     *
     * @param jobId The ID of the job containing the task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file to retrieve.
     * @return A {@link NodeFile} containing information about the file.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
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
     * @return A {@link NodeFile} containing information about the file.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public FileProperties getFilePropertiesFromTask(String jobId, String taskId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetNodeFilePropertiesFromTaskOptions options = new FileGetNodeFilePropertiesFromTaskOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromTaskHeaders> response = this._parentBatchClient.protocolLayer().files().getNodeFilePropertiesFromTask(jobId, taskId, fileName, options);

        return new FileProperties()
                .withContentLength(response.getHeaders().contentLength())
                .withContentType(response.getHeaders().contentType())
                .withCreationTime(response.getHeaders().ocpCreationTime())
                .withLastModified(response.getHeaders().lastModified())
                .withFileMode(response.getHeaders().ocpBatchFileMode());
    }

    /**
     * Gets information about a file on a compute node.
     *
     * @param poolId The ID of the pool that contains the compute node.
     * @param nodeId the ID of the compute node.
     * @param fileName The name of the file to retrieve.
     * @return A {@link NodeFile} containing information about the file.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
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
     * @return A {@link NodeFile} containing information about the file.
     * @throws BatchErrorException Exception thrown from REST call
     * @throws IOException Exception thrown from serialization/deserialization
     */
    public FileProperties getFilePropertiesFromComputeNode(String poolId, String nodeId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetNodeFilePropertiesFromComputeNodeOptions options = new FileGetNodeFilePropertiesFromComputeNodeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromComputeNodeHeaders> response = this._parentBatchClient.protocolLayer().files().getNodeFilePropertiesFromComputeNode(poolId, nodeId, fileName, options);

        return new FileProperties()
                .withContentLength(response.getHeaders().contentLength())
                .withContentType(response.getHeaders().contentType())
                .withCreationTime(response.getHeaders().ocpCreationTime())
                .withLastModified(response.getHeaders().lastModified())
                .withFileMode(response.getHeaders().ocpBatchFileMode());
    }

}

