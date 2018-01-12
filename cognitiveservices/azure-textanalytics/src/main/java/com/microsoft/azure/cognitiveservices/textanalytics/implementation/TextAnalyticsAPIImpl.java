/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics.implementation;

import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.cognitiveservices.textanalytics.AzureRegions;
import com.microsoft.azure.cognitiveservices.textanalytics.ErrorResponseException;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.Validator;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * Initializes a new instance of the TextAnalyticsAPIImpl class.
 */
public class TextAnalyticsAPIImpl extends AzureServiceClient {
    /** The Retrofit service to perform REST calls. */
    private TextAnalyticsAPIService service;
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'. */
    private AzureRegions azureRegion;

    /**
     * Gets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @return the azureRegion value.
     */
    public AzureRegions azureRegion() {
        return this.azureRegion;
    }

    /**
     * Sets Supported Azure regions for Cognitive Services endpoints. Possible values include: 'westus', 'westeurope', 'southeastasia', 'eastus2', 'westcentralus', 'westus2', 'eastus', 'southcentralus', 'northeurope', 'eastasia', 'australiaeast', 'brazilsouth'.
     *
     * @param azureRegion the azureRegion value.
     * @return the service client itself
     */
    public TextAnalyticsAPIImpl withAzureRegion(AzureRegions azureRegion) {
        this.azureRegion = azureRegion;
        return this;
    }

    /** Gets or sets the preferred language for the response. */
    private String acceptLanguage;

    /**
     * Gets Gets or sets the preferred language for the response.
     *
     * @return the acceptLanguage value.
     */
    public String acceptLanguage() {
        return this.acceptLanguage;
    }

