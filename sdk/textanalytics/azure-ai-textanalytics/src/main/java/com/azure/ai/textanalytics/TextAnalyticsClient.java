// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.models.AbstractSummaryOperationDetail;
import com.azure.ai.textanalytics.models.AbstractSummaryOptions;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesAction;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntity;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.ClassifyDocumentOperationDetail;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.DynamicClassificationOptions;
import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.ExtractSummaryOperationDetail;
import com.azure.ai.textanalytics.models.ExtractSummaryOptions;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.LinkedEntity;
import com.azure.ai.textanalytics.models.LinkedEntityCollection;
import com.azure.ai.textanalytics.models.MultiLabelClassifyAction;
import com.azure.ai.textanalytics.models.MultiLabelClassifyOptions;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOptions;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.SingleLabelClassifyAction;
import com.azure.ai.textanalytics.models.SingleLabelClassifyOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AbstractSummaryPagedIterable;
import com.azure.ai.textanalytics.util.AbstractSummaryResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedIterable;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedIterable;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.util.ClassifyDocumentPagedIterable;
import com.azure.ai.textanalytics.util.ClassifyDocumentResultCollection;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.util.DynamicClassifyDocumentResultCollection;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.ExtractSummaryPagedIterable;
import com.azure.ai.textanalytics.util.ExtractSummaryResultCollection;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesPagedIterable;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.SyncPoller;

import java.util.Collections;
import java.util.Objects;

import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapByIndex;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsException;

/**
 * This class provides a synchronous client that contains all the operations that apply to Azure Text Analytics.
 * Operations allowed by the client are language detection, entities recognition, linked entities recognition,
 * key phrases extraction, and sentiment analysis of a document or a list of documents.
 *
 * <p><strong>Instantiating a synchronous Text Analytics Client</strong></p>
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions -->
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
 *             .setExtractKeyPhrasesActions&#40;new ExtractKeyPhrasesAction&#40;&#41;&#41;,
 *         &quot;en&quot;,
 *         new AnalyzeActionsOptions&#40;&#41;.setIncludeStatistics&#40;false&#41;&#41;;
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
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions -->
 * <p>View {@link TextAnalyticsClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see TextAnalyticsClientBuilder
 */
@ServiceClient(builder = TextAnalyticsClientBuilder.class)
public final class TextAnalyticsClient {
    private static final ClientLogger LOGGER = new ClientLogger(TextAnalyticsClient.class);

    private final TextAnalyticsAsyncClient client;

