// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.util.BinaryData;
import com.azure.json.models.JsonArray;
import com.azure.json.models.JsonNumber;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;

/**
 * JavaDoc code snippets for {@link RequestOptions}.
 */
public class RequestOptionsJavaDocCodeSnippets {

    /**
     * Sample to demonstrate how to create an instance of {@link RequestOptions}.
     * @return An instance of {@link RequestOptions}.
     */
    @SuppressWarnings("deprecation")
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
        // END: com.azure.core.http.rest.requestoptions.createjsonrequest

        // BEGIN: com.azure.core.http.rest.requestoptions.postrequest
        RequestOptions options = new RequestOptions()
            .addRequestCallback(request -> request
                // may already be set if request is created from a client
                .setUrl("https://petstore.example.com/pet")
                .setHttpMethod(HttpMethod.POST)
                .setBody(requestBodyData)
                .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json"));
        // END: com.azure.core.http.rest.requestoptions.postrequest
        return options;
    }
}
