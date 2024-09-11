// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.AnalyzersImpl;
import com.azure.ai.formrecognizer.implementation.CustomModelsImpl;
import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.AnalyzersAnalyzeBusinessCardHeaders;
import com.azure.ai.formrecognizer.implementation.models.AnalyzersAnalyzeIdDocumentHeaders;
import com.azure.ai.formrecognizer.implementation.models.AnalyzersAnalyzeInvoiceHeaders;
import com.azure.ai.formrecognizer.implementation.models.AnalyzersAnalyzeLayoutHeaders;
import com.azure.ai.formrecognizer.implementation.models.AnalyzersAnalyzeReceiptHeaders;
import com.azure.ai.formrecognizer.implementation.models.ContentType;
import com.azure.ai.formrecognizer.implementation.models.CustomModelsAnalyzeDocumentHeaders;
import com.azure.ai.formrecognizer.implementation.models.ErrorResponseException;
import com.azure.ai.formrecognizer.implementation.models.Language;
import com.azure.ai.formrecognizer.implementation.models.Locale;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.implementation.models.ReadingOrder;
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
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.Transforms.toRecognizedForm;
import static com.azure.ai.formrecognizer.Transforms.toRecognizedLayout;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Constants.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.formrecognizer.implementation.Utility.getRecognizeBusinessCardsOptions;
import static com.azure.ai.formrecognizer.implementation.Utility.getRecognizeContentOptions;
import static com.azure.ai.formrecognizer.implementation.Utility.getRecognizeCustomFormOptions;
import static com.azure.ai.formrecognizer.implementation.Utility.getRecognizeIdentityDocumentOptions;
import static com.azure.ai.formrecognizer.implementation.Utility.getRecognizeInvoicesOptions;
import static com.azure.ai.formrecognizer.implementation.Utility.getRecognizeReceiptOptions;
import static com.azure.ai.formrecognizer.implementation.Utility.parseModelId;

/**
 * <p>This class provides an synchronous client to connect to the Form Recognizer Azure Cognitive Service.</p>
 * <p>This client provides synchronous methods to perform:</p>
 *
 * <ol>
 *   <li>Custom Form Analysis: Extraction and analysis of data from forms and documents specific to distinct business
 *   data and use cases. Use the custom trained model by passing its modelId into the
 *   {@link #beginRecognizeCustomForms(String, InputStream, long) beginRecognizeCustomForms} method.</li>
 *   <li>Prebuilt Model Analysis: Analyze receipts, business cards, invoices and other documents with
 *   <a href="https://aka.ms/form-recognizer-service-2.1.0">supported prebuilt models</a>
 *   Use the
 *   {@link #beginRecognizeReceipts(InputStream, long, RecognizeReceiptsOptions, Context) beginRecognizeReceipts}
 *   method to recognize receipt information.</li>
 *   <li>Layout Analysis: Extraction and analysis of text, selection marks, tables, and bounding box coordinates,
 *   from forms and documents. Use {@link #beginRecognizeContent(InputStream, long) beginRecognizeContent} method too
 *   perform layout analysis.</li>
 *   <li>Polling and Callbacks: It includes mechanisms for polling the service to check the status of an analysis
 *   operation or registering callbacks to receive notifications when the analysis is complete.</li>
 * </ol>
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
 * <p>The following code sample demonstrates the creation of a {@link FormRecognizerClient}, using the
 * `DefaultAzureCredentialBuilder` to configure it.</p>
 * <p>
 * <!-- src_embed readme-sample-createFormRecognizerClientWithAAD -->
 * <pre>
 * FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createFormRecognizerClientWithAAD  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.core.credential.AzureKeyCredential AzureKeyCredential} for client creation.</p>
 * <p>
 * <!-- src_embed readme-sample-createFormRecognizerClient -->
 * <pre>
 * FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createFormRecognizerClient  -->
 *
 * @see com.azure.ai.formrecognizer
 * @see FormRecognizerClientBuilder
 * @see FormRecognizerAsyncClient
 */
@ServiceClient(builder = FormRecognizerClientBuilder.class)
public final class FormRecognizerClient {
    private static final ClientLogger LOGGER = new ClientLogger(FormRecognizerClient.class);
    private final AnalyzersImpl analyzersImpl;
    private final CustomModelsImpl customModelsImpl;

