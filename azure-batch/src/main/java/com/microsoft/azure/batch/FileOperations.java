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

public class FileOperations implements IInheritedBehaviors {

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    FileOperations(BatchClient batchClient, Iterable<BatchClientBehavior> inheritedBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, inheritedBehaviors);
    }

    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return _customBehaviors;
    }

    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        this._customBehaviors = behaviors;
        return this;
    }

    public List<NodeFile> listFilesFromTask(String jobId, String taskId) throws BatchErrorException, IOException {
        return listFilesFromTask(jobId, taskId, null, null, null);
    }

    public List<NodeFile> listFilesFromTask(String jobId, String taskId, Boolean recursive, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listFilesFromTask(jobId, taskId, recursive, detailLevel, null);
    }

    public List<NodeFile> listFilesFromTask(String jobId, String taskId, Boolean recursive, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileListFromTaskOptions options = new FileListFromTaskOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<NodeFile>, FileListFromTaskHeaders> response = this._parentBatchClient.protocolLayer().files().listFromTask(jobId, taskId, recursive, options);

        return response.getBody();
    }

    public List<NodeFile> listFilesFromComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        return listFilesFromComputeNode(poolId, nodeId, null, null, null);
    }

    public List<NodeFile> listFilesFromComputeNode(String poolId, String nodeId, Boolean recursive, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listFilesFromComputeNode(poolId, nodeId, recursive, detailLevel, null);
    }

    public List<NodeFile> listFilesFromComputeNode(String poolId, String nodeId, Boolean recursive, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileListFromComputeNodeOptions options = new FileListFromComputeNodeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<NodeFile>, FileListFromComputeNodeHeaders> response = this._parentBatchClient.protocolLayer().files().listFromComputeNode(poolId, nodeId, recursive, options);

        return response.getBody();
    }

    public void deleteFileFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException {
        deleteFileFromTask(jobId, taskId, fileName, null, null);
    }

    public void deleteFileFromTask(String jobId, String taskId, String fileName, Boolean recursive) throws BatchErrorException, IOException {
        deleteFileFromTask(jobId, taskId, fileName, recursive, null);
    }

    public void deleteFileFromTask(String jobId, String taskId, String fileName, Boolean recursive, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileDeleteFromTaskOptions options = new FileDeleteFromTaskOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().files().deleteFromTask(jobId, taskId, fileName, recursive, options);
    }

    public void deleteFileFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException {
        deleteFileFromComputeNode(poolId, nodeId, fileName, null, null);
    }

    public void deleteFileFromComputeNode(String poolId, String nodeId, String fileName, Boolean recursive) throws BatchErrorException, IOException {
        deleteFileFromComputeNode(poolId, nodeId, fileName, recursive, null);
    }

    public void deleteFileFromComputeNode(String poolId, String nodeId, String fileName, Boolean recursive, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileDeleteFromComputeNodeOptions options = new FileDeleteFromComputeNodeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.protocolLayer().files().deleteFromComputeNode(poolId, nodeId, fileName, recursive, options);
    }

    public InputStream getFileFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException {
        return getFileFromTask(jobId, taskId, fileName, null);
    }

    public InputStream getFileFromTask(String jobId, String taskId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetFromTaskOptions options = new FileGetFromTaskOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<InputStream, FileGetFromTaskHeaders> response = this._parentBatchClient.protocolLayer().files().getFromTask(jobId, taskId, fileName, options);

        return response.getBody();
    }

    public InputStream getFileFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException {
        return getFileFromComputeNode(poolId, nodeId, fileName, null);
    }

    public InputStream getFileFromComputeNode(String poolId, String nodeId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetFromComputeNodeOptions options = new FileGetFromComputeNodeOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<InputStream, FileGetFromComputeNodeHeaders> response = this._parentBatchClient.protocolLayer().files().getFromComputeNode(poolId, nodeId, fileName, options);

        return response.getBody();
    }

    public FileProperties getFilePropertiesFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException {
        return getFilePropertiesFromTask(jobId, taskId, fileName, null);
    }

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

    public FileProperties getFilePropertiesFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException {
        return getFilePropertiesFromComputeNode(poolId, nodeId, fileName, null);
    }

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

