// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis;

import com.azure.ai.formrecognizer.FormRecognizerClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationStatus;
import com.azure.ai.formrecognizer.documentanalysis.implementation.DocumentClassifiersImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.DocumentModelsImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.AnalyzeDocumentRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.AnalyzeResultOperation;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.ClassifyDocumentRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentClassifiersClassifyDocumentHeaders;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelsAnalyzeDocumentHeaders;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.ErrorResponseException;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.StringIndexType;
import com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Constants.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Transforms.getHttpResponseException;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Utility.enableSyncRestProxy;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Utility.getAnalyzeDocumentOptions;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Utility.getTracingContext;

/**
 * <p>This class provides a synchronous client to connect to the Form Recognizer Azure Cognitive Service.</p>
 * <p>This client provides synchronous methods to perform:</p>
 *
 * <ol>
 *     <li>Custom Document Analysis: Classification, extraction and analysis of data from forms and documents specific
 *     to distinct business data and use cases. Use the custom trained model by passing its modelId into the
 *     {@link #beginAnalyzeDocument(String, BinaryData)} method.</li>
 *     <li>General Document Analysis: Extract text, tables, structure, and key-value pairs. Use general document model
 *     provided by the Form Recognizer service by passing modelId="rebuilt-document" into the
 *     {@link #beginAnalyzeDocument(String, BinaryData)} method.</li>
 *     <li>Prebuilt Model Analysis: Analyze receipts, business cards, invoices, ID's, W2's and other documents with
 *     <a href="https://aka.ms/azsdk/formrecognizer/models">supported prebuilt models. Use the prebuilt receipt model
 *     provided by passing modelId="prebuilt-receipt" into the {@link #beginAnalyzeDocument(String, BinaryData)}
 *     method.</a></li>
 *     <li>Layout Analysis: Extract text, selection marks, and tables structures, along with their bounding box
 *     coordinates, from forms and documents. Use the layout analysis model provided the service by passing
 *     modelId="prebuilt-layout" into the {@link #beginAnalyzeDocument(String, BinaryData)} method.</li>
 *     <li>Polling and Callbacks: It includes mechanisms for polling the service to check the status of an analysis
 *     operation or registering callbacks to receive notifications when the analysis is complete.</li>
 * </ol>
 *
 * <p>This client also provides different methods based on inputs from a URL and inputs from a stream.</p>
 *
 * <p><strong>Note: </strong>This client only supports {@link DocumentAnalysisServiceVersion#V2022_08_31} and newer.
 * To use an older service version, {@link FormRecognizerClient} and {@link FormTrainingClient}.</p>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Form Recognizer.
 * {@link DocumentAnalysisClient} is the synchronous service client and {@link DocumentAnalysisAsyncClient} is the
 * asynchronous service client.
 * The examples shown in this document use a credential object named DefaultAzureCredential for authentication, which is
 * appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a DocumentAnalysisAsyncClient with DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient}, using
 * the `DefaultAzureCredentialBuilder` to configure it.</p>
 *
 * <!-- src_embed readme-sample-createDocumentAnalysisClientWithAAD -->
 * <pre>
 * DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createDocumentAnalysisClientWithAAD  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.core.credential.AzureKeyCredential AzureKeyCredential} for client creation.</p>
 *
 * <!-- src_embed readme-sample-createDocumentAnalysisClient -->
 * <pre>
 * DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createDocumentAnalysisClient  -->
 *
 * @see com.azure.ai.formrecognizer.documentanalysis
 * @see DocumentAnalysisClientBuilder
 * @see DocumentAnalysisAsyncClient
 */
@ServiceClient(builder = DocumentAnalysisClientBuilder.class)
public final class DocumentAnalysisClient {
    private static final ClientLogger LOGGER = new ClientLogger(DocumentAnalysisClient.class);
    private final DocumentModelsImpl documentModelsImpl;
    private final DocumentClassifiersImpl documentClassifiersImpl;

