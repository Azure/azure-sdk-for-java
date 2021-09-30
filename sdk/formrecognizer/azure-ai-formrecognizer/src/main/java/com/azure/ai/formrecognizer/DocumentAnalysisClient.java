// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.models.DocumentAnalysisException;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeResultOperation;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.azure.ai.formrecognizer.implementation.util.Utility.toFluxByteBuffer;

/**
 * This class provides a synchronous client that contains the operations that apply to Azure Form Recognizer.
 * Operations allowed by the client are analyzing information from documents and images using custom-built document
 * analysis models, prebuilt models for invoices, receipts, identity documents and business cards, and the layout model.
 *
 * <p><strong>Instantiating a synchronous Document Analysis Client</strong></p>
 * {@codesnippet com.azure.ai.formrecognizer.DocumentAnalysisClient.instantiation}
 *
 * @see DocumentAnalysisClientBuilder
 */
@ServiceClient(builder = DocumentAnalysisClientBuilder.class)
public final class DocumentAnalysisClient {
    private final DocumentAnalysisAsyncClient client;

    /**
     * Create a {@link DocumentAnalysisClient client} that sends requests to the Document Analysis service's endpoint.
     * Each service call goes through the {@link DocumentAnalysisClientBuilder#pipeline http pipeline}.
     *
     * @param client The {@link DocumentAnalysisClient} that the client routes its request through.
     */
    DocumentAnalysisClient(DocumentAnalysisAsyncClient client) {
        this.client = client;
    }

    /**
     * Analyzes data from documents with optical character recognition (OCR) and semantic values from a given document
     * using any of the prebuilt models or a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an error message
     * indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string}
     *
     * @param modelId The unique model ID to be used or the supported prebuilt models - "prebuilt-receipt",
     * "prebuilt-businessCard", "prebuilt-idDocument", "prebuilt-document", "prebuilt-invoice", "prebuilt-layout".
     * @param documentUrl The URL of the document to analyze.
     *
     * @return A {@link SyncPoller} to poll the progress of the analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentAnalysisException If analyze operation fails and the {@link AnalyzeResultOperation} returns
     * with an {@link OperationStatus#FAILED}..
     * @throws NullPointerException If {@code documentUrl} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocumentFromUrl(String modelId, String documentUrl) {
        return beginAnalyzeDocumentFromUrl(modelId, documentUrl, null, Context.NONE);
    }

    /**
     * Analyzes data from documents with optical character recognition (OCR) and semantic values from a given document
     * using any of the prebuilt models or a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string-AnalyzeDocumentOptions-Context}
     *
     * @param modelId The unique model ID to be used or the supported prebuilt models - "prebuilt-receipt",
     * "prebuilt-businessCard", "prebuilt-idDocument", "prebuilt-document", "prebuilt-invoice", "prebuilt-layout".
     * @param documentUrl The source URL to the input document.
     * @param analyzeDocumentOptions The additional configurable {@link AnalyzeDocumentOptions options} that may be
     * passed when analyzing documents.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} to poll the progress of the analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentAnalysisException If analyze operation fails and the {@link AnalyzeResultOperation}returns
     * with an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code documentUrl} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocumentFromUrl(String modelId, String documentUrl,
                                    AnalyzeDocumentOptions analyzeDocumentOptions, Context context) {
        return client.beginAnalyzeDocumentFromUrl(documentUrl, modelId,
            analyzeDocumentOptions, context).getSyncPoller();
    }

    /**
     * Analyzes data from documents using optical character recognition (OCR)  using any of the prebuilt models or
     * a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocument#string-InputStream-long}
     *
     * @param modelId The unique model ID to be used or the supported prebuilt models - "prebuilt-receipt",
     * "prebuilt-businessCard", "prebuilt-idDocument", "prebuilt-document", "prebuilt-invoice", "prebuilt-layout".
     * @param document The data of the document to analyze information from.
     * @param length The exact length of the data.
     *
     * @return A {@link SyncPoller} that polls the of progress of analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentAnalysisException If analyze operation fails and the {@link AnalyzeResultOperation}returns
     * with an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code document} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocument(String modelId, InputStream document, long length) {
        return beginAnalyzeDocument(modelId, document, length, null, Context.NONE);
    }

    /**
     * Analyzes data from documents with optical character recognition (OCR) and semantic values from a given document
     * using any of the prebuilt models or a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocument#string-InputStream-long-AnalyzeDocumentOptions-Context}
     *
     * @param modelId The unique model ID to be used or the supported prebuilt models - "prebuilt-receipt",
     * "prebuilt-businessCard", "prebuilt-idDocument", "prebuilt-document", "prebuilt-invoice", "prebuilt-layout".
     * @param document The data of the document to analyze information from.
     * @param length The exact length of the data.
     * @param analyzeDocumentOptions The additional configurable {@link AnalyzeDocumentOptions options} that may be
     * passed when analyzing documents.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the of progress of analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentAnalysisException If analyze operation fails and the {@link AnalyzeResultOperation} returns
     * with an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code document} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocument(String modelId, InputStream document, long length,
                             AnalyzeDocumentOptions analyzeDocumentOptions, Context context) {
        Flux<ByteBuffer> buffer = toFluxByteBuffer(document);
        return client.beginAnalyzeDocument(modelId, buffer, length,
            analyzeDocumentOptions, context).getSyncPoller();
    }
}
