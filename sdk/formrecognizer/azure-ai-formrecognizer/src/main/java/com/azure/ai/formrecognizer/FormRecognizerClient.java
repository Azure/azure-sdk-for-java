// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizeOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
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
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model with or without labels.
     * <p>The service does not support cancellation of the long running operation and returns with an error message
     * indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string}
     *
     * @param formUrl The URL of the form to analyze.
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
        return beginRecognizeCustomFormsFromUrl(formUrl, modelId, null);
    }

    /**
     * Recognizes receipt data from documents using optical character recognition (OCR)
     * and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-recognizeOptions}
     *
     * @param formUrl The source URL to the input form.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * recognizing custom form.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String formUrl, String modelId, RecognizeOptions recognizeOptions) {
        return client.beginRecognizeCustomFormsFromUrl(formUrl, modelId, recognizeOptions).getSyncPoller();
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model with or without labels.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-long-string}
     *
     * @param form The data of the form to recognize form information from.
     * @param length The exact length of the data.
     * @param modelId The UUID string format custom trained model Id to be used.
     *
     * @return A {@link SyncPoller} that polls the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(InputStream form, long length, String modelId) {
        return beginRecognizeCustomForms(form, length, modelId, null);
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-long-string-recognizeOptions}
     *
     * @param form The data of the form to recognize form information from.
     * @param length The exact length of the data..
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * recognizing custom form.
     *
     * @return A {@link SyncPoller} that polls the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code recognizeOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(InputStream form, long length, String modelId, RecognizeOptions recognizeOptions) {
        Flux<ByteBuffer> buffer = Utility.toFluxByteBuffer(form);
        return client.beginRecognizeCustomForms(buffer, length, modelId, recognizeOptions).getSyncPoller();
    }

    /**
     * Recognizes content/layout data from documents using optical character recognition (OCR).
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string}
     *
     * @param formUrl The URL of the form to analyze.
     *
     * @return A {@link SyncPoller} that polls the recognize content form operation until it has completed, has failed,
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
     * Recognizes content/layout data using optical character recognition (OCR).
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string-recognizeOptions}
     *
     * @param formUrl The source URL to the input form.
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * recognizing content/layout on a form.
     *
     * @return A {@link SyncPoller} that polls the recognize layout operation until it has completed, has
     * failed, or has been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl, RecognizeOptions recognizeOptions) {
        return client.beginRecognizeContentFromUrl(formUrl, recognizeOptions).getSyncPoller();
    }

    /**
     * Recognizes layout data using optical character recognition (OCR) and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long}
     *
     * @param form The data of the form to recognize content information from.
     * @param length The exact length of the data..
     *
     * @return A {@link SyncPoller} that polls the recognize content operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>>
        beginRecognizeContent(InputStream form, long length) {
        return beginRecognizeContent(form, length, null);
    }

    /**
     * Recognizes content/layout data from the provided document data using optical character recognition (OCR).
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-recognizeOptions}
     *
     *  @param form The data of the form to recognize content information from.
     * @param length The exact length of the data..
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * analyzing a receipt. The configurable {@code RecognizeOptions options} that may be passed when recognizing
     * content on a form.
     *
     * @return A {@link SyncPoller} that polls the recognize content operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code recognizeOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<FormPage>> beginRecognizeContent(InputStream form, long length,
        RecognizeOptions recognizeOptions) {
        Flux<ByteBuffer> buffer = Utility.toFluxByteBuffer(form);
        return client.beginRecognizeContent(buffer, length, recognizeOptions).getSyncPoller();
    }

    /**
     * Recognizes receipt data from document using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/azsdk/python/formrecognizer/receiptfields">here</a> for fields found on a receipt.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string}
     *
     * @param receiptUrl The URL of the receipt to analyze.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>> beginRecognizeReceiptsFromUrl(String receiptUrl) {
        return beginRecognizeReceiptsFromUrl(receiptUrl, null);
    }

    /**
     * Recognizes receipt data from documents using optical character recognition (OCR) and a
     * prebuilt receipt trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-recognizeOptions}
     *
     * @param receiptUrl The source URL to the input receipt.
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * analyzing a receipt. Include text lines and element references in the result.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String receiptUrl, RecognizeOptions recognizeOptions) {
        return client.beginRecognizeReceiptsFromUrl(receiptUrl, recognizeOptions).getSyncPoller();
    }

    /**
     * Recognizes data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/azsdk/python/formrecognizer/receiptfields">here</a> for fields found on a receipt.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long}
     *
     * @param receipt The data of the receipt to recognize receipt information from.
     * @param length The exact length of the data..
     *
     * @return A {@link SyncPoller} that polls the recognize receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receipt} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceipts(InputStream receipt, long length) {
        return beginRecognizeReceipts(receipt, length, null);
    }

    /**
     * Recognizes data from the provided document data using optical character recognition (OCR) and a prebuilt
     * trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/azsdk/python/formrecognizer/receiptfields">here</a> for fields found on a receipt.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-recognizeOptions}
     *
     * @param receipt The data of the receipt to recognize receipt information from.
     * @param length The exact length of the data..
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * analyzing a receipt.
     *
     * @return A {@link SyncPoller} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedReceipt}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code recognizeOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<OperationResult, List<RecognizedReceipt>>
        beginRecognizeReceipts(InputStream receipt, long length, RecognizeOptions recognizeOptions) {
        Flux<ByteBuffer> buffer = Utility.toFluxByteBuffer(receipt);
        return client.beginRecognizeReceipts(buffer, length, recognizeOptions).getSyncPoller();
    }
}
