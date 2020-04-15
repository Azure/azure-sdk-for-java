// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.ContentType;
import com.azure.ai.formrecognizer.implementation.models.ErrorInformation;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.implementation.models.SourcePath;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static com.azure.ai.formrecognizer.FormRecognizerClientBuilder.DEFAULT_DURATION;
import static com.azure.ai.formrecognizer.Transforms.toReceipt;
import static com.azure.ai.formrecognizer.Transforms.toRecognizedForm;
import static com.azure.ai.formrecognizer.Transforms.toRecognizedLayout;
import static com.azure.ai.formrecognizer.implementation.Utility.parseModelId;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Form Recognizer.
 * Operations allowed by the client are recognizing receipt data from documents, extracting layout information and
 * analyzing custom forms for predefined data.
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
     * Creates a new {@link FormTrainingAsyncClient} object. The new {@code FormTrainingAsyncClient}
     * uses the same request policy pipeline as the {@code FormRecognizerAsyncClient}.
     *
     * @return A new {@link FormTrainingAsyncClient} object.
     */
    public FormTrainingAsyncClient getFormTrainingAsyncClient() {
        return new FormTrainingAsyncClient(this.service, this.serviceVersion);
    }

    /**
     * Creates a new {@link FormTrainingAsyncClient} object. The new {@code FormTrainingAsyncClient}
     * uses the same request policy pipeline as the {@code FormRecognizerAsyncClient}.
     *
     * @return A new {@link FormTrainingAsyncClient} object.
     */
    public FormTrainingAsyncClient getFormTrainingAsyncClient() {
        return new FormTrainingAsyncClient(this.service, this.serviceVersion);
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public FormRecognizerServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Recognizes and extracts form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 50 MB.
     * @param modelId The UUID string format custom trained model Id to be used.
     *
     * @return A {@link PollerFlux} that polls the extract custom form operation until it has completed, has failed,
     * or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String fileSourceUrl, String modelId) {
        return beginRecognizeCustomFormsFromUrl(fileSourceUrl, modelId, false, null);
    }

    /**
     * Recognizes and extracts form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 50 MB.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link PollerFlux} that polls the extract custom form operation until it has completed, has failed,
     * or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String fileSourceUrl, String modelId, boolean includeTextDetails,
        Duration pollInterval) {
        Objects.requireNonNull(fileSourceUrl, "'fileSourceUrl' is required and cannot be null.");
        Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");
        final Duration interval = pollInterval != null ? pollInterval : DEFAULT_DURATION;
        return new PollerFlux<OperationResult, IterableStream<RecognizedForm>>(
            interval,
            analyzeFormActivationOperation(fileSourceUrl, modelId, includeTextDetails),
            createAnalyzeFormPollOperation(modelId),
            (activationResponse, context) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchAnalyzeFormResultOperation(modelId, includeTextDetails));
    }

    /**
     * Recognizes and extracts form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<RecognizedForm>>
        beginRecognizeCustomForms(Flux<ByteBuffer> data, String modelId, long length, FormContentType formContentType) {
        return beginRecognizeCustomForms(data, modelId, length, formContentType, false, null);
    }

    /**
     * Recognizes and extracts form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param modelId The UUID string format custom trained model Id to be used.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<RecognizedForm>>
        beginRecognizeCustomForms(Flux<ByteBuffer> data, String modelId, long length, FormContentType formContentType,
        boolean includeTextDetails, Duration pollInterval) {
        Objects.requireNonNull(data, "'data' is required and cannot be null.");
        Objects.requireNonNull(modelId, "'modelId' is required and cannot be null.");
        Objects.requireNonNull(formContentType, "'formContentType' is required and cannot be null.");

        final Duration interval = pollInterval != null ? pollInterval : DEFAULT_DURATION;
        return new PollerFlux<OperationResult, IterableStream<RecognizedForm>>(
            interval,
            analyzeFormStreamActivationOperation(data, modelId, length, formContentType, includeTextDetails),
            createAnalyzeFormPollOperation(modelId),
            (activationResponse, context) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchAnalyzeFormResultOperation(modelId, includeTextDetails));
    }

    /**
     * Recognizes and extracts layout data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param fileSourceUrl The source URL to the input document. Size of the file must be less than 50 MB.
     *
     * @return A {@link PollerFlux} that polls the extract custom form operation until it has completed, has failed,
     * or has been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<FormPage>> beginRecognizeContentFromUrl(String fileSourceUrl) {
        return beginRecognizeContentFromUrl(fileSourceUrl, null);
    }

    /**
     * Recognizes and extracts layout data using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 50 MB.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<FormPage>>
        beginRecognizeContentFromUrl(String sourceUrl, Duration pollInterval) {
        Objects.requireNonNull(sourceUrl, "'sourceUrl' is required and cannot be null.");
        final Duration interval = pollInterval != null ? pollInterval : DEFAULT_DURATION;
        return new PollerFlux<OperationResult, IterableStream<FormPage>>(interval,
            contentAnalyzeActivationOperation(sourceUrl),
            extractContentPollOperation(),
            (activationResponse, context) -> monoError(logger,
                new RuntimeException("Cancellation is not supported")),
            fetchExtractContentResult());
    }

    /**
     * Recognizes and extracts layout data using optical character recognition (OCR) and a prebuilt receipt
     * trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<FormPage>> beginRecognizeContent(
        Flux<ByteBuffer> data, long length, FormContentType formContentType) {
        return beginRecognizeContent(data, formContentType, length, null);
    }

    /**
     * Recognizes and extracts layout data using optical character recognition (OCR) and a prebuilt receipt
     * trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<FormPage>>
        beginRecognizeContent(Flux<ByteBuffer> data, FormContentType formContentType, long length,
        Duration pollInterval) {
        Objects.requireNonNull(data, "'data' is required and cannot be null.");
        Objects.requireNonNull(formContentType, "'formContentType' is required and cannot be null.");

        final Duration interval = pollInterval != null ? pollInterval : DEFAULT_DURATION;
        return new PollerFlux<OperationResult, IterableStream<FormPage>>(interval,
            contentStreamActivationOperation(data, length, formContentType),
            extractContentPollOperation(),
            (activationResponse, context) -> monoError(logger,
                new RuntimeException("Cancellation is not supported")),
            fetchExtractContentResult());
    }

    /**
     * Recognizes and extracts receipt data using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 50 MB.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String sourceUrl) {
        return beginRecognizeReceiptsFromUrl(sourceUrl, false, null);
    }

    /**
     * Recognizes and extracts receipt data using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 50 MB.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String sourceUrl, boolean includeTextDetails, Duration pollInterval) {
        Objects.requireNonNull(sourceUrl, "'sourceUrl' is required and cannot be null.");
        final Duration interval = pollInterval != null ? pollInterval : DEFAULT_DURATION;
        return new PollerFlux<OperationResult, IterableStream<RecognizedReceipt>>(interval,
            receiptAnalyzeActivationOperation(sourceUrl, includeTextDetails),
            extractReceiptPollOperation(),
            (activationResponse, context) -> monoError(logger,
                new RuntimeException("Cancellation is not supported")),
            fetchExtractContentResult());
    }

    /**
     * Recognizes and extracts receipt data using optical character recognition (OCR) and a prebuilt receipt
     * trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<RecognizedReceipt>> beginRecognizeReceipts(
        Flux<ByteBuffer> data, long length, FormContentType formContentType) {
        return beginRecognizeReceipts(data, length, formContentType, false, null);
    }

    /**
     * Recognizes and extracts receipt data from documents using optical character recognition (OCR)
     * and a prebuilt receipt trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param data The data of the document to be extract receipt information from.
     * @param length The exact length of the data. Size of the file must be less than 50 MB.
     * @param formContentType Supported Media types including .pdf, .jpg, .png or .tiff type file stream.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<RecognizedReceipt>> beginRecognizeReceipts(
        Flux<ByteBuffer> data, long length, FormContentType formContentType, boolean includeTextDetails,
        Duration pollInterval) {
        Objects.requireNonNull(data, "'data' is required and cannot be null.");
        Objects.requireNonNull(formContentType, "'formContentType' is required and cannot be null.");

        final Duration interval = pollInterval != null ? pollInterval : DEFAULT_DURATION;
        return new PollerFlux<OperationResult, IterableStream<RecognizedReceipt>>(interval,
            receiptStreamActivationOperation(data, length, formContentType, includeTextDetails),
            extractReceiptPollOperation(),
            (activationResponse, context) -> monoError(logger,
                new RuntimeException("Cancellation is not supported")),
            fetchExtractContentResult());
    }

    /**
     * Recognizes and extracts receipt data using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 50 MB.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String sourceUrl) {
        return beginRecognizeReceiptsFromUrl(sourceUrl, false, null);
    }

    /**
     * Recognizes and extracts receipt data using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * @param sourceUrl The source URL to the input document. Size of the file must be less than 50 MB.
     * @param includeTextDetails Include text lines and element references in the result.
     * @param pollInterval Duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return A {@link PollerFlux} that polls the extract receipt operation until it has completed, has failed, or has
     * been cancelled.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<OperationResult, IterableStream<RecognizedReceipt>>
        beginRecognizeReceiptsFromUrl(String sourceUrl, boolean includeTextDetails, Duration pollInterval) {
        final Duration interval = pollInterval != null ? pollInterval : DEFAULT_DURATION;
        return new PollerFlux<OperationResult, IterableStream<RecognizedReceipt>>(interval,
            receiptAnalyzeActivationOperation(sourceUrl, includeTextDetails),
            extractReceiptPollOperation(),
            (activationResponse, context) -> monoError(logger,
                new RuntimeException("Cancellation is not supported")),
            fetchExtractReceiptResult(includeTextDetails));
    }



    private Function<PollingContext<OperationResult>, Mono<OperationResult>> receiptAnalyzeActivationOperation(
        String sourceUrl, boolean includeTextDetails) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(sourceUrl, "'sourceUrl' is required and cannot be null.");
                return service.analyzeReceiptAsyncWithResponseAsync(includeTextDetails,
                    new SourcePath().setSource(sourceUrl))
                    .map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation())));
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> receiptStreamActivationOperation(
        Flux<ByteBuffer> data, long length, FormContentType formContentType, boolean includeTextDetails) {
        return (pollingContext) -> {
            try {
                Objects.requireNonNull(data, "'data' is required and cannot be null.");
                Objects.requireNonNull(formContentType, "'formContentType' is required and cannot be null.");
                return service.analyzeReceiptAsyncWithResponseAsync(ContentType.fromString(formContentType.toString()),
                    data, length, includeTextDetails)
                    .map(response -> new OperationResult(
                        parseModelId(response.getDeserializedHeaders().getOperationLocation())));
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<PollResponse<OperationResult>>>
        extractReceiptPollOperation() {
        return (pollingContext) -> {
            PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
            String modelId = operationResultPollResponse.getValue().getResultId();
            try {
                UUID resultUid = UUID.fromString(modelId);
                return service.getAnalyzeReceiptResultWithResponseAsync(resultUid)
                    .flatMap(modelSimpleResponse -> processAnalyzeModelResponse(modelSimpleResponse,
                        operationResultPollResponse));
            } catch (HttpResponseException e) {
                logger.logExceptionAsError(e);
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<IterableStream<RecognizedReceipt>>>
        fetchExtractReceiptResult(boolean includeTextDetails) {
        return (pollingContext) -> {
            final UUID resultUid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
            return service.getAnalyzeReceiptResultWithResponseAsync(resultUid)
                .map(modelSimpleResponse -> {
                    throwIfAnalyzeStatusInvalid(modelSimpleResponse);
                    return toReceipt(modelSimpleResponse.getValue().getAnalyzeResult(),
                        includeTextDetails);
                });
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> contentAnalyzeActivationOperation(
        String sourceUrl) {
        return (pollingContext) -> {
            try {
                return service.analyzeLayoutAsyncWithResponseAsync(new SourcePath().setSource(sourceUrl))
                    .map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation())));
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> contentStreamActivationOperation(
        Flux<ByteBuffer> buffer, long length, FormContentType formContentType) {
        return (pollingContext) -> {
            try {
                return service.analyzeLayoutAsyncWithResponseAsync(ContentType.fromString(formContentType.toString()),
                    buffer, length)
                    .map(response -> new OperationResult(
                        parseModelId(response.getDeserializedHeaders().getOperationLocation())));
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<PollResponse<OperationResult>>>
        extractContentPollOperation() {
        return (pollingContext) -> {
            PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
            String modelId = operationResultPollResponse.getValue().getResultId();
            try {
                UUID resultUid = UUID.fromString(modelId);
                return service.getAnalyzeLayoutResultWithResponseAsync(resultUid)
                    .flatMap(modelSimpleResponse -> processAnalyzeModelResponse(modelSimpleResponse,
                        operationResultPollResponse));
            } catch (HttpResponseException e) {
                logger.logExceptionAsError(e);
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<IterableStream<FormPage>>>
        fetchExtractContentResult() {
        return (pollingContext) -> {
            final UUID resultUid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
            return service.getAnalyzeLayoutResultWithResponseAsync(resultUid)
                .map(modelSimpleResponse -> {
                    throwIfAnalyzeStatusInvalid(modelSimpleResponse);
                    return new IterableStream<>(
                        toRecognizedLayout(modelSimpleResponse.getValue().getAnalyzeResult(), true));
                });
        };
    }

    private Function<PollingContext<OperationResult>, Mono<IterableStream<RecognizedForm>>>
        fetchAnalyzeFormResultOperation(String modelId, boolean includeTextDetails) {
        return (pollingContext) -> {
            UUID resultUid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
            UUID modelUid = UUID.fromString(modelId);
            return service.getAnalyzeFormResultWithResponseAsync(modelUid, resultUid)
                .map(modelSimpleResponse -> {
                    throwIfAnalyzeStatusInvalid(modelSimpleResponse);
                    return new IterableStream<>(toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(),
                        includeTextDetails));
                });
        };
    }

    /**
     * Helper method that throws a {@link HttpResponseException} if {@link AnalyzeOperationResult#getStatus()} is
     * {@link OperationStatus#FAILED}.
     *
     * @param modelSimpleResponse The response returned from the service.
     */
    private void throwIfAnalyzeStatusInvalid(SimpleResponse<AnalyzeOperationResult> modelSimpleResponse) {
        if (modelSimpleResponse.getValue().getStatus().equals(OperationStatus.FAILED)) {
            List<ErrorInformation> errorInformationList =
                modelSimpleResponse.getValue().getAnalyzeResult().getErrors();
            if (!CoreUtils.isNullOrEmpty(errorInformationList)) {
                throw logger.logExceptionAsError(
                    new HttpResponseException(errorInformationList.get(0).getMessage(), null));
            }
        }
    }

    private Function<PollingContext<OperationResult>, Mono<PollResponse<OperationResult>>>
        createAnalyzeFormPollOperation(String modelId) {
        return (pollingContext) -> {
            PollResponse<OperationResult> operationResultPollResponse = pollingContext.getLatestResponse();
            String resultId = operationResultPollResponse.getValue().getResultId();
            try {
                UUID resultUid = UUID.fromString(resultId);
                UUID modelUid = UUID.fromString(modelId);
                return service.getAnalyzeFormResultWithResponseAsync(modelUid, resultUid)
                    .flatMap(modelSimpleResponse -> processAnalyzeModelResponse(modelSimpleResponse,
                        operationResultPollResponse));
            } catch (HttpResponseException e) {
                logger.logExceptionAsError(e);
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null));
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> analyzeFormActivationOperation(
        String fileSourceUrl, String modelId, boolean includeTextDetails) {
        return (pollingContext) -> {
            try {
                return service.analyzeWithCustomModelWithResponseAsync(UUID.fromString(modelId), includeTextDetails,
                    new SourcePath().setSource(fileSourceUrl))
                    .map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation())));
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    private Function<PollingContext<OperationResult>, Mono<OperationResult>> analyzeFormStreamActivationOperation(
        Flux<ByteBuffer> data, String modelId, long length,
        FormContentType formContentType, boolean includeTextDetails) {
        return (pollingContext) -> {
            try {
                return service.analyzeWithCustomModelWithResponseAsync(UUID.fromString(modelId),
                     ContentType.fromString(formContentType.toString()), data, length, includeTextDetails)
                    .map(response ->
                        new OperationResult(parseModelId(response.getDeserializedHeaders().getOperationLocation())));
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
}
