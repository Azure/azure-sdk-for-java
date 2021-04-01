// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * JavaDoc code snippets for {@link DynamicRequest}.
 */
public class DynamicRequestJavaDocCodeSnippets {

    /**
     * Sample to demonstrate how to create an instance of {@link DynamicRequest}.
     * @return An instance of {@link DynamicRequest}.
     */
    public DynamicRequest createInstance() {
        // BEGIN: com.azure.core.experimental.http.dynamicrequest.instantiation
        ObjectSerializer serializer = JsonSerializerProviders.createInstance(true);
        HttpPipeline pipeline = new HttpPipelineBuilder().build();
        DynamicRequest dynamicRequest = new DynamicRequest(serializer, pipeline);
        // END: com.azure.core.experimental.http.dynamicrequest.instantiation
        return dynamicRequest;
    }

    /**
     * Sample to demonstrate making a GET request.
     */
    public void getRequestSample() {
        DynamicRequest dynamicRequest = createInstance();
        // BEGIN: com.azure.core.experimental.http.dynamicrequest.getrequest
        DynamicResponse response = dynamicRequest
            .setUrl("https://petstore.example.com/pet/{petId}") // may already be set if request is created from a client
            .setPathParam("petId", "2343245")
            .send(); // makes the service call
        // END: com.azure.core.experimental.http.dynamicrequest.getrequest
    }

    /**
     * Sample to demonstrate making a POST request with JSON request body.
     */
    public void postRequestSampleWithJsonRequestBody() {
        // BEGIN: com.azure.core.experimental.http.dynamicrequest.createjsonrequest
        JsonArray photoUrls = Json.createArrayBuilder()
            .add("https://imgur.com/pet1")
            .add("https://imgur.com/pet2")
            .build();

        JsonArray tags = Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add("id", 0)
                .add("name", "Labrador")
                .build())
            .add(Json.createObjectBuilder()
                .add("id", 1)
                .add("name", "2021")
                .build())
            .build();

        JsonObject requestBody = Json.createObjectBuilder()
            .add("id", 0)
            .add("name", "foo")
            .add("status", "available")
            .add("category", Json.createObjectBuilder().add("id", 0).add("name", "dog"))
            .add("photoUrls", photoUrls)
            .add("tags", tags)
            .build();

        String requestBodyStr = requestBody.toString();
        // END: com.azure.core.experimental.http.dynamicrequest.createjsonrequest

        DynamicRequest dynamicRequest = createInstance();

        // BEGIN: com.azure.core.experimental.http.dynamicrequest.postrequest
        DynamicResponse response = dynamicRequest
            .setUrl("https://petstore.example.com/pet") // may already be set if request is created from a client
            .addHeader("Content-Type", "application/json")
            .setBody(requestBodyStr)
            .send(); // makes the service call
        // END: com.azure.core.experimental.http.dynamicrequest.postrequest
    }
}
