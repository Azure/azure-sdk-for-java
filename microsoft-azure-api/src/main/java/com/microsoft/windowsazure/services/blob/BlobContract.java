package com.microsoft.windowsazure.services.blob;

import java.io.InputStream;
import java.util.HashMap;

import com.microsoft.windowsazure.services.blob.models.AcquireLeaseOptions;
import com.microsoft.windowsazure.services.blob.models.AcquireLeaseResult;
import com.microsoft.windowsazure.services.blob.models.BlobServiceOptions;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ContainerACL;
import com.microsoft.windowsazure.services.blob.models.CopyBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobPagesOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobPagesResult;
import com.microsoft.windowsazure.services.blob.models.CreateBlobSnapshotOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobSnapshotResult;
import com.microsoft.windowsazure.services.blob.models.CreateContainerOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteBlobOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteContainerOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobMetadataOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobMetadataResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobResult;
import com.microsoft.windowsazure.services.blob.models.GetContainerACLResult;
import com.microsoft.windowsazure.services.blob.models.GetContainerPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobRegionsOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobRegionsResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobsOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobsResult;
import com.microsoft.windowsazure.services.blob.models.ListContainersOptions;
import com.microsoft.windowsazure.services.blob.models.ListContainersResult;
import com.microsoft.windowsazure.services.blob.models.PageRange;
import com.microsoft.windowsazure.services.blob.models.ServiceProperties;
import com.microsoft.windowsazure.services.blob.models.SetBlobMetadataOptions;
import com.microsoft.windowsazure.services.blob.models.SetBlobMetadataResult;
import com.microsoft.windowsazure.services.blob.models.SetBlobPropertiesOptions;
import com.microsoft.windowsazure.services.blob.models.SetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.SetContainerMetadataOptions;
import com.microsoft.windowsazure.services.core.FilterableService;
import com.microsoft.windowsazure.services.core.ServiceException;

public interface BlobContract extends FilterableService<BlobContract> {
    GetServicePropertiesResult getServiceProperties() throws ServiceException;

    GetServicePropertiesResult getServiceProperties(BlobServiceOptions options) throws ServiceException;

    void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException;

    void setServiceProperties(ServiceProperties serviceProperties, BlobServiceOptions options) throws ServiceException;

    ListContainersResult listContainers() throws ServiceException;

    ListContainersResult listContainers(ListContainersOptions options) throws ServiceException;

    void createContainer(String container) throws ServiceException;

    void createContainer(String container, CreateContainerOptions options) throws ServiceException;

    void deleteContainer(String container) throws ServiceException;

    void deleteContainer(String container, DeleteContainerOptions options) throws ServiceException;

    GetContainerPropertiesResult getContainerProperties(String container) throws ServiceException;

    GetContainerPropertiesResult getContainerProperties(String container, BlobServiceOptions options)
            throws ServiceException;

    GetContainerPropertiesResult getContainerMetadata(String container) throws ServiceException;

    GetContainerPropertiesResult getContainerMetadata(String container, BlobServiceOptions options)
            throws ServiceException;

    GetContainerACLResult getContainerACL(String container) throws ServiceException;

    GetContainerACLResult getContainerACL(String container, BlobServiceOptions options) throws ServiceException;

    void setContainerACL(String container, ContainerACL acl) throws ServiceException;

    void setContainerACL(String container, ContainerACL acl, BlobServiceOptions options) throws ServiceException;

    void setContainerMetadata(String container, HashMap<String, String> metadata) throws ServiceException;

    void setContainerMetadata(String container, HashMap<String, String> metadata, SetContainerMetadataOptions options)
            throws ServiceException;

    ListBlobsResult listBlobs(String container) throws ServiceException;

    ListBlobsResult listBlobs(String container, ListBlobsOptions options) throws ServiceException;

    void createPageBlob(String container, String blob, int length) throws ServiceException;

    void createPageBlob(String container, String blob, int length, CreateBlobOptions options) throws ServiceException;

