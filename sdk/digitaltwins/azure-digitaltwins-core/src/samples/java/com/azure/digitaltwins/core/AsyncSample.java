// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.digitaltwins.core.implementation.serialization.BasicDigitalTwin;
import com.azure.digitaltwins.core.implementation.serialization.DigitalTwinMetadata;
import com.azure.digitaltwins.core.util.DigitalTwinsResponse;
import com.azure.digitaltwins.core.util.UpdateOperationUtility;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.Arrays;
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
        String dtId_String = "dt_String_" + random.nextInt();
        BasicDigitalTwin basicDigitalTwin = new BasicDigitalTwin()
            .setId(dtId_String)
            .setMetadata(metadata)
            .setCustomProperties("AverageTemperature", random.nextInt(50))
            .setCustomProperties("TemperatureUnit", "Celsius");
        String dt_String = mapper.writeValueAsString(basicDigitalTwin);

        Mono<DigitalTwinsResponse<String>> sourceTwinWithResponseString = client.createDigitalTwinWithResponse(dtId_String, dt_String);
        sourceTwinWithResponseString.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_String, result.getStatusCode(), result.getDeserializedHeaders().getETag()));
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
                                dtId_String, customDigitalTwin.getId(), customDigitalTwin.getEtag(), customDigitalTwin.getMetadata().getModelId(), customDigitalTwin.getAverageTemperature(), customDigitalTwin.getTemperatureUnit()));
                    } else {
                        // Parse it as BasicDigitalTwin
                        System.out.println(
                            String.format("%s: Deserialized BasicDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tCustomProperties=%s \n",
                                dtId_String, twin.getId(), twin.getTwinETag(), twin.getMetadata().getModelId(), Arrays.toString(twin.getCustomProperties().entrySet().toArray())));
                    }
                } catch (JsonProcessingException e) {
                    System.err.println("Reading response into DigitalTwin failed: ");
                    e.printStackTrace();
                }
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_String + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Request is strongly typed object
        String dtId_Generic = "dt_Generic_" + random.nextInt();
        CustomDigitalTwin customDigitalTwin = new CustomDigitalTwin()
            .setId(dtId_Generic)
            .setMetadata((CustomDigitalTwinMetadata) new CustomDigitalTwinMetadata().setModelId(modelId))
            .setAverageTemperature(random.nextInt(50))
            .setTemperatureUnit("Celsius");


        // Response is strongly typed object Response<T>
        Mono<DigitalTwinsResponse<CustomDigitalTwin>> sourceTwinWithResponseGeneric = client.createDigitalTwinWithResponse(dtId_Generic, customDigitalTwin, CustomDigitalTwin.class);
        sourceTwinWithResponseGeneric.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_Generic, result.getStatusCode(), result.getHeaders().get("etag")));
                CustomDigitalTwin twin = result.getValue();
                System.out.println(String.format("%s: Deserialized CustomDigitalTwin, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                    dtId_Generic, twin.getId(), twin.getEtag(), twin.getMetadata().getModelId(), twin.getAverageTemperature(), twin.getTemperatureUnit()));
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_Generic + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        boolean created = createTwinsSemaphore.tryAcquire(2, 20, TimeUnit.SECONDS);
        System.out.println("Source twins created: " + created);

        String targetTwin1_Id = "targetTwin_1_" + random.nextInt();
        String targetTwin_1 = "{\"$metadata\": {\"$model\": \"dtmi:samples:HVAC;1\"}, \"Efficiency\": 10, \"TargetTemperature\": 10, \"TargetHumidity\": 10}";

        String targetTwin2_Id = "targetTwin_2_" + random.nextInt();
        String targetTwin_2 = "{\"$metadata\": {\"$model\": \"dtmi:samples:HVAC;1\"}, \"Efficiency\": 50, \"TargetTemperature\": 50, \"TargetHumidity\": 50}";

        String targetTwin3_Id = "targetTwin_3_" + random.nextInt();
        String targetTwin_3 = "{\"$metadata\": {\"$model\": \"dtmi:samples:Floor;1\"}, \"AverageTemperature\": 100}";

        client.createDigitalTwinWithResponse(targetTwin1_Id, targetTwin_1).block();
        client.createDigitalTwinWithResponse(targetTwin2_Id, targetTwin_2).block();
        client.createDigitalTwinWithResponse(targetTwin3_Id, targetTwin_3).block();

        String r_id_1 = "r_id_1_" + random.nextInt();
        String r_id_2 = "r_id_2_" + random.nextInt();
        String r_id_3 = "r_id_3_" + random.nextInt();
        String relationship1 = "{\"$relationshipName\": \"isEquippedWith\", \"$targetId\": \"" + targetTwin1_Id + "\"}";
        String relationship2 = "{\"$relationshipName\": \"isEquippedWith\", \"$targetId\": \"" + targetTwin2_Id + "\"}";
        String relationship3 = "{\"$relationshipName\": \"has\", \"$targetId\": \"" + targetTwin3_Id + "\"}";

        client.createRelationshipWithResponse(dtId_String, r_id_1, relationship1).block();
        client.createRelationshipWithResponse(dtId_String, r_id_2, relationship2).block();
        client.createRelationshipWithResponse(dtId_String, r_id_3, relationship3).block();

        UpdateOperationUtility utility = new UpdateOperationUtility().appendAddOperation("/isAccessRestricted", false);
        client.updateRelationship(dtId_String, r_id_3, utility.getUpdateOperations()).block();

        String createdRelationship2 = client.getRelationship(dtId_String, r_id_2).block();
        JsonNode jsonNode = mapper.readTree(createdRelationship2);
        String etag = jsonNode.path("$etag").textValue();

        client.deleteRelationship(dtId_String, r_id_1).block();
        client.deleteRelationshipWithResponse(dtId_String, r_id_2, new RequestOptions().setIfMatch(etag)).block();

    }
}
