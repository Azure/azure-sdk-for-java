// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.catalog;

import com.azure.core.experimental.http.DynamicResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.stream.Collectors;

/**
 * Sample for getting glossaries using the GlossaryBaseClient.
 */
public class ListGlossaries {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(final String[] args) {
        GlossaryBaseClient client = new PurviewCatalogClientBuilder()
            .endpoint(System.getenv("ATLAS_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildGlossaryBaseClient();

        DynamicResponse res = client.listGlossaries().send();
        if (res.getStatusCode() / 100 != 2) {
            System.out.println("Error code" + res.getStatusCode() + ": " + res.getBody());
        } else {
            JsonReader jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
            JsonArray glossaries = jsonReader.readArray();
            glossaries.forEach(value -> {
                JsonObject glossary = value.asJsonObject();
                System.out.println(glossary.getString("name"));
                System.out.print("Terms: [");
                System.out.print(glossary.getJsonArray("terms").stream()
                    .map(value2 -> value2.asJsonObject().getString("displayText"))
                    .collect(Collectors.joining(", ")));
                System.out.println("]");
            });
        }
    }
}
