// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.credential;

/**
 * Codesnippets for {@link ClientNamedKeyCredential}.
 */
public class ClientNamedKeyCredentialJavadocCodeSnippets {

    public void azureNamedKeyCredenialSasKey() {
        // BEGIN: com.client.core.credential.azureNamedKeyCredenialSasKey
        ClientNamedKeyCredential azureNamedKeyCredential =
            new ClientNamedKeyCredential("AZURE-SERVICE-SAS-KEY-NAME", "AZURE-SERVICE-SAS-KEY");
        // END: com.client.core.credential.azureNamedKeyCredenialSasKey
    }
}
