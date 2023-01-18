// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.AnalyzeTextsImpl;
import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeActionsOptions;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesAction;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.CategorizedEntityCollection;
import com.azure.ai.textanalytics.models.ClassifyDocumentOperationDetail;
import com.azure.ai.textanalytics.models.DetectLanguageInput;
import com.azure.ai.textanalytics.models.DetectLanguageResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.DynamicClassificationOptions;
import com.azure.ai.textanalytics.util.DynamicClassifyDocumentResultCollection;
import com.azure.ai.textanalytics.models.KeyPhrasesCollection;
import com.azure.ai.textanalytics.models.LinkedEntityCollection;
import com.azure.ai.textanalytics.models.MultiLabelClassifyAction;
import com.azure.ai.textanalytics.models.MultiLabelClassifyOptions;
import com.azure.ai.textanalytics.models.PiiEntityCollection;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesAction;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOptions;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesOptions;
import com.azure.ai.textanalytics.models.SingleLabelClassifyAction;
import com.azure.ai.textanalytics.models.SingleLabelClassifyOptions;
import com.azure.ai.textanalytics.models.TextAnalyticsActions;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.models.TextDocumentInput;
import com.azure.ai.textanalytics.util.AnalyzeActionsResultPagedFlux;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesPagedFlux;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.ai.textanalytics.util.ClassifyDocumentPagedFlux;
import com.azure.ai.textanalytics.util.ClassifyDocumentResultCollection;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesPagedFlux;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Objects;

import static com.azure.ai.textanalytics.implementation.Utility.inputDocumentsValidation;
import static com.azure.ai.textanalytics.implementation.Utility.mapByIndex;
import static com.azure.ai.textanalytics.implementation.Utility.toTextAnalyticsException;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Text Analytics.
 * Operations allowed by the client are language detection, entities recognition, linked entities recognition,
 * key phrases extraction, and sentiment analysis of a document or a list of documents.
 *
 * <p><strong>Instantiating an asynchronous Text Analytics Client</strong></p>
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation -->
 * <pre>
 * TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation -->
 *
 * <p>View {@link TextAnalyticsClientBuilder} for additional ways to construct the client.</p>
 *
 * @see TextAnalyticsClientBuilder
 */
@ServiceClient(builder = TextAnalyticsClientBuilder.class, isAsync = true)
public final class TextAnalyticsAsyncClient {
    private final ClientLogger logger = new ClientLogger(TextAnalyticsAsyncClient.class);
    private final TextAnalyticsClientImpl legacyService;
    private final MicrosoftCognitiveLanguageServiceTextAnalysisImpl service;
    private final TextAnalyticsServiceVersion serviceVersion;
    private final String defaultCountryHint;
    private final String defaultLanguage;

    // Please see <a href=https://docs.microsoft.com/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    static final String COGNITIVE_TRACING_NAMESPACE_VALUE = "Microsoft.CognitiveServices";
    final DetectLanguageAsyncClient detectLanguageAsyncClient;
    final AnalyzeSentimentAsyncClient analyzeSentimentAsyncClient;
    final ExtractKeyPhraseAsyncClient extractKeyPhraseAsyncClient;
    final RecognizeEntityAsyncClient recognizeEntityAsyncClient;
    final RecognizePiiEntityAsyncClient recognizePiiEntityAsyncClient;
    final RecognizeLinkedEntityAsyncClient recognizeLinkedEntityAsyncClient;
    final RecognizeCustomEntitiesAsyncClient recognizeCustomEntitiesAsyncClient;
    final LabelClassifyAsyncClient labelClassifyAsyncClient;
    final AnalyzeHealthcareEntityAsyncClient analyzeHealthcareEntityAsyncClient;
    final AnalyzeActionsAsyncClient analyzeActionsAsyncClient;

    final DynamicClassificationAsyncClient dynamicClassificationAsyncClient;

    /**
     * Creates a {@link TextAnalyticsAsyncClient} that sends requests to the Text Analytics service's endpoint. Each
     * service call goes through the {@link TextAnalyticsClientBuilder#pipeline http pipeline}.
     *
     * @param legacyService The proxy service used to perform REST calls. It applies to REST API version v3.0 and v3.1
     * @param serviceVersion The versions of Azure Text Analytics supported by this client library.
     * @param defaultCountryHint The default country hint.
     * @param defaultLanguage The default language.
     */
    TextAnalyticsAsyncClient(TextAnalyticsClientImpl legacyService, TextAnalyticsServiceVersion serviceVersion,
        String defaultCountryHint, String defaultLanguage) {
        this.legacyService = legacyService;
        this.service = null;
        this.serviceVersion = serviceVersion;
        this.defaultCountryHint = defaultCountryHint;
        this.defaultLanguage = defaultLanguage;
        this.detectLanguageAsyncClient = new DetectLanguageAsyncClient(legacyService, serviceVersion);
        this.analyzeSentimentAsyncClient = new AnalyzeSentimentAsyncClient(legacyService, serviceVersion);
        this.extractKeyPhraseAsyncClient = new ExtractKeyPhraseAsyncClient(legacyService, serviceVersion);
        this.recognizeEntityAsyncClient = new RecognizeEntityAsyncClient(legacyService, serviceVersion);
        this.recognizePiiEntityAsyncClient = new RecognizePiiEntityAsyncClient(legacyService, serviceVersion);
        this.recognizeLinkedEntityAsyncClient = new RecognizeLinkedEntityAsyncClient(legacyService, serviceVersion);
        this.recognizeCustomEntitiesAsyncClient = new RecognizeCustomEntitiesAsyncClient(null, serviceVersion);
        this.analyzeHealthcareEntityAsyncClient = new AnalyzeHealthcareEntityAsyncClient(legacyService, serviceVersion);
        this.analyzeActionsAsyncClient = new AnalyzeActionsAsyncClient(legacyService, serviceVersion);
        this.labelClassifyAsyncClient = new LabelClassifyAsyncClient(null, serviceVersion);
        this.dynamicClassificationAsyncClient = new DynamicClassificationAsyncClient(null, serviceVersion);
    }

    TextAnalyticsAsyncClient(MicrosoftCognitiveLanguageServiceTextAnalysisImpl service,
        TextAnalyticsServiceVersion serviceVersion, String defaultCountryHint, String defaultLanguage) {
        this.legacyService = null;
        this.service = service;
        this.serviceVersion = serviceVersion;
        this.defaultCountryHint = defaultCountryHint;
        this.defaultLanguage = defaultLanguage;
        this.detectLanguageAsyncClient = new DetectLanguageAsyncClient(service, serviceVersion);
        this.analyzeSentimentAsyncClient = new AnalyzeSentimentAsyncClient(service, serviceVersion);
        this.extractKeyPhraseAsyncClient = new ExtractKeyPhraseAsyncClient(service, serviceVersion);
        this.recognizeEntityAsyncClient = new RecognizeEntityAsyncClient(service, serviceVersion);
        this.recognizePiiEntityAsyncClient = new RecognizePiiEntityAsyncClient(service, serviceVersion);
        this.recognizeLinkedEntityAsyncClient = new RecognizeLinkedEntityAsyncClient(service, serviceVersion);
        this.recognizeCustomEntitiesAsyncClient = new RecognizeCustomEntitiesAsyncClient(
            new AnalyzeTextsImpl(service), serviceVersion);
        this.analyzeHealthcareEntityAsyncClient = new AnalyzeHealthcareEntityAsyncClient(new AnalyzeTextsImpl(service),
            serviceVersion);
        this.analyzeActionsAsyncClient = new AnalyzeActionsAsyncClient(new AnalyzeTextsImpl(service), serviceVersion);
        this.labelClassifyAsyncClient = new LabelClassifyAsyncClient(new AnalyzeTextsImpl(service), serviceVersion);
        this.dynamicClassificationAsyncClient = new DynamicClassificationAsyncClient(service, serviceVersion);
    }

    /**
     * Gets default country hint code.
     *
     * @return The default country hint code
     */
    public String getDefaultCountryHint() {
        return defaultCountryHint;
    }

