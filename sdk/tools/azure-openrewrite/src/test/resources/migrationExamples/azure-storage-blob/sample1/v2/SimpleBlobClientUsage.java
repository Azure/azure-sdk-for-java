// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.v2.storage.blob.BlobServiceClient;
import com.azure.v2.storage.blob.BlobServiceClientBuilder;
import com.azure.v2.storage.blob.models.BlobStorageException;
import com.azure.v2.storage.blob.models.ListBlobsOptions;
import com.azure.v2.storage.blob.specialized.BlockBlobClient;
import io.clientcore.core.credential.KeyCredential;
import io.clientcore.core.http.models.HttpInstrumentationOptions;
import io.clientcore.core.http.models.HttpInstrumentationOptions.HttpLogDetailLevel;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.util.Context;

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
            .policies(new HttpRetryPolicy())
            .build();

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .httpInstrumentationOptions(new HttpInstrumentationOptions().setLogLevel(HttpInstrumentationOptions.HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(new KeyCredential(sasToken))
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
            .listBlobs(options, Context.none())
            .forEach(blobItem -> blobNames.add(blobItem.getName()));

        logger.info("Blobs in container:");
        blobNames.forEach(logger::info);

        // Download the blob
        try {
            byte[] downloadedContent = new byte[(int) blobClient.getProperties().getBlobSize()];
            blobClient.download(downloadedContent, Context.none());
            logger.info("Downloaded blob content: " + new String(downloadedContent, StandardCharsets.UTF_8));
        } catch (BlobStorageException e) {
            logger.error("Failed to download blob.", e);
        }
    }
}
