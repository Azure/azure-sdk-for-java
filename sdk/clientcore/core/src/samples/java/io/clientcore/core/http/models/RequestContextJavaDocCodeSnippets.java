// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.json.models.JsonArray;
import io.clientcore.core.serialization.json.models.JsonObject;
import io.clientcore.core.utils.ProgressReporter;

/**
 * JavaDoc code snippets for {@link HttpRequestContext}.
 */
public class RequestContextJavaDocCodeSnippets {

    /**
     * Sample to demonstrate how to create an instance of {@link HttpRequestContext}.
     * @return An instance of {@link HttpRequestContext}.
     */
    public HttpRequestContext createInstance() {
        // BEGIN: io.clientcore.core.http.rest.httprequestcontext.instantiation
        HttpRequestContext options = new HttpRequestContext.Builder()
            .addHeader(HttpHeaderName.fromString("x-ms-pet-version"), "2021-06-01")
            .build();
        // END: io.clientcore.core.http.rest.httprequestcontext.instantiation
        return options;
    }

    /**
     * Sample to demonstrate setting the JSON request body in a {@link HttpRequestContext}.
     * @return An instance of {@link HttpRequestContext}.
     */
    public HttpRequestContext setJsonRequestBodyInRequestContext() {
        // BEGIN: io.clientcore.core.http.rest.httprequestcontext.createjsonrequest
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
        // END: io.clientcore.core.http.rest.httprequestcontext.createjsonrequest

        // BEGIN: io.clientcore.core.http.rest.httprequestcontext.postrequest
        HttpRequestContext options = HttpRequestContext.builder()
            .addRequestCallback(request -> request
                // may already be set if request is created from a client
                .setUri("https://petstore.example.com/pet")
                .setMethod(HttpMethod.POST)
                .setBody(requestBodyData)
                .getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"))
            .build();
        // END: io.clientcore.core.http.rest.httprequestcontext.postrequest
        return options;
    }


    /**
     * Code snippet for {@link HttpRequestContext.Builder#putMetadata(String, Object)}
     */
    public void putDataContext() {
        // BEGIN: io.clientcore.core.http.rest.httprequestcontext.putData

        HttpRequestContext options = HttpRequestContext.builder()
            .putMetadata("stringKey", "value")
            .putMetadata("complexObject", ProgressReporter.withProgressListener(value -> System.out.printf("Got %s bytes", value)))
            .build();

        // END: io.clientcore.core.http.rest.httprequestcontext.putData

        // BEGIN: io.clientcore.core.http.rest.httprequestcontext.getData

        // Get the string value
        Object stringKeyValue = options.getMetadata("stringKey");
        String stringValue = stringKeyValue instanceof String ? (String) stringKeyValue : null;
        System.out.printf("Key1 value: %s%n", stringValue);

        // Get the complex object
        Object complexObjectValue = options.getMetadata("complexObject");
        ProgressReporter progressReporter = complexObjectValue instanceof ProgressReporter
            ? (ProgressReporter) complexObjectValue
            : null;
        if (progressReporter != null) {
            // Use the progress reporter
            progressReporter.reportProgress(42);
        }

        // END: io.clientcore.core.http.rest.httprequestcontext.getData
    }
}
