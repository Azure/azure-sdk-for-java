// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SimpleBlobClientUsage {
    private static final ClientLogger logger = new ClientLogger(AzureStorageBlobExample.class);

    public static void main(String[] args) {
        String sasToken = "your-sas-token";
        String endpoint = "https://your-storage-account.blob.core.windows.net";
        String containerName = "your-container-name";
        String blobName = "sample-blob.txt";
        String content = "Hello, Azure Blob Storage!";

        // Create a BlobServiceClient with a custom HttpPipeline
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new RetryPolicy())
            .build();

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(new AzureSasCredential(sasToken))
            .pipeline(pipeline)
            .buildClient();

        // Upload a blob
        BlockBlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName)
            .getBlockBlobClient();

        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            blobClient.upload(dataStream, content.length(), true);
            logger.info("Blob uploaded successfully.");
        } catch (IOException e) {
            logger.error("Failed to upload blob.", e);
        }

        // List blobs in the container
        ListBlobsOptions options = new ListBlobsOptions().setPrefix("sample");
        List<String> blobNames = new ArrayList<>();
        blobServiceClient.getBlobContainerClient(containerName)
            .listBlobs(options, Context.NONE)
            .forEach(blobItem -> blobNames.add(blobItem.getName()));

        logger.info("Blobs in container:");
        blobNames.forEach(logger::info);

        // Download the blob
        try {
            byte[] downloadedContent = new byte[(int) blobClient.getProperties().getBlobSize()];
            blobClient.download(downloadedContent, Context.NONE);
            logger.info("Downloaded blob content: " + new String(downloadedContent, StandardCharsets.UTF_8));
        } catch (BlobStorageException e) {
            logger.error("Failed to download blob.", e);
        }
    }
}
