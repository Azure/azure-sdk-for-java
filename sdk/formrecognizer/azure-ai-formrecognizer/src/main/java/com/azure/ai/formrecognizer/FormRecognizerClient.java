// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;

/**
 * This class provides a synchronous client that contains all the operations that apply to Azure Form Recognizer.
 * Operations allowed by the client are, to recognize receipt data from documents, extract layout information and
 * analyze custom forms for predefined data.
 *
 * @see FormRecognizerClientBuilder
 */
@ServiceClient(builder = FormRecognizerClientBuilder.class)
public final class FormRecognizerClient {
    private final FormRecognizerAsyncClient client;

    /**
     * Create a {@link FormRecognizerClient client} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link FormRecognizerClientBuilder#pipeline http pipeline}.
     *
     * @param client The {@link FormRecognizerClient} that the client routes its request through.
     */
    FormRecognizerClient(FormRecognizerAsyncClient client) {
        this.client = client;
    }

    /**
     * Creates a new {@link FormTrainingClient} object.The new {@code FormRecognizerClient} uses the same request policy
     * pipeline as the {@code FormRecognizerClient}.
     *
     * @return A new {@link FormTrainingClient} object.
     */
    public FormTrainingClient getFormTrainingClient() {
        return new FormTrainingClient(client.getFormTrainingAsyncClient());
    }

    /**
     * Recognizes and extracts receipt data from documents using optical character recognition (OCR)
     * and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param modelId The custom trained model Id to be used.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract custom form operation until it has completed,
     * has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<RecognizedForm>>
        beginExtractCustomFormsFromUrl(String fileSourceUrl, String modelId) {
        return beginExtractCustomFormsFromUrl(fileSourceUrl, modelId, false);
    }

    /**
     * Recognizes and extracts receipt data from documents using optical character recognition (OCR)
     * and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param modelId The custom trained model Id to be used.
     * @param includeTextDetails Include text lines and element references in the result.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract custom form operation until it has completed,
     * has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<RecognizedForm>>
        beginExtractCustomFormsFromUrl(String fileSourceUrl, String modelId, boolean includeTextDetails) {
        return client.beginExtractCustomFormsFromUrl(fileSourceUrl, modelId, includeTextDetails).getSyncPoller();
    }

    /**
     * Recognizes and extracts form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param modelId The custom trained model Id to be used.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the extract custom form operation until it has completed,
     * has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<RecognizedForm>>
        beginExtractCustomForms(Flux<ByteBuffer> data, String modelId, Long length, FormContentType formContentType) {
        return beginExtractCustomForms(data, modelId, length, formContentType, false, null);
    }

    /**
     * Recognizes and extracts form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param modelId The custom trained model Id to be used.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract custom form operation until it has completed,
     * has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<RecognizedForm>>
        beginExtractCustomForms(Flux<ByteBuffer> data, String modelId, Long length, FormContentType formContentType,
        boolean includeTextDetails, Duration pollInterval) {
        return client.beginExtractCustomForms(data, modelId, length, formContentType, includeTextDetails, pollInterval)
            .getSyncPoller();
    }

    /**
     * Recognizes and extracts layout data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     *
     * @return A {@link SyncPoller} that polls the extract layout form operation until it has completed, has failed,
     * or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<FormPage>> beginExtractContentFromUrl(String fileSourceUrl) {
        return beginExtractContentFromUrl(fileSourceUrl, null);
    }

    /**
     * Recognizes and extracts layout data using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract layout operation until it has completed, has
     * failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<FormPage>>
        beginExtractContentFromUrl(String sourceUrl, Duration pollInterval) {
        return client.beginExtractContentFromUrl(sourceUrl, pollInterval).getSyncPoller();
    }

    /**
     * Recognizes and extracts layout data using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the extract layout operation until it has completed, has failed, or has
     * been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<FormPage>>
        beginExtractContent(InputStream data, long length, FormContentType formContentType) {
        return beginExtractContent(data, length, formContentType, null);
    }

    /**
     * Recognizes and extracts layout data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract layout operation until it has completed,
     * has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<FormPage>>
        beginExtractContent(InputStream data, long length, FormContentType formContentType,
                        Duration pollInterval) {
        // TODO: #9248 should be able to infer form content type
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(data);
        return client.beginExtractContent(buffer, formContentType, pollInterval, length)
            .getSyncPoller();
    }

    /**
     * Recognizes and extracts receipt data from documentsusing optical character recognition (OCR) and a
     * prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param modelId The UUID string format custom trained model Id to be used.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract custom form operation until it has completed,
     * has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<RecognizedReceipt>>
        beginExtractReceiptsFromUrl(String sourceUrl) {
        return beginExtractReceiptsFromUrl(sourceUrl, false, null);
    }

    /**
     * Recognizes and extracts receipt data from documentsusing optical character recognition (OCR) and a
     * prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract custom form operation until it has completed,
     * has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<RecognizedReceipt>>
        beginExtractReceiptsFromUrl(String sourceUrl, boolean includeTextDetails, Duration pollInterval) {
        return client.beginExtractReceiptsFromUrl(sourceUrl, pollInterval, includeTextDetails).getSyncPoller();
    }

    /**
     * Recognizes and extracts data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the extract receipt operation until it has completed,
     * has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<RecognizedReceipt>>
        beginExtractReceipts(InputStream data, long length, FormContentType formContentType) {
        return beginExtractReceipts(data, length, formContentType, false, null);
    }

    /**
     * Recognizes and extracts data from the providedd document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract receipt operation until it
     * has completed, has failed, or has been cancelled.
     */
    public SyncPoller<OperationResult, IterableStream<RecognizedReceipt>>
        beginExtractReceipts(InputStream data, long length, FormContentType formContentType,
        boolean includeTextDetails, Duration pollInterval) {
        // TODO: #9248 should be able to infer form content type
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(data);
        return client.beginExtractReceipts(buffer, includeTextDetails, formContentType, pollInterval, length)
            .getSyncPoller();
    }

    /**
     * Recognizes and extracts receipt data from documentsusing optical character recognition (OCR) and a
     * prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract receipt operation until it has completed,
     * has failed, or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, IterableStream<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String sourceUrl) {
        return beginRecognizeReceiptsFromUrl(sourceUrl, false, null);
    }

    /**
     * Recognizes and extracts receipt data from documentsusing optical character recognition (OCR) and a
     * prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract receipt operation until it has completed,
     * has failed, or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, IterableStream<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String sourceUrl, boolean includeTextDetails, Duration pollInterval) {
        return client.beginRecognizeReceiptsFromUrl(sourceUrl, includeTextDetails, pollInterval).getSyncPoller();
    }

    /**
     * Recognizes and extracts data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the extract receipt operation until it has completed,
     * has failed, or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, IterableStream<RecognizedReceipt>>
        beginRecognizeReceipts(InputStream data, long length, FormContentType formContentType) {
        return beginRecognizeReceipts(data, length, formContentType, false, null);
    }

    /**
     * Recognizes and extracts data from the providedd document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract receipt operation until it
     * has completed, has failed, or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, IterableStream<RecognizedReceipt>>
        beginRecognizeReceipts(InputStream data, long length, FormContentType formContentType,
        boolean includeTextDetails, Duration pollInterval) {
        // TODO: #9248 should be able to infer form content type
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(data);
        return client.beginRecognizeReceipts(buffer, length, formContentType, includeTextDetails, pollInterval)
            .getSyncPoller();
    }
}
