# TextAnalytics
## TextAnalyticsClientBuilder
```java
@ServiceClientBuilder(serviceClients = {TextAnalyticsAsyncClient.class, TextAnalyticsClient.class})
public final class TextAnalyticsClientBuilder {
    private static final String ECHO_REQUEST_ID_HEADER = "x-ms-return-client-request-id";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.microsoft.azconfig.kv+json";

    private String endpoint;
    private String connectionString;
    
    private final List<HttpPipelinePolicy> policies;
    private final HttpHeaders headers;
    
    private HttpLogOptions httpLogOptions;
    private HttpClient httpClient;
    private HttpPipeline pipeline;
    private Configuration configuration;
    private RetryPolicy retryPolicy;
    
    public TextAnalyticsClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();
        headers = new HttpHeaders()
                .put(ECHO_REQUEST_ID_HEADER, "true")
                .put(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE)
                .put(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
    }; 
         
    // Build Async and Sync client
    public TextAnalyticsAsyncClient buildAsyncClient();
    public TextAnalyticsClient buildClient();
    
    // Properties
    public TextAnalyticsClientBuilder endpoint(String endpoint);
    public TextAnalyticsClientBuilder subscriptonKey(String connectionString);
    public TextAnalyticsClientBuilder httpLogOptions(HttpLogOptions httpLogOptions);
    public TextAnalyticsClientBuilder addPolicy(HttpPipelinePolicy policy);
    public TextAnalyticsClientBuilder httpClient(HttpClient httpClient);
    public TextAnalyticsClientBuilder pipeline(HttpPipeline pipeline);
    public TextAnalyticsClientBuilder configuration(Configuration configuration);
    public TextAnalyticsClientBuilder retryPolicy(RetryPolicy retryPolicy);
}
```

## TextAnalyticsClientImpl 
```java
public final class TextAnalyticsClientImpl {
    private TextAnalyticsClientService service;
    private String endpoint;
    private HttpPipeline httpPipeline;

    public String getEndpoint();
    public void setEndpoint();
    public String getHttpPipeline();
  
    public TextAnalyticsClientImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.service = RestProxy.create(TextAnalyticsClientService.class, this.httpPipeline);
    }
    
    private interface TextAnalyticsClientService{
        @Post("languages")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<LanguageBatchResult>> detectLanguage(@HostParam("Endpoint") String endpoint, @QueryParam("showStats") Boolean showStats, @BodyParam("application/json; charset=utf-8") LanguageBatchInput languageBatchInput);

        @Post("entities")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<EntitiesBatchResult>> entities(@HostParam("Endpoint") String endpoint, @QueryParam("showStats") Boolean showStats, @BodyParam("application/json; charset=utf-8") MultiLanguageBatchInput multiLanguageBatchInput);

        @Post("healthcare")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<EntitiesBatchResult>> healthCareEntities(@HostParam("Endpoint") String endpoint, @QueryParam("showStats") Boolean showStats, @BodyParam("application/json; charset=utf-8") MultiLanguageBatchInput multiLanguageBatchInput);
        
        @Post("entities/recognition/pii")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<EntitiesBatchResult>> entities(@HostParam("Endpoint") String endpoint, @QueryParam("showStats") Boolean showStats, @BodyParam("application/json; charset=utf-8") MultiLanguageBatchInput multiLanguageBatchInput);
        
        @Post("entities/linking")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<EntitiesBatchResult>> entities(@HostParam("Endpoint") String endpoint, @QueryParam("showStats") Boolean showStats, @BodyParam("application/json; charset=utf-8") MultiLanguageBatchInput multiLanguageBatchInput);
       
        @Post("keyPhrases")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<KeyPhraseBatchResult>> keyPhrases(@HostParam("Endpoint") String endpoint, @QueryParam("showStats") Boolean showStats, @BodyParam("application/json; charset=utf-8") MultiLanguageBatchInput multiLanguageBatchInput);

        @Post("sentiment")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<SimpleResponse<Object>> sentiment(@HostParam("Endpoint") String endpoint, @QueryParam("showStats") Boolean showStats, @BodyParam("application/json; charset=utf-8") MultiLanguageBatchInput multiLanguageBatchInput);
        
    };
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<LanguageBatchResult>> detectLanguageWithRestResponseAsync();
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<LanguageBatchResult>> detectLanguageWithRestResponseAsync(Boolean showStats, List<LanguageInput> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<EntitiesBatchResult>> entitiesWithRestResponseAsync();
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<EntitiesBatchResult>> entitiesWithRestResponseAsync(Boolean showStats, List<MultiLanguageInput> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<KeyPhraseBatchResult>> keyPhrasesWithRestResponseAsync();
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<KeyPhraseBatchResult>> keyPhrasesWithRestResponseAsync(Boolean showStats, List<MultiLanguageInput> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Object>> sentimentWithRestResponseAsync();
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Object>> sentimentWithRestResponseAsync(Boolean showStats, List<MultiLanguageInput> documents);
}
```

