// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.core.credential.AzureKeyCredential;

/**
 * Code snippet for {@link PersonalizerAdminClient}
 */
public class PersonalizerAdminClientJavaDocCodeSnippets {

    /**
     * Code snippet for creating a {@link PersonalizerAdminClient}
     */
    public void createPersonalizerAdminClient() {
        // BEGIN: com.azure.ai.personalizer.PersonalizerAdminClient.instantiation
        PersonalizerAdminClient PersonalizerAdminClient = new PersonalizerAdminClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.ai.personalizer.PersonalizerAdminClient.instantiation
    }

    /**
     * Code snippet for creating a {@link PersonalizerAdminAsyncClient}
     */
    public void createPersonalizerAdminAsyncClient() {
        // BEGIN: com.azure.ai.personalizer.PersonalizerAdminAsyncClient.instantiation
        PersonalizerAdminAsyncClient PersonalizerAdminClient = new PersonalizerAdminClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.ai.personalizer.PersonalizerAdminAsyncClient.instantiation
    }
}
