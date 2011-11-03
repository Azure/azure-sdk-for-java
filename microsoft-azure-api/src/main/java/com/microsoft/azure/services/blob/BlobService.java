package com.microsoft.azure.services.blob;

import java.util.HashMap;

public interface BlobService {
    ListContainersResults listContainers();

    ListContainersResults listContainers(ListContainersOptions options);

    void createContainer(String container);

    void createContainer(String container, CreateContainerOptions options);

    void deleteContainer(String container);

    ContainerProperties getContainerProperties(String container);

    ContainerProperties getContainerMetadata(String container);

    void setContainerMetadata(String container, HashMap<String, String> metadata);

    ListBlobsResults listBlobs(String container);

    ListBlobsResults listBlobs(String container, ListBlobsOptions options);
}
