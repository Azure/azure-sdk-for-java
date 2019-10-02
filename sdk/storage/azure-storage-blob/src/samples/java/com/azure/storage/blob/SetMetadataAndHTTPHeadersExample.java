// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.credentials.SharedKeyCredential;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
     */
    public static void main(String[] args) throws IOException {
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        SharedKeyCredential credential = new SharedKeyCredential(accountName, accountKey);

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
        BlobHTTPHeaders blobHTTPHeaders = new BlobHTTPHeaders().setBlobContentDisposition("attachment")
            .setBlobContentType("text/html; charset=utf-8");

        /*
         * Data which will upload to block blob.
         */
        String data = "Hello world!";
        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        blobClient.uploadWithResponse(dataStream, data.length(), blobHTTPHeaders, blobMetadata, null, null, null, null);

        /*
         * Clean up the container and blob.
         */
        blobClient.delete();
        blobContainerClient.delete();
    }
}
