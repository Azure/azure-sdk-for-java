// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisContent;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Demonstrates extracting embedded PDF and DOCX metadata.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>{@link DocumentContent#getMetadata()} returns the metadata the service could extract as string key-value pairs.
 * PDF and DOCX inputs can expose different keys, and no individual key is guaranteed to be present. Applications
 * should enumerate the returned map and tolerate additional keys introduced by future service versions. PDF metadata
 * commonly includes {@code author}, {@code contentType}, {@code createdAt}, {@code language}, {@code pageCount}, and
 * {@code title}. DOCX can also include {@code lastModifiedBy}, {@code lastModifiedAt}, {@code characterCount}, and
 * {@code wordCount}; every key is optional.</p>
 *
 * <p>The bundled PDF contains an author, creation timestamp, language, title, and one page; the service also returns
 * its detected content type and page count. The bundled DOCX demonstrates Office-specific last-modified metadata and
 * application-maintained character and word counts.</p>
 */
public class Sample_Advanced_ExtractDocumentMetadata {
    public static void main(String[] args) throws Exception {
        String endpoint = SampleEnvironmentConfiguration.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT",
            System.getenv("CONTENTUNDERSTANDING_ENDPOINT"));
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");
        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW);
        ContentUnderstandingClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildClient();
        }
        analyze(client, "sample_metadata.pdf");
        analyze(client, "sample_metadata.docx");
    }

    private static void analyze(ContentUnderstandingClient client, String fileName) throws Exception {
        BinaryData data = BinaryData.fromBytes(Files.readAllBytes(Paths.get("src/samples/resources", fileName)));
        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller
            = client.beginAnalyzeBinary("prebuilt-layout", data);
        AnalysisResult result = requireSuccessfulResult(poller.waitForCompletion().getStatus(), poller::getFinalResult,
            fileName + " metadata analysis");
        printMetadata(fileName, getDocumentContent(result));
    }

    static <T> T requireSuccessfulResult(LongRunningOperationStatus status, Supplier<T> finalResult,
        String operationName) {
        if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            throw new IllegalStateException(operationName + " completed unsuccessfully with status: " + status);
        }
        T result = finalResult.get();
        if (result == null) {
            throw new IllegalStateException(operationName + " completed without a final result.");
        }
        return result;
    }

    static DocumentContent getDocumentContent(AnalysisResult result) {
        if (result.getContents() != null) {
            for (AnalysisContent content : result.getContents()) {
                if (content instanceof DocumentContent) {
                    return (DocumentContent) content;
                }
            }
        }
        throw new IllegalStateException("Metadata analysis did not return document content.");
    }

    static void printMetadata(String fileName, DocumentContent document) {
        Map<String, String> metadata = document.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            throw new IllegalStateException(fileName + " analysis did not return document metadata.");
        }

        System.out.println("Metadata for " + fileName + ":");
        metadata.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        if (fileName.endsWith(".pdf") && !metadata.containsKey("createdAt")) {
            System.out.println("createdAt: (not returned)");
        }
    }
}
