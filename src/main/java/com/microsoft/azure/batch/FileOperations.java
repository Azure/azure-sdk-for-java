/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.implementation.api.BatchErrorException;
import com.microsoft.azure.batch.protocol.implementation.api.FileDeleteFromComputeNodeOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileDeleteFromTaskOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileGetFromComputeNodeHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileGetFromComputeNodeOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileGetFromTaskHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileGetFromTaskOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileGetNodeFilePropertiesFromComputeNodeHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileGetNodeFilePropertiesFromComputeNodeOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileGetNodeFilePropertiesFromTaskHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileGetNodeFilePropertiesFromTaskOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileListFromComputeNodeHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileListFromComputeNodeOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileListFromTaskHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileListFromTaskOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.FileProperties;
import com.microsoft.azure.batch.protocol.implementation.api.NodeFileInner;
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
    public Collection<BatchClientBehavior> getCustomBehaviors() {
        return _customBehaviors;
    }

    @Override
    public void setCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        this._customBehaviors = behaviors;
    }

    public List<NodeFileInner> listFilesFromTask(String jobId, String taskId) throws BatchErrorException, IOException {
        return listFilesFromTask(jobId, taskId, null, null, null);
    }

    public List<NodeFileInner> listFilesFromTask(String jobId, String taskId, Boolean recursive, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listFilesFromTask(jobId, taskId, recursive, detailLevel, null);
    }

    public List<NodeFileInner> listFilesFromTask(String jobId, String taskId, Boolean recursive, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileListFromTaskOptionsInner options = new FileListFromTaskOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<NodeFileInner>, FileListFromTaskHeadersInner> response = this._parentBatchClient.getProtocolLayer().files().listFromTask(jobId, taskId, recursive, options);

        return response.getBody();
    }

    public List<NodeFileInner> listFilesFromComputeNode(String poolId, String nodeId) throws BatchErrorException, IOException {
        return listFilesFromComputeNode(poolId, nodeId, null, null, null);
    }

    public List<NodeFileInner> listFilesFromComputeNode(String poolId, String nodeId, Boolean recursive, DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listFilesFromComputeNode(poolId, nodeId, recursive, detailLevel, null);
    }

    public List<NodeFileInner> listFilesFromComputeNode(String poolId, String nodeId, Boolean recursive, DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileListFromComputeNodeOptionsInner options = new FileListFromComputeNodeOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<NodeFileInner>, FileListFromComputeNodeHeadersInner> response = this._parentBatchClient.getProtocolLayer().files().listFromComputeNode(poolId, nodeId, recursive, options);

        return response.getBody();
    }

    public void deleteFileFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException {
        deleteFileFromTask(jobId, taskId, fileName, null, null);
    }

    public void deleteFileFromTask(String jobId, String taskId, String fileName, Boolean recursive) throws BatchErrorException, IOException {
        deleteFileFromTask(jobId, taskId, fileName, recursive, null);
    }

    public void deleteFileFromTask(String jobId, String taskId, String fileName, Boolean recursive, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileDeleteFromTaskOptionsInner options = new FileDeleteFromTaskOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().files().deleteFromTask(jobId, taskId, fileName, recursive, options);
    }

    public void deleteFileFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException {
        deleteFileFromComputeNode(poolId, nodeId, fileName, null, null);
    }

    public void deleteFileFromComputeNode(String poolId, String nodeId, String fileName, Boolean recursive) throws BatchErrorException, IOException {
        deleteFileFromComputeNode(poolId, nodeId, fileName, recursive, null);
    }

    public void deleteFileFromComputeNode(String poolId, String nodeId, String fileName, Boolean recursive, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileDeleteFromComputeNodeOptionsInner options = new FileDeleteFromComputeNodeOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        this._parentBatchClient.getProtocolLayer().files().deleteFromComputeNode(poolId, nodeId, fileName, recursive, options);
    }

    public InputStream getFileFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException {
        return getFileFromTask(jobId, taskId, fileName, null);
    }

    public InputStream getFileFromTask(String jobId, String taskId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetFromTaskOptionsInner options = new FileGetFromTaskOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<InputStream, FileGetFromTaskHeadersInner> response = this._parentBatchClient.getProtocolLayer().files().getFromTask(jobId, taskId, fileName, options);

        return response.getBody();
    }

    public InputStream getFileFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException {
        return getFileFromComputeNode(poolId, nodeId, fileName, null);
    }

    public InputStream getFileFromComputeNode(String poolId, String nodeId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetFromComputeNodeOptionsInner options = new FileGetFromComputeNodeOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<InputStream, FileGetFromComputeNodeHeadersInner> response = this._parentBatchClient.getProtocolLayer().files().getFromComputeNode(poolId, nodeId, fileName, options);

        return response.getBody();
    }

    public FileProperties getFilePropertiesFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException {
        return getFilePropertiesFromTask(jobId, taskId, fileName, null);
    }

    public FileProperties getFilePropertiesFromTask(String jobId, String taskId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetNodeFilePropertiesFromTaskOptionsInner options = new FileGetNodeFilePropertiesFromTaskOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromTaskHeadersInner> response = this._parentBatchClient.getProtocolLayer().files().getNodeFilePropertiesFromTask(jobId, taskId, fileName, options);

        FileProperties properties = new FileProperties();
        properties.setContentLength(response.getHeaders().contentLength());
        properties.setContentType(response.getHeaders().contentType());
        properties.setCreationTime(response.getHeaders().ocpCreationTime());
        properties.setLastModified(response.getHeaders().lastModified());
        properties.setFileMode(response.getHeaders().ocpBatchFileMode());
        return properties;
    }

    public FileProperties getFilePropertiesFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException {
        return getFilePropertiesFromComputeNode(poolId, nodeId, fileName, null);
    }

    public FileProperties getFilePropertiesFromComputeNode(String poolId, String nodeId, String fileName, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        FileGetNodeFilePropertiesFromComputeNodeOptionsInner options = new FileGetNodeFilePropertiesFromComputeNodeOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromComputeNodeHeadersInner> response = this._parentBatchClient.getProtocolLayer().files().getNodeFilePropertiesFromComputeNode(poolId, nodeId, fileName, options);

        FileProperties properties = new FileProperties();
        properties.setContentLength(response.getHeaders().contentLength());
        properties.setContentType(response.getHeaders().contentType());
        properties.setCreationTime(response.getHeaders().ocpCreationTime());
        properties.setLastModified(response.getHeaders().lastModified());
        properties.setFileMode(response.getHeaders().ocpBatchFileMode());
        return properties;
    }

}

