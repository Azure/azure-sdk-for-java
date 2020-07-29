// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.ContentType;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.implementation.models.SourcePath;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizeOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.ai.formrecognizer.Transforms.toRecognizedForm;
import static com.azure.ai.formrecognizer.Transforms.toRecognizedLayout;
import static com.azure.ai.formrecognizer.implementation.Utility.detectContentType;
import static com.azure.ai.formrecognizer.implementation.Utility.parseModelId;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Form Recognizer.
 * Operations allowed by the client are recognizing receipt data from documents, extracting layout information and
 * analyzing custom forms for predefined data.
 *
 * <p><strong>Instantiating an asynchronous Form Recognizer Client</strong></p>
 * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.instantiation}
 *
 * @see FormRecognizerClientBuilder
 */
@ServiceClient(builder = FormRecognizerClientBuilder.class, isAsync = true)
public final class FormRecognizerAsyncClient {
    private final ClientLogger logger = new ClientLogger(FormRecognizerAsyncClient.class);
    private final FormRecognizerClientImpl service;
    private final FormRecognizerServiceVersion serviceVersion;

    /**
     * Create a {@link FormRecognizerAsyncClient} that sends requests to the Form Recognizer services's endpoint. Each
     * service call goes through the {@link FormRecognizerClientBuilder#pipeline(HttpPipeline)} http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Form Recognizer supported by this client library.
     */
    FormRecognizerAsyncClient(FormRecognizerClientImpl service, FormRecognizerServiceVersion serviceVersion) {
        this.service = service;
        this.serviceVersion = serviceVersion;
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model with or without labels.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string}
     *
     * @param formUrl The URL of the form to analyze.
     * @param modelId The UUID string format custom trained model Id to be used.
     *
     * @return A {@link PollerFlux} that polls the recognize custom form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String formUrl, String modelId) {
        return beginRecognizeCustomFormsFromUrl(formUrl, modelId, null);
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-recognizeOptions}
     *
     * @param formUrl The source URL to the input form.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * recognizing custom form.
     *
     * @return A {@link PollerFlux} that polls the recognize custom form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String formUrl, String modelId, RecognizeOptions recognizeOptions) {
        return beginRecognizeCustomFormsFromUrl(formUrl, modelId, recognizeOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String formUrl, String modelId, RecognizeOptions recognizeOptions,
        Context context) {
        try {
            Objects.requireNonNull(formUrl, "'formUrl' is required and cannot be null.");
            Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");

            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            final boolean isFieldElementsIncluded = recognizeOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                recognizeOptions.getPollInterval(),
                urlActivationOperation(() -> service.analyzeWithCustomModelWithResponseAsync(UUID.fromString(modelId),
                    isFieldElementsIncluded, new SourcePath().setSource(formUrl), context).map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                pollingOperation(resultUid ->
                    service.getAnalyzeFormResultWithResponseAsync(UUID.fromString(modelId), resultUid, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeFormResultWithResponseAsync(
                    UUID.fromString(modelId), resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model with or without labels.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * Note that the {@code data} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-long-string}
     *
     * @param form The data of the form to recognize form information from.
     * @param length The exact length of the data.
     * @param modelId The UUID string format custom trained model Id to be used.
     *
     * @return A {@link PollerFlux} that polls the recognize custom form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(Flux<ByteBuffer> form, long length, String modelId) {
        return beginRecognizeCustomForms(form, length, modelId, null);
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model with or without labels.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * Note that the {@code data} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-long-string-recognizeOptions}
     *
     * @param form The data of the form to recognize form information from.
     * @param length The exact length of the data.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * recognizing custom form.
     *
     * @return A {@link PollerFlux} that polls the recognize custom form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(Flux<ByteBuffer> form, long length, String modelId,
        RecognizeOptions recognizeOptions) {
        return beginRecognizeCustomForms(form, length, modelId, recognizeOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(Flux<ByteBuffer> form, long length, String modelId,
        RecognizeOptions recognizeOptions, Context context) {
        try {
            Objects.requireNonNull(form, "'form' is required and cannot be null.");
            Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");

            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            final boolean isFieldElementsIncluded = recognizeOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                recognizeOptions.getPollInterval(),
                streamActivationOperation(
                    contentType -> service.analyzeWithCustomModelWithResponseAsync(UUID.fromString(modelId),
                        contentType, form, length, isFieldElementsIncluded, context).map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    form, recognizeOptions.getContentType()),
                pollingOperation(
                    resultUuid -> service.getAnalyzeFormResultWithResponseAsync(
                        UUID.fromString(modelId), resultUuid, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeFormResultWithResponseAsync(
                    UUID.fromString(modelId), resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Recognizes content/layout data from documents using optical character recognition (OCR).
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string}
     *
     * @param formUrl The URL of the form to analyze.
     *
     * @return A {@link PollerFlux} that polls the recognize content operation until it has completed, has failed, or
     * has been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<FormPage>> beginRecognizeContentFromUrl(String formUrl) {
        return beginRecognizeContentFromUrl(formUrl, null);
    }

    /**
     * Recognizes layout data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-recognizeOptions}
     *
     * @param formUrl The source URL to the input form.
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * recognizing content/layout on a form.
     *
     * @return A {@link PollerFlux} that polls the recognized content/layout operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl, RecognizeOptions recognizeOptions) {
        return beginRecognizeContentFromUrl(formUrl, recognizeOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl, RecognizeOptions recognizeOptions, Context context) {
        try {
            Objects.requireNonNull(formUrl, "'formUrl' is required and cannot be null.");

            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            return new PollerFlux<>(
                recognizeOptions.getPollInterval(),
                urlActivationOperation(
                    () -> service.analyzeLayoutAsyncWithResponseAsync(new SourcePath().setSource(formUrl), context)
                        .map(response -> new OperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                pollingOperation(resultId -> service.getAnalyzeLayoutResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) ->
                    monoError(logger, new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeLayoutResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedLayout(modelSimpleResponse.getValue().getAnalyzeResult(), true))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Recognizes content/layout data using optical character recognition (OCR).
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * Note that the {@code data} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long}
     *
     * @param form The data of the form to recognize content information from.
     * @param length The exact length of the data.
     *
     * @return A {@link PollerFlux} polls the recognize content operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<FormPage>> beginRecognizeContent(
        Flux<ByteBuffer> form, long length) {
        return beginRecognizeContent(form, length, null);
    }

    /**
     * Recognizes content/layout data using optical character recognition (OCR).
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * Note that the {@code data} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-recognizeOptions}
     *
     * @param form The data of the form to recognize content information from.
     * @param length The exact length of the data.
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * recognizing content/layout on a form.
     *
     * @return A {@link PollerFlux} polls the recognize content operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<FormPage>> beginRecognizeContent(Flux<ByteBuffer> form, long length,
        RecognizeOptions recognizeOptions) {
        return beginRecognizeContent(form, length, recognizeOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<FormPage>> beginRecognizeContent(Flux<ByteBuffer> form, long length,
        RecognizeOptions recognizeOptions, Context context) {
        try {
            Objects.requireNonNull(form, "'form' is required and cannot be null.");
            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            return new PollerFlux<>(
                recognizeOptions.getPollInterval(),
                streamActivationOperation(
                    contentType -> service.analyzeLayoutAsyncWithResponseAsync(contentType, form, length, context)
                        .map(response -> new OperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    form, recognizeOptions.getContentType()),
                pollingOperation(resultId -> service.getAnalyzeLayoutResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) ->
                    monoError(logger, new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeLayoutResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedLayout(modelSimpleResponse.getValue().getAnalyzeResult(), true))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Recognizes receipt data using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://aka.ms/formrecognizer/receiptfields">here</a> for fields found on a receipt.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string}
     *
     * @param receiptUrl The URL of the receipt to analyze.
     *
     * @return A {@link PollerFlux} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl) {
        return beginRecognizeReceiptsFromUrl(receiptUrl, null);
    }

    /**
     * Recognizes receipt data using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string-recognizeOptions}
     *
     * @param receiptUrl The source URL to the input receipt.
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * analyzing a receipt.
     *
     * @return A {@link PollerFlux} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl, RecognizeOptions recognizeOptions) {
        return beginRecognizeReceiptsFromUrl(receiptUrl, recognizeOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl, RecognizeOptions recognizeOptions, Context context) {
        try {
            Objects.requireNonNull(receiptUrl, "'receiptUrl' is required and cannot be null.");

            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            final boolean isFieldElementsIncluded = recognizeOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                recognizeOptions.getPollInterval(),
                urlActivationOperation(
                    () -> service.analyzeReceiptAsyncWithResponseAsync(isFieldElementsIncluded,
                        new SourcePath().setSource(receiptUrl), context).map(response -> new OperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                pollingOperation(resultId -> service.getAnalyzeReceiptResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeReceiptResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Recognizes receipt data using optical character recognition (OCR) and a prebuilt receipt
     * trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://aka.ms/formrecognizer/receiptfields">here</a> for fields found on a receipt.
     *
     * Note that the {@code receipt} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long}
     *
     * @param receipt The data of the document to recognize receipt information from.
     * @param length The exact length of the data.
     *
     * @return A {@link PollerFlux} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receipt} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>> beginRecognizeReceipts(
        Flux<ByteBuffer> receipt, long length) {
        return beginRecognizeReceipts(receipt, length, null);
    }

    /**
     * Recognizes receipt data from documents using optical character recognition (OCR)
     * and a prebuilt receipt trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://aka.ms/formrecognizer/receiptfields">here</a> for fields found on a receipt.
     *
     * Note that the {@code data} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-recognizeOptions}
     *
     * @param receipt The data of the document to recognize receipt information from.
     * @param length The exact length of the data.
     * @param recognizeOptions The additional configurable {@link RecognizeOptions options} that may be passed when
     * analyzing a receipt.
     *
     * @return A {@link PollerFlux} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receipt} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceipts(Flux<ByteBuffer> receipt, long length, RecognizeOptions recognizeOptions) {
        return beginRecognizeReceipts(receipt, length, recognizeOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceipts(Flux<ByteBuffer> receipt, long length, RecognizeOptions recognizeOptions,
        Context context) {
        try {
            Objects.requireNonNull(receipt, "'receipt' is required and cannot be null.");
            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            final boolean isFieldElementsIncluded = recognizeOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                recognizeOptions.getPollInterval(),
                streamActivationOperation(
                    (contentType -> service.analyzeReceiptAsyncWithResponseAsync(
                        contentType, receipt, length, isFieldElementsIncluded, context).map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                    receipt, recognizeOptions.getContentType()),
                pollingOperation(resultId -> service.getAnalyzeReceiptResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeReceiptResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /*
     * Poller's ACTIVATION operation that takes stream as input.
     */
    private Function<PollingContext<OperationResult>, Mono<OperationResult>> streamActivationOperation(
        Function<ContentType, Mono<OperationResult>> activationOperation, Flux<ByteBuffer> form,
        FormContentType contentType) {
        return pollingContext -> {
            try {
                Objects.requireNonNull(form, "'form' is required and cannot be null.");
                if (contentType != null) {
                    return activationOperation.apply(ContentType.fromString(contentType.toString()))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
                } else {
                    return detectContentType(form)
                        .flatMap(activationOperation::apply)
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
                }
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    /*
     * Poller's ACTIVATION operation that takes URL as input.
     */
    private Function<PollingContext<OperationResult>, Mono<OperationResult>> urlActivationOperation(
        Supplier<Mono<OperationResult>> activationOperation) {
        return pollingContext -> {
            try {
                return activationOperation.get().onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    /*
     * Poller's POLLING operation.
     */
    private Function<PollingContext<OperationResult>, Mono<PollResponse<OperationResult>>> pollingOperation(
        Function<UUID, Mono<SimpleResponse<AnalyzeOperationResult>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
                final UUID resultUuid = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                return pollingFunction.apply(resultUuid)
                    .flatMap(modelSimpleResponse -> processAnalyzeModelResponse(modelSimpleResponse,
                        operationResultPollResponse));
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    /*
     * Poller's FETCHING operation.
     */
    private Function<PollingContext<OperationResult>, Mono<SimpleResponse<AnalyzeOperationResult>>> fetchingOperation(
        Function<UUID, Mono<SimpleResponse<AnalyzeOperationResult>>> fetchingFunction) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                return fetchingFunction.apply(resultUuid);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Mono<PollResponse<OperationResult>> processAnalyzeModelResponse(
        SimpleResponse<AnalyzeOperationResult> analyzeOperationResultSimpleResponse,
        PollResponse<OperationResult> operationResultPollResponse) {
        LongRunningOperationStatus status;
        switch (analyzeOperationResultSimpleResponse.getValue().getStatus()) {
            case NOT_STARTED:
            case RUNNING:
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            case SUCCEEDED:
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case FAILED:
                throw logger.logExceptionAsError(new FormRecognizerException("Analyze operation failed",
                    analyzeOperationResultSimpleResponse.getValue().getAnalyzeResult().getErrors()));
            default:
                status = LongRunningOperationStatus.fromString(
                    analyzeOperationResultSimpleResponse.getValue().getStatus().toString(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }

    private RecognizeOptions getRecognizeOptionsProperties(RecognizeOptions userProvidedOptions) {
        if (userProvidedOptions != null) {
            return userProvidedOptions;
        } else {
            return new RecognizeOptions();
        }
    }
}
