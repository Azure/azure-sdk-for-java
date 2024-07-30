// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.implementation.AnalyzersImpl;
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
 * <p>This class provides an asynchronous client to connect to the Form Recognizer Azure Cognitive Service.</p>
 * <p>This client provides asynchronous methods to perform:</p>
 *
 * <ol>
 *   <li>Custom Form Analysis: Extraction and analysis of data from forms and documents specific to distinct business
 *   data and use cases. Use the custom trained model by passing its modelId into the
 *   {@link #beginRecognizeCustomForms(String, Flux, long) beginRecognizeCustomForms} method.</li>
 *   <li>Prebuilt Model Analysis: Analyze receipts, business cards, invoices and other documents with
 *   <a href="https://aka.ms/form-recognizer-service-2.1.0">supported prebuilt models</a>
 *   Use the {@link #beginRecognizeReceipts(Flux, long) beginRecognizeReceipts} method to recognize receipt
 *   information.</li>
 *   <li>Layout Analysis: Extraction and analysis of text, selection marks, tables, and bounding box coordinates,
 *   from forms and documents. Use {@link #beginRecognizeContent(Flux, long) beginRecognizeContent} method too perform
 *   layout analysis.</li>
 *   <li>Polling and Callbacks: It includes mechanisms for polling the service to check the status of an analysis
 *   operation or registering callbacks to receive notifications when the analysis is complete.</li>
 * </ol>
 *
 * <p><strong>Note: </strong>This client only supports {@link FormRecognizerServiceVersion#V2_1} and lower.
 * Recommended to use a newer service version, {@link DocumentAnalysisClient} and
 * {@link DocumentModelAdministrationClient}.</p>
 *
 * <p><strong>Refer to the
 * <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/migration-guide.md">Migration guide</a> to use API versions 2022-08-31 and up.</strong></p>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Form Recognizer.
 * {@link FormRecognizerClient} is the synchronous service client and {@link FormRecognizerAsyncClient} is the
 * asynchronous service client. The examples shown in this document use a credential object named DefaultAzureCredential
 * for authentication, which is appropriate for most scenarios, including local development and production environments.
 * Additionally, we recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a FormRecognizerClient with DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link FormRecognizerAsyncClient}, using the
 * `DefaultAzureCredentialBuilder` to configure it.</p>
 *
 * <!-- src_embed readme-sample-createFormRecognizerAsyncClientWithAAD -->
 * <pre>
 * FormRecognizerAsyncClient formRecognizerAsyncClient = new FormRecognizerClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createFormRecognizerAsyncClientWithAAD  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.core.credential.AzureKeyCredential AzureKeyCredential} for client creation.</p>
 *
 * <!-- src_embed readme-sample-createFormRecognizerAsyncClient -->
 * <pre>
 * FormRecognizerAsyncClient formRecognizerAsyncClient = new FormRecognizerClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createFormRecognizerAsyncClient  -->
 *
 * @see com.azure.ai.formrecognizer
 * @see FormRecognizerClientBuilder
 * @see FormRecognizerClient
 */
@ServiceClient(builder = FormRecognizerClientBuilder.class, isAsync = true)
public final class FormRecognizerAsyncClient {
    private final ClientLogger logger = new ClientLogger(FormRecognizerAsyncClient.class);
    private final FormRecognizerClientImpl service;
    private final AnalyzersImpl analyzersImpl;
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
        this.analyzersImpl = analyzersImpl.getAnalyzers();
        this.serviceVersion = serviceVersion;
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model with or without labels.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
     * <pre>
     * String formUrl = &quot;&#123;form_url&#125;&quot;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl&#40;modelId, formUrl&#41;
     *     &#47;&#47; if training polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;recognizedForm -&gt; recognizedForm.getFields&#40;&#41;
     *         .forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
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
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeCustomFormsFromUrl(
        String modelId, String formUrl) {
        return beginRecognizeCustomFormsFromUrl(modelId, formUrl, null);
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-Options -->
     * <pre>
     * String formUrl = &quot;&#123;formUrl&#125;&quot;;
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * boolean includeFieldElements = true;
     *
     * formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl&#40;modelId, formUrl,
     *     new RecognizeCustomFormsOptions&#40;&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;
     *         .setPollInterval&#40;Duration.ofSeconds&#40;10&#41;&#41;&#41;
     *     &#47;&#47; if training polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;recognizedForm -&gt; recognizedForm.getFields&#40;&#41;
     *         .forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-Options -->
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
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeCustomFormsFromUrl(
        String modelId, String formUrl, RecognizeCustomFormsOptions recognizeCustomFormsOptions) {
        return beginRecognizeCustomFormsFromUrl(formUrl, modelId, recognizeCustomFormsOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeCustomFormsFromUrl(String formUrl,
        String modelId, RecognizeCustomFormsOptions recognizeCustomFormsOptions, Context context) {
        try {
            if (formUrl == null) {
                return PollerFlux.error(new NullPointerException("'formUrl' is required and cannot be null."));
            }
            if (modelId == null) {
                return PollerFlux.error(new NullPointerException("'modelId' is required and cannot be null."));
            }

            final RecognizeCustomFormsOptions finalRecognizeCustomFormsOptions
                = getRecognizeCustomFormOptions(recognizeCustomFormsOptions);
            UUID modelUuid = UUID.fromString(modelId);
            final boolean isFieldElementsIncluded = finalRecognizeCustomFormsOptions.isFieldElementsIncluded();
            return new PollerFlux<>(finalRecognizeCustomFormsOptions.getPollInterval(), urlActivationOperation(() ->
                analyzersImpl.(modelUuid, isFieldElementsIncluded,
                            finalRecognizeCustomFormsOptions.getPages(), new SourcePath().setSource(formUrl), context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))), logger),
                pollingOperation(resultUid ->
                    analyzersImpl.getAnalyzeFormResultWithResponseAsync(modelUuid, resultUid, context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeFormResultWithResponseAsync(modelUuid, resultId,
                    context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded,
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long -->
     * <pre>
     * File form = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * Flux&lt;ByteBuffer&gt; buffer = toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;form.toPath&#40;&#41;&#41;&#41;&#41;;
     *
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeCustomForms&#40;modelId, buffer, form.length&#40;&#41;&#41;
     *     &#47;&#47; if training polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;recognizedForm -&gt; recognizedForm.getFields&#40;&#41;
     *         .forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long -->
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
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeCustomForms(String modelId,
        Flux<ByteBuffer> form, long length) {
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long-Options -->
     * <pre>
     * File form = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     * boolean includeFieldElements = true;
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * Flux&lt;ByteBuffer&gt; buffer = toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;form.toPath&#40;&#41;&#41;&#41;&#41;;
     *
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeCustomForms&#40;modelId, buffer, form.length&#40;&#41;,
     *     new RecognizeCustomFormsOptions&#40;&#41;
     *         .setContentType&#40;FormContentType.IMAGE_JPEG&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;
     *         .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;&#41;
     *     &#47;&#47; if training polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;recognizedForm -&gt; recognizedForm.getFields&#40;&#41;
     *         .forEach&#40;&#40;fieldName, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldName&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long-Options -->
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
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeCustomForms(String modelId,
        Flux<ByteBuffer> form, long length, RecognizeCustomFormsOptions recognizeCustomFormsOptions) {
        return beginRecognizeCustomForms(modelId, form, length, recognizeCustomFormsOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(String modelId, Flux<ByteBuffer> form, long length,
        RecognizeCustomFormsOptions recognizeCustomFormsOptions, Context context) {
        try {
            if (form == null) {
                return PollerFlux.error(new NullPointerException("'form' is required and cannot be null."));
            }
            if (modelId == null) {
                return PollerFlux.error(new NullPointerException("'modelId' is required and cannot be null."));
            }

            final RecognizeCustomFormsOptions finalRecognizeCustomFormsOptions
                = getRecognizeCustomFormOptions(recognizeCustomFormsOptions);
            UUID modelUuid = UUID.fromString(modelId);
            final boolean isFieldElementsIncluded = finalRecognizeCustomFormsOptions.isFieldElementsIncluded();
            return new PollerFlux<>(finalRecognizeCustomFormsOptions.getPollInterval(), streamActivationOperation(
                    contentType -> analyzersImpl.analyzeWithCustomModelWithResponseAsync(modelUuid,
                            ContentType.fromString(contentType.toString()), isFieldElementsIncluded,
                            finalRecognizeCustomFormsOptions.getPages(), form, length, context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                form, finalRecognizeCustomFormsOptions.getContentType()),
                pollingOperation(resultUuid -> analyzersImpl.getAnalyzeFormResultWithResponseAsync(modelUuid, resultUuid,
                    context)),
                (activationResponse, pollingContext) ->
                    Mono.error(new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeFormResultWithResponseAsync(modelUuid, resultId,
                    context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded, modelId))
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string -->
     * <pre>
     * String formUrl = &quot;&#123;formUrl&#125;&quot;;
     * formRecognizerAsyncClient.beginRecognizeContentFromUrl&#40;formUrl&#41;
     *     &#47;&#47; if training polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;formPage -&gt; &#123;
     *         System.out.printf&#40;&quot;Page Angle: %s%n&quot;, formPage.getTextAngle&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Page Dimension unit: %s%n&quot;, formPage.getUnit&#40;&#41;&#41;;
     *         &#47;&#47; Table information
     *         System.out.println&#40;&quot;Recognized Tables: &quot;&#41;;
     *         formPage.getTables&#40;&#41;.forEach&#40;formTable -&gt;
     *             formTable.getCells&#40;&#41;.forEach&#40;recognizedTableCell -&gt;
     *                 System.out.printf&#40;&quot;%s &quot;, recognizedTableCell.getText&#40;&#41;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string -->
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-Options -->
     * <pre>
     * String formUrl = &quot;&#123;formUrl&#125;&quot;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeContentFromUrl&#40;formUrl,
     *     new RecognizeContentOptions&#40;&#41;.setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;formPage -&gt; &#123;
     *         System.out.printf&#40;&quot;Page Angle: %s%n&quot;, formPage.getTextAngle&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Page Dimension unit: %s%n&quot;, formPage.getUnit&#40;&#41;&#41;;
     *         &#47;&#47; Table information
     *         System.out.println&#40;&quot;Recognized Tables: &quot;&#41;;
     *         formPage.getTables&#40;&#41;.forEach&#40;formTable -&gt;
     *             formTable.getCells&#40;&#41;.forEach&#40;recognizedTableCell -&gt;
     *                 System.out.printf&#40;&quot;%s &quot;, recognizedTableCell.getText&#40;&#41;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-Options -->
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
    public PollerFlux<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContentFromUrl(String formUrl,
        RecognizeContentOptions recognizeContentOptions) {
        return beginRecognizeContentFromUrl(formUrl, recognizeContentOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContentFromUrl(String formUrl,
        RecognizeContentOptions recognizeContentOptions, Context context) {
        try {
            if (formUrl == null) {
                return PollerFlux.error(new NullPointerException("'formUrl' is required and cannot be null."));
            }

            RecognizeContentOptions finalRecognizeContentOptions = getRecognizeContentOptions(recognizeContentOptions);
            return new PollerFlux<>(finalRecognizeContentOptions.getPollInterval(), urlActivationOperation(
                () -> analyzersImpl.analyzeLayoutWithResponseAsync(finalRecognizeContentOptions.getPages(),
                    Language.fromString(Objects.toString(finalRecognizeContentOptions.getLanguage(), null)),
                    com.azure.ai.formrecognizer.implementation.models.ReadingOrder.fromString(
                        Objects.toString(finalRecognizeContentOptions.getReadingOrder(), null)),
                        new SourcePath().setSource(formUrl), context)
                    .map(response -> new FormRecognizerOperationResult(
                        parseModelId(response.getDeserializedHeaders().getOperationLocation()))), logger),
                pollingOperation(resultId -> analyzersImpl.getAnalyzeLayoutResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) ->
                    monoError(logger, new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeLayoutResultWithResponseAsync(resultId, context))
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long -->
     * <pre>
     * File form = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * Flux&lt;ByteBuffer&gt; buffer = toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;form.toPath&#40;&#41;&#41;&#41;&#41;;
     *
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeContent&#40;buffer, form.length&#40;&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;formPage -&gt; &#123;
     *         System.out.printf&#40;&quot;Page Angle: %s%n&quot;, formPage.getTextAngle&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Page Dimension unit: %s%n&quot;, formPage.getUnit&#40;&#41;&#41;;
     *         &#47;&#47; Table information
     *         System.out.println&#40;&quot;Recognized Tables: &quot;&#41;;
     *         formPage.getTables&#40;&#41;.forEach&#40;formTable -&gt;
     *             formTable.getCells&#40;&#41;.forEach&#40;recognizedTableCell -&gt;
     *                 System.out.printf&#40;&quot;%s &quot;, recognizedTableCell.getText&#40;&#41;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long -->
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
    public PollerFlux<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContent(Flux<ByteBuffer> form,
        long length) {
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-Options -->
     * <pre>
     * File form = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * Flux&lt;ByteBuffer&gt; buffer = toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;form.toPath&#40;&#41;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeContent&#40;buffer, form.length&#40;&#41;,
     *     new RecognizeContentOptions&#40;&#41;
     *         .setContentType&#40;FormContentType.IMAGE_JPEG&#41;
     *         .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;formPage -&gt; &#123;
     *         System.out.printf&#40;&quot;Page Angle: %s%n&quot;, formPage.getTextAngle&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Page Dimension unit: %s%n&quot;, formPage.getUnit&#40;&#41;&#41;;
     *         &#47;&#47; Table information
     *         System.out.println&#40;&quot;Recognized Tables: &quot;&#41;;
     *         formPage.getTables&#40;&#41;.forEach&#40;formTable -&gt; formTable.getCells&#40;&#41;.forEach&#40;recognizedTableCell -&gt;
     *             System.out.printf&#40;&quot;%s &quot;, recognizedTableCell.getText&#40;&#41;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-Options -->
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
            if (form == null) {
                return PollerFlux.error(new NullPointerException("'form' is required and cannot be null."));
            }
            RecognizeContentOptions finalRecognizeContentOptions = getRecognizeContentOptions(recognizeContentOptions);
            return new PollerFlux<>(finalRecognizeContentOptions.getPollInterval(), streamActivationOperation(
                contentType -> analyzersImpl.analyzeLayoutWithResponseAsync(contentType,
                        finalRecognizeContentOptions.getPages(),
                        Language.fromString(Objects.toString(finalRecognizeContentOptions.getLanguage(), null)),
                        com.azure.ai.formrecognizer.implementation.models.ReadingOrder.fromString(
                            Objects.toString(finalRecognizeContentOptions.getReadingOrder(), null)), form, length,
                        context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))),
                form, finalRecognizeContentOptions.getContentType()),
                pollingOperation(resultId -> analyzersImpl.getAnalyzeLayoutResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) ->
                    monoError(logger, new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeLayoutResultWithResponseAsync(resultId, context))
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
     * <pre>
     * String formUrl = &quot;&#123;form_url&#125;&quot;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl&#40;modelId, formUrl&#41;
     *     &#47;&#47; if training polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;recognizedForm -&gt; recognizedForm.getFields&#40;&#41;
     *         .forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
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
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceiptsFromUrl(
        String receiptUrl) {
        return beginRecognizeReceiptsFromUrl(receiptUrl, null);
    }

    /**
     * Recognizes receipt data using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
     * <pre>
     * String formUrl = &quot;&#123;form_url&#125;&quot;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl&#40;modelId, formUrl&#41;
     *     &#47;&#47; if training polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;recognizedForm -&gt; recognizedForm.getFields&#40;&#41;
     *         .forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
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
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceiptsFromUrl(
        String receiptUrl, RecognizeReceiptsOptions recognizeReceiptsOptions) {
        return beginRecognizeReceiptsFromUrl(receiptUrl, recognizeReceiptsOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceiptsFromUrl(String receiptUrl,
        RecognizeReceiptsOptions recognizeReceiptsOptions, Context context) {
        try {
            if (receiptUrl == null) {
                return PollerFlux.error(new NullPointerException("'receiptUrl' is required and cannot be null."));
            }

            final RecognizeReceiptsOptions finalRecognizeReceiptsOptions
                = getRecognizeReceiptOptions(recognizeReceiptsOptions);
            final boolean isFieldElementsIncluded = finalRecognizeReceiptsOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo  = finalRecognizeReceiptsOptions.getLocale();
            return new PollerFlux<>(finalRecognizeReceiptsOptions.getPollInterval(), urlActivationOperation(() ->
                    analyzersImpl.analyzeReceiptWithResponseAsync(isFieldElementsIncluded,
                            Locale.fromString(Objects.toString(localeInfo, null)),
                            finalRecognizeReceiptsOptions.getPages(), new SourcePath().setSource(receiptUrl), context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))), logger),
                pollingOperation(resultId -> analyzersImpl.getAnalyzeReceiptResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeReceiptResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded, null))
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
     * <p>
     * Note that the {@code receipt} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
     * <pre>
     * String formUrl = &quot;&#123;form_url&#125;&quot;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl&#40;modelId, formUrl&#41;
     *     &#47;&#47; if training polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;recognizedForm -&gt; recognizedForm.getFields&#40;&#41;
     *         .forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
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
     * <p>
     * Note that the {@code receipt} passed must be replayable if retries are enabled (the default). In other words, the
     * {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
     * <pre>
     * String formUrl = &quot;&#123;form_url&#125;&quot;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl&#40;modelId, formUrl&#41;
     *     &#47;&#47; if training polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;recognizedForm -&gt; recognizedForm.getFields&#40;&#41;
     *         .forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
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
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceipts(
        Flux<ByteBuffer> receipt, long length, RecognizeReceiptsOptions recognizeReceiptsOptions) {
        return beginRecognizeReceipts(receipt, length, recognizeReceiptsOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceipts(Flux<ByteBuffer> receipt,
        long length, RecognizeReceiptsOptions recognizeReceiptsOptions, Context context) {
        try {
            if (receipt == null) {
                return PollerFlux.error(new NullPointerException("'receipt' is required and cannot be null."));
            }
            final RecognizeReceiptsOptions finalRecognizeReceiptsOptions
                = getRecognizeReceiptOptions(recognizeReceiptsOptions);
            final boolean isFieldElementsIncluded = finalRecognizeReceiptsOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo  = finalRecognizeReceiptsOptions.getLocale();
            return new PollerFlux<>(finalRecognizeReceiptsOptions.getPollInterval(), streamActivationOperation(
                (contentType -> analyzersImpl.analyzeReceiptWithResponseAsync(contentType, isFieldElementsIncluded,
                        Locale.fromString(Objects.toString(localeInfo, null)), finalRecognizeReceiptsOptions.getPages(),
                        receipt, length, context)
                    .map(response -> new FormRecognizerOperationResult(
                        parseModelId(response.getDeserializedHeaders().getOperationLocation())))), receipt,
                finalRecognizeReceiptsOptions.getContentType()),
                pollingOperation(resultId -> analyzersImpl.getAnalyzeReceiptResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeReceiptResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded, null))
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
     * <pre>
     * String formUrl = &quot;&#123;form_url&#125;&quot;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl&#40;modelId, formUrl&#41;
     *     &#47;&#47; if training polling operation completed, retrieve the final result.
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;Flux::fromIterable&#41;
     *     .subscribe&#40;recognizedForm -&gt; recognizedForm.getFields&#40;&#41;
     *         .forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string -->
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl#string-Options -->
     * <pre>
     * String businessCardUrl = &quot;&#123;business_card_url&#125;&quot;;
     * boolean includeFieldElements = true;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl&#40;businessCardUrl,
     *     new RecognizeBusinessCardsOptions&#40;&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedBusinessCards -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedBusinessCards.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedBusinessCard = recognizedBusinessCards.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedBusinessCard.getFields&#40;&#41;;
     *             System.out.printf&#40;&quot;----------- Recognized Business Card page %d -----------%n&quot;, i&#41;;
     *             FormField contactNamesFormField = recognizedFields.get&#40;&quot;ContactNames&quot;&#41;;
     *             if &#40;contactNamesFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.LIST == contactNamesFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     List&lt;FormField&gt; contactNamesList = contactNamesFormField.getValue&#40;&#41;.asList&#40;&#41;;
     *                     contactNamesList.stream&#40;&#41;
     *                         .filter&#40;contactName -&gt; FieldValueType.MAP == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
     *                         .map&#40;contactName -&gt; &#123;
     *                             System.out.printf&#40;&quot;Contact name: %s%n&quot;, contactName.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *                             return contactName.getValue&#40;&#41;.asMap&#40;&#41;;
     *                         &#125;&#41;
     *                         .forEach&#40;contactNamesMap -&gt; contactNamesMap.forEach&#40;&#40;key, contactName&#41; -&gt; &#123;
     *                             if &#40;&quot;FirstName&quot;.equals&#40;key&#41;&#41; &#123;
     *                                 if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                     String firstName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                     System.out.printf&#40;&quot;&#92;tFirst Name: %s, confidence: %.2f%n&quot;,
     *                                         firstName, contactName.getConfidence&#40;&#41;&#41;;
     *                                 &#125;
     *                             &#125;
     *                             if &#40;&quot;LastName&quot;.equals&#40;key&#41;&#41; &#123;
     *                                 if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                     String lastName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                     System.out.printf&#40;&quot;&#92;tLast Name: %s, confidence: %.2f%n&quot;,
     *                                         lastName, contactName.getConfidence&#40;&#41;&#41;;
     *                                 &#125;
     *                             &#125;
     *                         &#125;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *             FormField jobTitles = recognizedFields.get&#40;&quot;JobTitles&quot;&#41;;
     *             if &#40;jobTitles != null&#41; &#123;
     *                 if &#40;FieldValueType.LIST == jobTitles.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     List&lt;FormField&gt; jobTitlesItems = jobTitles.getValue&#40;&#41;.asList&#40;&#41;;
     *                     jobTitlesItems.forEach&#40;jobTitlesItem -&gt; &#123;
     *                         if &#40;FieldValueType.STRING == jobTitlesItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                             String jobTitle = jobTitlesItem.getValue&#40;&#41;.asString&#40;&#41;;
     *                             System.out.printf&#40;&quot;Job Title: %s, confidence: %.2f%n&quot;,
     *                                 jobTitle, jobTitlesItem.getConfidence&#40;&#41;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl#string-Options -->
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
            if (businessCardUrl == null) {
                return PollerFlux.error(new NullPointerException("'businessCardUrl' is required and cannot be null."));
            }

            final RecognizeBusinessCardsOptions finalRecognizeBusinessCardsOptions
                = getRecognizeBusinessCardsOptions(recognizeBusinessCardsOptions);
            final boolean isFieldElementsIncluded = finalRecognizeBusinessCardsOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo = finalRecognizeBusinessCardsOptions.getLocale();
            return new PollerFlux<>(DEFAULT_POLL_INTERVAL, urlActivationOperation(() ->
                    analyzersImpl.analyzeBusinessCardWithResponseAsync(isFieldElementsIncluded,
                            Locale.fromString(Objects.toString(localeInfo, null)),
                            finalRecognizeBusinessCardsOptions.getPages(), new SourcePath().setSource(businessCardUrl),
                            context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))), logger),
                pollingOperation(resultId -> analyzersImpl.getAnalyzeBusinessCardResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeBusinessCardResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded, null))
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
     * <p>
     * Note that the {@code businessCard} passed must be replayable if retries are enabled (the default).
     * In other words, the {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeBusinessCards#Flux-long -->
     * <pre>
     * File businessCard = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * Flux&lt;ByteBuffer&gt; buffer = toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;businessCard.toPath&#40;&#41;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeBusinessCards&#40;buffer, businessCard.length&#40;&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedBusinessCards -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedBusinessCards.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedForm = recognizedBusinessCards.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *             System.out.printf&#40;&quot;----------- Recognized Business Card page %d -----------%n&quot;, i&#41;;
     *             FormField contactNamesFormField = recognizedFields.get&#40;&quot;ContactNames&quot;&#41;;
     *             if &#40;contactNamesFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.LIST == contactNamesFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     List&lt;FormField&gt; contactNamesList = contactNamesFormField.getValue&#40;&#41;.asList&#40;&#41;;
     *                     contactNamesList.stream&#40;&#41;
     *                         .filter&#40;contactName -&gt; FieldValueType.MAP == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
     *                         .map&#40;contactName -&gt; &#123;
     *                             System.out.printf&#40;&quot;Contact name: %s%n&quot;, contactName.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *                             return contactName.getValue&#40;&#41;.asMap&#40;&#41;;
     *                         &#125;&#41;
     *                         .forEach&#40;contactNamesMap -&gt; contactNamesMap.forEach&#40;&#40;key, contactName&#41; -&gt; &#123;
     *                             if &#40;&quot;FirstName&quot;.equals&#40;key&#41;&#41; &#123;
     *                                 if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                     String firstName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                     System.out.printf&#40;&quot;&#92;tFirst Name: %s, confidence: %.2f%n&quot;,
     *                                         firstName, contactName.getConfidence&#40;&#41;&#41;;
     *                                 &#125;
     *                             &#125;
     *                             if &#40;&quot;LastName&quot;.equals&#40;key&#41;&#41; &#123;
     *                                 if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                     String lastName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                     System.out.printf&#40;&quot;&#92;tLast Name: %s, confidence: %.2f%n&quot;,
     *                                         lastName, contactName.getConfidence&#40;&#41;&#41;;
     *                                 &#125;
     *                             &#125;
     *                         &#125;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *             FormField jobTitles = recognizedFields.get&#40;&quot;JobTitles&quot;&#41;;
     *             if &#40;jobTitles != null&#41; &#123;
     *                 if &#40;FieldValueType.LIST == jobTitles.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     List&lt;FormField&gt; jobTitlesItems = jobTitles.getValue&#40;&#41;.asList&#40;&#41;;
     *                     jobTitlesItems.forEach&#40;jobTitlesItem -&gt; &#123;
     *                         if &#40;FieldValueType.STRING == jobTitlesItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                             String jobTitle = jobTitlesItem.getValue&#40;&#41;.asString&#40;&#41;;
     *                             System.out.printf&#40;&quot;Job Title: %s, confidence: %.2f%n&quot;,
     *                                 jobTitle, jobTitlesItem.getConfidence&#40;&#41;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeBusinessCards#Flux-long -->
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
     * <p>
     * Note that the {@code businessCard} passed must be replayable if retries are enabled (the default).
     * In other words, the {@code Flux} must produce the same data each time it is subscribed to.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeBusinessCards#Flux-long-Options -->
     * <pre>
     * File businessCard = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * boolean includeFieldElements = true;
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * Flux&lt;ByteBuffer&gt; buffer = toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;businessCard.toPath&#40;&#41;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeBusinessCards&#40;buffer, businessCard.length&#40;&#41;,
     *     new RecognizeBusinessCardsOptions&#40;&#41;
     *         .setContentType&#40;FormContentType.IMAGE_JPEG&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedBusinessCards -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedBusinessCards.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedForm = recognizedBusinessCards.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *             System.out.printf&#40;&quot;----------- Recognized Business Card page %d -----------%n&quot;, i&#41;;
     *             FormField contactNamesFormField = recognizedFields.get&#40;&quot;ContactNames&quot;&#41;;
     *             if &#40;contactNamesFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.LIST == contactNamesFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     List&lt;FormField&gt; contactNamesList = contactNamesFormField.getValue&#40;&#41;.asList&#40;&#41;;
     *                     contactNamesList.stream&#40;&#41;
     *                         .filter&#40;contactName -&gt; FieldValueType.MAP == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
     *                         .map&#40;contactName -&gt; &#123;
     *                             System.out.printf&#40;&quot;Contact name: %s%n&quot;, contactName.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *                             return contactName.getValue&#40;&#41;.asMap&#40;&#41;;
     *                         &#125;&#41;
     *                         .forEach&#40;contactNamesMap -&gt; contactNamesMap.forEach&#40;&#40;key, contactName&#41; -&gt; &#123;
     *                             if &#40;&quot;FirstName&quot;.equals&#40;key&#41;&#41; &#123;
     *                                 if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                     String firstName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                     System.out.printf&#40;&quot;&#92;tFirst Name: %s, confidence: %.2f%n&quot;,
     *                                         firstName, contactName.getConfidence&#40;&#41;&#41;;
     *                                 &#125;
     *                             &#125;
     *                             if &#40;&quot;LastName&quot;.equals&#40;key&#41;&#41; &#123;
     *                                 if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                     String lastName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                     System.out.printf&#40;&quot;&#92;tLast Name: %s, confidence: %.2f%n&quot;,
     *                                         lastName, contactName.getConfidence&#40;&#41;&#41;;
     *                                 &#125;
     *                             &#125;
     *                         &#125;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *             FormField jobTitles = recognizedFields.get&#40;&quot;JobTitles&quot;&#41;;
     *             if &#40;jobTitles != null&#41; &#123;
     *                 if &#40;FieldValueType.LIST == jobTitles.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     List&lt;FormField&gt; jobTitlesItems = jobTitles.getValue&#40;&#41;.asList&#40;&#41;;
     *                     jobTitlesItems.forEach&#40;jobTitlesItem -&gt; &#123;
     *                         if &#40;FieldValueType.STRING == jobTitlesItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                             String jobTitle = jobTitlesItem.getValue&#40;&#41;.asString&#40;&#41;;
     *                             System.out.printf&#40;&quot;Job Title: %s, confidence: %.2f%n&quot;,
     *                                 jobTitle, jobTitlesItem.getConfidence&#40;&#41;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeBusinessCards#Flux-long-Options -->
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
            if (businessCard == null) {
                return PollerFlux.error(new NullPointerException("'businessCard' is required and cannot be null."));
            }
            final RecognizeBusinessCardsOptions finalRecognizeBusinessCardsOptions
                = getRecognizeBusinessCardsOptions(recognizeBusinessCardsOptions);
            final boolean isFieldElementsIncluded = finalRecognizeBusinessCardsOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo = finalRecognizeBusinessCardsOptions.getLocale();
            return new PollerFlux<>(DEFAULT_POLL_INTERVAL, streamActivationOperation((contentType ->
                    analyzersImpl.analyzeBusinessCardWithResponseAsync(contentType, isFieldElementsIncluded,
                            Locale.fromString(Objects.toString(localeInfo, null)),
                            finalRecognizeBusinessCardsOptions.getPages(), businessCard, length, context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                businessCard, finalRecognizeBusinessCardsOptions.getContentType()),
                pollingOperation(resultId -> analyzersImpl.getAnalyzeBusinessCardResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeBusinessCardResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded, null))
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl#string -->
     * <pre>
     * String idDocumentUrl = &quot;idDocumentUrl&quot;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl&#40;idDocumentUrl&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedIDDocumentResult -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedIDDocumentResult.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedForm = recognizedIDDocumentResult.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *             System.out.printf&#40;&quot;----------- Recognized license info for page %d -----------%n&quot;, i&#41;;
     *
     *             FormField firstNameField = recognizedFields.get&#40;&quot;FirstName&quot;&#41;;
     *             if &#40;firstNameField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == firstNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String firstName = firstNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;First Name: %s, confidence: %.2f%n&quot;,
     *                         firstName, firstNameField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField lastNameField = recognizedFields.get&#40;&quot;LastName&quot;&#41;;
     *             if &#40;lastNameField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == lastNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String lastName = lastNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;Last name: %s, confidence: %.2f%n&quot;,
     *                         lastName, lastNameField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField countryRegionFormField = recognizedFields.get&#40;&quot;CountryRegion&quot;&#41;;
     *             if &#40;countryRegionFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == countryRegionFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String countryRegion = countryRegionFormField.getValue&#40;&#41;.asCountryRegion&#40;&#41;;
     *                     System.out.printf&#40;&quot;Country or region: %s, confidence: %.2f%n&quot;,
     *                         countryRegion, countryRegionFormField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField dateOfExpirationField = recognizedFields.get&#40;&quot;DateOfExpiration&quot;&#41;;
     *             if &#40;dateOfExpirationField != null&#41; &#123;
     *                 if &#40;FieldValueType.DATE == dateOfExpirationField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     LocalDate expirationDate = dateOfExpirationField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                     System.out.printf&#40;&quot;Document date of expiration: %s, confidence: %.2f%n&quot;,
     *                         expirationDate, dateOfExpirationField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField documentNumberField = recognizedFields.get&#40;&quot;DocumentNumber&quot;&#41;;
     *             if &#40;documentNumberField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == documentNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String documentNumber = documentNumberField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;Document number: %s, confidence: %.2f%n&quot;,
     *                         documentNumber, documentNumberField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl#string -->
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl#string-Options -->
     * <pre>
     * String licenseDocumentUrl = &quot;licenseDocumentUrl&quot;;
     * boolean includeFieldElements = true;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl&#40;licenseDocumentUrl,
     *     new RecognizeIdentityDocumentOptions&#40;&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedIDDocumentResult -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedIDDocumentResult.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedForm = recognizedIDDocumentResult.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *             System.out.printf&#40;&quot;----------- Recognized license info for page %d -----------%n&quot;, i&#41;;
     *
     *             FormField firstNameField = recognizedFields.get&#40;&quot;FirstName&quot;&#41;;
     *             if &#40;firstNameField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == firstNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String firstName = firstNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;First Name: %s, confidence: %.2f%n&quot;,
     *                         firstName, firstNameField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField lastNameField = recognizedFields.get&#40;&quot;LastName&quot;&#41;;
     *             if &#40;lastNameField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == lastNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String lastName = lastNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;Last name: %s, confidence: %.2f%n&quot;,
     *                         lastName, lastNameField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField countryRegionFormField = recognizedFields.get&#40;&quot;CountryRegion&quot;&#41;;
     *             if &#40;countryRegionFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == countryRegionFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String countryRegion = countryRegionFormField.getValue&#40;&#41;.asCountryRegion&#40;&#41;;
     *                     System.out.printf&#40;&quot;Country or region: %s, confidence: %.2f%n&quot;,
     *                         countryRegion, countryRegionFormField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField dateOfExpirationField = recognizedFields.get&#40;&quot;DateOfExpiration&quot;&#41;;
     *             if &#40;dateOfExpirationField != null&#41; &#123;
     *                 if &#40;FieldValueType.DATE == dateOfExpirationField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     LocalDate expirationDate = dateOfExpirationField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                     System.out.printf&#40;&quot;Document date of expiration: %s, confidence: %.2f%n&quot;,
     *                         expirationDate, dateOfExpirationField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField documentNumberField = recognizedFields.get&#40;&quot;DocumentNumber&quot;&#41;;
     *             if &#40;documentNumberField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == documentNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String documentNumber = documentNumberField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;Document number: %s, confidence: %.2f%n&quot;,
     *                         documentNumber, documentNumberField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl#string-Options -->
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
            if (identityDocumentUrl == null) {
                return PollerFlux.error(new NullPointerException(
                    "'identityDocumentUrl' is required and cannot be null."));
            }

            final RecognizeIdentityDocumentOptions finalRecognizeIdentityDocumentOptions
                = getRecognizeIdentityDocumentOptions(recognizeIdentityDocumentOptions);
            final boolean isFieldElementsIncluded = finalRecognizeIdentityDocumentOptions.isFieldElementsIncluded();
            return new PollerFlux<>(DEFAULT_POLL_INTERVAL, urlActivationOperation(() ->
                    analyzersImpl.analyzeIdDocumentWithResponseAsync(isFieldElementsIncluded,
                            finalRecognizeIdentityDocumentOptions.getPages(),
                            new SourcePath().setSource(identityDocumentUrl), context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))), logger),
                pollingOperation(resultId -> analyzersImpl.getAnalyzeIdDocumentResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeIdDocumentResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded, null))
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeIdentityDocuments#Flux-long -->
     * <pre>
     * File license = new File&#40;&quot;local&#47;file_path&#47;license.jpg&quot;&#41;;
     * Flux&lt;ByteBuffer&gt; buffer =
     *     toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;license.toPath&#40;&#41;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeIdentityDocuments&#40;buffer, license.length&#40;&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedIDDocumentResult -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedIDDocumentResult.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedForm = recognizedIDDocumentResult.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *             System.out.printf&#40;&quot;----------- Recognized license info for page %d -----------%n&quot;, i&#41;;
     *
     *             FormField firstNameField = recognizedFields.get&#40;&quot;FirstName&quot;&#41;;
     *             if &#40;firstNameField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == firstNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String firstName = firstNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;First Name: %s, confidence: %.2f%n&quot;,
     *                         firstName, firstNameField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField lastNameField = recognizedFields.get&#40;&quot;LastName&quot;&#41;;
     *             if &#40;lastNameField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == lastNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String lastName = lastNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;Last name: %s, confidence: %.2f%n&quot;,
     *                         lastName, lastNameField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField countryRegionFormField = recognizedFields.get&#40;&quot;CountryRegion&quot;&#41;;
     *             if &#40;countryRegionFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == countryRegionFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String countryRegion = countryRegionFormField.getValue&#40;&#41;.asCountryRegion&#40;&#41;;
     *                     System.out.printf&#40;&quot;Country or region: %s, confidence: %.2f%n&quot;,
     *                         countryRegion, countryRegionFormField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField dateOfExpirationField = recognizedFields.get&#40;&quot;DateOfExpiration&quot;&#41;;
     *             if &#40;dateOfExpirationField != null&#41; &#123;
     *                 if &#40;FieldValueType.DATE == dateOfExpirationField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     LocalDate expirationDate = dateOfExpirationField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                     System.out.printf&#40;&quot;Document date of expiration: %s, confidence: %.2f%n&quot;,
     *                         expirationDate, dateOfExpirationField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField documentNumberField = recognizedFields.get&#40;&quot;DocumentNumber&quot;&#41;;
     *             if &#40;documentNumberField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == documentNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String documentNumber = documentNumberField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;Document number: %s, confidence: %.2f%n&quot;,
     *                         documentNumber, documentNumberField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeIdentityDocuments#Flux-long -->
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeIdentityDocuments#Flux-long-Options -->
     * <pre>
     * File licenseDocument = new File&#40;&quot;local&#47;file_path&#47;license.jpg&quot;&#41;;
     * boolean includeFieldElements = true;
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * Flux&lt;ByteBuffer&gt; buffer =
     *     toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;licenseDocument.toPath&#40;&#41;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeIdentityDocuments&#40;buffer,
     *     licenseDocument.length&#40;&#41;,
     *     new RecognizeIdentityDocumentOptions&#40;&#41;
     *         .setContentType&#40;FormContentType.IMAGE_JPEG&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedIDDocumentResult -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedIDDocumentResult.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedForm = recognizedIDDocumentResult.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *             System.out.printf&#40;&quot;----------- Recognized license info for page %d -----------%n&quot;, i&#41;;
     *
     *             FormField firstNameField = recognizedFields.get&#40;&quot;FirstName&quot;&#41;;
     *             if &#40;firstNameField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == firstNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String firstName = firstNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;First Name: %s, confidence: %.2f%n&quot;,
     *                         firstName, firstNameField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField lastNameField = recognizedFields.get&#40;&quot;LastName&quot;&#41;;
     *             if &#40;lastNameField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == lastNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String lastName = lastNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;Last name: %s, confidence: %.2f%n&quot;,
     *                         lastName, lastNameField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField countryRegionFormField = recognizedFields.get&#40;&quot;CountryRegion&quot;&#41;;
     *             if &#40;countryRegionFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == countryRegionFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String countryRegion = countryRegionFormField.getValue&#40;&#41;.asCountryRegion&#40;&#41;;
     *                     System.out.printf&#40;&quot;Country or region: %s, confidence: %.2f%n&quot;,
     *                         countryRegion, countryRegionFormField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField dateOfExpirationField = recognizedFields.get&#40;&quot;DateOfExpiration&quot;&#41;;
     *             if &#40;dateOfExpirationField != null&#41; &#123;
     *                 if &#40;FieldValueType.DATE == dateOfExpirationField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     LocalDate expirationDate = dateOfExpirationField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                     System.out.printf&#40;&quot;Document date of expiration: %s, confidence: %.2f%n&quot;,
     *                         expirationDate, dateOfExpirationField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField documentNumberField = recognizedFields.get&#40;&quot;DocumentNumber&quot;&#41;;
     *             if &#40;documentNumberField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == documentNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String documentNumber = documentNumberField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;Document number: %s, confidence: %.2f%n&quot;,
     *                         documentNumber, documentNumberField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeIdentityDocuments#Flux-long-Options -->
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
            if (identityDocument == null) {
                return PollerFlux.error(new NullPointerException("'identityDocument' is required and cannot be null."));
            }
            final RecognizeIdentityDocumentOptions finalRecognizeIdentityDocumentOptions
                = getRecognizeIdentityDocumentOptions(recognizeIdentityDocumentOptions);
            final boolean isFieldElementsIncluded = finalRecognizeIdentityDocumentOptions.isFieldElementsIncluded();
            return new PollerFlux<>(DEFAULT_POLL_INTERVAL, streamActivationOperation((contentType ->
                    analyzersImpl.analyzeIdDocumentWithResponseAsync(contentType, isFieldElementsIncluded,
                            finalRecognizeIdentityDocumentOptions.getPages(), identityDocument, length, context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                    identityDocument, finalRecognizeIdentityDocumentOptions.getContentType()),
                pollingOperation(resultId -> analyzersImpl.getAnalyzeIdDocumentResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeIdDocumentResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse -> toRecognizedForm(
                        modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded, null))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    /*
     * Poller's ACTIVATION operation that takes stream as input.
     */
    private Function<PollingContext<FormRecognizerOperationResult>, Mono<FormRecognizerOperationResult>>
        streamActivationOperation(Function<ContentType, Mono<FormRecognizerOperationResult>> activationOperation,
        Flux<ByteBuffer> form, FormContentType contentType) {
        return pollingContext -> {
            try {
                if (form == null) {
                    return Mono.error(new NullPointerException("'form' is required and cannot be null."));
                }
                if (contentType != null) {
                    return activationOperation.apply(ContentType.fromString(contentType.toString()))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
                } else {
                    return detectContentType(form)
                        .flatMap(activationOperation)
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
        pollingOperation(Function<UUID, Mono<Response<AnalyzeOperationResult>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<FormRecognizerOperationResult> operationResultPollResponse =
                    pollingContext.getLatestResponse();
                final UUID resultUuid = UUID.fromString(operationResultPollResponse.getValue().getResultId());
                return pollingFunction.apply(resultUuid)
                    .flatMap(modelResponse -> processAnalyzeModelResponse(modelResponse, operationResultPollResponse))
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
        fetchingOperation(Function<UUID, Mono<Response<AnalyzeOperationResult>>> fetchingFunction) {
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
                return monoError(logger, new FormRecognizerException("Analyze operation failed",
                    analyzeOperationResultResponse.getValue().getAnalyzeResult().getErrors().stream()
                        .map(errorInformation -> new FormRecognizerErrorInformation(errorInformation.getCode(),
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeInvoicesFromUrl#string -->
     * <pre>
     * String invoiceUrl = &quot;invoice_url&quot;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeInvoicesFromUrl&#40;invoiceUrl&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedInvoices -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedInvoices.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedForm = recognizedInvoices.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *             FormField customAddrFormField = recognizedFields.get&#40;&quot;CustomerAddress&quot;&#41;;
     *             if &#40;customAddrFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == customAddrFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     System.out.printf&#40;&quot;Customer Address: %s%n&quot;, customAddrFormField.getValue&#40;&#41;.asString&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *             FormField invoiceDateFormField = recognizedFields.get&#40;&quot;InvoiceDate&quot;&#41;;
     *             if &#40;invoiceDateFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.DATE == invoiceDateFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     LocalDate invoiceDate = invoiceDateFormField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                     System.out.printf&#40;&quot;Invoice Date: %s, confidence: %.2f%n&quot;,
     *                         invoiceDate, invoiceDateFormField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeInvoicesFromUrl#string -->
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
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoicesFromUrl(
        String invoiceUrl) {
        return beginRecognizeInvoicesFromUrl(invoiceUrl, null);
    }

    /**
     * Recognizes invoice data using optical character recognition (OCR) and a prebuilt invoice trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeInvoicesFromUrl#string-Options -->
     * <pre>
     * String invoiceUrl = &quot;invoice_url&quot;;
     * boolean includeFieldElements = true;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeInvoicesFromUrl&#40;invoiceUrl,
     *     new RecognizeInvoicesOptions&#40;&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedInvoices -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedInvoices.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedForm = recognizedInvoices.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *             FormField customAddrFormField = recognizedFields.get&#40;&quot;CustomerAddress&quot;&#41;;
     *             if &#40;customAddrFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == customAddrFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     System.out.printf&#40;&quot;Customer Address: %s%n&quot;, customAddrFormField.getValue&#40;&#41;.asString&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *             FormField invoiceDateFormField = recognizedFields.get&#40;&quot;InvoiceDate&quot;&#41;;
     *             if &#40;invoiceDateFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.DATE == invoiceDateFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     LocalDate invoiceDate = invoiceDateFormField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                     System.out.printf&#40;&quot;Invoice Date: %s, confidence: %.2f%n&quot;,
     *                         invoiceDate, invoiceDateFormField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeInvoicesFromUrl#string-Options -->
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
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoicesFromUrl(
        String invoiceUrl, RecognizeInvoicesOptions recognizeInvoicesOptions) {
        return beginRecognizeInvoicesFromUrl(invoiceUrl, recognizeInvoicesOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoicesFromUrl(String invoiceUrl,
        RecognizeInvoicesOptions recognizeInvoicesOptions, Context context) {
        try {
            if (invoiceUrl == null) {
                return PollerFlux.error(new NullPointerException("'invoiceUrl' is required and cannot be null."));
            }

            final RecognizeInvoicesOptions finalRecognizeInvoicesOptions
                = getRecognizeInvoicesOptions(recognizeInvoicesOptions);
            final boolean isFieldElementsIncluded = finalRecognizeInvoicesOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo  = finalRecognizeInvoicesOptions.getLocale();
            return new PollerFlux<>(DEFAULT_POLL_INTERVAL, urlActivationOperation(() ->
                    analyzersImpl.analyzeInvoiceWithResponseAsync(isFieldElementsIncluded,
                            Locale.fromString(Objects.toString(localeInfo, null)),
                            finalRecognizeInvoicesOptions.getPages(), new SourcePath().setSource(invoiceUrl), context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation()))), logger),
                pollingOperation(resultId -> analyzersImpl.getAnalyzeInvoiceResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeInvoiceResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded,
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeInvoices#Flux-long -->
     * <pre>
     * File invoice = new File&#40;&quot;local&#47;file_path&#47;invoice.jpg&quot;&#41;;
     * Flux&lt;ByteBuffer&gt; buffer =
     *     toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;invoice.toPath&#40;&#41;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeInvoices&#40;buffer, invoice.length&#40;&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedInvoices -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedInvoices.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedForm = recognizedInvoices.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *             FormField customAddrFormField = recognizedFields.get&#40;&quot;CustomerAddress&quot;&#41;;
     *             if &#40;customAddrFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == customAddrFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     System.out.printf&#40;&quot;Customer Address: %s%n&quot;, customAddrFormField.getValue&#40;&#41;.asString&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *             FormField invoiceDateFormField = recognizedFields.get&#40;&quot;InvoiceDate&quot;&#41;;
     *             if &#40;invoiceDateFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.DATE == invoiceDateFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     LocalDate invoiceDate = invoiceDateFormField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                     System.out.printf&#40;&quot;Invoice Date: %s, confidence: %.2f%n&quot;,
     *                         invoiceDate, invoiceDateFormField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeInvoices#Flux-long -->
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
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeInvoices#Flux-long-Options -->
     * <pre>
     * File invoice = new File&#40;&quot;local&#47;file_path&#47;invoice.jpg&quot;&#41;;
     * boolean includeFieldElements = true;
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * Flux&lt;ByteBuffer&gt; buffer =
     *     toFluxByteBuffer&#40;new ByteArrayInputStream&#40;Files.readAllBytes&#40;invoice.toPath&#40;&#41;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerAsyncClient.beginRecognizeInvoices&#40;buffer,
     *     invoice.length&#40;&#41;,
     *     new RecognizeInvoicesOptions&#40;&#41;
     *         .setContentType&#40;FormContentType.IMAGE_JPEG&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .subscribe&#40;recognizedInvoices -&gt; &#123;
     *         for &#40;int i = 0; i &lt; recognizedInvoices.size&#40;&#41;; i++&#41; &#123;
     *             RecognizedForm recognizedForm = recognizedInvoices.get&#40;i&#41;;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *             FormField customAddrFormField = recognizedFields.get&#40;&quot;CustomerAddress&quot;&#41;;
     *             if &#40;customAddrFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == customAddrFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     System.out.printf&#40;&quot;Customer Address: %s%n&quot;, customAddrFormField.getValue&#40;&#41;.asString&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *             FormField invoiceDateFormField = recognizedFields.get&#40;&quot;InvoiceDate&quot;&#41;;
     *             if &#40;invoiceDateFormField != null&#41; &#123;
     *                 if &#40;FieldValueType.DATE == invoiceDateFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     LocalDate invoiceDate = invoiceDateFormField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                     System.out.printf&#40;&quot;Invoice Date: %s, confidence: %.2f%n&quot;,
     *                         invoiceDate, invoiceDateFormField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerAsyncClient.beginRecognizeInvoices#Flux-long-Options -->
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
    public PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoices(
        Flux<ByteBuffer> invoice, long length, RecognizeInvoicesOptions recognizeInvoicesOptions) {
        return beginRecognizeInvoices(invoice, length, recognizeInvoicesOptions, Context.NONE);
    }

    PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoices(Flux<ByteBuffer> invoice,
        long length, RecognizeInvoicesOptions recognizeInvoicesOptions, Context context) {
        try {
            if (invoice == null) {
                return PollerFlux.error(new NullPointerException("'invoice' is required and cannot be null."));
            }
            final RecognizeInvoicesOptions finalRecognizeInvoicesOptions
                = getRecognizeInvoicesOptions(recognizeInvoicesOptions);
            final boolean isFieldElementsIncluded = finalRecognizeInvoicesOptions.isFieldElementsIncluded();
            final FormRecognizerLocale localeInfo  = finalRecognizeInvoicesOptions.getLocale();
            return new PollerFlux<>(DEFAULT_POLL_INTERVAL, streamActivationOperation((contentType ->
                    analyzersImpl.analyzeInvoiceWithResponseAsync(contentType, isFieldElementsIncluded,
                            Locale.fromString(Objects.toString(localeInfo, null)),
                            finalRecognizeInvoicesOptions.getPages(), invoice, length, context)
                        .map(response -> new FormRecognizerOperationResult(
                            parseModelId(response.getDeserializedHeaders().getOperationLocation())))),
                    invoice, finalRecognizeInvoicesOptions.getContentType()),
                pollingOperation(resultId -> analyzersImpl.getAnalyzeInvoiceResultWithResponseAsync(resultId, context)),
                (activationResponse, pollingContext) -> monoError(logger,
                    new RuntimeException("Cancellation is not supported")),
                fetchingOperation(resultId -> analyzersImpl.getAnalyzeInvoiceResultWithResponseAsync(resultId, context))
                    .andThen(after -> after.map(modelSimpleResponse ->
                        toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(),
                            isFieldElementsIncluded,
                            null))
                        .onErrorMap(Utility::mapToHttpResponseExceptionIfExists)));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private static RecognizeCustomFormsOptions getRecognizeCustomFormOptions(
        RecognizeCustomFormsOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeCustomFormsOptions() : userProvidedOptions;
    }

    static RecognizeContentOptions getRecognizeContentOptions(RecognizeContentOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeContentOptions() : userProvidedOptions;
    }

    static RecognizeReceiptsOptions getRecognizeReceiptOptions(RecognizeReceiptsOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeReceiptsOptions() : userProvidedOptions;
    }

    private static RecognizeBusinessCardsOptions getRecognizeBusinessCardsOptions(
        RecognizeBusinessCardsOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeBusinessCardsOptions() : userProvidedOptions;
    }

    private static RecognizeInvoicesOptions getRecognizeInvoicesOptions(RecognizeInvoicesOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeInvoicesOptions() : userProvidedOptions;
    }

    private static RecognizeIdentityDocumentOptions getRecognizeIdentityDocumentOptions(
        RecognizeIdentityDocumentOptions userProvidedOptions) {
        return userProvidedOptions == null ? new RecognizeIdentityDocumentOptions() : userProvidedOptions;
    }
}
