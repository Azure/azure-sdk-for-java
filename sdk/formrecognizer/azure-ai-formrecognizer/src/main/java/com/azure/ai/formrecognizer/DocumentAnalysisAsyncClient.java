// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeDocumentRequest;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeResultOperation;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.implementation.models.StringIndexType;
import com.azure.ai.formrecognizer.implementation.util.Transforms;
import com.azure.ai.formrecognizer.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.DocumentModelOperationException;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.ai.formrecognizer.implementation.util.Constants.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.formrecognizer.implementation.util.Utility.activationOperation;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * This class provides an asynchronous client that contains the operations that apply to Azure Form Recognizer.
 * Operations allowed by the client are analyzing information from documents and images using custom-built document
 * analysis models, prebuilt models for invoices, receipts, identity documents and business cards, and the layout model.
 *
 * <p><strong>Instantiating an asynchronous Document Analysis Client</strong></p>
 *
 * <!-- src_embed com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.instantiation -->
 * <pre>
 * DocumentAnalysisAsyncClient documentAnalysisAsyncClient = new DocumentAnalysisClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.instantiation -->
 *
 * @see DocumentAnalysisClientBuilder
 */
@ServiceClient(builder = DocumentAnalysisClientBuilder.class, isAsync = true)
public final class DocumentAnalysisAsyncClient {
    private final ClientLogger logger = new ClientLogger(DocumentAnalysisAsyncClient.class);
    private final FormRecognizerClientImpl service;
    private final DocumentAnalysisServiceVersion serviceVersion;

