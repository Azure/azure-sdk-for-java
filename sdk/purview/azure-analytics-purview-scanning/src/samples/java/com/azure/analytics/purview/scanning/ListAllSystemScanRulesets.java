// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.scanning;

import com.azure.core.experimental.http.DynamicResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

/**
 * Sample for listing all system scan rulesets using the SystemScanRulesetsBaseClient.
 */
public class ListAllSystemScanRulesets {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(final String[] args) {
        SystemScanRulesetsBaseClient client = new PurviewScanningClientBuilder()
            .endpoint(System.getenv("SCANNING_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildSystemScanRulesetsBaseClient();

        DynamicResponse res = client.listAll().send();
        if (res.getStatusCode() / 100 != 2) {
            System.out.println("Error code" + res.getStatusCode() + ": " + res.getBody());
        } else {
            JsonReader jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
            JsonObject result = jsonReader.readObject();
            if (result.containsKey("value")) {
                JsonArray values = result.getJsonArray("value");
                values.forEach(value -> {
                    JsonObject ruleset = value.asJsonObject();
                    System.out.println(ruleset.getString("name") + ": " + ruleset.getString("status"));
                });
            }
        }
    }
}
