// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.health.deidentification.models.*;

public class AsyncHelloWorld {
    public static void main(String[] args) {
        // BEGIN: com.azure.health.deidentification.async.helloworld
        DeidentificationClientBuilder deidentificationClientbuilder = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        DeidentificationAsyncClient deidentificationClient = deidentificationClientbuilder.buildAsyncClient();
        String inputText = "Hello, my name is John Smith.";

        DeidentificationContent content = new DeidentificationContent(inputText);
        // TODO: set operation to surrogate

        DeidentificationResult result = deidentificationClient.deidentifyText(content).block();
        System.out.println("Deidentified output: " + (result != null ? result.getOutputText() : null));
        // Deidentified output: Hello, my name is Krishna Doe.

        // END: com.azure.health.deidentification.async.helloworld
    }
}
