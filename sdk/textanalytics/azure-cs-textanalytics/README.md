# TextAnalytics
## TextAnalyticsClientBuilder
```java
public final class TextAnalyticsClientBuilder {

    public TextAnalyticsClientBuilder() {}

    public TextAnalyticsClient buildClient() {}
    public TextAnalyticsAsyncClient buildAsyncClient() {}
    
    public TextAnalyticsClientBuilder endpoint(String endpoint) {}
    public TextAnalyticsClientBuilder credential(TokenCredential tokenCredential) {}
    public TextAnalyticsClientBuilder httpLogOptions(HttpLogOptions logOptions) {}
    public TextAnalyticsClientBuilder addPolicy(HttpPipelinePolicy policy) {}
    public TextAnalyticsClientBuilder httpClient(HttpClient client) {}
    public TextAnalyticsClientBuilder pipeline(HttpPipeline pipeline) {}
    public TextAnalyticsClientBuilder configuration(Configuration configuration) {}
    public TextAnalyticsClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {}
    public TextAnalyticsClientBuilder serviceVersion(ConfigurationServiceVersion version) {}
}
```

## TextAnalyticsAsyncClient
```java
public final class TextAnalyticsAsyncClient {
    // (1) language
    public Mono<DetectedLanguage> detectLanguage(String text) {}
    public Mono<DetectedLanguage> detectLanguage(String text, String countryHint) {}
    public Mono<Response<DetectedLanguage>> detectLanguageWithResponse(String text, String countryHint) {}
    
    public Mono<DocumentResultCollection<DetectedLanguage>> detectLanguages(List<String> inputs) {}
    // why language parameter in detectLanguage?
    public Mono<DocumentResultCollection<DetectedLanguage>> detectLanguages(List<UnknownLanguageInput> inputs, String language) {}

    public Mono<DocumentResultCollection<DetectedLanguage>> detectLanguages(List<UnknownLanguageInput> documents) {}
    public Mono<DocumentResultCollection<DetectedLanguage>> detectLanguages(
        List<UnknownLanguageInput> documents, TextAnalyticsRequestOptions options) {}
    public Mono<Response<DocumentResultCollection<DetectedLanguage>>> detectLanguagesWithResponse(
        List<UnknownLanguageInput> documents, TextAnalyticsRequestOptions options) {}
    
    // (2) entities
    public PagedFlux<NamedEntity> recognizeEntities(String text) {}
    public PagedFlux<NamedEntity> recognizeEntities(String text, String language) {}
    
    public Mono<DocumentResultCollection<NamedEntity>> recognizeEntities(List<String> inputs) {}
    public Mono<DocumentResultCollection<NamedEntity>> recognizeEntities(List<String> inputs, String language) {}

    public Mono<DocumentResultCollection<NamedEntity>> recognizeEntities(
        List<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {}
    public Mono<Response<DocumentResultCollection<NamedEntity>>> recognizeEntitiesWithResponse(
        List<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {}

    // (3) PII entities
    public PagedFlux<NamedEntity> recognizePiiEntities(String text) {}
    public PagedFlux<NamedEntity> recognizePiiEntities(String text, String language) {}
    
    public Mono<DocumentResultCollection<NamedEntity>> recognizePiiEntities(List<String> inputs) {}
    public Mono<DocumentResultCollection<NamedEntity>> recognizePiiEntities(List<String> inputs, String language) {}
    
    public Mono<DocumentResultCollection<NamedEntity>> recognizePiiEntities(List<TextDocumentInput> documents) {}
    public Mono<DocumentResultCollection<NamedEntity>> recognizePiiEntities(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {}
    public Mono<Response<DocumentResultCollection<NamedEntity>>> recognizePiiEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {}
        
    // (4) Link entities
    public PagedFlux<LinkedEntity> recognizeLinkedEntities(String text) {} // in .NET this returns a collection why paged flux here?
    public PagedFlux<LinkedEntity> recognizeLinkedEntities(String text, String language) {}
    
    public Mono<DocumentResultCollection<LinkedEntity>> recognizeLinkedEntities(List<String> inputs) {}
    public Mono<DocumentResultCollection<LinkedEntity>> recognizeLinkedEntities(List<String> inputs, String language) {}
    
    public Mono<DocumentResultCollection<LinkedEntity>> recognizeLinkedEntities(List<TextDocumentInput> documents) {}
    public Mono<DocumentResultCollection<LinkedEntity>> recognizeLinkedEntities(
        List<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {}
    public Mono<Response<DocumentResultCollection<LinkedEntity>>> recognizeLinkedEntitiesWithResponse(
        List<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {}

    // (5) key phrase
    public PagedFlux<String> extractKeyPhrases(String text) {} // not be a string but a model type keyPhrase?
    public PagedFlux<String> extractKeyPhrases(String text, String language) {}
    
    public Mono<DocumentResultCollection<String>> extractKeyPhrases(List<String> inputs) {}
    public Mono<DocumentResultCollection<String>> extractKeyPhrases(List<String> inputs, String language) {}

    public Mono<DocumentResultCollection<String>> extractKeyPhrases(List<TextDocumentInput> documents) {}
    public Mono<DocumentResultCollection<String>> extractKeyPhrases(
        List<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {}
    public Mono<Response<DocumentResultCollection<String>>> extractKeyPhrasesWithResponse(
        List<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {}
        
    // (6) sentiment
    public Mono<TextSentiment> analyzeSentenceSentiment(String sentence) {}
    public Mono<TextSentiment> analyzeSentenceSentiment(String sentence, String language) {}
    public Mono<Response<TextSentiment>> analyzeSentenceSentimentWithResponse(String sentence, String language) {}
    
    public Mono<DocumentResultCollection<TextSentiment>> analyzeDocumentSentiment(List<String> inputs) {}
    public Mono<DocumentResultCollection<TextSentiment>> analyzeDocumentSentiment(List<String> inputs, String langauge) {}

    public Mono<DocumentResultCollection<TextSentiment>> analyzeDocumentSentiment(List<TextDocumentInput> documents) {}
    public Mono<DocumentResultCollection<TextSentiment>> analyzeDocumentSentiment(
        List<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {}
    public Mono<Response<DocumentResultCollection<DocumentSentiment>>> analyzeDocumentSentimentWithResponse(
        List<TextDocumentInput> documents, TextAnalyticsRequestOptions options) {}
}

```