    /**
     * Creates a {@code TextAnalyticsClient client} that sends requests to the Text Analytics service's endpoint.
     * Each service call goes through the {@link TextAnalyticsClientBuilder#pipeline http pipeline}.
     *
     * @param client The {@link TextAnalyticsClient} that the client routes its request through.
     */
    TextAnalyticsClient(TextAnalyticsAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets default country hint code.
     *
     * @return The default country hint code
     */
    public String getDefaultCountryHint() {
        return client.getDefaultCountryHint();
    }

    /**
     * Gets default language when the builder is setup.
     *
     * @return The default language
     */
    public String getDefaultLanguage() {
        return client.getDefaultLanguage();
    }

    /**
     * Returns the detected language and a confidence score between zero and one. Scores close to one indicate 100%
     * certainty that the identified language is true.
     *
     * This method will use the default country hint that sets up in
     * {@link TextAnalyticsClientBuilder#defaultCountryHint(String)}. If none is specified, service will use 'US' as
     * the country hint.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the language of single document.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String -->
     * <pre>
     * DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage&#40;&quot;Bonjour tout le monde&quot;&#41;;
     * System.out.printf&#40;&quot;Detected language name: %s, ISO 6391 name: %s, confidence score: %f.%n&quot;,
     *     detectedLanguage.getName&#40;&#41;, detectedLanguage.getIso6391Name&#40;&#41;, detectedLanguage.getConfidenceScore&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return The {@link DetectedLanguage detected language} of the document.
     *
     * @throws NullPointerException if {@code document} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectedLanguage detectLanguage(String document) {
        return detectLanguage(document, client.getDefaultCountryHint());
    }

    /**
     * Returns the detected language and a confidence score between zero and one.
     * Scores close to one indicate 100% certainty that the identified language is true.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the language of documents with a provided country hint.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String-String -->
     * <pre>
     * DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage&#40;
     *     &quot;This text is in English&quot;, &quot;US&quot;&#41;;
     * System.out.printf&#40;&quot;Detected language name: %s, ISO 6391 name: %s, confidence score: %f.%n&quot;,
     *     detectedLanguage.getName&#40;&#41;, detectedLanguage.getIso6391Name&#40;&#41;, detectedLanguage.getConfidenceScore&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguage#String-String -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified. To remove this behavior you can reset this parameter by setting this value to empty string
     * {@code countryHint} = "" or "none".
     *
     * @return The {@link DetectedLanguage detected language} of the document.
     *
     * @throws NullPointerException if {@code document} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectedLanguage detectLanguage(String document, String countryHint) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        DetectLanguageResultCollection resultCollection =
            detectLanguageBatch(Collections.singletonList(document), countryHint, null);
        DetectedLanguage detectedLanguage = null;
        for (DetectLanguageResult detectLanguageResult : resultCollection) {
            if (detectLanguageResult.isError()) {
                throw LOGGER.logExceptionAsError(toTextAnalyticsException(detectLanguageResult.getError()));
            }
            detectedLanguage = detectLanguageResult.getPrimaryLanguage();
        }
        // When the detected language result collection is empty,
        // return empty result for the empty collection returned by the service.
        return detectedLanguage;
    }

    /**
     * Detects Language for a batch of document with the provided country hint and request options.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the language in a list of documents with a provided country hint and request options.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;This is written in English&quot;,
     *     &quot;Este es un documento  escrito en Español.&quot;
     * &#41;;
     *
     * DetectLanguageResultCollection resultCollection =
     *     textAnalyticsClient.detectLanguageBatch&#40;documents, &quot;US&quot;, null&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;&quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * &#47;&#47; Batch result of languages
     * resultCollection.forEach&#40;detectLanguageResult -&gt; &#123;
     *     System.out.printf&#40;&quot;Document ID: %s%n&quot;, detectLanguageResult.getId&#40;&#41;&#41;;
     *     DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage&#40;&#41;;
     *     System.out.printf&#40;&quot;Primary language name: %s, ISO 6391 name: %s, confidence score: %f.%n&quot;,
     *         detectedLanguage.getName&#40;&#41;, detectedLanguage.getIso6391Name&#40;&#41;,
     *         detectedLanguage.getConfidenceScore&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions -->
     *
     * @param documents The list of documents to detect languages for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param countryHint Accepts two letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified. To remove this behavior you can reset this parameter by setting this value to empty string
     * {@code countryHint} = "" or "none".
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link DetectLanguageResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in service
     *   API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for API
     *   version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DetectLanguageResultCollection detectLanguageBatch(
        Iterable<String> documents, String countryHint, TextAnalyticsRequestOptions options) {
        inputDocumentsValidation(documents);
        return detectLanguageBatchWithResponse(mapByIndex(documents,
            (index, value) -> new DetectLanguageInput(index, value, countryHint)), options, Context.NONE).getValue();
    }

    /**
     * Detects Language for a batch of {@link DetectLanguageInput document} with provided request options.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Detects the languages with http response in a list of {@link DetectLanguageInput document} with provided
     * request options.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions-Context -->
     * <pre>
     * List&lt;DetectLanguageInput&gt; detectLanguageInputs = Arrays.asList&#40;
     *     new DetectLanguageInput&#40;&quot;1&quot;, &quot;This is written in English.&quot;, &quot;US&quot;&#41;,
     *     new DetectLanguageInput&#40;&quot;2&quot;, &quot;Este es un documento  escrito en Español.&quot;, &quot;es&quot;&#41;
     * &#41;;
     *
     * Response&lt;DetectLanguageResultCollection&gt; response =
     *     textAnalyticsClient.detectLanguageBatchWithResponse&#40;detectLanguageInputs,
     *         new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;, Context.NONE&#41;;
     *
     * &#47;&#47; Response's status code
     * System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * DetectLanguageResultCollection detectedLanguageResultCollection = response.getValue&#40;&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = detectedLanguageResultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;
     *     &quot;Documents statistics: document count = %d, erroneous document count = %d, transaction count = %d,&quot;
     *         + &quot; valid document count = %d.%n&quot;,
     *     batchStatistics.getDocumentCount&#40;&#41;, batchStatistics.getInvalidDocumentCount&#40;&#41;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * &#47;&#47; Batch result of languages
     * detectedLanguageResultCollection.forEach&#40;detectLanguageResult -&gt; &#123;
     *     System.out.printf&#40;&quot;Document ID: %s%n&quot;, detectLanguageResult.getId&#40;&#41;&#41;;
     *     DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage&#40;&#41;;
     *     System.out.printf&#40;&quot;Primary language name: %s, ISO 6391 name: %s, confidence score: %f.%n&quot;,
     *         detectedLanguage.getName&#40;&#41;, detectedLanguage.getIso6391Name&#40;&#41;,
     *         detectedLanguage.getConfidenceScore&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions-Context -->
     *
     * @param documents The list of {@link DetectLanguageInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains a {@link DetectLanguageResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in service
     *   API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for API
     *   version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DetectLanguageResultCollection> detectLanguageBatchWithResponse(
        Iterable<DetectLanguageInput> documents, TextAnalyticsRequestOptions options, Context context) {
        inputDocumentsValidation(documents);
        return client.detectLanguageUtilClient.getDetectedLanguageResponseSync(documents, options, context);
    }

    // Categorized Entity
    /**
     * Returns a list of general categorized entities in the provided document.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/taner">this</a>
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognize the entities of documents</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String -->
     * <pre>
     * final CategorizedEntityCollection recognizeEntitiesResult =
     *     textAnalyticsClient.recognizeEntities&#40;&quot;Satya Nadella is the CEO of Microsoft&quot;&#41;;
     * for &#40;CategorizedEntity entity : recognizeEntitiesResult&#41; &#123;
     *     System.out.printf&#40;&quot;Recognized entity: %s, entity category: %s, confidence score: %f.%n&quot;,
     *         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String -->
     *
     * @param document The document to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link CategorizedEntityCollection} contains a list of
     * {@link CategorizedEntity recognized categorized entities} and warnings.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CategorizedEntityCollection recognizeEntities(String document) {
        return recognizeEntities(document, client.getDefaultLanguage());
    }

    /**
     * Returns a list of general categorized entities in the provided document with provided language code.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/taner">this</a>
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities in a document with a provided language code.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String-String -->
     * <pre>
     * final CategorizedEntityCollection recognizeEntitiesResult =
     *     textAnalyticsClient.recognizeEntities&#40;&quot;Satya Nadella is the CEO of Microsoft&quot;, &quot;en&quot;&#41;;
     *
     * for &#40;CategorizedEntity entity : recognizeEntitiesResult&#41; &#123;
     *     System.out.printf&#40;&quot;Recognized entity: %s, entity category: %s, confidence score: %f.%n&quot;,
     *         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntities#String-String -->
     *
     * @param document The document to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     *
     * @return The {@link CategorizedEntityCollection} contains a list of
     * {@link CategorizedEntity recognized categorized entities} and warnings.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CategorizedEntityCollection recognizeEntities(String document, String language) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        final RecognizeEntitiesResultCollection recognizeEntitiesResultCollectionMono =
            recognizeEntitiesBatch(Collections.singletonList(document), language, null);
        CategorizedEntityCollection entityCollection = null;
        // for each loop will have only one entry inside
        for (RecognizeEntitiesResult entitiesResult : recognizeEntitiesResultCollectionMono) {
            if (entitiesResult.isError()) {
                throw LOGGER.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
            }
            entityCollection = new CategorizedEntityCollection(entitiesResult.getEntities(),
                entitiesResult.getEntities().getWarnings());
        }
        return entityCollection;
    }

    /**
     * Returns a list of general categorized entities for the provided list of documents with provided language code
     * and request options.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities in a list of documents with a provided language code and request options.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;I had a wonderful trip to Seattle last week.&quot;,
     *     &quot;I work at Microsoft.&quot;&#41;;
     *
     * RecognizeEntitiesResultCollection resultCollection =
     *     textAnalyticsClient.recognizeEntitiesBatch&#40;documents, &quot;en&quot;, null&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;
     *     &quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * resultCollection.forEach&#40;recognizeEntitiesResult -&gt;
     *     recognizeEntitiesResult.getEntities&#40;&#41;.forEach&#40;entity -&gt;
     *         System.out.printf&#40;&quot;Recognized entity: %s, entity category: %s, confidence score: %f.%n&quot;,
     *             entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of documents to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link RecognizeEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in
     *   service API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RecognizeEntitiesResultCollection recognizeEntitiesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        inputDocumentsValidation(documents);
        return recognizeEntitiesBatchWithResponse(mapByIndex(documents, (index, value) -> {
            final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
            textDocumentInput.setLanguage(language);
            return textDocumentInput;
        }), options, Context.NONE).getValue();
    }

    /**
     * Returns a list of general categorized entities for the provided list of {@link TextDocumentInput document} with
     * provided request options.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the entities with http response in a list of {@link TextDocumentInput document} with provided
     * request options.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;0&quot;, &quot;I had a wonderful trip to Seattle last week.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;I work at Microsoft.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;
     * &#41;;
     *
     * Response&lt;RecognizeEntitiesResultCollection&gt; response =
     *     textAnalyticsClient.recognizeEntitiesBatchWithResponse&#40;textDocumentInputs,
     *         new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;, Context.NONE&#41;;
     *
     * &#47;&#47; Response's status code
     * System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * RecognizeEntitiesResultCollection recognizeEntitiesResultCollection = response.getValue&#40;&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = recognizeEntitiesResultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;
     *     &quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * recognizeEntitiesResultCollection.forEach&#40;recognizeEntitiesResult -&gt;
     *     recognizeEntitiesResult.getEntities&#40;&#41;.forEach&#40;entity -&gt;
     *         System.out.printf&#40;&quot;Recognized entity: %s, entity category: %s, confidence score: %f.%n&quot;,
     *             entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizeEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains a {@link RecognizeEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in service
     *   API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for API
     *   version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RecognizeEntitiesResultCollection> recognizeEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        inputDocumentsValidation(documents);
        return client.recognizeEntityUtilClient.getRecognizedEntitiesResponseSync(documents, options, context);
    }

    // PII Entity
    /**
     * Returns a list of Personally Identifiable Information(PII) entities in the provided document.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/azsdk/language/pii">this</a>
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>. This method will use the
     * default language that is set using {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is
     * specified, service will use 'en' as the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognize the PII entities details in a document.</p>
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
     * @param document The document to recognize PII entities details for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link PiiEntityCollection recognized PII entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     * @throws UnsupportedOperationException if {@code recognizePiiEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code recognizePiiEntities} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PiiEntityCollection recognizePiiEntities(String document) {
        return recognizePiiEntities(document, client.getDefaultLanguage());
    }

    /**
     * Returns a list of Personally Identifiable Information(PII) entities in the provided document
     * with provided language code.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/azsdk/language/pii">this</a>
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the PII entities details in a document with a provided language code.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String -->
     * <pre>
     * PiiEntityCollection piiEntityCollection = textAnalyticsClient.recognizePiiEntities&#40;
     *     &quot;My SSN is 859-98-0987&quot;, &quot;en&quot;&#41;;
     * System.out.printf&#40;&quot;Redacted Text: %s%n&quot;, piiEntityCollection.getRedactedText&#40;&#41;&#41;;
     * piiEntityCollection.forEach&#40;entity -&gt; System.out.printf&#40;
     *         &quot;Recognized Personally Identifiable Information entity: %s, entity category: %s,&quot;
     *             + &quot; entity subcategory: %s, confidence score: %f.%n&quot;,
     *         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String -->
     *
     * @param document The document to recognize PII entities details for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     *
     * @return The {@link PiiEntityCollection recognized PII entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     * @throws UnsupportedOperationException if {@code recognizePiiEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code recognizePiiEntities} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PiiEntityCollection recognizePiiEntities(String document, String language) {
        return recognizePiiEntities(document, language, null);
    }

    /**
     * Returns a list of Personally Identifiable Information(PII) entities in the provided document
     * with provided language code.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/azsdk/language/pii">this</a>
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the PII entities details in a document with a provided language code and
     * {@link RecognizePiiEntitiesOptions}.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String-RecognizePiiEntitiesOptions -->
     * <pre>
     * PiiEntityCollection piiEntityCollection = textAnalyticsClient.recognizePiiEntities&#40;
     *     &quot;My SSN is 859-98-0987&quot;, &quot;en&quot;,
     *     new RecognizePiiEntitiesOptions&#40;&#41;.setDomainFilter&#40;PiiEntityDomain.PROTECTED_HEALTH_INFORMATION&#41;&#41;;
     * System.out.printf&#40;&quot;Redacted Text: %s%n&quot;, piiEntityCollection.getRedactedText&#40;&#41;&#41;;
     * piiEntityCollection.forEach&#40;entity -&gt; System.out.printf&#40;
     *     &quot;Recognized Personally Identifiable Information entity: %s, entity category: %s,&quot;
     *         + &quot; entity subcategory: %s, confidence score: %f.%n&quot;,
     *     entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntities#String-String-RecognizePiiEntitiesOptions -->
     *
     * @param document The document to recognize PII entities details for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return The {@link PiiEntityCollection recognized PII entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     * @throws UnsupportedOperationException if {@code recognizePiiEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code recognizePiiEntities} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PiiEntityCollection recognizePiiEntities(String document, String language,
        RecognizePiiEntitiesOptions options) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        final RecognizePiiEntitiesResultCollection recognizePiiEntitiesResults =
            recognizePiiEntitiesBatch(Collections.singletonList(document), language, options);
        PiiEntityCollection entityCollection = null;
        // for each loop will have only one entry inside
        for (RecognizePiiEntitiesResult entitiesResult : recognizePiiEntitiesResults) {
            if (entitiesResult.isError()) {
                throw LOGGER.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
            }
            entityCollection = new PiiEntityCollection(entitiesResult.getEntities(),
                entitiesResult.getEntities().getRedactedText(),
                entitiesResult.getEntities().getWarnings());
        }
        return entityCollection;
    }

    /**
     * Returns a list of Personally Identifiable Information(PII) entities for the provided list of documents with
     * provided language code and request options.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the PII entities details in a list of documents with a provided language code
     * and request options.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable-String-RecognizePiiEntitiesOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;My SSN is 859-98-0987&quot;,
     *     &quot;Visa card 4111 1111 1111 1111&quot;
     * &#41;;
     *
     * RecognizePiiEntitiesResultCollection resultCollection = textAnalyticsClient.recognizePiiEntitiesBatch&#40;
     *     documents, &quot;en&quot;, new RecognizePiiEntitiesOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;&quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * resultCollection.forEach&#40;recognizePiiEntitiesResult -&gt; &#123;
     *     PiiEntityCollection piiEntityCollection = recognizePiiEntitiesResult.getEntities&#40;&#41;;
     *     System.out.printf&#40;&quot;Redacted Text: %s%n&quot;, piiEntityCollection.getRedactedText&#40;&#41;&#41;;
     *     piiEntityCollection.forEach&#40;entity -&gt; System.out.printf&#40;
     *         &quot;Recognized Personally Identifiable Information entity: %s, entity category: %s,&quot;
     *             + &quot; entity subcategory: %s, confidence score: %f.%n&quot;,
     *         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable-String-RecognizePiiEntitiesOptions -->
     *
     * @param documents A list of documents to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return A {@link RecognizePiiEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code recognizePiiEntitiesBatch} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code recognizePiiEntitiesBatch} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RecognizePiiEntitiesResultCollection recognizePiiEntitiesBatch(
        Iterable<String> documents, String language, RecognizePiiEntitiesOptions options) {
        return recognizePiiEntitiesBatchWithResponse(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options, Context.NONE).getValue();
    }

    /**
     * Returns a list of Personally Identifiable Information(PII) entities for the provided list of
     * {@link TextDocumentInput document} with provided request options.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the PII entities details with http response in a list of {@link TextDocumentInput document}
     * with provided request options.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable-RecognizePiiEntitiesOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;0&quot;, &quot;My SSN is 859-98-0987&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;Visa card 4111 1111 1111 1111&quot;&#41;
     * &#41;;
     *
     * Response&lt;RecognizePiiEntitiesResultCollection&gt; response =
     *     textAnalyticsClient.recognizePiiEntitiesBatchWithResponse&#40;textDocumentInputs,
     *         new RecognizePiiEntitiesOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;, Context.NONE&#41;;
     *
     * RecognizePiiEntitiesResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;&quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * resultCollection.forEach&#40;recognizePiiEntitiesResult -&gt; &#123;
     *     PiiEntityCollection piiEntityCollection = recognizePiiEntitiesResult.getEntities&#40;&#41;;
     *     System.out.printf&#40;&quot;Redacted Text: %s%n&quot;, piiEntityCollection.getRedactedText&#40;&#41;&#41;;
     *     piiEntityCollection.forEach&#40;entity -&gt; System.out.printf&#40;
     *         &quot;Recognized Personally Identifiable Information entity: %s, entity category: %s,&quot;
     *             + &quot; entity subcategory: %s, confidence score: %f.%n&quot;,
     *         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizePiiEntitiesBatch#Iterable-RecognizePiiEntitiesOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains a {@link RecognizePiiEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code recognizePiiEntitiesBatchWithResponse} is called with service API
     *   version {@link TextAnalyticsServiceVersion#V3_0}. {@code recognizePiiEntitiesBatchWithResponse} is only
     *   available for API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RecognizePiiEntitiesResultCollection> recognizePiiEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> documents, RecognizePiiEntitiesOptions options, Context context) {
        return client.recognizePiiEntityUtilClient.getRecognizePiiEntitiesResponseSync(documents, options,
            context);
    }

    // Linked Entities
    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided document.
     * See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognize the linked entities of documents</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String -->
     * <pre>
     * final String document = &quot;Old Faithful is a geyser at Yellowstone Park.&quot;;
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
     * @param document The document to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link LinkedEntityCollection} contains a list of {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LinkedEntityCollection recognizeLinkedEntities(String document) {
        return recognizeLinkedEntities(document, client.getDefaultLanguage());
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided document with
     * language code.
     *
     * See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities in a document with a provided language code.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String-String -->
     * <pre>
     * String document = &quot;Old Faithful is a geyser at Yellowstone Park.&quot;;
     * textAnalyticsClient.recognizeLinkedEntities&#40;document, &quot;en&quot;&#41;.forEach&#40;linkedEntity -&gt; &#123;
     *     System.out.printf&#40;&quot;Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n&quot;,
     *         linkedEntity.getName&#40;&#41;, linkedEntity.getDataSourceEntityId&#40;&#41;, linkedEntity.getUrl&#40;&#41;,
     *         linkedEntity.getDataSource&#40;&#41;&#41;;
     *     linkedEntity.getMatches&#40;&#41;.forEach&#40;entityMatch -&gt; System.out.printf&#40;
     *         &quot;Matched entity: %s, confidence score: %f.%n&quot;,
     *         entityMatch.getText&#40;&#41;, entityMatch.getConfidenceScore&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntities#String-String -->
     *
     * @param document The document to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link LinkedEntityCollection} contains a list of {@link LinkedEntity recognized linked entities}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LinkedEntityCollection recognizeLinkedEntities(String document, String language) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        final RecognizeLinkedEntitiesResultCollection resultCollectionResponse =
            recognizeLinkedEntitiesBatch(Collections.singletonList(document), language, null);
        LinkedEntityCollection linkedEntityCollection = null;
        // for each loop will have only one entry inside
        for (RecognizeLinkedEntitiesResult entitiesResult : resultCollectionResponse) {
            if (entitiesResult.isError()) {
                throw LOGGER.logExceptionAsError(toTextAnalyticsException(entitiesResult.getError()));
            }
            linkedEntityCollection = new LinkedEntityCollection(entitiesResult.getEntities(),
                entitiesResult.getEntities().getWarnings());
        }
        return linkedEntityCollection;
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of documents with
     * provided language code and request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities in a list of documents with a provided language code and request options.
     * </p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;Old Faithful is a geyser at Yellowstone Park.&quot;,
     *     &quot;Mount Shasta has lenticular clouds.&quot;
     * &#41;;
     *
     * RecognizeLinkedEntitiesResultCollection resultCollection =
     *     textAnalyticsClient.recognizeLinkedEntitiesBatch&#40;documents, &quot;en&quot;, null&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;&quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * resultCollection.forEach&#40;recognizeLinkedEntitiesResult -&gt;
     *     recognizeLinkedEntitiesResult.getEntities&#40;&#41;.forEach&#40;linkedEntity -&gt; &#123;
     *         System.out.println&#40;&quot;Linked Entities:&quot;&#41;;
     *         System.out.printf&#40;&quot;Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n&quot;,
     *             linkedEntity.getName&#40;&#41;, linkedEntity.getDataSourceEntityId&#40;&#41;, linkedEntity.getUrl&#40;&#41;,
     *             linkedEntity.getDataSource&#40;&#41;&#41;;
     *         linkedEntity.getMatches&#40;&#41;.forEach&#40;entityMatch -&gt; System.out.printf&#40;
     *             &quot;Matched entity: %s, confidence score: %f.%n&quot;,
     *             entityMatch.getText&#40;&#41;, entityMatch.getConfidenceScore&#40;&#41;&#41;&#41;;
     *     &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of documents to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in
     *   service API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RecognizeLinkedEntitiesResultCollection recognizeLinkedEntitiesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        inputDocumentsValidation(documents);
        return recognizeLinkedEntitiesBatchWithResponse(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options, Context.NONE).getValue();
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of
     * {@link TextDocumentInput document} and request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recognizes the linked entities with http response in a list of {@link TextDocumentInput} with request options.
     * </p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;Old Faithful is a geyser at Yellowstone Park.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;2&quot;, &quot;Mount Shasta has lenticular clouds.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;
     * &#41;;
     *
     * Response&lt;RecognizeLinkedEntitiesResultCollection&gt; response =
     *     textAnalyticsClient.recognizeLinkedEntitiesBatchWithResponse&#40;textDocumentInputs,
     *         new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;, Context.NONE&#41;;
     *
     * &#47;&#47; Response's status code
     * System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * RecognizeLinkedEntitiesResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;
     *     &quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * resultCollection.forEach&#40;recognizeLinkedEntitiesResult -&gt;
     *     recognizeLinkedEntitiesResult.getEntities&#40;&#41;.forEach&#40;linkedEntity -&gt; &#123;
     *         System.out.println&#40;&quot;Linked Entities:&quot;&#41;;
     *         System.out.printf&#40;&quot;Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n&quot;,
     *             linkedEntity.getName&#40;&#41;, linkedEntity.getDataSourceEntityId&#40;&#41;, linkedEntity.getUrl&#40;&#41;,
     *             linkedEntity.getDataSource&#40;&#41;&#41;;
     *         linkedEntity.getMatches&#40;&#41;.forEach&#40;entityMatch -&gt; System.out.printf&#40;
     *             &quot;Matched entity: %s, confidence score: %.2f.%n&quot;,
     *             entityMatch.getText&#40;&#41;, entityMatch.getConfidenceScore&#40;&#41;&#41;&#41;;
     *     &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains a {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in
     *   service API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RecognizeLinkedEntitiesResultCollection> recognizeLinkedEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        inputDocumentsValidation(documents);
        return client.recognizeLinkedEntityUtilClient.getRecognizedLinkedEntitiesResponseSync(documents,
            options, context);
    }

    // Key Phrase
    /**
     * Returns a list of strings denoting the key phrases in the document.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases of documents</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String -->
     * <pre>
     * System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     * for &#40;String keyPhrase : textAnalyticsClient.extractKeyPhrases&#40;&quot;My cat might need to see a veterinarian.&quot;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;%s.%n&quot;, keyPhrase&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link KeyPhrasesCollection} contains a list of extracted key phrases.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyPhrasesCollection extractKeyPhrases(String document) {
        return extractKeyPhrases(document, client.getDefaultLanguage());
    }

    /**
     * Returns a list of strings denoting the key phrases in the document.
     * See <a href="https://aka.ms/talangs">this</a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases in a document with a provided language representation.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String-String-Context -->
     * <pre>
     * System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     * textAnalyticsClient.extractKeyPhrases&#40;&quot;My cat might need to see a veterinarian.&quot;, &quot;en&quot;&#41;
     *     .forEach&#40;kegPhrase -&gt; System.out.printf&#40;&quot;%s.%n&quot;, kegPhrase&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrases#String-String-Context -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link KeyPhrasesCollection} contains a list of extracted key phrases.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyPhrasesCollection extractKeyPhrases(String document, String language) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        final ExtractKeyPhrasesResultCollection resultCollectionResponse =
            extractKeyPhrasesBatch(Collections.singletonList(document), language, null);
        KeyPhrasesCollection keyPhrasesCollection = null;
        // for each loop will have only one entry inside
        for (ExtractKeyPhraseResult keyPhraseResult : resultCollectionResponse) {
            if (keyPhraseResult.isError()) {
                throw LOGGER.logExceptionAsError(toTextAnalyticsException(keyPhraseResult.getError()));
            }
            keyPhrasesCollection = new KeyPhrasesCollection(keyPhraseResult.getKeyPhrases(),
                keyPhraseResult.getKeyPhrases().getWarnings());
        }
        return keyPhrasesCollection;
    }

    /**
     * Returns a list of strings denoting the key phrases in the documents with provided language code and
     * request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases in a list of documents with a provided language code and request options.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;My cat might need to see a veterinarian.&quot;,
     *     &quot;The pitot tube is used to measure airspeed.&quot;
     * &#41;;
     *
     * &#47;&#47; Extracting batch key phrases
     * ExtractKeyPhrasesResultCollection resultCollection =
     *     textAnalyticsClient.extractKeyPhrasesBatch&#40;documents, &quot;en&quot;, null&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;
     *     &quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * &#47;&#47; Extracted key phrase for each of documents from a batch of documents
     * resultCollection.forEach&#40;extractKeyPhraseResult -&gt; &#123;
     *     System.out.printf&#40;&quot;Document ID: %s%n&quot;, extractKeyPhraseResult.getId&#40;&#41;&#41;;
     *     &#47;&#47; Valid document
     *     System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     *     extractKeyPhraseResult.getKeyPhrases&#40;&#41;.forEach&#40;keyPhrase -&gt; System.out.printf&#40;&quot;%s.%n&quot;, keyPhrase&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link ExtractKeyPhrasesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in
     *   service API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ExtractKeyPhrasesResultCollection extractKeyPhrasesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        inputDocumentsValidation(documents);
        return extractKeyPhrasesBatchWithResponse(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options, Context.NONE).getValue();
    }

    /**
     * Returns a list of strings denoting the key phrases in the a batch of {@link TextDocumentInput document} with
     * request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> for the list of enabled languages.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Extracts key phrases with http response in a list of {@link TextDocumentInput} with request options.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;My cat might need to see a veterinarian.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;2&quot;, &quot;The pitot tube is used to measure airspeed.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;
     * &#41;;
     *
     * &#47;&#47; Extracting batch key phrases
     * Response&lt;ExtractKeyPhrasesResultCollection&gt; response =
     *     textAnalyticsClient.extractKeyPhrasesBatchWithResponse&#40;textDocumentInputs,
     *         new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;, Context.NONE&#41;;
     *
     *
     * &#47;&#47; Response's status code
     * System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * ExtractKeyPhrasesResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;
     *     &quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * &#47;&#47; Extracted key phrase for each of documents from a batch of documents
     * resultCollection.forEach&#40;extractKeyPhraseResult -&gt; &#123;
     *     System.out.printf&#40;&quot;Document ID: %s%n&quot;, extractKeyPhraseResult.getId&#40;&#41;&#41;;
     *     &#47;&#47; Valid document
     *     System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     *     extractKeyPhraseResult.getKeyPhrases&#40;&#41;.forEach&#40;keyPhrase -&gt;
     *         System.out.printf&#40;&quot;%s.%n&quot;, keyPhrase&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains a {@link ExtractKeyPhrasesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in
     *   service API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ExtractKeyPhrasesResultCollection> extractKeyPhrasesBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        inputDocumentsValidation(documents);
        return client.extractKeyPhraseUtilClient.getExtractedKeyPhrasesResponseSync(documents, options, context);
    }

    // Sentiment
    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments of documents</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String -->
     * <pre>
     * final DocumentSentiment documentSentiment =
     *     textAnalyticsClient.analyzeSentiment&#40;&quot;The hotel was dark and unclean.&quot;&#41;;
     *
     * System.out.printf&#40;
     *     &quot;Recognized sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n&quot;,
     *     documentSentiment.getSentiment&#40;&#41;,
     *     documentSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *     documentSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *     documentSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;;
     *
     * for &#40;SentenceSentiment sentenceSentiment : documentSentiment.getSentences&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;
     *         &quot;Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n&quot;,
     *         sentenceSentiment.getSentiment&#40;&#41;,
     *         sentenceSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *         sentenceSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *         sentenceSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.TextAnalyticsClient.analyzeSentiment#String -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link DocumentSentiment analyzed document sentiment} of the document.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentSentiment analyzeSentiment(String document) {
        return analyzeSentiment(document, client.getDefaultLanguage());
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments in a document with a provided language representation.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String-String -->
     * <pre>
     * final DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment&#40;
     *     &quot;The hotel was dark and unclean.&quot;, &quot;en&quot;&#41;;
     *
     * System.out.printf&#40;
     *     &quot;Recognized sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n&quot;,
     *     documentSentiment.getSentiment&#40;&#41;,
     *     documentSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *     documentSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *     documentSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;;
     *
     * for &#40;SentenceSentiment sentenceSentiment : documentSentiment.getSentences&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;
     *         &quot;Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, negative score: %.2f.%n&quot;,
     *         sentenceSentiment.getSentiment&#40;&#41;,
     *         sentenceSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *         sentenceSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *         sentenceSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String-String -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link DocumentSentiment analyzed document sentiment} of the document.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentSentiment analyzeSentiment(String document, String language) {
        return analyzeSentiment(document, language, null);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it. If the {@code includeOpinionMining} of
     * {@link AnalyzeSentimentOptions} set to true, the output will include the opinion mining results. It mines the
     * opinions of a sentence and conducts more granular analysis around the aspects in the text
     * (also known as aspect-based sentiment analysis).
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiment and mine the opinions for each sentence in a document with a provided language
     * representation and {@link AnalyzeSentimentOptions} options.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentiment#String-String-AnalyzeSentimentOptions -->
     * <pre>
     * final DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment&#40;
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
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     *
     * @return A {@link DocumentSentiment analyzed document sentiment} of the document.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     * @throws UnsupportedOperationException if {@link AnalyzeSentimentOptions#isServiceLogsDisabled()} or
     *   {@link AnalyzeSentimentOptions#isIncludeOpinionMining()} is true in service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} and {@code includeOpinionMining} are only
     *   available for API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DocumentSentiment analyzeSentiment(String document, String language, AnalyzeSentimentOptions options) {
        Objects.requireNonNull(document, "'document' cannot be null.");
        final AnalyzeSentimentResultCollection sentimentResultCollection =
            analyzeSentimentBatch(Collections.singletonList(document), language, options);
        DocumentSentiment documentSentiment = null;
        for (AnalyzeSentimentResult sentimentResult : sentimentResultCollection) {
            if (sentimentResult.isError()) {
                throw LOGGER.logExceptionAsError(toTextAnalyticsException(sentimentResult.getError()));
            }
            documentSentiment = sentimentResult.getDocumentSentiment();
        }
        return documentSentiment;
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments in a list of documents with a provided language representation and request options.</p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;The hotel was dark and unclean. The restaurant had amazing gnocchi.&quot;,
     *     &quot;The restaurant had amazing gnocchi. The hotel was dark and unclean.&quot;
     * &#41;;
     *
     * &#47;&#47; Analyzing batch sentiments
     * AnalyzeSentimentResultCollection resultCollection = textAnalyticsClient.analyzeSentimentBatch&#40;
     *     documents, &quot;en&quot;, new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;&quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * &#47;&#47; Analyzed sentiment for each of documents from a batch of documents
     * resultCollection.forEach&#40;analyzeSentimentResult -&gt; &#123;
     *     System.out.printf&#40;&quot;Document ID: %s%n&quot;, analyzeSentimentResult.getId&#40;&#41;&#41;;
     *     &#47;&#47; Valid document
     *     DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment&#40;&#41;;
     *     System.out.printf&#40;
     *         &quot;Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f,&quot;
     *             + &quot; negative score: %.2f.%n&quot;,
     *         documentSentiment.getSentiment&#40;&#41;,
     *         documentSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *         documentSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *         documentSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;;
     *     documentSentiment.getSentences&#40;&#41;.forEach&#40;sentenceSentiment -&gt; System.out.printf&#40;
     *         &quot;Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f,&quot;
     *             + &quot; negative score: %.2f.%n&quot;,
     *         sentenceSentiment.getSentiment&#40;&#41;,
     *         sentenceSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *         sentenceSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *         sentenceSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     *
     * @deprecated Please use the {@link #analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnalyzeSentimentResultCollection analyzeSentimentBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        return analyzeSentimentBatchWithResponse(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options, Context.NONE).getValue();
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it. If the {@code includeOpinionMining} of
     * {@link AnalyzeSentimentOptions} set to true, the output will include the opinion mining results. It mines the
     * opinions of a sentence and conducts more granular analysis around the aspects in the text
     * (also known as aspect-based sentiment analysis).
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments and mine the opinions for each sentence in a list of documents with a provided language
     * representation and {@link AnalyzeSentimentOptions} options.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String-AnalyzeSentimentOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;The hotel was dark and unclean. The restaurant had amazing gnocchi.&quot;,
     *     &quot;The restaurant had amazing gnocchi. The hotel was dark and unclean.&quot;
     * &#41;;
     *
     * &#47;&#47; Analyzing batch sentiments
     * AnalyzeSentimentResultCollection resultCollection = textAnalyticsClient.analyzeSentimentBatch&#40;
     *     documents, &quot;en&quot;, new AnalyzeSentimentOptions&#40;&#41;.setIncludeOpinionMining&#40;true&#41;&#41;;
     *
     * &#47;&#47; Analyzed sentiment for each of documents from a batch of documents
     * resultCollection.forEach&#40;analyzeSentimentResult -&gt; &#123;
     *     System.out.printf&#40;&quot;Document ID: %s%n&quot;, analyzeSentimentResult.getId&#40;&#41;&#41;;
     *     DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment&#40;&#41;;
     *     documentSentiment.getSentences&#40;&#41;.forEach&#40;sentenceSentiment -&gt; &#123;
     *         System.out.printf&#40;&quot;&#92;tSentence sentiment: %s%n&quot;, sentenceSentiment.getSentiment&#40;&#41;&#41;;
     *         sentenceSentiment.getOpinions&#40;&#41;.forEach&#40;opinion -&gt; &#123;
     *             TargetSentiment targetSentiment = opinion.getTarget&#40;&#41;;
     *             System.out.printf&#40;&quot;&#92;tTarget sentiment: %s, target text: %s%n&quot;, targetSentiment.getSentiment&#40;&#41;,
     *                 targetSentiment.getText&#40;&#41;&#41;;
     *             for &#40;AssessmentSentiment assessmentSentiment : opinion.getAssessments&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;&#92;t&#92;t'%s' sentiment because of &#92;&quot;%s&#92;&quot;. Is the assessment negated: %s.%n&quot;,
     *                     assessmentSentiment.getSentiment&#40;&#41;, assessmentSentiment.getText&#40;&#41;, assessmentSentiment.isNegated&#40;&#41;&#41;;
     *             &#125;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-String-AnalyzeSentimentOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     *
     * @return A {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link AnalyzeSentimentOptions#isServiceLogsDisabled()} or
     *   {@link AnalyzeSentimentOptions#isIncludeOpinionMining()} is true in service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} and {@code includeOpinionMining} are only
     *   available for API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnalyzeSentimentResultCollection analyzeSentimentBatch(Iterable<String> documents,
        String language, AnalyzeSentimentOptions options) {
        return analyzeSentimentBatchWithResponse(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options, Context.NONE).getValue();
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label
     * (Positive, Negative, and Neutral) for the document and each sentence within it.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze sentiment in a list of {@link TextDocumentInput document} with provided request options.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;The hotel was dark and unclean. The restaurant had amazing gnocchi.&quot;&#41;
     *         .setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;2&quot;, &quot;The restaurant had amazing gnocchi. The hotel was dark and unclean.&quot;&#41;
     *         .setLanguage&#40;&quot;en&quot;&#41;
     * &#41;;
     *
     * &#47;&#47; Analyzing batch sentiments
     * Response&lt;AnalyzeSentimentResultCollection&gt; response =
     *     textAnalyticsClient.analyzeSentimentBatchWithResponse&#40;textDocumentInputs,
     *         new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;, Context.NONE&#41;;
     *
     * &#47;&#47; Response's status code
     * System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * AnalyzeSentimentResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;&quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * &#47;&#47; Analyzed sentiment for each of documents from a batch of documents
     * resultCollection.forEach&#40;analyzeSentimentResult -&gt; &#123;
     *     System.out.printf&#40;&quot;Document ID: %s%n&quot;, analyzeSentimentResult.getId&#40;&#41;&#41;;
     *     &#47;&#47; Valid document
     *     DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment&#40;&#41;;
     *     System.out.printf&#40;
     *         &quot;Recognized document sentiment: %s, positive score: %.2f, neutral score: %.2f, &quot;
     *             + &quot;negative score: %.2f.%n&quot;,
     *         documentSentiment.getSentiment&#40;&#41;,
     *         documentSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *         documentSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *         documentSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;;
     *     documentSentiment.getSentences&#40;&#41;.forEach&#40;sentenceSentiment -&gt; &#123;
     *         System.out.printf&#40;
     *             &quot;Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f,&quot;
     *                 + &quot; negative score: %.2f.%n&quot;,
     *             sentenceSentiment.getSentiment&#40;&#41;,
     *             sentenceSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *             sentenceSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *             sentenceSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains a {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     *
     * @deprecated Please use the
     * {@link #analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions, Context)}.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnalyzeSentimentResultCollection> analyzeSentimentBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context) {
        return analyzeSentimentBatchWithResponse(documents,
            new AnalyzeSentimentOptions().setIncludeStatistics(options == null ? false : options.isIncludeStatistics())
                .setModelVersion(options == null ? null : options.getModelVersion()),
            context);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it. If the {@code includeOpinionMining} of
     * {@link AnalyzeSentimentOptions} set to true, the output will include the opinion mining results. It mines the
     * opinions of a sentence and conducts more granular analysis around the aspects in the text
     * (also known as aspect-based sentiment analysis).
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze sentiment and mine the opinions for each sentence in a list of
     * {@link TextDocumentInput document} with provided {@link AnalyzeSentimentOptions} options.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-AnalyzeSentimentOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;The hotel was dark and unclean. The restaurant had amazing gnocchi.&quot;&#41;
     *         .setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;2&quot;, &quot;The restaurant had amazing gnocchi. The hotel was dark and unclean.&quot;&#41;
     *         .setLanguage&#40;&quot;en&quot;&#41;
     * &#41;;
     *
     * AnalyzeSentimentOptions options = new AnalyzeSentimentOptions&#40;&#41;.setIncludeOpinionMining&#40;true&#41;
     *     .setIncludeStatistics&#40;true&#41;;
     *
     * &#47;&#47; Analyzing batch sentiments
     * Response&lt;AnalyzeSentimentResultCollection&gt; response =
     *     textAnalyticsClient.analyzeSentimentBatchWithResponse&#40;textDocumentInputs, options, Context.NONE&#41;;
     *
     * &#47;&#47; Response's status code
     * System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * AnalyzeSentimentResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;&quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * &#47;&#47; Analyzed sentiment for each of documents from a batch of documents
     * resultCollection.forEach&#40;analyzeSentimentResult -&gt; &#123;
     *     System.out.printf&#40;&quot;Document ID: %s%n&quot;, analyzeSentimentResult.getId&#40;&#41;&#41;;
     *     DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment&#40;&#41;;
     *     documentSentiment.getSentences&#40;&#41;.forEach&#40;sentenceSentiment -&gt; &#123;
     *         System.out.printf&#40;&quot;&#92;tSentence sentiment: %s%n&quot;, sentenceSentiment.getSentiment&#40;&#41;&#41;;
     *         sentenceSentiment.getOpinions&#40;&#41;.forEach&#40;opinion -&gt; &#123;
     *             TargetSentiment targetSentiment = opinion.getTarget&#40;&#41;;
     *             System.out.printf&#40;&quot;&#92;tTarget sentiment: %s, target text: %s%n&quot;, targetSentiment.getSentiment&#40;&#41;,
     *                 targetSentiment.getText&#40;&#41;&#41;;
     *             for &#40;AssessmentSentiment assessmentSentiment : opinion.getAssessments&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;&#92;t&#92;t'%s' sentiment because of &#92;&quot;%s&#92;&quot;. Is the assessment negated: %s.%n&quot;,
     *                     assessmentSentiment.getSentiment&#40;&#41;, assessmentSentiment.getText&#40;&#41;,
     *                     assessmentSentiment.isNegated&#40;&#41;&#41;;
     *             &#125;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.analyzeSentimentBatch#Iterable-AnalyzeSentimentOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains a {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link AnalyzeSentimentOptions#isServiceLogsDisabled()} or
     *   {@link AnalyzeSentimentOptions#isIncludeOpinionMining()} is true in service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} and {@code includeOpinionMining} are only
     *   available for API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnalyzeSentimentResultCollection> analyzeSentimentBatchWithResponse(
        Iterable<TextDocumentInput> documents, AnalyzeSentimentOptions options, Context context) {
        return client.analyzeSentimentUtilClient.getAnalyzedSentimentResponseSync(documents, options, context);
    }

    /**
     * Perform dynamic classification on a batch of documents. On the fly classification of the input documents into
     * one or multiple categories. Assigns either one or multiple categories per document. This type of classification
     * doesn't require model training. See https://aka.ms/azsdk/textanalytics/data-limits for service data limits.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Dynamic classification of each document in a list of {@link String document} with provided
     * {@link DynamicClassificationOptions} options.
     *
     * <!-- src_embed Client.dynamicClassificationBatch#Iterable-String-DynamicClassificationOptions -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * documents.add&#40;&quot;The WHO is issuing a warning about Monkey Pox.&quot;&#41;;
     * documents.add&#40;&quot;Mo Salah plays in Liverpool FC in England.&quot;&#41;;
     * DynamicClassificationOptions options = new DynamicClassificationOptions&#40;&#41;
     *     .setCategories&#40;&quot;Health&quot;, &quot;Politics&quot;, &quot;Music&quot;, &quot;Sport&quot;&#41;;
     *
     * &#47;&#47; Analyzing dynamic classification
     * DynamicClassifyDocumentResultCollection resultCollection =
     *     textAnalyticsClient.dynamicClassificationBatch&#40;documents, &quot;en&quot;, options&#41;;
     *
     * &#47;&#47; Result of dynamic classification
     * resultCollection.forEach&#40;documentResult -&gt; &#123;
     *     System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *     for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
     *             classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end Client.dynamicClassificationBatch#Iterable-String-DynamicClassificationOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link DynamicClassificationOptions options} that may be passed when
     * analyzing dynamic classification.
     *
     * @return A {@link DynamicClassifyDocumentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code dynamicClassificationBatch} is called with
     * service API version {@link TextAnalyticsServiceVersion#V3_0}, {@link TextAnalyticsServiceVersion#V3_1},
     * or {@link TextAnalyticsServiceVersion#V2022_05_01}. Those actions are only available for API version
     * 2022-10-01-preview and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DynamicClassifyDocumentResultCollection dynamicClassificationBatch(Iterable<String> documents,
        String language, DynamicClassificationOptions options) {
        return dynamicClassificationBatchWithResponse(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options, Context.NONE).getValue();
    }

    /**
     * Perform dynamic classification on a batch of documents. On the fly classification of the input documents into
     * one or multiple categories. Assigns either one or multiple categories per document. This type of classification
     * doesn't require model training. See https://aka.ms/azsdk/textanalytics/data-limits for service data limits.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Dynamic classification of each document in a list of {@link TextDocumentInput document} with provided
     * {@link DynamicClassificationOptions} options.
     *
     * <!-- Client.dynamicClassificationBatchWithResponse#Iterable-DynamicClassificationOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;The hotel was dark and unclean. The restaurant had amazing gnocchi.&quot;&#41;
     *         .setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;2&quot;, &quot;The restaurant had amazing gnocchi. The hotel was dark and unclean.&quot;&#41;
     *         .setLanguage&#40;&quot;en&quot;&#41;
     * &#41;;
     *
     * AnalyzeSentimentOptions options = new AnalyzeSentimentOptions&#40;&#41;.setIncludeOpinionMining&#40;true&#41;
     *     .setIncludeStatistics&#40;true&#41;;
     *
     * &#47;&#47; Analyzing batch sentiments
     * Response&lt;AnalyzeSentimentResultCollection&gt; response =
     *     textAnalyticsClient.analyzeSentimentBatchWithResponse&#40;textDocumentInputs, options, Context.NONE&#41;;
     *
     * &#47;&#47; Response's status code
     * System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * AnalyzeSentimentResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     * &#47;&#47; Batch statistics
     * TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     * System.out.printf&#40;&quot;A batch of documents statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *     batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     * &#47;&#47; Analyzed sentiment for each of documents from a batch of documents
     * resultCollection.forEach&#40;analyzeSentimentResult -&gt; &#123;
     *     System.out.printf&#40;&quot;Document ID: %s%n&quot;, analyzeSentimentResult.getId&#40;&#41;&#41;;
     *     DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment&#40;&#41;;
     *     documentSentiment.getSentences&#40;&#41;.forEach&#40;sentenceSentiment -&gt; &#123;
     *         System.out.printf&#40;&quot;&#92;tSentence sentiment: %s%n&quot;, sentenceSentiment.getSentiment&#40;&#41;&#41;;
     *         sentenceSentiment.getOpinions&#40;&#41;.forEach&#40;opinion -&gt; &#123;
     *             TargetSentiment targetSentiment = opinion.getTarget&#40;&#41;;
     *             System.out.printf&#40;&quot;&#92;tTarget sentiment: %s, target text: %s%n&quot;, targetSentiment.getSentiment&#40;&#41;,
     *                 targetSentiment.getText&#40;&#41;&#41;;
     *             for &#40;AssessmentSentiment assessmentSentiment : opinion.getAssessments&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;&#92;t&#92;t'%s' sentiment because of &#92;&quot;%s&#92;&quot;. Is the assessment negated: %s.%n&quot;,
     *                     assessmentSentiment.getSentiment&#40;&#41;, assessmentSentiment.getText&#40;&#41;,
     *                     assessmentSentiment.isNegated&#40;&#41;&#41;;
     *             &#125;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end Client.dynamicClassificationBatchWithResponse#Iterable-DynamicClassificationOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The additional configurable {@link DynamicClassificationOptions options} that may be passed when
     * analyzing dynamic classification.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} that contains a {@link DynamicClassifyDocumentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code dynamicClassificationBatchWithResponse} is called with
     * service API version {@link TextAnalyticsServiceVersion#V3_0}, {@link TextAnalyticsServiceVersion#V3_1},
     * or {@link TextAnalyticsServiceVersion#V2022_05_01}. Those actions are only available for API version
     * 2022-10-01-preview and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DynamicClassifyDocumentResultCollection> dynamicClassificationBatchWithResponse(
        Iterable<TextDocumentInput> documents, DynamicClassificationOptions options, Context context) {
        return client.dynamicClassificationUtilClient.getResultCollectionResponseSync(
            documents, options, context);
    }

    /**
     * Analyze healthcare entities, entity data sources, and entity relations in a list of {@link String documents}.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
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
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @return A {@link SyncPoller} that polls the analyze healthcare operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link AnalyzeHealthcareEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAnalyzeHealthcareEntities} is called with service API
     *   version {@link TextAnalyticsServiceVersion#V3_0}. {@code beginAnalyzeHealthcareEntities} is only available for
     *   API version v3.1 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
        beginAnalyzeHealthcareEntities(Iterable<String> documents) {
        return beginAnalyzeHealthcareEntities(documents, client.getDefaultLanguage(), null);
    }

    /**
     * Analyze healthcare entities, entity data sources, and entity relations in a list of
     * {@link String documents} with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeHealthcareEntities#Iterable-String-AnalyzeHealthcareEntitiesOptions -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;&quot;The patient is a 54-year-old gentleman with a history of progressive angina over &quot;
     *         + &quot;the past several months.&quot;&#41;;
     * &#125;
     *
     * &#47;&#47; Request options: show statistics and model version
     * AnalyzeHealthcareEntitiesOptions options = new AnalyzeHealthcareEntitiesOptions&#40;&#41;
     *     .setIncludeStatistics&#40;true&#41;;
     *
     * SyncPoller&lt;AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable&gt;
     *     syncPoller = textAnalyticsClient.beginAnalyzeHealthcareEntities&#40;documents, &quot;en&quot;, options&#41;;
     *
     * syncPoller.waitForCompletion&#40;&#41;;
     * AnalyzeHealthcareEntitiesPagedIterable result = syncPoller.getFinalResult&#40;&#41;;
     *
     * result.forEach&#40;analyzeHealthcareEntitiesResultCollection -&gt; &#123;
     *     &#47;&#47; Model version
     *     System.out.printf&#40;&quot;Results of Azure Text Analytics &#92;&quot;Analyze Healthcare&#92;&quot; Model, version: %s%n&quot;,
     *         analyzeHealthcareEntitiesResultCollection.getModelVersion&#40;&#41;&#41;;
     *
     *     TextDocumentBatchStatistics healthcareTaskStatistics =
     *         analyzeHealthcareEntitiesResultCollection.getStatistics&#40;&#41;;
     *     &#47;&#47; Batch statistics
     *     System.out.printf&#40;&quot;Documents statistics: document count = %d, erroneous document count = %d,&quot;
     *             + &quot; transaction count = %d, valid document count = %d.%n&quot;,
     *         healthcareTaskStatistics.getDocumentCount&#40;&#41;, healthcareTaskStatistics.getInvalidDocumentCount&#40;&#41;,
     *         healthcareTaskStatistics.getTransactionCount&#40;&#41;, healthcareTaskStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
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
     *             System.out.printf&#40;&quot;&#92;tRelation confidence score: %f.%n&quot;, entityRelation.getConfidenceScore&#40;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeHealthcareEntities#Iterable-String-AnalyzeHealthcareEntitiesOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2-letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeHealthcareEntitiesOptions options} that may be passed
     * when analyzing healthcare entities.
     * @return A {@link SyncPoller} that polls the analyze healthcare operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link AnalyzeHealthcareEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAnalyzeHealthcareEntities} is called with service API
     *   version {@link TextAnalyticsServiceVersion#V3_0}. {@code beginAnalyzeHealthcareEntities} is only available for
     *   API version v3.1 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
        beginAnalyzeHealthcareEntities(Iterable<String> documents, String language,
            AnalyzeHealthcareEntitiesOptions options) {
        return beginAnalyzeHealthcareEntities(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options, Context.NONE);
    }

    /**
     * Analyze healthcare entities, entity data sources, and entity relations in a list of
     * {@link TextDocumentInput document} and provided request options to show statistics.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeHealthcareEntities#Iterable-AnalyzeHealthcareEntitiesOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;new TextDocumentInput&#40;Integer.toString&#40;i&#41;,
     *         &quot;The patient is a 54-year-old gentleman with a history of progressive angina over &quot;
     *             + &quot;the past several months.&quot;&#41;&#41;;
     * &#125;
     *
     * &#47;&#47; Request options: show statistics and model version
     * AnalyzeHealthcareEntitiesOptions options = new AnalyzeHealthcareEntitiesOptions&#40;&#41;
     *     .setIncludeStatistics&#40;true&#41;;
     *
     * SyncPoller&lt;AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable&gt;
     *     syncPoller = textAnalyticsClient.beginAnalyzeHealthcareEntities&#40;documents, options, Context.NONE&#41;;
     *
     * syncPoller.waitForCompletion&#40;&#41;;
     * AnalyzeHealthcareEntitiesPagedIterable result = syncPoller.getFinalResult&#40;&#41;;
     *
     * &#47;&#47; Task operation statistics
     * final AnalyzeHealthcareEntitiesOperationDetail operationResult = syncPoller.poll&#40;&#41;.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *     operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *
     * result.forEach&#40;analyzeHealthcareEntitiesResultCollection -&gt; &#123;
     *     &#47;&#47; Model version
     *     System.out.printf&#40;&quot;Results of Azure Text Analytics &#92;&quot;Analyze Healthcare&#92;&quot; Model, version: %s%n&quot;,
     *         analyzeHealthcareEntitiesResultCollection.getModelVersion&#40;&#41;&#41;;
     *
     *     TextDocumentBatchStatistics healthcareTaskStatistics =
     *         analyzeHealthcareEntitiesResultCollection.getStatistics&#40;&#41;;
     *     &#47;&#47; Batch statistics
     *     System.out.printf&#40;&quot;Documents statistics: document count = %d, erroneous document count = %d,&quot;
     *             + &quot; transaction count = %d, valid document count = %d.%n&quot;,
     *         healthcareTaskStatistics.getDocumentCount&#40;&#41;, healthcareTaskStatistics.getInvalidDocumentCount&#40;&#41;,
     *         healthcareTaskStatistics.getTransactionCount&#40;&#41;, healthcareTaskStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
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
     *             System.out.printf&#40;&quot;&#92;tRelation confidence score: %f.%n&quot;, entityRelation.getConfidenceScore&#40;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeHealthcareEntities#Iterable-AnalyzeHealthcareEntitiesOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * @param options The additional configurable {@link AnalyzeHealthcareEntitiesOptions options} that may be passed
     * when analyzing healthcare entities.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the analyze healthcare operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link AnalyzeHealthcareEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAnalyzeHealthcareEntities} is called with service API
     *   version {@link TextAnalyticsServiceVersion#V3_0}. {@code beginAnalyzeHealthcareEntities} is only available for
     *   API version v3.1 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedIterable>
        beginAnalyzeHealthcareEntities(Iterable<TextDocumentInput> documents, AnalyzeHealthcareEntitiesOptions options,
            Context context) {
        return client.analyzeHealthcareEntityUtilClient.beginAnalyzeHealthcarePagedIterable(documents, options,
            context);
    }

    /**
     * Returns a list of custom entities for the provided list of {@link String document}.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
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
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     *
     * @return A {@link SyncPoller} that polls the recognize custom entities operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link RecognizeCustomEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginRecognizeCustomEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   {@code beginRecognizeCustomEntities} is only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable>
        beginRecognizeCustomEntities(Iterable<String> documents, String projectName, String deploymentName) {
        return beginRecognizeCustomEntities(documents, projectName, deploymentName, client.getDefaultLanguage(), null);
    }

    /**
     * Returns a list of custom entities for the provided list of {@link String document} with provided
     * request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed Client.beginRecognizeCustomEntities#Iterable-String-String-String-RecognizeCustomEntitiesOptions -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;
     *         &quot;A recent report by the Government Accountability Office &#40;GAO&#41; found that the dramatic increase &quot;
     *             + &quot;in oil and natural gas development on federal lands over the past six years has stretched the&quot;
     *             + &quot; staff of the BLM to a point that it has been unable to meet its environmental protection &quot;
     *             + &quot;responsibilities.&quot;&#41;; &#125;
     * RecognizeCustomEntitiesOptions options = new RecognizeCustomEntitiesOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * SyncPoller&lt;RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable&gt; syncPoller =
     *     textAnalyticsClient.beginRecognizeCustomEntities&#40;documents, &quot;&#123;project_name&#125;&quot;,
     *         &quot;&#123;deployment_name&#125;&quot;, &quot;en&quot;, options&#41;;
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
     * <!-- end Client.beginRecognizeCustomEntities#Iterable-String-String-String-RecognizeCustomEntitiesOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     * @param language The 2-letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link RecognizeCustomEntitiesOptions options} that may be passed
     * when recognizing custom entities.
     *
     * @return A {@link SyncPoller} that polls the recognize custom entities operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link RecognizeCustomEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginRecognizeCustomEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   {@code beginRecognizeCustomEntities} is only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable>
        beginRecognizeCustomEntities(Iterable<String> documents, String projectName, String deploymentName,
            String language, RecognizeCustomEntitiesOptions options) {
        return beginRecognizeCustomEntities(mapByIndex(documents, (index, value) -> {
            final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
            textDocumentInput.setLanguage(language);
            return textDocumentInput;
        }), projectName, deploymentName, options, Context.NONE);
    }

    /**
     * Returns a list of custom entities for the provided list of {@link TextDocumentInput document} with provided
     * request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01} </p>
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed Client.beginRecognizeCustomEntities#Iterable-String-String-RecognizeCustomEntitiesOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;new TextDocumentInput&#40;Integer.toString&#40;i&#41;,
     *         &quot;A recent report by the Government Accountability Office &#40;GAO&#41; found that the dramatic increase &quot;
     *             + &quot;in oil and natural gas development on federal lands over the past six years has stretched the&quot;
     *             + &quot; staff of the BLM to a point that it has been unable to meet its environmental protection &quot;
     *             + &quot;responsibilities.&quot;&#41;&#41;;
     *     RecognizeCustomEntitiesOptions options = new RecognizeCustomEntitiesOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     *     SyncPoller&lt;RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable&gt; syncPoller =
     *         textAnalyticsClient.beginRecognizeCustomEntities&#40;documents, &quot;&#123;project_name&#125;&quot;,
     *             &quot;&#123;deployment_name&#125;&quot;, options, Context.NONE&#41;;
     *     syncPoller.waitForCompletion&#40;&#41;;
     *     syncPoller.getFinalResult&#40;&#41;.forEach&#40;documentsResults -&gt; &#123;
     *         System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
     *             documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
     *         for &#40;RecognizeEntitiesResult documentResult : documentsResults&#41; &#123;
     *             System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *             for &#40;CategorizedEntity entity : documentResult.getEntities&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;
     *                     &quot;&#92;tText: %s, category: %s, confidence score: %f.%n&quot;,
     *                     entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * &#125;
     * </pre>
     * <!-- end Client.beginRecognizeCustomEntities#Iterable-String-String-RecognizeCustomEntitiesOptions-Context -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     * English as default.
     * @param options The additional configurable {@link RecognizeCustomEntitiesOptions options} that may be passed
     * when recognizing custom entities.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the recognize custom entities operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link RecognizeCustomEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginRecognizeCustomEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   {@code beginRecognizeCustomEntities} is only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedIterable>
        beginRecognizeCustomEntities(Iterable<TextDocumentInput> documents, String projectName,
            String deploymentName, RecognizeCustomEntitiesOptions options, Context context) {
        return client.recognizeCustomEntitiesUtilClient.recognizeCustomEntitiesPagedIterable(
            documents, projectName, deploymentName, options, context);
    }

    /**
     * Returns a list of single-label classification for the provided list of {@link String document}.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
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
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     *
     * @return A {@link SyncPoller} that polls the single-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link ClassifyDocumentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginSingleLabelClassify} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   {@code beginSingleLabelClassify} is only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable>
        beginSingleLabelClassify(Iterable<String> documents, String projectName, String deploymentName) {
        return beginSingleLabelClassify(documents, projectName, deploymentName, client.getDefaultLanguage(), null);
    }

    /**
     * Returns a list of single-label classification for the provided list of {@link String document} with
     * provided request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed Client.beginSingleLabelClassify#Iterable-String-String-String-SingleLabelClassifyOptions -->
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
     * SingleLabelClassifyOptions options = new SingleLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * &#47;&#47; See the service documentation for regional support and how to train a model to classify your documents,
     * &#47;&#47; see https:&#47;&#47;aka.ms&#47;azsdk&#47;textanalytics&#47;customfunctionalities
     * SyncPoller&lt;ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable&gt; syncPoller =
     *     textAnalyticsClient.beginSingleLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;,
     *         &quot;en&quot;, options&#41;;
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
     * <!-- end Client.beginSingleLabelClassify#Iterable-String-String-String-SingleLabelClassifyOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     * @param language The 2-letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link SingleLabelClassifyOptions options} that may be passed
     * when analyzing single-label classification.
     *
     * @return A {@link SyncPoller} that polls the single-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link ClassifyDocumentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginSingleLabelClassify} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   {@code beginSingleLabelClassify} is only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable>
        beginSingleLabelClassify(Iterable<String> documents, String projectName, String deploymentName,
            String language, SingleLabelClassifyOptions options) {
        return beginSingleLabelClassify(mapByIndex(documents, (index, value) -> {
            final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
            textDocumentInput.setLanguage(language);
            return textDocumentInput;
        }), projectName, deploymentName, options, Context.NONE);
    }

    /**
     * Returns a list of single-label classification for the provided list of {@link TextDocumentInput document} with
     * provided request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed Client.beginSingleLabelClassify#Iterable-String-String-SingleLabelClassifyOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;new TextDocumentInput&#40;Integer.toString&#40;i&#41;,
     *         &quot;A recent report by the Government Accountability Office &#40;GAO&#41; found that the dramatic increase &quot;
     *             + &quot;in oil and natural gas development on federal lands over the past six years has stretched the&quot;
     *             + &quot; staff of the BLM to a point that it has been unable to meet its environmental protection &quot;
     *             + &quot;responsibilities.&quot;&#41;&#41;;
     * &#125;
     * SingleLabelClassifyOptions options = new SingleLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * &#47;&#47; See the service documentation for regional support and how to train a model to classify your documents,
     * &#47;&#47; see https:&#47;&#47;aka.ms&#47;azsdk&#47;textanalytics&#47;customfunctionalities
     * SyncPoller&lt;ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable&gt; syncPoller =
     *     textAnalyticsClient.beginSingleLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;,
     *         options, Context.NONE&#41;;
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
     * <!-- end Client.beginSingleLabelClassify#Iterable-String-String-SingleLabelClassifyOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     * @param options The additional configurable {@link SingleLabelClassifyOptions options} that may be passed
     * when analyzing single-label classification.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the single-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link ClassifyDocumentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginSingleLabelClassify} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   {@code beginSingleLabelClassify} is only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable>
        beginSingleLabelClassify(Iterable<TextDocumentInput> documents, String projectName,
            String deploymentName, SingleLabelClassifyOptions options, Context context) {
        return client.labelClassifyUtilClient.singleLabelClassifyPagedIterable(
            documents, projectName, deploymentName, options, context);
    }

    /**
     * Returns a list of multi-label classification for the provided list of {@link String document}.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
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
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     *
     * @return A {@link SyncPoller} that polls the multi-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link ClassifyDocumentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginMultiLabelClassify} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   {@code beginMultiLabelClassify} is only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable>
        beginMultiLabelClassify(Iterable<String> documents, String projectName, String deploymentName) {
        return beginMultiLabelClassify(documents, projectName, deploymentName, client.getDefaultLanguage(), null);
    }

    /**
     * Returns a list of multi-label classification for the provided list of {@link String document} with
     * provided request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed Client.beginMultiLabelClassify#Iterable-String-String-String-MultiLabelClassifyOptions -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;
     *         &quot;I need a reservation for an indoor restaurant in China. Please don't stop the music.&quot;
     *             + &quot; Play music and add it to my playlist&quot;&#41;;
     * &#125;
     * MultiLabelClassifyOptions options = new MultiLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * SyncPoller&lt;ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable&gt; syncPoller =
     *     textAnalyticsClient.beginMultiLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;, &quot;en&quot;, options&#41;;
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
     * <!-- end Client.beginMultiLabelClassify#Iterable-String-String-String-MultiLabelClassifyOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     * @param language The 2-letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link SingleLabelClassifyOptions options} that may be passed
     * when analyzing multi-label classification.
     *
     * @return A {@link SyncPoller} that polls the multi-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link ClassifyDocumentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginMultiLabelClassify} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   {@code beginMultiLabelClassify} is only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable>
        beginMultiLabelClassify(Iterable<String> documents, String projectName, String deploymentName,
            String language, MultiLabelClassifyOptions options) {
        return beginMultiLabelClassify(mapByIndex(documents, (index, value) -> {
            final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
            textDocumentInput.setLanguage(language);
            return textDocumentInput;
        }), projectName, deploymentName, options, Context.NONE);
    }

    /**
     * Returns a list of multi-label classification for the provided list of {@link TextDocumentInput document} with
     * provided request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed Client.beginMultiLabelClassify#Iterable-String-String-MultiLabelClassifyOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;new TextDocumentInput&#40;Integer.toString&#40;i&#41;,
     *         &quot;I need a reservation for an indoor restaurant in China. Please don't stop the music.&quot;
     *             + &quot; Play music and add it to my playlist&quot;&#41;&#41;;
     * &#125;
     * MultiLabelClassifyOptions options = new MultiLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * SyncPoller&lt;ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable&gt; syncPoller =
     *     textAnalyticsClient.beginMultiLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;,
     *         options, Context.NONE&#41;;
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
     * <!-- end Client.beginMultiLabelClassify#Iterable-String-String-MultiLabelClassifyOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     * @param options The additional configurable {@link SingleLabelClassifyOptions options} that may be passed
     * when analyzing multi-label classification.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the multi-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link ClassifyDocumentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginMultiLabelClassify} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   {@code beginMultiLabelClassify} is only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable>
        beginMultiLabelClassify(Iterable<TextDocumentInput> documents, String projectName,
            String deploymentName, MultiLabelClassifyOptions options, Context context) {
        return client.labelClassifyUtilClient.multiLabelClassifyPagedIterable(
            documents, projectName, deploymentName, options, context);
    }

    // Abstractive Summarization
    /**
     * Returns a list of abstract summary for the provided list of {@link String document}.
     *
     * <p>This method is supported since service API version {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW}.</p>
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
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
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>..
     *
     * @return A {@link SyncPoller} that polls the abstractive summarization operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link AbstractSummaryResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAbstractSummary} is called with
     * service API version {@link TextAnalyticsServiceVersion#V3_0}, {@link TextAnalyticsServiceVersion#V3_1},
     * or {@link TextAnalyticsServiceVersion#V2022_05_01}. Those actions are only available for API version
     * {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW} and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<AbstractSummaryOperationDetail, AbstractSummaryPagedIterable> beginAbstractSummary(
        Iterable<String> documents) {
        return beginAbstractSummary(documents, client.getDefaultLanguage(), null);
    }

    /**
     * Returns a list of abstract summary for the provided list of {@link String document} with
     * provided request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed Client.beginMultiLabelClassify#Iterable-String-String-String-MultiLabelClassifyOptions -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;
     *         &quot;I need a reservation for an indoor restaurant in China. Please don't stop the music.&quot;
     *             + &quot; Play music and add it to my playlist&quot;&#41;;
     * &#125;
     * MultiLabelClassifyOptions options = new MultiLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * SyncPoller&lt;ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable&gt; syncPoller =
     *     textAnalyticsClient.beginMultiLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;, &quot;en&quot;, options&#41;;
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
     * <!-- end Client.beginMultiLabelClassify#Iterable-String-String-String-MultiLabelClassifyOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2-letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AbstractSummaryOptions options} that may be passed
     * when analyzing abstractive summarization.
     *
     * @return A {@link SyncPoller} that polls the abstractive summarization operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link AbstractSummaryResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAbstractSummary} is called with
     * service API version {@link TextAnalyticsServiceVersion#V3_0}, {@link TextAnalyticsServiceVersion#V3_1},
     * or {@link TextAnalyticsServiceVersion#V2022_05_01}. Those actions are only available for API version
     * {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW}  and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<AbstractSummaryOperationDetail, AbstractSummaryPagedIterable> beginAbstractSummary(
        Iterable<String> documents, String language, AbstractSummaryOptions options) {
        return client.abstractSummaryUtilClient.abstractSummaryPagedIterable(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options, Context.NONE);
    }

    /**
     * Returns a list of abstract summary for the provided list of {@link TextDocumentInput document} with
     * provided request options.
     *
     * <p>This method is supported since service API version {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed Client.beginMultiLabelClassify#Iterable-String-String-MultiLabelClassifyOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;new TextDocumentInput&#40;Integer.toString&#40;i&#41;,
     *         &quot;I need a reservation for an indoor restaurant in China. Please don't stop the music.&quot;
     *             + &quot; Play music and add it to my playlist&quot;&#41;&#41;;
     * &#125;
     * MultiLabelClassifyOptions options = new MultiLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * SyncPoller&lt;ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable&gt; syncPoller =
     *     textAnalyticsClient.beginMultiLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;,
     *         options, Context.NONE&#41;;
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
     * <!-- end Client.beginMultiLabelClassify#Iterable-String-String-MultiLabelClassifyOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The additional configurable {@link AbstractSummaryOptions options} that may be passed
     * when analyzing abstractive summarization.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the abstractive summarization operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link AbstractSummaryResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAbstractSummary} is called with
     * service API version {@link TextAnalyticsServiceVersion#V3_0}, {@link TextAnalyticsServiceVersion#V3_1},
     * or {@link TextAnalyticsServiceVersion#V2022_05_01}. Those actions are only available for API version
     * {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW}  and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<AbstractSummaryOperationDetail, AbstractSummaryPagedIterable> beginAbstractSummary(
        Iterable<TextDocumentInput> documents, AbstractSummaryOptions options, Context context) {
        return client.abstractSummaryUtilClient.abstractSummaryPagedIterable(documents, options, context);
    }

    // Extractive Summarization
    /**
     * Returns a list of extract summaries for the provided list of {@link String document}.
     *
     * <p>This method is supported since service API version {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW}.</p>
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
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
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link SyncPoller} that polls the extractive summarization operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link ExtractSummaryResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginExtractSummary} is called with
     * service API version {@link TextAnalyticsServiceVersion#V3_0}, {@link TextAnalyticsServiceVersion#V3_1},
     * or {@link TextAnalyticsServiceVersion#V2022_05_01}. Those actions are only available for API version
     * {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW}  and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<ExtractSummaryOperationDetail, ExtractSummaryPagedIterable> beginExtractSummary(
        Iterable<String> documents) {
        return beginExtractSummary(documents, client.getDefaultLanguage(), null);
    }

    /**
     * Returns a list of  extract summaries for the provided list of {@link String document} with
     * provided request options.
     *
     * <p>This method is supported since service API version {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW}.</p>
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed Client.beginMultiLabelClassify#Iterable-String-String-String-MultiLabelClassifyOptions -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;
     *         &quot;I need a reservation for an indoor restaurant in China. Please don't stop the music.&quot;
     *             + &quot; Play music and add it to my playlist&quot;&#41;;
     * &#125;
     * MultiLabelClassifyOptions options = new MultiLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * SyncPoller&lt;ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable&gt; syncPoller =
     *     textAnalyticsClient.beginMultiLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;, &quot;en&quot;, options&#41;;
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
     * <!-- end Client.beginMultiLabelClassify#Iterable-String-String-String-MultiLabelClassifyOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2-letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link ExtractSummaryOptions options} that may be passed
     * when analyzing extractive summarization.
     *
     * @return A {@link SyncPoller} that polls the extractive summarization operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link ExtractSummaryResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginExtractSummary} is called with
     * service API version {@link TextAnalyticsServiceVersion#V3_0}, {@link TextAnalyticsServiceVersion#V3_1},
     * or {@link TextAnalyticsServiceVersion#V2022_05_01}. Those actions are only available for API version
     * {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW}  and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<ExtractSummaryOperationDetail, ExtractSummaryPagedIterable> beginExtractSummary(
        Iterable<String> documents, String language, ExtractSummaryOptions options) {
        return client.extractSummaryUtilClient.extractSummaryPagedIterable(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options, Context.NONE);
    }

    /**
     * Returns a list of extract summaries for the provided list of {@link TextDocumentInput document} with
     * provided request options.
     *
     * <p>This method is supported since service API version {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed Client.beginMultiLabelClassify#Iterable-String-String-MultiLabelClassifyOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;new TextDocumentInput&#40;Integer.toString&#40;i&#41;,
     *         &quot;I need a reservation for an indoor restaurant in China. Please don't stop the music.&quot;
     *             + &quot; Play music and add it to my playlist&quot;&#41;&#41;;
     * &#125;
     * MultiLabelClassifyOptions options = new MultiLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * SyncPoller&lt;ClassifyDocumentOperationDetail, ClassifyDocumentPagedIterable&gt; syncPoller =
     *     textAnalyticsClient.beginMultiLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;,
     *         options, Context.NONE&#41;;
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
     * <!-- end Client.beginMultiLabelClassify#Iterable-String-String-MultiLabelClassifyOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The additional configurable {@link ExtractSummaryOptions options} that may be passed
     * when analyzing extractive summarization.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the extractive summarization operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedIterable} of
     * {@link ExtractSummaryResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginExtractSummary} is called with
     * service API version {@link TextAnalyticsServiceVersion#V3_0}, {@link TextAnalyticsServiceVersion#V3_1},
     * or {@link TextAnalyticsServiceVersion#V2022_05_01}. Those actions are only available for API version
     * {@link TextAnalyticsServiceVersion#V2022_10_01_PREVIEW}  and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<ExtractSummaryOperationDetail, ExtractSummaryPagedIterable> beginExtractSummary(
        Iterable<TextDocumentInput> documents, ExtractSummaryOptions options, Context context) {
        return client.extractSummaryUtilClient.extractSummaryPagedIterable(documents, options, context);
    }

    /**
     * Execute actions, such as, entities recognition, PII entities recognition and key phrases extraction for a list of
     * {@link String documents}.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
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
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param actions The {@link TextAnalyticsActions actions} that contains all actions to be executed.
     * An action is one task of execution, such as a single task of 'Key Phrases Extraction' on the given document
     * inputs.
     *
     * @return A {@link SyncPoller} that polls the analyze a collection of actions operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link AnalyzeActionsResultPagedIterable}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAnalyzeActions} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code beginAnalyzeActions} is only available for API version
     *   v3.1 and newer.
     * @throws UnsupportedOperationException if request {@link AnalyzeHealthcareEntitiesAction},
     *   {@link RecognizeCustomEntitiesAction}, {@link SingleLabelClassifyAction}, or {@link MultiLabelClassifyAction}
     *   in service API version {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   Those actions are only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> beginAnalyzeActions(
        Iterable<String> documents, TextAnalyticsActions actions) {
        return beginAnalyzeActions(documents, actions, client.getDefaultLanguage(), null);
    }

    /**
     * Execute actions, such as, entities recognition, PII entities recognition and key phrases extraction for a list of
     * {@link String documents} with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions -->
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
     *             .setExtractKeyPhrasesActions&#40;new ExtractKeyPhrasesAction&#40;&#41;&#41;,
     *         &quot;en&quot;,
     *         new AnalyzeActionsOptions&#40;&#41;.setIncludeStatistics&#40;false&#41;&#41;;
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
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param actions The {@link TextAnalyticsActions actions} that contains all actions to be executed.
     * An action is one task of execution, such as a single task of 'Key Phrases Extraction' on the given document
     * inputs.
     * @param language The 2 letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeActionsOptions options} that may be passed when
     * analyzing a collection of actions.
     *
     * @return A {@link SyncPoller} that polls the analyze a collection of actions operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link AnalyzeActionsResultPagedIterable}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAnalyzeActions} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code beginAnalyzeActions} is only available for API version
     *   v3.1 and newer.
     * @throws UnsupportedOperationException if request {@link AnalyzeHealthcareEntitiesAction},
     *   {@link RecognizeCustomEntitiesAction}, {@link SingleLabelClassifyAction}, or {@link MultiLabelClassifyAction}
     *   in service API version {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   Those actions are only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> beginAnalyzeActions(
        Iterable<String> documents, TextAnalyticsActions actions, String language, AnalyzeActionsOptions options) {
        return client.analyzeActionsUtilClient.beginAnalyzeActionsIterable(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), actions, options, Context.NONE);
    }

    /**
     * Execute actions, such as, entities recognition, PII entities recognition and key phrases extraction for a list of
     * {@link TextDocumentInput documents} with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-AnalyzeActionsOptions-Context -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;0&quot;, &quot;Elon Musk is the CEO of SpaceX and Tesla.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;My SSN is 859-98-0987&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;
     * &#41;;
     *
     * SyncPoller&lt;AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable&gt; syncPoller =
     *     textAnalyticsClient.beginAnalyzeActions&#40;
     *         documents,
     *         new TextAnalyticsActions&#40;&#41;.setDisplayName&#40;&quot;&#123;tasks_display_name&#125;&quot;&#41;
     *            .setRecognizeEntitiesActions&#40;new RecognizeEntitiesAction&#40;&#41;&#41;
     *            .setExtractKeyPhrasesActions&#40;new ExtractKeyPhrasesAction&#40;&#41;&#41;,
     *         new AnalyzeActionsOptions&#40;&#41;.setIncludeStatistics&#40;false&#41;,
     *         Context.NONE&#41;;
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
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-AnalyzeActionsOptions-Context -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * @param actions The {@link TextAnalyticsActions actions} that contains all actions to be executed.
     * An action is one task of execution, such as a single task of 'Key Phrases Extraction' on the given document
     * inputs.
     * @param options The additional configurable {@link AnalyzeActionsOptions options} that may be passed when
     * analyzing a collection of actions.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link SyncPoller} that polls the analyze a collection of actions operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link AnalyzeActionsResultPagedIterable}.
     *
     * @throws NullPointerException if {@code documents} or {@code actions} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAnalyzeActions} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code beginAnalyzeActions} is only available for API version
     *   v3.1 and newer.
     * @throws UnsupportedOperationException if request {@link AnalyzeHealthcareEntitiesAction},
     *   {@link RecognizeCustomEntitiesAction}, {@link SingleLabelClassifyAction}, or {@link MultiLabelClassifyAction}
     *   in service API version {@link TextAnalyticsServiceVersion#V3_0} or {@link TextAnalyticsServiceVersion#V3_1}.
     *   Those actions are only available for API version 2022-05-01 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public SyncPoller<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedIterable> beginAnalyzeActions(
        Iterable<TextDocumentInput> documents, TextAnalyticsActions actions, AnalyzeActionsOptions options,
        Context context) {
        return client.analyzeActionsUtilClient.beginAnalyzeActionsIterable(documents, actions, options, context);
    }
}
