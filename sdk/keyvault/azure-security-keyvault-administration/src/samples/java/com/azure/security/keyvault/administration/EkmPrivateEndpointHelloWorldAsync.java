// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnectivityMode;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpoint;

import java.util.Collections;

/**
 * This sample demonstrates how to create, get, list, and delete an External Key Manager (EKM) proxy private endpoint
 * asynchronously for a Managed HSM, and how to create an EKM connection that reaches the EKM proxy through it.
 */
public class EkmPrivateEndpointHelloWorldAsync {
    /**
     * Authenticates with the Managed HSM and shows how to create, get, list, and delete an EKM proxy private endpoint
     * asynchronously, and how to create an EKM connection that uses it.
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

        String privateEndpointName = "<private-endpoint-name>";
        String privateLinkServiceAlias = "<private-link-service-alias>";

        /* Each step depends on the previous one, so the operations are chained in order. */
        keyVaultEkmAsyncClient
            /* Let's create an EKM proxy private endpoint that connects the Managed HSM EKM proxy pool to a Private Link
            Service. A pool may have up to two private endpoints. This is a long-running operation, so we wait for
            the last poll response. */
            .beginCreateEkmPrivateEndpoint(privateEndpointName, privateLinkServiceAlias,
                "Please approve this connection from my Managed HSM")
            .last()
            .flatMap(createResponse -> {
                System.out.printf("EKM private endpoint creation finished with status: %s%n",
                    createResponse.getValue().getStatus());

                /* The Private Link Service owner has to approve the connection before the private endpoint can be used
                by an EKM connection. Let's get the private endpoint to inspect its provisioning and connection
                states. */
                return keyVaultEkmAsyncClient.getEkmPrivateEndpoint(privateEndpointName);
            })
            .flatMap(privateEndpoint -> {
                System.out.printf("Provisioning state: %s%n", privateEndpoint.getProvisioningState());
                System.out.printf("Connection status: %s%n",
                    privateEndpoint.getPrivateLinkServiceConnectionState() == null
                        ? null
                        : privateEndpoint.getPrivateLinkServiceConnectionState().getStatus());

                /* Now let's list all of the EKM proxy private endpoints on the Managed HSM. */
                return keyVaultEkmAsyncClient.listEkmPrivateEndpoints();
            })
            .flatMap(privateEndpoints -> {
                for (KeyVaultEkmPrivateEndpoint endpoint : privateEndpoints) {
                    System.out.printf("EKM private endpoint '%s' is in state '%s'.%n", endpoint.getName(),
                        endpoint.getProvisioningState());
                }

                /* Once the connection is approved, an EKM connection can reach the EKM proxy through the private
                endpoint. To do so, set the connection's host to the private endpoint's name and its connectivity mode
                to KeyVaultEkmConnectivityMode.PRIVATE_ENDPOINT. Since the host is now the private endpoint's name
                rather than the proxy's real DNS name, the server subject common name must be set so the proxy's
                certificate can still be validated. */
                KeyVaultEkmConnection ekmConnectionToCreate =
                    new KeyVaultEkmConnection(privateEndpointName,
                        Collections.singletonList("<server-ca-certificate>".getBytes()))
                        .setPathPrefix("/api/v1")
                        .setConnectivityMode(KeyVaultEkmConnectivityMode.PRIVATE_ENDPOINT)
                        .setServerSubjectCommonName("<server-subject-common-name>");

                return keyVaultEkmAsyncClient.createEkmConnection(ekmConnectionToCreate);
            })
            .flatMap(createdEkmConnection -> {
                System.out.printf("EKM connection created with connectivity mode: %s%n",
                    createdEkmConnection.getConnectivityMode());

                /* Deleting a private endpoint is rejected while an EKM connection still references it, so we delete
                the EKM connection first. */
                return keyVaultEkmAsyncClient.deleteEkmConnection();
            })
            .flatMap(deletedEkmConnection ->
                keyVaultEkmAsyncClient.beginDeleteEkmPrivateEndpoint(privateEndpointName).last())
            .doOnNext(deleteResponse ->
                System.out.printf("EKM private endpoint deletion finished with status: %s%n",
                    deleteResponse.getValue().getStatus()))
            /* Private endpoint operations can take several minutes, so we block until the whole chain completes to
            prevent the application from exiting early. In a real application, you would handle this differently. */
            .block();
    }
}
