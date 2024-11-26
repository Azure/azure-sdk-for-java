// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.json.models.JsonArray;
import io.clientcore.core.json.models.JsonObject;
import io.clientcore.core.util.binarydata.BinaryData;

/**
 * JavaDoc code snippets for {@link RequestOptions}.
 */
public class RequestOptionsJavaDocCodeSnippets {

    /**
     * Sample to demonstrate how to create an instance of {@link RequestOptions}.
     * @return An instance of {@link RequestOptions}.
     */
    public RequestOptions createInstance() {
        // BEGIN: io.clientcore.core.http.rest.requestoptions.instantiation
        RequestOptions options = new RequestOptions()
            .setBody(BinaryData.fromString("{\"name\":\"Fluffy\"}"))
            .addHeader(new HttpHeader(HttpHeaderName.fromString("x-ms-pet-version"), "2021-06-01"));
        // END: io.clientcore.core.http.rest.requestoptions.instantiation
        return options;
    }

    /**
     * Sample to demonstrate setting the JSON request body in a {@link RequestOptions}.
     * @return An instance of {@link RequestOptions}.
     */
    public RequestOptions setJsonRequestBodyInRequestOptions() {
        // BEGIN: io.clientcore.core.http.rest.requestoptions.createjsonrequest
        JsonArray photoUris = new JsonArray()
            .addElement("https://imgur.com/pet1")
            .addElement("https://imgur.com/pet2");

        JsonArray tags = new JsonArray()
            .addElement(new JsonObject()
                .setProperty("id", 0)
                .setProperty("name", "Labrador"))
            .addElement(new JsonObject()
                .setProperty("id", 1)
                .setProperty("name", "2021"));

        JsonObject requestBody = new JsonObject()
            .setProperty("id", 0)
            .setProperty("name", "foo")
            .setProperty("status", "available")
            .setProperty("category", new JsonObject().setProperty("id", 0).setProperty("name", "dog"))
            .setProperty("photoUris", photoUris)
            .setProperty("tags", tags);

        BinaryData requestBodyData = BinaryData.fromObject(requestBody);
        // END: io.clientcore.core.http.rest.requestoptions.createjsonrequest

        // BEGIN: io.clientcore.core.http.rest.requestoptions.postrequest
        RequestOptions options = new RequestOptions()
            .addRequestCallback(request -> request
                // may already be set if request is created from a client
                .setUri("https://petstore.example.com/pet")
                .setHttpMethod(HttpMethod.POST)
                .setBody(BinaryData.fromString(requestBodyData))
                .getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"));
        // END: io.clientcore.core.http.rest.requestoptions.postrequest
        return options;
    }
}
