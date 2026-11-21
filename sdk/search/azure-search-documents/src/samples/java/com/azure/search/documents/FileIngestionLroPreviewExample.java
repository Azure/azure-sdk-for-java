// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.FileKnowledgeSource;
import com.azure.search.documents.indexes.models.FileKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.FileUploadMetadata;
import com.azure.search.documents.indexes.models.KnowledgeSourceContentExtractionMode;
import com.azure.search.documents.indexes.models.KnowledgeSourceFile;
import com.azure.search.documents.indexes.models.KnowledgeSourceFileIngestionOperation;
import com.azure.search.documents.indexes.models.UpdateKnowledgeSourceFileRequest;
import com.azure.search.documents.indexes.models.UploadKnowledgeSourceFileMultipartRequest;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.models.ContentFileDetails;

import java.time.Duration;
import java.util.Collections;

/**
 * Ingests and replaces a text file using native SDK pollers, then inspects retained operations.
 *
 * <p>Set {@code SEARCH_ENDPOINT} and {@code SEARCH_API_KEY} for a November-preview service that supports File
 * knowledge source ingestion. Minimal text extraction needs no chat or embedding deployment. This sample creates
 * a unique source and deletes only that source and its files. It demonstrates ingestion, not KB retrieval.</p>
 */
public final class FileIngestionLroPreviewExample {
    /**
     * Runs the file-ingestion lifecycle.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        run(PreviewSampleSupport.indexClient(), PreviewSampleSupport.name("java-dp11-file"),
            Duration.ofMinutes(3), Duration.ofSeconds(2));
    }

    static void run(SearchIndexClient client, String sourceName, Duration timeout, Duration pollInterval) {
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(client)) {
            resources.createKnowledgeSource(new FileKnowledgeSource(sourceName,
                new FileKnowledgeSourceParameters().setIngestionParameters(new KnowledgeSourceIngestionParameters()
                    .setContentExtractionMode(KnowledgeSourceContentExtractionMode.MINIMAL))));
            System.out.println("Created owned File knowledge source: " + sourceName);

            SyncPoller<KnowledgeSourceFileIngestionOperation, KnowledgeSourceFile> upload = client
                .beginIngestKnowledgeSourceFileMultipart(sourceName,
                    new UploadKnowledgeSourceFileMultipartRequest(metadata("1"),
                        content("The hotel offers breakfast and wireless internet.")))
                .setPollInterval(pollInterval);
            waitForSuccess(upload, timeout);
            KnowledgeSourceFile original = upload.getFinalResult();
            verifyFile(original, "1");

            SyncPoller<KnowledgeSourceFileIngestionOperation, KnowledgeSourceFile> replacement = client
                .beginIngestKnowledgeSourceFileReplacement(sourceName, original.getFileId(),
                    new UpdateKnowledgeSourceFileRequest(metadata("2"),
                        content("The hotel now offers breakfast, wireless internet, and parking.")))
                .setPollInterval(pollInterval);
            PollResponse<KnowledgeSourceFileIngestionOperation> completed = waitForSuccess(replacement, timeout);
            KnowledgeSourceFile updated = replacement.getFinalResult();
            verifyFile(updated, "2");
            if (!original.getFileId().equals(updated.getFileId())) {
                throw new IllegalStateException("Replacement must retain the original file ID.");
            }

            String operationId = completed.getValue().getId();
            KnowledgeSourceFileIngestionOperation operation
                = client.getKnowledgeSourceFileIngestionOperation(sourceName, operationId);
            if (operation.getResult() == null
                || !updated.getFileId().equals(operation.getResult().getFileId())) {
                throw new IllegalStateException("The retained operation must identify the ingested file.");
            }
            client.listKnowledgeSourceFileIngestionOperations(sourceName).forEach(item ->
                System.out.printf("Operation %s: %s%n", item.getId(), item.getStatus()));

            KnowledgeSourceFile listed = client.listKnowledgeSourceFiles(sourceName).stream()
                .filter(file -> updated.getFileId().equals(file.getFileId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("The ingested file was not listed."));
            verifyFile(listed, "2");
            client.deleteKnowledgeSourceFile(sourceName, updated.getFileId());
            if (client.listKnowledgeSourceFiles(sourceName).stream().findAny().isPresent()) {
                throw new IllegalStateException("The owned source should be empty after file deletion.");
            }
        }
        System.out.println("Native ingestion and in-place replacement completed; owned resources cleaned up.");
    }

    private static PollResponse<KnowledgeSourceFileIngestionOperation> waitForSuccess(
        SyncPoller<KnowledgeSourceFileIngestionOperation, KnowledgeSourceFile> poller, Duration timeout) {
        PollResponse<KnowledgeSourceFileIngestionOperation> terminal = poller.waitForCompletion(timeout);
        if (!LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.equals(terminal.getStatus())) {
            String code = terminal.getValue() == null || terminal.getValue().getError() == null
                ? "no error code" : terminal.getValue().getError().getCode();
            throw new IllegalStateException("File ingestion ended with " + terminal.getStatus() + " (" + code + ").");
        }
        return terminal;
    }

    private static FileUploadMetadata metadata(String version) {
        return new FileUploadMetadata().setFileName("notes/hotel.txt")
            .setMetadata(Collections.singletonMap("version", version));
    }

    private static ContentFileDetails content(String text) {
        return new ContentFileDetails(BinaryData.fromString(text)).setFilename("hotel.txt")
            .setContentType("text/plain; charset=utf-8");
    }

    private static void verifyFile(KnowledgeSourceFile file, String version) {
        if (file == null || file.getFileId() == null || !"notes/hotel.txt".equals(file.getFileName())
            || !Collections.singletonMap("version", version).equals(file.getMetadata())) {
            throw new IllegalStateException("The ingestion result did not preserve the requested file metadata.");
        }
    }
}