    /**
     * Create a {@link DocumentAnalysisAsyncClient} that sends requests to the Form recognizer service's endpoint. Each
     * service call goes through the {@link DocumentAnalysisClientBuilder#pipeline(HttpPipeline)} http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Form Recognizer service supported by this client library.
     */
    DocumentAnalysisAsyncClient(FormRecognizerClientImpl service, DocumentAnalysisServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Analyzes data from documents with optical character recognition (OCR) and semantic values from a given document
     * using any of the prebuilt models or a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <p> Analyze a document using the URL of the document. </p>
     * <!-- src_embed com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl#string-string -->
     * <pre>
     * String documentUrl = &quot;&#123;document_url&#125;&quot;;
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * documentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl&#40;modelId, documentUrl&#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;analyzeResult -&gt;
     *         analyzeResult.getDocuments&#40;&#41;
     *             .stream&#40;&#41;
     *             .forEach&#40;document -&gt;
     *                 document.getFields&#40;&#41;
     *                     .forEach&#40;&#40;key, documentField&#41; -&gt; &#123;
     *                         System.out.printf&#40;&quot;Field text: %s%n&quot;, key&#41;;
     *                         System.out.printf&#40;&quot;Field value data content: %s%n&quot;, documentField.getContent&#40;&#41;&#41;;
     *                         System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, documentField.getConfidence&#40;&#41;&#41;;
     *                     &#125;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl#string-string -->
     *
     * @param modelId The unique model ID to be used. Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param documentUrl The URL of the document to analyze.
     *
     * @return A {@link PollerFlux} that polls the progress of the analyze document operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentModelOperationException If analyze operation fails and the {@link AnalyzeResultOperation} returns
     * with an {@link OperationStatus#FAILED}..
     * @throws IllegalArgumentException If {@code documentUrl} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocumentFromUrl(String modelId, String documentUrl) {
        return beginAnalyzeDocumentFromUrl(modelId, documentUrl, null);
    }

    /**
     * Analyzes data from documents with optical character recognition (OCR) and semantic values from a given document
     * using any of the prebuilt models or a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <p> Analyze a document using the URL of the document with configurable options. </p>
     * <!-- src_embed com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl#string-string-AnalyzeDocumentOptions -->
     * <pre>
     * String documentUrl = &quot;&#123;document_url&#125;&quot;;
     * &#47;&#47; analyze a receipt using prebuilt model
     * String modelId = &quot;prebuilt-receipt&quot;;
     *
     * documentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl&#40;modelId, documentUrl,
     *         new AnalyzeDocumentOptions&#40;&#41;.setPages&#40;Arrays.asList&#40;&quot;1&quot;, &quot;3&quot;&#41;&#41;&#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;analyzeResult -&gt; &#123;
     *         System.out.println&#40;analyzeResult.getModelId&#40;&#41;&#41;;
     *         analyzeResult.getDocuments&#40;&#41;
     *             .stream&#40;&#41;
     *             .forEach&#40;document -&gt;
     *                 document.getFields&#40;&#41;
     *                     .forEach&#40;&#40;key, documentField&#41; -&gt; &#123;
     *                         System.out.printf&#40;&quot;Field text: %s%n&quot;, key&#41;;
     *                         System.out.printf&#40;&quot;Field value data content: %s%n&quot;, documentField.getContent&#40;&#41;&#41;;
     *                         System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, documentField.getConfidence&#40;&#41;&#41;;
     *                     &#125;&#41;&#41;;
     *     &#125;&#41;;
     *
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl#string-string-AnalyzeDocumentOptions -->
     *
     * @param modelId The unique model ID to be used. Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param documentUrl The source URL to the input form.
     * @param analyzeDocumentOptions The additional configurable {@link AnalyzeDocumentOptions options}
     * that may be passed when analyzing documents.
     * @return A {@link PollerFlux} that polls progress of the analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentModelOperationException If analyze operation fails and the {@link AnalyzeResultOperation} returns
     * with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code documentUrl} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocumentFromUrl(String modelId, String documentUrl,
                                   AnalyzeDocumentOptions analyzeDocumentOptions) {
        return beginAnalyzeDocumentFromUrl(documentUrl, modelId, analyzeDocumentOptions, Context.NONE);
    }

    PollerFlux<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocumentFromUrl(String documentUrl, String modelId,
                                   AnalyzeDocumentOptions analyzeDocumentOptions,
                                   Context context) {
        try {
            if (CoreUtils.isNullOrEmpty(documentUrl)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("'documentUrl' is required and cannot"
                    + " be null or empty"));
            }
            if (CoreUtils.isNullOrEmpty(modelId)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                    + " be null or empty"));
            }
            final AnalyzeDocumentOptions finalAnalyzeDocumentOptions
                = getAnalyzeDocumentOptions(analyzeDocumentOptions);
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(() ->
                        service.analyzeDocumentWithResponseAsync(modelId,
                                CoreUtils.isNullOrEmpty(finalAnalyzeDocumentOptions.getPages())
                                    ? null : String.join(",", finalAnalyzeDocumentOptions.getPages()),
                                finalAnalyzeDocumentOptions.getLocale() == null ? null
                                    : finalAnalyzeDocumentOptions.getLocale(),
                                StringIndexType.UTF16CODE_UNIT,
                                new AnalyzeDocumentRequest().setUrlSource(documentUrl),
                                context)
                            .map(analyzeDocumentResponse ->
                                Transforms.toDocumentOperationResult(
                                    analyzeDocumentResponse.getDeserializedHeaders().getOperationLocation())),
                    logger),
                pollingOperation(resultId ->
                    service.getAnalyzeDocumentResultWithResponseAsync(modelId, resultId, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId ->
                    service.getAnalyzeDocumentResultWithResponseAsync(
                        modelId,
                        resultId,
                        context))
                    .andThen(after -> after
                        .map(modelSimpleResponse ->
                            Transforms.toAnalyzeResultOperation(modelSimpleResponse.getValue().getAnalyzeResult()))
                        .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Analyzes data from documents with optical character recognition (OCR) and semantic values from a given document
     * using any of the prebuilt models or a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * <p>
     * Note that the {@code data} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * <p> Analyze a document. </p>
     * <!-- src_embed com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocument#string-Flux-long -->
     * <pre>
     * File document = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * Flux&lt;ByteBuffer&gt; buffer =
     *     Utility.toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;document.toPath&#40;&#41;&#41;&#41;&#41;;
     *
     * documentAnalysisAsyncClient.beginAnalyzeDocument&#40;modelId, buffer, document.length&#40;&#41;&#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;analyzeResult -&gt;
     *         analyzeResult.getDocuments&#40;&#41;
     *             .stream&#40;&#41;
     *             .forEach&#40;analyzedDocument -&gt;
     *                 analyzedDocument.getFields&#40;&#41;
     *                     .forEach&#40;&#40;key, documentField&#41; -&gt; &#123;
     *                         System.out.printf&#40;&quot;Field text: %s%n&quot;, key&#41;;
     *                         System.out.printf&#40;&quot;Field value data content: %s%n&quot;, documentField.getContent&#40;&#41;&#41;;
     *                         System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, documentField.getConfidence&#40;&#41;&#41;;
     *                     &#125;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocument#string-Flux-long -->
     *
     * @param modelId The unique model ID to be used. Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param document The data of the document to analyze information from.
     * @param length The exact length of the data.
     *
     * @return A {@link PollerFlux} that polls the progress of the analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentModelOperationException If analyze operation fails and the {@link AnalyzeResultOperation} returns
     * with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code document} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocument(String modelId, Flux<ByteBuffer> document, long length) {
        return beginAnalyzeDocument(modelId, document, length, null);
    }

    /**
     * Analyzes data from documents with optical character recognition (OCR) and semantic values from a given document
     * using any of the prebuilt models or a custom-built analysis model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * <p>
     * Note that the {@code data} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * <p> Analyze a document with configurable options. . </p>
     * <!-- src_embed com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocument#string-Flux-long-AnalyzeDocumentOptions -->
     * <pre>
     * File document = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     *
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * Flux&lt;ByteBuffer&gt; buffer =
     *     Utility.toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;document.toPath&#40;&#41;&#41;&#41;&#41;;
     *
     * documentAnalysisAsyncClient.beginAnalyzeDocument&#40;modelId, buffer, document.length&#40;&#41;,
     *         new AnalyzeDocumentOptions&#40;&#41;.setPages&#40;Arrays.asList&#40;&quot;1&quot;, &quot;3&quot;&#41;&#41;&#41;
     *     &#47;&#47; if polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;analyzeResult -&gt; &#123;
     *         System.out.println&#40;analyzeResult.getModelId&#40;&#41;&#41;;
     *         analyzeResult.getDocuments&#40;&#41;
     *             .stream&#40;&#41;
     *             .forEach&#40;analyzedDocument -&gt;
     *                 analyzedDocument.getFields&#40;&#41;
     *                     .forEach&#40;&#40;key, documentField&#41; -&gt; &#123;
     *                         System.out.printf&#40;&quot;Field text: %s%n&quot;, key&#41;;
     *                         System.out.printf&#40;&quot;Field value data content: %s%n&quot;, documentField.getContent&#40;&#41;&#41;;
     *                         System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, documentField.getConfidence&#40;&#41;&#41;;
     *                     &#125;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocument#string-Flux-long-AnalyzeDocumentOptions -->
     *
     * @param modelId The unique model ID to be used. Use this to specify the custom model ID or prebuilt model ID.
     * Prebuilt model IDs supported can be found <a href="https://aka.ms/azsdk/formrecognizer/models">here</a>
     * @param document The data of the document to analyze information from.
     * @param length The exact length of the data.
     * @param analyzeDocumentOptions The additional configurable {@link AnalyzeDocumentOptions options} that may be
     * passed when analyzing documents.
     *
     * @return A {@link PollerFlux} that polls the progress of the analyze document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns an {@link AnalyzeResult}.
     * @throws DocumentModelOperationException If analyze operation fails and the {@link AnalyzeResultOperation} returns
     * with an {@link OperationStatus#FAILED}.
     * @throws IllegalArgumentException If {@code document} or {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocument(String modelId, Flux<ByteBuffer> document, long length,
                            AnalyzeDocumentOptions analyzeDocumentOptions) {
        return beginAnalyzeDocument(modelId, document, length, analyzeDocumentOptions, Context.NONE);
    }

    PollerFlux<DocumentOperationResult, AnalyzeResult>
        beginAnalyzeDocument(String modelId, Flux<ByteBuffer> document, long length,
                             AnalyzeDocumentOptions analyzeDocumentOptions, Context context) {
        try {
            Objects.requireNonNull(document, "'document' is required and cannot be null.");
            if (CoreUtils.isNullOrEmpty(modelId)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                    + " be null or empty"));
            }

            final AnalyzeDocumentOptions finalAnalyzeDocumentOptions
                = getAnalyzeDocumentOptions(analyzeDocumentOptions);

            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                activationOperation(() ->
                    service.analyzeDocumentWithResponseAsync(modelId,
                            null,
                            CoreUtils.isNullOrEmpty(finalAnalyzeDocumentOptions.getPages())
                                ? null : String.join(",", finalAnalyzeDocumentOptions.getPages()),
                            finalAnalyzeDocumentOptions.getLocale() == null ? null
                                : finalAnalyzeDocumentOptions.getLocale(),
                            StringIndexType.UTF16CODE_UNIT,
                            document,
                            length,
                            context)
                        .map(analyzeDocumentResponse -> Transforms.toDocumentOperationResult(
                            analyzeDocumentResponse.getDeserializedHeaders().getOperationLocation())),
                    logger),
                pollingOperation(
                    resultId -> service.getAnalyzeDocumentResultWithResponseAsync(
                        modelId, resultId, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeDocumentResultWithResponseAsync(
                    modelId, resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                            Transforms.toAnalyzeResultOperation(modelSimpleResponse.getValue().getAnalyzeResult()))
                        .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /*
     * Poller's POLLING operation.
     */
    private Function<PollingContext<DocumentOperationResult>, Mono<PollResponse<DocumentOperationResult>>>
        pollingOperation(
        Function<String, Mono<Response<AnalyzeResultOperation>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<DocumentOperationResult> operationResultPollResponse
                    = pollingContext.getLatestResponse();
                final String resultId = operationResultPollResponse.getValue().getResultId();
                return pollingFunction.apply(resultId)
                    .flatMap(modelResponse -> processAnalyzeModelResponse(modelResponse, operationResultPollResponse))
                    .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    /*
     * Poller's FETCHING operation.
     */
    private Function<PollingContext<DocumentOperationResult>, Mono<Response<AnalyzeResultOperation>>>
        fetchingOperation(
        Function<String, Mono<Response<AnalyzeResultOperation>>> fetchingFunction) {
        return pollingContext -> {
            try {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                return fetchingFunction.apply(resultId);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Mono<PollResponse<DocumentOperationResult>> processAnalyzeModelResponse(
        Response<AnalyzeResultOperation> analyzeResultOperationResponse,
        PollResponse<DocumentOperationResult> operationResultPollResponse) {
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
                throw logger.logExceptionAsError(
                    Transforms.toDocumentModelOperationException(analyzeResultOperationResponse.getValue().getError()));
            default:
                status = LongRunningOperationStatus.fromString(
                    analyzeResultOperationResponse.getValue().getStatus().toString(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }

    private static AnalyzeDocumentOptions getAnalyzeDocumentOptions(AnalyzeDocumentOptions userProvidedOptions) {
        return userProvidedOptions == null ? new AnalyzeDocumentOptions() : userProvidedOptions;
    }
}
