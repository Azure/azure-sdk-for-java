package com.azure.storage.datamover.sample;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.resource.BlobStorageResources;
import com.azure.storage.common.datamover.DataMover;
import com.azure.storage.common.datamover.DataMoverBuilder;
import com.azure.storage.common.datamover.DataTransfer;
import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import com.azure.storage.common.resource.filesystem.LocalFileSystemStorageResources;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.resource.ShareStorageResources;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DataMoverSample {

    private static final String STORAGE_CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    private static final long timestamp = System.currentTimeMillis();

    public static void main(String[] args) throws Exception {
        DataMover dataMover = new DataMoverBuilder().build();
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(STORAGE_CONNECTION_STRING)
            .buildClient();

        ShareServiceClient shareServiceClient = new ShareServiceClientBuilder()
            .connectionString(STORAGE_CONNECTION_STRING)
            .buildClient();

        cleanup(blobServiceClient, shareServiceClient);

        transferFileToBlob(dataMover, blobServiceClient);
        BlobContainerClient blobContainerClient = transferDirectoryToBlobContainer(dataMover, blobServiceClient);

        transferFileToShareFile(dataMover, shareServiceClient);
        ShareClient shareClient = transferDirectoryToShare(dataMover, shareServiceClient);
        transferDirectoryToShareDirectory(dataMover, shareServiceClient);

        transferBlobContainerToShare(dataMover, blobContainerClient, shareServiceClient);

        transferShareToBlobContainer(dataMover, shareClient, blobServiceClient);
    }

    private static BlobClient transferFileToBlob(DataMover dataMover, BlobServiceClient blobServiceClient) throws Exception {
        BlobContainerClient containerClient = blobServiceClient.createBlobContainerIfNotExists("a" +timestamp + "-01-filetoblob");

        Path sampleFile = Paths.get(DataMoverSample.class.getResource("/samplefile.txt").toURI());

        StorageResource localFile = LocalFileSystemStorageResources.file(sampleFile);
        BlobClient blobClient = containerClient.getBlobClient("samplefile.txt");
        StorageResource blob = BlobStorageResources.blob(blobClient);

        DataTransfer dataTransfer = dataMover.startTransfer(localFile, blob);

        dataTransfer.awaitCompletion();

        return blobClient;
    }

    private static BlobContainerClient transferDirectoryToBlobContainer(DataMover dataMover, BlobServiceClient blobServiceClient) throws Exception {
        BlobContainerClient containerClient = blobServiceClient.createBlobContainerIfNotExists("a" +timestamp + "-02-directorytoblobcontainer");

        Path sampleDirectory = Paths.get(DataMoverSample.class.getResource("/samplefiles").toURI());

        StorageResourceContainer localDirectory = LocalFileSystemStorageResources.directory(sampleDirectory);
        StorageResourceContainer blobContainer = BlobStorageResources.blobContainer(containerClient);

        DataTransfer dataTransfer = dataMover.startTransfer(localDirectory, blobContainer);

        dataTransfer.awaitCompletion();

        return containerClient;
    }

    private static void transferFileToShareFile(DataMover dataMover, ShareServiceClient shareServiceClient) throws Exception {
        ShareClient shareClient = shareServiceClient.createShare("a" + timestamp + "-03-filetosharefile");
        ShareFileClient fileClient = shareClient.getFileClient("samplefile.txt");

        Path sampleFile = Paths.get(DataMoverSample.class.getResource("/samplefile.txt").toURI());

        StorageResource localFile = LocalFileSystemStorageResources.file(sampleFile);
        StorageResource shareFile = ShareStorageResources.file(fileClient);

        DataTransfer dataTransfer = dataMover.startTransfer(localFile, shareFile);

        dataTransfer.awaitCompletion();
    }

    private static ShareClient transferDirectoryToShare(DataMover dataMover, ShareServiceClient shareServiceClient) throws Exception {
        ShareClient shareClient = shareServiceClient.createShare("a" + timestamp + "-04-directorytoshare");

        Path sampleDirectory = Paths.get(DataMoverSample.class.getResource("/samplefiles").toURI());

        StorageResourceContainer localDirectory = LocalFileSystemStorageResources.directory(sampleDirectory);
        StorageResourceContainer share = ShareStorageResources.share(shareClient);

        DataTransfer dataTransfer = dataMover.startTransfer(localDirectory, share);

        dataTransfer.awaitCompletion();

        return shareClient;
    }

    private static void transferDirectoryToShareDirectory(DataMover dataMover, ShareServiceClient shareServiceClient) throws Exception {
        ShareClient shareClient = shareServiceClient.createShare("a" + timestamp + "-05-directorytosharedirectory");
        ShareDirectoryClient shareDirectoryClient = shareClient.createDirectory("foo");

        Path sampleDirectory = Paths.get(DataMoverSample.class.getResource("/samplefiles").toURI());

        StorageResourceContainer localDirectory = LocalFileSystemStorageResources.directory(sampleDirectory);
        StorageResourceContainer shareDirectory = ShareStorageResources.directory(shareDirectoryClient);

        DataTransfer dataTransfer = dataMover.startTransfer(localDirectory, shareDirectory);

        dataTransfer.awaitCompletion();
    }

    private static void transferBlobContainerToShare(
        DataMover dataMover, BlobContainerClient blobContainerClient, ShareServiceClient shareServiceClient) throws Exception {

        ShareClient shareClient = shareServiceClient.createShare("a" + timestamp + "-06-blobcontainertoshare");

        StorageResourceContainer blobContainer = BlobStorageResources.blobContainer(blobContainerClient);
        StorageResourceContainer share = ShareStorageResources.share(shareClient);

        DataTransfer dataTransfer = dataMover.startTransfer(blobContainer, share);

        dataTransfer.awaitCompletion();
    }

    private static void transferShareToBlobContainer(DataMover dataMover, ShareClient shareClient, BlobServiceClient blobServiceClient) throws Exception{
        BlobContainerClient containerClient = blobServiceClient.createBlobContainerIfNotExists("a" +timestamp + "-07-sharetoblobcontainer");

        StorageResourceContainer share = ShareStorageResources.share(shareClient);
        StorageResourceContainer blobContainer = BlobStorageResources.blobContainer(containerClient);

        DataTransfer dataTransfer = dataMover.startTransfer(share, blobContainer);

        dataTransfer.awaitCompletion();
    }

    private static void cleanup(BlobServiceClient blobServiceClient, ShareServiceClient shareServiceClient) {
        blobServiceClient.listBlobContainers()
            .forEach(item -> blobServiceClient.deleteBlobContainer(item.getName()));

        shareServiceClient.listShares()
            .forEach(item -> shareServiceClient.deleteShare(item.getName()));
    }
}
