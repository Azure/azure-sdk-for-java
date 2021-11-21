// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * This example shows how to set metadata for containers and blobs and how to set HTTPHeaders for blobs using the Azure
 * Storage Blob SDK for Java.
 */
public class SetMetadataAndHTTPHeadersExample {

    /**
     * Entry point into the setting metadata examples for Storage blobs.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException If an I/O error occurs
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();

        /*
         * Create a container client from storageClient.
         */
        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient("mycontainer" + System.currentTimeMillis());

        /*
         * Setup containerMetadata for container.
         */
        Map<String, String> containerMetadata = Collections.singletonMap("mycontainermetadata", "sample");

        /*
         * Create a container with the containerMetadata above.
         */
        blobContainerClient.createWithResponse(containerMetadata, null, null, new Context("key1", "value1"));

        /*
         * Create a blob client.
         */
        BlockBlobClient blobClient = blobContainerClient.getBlobClient("myblob" + System.currentTimeMillis()).getBlockBlobClient();

        /*
         * Create a blob with blob's blobMetadata and BlobHttpHeaders.
         */
        Map<String, String> blobMetadata = Collections.singletonMap("myblobmetadata", "sample");
        BlobHttpHeaders blobHTTPHeaders = new BlobHttpHeaders().setContentDisposition("attachment")
            .setContentType("text/html; charset=utf-8");


        String data = "Hello world!";

        /*
         * Send an MD5 hash of the content to be validated by the service.
         */
        byte[] md5 = MessageDigest.getInstance("MD5").digest(data.getBytes(StandardCharsets.UTF_8));

        /*
         * Data which will upload to block blob.
         */
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        blobClient.uploadWithResponse(dataStream, data.length(), blobHTTPHeaders, blobMetadata, null, md5, null, null,
            null);

        /*
         * Clean up the container and blob.
         */
        blobClient.delete();
        blobContainerClient.delete();
    }
}
