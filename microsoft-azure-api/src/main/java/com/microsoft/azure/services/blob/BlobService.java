package com.microsoft.azure.services.blob;

import java.io.InputStream;
import java.util.HashMap;

//TODO: ServiceException annotation and handling in implementation
public interface BlobService {
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

    void putPageBlob(String container, String blob, int length);
    void putPageBlob(String container, String blob, int length, PutBlobOptions options);

    void putBlockBlob(String container, String blob, InputStream content);
    void putBlockBlob(String container, String blob, InputStream content, PutBlobOptions options);

    BlobProperties getBlobProperties(String container, String blob);
    BlobProperties getBlobProperties(String container, String blob, GetBlobPropertiesOptions options);
}
