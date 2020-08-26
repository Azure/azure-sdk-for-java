// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.digitaltwins.core.implementation.models.DigitalTwinsAddHeaders;
import com.azure.digitaltwins.core.implementation.models.DigitalTwinsAddResponse;
import com.azure.digitaltwins.core.implementation.serialization.BasicDigitalTwin;
import com.azure.digitaltwins.core.implementation.serialization.DigitalTwinMetadata;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AsyncSample
{
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Random random = new Random();

    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        String tenantId = System.getenv("TENANT_ID");
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");
        String endpoint = System.getenv("DIGITAL_TWINS_ENDPOINT");

        String modelId = "dtmi:samples:Building;1";

        TokenCredential tokenCredential = new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();

        DigitalTwinsAsyncClient client = new DigitalTwinsClientBuilder()
            .tokenCredential(tokenCredential)
            .endpoint(endpoint)
            .httpLogOptions(
                new HttpLogOptions()
                    .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();

        // Create the source twin
        final Semaphore createTwinsSemaphore = new Semaphore(0);

        // Request is json string
        DigitalTwinMetadata metadata = new DigitalTwinMetadata().setModelId(modelId);

        // Response is Response<String>
        String dtId_Response_String = "dt_Response_String_" + random.nextInt();
        BasicDigitalTwin basicDigitalTwin_Response_String = new BasicDigitalTwin()
            .setId(dtId_Response_String)
            .setMetadata(metadata)
            .setCustomProperties("AverageTemperature", random.nextInt(50))
            .setCustomProperties("TemperatureUnit", "Celsius");
        String dt_Response_String = mapper.writeValueAsString(basicDigitalTwin_Response_String);

        Mono<Response<String>> sourceTwinWithResponseString = client.createDigitalTwinWithResponseString(dtId_Response_String, dt_Response_String);
        sourceTwinWithResponseString.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_Response_String, result.getStatusCode(), result.getHeaders().get("etag").getValue()));
                try {
                    // Convert to Jackson's tree model, which is useful to parse json string when you are not sure what the json string looks like
                    JsonNode jsonNode = mapper.readTree(result.getValue());

                    // Verify that the returned json string conforms to digital twin format -> has a root element $metadata and child-element $model in it.
                    if (!jsonNode.path("$metadata").path("$model").isNull()) {

                        // Verify if the returned json string is CustomDigitalTwin
                        if (jsonNode.path("$metadata").path("$model").textValue().equals(modelId)) {
                            // Parse it as CustomDigitalTwin
                            CustomDigitalTwin twin = mapper.treeToValue(jsonNode, CustomDigitalTwin.class);
                            System.out.println(
                                String.format("%s: Deserialized CustomDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                                    dtId_Response_String, twin.getId(), twin.getEtag(), twin.getMetadata().getModelId(), twin.getAverageTemperature(), twin.getTemperatureUnit()));
                        } else {
                            // Parse it as BasicDigitalTwin
                            BasicDigitalTwin twin = mapper.treeToValue(jsonNode, BasicDigitalTwin.class);
                            System.out.println(
                                String.format("%s: Deserialized BasicDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tCustomProperties=%s \n",
                                    dtId_Response_String, twin.getId(), twin.getTwinETag(), twin.getMetadata().getModelId(), Arrays.toString(twin.getCustomProperties().entrySet().toArray())));
                        }
                    }
                } catch (JsonProcessingException e) {
                    System.err.println("Reading response into DigitalTwin failed: ");
                    e.printStackTrace();
                }
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_Response_String + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Response is Response<String> -> ResponseBase<DigitalTwinsAddHeaders, String>
        String dtId_ResponseBase_String = "dt_ResponseBase_String_" + random.nextInt();
        BasicDigitalTwin basicDigitalTwin_ResponseBase_String = new BasicDigitalTwin()
            .setId(dtId_ResponseBase_String)
            .setMetadata(metadata)
            .setCustomProperties("AverageTemperature", random.nextInt(50))
            .setCustomProperties("TemperatureUnit", "Celsius");
        String dt_ResponseBase_String = mapper.writeValueAsString(basicDigitalTwin_ResponseBase_String);

        Mono<ResponseBase<DigitalTwinsAddHeaders, String>> sourceTwinWithResponseBaseString = client.createDigitalTwinWithResponseBaseString(dtId_ResponseBase_String, dt_ResponseBase_String);
        sourceTwinWithResponseBaseString.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_ResponseBase_String, result.getStatusCode(), result.getDeserializedHeaders().getETag()));
                try {
                    String jsonResponse = result.getValue();

                    // Deserialize the String output to a BasicDigitalTwin so that the metadata fields are easily accessible.
                    BasicDigitalTwin twin = mapper.readValue(jsonResponse, BasicDigitalTwin.class);

                    // Check if the returned digital twin follows CustomDigitalTwin's model definition.
                    if (twin.getMetadata().getModelId().equals(modelId)) {
                        // Parse it as CustomDigitalTwin
                        CustomDigitalTwin customDigitalTwin = mapper.readValue(jsonResponse, CustomDigitalTwin.class);
                        System.out.println(
                            String.format("%s: Deserialized CustomDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                                dtId_ResponseBase_String, customDigitalTwin.getId(), customDigitalTwin.getEtag(), customDigitalTwin.getMetadata().getModelId(), customDigitalTwin.getAverageTemperature(), customDigitalTwin.getTemperatureUnit()));
                    } else {
                    // Parse it as BasicDigitalTwin
                    System.out.println(
                        String.format("%s: Deserialized BasicDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tCustomProperties=%s \n",
                            dtId_ResponseBase_String, twin.getId(), twin.getTwinETag(), twin.getMetadata().getModelId(), Arrays.toString(twin.getCustomProperties().entrySet().toArray())));
                    }
                } catch (JsonProcessingException e) {
                    System.err.println("Reading response into DigitalTwin failed: ");
                    e.printStackTrace();
                }
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_ResponseBase_String + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Response is Response<String> -> DigitalTwinsAddResponse (json string)
        String dtId_DigitalTwinsAddResponse_String = "dt_DigitalTwinsAddResponse_String_" + random.nextInt();
        BasicDigitalTwin basicDigitalTwin_DigitalTwinsAddResponse_String = new BasicDigitalTwin()
            .setId(dtId_DigitalTwinsAddResponse_String)
            .setMetadata(metadata)
            .setCustomProperties("AverageTemperature", random.nextInt(50))
            .setCustomProperties("TemperatureUnit", "Celsius");
        String dt_DigitalTwinsAddResponse_String = mapper.writeValueAsString(basicDigitalTwin_DigitalTwinsAddResponse_String);

        Mono<DigitalTwinsAddResponse> sourceTwinWithDigitalTwinAddResponseString = client.createDigitalTwinWithDigitalTwinAddResponseString(dtId_DigitalTwinsAddResponse_String, dt_DigitalTwinsAddResponse_String);
        sourceTwinWithDigitalTwinAddResponseString.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_DigitalTwinsAddResponse_String, result.getStatusCode(), result.getDeserializedHeaders().getETag()));
                try {
                    // Convert to Jackson's tree model, which is useful to parse json string when you are not sure what the json string looks like
                    JsonNode jsonNode = mapper.readTree(result.getValue().toString());

                    // Verify that the returned json string conforms to digital twin format -> has a root element $metadata and child-element $model in it.
                    if (!jsonNode.path("$metadata").path("$model").isNull()) {

                        // Verify if the returned json string is CustomDigitalTwin
                        if (jsonNode.path("$metadata").path("$model").textValue().equals(modelId)) {
                            // Parse it as CustomDigitalTwin
                            CustomDigitalTwin twin = mapper.treeToValue(jsonNode, CustomDigitalTwin.class);
                            System.out.println(
                                String.format("%s: Deserialized CustomDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                                    dtId_DigitalTwinsAddResponse_String, twin.getId(), twin.getEtag(), twin.getMetadata().getModelId(), twin.getAverageTemperature(), twin.getTemperatureUnit()));
                        } else {
                            // Parse it as BasicDigitalTwin
                            BasicDigitalTwin twin = mapper.treeToValue(jsonNode, BasicDigitalTwin.class);
                            System.out.println(
                                String.format("%s: Deserialized BasicDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tCustomProperties=%s \n",
                                    dtId_DigitalTwinsAddResponse_String, twin.getId(), twin.getTwinETag(), twin.getMetadata().getModelId(), Arrays.toString(twin.getCustomProperties().entrySet().toArray())));
                        }
                    }
                } catch (JsonProcessingException e) {
                    System.err.println("Reading response into DigitalTwin failed: ");
                    e.printStackTrace();
                }
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_DigitalTwinsAddResponse_String + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Request is Object.
        String dtId_WithResponse_Object = "dt_WithResponse_Object_" + random.nextInt();
        CustomDigitalTwin customDigitalTwin_Object = new CustomDigitalTwin()
            .setId(dtId_WithResponse_Object)
            .setMetadata((CustomDigitalTwinMetadata) new CustomDigitalTwinMetadata().setModelId(modelId))
            .setAverageTemperature(random.nextInt(50))
            .setTemperatureUnit("Celsius");

        // Response is Response<Object>
        Mono<DigitalTwinsAddResponse> sourceTwinWithResponseObject = client.createDigitalTwinWithDigitalTwinsAddResponseObject(dtId_WithResponse_Object, customDigitalTwin_Object);
        sourceTwinWithResponseObject.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_WithResponse_Object, result.getStatusCode(), result.getDeserializedHeaders().getETag()));
                try {
                    // Convert to Jackson's tree model, which is useful to parse json string when you are not sure what the json string looks like
                    JsonNode jsonNode = mapper.valueToTree(result.getValue());

                    // Since the return type is defined as Object, but is dynamically returned as a Map, you can also do the following: (JsonNode approach is cleaner, though)
                    String createdTwinModelId = ((LinkedHashMap)((LinkedHashMap)result.getValue()).get("$metadata")).get("$model").toString();

                    // Verify that the returned json string conforms to digital twin format -> has a root element $metadata and child-element $model in it.
                    if (!jsonNode.path("$metadata").path("$model").isNull()) {

                        // Verify if the returned json string is CustomDigitalTwin
                        if (jsonNode.path("$metadata").path("$model").textValue().equals(modelId)) {
                            // Parse it as CustomDigitalTwin
                            CustomDigitalTwin twin = mapper.treeToValue(jsonNode, CustomDigitalTwin.class);
                            System.out.println(
                                String.format("%s: Deserialized CustomDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                                    dtId_WithResponse_Object, twin.getId(), twin.getEtag(), twin.getMetadata().getModelId(), twin.getAverageTemperature(), twin.getTemperatureUnit()));
                        } else {
                            // Parse it as BasicDigitalTwin
                            BasicDigitalTwin twin = mapper.treeToValue(jsonNode, BasicDigitalTwin.class);
                            System.out.println(
                                String.format("%s: Deserialized BasicDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tCustomProperties=%s \n",
                                    dtId_WithResponse_Object, twin.getId(), twin.getTwinETag(), twin.getMetadata().getModelId(), Arrays.toString(twin.getCustomProperties().entrySet().toArray())));
                        }
                    }
                } catch (JsonProcessingException e) {
                    System.err.println("Reading response into DigitalTwin failed: ");
                    e.printStackTrace();
                }
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_WithResponse_Object + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Request is strongly typed object
        String dtId_WithResponse_BasicDigitalTwin = "dt_WithResponse_BasicDigitalTwin_" + random.nextInt();
        CustomDigitalTwin genericDigitalTwin1 = new CustomDigitalTwin()
            .setId(dtId_WithResponse_BasicDigitalTwin)
            .setMetadata((CustomDigitalTwinMetadata) new CustomDigitalTwinMetadata().setModelId(modelId))
            .setAverageTemperature(random.nextInt(50))
            .setTemperatureUnit("Celsius");

        // Response is strongly typed object Response<BasicDigitalTwin>
        Mono<Response<BasicDigitalTwin>> sourceTwinWithResponseBasicDigitalTwin = client.createDigitalTwinWithResponseBasicDigitalTwin(dtId_WithResponse_BasicDigitalTwin, genericDigitalTwin1);
        sourceTwinWithResponseBasicDigitalTwin.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_WithResponse_BasicDigitalTwin, result.getStatusCode(), result.getHeaders().get("etag")));
                BasicDigitalTwin twin = result.getValue();
                System.out.println(
                    String.format("%s: Deserialized BasicDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tCustomProperties=%s \n",
                        dtId_WithResponse_BasicDigitalTwin, twin.getId(), twin.getTwinETag(), twin.getMetadata().getModelId(), Arrays.toString(twin.getCustomProperties().entrySet().toArray())));
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_WithResponse_BasicDigitalTwin + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Request is strongly typed object
        String dtId_WithResponse_Generic = "dt_WithResponse_Generic_" + random.nextInt();
        CustomDigitalTwin genericDigitalTwin = new CustomDigitalTwin()
            .setId(dtId_WithResponse_BasicDigitalTwin)
            .setMetadata((CustomDigitalTwinMetadata) new CustomDigitalTwinMetadata().setModelId(modelId))
            .setAverageTemperature(random.nextInt(50))
            .setTemperatureUnit("Celsius");


        // Response is strongly typed object Response<T>
        Mono<Response<CustomDigitalTwin>> sourceTwinGenericWithResponse = client.createDigitalTwinWithResponseGeneric(dtId_WithResponse_Generic, genericDigitalTwin, CustomDigitalTwin.class);
        sourceTwinGenericWithResponse.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_WithResponse_Generic, result.getStatusCode(), result.getHeaders().get("etag")));
                CustomDigitalTwin twin = result.getValue();
                System.out.println(String.format("%s: Deserialized CustomDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                    dtId_WithResponse_Generic, twin.getId(), twin.getEtag(), twin.getMetadata().getModelId(), twin.getAverageTemperature(), twin.getTemperatureUnit()));
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_WithResponse_Generic + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        boolean created = createTwinsSemaphore.tryAcquire(6, 20, TimeUnit.SECONDS);
        System.out.println("Source twins created: " + created);

        String targetTwin1_Id = "targetTwin_1_" + random.nextInt();
        String targetTwin_1 = "{\"$metadata\": {\"$model\": \"dtmi:samples:HVAC;1\"}, \"Efficiency\": 10, \"TargetTemperature\": 10, \"TargetHumidity\": 10}";

        String targetTwin2_Id = "targetTwin_2_" + random.nextInt();
        String targetTwin_2 = "{\"$metadata\": {\"$model\": \"dtmi:samples:HVAC;1\"}, \"Efficiency\": 50, \"TargetTemperature\": 50, \"TargetHumidity\": 50}";

        client.createDigitalTwinWithResponseString(targetTwin1_Id, targetTwin_1).block();
        client.createDigitalTwinWithResponseString(targetTwin2_Id, targetTwin_2).block();

        String relationship1 = "{\"$relationshipName\": \"isEquippedWith\", \"$targetId\": \"" + targetTwin1_Id + "\"}";
        String relationship2 = "{\"$relationshipName\": \"isEquippedWith\", \"$targetId\": \"" + targetTwin2_Id + "\"}";

        client.createRelationshipWithResponse(dtId_Response_String, "rid_1", relationship1).block();
        client.createRelationshipWithResponse(dtId_Response_String, "rid_2", relationship2).block();

        final Semaphore listSemaphore = new Semaphore(0);
        PagedFlux<String> relationships = client.listRelationships(dtId_Response_String, "isEquippedWith");

        relationships.subscribe(
            item -> System.out.println("Relationship retrieved: " + item),
            throwable -> System.err.println("Error: " + throwable),
            listSemaphore::release
        );

        if (listSemaphore.tryAcquire(1, 5, TimeUnit.SECONDS)) {
            System.out.println("Successfully retrieved all relationships");
        } else {
            System.err.println("Could not retrieve relationships");
        }

    }
}
