// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.digitaltwins.core.models.DigitalTwinsAddResponse;
import com.azure.digitaltwins.core.serialization.BasicDigitalTwin;
import com.azure.digitaltwins.core.serialization.DigitalTwinMetadata;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AsyncSample
{
    private static final ObjectMapper mapper = new ObjectMapper();

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

        DigitalTwinMetadata metadata = new DigitalTwinMetadata().setModelId(modelId);

        // Request and response are both json string
        String dtId_String = "dt_String_" + new Random().nextInt();
        BasicDigitalTwin basicDigitalTwin = new BasicDigitalTwin()
            .setId(dtId_String)
            .setMetadata(metadata)
            .setCustomProperties("AverageTemperature", 68)
            .setCustomProperties("TemperatureUnit", "Celsius");

        String dt_String = mapper.writeValueAsString(basicDigitalTwin);
        Mono<String> sourceTwinString = client.createDigitalTwinAsString(dtId_String, dt_String);
        sourceTwinString.subscribe(
            result -> {
                System.out.println("Successfully created twin with Id: " + dtId_String);
                try {
                    BasicDigitalTwin twin = mapper.readValue(result, BasicDigitalTwin.class);
                    System.out.println(
                        String.format("Created twin: Id=%s, Etag=%s, ModelId=%s, AverageTemperature=%d, TemperatureUnit=%s",
                            twin.getId(), twin.getEtag(), twin.getMetadata().getModelId(), (Integer)twin.getCustomProperties().get("AverageTemperature"), twin.getCustomProperties().get("TemperatureUnit")));
                } catch (JsonProcessingException e) {
                    System.err.println("Reading response into BasicDigitalTwin failed: ");
                    e.printStackTrace();
                }
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_String + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Request and response are both objects - input is strongly typed, and output is Object.
        String dtId_Object = "dt_Object_" + new Random().nextInt();
        CustomDigitalTwin customDigitalTwin = new CustomDigitalTwin()
            .id(dtId_Object)
            .metadata((CustomDigitalTwinMetadata) new CustomDigitalTwinMetadata().modelId(modelId))
            .averageTemperature(48)
            .temperatureUnit("Celsius");

        Mono<DigitalTwinsAddResponse> sourceTwinObject = client.createDigitalTwinAsObject(dtId_Object, customDigitalTwin);
        sourceTwinObject.subscribe(
            result -> {
                System.out.println("Successfully created twin with Id: " + dtId_Object);
                CustomDigitalTwin twin = mapper.convertValue(result.getValue(), CustomDigitalTwin.class);
                System.out.println(
                    String.format("Created twin: Id=%s, Etag=%s, ModelId=%s, AverageTemperature=%d, TemperatureUnit=%s",
                        twin.id(), twin.etag(), twin.metadata().modelId(), twin.averageTemperature(), twin.temperatureUnit()));
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_String + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        boolean created = createTwinsSemaphore.tryAcquire(2, 20, TimeUnit.SECONDS);
        System.out.println("Source twins created: " + created);
    }
}
