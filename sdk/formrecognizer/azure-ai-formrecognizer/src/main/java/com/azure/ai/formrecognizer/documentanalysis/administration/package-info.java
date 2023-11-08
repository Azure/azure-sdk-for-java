// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://aka.ms/form-recognizer-3.0.0">Azure Form Recognizer</a>
 * is a cloud-based service provided by Microsoft Azure that utilizes machine learning to extract information
 * from various types of documents. Form Recognizer applies machine-learning-based optical character recognition (OCR)
 * and document understanding technologies to classify documents, extract text, tables, structure, and key-value
 * pairs from documents.
 * You can also label and train custom models to automate data extraction from structured, semi-structured, and
 * unstructured documents.</p>
 *
 * <p>The service uses advanced optical character recognition (OCR) technology to extract text and key-value
 * pairs from documents, enabling organizations to automate data entry tasks that would otherwise require
 * manual effort. It can recognize and extract information like dates, addresses, invoice numbers, line items,
 * and other relevant data points from documents. </p>
 *
 * <p> The Azure Form Recognizer client library allows Java developers to interact with the Azure Form
 * Recognizer service.
 * It provides a set of classes and methods that abstract the underlying RESTful API of Azure
 * Form Recognizer, making it easier to integrate the service into Java applications.</p>
 *
 * <p>The Azure Form Recognizer client library provides the following capabilities:</p>
 *
 * <ol>
 *     <li>Document Analysis: It allows you to submit documents for analysis to detect and extract information like text,
 *     key-value pairs, tables, language, and fields. You can analyze both structured and unstructured documents.</li>
 *     <li>Model Management: It enables you to manage models created in your account by building, listing,
 *     deleting, and see the limit of custom models your account.</li>
 *     <li>Analysis Results: It provides methods to retrieve and interpret analysis results, including extracted text
 *     and field values, confidence scores, and document layout information.</li>
 *     <li>Polling and Callbacks: It includes mechanisms for polling the service to check the status of an analysis
 *     operation or registering callbacks to receive notifications when the analysis is complete.</li>
 * </ol>
 *
 * <h2>Getting Started</h2>
 *
 * <p>The Azure Form Recognizer library provides
 * analysis clients like {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient}
 * and {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient} to connect to the Form Recognizer
 * Azure Cognitive Service to analyze information from documents and extract it into structured data.
 * It also provides administration clients like
 * {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient}
 * and {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient} to
 * build and manage models from custom documents.</p>
 *
 * <p><strong>Note: </strong>This client only supports
 * {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisServiceVersion#V2022_08_31} and newer.
 * To use an older service version, {@link com.azure.ai.formrecognizer.training.FormTrainingClient} and
 * {@link com.azure.ai.formrecognizer.training.FormTrainingAsyncClient}</p>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Form Recognizer.
 * {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient} is the synchronous service client and
 * {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient} is the asynchronous service client.
 * The examples shown in this document use a credential object named DefaultAzureCredential for authentication, which is
 * appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a
 * {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient} with
 * DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient}, using
 * the `DefaultAzureCredentialBuilder` to configure it.</p>
 *
 * <!-- src_embed com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.initialization -->
 * <pre>
 * DocumentModelAdministrationClient client = new DocumentModelAdministrationClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdminClient.initialization  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.core.credential.AzureKeyCredential AzureKeyCredential} for client creation.</p>
 *
 * <!-- src_embed readme-sample-createDocumentModelAdministrationClient -->
 * <pre>
 * DocumentModelAdministrationClient client =
 *     new DocumentModelAdministrationClientBuilder&#40;&#41;
 *         .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createDocumentModelAdministrationClient  -->
 *
 * <p>Let's take a look at the administration client scenarios and their respective usage below.</p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Build custom document models</h2>
 *
 * <p><a href="https://learn.microsoft.com/azure/applied-ai-services/form-recognizer/concept-custom?view=form-recog-3.0.0&tabs=extraction%2Cclassification#custom-document-model-types">Custom document models</a>
 * are built by labelling a dataset of documents with the values you want extracted to address your document scenario
 * needs.</p>
 *
 * <p>The request must include a `blobContainerUrl` that is an externally accessible Azure storage blob container URI
 * (preferably a Shared Access Signature URI).
 * Note that a container URI (without SAS) is accepted only when the container is public or has a managed identity
 * configured, see more about configuring managed identities to work with Form Recognizer
 * <a href="https://docs.microsoft.com/azure/applied-ai-services/form-recognizer/managed-identities">here</a>.</p>
 *
 * <p>For more information on different custom document model types, refer to
 * <a href="https://learn.microsoft.com/azure/applied-ai-services/form-recognizer/concept-custom?view=form-recog-3.0.0&tabs=extraction%2Cclassification#custom-document-model-types">custom document models types</a>
 *
 * <p><strong>Sample: Build a custom document model</strong></p>
 *
 * <p>The following code sample demonstrates how to build a custom model with your own data.</p>
 *
 * <!-- src_embed readme-sample-buildModel -->
 * <pre>
 * &#47;&#47; Build custom document analysis model
 * String blobContainerUrl = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
 * &#47;&#47; The shared access signature &#40;SAS&#41; Url of your Azure Blob Storage container with your forms.
 * String prefix = &quot;&#123;blob_name_prefix&#125;&#125;&quot;;
 * SyncPoller&lt;OperationResult, DocumentModelDetails&gt; buildOperationPoller =
 *     documentModelAdminClient.beginBuildDocumentModel&#40;blobContainerUrl,
 *         DocumentModelBuildMode.TEMPLATE,
 *         prefix,
 *         new BuildDocumentModelOptions&#40;&#41;.setModelId&#40;&quot;my-build-model&quot;&#41;.setDescription&#40;&quot;model desc&quot;&#41;,
 *         Context.NONE&#41;;
 *
 * DocumentModelDetails documentModelDetails = buildOperationPoller.getFinalResult&#40;&#41;;
 *
 * &#47;&#47; Model Info
 * System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelDetails.getModelId&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModelDetails.getDescription&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Model created on: %s%n%n&quot;, documentModelDetails.getCreatedOn&#40;&#41;&#41;;
 * documentModelDetails.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
 *     System.out.printf&#40;&quot;Document type: %s%n&quot;, key&#41;;
 *     documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;name, documentFieldSchema&#41; -&gt; &#123;
 *         System.out.printf&#40;&quot;Document field: %s%n&quot;, name&#41;;
 *         System.out.printf&#40;&quot;Document field type: %s%n&quot;, documentFieldSchema.getType&#40;&#41;.toString&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Document field confidence: %.2f%n&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;name&#41;&#41;;
 *     &#125;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end readme-sample-buildModel -->
 *
 * Please note that models can also be built using a graphical user interface
 * <a href="https://aka.ms/azsdk/formrecognizer/labelingtool">Form Recognizer Labeling Tool
 * .</a>.
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Manage models</h2>
 *
 * Managing models created in your account by building, listing, deleting, and see the limit of custom models your
 * account.
 *
 * <p>
 * Refer to
 * <a href="https://learn.microsoft.com/azure/applied-ai-services/form-recognizer/service-limits?view=form-recog-3.0.0">service quotas and limits</a>
 * to know more your resource models and custom models usage.</p>
 *
 * <p><strong>Sample: Manage models</strong></p>
 *
 * <p>This sample demonstrates how to manage (get, list, delete) the models stored in your account.</p>
 *
 * <!-- src_embed readme-sample-manageModels -->
 * <pre>
 * AtomicReference&lt;String&gt; modelId = new AtomicReference&lt;&gt;&#40;&#41;;
 *
 * &#47;&#47; First, we see how many models we have, and what our limit is
 * ResourceDetails resourceDetails = documentModelAdminClient.getResourceDetails&#40;&#41;;
 * System.out.printf&#40;&quot;The resource has %s models, and we can have at most %s models&quot;,
 *     resourceDetails.getCustomDocumentModelCount&#40;&#41;, resourceDetails.getCustomDocumentModelLimit&#40;&#41;&#41;;
 *
 * &#47;&#47; Next, we get a paged list of all of our models
 * PagedIterable&lt;DocumentModelSummary&gt; customDocumentModels = documentModelAdminClient.listDocumentModels&#40;&#41;;
 * System.out.println&#40;&quot;We have following models in the account:&quot;&#41;;
 * customDocumentModels.forEach&#40;documentModelSummary -&gt; &#123;
 *     System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModelSummary.getModelId&#40;&#41;&#41;;
 *     modelId.set&#40;documentModelSummary.getModelId&#40;&#41;&#41;;
 *
 *     &#47;&#47; get custom document analysis model info
 *     DocumentModelDetails documentModel = documentModelAdminClient.getDocumentModel&#40;documentModelSummary.getModelId&#40;&#41;&#41;;
 *     System.out.printf&#40;&quot;Model ID: %s%n&quot;, documentModel.getModelId&#40;&#41;&#41;;
 *     System.out.printf&#40;&quot;Model Description: %s%n&quot;, documentModel.getDescription&#40;&#41;&#41;;
 *     System.out.printf&#40;&quot;Model created on: %s%n&quot;, documentModel.getCreatedOn&#40;&#41;&#41;;
 *     documentModel.getDocumentTypes&#40;&#41;.forEach&#40;&#40;key, documentTypeDetails&#41; -&gt; &#123;
 *         documentTypeDetails.getFieldSchema&#40;&#41;.forEach&#40;&#40;field, documentFieldSchema&#41; -&gt; &#123;
 *             System.out.printf&#40;&quot;Field: %s&quot;, field&#41;;
 *             System.out.printf&#40;&quot;Field type: %s&quot;, documentFieldSchema.getType&#40;&#41;&#41;;
 *             System.out.printf&#40;&quot;Field confidence: %.2f&quot;, documentTypeDetails.getFieldConfidence&#40;&#41;.get&#40;field&#41;&#41;;
 *         &#125;&#41;;
 *     &#125;&#41;;
 * &#125;&#41;;
 *
 * &#47;&#47; Delete Model
 * documentModelAdminClient.deleteDocumentModel&#40;modelId.get&#40;&#41;&#41;;
 * </pre>
 * <!-- end readme-sample-manageModels -->
 *
 * @see com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient
 * @see com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient
 * @see com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder
 */
package com.azure.ai.formrecognizer.documentanalysis.administration;
