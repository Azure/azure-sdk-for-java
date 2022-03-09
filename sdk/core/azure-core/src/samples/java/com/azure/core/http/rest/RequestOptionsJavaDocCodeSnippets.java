// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpMethod;
import com.azure.core.util.BinaryData;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * JavaDoc code snippets for {@link RequestOptions}.
 */
public class RequestOptionsJavaDocCodeSnippets {

    /**
     * Sample to demonstrate how to create an instance of {@link RequestOptions}.
     * @return An instance of {@link RequestOptions}.
     */
    public RequestOptions createInstance() {
        // BEGIN: com.azure.core.http.rest.requestoptions.instantiation
        RequestOptions options = new RequestOptions()
            .setBody(BinaryData.fromString("{\"name\":\"Fluffy\"}"))
            .addHeader("x-ms-pet-version", "2021-06-01");
        // END: com.azure.core.http.rest.requestoptions.instantiation
        return options;
    }

    /**
     * Sample to demonstrate setting the JSON request body in a {@link RequestOptions}.
     * @return An instance of {@link RequestOptions}.
     */
    public RequestOptions setJsonRequestBodyInRequestOptions() {
        // BEGIN: com.azure.core.http.rest.requestoptions.createjsonrequest
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
        // END: com.azure.core.http.rest.requestoptions.createjsonrequest

        // BEGIN: com.azure.core.http.rest.requestoptions.postrequest
        RequestOptions options = new RequestOptions()
            .addRequestCallback(request -> request
                // may already be set if request is created from a client
                .setUrl("https://petstore.example.com/pet")
                .setHttpMethod(HttpMethod.POST)
                .setBody(requestBodyStr)
                .setHeader("Content-Type", "application/json"));
        // END: com.azure.core.http.rest.requestoptions.postrequest
        return options;
    }
}