    /**
     * Create a {@link DocumentAnalysisClient client} that sends requests to the Document Analysis service's endpoint.
     * Each service call goes through the {@link DocumentAnalysisClientBuilder#pipeline http pipeline}.
     *
     * @param formRecognizerClientImpl The proxy service used to perform REST calls.
     */
    DocumentAnalysisClient(FormRecognizerClientImpl formRecognizerClientImpl) {
        this.documentModelsImpl = formRecognizerClientImpl.getDocumentModels();
        this.documentClassifiersImpl = formRecognizerClientImpl.getDocumentClassifiers();
    }
    /**
     * Analyzes data from documents with optical character recognition (OCR) and semantic values from a given document
     * using any of the prebuilt models or a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an error message
     * indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <p> Analyze a document using the URL of the document. </p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string -->
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
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string -->
     *
     * @param modelId The unique model ID to be used. Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param documentUrl The URL of the document to analyze.
     *
     * @return A {@link SyncPoller} to poll the progress of the analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws HttpResponseException If analyze operation fails and returns with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code documentUrl} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, AnalyzeResult> beginAnalyzeDocumentFromUrl(String modelId, String documentUrl) {
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
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string -->
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
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string -->
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
     * @throws HttpResponseException If analyze operation fails and returns with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code documentUrl} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, AnalyzeResult> beginAnalyzeDocumentFromUrl(String modelId, String documentUrl,
        AnalyzeDocumentOptions analyzeDocumentOptions, Context context) {
        return beginAnalyzeDocumentFromUrlSync(documentUrl, modelId, analyzeDocumentOptions, context);
    }

    private SyncPoller<OperationResult, AnalyzeResult> beginAnalyzeDocumentFromUrlSync(String documentUrl,
        String modelId, AnalyzeDocumentOptions analyzeDocumentOptions, Context context) {
        if (CoreUtils.isNullOrEmpty(documentUrl)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'documentUrl' is required and cannot"
                + " be null or empty"));
        }
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                + " be null or empty"));
        }
        final AnalyzeDocumentOptions finalAnalyzeDocumentOptions = getAnalyzeDocumentOptions(analyzeDocumentOptions);
        Context finalContext = enableSyncRestProxy(getTracingContext(context));
        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, analyzeActivationOperation(modelId,
                finalAnalyzeDocumentOptions.getPages(), finalAnalyzeDocumentOptions.getLocale(),
                finalAnalyzeDocumentOptions.getDocumentAnalysisFeatures(), null, documentUrl, finalContext).apply(cxt)),
            pollingOperation(modelId, finalContext), getCancellationIsNotSupported(),
            fetchingOperation(modelId, finalContext));
    }

    /**
     * Analyzes data from documents using optical character recognition (OCR)  using any of the prebuilt models or
     * a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData -->
     * <pre>
     *     File document = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     *     String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *     byte[] fileContent = Files.readAllBytes&#40;document.toPath&#40;&#41;&#41;;
     *
     *     documentAnalysisClient.beginAnalyzeDocument&#40;modelId, BinaryData.fromBytes&#40;fileContent&#41;&#41;
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
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData -->
     *
     * @param modelId The unique model ID to be used. Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param document The data of the document to analyze information from.
     * @return A {@link SyncPoller} that polls the of progress of analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws HttpResponseException If analyze operation fails and returns with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code document} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, AnalyzeResult> beginAnalyzeDocument(String modelId, BinaryData document) {
        return beginAnalyzeDocument(modelId, document, null, Context.NONE);
    }

    /**
     * Analyzes data from documents with optical character recognition (OCR) and semantic values from a given document
     * using any of the prebuilt models or a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <p> Analyze a document with configurable options. </p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData-Options-Context -->
     * <pre>
     * File document = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     * byte[] fileContent = Files.readAllBytes&#40;document.toPath&#40;&#41;&#41;;
     *
     * documentAnalysisClient.beginAnalyzeDocument&#40;modelId, BinaryData.fromBytes&#40;fileContent&#41;,
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
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData-Options-Context -->
     *
     * @param modelId The unique model ID to be used. Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param document The data of the document to analyze information from.
     * @param analyzeDocumentOptions The additional configurable {@link AnalyzeDocumentOptions options} that may be
     * passed when analyzing documents.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link SyncPoller} that polls the of progress of analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws HttpResponseException If analyze operation fails and returns with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code document} or {@code modelId} is null.
     * @throws IllegalArgumentException If {@code document} length is null or unspecified.
     * Use {@link BinaryData#fromStream(InputStream, Long)} to create an instance of the {@code document}
     * from given {@link InputStream} with length.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, AnalyzeResult> beginAnalyzeDocument(String modelId, BinaryData document,
        AnalyzeDocumentOptions analyzeDocumentOptions, Context context) {
        Objects.requireNonNull(document, "'document' is required and cannot be null.");
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                + " be null or empty"));
        }

        if (document.getLength() == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'document length' is required and cannot"
                + " be null"));
        }

        final AnalyzeDocumentOptions finalAnalyzeDocumentOptions = getAnalyzeDocumentOptions(analyzeDocumentOptions);
        Context finalContext = enableSyncRestProxy(getTracingContext(context));
        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, analyzeActivationOperation(modelId,
                finalAnalyzeDocumentOptions.getPages(), finalAnalyzeDocumentOptions.getLocale(),
                finalAnalyzeDocumentOptions.getDocumentAnalysisFeatures(), document, null, finalContext).apply(cxt)),
            pollingOperation(modelId, finalContext), getCancellationIsNotSupported(),
            fetchingOperation(modelId, finalContext));
    }

    /**
     * Classify a given document using a document classifier.
     * For more information on how to build a custom classifier model,
     * see <a href="https://aka.ms/azsdk/formrecognizer/buildclassifiermodel"></a>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <p> Analyze a document using the URL of the document with configurable options. </p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocumentFromUrl#string-string -->
     * <pre>
     * String documentUrl = &quot;&#123;file_source_url&#125;&quot;;
     * String classifierId = &quot;&#123;custom_trained_classifier_id&#125;&quot;;
     *
     * documentAnalysisClient.beginClassifyDocumentFromUrl&#40;classifierId, documentUrl&#41;
     *     .getFinalResult&#40;&#41;
     *     .getDocuments&#40;&#41;
     *     .forEach&#40;analyzedDocument -&gt; System.out.printf&#40;&quot;Doc Type: %s%n&quot;, analyzedDocument.getDocType&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocumentFromUrl#string-string -->
     *
     * @param classifierId The unique classifier ID to be used. Use this to specify the custom classifier ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param documentUrl The source URL to the input document.
     *
     * @return A {@link SyncPoller} to poll the progress of the analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws HttpResponseException If analyze operation fails and returns with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code documentUrl} or {@code classifierId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, AnalyzeResult> beginClassifyDocumentFromUrl(String classifierId,
        String documentUrl) {
        return beginClassifyDocumentFromUrl(documentUrl, classifierId, Context.NONE);
    }

    /**
     * Classify a given document using a document classifier.
     * For more information on how to build a custom classifier model,
     * see <a href="https://aka.ms/azsdk/formrecognizer/buildclassifiermodel"></a>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocument#string-BinaryData -->
     * <pre>
     * File document = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * String classifierId = &quot;&#123;custom_trained_classifier_id&#125;&quot;;
     * byte[] fileContent = Files.readAllBytes&#40;document.toPath&#40;&#41;&#41;;
     *
     * documentAnalysisClient.beginClassifyDocument&#40;classifierId, BinaryData.fromBytes&#40;fileContent&#41;&#41;
     *     .getFinalResult&#40;&#41;
     *     .getDocuments&#40;&#41;
     *     .forEach&#40;analyzedDocument -&gt; System.out.printf&#40;&quot;Doc Type: %s%n&quot;, analyzedDocument.getDocType&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocument#string-BinaryData -->
     *
     * @param classifierId The unique classifier ID to be used. Use this to specify the custom classifier ID.
     * @param document The data of the document to analyze information from.
     * @return A {@link SyncPoller} that polls the of progress of analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws HttpResponseException If analyze operation fails and returns with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code document} or {@code classifierId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, AnalyzeResult> beginClassifyDocument(String classifierId, BinaryData document) {
        return beginClassifyDocument(classifierId, document, Context.NONE);
    }

    /**
     * Classify a given document using a document classifier.
     * For more information on how to build a custom classifier model,
     * see <a href="https://aka.ms/azsdk/formrecognizer/buildclassifiermodel"></a>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <p> Analyze a document using the URL of the document with configurable options. </p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocumentFromUrl#string-string-Context -->
     * <pre>
     * String documentUrl = &quot;&#123;file_source_url&#125;&quot;;
     * String classifierId = &quot;&#123;custom_trained_classifier_id&#125;&quot;;
     *
     * documentAnalysisClient.beginClassifyDocumentFromUrl&#40;classifierId, documentUrl, Context.NONE&#41;
     *     .getFinalResult&#40;&#41;
     *     .getDocuments&#40;&#41;
     *     .forEach&#40;analyzedDocument -&gt; System.out.printf&#40;&quot;Doc Type: %s%n&quot;, analyzedDocument.getDocType&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocumentFromUrl#string-string-Context -->
     *
     * @param classifierId The unique classifier ID to be used. Use this to specify the custom classifier ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param documentUrl The source URL to the input document.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} to poll the progress of the analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws HttpResponseException If analyze operation fails and returns with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code documentUrl} or {@code classifierId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, AnalyzeResult> beginClassifyDocumentFromUrl(String classifierId,
        String documentUrl, Context context) {
        return beginClassifyDocumentFromUrlSync(documentUrl, classifierId, context);
    }
    /**
     * Classify a given document using a document classifier.
     * For more information on how to build a custom classifier model,
     * see <a href="https://aka.ms/azsdk/formrecognizer/buildclassifiermodel"></a>
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocument#string-BinaryData-Context -->
     * <pre>
     * File document = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * String classifierId = &quot;&#123;custom_trained_classifier_id&#125;&quot;;
     * byte[] fileContent = Files.readAllBytes&#40;document.toPath&#40;&#41;&#41;;
     *
     * documentAnalysisClient.beginClassifyDocument&#40;classifierId, BinaryData.fromBytes&#40;fileContent&#41;, Context.NONE&#41;
     *     .getFinalResult&#40;&#41;
     *     .getDocuments&#40;&#41;
     *     .forEach&#40;analyzedDocument -&gt; System.out.printf&#40;&quot;Doc Type: %s%n&quot;, analyzedDocument.getDocType&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocument#string-BinaryData-Context -->
     *
     * @param classifierId The unique classifier ID to be used. Use this to specify the custom classifier ID.
     * @param document The data of the document to analyze information from.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link SyncPoller} that polls the of progress of analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws HttpResponseException If analyze operation fails and returns with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code document} or {@code classifierId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<OperationResult, AnalyzeResult> beginClassifyDocument(String classifierId, BinaryData document,
        Context context) {
        Objects.requireNonNull(document, "'document' is required and cannot be null.");
        if (CoreUtils.isNullOrEmpty(classifierId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'classifierId' is required and cannot"
                + " be null or empty"));
        }

        if (document.getLength() == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'document length' is required and cannot"
                + " be null"));
        }

        Context finalContext = enableSyncRestProxy(getTracingContext(context));
        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, classifyActivationOperation(classifierId,
                document, null, finalContext).apply(cxt)),
            pollingClassifierOperation(classifierId, finalContext), getCancellationIsNotSupported(),
            fetchingClassifierOperation(classifierId, finalContext));
    }

    private SyncPoller<OperationResult, AnalyzeResult> beginClassifyDocumentFromUrlSync(String documentUrl,
        String classifierId, Context context) {
        if (CoreUtils.isNullOrEmpty(documentUrl)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'documentUrl' is required and cannot"
                + " be null or empty"));
        }
        if (CoreUtils.isNullOrEmpty(classifierId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'classifierId' is required and cannot"
                + " be null or empty"));
        }
        Context finalContext = enableSyncRestProxy(getTracingContext(context));
        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, classifyActivationOperation(classifierId,
                null, documentUrl, finalContext).apply(cxt)),
            pollingClassifierOperation(classifierId, finalContext), getCancellationIsNotSupported(),
            fetchingClassifierOperation(classifierId, finalContext));
    }

    private Function<PollingContext<OperationResult>, OperationResult> analyzeActivationOperation(
        String modelId, List<String> pages, String locale,
        List<com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnalysisFeature> features, BinaryData document,
        String documentUrl, Context context) {
        return (pollingContext) ->
            Transforms.toDocumentOperationResult(analyzeDocument(modelId,
                CoreUtils.isNullOrEmpty(pages) ? null : String.join(",", pages), locale, features, document,
                documentUrl, context)
                .getDeserializedHeaders().getOperationLocation());
    }

    private Function<PollingContext<OperationResult>, OperationResult> classifyActivationOperation(String classifierId,
        BinaryData document, String documentUrl, Context context) {
        ResponseBase<DocumentClassifiersClassifyDocumentHeaders, Void> response;
        try {
            if (documentUrl != null) {
                response = documentClassifiersImpl.classifyDocumentWithResponse(classifierId,
                    StringIndexType.UTF16CODE_UNIT, new ClassifyDocumentRequest().setUrlSource(documentUrl), context);
            } else {
                response = documentClassifiersImpl.classifyDocumentWithResponse(classifierId, null,
                    StringIndexType.UTF16CODE_UNIT, document, document.getLength(), context);
            }
            return (pollingContext) ->
                Transforms.toDocumentOperationResult(response.getDeserializedHeaders().getOperationLocation());
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    private ResponseBase<DocumentModelsAnalyzeDocumentHeaders, Void> analyzeDocument(String modelId, String pages,
        String locale, List<com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnalysisFeature> features,
        BinaryData document, String documentUrl, Context context) {
        try {
            if (documentUrl == null) {
                return documentModelsImpl.analyzeDocumentWithResponse(modelId, null, pages, locale,
                    StringIndexType.UTF16CODE_UNIT, features, document, document.getLength(), context);
            } else {
                return documentModelsImpl.analyzeDocumentWithResponse(modelId, pages, locale,
                    StringIndexType.UTF16CODE_UNIT, features, new AnalyzeDocumentRequest().setUrlSource(documentUrl),
                    context);
            }
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(getHttpResponseException(ex));
        }
    }

    private BiFunction<PollingContext<OperationResult>, PollResponse<OperationResult>, OperationResult>
        getCancellationIsNotSupported() {
        return (pollingContext, activationResponse) -> {
            throw LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"));
        };
    }

    private Function<PollingContext<OperationResult>, PollResponse<OperationResult>> pollingOperation(
        String modelId, Context finalContext) {
        return pollingContext -> {
            final PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
            final String resultId = operationResultPollResponse.getValue().getOperationId();
            Response<AnalyzeResultOperation> modelResponse;
            try {
                modelResponse = documentModelsImpl.getAnalyzeResultWithResponse(modelId, resultId, finalContext);
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(Transforms.getHttpResponseException(ex));
            }
            return processAnalyzeModelResponse(modelResponse, operationResultPollResponse);
        };
    }

    private PollResponse<OperationResult> processAnalyzeModelResponse(
        Response<AnalyzeResultOperation> analyzeResultOperationResponse,
        PollResponse<OperationResult> operationResultPollResponse) {
        LongRunningOperationStatus status;
        switch (analyzeResultOperationResponse.getValue().getStatus()) {
            case NOT_STARTED:
            case RUNNING:
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            case SUCCEEDED:
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case FAILED:
                throw LOGGER.logExceptionAsError(Transforms
                    .mapResponseErrorToHttpResponseException(analyzeResultOperationResponse.getValue().getError()));
            default:
                status = LongRunningOperationStatus.fromString(
                    analyzeResultOperationResponse.getValue().getStatus().toString(), true);
                break;
        }
        return new PollResponse<>(status, operationResultPollResponse.getValue());
    }

    private Function<PollingContext<OperationResult>, AnalyzeResult> fetchingOperation(String modelId,
        Context finalContext) {
        return pollingContext -> {
            final String resultId = pollingContext.getLatestResponse().getValue().getOperationId();
            try {
                return Transforms.toAnalyzeResultOperation(documentModelsImpl.getAnalyzeResultWithResponse(modelId,
                    resultId, finalContext).getValue().getAnalyzeResult());
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(Transforms.getHttpResponseException(ex));
            }
        };
    }

    private Function<PollingContext<OperationResult>, PollResponse<OperationResult>> pollingClassifierOperation(
        String classifierId, Context finalContext) {
        return pollingContext -> {
            final PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
            final String resultId = operationResultPollResponse.getValue().getOperationId();
            Response<AnalyzeResultOperation> analyzeResultOperationResponse;
            try {
                analyzeResultOperationResponse = documentClassifiersImpl.getClassifyResultWithResponse(classifierId,
                    resultId, finalContext);
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(Transforms.getHttpResponseException(ex));
            }
            return processAnalyzeModelResponse(analyzeResultOperationResponse, operationResultPollResponse);
        };
    }

    private Function<PollingContext<OperationResult>, AnalyzeResult> fetchingClassifierOperation(String classifierId,
        Context finalContext) {
        return pollingContext -> {
            final String resultId = pollingContext.getLatestResponse().getValue().getOperationId();
            try {
                return Transforms.toAnalyzeResultOperation(documentClassifiersImpl.getClassifyResultWithResponse(
                    classifierId, resultId, finalContext).getValue().getAnalyzeResult());
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(Transforms.getHttpResponseException(ex));
            }
        };
    }
}
