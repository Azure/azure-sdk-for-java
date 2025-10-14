// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification;

import com.azure.core.util.Configuration;
import com.azure.health.deidentification.models.DeidentificationContent;
import com.azure.health.deidentification.models.DeidentificationOperationType;
import com.azure.health.deidentification.models.DeidentificationResult;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

public class DeidentifyTextAsync {
    public static void main(String[] args) {
        DeidentificationAsyncClient deidentificationClient = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // BEGIN: com.azure.health.deidentification.samples.deidentify_text_async
        String inputText = "Hello, my name is John Smith.";

        DeidentificationContent content = new DeidentificationContent(inputText);
        content.setOperationType(DeidentificationOperationType.SURROGATE);

        Mono<DeidentificationResult> resultMono = deidentificationClient.deidentifyText(content);

        resultMono.subscribe(
            result -> System.out.println("De-identified output: " + (result != null ? result.getOutputText() : null)),
            error -> System.err.println("Error: " + error)
        );
        // De-identified output: Hello, my name is <synthetic name>.
        // END: com.azure.health.deidentification.samples.deidentify_text_async
    }
}
