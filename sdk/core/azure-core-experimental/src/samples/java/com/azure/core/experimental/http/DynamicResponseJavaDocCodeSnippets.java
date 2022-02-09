// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.util.BinaryData;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;

/**
 * JavaDoc codesnippets for {@link DynamicResponse}.
 */
public class DynamicResponseJavaDocCodeSnippets {

    /**
     * Sample to demonstrate reading a JSON response from {@link DynamicResponse}.
     */
    public void readResponse() {
        DynamicRequest dynamicRequest = new DynamicRequestJavaDocCodeSnippets().createInstance();

        // BEGIN: com.azure.core.experimental.http.dynamicresponse.readresponse
        DynamicResponse response = dynamicRequest
            .setUrl("https://petstore.example.com/pet/{petId}") // may already be set if request is created from a client
            .setPathParam("petId", "2343245")
            .send(); // makes the service call

        // Check the HTTP status
        int statusCode = response.getStatusCode();
        if (statusCode == 200) {
            BinaryData responseBody = response.getBody();
            String responseBodyStr = responseBody.toString();
            JsonObject deserialized = Json.createReader(new StringReader(responseBodyStr)).readObject();
            int id = deserialized.getInt("id");
            String firstTag = deserialized.getJsonArray("tags").get(0).asJsonObject().getString("name");
        }
        // END: com.azure.core.experimental.http.dynamicresponse.readresponse
    }
}
