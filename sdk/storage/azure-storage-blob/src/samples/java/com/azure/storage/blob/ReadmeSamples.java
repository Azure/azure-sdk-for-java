// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob;

import com.azure.core.http.RequestConditions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().buildClient();
    private BlobContainerClient blobContainerClient = new BlobContainerClientBuilder().buildClient();
    private BlobClient blobClient = new BlobClientBuilder().buildClient();

    public void getBlobServiceClient1() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .buildClient();
    }

    public void getBlobServiceClient2() {
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint("<your-storage-account-url>" + "?" + "<your-sasToken>")
            .buildClient();
    }

    public void getBlobContainerClient1() {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient("mycontainer");
    }

    public void getBlobContainerClient2() {
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .containerName("mycontainer")
            .buildClient();
    }

    public void getBlobContainerClient3() {
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "mycontainer" + "?" + "<your-sasToken>")
            .buildClient();
    }

    public void getBlobClient1() {
        BlobClient blobClient = blobContainerClient.getBlobClient("myblob");
    }

    public void getBlobClient2() {
        BlobClient blobClient = new BlobClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .containerName("mycontainer")
            .blobName("myblob")
            .buildClient();
    }

    public void getBlobClient3() {
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        BlobClient blobClient = new BlobClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "mycontainer" + "/" + "myblob" + "?" + "<your-sasToken>")
            .buildClient();
    }

    public void createBlobContainerClient1() {
        blobServiceClient.createBlobContainer("mycontainer");
    }

    public void createBlobContainerClient2() {
        blobContainerClient.create();
    }

    public void uploadBlobFromStream() {
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient("myblockblob").getBlockBlobClient();
        String dataSample = "samples";
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            blockBlobClient.upload(dataStream, dataSample.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadBlobFromFile() {
        BlobClient blobClient = blobContainerClient.getBlobClient("myblockblob");
        blobClient.uploadFromFile("local-file.jpg");
    }

    public void downloadBlobToStream() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blobClient.downloadStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadBlobToFile() {
        blobClient.downloadToFile("downloaded-file.jpg");
    }

    public void enumerateBlobs() {
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            System.out.println("This is the blob name: " + blobItem.getName());
        }
    }

    public void authWithIdentity() {
        BlobServiceClient blobStorageClient = new BlobServiceClientBuilder()
            .endpoint("<your-storage-account-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }

    public void copyBlob() {
        SyncPoller<BlobCopyInfo, Void> poller = blobClient.beginCopy("<url-to-blob>", Duration.ofSeconds(1));
        poller.waitForCompletion();
    }

    public void copyBlob2() {
        blobClient.copyFromUrl("url-to-blob");
    }

    public void uploadBinaryDataToBlob() {
        BlobClient blobClient = blobContainerClient.getBlobClient("myblockblob");
        String dataSample = "samples";
        blobClient.upload(BinaryData.fromString(dataSample));
    }

    public void downloadDataFromBlob() {
        BinaryData content = blobClient.downloadContent();
    }

    public void enumerateBlobsCreateClient() {
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            BlobClient blobClient;
            if (blobItem.getSnapshot() != null) {
                blobClient = blobContainerClient.getBlobClient(blobItem.getName(), blobItem.getSnapshot());
            } else {
                blobClient = blobContainerClient.getBlobClient(blobItem.getName());
            }
            System.out.println("This is the new blob uri: " + blobClient.getBlobUrl());
        }
    }

    public void uploadIfNotExists() {
        /*
       Rather than use an if block conditioned on an exists call, there are three ways to upload-if-not-exists in one
       line.
         */
        // 1. The minimal upload method defaults to no overwriting
        String dataSample = "samples";
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            blobClient.upload(dataStream, dataSample.length());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2. The overwrite flag can explicitly be set to false to make intention clear
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            blobClient.upload(dataStream, dataSample.length(), false /* overwrite */);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3. If the max overload is needed, access conditions must be used to prevent overwriting.
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            BlobParallelUploadOptions options =
                new BlobParallelUploadOptions(dataStream, dataSample.length());
            // Setting IfNoneMatch="*" ensures the upload will fail if there is already a blob at the destination.
            options.setRequestConditions(new BlobRequestConditions().setIfNoneMatch("*"));
            blobClient.uploadWithResponse(options, null, Context.NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void overwriteBlob() {
        /*
       Rather than use an if block conditioned on an exists call, there are three ways to upload-if-exists in one
       line.
         */
        String dataSample = "samples";

        // 1. The overwrite flag can explicitly be set to true. This will succeed as a create and overwrite.
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            blobClient.upload(dataStream, dataSample.length(), true /* overwrite */);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         2. If the max overload is needed and no access conditions are passed, the upload will succeed as both a
         create and overwrite.
         */
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            BlobParallelUploadOptions options =
                new BlobParallelUploadOptions(dataStream, dataSample.length());
            blobClient.uploadWithResponse(options, null, Context.NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         3. If the max overload is needed, access conditions may be used to assert that the upload is an overwrite and
         not simply a create.
         */
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            BlobParallelUploadOptions options =
                new BlobParallelUploadOptions(dataStream, dataSample.length());

            // Setting IfMatch="*" ensures the upload will succeed only if there is already a blob at the destination.
            options.setRequestConditions(new BlobRequestConditions().setIfMatch("*"));
            blobClient.uploadWithResponse(options, null, Context.NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setProxy() {

    }

    public void openBlobInputStream() {
        /*
        Opening a blob input stream allows you to read from a blob through a normal stream interface. It is also
        markable.
         */
    }

    public void openBlobOutputStream() {
        /*
        Opening a blob input stream allows you to write to a blob through a normal stream interface. It will not be
        committed until the stream is closed.
        This option is convenient when the length of the data is unknown.
         */
    }

    public void generateSas() {

    }
}