    /**
     * Gets default language when the builder is setup.
     *
     * @return The default language
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Returns the detected language and a confidence score between zero and one. Scores close to one indicate 100%
     * certainty that the identified language is true.
     *
     * This method will use the default country hint that sets up in
     * {@link TextAnalyticsClientBuilder#defaultCountryHint(String)}. If none is specified, service will use 'US' as
     * the country hint.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a document. Subscribes to the call asynchronously and prints out the detected language
     * details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string -->
     * <pre>
     * String document = &quot;Bonjour tout le monde&quot;;
     * textAnalyticsAsyncClient.detectLanguage&#40;document&#41;.subscribe&#40;detectedLanguage -&gt;
     *     System.out.printf&#40;&quot;Detected language name: %s, ISO 6391 Name: %s, confidence score: %f.%n&quot;,
     *         detectedLanguage.getName&#40;&#41;, detectedLanguage.getIso6391Name&#40;&#41;, detectedLanguage.getConfidenceScore&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link Mono} containing the {@link DetectedLanguage detected language} of the document.
     *
     * @throws NullPointerException if the document is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectedLanguage> detectLanguage(String document) {
        return detectLanguage(document, defaultCountryHint);
    }

    /**
     * Returns a {@link Response} contains the detected language and a confidence score between zero and one. Scores
     * close to one indicate 100% certainty that the identified language is true.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language with http response in a document with a provided country hint. Subscribes to the call
     * asynchronously and prints out the detected language details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string-string -->
     * <pre>
     * String document = &quot;This text is in English&quot;;
     * String countryHint = &quot;US&quot;;
     * textAnalyticsAsyncClient.detectLanguage&#40;document, countryHint&#41;.subscribe&#40;detectedLanguage -&gt;
     *     System.out.printf&#40;&quot;Detected language name: %s, ISO 6391 Name: %s, confidence score: %f.%n&quot;,
     *         detectedLanguage.getName&#40;&#41;, detectedLanguage.getIso6391Name&#40;&#41;, detectedLanguage.getConfidenceScore&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguage#string-string -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param countryHint Accepts 2-letter country codes specified by ISO 3166-1 alpha-2. Defaults to "US" if not
     * specified. To remove this behavior you can reset this parameter by setting this value to empty string
     * {@code countryHint} = "" or "none".
     *
     * @return A {@link Mono} contains a {@link DetectedLanguage detected language} of the document.
     *
     * @throws NullPointerException if the document is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectedLanguage> detectLanguage(String document, String countryHint) {
        try {
            Objects.requireNonNull(document, "'document' cannot be null.");
            return detectLanguageBatch(Collections.singletonList(document), countryHint, null)
                .map(detectLanguageResultCollection ->  {
                    DetectedLanguage detectedLanguage = null;
                    for (DetectLanguageResult detectLanguageResult : detectLanguageResultCollection) {
                        if (detectLanguageResult.isError()) {
                            throw logger.logExceptionAsError(toTextAnalyticsException(detectLanguageResult.getError()));
                        }
                        detectedLanguage = detectLanguageResult.getPrimaryLanguage();
                    }
                    // When the detected language result collection is empty,
                    // return empty result for the empty collection returned by the service.
                    return detectedLanguage;
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the detected language for each of documents with the provided country hint and request option.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a list of documents with a provided country hint and request option for the batch.
     * Subscribes to the call asynchronously and prints out the detected language details when a response is received.
     * </p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;This is written in English&quot;,
     *     &quot;Este es un documento  escrito en Español.&quot;
     * &#41;;
     * textAnalyticsAsyncClient.detectLanguageBatch&#40;documents, &quot;US&quot;, null&#41;.subscribe&#40;
     *     batchResult -&gt; &#123;
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = batchResult.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *         &#47;&#47; Batch result of languages
     *         for &#40;DetectLanguageResult detectLanguageResult : batchResult&#41; &#123;
     *             DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage&#40;&#41;;
     *             System.out.printf&#40;&quot;Detected language name: %s, ISO 6391 Name: %s, confidence score: %f.%n&quot;,
     *                 detectedLanguage.getName&#40;&#41;, detectedLanguage.getIso6391Name&#40;&#41;,
     *                 detectedLanguage.getConfidenceScore&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-String-TextAnalyticsRequestOptions -->
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
     * @return A {@link Mono} contains a {@link DetectLanguageResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in
     *   service API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DetectLanguageResultCollection> detectLanguageBatch(
        Iterable<String> documents, String countryHint, TextAnalyticsRequestOptions options) {

        if (countryHint != null && "none".equalsIgnoreCase(countryHint)) {
            countryHint = "";
        }
        final String finalCountryHint = countryHint;
        try {
            return detectLanguageBatchWithResponse(
                mapByIndex(documents, (index, value) -> new DetectLanguageInput(index, value, finalCountryHint)),
                options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the detected language for a batch of {@link DetectLanguageInput document} with provided request options.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Detects language in a batch of {@link DetectLanguageInput document} with provided request options. Subscribes
     * to the call asynchronously and prints out the detected language details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;DetectLanguageInput&gt; detectLanguageInputs1 = Arrays.asList&#40;
     *     new DetectLanguageInput&#40;&quot;1&quot;, &quot;This is written in English.&quot;, &quot;US&quot;&#41;,
     *     new DetectLanguageInput&#40;&quot;2&quot;, &quot;Este es un documento  escrito en Español.&quot;, &quot;ES&quot;&#41;
     * &#41;;
     *
     * TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     *
     * textAnalyticsAsyncClient.detectLanguageBatchWithResponse&#40;detectLanguageInputs1, requestOptions&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         &#47;&#47; Response's status code
     *         System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *
     *         DetectLanguageResultCollection resultCollection = response.getValue&#40;&#41;;
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *         &#47;&#47; Batch result of languages
     *         for &#40;DetectLanguageResult detectLanguageResult : resultCollection&#41; &#123;
     *             DetectedLanguage detectedLanguage = detectLanguageResult.getPrimaryLanguage&#40;&#41;;
     *             System.out.printf&#40;&quot;Detected language name: %s, ISO 6391 Name: %s, confidence score: %f.%n&quot;,
     *                 detectedLanguage.getName&#40;&#41;, detectedLanguage.getIso6391Name&#40;&#41;,
     *                 detectedLanguage.getConfidenceScore&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.detectLanguageBatch#Iterable-TextAnalyticsRequestOptions -->
     *
     * @param documents The list of {@link DetectLanguageInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} contains a {@link Response} which contains a {@link DetectLanguageResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in
     *   service API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DetectLanguageResultCollection>> detectLanguageBatchWithResponse(
        Iterable<DetectLanguageInput> documents, TextAnalyticsRequestOptions options) {
        return detectLanguageAsyncClient.detectLanguageBatch(documents, options);
    }

    // Categorized Entity

    /**
     * Returns a list of general categorized entities in the provided document.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/taner">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a document. Subscribes to the call asynchronously and prints out the recognized entity
     * details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string -->
     * <pre>
     * String document = &quot;Satya Nadella is the CEO of Microsoft&quot;;
     * textAnalyticsAsyncClient.recognizeEntities&#40;document&#41;
     *     .subscribe&#40;entityCollection -&gt; entityCollection.forEach&#40;entity -&gt;
     *         System.out.printf&#40;&quot;Recognized categorized entity: %s, category: %s, confidence score: %f.%n&quot;,
     *         entity.getText&#40;&#41;,
     *         entity.getCategory&#40;&#41;,
     *         entity.getConfidenceScore&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string -->
     *
     * @param document The document to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link Mono} contains a {@link CategorizedEntityCollection recognized categorized entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CategorizedEntityCollection> recognizeEntities(String document) {
        return recognizeEntities(document, defaultLanguage);
    }

    /**
     * Returns a list of general categorized entities in the provided document.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/taner">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a document with provided language code. Subscribes to the call asynchronously and prints
     * out the entity details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string-string -->
     * <pre>
     * String document = &quot;Satya Nadella is the CEO of Microsoft&quot;;
     * textAnalyticsAsyncClient.recognizeEntities&#40;document, &quot;en&quot;&#41;
     *     .subscribe&#40;entityCollection -&gt; entityCollection.forEach&#40;entity -&gt;
     *         System.out.printf&#40;&quot;Recognized categorized entity: %s, category: %s, confidence score: %f.%n&quot;,
     *         entity.getText&#40;&#41;,
     *         entity.getCategory&#40;&#41;,
     *         entity.getConfidenceScore&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeEntities#string-string -->
     *
     * @param document the text to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     *
     * @return A {@link Mono} contains a {@link CategorizedEntityCollection recognized categorized entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CategorizedEntityCollection> recognizeEntities(String document, String language) {
        return recognizeEntityAsyncClient.recognizeEntities(document, language);
    }

    /**
     * Returns a list of general categorized entities for the provided list of documents with the provided language code
     * and request options.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a document with the provided language code. Subscribes to the call asynchronously and
     * prints out the entity details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;I had a wonderful trip to Seattle last week.&quot;, &quot;I work at Microsoft.&quot;&#41;;
     *
     * textAnalyticsAsyncClient.recognizeEntitiesBatch&#40;documents, &quot;en&quot;, null&#41;
     *     .subscribe&#40;batchResult -&gt; &#123;
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = batchResult.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *         &#47;&#47; Batch Result of entities
     *         batchResult.forEach&#40;recognizeEntitiesResult -&gt;
     *             recognizeEntitiesResult.getEntities&#40;&#41;.forEach&#40;entity -&gt; System.out.printf&#40;
     *                 &quot;Recognized categorized entity: %s, category: %s, confidence score: %f.%n&quot;,
     *                     entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of documents to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as
     * default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} contains a {@link RecognizeEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in
     *   service API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecognizeEntitiesResultCollection> recognizeEntitiesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        try {
            return recognizeEntitiesBatchWithResponse(
                mapByIndex(documents, (index, value) -> {
                    final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                    textDocumentInput.setLanguage(language);
                    return textDocumentInput;
                }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of general categorized entities for the provided list of {@link TextDocumentInput document} with
     * provided request options.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize entities in a list of {@link TextDocumentInput document}. Subscribes to the call asynchronously
     * and prints out the entity details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs1 = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;0&quot;, &quot;I had a wonderful trip to Seattle last week.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;I work at Microsoft.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;&#41;;
     *
     * TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     *
     * textAnalyticsAsyncClient.recognizeEntitiesBatchWithResponse&#40;textDocumentInputs1, requestOptions&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         &#47;&#47; Response's status code
     *         System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         RecognizeEntitiesResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *         resultCollection.forEach&#40;recognizeEntitiesResult -&gt;
     *             recognizeEntitiesResult.getEntities&#40;&#41;.forEach&#40;entity -&gt; System.out.printf&#40;
     *                 &quot;Recognized categorized entity: %s, category: %s, confidence score: %f.%n&quot;,
     *                 entity.getText&#40;&#41;,
     *                 entity.getCategory&#40;&#41;,
     *                 entity.getConfidenceScore&#40;&#41;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeCategorizedEntitiesBatch#Iterable-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to recognize entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} contains a {@link Response} which contains a {@link RecognizeEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in
     *   service API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecognizeEntitiesResultCollection>> recognizeEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return recognizeEntityAsyncClient.recognizeEntitiesBatch(documents, options);
    }

    // PII Entity

    /**
     * Returns a list of Personally Identifiable Information(PII) entities in the provided document.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/azsdk/language/pii">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>. This method will use the
     * default language that is set using {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is
     * specified, service will use 'en' as the language.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize the PII entities details in a document.
     * Subscribes to the call asynchronously and prints out the recognized entity details when a response is
     * received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string -->
     * <pre>
     * String document = &quot;My SSN is 859-98-0987&quot;;
     * textAnalyticsAsyncClient.recognizePiiEntities&#40;document&#41;.subscribe&#40;piiEntityCollection -&gt; &#123;
     *     System.out.printf&#40;&quot;Redacted Text: %s%n&quot;, piiEntityCollection.getRedactedText&#40;&#41;&#41;;
     *     piiEntityCollection.forEach&#40;entity -&gt; System.out.printf&#40;
     *         &quot;Recognized Personally Identifiable Information entity: %s, entity category: %s,&quot;
     *             + &quot; entity subcategory: %s, confidence score: %f.%n&quot;,
     *         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string -->
     *
     * @param document The document to recognize PII entities details for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link Mono} contains a {@link PiiEntityCollection recognized PII entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     * @throws UnsupportedOperationException if {@code recognizePiiEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code recognizePiiEntities} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PiiEntityCollection> recognizePiiEntities(String document) {
        return recognizePiiEntities(document, defaultLanguage);
    }

    /**
     * Returns a list of Personally Identifiable Information(PII) entities in the provided document
     * with provided language code.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/azsdk/language/pii">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize the PII entities details in a document with provided language code.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string -->
     * <pre>
     * String document = &quot;My SSN is 859-98-0987&quot;;
     * textAnalyticsAsyncClient.recognizePiiEntities&#40;document, &quot;en&quot;&#41;
     *     .subscribe&#40;piiEntityCollection -&gt; &#123;
     *         System.out.printf&#40;&quot;Redacted Text: %s%n&quot;, piiEntityCollection.getRedactedText&#40;&#41;&#41;;
     *         piiEntityCollection.forEach&#40;entity -&gt; System.out.printf&#40;
     *             &quot;Recognized Personally Identifiable Information entity: %s, entity category: %s,&quot;
     *                 + &quot; entity subcategory: %s, confidence score: %f.%n&quot;,
     *             entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string -->
     *
     * @param document the text to recognize PII entities details for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     *
     * @return A {@link Mono} contains a {@link PiiEntityCollection recognized PII entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     * @throws UnsupportedOperationException if {@code recognizePiiEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code recognizePiiEntities} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PiiEntityCollection> recognizePiiEntities(String document, String language) {
        return recognizePiiEntityAsyncClient.recognizePiiEntities(document, language, null);
    }

    /**
     * Returns a list of Personally Identifiable Information(PII) entities in the provided document
     * with provided language code.
     *
     * For a list of supported entity types, check: <a href="https://aka.ms/azsdk/language/pii">this</a>.
     * For a list of enabled languages, check: <a href="https://aka.ms/talangs">this</a>.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize the PII entities details in a document with provided language code and
     * {@link RecognizePiiEntitiesOptions}.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string-RecognizePiiEntitiesOptions -->
     * <pre>
     * String document = &quot;My SSN is 859-98-0987&quot;;
     * textAnalyticsAsyncClient.recognizePiiEntities&#40;document, &quot;en&quot;,
     *     new RecognizePiiEntitiesOptions&#40;&#41;.setDomainFilter&#40;PiiEntityDomain.PROTECTED_HEALTH_INFORMATION&#41;&#41;
     *     .subscribe&#40;piiEntityCollection -&gt; &#123;
     *         System.out.printf&#40;&quot;Redacted Text: %s%n&quot;, piiEntityCollection.getRedactedText&#40;&#41;&#41;;
     *         piiEntityCollection.forEach&#40;entity -&gt; System.out.printf&#40;
     *             &quot;Recognized Personally Identifiable Information entity: %s, entity category: %s,&quot;
     *                 + &quot; entity subcategory: %s, confidence score: %f.%n&quot;,
     *             entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntities#string-string-RecognizePiiEntitiesOptions -->
     *
     * @param document the text to recognize PII entities details for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2-letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return A {@link Mono} contains a {@link PiiEntityCollection recognized PII entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     * @throws UnsupportedOperationException if {@code recognizePiiEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code recognizePiiEntities} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PiiEntityCollection> recognizePiiEntities(String document, String language,
        RecognizePiiEntitiesOptions options) {
        return recognizePiiEntityAsyncClient.recognizePiiEntities(document, language, options);
    }

    /**
     * Returns a list of Personally Identifiable Information(PII) entities for the provided list of documents with
     * the provided language code and request options.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize Personally Identifiable Information entities in a document with the provided language code.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-String-RecognizePiiEntitiesOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;My SSN is 859-98-0987.&quot;,
     *     &quot;Visa card 0111 1111 1111 1111.&quot;
     * &#41;;
     *
     * &#47;&#47; Show statistics and model version
     * RecognizePiiEntitiesOptions requestOptions = new RecognizePiiEntitiesOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;
     *     .setModelVersion&#40;&quot;latest&quot;&#41;;
     *
     * textAnalyticsAsyncClient.recognizePiiEntitiesBatch&#40;documents, &quot;en&quot;, requestOptions&#41;
     *     .subscribe&#40;piiEntitiesResults -&gt; &#123;
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = piiEntitiesResults.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *         piiEntitiesResults.forEach&#40;recognizePiiEntitiesResult -&gt; &#123;
     *             PiiEntityCollection piiEntityCollection = recognizePiiEntitiesResult.getEntities&#40;&#41;;
     *             System.out.printf&#40;&quot;Redacted Text: %s%n&quot;, piiEntityCollection.getRedactedText&#40;&#41;&#41;;
     *             piiEntityCollection.forEach&#40;entity -&gt; System.out.printf&#40;
     *                 &quot;Recognized Personally Identifiable Information entity: %s, entity category: %s,&quot;
     *                     + &quot; entity subcategory: %s, confidence score: %f.%n&quot;,
     *                 entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-String-RecognizePiiEntitiesOptions -->
     *
     * @param documents A list of documents to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language. If not set, uses "en" for English as default.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return A {@link Mono} contains a {@link RecognizePiiEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code recognizePiiEntitiesBatch} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code recognizePiiEntitiesBatch} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecognizePiiEntitiesResultCollection> recognizePiiEntitiesBatch(
        Iterable<String> documents, String language, RecognizePiiEntitiesOptions options) {
        try {
            inputDocumentsValidation(documents);
            return recognizePiiEntitiesBatchWithResponse(
                mapByIndex(documents, (index, value) -> {
                    final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                    textDocumentInput.setLanguage(language);
                    return textDocumentInput;
                }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of Personally Identifiable Information entities for the provided list of
     * {@link TextDocumentInput document} with provided request options.
     *
     * <p><strong>Code sample</strong></p>
     * <p>Recognize the PII entities details with http response in a list of {@link TextDocumentInput document}
     * with provided request options.
     * Subscribes to the call asynchronously and prints out the entity details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-RecognizePiiEntitiesOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs1 = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;0&quot;, &quot;My SSN is 859-98-0987.&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;Visa card 0111 1111 1111 1111.&quot;&#41;&#41;;
     *
     * &#47;&#47; Show statistics and model version
     * RecognizePiiEntitiesOptions requestOptions = new RecognizePiiEntitiesOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;
     *     .setModelVersion&#40;&quot;latest&quot;&#41;;
     *
     * textAnalyticsAsyncClient.recognizePiiEntitiesBatchWithResponse&#40;textDocumentInputs1, requestOptions&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         RecognizePiiEntitiesResultCollection piiEntitiesResults = response.getValue&#40;&#41;;
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = piiEntitiesResults.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *         piiEntitiesResults.forEach&#40;recognizePiiEntitiesResult -&gt; &#123;
     *             PiiEntityCollection piiEntityCollection = recognizePiiEntitiesResult.getEntities&#40;&#41;;
     *             System.out.printf&#40;&quot;Redacted Text: %s%n&quot;, piiEntityCollection.getRedactedText&#40;&#41;&#41;;
     *             piiEntityCollection.forEach&#40;entity -&gt; System.out.printf&#40;
     *                 &quot;Recognized Personally Identifiable Information entity: %s, entity category: %s,&quot;
     *                     + &quot; entity subcategory: %s, confidence score: %f.%n&quot;,
     *                 entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizePiiEntitiesBatch#Iterable-RecognizePiiEntitiesOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to recognize PII entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The additional configurable {@link RecognizePiiEntitiesOptions options} that may be passed when
     * recognizing PII entities.
     *
     * @return A {@link Mono} contains a {@link Response} which contains a {@link RecognizePiiEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code recognizePiiEntitiesBatchWithResponse} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code recognizePiiEntitiesBatchWithResponse} is only available for
     *   API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecognizePiiEntitiesResultCollection>> recognizePiiEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> documents, RecognizePiiEntitiesOptions options) {
        return recognizePiiEntityAsyncClient.recognizePiiEntitiesBatch(documents, options);
    }

    // Linked Entities

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided document. See
     * <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Recognize linked entities in a document. Subscribes to the call asynchronously and prints out the
     * entity details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string -->
     * <pre>
     * String document = &quot;Old Faithful is a geyser at Yellowstone Park.&quot;;
     * textAnalyticsAsyncClient.recognizeLinkedEntities&#40;document&#41;.subscribe&#40;
     *     linkedEntityCollection -&gt; linkedEntityCollection.forEach&#40;linkedEntity -&gt; &#123;
     *         System.out.println&#40;&quot;Linked Entities:&quot;&#41;;
     *         System.out.printf&#40;&quot;Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n&quot;,
     *             linkedEntity.getName&#40;&#41;, linkedEntity.getDataSourceEntityId&#40;&#41;, linkedEntity.getUrl&#40;&#41;,
     *             linkedEntity.getDataSource&#40;&#41;&#41;;
     *         linkedEntity.getMatches&#40;&#41;.forEach&#40;entityMatch -&gt; System.out.printf&#40;
     *             &quot;Matched entity: %s, confidence score: %f.%n&quot;,
     *             entityMatch.getText&#40;&#41;, entityMatch.getConfidenceScore&#40;&#41;&#41;&#41;;
     *     &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string -->
     *
     * @param document The document to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link Mono} contains a {@link LinkedEntityCollection recognized linked entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LinkedEntityCollection> recognizeLinkedEntities(String document) {
        return recognizeLinkedEntities(document, defaultLanguage);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the provided document. See
     * <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * <p>Recognize linked entities in a text with provided language code. Subscribes to the call asynchronously
     * and prints out the entity details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string-string -->
     * <pre>
     * String document = &quot;Old Faithful is a geyser at Yellowstone Park.&quot;;
     * textAnalyticsAsyncClient.recognizeLinkedEntities&#40;document, &quot;en&quot;&#41;.subscribe&#40;
     *     linkedEntityCollection -&gt; linkedEntityCollection.forEach&#40;linkedEntity -&gt; &#123;
     *         System.out.println&#40;&quot;Linked Entities:&quot;&#41;;
     *         System.out.printf&#40;&quot;Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n&quot;,
     *             linkedEntity.getName&#40;&#41;, linkedEntity.getDataSourceEntityId&#40;&#41;, linkedEntity.getUrl&#40;&#41;,
     *             linkedEntity.getDataSource&#40;&#41;&#41;;
     *         linkedEntity.getMatches&#40;&#41;.forEach&#40;entityMatch -&gt; System.out.printf&#40;
     *             &quot;Matched entity: %s, confidence score: %f.%n&quot;,
     *             entityMatch.getText&#40;&#41;, entityMatch.getConfidenceScore&#40;&#41;&#41;&#41;;
     *     &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntities#string-string -->
     *
     * @param document The document to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} contains a {@link LinkedEntityCollection recognized linked entities collection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LinkedEntityCollection> recognizeLinkedEntities(String document, String language) {
        return recognizeLinkedEntityAsyncClient.recognizeLinkedEntities(document, language);
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of documents with
     * provided language code and request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> for supported languages in Text Analytics API.
     *
     * <p>Recognize linked entities in a list of documents with provided language code. Subscribes to the call
     * asynchronously and prints out the entity details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;Old Faithful is a geyser at Yellowstone Park.&quot;,
     *     &quot;Mount Shasta has lenticular clouds.&quot;
     * &#41;;
     *
     * textAnalyticsAsyncClient.recognizeLinkedEntitiesBatch&#40;documents, &quot;en&quot;, null&#41;
     *     .subscribe&#40;batchResult -&gt; &#123;
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = batchResult.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *         batchResult.forEach&#40;recognizeLinkedEntitiesResult -&gt;
     *             recognizeLinkedEntitiesResult.getEntities&#40;&#41;.forEach&#40;linkedEntity -&gt; &#123;
     *                 System.out.println&#40;&quot;Linked Entities:&quot;&#41;;
     *                 System.out.printf&#40;&quot;Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n&quot;,
     *                     linkedEntity.getName&#40;&#41;, linkedEntity.getDataSourceEntityId&#40;&#41;, linkedEntity.getUrl&#40;&#41;,
     *                     linkedEntity.getDataSource&#40;&#41;&#41;;
     *                 linkedEntity.getMatches&#40;&#41;.forEach&#40;entityMatch -&gt; System.out.printf&#40;
     *                     &quot;Matched entity: %s, confidence score: %f.%n&quot;,
     *                     entityMatch.getText&#40;&#41;, entityMatch.getConfidenceScore&#40;&#41;&#41;&#41;;
     *             &#125;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of documents to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} contains a {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in service
     *   API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for API
     *   version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RecognizeLinkedEntitiesResultCollection> recognizeLinkedEntitiesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        try {
            return recognizeLinkedEntitiesBatchWithResponse(mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of recognized entities with links to a well-known knowledge base for the list of
     * {@link TextDocumentInput document} with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p>Recognize linked  entities in a list of {@link TextDocumentInput document} and provided request options to
     * show statistics. Subscribes to the call asynchronously and prints out the entity details when a response is
     * received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs1 = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;0&quot;, &quot;Old Faithful is a geyser at Yellowstone Park.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;Mount Shasta has lenticular clouds.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;&#41;;
     *
     * TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     *
     * textAnalyticsAsyncClient.recognizeLinkedEntitiesBatchWithResponse&#40;textDocumentInputs1, requestOptions&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         &#47;&#47; Response's status code
     *         System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         RecognizeLinkedEntitiesResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *         resultCollection.forEach&#40;recognizeLinkedEntitiesResult -&gt;
     *             recognizeLinkedEntitiesResult.getEntities&#40;&#41;.forEach&#40;linkedEntity -&gt; &#123;
     *                 System.out.println&#40;&quot;Linked Entities:&quot;&#41;;
     *                 System.out.printf&#40;&quot;Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n&quot;,
     *                     linkedEntity.getName&#40;&#41;, linkedEntity.getDataSourceEntityId&#40;&#41;, linkedEntity.getUrl&#40;&#41;,
     *                     linkedEntity.getDataSource&#40;&#41;&#41;;
     *                 linkedEntity.getMatches&#40;&#41;.forEach&#40;entityMatch -&gt; System.out.printf&#40;
     *                     &quot;Matched entity: %s, confidence score: %.2f.%n&quot;,
     *                     entityMatch.getText&#40;&#41;, entityMatch.getConfidenceScore&#40;&#41;&#41;&#41;;
     *             &#125;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.recognizeLinkedEntitiesBatch#Iterable-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to recognize linked entities for.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} contains a {@link Response} which contains a
     * {@link RecognizeLinkedEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in service
     *   API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for API
     *   version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RecognizeLinkedEntitiesResultCollection>> recognizeLinkedEntitiesBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return recognizeLinkedEntityAsyncClient.recognizeLinkedEntitiesBatch(documents, options);
    }

    // Key Phrases

    /**
     * Returns a list of strings denoting the key phrases in the document.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p>Extract key phrases in a document. Subscribes to the call asynchronously and prints out the
     * key phrases when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string -->
     * <pre>
     * System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     * textAnalyticsAsyncClient.extractKeyPhrases&#40;&quot;Bonjour tout le monde&quot;&#41;.subscribe&#40;keyPhrase -&gt;
     *     System.out.printf&#40;&quot;%s.%n&quot;, keyPhrase&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link Mono} contains a {@link KeyPhrasesCollection}.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhrasesCollection> extractKeyPhrases(String document) {
        return extractKeyPhrases(document, defaultLanguage);
    }

    /**
     * Returns a list of strings denoting the key phrases in the document.
     *
     * See <a href="https://aka.ms/talangs">this</a> for the list of enabled languages.
     *
     * <p>Extract key phrases in a document with a provided language code. Subscribes to the call asynchronously and
     * prints out the key phrases when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string-string -->
     * <pre>
     * System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     * textAnalyticsAsyncClient.extractKeyPhrases&#40;&quot;Bonjour tout le monde&quot;, &quot;fr&quot;&#41;
     *     .subscribe&#40;keyPhrase -&gt; System.out.printf&#40;&quot;%s.%n&quot;, keyPhrase&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrases#string-string -->
     *
     * @param document The document to be analyzed. For text length limits, maximum batch size, and supported text
     * encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} contains a {@link KeyPhrasesCollection}
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhrasesCollection> extractKeyPhrases(String document, String language) {
        return extractKeyPhraseAsyncClient.extractKeyPhrasesSingleText(document, language);
    }

    /**
     * Returns a list of strings denoting the key phrases in the document with provided language code and request
     * options.
     *
     * See <a href="https://aka.ms/talangs">this</a> for the list of enabled languages.
     *
     * <p>Extract key phrases in a list of documents with a provided language and request options. Subscribes to the
     * call asynchronously and prints out the key phrases when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;Hello world. This is some input text that I love.&quot;,
     *     &quot;Bonjour tout le monde&quot;&#41;;
     *
     * textAnalyticsAsyncClient.extractKeyPhrasesBatch&#40;documents, &quot;en&quot;, null&#41;.subscribe&#40;
     *     extractKeyPhraseResults -&gt; &#123;
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = extractKeyPhraseResults.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *         extractKeyPhraseResults.forEach&#40;extractKeyPhraseResult -&gt; &#123;
     *             System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     *             extractKeyPhraseResult.getKeyPhrases&#40;&#41;.forEach&#40;keyPhrase -&gt; System.out.printf&#40;&quot;%s.%n&quot;, keyPhrase&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-String-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} contains a {@link ExtractKeyPhrasesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in service
     *   API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for API
     *   version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ExtractKeyPhrasesResultCollection> extractKeyPhrasesBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        try {
            return extractKeyPhrasesBatchWithResponse(
                mapByIndex(documents, (index, value) -> {
                    final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                    textDocumentInput.setLanguage(language);
                    return textDocumentInput;
                }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a list of strings denoting the key phrases in the document with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> for the list of enabled languages.
     *
     * <p>Extract key phrases in a list of {@link TextDocumentInput document} with provided request options.
     * Subscribes to the call asynchronously and prints out the key phrases when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs1 = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;0&quot;, &quot;I had a wonderful trip to Seattle last week.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;I work at Microsoft.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;&#41;;
     *
     * TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     *
     * textAnalyticsAsyncClient.extractKeyPhrasesBatchWithResponse&#40;textDocumentInputs1, requestOptions&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         &#47;&#47; Response's status code
     *         System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         ExtractKeyPhrasesResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *         for &#40;ExtractKeyPhraseResult extractKeyPhraseResult : resultCollection&#41; &#123;
     *             System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     *             for &#40;String keyPhrase : extractKeyPhraseResult.getKeyPhrases&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;%s.%n&quot;, keyPhrase&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.extractKeyPhrasesBatch#Iterable-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} contains a {@link Response} that contains a {@link ExtractKeyPhrasesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws UnsupportedOperationException if {@link TextAnalyticsRequestOptions#isServiceLogsDisabled()} is true in service
     *   API version {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} is only available for API
     *   version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ExtractKeyPhrasesResultCollection>> extractKeyPhrasesBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return extractKeyPhraseAsyncClient.extractKeyPhrasesWithResponse(documents, options);
    }

    // Sentiment

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiment in a document. Subscribes to the call asynchronously and prints out the
     * sentiment details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string -->
     * <pre>
     * String document = &quot;The hotel was dark and unclean.&quot;;
     * textAnalyticsAsyncClient.analyzeSentiment&#40;document&#41;.subscribe&#40;documentSentiment -&gt; &#123;
     *     System.out.printf&#40;&quot;Recognized document sentiment: %s.%n&quot;, documentSentiment.getSentiment&#40;&#41;&#41;;
     *
     *     for &#40;SentenceSentiment sentenceSentiment : documentSentiment.getSentences&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;
     *             &quot;Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, &quot;
     *                 + &quot;negative score: %.2f.%n&quot;,
     *             sentenceSentiment.getSentiment&#40;&#41;,
     *             sentenceSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *             sentenceSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *             sentenceSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#string -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     *
     * @return A {@link Mono} contains the {@link DocumentSentiment analyzed document sentiment} of the document.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String document) {
        return analyzeSentiment(document, defaultLanguage);
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Analyze the sentiments in a document with a provided language representation. Subscribes to the call
     * asynchronously and prints out the sentiment details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#String-String -->
     * <pre>
     * String document = &quot;The hotel was dark and unclean.&quot;;
     * textAnalyticsAsyncClient.analyzeSentiment&#40;document, &quot;en&quot;&#41;
     *     .subscribe&#40;documentSentiment -&gt; &#123;
     *         System.out.printf&#40;&quot;Recognized sentiment label: %s.%n&quot;, documentSentiment.getSentiment&#40;&#41;&#41;;
     *         for &#40;SentenceSentiment sentenceSentiment : documentSentiment.getSentences&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;Recognized sentence sentiment: %s, positive score: %.2f, neutral score: %.2f, &quot;
     *                     + &quot;negative score: %.2f.%n&quot;,
     *                 sentenceSentiment.getSentiment&#40;&#41;,
     *                 sentenceSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *                 sentenceSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *                 sentenceSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#String-String -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     *
     * @return A {@link Mono} contains the {@link DocumentSentiment analyzed document sentiment} of the document.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String document, String language) {
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
     * representation and {@link AnalyzeSentimentOptions} options. Subscribes to the call asynchronously and prints
     * out the sentiment and sentence opinions details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#String-String-AnalyzeSentimentOptions -->
     * <pre>
     * textAnalyticsAsyncClient.analyzeSentiment&#40;&quot;The hotel was dark and unclean.&quot;, &quot;en&quot;,
     *     new AnalyzeSentimentOptions&#40;&#41;.setIncludeOpinionMining&#40;true&#41;&#41;
     *     .subscribe&#40;documentSentiment -&gt; &#123;
     *         for &#40;SentenceSentiment sentenceSentiment : documentSentiment.getSentences&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;&#92;tSentence sentiment: %s%n&quot;, sentenceSentiment.getSentiment&#40;&#41;&#41;;
     *             sentenceSentiment.getOpinions&#40;&#41;.forEach&#40;opinion -&gt; &#123;
     *                 TargetSentiment targetSentiment = opinion.getTarget&#40;&#41;;
     *                 System.out.printf&#40;&quot;&#92;tTarget sentiment: %s, target text: %s%n&quot;,
     *                     targetSentiment.getSentiment&#40;&#41;, targetSentiment.getText&#40;&#41;&#41;;
     *                 for &#40;AssessmentSentiment assessmentSentiment : opinion.getAssessments&#40;&#41;&#41; &#123;
     *                     System.out.printf&#40;&quot;&#92;t&#92;t'%s' sentiment because of &#92;&quot;%s&#92;&quot;. Is the assessment negated: %s.%n&quot;,
     *                         assessmentSentiment.getSentiment&#40;&#41;, assessmentSentiment.getText&#40;&#41;,
     *                         assessmentSentiment.isNegated&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentiment#String-String-AnalyzeSentimentOptions -->
     *
     * @param document The document to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the text. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     *
     * @return A {@link Mono} contains the {@link DocumentSentiment analyzed document sentiment} of the document.
     *
     * @throws NullPointerException if {@code document} is null.
     * @throws TextAnalyticsException if the response returned with an {@link TextAnalyticsError error}.
     * @throws UnsupportedOperationException if {@link AnalyzeSentimentOptions#isServiceLogsDisabled()} or
     *   {@link AnalyzeSentimentOptions#isIncludeOpinionMining()} is true in service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} and {@code includeOpinionMining} are only
     *   available for API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DocumentSentiment> analyzeSentiment(String document, String language, AnalyzeSentimentOptions options) {
        try {
            Objects.requireNonNull(document, "'document' cannot be null.");
            return analyzeSentimentBatch(Collections.singletonList(document), language, options)
                .map(sentimentResultCollection -> {
                    DocumentSentiment documentSentiment = null;
                    for (AnalyzeSentimentResult sentimentResult : sentimentResultCollection) {
                        if (sentimentResult.isError()) {
                            throw logger.logExceptionAsError(toTextAnalyticsException(sentimentResult.getError()));
                        }
                        documentSentiment = sentimentResult.getDocumentSentiment();
                    }
                    // When the sentiment result collection is empty,
                    // return empty result for the empty collection returned by the service.
                    return documentSentiment;
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of documents with provided language code and request options. Subscribes to the
     * call asynchronously and prints out the sentiment details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;The hotel was dark and unclean.&quot;,
     *     &quot;The restaurant had amazing gnocchi.&quot;
     * &#41;;
     *
     * textAnalyticsAsyncClient.analyzeSentimentBatch&#40;documents, &quot;en&quot;,
     *     new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;&#41;.subscribe&#40;
     *         response -&gt; &#123;
     *             &#47;&#47; Batch statistics
     *             TextDocumentBatchStatistics batchStatistics = response.getStatistics&#40;&#41;;
     *             System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *                 batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *             response.forEach&#40;analyzeSentimentResult -&gt; &#123;
     *                 System.out.printf&#40;&quot;Document ID: %s%n&quot;, analyzeSentimentResult.getId&#40;&#41;&#41;;
     *                 DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment&#40;&#41;;
     *                 System.out.printf&#40;&quot;Recognized document sentiment: %s.%n&quot;, documentSentiment.getSentiment&#40;&#41;&#41;;
     *                 documentSentiment.getSentences&#40;&#41;.forEach&#40;sentenceSentiment -&gt;
     *                     System.out.printf&#40;&quot;Recognized sentence sentiment: %s, positive score: %.2f, &quot;
     *                             + &quot;neutral score: %.2f, negative score: %.2f.%n&quot;,
     *                         sentenceSentiment.getSentiment&#40;&#41;,
     *                         sentenceSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *                         sentenceSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *                         sentenceSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-String-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} contains a {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     *
     * @deprecated Please use the {@link #analyzeSentimentBatch(Iterable, String, AnalyzeSentimentOptions)}.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnalyzeSentimentResultCollection> analyzeSentimentBatch(
        Iterable<String> documents, String language, TextAnalyticsRequestOptions options) {
        return analyzeSentimentBatch(documents, language, new AnalyzeSentimentOptions()
            .setIncludeStatistics(options == null ? false : options.isIncludeStatistics())
            .setModelVersion(options == null ? null : options.getModelVersion()));
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
     * representation and {@link AnalyzeSentimentOptions} options. Subscribes to the call asynchronously and prints out
     * the sentiment and sentence opinions details when a response is received.</p>
     *
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
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     *
     * @return A {@link Mono} contains a {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link AnalyzeSentimentOptions#isServiceLogsDisabled()} or
     *   {@link AnalyzeSentimentOptions#isIncludeOpinionMining()} is true in service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} and {@code includeOpinionMining} are only
     *   available for API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnalyzeSentimentResultCollection> analyzeSentimentBatch(Iterable<String> documents,
        String language, AnalyzeSentimentOptions options) {
        try {
            return analyzeSentimentBatchWithResponse(
                mapByIndex(documents, (index, value) -> {
                    final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                    textDocumentInput.setLanguage(language);
                    return textDocumentInput;
                }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns a sentiment prediction, as well as confidence scores for each sentiment label (Positive, Negative, and
     * Neutral) for the document and each sentence within it.
     *
     * <p>Analyze sentiment in a list of {@link TextDocumentInput document} with provided request options. Subscribes
     * to the call asynchronously and prints out the sentiment details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs1 = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;0&quot;, &quot;The hotel was dark and unclean.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;The restaurant had amazing gnocchi.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;&#41;;
     *
     * TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     *
     * textAnalyticsAsyncClient.analyzeSentimentBatchWithResponse&#40;textDocumentInputs1, requestOptions&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         &#47;&#47; Response's status code
     *         System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         AnalyzeSentimentResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;,
     *             batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *         resultCollection.forEach&#40;analyzeSentimentResult -&gt; &#123;
     *             System.out.printf&#40;&quot;Document ID: %s%n&quot;, analyzeSentimentResult.getId&#40;&#41;&#41;;
     *             DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment&#40;&#41;;
     *             System.out.printf&#40;&quot;Recognized document sentiment: %s.%n&quot;, documentSentiment.getSentiment&#40;&#41;&#41;;
     *             documentSentiment.getSentences&#40;&#41;.forEach&#40;sentenceSentiment -&gt;
     *                 System.out.printf&#40;&quot;Recognized sentence sentiment: %s, positive score: %.2f, &quot;
     *                         + &quot;neutral score: %.2f, negative score: %.2f.%n&quot;,
     *                     sentenceSentiment.getSentiment&#40;&#41;,
     *                     sentenceSentiment.getConfidenceScores&#40;&#41;.getPositive&#40;&#41;,
     *                     sentenceSentiment.getConfidenceScores&#40;&#41;.getNeutral&#40;&#41;,
     *                     sentenceSentiment.getConfidenceScores&#40;&#41;.getNegative&#40;&#41;&#41;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-TextAnalyticsRequestOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents}  to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The {@link TextAnalyticsRequestOptions options} to configure the scoring model for documents
     * and show statistics.
     *
     * @return A {@link Mono} contains a {@link Response} that contains a {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     *
     * @deprecated Please use the {@link #analyzeSentimentBatchWithResponse(Iterable, AnalyzeSentimentOptions)}.
     */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnalyzeSentimentResultCollection>> analyzeSentimentBatchWithResponse(
        Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {
        return analyzeSentimentAsyncClient.analyzeSentimentBatch(documents, new AnalyzeSentimentOptions()
            .setIncludeStatistics(options == null ? false : options.isIncludeStatistics())
            .setModelVersion(options == null ? null : options.getModelVersion()));
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
     * {@link TextDocumentInput document} with provided {@link AnalyzeSentimentOptions} options. Subscribes to the call
     * asynchronously and prints out the sentiment and sentence opinions details when a response is received.</p>
     *
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-AnalyzeSentimentOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; textDocumentInputs1 = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;0&quot;, &quot;The hotel was dark and unclean.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;The restaurant had amazing gnocchi.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;&#41;;
     *
     * AnalyzeSentimentOptions options = new AnalyzeSentimentOptions&#40;&#41;
     *     .setIncludeOpinionMining&#40;true&#41;.setIncludeStatistics&#40;true&#41;;
     * textAnalyticsAsyncClient.analyzeSentimentBatchWithResponse&#40;textDocumentInputs1, options&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         &#47;&#47; Response's status code
     *         System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         AnalyzeSentimentResultCollection resultCollection = response.getValue&#40;&#41;;
     *
     *         &#47;&#47; Batch statistics
     *         TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     *         System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *             batchStatistics.getTransactionCount&#40;&#41;,
     *             batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *         resultCollection.forEach&#40;analyzeSentimentResult -&gt; &#123;
     *             System.out.printf&#40;&quot;Document ID: %s%n&quot;, analyzeSentimentResult.getId&#40;&#41;&#41;;
     *             DocumentSentiment documentSentiment = analyzeSentimentResult.getDocumentSentiment&#40;&#41;;
     *             documentSentiment.getSentences&#40;&#41;.forEach&#40;sentenceSentiment -&gt; &#123;
     *                 System.out.printf&#40;&quot;&#92;tSentence sentiment: %s%n&quot;, sentenceSentiment.getSentiment&#40;&#41;&#41;;
     *                 sentenceSentiment.getOpinions&#40;&#41;.forEach&#40;opinion -&gt; &#123;
     *                     TargetSentiment targetSentiment = opinion.getTarget&#40;&#41;;
     *                     System.out.printf&#40;&quot;&#92;t&#92;tTarget sentiment: %s, target text: %s%n&quot;,
     *                         targetSentiment.getSentiment&#40;&#41;, targetSentiment.getText&#40;&#41;&#41;;
     *                     for &#40;AssessmentSentiment assessmentSentiment : opinion.getAssessments&#40;&#41;&#41; &#123;
     *                         System.out.printf&#40;
     *                             &quot;&#92;t&#92;t&#92;t'%s' assessment sentiment because of &#92;&quot;%s&#92;&quot;. Is the assessment negated: %s.%n&quot;,
     *                             assessmentSentiment.getSentiment&#40;&#41;, assessmentSentiment.getText&#40;&#41;,
     *                             assessmentSentiment.isNegated&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;&#41;;
     *             &#125;&#41;;
     *         &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.analyzeSentimentBatch#Iterable-AnalyzeSentimentOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The additional configurable {@link AnalyzeSentimentOptions options} that may be passed when
     * analyzing sentiments.
     *
     * @return A {@link Mono} contains a {@link Response} that contains a {@link AnalyzeSentimentResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@link AnalyzeSentimentOptions#isServiceLogsDisabled()} or
     *   {@link AnalyzeSentimentOptions#isIncludeOpinionMining()} is true in service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code disableServiceLogs} and {@code includeOpinionMining} are only
     *   available for API version v3.1 and newer.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnalyzeSentimentResultCollection>> analyzeSentimentBatchWithResponse(
        Iterable<TextDocumentInput> documents, AnalyzeSentimentOptions options) {
        return analyzeSentimentAsyncClient.analyzeSentimentBatch(documents, options);
    }

    /**
     * Perform dynamic classification on a batch of documents. On the fly classification of the input documents into
     * one or multiple categories. Assigns either one or multiple categories per document. This type of classification
     * doesn't require model training. See https://aka.ms/azsdk/textanalytics/data-limits for service data limits.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Dynamic classification of each document in a list of {@link String document} with provided
     * {@link DynamicClassificationOptions} options. Subscribes to the call asynchronously and prints out the
     * dynamic classification details when a response is received.</p>
     *
     * <!-- src_embed AsyncClient.dynamicClassificationBatch#Iterable-String-DynamicClassificationOptions -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * documents.add&#40;&quot;The WHO is issuing a warning about Monkey Pox.&quot;&#41;;
     * documents.add&#40;&quot;Mo Salah plays in Liverpool FC in England.&quot;&#41;;
     * DynamicClassificationOptions options = new DynamicClassificationOptions&#40;&#41;
     *     .setCategories&#40;&quot;Health&quot;, &quot;Politics&quot;, &quot;Music&quot;, &quot;Sport&quot;&#41;;
     * textAnalyticsAsyncClient.dynamicClassificationBatch&#40;documents,  &quot;en&quot;, options&#41;
     *     .subscribe&#40;
     *         resultCollection -&gt; resultCollection.forEach&#40;documentResult -&gt; &#123;
     *             System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *             for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
     *                     classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
     *             &#125;
     *         &#125;&#41;,
     *         error -&gt; System.err.println&#40;&quot;There was an error analyzing dynamic classification of the documents. &quot; + error&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;End of analyzing dynamic classification.&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.dynamicClassificationBatch#Iterable-String-DynamicClassificationOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2 letter ISO 639-1 representation of language for the document. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link DynamicClassificationOptions options} that may be passed when
     * analyzing dynamic classification.
     *
     * @return A {@link Mono} that contains a {@link DynamicClassifyDocumentResultCollection}.
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
    public Mono<DynamicClassifyDocumentResultCollection> dynamicClassificationBatch(
        Iterable<String> documents, String language, DynamicClassificationOptions options) {
        try {
            return dynamicClassificationBatchWithResponse(
                mapByIndex(documents, (index, value) -> {
                    final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                    textDocumentInput.setLanguage(language);
                    return textDocumentInput;
                }), options).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Perform dynamic classification on a batch of documents. On the fly classification of the input documents into
     * one or multiple categories. Assigns either one or multiple categories per document. This type of classification
     * doesn't require model training. See https://aka.ms/azsdk/textanalytics/data-limits for service data limits.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Dynamic classification of each document in a list of {@link TextDocumentInput document} with provided
     * {@link DynamicClassificationOptions} options. Subscribes to the call asynchronously and prints out the
     * dynamic classification details when a response is received.</p>
     *
     * <!-- src_embed AsyncClient.dynamicClassificationBatchWithResponse#Iterable-DynamicClassificationOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * documents.add&#40;new TextDocumentInput&#40;&quot;1&quot;, &quot;The WHO is issuing a warning about Monkey Pox.&quot;&#41;&#41;;
     * documents.add&#40;new TextDocumentInput&#40;&quot;2&quot;, &quot;Mo Salah plays in Liverpool FC in England.&quot;&#41;&#41;;
     * DynamicClassificationOptions options = new DynamicClassificationOptions&#40;&#41;
     *     .setCategories&#40;&quot;Health&quot;, &quot;Politics&quot;, &quot;Music&quot;, &quot;Sport&quot;&#41;;
     * textAnalyticsAsyncClient.dynamicClassificationBatchWithResponse&#40;documents, options&#41;
     *     .subscribe&#40;
     *         response -&gt; &#123;
     *             &#47;&#47; Response's status code
     *             System.out.printf&#40;&quot;Status code of request response: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *             DynamicClassifyDocumentResultCollection resultCollection = response.getValue&#40;&#41;;
     *             &#47;&#47; Batch statistics
     *             TextDocumentBatchStatistics batchStatistics = resultCollection.getStatistics&#40;&#41;;
     *             System.out.printf&#40;&quot;Batch statistics, transaction count: %s, valid document count: %s.%n&quot;,
     *                 batchStatistics.getTransactionCount&#40;&#41;, batchStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *             resultCollection.forEach&#40;documentResult -&gt; &#123;
     *                 System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *                 for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
     *                     System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
     *                         classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
     *                 &#125;
     *             &#125;&#41;;
     *         &#125;,
     *         error -&gt; System.err.println&#40;
     *             &quot;There was an error analyzing dynamic classification of the documents. &quot; + error&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;End of analyzing dynamic classification.&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.dynamicClassificationBatchWithResponse#Iterable-DynamicClassificationOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param options The additional configurable {@link DynamicClassificationOptions options} that may be passed when
     * analyzing dynamic classification.
     *
     * @return A {@link Mono} contains a {@link Response} that contains a
     * {@link DynamicClassifyDocumentResultCollection}.
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
    public Mono<Response<DynamicClassifyDocumentResultCollection>> dynamicClassificationBatchWithResponse(
        Iterable<TextDocumentInput> documents, DynamicClassificationOptions options) {
        return dynamicClassificationAsyncClient.dynamicClassifyBatch(documents, options);
    }

    /**
     * Analyze healthcare entities, entity data sources, and entity relations in a list of {@link String documents}.
     *
     * This method will use the default language that can be set by using method
     * {@link TextAnalyticsClientBuilder#defaultLanguage(String)}. If none is specified, service will use 'en' as
     * the language.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeHealthcareEntities#Iterable -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;&quot;The patient is a 54-year-old gentleman with a history of progressive angina &quot;
     *         + &quot;over the past several months.&quot;&#41;;
     * &#125;
     * textAnalyticsAsyncClient.beginAnalyzeHealthcareEntities&#40;documents&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;pagedFlux -&gt; pagedFlux.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         pagedResponse -&gt; pagedResponse.getElements&#40;&#41;.forEach&#40;
     *             analyzeHealthcareEntitiesResultCollection -&gt; &#123;
     *                 analyzeHealthcareEntitiesResultCollection.forEach&#40;healthcareEntitiesResult -&gt; &#123;
     *                     System.out.println&#40;&quot;document id = &quot; + healthcareEntitiesResult.getId&#40;&#41;&#41;;
     *                     System.out.println&#40;&quot;Document entities: &quot;&#41;;
     *                     AtomicInteger ct = new AtomicInteger&#40;&#41;;
     *                     healthcareEntitiesResult.getEntities&#40;&#41;.forEach&#40;healthcareEntity -&gt; &#123;
     *                         System.out.printf&#40;
     *                             &quot;&#92;ti = %d, Text: %s, category: %s, confidence score: %f.%n&quot;,
     *                             ct.getAndIncrement&#40;&#41;, healthcareEntity.getText&#40;&#41;, healthcareEntity.getCategory&#40;&#41;,
     *                             healthcareEntity.getConfidenceScore&#40;&#41;&#41;;
     *
     *                         IterableStream&lt;EntityDataSource&gt; healthcareEntityDataSources =
     *                             healthcareEntity.getDataSources&#40;&#41;;
     *                         if &#40;healthcareEntityDataSources != null&#41; &#123;
     *                             healthcareEntityDataSources.forEach&#40;healthcareEntityLink -&gt; System.out.printf&#40;
     *                                 &quot;&#92;t&#92;tEntity ID in data source: %s, data source: %s.%n&quot;,
     *                                 healthcareEntityLink.getEntityId&#40;&#41;, healthcareEntityLink.getName&#40;&#41;&#41;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *                     &#47;&#47; Healthcare entity relation groups
     *                     healthcareEntitiesResult.getEntityRelations&#40;&#41;.forEach&#40;entityRelation -&gt; &#123;
     *                         System.out.printf&#40;&quot;&#92;tRelation type: %s.%n&quot;, entityRelation.getRelationType&#40;&#41;&#41;;
     *                         entityRelation.getRoles&#40;&#41;.forEach&#40;role -&gt; &#123;
     *                             final HealthcareEntity entity = role.getEntity&#40;&#41;;
     *                             System.out.printf&#40;&quot;&#92;t&#92;tEntity text: %s, category: %s, role: %s.%n&quot;,
     *                                 entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, role.getName&#40;&#41;&#41;;
     *                         &#125;&#41;;
     *                         System.out.printf&#40;&quot;&#92;tRelation confidence score: %f.%n&quot;,
     *                             entityRelation.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;&#41;;
     *                 &#125;&#41;;
     *             &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeHealthcareEntities#Iterable -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>..
     *
     * @return A {@link PollerFlux} that polls the analyze healthcare operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a {@link PagedFlux} of
     * {@link AnalyzeHealthcareEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAnalyzeHealthcareEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code beginAnalyzeHealthcareEntities} is only available for API
     *   version v3.1 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
        beginAnalyzeHealthcareEntities(Iterable<String> documents) {
        return beginAnalyzeHealthcareEntities(documents, defaultLanguage, null);
    }

    /**
     * Analyze healthcare entities, entity data sources, and entity relations in a list of
     * {@link String documents} with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeHealthcareEntities#Iterable-String-AnalyzeHealthcareEntitiesOptions -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;&quot;The patient is a 54-year-old gentleman with a history of progressive angina &quot;
     *         + &quot;over the past several months.&quot;&#41;;
     * &#125;
     *
     * AnalyzeHealthcareEntitiesOptions options = new AnalyzeHealthcareEntitiesOptions&#40;&#41;
     *     .setIncludeStatistics&#40;true&#41;;
     *
     * textAnalyticsAsyncClient.beginAnalyzeHealthcareEntities&#40;documents, &quot;en&quot;, options&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;pagedFlux -&gt; pagedFlux.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         pagedResponse -&gt; pagedResponse.getElements&#40;&#41;.forEach&#40;
     *             analyzeHealthcareEntitiesResultCollection -&gt; &#123;
     *                 &#47;&#47; Model version
     *                 System.out.printf&#40;&quot;Results of Azure Text Analytics &#92;&quot;Analyze Healthcare&#92;&quot; Model, version: %s%n&quot;,
     *                     analyzeHealthcareEntitiesResultCollection.getModelVersion&#40;&#41;&#41;;
     *
     *                 TextDocumentBatchStatistics healthcareTaskStatistics =
     *                     analyzeHealthcareEntitiesResultCollection.getStatistics&#40;&#41;;
     *                 &#47;&#47; Batch statistics
     *                 System.out.printf&#40;&quot;Documents statistics: document count = %d, erroneous document count = %d,&quot;
     *                         + &quot; transaction count = %d, valid document count = %d.%n&quot;,
     *                     healthcareTaskStatistics.getDocumentCount&#40;&#41;,
     *                     healthcareTaskStatistics.getInvalidDocumentCount&#40;&#41;,
     *                     healthcareTaskStatistics.getTransactionCount&#40;&#41;,
     *                     healthcareTaskStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *                 analyzeHealthcareEntitiesResultCollection.forEach&#40;healthcareEntitiesResult -&gt; &#123;
     *                     System.out.println&#40;&quot;document id = &quot; + healthcareEntitiesResult.getId&#40;&#41;&#41;;
     *                     System.out.println&#40;&quot;Document entities: &quot;&#41;;
     *                     AtomicInteger ct = new AtomicInteger&#40;&#41;;
     *                     healthcareEntitiesResult.getEntities&#40;&#41;.forEach&#40;healthcareEntity -&gt; &#123;
     *                         System.out.printf&#40;
     *                             &quot;&#92;ti = %d, Text: %s, category: %s, confidence score: %f.%n&quot;,
     *                             ct.getAndIncrement&#40;&#41;, healthcareEntity.getText&#40;&#41;, healthcareEntity.getCategory&#40;&#41;,
     *                             healthcareEntity.getConfidenceScore&#40;&#41;&#41;;
     *
     *                         IterableStream&lt;EntityDataSource&gt; healthcareEntityDataSources =
     *                             healthcareEntity.getDataSources&#40;&#41;;
     *                         if &#40;healthcareEntityDataSources != null&#41; &#123;
     *                             healthcareEntityDataSources.forEach&#40;healthcareEntityLink -&gt; System.out.printf&#40;
     *                                 &quot;&#92;t&#92;tEntity ID in data source: %s, data source: %s.%n&quot;,
     *                                 healthcareEntityLink.getEntityId&#40;&#41;, healthcareEntityLink.getName&#40;&#41;&#41;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *                     &#47;&#47; Healthcare entity relation groups
     *                     healthcareEntitiesResult.getEntityRelations&#40;&#41;.forEach&#40;entityRelation -&gt; &#123;
     *                         System.out.printf&#40;&quot;&#92;tRelation type: %s.%n&quot;, entityRelation.getRelationType&#40;&#41;&#41;;
     *                         entityRelation.getRoles&#40;&#41;.forEach&#40;role -&gt; &#123;
     *                             final HealthcareEntity entity = role.getEntity&#40;&#41;;
     *                             System.out.printf&#40;&quot;&#92;t&#92;tEntity text: %s, category: %s, role: %s.%n&quot;,
     *                                 entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, role.getName&#40;&#41;&#41;;
     *                         &#125;&#41;;
     *                         System.out.printf&#40;&quot;&#92;tRelation confidence score: %f.%n&quot;,
     *                             entityRelation.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;&#41;;
     *                 &#125;&#41;;
     *             &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeHealthcareEntities#Iterable-String-AnalyzeHealthcareEntitiesOptions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param language The 2-letter ISO 639-1 representation of language for the documents. If not set, uses "en" for
     * English as default.
     * @param options The additional configurable {@link AnalyzeHealthcareEntitiesOptions options} that may be passed
     * when analyzing healthcare entities.
     *
     * @return A {@link PollerFlux} that polls the analyze healthcare operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a {@link PagedFlux} of
     * {@link AnalyzeHealthcareEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAnalyzeHealthcareEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code beginAnalyzeHealthcareEntities} is only available for API
     *   version v3.1 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
        beginAnalyzeHealthcareEntities(Iterable<String> documents, String language,
            AnalyzeHealthcareEntitiesOptions options) {
        return beginAnalyzeHealthcareEntities(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), options);
    }

    /**
     * Analyze healthcare entities, entity data sources, and entity relations in a list of
     * {@link TextDocumentInput document} and provided request options to
     * show statistics. Subscribes to the call asynchronously and prints out the entity details when a response is
     * received.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeHealthcareEntities#Iterable-AnalyzeHealthcareEntitiesOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;new TextDocumentInput&#40;Integer.toString&#40;i&#41;,
     *         &quot;The patient is a 54-year-old gentleman with a history of progressive angina &quot;
     *             + &quot;over the past several months.&quot;&#41;&#41;;
     * &#125;
     *
     * AnalyzeHealthcareEntitiesOptions options = new AnalyzeHealthcareEntitiesOptions&#40;&#41;
     *     .setIncludeStatistics&#40;true&#41;;
     *
     * textAnalyticsAsyncClient.beginAnalyzeHealthcareEntities&#40;documents, options&#41;
     *     .flatMap&#40;pollResult -&gt; &#123;
     *         AnalyzeHealthcareEntitiesOperationDetail operationResult = pollResult.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *             operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *         return pollResult.getFinalResult&#40;&#41;;
     *     &#125;&#41;
     *     .flatMap&#40;analyzeActionsResultPagedFlux -&gt; analyzeActionsResultPagedFlux.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         pagedResponse -&gt; pagedResponse.getElements&#40;&#41;.forEach&#40;
     *             analyzeHealthcareEntitiesResultCollection -&gt; &#123;
     *                 &#47;&#47; Model version
     *                 System.out.printf&#40;&quot;Results of Azure Text Analytics &#92;&quot;Analyze Healthcare&#92;&quot; Model, version: %s%n&quot;,
     *                     analyzeHealthcareEntitiesResultCollection.getModelVersion&#40;&#41;&#41;;
     *
     *                 TextDocumentBatchStatistics healthcareTaskStatistics =
     *                     analyzeHealthcareEntitiesResultCollection.getStatistics&#40;&#41;;
     *                 &#47;&#47; Batch statistics
     *                 System.out.printf&#40;&quot;Documents statistics: document count = %d, erroneous document count = %d,&quot;
     *                                       + &quot; transaction count = %d, valid document count = %d.%n&quot;,
     *                     healthcareTaskStatistics.getDocumentCount&#40;&#41;,
     *                     healthcareTaskStatistics.getInvalidDocumentCount&#40;&#41;,
     *                     healthcareTaskStatistics.getTransactionCount&#40;&#41;,
     *                     healthcareTaskStatistics.getValidDocumentCount&#40;&#41;&#41;;
     *
     *                 analyzeHealthcareEntitiesResultCollection.forEach&#40;healthcareEntitiesResult -&gt; &#123;
     *                     System.out.println&#40;&quot;document id = &quot; + healthcareEntitiesResult.getId&#40;&#41;&#41;;
     *                     System.out.println&#40;&quot;Document entities: &quot;&#41;;
     *                     AtomicInteger ct = new AtomicInteger&#40;&#41;;
     *                     healthcareEntitiesResult.getEntities&#40;&#41;.forEach&#40;healthcareEntity -&gt; &#123;
     *                         System.out.printf&#40;
     *                             &quot;&#92;ti = %d, Text: %s, category: %s, confidence score: %f.%n&quot;,
     *                             ct.getAndIncrement&#40;&#41;, healthcareEntity.getText&#40;&#41;, healthcareEntity.getCategory&#40;&#41;,
     *                             healthcareEntity.getConfidenceScore&#40;&#41;&#41;;
     *
     *                         IterableStream&lt;EntityDataSource&gt; healthcareEntityDataSources =
     *                             healthcareEntity.getDataSources&#40;&#41;;
     *                         if &#40;healthcareEntityDataSources != null&#41; &#123;
     *                             healthcareEntityDataSources.forEach&#40;healthcareEntityLink -&gt; System.out.printf&#40;
     *                                 &quot;&#92;t&#92;tEntity ID in data source: %s, data source: %s.%n&quot;,
     *                                 healthcareEntityLink.getEntityId&#40;&#41;, healthcareEntityLink.getName&#40;&#41;&#41;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *                     &#47;&#47; Healthcare entity relation groups
     *                     healthcareEntitiesResult.getEntityRelations&#40;&#41;.forEach&#40;entityRelation -&gt; &#123;
     *                         System.out.printf&#40;&quot;&#92;tRelation type: %s.%n&quot;, entityRelation.getRelationType&#40;&#41;&#41;;
     *                         entityRelation.getRoles&#40;&#41;.forEach&#40;role -&gt; &#123;
     *                             final HealthcareEntity entity = role.getEntity&#40;&#41;;
     *                             System.out.printf&#40;&quot;&#92;t&#92;tEntity text: %s, category: %s, role: %s.%n&quot;,
     *                                 entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, role.getName&#40;&#41;&#41;;
     *                         &#125;&#41;;
     *                         System.out.printf&#40;&quot;&#92;tRelation confidence score: %f.%n&quot;,
     *                             entityRelation.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;&#41;;
     *                 &#125;&#41;;
     *             &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeHealthcareEntities#Iterable-AnalyzeHealthcareEntitiesOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * @param options The additional configurable {@link AnalyzeHealthcareEntitiesOptions options} that may be passed
     * when analyzing healthcare entities.
     *
     * @return A {@link PollerFlux} that polls the analyze healthcare operation until it has completed, has failed,
     * or has been cancelled. The completed operation returns a {@link PagedFlux} of
     * {@link AnalyzeHealthcareEntitiesResultCollection}.
     *
     * @throws NullPointerException if {@code documents} is null.
     * @throws IllegalArgumentException if {@code documents} is empty.
     * @throws UnsupportedOperationException if {@code beginAnalyzeHealthcareEntities} is called with service API version
     *   {@link TextAnalyticsServiceVersion#V3_0}. {@code beginAnalyzeHealthcareEntities} is only available for API
     *   version v3.1 and newer.
     * @throws TextAnalyticsException If analyze operation fails.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<AnalyzeHealthcareEntitiesOperationDetail, AnalyzeHealthcareEntitiesPagedFlux>
        beginAnalyzeHealthcareEntities(Iterable<TextDocumentInput> documents,
            AnalyzeHealthcareEntitiesOptions options) {
        return analyzeHealthcareEntityAsyncClient.beginAnalyzeHealthcareEntities(documents, options, Context.NONE);
    }

    // Custom Entities Recognition

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
     * <!-- src_embed AsyncClient.beginRecognizeCustomEntities#Iterable-String-String -->
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
     * textAnalyticsAsyncClient.beginRecognizeCustomEntities&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;&#41;
     *     .flatMap&#40;pollResult -&gt; &#123;
     *         RecognizeCustomEntitiesOperationDetail operationResult = pollResult.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *             operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *         return pollResult.getFinalResult&#40;&#41;;
     *     &#125;&#41;
     *     .flatMap&#40;pagedFlux -&gt; pagedFlux.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         perPage -&gt; &#123;
     *             System.out.printf&#40;&quot;Response code: %d, Continuation Token: %s.%n&quot;,
     *                 perPage.getStatusCode&#40;&#41;, perPage.getContinuationToken&#40;&#41;&#41;;
     *             for &#40;RecognizeCustomEntitiesResultCollection documentsResults : perPage.getElements&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
     *                     documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
     *                 for &#40;RecognizeEntitiesResult documentResult : documentsResults&#41; &#123;
     *                     System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *                     for &#40;CategorizedEntity entity : documentResult.getEntities&#40;&#41;&#41; &#123;
     *                         System.out.printf&#40;
     *                             &quot;&#92;tText: %s, category: %s, confidence score: %f.%n&quot;,
     *                             entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;
     *             &#125;
     *         &#125;,
     *         ex -&gt; System.out.println&#40;&quot;Error listing pages: &quot; + ex.getMessage&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Successfully listed all pages&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.beginRecognizeCustomEntities#Iterable-String-String -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     *
     * @return A {@link PollerFlux} that polls the recognize custom entities operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedFlux} of
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
    public PollerFlux<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedFlux>
        beginRecognizeCustomEntities(Iterable<String> documents, String projectName, String deploymentName) {
        return beginRecognizeCustomEntities(documents, projectName, deploymentName, defaultLanguage, null);
    }

    /**
     * Returns a list of custom entities for the provided list of {@link String document} with provided request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed AsyncClient.beginRecognizeCustomEntities#Iterable-String-String-String-RecognizeCustomEntitiesOptions -->
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
     * RecognizeCustomEntitiesOptions options = new RecognizeCustomEntitiesOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * textAnalyticsAsyncClient.beginRecognizeCustomEntities&#40;documents, &quot;&#123;project_name&#125;&quot;,
     *     &quot;&#123;deployment_name&#125;&quot;, &quot;en&quot;, options&#41;
     *     .flatMap&#40;pollResult -&gt; &#123;
     *         RecognizeCustomEntitiesOperationDetail operationResult = pollResult.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *             operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *         return pollResult.getFinalResult&#40;&#41;;
     *     &#125;&#41;
     *     .flatMap&#40;pagedFlux -&gt; pagedFlux.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         perPage -&gt; &#123;
     *             System.out.printf&#40;&quot;Response code: %d, Continuation Token: %s.%n&quot;,
     *                 perPage.getStatusCode&#40;&#41;, perPage.getContinuationToken&#40;&#41;&#41;;
     *             for &#40;RecognizeCustomEntitiesResultCollection documentsResults : perPage.getElements&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
     *                     documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
     *                 for &#40;RecognizeEntitiesResult documentResult : documentsResults&#41; &#123;
     *                     System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *                     for &#40;CategorizedEntity entity : documentResult.getEntities&#40;&#41;&#41; &#123;
     *                         System.out.printf&#40;
     *                             &quot;&#92;tText: %s, category: %s, confidence score: %f.%n&quot;,
     *                             entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;
     *             &#125;
     *         &#125;,
     *         ex -&gt; System.out.println&#40;&quot;Error listing pages: &quot; + ex.getMessage&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Successfully listed all pages&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.beginRecognizeCustomEntities#Iterable-String-String-String-RecognizeCustomEntitiesOptions -->
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
     * @return A {@link PollerFlux} that polls the recognize custom entities operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedFlux} of
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
    public PollerFlux<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedFlux>
        beginRecognizeCustomEntities(Iterable<String> documents, String projectName, String deploymentName,
            String language, RecognizeCustomEntitiesOptions options) {
        return beginRecognizeCustomEntities(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), projectName, deploymentName, options);
    }

    /**
     * Returns a list of custom entities for the provided list of {@link TextDocumentInput document} with provided
     * request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed AsyncClient.beginRecognizeCustomEntities#Iterable-String-String-RecognizeCustomEntitiesOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;new TextDocumentInput&#40;Integer.toString&#40;i&#41;,
     *         &quot;A recent report by the Government Accountability Office &#40;GAO&#41; found that the dramatic increase &quot;
     *             + &quot;in oil and natural gas development on federal lands over the past six years has stretched the&quot;
     *             + &quot; staff of the BLM to a point that it has been unable to meet its environmental protection &quot;
     *             + &quot;responsibilities.&quot;&#41;&#41;;
     * &#125;
     * RecognizeCustomEntitiesOptions options = new RecognizeCustomEntitiesOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * textAnalyticsAsyncClient.beginRecognizeCustomEntities&#40;documents, &quot;&#123;project_name&#125;&quot;,
     *     &quot;&#123;deployment_name&#125;&quot;, options&#41;
     *     .flatMap&#40;pollResult -&gt; &#123;
     *         RecognizeCustomEntitiesOperationDetail operationResult = pollResult.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *             operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *         return pollResult.getFinalResult&#40;&#41;;
     *     &#125;&#41;
     *     .flatMap&#40;pagedFlux -&gt; pagedFlux.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         perPage -&gt; &#123;
     *             System.out.printf&#40;&quot;Response code: %d, Continuation Token: %s.%n&quot;,
     *                 perPage.getStatusCode&#40;&#41;, perPage.getContinuationToken&#40;&#41;&#41;;
     *             for &#40;RecognizeCustomEntitiesResultCollection documentsResults : perPage.getElements&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
     *                     documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
     *                 for &#40;RecognizeEntitiesResult documentResult : documentsResults&#41; &#123;
     *                     System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *                     for &#40;CategorizedEntity entity : documentResult.getEntities&#40;&#41;&#41; &#123;
     *                         System.out.printf&#40;
     *                             &quot;&#92;tText: %s, category: %s, confidence score: %f.%n&quot;,
     *                             entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;
     *             &#125;
     *         &#125;,
     *         ex -&gt; System.out.println&#40;&quot;Error listing pages: &quot; + ex.getMessage&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Successfully listed all pages&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.beginRecognizeCustomEntities#Iterable-String-String-RecognizeCustomEntitiesOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     * @param options The additional configurable {@link RecognizeCustomEntitiesOptions options} that may be passed
     * when recognizing custom entities.
     *
     * @return A {@link PollerFlux} that polls the recognize custom entities until it has completed, has failed,
     * or has been cancelled. The completed operation returns a {@link PagedFlux} of
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
    public PollerFlux<RecognizeCustomEntitiesOperationDetail, RecognizeCustomEntitiesPagedFlux>
        beginRecognizeCustomEntities(Iterable<TextDocumentInput> documents, String projectName, String deploymentName,
            RecognizeCustomEntitiesOptions options) {
        return recognizeCustomEntitiesAsyncClient.recognizeCustomEntities(documents, projectName, deploymentName,
            options, Context.NONE);
    }

    // Single Label Classification
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
     * <!-- src_embed AsyncClient.beginSingleLabelClassify#Iterable-String-String -->
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
     * textAnalyticsAsyncClient.beginSingleLabelClassify&#40;documents,
     *         &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;&#41;
     *     .flatMap&#40;pollResult -&gt; &#123;
     *         ClassifyDocumentOperationDetail operationResult = pollResult.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *             operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *         return pollResult.getFinalResult&#40;&#41;;
     *     &#125;&#41;
     *     .flatMap&#40;pagedFluxAsyncPollResponse -&gt; pagedFluxAsyncPollResponse.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         perPage -&gt; &#123;
     *             System.out.printf&#40;&quot;Response code: %d, Continuation Token: %s.%n&quot;,
     *                 perPage.getStatusCode&#40;&#41;, perPage.getContinuationToken&#40;&#41;&#41;;
     *             for &#40;ClassifyDocumentResultCollection documentsResults : perPage.getElements&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
     *                     documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
     *                 for &#40;ClassifyDocumentResult documentResult : documentsResults&#41; &#123;
     *                     System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *                     for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
     *                         System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
     *                             classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;
     *             &#125;
     *         &#125;,
     *         ex -&gt; System.out.println&#40;&quot;Error listing pages: &quot; + ex.getMessage&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Successfully listed all pages&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.beginSingleLabelClassify#Iterable-String-String -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     *
     * @return A {@link PollerFlux} that polls the single-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedFlux} of
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
    public PollerFlux<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> beginSingleLabelClassify(
        Iterable<String> documents, String projectName, String deploymentName) {
        return beginSingleLabelClassify(documents, projectName, deploymentName, defaultLanguage, null);
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
     * <!-- src_embed AsyncClient.beginSingleLabelClassify#Iterable-String-String-String-SingleLabelClassifyOptions -->
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
     * textAnalyticsAsyncClient.beginSingleLabelClassify&#40;documents,
     *     &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;, &quot;en&quot;, options&#41;
     *     .flatMap&#40;pollResult -&gt; &#123;
     *         ClassifyDocumentOperationDetail operationResult = pollResult.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *             operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *         return pollResult.getFinalResult&#40;&#41;;
     *     &#125;&#41;
     *     .flatMap&#40;pagedFluxAsyncPollResponse -&gt; pagedFluxAsyncPollResponse.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         perPage -&gt; &#123;
     *             System.out.printf&#40;&quot;Response code: %d, Continuation Token: %s.%n&quot;,
     *                 perPage.getStatusCode&#40;&#41;, perPage.getContinuationToken&#40;&#41;&#41;;
     *             for &#40;ClassifyDocumentResultCollection documentsResults : perPage.getElements&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
     *                     documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
     *                 for &#40;ClassifyDocumentResult documentResult : documentsResults&#41; &#123;
     *                     System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *                     for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
     *                         System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
     *                             classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;
     *             &#125;
     *         &#125;,
     *         ex -&gt; System.out.println&#40;&quot;Error listing pages: &quot; + ex.getMessage&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Successfully listed all pages&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.beginSingleLabelClassify#Iterable-String-String-String-SingleLabelClassifyOptions -->
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
     * @return A {@link PollerFlux} that polls the single-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedFlux} of
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
    public PollerFlux<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> beginSingleLabelClassify(
        Iterable<String> documents, String projectName, String deploymentName, String language,
        SingleLabelClassifyOptions options) {
        return beginSingleLabelClassify(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), projectName, deploymentName, options);
    }

    /**
     * Returns a list of single-label classification for the provided list of {@link TextDocumentInput document} with
     * provided request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed AsyncClient.beginSingleLabelClassify#Iterable-String-String-SingleLabelClassifyOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;new TextDocumentInput&#40;Integer.toString&#40;i&#41;,
     *         &quot;A recent report by the Government Accountability Office &#40;GAO&#41; found that the dramatic increase &quot;
     *         + &quot;in oil and natural gas development on federal lands over the past six years has stretched the&quot;
     *         + &quot; staff of the BLM to a point that it has been unable to meet its environmental protection &quot;
     *         + &quot;responsibilities.&quot;&#41;&#41;;
     * &#125;
     * SingleLabelClassifyOptions options = new SingleLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * &#47;&#47; See the service documentation for regional support and how to train a model to classify your documents,
     * &#47;&#47; see https:&#47;&#47;aka.ms&#47;azsdk&#47;textanalytics&#47;customfunctionalities
     * textAnalyticsAsyncClient.beginSingleLabelClassify&#40;documents,
     *     &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;, options&#41;
     *     .flatMap&#40;pollResult -&gt; &#123;
     *         ClassifyDocumentOperationDetail operationResult = pollResult.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *             operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *         return pollResult.getFinalResult&#40;&#41;;
     *     &#125;&#41;
     *     .flatMap&#40;pagedFluxAsyncPollResponse -&gt; pagedFluxAsyncPollResponse.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         perPage -&gt; &#123;
     *             System.out.printf&#40;&quot;Response code: %d, Continuation Token: %s.%n&quot;,
     *                 perPage.getStatusCode&#40;&#41;, perPage.getContinuationToken&#40;&#41;&#41;;
     *             for &#40;ClassifyDocumentResultCollection documentsResults : perPage.getElements&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
     *                     documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
     *                 for &#40;ClassifyDocumentResult documentResult : documentsResults&#41; &#123;
     *                     System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *                     for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
     *                         System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
     *                             classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;
     *             &#125;
     *         &#125;,
     *         ex -&gt; System.out.println&#40;&quot;Error listing pages: &quot; + ex.getMessage&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Successfully listed all pages&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.beginSingleLabelClassify#Iterable-String-String-SingleLabelClassifyOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     * @param options The additional configurable {@link SingleLabelClassifyOptions options} that may be passed
     * when analyzing single-label classification.
     *
     * @return A {@link PollerFlux} that polls the single-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedFlux} of
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
    public PollerFlux<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> beginSingleLabelClassify(
        Iterable<TextDocumentInput> documents, String projectName, String deploymentName,
        SingleLabelClassifyOptions options) {
        return labelClassifyAsyncClient.singleLabelClassify(documents, projectName, deploymentName,
            options, Context.NONE);
    }

    // Multi-label Classification
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
     * <!-- src_embed AsyncClient.beginMultiLabelClassify#Iterable-String-String -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;
     *         &quot;I need a reservation for an indoor restaurant in China. Please don't stop the music.&quot;
     *             + &quot; Play music and add it to my playlist&quot;&#41;;
     * &#125;
     * textAnalyticsAsyncClient.beginMultiLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;, &quot;&#123;deployment_name&#125;&quot;&#41;
     *     .flatMap&#40;pollResult -&gt; &#123;
     *         ClassifyDocumentOperationDetail operationResult = pollResult.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *             operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *         return pollResult.getFinalResult&#40;&#41;;
     *     &#125;&#41;
     *     .flatMap&#40;pagedFluxAsyncPollResponse -&gt; pagedFluxAsyncPollResponse.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         perPage -&gt; &#123;
     *             System.out.printf&#40;&quot;Response code: %d, Continuation Token: %s.%n&quot;,
     *                 perPage.getStatusCode&#40;&#41;, perPage.getContinuationToken&#40;&#41;&#41;;
     *             for &#40;ClassifyDocumentResultCollection documentsResults : perPage.getElements&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
     *                     documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
     *                 for &#40;ClassifyDocumentResult documentResult : documentsResults&#41; &#123;
     *                     System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *                     for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
     *                         System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
     *                             classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;
     *             &#125;
     *         &#125;,
     *         ex -&gt; System.out.println&#40;&quot;Error listing pages: &quot; + ex.getMessage&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Successfully listed all pages&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.beginMultiLabelClassify#Iterable-String-String -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     *
     * @return A {@link PollerFlux} that polls the multi-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedFlux} of
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
    public PollerFlux<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> beginMultiLabelClassify(
        Iterable<String> documents, String projectName, String deploymentName) {
        return beginMultiLabelClassify(documents, projectName, deploymentName, defaultLanguage, null);
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
     * <!-- src_embed AsyncClient.beginMultiLabelClassify#Iterable-String-String-String-MultiLabelClassifyOptions -->
     * <pre>
     * List&lt;String&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;
     *         &quot;I need a reservation for an indoor restaurant in China. Please don't stop the music.&quot;
     *             + &quot; Play music and add it to my playlist&quot;&#41;;
     * &#125;
     * MultiLabelClassifyOptions options = new MultiLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * textAnalyticsAsyncClient.beginMultiLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;,
     *     &quot;&#123;deployment_name&#125;&quot;, &quot;en&quot;, options&#41;
     *     .flatMap&#40;pollResult -&gt; &#123;
     *         ClassifyDocumentOperationDetail operationResult = pollResult.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *             operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *         return pollResult.getFinalResult&#40;&#41;;
     *     &#125;&#41;
     *     .flatMap&#40;pagedFluxAsyncPollResponse -&gt; pagedFluxAsyncPollResponse.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         perPage -&gt; &#123;
     *             System.out.printf&#40;&quot;Response code: %d, Continuation Token: %s.%n&quot;,
     *                 perPage.getStatusCode&#40;&#41;, perPage.getContinuationToken&#40;&#41;&#41;;
     *             for &#40;ClassifyDocumentResultCollection documentsResults : perPage.getElements&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
     *                     documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
     *                 for &#40;ClassifyDocumentResult documentResult : documentsResults&#41; &#123;
     *                     System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *                     for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
     *                         System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
     *                             classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;
     *             &#125;
     *         &#125;,
     *         ex -&gt; System.out.println&#40;&quot;Error listing pages: &quot; + ex.getMessage&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Successfully listed all pages&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.beginMultiLabelClassify#Iterable-String-String-String-MultiLabelClassifyOptions -->
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
     * @return A {@link PollerFlux} that polls the multi-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedFlux} of
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
    public PollerFlux<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> beginMultiLabelClassify(
        Iterable<String> documents, String projectName, String deploymentName, String language,
        MultiLabelClassifyOptions options) {
        return beginMultiLabelClassify(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), projectName, deploymentName, options);
    }

    /**
     * Returns a list of multi-label classification for the provided list of {@link TextDocumentInput document} with
     * provided request options.
     *
     * <p>This method is supported since service API version {@code V2022_05_01}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed AsyncClient.beginMultiLabelClassify#Iterable-String-String-MultiLabelClassifyOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = new ArrayList&lt;&gt;&#40;&#41;;
     * for &#40;int i = 0; i &lt; 3; i++&#41; &#123;
     *     documents.add&#40;new TextDocumentInput&#40;Integer.toString&#40;i&#41;,
     *         &quot;I need a reservation for an indoor restaurant in China. Please don't stop the music.&quot;
     *             + &quot; Play music and add it to my playlist&quot;&#41;&#41;;
     * &#125;
     * MultiLabelClassifyOptions options = new MultiLabelClassifyOptions&#40;&#41;.setIncludeStatistics&#40;true&#41;;
     * textAnalyticsAsyncClient.beginMultiLabelClassify&#40;documents, &quot;&#123;project_name&#125;&quot;,
     *     &quot;&#123;deployment_name&#125;&quot;, options&#41;
     *     .flatMap&#40;pollResult -&gt; &#123;
     *         ClassifyDocumentOperationDetail operationResult = pollResult.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Operation created time: %s, expiration time: %s.%n&quot;,
     *             operationResult.getCreatedAt&#40;&#41;, operationResult.getExpiresAt&#40;&#41;&#41;;
     *         return pollResult.getFinalResult&#40;&#41;;
     *     &#125;&#41;
     *     .flatMap&#40;pagedFluxAsyncPollResponse -&gt; pagedFluxAsyncPollResponse.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         perPage -&gt; &#123;
     *             System.out.printf&#40;&quot;Response code: %d, Continuation Token: %s.%n&quot;,
     *                 perPage.getStatusCode&#40;&#41;, perPage.getContinuationToken&#40;&#41;&#41;;
     *             for &#40;ClassifyDocumentResultCollection documentsResults : perPage.getElements&#40;&#41;&#41; &#123;
     *                 System.out.printf&#40;&quot;Project name: %s, deployment name: %s.%n&quot;,
     *                     documentsResults.getProjectName&#40;&#41;, documentsResults.getDeploymentName&#40;&#41;&#41;;
     *                 for &#40;ClassifyDocumentResult documentResult : documentsResults&#41; &#123;
     *                     System.out.println&#40;&quot;Document ID: &quot; + documentResult.getId&#40;&#41;&#41;;
     *                     for &#40;ClassificationCategory classification : documentResult.getClassifications&#40;&#41;&#41; &#123;
     *                         System.out.printf&#40;&quot;&#92;tCategory: %s, confidence score: %f.%n&quot;,
     *                             classification.getCategory&#40;&#41;, classification.getConfidenceScore&#40;&#41;&#41;;
     *                     &#125;
     *                 &#125;
     *             &#125;
     *         &#125;,
     *         ex -&gt; System.out.println&#40;&quot;Error listing pages: &quot; + ex.getMessage&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Successfully listed all pages&quot;&#41;&#41;;
     * </pre>
     * <!-- end AsyncClient.beginMultiLabelClassify#Iterable-String-String-MultiLabelClassifyOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param projectName The name of the project which owns the model being consumed.
     * @param deploymentName The name of the deployment being consumed.
     * @param options The additional configurable {@link SingleLabelClassifyOptions options} that may be passed
     * when analyzing multi-label classification.
     *
     * @return A {@link PollerFlux} that polls the multi-label classification operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link PagedFlux} of
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
    public PollerFlux<ClassifyDocumentOperationDetail, ClassifyDocumentPagedFlux> beginMultiLabelClassify(
        Iterable<TextDocumentInput> documents, String projectName, String deploymentName,
        MultiLabelClassifyOptions options) {
        return labelClassifyAsyncClient.multiLabelClassify(documents, projectName, deploymentName,
            options, Context.NONE);
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
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;Elon Musk is the CEO of SpaceX and Tesla.&quot;,
     *     &quot;1&quot;, &quot;My SSN is 859-98-0987&quot;
     * &#41;;
     * textAnalyticsAsyncClient.beginAnalyzeActions&#40;documents,
     *         new TextAnalyticsActions&#40;&#41;.setDisplayName&#40;&quot;&#123;tasks_display_name&#125;&quot;&#41;
     *             .setRecognizeEntitiesActions&#40;new RecognizeEntitiesAction&#40;&#41;&#41;
     *             .setExtractKeyPhrasesActions&#40;new ExtractKeyPhrasesAction&#40;&#41;&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;analyzeActionsResultPagedFlux -&gt; analyzeActionsResultPagedFlux.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         pagedResponse -&gt; pagedResponse.getElements&#40;&#41;.forEach&#40;
     *             analyzeActionsResult -&gt; &#123;
     *                 analyzeActionsResult.getRecognizeEntitiesResults&#40;&#41;.forEach&#40;
     *                     actionResult -&gt; &#123;
     *                         if &#40;!actionResult.isError&#40;&#41;&#41; &#123;
     *                             actionResult.getDocumentsResults&#40;&#41;.forEach&#40;
     *                                 entitiesResult -&gt; entitiesResult.getEntities&#40;&#41;.forEach&#40;
     *                                     entity -&gt; System.out.printf&#40;
     *                                         &quot;Recognized entity: %s, entity category: %s, entity subcategory: %s,&quot;
     *                                             + &quot; confidence score: %f.%n&quot;,
     *                                         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;,
     *                                         entity.getConfidenceScore&#40;&#41;&#41;&#41;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *                 analyzeActionsResult.getExtractKeyPhrasesResults&#40;&#41;.forEach&#40;
     *                     actionResult -&gt; &#123;
     *                         if &#40;!actionResult.isError&#40;&#41;&#41; &#123;
     *                             actionResult.getDocumentsResults&#40;&#41;.forEach&#40;extractKeyPhraseResult -&gt; &#123;
     *                                 System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     *                                 extractKeyPhraseResult.getKeyPhrases&#40;&#41;
     *                                     .forEach&#40;keyPhrases -&gt; System.out.printf&#40;&quot;&#92;t%s.%n&quot;, keyPhrases&#41;&#41;;
     *                             &#125;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *             &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions -->
     *
     * @param documents A list of documents to be analyzed.
     * For text length limits, maximum batch size, and supported text encoding, see
     * <a href="https://aka.ms/azsdk/textanalytics/data-limits">data limits</a>.
     * @param actions The {@link TextAnalyticsActions actions} that contains all actions to be executed.
     * An action is one task of execution, such as a single task of 'Key Phrases Extraction' on the given document
     * inputs.
     *
     * @return A {@link PollerFlux} that polls the analyze a collection of actions operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link AnalyzeActionsResultPagedFlux}.
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
    public PollerFlux<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> beginAnalyzeActions(
        Iterable<String> documents, TextAnalyticsActions actions) {
        return beginAnalyzeActions(documents, actions, defaultLanguage, null);
    }

    /**
     * Execute actions, such as, entities recognition, PII entities recognition and key phrases extraction for a list of
     * {@link String documents} with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions -->
     * <pre>
     * List&lt;String&gt; documents = Arrays.asList&#40;
     *     &quot;Elon Musk is the CEO of SpaceX and Tesla.&quot;,
     *     &quot;1&quot;, &quot;My SSN is 859-98-0987&quot;
     * &#41;;
     * textAnalyticsAsyncClient.beginAnalyzeActions&#40;documents,
     *     new TextAnalyticsActions&#40;&#41;.setDisplayName&#40;&quot;&#123;tasks_display_name&#125;&quot;&#41;
     *         .setRecognizeEntitiesActions&#40;new RecognizeEntitiesAction&#40;&#41;&#41;
     *         .setExtractKeyPhrasesActions&#40;new ExtractKeyPhrasesAction&#40;&#41;&#41;,
     *     &quot;en&quot;,
     *     new AnalyzeActionsOptions&#40;&#41;.setIncludeStatistics&#40;false&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;analyzeActionsResultPagedFlux -&gt; analyzeActionsResultPagedFlux.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         pagedResponse -&gt; pagedResponse.getElements&#40;&#41;.forEach&#40;
     *             analyzeActionsResult -&gt; &#123;
     *                 analyzeActionsResult.getRecognizeEntitiesResults&#40;&#41;.forEach&#40;
     *                     actionResult -&gt; &#123;
     *                         if &#40;!actionResult.isError&#40;&#41;&#41; &#123;
     *                             actionResult.getDocumentsResults&#40;&#41;.forEach&#40;
     *                                 entitiesResult -&gt; entitiesResult.getEntities&#40;&#41;.forEach&#40;
     *                                     entity -&gt; System.out.printf&#40;
     *                                         &quot;Recognized entity: %s, entity category: %s, entity subcategory: %s,&quot;
     *                                             + &quot; confidence score: %f.%n&quot;,
     *                                         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;,
     *                                         entity.getConfidenceScore&#40;&#41;&#41;&#41;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *                 analyzeActionsResult.getExtractKeyPhrasesResults&#40;&#41;.forEach&#40;
     *                     actionResult -&gt; &#123;
     *                         if &#40;!actionResult.isError&#40;&#41;&#41; &#123;
     *                             actionResult.getDocumentsResults&#40;&#41;.forEach&#40;extractKeyPhraseResult -&gt; &#123;
     *                                 System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     *                                 extractKeyPhraseResult.getKeyPhrases&#40;&#41;
     *                                     .forEach&#40;keyPhrases -&gt; System.out.printf&#40;&quot;&#92;t%s.%n&quot;, keyPhrases&#41;&#41;;
     *                             &#125;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *             &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-String-AnalyzeActionsOptions -->
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
     * @return A {@link PollerFlux} that polls the analyze a collection of actions operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link AnalyzeActionsResultPagedFlux}.
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
    public PollerFlux<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> beginAnalyzeActions(
        Iterable<String> documents, TextAnalyticsActions actions, String language, AnalyzeActionsOptions options) {
        return beginAnalyzeActions(
            mapByIndex(documents, (index, value) -> {
                final TextDocumentInput textDocumentInput = new TextDocumentInput(index, value);
                textDocumentInput.setLanguage(language);
                return textDocumentInput;
            }), actions, options);
    }

    /**
     * Execute actions, such as, entities recognition, PII entities recognition and key phrases extraction for a list of
     * {@link TextDocumentInput documents} with provided request options.
     *
     * See <a href="https://aka.ms/talangs">this</a> supported languages in Language service API.
     *
     * <p><strong>Code Sample</strong></p>
     * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-AnalyzeActionsOptions -->
     * <pre>
     * List&lt;TextDocumentInput&gt; documents = Arrays.asList&#40;
     *     new TextDocumentInput&#40;&quot;0&quot;, &quot;Elon Musk is the CEO of SpaceX and Tesla.&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;,
     *     new TextDocumentInput&#40;&quot;1&quot;, &quot;My SSN is 859-98-0987&quot;&#41;.setLanguage&#40;&quot;en&quot;&#41;
     * &#41;;
     * textAnalyticsAsyncClient.beginAnalyzeActions&#40;documents,
     *     new TextAnalyticsActions&#40;&#41;.setDisplayName&#40;&quot;&#123;tasks_display_name&#125;&quot;&#41;
     *         .setRecognizeEntitiesActions&#40;new RecognizeEntitiesAction&#40;&#41;&#41;
     *         .setExtractKeyPhrasesActions&#40;new ExtractKeyPhrasesAction&#40;&#41;&#41;,
     *     new AnalyzeActionsOptions&#40;&#41;.setIncludeStatistics&#40;false&#41;&#41;
     *     .flatMap&#40;AsyncPollResponse::getFinalResult&#41;
     *     .flatMap&#40;analyzeActionsResultPagedFlux -&gt; analyzeActionsResultPagedFlux.byPage&#40;&#41;&#41;
     *     .subscribe&#40;
     *         pagedResponse -&gt; pagedResponse.getElements&#40;&#41;.forEach&#40;
     *             analyzeActionsResult -&gt; &#123;
     *                 System.out.println&#40;&quot;Entities recognition action results:&quot;&#41;;
     *                 analyzeActionsResult.getRecognizeEntitiesResults&#40;&#41;.forEach&#40;
     *                     actionResult -&gt; &#123;
     *                         if &#40;!actionResult.isError&#40;&#41;&#41; &#123;
     *                             actionResult.getDocumentsResults&#40;&#41;.forEach&#40;
     *                                 entitiesResult -&gt; entitiesResult.getEntities&#40;&#41;.forEach&#40;
     *                                     entity -&gt; System.out.printf&#40;
     *                                         &quot;Recognized entity: %s, entity category: %s, entity subcategory: %s,&quot;
     *                                             + &quot; confidence score: %f.%n&quot;,
     *                                         entity.getText&#40;&#41;, entity.getCategory&#40;&#41;, entity.getSubcategory&#40;&#41;,
     *                                         entity.getConfidenceScore&#40;&#41;&#41;&#41;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *                 System.out.println&#40;&quot;Key phrases extraction action results:&quot;&#41;;
     *                 analyzeActionsResult.getExtractKeyPhrasesResults&#40;&#41;.forEach&#40;
     *                     actionResult -&gt; &#123;
     *                         if &#40;!actionResult.isError&#40;&#41;&#41; &#123;
     *                             actionResult.getDocumentsResults&#40;&#41;.forEach&#40;extractKeyPhraseResult -&gt; &#123;
     *                                 System.out.println&#40;&quot;Extracted phrases:&quot;&#41;;
     *                                 extractKeyPhraseResult.getKeyPhrases&#40;&#41;
     *                                     .forEach&#40;keyPhrases -&gt; System.out.printf&#40;&quot;&#92;t%s.%n&quot;, keyPhrases&#41;&#41;;
     *                             &#125;&#41;;
     *                         &#125;
     *                     &#125;&#41;;
     *             &#125;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.beginAnalyzeActions#Iterable-TextAnalyticsActions-AnalyzeActionsOptions -->
     *
     * @param documents A list of {@link TextDocumentInput documents} to be analyzed.
     * @param actions The {@link TextAnalyticsActions actions} that contains all actions to be executed.
     * An action is one task of execution, such as a single task of 'Key Phrases Extraction' on the given document
     * inputs.
     * @param options The additional configurable {@link AnalyzeActionsOptions options} that may be passed when
     * analyzing a collection of tasks.
     *
     * @return A {@link PollerFlux} that polls the analyze a collection of tasks operation until it has completed,
     * has failed, or has been cancelled. The completed operation returns a {@link AnalyzeActionsResultPagedFlux}.
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
    public PollerFlux<AnalyzeActionsOperationDetail, AnalyzeActionsResultPagedFlux> beginAnalyzeActions(
        Iterable<TextDocumentInput> documents, TextAnalyticsActions actions, AnalyzeActionsOptions options) {
        return analyzeActionsAsyncClient.beginAnalyzeActions(documents, actions, options, Context.NONE);
    }
}
