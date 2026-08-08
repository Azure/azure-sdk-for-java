// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;

import java.util.Collections;

/**
 * This sample demonstrates how to create, get, check, update, and delete an External Key Manager (EKM) connection
 * asynchronously for a Key Vault account.
 */
public class EkmConnectionHelloWorldAsync {
    /**
     * Authenticates with the key vault and shows how to create, get, check, update, and delete an EKM connection
     * asynchronously for a Key Vault account.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when an invalid key vault URL is passed.
     */
    public static void main(String[] args) {
        /* Instantiate a KeyVaultEkmAsyncClient that will be used to call the service. Notice that the client is using
        default Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault Managed HSM. See the README
        (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-administration/README.md)
        for links and instructions. */
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = new KeyVaultEkmClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        /* Let's create an EKM connection that points to the EKM proxy. The host and server CA certificates are
        required, while the path prefix and server subject common name are optional. */
        KeyVaultEkmConnection ekmConnectionToCreate =
            new KeyVaultEkmConnection("<ekm-proxy-host>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setServerSubjectCommonName("<server-subject-common-name>");

        keyVaultEkmAsyncClient.createEkmConnection(ekmConnectionToCreate)
            .subscribe(createdEkmConnection -> {
                System.out.printf("Created EKM connection with host '%s'.%n", createdEkmConnection.getHost());

                /* Now let's retrieve the EKM connection we just created. */
                keyVaultEkmAsyncClient.getEkmConnection()
                    .subscribe(retrievedEkmConnection ->
                        System.out.printf("Retrieved EKM connection with host '%s'.%n",
                            retrievedEkmConnection.getHost()));

                /* We can check the connectivity and authentication with the EKM proxy. */
                keyVaultEkmAsyncClient.checkEkmConnection()
                    .subscribe(ekmProxyInfo ->
                        System.out.printf("Checked EKM connection. Proxy vendor: '%s', proxy name: '%s'.%n",
                            ekmProxyInfo.getProxyVendor(), ekmProxyInfo.getProxyName()));

                /* We can also retrieve the EKM proxy client certificate used to authenticate to the EKM proxy. */
                keyVaultEkmAsyncClient.getEkmCertificate()
                    .subscribe(certificateInfo ->
                        System.out.printf("Retrieved EKM proxy client certificate with subject common name '%s'.%n",
                            certificateInfo.getSubjectCommonName()));
            });

        /* If we want to change the EKM connection, we can update it. */
        KeyVaultEkmConnection ekmConnectionToUpdate =
            new KeyVaultEkmConnection("<updated-ekm-proxy-host>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setServerSubjectCommonName("<server-subject-common-name>");

        keyVaultEkmAsyncClient.updateEkmConnection(ekmConnectionToUpdate)
            .subscribe(updatedEkmConnection ->
                System.out.printf("Updated EKM connection with host '%s'.%n", updatedEkmConnection.getHost()));

        /* Finally, let's delete the EKM connection. */
        keyVaultEkmAsyncClient.deleteEkmConnection()
            .subscribe(deletedEkmConnection ->
                System.out.printf("Deleted EKM connection with host '%s'.%n", deletedEkmConnection.getHost()));

        /* Since the above calls are non-blocking and asynchronous, we need to prevent the application from exiting
        before the operations complete. In a real application, you would handle this differently. */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for asynchronous operations to complete.");
        }
    }
}
