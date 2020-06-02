// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;

/**
 * This class provides a synchronous client that contains all the operations that apply to Azure Form Recognizer.
 * Operations allowed by the client are recognizing receipt data from documents, extracting layout information and
 * analyzing custom forms for predefined data.
 *
 * <p><strong>Instantiating a synchronous Form Recognizer Client</strong></p>
 * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.instantiation}
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
     * Recognizes and extracts receipt data from documents using optical character recognition (OCR)
     * and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string}
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param modelId The UUID string format custom trained model Id to be used.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String fileSourceUrl, String modelId) {
        return beginRecognizeCustomFormsFromUrl(fileSourceUrl, modelId, false, null);
    }

    /**
     * Recognizes and extracts receipt data from documents using optical character recognition (OCR)
     * and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-boolean-Duration}
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String fileSourceUrl, String modelId, boolean includeTextDetails,
        Duration pollInterval) {
        return client.beginRecognizeCustomFormsFromUrl(fileSourceUrl, modelId, includeTextDetails, pollInterval)
            .getSyncPoller();
    }

    /**
     * Recognizes and extracts form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-string-long-FormContentType}
     *
     * @param data The data of the document to be extract receipt information from.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the extract custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(InputStream data, String modelId, long length, FormContentType formContentType) {
        return beginRecognizeCustomForms(data, modelId, length, formContentType, false, null);
    }

    /**
     * Recognizes and extracts form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-string-long-FormContentType-boolean-Duration}
     *
     * @param data The data of the document to be extract receipt information from.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(InputStream data, String modelId, long length, FormContentType formContentType,
        boolean includeTextDetails, Duration pollInterval) {
        Flux<ByteBuffer> buffer = Utility.toFluxByteBuffer(data);
        return client.beginRecognizeCustomForms(buffer, modelId, length, formContentType,
            includeTextDetails, pollInterval).getSyncPoller();
    }

    /**
     * Recognizes and extracts layout data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string}
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     *
     * @return A {@link SyncPoller} that polls the extract layout form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link FormPage}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>> beginRecognizeContentFromUrl(String fileSourceUrl) {
        return beginRecognizeContentFromUrl(fileSourceUrl, null);
    }

    /**
     * Recognizes and extracts layout data using optical character recognition (OCR) and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string-Duration}
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract layout operation until it has completed, has
     * failed, or has been cancelled. The completed operation returns a List of {@link FormPage}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String sourceUrl, Duration pollInterval) {
        return client.beginRecognizeContentFromUrl(sourceUrl, pollInterval).getSyncPoller();
    }

    /**
     * Recognizes and extracts layout data using optical character recognition (OCR) and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-FormContentType}
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the extract layout operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns a List of {@link FormPage}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>>
        beginRecognizeContent(InputStream data, long length, FormContentType formContentType) {
        return beginRecognizeContent(data, length, formContentType, null);
    }

    /**
     * Recognizes and extracts layout data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-FormContentType-Duration}
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract layout operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link FormPage}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>>
        beginRecognizeContent(InputStream data, long length, FormContentType formContentType, Duration pollInterval) {
        Flux<ByteBuffer> buffer = Utility.toFluxByteBuffer(data);
        return client.beginRecognizeContent(buffer, length, formContentType, pollInterval)
            .getSyncPoller();
    }

    /**
     * Recognizes and extracts receipt data from document susing optical character recognition (OCR) and a
     * prebuilt receipt trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string}
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String sourceUrl) {
        return beginRecognizeReceiptsFromUrl(sourceUrl, false, null);
    }

    /**
     * Recognizes and extracts receipt data from documents using optical character recognition (OCR) and a
     * prebuilt receipt trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-boolean-Duration}
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 20 MB.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} to poll the progress of the extract receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String sourceUrl, boolean includeTextDetails, Duration pollInterval) {
        return client.beginRecognizeReceiptsFromUrl(sourceUrl, includeTextDetails, pollInterval).getSyncPoller();
    }

    /**
     * Recognizes and extracts data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-FormContentType}
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the extract receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceipts(InputStream data, long length, FormContentType formContentType) {
        return beginRecognizeReceipts(data, length, formContentType, false, null);
    }

    /**
     * Recognizes and extracts data from the providedd document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-FormContentType-boolean-Duration}
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 20 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the extract receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceipts(InputStream data, long length, FormContentType formContentType,
        boolean includeTextDetails, Duration pollInterval) {
        Flux<ByteBuffer> buffer = Utility.toFluxByteBuffer(data);
        return client.beginRecognizeReceipts(buffer, length, formContentType, includeTextDetails, pollInterval)
            .getSyncPoller();
    }
}
