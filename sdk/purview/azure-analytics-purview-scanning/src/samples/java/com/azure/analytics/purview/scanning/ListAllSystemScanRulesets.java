// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.scanning;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

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
        SystemScanRulesetsClient client = new PurviewScanningClientBuilder()
            .endpoint(System.getenv("SCANNING_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildSystemScanRulesetsClient();

        PagedIterable<BinaryData> response = client.listAll(null);
        List<BinaryData> list = response.stream().collect(Collectors.toList());

        System.out.println(list.size());

        for(BinaryData systemScanRuleSet : list) {
            JsonReader jsonReader = Json.createReader(new StringReader(systemScanRuleSet.toString()));
            JsonObject ruleset = jsonReader.readObject();
            System.out.println(ruleset.getString("name") + ": " + ruleset.getString("status"));
        }

    }
}
