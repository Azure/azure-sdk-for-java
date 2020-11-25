# Introduction

Azure Digital Twins is a developer platform for next-generation IoT solutions that lets you create, run, and manage digital representations of your business environment, securely and efficiently in the cloud. With Azure Digital Twins, creating live operational state representations is quick and cost-effective, and digital representations stay current with real-time data from IoT and other data sources. If you are new to Azure Digital Twins and would like to learn more about the platform, please make sure you check out the Azure Digital Twins [official documentation page](https://docs.microsoft.com/azure/digital-twins/overview).

For an introduction on how to program against the Azure Digital Twins service, visit the [coding tutorial page](https://docs.microsoft.com/azure/digital-twins/tutorial-code) for an easy step-by-step guide. Visit [this tutorial](https://docs.microsoft.com/azure/digital-twins/tutorial-command-line-app) to learn how to interact with an Azure Digital Twin instance using a command-line client application. Finally, for a quick guide on how to build an end-to-end Azure Digital Twins solution that is driven by live data from your environment, make sure you check out [this helpful guide](https://docs.microsoft.com/azure/digital-twins/tutorial-end-to-end).

The guides mentioned above can help you get started with key elements of Azure Digital Twins, such as creating Azure Digital Twins instances, models, twin graphs, etc. Use this samples guide below to familiarize yourself with the various APIs that help you program against Azure Digital Twins.

# Digital Twins Samples

You can explore the digital twins APIs (using the client library) using the samples project.

The samples project demonstrates the following:

- Instantiate the client
- Create, get, and decommission models
- Create, query, and delete a digital twin
- Get and update components for a digital twin
- Create, get, and delete relationships between digital twins
- Create, get, and delete event routes for digital twin
- Publish telemetry messages to a digital twin and digital twin component

## sync vs async clients

Azure DigitalTwins SDK for java has two sets of APIs available for every operation, sync APIs and async APIs.

You can use `DigitalTwinsClientBuilder` to build either a sync client: `buildClient()` or an async client: `buildAsyncClient()`.

While using the sync client, the running thread will be blocked by the SDK for the duration of the HTTP request/response.
The async client is implemented using [Reactor](https://projectreactor.io/docs/core/release/reference/). The rest of this document assumes the reader has basic understanding of Reactor and how to interact with async API response types.

## Creating the digital twins client

To create a new digital twins client, you need the endpoint to an Azure Digital Twin instance and credentials.
In the sample below, you can set `AdtEndpoint`, `TenantId`, `ClientId`, and `ClientSecret` as command-line arguments.
The client requires an instance of [TokenCredential](https://docs.microsoft.com/java/api/com.azure.core.credential.tokencredential?view=azure-java-stable).
In this samples, we illustrate how to use one derived class: ClientSecretCredential.

> Note: In order to access the data plane for the Digital Twins service, the entity must be given permissions.
> To do this, use the Azure CLI command: `az dt rbac assign-role --assignee '<user-email | application-id>' --role owner -n '<your-digital-twins-instance>'`

### Building the sync client

```java
client = new DigitalTwinsClientBuilder()
    .credential(
        new ClientSecretCredentialBuilder()
            .tenantId(<your-tenantId>)
            .clientId(<your-clientId>)
            .clientSecret(<your-clientSecret>)
            .build()
    )
    .endpoint(<your-AdtEndpoint>)
    .buildClient();
```

### Building the async client

You can use the same client builder and build the async client:

```java
client = new DigitalTwinsClientBuilder()
    .credential(
        new ClientSecretCredentialBuilder()
            .tenantId(<your-tenantId>)
            .clientId(<your-clientId>)
            .clientSecret(<your-clientSecret>)
            .build()
    )
    .endpoint(<your-AdtEndpoint>)
    .buildAsyncClient();
```

Also, if you need to override pipeline behavior, such as provide your own HttpClient instance, you can do that via the other setters in the DigitalTwinsClientBuilder instance.

For example if you would like to use your own instance of `HttpClient`:

```java
client = new DigitalTwinsClientBuilder()
    .credential(
        new ClientSecretCredentialBuilder()
            .tenantId(<your-tenantId>)
            .clientId(<your-clientId>)
            .clientSecret(<your-clientSecret>)
            .build()
    )
    .endpoint(<your-AdtEndpoint>)
    .httpClient(<your-http-client>)
    .buildAsyncClient();
```

It provides an opportunity to override default behavior including:

- Specifying API version
- Overriding [HttpPipeline](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/http/HttpPipeline.java).
- Enabling [HttpLogOptions](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/http/policy/HttpLogOptions.java).
- Controlling [retry strategy](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/http/policy/RetryPolicy.java).

## Working with Response types

Both sync and async clients have maximal overloads for getting the Http response along with the return type. These APIs have a `withResponse` suffix.

For example you can get a model by calling:

```java
DigitalTwinsModelData model = syncClient.getModel(modelId);
```

This will only return the Model payload. The API's response will not contain any REST information.
To get information about the REST call you can call the maximal overload and have access to both the response body (ModelData) and the HTTP REST information.

```java
Response<DigitalTwinsModelData> modelResponse = syncClient.getModelWithResponse(modelId, context);

System.out.println(modelResponse.getStatuscode());

DigitalTwinsModelData modelObject = modelResponse.getValue();
```

## Create, list, decommission, and delete models

### Create models

Let's create models using the code below. You need to pass in `List<string>` containing list of json models.
Check out sample models [here](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/digitaltwins/azure-digitaltwins-core/src/samples/resources/DTDL/Models).

Example of using sync client to create models.

```java
List<String> modelsList = Arrays.asList(newComponentModelPayload, newModelPayload);
List<DigitalTwinsModelData> modelList =  syncClient.createModels(modelsList);

for (DigitalTwinsModelData model : modelList) {
    ConsoleLogger.print("Created model: " + model.getId());
}
```

### List models

Using the sync client, `listModels`, all created models are returned as [`PagedIterable<DigitalTwinsModelData>`](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/http/rest/PagedIterable.java) while the async API will return a [`PagedFlux<ModelData>`](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/http/rest/PagedFlux.java).

Example of using the async client to list all models:

```java
final CountDownLatch listModelsLatch = new CountDownLatch(1);

asynClient.listModels()
    .doOnNext(modelData -> System.out.println(
        "Retrieved model: " +
        modelData.getId() +
        ", display name '" +
        modelData.getDisplayName().get("en") +
        "'," +
        " upload time '" +
        modelData.getUploadTime() +
        "' and decommissioned '" +
        modelData.isDecommissioned() + "'"))
    .doOnError(throwable -> System.out.println("List models error: " + throwable))
    .doOnTerminate(listModelsLatch::countDown)
    .subscribe();
```

Use `getModel` with model's unique identifier to get a specific model.

```java
asyncClient.getModel(modelId)
    .subscribe(
        model -> System.out.println("Retrieved model with Id: " + model.getModelId()),
        throwable -> System.out.println("Could not get model " + modelId + " due to " + throwable)
    )
```

### Decommission models

To decommission a model, pass in a model Id for the model you want to decommission.

```java
client.decommissionModel(modelId);
```

### Delete models

To delete a model, pass in a model Id for the model you want to delete.

```java
client.deleteModel(modelId);
```

## Create and delete digital twins

### Create digital twins

For Creating Twin you will need to provide Id of a digital Twin such as `myTwin` and the application/json digital twin based on the model created earlier. You can look at sample application/json [here](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/digitaltwins/azure-digitaltwins-core/src/samples/resources/DTDL/DigitalTwins).

One option is to use the provided class BasicDigitalTwin for serialization and deserialization.

```java
// Create digital twin with component payload using the BasicDigitalTwin serialization helper

BasicDigitalTwin basicTwin = new BasicDigitalTwin(basicDigitalTwinId)
    .setMetadata(
        new BasicDigitalTwinMetadata()
            .setModelId(modelId)
    )
    .addToContents("Prop1", "Value1")
    .addToContents("Prop2", 987)
    .addToContents(
        "Component1",
        new BasicDigitalTwinComponent()
            .addToContents("ComponentProp1", "Component value 1")
            .addToContents("ComponentProp2", 123));

BasicDigitalTwin basicTwinResponse = syncClient.createOrReplaceDigitalTwin(basicDigitalTwinId, basicTwin, BasicDigitalTwin.class);
```

Alternatively, you can create your own custom data types to serialize and deserialize your digital twins.
By specifying your properties and types directly, it requires less code or knowledge of the type for interaction.

You can also retrieve the application/json string payload from disk and pass it down to the client directly.

```java
String payload = <Load the file content into memory>;
String digitalTwinCreateResponse = syncClient.createOrReplaceDigitalTwin(twinId, payload, String.class);
```

### Get and deserialize a digital twin

You can get a digital twin in 2 separate formats

- In a String format by just calling:

```java
String stringDt = syncClient.getDigitalTwin(twinId, String.class);
```

- Choose what type you would like the twin to be deserialized as:

```java
BasicDigitalTwin basicDt = syncClient.getDigitalTwin(twinId, BasicDigitalTwin.class);
```

### Query digital twins

Query the Azure Digital Twins instance for digital twins using the [Azure Digital Twins Query Store language](https://review.docs.microsoft.com/azure/digital-twins-v2/concepts-query-language?branch=pr-en-us-114648). Query calls support paging. Here's an example of how to query for digital twins and how to iterate over the results.

```java
// This code snippet demonstrates the simplest way to iterate over the digital twin results, where paging
// happens under the covers.

// You can either get a String representation of your query response
PagedIterable<String> pageableResponse = syncClient.query("SELECT * FROM digitaltwins", String.class);

// Iterate over the twin instances in the pageable response.
foreach (String response in pageableResponse) {
    System.out.println(response);
}

// Or you can use the generic API to get a specific type back.
PagedIterable<BasicDigitalTwin> deserializedResponse = syncClient.query("SELECT * FROM digitaltwins", BasicDigitalTwin.class);

for(BasicDigitalTwin digitalTwin : deserializedResponse){
    System.out.println("Retrieved digital twin with Id: " + digitalTwin.getId());
}
```

### Delete digital twins

Delete a digital twin simply by providing Id of a digital twin as below.

```java
syncClient.deleteDigitalTwin(digitalTwinId);
```

## Get and update digital twin components

### Update digital twin components

To update a component or in other words to replace, remove and/or add a component property or subproperty within Digital Twin, you would need Id of a digital twin, component name and application/json-patch+json operations to be performed on the specified digital twin's component. Here is the sample code on how to do it.

```C# Snippet:DigitalTwinsSampleUpdateComponent
// Update Component1 by replacing the property ComponentProp1 value,
// using the JsonPatchDocument to build the update operation patch document.
JsonPatchDocument updateOp = new JsonPatchDocument()
    .appendReplace("/ComponentProp1", "Some new Value");

client.updateComponent(basicDigitalTwinId, "Component1", updateOp);
```

### Get digital twin components

Get a component by providing name of a component and Id of digital twin to which it belongs.

```java
String getComponentResponse = client.getComponent(digitalTwinId, "Component1", String.class);
```

## Create, get,  list and delete digital twin relationships

### Create digital twin relationships

`createRelationship` creates a relationship on a digital twin provided with Id of a digital twin, name of relationship such as "contains", Id of a relationship such as "FloorContainsRoom" and an application/json relationship to be created. Must contain property with key "$targetId" to specify the target of the relationship. Sample payloads for relationships can be found [here](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/digitaltwins/azure-digitaltwins-core/src/samples/resources/DTDL/Relationships/HospitalRelationships.json).

One option is to use the provided class BasicRelationship for serialization and deserialization.

```java
BasicRelationship buildingToFloorBasicRelationship = 
new BasicRelationship(
        "myRelationshipId",
        "mySourceDigitalTwinId",
        "myTargetDigitalTwinId",
        "contains")
    .addProperty("Prop1", "Prop1 value")
    .addProperty("Prop2", 6);

BasicRelationship createdRelationship = client.createOrReplaceRelationship(
    "mySourceDigitalTwinId",
    "myRelationshipId",
    buildingToFloorBasicRelationship,
    BasicRelationship.class);
```

### Get and deserialize a digital twin relationship

You can get a digital twin relationship and deserialize it into type of your choice. Here is an example of how to do this and also check the HTTP response.

```java
Response<BasicRelationship> getRelationshipResponse = client.getRelationshipWithResponse(
    buildingTwinId,
    buildingFloorRelationshipId,
    BasicRelationship.class,
    Context.NONE);

if (getRelationshipResponse.getStatusCode() == HttpURLConnection.HTTP_OK) {
    BasicRelationship retrievedRelationship = getRelationshipResponse.getValue();
    ConsoleLogger.printSuccess("Retrieved relationship: " + retrievedRelationship.getId() + " from twin: " + retrievedRelationship.getSourceId() + "\n\t" +
        "Prop1: " + retrievedRelationship.getProperties().get("Prop1") + "\n\t" +
        "Prop2: " + retrievedRelationship.getProperties().get("Prop2"));
}
```

### List digital twin relationships

`listRelationships` lists all the relationships of a digital twin. You can get digital twin relationships and deserialize them into `BasicRelationship`.

```java
PagedIterable<BasicRelationship> relationshipPages = client.listRelationships(buildingTwinId, BasicRelationship.class);

for (BasicRelationship relationship : relationshipPages) {
    System.out.println(
        "Retrieved relationship: " +
        relationship.getId() +
        " with source: " +
        relationship.getSourceId() +
        " and target: " +
        relationship.getTargetId());
}
```

`listIncomingRelationships` lists all incoming relationships of digital twin.

```java
PagedIterable<IncomingRelationship> incomingRelationships = client.listIncomingRelationships(floorTwinId);

for (IncomingRelationship incomingRelationship : incomingRelationships) {
    System.out.println(
        "Found an incoming relationship: " +
        incomingRelationship.getRelationshipId() +
        " from: " +
        incomingRelationship.getSourceId());
}
```

### Delete a digital twin relationship

To delete all outgoing relationships for a digital twin, simply iterate over the relationships and delete them iteratively.

```java
client.deleteRelationship(buildingTwinId, buildingFloorRelationshipId);
```

## Create, list, and delete event routes of digital twins

### Create event routes

To create an event route, provide an Id of an event route such as "sampleEventRoute" and event route data containing the endpoint and optional filter like the example shown below.

```java
String filter ="$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'";

DigitalTwinsEventRoute eventRoute = new DigitalTwinsEventRoute("myEndpointName").setFilter(filter);
client.createOrReplaceEventRoute("myEventRouteId", eventRoute);
```

For more information on the event route filter language, see the "how to manage routes" [filter events documentation](https://docs.microsoft.com/azure/digital-twins/how-to-manage-routes-apis-cli#filter-events).

### List event routes

List a specific event route given event route Id or all event routes setting options with `GetEventRouteAsync` and `GetEventRoutesAsync`.

```java
PagedIterable<DigitalTwinsEventRoute> listResponse =  client.listEventRoutes();

listResponse.forEach(
    eventRoute -> System.out.println("Retrieved event route with Id: " + eventRoute.getEventRouteId()));
```

### Delete event routes

Delete an event route given event route Id.

```java
client.deleteEventRoute(eventRouteId);
```

### Publish telemetry messages for a digital twin

To publish a telemetry message for a digital twin, you need to provide the digital twin Id, along with the payload on which telemetry that needs the update.

```java
// construct your json telemetry payload by hand.
client.publishTelemetry(digitalTwinId, "{\"Telemetry1\": 5}");

// Or construct it using a Dictionary and pass it to the API
Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
telemetryPayload.put("Telemetry1", 5);

client.publishTelemetry(digitalTwinId, telemetryPayload);
```

You can also publish a telemetry message for a specific component in a digital twin. In addition to the digital twin Id and payload, you need to specify the target component Id.

```java
Dictionary<String, Integer> telemetryPayload = new Hashtable<>();
telemetryPayload.put("ComponentTelemetry1", 9);

// You can use PublishTelemetryRequestOptions to set your message Id and timestamp
// By default, the message Id is a random guid and timestamp is OffsetDateTime.now(ZoneOffset.UTC);
PublishTelemetryRequestOptions componentTelemetryRequestOptions = new PublishTelemetryRequestOptions();

Response<Void> publishComponentTelemetryResponse = client.publishComponentTelemetryWithResponse(
    digitalTwinId,
    "Component1",
    telemetryPayload,
    componentTelemetryRequestOptions,
    Context.NONE);
```
