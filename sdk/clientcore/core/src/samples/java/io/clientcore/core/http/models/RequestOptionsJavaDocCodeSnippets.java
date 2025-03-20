// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.json.models.JsonArray;
import io.clientcore.core.serialization.json.models.JsonObject;
import io.clientcore.core.utils.ProgressReporter;

/**
 * JavaDoc code snippets for {@link RequestOptions}.
 */
public class RequestOptionsJavaDocCodeSnippets {

    /**
     * Sample to demonstrate how to create an instance of {@link RequestOptions}.
     * @return An instance of {@link RequestOptions}.
     */
    public RequestOptions createInstance() {
        // BEGIN: io.clientcore.core.http.rest.requestcontext.instantiation
        RequestOptions options = new RequestOptions()
            .addRequestCallback(r -> r.getHeaders().add(HttpHeaderName.fromString("x-ms-pet-version"), "2021-06-01"));
        // END: io.clientcore.core.http.rest.requestcontext.instantiation
        return options;
    }

    /**
     * Sample to demonstrate setting the JSON request body in a {@link RequestOptions}.
     * @return An instance of {@link RequestOptions}.
     */
    public RequestOptions setJsonRequestBodyInRequestContext() {
        // BEGIN: io.clientcore.core.http.rest.requestcontext.createjsonrequest
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
        // END: io.clientcore.core.http.rest.requestcontext.createjsonrequest

        // BEGIN: io.clientcore.core.http.rest.requestcontext.postrequest
        RequestOptions options = new RequestOptions()
            .addRequestCallback(request -> request
                // may already be set if request is created from a client
                .setUri("https://petstore.example.com/pet")
                .setMethod(HttpMethod.POST)
                .setBody(requestBodyData)
                .getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"));
        // END: io.clientcore.core.http.rest.requestcontext.postrequest
        return options;
    }


    /**
     * Code snippet for {@link RequestOptions#putData(String, Object)}
     */
    public void putDataContext() {
        // BEGIN: io.clientcore.core.http.rest.requestcontext.putData

        RequestOptions options = new RequestOptions()
            .putData("stringKey", "value")
            .putData("complexObject", ProgressReporter.withProgressListener(value -> System.out.printf("Got %s bytes", value)));

        // END: io.clientcore.core.http.rest.requestcontext.putData

        // BEGIN: io.clientcore.core.http.rest.requestcontext.getData

        // Get the string value
        Object stringKeyValue = options.getData("stringKey");
        String stringValue = stringKeyValue instanceof String ? (String) stringKeyValue : null;
        System.out.printf("Key1 value: %s%n", stringValue);

        // Get the complex object
        Object complexObjectValue = options.getData("complexObject");
        ProgressReporter progressReporter = complexObjectValue instanceof ProgressReporter
            ? (ProgressReporter) complexObjectValue
            : null;
        if (progressReporter != null) {
            // Use the progress reporter
            progressReporter.reportProgress(42);
        }

        // END: io.clientcore.core.http.rest.requestcontext.getData
    }
}
