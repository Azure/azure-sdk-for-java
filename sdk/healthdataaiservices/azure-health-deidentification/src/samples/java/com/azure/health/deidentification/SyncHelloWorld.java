// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.models.DeidentificationContent;
import com.azure.health.deidentification.models.DeidentificationResult;

public class SyncHelloWorld {
    public static void main(String[] args) {
        DeidentificationClientBuilder deidentificationClientbuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        DeidentificationClient deidentificationClient = deidentificationClientbuilder.buildClient();
        // BEGIN: com.azure.health.deidentification.sync.helloworld
        String inputText = "Hello, my name is John Smith.";

        DeidentificationContent content = new DeidentificationContent(inputText);

        DeidentificationResult result = deidentificationClient.deidentify(content);

        System.out.println("Deidentified output: " + result.getOutputText());
        // Deidentified output: Hello, my name is Harley Billiard.
        // END: com.azure.health.deidentification.sync.helloworld
    }
}
