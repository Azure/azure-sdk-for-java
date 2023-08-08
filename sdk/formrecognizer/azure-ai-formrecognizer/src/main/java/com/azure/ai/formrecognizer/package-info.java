// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://aka.ms/form-recognizer-3.0.0">Azure Form Recognizer</a>
 * is a cloud-based service provided by Microsoft Azure that utilizes machine learning to extract information
 * from various types of forms. It is designed to automate the process of
 * form recognition, data extraction, and form understanding. Azure Form Recognizer can handle structured
 * forms, such as invoices, receipts, and surveys, as well as unstructured form data, such as contracts,
 * agreements, and financial reports.</p>
 *
 * <p>The service uses advanced optical character recognition (OCR) technology to extract text and key-value
 * pairs from custom forms, enabling organizations to automate data entry tasks that would otherwise require
 * manual effort. It can recognize and extract information like dates, addresses, invoice numbers, line items,
 * and other relevant data points from forms. </p>
 *
 * <p> The Azure Form Recognizer client library allows Java developers to interact with the Azure Form
 * Recognizer service.
 * It provides a set of classes and methods that abstract the underlying RESTful API of Azure
 * Form Recognizer, making it easier to integrate the service into Java applications.</p>
 *
 * <p>The Azure Form Recognizer client library provides the following capabilities:</p>
 *
 * <ol>
 *     <li>Form recognizing: It allows you to submit forms to extract information like text, key-value pairs, tables, and
 *     form fields. You can analyze both structured and unstructured documents.</li>
 *     <li>Model Management: It enables you to train custom models by providing labeled training data. You can also
 *     list and delete existing models.</li>
 *     <li>Recognize Results: It provides methods to retrieve and interpret analysis results, including extracted text
 *     and field values, confidence scores, and form layout information.</li>
 *     <li>Polling and Callbacks: It includes mechanisms for polling the service to check the status of an analysis
 *     operation or registering callbacks to receive notifications when the analysis is complete.</li>
 * </ol>
 *
 * <h2>Getting Started</h2>
 *
 * <p>The Azure Form Recognizer library provides
 * analysis clients like {@link com.azure.ai.formrecognizer.FormRecognizerAsyncClient}
 * and {@link com.azure.ai.formrecognizer.FormRecognizerClient} to connect to the Form Recognizer Azure Cognitive
 * Service
 * to analyze information from forms and extract it into structured data.
 * It also provides training clients like {@link com.azure.ai.formrecognizer.training.FormTrainingClient}
 * and {@link com.azure.ai.formrecognizer.training.FormTrainingAsyncClient} to build and manage models from custom
 * forms.
 *
 * <p><strong>Note:</strong>This client only supports
 * {@link com.azure.ai.formrecognizer.FormRecognizerServiceVersion#V2_1} and lower.
 * Recommended to use a newer service version,
 * {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient} and
 * {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient}.</p>
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
 * <p>Let's take a look at the analysis client scenarios and their respective usage below.</p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Analyzing forms with prebuilt models</h2>
 *
 * <p><a href="https://learn.microsoft.com/azure/applied-ai-services/form-recognizer/concept-model-overview?view=form-recog-3.0.0#model-overview">Form Recognizer models</a>
 * and their associated output to help you choose the best model to address your document scenario needs.</p>
 *
 * <p>You can use domain specific models or train a custom model tailored to your specific business needs and use cases.
 *
 * <p><strong>Sample: Recognize data from receipts using a url source</strong></p>
 *
 * <p>The following code sample demonstrates how to detect and extract data from receipts using optical character
 * recognition (OCR).</p>
 *
 * <!-- src_embed readme-sample-recognize-receipt-url -->
 * <pre>
 * String receiptUrl = &quot;https:&#47;&#47;raw.githubusercontent.com&#47;Azure&#47;azure-sdk-for-java&#47;main&#47;sdk&#47;formrecognizer&quot;
 *         + &quot;&#47;azure-ai-formrecognizer&#47;src&#47;samples&#47;resources&#47;sample-forms&#47;receipts&#47;contoso-allinone.jpg&quot;;
 * SyncPoller&lt;FormRecognizerOperationResult, List&lt;RecognizedForm&gt;&gt; syncPoller =
 *     formRecognizerClient.beginRecognizeReceiptsFromUrl&#40;receiptUrl&#41;;
 * List&lt;RecognizedForm&gt; receiptPageResults = syncPoller.getFinalResult&#40;&#41;;
 *
 * for &#40;int i = 0; i &lt; receiptPageResults.size&#40;&#41;; i++&#41; &#123;
 *     RecognizedForm recognizedForm = receiptPageResults.get&#40;i&#41;;
 *     Map&lt;String, FormField&gt; recognizedFields = recognizedForm.getFields&#40;&#41;;
 *     System.out.printf&#40;&quot;----------- Recognizing receipt info for page %d -----------%n&quot;, i&#41;;
 *     FormField merchantNameField = recognizedFields.get&#40;&quot;MerchantName&quot;&#41;;
 *     if &#40;merchantNameField != null&#41; &#123;
 *         if &#40;FieldValueType.STRING == merchantNameField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
 *             String merchantName = merchantNameField.getValue&#40;&#41;.asString&#40;&#41;;
 *             System.out.printf&#40;&quot;Merchant Name: %s, confidence: %.2f%n&quot;,
 *                 merchantName, merchantNameField.getConfidence&#40;&#41;&#41;;
 *         &#125;
 *     &#125;
 *
 *     FormField merchantPhoneNumberField = recognizedFields.get&#40;&quot;MerchantPhoneNumber&quot;&#41;;
 *     if &#40;merchantPhoneNumberField != null&#41; &#123;
 *         if &#40;FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
 *             String merchantAddress = merchantPhoneNumberField.getValue&#40;&#41;.asPhoneNumber&#40;&#41;;
 *             System.out.printf&#40;&quot;Merchant Phone number: %s, confidence: %.2f%n&quot;,
 *                 merchantAddress, merchantPhoneNumberField.getConfidence&#40;&#41;&#41;;
 *         &#125;
 *     &#125;
 *
 *     FormField transactionDateField = recognizedFields.get&#40;&quot;TransactionDate&quot;&#41;;
 *     if &#40;transactionDateField != null&#41; &#123;
 *         if &#40;FieldValueType.DATE == transactionDateField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
 *             LocalDate transactionDate = transactionDateField.getValue&#40;&#41;.asDate&#40;&#41;;
 *             System.out.printf&#40;&quot;Transaction Date: %s, confidence: %.2f%n&quot;,
 *                 transactionDate, transactionDateField.getConfidence&#40;&#41;&#41;;
 *         &#125;
 *     &#125;
 *
 *     FormField receiptItemsField = recognizedFields.get&#40;&quot;Items&quot;&#41;;
 *     if &#40;receiptItemsField != null&#41; &#123;
 *         System.out.printf&#40;&quot;Receipt Items: %n&quot;&#41;;
 *         if &#40;FieldValueType.LIST == receiptItemsField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
 *             List&lt;FormField&gt; receiptItems = receiptItemsField.getValue&#40;&#41;.asList&#40;&#41;;
 *             receiptItems.stream&#40;&#41;
 *                 .filter&#40;receiptItem -&gt; FieldValueType.MAP == receiptItem.getValue&#40;&#41;.getValueType&#40;&#41;&#41;
 *                 .map&#40;formField -&gt; formField.getValue&#40;&#41;.asMap&#40;&#41;&#41;
 *                 .forEach&#40;formFieldMap -&gt; formFieldMap.forEach&#40;&#40;key, formField&#41; -&gt; &#123;
 *                     if &#40;&quot;Quantity&quot;.equals&#40;key&#41;&#41; &#123;
 *                         if &#40;FieldValueType.FLOAT == formField.getValue&#40;&#41;.getValueType&#40;&#41;&#41; &#123;
 *                             Float quantity = formField.getValue&#40;&#41;.asFloat&#40;&#41;;
 *                             System.out.printf&#40;&quot;Quantity: %f, confidence: %.2f%n&quot;,
 *                                 quantity, formField.getConfidence&#40;&#41;&#41;;
 *                         &#125;
 *                     &#125;
 *                 &#125;&#41;&#41;;
 *         &#125;
 *     &#125;
 * &#125;
 * </pre>
 * <!-- end readme-sample-recognize-receipt-url -->
 *
 * <p>
 * You can also extract data from a local receipt with prebuilt models using the
 * {@link com.azure.ai.formrecognizer.FormRecognizerClient#beginRecognizeReceipts(java.io.InputStream, long, com.azure.ai.formrecognizer.models.RecognizeReceiptsOptions, com.azure.core.util.Context) beginRecognizeReceipts}
 * method.</p>
 *
 * <p>For more information on which supported model you should use refer to
 * <a href="https://learn.microsoft.com/azure/applied-ai-services/form-recognizer/concept-model-overview?view=form-recog-2.1.0">models usage
 * documentation</a>.</p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Analyze a custom form with a model trained with or without labels.</h2>
 *
 * Analyze a custom form with a model trained with or without labels. Custom models are trained with your own data,
 * so they're tailored to your documents.
 *
 * <p>For more information, see
 * <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/v3/TrainModelWithLabels.java">train a model with labels</a>.
 *
 * <p><strong>Sample: Analyze a custom form with a model trained with labels</strong></p>
 *
 * <p>This sample demonstrates how to recognize form fields and other content from your custom forms, using models
 * you trained with your own form types.</p>
 *
 * <!-- src_embed readme-sample-train-recognize-custom-forms -->
 * <pre>
 * String trainingFilesUrl = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
 * boolean useTrainingLabels = true;
 *
 * SyncPoller&lt;FormRecognizerOperationResult, CustomFormModel&gt; trainingPoller =
 *     formTrainingClient.beginTraining&#40;trainingFilesUrl,
 *         useTrainingLabels,
 *         new TrainingOptions&#40;&#41;
 *             .setModelName&#40;&quot;my model trained with labels&quot;&#41;,
 *         Context.NONE&#41;;
 *
 * CustomFormModel customFormModel = trainingPoller.getFinalResult&#40;&#41;;
 *
 * &#47;&#47; Model Info
 * System.out.printf&#40;&quot;Model Id: %s%n&quot;, customFormModel.getModelId&#40;&#41;&#41;;
 *
 * String customFormUrl = &quot;customFormUrl&quot;;
 * String modelId = customFormModel.getModelId&#40;&#41;;
 * SyncPoller&lt;FormRecognizerOperationResult, List&lt;RecognizedForm&gt;&gt; recognizeFormPoller =
 *     formRecognizerClient.beginRecognizeCustomFormsFromUrl&#40;modelId, customFormUrl&#41;;
 *
 * List&lt;RecognizedForm&gt; recognizedForms = recognizeFormPoller.getFinalResult&#40;&#41;;
 *
 * for &#40;int i = 0; i &lt; recognizedForms.size&#40;&#41;; i++&#41; &#123;
 *     RecognizedForm form = recognizedForms.get&#40;i&#41;;
 *     System.out.printf&#40;&quot;----------- Recognized custom form info for page %d -----------%n&quot;, i&#41;;
 *     System.out.printf&#40;&quot;Form type: %s%n&quot;, form.getFormType&#40;&#41;&#41;;
 *     System.out.printf&#40;&quot;Form type confidence: %.2f%n&quot;, form.getFormTypeConfidence&#40;&#41;&#41;;
 *     form.getFields&#40;&#41;.forEach&#40;&#40;label, formField&#41; -&gt;
 *         System.out.printf&#40;&quot;Field %s has value %s with confidence score of %f.%n&quot;, label,
 *             formField.getValueData&#40;&#41;.getText&#40;&#41;,
 *             formField.getConfidence&#40;&#41;&#41;
 *     &#41;;
 * &#125;
 * </pre>
 * <!-- end readme-sample-train-recognize-custom-forms -->
 *
 * <p>For a suggested approach to extracting information from custom forms with known fields,
 * see
 * <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/v3/StronglyTypedRecognizedForm.java">strongly-typing a recognized form</a>.</p>
 *
 * @see com.azure.ai.formrecognizer.FormRecognizerClient
 * @see com.azure.ai.formrecognizer.FormRecognizerAsyncClient
 * @see com.azure.ai.formrecognizer.FormRecognizerClientBuilder
 */
package com.azure.ai.formrecognizer;