## TextAnalyticsClient // TODO: need to update with the async api changes
```java
public final class TextAnalyticsClient {
    // (1) language
    public DetectedLanguage detectLanguage(String text, String countryHint) {}
    public Response<DetectedLanguage> detectLanguageWithResponse(String text, String countryHint, Context context){}
                                                                     
    public DocumentResultCollection<DetectedLanguage> detectLanguages(List<UnknownLanguageInput> document,
                                                                      TextAnalyticsRequestOptions options) {}
    public Response<DocumentResultCollection<DetectedLanguage>> detectLanguagesWithResponse(
        List<UnknownLanguageInput> document, TextAnalyticsRequestOptions options, Context context) {}

    // (2) entities
    public PagedIterable<Entity> recognizeEntities(String text, String language) {}
        
    public DocumentResultCollection<Entity> recognizeEntities(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options){}
    public Response<DocumentResultCollection<Entity>> recognizeEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {}

    // (3) PII entities
    public PagedIterable<Entity> recognizePiiEntities(String text, String language) {}    
    
    public DocumentResultCollection<Entity> recognizePiiEntities(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {}
    public Response<DocumentResultCollection<Entity>> recognizePiiEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {}
        
    // (4) Link entities
    public PagedIterable<LinkedEntity> recognizeLinkedEntities(String text, String language) {}
            
    public DocumentResultCollection<LinkedEntity> recognizeLinkedEntities(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {}
    public Response<DocumentResultCollection<LinkedEntity>> recognizeLinkedEntitiesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {}

    // (5) key phrase
    public PagedIterable<String> extractKeyPhrases(String text, String language) {}
        
    public DocumentResultCollection<String> extractKeyPhrases(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {}
    public Response<DocumentResultCollection<String>> extractKeyPhrasesWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {}
        
    // (6) sentiment
    public Sentiment analyzeSentenceSentiment(String text, String language) {}
    public Response<Sentiment> analyzeSentenceSentimentWithResponse(
        String text, String language, Context context) {}
            
    public DocumentResultCollection<DocumentSentiment> analyzeDocumentSentiment(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options) {}
    public Response<DocumentResultCollection<DocumentSentiment>> analyzeDocumentSentimentWithResponse(
        List<TextDocumentInput> document, TextAnalyticsRequestOptions options, Context context) {}
}

```

