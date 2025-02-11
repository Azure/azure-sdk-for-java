// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.AddUploadPartRequest;
import com.azure.ai.openai.models.CompleteUploadRequest;
import com.azure.ai.openai.models.CreateUploadRequest;
import com.azure.ai.openai.models.CreateUploadRequestPurpose;
import com.azure.ai.openai.models.DataFileDetails;
import com.azure.ai.openai.models.Upload;
import com.azure.ai.openai.models.UploadPart;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * A sample demonstrating how to upload large files in parts.
 */
public class UploadLargeFileInPartsSample {
    /**
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
        String fileName2 = "java_sdk_tests_fine_tuning.json";

        Path path = Paths.get("{your_local_path}\\azure-sdk-for-java\\sdk\\openai\\azure-ai-openai\\src\\test\\resources\\java_sdk_tests_files.txt");
        Path path2 = Paths.get("{your_local_path}\\azure-sdk-for-java\\sdk\\openai\\azure-ai-openai\\src\\test\\resources\\java_sdk_tests_fine_tuning.json");

        int fileSize = getFileSize(path);
        int fileSize2 = getFileSize(path2);

        CreateUploadRequest createUploadRequest = new CreateUploadRequest("testJavaUploadLargeFile.txt", CreateUploadRequestPurpose.ASSISTANTS,
            fileSize + fileSize2, "text/plain");
        Upload upload = client.createUpload(createUploadRequest);

        String uploadId = upload.getId();
        System.out.println("Upload created, upload ID = " + uploadId);

        UploadPart uploadPartAdded = client.addUploadPart(uploadId,
            new AddUploadPartRequest(new DataFileDetails(BinaryData.fromFile(path)).setFilename(fileName)));
        String uploadPartAddedId = uploadPartAdded.getId();
        System.out.println("Upload part added, upload part ID = " + uploadPartAddedId);

        UploadPart uploadPartAdded2 = client.addUploadPart(uploadId,
            new AddUploadPartRequest(new DataFileDetails(BinaryData.fromFile(path2)).setFilename(fileName2)));
        String uploadPartAddedId2 = uploadPartAdded2.getId();
        System.out.println("Upload part 2 added, upload part ID = " + uploadPartAddedId2);

        CompleteUploadRequest completeUploadRequest = new CompleteUploadRequest(Arrays.asList(uploadPartAddedId, uploadPartAddedId2));

        Upload completeUpload = client.completeUpload(uploadId, completeUploadRequest);
        System.out.println("Upload completed, upload ID = " + completeUpload.getId());
    }

    // Find the bytes of the file
    private static int getFileSize(Path path) {
        return (int) path.toFile().length();
    }
}
