// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p> <a href="https://aka.ms/form-recognizer-3.0.0">Azure Form Recognizer</a>
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
 * <p>The Azure Form Recognizer client library allows Java developers to interact with the Azure Form
 * Recognizer service.
 * It provides a set of classes and methods that abstract the underlying RESTful API of Azure
 * Form Recognizer, making it easier to integrate the service into Java applications.</p>
 *
 * <p>The Azure Form Recognizer client library provides the following capabilities:</p>
 *
 * <ol>
 *   <li>Form recognizing: It allows you to submit forms to extract information like text, key-value pairs, tables, and
 *   form fields. You can analyze both structured and unstructured documents.</li>
 *   <li>Model Management: It enables you to train custom models by providing labeled training data. You can also list
 *   and delete existing models.</li>
 *   <li>Recognize Results: It provides methods to retrieve and interpret analysis results, including extracted text
 *   and field values, confidence scores, and form layout information.</li>
 *   <li>Polling and Callbacks: It includes mechanisms for polling the service to check the status of an analysis
 *   operation or registering callbacks to receive notifications when the analysis is complete.</li>
 * </ol>
 *
 * <h2>Getting Started</h2>
 *
 * <p>The Azure Form Recognizer library provides
 * analysis clients like {@link com.azure.ai.formrecognizer.FormRecognizerAsyncClient}
 * and {@link com.azure.ai.formrecognizer.FormRecognizerClient} to connect to the Form Recognizer Azure Cognitive
 * Service to analyze information from documents and extract it into structured data.
 * It also provides training clients like {@link com.azure.ai.formrecognizer.training.FormTrainingClient}
 * and {@link com.azure.ai.formrecognizer.training.FormTrainingAsyncClient} to build and manage models from custom
 * documents.</p>
 *
 * <p><strong>Note: </strong>This client only supports
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
 * {@link com.azure.ai.formrecognizer.FormRecognizerAsyncClient} is the asynchronous service client. The examples
 * shown in this document use a credential object named DefaultAzureCredential for authentication, which is appropriate
 * for most scenarios, including local development and production environments. Additionally, we recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a FormTrainingClient with DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.ai.formrecognizer.training.FormTrainingClient}, using
 * the `DefaultAzureCredentialBuilder` to configure it.</p>
 *
 * <!-- src_embed readme-sample-createFormTrainingClientWithAAD -->
 * <pre>
 * FormTrainingClient client = new FormTrainingClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createFormTrainingClientWithAAD  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.core.credential.AzureKeyCredential AzureKeyCredential} for client creation.</p>
 *
 * <!-- src_embed readme-sample-createFormTrainingClient -->
 * <pre>
 * FormTrainingClient formTrainingClient = new FormTrainingClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createFormTrainingClient  -->
 *
 * <p>Let's take a look at the analysis client scenarios and their respective usage below.</p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Train a model</h2>
 *
 * <p>A trained model can output structured data that includes the relationships in the original form document.
 * For instructions on setting up forms for training in an Azure Blob Storage Container, see
 * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/build-training-data-set#upload-your-training-data"></a></p>
 *
 * <p>You can train custom models to recognize specific fields and values you specify by labeling your custom forms.</p>
 *
 * <p><strong>Sample: Train a model with your own data</strong></p>
 *
 * <p>The following code sample demonstrates how to train a model with your own data.</p>
 *
 * <!-- src_embed readme-sample-train-model -->
 * <pre>
 * String trainingFilesUrl = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
 * SyncPoller&lt;FormRecognizerOperationResult, CustomFormModel&gt; trainingPoller =
 *     formTrainingClient.beginTraining&#40;trainingFilesUrl,
 *         false,
 *         new TrainingOptions&#40;&#41;
 *             .setModelName&#40;&quot;my model trained without labels&quot;&#41;,
 *         Context.NONE&#41;;
 *
 * CustomFormModel customFormModel = trainingPoller.getFinalResult&#40;&#41;;
 *
 * &#47;&#47; Model Info
 * System.out.printf&#40;&quot;Model Id: %s%n&quot;, customFormModel.getModelId&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Model name given by user: %s%n&quot;, customFormModel.getModelName&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Model Status: %s%n&quot;, customFormModel.getModelStatus&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Training started on: %s%n&quot;, customFormModel.getTrainingStartedOn&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Training completed on: %s%n%n&quot;, customFormModel.getTrainingCompletedOn&#40;&#41;&#41;;
 *
 * System.out.println&#40;&quot;Recognized Fields:&quot;&#41;;
 * &#47;&#47; looping through the subModels, which contains the fields they were trained on
 * &#47;&#47; Since the given training documents are unlabeled, we still group them but they do not have a label.
 * customFormModel.getSubmodels&#40;&#41;.forEach&#40;customFormSubmodel -&gt; &#123;
 *     System.out.printf&#40;&quot;Submodel Id: %s%n: &quot;, customFormSubmodel.getModelId&#40;&#41;&#41;;
 *     &#47;&#47; Since the training data is unlabeled, we are unable to return the accuracy of this model
 *     customFormSubmodel.getFields&#40;&#41;.forEach&#40;&#40;field, customFormModelField&#41; -&gt;
 *         System.out.printf&#40;&quot;Field: %s Field Label: %s%n&quot;,
 *             field, customFormModelField.getLabel&#40;&#41;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end readme-sample-train-model -->
 *
 * <p>
 * Please note that models can also be trained using a graphical user interface such as the
 * <a href="https://docs.microsoft.com/azure/cognitive-services/form-recognizer/label-tool?tabs=v2-1">Form Recognizer Labeling Tool</a>.
 * </p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Manage custom models</h2>
 *
 * <p>Operations that can be executed are:</p>
 *
 * <ol>
 *     <li> Check the number of models in the FormRecognizer resource account, and the maximum number of models that
 *     can be stored. </li>
 *     <li>List the models currently stored in the resource account.</li>
 *     <li>Get a specific model using the model's Id.</li>
 *     <li>Delete a model from the resource account.</li>
 * </ol>
 *
 * <p><strong>Sample: Manage custom models stored in your account.</strong></p>
 *
 * <p>This sample demonstrates how to manage the custom models stored in your account.</p>
 *
 * <!-- src_embed readme-sample-manage-models -->
 * <pre>
 * &#47;&#47; First, we see how many custom models we have, and what our limit is
 * AccountProperties accountProperties = formTrainingClient.getAccountProperties&#40;&#41;;
 * System.out.printf&#40;&quot;The account has %d custom models, and we can have at most %d custom models&quot;,
 *     accountProperties.getCustomModelCount&#40;&#41;, accountProperties.getCustomModelLimit&#40;&#41;&#41;;
 *
 * &#47;&#47; Next, we get a paged list of all of our custom models
 * PagedIterable&lt;CustomFormModelInfo&gt; customModels = formTrainingClient.listCustomModels&#40;&#41;;
 * System.out.println&#40;&quot;We have following models in the account:&quot;&#41;;
 * customModels.forEach&#40;customFormModelInfo -&gt; &#123;
 *     System.out.printf&#40;&quot;Model Id: %s%n&quot;, customFormModelInfo.getModelId&#40;&#41;&#41;;
 *     &#47;&#47; get specific custom model info
 *     CustomFormModel customModel = formTrainingClient.getCustomModel&#40;customFormModelInfo.getModelId&#40;&#41;&#41;;
 *     System.out.printf&#40;&quot;Model Status: %s%n&quot;, customModel.getModelStatus&#40;&#41;&#41;;
 *     System.out.printf&#40;&quot;Training started on: %s%n&quot;, customModel.getTrainingStartedOn&#40;&#41;&#41;;
 *     System.out.printf&#40;&quot;Training completed on: %s%n&quot;, customModel.getTrainingCompletedOn&#40;&#41;&#41;;
 *     customModel.getSubmodels&#40;&#41;.forEach&#40;customFormSubmodel -&gt; &#123;
 *         System.out.printf&#40;&quot;Custom Model Form type: %s%n&quot;, customFormSubmodel.getFormType&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Custom Model Accuracy: %f%n&quot;, customFormSubmodel.getAccuracy&#40;&#41;&#41;;
 *         if &#40;customFormSubmodel.getFields&#40;&#41; != null&#41; &#123;
 *             customFormSubmodel.getFields&#40;&#41;.forEach&#40;&#40;fieldText, customFormModelField&#41; -&gt; &#123;
 *                 System.out.printf&#40;&quot;Field Text: %s%n&quot;, fieldText&#41;;
 *                 System.out.printf&#40;&quot;Field Accuracy: %f%n&quot;, customFormModelField.getAccuracy&#40;&#41;&#41;;
 *             &#125;&#41;;
 *         &#125;
 *     &#125;&#41;;
 * &#125;&#41;;
 *
 * &#47;&#47; Delete Custom Model
 * formTrainingClient.deleteModel&#40;&quot;&#123;modelId&#125;&quot;&#41;;
 * </pre>
 * <!-- end readme-sample-manage-models -->
 *
 * <p>For a suggested approach to extracting information from custom forms with known fields, see
 * <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/v3/StronglyTypedRecognizedForm.java">strongly-typing a recognized form</a>.</p>
 *
 * @see com.azure.ai.formrecognizer.training.FormTrainingClient
 * @see com.azure.ai.formrecognizer.training.FormTrainingAsyncClient
 * @see com.azure.ai.formrecognizer.training.FormTrainingClientBuilder
 */
package com.azure.ai.formrecognizer.training;
