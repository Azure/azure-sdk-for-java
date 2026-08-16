# Protocol Methods


Protocol methods provide direct access to raw HTTP for advanced usage and preview APIs. They live in the same client as high-level convenience methods and leverage the full `azure-core` HTTP pipeline (retry, auth, logging, distributed tracing).

---

## Quick Start

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core</artifactId>
  <version>1.49.0</version>
</dependency>
```

```java
ExampleClient client = new ExampleClientBuilder()
    .endpoint("https://example.org/")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

Response<BinaryData> response = client.getHelloWithResponse(/*RequestOptions*/ null);

if (response.getStatusCode() != 200) {
    System.out.println("Failed: " + response.getValue().toString());
} else {
    System.out.println("Succeeded: " + response.getValue().toString());
}
```

---

## 1. Initialize the Client

The client is created from a builder named `{ServiceName}Builder`. One builder creates both high-level and protocol-method clients.

### Authenticate with Azure AD (`DefaultAzureCredential`)

```java
// Add azure-identity dependency first
ExampleClient client = new ExampleClientBuilder()
    .endpoint("https://example.org/")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### Authenticate with an API Key

```java
ExampleClient client = new ExampleClientBuilder()
    .endpoint("https://example.org/")
    .credential(new AzureKeyCredential("your-api-key"))
    .build();
```

---

## 2. Send a Request

All protocol methods accept:

```java
Response<BinaryData> methodCallWithResponse(
    String requiredParam1,
    String requiredParam2,
    BinaryData body,
    RequestOptions options);         // sync

Mono<Response<BinaryData>> methodCallWithResponseAsync(...);  // async
```

### `RequestOptions`

```java
public class RequestOptions {
    public RequestOptions addQueryParam(String name, String value);
    public RequestOptions addHeader(String header, String value);
    public RequestOptions setHeader(String header, String value);
    public RequestOptions addRequestCallback(Consumer<HttpRequest> callback);
    public RequestOptions setBody(BinaryData body);
    public RequestOptions setContext(Context context);
}
```

### Setting Optional Parameters

```java
RequestOptions options = new RequestOptions()
    .addQueryParam("$top", "10")
    .addHeader("x-ms-custom", "value");

Response<BinaryData> response = client.listItemsWithResponse(options);
```

### Setting the Request Body

```java
String jsonPayload = "{\"id\":\"0\",\"text\":\"hello world\"}";
RequestOptions options = new RequestOptions()
    .setBody(BinaryData.fromString(jsonPayload));

Response<BinaryData> response = client.createItemWithResponse(options);
```

### Override Method/URL (catch-all)

```java
RequestOptions options = new RequestOptions()
    .addRequestCallback(request -> {
        request.setHttpMethod(HttpMethod.PATCH);
        request.setUrl("https://example.org/custom-path");
    });
client.invokeWithResponse("https://...", HttpMethod.POST, body, options);
```

---

## 3. Handle the Response

### Response Body (`BinaryData`)

```java
Response<BinaryData> response = client.getItemWithResponse("id-1", null);
BinaryData responseBody = response.getValue();

// Convert to String
String json = responseBody.toString();

// Convert to a typed object with JSON-P
JsonReader reader = Json.createReader(new StringReader(json));
JsonObject obj = reader.readObject();
String name = obj.getString("name");
```

### Response Headers

```java
HttpHeaders headers = response.getHeaders();
String requestId = headers.getValue("x-ms-request-id");
```

### Status Code Checking

```java
if (response.getStatusCode() == 200) {
    // success
} else if (response.getStatusCode() == 404) {
    // not found
}
```

---

## Full Example (JSON-P)

```java
TextAnalyticsClient client = new TextAnalyticsClientBuilder()
    .credential(new AzureKeyCredential("{ApiKey}"))
    .endpoint("{Endpoint}")
    .buildClient();

// Build request JSON
JsonObject document = Json.createObjectBuilder()
    .add("id", "0")
    .add("text", "Old Faithful is a geyser at Yellowstone Park.")
    .build();
JsonObject batchInput = Json.createObjectBuilder()
    .add("documents", Json.createArrayBuilder().add(document))
    .build();

// Call protocol method
Response<BinaryData> response = client.entitiesLinkingWithResponse(
    BinaryData.fromString(batchInput.toString()), null);

// Parse response
JsonObject result = Json.createReader(
    new StringReader(response.getValue().toString())).readObject();
result.getJsonArray("entities").forEach(e -> {
    JsonObject entity = e.asJsonObject();
    System.out.printf("Entity: %s%n", entity.getString("name"));
});
```

---

## See Also

- [TypeSpec Quickstart](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/contributor/typespec-quickstart.md)
- [Azure Identity Examples](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/identity-examples.md)
- [Configuration](https://github.com/Azure/azure-sdk-for-java/blob/main/docs/configuration.md)
