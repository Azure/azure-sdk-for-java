package com.microsoft.azure.services.blob;

import java.io.InputStream;
import java.util.HashMap;

//TODO: ServiceException annotation and handling in implementation
public interface BlobService {
    ServiceProperties getServiceProperties();

    void setServiceProperties(ServiceProperties serviceProperties);

    ListContainersResult listContainers();

    ListContainersResult listContainers(ListContainersOptions options);

    void createContainer(String container);

    void createContainer(String container, CreateContainerOptions options);

    void deleteContainer(String container);

    ContainerProperties getContainerProperties(String container);

    ContainerProperties getContainerMetadata(String container);

    ContainerACL getContainerACL(String container);

    void setContainerACL(String container, ContainerACL acl);

    void setContainerMetadata(String container, HashMap<String, String> metadata);

    ListBlobsResult listBlobs(String container);

    ListBlobsResult listBlobs(String container, ListBlobsOptions options);

    void createPageBlob(String container, String blob, int length);

    void createPageBlob(String container, String blob, int length, CreateBlobOptions options);

    void createBlockBlob(String container, String blob, InputStream content);

    void createBlockBlob(String container, String blob, InputStream content, CreateBlobOptions options);

    CreateBlobPagesResult clearBlobPages(String container, String blob, long rangeStart, long rangeEnd);

    CreateBlobPagesResult clearBlobPages(String container, String blob, long rangeStart, long rangeEnd, CreateBlobPagesOptions options);

    CreateBlobPagesResult createBlobPages(String container, String blob, long rangeStart, long rangeEnd, long length, InputStream contentStream);

    CreateBlobPagesResult createBlobPages(String container, String blob, long rangeStart, long rangeEnd, long length, InputStream contentStream,
            CreateBlobPagesOptions options);

    void createBlobBlock(String container, String blob, String blockId, InputStream contentStream);

    void createBlobBlock(String container, String blob, String blockId, InputStream contentStream, CreateBlobBlockOptions options);

    void commitBlobBlocks(String container, String blob, BlockList blockList);

    void commitBlobBlocks(String container, String blob, BlockList blockList, CommitBlobBlocksOptions options);

    ListBlobBlocksResult listBlobBlocks(String container, String blob);

    ListBlobBlocksResult listBlobBlocks(String container, String blob, ListBlobBlocksOptions options);

    BlobProperties getBlobProperties(String container, String blob);

    BlobProperties getBlobProperties(String container, String blob, GetBlobPropertiesOptions options);

    ListBlobRegionsResult listBlobRegions(String container, String blob);

    ListBlobRegionsResult listBlobRegions(String container, String blob, ListBlobRegionsOptions options);

    SetBlobPropertiesResult setBlobProperties(String container, String blob, SetBlobPropertiesOptions options);

    SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata);

    SetBlobMetadataResult setBlobMetadata(String container, String blob, HashMap<String, String> metadata, SetBlobMetadataOptions options);

    Blob getBlob(String container, String blob);

    Blob getBlob(String container, String blob, GetBlobOptions options);

    void deleteBlob(String container, String blob);

    void deleteBlob(String container, String blob, DeleteBlobOptions options);

    BlobSnapshot createBlobSnapshot(String container, String blob);

    BlobSnapshot createBlobSnapshot(String container, String blob, CreateBlobSnapshotOptions options);

    void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob);

    void copyBlob(String destinationContainer, String destinationBlob, String sourceContainer, String sourceBlob, CopyBlobOptions options);

    String acquireLease(String container, String blob);

    String renewLease(String container, String blob, String leaseId);

    void releaseLease(String container, String blob, String leaseId);

    void breakLease(String container, String blob, String leaseId);

}