    /**
     * Sets Gets or sets the preferred language for the response.
     *
     * @param acceptLanguage the acceptLanguage value.
     * @return the service client itself
     */
    public TextAnalyticsAPIImpl withAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    /** Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30. */
    private int longRunningOperationRetryTimeout;

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    public int longRunningOperationRetryTimeout() {
        return this.longRunningOperationRetryTimeout;
    }

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     * @return the service client itself
     */
    public TextAnalyticsAPIImpl withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
        return this;
    }

    /** When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true. */
    private boolean generateClientRequestId;

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @return the generateClientRequestId value.
     */
    public boolean generateClientRequestId() {
        return this.generateClientRequestId;
    }

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @param generateClientRequestId the generateClientRequestId value.
     * @return the service client itself
     */
    public TextAnalyticsAPIImpl withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
        return this;
    }

    /**
     * Initializes an instance of TextAnalyticsAPI client.
     *
     * @param credentials the management credentials for Azure
     */
    public TextAnalyticsAPIImpl(ServiceClientCredentials credentials) {
        this("https://{AzureRegion}.api.cognitive.microsoft.com/text/analytics", credentials);
    }

    /**
     * Initializes an instance of TextAnalyticsAPI client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public TextAnalyticsAPIImpl(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Initializes an instance of TextAnalyticsAPI client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public TextAnalyticsAPIImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.azureClient = new AzureClient(this);
        initializeService();
    }

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    @Override
    public String userAgent() {
        return String.format("%s (%s, %s)", super.userAgent(), "TextAnalyticsAPI", "v2.0");
    }

    private void initializeService() {
        service = restClient().retrofit().create(TextAnalyticsAPIService.class);
    }

    /**
     * The interface defining all the services for TextAnalyticsAPI to be
     * used by Retrofit to perform actually REST calls.
     */
    interface TextAnalyticsAPIService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.textanalytics.TextAnalyticsAPI keyPhrases" })
        @POST("v2.0/keyPhrases")
        Observable<Response<ResponseBody>> keyPhrases(@Body MultiLanguageBatchInputInner input, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.textanalytics.TextAnalyticsAPI detectLanguage" })
        @POST("v2.0/languages")
        Observable<Response<ResponseBody>> detectLanguage(@Body BatchInputInner input, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.cognitiveservices.textanalytics.TextAnalyticsAPI sentiment" })
        @POST("v2.0/sentiment")
        Observable<Response<ResponseBody>> sentiment(@Body MultiLanguageBatchInputInner input, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

    }

    /**
     * The API returns a list of strings denoting the key talking points in the input text.
     * We employ techniques from Microsoft Office's sophisticated Natural Language Processing toolkit. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by key phrase extraction.
     *
     * @param input Collection of documents to analyze. Documents can now contain a language field to indicate the text language
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the KeyPhraseBatchResultInner object if successful.
     */
    public KeyPhraseBatchResultInner keyPhrases(MultiLanguageBatchInputInner input) {
        return keyPhrasesWithServiceResponseAsync(input).toBlocking().single().body();
    }

    /**
     * The API returns a list of strings denoting the key talking points in the input text.
     * We employ techniques from Microsoft Office's sophisticated Natural Language Processing toolkit. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by key phrase extraction.
     *
     * @param input Collection of documents to analyze. Documents can now contain a language field to indicate the text language
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<KeyPhraseBatchResultInner> keyPhrasesAsync(MultiLanguageBatchInputInner input, final ServiceCallback<KeyPhraseBatchResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(keyPhrasesWithServiceResponseAsync(input), serviceCallback);
    }

    /**
     * The API returns a list of strings denoting the key talking points in the input text.
     * We employ techniques from Microsoft Office's sophisticated Natural Language Processing toolkit. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by key phrase extraction.
     *
     * @param input Collection of documents to analyze. Documents can now contain a language field to indicate the text language
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the KeyPhraseBatchResultInner object
     */
    public Observable<KeyPhraseBatchResultInner> keyPhrasesAsync(MultiLanguageBatchInputInner input) {
        return keyPhrasesWithServiceResponseAsync(input).map(new Func1<ServiceResponse<KeyPhraseBatchResultInner>, KeyPhraseBatchResultInner>() {
            @Override
            public KeyPhraseBatchResultInner call(ServiceResponse<KeyPhraseBatchResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * The API returns a list of strings denoting the key talking points in the input text.
     * We employ techniques from Microsoft Office's sophisticated Natural Language Processing toolkit. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by key phrase extraction.
     *
     * @param input Collection of documents to analyze. Documents can now contain a language field to indicate the text language
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the KeyPhraseBatchResultInner object
     */
    public Observable<ServiceResponse<KeyPhraseBatchResultInner>> keyPhrasesWithServiceResponseAsync(MultiLanguageBatchInputInner input) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (input == null) {
            throw new IllegalArgumentException("Parameter input is required and cannot be null.");
        }
        Validator.validate(input);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.keyPhrases(input, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<KeyPhraseBatchResultInner>>>() {
                @Override
                public Observable<ServiceResponse<KeyPhraseBatchResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<KeyPhraseBatchResultInner> clientResponse = keyPhrasesDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<KeyPhraseBatchResultInner> keyPhrasesDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<KeyPhraseBatchResultInner, ErrorResponseException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<KeyPhraseBatchResultInner>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * The API returns the detected language and a numeric score between 0 and 1.
     * Scores close to 1 indicate 100% certainty that the identified language is true. A total of 120 languages are supported.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the LanguageBatchResultInner object if successful.
     */
    public LanguageBatchResultInner detectLanguage(BatchInputInner input) {
        return detectLanguageWithServiceResponseAsync(input).toBlocking().single().body();
    }

    /**
     * The API returns the detected language and a numeric score between 0 and 1.
     * Scores close to 1 indicate 100% certainty that the identified language is true. A total of 120 languages are supported.
     *
     * @param input Collection of documents to analyze.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<LanguageBatchResultInner> detectLanguageAsync(BatchInputInner input, final ServiceCallback<LanguageBatchResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(detectLanguageWithServiceResponseAsync(input), serviceCallback);
    }

    /**
     * The API returns the detected language and a numeric score between 0 and 1.
     * Scores close to 1 indicate 100% certainty that the identified language is true. A total of 120 languages are supported.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the LanguageBatchResultInner object
     */
    public Observable<LanguageBatchResultInner> detectLanguageAsync(BatchInputInner input) {
        return detectLanguageWithServiceResponseAsync(input).map(new Func1<ServiceResponse<LanguageBatchResultInner>, LanguageBatchResultInner>() {
            @Override
            public LanguageBatchResultInner call(ServiceResponse<LanguageBatchResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * The API returns the detected language and a numeric score between 0 and 1.
     * Scores close to 1 indicate 100% certainty that the identified language is true. A total of 120 languages are supported.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the LanguageBatchResultInner object
     */
    public Observable<ServiceResponse<LanguageBatchResultInner>> detectLanguageWithServiceResponseAsync(BatchInputInner input) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (input == null) {
            throw new IllegalArgumentException("Parameter input is required and cannot be null.");
        }
        Validator.validate(input);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.detectLanguage(input, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<LanguageBatchResultInner>>>() {
                @Override
                public Observable<ServiceResponse<LanguageBatchResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<LanguageBatchResultInner> clientResponse = detectLanguageDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<LanguageBatchResultInner> detectLanguageDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<LanguageBatchResultInner, ErrorResponseException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<LanguageBatchResultInner>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

    /**
     * The API returns a numeric score between 0 and 1.
     * Scores close to 1 indicate positive sentiment, while scores close to 0 indicate negative sentiment. Sentiment score is generated using classification techniques. The input features to the classifier include n-grams, features generated from part-of-speech tags, and word embeddings. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by sentiment analysis.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws ErrorResponseException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the SentimentBatchResultInner object if successful.
     */
    public SentimentBatchResultInner sentiment(MultiLanguageBatchInputInner input) {
        return sentimentWithServiceResponseAsync(input).toBlocking().single().body();
    }

    /**
     * The API returns a numeric score between 0 and 1.
     * Scores close to 1 indicate positive sentiment, while scores close to 0 indicate negative sentiment. Sentiment score is generated using classification techniques. The input features to the classifier include n-grams, features generated from part-of-speech tags, and word embeddings. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by sentiment analysis.
     *
     * @param input Collection of documents to analyze.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<SentimentBatchResultInner> sentimentAsync(MultiLanguageBatchInputInner input, final ServiceCallback<SentimentBatchResultInner> serviceCallback) {
        return ServiceFuture.fromResponse(sentimentWithServiceResponseAsync(input), serviceCallback);
    }

    /**
     * The API returns a numeric score between 0 and 1.
     * Scores close to 1 indicate positive sentiment, while scores close to 0 indicate negative sentiment. Sentiment score is generated using classification techniques. The input features to the classifier include n-grams, features generated from part-of-speech tags, and word embeddings. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by sentiment analysis.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the SentimentBatchResultInner object
     */
    public Observable<SentimentBatchResultInner> sentimentAsync(MultiLanguageBatchInputInner input) {
        return sentimentWithServiceResponseAsync(input).map(new Func1<ServiceResponse<SentimentBatchResultInner>, SentimentBatchResultInner>() {
            @Override
            public SentimentBatchResultInner call(ServiceResponse<SentimentBatchResultInner> response) {
                return response.body();
            }
        });
    }

    /**
     * The API returns a numeric score between 0 and 1.
     * Scores close to 1 indicate positive sentiment, while scores close to 0 indicate negative sentiment. Sentiment score is generated using classification techniques. The input features to the classifier include n-grams, features generated from part-of-speech tags, and word embeddings. See the &lt;a href="https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/overview#supported-languages"&gt;Text Analytics Documentation&lt;/a&gt; for details about the languages that are supported by sentiment analysis.
     *
     * @param input Collection of documents to analyze.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the SentimentBatchResultInner object
     */
    public Observable<ServiceResponse<SentimentBatchResultInner>> sentimentWithServiceResponseAsync(MultiLanguageBatchInputInner input) {
        if (this.azureRegion() == null) {
            throw new IllegalArgumentException("Parameter this.azureRegion() is required and cannot be null.");
        }
        if (input == null) {
            throw new IllegalArgumentException("Parameter input is required and cannot be null.");
        }
        Validator.validate(input);
        String parameterizedHost = Joiner.on(", ").join("{AzureRegion}", this.azureRegion());
        return service.sentiment(input, this.acceptLanguage(), parameterizedHost, this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<SentimentBatchResultInner>>>() {
                @Override
                public Observable<ServiceResponse<SentimentBatchResultInner>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<SentimentBatchResultInner> clientResponse = sentimentDelegate(response);
                        return Observable.just(clientResponse);
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<SentimentBatchResultInner> sentimentDelegate(Response<ResponseBody> response) throws ErrorResponseException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<SentimentBatchResultInner, ErrorResponseException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<SentimentBatchResultInner>() { }.getType())
                .registerError(ErrorResponseException.class)
                .build(response);
    }

}
