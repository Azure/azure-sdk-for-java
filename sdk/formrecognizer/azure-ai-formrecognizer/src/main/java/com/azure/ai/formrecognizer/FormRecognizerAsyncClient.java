// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.ContentType;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.implementation.models.SourcePath;
import com.azure.ai.formrecognizer.models.RecognizeContentOptions;
import com.azure.ai.formrecognizer.models.RecognizeReceiptOptions;
import com.azure.ai.formrecognizer.models.ErrorInformation;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
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
import java.util.stream.Collectors;

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
     * Create a {@link FormRecognizerAsyncClient} that sends requests to the Form Recognizer service's endpoint. Each
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
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param formUrl The URL of the form to analyze.
     *
     * @return A {@link PollerFlux} that polls the recognize custom form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String modelId, String formUrl) {
        return beginRecognizeCustomFormsFromUrl(modelId, formUrl, null);
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-RecognizeCustomFormOptions}
     *
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param formUrl The source URL to the input form.
     * @param recognizeCustomFormOptions The additional configurable
     * {@link RecognizeCustomFormOptions options} that may be passed when recognizing custom forms.
     *
     * @return A {@link PollerFlux} that polls the recognize custom form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String modelId, String formUrl,
        RecognizeCustomFormOptions recognizeCustomFormOptions) {
        return beginRecognizeCustomFormsFromUrl(formUrl, modelId, recognizeCustomFormOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String formUrl, String modelId,
        RecognizeCustomFormOptions recognizeCustomFormOptions,
        Context context) {
        try {
            Objects.requireNonNull(formUrl, "'formUrl' is required and cannot be null.");
            Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");

            recognizeCustomFormOptions = getRecognizeCustomFormOptions(recognizeCustomFormOptions);
            final boolean isFieldElementsIncluded = recognizeCustomFormOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                recognizeCustomFormOptions.getPollInterval(),
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
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long}
     *
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param form The data of the form to recognize form information from.
     * @param length The exact length of the data.
     *
     * @return A {@link PollerFlux} that polls the recognize custom form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(String modelId, Flux<ByteBuffer> form, long length) {
        return beginRecognizeCustomForms(modelId, form, length, null);
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
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long-RecognizeCustomFormOptions}
     *
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param form The data of the form to recognize form information from.
     * @param length The exact length of the data.
     * @param recognizeCustomFormOptions The additional configurable
     * {@link RecognizeCustomFormOptions options} that may be passed when recognizing custom forms.
     *
     * @return A {@link PollerFlux} that polls the recognize custom form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(String modelId, Flux<ByteBuffer> form, long length,
        RecognizeCustomFormOptions recognizeCustomFormOptions) {
        return beginRecognizeCustomForms(modelId, form, length, recognizeCustomFormOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(String modelId, Flux<ByteBuffer> form, long length,
        RecognizeCustomFormOptions recognizeCustomFormOptions, Context context) {
        try {
            Objects.requireNonNull(form, "'form' is required and cannot be null.");
            Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");

            recognizeCustomFormOptions = getRecognizeCustomFormOptions(recognizeCustomFormOptions);
            final boolean isFieldElementsIncluded = recognizeCustomFormOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                recognizeCustomFormOptions.getPollInterval(),
                streamActivationOperation(
                    contentType -> service.analyzeWithCustomModelWithResponseAsync(UUID.fromString(modelId),
                        contentType, form, length, isFieldElementsIncluded, context).map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    form, recognizeCustomFormOptions.getContentType()),
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
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-RecognizeContentOptions}
     *
     * @param formUrl The source URL to the input form.
     * @param recognizeContentOptions The additional configurable {@link RecognizeContentOptions options}
     * that may be passed when recognizing content/layout on a form.
     *
     * @return A {@link PollerFlux} that polls the recognized content/layout operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl, RecognizeContentOptions recognizeContentOptions) {
        return beginRecognizeContentFromUrl(formUrl, recognizeContentOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl,
        RecognizeContentOptions recognizeContentOptions, Context context) {
        try {
            Objects.requireNonNull(formUrl, "'formUrl' is required and cannot be null.");

            recognizeContentOptions = getRecognizeContentOptions(recognizeContentOptions);
            return new PollerFlux<>(
                recognizeContentOptions.getPollInterval(),
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
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-RecognizeContentOptions}
     *
     * @param form The data of the form to recognize content information from.
     * @param length The exact length of the data.
     * @param recognizeContentOptions The additional configurable {@link RecognizeContentOptions options}
     * that may be passed when recognizing content/layout on a form.
     *
     * @return A {@link PollerFlux} polls the recognize content operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns a List of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<FormPage>> beginRecognizeContent(Flux<ByteBuffer> form, long length,
        RecognizeContentOptions recognizeContentOptions) {
        return beginRecognizeContent(form, length, recognizeContentOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<FormPage>> beginRecognizeContent(Flux<ByteBuffer> form, long length,
        RecognizeContentOptions recognizeContentOptions, Context context) {
        try {
            Objects.requireNonNull(form, "'form' is required and cannot be null.");
            recognizeContentOptions = getRecognizeContentOptions(recognizeContentOptions);
            return new PollerFlux<>(
                recognizeContentOptions.getPollInterval(),
                streamActivationOperation(
                    contentType -> service.analyzeLayoutAsyncWithResponseAsync(contentType, form, length, context)
                        .map(response -> new OperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    form, recognizeContentOptions.getContentType()),
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
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string-RecognizeReceiptOptions}
     *
     * @param receiptUrl The source URL to the input receipt.
     * @param recognizeReceiptOptions The additional configurable {@link RecognizeReceiptOptions options}
     * that may be passed when analyzing a receipt.
     *
     * @return A {@link PollerFlux} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl, RecognizeReceiptOptions recognizeReceiptOptions) {
        return beginRecognizeReceiptsFromUrl(receiptUrl, recognizeReceiptOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl,
        RecognizeReceiptOptions recognizeReceiptOptions, Context context) {
        try {
            Objects.requireNonNull(receiptUrl, "'receiptUrl' is required and cannot be null.");

            recognizeReceiptOptions = getRecognizeReceiptOptions(recognizeReceiptOptions);
            final boolean isFieldElementsIncluded = recognizeReceiptOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                recognizeReceiptOptions.getPollInterval(),
                urlActivationOperation(
                    () -> service.analyzeReceiptAsyncWithResponseAsync(isFieldElementsIncluded,
                        new SourcePath().setSource(receiptUrl), context)
                        .map(response -> new OperationResult(
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
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-RecognizeReceiptOptions}
     *
     * @param receipt The data of the document to recognize receipt information from.
     * @param length The exact length of the data.
     * @param recognizeReceiptOptions The additional configurable {@link RecognizeReceiptOptions options}
     * that may be passed when analyzing a receipt.
     *
     * @return A {@link PollerFlux} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a List of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receipt} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceipts(Flux<ByteBuffer> receipt, long length,
        RecognizeReceiptOptions recognizeReceiptOptions) {
        return beginRecognizeReceipts(receipt, length, recognizeReceiptOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceipts(Flux<ByteBuffer> receipt, long length,
        RecognizeReceiptOptions recognizeReceiptOptions,
        Context context) {
        try {
            Objects.requireNonNull(receipt, "'receipt' is required and cannot be null.");
            recognizeReceiptOptions = getRecognizeReceiptOptions(recognizeReceiptOptions);
            final boolean isFieldElementsIncluded = recognizeReceiptOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                recognizeReceiptOptions.getPollInterval(),
                streamActivationOperation(
                    (contentType -> service.analyzeReceiptAsyncWithResponseAsync(
                        contentType, receipt, length, isFieldElementsIncluded, context).map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                    receipt, recognizeReceiptOptions.getContentType()),
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
        Function<UUID, Mono<Response<AnalyzeOperationResult>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
                final UUID resultUuid = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                return pollingFunction.apply(resultUuid)
                    .flatMap(modelResponse -> processAnalyzeModelResponse(modelResponse,
                        operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    /*
     * Poller's FETCHING operation.
     */
    private Function<PollingContext<OperationResult>, Mono<Response<AnalyzeOperationResult>>> fetchingOperation(
        Function<UUID, Mono<Response<AnalyzeOperationResult>>> fetchingFunction) {
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
        Response<AnalyzeOperationResult> analyzeOperationResultResponse,
        PollResponse<OperationResult> operationResultPollResponse) {
        LongRunningOperationStatus status;
        switch (analyzeOperationResultResponse.getValue().getStatus()) {
            case NOT_STARTED:
            case RUNNING:
                status = LongRunningOperationStatus.IN_PROGRESS;
                break;
            case SUCCEEDED:
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case FAILED:
                throw logger.logExceptionAsError(new FormRecognizerException("Analyze operation failed",
                    analyzeOperationResultResponse.getValue().getAnalyzeResult().getErrors().stream()
                        .map(errorInformation ->
                            new ErrorInformation(errorInformation.getCode(), errorInformation.getMessage()))
                        .collect(Collectors.toList())));
            default:
                status = LongRunningOperationStatus.fromString(
                    analyzeOperationResultResponse.getValue().getStatus().toString(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }

    private static RecognizeCustomFormOptions
        getRecognizeCustomFormOptions(RecognizeCustomFormOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeCustomFormOptions() : userProvidedOptions;
    }

    private static RecognizeContentOptions
        getRecognizeContentOptions(RecognizeContentOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeContentOptions() : userProvidedOptions;
    }

    private static RecognizeReceiptOptions
        getRecognizeReceiptOptions(RecognizeReceiptOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeReceiptOptions() : userProvidedOptions;
    }
}