    /**
     * Create a {@link FormRecognizerClient client} that sends requests to the Form Recognizer service's endpoint.
     * Each service call goes through the {@link FormRecognizerClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The service version
     */
    FormRecognizerClient(FormRecognizerClientImpl service, FormRecognizerServiceVersion serviceVersion) {
        this.analyzersImpl = service.getAnalyzers();
        this.customModelsImpl = service.getCustomModels();
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model with or without labels.
     * <p>The service does not support cancellation of the long running operation and returns with an error message
     * indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string -->
     * <pre>
     * String formUrl = &quot;&#123;form_url&#125;&quot;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     *
     * formRecognizerClient.beginRecognizeCustomFormsFromUrl&#40;modelId, formUrl&#41;.getFinalResult&#40;&#41;
     *     .stream&#40;&#41;
     *     .map&#40;RecognizedForm::getFields&#41;
     *     .forEach&#40;formFieldMap -&gt; formFieldMap.forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *         System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *     &#125;&#41;&#41;;
     *
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string -->

     * @param modelId The UUID string format custom trained model Id to be used.
     * @param formUrl The URL of the form to analyze.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
    beginRecognizeCustomFormsFromUrl(String modelId, String formUrl) {
        return beginRecognizeCustomFormsFromUrl(modelId, formUrl, null, Context.NONE);
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR)
     * and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-Options-Context -->
     * <pre>
     * String analyzeFilePath = &quot;&#123;file_source_url&#125;&quot;;
     * String modelId = &quot;&#123;model_id&#125;&quot;;
     * boolean includeFieldElements = true;
     *
     * formRecognizerClient.beginRecognizeCustomFormsFromUrl&#40;modelId, analyzeFilePath,
     *     new RecognizeCustomFormsOptions&#40;&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;
     *         .setPollInterval&#40;Duration.ofSeconds&#40;10&#41;&#41;, Context.NONE&#41;
     *     .getFinalResult&#40;&#41;
     *     .stream&#40;&#41;
     *     .map&#40;RecognizedForm::getFields&#41;
     *     .forEach&#40;formFieldMap -&gt; formFieldMap.forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *         System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *         System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *     &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-Options-Context -->

     * @param modelId The UUID string format custom trained model Id to be used.
     * @param formUrl The source URL to the input form.
     * @param recognizeCustomFormsOptions The additional configurable
     * {@link RecognizeCustomFormsOptions options} that may be passed when recognizing custom forms.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeCustomFormsFromUrl(
        String modelId, String formUrl, RecognizeCustomFormsOptions recognizeCustomFormsOptions, Context context) {
        return beginRecognizeCustomFormsFromUrlInternal(formUrl, modelId, recognizeCustomFormsOptions, context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeCustomFormsFromUrlInternal(String formUrl, String modelId, RecognizeCustomFormsOptions recognizeCustomFormsOptions, Context context) {
        if (CoreUtils.isNullOrEmpty(formUrl)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'formUrl' is required and cannot"
                + " be null or empty"));
        }
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                + " be null or empty"));
        }
        final RecognizeCustomFormsOptions finalRecognizeCustomFormsOptions
            = getRecognizeCustomFormOptions(recognizeCustomFormsOptions);
        UUID modelUuid = UUID.fromString(modelId);
        final boolean isFieldElementsIncluded = finalRecognizeCustomFormsOptions.isFieldElementsIncluded();
        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                analyzeActivationOperation(modelUuid,
                    formUrl,
                    null,
                    null,
                    0,
                    isFieldElementsIncluded,
                    finalRecognizeCustomFormsOptions,
                    context).apply(cxt)),
            pollingOperation(resultUid ->
                customModelsImpl.getAnalyzeResultWithResponse(modelUuid, resultUid, context)),
            getCancellationIsNotSupported(),
            fetchingOperation(resultId -> customModelsImpl.getAnalyzeResultWithResponse(modelUuid, resultId,
                context), isFieldElementsIncluded, modelId));
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model with or without labels.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeCustomForms#string-InputStream-long -->
     * <pre>
     * File form = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     * byte[] fileContent = Files.readAllBytes&#40;form.toPath&#40;&#41;&#41;;
     * try &#40;InputStream targetStream = new ByteArrayInputStream&#40;fileContent&#41;&#41; &#123;
     *
     *     formRecognizerClient.beginRecognizeCustomForms&#40;modelId, targetStream, form.length&#40;&#41;&#41;
     *         .getFinalResult&#40;&#41;
     *         .stream&#40;&#41;
     *         .map&#40;RecognizedForm::getFields&#41;
     *         .forEach&#40;formFieldMap -&gt; formFieldMap.forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeCustomForms#string-InputStream-long -->

     * @param modelId The UUID string format custom trained model Id to be used.
     *
     * @param form The data of the form to recognize form information from.
     * @param length The exact length of the data.
     *
     * @return A {@link SyncPoller} that polls the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeCustomForms(String modelId,
        InputStream form, long length) {
        return beginRecognizeCustomForms(modelId, form, length, null, Context.NONE);
    }

    /**
     * Recognizes form data from documents using optical character recognition (OCR) and a custom trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeCustomForms#string-InputStream-long-Options-Context -->
     * <pre>
     * File form = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * String modelId = &quot;&#123;custom_trained_model_id&#125;&quot;;
     * boolean includeFieldElements = true;
     * byte[] fileContent = Files.readAllBytes&#40;form.toPath&#40;&#41;&#41;;
     *
     * try &#40;InputStream targetStream = new ByteArrayInputStream&#40;fileContent&#41;&#41; &#123;
     *     formRecognizerClient.beginRecognizeCustomForms&#40;modelId, targetStream, form.length&#40;&#41;,
     *         new RecognizeCustomFormsOptions&#40;&#41;
     *             .setContentType&#40;FormContentType.IMAGE_JPEG&#41;
     *             .setFieldElementsIncluded&#40;includeFieldElements&#41;
     *             .setPollInterval&#40;Duration.ofSeconds&#40;10&#41;&#41;, Context.NONE&#41;
     *         .getFinalResult&#40;&#41;
     *         .stream&#40;&#41;
     *         .map&#40;RecognizedForm::getFields&#41;
     *         .forEach&#40;formFieldMap -&gt; formFieldMap.forEach&#40;&#40;fieldText, formField&#41; -&gt; &#123;
     *             System.out.printf&#40;&quot;Field text: %s%n&quot;, fieldText&#41;;
     *             System.out.printf&#40;&quot;Field value data text: %s%n&quot;, formField.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Confidence score: %.2f%n&quot;, formField.getConfidence&#40;&#41;&#41;;
     *         &#125;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeCustomForms#string-InputStream-long-Options-Context -->

     * @param modelId The UUID string format custom trained model Id to be used.
     * @param form The data of the form to recognize form information from.
     * @param length The exact length of the data.
     * @param recognizeCustomFormsOptions The additional configurable
     * {@link RecognizeCustomFormsOptions options} that may be passed when recognizing custom forms.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the recognize custom form operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form}, {@code modelId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeCustomForms(String modelId,
        InputStream form, long length, RecognizeCustomFormsOptions recognizeCustomFormsOptions, Context context) {
        return beginRecognizeCustomFormsInternal(modelId, form, length, recognizeCustomFormsOptions, context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeCustomFormsInternal(String modelId, InputStream form, long length,
                                                                                                              RecognizeCustomFormsOptions recognizeCustomFormsOptions, Context context) {
        if (form == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'form' is required and cannot"
                + " be null or empty"));
        }
        if (CoreUtils.isNullOrEmpty(modelId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'modelId' is required and cannot"
                + " be null or empty"));
        }
        final RecognizeCustomFormsOptions finalRecognizeCustomFormsOptions
            = getRecognizeCustomFormOptions(recognizeCustomFormsOptions);
        UUID modelUuid = UUID.fromString(modelId);
        final boolean isFieldElementsIncluded = finalRecognizeCustomFormsOptions.isFieldElementsIncluded();
        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            cxt -> {
                try {
                    return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, analyzeActivationOperation(modelUuid, null, finalRecognizeCustomFormsOptions.getContentType(),
                        BinaryData.fromStream(form), length, isFieldElementsIncluded, finalRecognizeCustomFormsOptions, context).apply(cxt));
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            },
            pollingOperation(resultUid ->
                        customModelsImpl.getAnalyzeResultWithResponse(modelUuid, resultUid, context)),
                    getCancellationIsNotSupported(),
                fetchingOperation(resultId -> customModelsImpl.getAnalyzeResultWithResponse(modelUuid, resultId,
                    context), isFieldElementsIncluded, modelId));
    }

    /**
     * Recognizes content/layout data from documents using optical character recognition (OCR).
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeContentFromUrl#string -->
     * <pre>
     * String formUrl = &quot;&#123;form_url&#125;&quot;;
     * formRecognizerClient.beginRecognizeContentFromUrl&#40;formUrl&#41;
     *     .getFinalResult&#40;&#41;
     *     .forEach&#40;formPage -&gt; &#123;
     *         System.out.printf&#40;&quot;Page Angle: %s%n&quot;, formPage.getTextAngle&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Page Dimension unit: %s%n&quot;, formPage.getUnit&#40;&#41;&#41;;
     *         &#47;&#47; Table information
     *         System.out.println&#40;&quot;Recognized Tables: &quot;&#41;;
     *         formPage.getTables&#40;&#41;
     *             .stream&#40;&#41;
     *             .flatMap&#40;formTable -&gt; formTable.getCells&#40;&#41;.stream&#40;&#41;&#41;
     *             .forEach&#40;recognizedTableCell -&gt; System.out.printf&#40;&quot;%s &quot;, recognizedTableCell.getText&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeContentFromUrl#string -->

     * @param formUrl The URL of the form to analyze.
     *
     * @return A {@link SyncPoller} that polls the recognize content form operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContentFromUrl(String formUrl) {
        return beginRecognizeContentFromUrl(formUrl, null, Context.NONE);
    }

    /**
     * Recognizes content/layout data using optical character recognition (OCR).
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p>Content recognition supports auto language identification and multilanguage documents, so only
     * provide a language code if you would like to force the documented to be processed as
     * that specific language in the {@link RecognizeContentOptions options}.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeContentFromUrl#string-Options-Context -->
     * <pre>
     * String formPath = &quot;&#123;file_source_url&#125;&quot;;
     * formRecognizerClient.beginRecognizeContentFromUrl&#40;formPath,
     *     new RecognizeContentOptions&#40;&#41;
     *         .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;, Context.NONE&#41;
     *     .getFinalResult&#40;&#41;
     *     .forEach&#40;formPage -&gt; &#123;
     *         System.out.printf&#40;&quot;Page Angle: %s%n&quot;, formPage.getTextAngle&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Page Dimension unit: %s%n&quot;, formPage.getUnit&#40;&#41;&#41;;
     *         &#47;&#47; Table information
     *         System.out.println&#40;&quot;Recognized Tables: &quot;&#41;;
     *         formPage.getTables&#40;&#41;
     *             .stream&#40;&#41;
     *             .flatMap&#40;formTable -&gt; formTable.getCells&#40;&#41;.stream&#40;&#41;&#41;
     *             .forEach&#40;recognizedTableCell -&gt; System.out.printf&#40;&quot;%s &quot;, recognizedTableCell.getText&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeContentFromUrl#string-Options-Context -->

     * @param formUrl The source URL to the input form.
     * @param recognizeContentOptions The additional configurable {@link RecognizeContentOptions options}
     * that may be passed when recognizing content/layout on a form.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the recognize layout operation until it has completed, has
     * failed, or has been cancelled. The completed operation returns a list of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code formUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContentFromUrl(String formUrl,
        RecognizeContentOptions recognizeContentOptions, Context context) {
        return beginRecognizeContentFromUrlInternal(formUrl, recognizeContentOptions, context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContentFromUrlInternal(String formUrl,
                                                                                           RecognizeContentOptions recognizeContentOptions, Context context) {
        if (formUrl == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'formUrl' is required and cannot be null."));
        }

        RecognizeContentOptions finalRecognizeContentOptions = getRecognizeContentOptions(recognizeContentOptions);

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            (cxt) -> {
                try {
                    ResponseBase<AnalyzersAnalyzeLayoutHeaders, Void> analyzeLayoutWithResponse = analyzersImpl.analyzeLayoutWithResponse(
                        finalRecognizeContentOptions.getPages(),
                        Language.fromString(Objects.toString(finalRecognizeContentOptions.getLanguage(), null)),
                        ReadingOrder.fromString(
                            Objects.toString(finalRecognizeContentOptions.getReadingOrder(), null)),
                        new SourcePath().setSource(formUrl),
                        context);
                    return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new FormRecognizerOperationResult(parseModelId(analyzeLayoutWithResponse.getDeserializedHeaders().getOperationLocation())));
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            },
            pollingOperation(resultId -> {
                try {
                    return analyzersImpl.getAnalyzeLayoutResultWithResponse(resultId, context);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            }),
            getCancellationIsNotSupported(),
            pollingContext -> {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                try {
                    return toRecognizedLayout(analyzersImpl.getAnalyzeLayoutResultWithResponse(UUID.fromString(resultId), context).getValue().getAnalyzeResult(), true);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            });
    }

    /**
     * Recognizes layout data using optical character recognition (OCR) and a custom trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeContent#InputStream-long -->
     * <pre>
     * File form = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.pdf&#125;&quot;&#41;;
     * byte[] fileContent = Files.readAllBytes&#40;form.toPath&#40;&#41;&#41;;
     * try &#40;InputStream targetStream = new ByteArrayInputStream&#40;fileContent&#41;&#41; &#123;
     *     formRecognizerClient.beginRecognizeContent&#40;targetStream, form.length&#40;&#41;&#41;
     *         .getFinalResult&#40;&#41;
     *         .forEach&#40;formPage -&gt; &#123;
     *             System.out.printf&#40;&quot;Page Angle: %s%n&quot;, formPage.getTextAngle&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Page Dimension unit: %s%n&quot;, formPage.getUnit&#40;&#41;&#41;;
     *             &#47;&#47; Table information
     *             System.out.println&#40;&quot;Recognized Tables: &quot;&#41;;
     *             formPage.getTables&#40;&#41;
     *                 .stream&#40;&#41;
     *                 .flatMap&#40;formTable -&gt; formTable.getCells&#40;&#41;.stream&#40;&#41;&#41;
     *                 .forEach&#40;recognizedTableCell -&gt; System.out.printf&#40;&quot;%s &quot;, recognizedTableCell.getText&#40;&#41;&#41;&#41;;
     *         &#125;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeContent#InputStream-long -->
     * @param form The data of the form to recognize content information from.
     * @param length The exact length of the data.
     *
     * @return A {@link SyncPoller} that polls the recognize content operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContent(InputStream form,
        long length) {
        return beginRecognizeContent(form, length, null, Context.NONE);
    }

    /**
     * Recognizes content/layout data from the provided document data using optical character recognition (OCR).
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p>Content recognition supports auto language identification and multilanguage documents, so only
     * provide a language code if you would like to force the documented to be processed as
     * that specific language in the {@link RecognizeContentOptions options}.</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeContent#InputStream-long-Options-Context -->
     * <pre>
     * File form = new File&#40;&quot;&#123;file_source_url&#125;&quot;&#41;;
     * byte[] fileContent = Files.readAllBytes&#40;form.toPath&#40;&#41;&#41;;
     * try &#40;InputStream targetStream = new ByteArrayInputStream&#40;fileContent&#41;&#41; &#123;
     *
     *     for &#40;FormPage formPage : formRecognizerClient.beginRecognizeContent&#40;targetStream, form.length&#40;&#41;,
     *         new RecognizeContentOptions&#40;&#41;
     *             .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;, Context.NONE&#41;
     *         .getFinalResult&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Page Angle: %s%n&quot;, formPage.getTextAngle&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Page Dimension unit: %s%n&quot;, formPage.getUnit&#40;&#41;&#41;;
     *         &#47;&#47; Table information
     *         System.out.println&#40;&quot;Recognized Tables: &quot;&#41;;
     *         formPage.getTables&#40;&#41;
     *             .stream&#40;&#41;
     *             .flatMap&#40;formTable -&gt; formTable.getCells&#40;&#41;.stream&#40;&#41;&#41;
     *             .forEach&#40;recognizedTableCell -&gt; System.out.printf&#40;&quot;%s &quot;, recognizedTableCell.getText&#40;&#41;&#41;&#41;;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeContent#InputStream-long-Options-Context -->
     *  @param form The data of the form to recognize content information from.
     * @param length The exact length of the data.
     * @param recognizeContentOptions The additional configurable {@link RecognizeContentOptions options}
     * that may be passed when recognizing content/layout on a form.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the recognize content operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link FormPage}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code form} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContent(InputStream form,
        long length, RecognizeContentOptions recognizeContentOptions, Context context) {
        return beginRecognizeContentInternal(form, length, recognizeContentOptions, context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<FormPage>> beginRecognizeContentInternal(InputStream form, long length, RecognizeContentOptions recognizeContentOptions, Context context) {
        if (form == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'form' is required and cannot be null."));
        }

        RecognizeContentOptions finalRecognizeContentOptions = getRecognizeContentOptions(recognizeContentOptions);

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            (cxt) -> {
                ResponseBase<AnalyzersAnalyzeLayoutHeaders, Void> analyzeLayoutWithResponse = analyzersImpl.analyzeLayoutWithResponse(
                    finalRecognizeContentOptions.getContentType() != null ? ContentType.fromString(finalRecognizeContentOptions.getContentType().toString()) : null,
                    finalRecognizeContentOptions.getPages(),
                    Language.fromString(Objects.toString(finalRecognizeContentOptions.getLanguage(), null)),
                    ReadingOrder.fromString(
                        Objects.toString(finalRecognizeContentOptions.getReadingOrder(), null)),
                    BinaryData.fromStream(form),
                    length,
                    context);
                return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new FormRecognizerOperationResult(parseModelId(analyzeLayoutWithResponse.getDeserializedHeaders().getOperationLocation())));
            },
            pollingOperation(resultId -> analyzersImpl.getAnalyzeLayoutResultWithResponse(resultId, context)),
            getCancellationIsNotSupported(),
            pollingContext -> {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                try {
                    return toRecognizedLayout(analyzersImpl.getAnalyzeLayoutResultWithResponse(UUID.fromString(resultId), context).getValue().getAnalyzeResult(), true);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            });
    }

    /**
     * Recognizes receipt data from document using optical character recognition (OCR) and a prebuilt receipt trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/formrecognizer/receiptfields">here</a> for fields found on a receipt.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string -->
     * <pre>
     * String receiptUrl = &quot;&#123;file_source_url&#125;&quot;;
     * formRecognizerClient.beginRecognizeReceiptsFromUrl&#40;receiptUrl&#41;
     *     .getFinalResult&#40;&#41;
     *     .forEach&#40;recognizedReceipt -&gt; &#123;
     *         Map&lt;String, FormField&gt; recognizedFields = recognizedReceipt.getFields&#40;&#41;;
     *         FormField merchantNameField = recognizedFields.get&#40;&quot;MerchantName&quot;&#41;;
     *         if &#40;merchantNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == merchantNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String merchantName = merchantNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Merchant Name: %s, confidence: %.2f%n&quot;,
     *                     merchantName, merchantNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField merchantPhoneNumberField = recognizedFields.get&#40;&quot;MerchantPhoneNumber&quot;&#41;;
     *         if &#40;merchantPhoneNumberField != null&#41; &#123;
     *             if &#40;FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String merchantAddress = merchantPhoneNumberField.getValue&#40;&#41;.asPhoneNumber&#40;&#41;;
     *                 System.out.printf&#40;&quot;Merchant Phone number: %s, confidence: %.2f%n&quot;,
     *                     merchantAddress, merchantPhoneNumberField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField transactionDateField = recognizedFields.get&#40;&quot;TransactionDate&quot;&#41;;
     *         if &#40;transactionDateField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == transactionDateField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate transactionDate = transactionDateField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Transaction Date: %s, confidence: %.2f%n&quot;,
     *                     transactionDate, transactionDateField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField receiptItemsField = recognizedFields.get&#40;&quot;Items&quot;&#41;;
     *         if &#40;receiptItemsField != null&#41; &#123;
     *             System.out.printf&#40;&quot;Receipt Items: %n&quot;&#41;;
     *             if &#40;FieldValueType.LIST == receiptItemsField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 List&lt;FormField&gt; receiptItems = receiptItemsField.getValue&#40;&#41;.asList&#40;&#41;;
     *                 receiptItems.stream&#40;&#41;
     *                     .filter&#40;receiptItem -&gt; FieldValueType.MAP == receiptItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
     *                     .map&#40;formField -&gt; formField.getValue&#40;&#41;.asMap&#40;&#41;&#41;
     *                     .forEach&#40;formFieldMap -&gt; formFieldMap.forEach&#40;&#40;key, formField&#41; -&gt; &#123;
     *                         if &#40;&quot;Quantity&quot;.equals&#40;key&#41;&#41; &#123;
     *                             if &#40;FieldValueType.FLOAT == formField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                 Float quantity = formField.getValue&#40;&#41;.asFloat&#40;&#41;;
     *                                 System.out.printf&#40;&quot;Quantity: %f, confidence: %.2f%n&quot;,
     *                                     quantity, formField.getConfidence&#40;&#41;&#41;;
     *                             &#125;
     *                         &#125;
     *                     &#125;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string -->
     * @param receiptUrl The URL of the receipt to analyze.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceiptsFromUrl(
        String receiptUrl) {
        return beginRecognizeReceiptsFromUrl(receiptUrl, null, Context.NONE);
    }

    /**
     * Recognizes receipt data from documents using optical character recognition (OCR) and a
     * prebuilt receipt trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-Options-Context -->
     * <pre>
     * String receiptUrl = &quot;&#123;receipt_url&#125;&quot;;
     * formRecognizerClient.beginRecognizeReceiptsFromUrl&#40;receiptUrl,
     *     new RecognizeReceiptsOptions&#40;&#41;
     *         .setLocale&#40;FormRecognizerLocale.EN_US&#41;
     *         .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *         .setFieldElementsIncluded&#40;true&#41;, Context.NONE&#41;
     *     .getFinalResult&#40;&#41;
     *     .forEach&#40;recognizedReceipt -&gt; &#123;
     *         Map&lt;String, FormField&gt; recognizedFields = recognizedReceipt.getFields&#40;&#41;;
     *         FormField merchantNameField = recognizedFields.get&#40;&quot;MerchantName&quot;&#41;;
     *         if &#40;merchantNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == merchantNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String merchantName = merchantNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Merchant Name: %s, confidence: %.2f%n&quot;,
     *                     merchantName, merchantNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField merchantPhoneNumberField = recognizedFields.get&#40;&quot;MerchantPhoneNumber&quot;&#41;;
     *         if &#40;merchantPhoneNumberField != null&#41; &#123;
     *             if &#40;FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String merchantAddress = merchantPhoneNumberField.getValue&#40;&#41;.asPhoneNumber&#40;&#41;;
     *                 System.out.printf&#40;&quot;Merchant Phone number: %s, confidence: %.2f%n&quot;,
     *                     merchantAddress, merchantPhoneNumberField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField transactionDateField = recognizedFields.get&#40;&quot;TransactionDate&quot;&#41;;
     *         if &#40;transactionDateField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == transactionDateField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate transactionDate = transactionDateField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Transaction Date: %s, confidence: %.2f%n&quot;,
     *                     transactionDate, transactionDateField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField receiptItemsField = recognizedFields.get&#40;&quot;Items&quot;&#41;;
     *         if &#40;receiptItemsField != null&#41; &#123;
     *             System.out.printf&#40;&quot;Receipt Items: %n&quot;&#41;;
     *             if &#40;FieldValueType.LIST == receiptItemsField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 List&lt;FormField&gt; receiptItems = receiptItemsField.getValue&#40;&#41;.asList&#40;&#41;;
     *                 receiptItems.stream&#40;&#41;
     *                     .filter&#40;receiptItem -&gt; FieldValueType.MAP == receiptItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
     *                     .map&#40;formField -&gt; formField.getValue&#40;&#41;.asMap&#40;&#41;&#41;
     *                     .forEach&#40;formFieldMap -&gt; formFieldMap.forEach&#40;&#40;key, formField&#41; -&gt; &#123;
     *                         if &#40;&quot;Quantity&quot;.equals&#40;key&#41;&#41; &#123;
     *                             if &#40;FieldValueType.FLOAT == formField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                 Float quantity = formField.getValue&#40;&#41;.asFloat&#40;&#41;;
     *                                 System.out.printf&#40;&quot;Quantity: %f, confidence: %.2f%n&quot;,
     *                                     quantity, formField.getConfidence&#40;&#41;&#41;;
     *                             &#125;
     *                         &#125;
     *                     &#125;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-Options-Context -->
     * @param receiptUrl The source URL to the input receipt.
     * @param recognizeReceiptsOptions The additional configurable {@link RecognizeReceiptsOptions options}
     * that may be passed when analyzing a receipt.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receiptUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceiptsFromUrl(
        String receiptUrl, RecognizeReceiptsOptions recognizeReceiptsOptions, Context context) {
        return beginRecognizeReceiptsFromUrlInternal(receiptUrl, recognizeReceiptsOptions, context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceiptsFromUrlInternal(String receiptUrl, RecognizeReceiptsOptions recognizeReceiptsOptions, Context context) {
        if (receiptUrl == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'receiptUrl' is required and cannot be null."));
        }

        RecognizeReceiptsOptions finalRecognizeReceiptsOptions = getRecognizeReceiptOptions(recognizeReceiptsOptions);
        final boolean isFieldElementsIncluded = finalRecognizeReceiptsOptions.isFieldElementsIncluded();
        final FormRecognizerLocale localeInfo  = finalRecognizeReceiptsOptions.getLocale();

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            (cxt) -> {
                ResponseBase<AnalyzersAnalyzeReceiptHeaders, Void> analyzeReceiptWithResponse = analyzersImpl.analyzeReceiptWithResponse(
                    isFieldElementsIncluded,
                    Locale.fromString(Objects.toString(localeInfo, null)),
                    finalRecognizeReceiptsOptions.getPages(),
                    new SourcePath().setSource(receiptUrl), context);
                return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new FormRecognizerOperationResult(parseModelId(analyzeReceiptWithResponse.getDeserializedHeaders().getOperationLocation())));
            },
            pollingOperation(resultId -> analyzersImpl.getAnalyzeReceiptResultWithResponse(resultId, context)),
            getCancellationIsNotSupported(),
            pollingContext -> {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                try {
                    return toRecognizedForm(analyzersImpl.getAnalyzeReceiptResultWithResponse(UUID.fromString(resultId), context).getValue().getAnalyzeResult(), true, null);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            });
    }

    /**
     * Recognizes data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/formrecognizer/receiptfields">here</a> for fields found on a receipt.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeReceipts#InputStream-long -->
     * <pre>
     * File receipt = new File&#40;&quot;&#123;receipt_url&#125;&quot;&#41;;
     * byte[] fileContent = Files.readAllBytes&#40;receipt.toPath&#40;&#41;&#41;;
     * try &#40;InputStream targetStream = new ByteArrayInputStream&#40;fileContent&#41;&#41; &#123;
     *
     *     formRecognizerClient.beginRecognizeReceipts&#40;targetStream, receipt.length&#40;&#41;&#41;.getFinalResult&#40;&#41;
     *         .forEach&#40;recognizedReceipt -&gt; &#123;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedReceipt.getFields&#40;&#41;;
     *             FormField merchantNameField = recognizedFields.get&#40;&quot;MerchantName&quot;&#41;;
     *             if &#40;merchantNameField != null&#41; &#123;
     *                 if &#40;FieldValueType.STRING == merchantNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String merchantName = merchantNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                     System.out.printf&#40;&quot;Merchant Name: %s, confidence: %.2f%n&quot;,
     *                         merchantName, merchantNameField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField merchantPhoneNumberField = recognizedFields.get&#40;&quot;MerchantPhoneNumber&quot;&#41;;
     *             if &#40;merchantPhoneNumberField != null&#41; &#123;
     *                 if &#40;FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     String merchantAddress = merchantPhoneNumberField.getValue&#40;&#41;.asPhoneNumber&#40;&#41;;
     *                     System.out.printf&#40;&quot;Merchant Phone number: %s, confidence: %.2f%n&quot;,
     *                         merchantAddress, merchantPhoneNumberField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField transactionDateField = recognizedFields.get&#40;&quot;TransactionDate&quot;&#41;;
     *             if &#40;transactionDateField != null&#41; &#123;
     *                 if &#40;FieldValueType.DATE == transactionDateField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     LocalDate transactionDate = transactionDateField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                     System.out.printf&#40;&quot;Transaction Date: %s, confidence: %.2f%n&quot;,
     *                         transactionDate, transactionDateField.getConfidence&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *
     *             FormField receiptItemsField = recognizedFields.get&#40;&quot;Items&quot;&#41;;
     *             if &#40;receiptItemsField != null&#41; &#123;
     *                 System.out.printf&#40;&quot;Receipt Items: %n&quot;&#41;;
     *                 if &#40;FieldValueType.LIST == receiptItemsField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                     List&lt;FormField&gt; receiptItems = receiptItemsField.getValue&#40;&#41;.asList&#40;&#41;;
     *                     receiptItems.stream&#40;&#41;
     *                         .filter&#40;receiptItem -&gt; FieldValueType.MAP == receiptItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
     *                         .map&#40;formField -&gt; formField.getValue&#40;&#41;.asMap&#40;&#41;&#41;
     *                         .forEach&#40;formFieldMap -&gt; formFieldMap.forEach&#40;&#40;key, formField&#41; -&gt; &#123;
     *                             if &#40;&quot;Quantity&quot;.equals&#40;key&#41;&#41; &#123;
     *                                 if &#40;FieldValueType.FLOAT == formField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                     Float quantity = formField.getValue&#40;&#41;.asFloat&#40;&#41;;
     *                                     System.out.printf&#40;&quot;Quantity: %f, confidence: %.2f%n&quot;,
     *                                         quantity, formField.getConfidence&#40;&#41;&#41;;
     *                                 &#125;
     *                             &#125;
     *                         &#125;&#41;&#41;;
     *                 &#125;
     *             &#125;
     *         &#125;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeReceipts#InputStream-long -->
     * @param receipt The data of the receipt to recognize receipt information from.
     * @param length The exact length of the data.
     *
     * @return A {@link SyncPoller} that polls the recognize receipt operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receipt} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceipts(InputStream receipt,
        long length) {
        return beginRecognizeReceipts(receipt, length, null, Context.NONE);
    }

    /**
     * Recognizes data from the provided document data using optical character recognition (OCR) and a prebuilt
     * trained receipt model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/formrecognizer/receiptfields">here</a> for fields found on a receipt.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-Options-Context -->
     * <pre>
     * File receipt = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * boolean includeFieldElements = true;
     * byte[] fileContent = Files.readAllBytes&#40;receipt.toPath&#40;&#41;&#41;;
     * try &#40;InputStream targetStream = new ByteArrayInputStream&#40;fileContent&#41;&#41; &#123;
     *     for &#40;RecognizedForm recognizedForm : formRecognizerClient
     *         .beginRecognizeReceipts&#40;targetStream, receipt.length&#40;&#41;,
     *             new RecognizeReceiptsOptions&#40;&#41;
     *                 .setContentType&#40;FormContentType.IMAGE_JPEG&#41;
     *                 .setFieldElementsIncluded&#40;includeFieldElements&#41;
     *                 .setLocale&#40;FormRecognizerLocale.EN_US&#41;
     *                 .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;, Context.NONE&#41;
     *         .getFinalResult&#40;&#41;&#41; &#123;
     *         Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *         FormField merchantNameField = recognizedFields.get&#40;&quot;MerchantName&quot;&#41;;
     *         if &#40;merchantNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == merchantNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String merchantName = merchantNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Merchant Name: %s, confidence: %.2f%n&quot;,
     *                     merchantName, merchantNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *         FormField merchantPhoneNumberField = recognizedFields.get&#40;&quot;MerchantPhoneNumber&quot;&#41;;
     *         if &#40;merchantPhoneNumberField != null&#41; &#123;
     *             if &#40;FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String merchantAddress = merchantPhoneNumberField.getValue&#40;&#41;.asPhoneNumber&#40;&#41;;
     *                 System.out.printf&#40;&quot;Merchant Phone number: %s, confidence: %.2f%n&quot;,
     *                     merchantAddress, merchantPhoneNumberField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *         FormField transactionDateField = recognizedFields.get&#40;&quot;TransactionDate&quot;&#41;;
     *         if &#40;transactionDateField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == transactionDateField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate transactionDate = transactionDateField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Transaction Date: %s, confidence: %.2f%n&quot;,
     *                     transactionDate, transactionDateField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *         FormField receiptItemsField = recognizedFields.get&#40;&quot;Items&quot;&#41;;
     *         if &#40;receiptItemsField != null&#41; &#123;
     *             System.out.printf&#40;&quot;Receipt Items: %n&quot;&#41;;
     *             if &#40;FieldValueType.LIST == receiptItemsField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 List&lt;FormField&gt; receiptItems = receiptItemsField.getValue&#40;&#41;.asList&#40;&#41;;
     *                 receiptItems.stream&#40;&#41;
     *                     .filter&#40;receiptItem -&gt; FieldValueType.MAP == receiptItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
     *                     .map&#40;formField -&gt; formField.getValue&#40;&#41;.asMap&#40;&#41;&#41;
     *                     .forEach&#40;formFieldMap -&gt; formFieldMap.forEach&#40;&#40;key, formField&#41; -&gt; &#123;
     *                         if &#40;&quot;Quantity&quot;.equals&#40;key&#41;&#41; &#123;
     *                             if &#40;FieldValueType.FLOAT == formField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                 Float quantity = formField.getValue&#40;&#41;.asFloat&#40;&#41;;
     *                                 System.out.printf&#40;&quot;Quantity: %f, confidence: %.2f%n&quot;,
     *                                     quantity, formField.getConfidence&#40;&#41;&#41;;
     *                             &#125;
     *                         &#125;
     *                     &#125;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-Options-Context -->
     * @param receipt The data of the receipt to recognize receipt information from.
     * @param length The exact length of the data.
     * @param recognizeReceiptsOptions The additional configurable {@link RecognizeReceiptsOptions options}
     * that may be passed when analyzing a receipt.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the recognize receipt operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code receipt} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceipts(InputStream receipt,
        long length, RecognizeReceiptsOptions recognizeReceiptsOptions, Context context) {
        return beginRecognizeReceiptsInternal(receipt, length, recognizeReceiptsOptions, context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeReceiptsInternal(InputStream receipt, long length, RecognizeReceiptsOptions recognizeReceiptsOptions, Context context) {
        if (receipt == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'receipt' is required and cannot be null."));
        }

        RecognizeReceiptsOptions finalRecognizeReceiptsOptions = getRecognizeReceiptOptions(recognizeReceiptsOptions);
        final boolean isFieldElementsIncluded = finalRecognizeReceiptsOptions.isFieldElementsIncluded();
        final FormRecognizerLocale localeInfo  = finalRecognizeReceiptsOptions.getLocale();

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            (cxt) -> {
                try {
                    ResponseBase<AnalyzersAnalyzeReceiptHeaders, Void> analyzeReceiptWithResponse = analyzersImpl.analyzeReceiptWithResponse(
                        finalRecognizeReceiptsOptions.getContentType() != null ? ContentType.fromString(finalRecognizeReceiptsOptions.getContentType().toString()) : null,
                        isFieldElementsIncluded,
                        Locale.fromString(Objects.toString(localeInfo, null)),
                        finalRecognizeReceiptsOptions.getPages(),
                        BinaryData.fromStream(receipt),
                        length, context);
                    return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new FormRecognizerOperationResult(parseModelId(analyzeReceiptWithResponse.getDeserializedHeaders().getOperationLocation())));
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            },
            pollingOperation(resultId -> {
                try {
                    return analyzersImpl.getAnalyzeReceiptResultWithResponse(resultId, context);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            }),
            getCancellationIsNotSupported(),
            pollingContext -> {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                try {
                    return toRecognizedForm(analyzersImpl.getAnalyzeReceiptResultWithResponse(UUID.fromString(resultId), context).getValue().getAnalyzeResult(), isFieldElementsIncluded, null);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            });
    }

    /**
     * Recognizes business card data from document using optical character recognition (OCR) and a prebuilt
     * business card trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/formrecognizer/businesscardfields">here</a> for fields found on a business card.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeBusinessCardsFromUrl#string -->
     * <pre>
     * String businessCardUrl = &quot;&#123;business_card_url&#125;&quot;;
     * formRecognizerClient.beginRecognizeBusinessCardsFromUrl&#40;businessCardUrl&#41;
     *     .getFinalResult&#40;&#41;
     *     .forEach&#40;recognizedBusinessCard -&gt; &#123;
     *         Map&lt;String, FormField&gt; recognizedFields = recognizedBusinessCard.getFields&#40;&#41;;
     *         FormField contactNamesFormField = recognizedFields.get&#40;&quot;ContactNames&quot;&#41;;
     *         if &#40;contactNamesFormField != null&#41; &#123;
     *             if &#40;FieldValueType.LIST == contactNamesFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 List&lt;FormField&gt; contactNamesList = contactNamesFormField.getValue&#40;&#41;.asList&#40;&#41;;
     *                 contactNamesList.stream&#40;&#41;
     *                     .filter&#40;contactName -&gt; FieldValueType.MAP == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
     *                     .map&#40;contactName -&gt; &#123;
     *                         System.out.printf&#40;&quot;Contact name: %s%n&quot;, contactName.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *                         return contactName.getValue&#40;&#41;.asMap&#40;&#41;;
     *                     &#125;&#41;
     *                     .forEach&#40;contactNamesMap -&gt; contactNamesMap.forEach&#40;&#40;key, contactName&#41; -&gt; &#123;
     *                         if &#40;&quot;FirstName&quot;.equals&#40;key&#41;&#41; &#123;
     *                             if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                 String firstName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                 System.out.printf&#40;&quot;&#92;tFirst Name: %s, confidence: %.2f%n&quot;,
     *                                     firstName, contactName.getConfidence&#40;&#41;&#41;;
     *                             &#125;
     *                         &#125;
     *                         if &#40;&quot;LastName&quot;.equals&#40;key&#41;&#41; &#123;
     *                             if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                 String lastName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                 System.out.printf&#40;&quot;&#92;tLast Name: %s, confidence: %.2f%n&quot;,
     *                                     lastName, contactName.getConfidence&#40;&#41;&#41;;
     *                             &#125;
     *                         &#125;
     *                     &#125;&#41;&#41;;
     *             &#125;
     *         &#125;
     *         FormField jobTitles = recognizedFields.get&#40;&quot;JobTitles&quot;&#41;;
     *         if &#40;jobTitles != null&#41; &#123;
     *             if &#40;FieldValueType.LIST == jobTitles.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 List&lt;FormField&gt; jobTitlesItems = jobTitles.getValue&#40;&#41;.asList&#40;&#41;;
     *                 jobTitlesItems.forEach&#40;jobTitlesItem -&gt; &#123;
     *                     if &#40;FieldValueType.STRING == jobTitlesItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                         String jobTitle = jobTitlesItem.getValue&#40;&#41;.asString&#40;&#41;;
     *                         System.out.printf&#40;&quot;Job Title: %s, confidence: %.2f%n&quot;,
     *                             jobTitle, jobTitlesItem.getConfidence&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeBusinessCardsFromUrl#string -->
     *
     * @param businessCardUrl The source URL to the input business card.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize business card operation until it has
     * completed, has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code businessCardUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCardsFromUrl(
        String businessCardUrl) {
        return beginRecognizeBusinessCardsFromUrl(businessCardUrl, null, Context.NONE);
    }

    /**
     * Recognizes business card data from documents using optical character recognition (OCR) and a
     * prebuilt business card trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/formrecognizer/businesscardfields">here</a> for fields found on a business card.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeBusinessCardsFromUrl#string-Options-Context -->
     * <pre>
     * String businessCardUrl = &quot;&#123;business_card_url&#125;&quot;;
     * formRecognizerClient.beginRecognizeBusinessCardsFromUrl&#40;businessCardUrl,
     *     new RecognizeBusinessCardsOptions&#40;&#41;
     *         .setFieldElementsIncluded&#40;true&#41;, Context.NONE&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;.getFinalResult&#40;&#41;
     *     .forEach&#40;recognizedBusinessCard -&gt; &#123;
     *         Map&lt;String, FormField&gt; recognizedFields = recognizedBusinessCard.getFields&#40;&#41;;
     *         FormField contactNamesFormField = recognizedFields.get&#40;&quot;ContactNames&quot;&#41;;
     *         if &#40;contactNamesFormField != null&#41; &#123;
     *             if &#40;FieldValueType.LIST == contactNamesFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 List&lt;FormField&gt; contactNamesList = contactNamesFormField.getValue&#40;&#41;.asList&#40;&#41;;
     *                 contactNamesList.stream&#40;&#41;
     *                     .filter&#40;contactName -&gt; FieldValueType.MAP == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
     *                     .map&#40;contactName -&gt; &#123;
     *                         System.out.printf&#40;&quot;Contact name: %s%n&quot;, contactName.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *                         return contactName.getValue&#40;&#41;.asMap&#40;&#41;;
     *                     &#125;&#41;
     *                     .forEach&#40;contactNamesMap -&gt; contactNamesMap.forEach&#40;&#40;key, contactName&#41; -&gt; &#123;
     *                         if &#40;&quot;FirstName&quot;.equals&#40;key&#41;&#41; &#123;
     *                             if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                 String firstName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                 System.out.printf&#40;&quot;&#92;tFirst Name: %s, confidence: %.2f%n&quot;,
     *                                     firstName, contactName.getConfidence&#40;&#41;&#41;;
     *                             &#125;
     *                         &#125;
     *                         if &#40;&quot;LastName&quot;.equals&#40;key&#41;&#41; &#123;
     *                             if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                 String lastName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                 System.out.printf&#40;&quot;&#92;tLast Name: %s, confidence: %.2f%n&quot;,
     *                                     lastName, contactName.getConfidence&#40;&#41;&#41;;
     *                             &#125;
     *                         &#125;
     *                     &#125;&#41;&#41;;
     *             &#125;
     *         &#125;
     *         FormField jobTitles = recognizedFields.get&#40;&quot;JobTitles&quot;&#41;;
     *         if &#40;jobTitles != null&#41; &#123;
     *             if &#40;FieldValueType.LIST == jobTitles.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 List&lt;FormField&gt; jobTitlesItems = jobTitles.getValue&#40;&#41;.asList&#40;&#41;;
     *                 jobTitlesItems.forEach&#40;jobTitlesItem -&gt; &#123;
     *                     if &#40;FieldValueType.STRING == jobTitlesItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                         String jobTitle = jobTitlesItem.getValue&#40;&#41;.asString&#40;&#41;;
     *                         System.out.printf&#40;&quot;Job Title: %s, confidence: %.2f%n&quot;,
     *                             jobTitle, jobTitlesItem.getConfidence&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeBusinessCardsFromUrl#string-Options-Context -->
     * @param businessCardUrl The source URL to the input business card.
     * @param recognizeBusinessCardsOptions The additional configurable {@link RecognizeBusinessCardsOptions options}
     * that may be passed when analyzing a business card.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize business card operation until it has
     * completed, has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code businessCardUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCardsFromUrl(
        String businessCardUrl, RecognizeBusinessCardsOptions recognizeBusinessCardsOptions, Context context) {
        return beginRecognizeBusinessCardsFromUrlInternal(businessCardUrl, recognizeBusinessCardsOptions, context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCardsFromUrlInternal(String businessCardUrl, RecognizeBusinessCardsOptions recognizeBusinessCardsOptions, Context context) {
        if (businessCardUrl == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'businessCardUrl' is required and cannot be null."));
        }

        RecognizeBusinessCardsOptions finalRecognizeBusinessCardsOptions = getRecognizeBusinessCardsOptions(recognizeBusinessCardsOptions);
        final boolean isFieldElementsIncluded = finalRecognizeBusinessCardsOptions.isFieldElementsIncluded();
        final FormRecognizerLocale localeInfo  = finalRecognizeBusinessCardsOptions.getLocale();

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            (cxt) -> {
                try {
                    ResponseBase<AnalyzersAnalyzeBusinessCardHeaders, Void> analyzeBusinessCardWithResponse = analyzersImpl.analyzeBusinessCardWithResponse(
                        isFieldElementsIncluded,
                        Locale.fromString(Objects.toString(localeInfo, null)),
                        finalRecognizeBusinessCardsOptions.getPages(),
                        new SourcePath().setSource(businessCardUrl), context);
                    return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new FormRecognizerOperationResult(parseModelId(analyzeBusinessCardWithResponse.getDeserializedHeaders().getOperationLocation())));
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            },
            pollingOperation(resultId -> {
                try {
                    return analyzersImpl.getAnalyzeBusinessCardResultWithResponse(resultId, context);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            }),
            getCancellationIsNotSupported(),
            pollingContext -> {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                try {
                    return toRecognizedForm(analyzersImpl.getAnalyzeBusinessCardResultWithResponse(UUID.fromString(resultId), context).getValue().getAnalyzeResult(), true, null);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            });
    }

    /**
     * Recognizes business card data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained business card model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/formrecognizer/businesscardfields">here</a> for fields found on a business card.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeBusinessCards#InputStream-long -->
     * <pre>
     * File businessCard = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * byte[] fileContent = Files.readAllBytes&#40;businessCard.toPath&#40;&#41;&#41;;
     * try &#40;InputStream targetStream = new ByteArrayInputStream&#40;fileContent&#41;&#41; &#123;
     *     formRecognizerClient.beginRecognizeBusinessCards&#40;targetStream, businessCard.length&#40;&#41;&#41;.getFinalResult&#40;&#41;
     *         .forEach&#40;recognizedBusinessCard -&gt; &#123;
     *             Map&lt;String, FormField&gt; recognizedFields = recognizedBusinessCard.getFields&#40;&#41;;
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
     *         &#125;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeBusinessCards#InputStream-long -->
     *
     * @param businessCard The data of the business card to recognize business card information from.
     * @param length The exact length of the data.
     *
     * @return A {@link SyncPoller} that polls the recognize business card operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code businessCard} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCards(
        InputStream businessCard, long length) {
        return beginRecognizeBusinessCards(businessCard, length, null, Context.NONE);
    }

    /**
     * Recognizes business card data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained business card model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/formrecognizer/businesscardfields">here</a> for fields found on a business card.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeBusinessCards#InputStream-long-Options-Context -->
     * <pre>
     * File businessCard = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
     * boolean includeFieldElements = true;
     * byte[] fileContent = Files.readAllBytes&#40;businessCard.toPath&#40;&#41;&#41;;
     * try &#40;InputStream targetStream = new ByteArrayInputStream&#40;fileContent&#41;&#41; &#123;
     *     for &#40;RecognizedForm recognizedForm : formRecognizerClient.beginRecognizeBusinessCards&#40;targetStream,
     *         businessCard.length&#40;&#41;,
     *         new RecognizeBusinessCardsOptions&#40;&#41;
     *             .setContentType&#40;FormContentType.IMAGE_JPEG&#41;
     *             .setFieldElementsIncluded&#40;includeFieldElements&#41;,
     *         Context.NONE&#41;.setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *         .getFinalResult&#40;&#41;&#41; &#123;
     *         Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
     *         FormField contactNamesFormField = recognizedFields.get&#40;&quot;ContactNames&quot;&#41;;
     *         if &#40;contactNamesFormField != null&#41; &#123;
     *             if &#40;FieldValueType.LIST == contactNamesFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 List&lt;FormField&gt; contactNamesList = contactNamesFormField.getValue&#40;&#41;.asList&#40;&#41;;
     *                 contactNamesList.stream&#40;&#41;
     *                     .filter&#40;contactName -&gt; FieldValueType.MAP == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
     *                     .map&#40;contactName -&gt; &#123;
     *                         System.out.printf&#40;&quot;Contact name: %s%n&quot;, contactName.getValueData&#40;&#41;.getText&#40;&#41;&#41;;
     *                         return contactName.getValue&#40;&#41;.asMap&#40;&#41;;
     *                     &#125;&#41;
     *                     .forEach&#40;contactNamesMap -&gt; contactNamesMap.forEach&#40;&#40;key, contactName&#41; -&gt; &#123;
     *                         if &#40;&quot;FirstName&quot;.equals&#40;key&#41;&#41; &#123;
     *                             if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                 String firstName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                 System.out.printf&#40;&quot;&#92;tFirst Name: %s, confidence: %.2f%n&quot;,
     *                                     firstName, contactName.getConfidence&#40;&#41;&#41;;
     *                             &#125;
     *                         &#125;
     *                         if &#40;&quot;LastName&quot;.equals&#40;key&#41;&#41; &#123;
     *                             if &#40;FieldValueType.STRING == contactName.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                                 String lastName = contactName.getValue&#40;&#41;.asString&#40;&#41;;
     *                                 System.out.printf&#40;&quot;&#92;tLast Name: %s, confidence: %.2f%n&quot;,
     *                                     lastName, contactName.getConfidence&#40;&#41;&#41;;
     *                             &#125;
     *                         &#125;
     *                     &#125;&#41;&#41;;
     *             &#125;
     *         &#125;
     *         FormField jobTitles = recognizedFields.get&#40;&quot;JobTitles&quot;&#41;;
     *         if &#40;jobTitles != null&#41; &#123;
     *             if &#40;FieldValueType.LIST == jobTitles.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 List&lt;FormField&gt; jobTitlesItems = jobTitles.getValue&#40;&#41;.asList&#40;&#41;;
     *                 jobTitlesItems.forEach&#40;jobTitlesItem -&gt; &#123;
     *                     if &#40;FieldValueType.STRING == jobTitlesItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                         String jobTitle = jobTitlesItem.getValue&#40;&#41;.asString&#40;&#41;;
     *                         System.out.printf&#40;&quot;Job Title: %s, confidence: %.2f%n&quot;,
     *                             jobTitle, jobTitlesItem.getConfidence&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeBusinessCards#InputStream-long-Options-Context -->
     *
     * @param businessCard The data of the business card to recognize business card information from.
     * @param length The exact length of the data.
     * @param recognizeBusinessCardsOptions The additional configurable {@link RecognizeBusinessCardsOptions options}
     * that may be passed when analyzing a business card.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the recognize business card operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code businessCard} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCards(
        InputStream businessCard, long length, RecognizeBusinessCardsOptions recognizeBusinessCardsOptions,
        Context context) {
        return beginRecognizeBusinessCardsInternal(businessCard, length, recognizeBusinessCardsOptions,
            context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeBusinessCardsInternal(InputStream businessCard, long length, RecognizeBusinessCardsOptions recognizeBusinessCardsOptions, Context context) {
        if (businessCard == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'businessCard' is required and cannot be null."));
        }

        RecognizeBusinessCardsOptions finalBusinessCardOptions = getRecognizeBusinessCardsOptions(recognizeBusinessCardsOptions);
        final boolean isFieldElementsIncluded = finalBusinessCardOptions.isFieldElementsIncluded();
        final FormRecognizerLocale localeInfo  = finalBusinessCardOptions.getLocale();

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            (cxt) -> {
                try {
                    ResponseBase<AnalyzersAnalyzeBusinessCardHeaders, Void> analyzeBusinessCardWithResponse = analyzersImpl.analyzeBusinessCardWithResponse(
                        finalBusinessCardOptions.getContentType() != null ? ContentType.fromString(finalBusinessCardOptions.getContentType().toString()) : null,
                        isFieldElementsIncluded,
                        Locale.fromString(Objects.toString(localeInfo, null)),
                        finalBusinessCardOptions.getPages(),
                        BinaryData.fromStream(businessCard),
                        length, context);
                    return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new FormRecognizerOperationResult(parseModelId(analyzeBusinessCardWithResponse.getDeserializedHeaders().getOperationLocation())));
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            },
            pollingOperation(resultId -> analyzersImpl.getAnalyzeBusinessCardResultWithResponse(resultId, context)),
            getCancellationIsNotSupported(),
            pollingContext -> {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                try {
                    return toRecognizedForm(analyzersImpl.getAnalyzeBusinessCardResultWithResponse(UUID.fromString(resultId), context).getValue().getAnalyzeResult(), isFieldElementsIncluded, null);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            });
    }

    /**
     * Recognizes invoice data from document using optical character recognition (OCR) and a prebuilt invoice trained
     * model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/formrecognizer/invoicefields">here</a> for fields found on an invoice.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeInvoicesFromUrl#string -->
     * <pre>
     * String invoiceUrl = &quot;invoice_url&quot;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerClient.beginRecognizeInvoicesFromUrl&#40;invoiceUrl&#41;
     *     .getFinalResult&#40;&#41;
     *     .stream&#40;&#41;
     *     .map&#40;RecognizedForm::getFields&#41;
     *     .forEach&#40;recognizedFields -&gt; &#123;
     *         FormField customAddrFormField = recognizedFields.get&#40;&quot;CustomerAddress&quot;&#41;;
     *         if &#40;customAddrFormField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == customAddrFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Customer Address: %s%n&quot;, customAddrFormField.getValue&#40;&#41;.asString&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *         FormField invoiceDateFormField = recognizedFields.get&#40;&quot;InvoiceDate&quot;&#41;;
     *         if &#40;invoiceDateFormField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == invoiceDateFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate invoiceDate = invoiceDateFormField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Invoice Date: %s, confidence: %.2f%n&quot;,
     *                     invoiceDate, invoiceDateFormField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeInvoicesFromUrl#string -->
     *
     * @param invoiceUrl The URL of the invoice document to analyze.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize invoice operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code invoiceUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoicesFromUrl(
        String invoiceUrl) {
        return beginRecognizeInvoicesFromUrl(invoiceUrl, null, Context.NONE);
    }

    /**
     * Recognizes invoice data from documents using optical character recognition (OCR) and a
     * prebuilt invoice trained model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeInvoicesFromUrl#string-Options-Context -->
     * <pre>
     * String invoiceUrl = &quot;invoice_url&quot;;
     * boolean includeFieldElements = true;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerClient.beginRecognizeInvoicesFromUrl&#40;invoiceUrl,
     *     new RecognizeInvoicesOptions&#40;&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;,
     *     Context.NONE&#41;.setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *     .getFinalResult&#40;&#41;
     *     .stream&#40;&#41;
     *     .map&#40;RecognizedForm::getFields&#41;
     *     .forEach&#40;recognizedFields -&gt; &#123;
     *         FormField customAddrFormField = recognizedFields.get&#40;&quot;CustomerAddress&quot;&#41;;
     *         if &#40;customAddrFormField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == customAddrFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Customer Address: %s%n&quot;, customAddrFormField.getValue&#40;&#41;.asString&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *         FormField invoiceDateFormField = recognizedFields.get&#40;&quot;InvoiceDate&quot;&#41;;
     *         if &#40;invoiceDateFormField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == invoiceDateFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate invoiceDate = invoiceDateFormField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Invoice Date: %s, confidence: %.2f%n&quot;,
     *                     invoiceDate, invoiceDateFormField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeInvoicesFromUrl#string-Options-Context -->
     *
     * @param invoiceUrl The source URL to the input invoice document.
     * @param recognizeInvoicesOptions The additional configurable {@link RecognizeInvoicesOptions options}
     * that may be passed when analyzing an invoice.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize invoice operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code invoiceUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoicesFromUrl(
        String invoiceUrl, RecognizeInvoicesOptions recognizeInvoicesOptions, Context context) {
        return beginRecognizeInvoicesFromUrlInternal(invoiceUrl, recognizeInvoicesOptions, context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoicesFromUrlInternal(String invoiceUrl, RecognizeInvoicesOptions recognizeInvoicesOptions, Context context) {
        if (invoiceUrl == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'invoiceUrl' is required and cannot be null."));
        }

        RecognizeInvoicesOptions finalRecognizeInvoicesOptions = getRecognizeInvoicesOptions(recognizeInvoicesOptions);
        final boolean isFieldElementsIncluded = finalRecognizeInvoicesOptions.isFieldElementsIncluded();
        final FormRecognizerLocale localeInfo  = finalRecognizeInvoicesOptions.getLocale();

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            (cxt) -> {
            try {
                ResponseBase<AnalyzersAnalyzeInvoiceHeaders, Void> analyzeInvoiceWithResponse = analyzersImpl.analyzeInvoiceWithResponse(
                    isFieldElementsIncluded,
                    Locale.fromString(Objects.toString(localeInfo, null)),
                    finalRecognizeInvoicesOptions.getPages(),
                    new SourcePath().setSource(invoiceUrl), context);
                return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new FormRecognizerOperationResult(parseModelId(analyzeInvoiceWithResponse.getDeserializedHeaders().getOperationLocation())));
            } catch (ErrorResponseException ex) {
                throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
            }},
            pollingOperation(resultId -> {
                try {
                    return analyzersImpl.getAnalyzeInvoiceResultWithResponse(resultId, context);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            }),
            getCancellationIsNotSupported(),
            pollingContext -> {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                try {
                    return toRecognizedForm(analyzersImpl.getAnalyzeInvoiceResultWithResponse(UUID.fromString(resultId), context).getValue().getAnalyzeResult(), true, null);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            });
    }

    /**
     * Recognizes data from the provided document data using optical character recognition (OCR)
     * and a prebuilt trained invoice model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/formrecognizer/invoicefields">here</a> for fields found on a invoice.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeInvoices#InputStream-long -->
     * <pre>
     * File invoice = new File&#40;&quot;local&#47;file_path&#47;invoice.jpg&quot;&#41;;
     * ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;Files.readAllBytes&#40;invoice.toPath&#40;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerClient.beginRecognizeInvoices&#40;inputStream, invoice.length&#40;&#41;&#41;
     *     .getFinalResult&#40;&#41;
     *     .stream&#40;&#41;
     *     .map&#40;RecognizedForm::getFields&#41;
     *     .forEach&#40;recognizedFields -&gt; &#123;
     *         FormField customAddrFormField = recognizedFields.get&#40;&quot;CustomerAddress&quot;&#41;;
     *         if &#40;customAddrFormField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == customAddrFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Customer Address: %s%n&quot;, customAddrFormField.getValue&#40;&#41;.asString&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *         FormField invoiceDateFormField = recognizedFields.get&#40;&quot;InvoiceDate&quot;&#41;;
     *         if &#40;invoiceDateFormField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == invoiceDateFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate invoiceDate = invoiceDateFormField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Invoice Date: %s, confidence: %.2f%n&quot;,
     *                     invoiceDate, invoiceDateFormField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeInvoices#InputStream-long -->
     *
     * @param invoice The data of the invoice to recognize invoice related information from.
     * @param length The exact length of the data.
     *
     * @return A {@link SyncPoller} that polls the recognize invoice operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code invoice} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoices(InputStream invoice,
        long length) {
        return beginRecognizeInvoices(invoice, length, null, Context.NONE);
    }

    /**
     * Recognizes data from the provided document data using optical character recognition (OCR) and a prebuilt
     * trained invoice model.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     * See <a href="https://aka.ms/formrecognizer/invoicefields">here</a> for fields found on a invoice.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeInvoices#InputStream-long-Options-Context -->
     * <pre>
     * File invoice = new File&#40;&quot;local&#47;file_path&#47;invoice.jpg&quot;&#41;;
     * boolean includeFieldElements = true;
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;Files.readAllBytes&#40;invoice.toPath&#40;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerClient.beginRecognizeInvoices&#40;inputStream,
     *     invoice.length&#40;&#41;,
     *     new RecognizeInvoicesOptions&#40;&#41;
     *         .setContentType&#40;FormContentType.IMAGE_JPEG&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;,
     *     Context.NONE&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *     .getFinalResult&#40;&#41;
     *     .stream&#40;&#41;
     *     .map&#40;RecognizedForm::getFields&#41;
     *     .forEach&#40;recognizedFields -&gt; &#123;
     *         FormField customAddrFormField = recognizedFields.get&#40;&quot;CustomerAddress&quot;&#41;;
     *         if &#40;customAddrFormField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == customAddrFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Customer Address: %s%n&quot;, customAddrFormField.getValue&#40;&#41;.asString&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *         FormField invoiceDateFormField = recognizedFields.get&#40;&quot;InvoiceDate&quot;&#41;;
     *         if &#40;invoiceDateFormField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == invoiceDateFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate invoiceDate = invoiceDateFormField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Invoice Date: %s, confidence: %.2f%n&quot;,
     *                     invoiceDate, invoiceDateFormField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeInvoices#InputStream-long-Options-Context -->
     *
     * @param invoice The data of the invoice to recognize invoice related information from.
     * @param length The exact length of the data.
     * @param recognizeInvoicesOptions The additional configurable {@link RecognizeInvoicesOptions options}
     * that may be passed when analyzing a invoice.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the recognize invoice operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code invoice} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoices(InputStream invoice,
        long length, RecognizeInvoicesOptions recognizeInvoicesOptions, Context context) {
        return beginRecognizeInvoicesInternal(invoice, length, recognizeInvoicesOptions, context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeInvoicesInternal(InputStream businessCard, long length, RecognizeInvoicesOptions recognizeInvoicesOptions, Context context) {
        if (businessCard == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'businessCard' is required and cannot be null."));
        }

        RecognizeInvoicesOptions finalInvoiceOptions = getRecognizeInvoicesOptions(recognizeInvoicesOptions);
        final boolean isFieldElementsIncluded = finalInvoiceOptions.isFieldElementsIncluded();
        final FormRecognizerLocale localeInfo  = finalInvoiceOptions.getLocale();

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            (cxt) -> {
                try {
                    ResponseBase<AnalyzersAnalyzeInvoiceHeaders, Void> analyzeInvoiceWithResponse = analyzersImpl.analyzeInvoiceWithResponse(
                        finalInvoiceOptions.getContentType() != null ? ContentType.fromString(finalInvoiceOptions.getContentType().toString()) : null,
                        isFieldElementsIncluded,
                        Locale.fromString(Objects.toString(localeInfo, null)),
                        finalInvoiceOptions.getPages(),
                        BinaryData.fromStream(businessCard),
                        length, context);
                    return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new FormRecognizerOperationResult(parseModelId(analyzeInvoiceWithResponse.getDeserializedHeaders().getOperationLocation())));
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            },
            pollingOperation(resultId -> {
                try {
                    return analyzersImpl.getAnalyzeInvoiceResultWithResponse(resultId, context);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }}),
            getCancellationIsNotSupported(),
            pollingContext -> {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                try {
                    return toRecognizedForm(analyzersImpl.getAnalyzeInvoiceResultWithResponse(UUID.fromString(resultId), context).getValue().getAnalyzeResult(), isFieldElementsIncluded, null);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            });
    }

    /**
     * Analyze identity documents using optical character recognition (OCR) and a prebuilt model trained on identity
     * documents model to extract key information from passports and US driver licenses.
     * See <a href="https://aka.ms/formrecognizer/iddocumentfields">here</a> for fields found on an identity document.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeIdentityDocumentsFromUrl#string -->
     * <pre>
     * String licenseDocumentUrl = &quot;licenseDocumentUrl&quot;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerClient.beginRecognizeIdentityDocumentsFromUrl&#40;licenseDocumentUrl&#41;
     *     .getFinalResult&#40;&#41;
     *     .stream&#40;&#41;
     *     .map&#40;RecognizedForm::getFields&#41;
     *     .forEach&#40;recognizedFields -&gt; &#123;
     *         FormField firstNameField = recognizedFields.get&#40;&quot;FirstName&quot;&#41;;
     *         if &#40;firstNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == firstNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String firstName = firstNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;First Name: %s, confidence: %.2f%n&quot;,
     *                     firstName, firstNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField lastNameField = recognizedFields.get&#40;&quot;LastName&quot;&#41;;
     *         if &#40;lastNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == lastNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String lastName = lastNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Last name: %s, confidence: %.2f%n&quot;,
     *                     lastName, lastNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField countryRegionFormField = recognizedFields.get&#40;&quot;CountryRegion&quot;&#41;;
     *         if &#40;countryRegionFormField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == countryRegionFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String countryRegion = countryRegionFormField.getValue&#40;&#41;.asCountryRegion&#40;&#41;;
     *                 System.out.printf&#40;&quot;Country or region: %s, confidence: %.2f%n&quot;,
     *                     countryRegion, countryRegionFormField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField dateOfBirthField = recognizedFields.get&#40;&quot;DateOfBirth&quot;&#41;;
     *         if &#40;dateOfBirthField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == dateOfBirthField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate dateOfBirth = dateOfBirthField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Date of Birth: %s, confidence: %.2f%n&quot;,
     *                     dateOfBirth, dateOfBirthField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField dateOfExpirationField = recognizedFields.get&#40;&quot;DateOfExpiration&quot;&#41;;
     *         if &#40;dateOfExpirationField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == dateOfExpirationField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate expirationDate = dateOfExpirationField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Document date of expiration: %s, confidence: %.2f%n&quot;,
     *                     expirationDate, dateOfExpirationField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField documentNumberField = recognizedFields.get&#40;&quot;DocumentNumber&quot;&#41;;
     *         if &#40;documentNumberField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == documentNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String documentNumber = documentNumberField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Document number: %s, confidence: %.2f%n&quot;,
     *                     documentNumber, documentNumberField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeIdentityDocumentsFromUrl#string -->
     *
     * @param identityDocumentUrl The source URL to the input identity document.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize identity document operation until it has
     * completed, has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code identityDocumentUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocumentsFromUrl(
        String identityDocumentUrl) {
        return beginRecognizeIdentityDocumentsFromUrl(identityDocumentUrl, null, Context.NONE);
    }

    /**
     * Analyze identity documents using optical character recognition (OCR) and a prebuilt model trained on identity
     * documents model to extract key information from passports and US driver licenses.
     * See <a href="https://aka.ms/formrecognizer/iddocumentfields">here</a> for fields found on an identity document.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeIdentityDocumentsFromUrl#string-Options-Context -->
     * <pre>
     * String licenseDocumentUrl = &quot;licenseDocumentUrl&quot;;
     * boolean includeFieldElements = true;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerClient.beginRecognizeIdentityDocumentsFromUrl&#40;licenseDocumentUrl,
     *     new RecognizeIdentityDocumentOptions&#40;&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;,
     *     Context.NONE&#41;.setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *     .getFinalResult&#40;&#41;
     *     .stream&#40;&#41;
     *     .map&#40;RecognizedForm::getFields&#41;
     *     .forEach&#40;recognizedFields -&gt; &#123;
     *         FormField firstNameField = recognizedFields.get&#40;&quot;FirstName&quot;&#41;;
     *         if &#40;firstNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == firstNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String firstName = firstNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;First Name: %s, confidence: %.2f%n&quot;,
     *                     firstName, firstNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField lastNameField = recognizedFields.get&#40;&quot;LastName&quot;&#41;;
     *         if &#40;lastNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == lastNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String lastName = lastNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Last name: %s, confidence: %.2f%n&quot;,
     *                     lastName, lastNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField countryRegionFormField = recognizedFields.get&#40;&quot;CountryRegion&quot;&#41;;
     *         if &#40;countryRegionFormField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == countryRegionFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String countryRegion = countryRegionFormField.getValue&#40;&#41;.asCountryRegion&#40;&#41;;
     *                 System.out.printf&#40;&quot;Country or region: %s, confidence: %.2f%n&quot;,
     *                     countryRegion, countryRegionFormField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField dateOfExpirationField = recognizedFields.get&#40;&quot;DateOfExpiration&quot;&#41;;
     *         if &#40;dateOfExpirationField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == dateOfExpirationField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate expirationDate = dateOfExpirationField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Document date of expiration: %s, confidence: %.2f%n&quot;,
     *                     expirationDate, dateOfExpirationField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField documentNumberField = recognizedFields.get&#40;&quot;DocumentNumber&quot;&#41;;
     *         if &#40;documentNumberField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == documentNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String documentNumber = documentNumberField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Document number: %s, confidence: %.2f%n&quot;,
     *                     documentNumber, documentNumberField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeIdentityDocumentsFromUrl#string-Options-Context -->
     *
     * @param identityDocumentUrl The source URL to the input identity Document.
     * @param recognizeIdentityDocumentOptions The additional configurable
     * {@link RecognizeIdentityDocumentOptions options} that may be passed when analyzing an identity document.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} to poll the progress of the recognize identity document operation until it has
     * completed, has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code identityDocumentUrl} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocumentsFromUrl(
        String identityDocumentUrl, RecognizeIdentityDocumentOptions recognizeIdentityDocumentOptions,
        Context context) {
        return beginRecognizeIdentityDocumentsFromUrlInternal(identityDocumentUrl, recognizeIdentityDocumentOptions,
            context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocumentsFromUrlInternal(String identityDocumentUrl, RecognizeIdentityDocumentOptions recognizeIdentityDocumentOptions, Context context) {
        if (identityDocumentUrl == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'identityDocumentUrl' is required and cannot be null."));
        }

        RecognizeIdentityDocumentOptions finalRecognizeInvoicesOptions = getRecognizeIdentityDocumentOptions(recognizeIdentityDocumentOptions);
        final boolean isFieldElementsIncluded = finalRecognizeInvoicesOptions.isFieldElementsIncluded();

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            (cxt) -> {
                ResponseBase<AnalyzersAnalyzeIdDocumentHeaders, Void> analyzeInvoiceWithResponse = analyzersImpl.analyzeIdDocumentWithResponse(
                    isFieldElementsIncluded,
                    finalRecognizeInvoicesOptions.getPages(),
                    new SourcePath().setSource(identityDocumentUrl), context);
                return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new FormRecognizerOperationResult(parseModelId(analyzeInvoiceWithResponse.getDeserializedHeaders().getOperationLocation())));
            },
            pollingOperation(resultId -> analyzersImpl.getAnalyzeIdDocumentResultWithResponse(resultId, context)),
            getCancellationIsNotSupported(),
            pollingContext -> {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                try {
                    return toRecognizedForm(analyzersImpl.getAnalyzeIdDocumentResultWithResponse(UUID.fromString(resultId), context).getValue().getAnalyzeResult(), true, null);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            });
    }

    /**
     * Analyze identity documents using optical character recognition (OCR) and a prebuilt model trained on identity
     * documents model to extract key information from passports and US driver licenses.
     * See <a href="https://aka.ms/formrecognizer/iddocumentfields">here</a> for fields found on an identity document.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeIdentityDocuments#InputStream-long -->
     * <pre>
     * File license = new File&#40;&quot;local&#47;file_path&#47;license.jpg&quot;&#41;;
     * ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;Files.readAllBytes&#40;license.toPath&#40;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerClient.beginRecognizeIdentityDocuments&#40;inputStream, license.length&#40;&#41;&#41;
     *     .getFinalResult&#40;&#41;
     *     .stream&#40;&#41;
     *     .map&#40;RecognizedForm::getFields&#41;
     *     .forEach&#40;recognizedFields -&gt; &#123;
     *         FormField firstNameField = recognizedFields.get&#40;&quot;FirstName&quot;&#41;;
     *         if &#40;firstNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == firstNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String firstName = firstNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;First Name: %s, confidence: %.2f%n&quot;,
     *                     firstName, firstNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField lastNameField = recognizedFields.get&#40;&quot;LastName&quot;&#41;;
     *         if &#40;lastNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == lastNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String lastName = lastNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Last name: %s, confidence: %.2f%n&quot;,
     *                     lastName, lastNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField countryRegionFormField = recognizedFields.get&#40;&quot;CountryRegion&quot;&#41;;
     *         if &#40;countryRegionFormField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == countryRegionFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String countryRegion = countryRegionFormField.getValue&#40;&#41;.asCountryRegion&#40;&#41;;
     *                 System.out.printf&#40;&quot;Country or region: %s, confidence: %.2f%n&quot;,
     *                     countryRegion, countryRegionFormField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField dateOfExpirationField = recognizedFields.get&#40;&quot;DateOfExpiration&quot;&#41;;
     *         if &#40;dateOfExpirationField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == dateOfExpirationField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate expirationDate = dateOfExpirationField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Document date of expiration: %s, confidence: %.2f%n&quot;,
     *                     expirationDate, dateOfExpirationField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField documentNumberField = recognizedFields.get&#40;&quot;DocumentNumber&quot;&#41;;
     *         if &#40;documentNumberField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == documentNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String documentNumber = documentNumberField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Document number: %s, confidence: %.2f%n&quot;,
     *                     documentNumber, documentNumberField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeIdentityDocuments#InputStream-long -->
     *
     * @param identityDocument The data of the identity document to recognize identity document information from.
     * @param length The exact length of the data.
     *
     * @return A {@link SyncPoller} that polls the recognize identity Document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code identityDocument} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocuments(
        InputStream identityDocument, long length) {
        return beginRecognizeIdentityDocuments(identityDocument, length, null, Context.NONE);
    }

    /**
     * Analyze identity documents using optical character recognition (OCR) and a prebuilt model trained on identity
     * documents model to extract key information from passports and US driver licenses.
     * See <a href="https://aka.ms/formrecognizer/iddocumentfields">here</a> for fields found on an identity document.
     * <p>The service does not support cancellation of the long running operation and returns with an
     * error message indicating absence of cancellation support</p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeIdentityDocuments#InputStream-long-Options-Context -->
     * <pre>
     * File licenseDocument = new File&#40;&quot;local&#47;file_path&#47;license.jpg&quot;&#41;;
     * boolean includeFieldElements = true;
     * &#47;&#47; Utility method to convert input stream to Byte buffer
     * ByteArrayInputStream inputStream = new ByteArrayInputStream&#40;Files.readAllBytes&#40;licenseDocument.toPath&#40;&#41;&#41;&#41;;
     * &#47;&#47; if training polling operation completed, retrieve the final result.
     * formRecognizerClient.beginRecognizeIdentityDocuments&#40;inputStream,
     *     licenseDocument.length&#40;&#41;,
     *     new RecognizeIdentityDocumentOptions&#40;&#41;
     *         .setContentType&#40;FormContentType.IMAGE_JPEG&#41;
     *         .setFieldElementsIncluded&#40;includeFieldElements&#41;,
     *     Context.NONE&#41;
     *     .setPollInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *     .getFinalResult&#40;&#41;
     *     .stream&#40;&#41;
     *     .map&#40;RecognizedForm::getFields&#41;
     *     .forEach&#40;recognizedFields -&gt; &#123;
     *         FormField firstNameField = recognizedFields.get&#40;&quot;FirstName&quot;&#41;;
     *         if &#40;firstNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == firstNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String firstName = firstNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;First Name: %s, confidence: %.2f%n&quot;,
     *                     firstName, firstNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField lastNameField = recognizedFields.get&#40;&quot;LastName&quot;&#41;;
     *         if &#40;lastNameField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == lastNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String lastName = lastNameField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Last name: %s, confidence: %.2f%n&quot;,
     *                     lastName, lastNameField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField countryRegionFormField = recognizedFields.get&#40;&quot;CountryRegion&quot;&#41;;
     *         if &#40;countryRegionFormField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == countryRegionFormField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String countryRegion = countryRegionFormField.getValue&#40;&#41;.asCountryRegion&#40;&#41;;
     *                 System.out.printf&#40;&quot;Country or region: %s, confidence: %.2f%n&quot;,
     *                     countryRegion, countryRegionFormField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField dateOfBirthField = recognizedFields.get&#40;&quot;DateOfBirth&quot;&#41;;
     *         if &#40;dateOfBirthField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == dateOfBirthField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate dateOfBirth = dateOfBirthField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Date of Birth: %s, confidence: %.2f%n&quot;,
     *                     dateOfBirth, dateOfBirthField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField dateOfExpirationField = recognizedFields.get&#40;&quot;DateOfExpiration&quot;&#41;;
     *         if &#40;dateOfExpirationField != null&#41; &#123;
     *             if &#40;FieldValueType.DATE == dateOfExpirationField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 LocalDate expirationDate = dateOfExpirationField.getValue&#40;&#41;.asDate&#40;&#41;;
     *                 System.out.printf&#40;&quot;Document date of expiration: %s, confidence: %.2f%n&quot;,
     *                     expirationDate, dateOfExpirationField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *
     *         FormField documentNumberField = recognizedFields.get&#40;&quot;DocumentNumber&quot;&#41;;
     *         if &#40;documentNumberField != null&#41; &#123;
     *             if &#40;FieldValueType.STRING == documentNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
     *                 String documentNumber = documentNumberField.getValue&#40;&#41;.asString&#40;&#41;;
     *                 System.out.printf&#40;&quot;Document number: %s, confidence: %.2f%n&quot;,
     *                     documentNumber, documentNumberField.getConfidence&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.formrecognizer.v3.FormRecognizerClient.beginRecognizeIdentityDocuments#InputStream-long-Options-Context -->
     *
     * @param identityDocument                 The data of the identity document to recognize information from.
     * @param length                           The exact length of the data.
     * @param recognizeIdentityDocumentOptions The additional configurable
     *                                         {@link RecognizeIdentityDocumentOptions options} that may be passed when analyzing an identity document.
     * @param context                          Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link SyncPoller} that polls the recognize identity document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     *                                 an {@link OperationStatus#FAILED}.
     * @throws NullPointerException    If {@code identityDocument} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocuments(
        InputStream identityDocument, long length, RecognizeIdentityDocumentOptions recognizeIdentityDocumentOptions,
        Context context) {
        return beginRecognizeIdentityDocumentsInternal(identityDocument, length,
            recognizeIdentityDocumentOptions, context);
    }

    private SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocumentsInternal(InputStream identityDocument, long length, RecognizeIdentityDocumentOptions recognizeIdentityDocumentOptions, Context context) {
        if (identityDocument == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'identityDocument' is required and cannot be null."));
        }

        RecognizeIdentityDocumentOptions finalRecognizeIdentityDocumentOptions = getRecognizeIdentityDocumentOptions(recognizeIdentityDocumentOptions);
        final boolean isFieldElementsIncluded = finalRecognizeIdentityDocumentOptions.isFieldElementsIncluded();

        return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
            (cxt) -> {
                try {
                    ResponseBase<AnalyzersAnalyzeIdDocumentHeaders, Void> analyzeIdDocumentWithResponse = analyzersImpl.analyzeIdDocumentWithResponse(
                        finalRecognizeIdentityDocumentOptions.getContentType() != null ? ContentType.fromString(finalRecognizeIdentityDocumentOptions.getContentType().toString()) : null,
                        isFieldElementsIncluded,
                        finalRecognizeIdentityDocumentOptions.getPages(),
                        BinaryData.fromStream(identityDocument),
                        length, context);
                    return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, new FormRecognizerOperationResult(parseModelId(analyzeIdDocumentWithResponse.getDeserializedHeaders().getOperationLocation())));
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            },
            pollingOperation(resultId ->  {
                try {
                   return analyzersImpl.getAnalyzeIdDocumentResultWithResponse(resultId, context);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            }),
            getCancellationIsNotSupported(),
            pollingContext -> {
                final String resultId = pollingContext.getLatestResponse().getValue().getResultId();
                try {
                    return toRecognizedForm(analyzersImpl.getAnalyzeIdDocumentResultWithResponse(UUID.fromString(resultId), context).getValue().getAnalyzeResult(), isFieldElementsIncluded, null);
                } catch (ErrorResponseException ex) {
                    throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
                }
            });
    }

    /*
     * Poller's POLLING operation.
     */
    private Function<PollingContext<FormRecognizerOperationResult>, PollResponse<FormRecognizerOperationResult>>
    pollingOperation(Function<UUID, Response<AnalyzeOperationResult>> pollingFunction) {
        return pollingContext -> {
            final PollResponse<FormRecognizerOperationResult> operationResultPollResponse =
                pollingContext.getLatestResponse();
            final UUID resultUuid = UUID.fromString(operationResultPollResponse.getValue().getResultId());
            Response<AnalyzeOperationResult> p = pollingFunction.apply(resultUuid);
            return processAnalyzeModelResponse(p, operationResultPollResponse);
        };
    }

    private Function<PollingContext<FormRecognizerOperationResult>, List<RecognizedForm>>
    fetchingOperation(Function<UUID, Response<AnalyzeOperationResult>> fetchingFunction,
        boolean isFieldElementsIncluded, String modelId) {
        return pollingContext -> {
            try {
                final UUID resultUuid = UUID.fromString(pollingContext.getLatestResponse().getValue().getResultId());
                Response<AnalyzeOperationResult> modelSimpleResponse = fetchingFunction.apply(resultUuid);
                return toRecognizedForm(modelSimpleResponse.getValue().getAnalyzeResult(), isFieldElementsIncluded, modelId);
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError(ex);
            }
        };
    }

    private PollResponse<FormRecognizerOperationResult> processAnalyzeModelResponse(
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
                throw LOGGER.logExceptionAsError(new FormRecognizerException("Analyze operation failed",
                    analyzeOperationResultResponse.getValue().getAnalyzeResult().getErrors().stream()
                        .map(errorInformation -> new FormRecognizerErrorInformation(errorInformation.getCode(),
                            errorInformation.getMessage()))
                        .collect(Collectors.toList())));
            default:
                status = LongRunningOperationStatus.fromString(
                    analyzeOperationResultResponse.getValue().getStatus().toString(), true);
                break;
        }
        return new PollResponse<>(status, operationResultPollResponse.getValue());
    }

    private BiFunction<PollingContext<FormRecognizerOperationResult>, PollResponse<FormRecognizerOperationResult>, FormRecognizerOperationResult>
    getCancellationIsNotSupported() {
        return (pollingContext, activationResponse) -> {
            throw LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported"));
        };
    }

    private Function<PollingContext<FormRecognizerOperationResult>, FormRecognizerOperationResult> analyzeActivationOperation(UUID modelId, String formUrl,
                                                                                                                              FormContentType contentType, BinaryData form, long length, boolean isFieldElementsIncluded,
                                                                                                                              RecognizeCustomFormsOptions finalRecognizeCustomFormsOptions, Context context) {
        return (pollingContext) ->
            new FormRecognizerOperationResult(parseModelId(analyzeDocument(modelId, formUrl, contentType, form, length, isFieldElementsIncluded,
            finalRecognizeCustomFormsOptions, context)
                .getDeserializedHeaders().getOperationLocation()));
    }

    private ResponseBase<CustomModelsAnalyzeDocumentHeaders, Void> analyzeDocument(UUID modelId, String formUrl,
        FormContentType contentType, BinaryData form, long length, boolean isFieldElementsIncluded,
        RecognizeCustomFormsOptions finalRecognizeCustomFormsOptions, Context context) {
        try {
            if (formUrl != null) {
                return customModelsImpl.analyzeDocumentWithResponse(modelId, isFieldElementsIncluded,
                    finalRecognizeCustomFormsOptions.getPages(), new SourcePath().setSource(formUrl), context);
            } else {
                return customModelsImpl.analyzeDocumentWithResponse(modelId,
                    contentType != null ? ContentType.fromString(contentType.toString()) : null,
                    isFieldElementsIncluded,
                    finalRecognizeCustomFormsOptions.getPages(), form, length, context);
            }
        } catch (ErrorResponseException ex) {
            throw LOGGER.logExceptionAsError(Utility.getHttpResponseException(ex));
        }
    }
}
