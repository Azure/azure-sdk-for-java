package com.microsoft.azure.services.blob;

import java.io.InputStream;
import java.util.HashMap;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.http.ServiceFilter;

public interface BlobService {
    BlobService withFilter(ServiceFilter filter);

    ServiceProperties getServiceProperties() throws ServiceException;

    void setServiceProperties(ServiceProperties serviceProperties) throws ServiceException;

    ListContainersResult listContainers() throws ServiceException;

    ListContainersResult listContainers(ListContainersOptions options) throws ServiceException;

    //TODO: Should we use "create" or "put"?
    void createContainer(String container) throws ServiceException;

    void createContainer(String container, CreateContainerOptions options) throws ServiceException;

    // TODO: Should this have a "DeleteContainerResult" class
    void deleteContainer(String container) throws ServiceException;

    void deleteContainer(String container, DeleteContainerOptions options) throws ServiceException;

    ContainerProperties getContainerProperties(String container) throws ServiceException;

    ContainerProperties getContainerMetadata(String container) throws ServiceException;

    ContainerACL getContainerACL(String container) throws ServiceException;

    void setContainerACL(String container, ContainerACL acl) throws ServiceException;

    void setContainerMetadata(String container, HashMap<String, String> metadata) throws ServiceException;

    void setContainerMetadata(String container, HashMap<String, String> metadata, SetContainerMetadataOptions options) throws ServiceException;

    ListBlobsResult listBlobs(String container) throws ServiceException;

    ListBlobsResult listBlobs(String container, ListBlobsOptions options) throws ServiceException;

    //TODO: Should we use "create" or "put"?
    void createPageBlob(String container, String blob, int length) throws ServiceException;

    void createPageBlob(String container, String blob, int length, CreateBlobOptions options) throws ServiceException;

    void createBlockBlob(String container, String blob, InputStream contentStream) throws ServiceException;

    void createBlockBlob(String container, String blob, InputStream contentStream, CreateBlobOptions options) throws ServiceException;

    //TODO: Should we use "createPageBlobPages"?
    CreateBlobPagesResult clearBlobPages(String container, String blob, long rangeStart, long rangeEnd) throws ServiceException;

    CreateBlobPagesResult clearBlobPages(String container, String blob, long rangeStart, long rangeEnd, CreateBlobPagesOptions options) throws ServiceException;

    //TODO: Should we use "updatePageBlobPages"?
    CreateBlobPagesResult createBlobPages(String container, String blob, long rangeStart, long rangeEnd, long length, InputStream contentStream)
            throws ServiceException;

    CreateBlobPagesResult createBlobPages(String container, String blob, long rangeStart, long rangeEnd, long length, InputStream contentStream,
            CreateBlobPagesOptions options) throws ServiceException;

    //TODO: createBlockBlobBlock?
    void createBlobBlock(String container, String blob, String blockId, InputStream contentStream) throws ServiceException;

    void createBlobBlock(String container, String blob, String blockId, InputStream contentStream, CreateBlobBlockOptions options) throws ServiceException;

    //TODO: commitBlockBlobBlocks?
    void commitBlobBlocks(String container, String blob, BlockList blockList) throws ServiceException;

    void commitBlobBlocks(String container, String blob, BlockList blockList, CommitBlobBlocksOptions options) throws ServiceException;

    ListBlobBlocksResult listBlobBlocks(String container, String blob) throws ServiceException;

    ListBlobBlocksResult listBlobBlocks(String container, String blob, ListBlobBlocksOptions options) throws ServiceException;

    BlobProperties getBlobProperties(String container, String blob) throws ServiceException;

    BlobProperties getBlobProperties(String container, String blob, GetBlobPropertiesOptions options) throws ServiceException;

    GetBlobMetadataResult getBlobMetadata(String container, String blob) throws ServiceException;

    GetBlobMetadataResult getBlobMetadata(String container, String blob, GetBlobMetadataOptions options) throws ServiceException;

    ListBlobRegionsResult listBlobRegions(String container, String blob) throws ServiceException;

    ListBlobRegionsResult listBlobRegions(String container, String blob, ListBlobRegionsOptions options) throws ServiceException;

    SetBlobPropertiesResult setBlobProperties(String container, String blob, SetBlobPropertiesOptions options) throws ServiceException;

    SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata) throws ServiceException;

    SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata, SetBlobMetadataOptions options)
            throws ServiceException;

    Blob getBlob(String container, String blob) throws ServiceException;

    Blob getBlob(String container, String blob, GetBlobOptions options) throws ServiceException;

    void deleteBlob(String container, String blob) throws ServiceException;

    void deleteBlob(String container, String blob, DeleteBlobOptions options) throws ServiceException;

    BlobSnapshot createBlobSnapshot(String container, String blob) throws ServiceException;

    BlobSnapshot createBlobSnapshot(String container, String blob, CreateBlobSnapshotOptions options) throws ServiceException;

    void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob) throws ServiceException;

    void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob, CopyBlobOptions options)
            throws ServiceException;

    String acquireLease(String container, String blob) throws ServiceException;

    String acquireLease(String container, String blob, AcquireLeaseOptions options) throws ServiceException;

    String renewLease(String container, String blob, String leaseId) throws ServiceException;

    void releaseLease(String container, String blob, String leaseId) throws ServiceException;

    void breakLease(String container, String blob, String leaseId) throws ServiceException;

}
