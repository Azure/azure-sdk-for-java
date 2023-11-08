// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.administration.PersonalizerAdministrationAsyncClient;
import com.azure.ai.personalizer.administration.PersonalizerAdministrationClient;
import com.azure.ai.personalizer.administration.PersonalizerAdministrationClientBuilder;
import com.azure.core.credential.AzureKeyCredential;

/**
 * Code snippet for {@link PersonalizerAdministrationClient}
 */
public class PersonalizerAdministrationClientJavaDocCodeSnippets {

    /**
     * Code snippet for creating a {@link PersonalizerAdministrationClient}
     */
    public void createPersonalizerAdministrationClient() {
        // BEGIN: com.azure.ai.personalizer.PersonalizerAdministrationClient.instantiation
        PersonalizerAdministrationClient adminClient = new PersonalizerAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.ai.personalizer.PersonalizerAdministrationClient.instantiation
    }

    /**
     * Code snippet for creating a {@link PersonalizerAdministrationAsyncClient}
     */
    public void createPersonalizerAdministrationAsyncClient() {
        // BEGIN: com.azure.ai.personalizer.PersonalizerAdministrationAsyncClient.instantiation
        PersonalizerAdministrationAsyncClient adminClient = new PersonalizerAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.ai.personalizer.PersonalizerAdministrationAsyncClient.instantiation
    }
}