## TextAnalyticsAsyncClient
```java
@ServiceClient(builder = TextAnalyticsClientBuilder.class, isAsync = true)
public final class TextAnalyticsAsyncClient {
    final TextAnalyticsClientImpl textAnalyticsClientImpl;
    
    TextAnalyticsAsyncClient(TextAnalyticsClientImpl textAnalyticsClientImpl);
    
    // (1) language
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LanguageResult> detectLanguage(List<DocumentLanguage> documents);
   
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LanguageResult>> detectLanguageWithResponse(List<DocumentLanguage> documents, Boolean showStats);
    
    // (2) health care entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EntitiesBatchResult> detectHealthCareEntities(List<MultiLanguageInput> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesBatchResult>> detectHealthCareEntitiesWithResponse(List<MultiLanguageInput> documents, Boolean showStats);
    
    // (3) PII entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EntitiesBatchResult> detectPIIEntities(List<MultiLanguageInput> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesBatchResult>> detectPIIEntitiesWithResponse(List<MultiLanguageInput> documents, Boolean showStats);
    
    // (4) Link entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EntityLinkingResult> detectLinkedEntities(List<DocumentLinkedEntities> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntityLinkingResult>> detectLinkedEntitiesWithResponse(List<DocumentLinkedEntities> documents, Boolean showStats);
    
    // (5) entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EntitiesResult> detectEntitiesWithResponse(List<DocumentEntities> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> detectEntitiesWithResponse(List<DocumentEntities> documents, Boolean showStats);

    // (6) key phrase
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhraseResult> detectKeyPhrases(List<DocumentKeyPhrases> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyPhraseResult>> detectKeyPhrasesWithResponse(List<DocumentKeyPhrases> documents, Boolean showStats);
    
    // (7) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SentimentResponse> detectSentiment(List<DocumentSentiment> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SentimentResponse>> detectSentimentWithResponse(List<DocumentSentiment> documents, Boolean showStats);
}
```

## TextAnalyticsClient
```java
@ServiceClient(builder = TextAnalyticsClientBuilder.class)
public final class TextAnalyticsClient {
    private final TextAnalyticsAsyncClient textAnalyticsAsyncClient;
    
    TextAnalyticsClient(TextAnalyticsAsyncClient textAnalyticsAsyncClient);
    
    // (1) language
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LanguageResult detectLanguage(String text, String countryHint, boolean showStats);
   
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LanguageResult> detectLanguageWithResponse(LanguageInput languageInput, boolean showStats, Context context);
    
    // (1.1) A batch of language input 
     @ServiceMethod(returns = ReturnType.SINGLE)
     public LanguageResult detectLanguage(LanguageBatchInput languageBatchInput, boolean showStats);
       
     @ServiceMethod(returns = ReturnType.SINGLE)
     public Response<LanguageResult> detectLanguageWithResponse(LanguageBatchInput languageBatchInput, boolean showStats, Context context);

    // (2) entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntitiesResult detectEntities(String text, String countryHint, boolean showStats);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> detectEntitiesWithResponse(List<DocumentEntities> documents, Boolean showStats);

    // (2) health care entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntitiesBatchResult detectHealthCareEntities(List<MultiLanguageInput> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesBatchResult> detectHealthCareEntitiesWithResponse(List<MultiLanguageInput> documents, Boolean showStats);
    
    // (3) PII entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntitiesBatchResult detectPIIEntities(List<MultiLanguageInput> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesBatchResult> detectPIIEntitiesWithResponse(List<MultiLanguageInput> documents, Boolean showStats);
    
    // (4) Link entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntityLinkingResult detectLinkedEntities(List<DocumentLinkedEntities> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntityLinkingResult> detectLinkedEntitiesWithResponse(List<DocumentLinkedEntities> documents, Boolean showStats);
    
    

    // (6) key phrase
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyPhraseResult detectKeyPhrases(List<DocumentKeyPhrases> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyPhraseResult> detectKeyPhrasesWithResponse(List<DocumentKeyPhrases> documents, Boolean showStats);
    
    // (7) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SentimentResponse detectSentiment(List<DocumentSentiment> documents);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SentimentResponse> detectSentimentWithResponse(List<DocumentSentiment> documents, Boolean showStats);
}
