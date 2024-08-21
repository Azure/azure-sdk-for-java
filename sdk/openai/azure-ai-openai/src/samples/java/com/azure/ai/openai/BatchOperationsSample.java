// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Batch;
import com.azure.ai.openai.models.BatchCreateRequest;
import com.azure.ai.openai.models.BatchStatus;
import com.azure.ai.openai.models.FileDeletionStatus;
import com.azure.ai.openai.models.FileDetails;
import com.azure.ai.openai.models.FilePurpose;
import com.azure.ai.openai.models.FileState;
import com.azure.ai.openai.models.OpenAIFile;
import com.azure.ai.openai.models.PageableList;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A sample demonstrating how to call batch operations.
 */
public class BatchOperationsSample {
    /**
     * Runs the sample algorithm and demonstrates how to call batch operations.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildClient();

        // Upload file
        String fileName = "batch_tasks.jsonl";
        FileDetails fileDetails = new FileDetails(BinaryData.fromFile(openResourceFile(fileName)), fileName);
        OpenAIFile file = client.uploadFile(fileDetails, FilePurpose.BATCH);
        String fileId = file.getId();
        System.out.println("File uploaded, file ID = " + fileId);

        // Create a batch
        while (file.getStatus() == FileState.PENDING) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            file = client.getFile(file.getId());
        }

        Batch batch = client.createBatch(new BatchCreateRequest("/chat/completions", file.getId(), "24h"));

        // Get single file
        while (batch.getStatus() == BatchStatus.VALIDATING
            || batch.getStatus() == BatchStatus.IN_PROGRESS
            || batch.getStatus() == BatchStatus.FINALIZING) {
            try {
                Thread.sleep(1000);
                System.out.println("Batch status: " + batch.getStatus());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            batch = client.getBatch(batch.getId());
        }

        String outputFileId = batch.getOutputFileId();
        byte[] fileContent = client.getFileContent(outputFileId);
        System.out.println("File output ID is " + outputFileId);
        System.out.println("File content: ");
        System.out.println(new String(fileContent));

        // List batches
        PageableList<Batch> batchPageableList = client.listBatches();
        System.out.println("List of batches:");
        batchPageableList.getData().forEach(b -> System.out.println("\t" + b.getId()));

        // Delete file
        FileDeletionStatus deletionStatus = client.deleteFile(fileId);
        System.out.println("File is deleted, file ID = " + deletionStatus.getId());
    }

    private static Path openResourceFile(String fileName) {
        return Paths.get("src", "test", "resources", fileName);
    }
}
