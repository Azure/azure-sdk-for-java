// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.http.HttpMethod;
import com.azure.core.util.BinaryData;
import com.azure.json.models.JsonArray;
import com.azure.json.models.JsonNumber;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;

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
            .setHttpMethod(HttpMethod.POST)
            .send(); // makes the service call

        // Check the HTTP status
        int statusCode = response.getStatusCode();
        if (statusCode == 200) {
            BinaryData responseBody = response.getBody();
            JsonObject deserialized = responseBody.toObject(JsonObject.class);
            int id = ((JsonNumber) deserialized.getProperty("id")).getValue().intValue();
            JsonArray tags = ((JsonArray) deserialized.getProperty("tags"));
            String firstTag = ((JsonString) ((JsonObject) tags.getElement(0)).getProperty("name")).getValue();
        }
        // END: com.azure.core.experimental.http.dynamicresponse.readresponse
    }
}
