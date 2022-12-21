// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.core.credential.AzureKeyCredential;

/**
 * Code snippet for {@link PersonalizerClient}
 */
public class PersonalizerClientJavaDocCodeSnippets {

    /**
     * Code snippet for creating a {@link PersonalizerClient}
     */
    public void createPersonalizerClient() {
        // BEGIN: com.azure.ai.personalizer.PersonalizerClient.instantiation
        PersonalizerClient personalizerClient = new PersonalizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.ai.personalizer.PersonalizerClient.instantiation
    }

    /**
     * Code snippet for creating a {@link PersonalizerAsyncClient}
     */
    public void createPersonalizerAsyncClient() {
        // BEGIN: com.azure.ai.personalizer.PersonalizerAsyncClient.instantiation
        PersonalizerAsyncClient personalizerClient = new PersonalizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.ai.personalizer.PersonalizerAsyncClient.instantiation
    }
}
