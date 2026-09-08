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
import com.azure.ai.contentunderstanding.models.DocumentSignature;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;

/**
 * Demonstrates detecting signatures in a document image.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>Signature detection requires layout extraction. The {@code prebuilt-layout} analyzer used here enables that
 * capability. Each {@link DocumentSignature} has an ID and source expression; role and Markdown span information can
 * be absent when the service cannot determine them. When a span is available, the corresponding Markdown uses an
 * image reference such as <code>![recognized signature text](signatures/{id})</code>. The alt text is OCR output and can
 * be incomplete or contain extra characters.</p>
 *
 * <p>The bundled image is a synthetic training acknowledgment containing participant and approver signatures. Its
 * names and other details are fake data.</p>
 */
public class Sample_Advanced_DetectSignatures {
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
        BinaryData data = BinaryData.fromBytes(
            Files.readAllBytes(Paths.get("src/samples/resources/sample_signature.png")));
        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller
            = client.beginAnalyzeBinary("prebuilt-layout", data);
        AnalysisResult result = requireSuccessfulResult(poller.waitForCompletion().getStatus(), poller::getFinalResult,
            "Signature analysis");
        printSignatures(getDocumentContent(result));
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
        throw new IllegalStateException("Signature analysis did not return document content.");
    }

    static void printSignatures(DocumentContent document) {
        List<DocumentSignature> signatures = document.getSignatures();
        if (signatures == null || signatures.size() < 2) {
            throw new IllegalStateException("Signature analysis did not return at least two signatures.");
        }

        System.out.println("Found " + signatures.size() + " signature(s).");
        for (DocumentSignature signature : signatures) {
            if (signature == null || signature.getId() == null || signature.getId().trim().isEmpty()
                || signature.getSource() == null || signature.getSource().trim().isEmpty()) {
                throw new IllegalStateException("Signature analysis returned incomplete signature metadata.");
            }
            System.out.println("Signature ID: " + signature.getId());
            System.out.println("  Role: " + (signature.getRole() == null ? "(not available)" : signature.getRole()));
            System.out.println("  Source: " + signature.getSource());
            if (signature.getSpan() != null) {
                String markdown = document.getMarkdown();
                int offset = signature.getSpan().getOffset();
                int length = signature.getSpan().getLength();
                if (markdown == null || offset < 0 || length <= 0 || offset > markdown.length() - length) {
                    throw new IllegalStateException("Signature span falls outside the document Markdown.");
                }
                System.out.println("  Span: " + signature.getSpan().getOffset() + ", "
                    + signature.getSpan().getLength());
                String markdownFragment = markdown.substring(offset, offset + length);
                if (!markdownFragment.contains("(signatures/" + signature.getId() + ")")) {
                    throw new IllegalStateException("Signature Markdown does not reference its signature ID.");
                }
                System.out.println("  Markdown: " + markdownFragment);
            }
        }
    }
}
