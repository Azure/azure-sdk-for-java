// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://aka.ms/form-recognizer-3.0.0">
 * Azure Form Recognizer</a> is a cloud-based service provided by Microsoft Azure that utilizes machine learning to
 * extract information from various types of documents. Form Recognizer applies machine-learning-based optical
 * character recognition (OCR) and document understanding technologies to classify documents, extract text, tables,
 * structure, and key-value pairs from documents. You can also label and train custom models to automate data
 * extraction from structured, semi-structured, and unstructured documents.</p>
 *
 * <p>The service uses advanced optical character recognition (OCR) technology to extract text and key-value
 * pairs from documents, enabling organizations to automate data entry tasks that would otherwise require
 * manual effort. It can recognize and extract information like dates, addresses, invoice numbers, line items,
 * and other relevant data points from documents. </p>
 *
 * <p>The Azure Form Recognizer client library allows Java developers to interact with the Azure Form
 * Recognizer service.
 * It provides a set of classes and methods that abstract the underlying RESTful API of Azure
 * Form Recognizer, making it easier to integrate the service into Java applications.</p>
 *
 * <p>The Azure Form Recognizer client library provides the following capabilities:</p>
 *
 * <ol>
 *     <li>Document Analysis: It allows you to submit documents for analysis to detect and extract information like
 *     text, key-value pairs, tables, language, and fields. You can analyze both structured and unstructured
 *     documents.</li>
 *     <li>Model Management: It enables you to manage models created in your account by building, listing,
 *     deleting, and see the limit of custom models your account.</li>
 *     <li>Analysis Results: It provides methods to retrieve and interpret analysis results, including extracted
 *     text and field values, confidence scores, and document layout information.</li>
 *     <li>Polling and Callbacks: It includes mechanisms for polling the service to check the status of an analysis
 *     operation or registering callbacks to receive notifications when the analysis is complete.</li>
 * </ol>
 *
 * <h2>Getting Started</h2>
 *
 * <p>The Azure Form Recognizer library provides analysis clients like
 * {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient}
 * and {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient} to connect to the Form Recognizer
 * Azure Cognitive Service
 * to analyze information from documents and extract it into structured data.
 * It also provides administration clients like
 * {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient}
 * and {@link com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient} to
 * build and manage models from custom documents.</p>
 *
 * <p><strong>Note:</strong>This client only supports
 * {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisServiceVersion#V2022_08_31} and newer.
 * To use an older service version, {@link com.azure.ai.formrecognizer.FormRecognizerClient} and
 * {@link com.azure.ai.formrecognizer.training.FormTrainingClient}.</p>
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
 * <p><strong>Sample: Construct a DocumentAnalysisClient with DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient}, using
 * the `DefaultAzureCredentialBuilder` to configure it.</p>
 *
 * <!-- src_embed readme-sample-createDocumentAnalysisClientWithAAD -->
 * <pre>
 * DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createDocumentAnalysisClientWithAAD  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.core.credential.AzureKeyCredential AzureKeyCredential} for client creation.</p>
 *
 * <!-- src_embed readme-sample-createDocumentAnalysisClient -->
 * <pre>
 * DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createDocumentAnalysisClient  -->
 *
 * <p>Let's take a look at the analysis client scenarios and their respective usage below.</p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Analyzing documents with prebuilt models</h2>
 *
 * <p><a href="https://learn.microsoft.com/en-us/azure/applied-ai-services/form-recognizer/concept-model-overview?view=form-recog-3.0.0#model-overview">Form Recognizer models</a>
 * and their associated output to help you choose the best model to address your document scenario needs.</p>
 *
 * <p>You can use a prebuilt document analysis or domain specific model or build a custom model tailored to your
 * specific business needs and use cases.
 *
 * <p><strong>Sample: Analyze with the prebuilt read model from url source</strong></p>
 *
 * <p>The following code sample demonstrates how to analyze textual elements, such as words, lines, styles, and text
 * language information from documents using the prebuilt read model.</p>
 *
 * <!-- src_embed readme-sample-prebuiltRead-url -->
 * <pre>
 * String documentUrl = &quot;documentUrl&quot;;
 *
 * SyncPoller&lt;OperationResult, AnalyzeResult&gt; analyzeResultPoller =
 *     documentAnalysisClient.beginAnalyzeDocumentFromUrl&#40;&quot;prebuilt-read&quot;, documentUrl&#41;;
 * AnalyzeResult analyzeResult = analyzeResultPoller.getFinalResult&#40;&#41;;
 *
 * System.out.println&#40;&quot;Detected Languages: &quot;&#41;;
 * for &#40;DocumentLanguage language : analyzeResult.getLanguages&#40;&#41;&#41; &#123;
 *     System.out.printf&#40;&quot;Found language with locale %s and confidence %.2f&quot;,
 *         language.getLocale&#40;&#41;,
 *         language.getConfidence&#40;&#41;&#41;;
 * &#125;
 *
 * System.out.println&#40;&quot;Detected Styles: &quot;&#41;;
 * for &#40;DocumentStyle style: analyzeResult.getStyles&#40;&#41;&#41; &#123;
 *     if &#40;style.isHandwritten&#40;&#41;&#41; &#123;
 *         System.out.printf&#40;&quot;Found handwritten content %s with confidence %.2f&quot;,
 *             style.getSpans&#40;&#41;.stream&#40;&#41;.map&#40;span -&gt; analyzeResult.getContent&#40;&#41;
 *                 .substring&#40;span.getOffset&#40;&#41;, span.getLength&#40;&#41;&#41;&#41;,
 *             style.getConfidence&#40;&#41;&#41;;
 *     &#125;
 * &#125;
 *
 * &#47;&#47; pages
 * analyzeResult.getPages&#40;&#41;.forEach&#40;documentPage -&gt; &#123;
 *     System.out.printf&#40;&quot;Page has width: %.2f and height: %.2f, measured with unit: %s%n&quot;,
 *         documentPage.getWidth&#40;&#41;,
 *         documentPage.getHeight&#40;&#41;,
 *         documentPage.getUnit&#40;&#41;&#41;;
 *
 *     &#47;&#47; lines
 *     documentPage.getLines&#40;&#41;.forEach&#40;documentLine -&gt;
 *         System.out.printf&#40;&quot;Line '%s' is within a bounding polygon %s.%n&quot;,
 *             documentLine.getContent&#40;&#41;,
 *             documentLine.getBoundingPolygon&#40;&#41;.stream&#40;&#41;.map&#40;point -&gt; String.format&#40;&quot;[%.2f, %.2f]&quot;, point.getX&#40;&#41;,
 *                 point.getY&#40;&#41;&#41;&#41;.collect&#40;Collectors.joining&#40;&quot;, &quot;&#41;&#41;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end readme-sample-prebuiltRead-url -->
 *
 * <p>You can also analyze a local file with prebuilt models using the
 * {@link com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient}.</p>
 *
 * <p><strong>Sample: Analyze local file with the prebuilt read model</strong></p>
 *
 * <p>The following code sample demonstrates how to analyze a local file with "prebuilt-read" analysis model.</p>
 *
 * <!-- src_embed readme-sample-prebuiltRead-file -->
 * <pre>
 * File document = new File&#40;&quot;&#123;local&#47;file_path&#47;fileName.jpg&#125;&quot;&#41;;
 * SyncPoller&lt;OperationResult, AnalyzeResult&gt; analyzeResultPoller =
 *     documentAnalysisClient.beginAnalyzeDocument&#40;&quot;prebuilt-read&quot;,
 *         BinaryData.fromFile&#40;document.toPath&#40;&#41;,
 *             &#40;int&#41; document.length&#40;&#41;&#41;&#41;;
 * AnalyzeResult analyzeResult = analyzeResultPoller.getFinalResult&#40;&#41;;
 *
 * System.out.println&#40;&quot;Detected Languages: &quot;&#41;;
 * for &#40;DocumentLanguage language : analyzeResult.getLanguages&#40;&#41;&#41; &#123;
 *     System.out.printf&#40;&quot;Found language with locale %s and confidence %.2f&quot;,
 *         language.getLocale&#40;&#41;,
 *         language.getConfidence&#40;&#41;&#41;;
 * &#125;
 *
 * System.out.println&#40;&quot;Detected Styles: &quot;&#41;;
 * for &#40;DocumentStyle style: analyzeResult.getStyles&#40;&#41;&#41; &#123;
 *     if &#40;style.isHandwritten&#40;&#41;&#41; &#123;
 *         System.out.printf&#40;&quot;Found handwritten content %s with confidence %.2f&quot;,
 *             style.getSpans&#40;&#41;.stream&#40;&#41;.map&#40;span -&gt; analyzeResult.getContent&#40;&#41;
 *                 .substring&#40;span.getOffset&#40;&#41;, span.getLength&#40;&#41;&#41;&#41;,
 *             style.getConfidence&#40;&#41;&#41;;
 *     &#125;
 * &#125;
 *
 * &#47;&#47; pages
 * analyzeResult.getPages&#40;&#41;.forEach&#40;documentPage -&gt; &#123;
 *     System.out.printf&#40;&quot;Page has width: %.2f and height: %.2f, measured with unit: %s%n&quot;,
 *         documentPage.getWidth&#40;&#41;,
 *         documentPage.getHeight&#40;&#41;,
 *         documentPage.getUnit&#40;&#41;&#41;;
 *
 *     &#47;&#47; lines
 *     documentPage.getLines&#40;&#41;.forEach&#40;documentLine -&gt;
 *         System.out.printf&#40;&quot;Line '%s' is within a bounding polygon %s.%n&quot;,
 *             documentLine.getContent&#40;&#41;,
 *             documentLine.getBoundingPolygon&#40;&#41;.stream&#40;&#41;.map&#40;point -&gt; String.format&#40;&quot;[%.2f, %.2f]&quot;, point.getX&#40;&#41;,
 *                 point.getY&#40;&#41;&#41;&#41;.collect&#40;Collectors.joining&#40;&quot;, &quot;&#41;&#41;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end readme-sample-prebuiltRead-file -->
 * <p>
 * For more information on which supported model you should use refer to
 * <a href="https://learn.microsoft.com/azure/applied-ai-services/form-recognizer/overview?view=form-recog-3.0.0#which-form-recognizer-model-should-i-use">models usage
 * documentation</a>.
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Analyzing documents with custom models</h2>
 *
 * <p>Custom models are trained with your own data, so they're tailored to your documents.
 * For more information on how to build your own custom model, see
 * <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/BuildDocumentModel.java">build a model</a>.</p>
 *
 * <p><strong>Sample: Analyze documents using custom trained model</strong></p>
 *
 * <p>This sample demonstrates how to analyze text, field values, selection marks, and table data from custom
 * documents.</p>
 *
 * <!-- src_embed readme-sample-build-analyze -->
 * <pre>
 * String blobContainerUrl = &quot;&#123;SAS_URL_of_your_container_in_blob_storage&#125;&quot;;
 * &#47;&#47; The shared access signature &#40;SAS&#41; Url of your Azure Blob Storage container with your custom documents.
 * String prefix = &quot;&#123;blob_name_prefix&#125;&#125;&quot;;
 * &#47;&#47; Build custom document analysis model
 * SyncPoller&lt;OperationResult, DocumentModelDetails&gt; buildOperationPoller =
 *     documentModelAdminClient.beginBuildDocumentModel&#40;blobContainerUrl,
 *         DocumentModelBuildMode.TEMPLATE,
 *         prefix,
 *         new BuildDocumentModelOptions&#40;&#41;.setModelId&#40;&quot;my-custom-built-model&quot;&#41;.setDescription&#40;&quot;model desc&quot;&#41;,
 *         Context.NONE&#41;;
 *
 * DocumentModelDetails customBuildModel = buildOperationPoller.getFinalResult&#40;&#41;;
 *
 * &#47;&#47; analyze using custom-built model
 * String modelId = customBuildModel.getModelId&#40;&#41;;
 * String documentUrl = &quot;documentUrl&quot;;
 * SyncPoller&lt;OperationResult, AnalyzeResult&gt; analyzeDocumentPoller =
 *     documentAnalysisClient.beginAnalyzeDocumentFromUrl&#40;modelId, documentUrl&#41;;
 *
 * AnalyzeResult analyzeResult = analyzeDocumentPoller.getFinalResult&#40;&#41;;
 *
 * for &#40;int i = 0; i &lt; analyzeResult.getDocuments&#40;&#41;.size&#40;&#41;; i++&#41; &#123;
 *     final AnalyzedDocument analyzedDocument = analyzeResult.getDocuments&#40;&#41;.get&#40;i&#41;;
 *     System.out.printf&#40;&quot;----------- Analyzing custom document %d -----------%n&quot;, i&#41;;
 *     System.out.printf&#40;&quot;Analyzed document has doc type %s with confidence : %.2f%n&quot;,
 *         analyzedDocument.getDocType&#40;&#41;, analyzedDocument.getConfidence&#40;&#41;&#41;;
 *     analyzedDocument.getFields&#40;&#41;.forEach&#40;&#40;key, documentField&#41; -&gt; &#123;
 *         System.out.printf&#40;&quot;Document Field content: %s%n&quot;, documentField.getContent&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Document Field confidence: %.2f%n&quot;, documentField.getConfidence&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Document Field Type: %s%n&quot;, documentField.getType&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Document Field found within bounding region: %s%n&quot;,
 *             documentField.getBoundingRegions&#40;&#41;.toString&#40;&#41;&#41;;
 *     &#125;&#41;;
 * &#125;
 *
 * analyzeResult.getPages&#40;&#41;.forEach&#40;documentPage -&gt; &#123;
 *     System.out.printf&#40;&quot;Page has width: %.2f and height: %.2f, measured with unit: %s%n&quot;,
 *         documentPage.getWidth&#40;&#41;,
 *         documentPage.getHeight&#40;&#41;,
 *         documentPage.getUnit&#40;&#41;&#41;;
 *
 *     &#47;&#47; lines
 *     documentPage.getLines&#40;&#41;.forEach&#40;documentLine -&gt;
 *         System.out.printf&#40;&quot;Line '%s' is within a bounding box %s.%n&quot;,
 *             documentLine.getContent&#40;&#41;,
 *             documentLine.getBoundingPolygon&#40;&#41;.toString&#40;&#41;&#41;&#41;;
 *
 *     &#47;&#47; words
 *     documentPage.getWords&#40;&#41;.forEach&#40;documentWord -&gt;
 *         System.out.printf&#40;&quot;Word '%s' has a confidence score of %.2f.%n&quot;,
 *             documentWord.getContent&#40;&#41;,
 *             documentWord.getConfidence&#40;&#41;&#41;&#41;;
 * &#125;&#41;;
 *
 * &#47;&#47; tables
 * List&lt;DocumentTable&gt; tables = analyzeResult.getTables&#40;&#41;;
 * for &#40;int i = 0; i &lt; tables.size&#40;&#41;; i++&#41; &#123;
 *     DocumentTable documentTable = tables.get&#40;i&#41;;
 *     System.out.printf&#40;&quot;Table %d has %d rows and %d columns.%n&quot;, i, documentTable.getRowCount&#40;&#41;,
 *         documentTable.getColumnCount&#40;&#41;&#41;;
 *     documentTable.getCells&#40;&#41;.forEach&#40;documentTableCell -&gt; &#123;
 *         System.out.printf&#40;&quot;Cell '%s', has row index %d and column index %d.%n&quot;,
 *             documentTableCell.getContent&#40;&#41;,
 *             documentTableCell.getRowIndex&#40;&#41;, documentTableCell.getColumnIndex&#40;&#41;&#41;;
 *     &#125;&#41;;
 *     System.out.println&#40;&#41;;
 * &#125;
 * </pre>
 * <!-- end readme-sample-build-analyze -->
 *
 * @see com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient
 * @see com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient
 * @see com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder
 * @see com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult
 */
package com.azure.ai.formrecognizer.documentanalysis;