    void createBlockBlob(String container, String blob, InputStream contentStream) throws ServiceException;

    void createBlockBlob(String container, String blob, InputStream contentStream, CreateBlobOptions options)
            throws ServiceException;

    CreateBlobPagesResult clearBlobPages(String container, String blob, PageRange range) throws ServiceException;

    CreateBlobPagesResult clearBlobPages(String container, String blob, PageRange range, CreateBlobPagesOptions options)
            throws ServiceException;

    CreateBlobPagesResult createBlobPages(String container, String blob, PageRange range, long length,
            InputStream contentStream) throws ServiceException;

    CreateBlobPagesResult createBlobPages(String container, String blob, PageRange range, long length,
            InputStream contentStream, CreateBlobPagesOptions options) throws ServiceException;

    void createBlobBlock(String container, String blob, String blockId, InputStream contentStream)
            throws ServiceException;

    void createBlobBlock(String container, String blob, String blockId, InputStream contentStream,
            CreateBlobBlockOptions options) throws ServiceException;

    void commitBlobBlocks(String container, String blob, BlockList blockList) throws ServiceException;

    void commitBlobBlocks(String container, String blob, BlockList blockList, CommitBlobBlocksOptions options)
            throws ServiceException;

    ListBlobBlocksResult listBlobBlocks(String container, String blob) throws ServiceException;

    ListBlobBlocksResult listBlobBlocks(String container, String blob, ListBlobBlocksOptions options)
            throws ServiceException;

    GetBlobPropertiesResult getBlobProperties(String container, String blob) throws ServiceException;

    GetBlobPropertiesResult getBlobProperties(String container, String blob, GetBlobPropertiesOptions options)
            throws ServiceException;

    GetBlobMetadataResult getBlobMetadata(String container, String blob) throws ServiceException;

    GetBlobMetadataResult getBlobMetadata(String container, String blob, GetBlobMetadataOptions options)
            throws ServiceException;

    ListBlobRegionsResult listBlobRegions(String container, String blob) throws ServiceException;

    ListBlobRegionsResult listBlobRegions(String container, String blob, ListBlobRegionsOptions options)
            throws ServiceException;

    SetBlobPropertiesResult setBlobProperties(String container, String blob, SetBlobPropertiesOptions options)
            throws ServiceException;

    SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata)
            throws ServiceException;

    SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata,
            SetBlobMetadataOptions options) throws ServiceException;

    GetBlobResult getBlob(String container, String blob) throws ServiceException;

    GetBlobResult getBlob(String container, String blob, GetBlobOptions options) throws ServiceException;

    void deleteBlob(String container, String blob) throws ServiceException;

    void deleteBlob(String container, String blob, DeleteBlobOptions options) throws ServiceException;

    CreateBlobSnapshotResult createBlobSnapshot(String container, String blob) throws ServiceException;

    CreateBlobSnapshotResult createBlobSnapshot(String container, String blob, CreateBlobSnapshotOptions options)
            throws ServiceException;

    void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob)
            throws ServiceException;

    void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob,
            CopyBlobOptions options) throws ServiceException;

    AcquireLeaseResult acquireLease(String container, String blob) throws ServiceException;

    AcquireLeaseResult acquireLease(String container, String blob, AcquireLeaseOptions options) throws ServiceException;

    AcquireLeaseResult renewLease(String container, String blob, String leaseId) throws ServiceException;

    AcquireLeaseResult renewLease(String container, String blob, String leaseId, BlobServiceOptions options)
            throws ServiceException;

    void releaseLease(String container, String blob, String leaseId) throws ServiceException;

    void releaseLease(String container, String blob, String leaseId, BlobServiceOptions options)
            throws ServiceException;

    void breakLease(String container, String blob, String leaseId) throws ServiceException;

    void breakLease(String container, String blob, String leaseId, BlobServiceOptions options) throws ServiceException;
}
