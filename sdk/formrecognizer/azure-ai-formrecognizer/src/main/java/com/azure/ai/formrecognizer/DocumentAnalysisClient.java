// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.AnalyzeResultOperation;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.DocumentModelOperationException;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * This class provides a synchronous client that contains the operations that apply to Azure Form Recognizer.
 * Operations allowed by the client are analyzing information from documents and images using custom-built document
 * analysis models, prebuilt models for invoices, receipts, identity documents and business cards, and the layout model.
 *
 * <p><strong>Instantiating an asynchronous Document Analysis Client</strong></p>
 *
 * <!-- src_embed com.azure.ai.formrecognizer.DocumentAnalysisClient.instantiation -->
 * <pre>
 * DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.formrecognizer.DocumentAnalysisClient.instantiation -->
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
     * <p> Analyze a document using the URL of the document. </p>
     * <!-- src_embed com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string -->
     * <pre>
     * String documentUrl = &quot;&#123;document_url&#125;&quot;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *
     * documentAnalysisClient.beginAnalyzeDocumentFromUrl&#40;modelId, documentUrl&#41;.getFinalResult&#40;&#41;
     *     .getDocuments&#40;&#41;.stream&#40;&#41;
     *     .map&#40;AnalyzedDocument::getFields&#41;
     *     .forEach&#40;documentFieldMap -&gt; documentFieldMap.forEach&#40;&#40;key, documentField&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field text: %s%n&quot;, key&#41;;
     *         System.out.printf&#40;&quot;Field value data content: %s%n&quot;, documentField.getContent&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, documentField.getConfidence&#40;&#41;&#41;;
     *     &#125;&#41;&#41;;
     *
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string -->
     *
     * @param modelId The unique model ID to be used. Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param documentUrl The URL of the document to analyze.
     *
     * @return A {@link SyncPoller} to poll the progress of the analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentModelOperationException If analyze operation fails and the {@link AnalyzeResultOperation} returns
     * with an {@link OperationStatus#FAILED}..
     * @throws IllegalArgumentException If {@code documentUrl} or {@code modelId} is null.
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
     * <p> Analyze a document using the URL of the document with configurable options. </p>
     * <!-- src_embed com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string -->
     * <pre>
     * String documentUrl = &quot;&#123;document_url&#125;&quot;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *
     * documentAnalysisClient.beginAnalyzeDocumentFromUrl&#40;modelId, documentUrl&#41;.getFinalResult&#40;&#41;
     *     .getDocuments&#40;&#41;.stream&#40;&#41;
     *     .map&#40;AnalyzedDocument::getFields&#41;
     *     .forEach&#40;documentFieldMap -&gt; documentFieldMap.forEach&#40;&#40;key, documentField&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field text: %s%n&quot;, key&#41;;
     *         System.out.printf&#40;&quot;Field value data content: %s%n&quot;, documentField.getContent&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, documentField.getConfidence&#40;&#41;&#41;;
     *     &#125;&#41;&#41;;
     *
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string -->
     *
     * @param modelId The unique model ID to be used.  Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param documentUrl The source URL to the input document.
     * @param analyzeDocumentOptions The additional configurable {@link AnalyzeDocumentOptions options} that may be
     * passed when analyzing documents.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} to poll the progress of the analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentModelOperationException If analyze operation fails and the {@link AnalyzeResultOperation} returns
     * with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code documentUrl} or {@code modelId} is null.
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
     * <!-- src_embed com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData-long -->
     * <pre>
     *     File document = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     *     String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *     byte[] fileContent = Files.readAllBytes&#40;document.toPath&#40;&#41;&#41;;
     *
     *     documentAnalysisClient.beginAnalyzeDocument&#40;modelId, BinaryData.fromBytes&#40;fileContent&#41;, document.length&#40;&#41;&#41;
     *         .getFinalResult&#40;&#41;
     *         .getDocuments&#40;&#41;.stream&#40;&#41;
     *         .map&#40;AnalyzedDocument::getFields&#41;
     *         .forEach&#40;documentFieldMap -&gt; documentFieldMap.forEach&#40;&#40;key, documentField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, key&#41;;
     *             System.out.printf&#40;&quot;Field value data content: %s%n&quot;, documentField.getContent&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, documentField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData-long -->
     *
     * @param modelId The unique model ID to be used. Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param document The data of the document to analyze information from.
     * @param length The exact length of the data.
     *
     * @return A {@link SyncPoller} that polls the of progress of analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentModelOperationException If analyze operation fails and the {@link AnalyzeResultOperation}returns
     * with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code document} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocument(String modelId, BinaryData document, long length) {
        return beginAnalyzeDocument(modelId, document, length, null, Context.NONE);
    }

    /**
     * Analyzes data from documents with optical character recognition (OCR) and semantic values from a given document
     * using any of the prebuilt models or a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <p> Analyze a document with configurable options. </p>
     * <!-- src_embed com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData-long-AnalyzeDocumentOptions-Context -->
     * <pre>
     * File document = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     * byte[] fileContent = Files.readAllBytes&#40;document.toPath&#40;&#41;&#41;;
     *
     * documentAnalysisClient.beginAnalyzeDocument&#40;modelId, BinaryData.fromBytes&#40;fileContent&#41;, document.length&#40;&#41;,
     *         new AnalyzeDocumentOptions&#40;&#41;.setPages&#40;Arrays.asList&#40;&quot;1&quot;, &quot;3&quot;&#41;&#41;, Context.NONE&#41;
     *     .getFinalResult&#40;&#41;
     *     .getDocuments&#40;&#41;.stream&#40;&#41;
     *     .map&#40;AnalyzedDocument::getFields&#41;
     *     .forEach&#40;documentFieldMap -&gt; documentFieldMap.forEach&#40;&#40;key, documentField&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field text: %s%n&quot;, key&#41;;
     *         System.out.printf&#40;&quot;Field value data content: %s%n&quot;, documentField.getContent&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, documentField.getConfidence&#40;&#41;&#41;;
     *     &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData-long-AnalyzeDocumentOptions-Context -->
     *
     * @param modelId The unique model ID to be used. Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param document The data of the document to analyze information from.
     * @param length The exact length of the data.
     * @param analyzeDocumentOptions The additional configurable {@link AnalyzeDocumentOptions options} that may be
     * passed when analyzing documents.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the of progress of analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentModelOperationException If analyze operation fails and the {@link AnalyzeResultOperation} returns
     * with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code document} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocument(String modelId, BinaryData document, long length,
                             AnalyzeDocumentOptions analyzeDocumentOptions, Context context) {
        Flux<ByteBuffer> buffer = document.toFluxByteBuffer();
        return client.beginAnalyzeDocument(modelId, buffer, length,
            analyzeDocumentOptions, context).getSyncPoller();
    }
}
