// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.scanning;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sample for listing all system scan rulesets using the SystemScanRulesetsClient.
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

        for (BinaryData systemScanRuleSet : list) {
            JsonObject ruleset = systemScanRuleSet.toObject(JsonObject.class);
            System.out.println(((JsonString) ruleset.getProperty("name")).getValue() + ": "
                + ((JsonString) ruleset.getProperty("status")).getValue());
        }

    }
}