## Input Models
### UnknownLanguageInput
``` java 
public final class UnknownLanguageInput {
    public UnknownLanguageInput(String text) {}
    public String getId() {}
    public UnknownLanguageInput setId(String id) {}
    public String getText() {}
    public String getCountryHint() {}
    public UnknownLanguageInput setCountryHint(String countryHint) {}
}
```
### TextDocumentInput
``` java 
public final class TextDocumentInput {
    public TextDocumentInput(String text) {}
    public String getId() {}
    public TextDocumentInput setId(String id) {}
    public String getText() {}
    public String getLanguage() {}
    public TextDocumentInput setLanguage(String language) {}
}
```
### TextAnalyticsRequestOptions
``` java 
public final class TextAnalyticsRequestOptions {
    public String getModelVersion() {}
    public TextAnalyticsRequestOptions setModelVersion(String modelVersion) {}
    public boolean isShowStatistics() {}
    public TextAnalyticsRequestOptions setShowStatistics(boolean showStatistics) {}
}
```
## Output Models
### DocumentResult<T>
``` java 
public final class DocumentResult<T> {
    public String getId() {}
    public DocumentStatistics getStatistics() {}
    public List<T> getItems() {}
}
```
### DocumentResultCollection<T>
``` java 
public final class DocumentResultCollection<T> extends IterableStream<DocumentResult<T>> {
    public IterableStream<DocumentError> getErrors() {}
    public String getModelVersion() {}
    public DocumentBatchStatistics getStatistics() {}
}
```
### TextDocumentStatistics
``` java 
public final class TextDocumentStatistics {
    public int getCharacterCount() {}
    public int getTransactionCount() {}
}
```
### TextBatchStatistics
``` java 
public final class TextBatchStatistics {
    public int getDocumentCount() {}
    public int getValidDocumentCount() {}
    public int getErroneousDocumentCount() {}
    public long getTransactionCount() {}
}
```
### DocumentError
``` java 
public final class DocumentError {
    public String getId() {}
    public Object getError()
}
```
### DetectedLanguage
``` java 
public final class DetectedLanguage {
    public String getName() {}
    public String getIso6391Name() {}
    public double getScore() {}
}
```
### NamedEntity
``` java 
public final class NamedEntity {
    public String getText() {}
    public String getSubType() {}
    public String getType() {}
    public int getOffset() {}
    public int getLength() {}
    public double getScore() {}
}
```
### LinkedEntity
``` java 
public final class LinkedEntity {
    public String getName() {}
    public List<LinkedEntityMatch> getMatches() {}
    public String getLanguage() {}
    public String getId() {}
    public String getUrl() {}
    public String getDataSource() {}
}
```
### LinkedEntityMatch
``` java 
public final class LinkedEntityMatch {
    public double getScore() {}
    public String getText() {}
    public int getOffset() {}
    public int getLength() {}
}
```    
### TextSentiment
``` java 
public final class TextSentiment {
    public String getLength() {}
    public double getNegativeScore() {}
    public double getNeutralScore() {}
    public double getPositiveScore() {}
    public int getOffSet() {}
    public TextSentimentClass getSentimentClass() {}
}
```
### TextSentimentClass
``` java 
public enum TextSentimentClass {
    POSITIVE,
    NEGATIVE,
    NEUTRAL,
    MIXED;
}
```
### DocumentSentiment
``` java 
public final class DocumentSentiment {
    public TextSentiment getDocumentSentiment() {}
    public DocumentResult<TextSentiment> getSentenceSentiments() {}
    //  public static implicit operator TextSentiment(TextSentimentCollection sentiments);
}
```

### TextAnalyticsServiceVersion
```java
public enum TextAnalyticsServiceVersion implements ServiceVersion {
    V1_0("1.0");
    public String getVersion() {}
    public static TextAnalyticsServiceVersion getLatest() {}
}
```

