// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
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
 * Operations allowed by the client are recognizing receipt data from documents, recognizing layout information and
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
     * Recognizes receipt data from documents using optical character recognition (OCR)
     * and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string}
     *
     * @param formUrl The source URL to the input form.
     * @param modelId The UUID string format custom trained model Id to be used.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String formUrl, String modelId) {
        return beginRecognizeCustomFormsFromUrl(formUrl, modelId, false, null);
    }

    /**
     * Recognizes receipt data from documents using optical character recognition (OCR)
     * and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-boolean-Duration}
     *
     * @param formUrl The source URL to the input form.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String formUrl, String modelId, boolean includeTextDetails,
        Duration pollInterval) {
        return client.beginRecognizeCustomFormsFromUrl(formUrl, modelId, includeTextDetails, pollInterval)
            .getSyncPoller();
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-string-long-FormContentType}
     *
     * @param form The data of the form to recognize form information from.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(InputStream form, String modelId, long length, FormContentType formContentType) {
        return beginRecognizeCustomForms(form, modelId, length, formContentType, false, null);
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-string-long-FormContentType-boolean-Duration}
     *
     * @param form The data of the form to recognize form information from.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(InputStream form, String modelId, long length, FormContentType formContentType,
        boolean includeTextDetails, Duration pollInterval) {
        Flux<ByteBuffer> buffer = Utility.toFluxByteBuffer(form);
        return client.beginRecognizeCustomForms(buffer, modelId, length, formContentType,
            includeTextDetails, pollInterval).getSyncPoller();
    }

    /**
     * Recognizes layout data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string}
     *
     * @param formUrl The source URL to the input form.
     *
     * @return A {@link SyncPoller} that polls the recognize layout form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>> beginRecognizeContentFromUrl(String formUrl) {
        return beginRecognizeContentFromUrl(formUrl, null);
    }

    /**
     * Recognizes layout data using optical character recognition (OCR) and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string-Duration}
     *
     * @param formUrl The source URL to the input form.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the recognize layout operation until it has completed, has
     * failed, or has been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl, Duration pollInterval) {
        return client.beginRecognizeContentFromUrl(formUrl, pollInterval).getSyncPoller();
    }

    /**
     * Recognizes layout data using optical character recognition (OCR) and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-FormContentType}
     *
     * @param form The data of the form to recognize content information from.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the recognize layout operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>>
        beginRecognizeContent(InputStream form, long length, FormContentType formContentType) {
        return beginRecognizeContent(form, length, formContentType, null);
    }

    /**
     * Recognizes layout data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-FormContentType-Duration}
     *
     * @param form The data of the form to recognize content information from.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the recognize layout operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>>
        beginRecognizeContent(InputStream form, long length, FormContentType formContentType, Duration pollInterval) {
        Flux<ByteBuffer> buffer = Utility.toFluxByteBuffer(form);
        return client.beginRecognizeContent(buffer, length, formContentType, pollInterval)
            .getSyncPoller();
    }

    /**
     * Recognizes receipt data from document using optical character recognition (OCR) and a
     * prebuilt receipt trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string}
     *
     * @param receiptUrl The source URL to the input receipt.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String receiptUrl) {
        return beginRecognizeReceiptsFromUrl(receiptUrl, false, null);
    }

    /**
     * Recognizes receipt data from documents using optical character recognition (OCR) and a
     * prebuilt receipt trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-boolean-Duration}
     *
     * @param receiptUrl The source URL to the input receipt.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String receiptUrl, boolean includeTextDetails, Duration pollInterval) {
        return client.beginRecognizeReceiptsFromUrl(receiptUrl, includeTextDetails, pollInterval).getSyncPoller();
    }

    /**
     * Recognizes data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-FormContentType}
     *
     * @param receipt The data of the receipt to recognize receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link SyncPoller} that polls the recognize receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receipt} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceipts(InputStream receipt, long length, FormContentType formContentType) {
        return beginRecognizeReceipts(receipt, length, formContentType, false, null);
    }

    /**
     * Recognizes data from the providedd document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-FormContentType-boolean-Duration}
     *
     * @param receipt The data of the receipt to recognize receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link SyncPoller} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receipt} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceipts(InputStream receipt, long length, FormContentType formContentType,
        boolean includeTextDetails, Duration pollInterval) {
        Flux<ByteBuffer> buffer = Utility.toFluxByteBuffer(receipt);
        return client.beginRecognizeReceipts(buffer, length, formContentType, includeTextDetails, pollInterval)
            .getSyncPoller();
    }
}
