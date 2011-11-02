package com.microsoft.azure.services.blob;

public interface BlobContract {
    ListContainersResults listContainers();

    ListContainersResults listContainers(ListContainersOptions options);

    void createContainer(String container);

    void createContainer(String container, CreateContainerOptions options);

    void deleteContainer(String container);

    ContainerProperties getContainerProperties(String container);

    ContainerProperties getContainerMetadata(String container);

    ListBlobsResults listBlobs(String container);

    ListBlobsResults listBlobs(String container, ListBlobsOptions options);
}