# Samples
## Create a synchronous client
```java
// TODO: user AAD token to do the authentication
// Instantiate a client that will be used to call the service.
TextAnalyticsClient client = new TextAnalyticsClientBuilder()
    .buildClient();

```
## Single Text
### 1. Detect language in text.
```java
 // The text that need be analysed.
String text = "hello world";

DetectedLanguage detectedLanguage = client.detectLanguage(text);
System.out.printf("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());

```
### 2. Recognize entities in text.
```java
// The text that need be analysed.
String text = "Satya Nadella is the CEO of Microsoft";

client.recognizeEntities(text).stream().forEach(
    namedEntity -> System.out.printf(
        "Recognized Entity: %s, Entity Type: %s, Entity Subtype: %s, Offset: %s, Length: %s, Score: %s",
        namedEntity.getText(),
        namedEntity.getType(),
        namedEntity.getSubType(),
        namedEntity.getOffset(),
        namedEntity.getLength(),
        namedEntity.getScore()));

```
### 3. Recognize personally identifiable information in text.
```java
// The text that need be analysed.
String text = "My SSN is 555-55-5555";

client.recognizePiiEntities(text).stream().forEach(
    namedEntity -> System.out.printf(
        "Recognized PII Entity: %s, Entity Type: %s, Entity Subtype: %s, Offset: %s, Length: %s, Score: %s%n",
        namedEntity.getText(),
        namedEntity.getType(),
        namedEntity.getSubType(),
        namedEntity.getOffset(),
        namedEntity.getLength(),
        namedEntity.getScore())));
```
### 4. Recognize linked entities in text.
```java
// The text that need be analysed.
String text = "Old Faithful is a geyser at Yellowstone Park";

client.recognizeLinkedEntities(text).stream().forEach(
    linkedEntity -> System.out.printf("Recognized Linked Entity: %s, URL: %s, Data Source: %s%n",
        linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource());

```
### 5. Recognize key phrases in text.
```java
String text = "My cat might need to see a veterinarian";
client.extractKeyPhrases(text).stream().forEach(
    phrase -> System.out.printf(String.format("Recognized Phrases: %s", phrase)));

```
### 6. Analyze sentiment in text.
```java
 // The text that need be analysed.
String text = "The hotel was dark and unclean.";

TextSentiment sentenceTextSentiment = client.analyzeSentenceSentiment(text);

System.out.printf("Recognized Sentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s.%n",
    sentenceTextSentiment.getSentimentClass(),
    sentenceTextSentiment.getPositiveScore(),
    sentenceTextSentiment.getNeutralScore(),
    sentenceTextSentiment.getNegativeScore()
);

```
### Batch of Documents
### 1. Batch level document statistics
```java
// The texts that need be analysed.
List<UnknownLanguageInput> inputs = Arrays.asList(
    new UnknownLanguageInput("This is written in English").setCountryHint("US"), 
    new UnknownLanguageInput("Este es un document escrito en Español.").setCountryHint("es")
);

TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
DocumentResultCollection<DetectedLanguage> detectedBatchResult = client.detectLanguages(inputs, requestOptions);

final String modelVersion = detectedBatchResult.getModelVersion();
System.out.printf("Model version: %s", modelVersion);

final DocumentBatchStatistics batchStatistics = detectedBatchResult.getBatchStatistics();
System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
    batchStatistics.getDocumentsCount(),
    batchStatistics.getErroneousDocumentsCount(),
    batchStatistics.getTransactionsCount(),
    batchStatistics.getValidDocumentsCount());
```
### 1.1 Detect language per document
```java
// Detecting languages per document from a batch of documents
detectedBatchResult.stream().forEach(detectedLanguageDocumentResult -> 
    detectedLanguageDocumentResult.getItems().stream().forEach(detectedLanguage ->
        System.out.printf("Detected Language: %s, ISO 6391 Name: %s, Score: %s",
            detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore())));

```
### 2. Batch level document statistics
```java
// The texts that need be analysed.
List<TextDocumentInput> inputs = Arrays.asList(
    new TextDocumentInput("Satya Nadella is the CEO of Microsoft").setLanguage("US"), 
    new TextDocumentInput("Elon Musk is the CEO of SpaceX and Tesla.").setLanguage("US")
));

TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
DocumentResultCollection<NamedEntity> recognizedEntitiesBatchResult = client.recognizeEntities(inputs, requestOptions);
// Document level statistics
final String modelVersion = recognizedEntitiesBatchResult.getModelVersion();
System.out.printf("Model version: %s", modelVersion);

final DocumentBatchStatistics batchStatistics = recognizedEntitiesBatchResult.getStatistics();
System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
    batchStatistics.getDocumentsCount(),
    batchStatistics.getErroneousDocumentsCount(),
    batchStatistics.getTransactionsCount(),
    batchStatistics.getValidDocumentsCount());
```
### 2.1 Recognize entities per document in a batch of documents.
```java
// Detecting entities for each of document from a batch of documents
for (DocumentResult<NamedEntity> entitiesList : recognizedEntitiesBatchResult.getDocumentResults()) {
    final DocumentStatistics textDocumentStatistics = entitiesList.getDocumentStatistics();
    System.out.printf("Per entity document statistics, character count: %s, transaction count: %s.",
        textDocumentStatistics.getCharactersCount(), textDocumentStatistics.getTransactionsCount());

    final List<NamedEntity> entities = entitiesList.getItems();
    for (Entity entity : entities) {
        System.out.printf("Recognized Entity: %s, Entity Type: %s, Entity Subtype: %s, Offset: %s, Length: %s, Score: %s",
            entity.getText(), entity.getType(), entity.getSubType(), entity.getOffset(), entity.getLength(), entity.getScore());
    }
}
```

