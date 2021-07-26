// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.ContentType;
import com.azure.ai.formrecognizer.implementation.models.Language;
import com.azure.ai.formrecognizer.implementation.models.Locale;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.implementation.models.SourcePath;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
import com.azure.ai.formrecognizer.models.FormRecognizerLocale;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizeBusinessCardsOptions;
import com.azure.ai.formrecognizer.models.RecognizeContentOptions;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizeIdentityDocumentOptions;
import com.azure.ai.formrecognizer.models.RecognizeInvoicesOptions;
import com.azure.ai.formrecognizer.models.RecognizeReceiptsOptions;
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
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.Transforms.toRecognizedForm;
import static com.azure.ai.formrecognizer.Transforms.toRecognizedLayout;
import static com.azure.ai.formrecognizer.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.formrecognizer.implementation.Utility.detectContentType;
import static com.azure.ai.formrecognizer.implementation.Utility.parseModelId;
import static com.azure.ai.formrecognizer.implementation.Utility.urlActivationOperation;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Form Recognizer.
 * Operations allowed by the client are recognizing receipt, business card, invoice and identity document data from
 * input documents, extracting layout information, analyzing custom forms for predefined data.
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
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
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
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-RecognizeCustomFormsOptions}
     *
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param formUrl The source URL to the input form.
     * @param recognizeCustomFormsOptions The additional configurable
     * {@link RecognizeCustomFormsOptions options} that may be passed when recognizing custom forms.
     *
     * @return A {@link PollerFlux} that polls the recognize custom form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String modelId, String formUrl,
        RecognizeCustomFormsOptions recognizeCustomFormsOptions) {
        return beginRecognizeCustomFormsFromUrl(formUrl, modelId, recognizeCustomFormsOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String formUrl, String modelId,
        RecognizeCustomFormsOptions recognizeCustomFormsOptions,
        Context context) {
        try {
            Objects.requireNonNull(formUrl, "'formUrl' is required and cannot be null.");
            Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");

            final RecognizeCustomFormsOptions finalRecognizeCustomFormsOptions
                = getRecognizeCustomFormOptions(recognizeCustomFormsOptions);
            final boolean isFieldElementsIncluded = finalRecognizeCustomFormsOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                finalRecognizeCustomFormsOptions.getPollInterval(),
                urlActivationOperation(() ->
                        service.analyzeWithCustomModelWithResponseAsync(UUID.fromString(modelId),
                            isFieldElementsIncluded,
                            finalRecognizeCustomFormsOptions.getPages(),
                            new SourcePath().setSource(formUrl),
                            context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    logger),
                pollingOperation(resultUid ->
                    service.getAnalyzeFormResultWithResponseAsync(UUID.fromString(modelId), resultUid, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeFormResultWithResponseAsync(
                    UUID.fromString(modelId), resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(),
                            isFieldElementsIncluded,
                            modelId))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
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
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
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
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long-RecognizeCustomFormsOptions}
     *
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param form The data of the form to recognize form information from.
     * @param length The exact length of the data.
     * @param recognizeCustomFormsOptions The additional configurable
     * {@link RecognizeCustomFormsOptions options} that may be passed when recognizing custom forms.
     *
     * @return A {@link PollerFlux} that polls the recognize custom form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(String modelId, Flux<ByteBuffer> form, long length,
        RecognizeCustomFormsOptions recognizeCustomFormsOptions) {
        return beginRecognizeCustomForms(modelId, form, length, recognizeCustomFormsOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(String modelId, Flux<ByteBuffer> form, long length,
        RecognizeCustomFormsOptions recognizeCustomFormsOptions, Context context) {
        try {
            Objects.requireNonNull(form, "'form' is required and cannot be null.");
            Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");

            final RecognizeCustomFormsOptions finalRecognizeCustomFormsOptions
                = getRecognizeCustomFormOptions(recognizeCustomFormsOptions);
            final boolean isFieldElementsIncluded = finalRecognizeCustomFormsOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                finalRecognizeCustomFormsOptions.getPollInterval(),
                streamActivationOperation(
                    contentType -> service.analyzeWithCustomModelWithResponseAsync(UUID.fromString(modelId),
                        ContentType.fromString(contentType.toString()),
                        isFieldElementsIncluded,
                        finalRecognizeCustomFormsOptions.getPages(),
                        form,
                        length,
                        context)
                        .map(response ->
                            new FormRecognizerOperationResult(
                                parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    form, finalRecognizeCustomFormsOptions.getContentType()),
                pollingOperation(
                    resultUuid -> service.getAnalyzeFormResultWithResponseAsync(
                        UUID.fromString(modelId), resultUuid, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeFormResultWithResponseAsync(
                    UUID.fromString(modelId), resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(),
                            isFieldElementsIncluded,
                            modelId))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
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
     * has been cancelled. The completed operation returns a list of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContentFromUrl(String formUrl) {
        return beginRecognizeContentFromUrl(formUrl, null);
    }

    /**
     * Recognizes layout data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p>Content recognition supports auto language identification and multilanguage documents, so only
     * provide a language code if you would like to force the documented to be processed as
     * that specific language in the {@link RecognizeContentOptions options}.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-RecognizeContentOptions}
     *
     * @param formUrl The source URL to the input form.
     * @param recognizeContentOptions The additional configurable {@link RecognizeContentOptions options}
     * that may be passed when recognizing content/layout on a form.
     *
     * @return A {@link PollerFlux} that polls the recognized content/layout operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl, RecognizeContentOptions recognizeContentOptions) {
        return beginRecognizeContentFromUrl(formUrl, recognizeContentOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl, RecognizeContentOptions recognizeContentOptions, Context context) {
        try {
            Objects.requireNonNull(formUrl, "'formUrl' is required and cannot be null.");

            RecognizeContentOptions finalRecognizeContentOptions = getRecognizeContentOptions(recognizeContentOptions);
            return new PollerFlux<>(
                finalRecognizeContentOptions.getPollInterval(),
                urlActivationOperation(
                    () -> service.analyzeLayoutAsyncWithResponseAsync(
                        finalRecognizeContentOptions.getPages(),
                        finalRecognizeContentOptions.getLanguage() == null
                            ? null : Language.fromString(finalRecognizeContentOptions.getLanguage().toString()),
                        finalRecognizeContentOptions.getReadingOrder() != null
                            ? com.azure.ai.formrecognizer.implementation.models.ReadingOrder.fromString(
                            finalRecognizeContentOptions.getReadingOrder().toString())
                            : null,
                        new SourcePath().setSource(formUrl),
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    logger),
                pollingOperation(resultId -> service.getAnalyzeLayoutResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) ->
                    monoError(logger, new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeLayoutResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedLayout(modelSimpleResponse.getValue().getAnalyzeResult(), true))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
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
     * been cancelled. The completed operation returns a list of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContent(
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
     * <p>Content recognition supports auto language identification and multilanguage documents, so only
     * provide a language code if you would like to force the documented to be processed as
     * that specific language in the {@link RecognizeContentOptions options}.</p>

     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-RecognizeContentOptions}
     *
     * @param form The data of the form to recognize content information from.
     * @param length The exact length of the data.
     * @param recognizeContentOptions The additional configurable {@link RecognizeContentOptions options}
     * that may be passed when recognizing content/layout on a form.
     *
     * @return A {@link PollerFlux} polls the recognize content operation until it has completed, has failed, or has
     * been cancelled. The completed operation returns a list of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContent(Flux<ByteBuffer> form,
        long length, RecognizeContentOptions recognizeContentOptions) {
        return beginRecognizeContent(form, length, recognizeContentOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContent(Flux<ByteBuffer> form, long length,
        RecognizeContentOptions recognizeContentOptions, Context context) {
        try {
            Objects.requireNonNull(form, "'form' is required and cannot be null.");
            RecognizeContentOptions finalRecognizeContentOptions = getRecognizeContentOptions(recognizeContentOptions);
            return new PollerFlux<>(
                finalRecognizeContentOptions.getPollInterval(),
                streamActivationOperation(
                    contentType -> service.analyzeLayoutAsyncWithResponseAsync(contentType,
                        finalRecognizeContentOptions.getPages(),
                        finalRecognizeContentOptions.getLanguage() == null
                            ? null : Language.fromString(finalRecognizeContentOptions.getLanguage().toString()),
                        finalRecognizeContentOptions.getReadingOrder() != null
                            ? com.azure.ai.formrecognizer.implementation.models.ReadingOrder.fromString(
                            finalRecognizeContentOptions.getReadingOrder().toString())
                            : null,
                        form,
                        length,
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    form, finalRecognizeContentOptions.getContentType()),
                pollingOperation(resultId -> service.getAnalyzeLayoutResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) ->
                    monoError(logger, new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeLayoutResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedLayout(modelSimpleResponse.getValue().getAnalyzeResult(), true))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
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
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
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
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string-RecognizeReceiptsOptions}
     *
     * @param receiptUrl The source URL to the input receipt.
     * @param recognizeReceiptsOptions The additional configurable {@link RecognizeReceiptsOptions options}
     * that may be passed when analyzing a receipt.
     *
     * @return A {@link PollerFlux} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl, RecognizeReceiptsOptions recognizeReceiptsOptions) {
        return beginRecognizeReceiptsFromUrl(receiptUrl, recognizeReceiptsOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl,
        RecognizeReceiptsOptions recognizeReceiptsOptions, Context context) {
        try {
            Objects.requireNonNull(receiptUrl, "'receiptUrl' is required and cannot be null.");

            final RecognizeReceiptsOptions finalRecognizeReceiptsOptions
                = getRecognizeReceiptOptions(recognizeReceiptsOptions);
            final boolean isFieldElementsIncluded = finalRecognizeReceiptsOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo  = finalRecognizeReceiptsOptions.getLocale();
            return new PollerFlux<>(
                finalRecognizeReceiptsOptions.getPollInterval(),
                urlActivationOperation(
                    () -> service.analyzeReceiptAsyncWithResponseAsync(isFieldElementsIncluded,
                        localeInfo == null ? null : Locale.fromString(localeInfo.toString()),
                        finalRecognizeReceiptsOptions.getPages(),
                        new SourcePath().setSource(receiptUrl),
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    logger),
                pollingOperation(resultId -> service.getAnalyzeReceiptResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeReceiptResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(),
                            isFieldElementsIncluded,
                            null))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
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
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receipt} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceipts(
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
     * Note that the {@code receipt} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-RecognizeReceiptsOptions}
     *
     * @param receipt The data of the document to recognize receipt information from.
     * @param length The exact length of the data.
     * @param recognizeReceiptsOptions The additional configurable {@link RecognizeReceiptsOptions options}
     * that may be passed when analyzing a receipt.
     *
     * @return A {@link PollerFlux} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receipt} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeReceipts(Flux<ByteBuffer> receipt, long length,
        RecognizeReceiptsOptions recognizeReceiptsOptions) {
        return beginRecognizeReceipts(receipt, length, recognizeReceiptsOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeReceipts(Flux<ByteBuffer> receipt, long length,
        RecognizeReceiptsOptions recognizeReceiptsOptions,
        Context context) {
        try {
            Objects.requireNonNull(receipt, "'receipt' is required and cannot be null.");
            final RecognizeReceiptsOptions finalRecognizeReceiptsOptions
                = getRecognizeReceiptOptions(recognizeReceiptsOptions);
            final boolean isFieldElementsIncluded = finalRecognizeReceiptsOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo  = finalRecognizeReceiptsOptions.getLocale();
            return new PollerFlux<>(
                finalRecognizeReceiptsOptions.getPollInterval(),
                streamActivationOperation(
                    (contentType -> service.analyzeReceiptAsyncWithResponseAsync(
                        contentType,
                        isFieldElementsIncluded,
                        localeInfo == null ? null : Locale.fromString(localeInfo.toString()),
                        finalRecognizeReceiptsOptions.getPages(),
                        receipt,
                        length,
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                    receipt, finalRecognizeReceiptsOptions.getContentType()),
                pollingOperation(resultId -> service.getAnalyzeReceiptResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeReceiptResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(),
                            isFieldElementsIncluded,
                            null))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Recognizes business card data using optical character recognition (OCR) and a prebuilt business card trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://aka.ms/formrecognizer/businesscardfields">here</a> for fields found on a business card.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl#string}
     *
     * @param businessCardUrl The source URL to the input business card.
     *
     * @return A {@link PollerFlux} that polls the recognize business card operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code businessCardUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCardsFromUrl(
        String businessCardUrl) {
        return beginRecognizeBusinessCardsFromUrl(businessCardUrl, null);
    }

    /**
     * Recognizes business card data using optical character recognition (OCR) and a prebuilt business card trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://aka.ms/formrecognizer/businesscardfields">here</a> for fields found on a business card.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl#string-RecognizeBusinessCardsOptions}
     *
     * @param businessCardUrl The source URL to the input business card.
     * @param recognizeBusinessCardsOptions The additional configurable {@link RecognizeBusinessCardsOptions options}
     * that may be passed when analyzing a business card.
     *
     * @return A {@link PollerFlux} that polls the recognize business card operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code businessCardUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCardsFromUrl(
        String businessCardUrl, RecognizeBusinessCardsOptions recognizeBusinessCardsOptions) {
        return beginRecognizeBusinessCardsFromUrl(businessCardUrl, recognizeBusinessCardsOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCardsFromUrl(
        String businessCardUrl, RecognizeBusinessCardsOptions recognizeBusinessCardsOptions, Context context) {
        try {
            Objects.requireNonNull(businessCardUrl, "'businessCardUrl' is required and cannot be null.");

            final RecognizeBusinessCardsOptions finalRecognizeBusinessCardsOptions
                = getRecognizeBusinessCardsOptions(recognizeBusinessCardsOptions);
            final boolean isFieldElementsIncluded = finalRecognizeBusinessCardsOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo = finalRecognizeBusinessCardsOptions.getLocale();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                urlActivationOperation(
                    () -> service.analyzeBusinessCardAsyncWithResponseAsync(isFieldElementsIncluded,
                        localeInfo == null ? null : Locale.fromString(localeInfo.toString()),
                        finalRecognizeBusinessCardsOptions.getPages(),
                        new SourcePath().setSource(businessCardUrl),
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    logger),
                pollingOperation(resultId -> service.getAnalyzeBusinessCardResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeBusinessCardResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(),
                        isFieldElementsIncluded,
                        null))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Recognizes business card data using optical character recognition (OCR) and a prebuilt business card
     * trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://aka.ms/formrecognizer/businesscardfields">here</a> for fields found on a business card.
     *
     * Note that the {@code businessCard} passed must be replayable if retries are enabled (the default).
     * In other words, the {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCards#Flux-long}
     *
     * @param businessCard The data of the document to recognize business card information from.
     * @param length The exact length of the data.
     *
     * @return A {@link PollerFlux} that polls the recognize business card operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code businessCard} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCards(
        Flux<ByteBuffer> businessCard, long length) {
        return beginRecognizeBusinessCards(businessCard, length, null);
    }

    /**
     * Recognizes business card data from documents using optical character recognition (OCR)
     * and a prebuilt business card trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://aka.ms/formrecognizer/businesscardfields">here</a> for fields found on a business card.
     *
     * Note that the {@code businessCard} passed must be replayable if retries are enabled (the default).
     * In other words, the {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCards#Flux-long-RecognizeBusinessCardsOptions}
     *
     * @param businessCard The data of the document to recognize business card information from.
     * @param length The exact length of the data.
     * @param recognizeBusinessCardsOptions The additional configurable {@link RecognizeBusinessCardsOptions options}
     * that may be passed when analyzing a business card.
     *
     * @return A {@link PollerFlux} that polls the recognize business card operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code businessCard} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCards(
        Flux<ByteBuffer> businessCard, long length, RecognizeBusinessCardsOptions recognizeBusinessCardsOptions) {
        return beginRecognizeBusinessCards(businessCard, length, recognizeBusinessCardsOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCards(
        Flux<ByteBuffer> businessCard, long length, RecognizeBusinessCardsOptions recognizeBusinessCardsOptions,
        Context context) {
        try {
            Objects.requireNonNull(businessCard, "'businessCard' is required and cannot be null.");
            final RecognizeBusinessCardsOptions finalRecognizeBusinessCardsOptions
                = getRecognizeBusinessCardsOptions(recognizeBusinessCardsOptions);
            final boolean isFieldElementsIncluded = finalRecognizeBusinessCardsOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo = finalRecognizeBusinessCardsOptions.getLocale();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                streamActivationOperation(
                    (contentType -> service.analyzeBusinessCardAsyncWithResponseAsync(
                        contentType,
                        isFieldElementsIncluded,
                        localeInfo == null ? null : Locale.fromString(localeInfo.toString()),
                        finalRecognizeBusinessCardsOptions.getPages(),
                        businessCard,
                        length,
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                    businessCard, finalRecognizeBusinessCardsOptions.getContentType()),
                pollingOperation(resultId -> service.getAnalyzeBusinessCardResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeBusinessCardResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(),
                        isFieldElementsIncluded,
                        null))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Analyze identity documents using optical character recognition (OCR) and a prebuilt model trained on identity
     * documents model to extract key information from passports and US driver licenses.
     * See <a href="https://aka.ms/formrecognizer/iddocumentfields">here</a> for fields found on an identity document.
     *
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl#string}
     *
     * @param identityDocumentUrl The source URL to the input identity document.
     *
     * @return A {@link PollerFlux} that polls the recognize identity document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code identityDocumentUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocumentsFromUrl(
        String identityDocumentUrl) {
        return beginRecognizeIdentityDocumentsFromUrl(identityDocumentUrl, null);
    }

    /**
     * Analyze identity documents using optical character recognition (OCR) and a prebuilt model trained on identity
     * documents model to extract key information from passports and US driver licenses.
     * See <a href="https://aka.ms/formrecognizer/iddocumentfields">here</a> for fields found on an identity document.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl#string-RecognizeIdentityDocumentOptions}
     *
     * @param identityDocumentUrl The source URL to the input identity document.
     * @param recognizeIdentityDocumentOptions The additional configurable
     * {@link RecognizeIdentityDocumentOptions options} that may be passed when analyzing an identity document.
     *
     * @return A {@link PollerFlux} that polls the analyze identity document operation until it has completed, has
     * failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code identityDocumentUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocumentsFromUrl(
        String identityDocumentUrl, RecognizeIdentityDocumentOptions recognizeIdentityDocumentOptions) {
        return beginRecognizeIdentityDocumentsFromUrl(identityDocumentUrl, recognizeIdentityDocumentOptions,
            Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocumentsFromUrl(
        String identityDocumentUrl, RecognizeIdentityDocumentOptions recognizeIdentityDocumentOptions,
        Context context) {
        try {
            Objects.requireNonNull(identityDocumentUrl, "'identityDocumentUrl' is required and cannot be null.");

            final RecognizeIdentityDocumentOptions finalRecognizeIdentityDocumentOptions
                = getRecognizeIdentityDocumentOptions(recognizeIdentityDocumentOptions);
            final boolean isFieldElementsIncluded = finalRecognizeIdentityDocumentOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                urlActivationOperation(
                    () -> service.analyzeIdDocumentAsyncWithResponseAsync(isFieldElementsIncluded,
                        finalRecognizeIdentityDocumentOptions.getPages(),
                        new SourcePath().setSource(identityDocumentUrl),
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    logger),
                pollingOperation(resultId -> service.getAnalyzeIdDocumentResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeIdDocumentResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(),
                        isFieldElementsIncluded,
                        null))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Analyze identity documents using optical character recognition (OCR) and a prebuilt model trained on identity
     * documents model to extract key information from passports and US driver licenses.
     * See <a href="https://aka.ms/formrecognizer/iddocumentfields">here</a> for fields found on an identity document.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * Note that the {@code identityDocument} passed must be replayable if retries are enabled (the default).
     * In other words, the {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocuments#Flux-long}
     *
     * @param identityDocument The data of the document to recognize identity document information from.
     * @param length The exact length of the data.
     *
     * @return A {@link PollerFlux} that polls the recognize identity document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code identityDocument} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocuments(
        Flux<ByteBuffer> identityDocument, long length) {
        return beginRecognizeIdentityDocuments(identityDocument, length, null);
    }

    /**
     * Analyze identity documents using optical character recognition (OCR) and a prebuilt model trained on identity
     * documents model to extract key information from passports and US driver licenses.
     * See <a href="https://aka.ms/formrecognizer/iddocumentfields">here</a> for fields found on an identity document.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * Note that the {@code identityDocument} passed must be replayable if retries are enabled (the default).
     * In other words, the {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocuments#Flux-long-RecognizeIdentityDocumentOptions}
     *
     * @param identityDocument The data of the document to recognize identity document information from.
     * @param length The exact length of the data.
     * @param recognizeIdentityDocumentOptions The additional configurable
     * {@link RecognizeIdentityDocumentOptions options} that may be passed when analyzing an identity document.
     *
     * @return A {@link PollerFlux} that polls the recognize identity document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code identityDocument} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocuments(
        Flux<ByteBuffer> identityDocument, long length,
        RecognizeIdentityDocumentOptions recognizeIdentityDocumentOptions) {
        return beginRecognizeIdentityDocuments(identityDocument, length, recognizeIdentityDocumentOptions,
            Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocuments(
        Flux<ByteBuffer> identityDocument, long length,
        RecognizeIdentityDocumentOptions recognizeIdentityDocumentOptions, Context context) {
        try {
            Objects.requireNonNull(identityDocument, "'identityDocument' is required and cannot be null.");
            final RecognizeIdentityDocumentOptions finalRecognizeIdentityDocumentOptions
                = getRecognizeIdentityDocumentOptions(recognizeIdentityDocumentOptions);
            final boolean isFieldElementsIncluded = finalRecognizeIdentityDocumentOptions.isFieldElementsIncluded();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                streamActivationOperation(
                    (contentType -> service.analyzeIdDocumentAsyncWithResponseAsync(
                        contentType,
                        isFieldElementsIncluded,
                        finalRecognizeIdentityDocumentOptions.getPages(),
                        identityDocument,
                        length,
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                    identityDocument, finalRecognizeIdentityDocumentOptions.getContentType()),
                pollingOperation(resultId -> service.getAnalyzeIdDocumentResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeIdDocumentResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(),
                        isFieldElementsIncluded,
                        null))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /*
     * Poller's ACTIVATION operation that takes stream as input.
     */
    private Function<PollingContext<FormRecognizerOperationResult>, Mono<FormRecognizerOperationResult>>
        streamActivationOperation(
        Function<ContentType, Mono<FormRecognizerOperationResult>> activationOperation, Flux<ByteBuffer> form,
        FormContentType contentType) {
        return pollingContext -> {
            try {
                Objects.requireNonNull(form, "'form' is required and cannot be null.");
                if (contentType != null) {
                    return activationOperation.apply(ContentType.fromString(contentType.toString()))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
                } else {
                    return detectContentType(form)
                        .flatMap(activationOperation::apply)
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
                }
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    /*
     * Poller's POLLING operation.
     */
    private Function<PollingContext<FormRecognizerOperationResult>, Mono<PollResponse<FormRecognizerOperationResult>>>
        pollingOperation(
        Function<UUID, Mono<Response<AnalyzeOperationResult>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<FormRecognizerOperationResult> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID resultUuid = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                return pollingFunction.apply(resultUuid)
                    .flatMap(modelResponse -> processAnalyzeModelResponse(modelResponse,
                        operationResultPollResponse))
                    .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    /*
     * Poller's FETCHING operation.
     */
    private Function<PollingContext<FormRecognizerOperationResult>, Mono<Response<AnalyzeOperationResult>>>
        fetchingOperation(
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

    private Mono<PollResponse<FormRecognizerOperationResult>> processAnalyzeModelResponse(
        Response<AnalyzeOperationResult> analyzeOperationResultResponse,
        PollResponse<FormRecognizerOperationResult> operationResultPollResponse) {
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
                            new FormRecognizerErrorInformation(errorInformation.getCode(),
                                errorInformation.getMessage()))
                        .collect(Collectors.toList())));
            default:
                status = LongRunningOperationStatus.fromString(
                    analyzeOperationResultResponse.getValue().getStatus().toString(), true);
                break;
        }
        return Mono.just(new PollResponse<>(status, operationResultPollResponse.getValue()));
    }

    /**
     * Recognizes invoice data using optical character recognition (OCR) and a prebuilt invoice trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://aka.ms/formrecognizer/invoicefields">here</a> for fields found on a invoice.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoicesFromUrl#string}
     *
     * @param invoiceUrl The URL of the invoice to analyze.
     *
     * @return A {@link PollerFlux} that polls the recognize invoice operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code invoiceUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeInvoicesFromUrl(String invoiceUrl) {
        return beginRecognizeInvoicesFromUrl(invoiceUrl, null);
    }

    /**
     * Recognizes invoice data using optical character recognition (OCR) and a prebuilt invoice trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoicesFromUrl#string-RecognizeInvoicesOptions}
     *
     * @param invoiceUrl The source URL to the input invoice.
     * @param recognizeInvoicesOptions The additional configurable {@link RecognizeInvoicesOptions options}
     * that may be passed when analyzing a invoice.
     *
     * @return A {@link PollerFlux} that polls the recognize invoice operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code invoiceUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeInvoicesFromUrl(String invoiceUrl, RecognizeInvoicesOptions recognizeInvoicesOptions) {
        return beginRecognizeInvoicesFromUrl(invoiceUrl, recognizeInvoicesOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeInvoicesFromUrl(String invoiceUrl,
        RecognizeInvoicesOptions recognizeInvoicesOptions, Context context) {
        try {
            Objects.requireNonNull(invoiceUrl, "'invoiceUrl' is required and cannot be null.");

            final RecognizeInvoicesOptions finalRecognizeInvoicesOptions
                = getRecognizeInvoicesOptions(recognizeInvoicesOptions);
            final boolean isFieldElementsIncluded = finalRecognizeInvoicesOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo  = finalRecognizeInvoicesOptions.getLocale();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                urlActivationOperation(
                    () -> service.analyzeInvoiceAsyncWithResponseAsync(isFieldElementsIncluded,
                        localeInfo == null ? null : Locale.fromString(localeInfo.toString()),
                        finalRecognizeInvoicesOptions.getPages(),
                        new SourcePath().setSource(invoiceUrl),
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                    logger),
                pollingOperation(resultId -> service.getAnalyzeInvoiceResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeInvoiceResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(),
                            isFieldElementsIncluded,
                            null))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /**
     * Recognizes invoice data using optical character recognition (OCR) and a prebuilt invoice
     * trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://aka.ms/formrecognizer/invoicefields">here</a> for fields found on a invoice.
     *
     * Note that the {@code invoice} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoices#Flux-long}
     *
     * @param invoice The data of the document to recognize invoice information from.
     * @param length The exact length of the data.
     *
     * @return A {@link PollerFlux} that polls the recognize invoice operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code invoice} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoices(
        Flux<ByteBuffer> invoice, long length) {
        return beginRecognizeInvoices(invoice, length, null);
    }

    /**
     * Recognizes invoice data from documents using optical character recognition (OCR)
     * and a prebuilt invoice trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     * See <a href="https://aka.ms/formrecognizer/invoicefields">here</a> for fields found on a invoice.
     *
     * Note that the {@code invoice} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoices#Flux-long-RecognizeInvoicesOptions}
     *
     * @param invoice The data of the document to recognize invoice information from.
     * @param length The exact length of the data.
     * @param recognizeInvoicesOptions The additional configurable {@link RecognizeInvoicesOptions options}
     * that may be passed when analyzing a invoice.
     *
     * @return A {@link PollerFlux} that polls the recognize invoice operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code invoice} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeInvoices(Flux<ByteBuffer> invoice, long length,
        RecognizeInvoicesOptions recognizeInvoicesOptions) {
        return beginRecognizeInvoices(invoice, length, recognizeInvoicesOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeInvoices(Flux<ByteBuffer> invoice, long length,
        RecognizeInvoicesOptions recognizeInvoicesOptions,
        Context context) {
        try {
            Objects.requireNonNull(invoice, "'invoice' is required and cannot be null.");
            final RecognizeInvoicesOptions finalRecognizeInvoicesOptions
                = getRecognizeInvoicesOptions(recognizeInvoicesOptions);
            final boolean isFieldElementsIncluded = finalRecognizeInvoicesOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo  = finalRecognizeInvoicesOptions.getLocale();
            return new PollerFlux<>(
                DEFAULT_POLL_INTERVAL,
                streamActivationOperation(
                    (contentType -> service.analyzeInvoiceAsyncWithResponseAsync(
                        contentType,
                        isFieldElementsIncluded,
                        localeInfo == null ? null : Locale.fromString(localeInfo.toString()),
                        finalRecognizeInvoicesOptions.getPages(),
                        invoice,
                        length,
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                    invoice, finalRecognizeInvoicesOptions.getContentType()),
                pollingOperation(resultId -> service.getAnalyzeInvoiceResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> service.getAnalyzeInvoiceResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(),
                            isFieldElementsIncluded,
                            null))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private static RecognizeCustomFormsOptions
        getRecognizeCustomFormOptions(RecognizeCustomFormsOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeCustomFormsOptions() : userProvidedOptions;
    }

    private static RecognizeContentOptions
        getRecognizeContentOptions(RecognizeContentOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeContentOptions() : userProvidedOptions;
    }

    private static RecognizeReceiptsOptions
        getRecognizeReceiptOptions(RecognizeReceiptsOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeReceiptsOptions() : userProvidedOptions;
    }

    private static RecognizeBusinessCardsOptions getRecognizeBusinessCardsOptions(
        RecognizeBusinessCardsOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeBusinessCardsOptions() : userProvidedOptions;
    }

    private static RecognizeInvoicesOptions
        getRecognizeInvoicesOptions(RecognizeInvoicesOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeInvoicesOptions() : userProvidedOptions;
    }

    private static RecognizeIdentityDocumentOptions getRecognizeIdentityDocumentOptions(
        RecognizeIdentityDocumentOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeIdentityDocumentOptions() : userProvidedOptions;
    }
}
