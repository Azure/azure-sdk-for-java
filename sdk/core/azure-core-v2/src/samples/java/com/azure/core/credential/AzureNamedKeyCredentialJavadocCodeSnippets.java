// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

/**
 * Codesnippets for {@link AzureNamedKeyCredential}.
 */
public class AzureNamedKeyCredentialJavadocCodeSnippets {

    public void azureNamedKeyCredenialSasKey() {
        // BEGIN: com.azure.core.credential.azureNamedKeyCredenialSasKey
        AzureNamedKeyCredential azureNamedKeyCredential =
            new AzureNamedKeyCredential("AZURE-SERVICE-SAS-KEY-NAME", "AZURE-SERVICE-SAS-KEY");
        // END: com.azure.core.credential.azureNamedKeyCredenialSasKey
    }

    public void azureSasCredential() {
        // BEGIN: com.azure.core.credential.azureSasCredential
        AzureSasCredential azureSasCredential =
            new AzureSasCredential("AZURE-SERVICE-SAS-KEY");
        // END: com.azure.core.credential.azureSasCredential
    }

    public void azureKeyCredential() {
        // BEGIN: com.azure.core.credential.azureKeyCredential
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("AZURE-SERVICE-KEY");
        // END: com.azure.core.credential.azureKeyCredential
    }
}