### 3. Batch level document statistics
```java
// The texts that need be analysed.
List<TextDocumentInput> documents = Arrays.asList(
        new TextDocumentInput("My SSN is 555-55-5555").setLanguage("US"), 
        new TextDocumentInput("Visa card 4147999933330000").setLanguage("US")
    ));

TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
DocumentResultCollection<NamedEntity> recognizedPiiEnitiesBatchResult = client.recognizePiiEntities(documents, requestOptions);
// Batch level statistics
final String modelVersion = recognizedPiiEnitiesBatchResult.getModelVersion();
System.out.printf(("Model version: %s", modelVersion));

final DocumentBatchStatistics batchStatistics = recognizedPiiEnitiesBatchResult.getStatistics();
System.out.printf("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
    batchStatistics.getDocumentsCount(),
    batchStatistics.getErroneousDocumentsCount(),
    batchStatistics.getTransactionsCount(),
    batchStatistics.getValidDocumentsCount());

```
### 3.1 Recognize personally identifiable information per document in a batch of documents.
```java
for (DocumentResult<NamedEntity> entitiesList : recognizedPiiEnitiesBatchResult.getDocumentResults()) {
    final DocumentStatistics textDocumentStatistics = entitiesList.getDocumentStatistics();
    System.out.printf(("One PII entity document statistics, character count: %s, transaction count: %s.",
        textDocumentStatistics.getCharactersCount(), textDocumentStatistics.getTransactionsCount()));

    List<NamedEntity> entities = entitiesList.getItems();
    for (Entity entity : entities) {
        System.out.printf((
            "Recognized Personal Idenfiable Info Entity: %s, Entity Type: %s, Entity Subtype: %s, Offset: %s, Length: %s, Score: %s",
            entity.getText(), entity.getType(), entity.getSubType(), entity.getOffset(), entity.getLength(), entity.getScore()));
    }
}
```
> ===== TODO Below this ===

