# TextAnalytics
## TextAnalyticsClientBuilder
```java
@ServiceClientBuilder(serviceClients = {TextAnalyticsAsyncClient.class, TextAnalyticsClient.class})
public final class TextAnalyticsClientBuilder {
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

## TextAnalyticsAsyncClient
```java
@ServiceClient(builder = TextAnalyticsClientBuilder.class, isAsync = true)
public final class TextAnalyticsAsyncClient {
    final TextAnalyticsClientImpl textAnalyticsClientImpl;
    
    TextAnalyticsAsyncClient(TextAnalyticsClientImpl textAnalyticsClientImpl);
    
    // (1) language
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LanguageResult> detectLanguage(String text, String countryHint, boolean showStats);
   
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LanguageResult>> detectLanguageWithResponse(LanguageInput languageInput, boolean showStats);

    // (1.1) A batch of language input 
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LanguageResult> detectLanguage(LanguageBatchInput languageBatchInput, boolean showStats);
       
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LanguageResult>> detectLanguageBatchWithResponse(LanguageBatchInput languageBatchInput, boolean showStats);

    // (2) entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EntitiesResult> detectEntitiesWithResponse(String text, String language, boolean showStats);

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> detectEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats);

    // (3) health care entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EntitiesResult> detectHealthCareEntities(String text, String language, boolean showStats);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> detectHealthCareEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats);
    
    // (4) PII entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EntitiesResult> detectPIIEntities(String text, String language, boolean showStats);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntitiesResult>> detectPIIEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats);
    
    // (5) Link entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EntityLinkingResult> detectLinkedEntities(String text, String language, boolean showStats);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EntityLinkingResult>> detectLinkedEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats);
   
    // (6) key phrase
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyPhraseResult> detectKeyPhrases(String text, String language, boolean showStats);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyPhraseResult>> detectKeyPhrasesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats);
    
    // (7) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SentimentResponse> detectSentiment(String text, String language, boolean showStats);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SentimentResponse>> detectSentimentBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats);
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
    public EntitiesResult detectEntities(String text, String language, boolean showStats);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> detectEntitiesBatchWithResponse(MultiLanguageBatchInput  multiLanguageBatchInput, boolean showStats, Context context);

    // (3) health care entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntitiesResult detectHealthCareEntities(String text, String language, boolean showStats);

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> detectHealthCareEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context);
    
    // (4) PII entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntitiesResult detectPIIEntities(String text, String language, boolean showStats);

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntitiesResult> detectPIIEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context);
    
    // (5) Link entities
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EntityLinkingResult detectLinkedEntities(String text, String language, boolean showStats);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<EntityLinkingResult> detectLinkedEntitiesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context);
    
    // (6) key phrase
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyPhraseResult detectKeyPhrases(String text, String language, boolean showStats);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyPhraseResult> detectKeyPhrasesBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context);
    
    // (7) sentiment
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SentimentResponse detectSentiment(String text, String language, boolean showStats);
    
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SentimentResponse> detectSentimentBatchWithResponse(MultiLanguageBatchInput multiLanguageBatchInput, boolean showStats, Context context);
}
```
