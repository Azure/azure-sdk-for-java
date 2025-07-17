// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification;

import com.azure.core.util.Configuration;
import com.azure.health.deidentification.models.DeidentificationContent;
import com.azure.health.deidentification.models.DeidentificationResult;
import com.azure.health.deidentification.models.DeidentificationOperationType;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class DeidentifyText {
    public static void main(String[] args) {
        DeidentificationClient deidentificationClient = new DeidentificationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: com.azure.health.deidentification.samples.deidentify_text
        String inputText = "Hello, my name is John Smith.";

        DeidentificationContent content = new DeidentificationContent(inputText);
        content.setOperationType(DeidentificationOperationType.SURROGATE);

        DeidentificationResult result = deidentificationClient.deidentifyText(content);
        System.out.println("De-identified output: " + (result != null ? result.getOutputText() : null));
        // De-identified output: Hello, my name is <synthetic name>.
        // END: com.azure.health.deidentification.samples.deidentify_text
    }
}
