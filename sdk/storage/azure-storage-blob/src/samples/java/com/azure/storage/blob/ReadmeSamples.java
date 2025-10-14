// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob;

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private final BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().buildClient();
    private final BlobContainerClient blobContainerClient = new BlobContainerClientBuilder().buildClient();
    private final BlobClient blobClient = new BlobClientBuilder().buildClient();

    public void getBlobServiceClient1() {
        // BEGIN: readme-sample-getBlobServiceClient1
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .buildClient();
        // END: readme-sample-getBlobServiceClient1
    }

    public void getBlobServiceClient2() {
        // BEGIN: readme-sample-getBlobServiceClient2
        // Only one "?" is needed here. If the SAS token starts with "?", please removing one "?".
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint("<your-storage-account-url>" + "?" + "<your-sasToken>")
            .buildClient();
        // END: readme-sample-getBlobServiceClient2
    }

    public void getBlobContainerClient1() {
        // BEGIN: readme-sample-getBlobContainerClient1
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient("mycontainer");
        // END: readme-sample-getBlobContainerClient1
    }

    public void getBlobContainerClient2() {
        // BEGIN: readme-sample-getBlobContainerClient2
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .containerName("mycontainer")
            .buildClient();
        // END: readme-sample-getBlobContainerClient2
    }

    public void getBlobContainerClient3() {
        // BEGIN: readme-sample-getBlobContainerClient3
        // Only one "?" is needed here. If the SAS token starts with "?", please removing one "?".
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "mycontainer" + "?" + "<your-sasToken>")
            .buildClient();
        // END: readme-sample-getBlobContainerClient3
    }

    public void getBlobClient1() {
        // BEGIN: readme-sample-getBlobClient1
        BlobClient blobClient = blobContainerClient.getBlobClient("myblob");
        // END: readme-sample-getBlobClient1
    }

    public void getBlobClient2() {
        // BEGIN: readme-sample-getBlobClient2
        BlobClient blobClient = new BlobClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .containerName("mycontainer")
            .blobName("myblob")
            .buildClient();
        // END: readme-sample-getBlobClient2
    }

    public void getBlobClient3() {
        // BEGIN: readme-sample-getBlobClient3
        // Only one "?" is needed here. If the SAS token starts with "?", please removing one "?".
        BlobClient blobClient = new BlobClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "mycontainer" + "/" + "myblob" + "?" + "<your-sasToken>")
            .buildClient();
        // END: readme-sample-getBlobClient3
    }

    public void createBlobContainerClient1() {
        // BEGIN: readme-sample-createBlobContainerClient1
        blobServiceClient.createBlobContainer("mycontainer");
        // END: readme-sample-createBlobContainerClient1
    }

    public void createBlobContainerClient2() {
        // BEGIN: readme-sample-createBlobContainerClient2
        blobContainerClient.create();
        // END: readme-sample-createBlobContainerClient2
    }

    public void uploadBinaryDataToBlob() {
        // BEGIN: readme-sample-uploadBinaryDataToBlob
        BlobClient blobClient = blobContainerClient.getBlobClient("myblockblob");
        String dataSample = "samples";
        blobClient.upload(BinaryData.fromString(dataSample));
        // END: readme-sample-uploadBinaryDataToBlob
    }

    public void uploadBlobFromStream() {
        // BEGIN: readme-sample-uploadBlobFromStream
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient("myblockblob").getBlockBlobClient();
        String dataSample = "samples";
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            blockBlobClient.upload(dataStream, dataSample.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // END: readme-sample-uploadBlobFromStream
    }

    public void uploadBlobFromFile() {
        // BEGIN: readme-sample-uploadBlobFromFile
        BlobClient blobClient = blobContainerClient.getBlobClient("myblockblob");
        blobClient.uploadFromFile("local-file.jpg");
        // END: readme-sample-uploadBlobFromFile
    }

    public void uploadIfNotExists() {
        // BEGIN: readme-sample-uploadIfNotExists
        /*
         * Rather than use an if block conditioned on an exists call, there are three ways to upload-if-not-exists using
         * one network call instead of two. Equivalent options are present on all upload methods.
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

        // 3. If the max overload is needed, access conditions must be used to prevent overwriting
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            BlobParallelUploadOptions options =
                new BlobParallelUploadOptions(dataStream, dataSample.length());
            // Setting IfNoneMatch="*" ensures the upload will fail if there is already a blob at the destination.
            options.setRequestConditions(new BlobRequestConditions().setIfNoneMatch("*"));
            blobClient.uploadWithResponse(options, null, Context.NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // END: readme-sample-uploadIfNotExists
    }

    public void overwriteBlob() {
        // BEGIN: readme-sample-overwriteBlob
        /*
         * Rather than use an if block conditioned on an exists call, there are three ways to upload-if-exists in one
         * network call instead of two. Equivalent options are present on all upload methods.
         */
        String dataSample = "samples";

        // 1. The overwrite flag can explicitly be set to true. This will succeed as a create and overwrite.
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            blobClient.upload(dataStream, dataSample.length(), true /* overwrite */);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * 2. If the max overload is needed and no access conditions are passed, the upload will succeed as both a
         * create and overwrite.
         */
        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
            BlobParallelUploadOptions options =
                new BlobParallelUploadOptions(dataStream, dataSample.length());
            blobClient.uploadWithResponse(options, null, Context.NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * 3. If the max overload is needed, access conditions may be used to assert that the upload is an overwrite and
         * not simply a create.
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
        // END: readme-sample-overwriteBlob
    }

    public void openBlobOutputStream() {
        // BEGIN: readme-sample-openBlobOutputStream
        /*
         * Opening a blob input stream allows you to write to a blob through a normal stream interface. It will not be
         * committed until the stream is closed.
         * This option is convenient when the length of the data is unknown.
         * This can only be done for block blobs. If the target blob already exists as another type of blob, it will
         * fail.
         */
        try (BlobOutputStream blobOS = blobClient.getBlockBlobClient().getBlobOutputStream()) {
            blobOS.write(new byte[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // END: readme-sample-openBlobOutputStream
    }

    public void downloadDataFromBlob() {
        // BEGIN: readme-sample-downloadDataFromBlob
        BinaryData content = blobClient.downloadContent();
        // END: readme-sample-downloadDataFromBlob
    }

    public void downloadBlobToStream() {
        // BEGIN: readme-sample-downloadBlobToStream
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blobClient.downloadStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // END: readme-sample-downloadBlobToStream
    }

    public void downloadBlobToFile() {
        // BEGIN: readme-sample-downloadBlobToFile
        blobClient.downloadToFile("downloaded-file.jpg");
        // END: readme-sample-downloadBlobToFile
    }

    public void openBlobInputStream() {
        // BEGIN: readme-sample-openBlobInputStream
        /*
         * Opening a blob input stream allows you to read from a blob through a normal stream interface. It is also
         * mark-able.
        */
        try (BlobInputStream blobIS = blobClient.openInputStream()) {
            blobIS.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // END: readme-sample-openBlobInputStream
    }

    public void enumerateBlobs() {
        // BEGIN: readme-sample-enumerateBlobs
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            System.out.println("This is the blob name: " + blobItem.getName());
        }
        // END: readme-sample-enumerateBlobs
    }

    public void enumerateBlobsCreateClient() {
        // BEGIN: readme-sample-enumerateBlobsCreateClient
        for (BlobItem blobItem : blobContainerClient.listBlobs()) {
            BlobClient blobClient;
            if (blobItem.getSnapshot() != null) {
                blobClient = blobContainerClient.getBlobClient(blobItem.getName(), blobItem.getSnapshot());
            } else {
                blobClient = blobContainerClient.getBlobClient(blobItem.getName());
            }
            System.out.println("This is the new blob uri: " + blobClient.getBlobUrl());
        }
        // END: readme-sample-enumerateBlobsCreateClient
    }

    public void copyBlob() {
        // BEGIN: readme-sample-copyBlob
        SyncPoller<BlobCopyInfo, Void> poller = blobClient.beginCopy("<url-to-blob>", Duration.ofSeconds(1));
        poller.waitForCompletion();
        // END: readme-sample-copyBlob
    }

    public void copyBlob2() {
        // BEGIN: readme-sample-copyBlob2
        blobClient.copyFromUrl("url-to-blob");
        // END: readme-sample-copyBlob2
    }

    public void generateSas() {
        // BEGIN: readme-sample-generateSas
        /*
         * Generate an account sas. Other samples in this file will demonstrate how to create a client with the sas
         * token.
         */
        // Configure the sas parameters. This is the minimal set.
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        AccountSasPermission accountSasPermission = new AccountSasPermission().setReadPermission(true);
        AccountSasService services = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceTypes = new AccountSasResourceType().setObject(true);

        // Generate the account sas.
        AccountSasSignatureValues accountSasValues =
            new AccountSasSignatureValues(expiryTime, accountSasPermission, services, resourceTypes);
        String sasToken = blobServiceClient.generateAccountSas(accountSasValues);

        // Generate a sas using a container client
        BlobContainerSasPermission containerSasPermission = new BlobContainerSasPermission().setCreatePermission(true);
        BlobServiceSasSignatureValues serviceSasValues =
            new BlobServiceSasSignatureValues(expiryTime, containerSasPermission);
        blobContainerClient.generateSas(serviceSasValues);

        // Generate a sas using a blob client
        BlobSasPermission blobSasPermission = new BlobSasPermission().setReadPermission(true);
        serviceSasValues = new BlobServiceSasSignatureValues(expiryTime, blobSasPermission);
        blobClient.generateSas(serviceSasValues);
        // END: readme-sample-generateSas
    }

    public void authWithIdentity() {
        // BEGIN: readme-sample-authWithIdentity
        BlobServiceClient blobStorageClient = new BlobServiceClientBuilder()
            .endpoint("<your-storage-account-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-authWithIdentity
    }

    public void setProxy() {
        // BEGIN: readme-sample-setProxy
        ProxyOptions options = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 888));
        BlobServiceClient client = new BlobServiceClientBuilder()
            .endpoint("<ENDPOINT>")
            .sasToken("<SAS_TOKEN>")
            .httpClient(new NettyAsyncHttpClientBuilder().proxy(options).build())
            .buildClient();
        // END: readme-sample-setProxy
    }

    public void setProxy2() {
        // BEGIN: readme-sample-setProxy2
        HttpClientOptions clientOptions = new HttpClientOptions()
            .setProxyOptions(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 888)));
        BlobServiceClient client = new BlobServiceClientBuilder()
            .endpoint("<ENDPOINT>")
            .sasToken("<SAS_TOKEN>")
            .clientOptions(clientOptions)
            .buildClient();
        // END: readme-sample-setProxy2
    }
}

