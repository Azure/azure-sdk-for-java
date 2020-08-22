// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.digitaltwins.core.models.DigitalTwinsAddHeaders;
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
import java.util.function.Consumer;

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
                    .setLogLevel(HttpLogDetailLevel.NONE))
            .buildAsyncClient();

        // Create the source twin
        final Semaphore createTwinsSemaphore = new Semaphore(0);

        // Request is json string
        String dtId_String = "dt_String_" + random.nextInt();
        String dtId_WithResponse_ResponseBase_String = "dt_WithResponse_ResponseBase_String_" + random.nextInt();
        String dtId_WithResponse_DigitalTwinsAddResponse_String = "dt_WithResponse_DigitalTwinsAddResponse_String_" + random.nextInt();
        DigitalTwinMetadata metadata = new DigitalTwinMetadata().setModelId(modelId);
        BasicDigitalTwin basicDigitalTwin = new BasicDigitalTwin()
            .setId(dtId_String)
            .setMetadata(metadata)
            .setCustomProperties("AverageTemperature", random.nextInt(50))
            .setCustomProperties("TemperatureUnit", "Celsius");
        String dt_String = mapper.writeValueAsString(basicDigitalTwin);

        // Response is String
        Mono<String> sourceTwinString = client.createDigitalTwinString(dtId_String, dt_String);
        sourceTwinString.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin", dtId_String));
                try {
                    BasicDigitalTwin twin = mapper.readValue(result, BasicDigitalTwin.class);
                    System.out.println(
                        String.format("%s: Deserialized, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                            dtId_String, twin.getId(), twin.getEtag(), twin.getMetadata().getModelId(), (Integer)twin.getCustomProperties().get("AverageTemperature"), twin.getCustomProperties().get("TemperatureUnit")));
                } catch (JsonProcessingException e) {
                    System.err.println("Reading response into BasicDigitalTwin failed: ");
                    e.printStackTrace();
                }
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_String + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Response is Response<String> -> ResponseBase<DigitalTwinsAddHeaders, String>
        Mono<ResponseBase<DigitalTwinsAddHeaders, String>> sourceTwinWithResponseResponseBaseString = client.createDigitalTwinWithResponseResponseBaseString(dtId_WithResponse_ResponseBase_String, dt_String);
        sourceTwinWithResponseResponseBaseString.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_WithResponse_ResponseBase_String, result.getStatusCode(), result.getDeserializedHeaders().getETag()));
                try {
                    BasicDigitalTwin twin = mapper.readValue(result.getValue(), BasicDigitalTwin.class);
                    System.out.println(String.format("%s: Deserialized, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                        dtId_WithResponse_ResponseBase_String, twin.getId(), twin.getEtag(), twin.getMetadata().getModelId(), (Integer)twin.getCustomProperties().get("AverageTemperature"), twin.getCustomProperties().get("TemperatureUnit")));
                } catch (JsonProcessingException e) {
                    System.err.println("Reading response into BasicDigitalTwin failed: ");
                    e.printStackTrace();
                }
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_WithResponse_ResponseBase_String + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Response is Response<String> -> DigitalTwinsAddResponse (json string)
        Mono<DigitalTwinsAddResponse> sourceTwinWithResponseDigitalTwinAddResponseString = client.createDigitalTwinWithResponseDigitalTwinAddResponseString(dtId_WithResponse_DigitalTwinsAddResponse_String, dt_String);
        sourceTwinWithResponseDigitalTwinAddResponseString.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_WithResponse_DigitalTwinsAddResponse_String, result.getStatusCode(), result.getDeserializedHeaders().getETag()));
                try {
                    BasicDigitalTwin twin = mapper.readValue(result.getValue().toString(), BasicDigitalTwin.class);
                    System.out.println(String.format("%s: Deserialized, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                        dtId_WithResponse_DigitalTwinsAddResponse_String, twin.getId(), twin.getEtag(), twin.getMetadata().getModelId(), (Integer)twin.getCustomProperties().get("AverageTemperature"), twin.getCustomProperties().get("TemperatureUnit")));
                } catch (JsonProcessingException e) {
                    System.err.println("Reading response into BasicDigitalTwin failed: ");
                    e.printStackTrace();
                }
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_WithResponse_DigitalTwinsAddResponse_String + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Request is Object - strongly typed is allowed. - input can be strongly typed, and output is Object.
        String dtId_Object = "dt_Object_" + random.nextInt();
        String dtId_WithResponse_Object = "dt_WithResponse_Object_" + random.nextInt();
        CustomDigitalTwin customDigitalTwin = new CustomDigitalTwin()
            .id(dtId_Object)
            .metadata((CustomDigitalTwinMetadata) new CustomDigitalTwinMetadata().modelId(modelId))
            .averageTemperature(random.nextInt(50))
            .temperatureUnit("Celsius");

        // Response is Object
        Mono<Object> sourceTwinObject = client.createDigitalTwinObject(dtId_Object, customDigitalTwin);
        sourceTwinObject.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin", dtId_Object));
                CustomDigitalTwin twin = mapper.convertValue(result, CustomDigitalTwin.class);
                System.out.println(
                    String.format("%s: Deserialized, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                        dtId_Object, twin.id(), twin.etag(), twin.metadata().modelId(), twin.averageTemperature(), twin.temperatureUnit()));
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_Object + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Response is Response<Object>
        Mono<DigitalTwinsAddResponse> sourceTwinWithResponseObject = client.createDigitalTwinWithResponseDigitalTwinsAddResponseObject(dtId_WithResponse_Object, customDigitalTwin);
        sourceTwinWithResponseObject.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_WithResponse_Object, result.getStatusCode(), result.getDeserializedHeaders().getETag()));
                CustomDigitalTwin twin = mapper.convertValue(result.getValue(), CustomDigitalTwin.class);
                System.out.println(String.format("%s: Deserialized, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                    dtId_WithResponse_Object, twin.id(), twin.etag(), twin.metadata().modelId(), twin.averageTemperature(), twin.temperatureUnit()));
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_WithResponse_Object + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Request is strongly typed object
        String dtId_Generic = "dt_Generic_" + random.nextInt();
        String dtId_WithResponse_Generic = "dt_WithResponse_Generic_" + random.nextInt();
        CustomDigitalTwin genericDigitalTwin = new CustomDigitalTwin()
            .id(dtId_Object)
            .metadata((CustomDigitalTwinMetadata) new CustomDigitalTwinMetadata().modelId(modelId))
            .averageTemperature(random.nextInt(50))
            .temperatureUnit("Celsius");

        // Response is strongly typed object <T>
        Mono<CustomDigitalTwin> sourceTwinGeneric = client.createDigitalTwinGeneric(dtId_Generic, genericDigitalTwin, CustomDigitalTwin.class);
        sourceTwinGeneric.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin", dtId_Generic));
                CustomDigitalTwin twin = mapper.convertValue(result, CustomDigitalTwin.class);
                System.out.println(
                    String.format("%s: Deserialized, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                        dtId_Generic, twin.id(), twin.etag(), twin.metadata().modelId(), twin.averageTemperature(), twin.temperatureUnit()));
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_Generic + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        // Response is strongly typed object ResponseBase<DigitalTwinsAddHeaders, T>
        Mono<ResponseBase<DigitalTwinsAddHeaders, CustomDigitalTwin>> sourceTwinGenericWithResponse = client.createDigitalTwinWithResponseGeneric(dtId_WithResponse_Generic, genericDigitalTwin, CustomDigitalTwin.class);
        sourceTwinGenericWithResponse.subscribe(
            result -> {
                System.out.println(String.format("%s: Created twin, Status = %d, Etag = %s",
                    dtId_WithResponse_Generic, result.getStatusCode(), result.getDeserializedHeaders().getETag()));
                CustomDigitalTwin twin = mapper.convertValue(result.getValue(), CustomDigitalTwin.class);
                System.out.println(String.format("%s: Deserialized, \n\tId=%s, \n\tEtag=%s, \n\tModelId=%s, \n\tAverageTemperature=%d, \n\tTemperatureUnit=%s \n",
                    dtId_WithResponse_Generic, twin.id(), twin.etag(), twin.metadata().modelId(), twin.averageTemperature(), twin.temperatureUnit()));
            },
            throwable -> System.err.println("Failed to create source twin on digital twin with Id " + dtId_WithResponse_Generic + " due to error message " + throwable.getMessage()),
            createTwinsSemaphore::release);

        boolean created = createTwinsSemaphore.tryAcquire(7, 20, TimeUnit.SECONDS);
        System.out.println("Source twins created: " + created);

    }
}
