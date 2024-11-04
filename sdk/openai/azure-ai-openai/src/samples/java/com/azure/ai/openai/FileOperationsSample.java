// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.FileDeletionStatus;
import com.azure.ai.openai.models.FileDetails;
import com.azure.ai.openai.models.FilePurpose;
import com.azure.ai.openai.models.OpenAIFile;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A sample demonstrating how to call files operations.
 */
public class FileOperationsSample {

    /**
     * Runs the sample algorithm and demonstrates how to call files operations.
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
        String fileName = "java_sdk_tests_files.txt";
        FileDetails fileDetails = new FileDetails(BinaryData.fromFile(openResourceFile(fileName)), fileName);
        OpenAIFile file = client.uploadFile(fileDetails, FilePurpose.ASSISTANTS);
        String fileId = file.getId();
        System.out.println("File uploaded, file ID = " + fileId);

        // Get single file
        OpenAIFile fileFromBackend = client.getFile(fileId);
        System.out.println("File retrieved, file ID = " + fileFromBackend.getId());

        // List files
        System.out.println("List of files filtered by purpose:");
        List<OpenAIFile> files = client.listFiles(FilePurpose.ASSISTANTS);
        files.stream()
            .filter(f -> f.getId().equals(fileId))
            .forEach(f -> System.out.println("\t" + f.getId()));

        // Delete file
        FileDeletionStatus deletionStatus = client.deleteFile(fileId);
        System.out.println("File is deleted, file ID = " + deletionStatus.getId());
    }

    private static Path openResourceFile(String fileName) {
        return Paths.get("src", "test", "resources", fileName);
    }
}
