# Digital Twin API Design Doc

## Async APIs
This library utilizes [Project Reactor](https://projectreactor.io/) to provide consumers with async APIs.

Note:
- [Mono](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html) is a reactive streams publisher which completes an async operation after emiting a single element, or an error.
- [Flux](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html) is a reactive streams publisher which completes an async operation after emiting 0 to N elements, or an error.

This document outlines the APIs for the Digital Twin SDK

## Azure.Core usage
Within this SDK, we will make use of several Azure.Core library classes:

Pagination:

[PagedIterable\<T>](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/http/rest/PagedIterable.java): For synchronous APIs - This class provides utility to iterate over PagedResponse using Stream and Iterable interfaces.

Code sample using `Stream` by page:
```java
// process the streamByPage
pagedIterableResponse.streamByPage().forEach(resp -> {
    System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
        resp.getRequest().getUrl(), resp.getStatusCode());
    resp.getItems().forEach(value -> {
        System.out.printf("Response value is %d %n", value);
    });
});
```

 Code sample using `Iterable` by page:
 ```java
// process the iterableByPage
pagedIterableResponse.iterableByPage().forEach(resp -> {
    System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
        resp.getRequest().getUrl(), resp.getStatusCode());
    resp.getItems().forEach(value -> {
        System.out.printf("Response value is %d %n", value);
    });
});
 ```

Code sample using `Iterable` by page and while loop:
```java
// iterate over each page
Iterator<PagedResponse<Integer>> ite = pagedIterableResponse.iterableByPage().iterator();
while (ite.hasNext()) {
    PagedResponse<Integer> resp = ite.next();
    System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
        resp.getRequest().getUrl(), resp.getStatusCode());
    resp.getItems().forEach(value -> {
        System.out.printf("Response value is %d %n", value);
    });
}
```

[PagedFlux\<T>](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/http/rest/PagedFlux.java): This type is a Flux that provides the ability to operate on paginated REST responses of type PagedResponse and individual items in such pages. When processing the response by page, each response will contain the items in the page as well as the REST response details like status code and headers.

To process one item at a time, simply subscribe to this flux as shown below:
```java
// Subscribe to process one item at a time
pagedFlux
    .log()
    .subscribe(item -> System.out.println("Processing item " + item),
        error -> System.err.println("Error occurred " + error),
        () -> System.out.println("Completed processing."));
```

To process one page at a time, use `byPage()` method as shown below:
```java
// Subscribe to process one page at a time from the beginning
pagedFlux
    .byPage()
    .log()
    .subscribe(page -> System.out.println("Processing page containing " + page.getItems()),
        error -> System.err.println("Error occurred " + error),
        () -> System.out.println("Completed processing."));
```

To process items one page at a time starting from any page associated with a continuation token, `byPage(String)` as shown below:
```java
// Subscribe to process one page at a time starting from a page associated with a continuation token
String continuationToken = getContinuationToken();
pagedFlux
    .byPage(continuationToken)
    .log()
    .doOnSubscribe(ignored -> System.out.println(
        "Subscribed to paged flux processing pages starting from: " + continuationToken))
    .subscribe(page -> System.out.println("Processing page containing " + page.getItems()),
        error -> System.err.println("Error occurred " + error),
        () -> System.out.println("Completed processing."));
```

## Constructors
<details><summary><b>Constructors</b></summary>

Azure Digital Twins Service SDK exposes two clients - DigitalTwinsClient (sync version) and DigitalTwinsAsyncClient (async version).
They are initialized using builder pattern, where the `TokenCredential` (specifying the mechanism for retrieving OAuth tokens) and `endpoint` (the URL endpoint to connect to) are mandatory.

Sample usage:
```java
DigitalTwinsClient client = new DigitalTwinsClientBuilder()
            .tokenCredential(tokenCredential)
            .endpoint(endpoint)
            .buildClient();
```
OR
```java
DigitalTwinsAsyncClient client = new DigitalTwinsClientBuilder()
            .tokenCredential(tokenCredential)
            .endpoint(endpoint)
            .buildAsyncClient();
```
</details>

## Digital Twins
<details><summary><b>Examples</b></summary>
Here is an example digital twin
	
```json
{
  "$id": "myTwinId",
  "$metadata": {
    "$model": "urn:examplecom:interfaces:interfaceName:1",
    "$kind": "DigitalTwin",
    "property1": {
      "desiredValue": 1,
      "desiredVersion": 1,
      "ackVersion": 1,
      "ackCode": 200,
      "ackDescription": "description",
      "lastUpdateTime": "2020-05-23T21:44:02Z"
    },
    "property2": {
      "desiredValue": {
        "subProperty1": "some value",
        "subProperty2": "some other value"
      },
      "desiredVersion": 1,
      "ackVersion": 1,
      "ackCode": 200,
      "ackDescription": "description",
      "lastUpdateTime": "2020-05-23T21:44:02Z"
    }
  },
  "property1": 1,
  "property2": {
    "subProperty1": "some value",
    "subProperty2": "some other value"
  },
  "component1": {
    "$metadata": {
      "$model": "urn:examplecom:interfaces:interfaceName:1",
      "componentProperty": {
        "desiredValue": "some value",
        "desiredVersion": 1,
        "ackVersion": 1,
        "ackCode": 200,
        "ackDescription": "description",
        "lastUpdateTime": "2020-05-23T21:44:02Z"
      }
    }
  }
}
```
</details>


<details><summary><b>APIs</b></summary>

```java
TODO:
```
</details>


## Relationships
<details><summary><b>Terminology</b></summary>

Relationships: A named set of outgoing edges/relationships part of a Digital Twin persisted state.

Each Relationship: (aka a "Relationship Edge") an individual edge in the Digital Twin relationship graph, ie. a tuple containing:
    
	RelationshipId (Unique identifier of this edge within the context of the source Digital Twin)
	SourceId (Id of the source Digital Twin) 
	TargetId (Id of the target Digital Twin)
	RelationshipName (User defined string such as "contains", "hasDoorTo", "isNextTo")
	0 to many user defined properties (ie: "OccupancyLimit", "temperature")

Each relationship in a digital twin is identified by its RelationshipId. An RelationshipId must be unique within the scope of the source Digital Twin. The combination of SourceId and RelationshipId must be unique within the scope of the service.
</details>

<details><summary><b>Examples</b></summary>
A relationship that signifies that room1 has a door to room2, and that it is open, would look like
	
```json
{
    "$relationshipId": "Door1",
    "$sourceId": "Room1",
    "$targetId": "Room2",
    "$relationshipName": "hasDoorTo",
    "doorStatus": "open"
}
```
	
A relationship that signifies that Room 1 contains a thermostat would look like

```json
{
	"$relationshipId" : "ThermostatEdge1",
	"$sourceId" : "Room1",
	"$targetId" : "Thermostat1",
	"$relationshipName" : "contains",
	"installDate" : "2019-4-1",
	"replaceBatteryDate" : "2020-4-1"
}
```

When getting a list of relationships (operations like "get all relationships for a Digital Twin" or "get all relationships for a Digital Twin with a given relationshipName"), the client library will return a string in the below format:

```json
{
  "value": [
    {
      "$relationshipId": "Door1",
      "$sourceId": "Room1",
      "$targetId": "Room2",
      "$relationshipName": "hasDoorTo",
      "doorStatus": "open"
    },
    {
      "$relationshipId": "Door2",
      "$sourceId": "Room1",
      "$targetId": "Room3",
      "$relationshipName": "hasDoorTo",
      "doorStatus": "closed"
    }
  ],
  "nextLink": "url-to-next-page"
}
```

When creating a relationship, the edge string does not follow the above format. The rest endpoint to create a relationship edge contains the sourceId, relationshipName, and the relationShipId, so the payload only needs to specify the targetId and any application properties, as seen below:
```json
{
        "$targetId": "myTargetTwin",
        "myApplicationProperty1": 1,
        "myApplicationProperty2": "some value"
}
```

When updating a relationship edge, the patch string follows the below format
```json
{
    "patchDocument": 
    [
        {
            "op": "replace",
            "path": "property1",
            "value": 1
        },
        {
            "op": "add",
            "path": "property2/subProperty1",
            "value": 1
        },
        {
            "op": "remove",
            "path": "property3"
        }
    ]
}
```
</details>


<details><summary><b>Async APIs</b></summary>

These APIs are invoked via DigitalTwinsAsyncClient.

```java
    /**
     * Creates a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The application/json relationship to be created.
     * @return The application/json relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> createRelationship(String digitalTwinId, String relationshipId, String relationship) { }

    /**
     * Creates a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The application/json relationship to be created.
     * @return A REST response containing the application/json relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> createRelationshipWithResponse(String digitalTwinId, String relationshipId, String relationship) { }

    /**
     * Gets a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @return The application/json relationship corresponding to the provided relationshipId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getRelationship(String digitalTwinId, String relationshipId) { }

    /**
     * Gets a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @return A REST response containing the application/json relationship corresponding to the provided relationshipId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> getRelationshipWithResponse(String digitalTwinId, String relationshipId) { }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param relationshipUpdateOperations The application/json-patch+json operations to be performed on the specified digital twin's relationship.
     * @param options The optional settings for this request.
     * @return An empty response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateRelationship(String digitalTwinId, String relationshipId, String relationshipUpdateOperations, RequestOptions options) { }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param relationshipUpdateOperations The application/json-patch+json operations to be performed on the specified digital twin's relationship.
     * @param options The optional settings for this request.
     * @return A REST response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response> updateRelationshipWithResponse(String digitalTwinId, String relationshipId, String relationshipUpdateOperations, RequestOptions options) { }

    /**
     * Deletes a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @param options The optional settings for this request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRelationship(String digitalTwinId, String relationshipId, RequestOptions options) { }

    /**
     * Deletes a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @param options The optional settings for this request.
     * @return A REST response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, RequestOptions options) { }

    /**
     * Gets all the relationships on a digital twin by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @return A {@link PagedFlux} of application/json relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listRelationships(String digitalTwinId) { }

    /**
     * Gets all the relationships on a digital twin filtered by the relationship name, by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipName The name of a relationship to filter to.
     * @return A {@link PagedFlux} of application/json relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listRelationships(String digitalTwinId, String relationshipName) { }

    /**
     * Gets all the relationships referencing a digital twin as a target by iterating through a collection.
     *
     * @param digitalTwinId The Id of the target digital twin.
     * @return A {@link PagedFlux} of application/json relationships directed towards the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<IncomingRelationship> listIncomingRelationships(String digitalTwinId) { }

```
</details>

<details><summary><b>Sync APIs</b></summary>

These APIs are invoked via DigitalTwinsClient.

```java
    /**
     * Creates a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The application/json relationship to be created.
     * @return The application/json relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String createRelationship(String digitalTwinId, String relationshipId, String relationship) { }

    /**
     * Creates a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The application/json relationship to be created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the application/json relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> createRelationshipWithResponse(String digitalTwinId, String relationshipId, String relationship, Context context) { }

    /**
     * Gets a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @return The application/json relationship corresponding to the provided relationshipId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getRelationship(String digitalTwinId, String relationshipId) { }

    /**
     * Gets a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the application/json relationship corresponding to the provided relationshipId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<String> getRelationshipWithResponse(String digitalTwinId, String relationshipId, Context context) { }
    
    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param relationshipUpdateOperations The application/json-patch+json operations to be performed on the specified digital twin's relationship.
     * @param options The optional settings for this request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateRelationship(String digitalTwinId, String relationshipId, String relationshipUpdateOperations, RequestOptions options) { }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param relationshipUpdateOperations The application/json-patch+json operations to be performed on the specified digital twin's relationship.
     * @param options The optional settings for this request.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response updateRelationshipWithResponse(String digitalTwinId, String relationshipId, String relationshipUpdateOperations, RequestOptions options, Context context) { }

    /**
     * Deletes a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @param options The optional settings for this request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRelationship(String digitalTwinId, String relationshipId, RequestOptions options) { }

    /**
     * Deletes a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @param options The optional settings for this request.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, RequestOptions options, Context context) { }

    /**
     * Gets all the relationships on a digital twin by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @return A {@link PagedIterable} of application/json relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listRelationships(String digitalTwinId) { }

    /**
     * Gets all the relationships on a digital twin filtered by the relationship name, by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipName The name of a relationship to filter to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of application/json relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listRelationships(String digitalTwinId, String relationshipName, Context context) { }

    /**
     * Gets all the relationships referencing a digital twin as a target by iterating through a collection.
     *
     * @param digitalTwinId The Id of the target digital twin.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of application/json relationships directed towards the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IncomingRelationship> listIncomingRelationships(String digitalTwinId, Context context) { }

```
</details>

## Components
<details><summary><b>Terminology</b></summary>

Component: A named instance of an interface in the context of a capability model or another interface. 

</details>

<details><summary><b>Examples</b></summary>
Getting a component, SDK will return a string in following format:
	
```json
{
    "$metadata": {
        "$model": "urn:examplecom:interfaces:interfaceName:1",
        "property1": {
        "desiredValue": 1,
        "desiredVersion": 1,
        "ackVersion": 1,
        "ackCode": 200,
        "ackDescription": "description",
        "lastUpdateTime": "2020-05-23T21:44:02Z"
        },
        "property2": {
        "desiredValue": {
            "subProperty1": "some value",
            "subProperty2": "some other value"
        },
        "desiredVersion": 1,
        "ackVersion": 1,
        "ackCode": 200,
        "ackDescription": "description",
        "lastUpdateTime": "2020-05-23T21:44:02Z"
        }
    },
    "property1": 1,
    "property2": {
        "subProperty1": "some value",
        "subProperty2": "some other value"
    },
    "component1": {
        "$metadata": {
        "$model": "urn:examplecom:interfaces:interfaceName:1",
        "componentProperty": {
            "desiredValue": "some value",
            "desiredVersion": 1,
            "ackVersion": 1,
            "ackCode": 200,
            "ackDescription": "description",
            "lastUpdateTime": "2020-05-23T21:44:02Z"
        }
        }
    }
}
```

When updating a component, the patch string follows the below format
```json
    "jsonPatchDocument": [
        {
        "op": "add",
        "path": "property1",
        "value": 1
        },
        {
        "op": "remove",
        "path": "property2"
        },
        {
        "op": "replace",
        "path": "property3/subProperty1",
        "value": "new value"
        }
    ]
```

</details>


<details>
<summary><b>APIs</b></summary>


```java
TODO:
```

</details>

## Query
<details>
<summary><b>APIs</b></summary>


```java
TODO:
```
</details>

## Models
<details><summary><b>Examples</b></summary>
A model defines the properties, components, and relationships of a given digital twin. A sample model can be seen below

```json
{
  "@id": "urn:azureiotcom:SampleModel:1",
  "@type": "Interface",
  "contents": 
  [
    {
  	"@type": "Property",
  	"name": "name",
  	"displayName": "Sample instance name",
  	"schema": "string"
    },
    {
  	"@type": "Property",
  	"name": "temp",
  	"displayName": "Sample instance temperature",
  	"schema": "integer"
    },
    {
  	"@type": "Property",
  	"name": "comfortIndex",
  	"displayName": "Sample instance comfort index",
  	"schema": "integer"
    }
  ],
  "@context": "http://azureiot.com/v1/contexts/IoTModel.json"
}
```

When creating a model, the payload must be an array of models
```json
{
  "value": 
  [
    {
        "@id": "urn:azureiotcom:SampleModel:1",
        "@type": "Interface",
        "contents": [
          {
            "@type": "Property",
            "name": "name",
            "displayName": "Sample instance name",
            "schema": "string"
          },
          {
            "@type": "Property",
            "name": "temp",
            "displayName": "Sample instance temperature",
            "schema": "integer"
          },
          {
            "@type": "Property",
            "name": "comfortIndex",
            "displayName": "Sample instance comfort index",
            "schema": "integer"
          }
        ],
        "@context": "http://azureiot.com/v1/contexts/IoTModel.json"
      },
      {
        "@id": "urn:azureiotcom:OtherSampleModel:1",
        "@type": "Interface",
        "contents": [
          {
            "@type": "Property",
            "name": "count",
            "displayName": "Count",
            "schema": "integer"
          }
        ],
        "@context": "http://azureiot.com/v1/contexts/IoTModel.json"
      }
  ],
  "nextLink": "url-to-next-page"
}
```

When updating a model, the payload for a multi-operation json patch follows the below format
```json
[
      {
        "op": "replace",
        "path": "/decommissioned",
        "value": true
      },
      {
        "op": "replace",
        "path": "/decommissioned",
        "value": false
      }

]
```

</details>


<details><summary><b>APIs</b></summary>

```java
TODO:
```
</details>

## Event Routes
<details>
<summary><b>Terminology</b></summary>
Event Route - A route which directs notification and telemetry events to an endpoint. Endpoints are a destination outside of Azure Digital Twins such as an EventHub.

An event route has the following format:
- Id: The Id of the event route.
- Endpoint Id: The Id of the endpoint this event route is bound to.
- Filter: An expression which describes the events which are routed to the endpoint.


```json
{
  "id": "eventroute-001",
  "endpointId": "endpoint-001",
  "filter": "$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'"
}
```

</details>

<details>
<summary><b>APIs</b></summary>

```java
TODO:
```
</details>

## Telemetry
<details><summary><b>Details</b></summary>

The telemetry API requests for generation of a telemetry message for a logical twin. This method sends a fire and forget telemetry message by a digital twin. When this API succeeds, the caller is guaranteed that the telemetry message has been sent. There is no acknowledgement if or when the telemetry message is processed.

Telemtry for digital twin follows the pub/sub model. A digital twin publishes a telemetry message, which is then consumed by one or many destination endpoints (subscribers) defined under event routes.
These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.

* Note: Currently, the event route specifies broad filters, eg., `"filter": "$eventType = 'DigitalTwinTelemetryMessages'`. The ability to specify filters with increased granularity will be available sometime between public-preview and GA (TBD). There will also be support for subscriptions, which can be used to create subscriptions between two twins, and used for routing telemetry, notifications etc.

The Telemetry API takes in the telemetry payload in the HTTP request body. It also requires the following additional HTTP headers to be set:

```
"dt-id": (required) A unique message identifier (in the scope of the digital twin id) that is commonly used for de-duplicating messages.
"dt-timestamp": (optional) An RFC 3339 timestamp that identifies the time the telemetry was measured."
```

These can be set using the `TelemetryOptions` object, which will take in the required parameter in the constructor, and have public setters for optional parameters.

```java
public class TelemetryOptions
{
    
}
```
Currently there is no support for user-provided headers.

The telemetry payload is a JSON object, as defined in the digital twin's DTDL.

</details>
<details><summary><b>APIs</b></summary>

```java
TODO:
```
</details>
