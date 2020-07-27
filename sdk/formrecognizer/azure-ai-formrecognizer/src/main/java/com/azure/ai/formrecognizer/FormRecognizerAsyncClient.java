// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.ContentType;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.implementation.models.SourcePath;
import com.azure.ai.formrecognizer.models.ErrorInformation;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizeOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.SimpleResponse;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

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
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is {@code null}.
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
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is {@code null}.
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
            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            return new PollerFlux<OperationResult, List<RecognizedForm>>(
                recognizeOptions.getPollInterval(),
                analyzeFormActivationOperation(formUrl, modelId, recognizeOptions.isIncludeFieldElements(), context),
                createAnalyzeFormPollOperation(modelId, context),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchAnalyzeFormResultOperation(modelId, recognizeOptions.isIncludeFieldElements(), context));
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
     * @throws NullPointerException If {@code form}, {@code modelId} is {@code null}.
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
     * @throws NullPointerException If {@code form}, {@code modelId} is {@code null}.
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
            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            return new PollerFlux<OperationResult, List<RecognizedForm>>(
                recognizeOptions.getPollInterval(),
                analyzeFormStreamActivationOperation(form, modelId, length,
                    recognizeOptions.getContentType(),
                    recognizeOptions.isIncludeFieldElements(), context),
                createAnalyzeFormPollOperation(modelId, context),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchAnalyzeFormResultOperation(modelId, recognizeOptions.isIncludeFieldElements(), context));
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
     * @throws NullPointerException If {@code formUrl} is {@code null}.
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
     * @throws NullPointerException If {@code formUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl, RecognizeOptions recognizeOptions) {
        return beginRecognizeContentFromUrl(formUrl, recognizeOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl, RecognizeOptions recognizeOptions, Context context) {
        try {
            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            return new PollerFlux<OperationResult, List<FormPage>>(
                recognizeOptions.getPollInterval(),
                contentAnalyzeActivationOperation(formUrl, context),
                extractContentPollOperation(context),
                (activationResponse, pollingContext) ->
                    monoError(logger, new RuntimeException("Cancellation is not supported")),
                fetchExtractContentResult(context));
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
     * @throws NullPointerException If {@code form} is {@code null}.
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
     * @throws NullPointerException If {@code form} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<FormPage>> beginRecognizeContent(Flux<ByteBuffer> form, long length,
        RecognizeOptions recognizeOptions) {
        return beginRecognizeContent(form, length, recognizeOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<FormPage>> beginRecognizeContent(Flux<ByteBuffer> form, long length,
        RecognizeOptions recognizeOptions, Context context) {
        try {
            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            return new PollerFlux<>(
                recognizeOptions.getPollInterval(),
                contentStreamActivationOperation(form, length, recognizeOptions.getContentType(), context),
                extractContentPollOperation(context),
                (activationResponse, pollingContext) ->
                    monoError(logger, new RuntimeException("Cancellation is not supported")),
                fetchExtractContentResult(context));
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
     * @throws NullPointerException If {@code receiptUrl} is {@code null}.
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
     * @throws NullPointerException If {@code receiptUrl} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl, RecognizeOptions recognizeOptions) {
        return beginRecognizeReceiptsFromUrl(receiptUrl, recognizeOptions, Context.NONE);
    }

    PollerFlux<OperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl, RecognizeOptions recognizeOptions, Context context) {
        try {
            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            return new PollerFlux<OperationResult, List<RecognizedForm>>(
                recognizeOptions.getPollInterval(),
                receiptAnalyzeActivationOperation(receiptUrl, recognizeOptions.isIncludeFieldElements(), context),
                extractReceiptPollOperation(context),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchExtractReceiptResult(recognizeOptions.isIncludeFieldElements(), context));
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
     * @throws NullPointerException If {@code receipt} is {@code null}.
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
     * @throws NullPointerException If {@code receipt} is {@code null}.
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
            recognizeOptions = getRecognizeOptionsProperties(recognizeOptions);
            return new PollerFlux<>(
                recognizeOptions.getPollInterval(),
                receiptStreamActivationOperation(receipt, length,
                    recognizeOptions.getContentType(), recognizeOptions.isIncludeFieldElements(), context),
                extractReceiptPollOperation(context),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchExtractReceiptResult(recognizeOptions.isIncludeFieldElements(), context));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> receiptAnalyzeActivationOperation(
        String receiptUrl, boolean includeFieldElements, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(receiptUrl, "'receiptUrl' is required and cannot be null.");
                return service.analyzeReceiptAsyncWithResponseAsync(includeFieldElements,
                    new SourcePath().setSource(receiptUrl), context)
                    .map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation())))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> receiptStreamActivationOperation(
        Flux<ByteBuffer> form, long length, FormContentType contentType, boolean includeFieldElements,
        Context context) {
        return pollingContext -> {
            try {
                Objects.requireNonNull(form, "'form' is required and cannot be null.");
                if (contentType != null) {
                    return service.analyzeReceiptAsyncWithResponseAsync(
                        ContentType.fromString(contentType.toString()), form, length, includeFieldElements, context)
                        .map(response -> new OperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))
                        .onErrorMap(throwable -> throwable);
                } else {
                    return detectContentType(form)
                        .flatMap(detectedContentType ->
                            service.analyzeReceiptAsyncWithResponseAsync(detectedContentType, form, length,
                                includeFieldElements, context)
                                .map(response -> new OperationResult(
                                    parseModelId(response.getDeserializedHeaders().getOperationLocation()))))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
                }
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<PollResponse<OperationResult>>>
        extractReceiptPollOperation(Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
                UUID resultUid = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                return service.getAnalyzeReceiptResultWithResponseAsync(resultUid, context)
                    .flatMap(modelSimpleResponse -> processAnalyzeModelResponse(modelSimpleResponse,
                        operationResultPollResponse));
            } catch (HttpResponseException e) {
                logger.logExceptionAsError(e);
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<List<RecognizedForm>>>
        fetchExtractReceiptResult(boolean includeFieldElements, Context context) {
        return (pollingContext) -> {
            try {
                final UUID resultUid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                return service.getAnalyzeReceiptResultWithResponseAsync(resultUid, context)
                    .map(modelSimpleResponse -> {
                        throwIfAnalyzeStatusInvalid(modelSimpleResponse.getValue());
                        return toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(),
                            includeFieldElements);
                    })
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> contentAnalyzeActivationOperation(
        String formUrl, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(formUrl, "'formUrl' is required and cannot be null.");
                return service.analyzeLayoutAsyncWithResponseAsync(new SourcePath().setSource(formUrl), context)
                    .map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation())))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> contentStreamActivationOperation(
        Flux<ByteBuffer> form, long length, FormContentType contentType, Context context) {
        return pollingContext -> {
            try {
                Objects.requireNonNull(form, "'form' is required and cannot be null.");
                if (contentType != null) {
                    return service.analyzeLayoutAsyncWithResponseAsync(
                        ContentType.fromString(contentType.toString()), form, length, context)
                        .map(response -> new OperationResult(parseModelId(
                            response.getDeserializedHeaders().getOperationLocation())))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
                } else {
                    return detectContentType(form)
                        .flatMap(detectedContentType ->
                            service.analyzeLayoutAsyncWithResponseAsync(detectedContentType, form, length, context)
                                .map(response -> new OperationResult(
                                    parseModelId(response.getDeserializedHeaders().getOperationLocation()))))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
                }
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<PollResponse<OperationResult>>>
        extractContentPollOperation(Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
                UUID resultUid = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                return service.getAnalyzeLayoutResultWithResponseAsync(resultUid, context)
                    .flatMap(modelSimpleResponse -> processAnalyzeModelResponse(modelSimpleResponse,
                        operationResultPollResponse));
            } catch (HttpResponseException e) {
                logger.logExceptionAsError(e);
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<List<FormPage>>>
        fetchExtractContentResult(Context context) {
        return (pollingContext) -> {
            try {
                final UUID resultUid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                return service.getAnalyzeLayoutResultWithResponseAsync(resultUid, context)
                    .map(modelSimpleResponse -> {
                        throwIfAnalyzeStatusInvalid(modelSimpleResponse.getValue());
                        return toRecognizedLayout(modelSimpleResponse.getValue().getAnalyzeResult(), true);
                    })
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<List<RecognizedForm>>>
        fetchAnalyzeFormResultOperation(String modelId, boolean includeFieldElements, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");
                UUID resultUid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                UUID modelUid = UUID.fromString(modelId);
                return service.getAnalyzeFormResultWithResponseAsync(modelUid, resultUid, context)
                    .map(modelSimpleResponse -> {
                        throwIfAnalyzeStatusInvalid(modelSimpleResponse.getValue());
                        return toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(),
                            includeFieldElements);
                    })
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    /**
     * Helper method that throws a {@link FormRecognizerException} if {@link AnalyzeOperationResult#getStatus()} is
     * {@link OperationStatus#FAILED}.
     *
     * @param analyzeResponse The response returned from the service.
     */
    private void throwIfAnalyzeStatusInvalid(AnalyzeOperationResult analyzeResponse) {
        if (OperationStatus.FAILED.equals(analyzeResponse.getStatus())) {
            List<ErrorInformation> errorInformationList = analyzeResponse.getAnalyzeResult().getErrors();
            if (!CoreUtils.isNullOrEmpty(errorInformationList)) {
                throw logger.logExceptionAsError(new FormRecognizerException("Analyze operation failed",
                    errorInformationList));
            }
        }
    }

    private Function<PollingContext<OperationResult>, Mono<PollResponse<OperationResult>>>
        createAnalyzeFormPollOperation(String modelId, Context context) {
        return (pollingContext) -> {
            try {
                PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
                Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");
                UUID resultUid = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                UUID modelUid = UUID.fromString(modelId);
                return service.getAnalyzeFormResultWithResponseAsync(modelUid, resultUid, context)
                    .flatMap(modelSimpleResponse -> processAnalyzeModelResponse(modelSimpleResponse,
                        operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (HttpResponseException e) {
                logger.logExceptionAsError(e);
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> analyzeFormActivationOperation(
        String formUrl, String modelId, boolean includeFieldElements, Context context) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(formUrl, "'formUrl' is required and cannot be null.");
                Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");
                return service.analyzeWithCustomModelWithResponseAsync(UUID.fromString(modelId), includeFieldElements,
                    new SourcePath().setSource(formUrl), context)
                    .map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation())))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> analyzeFormStreamActivationOperation(
        Flux<ByteBuffer> form, String modelId, long length, FormContentType contentType,
        boolean includeFieldElements, Context context) {

        return pollingContext -> {
            try {
                Objects.requireNonNull(form, "'form' is required and cannot be null.");
                Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");
                if (contentType != null) {
                    return service.analyzeWithCustomModelWithResponseAsync(UUID.fromString(modelId),
                        ContentType.fromString(contentType.toString()), form, length, includeFieldElements, context)
                        .map(response -> new OperationResult(parseModelId(
                            response.getDeserializedHeaders().getOperationLocation())))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
                } else {
                    return detectContentType(form)
                        .flatMap(detectedContentType ->
                            service.analyzeWithCustomModelWithResponseAsync(UUID.fromString(modelId),
                                detectedContentType, form, length, includeFieldElements, context)
                                .map(response -> new OperationResult(
                                    parseModelId(response.getDeserializedHeaders().getOperationLocation()))))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
                }
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private static Mono<PollResponse<OperationResult>> processAnalyzeModelResponse(
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
                status = LongRunningOperationStatus.FAILED;
                break;
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
