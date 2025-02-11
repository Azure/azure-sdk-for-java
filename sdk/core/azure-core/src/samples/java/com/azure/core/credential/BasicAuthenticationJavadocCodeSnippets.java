// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

/**
 * Codesnippets for {@link AzureNamedKeyCredential}.
 */
public class BasicAuthenticationJavadocCodeSnippets {

    public void azureNamedKeyCredentialSasKey() {
        // BEGIN: com.azure.core.credential.basicAuthenticationCredential
        BasicAuthenticationCredential basicAuthenticationCredential =
            new BasicAuthenticationCredential("<username>", "<password>");
        // END: com.azure.core.credential.basicAuthenticationCredential
    }

}
