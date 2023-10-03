// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/azure/ai-services/language-service">Azure AI Language Service</a>
 * is a cloud-based natural language processing (NLP) service offered by Microsoft Azure. It's designed to
 * extract valuable insights and information from text data through various NLP techniques. The service provides
 * a range of capabilities for analyzing text, including sentiment analysis, entity recognition, key phrase extraction,
 * language detection, and more. These capabilities can be leveraged to gain a deeper understanding of textual data,
 * automate processes, and make informed decisions based on the analyzed content.</p>
 *
 * <p>Here are some of the key features of Azure Text Analytics:</p>
 *
 * <ul>
 *     <li>Sentiment Analysis: This feature determines the sentiment expressed in a piece of text, whether
 *     it's positive, negative, or neutral. It's useful for understanding the overall emotional tone of
 *     customer reviews, social media posts, and other text-based content.</li>
 *
 *     <li>Entity Recognition: Azure AI Language can identify and categorize entities mentioned in the text,
 *     such as people, organizations, locations, dates, and more. This is particularly useful for extracting
 *     structured information from unstructured text.</li>
 *
 *     <li>Key Phrase Extraction: The service can automatically identify and extract key phrases or important terms
 *     from a given text. This can help summarize the main topics or subjects discussed in the text.</li>
 *
 *     <li>Language Detection: Azure AI Language can detect the language in which the text is written. This is
 *     useful for routing content to appropriate language-specific processes or for organizing and categorizing
 *     multilingual data.</li>
 *
 *     <li>Named Entity Recognition: In addition to identifying entities, the service can categorize them into
 *     pre-defined types, such as person names, organization names, locations, dates, and more.</li>
 *
 *     <li>Entity Linking: This feature can link recognized entities to external databases or sources of information,
 *     enriching the extracted data with additional context.</li>
 *
 *     <li>Customizable Models: Azure AI Language allows you to fine-tune and train the service's models with your
 *     specific domain or industry terminology, which can enhance the accuracy of entity recognition and sentiment
 *     analysis.</li>
 * </ul>
 *
 * <p>The Azure Text Analytics library is a client library that provides Java developers with a simple and
 * easy-to-use interface for accessing and using the Azure AI Language Service. This library allows developers to
 * can be used to analyze unstructured text for tasks, such as sentiment analysis, entities recognition(PII, Health,
 * Linked, Custom), key phrases extraction, language detection, abstractive and extractive summarizations,
 * single-label and multi-label classifications, and execute multiple actions/operations in a single request.
 * </p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Text Analytics features in Azure AI Language Service, you'll need to create an
 * instance of the Text Analytics Client class. To make this possible you'll need the key credential of the service.
 * Alternatively, you can use AAD authentication via
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity</a>
 * to connect to the service.</p>
 *
 * <ol>
 *   <li>Azure Key Credential, see {@link com.azure.ai.textanalytics.TextAnalyticsClientBuilder#credential(
 *   com.azure.core.credential.AzureKeyCredential) AzureKeyCredential}.</li>
 *   <li>Azure Active Directory, see {@link com.azure.ai.textanalytics.TextAnalyticsClientBuilder#credential(
 *   com.azure.core.credential.TokenCredential) TokenCredential}.</li>
 * </ol>
 *
 * <p><strong>Sample: Construct Synchronous Text Analytics Client with Azure Key Credential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.ai.textanalytics.TextAnalyticsClient},
 * using the {@link com.azure.ai.textanalytics.TextAnalyticsClientBuilder} to configure it with a key credential.</p>
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.instantiation -->
 * <pre>
 * TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.instantiation -->
 *
 * <p><strong>Sample: Construct Asynchronous Text Analytics Client with Azure Key Credential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient},
 * using the {@link com.azure.ai.textanalytics.TextAnalyticsClientBuilder} to configure it with a key credential.</p>
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation  -->
 * <pre>
 * TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation  -->
 *
 * <p><Strong>Note:</Strong> See methods in client level class below to explore all features that library provides.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Extract information</h2>
 *
 * <p>Text Analytics client can be use Natural Language Understanding (NLU) to extract information from unstructured text.
 * For example, identify key phrases or Personally Identifiable, etc. Below you can look at the samples on how to use it.</p>
 *
 * <h3>Key Phrases Extraction</h3>
 *
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#extractKeyPhrases(java.lang.String) extractKeyPhrases}
 * method can be used to extract key phrases, which returns a list of strings denoting the key phrases in the document.
 * </p>
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String -->
 * <pre>
 * KeyPhrasesCollection extractedKeyPhrases =
 *     textAnalyticsClient.extractKeyPhrases&#40;&quot;My cat might need to see a veterinarian.&quot;&#41;;
 * for &#40;String keyPhrase : extractedKeyPhrases&#41; &#123;
 *     System.out.printf&#40;&quot;%s.%n&quot;, keyPhrase&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <h3>Named Entities Recognition(NER): Prebuilt Model</h3>
 *
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#recognizeEntities(java.lang.String) recognizeEntities}
 * method can be used to recognize entities, which returns a list of general categorized entities in the provided
 * document.</p>
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String -->
 * <pre>
 * CategorizedEntityCollection recognizeEntitiesResult =
 *     textAnalyticsClient.recognizeEntities&#40;&quot;Satya Nadella is the CEO of Microsoft&quot;&#41;;
 * for &#40;CategorizedEntity entity : recognizeEntitiesResult&#41; &#123;
 *     System.out.printf&#40;&quot;Recognized entity: %s, entity category: %s, confidence score: %f.%n&quot;,
 *         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <h3>Custom Named Entities Recognition(NER): Custom Model</h3>
 *
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#beginRecognizeCustomEntities(
 * java.lang.Iterable, java.lang.String, java.lang.String)} method can be used to recognize custom entities,
 * which returns a list of custom entities for the provided list of {@link java.lang.String document}.</p>
 *
 * <!-- src_embed Client.beginRecognizeCustomEntities#Iterable-String-String -->
 * <pre>
 * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
 * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
 *     documents.add&#40;
 *         &quot;A recent report by the Government Accountability Office &#40;GAO&#41; found that the dramatic increase &quot;
 *             + &quot;in oil and natural gas development on federal lands over the past six years has stretched the&quot;
 *             + &quot; staff of the BLM to a point that it has been unable to meet its environmental protection &quot;
 *             + &quot;responsibilities.&quot;&#41;; &#125;
 * SyncPoller&lt;RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable&gt; syncPoller =
 *     textAnalyticsClient.beginRecognizeCustomEntities&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;&#41;;
 * syncPoller.waitForCompletion&#40;&#41;;
 * syncPoller.getFinalResult&#40;&#41;.forEach&#40;documentsResults -&gt; &#123;
 *     System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
 *         documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
 *     for &#40;RecognizeEntitiesResult documentResult : documentsResults&#41; &#123;
 *         System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
 *         for &#40;CategorizedEntity entity : documentResult.getEntities&#40;&#41;&#41; &#123;
 *             System.out.printf&#40;
 *                 &quot;&#92;tText: %s, category: %s, confidence score: %f.%n&quot;,
 *                 entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;;
 *         &#125;
 *     &#125;
 * &#125;&#41;;
 * </pre>
 * <!-- end Client.beginRecognizeCustomEntities#Iterable-String-String -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample,
 * refer to {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <h3>Linked Entities Recognition</h3>
 *
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#recognizeLinkedEntities(java.lang.String)
 * recognizeLinkedEntities} method can be used to find linked entities, which returns a list of recognized entities
 * with links to a well-known knowledge base for the provided document.
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String -->
 * <pre>
 * String document = &quot;Old Faithful is a geyser at Yellowstone Park.&quot;;
 * System.out.println&#40;&quot;Linked Entities:&quot;&#41;;
 * textAnalyticsClient.recognizeLinkedEntities&#40;document&#41;.forEach&#40;linkedEntity -&gt; &#123;
 *     System.out.printf&#40;&quot;Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n&quot;,
 *         linkedEntity.getName&#40;&#41;, linkedEntity.getDataSourceEntityId&#40;&#41;, linkedEntity.getUrl&#40;&#41;,
 *         linkedEntity.getDataSource&#40;&#41;&#41;;
 *     linkedEntity.getMatches&#40;&#41;.forEach&#40;entityMatch -&gt; System.out.printf&#40;
 *         &quot;Matched entity: %s, confidence score: %f.%n&quot;,
 *         entityMatch.getText&#40;&#41;, entityMatch.getConfidenceScore&#40;&#41;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <h3>Personally Identifiable Information(PII) Entities Recognition</h3>
 *
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#recognizePiiEntities(java.lang.String) recognizePiiEntities}
 * method can be used to recognize PII entities, which returns a list of Personally Identifiable Information(PII)
 * entities in the provided document.
 *
 * For a list of supported entity types, check: <a href="https://aka.ms/azsdk/language/pii">this</a>
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String -->
 * <pre>
 * PiiEntityCollection piiEntityCollection = textAnalyticsClient.recognizePiiEntities&#40;&quot;My SSN is 859-98-0987&quot;&#41;;
 * System.out.printf&#40;&quot;Redacted Text: %s%n&quot;, piiEntityCollection.getRedactedText&#40;&#41;&#41;;
 * for &#40;PiiEntity entity : piiEntityCollection&#41; &#123;
 *     System.out.printf&#40;
 *         &quot;Recognized Personally Identifiable Information entity: %s, entity category: %s,&quot;
 *             + &quot; entity subcategory: %s, confidence score: %f.%n&quot;,
 *         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <h3>Text Analytics for Health: Prebuilt Model</h3>
 *
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#beginAnalyzeHealthcareEntities(java.lang.Iterable)
 * beginAnalyzeHealthcareEntities} method can be used to analyze healthcare entities, entity data sources, and
 * entity relations in a list of {@link java.lang.String documents}.
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeHealthcareEntities#Iterable -->
 * <pre>
 * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
 * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
 *     documents.add&#40;&quot;The patient is a 54-year-old gentleman with a history of progressive angina over &quot;
 *         + &quot;the past several months.&quot;&#41;;
 * &#125;
 *
 * SyncPoller&lt;AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable&gt;
 *     syncPoller = textAnalyticsClient.beginAnalyzeHealthcareEntities&#40;documents&#41;;
 *
 * syncPoller.waitForCompletion&#40;&#41;;
 * AnalyzeHealthcareEntitiesPagedIterable result = syncPoller.getFinalResult&#40;&#41;;
 *
 * result.forEach&#40;analyzeHealthcareEntitiesResultCollection -&gt; &#123;
 *     analyzeHealthcareEntitiesResultCollection.forEach&#40;healthcareEntitiesResult -&gt; &#123;
 *         System.out.println&#40;&quot;document id = &quot; + healthcareEntitiesResult.getId&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Document entities: &quot;&#41;;
 *         AtomicInteger ct = new AtomicInteger&#40;&#41;;
 *         healthcareEntitiesResult.getEntities&#40;&#41;.forEach&#40;healthcareEntity -&gt; &#123;
 *             System.out.printf&#40;&quot;&#92;ti = %d, Text: %s, category: %s, confidence score: %f.%n&quot;,
 *                 ct.getAndIncrement&#40;&#41;, healthcareEntity.getText&#40;&#41;, healthcareEntity.getCategory&#40;&#41;,
 *                 healthcareEntity.getConfidenceScore&#40;&#41;&#41;;
 *
 *             IterableStream&lt;EntityDataSource&gt; healthcareEntityDataSources =
 *                 healthcareEntity.getDataSources&#40;&#41;;
 *             if &#40;healthcareEntityDataSources != null&#41; &#123;
 *                 healthcareEntityDataSources.forEach&#40;healthcareEntityLink -&gt; System.out.printf&#40;
 *                     &quot;&#92;t&#92;tEntity ID in data source: %s, data source: %s.%n&quot;,
 *                     healthcareEntityLink.getEntityId&#40;&#41;, healthcareEntityLink.getName&#40;&#41;&#41;&#41;;
 *             &#125;
 *         &#125;&#41;;
 *         &#47;&#47; Healthcare entity relation groups
 *         healthcareEntitiesResult.getEntityRelations&#40;&#41;.forEach&#40;entityRelation -&gt; &#123;
 *             System.out.printf&#40;&quot;&#92;tRelation type: %s.%n&quot;, entityRelation.getRelationType&#40;&#41;&#41;;
 *             entityRelation.getRoles&#40;&#41;.forEach&#40;role -&gt; &#123;
 *                 final HealthcareEntity entity = role.getEntity&#40;&#41;;
 *                 System.out.printf&#40;&quot;&#92;t&#92;tEntity text: %s, category: %s, role: %s.%n&quot;,
 *                     entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, role.getName&#40;&#41;&#41;;
 *             &#125;&#41;;
 *             System.out.printf&#40;&quot;&#92;tRelation confidence score: %f.%n&quot;,
 *                 entityRelation.getConfidenceScore&#40;&#41;&#41;;
 *         &#125;&#41;;
 *     &#125;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeHealthcareEntities#Iterable -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Summarize text-based content: Document Summarization</h2>
 *
 * <p>Text Analytics client can use Natural Language Understanding (NLU) to summarize lengthy documents.
 * For example, extractive or abstractive summarization. Below you can look at the samples on how to use it.</p>
 *
 * <h3>Extractive summarization</h3>
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#beginExtractSummary(java.lang.Iterable) beginExtractSummary}
 * method returns a list of extract summaries for the provided list of {@link java.lang.String document}.</p>
 *
 * <p>This method is supported since service API version
 * {@link com.azure.ai.textanalytics.TextAnalyticsServiceVersion#V2023_04_01}.</p>
 *
 * <!-- src_embed Client.beginExtractSummary#Iterable -->
 * <pre>
 * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
 * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
 *     documents.add&#40;
 *         &quot;At Microsoft, we have been on a quest to advance AI beyond existing techniques, by taking a more holistic,&quot;
 *             + &quot; human-centric approach to learning and understanding. As Chief Technology Officer of Azure AI&quot;
 *             + &quot; Cognitive Services, I have been working with a team of amazing scientists and engineers to turn &quot;
 *             + &quot;this quest into a reality. In my role, I enjoy a unique perspective in viewing the relationship&quot;
 *             + &quot; among three attributes of human cognition: monolingual text &#40;X&#41;, audio or visual sensory signals,&quot;
 *             + &quot; &#40;Y&#41; and multilingual &#40;Z&#41;. At the intersection of all three, there’s magic—what we call XYZ-code&quot;
 *             + &quot; as illustrated in Figure 1—a joint representation to create more powerful AI that can speak, hear,&quot;
 *             + &quot; see, and understand humans better. We believe XYZ-code will enable us to fulfill our long-term&quot;
 *             + &quot; vision: cross-domain transfer learning, spanning modalities and languages. The goal is to have&quot;
 *             + &quot; pretrained models that can jointly learn representations to support a broad range of downstream&quot;
 *             + &quot; AI tasks, much in the way humans do today. Over the past five years, we have achieved human&quot;
 *             + &quot; performance on benchmarks in conversational speech recognition, machine translation, &quot;
 *             + &quot;conversational question answering, machine reading comprehension, and image captioning. These&quot;
 *             + &quot; five breakthroughs provided us with strong signals toward our more ambitious aspiration to&quot;
 *             + &quot; produce a leap in AI capabilities, achieving multisensory and multilingual learning that &quot;
 *             + &quot;is closer in line with how humans learn and understand. I believe the joint XYZ-code is a &quot;
 *             + &quot;foundational component of this aspiration, if grounded with external knowledge sources in &quot;
 *             + &quot;the downstream AI tasks.&quot;&#41;;
 * &#125;
 * SyncPoller&lt;ExtractiveSummaryOperationDetail, ExtractiveSummaryPagedIterable&gt; syncPoller =
 *     textAnalyticsClient.beginExtractSummary&#40;documents&#41;;
 * syncPoller.waitForCompletion&#40;&#41;;
 * syncPoller.getFinalResult&#40;&#41;.forEach&#40;resultCollection -&gt; &#123;
 *     for &#40;ExtractiveSummaryResult documentResult : resultCollection&#41; &#123;
 *         System.out.println&#40;&quot;&#92;tExtracted summary sentences:&quot;&#41;;
 *         for &#40;ExtractiveSummarySentence extractiveSummarySentence : documentResult.getSentences&#40;&#41;&#41; &#123;
 *             System.out.printf&#40;
 *                 &quot;&#92;t&#92;t Sentence text: %s, length: %d, offset: %d, rank score: %f.%n&quot;,
 *                 extractiveSummarySentence.getText&#40;&#41;, extractiveSummarySentence.getLength&#40;&#41;,
 *                 extractiveSummarySentence.getOffset&#40;&#41;, extractiveSummarySentence.getRankScore&#40;&#41;&#41;;
 *         &#125;
 *     &#125;
 * &#125;&#41;;
 * </pre>
 * <!-- end Client.beginExtractSummary#Iterable -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample,
 * refer to {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <h3>Abstractive summarization</h3>
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#beginAbstractSummary(java.lang.Iterable) beginAbstractSummary}
 * method returns  a list of abstractive summary for the provided list of {@link java.lang.String document}.</p>
 *
 * <p>This method is supported since service API version
 * {@link com.azure.ai.textanalytics.TextAnalyticsServiceVersion#V2023_04_01}.</p>
 *
 * <!-- src_embed Client.beginAbstractSummary#Iterable -->
 * <pre>
 * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
 * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
 *     documents.add&#40;
 *         &quot;At Microsoft, we have been on a quest to advance AI beyond existing techniques, by taking a more holistic,&quot;
 *             + &quot; human-centric approach to learning and understanding. As Chief Technology Officer of Azure AI&quot;
 *             + &quot; Cognitive Services, I have been working with a team of amazing scientists and engineers to turn &quot;
 *             + &quot;this quest into a reality. In my role, I enjoy a unique perspective in viewing the relationship&quot;
 *             + &quot; among three attributes of human cognition: monolingual text &#40;X&#41;, audio or visual sensory signals,&quot;
 *             + &quot; &#40;Y&#41; and multilingual &#40;Z&#41;. At the intersection of all three, there’s magic—what we call XYZ-code&quot;
 *             + &quot; as illustrated in Figure 1—a joint representation to create more powerful AI that can speak, hear,&quot;
 *             + &quot; see, and understand humans better. We believe XYZ-code will enable us to fulfill our long-term&quot;
 *             + &quot; vision: cross-domain transfer learning, spanning modalities and languages. The goal is to have&quot;
 *             + &quot; pretrained models that can jointly learn representations to support a broad range of downstream&quot;
 *             + &quot; AI tasks, much in the way humans do today. Over the past five years, we have achieved human&quot;
 *             + &quot; performance on benchmarks in conversational speech recognition, machine translation, &quot;
 *             + &quot;conversational question answering, machine reading comprehension, and image captioning. These&quot;
 *             + &quot; five breakthroughs provided us with strong signals toward our more ambitious aspiration to&quot;
 *             + &quot; produce a leap in AI capabilities, achieving multisensory and multilingual learning that &quot;
 *             + &quot;is closer in line with how humans learn and understand. I believe the joint XYZ-code is a &quot;
 *             + &quot;foundational component of this aspiration, if grounded with external knowledge sources in &quot;
 *             + &quot;the downstream AI tasks.&quot;&#41;;
 * &#125;
 * SyncPoller&lt;AbstractiveSummaryOperationDetail, AbstractiveSummaryPagedIterable&gt; syncPoller =
 *     textAnalyticsClient.beginAbstractSummary&#40;documents&#41;;
 * syncPoller.waitForCompletion&#40;&#41;;
 * syncPoller.getFinalResult&#40;&#41;.forEach&#40;resultCollection -&gt; &#123;
 *     for &#40;AbstractiveSummaryResult documentResult : resultCollection&#41; &#123;
 *         System.out.println&#40;&quot;&#92;tAbstractive summary sentences:&quot;&#41;;
 *         for &#40;AbstractiveSummary summarySentence : documentResult.getSummaries&#40;&#41;&#41; &#123;
 *             System.out.printf&#40;&quot;&#92;t&#92;t Summary text: %s.%n&quot;, summarySentence.getText&#40;&#41;&#41;;
 *             for &#40;AbstractiveSummaryContext abstractiveSummaryContext : summarySentence.getContexts&#40;&#41;&#41; &#123;
 *                 System.out.printf&#40;&quot;&#92;t&#92;t offset: %d, length: %d%n&quot;,
 *                     abstractiveSummaryContext.getOffset&#40;&#41;, abstractiveSummaryContext.getLength&#40;&#41;&#41;;
 *             &#125;
 *         &#125;
 *     &#125;
 * &#125;&#41;;
 * </pre>
 * <!-- end Client.beginAbstractSummary#Iterable -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Classify Text</h2>
 *
 * <p>Text Analytics client can use Natural Language Understanding (NLU) to detect the language or
 * classify the sentiment of text you have. For example, language detection, sentiment analysis, or
 * custom text classification. Below you can look at the samples on how to use it.</p>
 *
 * <h3>Analyze Sentiment and Mine Text for Opinions</h3>
 *
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#analyzeSentiment(java.lang.String, java.lang.String,
 * com.azure.ai.textanalytics.models.AnalyzeSentimentOptions)} analyzeSentiment}
 * method can be used to analyze sentiment on a given input text string, which returns a sentiment prediction,
 * as well as confidence scores for each sentiment label (Positive, Negative, and Neutral) for the document and each
 * sentence within it. If the {@code includeOpinionMining} of
 * {@link com.azure.ai.textanalytics.models.AnalyzeSentimentOptions} set to true, the output will include the opinion
 * mining results. It mines the opinions of a sentence and conducts more granular analysis around the aspects in the
 * text (also known as aspect-based sentiment analysis).</p>
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String-String-AnalyzeSentimentOptions -->
 * <pre>
 * DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment&#40;
 *     &quot;The hotel was dark and unclean.&quot;, &quot;en&quot;,
 *     new AnalyzeSentimentOptions&#40;&#41;.setIncludeOpinionMining&#40;true&#41;&#41;;
 * for &#40;SentenceSentiment sentenceSentiment : documentSentiment.getSentences&#40;&#41;&#41; &#123;
 *     System.out.printf&#40;&quot;&#92;tSentence sentiment: %s%n&quot;, sentenceSentiment.getSentiment&#40;&#41;&#41;;
 *     sentenceSentiment.getOpinions&#40;&#41;.forEach&#40;opinion -&gt; &#123;
 *         TargetSentiment targetSentiment = opinion.getTarget&#40;&#41;;
 *         System.out.printf&#40;&quot;&#92;tTarget sentiment: %s, target text: %s%n&quot;, targetSentiment.getSentiment&#40;&#41;,
 *             targetSentiment.getText&#40;&#41;&#41;;
 *         for &#40;AssessmentSentiment assessmentSentiment : opinion.getAssessments&#40;&#41;&#41; &#123;
 *             System.out.printf&#40;&quot;&#92;t&#92;t'%s' sentiment because of &#92;&quot;%s&#92;&quot;. Is the assessment negated: %s.%n&quot;,
 *                 assessmentSentiment.getSentiment&#40;&#41;, assessmentSentiment.getText&#40;&#41;, assessmentSentiment.isNegated&#40;&#41;&#41;;
 *         &#125;
 *     &#125;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String-String-AnalyzeSentimentOptions -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <h3>Detect Language</h3>
 *
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#detectLanguage(java.lang.String) detectLanguage}
 * method returns the detected language and a confidence score between zero and one. Scores close to one indicate 100%
 * certainty that the identified language is true.</p>
 *
 * This method will use the default country hint that sets up in
 * {@link com.azure.ai.textanalytics.TextAnalyticsClientBuilder#defaultCountryHint(String)}. If none is specified,
 * service will use 'US' as the country hint.
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String -->
 * <pre>
 * DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage&#40;&quot;Bonjour tout le monde&quot;&#41;;
 * System.out.printf&#40;&quot;Detected language name: %s, ISO 6391 name: %s, confidence score: %f.%n&quot;,
 *     detectedLanguage.getName&#40;&#41;, detectedLanguage.getIso6391Name&#40;&#41;, detectedLanguage.getConfidenceScore&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <h3>Single-Label Classification</h3>
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#beginSingleLabelClassify(java.lang.Iterable,
 * java.lang.String, java.lang.String) beginSingleLabelClassify}
 * beginSingleLabelClassify} method returns a list of single-label classification for the provided list of
 * {@link java.lang.String document}.</p>
 *
 * <p><strong>Note:</strong> this method is supported since service API version
 * {@link com.azure.ai.textanalytics.TextAnalyticsServiceVersion#V2022_05_01}.</p>
 *
 * <!-- src_embed Client.beginSingleLabelClassify#Iterable-String-String -->
 * <pre>
 * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
 * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
 *     documents.add&#40;
 *         &quot;A recent report by the Government Accountability Office &#40;GAO&#41; found that the dramatic increase &quot;
 *             + &quot;in oil and natural gas development on federal lands over the past six years has stretched the&quot;
 *             + &quot; staff of the BLM to a point that it has been unable to meet its environmental protection &quot;
 *             + &quot;responsibilities.&quot;
 *     &#41;;
 * &#125;
 * &#47;&#47; See the service documentation for regional support and how to train a model to classify your documents,
 * &#47;&#47; see https:&#47;&#47;aka.ms&#47;azsdk&#47;textanalytics&#47;customfunctionalities
 * SyncPoller&lt;ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable&gt; syncPoller =
 *     textAnalyticsClient.beginSingleLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;&#41;;
 * syncPoller.waitForCompletion&#40;&#41;;
 * syncPoller.getFinalResult&#40;&#41;.forEach&#40;documentsResults -&gt; &#123;
 *     System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
 *         documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
 *     for &#40;ClassifyDocumentResult documentResult : documentsResults&#41; &#123;
 *         System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
 *         for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
 *             System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
 *                 classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
 *         &#125;
 *     &#125;
 * &#125;&#41;;
 * </pre>
 * <!-- end Client.beginSingleLabelClassify#Iterable-String-String -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <h3>Multi-Label Classification</h3>
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#beginMultiLabelClassify(java.lang.Iterable,
 * java.lang.String, java.lang.String) beginMultiLabelClassify}  method returns a list of multi-label classification
 * for the provided list of {@link java.lang.String document}.</p>
 *
 * <p><strong>Note:</strong> this method is supported since service API version
 * {@link com.azure.ai.textanalytics.TextAnalyticsServiceVersion#V2022_05_01}.</p>
 *
 * <!-- src_embed Client.beginMultiLabelClassify#Iterable-String-String -->
 * <pre>
 * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
 * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
 *     documents.add&#40;
 *         &quot;I need a reservation for an indoor restaurant in China. Please don't stop the music.&quot;
 *             + &quot; Play music and add it to my playlist&quot;&#41;;
 * &#125;
 * SyncPoller&lt;ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable&gt; syncPoller =
 *     textAnalyticsClient.beginMultiLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;&#41;;
 * syncPoller.waitForCompletion&#40;&#41;;
 * syncPoller.getFinalResult&#40;&#41;.forEach&#40;documentsResults -&gt; &#123;
 *     System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
 *         documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
 *     for &#40;ClassifyDocumentResult documentResult : documentsResults&#41; &#123;
 *         System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
 *         for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
 *             System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
 *                 classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
 *         &#125;
 *     &#125;
 * &#125;&#41;;
 * </pre>
 * <!-- end Client.beginMultiLabelClassify#Iterable-String-String -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Execute multiple actions</h2>
 * <p>The {@link com.azure.ai.textanalytics.TextAnalyticsClient#beginAnalyzeActions(java.lang.Iterable,
 * com.azure.ai.textanalytics.models.TextAnalyticsActions) beginAnalyzeActions} method execute actions, such as,
 * entities recognition, PII entities recognition, key phrases extraction, and etc, for a list of
 * {@link java.lang.String documents}.</p>
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions -->
 * <pre>
 * List&lt;String&gt; documents = Arrays.asList&#40;
 *     &quot;Elon Musk is the CEO of SpaceX and Tesla.&quot;,
 *     &quot;My SSN is 859-98-0987&quot;
 * &#41;;
 *
 * SyncPoller&lt;AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable&gt; syncPoller =
 *     textAnalyticsClient.beginAnalyzeActions&#40;
 *         documents,
 *         new TextAnalyticsActions&#40;&#41;.setDisplayName&#40;&quot;&#123;tasks_display_name&#125;&quot;&#41;
 *             .setRecognizeEntitiesActions&#40;new RecognizeEntitiesAction&#40;&#41;&#41;
 *             .setExtractKeyPhrasesActions&#40;new ExtractKeyPhrasesAction&#40;&#41;&#41;&#41;;
 * syncPoller.waitForCompletion&#40;&#41;;
 * AnalyzeActionsResultPagedIterable result = syncPoller.getFinalResult&#40;&#41;;
 * result.forEach&#40;analyzeActionsResult -&gt; &#123;
 *     System.out.println&#40;&quot;Entities recognition action results:&quot;&#41;;
 *     analyzeActionsResult.getRecognizeEntitiesResults&#40;&#41;.forEach&#40;
 *         actionResult -&gt; &#123;
 *             if &#40;!actionResult.isError&#40;&#41;&#41; &#123;
 *                 actionResult.getDocumentsResults&#40;&#41;.forEach&#40;
 *                     entitiesResult -&gt; entitiesResult.getEntities&#40;&#41;.forEach&#40;
 *                         entity -&gt; System.out.printf&#40;
 *                             &quot;Recognized entity: %s, entity category: %s, entity subcategory: %s,&quot;
 *                                 + &quot; confidence score: %f.%n&quot;,
 *                             entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;,
 *                             entity.getConfidenceScore&#40;&#41;&#41;&#41;&#41;;
 *             &#125;
 *         &#125;&#41;;
 *     System.out.println&#40;&quot;Key phrases extraction action results:&quot;&#41;;
 *     analyzeActionsResult.getExtractKeyPhrasesResults&#40;&#41;.forEach&#40;
 *         actionResult -&gt; &#123;
 *             if &#40;!actionResult.isError&#40;&#41;&#41; &#123;
 *                 actionResult.getDocumentsResults&#40;&#41;.forEach&#40;extractKeyPhraseResult -&gt; &#123;
 *                     System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
 *                     extractKeyPhraseResult.getKeyPhrases&#40;&#41;
 *                         .forEach&#40;keyPhrases -&gt; System.out.printf&#40;&quot;&#92;t%s.%n&quot;, keyPhrases&#41;&#41;;
 *                 &#125;&#41;;
 *             &#125;
 *         &#125;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions -->
 *
 * <p>See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.</p>
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to
 * {@link com.azure.ai.textanalytics.TextAnalyticsAsyncClient}.</p>
 *
 * @see TextAnalyticsClientBuilder
 * @see TextAnalyticsAsyncClient
 * @see TextAnalyticsClient
 */
package com.azure.ai.textanalytics;
