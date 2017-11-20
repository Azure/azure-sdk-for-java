/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.cognitiveservices.textanalytics.models.AzureRegions;
import com.microsoft.azure.cognitiveservices.textanalytics.models.BatchInput;
import com.microsoft.azure.cognitiveservices.textanalytics.models.ErrorResponseException;
import com.microsoft.azure.cognitiveservices.textanalytics.models.KeyPhraseBatchResult;
import com.microsoft.azure.cognitiveservices.textanalytics.models.LanguageBatchResult;
import com.microsoft.azure.cognitiveservices.textanalytics.models.MultiLanguageBatchInput;
import com.microsoft.azure.cognitiveservices.textanalytics.models.SentimentBatchResult;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;
import rx.Observable;

/**
 * The interface for TextAnalyticsAPI class.
 */
public interface TextAnalyticsAPI {
    /**
     * Gets the REST client.
     *
     * @return the {@link RestClient} object.
    */
    RestClient restClient();

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    AzureClient getAzureClient();

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    String userAgent();

    /**
     * Gets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @return the azureRegion value.
     */
    AzureRegions azureRegion();

    /**
     * Sets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @param azureRegion the azureRegion value.
     * @return the service client itself
     */
    TextAnalyticsAPI withAzureRegion(AzureRegions azureRegion);

    /**
     * Gets Gets or sets the preferred language for the response..
     *
     * @return the acceptLanguage value.
     */
    String acceptLanguage();

    /**
     * Sets Gets or sets the preferred language for the response..
     *
     * @param acceptLanguage the acceptLanguage value.
     * @return the service client itself
     */
    TextAnalyticsAPI withAcceptLanguage(String acceptLanguage);

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30..
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    int longRunningOperationRetryTimeout();

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30..
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     * @return the service client itself
     */
    TextAnalyticsAPI withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout);

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true..
     *
     * @return the generateClientRequestId value.
     */
    boolean generateClientRequestId();

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true..
     *
     * @param generateClientRequestId the generateClientRequestId value.
     * @return the service client itself
     */
    TextAnalyticsAPI withGenerateClientRequestId(boolean generateClientRequestId);

    /**
     * The API returns a list of strings denoting the key talking points in the input text.
     * We employ techniques from Microsoft Office's sophisticated Natural Language Processing toolkit. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by key phrase extraction.
     *
     * @param input Collection of documents to analyze. Documents can now contain a language field to indicate the text language
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the KeyPhraseBatchResult object if successful.
     */
    KeyPhraseBatchResult keyPhrases(MultiLanguageBatchInput input);

    /**
     * The API returns a list of strings denoting the key talking points in the input text.
     * We employ techniques from Microsoft Office's sophisticated Natural Language Processing toolkit. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by key phrase extraction.
     *
     * @param input Collection of documents to analyze. Documents can now contain a language field to indicate the text language
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<KeyPhraseBatchResult> keyPhrasesAsync(MultiLanguageBatchInput input, final ServiceCallback<KeyPhraseBatchResult> serviceCallback);

    /**
     * The API returns a list of strings denoting the key talking points in the input text.
     * We employ techniques from Microsoft Office's sophisticated Natural Language Processing toolkit. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by key phrase extraction.
     *
     * @param input Collection of documents to analyze. Documents can now contain a language field to indicate the text language
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the KeyPhraseBatchResult object
     */
    Observable<KeyPhraseBatchResult> keyPhrasesAsync(MultiLanguageBatchInput input);

    /**
     * The API returns a list of strings denoting the key talking points in the input text.
     * We employ techniques from Microsoft Office's sophisticated Natural Language Processing toolkit. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by key phrase extraction.
     *
     * @param input Collection of documents to analyze. Documents can now contain a language field to indicate the text language
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the KeyPhraseBatchResult object
     */
    Observable<ServiceResponse<KeyPhraseBatchResult>> keyPhrasesWithServiceResponseAsync(MultiLanguageBatchInput input);

    /**
     * The API returns the detected language and a numeric score between 0 and 1.
     * Scores close to 1 indicate 100% certainty that the identified language is true. A total of 120 languages are supported.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the LanguageBatchResult object if successful.
     */
    LanguageBatchResult detectLanguage(BatchInput input);

    /**
     * The API returns the detected language and a numeric score between 0 and 1.
     * Scores close to 1 indicate 100% certainty that the identified language is true. A total of 120 languages are supported.
     *
     * @param input Collection of documents to analyze.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<LanguageBatchResult> detectLanguageAsync(BatchInput input, final ServiceCallback<LanguageBatchResult> serviceCallback);

    /**
     * The API returns the detected language and a numeric score between 0 and 1.
     * Scores close to 1 indicate 100% certainty that the identified language is true. A total of 120 languages are supported.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the LanguageBatchResult object
     */
    Observable<LanguageBatchResult> detectLanguageAsync(BatchInput input);

    /**
     * The API returns the detected language and a numeric score between 0 and 1.
     * Scores close to 1 indicate 100% certainty that the identified language is true. A total of 120 languages are supported.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the LanguageBatchResult object
     */
    Observable<ServiceResponse<LanguageBatchResult>> detectLanguageWithServiceResponseAsync(BatchInput input);

    /**
     * The API returns a numeric score between 0 and 1.
     * Scores close to 1 indicate positive sentiment, while scores close to 0 indicate negative sentiment. Sentiment score is generated using classification techniques. The input features to the classifier include n-grams, features generated from part-of-speech tags, and word embeddings. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by sentiment analysis.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the SentimentBatchResult object if successful.
     */
    SentimentBatchResult sentiment(MultiLanguageBatchInput input);

    /**
     * The API returns a numeric score between 0 and 1.
     * Scores close to 1 indicate positive sentiment, while scores close to 0 indicate negative sentiment. Sentiment score is generated using classification techniques. The input features to the classifier include n-grams, features generated from part-of-speech tags, and word embeddings. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by sentiment analysis.
     *
     * @param input Collection of documents to analyze.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    ServiceFuture<SentimentBatchResult> sentimentAsync(MultiLanguageBatchInput input, final ServiceCallback<SentimentBatchResult> serviceCallback);

    /**
     * The API returns a numeric score between 0 and 1.
     * Scores close to 1 indicate positive sentiment, while scores close to 0 indicate negative sentiment. Sentiment score is generated using classification techniques. The input features to the classifier include n-grams, features generated from part-of-speech tags, and word embeddings. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by sentiment analysis.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the SentimentBatchResult object
     */
    Observable<SentimentBatchResult> sentimentAsync(MultiLanguageBatchInput input);

    /**
     * The API returns a numeric score between 0 and 1.
     * Scores close to 1 indicate positive sentiment, while scores close to 0 indicate negative sentiment. Sentiment score is generated using classification techniques. The input features to the classifier include n-grams, features generated from part-of-speech tags, and word embeddings. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by sentiment analysis.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the SentimentBatchResult object
     */
    Observable<ServiceResponse<SentimentBatchResult>> sentimentWithServiceResponseAsync(MultiLanguageBatchInput input);

}
