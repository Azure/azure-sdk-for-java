// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.json.models.JsonArray;
import com.azure.json.models.JsonNumber;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;

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
            .setHttpMethod(HttpMethod.POST)
            .send(); // makes the service call
        // END: com.azure.core.experimental.http.dynamicrequest.getrequest
    }

    /**
     * Sample to demonstrate making a POST request with JSON request body.
     */
    public void postRequestSampleWithJsonRequestBody() {
        // BEGIN: com.azure.core.experimental.http.dynamicrequest.createjsonrequest
        JsonArray photoUrls = new JsonArray()
            .addElement(new JsonString("https://imgur.com/pet1"))
            .addElement(new JsonString("https://imgur.com/pet2"));

        JsonArray tags = new JsonArray()
            .addElement(new JsonObject()
                .setProperty("id", new JsonNumber(0))
                .setProperty("name", new JsonString("Labrador")))
            .addElement(new JsonObject()
                .setProperty("id", new JsonNumber(1))
                .setProperty("name", new JsonString("2021")));

        JsonObject requestBody = new JsonObject()
            .setProperty("id", new JsonNumber(0))
            .setProperty("name", new JsonString("foo"))
            .setProperty("status", new JsonString("available"))
            .setProperty("category", new JsonObject()
                .setProperty("id", new JsonNumber(0))
                .setProperty("name", new JsonString("dog")))
            .setProperty("photoUrls", photoUrls)
            .setProperty("tags", tags);

        BinaryData requestBodyData = BinaryData.fromObject(requestBody);
        // END: com.azure.core.experimental.http.dynamicrequest.createjsonrequest

        DynamicRequest dynamicRequest = createInstance();

        // BEGIN: com.azure.core.experimental.http.dynamicrequest.postrequest
        DynamicResponse response = dynamicRequest
            .setUrl("https://petstore.example.com/pet") // may already be set if request is created from a client
            .addHeader(HttpHeaderName.CONTENT_TYPE, "application/json")
            .setBody(requestBodyData)
            .send(); // makes the service call
        // END: com.azure.core.experimental.http.dynamicrequest.postrequest
    }
}