### 4. Recognize linked entities in a batch of documents.
```java
// The texts that need be analysed.
List<TextDocumentInput> documents = new ArrayList<>();
TextDocumentInput input = new TextDocumentInput();

input.setId("1").setText("Old Faithful is a geyser at Yellowstone Park").setLanguage("US");
TextDocumentInput input2 = new TextDocumentInput();
input2.setId("2").setText("Mount Shasta has lenticular clouds.").setLanguage("US");

documents.add(input);
documents.add(input2);

TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
DocumentResultCollection<LinkedEntity> detectedResult = client.recognizeLinkedEntities(documents, requestOptions);

final String modelVersion = detectedResult.getModelVersion();
System.out.printf(("Model version: %s", modelVersion));

final DocumentBatchStatistics batchStatistics = detectedResult.getStatistics();
System.out.printf(("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
    batchStatistics.getDocumentsCount(),
    batchStatistics.getErroneousDocumentsCount(),
    batchStatistics.getTransactionsCount(),
    batchStatistics.getValidDocumentsCount()));

// Detecting language from a batch of documents
for (DocumentResult<LinkedEntity> documentLinkedEntities : detectedResult) {
    final DocumentStatistics textDocumentStatistics = documentLinkedEntities.getDocumentStatistics();
    System.out.printf(("One linked entity document statistics, character count: %s, transaction count: %s.",
        textDocumentStatistics.getCharactersCount(), textDocumentStatistics.getTransactionsCount()));

    final List<LinkedEntity> linkedEntities = documentLinkedEntities.getItems();
    for (LinkedEntity linkedEntity : linkedEntities) {
        System.out.printf(("Recognized Linked Entity: %s, URL: %s, Data Source: %s",
            linkedEntity.getName(), linkedEntity.getUrl(), linkedEntity.getDataSource()));
    }
}
```
### 5. Recognize key phrases in a batch of documents.
```java
// The texts that need be analysed.
List<TextDocumentInput> document = new ArrayList<>();
TextDocumentInput input = new TextDocumentInput();
input.setId("1").setText("My cat might need to see a veterinarian").setLanguage("US");
TextDocumentInput input2 = new TextDocumentInput();
input2.setId("2").setText("The pitot tube is used to measure airspeed.").setLanguage("US");
document.add(input);
document.add(input2);

TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");
DocumentResultCollection<String> detectedResult = client.extractKeyPhrases(document, requestOptions);

final String modelVersion = detectedResult.getModelVersion();
System.out.printf(("Model version: %s", modelVersion));

final DocumentBatchStatistics batchStatistics = detectedResult.getStatistics();
System.out.printf(("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
    batchStatistics.getDocumentsCount(),
    batchStatistics.getErroneousDocumentsCount(),
    batchStatistics.getTransactionsCount(),
    batchStatistics.getValidDocumentsCount()));

// Detecting key phrase for each of document from a batch of documents
for (DocumentResult<String> keyPhraseList : detectedResult) {
    final DocumentStatistics textDocumentStatistics = keyPhraseList.getDocumentStatistics();
    System.out.printf(("One key phrase document statistics, character count: %s, transaction count: %s.",
        textDocumentStatistics.getCharactersCount(), textDocumentStatistics.getTransactionsCount()));
    final List<String> keyPhrases = keyPhraseList.getItems();
    for (String phrase : keyPhrases) {
        System.out.printf(("Recognized Phrases: %s", phrase));
    }
}
```
### 6. Analyze sentiment in a batch of documents.
```java
 // The texts that need be analysed.
List<TextDocumentInput> documents = new ArrayList<>();
TextDocumentInput input = new TextDocumentInput();
input.setId("1").setText("The hotel was dark and unclean.").setLanguage("US");
TextDocumentInput input2 = new TextDocumentInput();
input2.setId("2").setText("The restaurant had amazing gnocci.").setLanguage("US");
documents.add(input);
documents.add(input2);

TextAnalyticsRequestOptions requestOptions = new TextAnalyticsRequestOptions().setShowStatistics(true).setModelVersion("1.0");

DocumentResultCollection<DocumentSentiment> detectedResult = client.analyzeDocumentSentiment(documents, requestOptions);
final String modelVersion = detectedResult.getModelVersion();
System.out.printf(("Model version: %s", modelVersion));

final DocumentBatchStatistics batchStatistics = detectedResult.getStatistics();
System.out.printf(("A batch of document statistics, document count: %s, erroneous document count: %s, transaction count: %s, valid document count: %s",
    batchStatistics.getDocumentsCount(),
    batchStatistics.getErroneousDocumentsCount(),
    batchStatistics.getTransactionsCount(),
    batchStatistics.getValidDocumentsCount()));

// Detecting sentiment for each of document from a batch of documents
for (DocumentResult<DocumentSentiment> documentSentimentDocumentResult : detectedResult) {
    // For each document
    final DocumentStatistics textDocumentStatistics = documentSentimentDocumentResult.getDocumentStatistics();
    System.out.printf(("One sentiment document statistics, character count: %s, transaction count: %s.",
        textDocumentStatistics.getCharactersCount(), textDocumentStatistics.getTransactionsCount()));

    final List<DocumentSentiment> documentSentiments = documentSentimentDocumentResult.getItems();

    for (DocumentSentiment item : documentSentiments) {
        final Sentiment documentTextSentiment = item.getDocumentSentiment();
        System.out.printf((
            "Recognized document sentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s.",
            documentTextSentiment.getSentimentClass(),
            documentTextSentiment.getPositiveScore(),
            documentTextSentiment.getNeutralScore(),
            documentTextSentiment.getNegativeScore()));

        final List<Sentiment> sentenceSentiments = item.getItems();
        for (Sentiment sentenceTextSentiment : sentenceSentiments) {
            System.out.printf((
                "Recognized sentence sentiment: %s, Positive Score: %s, Neutral Score: %s, Negative Score: %s. Length of sentence: %s, Offset of sentence: %s",
                sentenceTextSentiment.getSentimentClass(),
                sentenceTextSentiment.getPositiveScore(),
                sentenceTextSentiment.getNeutralScore(),
                sentenceTextSentiment.getNegativeScore(),
                sentenceTextSentiment.getLength(),
                sentenceTextSentiment.getOffSet()));
        }
    }
}

```
