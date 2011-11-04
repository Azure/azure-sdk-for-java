package com.microsoft.azure.services.blob;

import java.io.InputStream;
import java.util.HashMap;

//TODO: ServiceException annotation and handling in implementation
public interface BlobService {
    ServiceProperties getServiceProperties();

    void setServiceProperties(ServiceProperties serviceProperties);

    ListContainersResults listContainers();

    ListContainersResults listContainers(ListContainersOptions options);

    void createContainer(String container);

    void createContainer(String container, CreateContainerOptions options);

    void deleteContainer(String container);

    ContainerProperties getContainerProperties(String container);

    ContainerProperties getContainerMetadata(String container);

    ContainerACL getContainerACL(String container);

    void setContainerACL(String container, ContainerACL acl);

    void setContainerMetadata(String container, HashMap<String, String> metadata);

    ListBlobsResults listBlobs(String container);

    ListBlobsResults listBlobs(String container, ListBlobsOptions options);

    void createPageBlob(String container, String blob, int length);

    void createPageBlob(String container, String blob, int length, CreateBlobOptions options);

    UpdatePageBlobPagesResult clearPageBlobPages(String container, String blob, long rangeStart, long rangeEnd);

    UpdatePageBlobPagesResult clearPageBlobPages(String container, String blob, long rangeStart, long rangeEnd, UpdatePageBlobPagesOptions options);

    UpdatePageBlobPagesResult updatePageBlobPages(String container, String blob, long rangeStart, long rangeEnd, long length, InputStream contentStream);

    UpdatePageBlobPagesResult updatePageBlobPages(String container, String blob, long rangeStart, long rangeEnd, long length, InputStream contentStream,
            UpdatePageBlobPagesOptions options);

    void createBlockBlob(String container, String blob, InputStream content);

    void createBlockBlob(String container, String blob, InputStream content, CreateBlobOptions options);

    BlobProperties getBlobProperties(String container, String blob);

    BlobProperties getBlobProperties(String container, String blob, GetBlobPropertiesOptions options);

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
