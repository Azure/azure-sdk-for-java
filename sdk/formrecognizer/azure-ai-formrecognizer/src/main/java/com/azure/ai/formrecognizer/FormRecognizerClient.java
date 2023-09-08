// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.OperationStatus;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormRecognizerException;
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
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;

/**
 * <p>This class provides an synchronous client to connect to the Form Recognizer Azure Cognitive Service.</p>
 * <p>This client provides synchronous methods to perform:</p>
 *
 * <ol>
 *   <li>Custom Form Analysis: Extraction and analysis of data from forms and documents specific to distinct business
 *   data and use cases. Use the custom trained model by passing its modelId into the
 *   {@link com.azure.ai.formrecognizer.FormRecognizerClient#beginRecognizeCustomForms(String, InputStream, long)
 *   beginRecognizeCustomForms}
 *   method.</li>
 *   <li>Prebuilt Model Analysis: Analyze receipts, business cards, invoices and other documents with
 *   <a href="https://aka.ms/form-recognizer-service-2.1.0">supported prebuilt models</a>
 *   Use the
 *   {@link com.azure.ai.formrecognizer.FormRecognizerClient#beginRecognizeReceipts(InputStream, long, RecognizeReceiptsOptions, Context) beginRecognizeReceipts}
 *   method to recognize receipt information.</li>
 *   <li>Layout Analysis: Extraction and analysis of text, selection marks, tables, and bounding box coordinates,
 *   from forms and documents. Use
 *   {@link com.azure.ai.formrecognizer.FormRecognizerClient#beginRecognizeContent(InputStream, long) beginRecognizeContent}
 *   method tpo perform layout analysis.</li>
 *   <li>Polling and Callbacks: It includes mechanisms for polling the service to check the status of an analysis
 *   operation or registering callbacks to receive notifications when the analysis is complete.</li>
 * </ol>
 *
 * <p><strong>Refer to the
 * <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/migration-guide.md">Migration guide</a> to use API versions 2022-08-31 and up.</strong></p>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Form Recognizer.
 * {@link com.azure.ai.formrecognizer.FormRecognizerClient} is the synchronous service client and
 * {@link com.azure.ai.formrecognizer.FormRecognizerAsyncClient} is the asynchronous service client.  The examples
 * shown in this document use a credential object named DefaultAzureCredential for authentication, which is appropriate
 * for most scenarios, including local development and production environments. Additionally, we recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a FormRecognizerClient with DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.ai.formrecognizer.FormRecognizerClient}, using
 * the `DefaultAzureCredentialBuilder` to configure it.</p>
 *
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
 *
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
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeCustomFormsFromUrl(String modelId, String formUrl,
        RecognizeCustomFormsOptions recognizeCustomFormsOptions, Context context) {
        return client.beginRecognizeCustomFormsFromUrl(formUrl, modelId,
            recognizeCustomFormsOptions, context).getSyncPoller();
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
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(String modelId, InputStream form, long length) {
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
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeCustomForms(String modelId, InputStream form, long length,
        RecognizeCustomFormsOptions recognizeCustomFormsOptions, Context context) {
        Flux<ByteBuffer> buffer = toFluxByteBuffer(form);
        return client.beginRecognizeCustomForms(modelId, buffer, length,
            recognizeCustomFormsOptions, context).getSyncPoller();
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
    public SyncPoller<FormRecognizerOperationResult, List<FormPage>>
        beginRecognizeContentFromUrl(String formUrl,
        RecognizeContentOptions recognizeContentOptions, Context context) {
        return client.beginRecognizeContentFromUrl(formUrl, recognizeContentOptions, context).getSyncPoller();
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
    public SyncPoller<FormRecognizerOperationResult, List<FormPage>>
        beginRecognizeContent(InputStream form, long length) {
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
        Flux<ByteBuffer> buffer = toFluxByteBuffer(form);
        return client.beginRecognizeContent(buffer, length, recognizeContentOptions, context).getSyncPoller();
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
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl) {
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
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeReceiptsFromUrl(String receiptUrl,
        RecognizeReceiptsOptions recognizeReceiptsOptions, Context context) {
        return client.beginRecognizeReceiptsFromUrl(receiptUrl, recognizeReceiptsOptions, context).getSyncPoller();
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
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeReceipts(InputStream receipt, long length) {
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
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeReceipts(InputStream receipt, long length,
        RecognizeReceiptsOptions recognizeReceiptsOptions, Context context) {
        Flux<ByteBuffer> buffer = toFluxByteBuffer(receipt);
        return client.beginRecognizeReceipts(buffer, length, recognizeReceiptsOptions, context).getSyncPoller();
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
        return client.beginRecognizeBusinessCardsFromUrl(businessCardUrl, recognizeBusinessCardsOptions, context)
                   .getSyncPoller();
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
        return client.beginRecognizeBusinessCards(toFluxByteBuffer(businessCard), length,
            recognizeBusinessCardsOptions, context).getSyncPoller();
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
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeInvoicesFromUrl(String invoiceUrl) {
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
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeInvoicesFromUrl(String invoiceUrl,
        RecognizeInvoicesOptions recognizeInvoicesOptions, Context context) {
        return client.beginRecognizeInvoicesFromUrl(invoiceUrl, recognizeInvoicesOptions, context).getSyncPoller();
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
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>>
        beginRecognizeInvoices(InputStream invoice, long length) {
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
        Flux<ByteBuffer> buffer = toFluxByteBuffer(invoice);
        return client.beginRecognizeInvoices(buffer, length, recognizeInvoicesOptions, context).getSyncPoller();
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
        return client.beginRecognizeIdentityDocumentsFromUrl(identityDocumentUrl, recognizeIdentityDocumentOptions,
            context).getSyncPoller();
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
     * @param identityDocument The data of the identity document to recognize information from.
     * @param length The exact length of the data.
     * @param recognizeIdentityDocumentOptions The additional configurable
     * {@link RecognizeIdentityDocumentOptions options} that may be passed when analyzing an identity document.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the recognize identity document operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a list of {@link RecognizedForm}.
     * @throws FormRecognizerException If recognize operation fails and the {@link AnalyzeOperationResult} returned with
     * an {@link OperationStatus#FAILED}.
     * @throws NullPointerException If {@code identityDocument} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> beginRecognizeIdentityDocuments(
        InputStream identityDocument, long length, RecognizeIdentityDocumentOptions recognizeIdentityDocumentOptions,
        Context context) {
        return client.beginRecognizeIdentityDocuments(toFluxByteBuffer(identityDocument), length,
                recognizeIdentityDocumentOptions, context).getSyncPoller();
    }
}
