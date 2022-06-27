package com.azure.storage.datamover.sample;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.datamover.DataMover;
import com.azure.storage.datamover.DataMoverBuilder;
import com.azure.storage.datamover.DataTransfer;
import com.azure.storage.datamover.blob.BlobResources;
import com.azure.storage.datamover.file.share.FileShareResources;
import com.azure.storage.datamover.filesystem.FileSystemResources;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;

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
        transferDirectoryToBlobContainer(dataMover, blobServiceClient);

        transferFileToShareFile(dataMover, shareServiceClient);
        transferDirectoryToShare(dataMover, shareServiceClient);
    }



    private static void transferFileToBlob(DataMover dataMover, BlobServiceClient blobServiceClient) throws Exception {
        BlobContainerClient containerClient = blobServiceClient.createBlobContainerIfNotExists("a" +timestamp + "-01-filetoblob");

        Path sampleFile = Paths.get(DataMoverSample.class.getResource("/samplefile.txt").toURI());

        DataTransfer dataTransfer = dataMover.startTransfer(
            FileSystemResources.file(sampleFile),
            BlobResources.blob(containerClient.getBlobClient("samplefile.txt"))
        );

        dataTransfer.awaitCompletion();
    }

    private static void transferDirectoryToBlobContainer(DataMover dataMover, BlobServiceClient blobServiceClient) throws Exception {
        BlobContainerClient containerClient = blobServiceClient.createBlobContainerIfNotExists("a" +timestamp + "-02-directorytoblobcontainer");

        Path sampleDirectory = Paths.get(DataMoverSample.class.getResource("/samplefiles").toURI());

        DataTransfer dataTransfer = dataMover.startTransfer(
            FileSystemResources.directory(sampleDirectory),
            BlobResources.blobContainer(containerClient)
        );

        dataTransfer.awaitCompletion();
    }

    private static void transferFileToShareFile(DataMover dataMover, ShareServiceClient shareServiceClient) throws Exception {
        ShareClient shareClient = shareServiceClient.createShare("a" + timestamp + "-03-filetosharefile");
        ShareFileClient fileClient = shareClient.getFileClient("samplefile.txt");

        Path sampleFile = Paths.get(DataMoverSample.class.getResource("/samplefile.txt").toURI());

        DataTransfer dataTransfer = dataMover.startTransfer(
            FileSystemResources.file(sampleFile),
            FileShareResources.file(fileClient)
        );

        dataTransfer.awaitCompletion();
    }

    private static void transferDirectoryToShare(DataMover dataMover, ShareServiceClient shareServiceClient) throws Exception {
        ShareClient shareClient = shareServiceClient.createShare("a" + timestamp + "-04-directorytoshare");

        Path sampleDirectory = Paths.get(DataMoverSample.class.getResource("/samplefiles").toURI());

        DataTransfer dataTransfer = dataMover.startTransfer(
            FileSystemResources.directory(sampleDirectory),
            FileShareResources.share(shareClient)
        );

        dataTransfer.awaitCompletion();
    }

    private static void cleanup(BlobServiceClient blobServiceClient, ShareServiceClient shareServiceClient) {
        blobServiceClient.listBlobContainers()
            .forEach(item -> blobServiceClient.deleteBlobContainer(item.getName()));

        shareServiceClient.listShares()
            .forEach(item -> shareServiceClient.deleteShare(item.getName()));
    }
}
