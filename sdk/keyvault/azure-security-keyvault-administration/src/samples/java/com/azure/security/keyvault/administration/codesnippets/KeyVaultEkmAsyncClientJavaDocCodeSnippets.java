// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.codesnippets;

import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient;
import com.azure.security.keyvault.administration.KeyVaultEkmClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnectivityMode;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpoint;

import java.util.Collections;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyVaultEkmAsyncClient}.
 */
public class KeyVaultEkmAsyncClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link KeyVaultEkmAsyncClient}.
     *
     * @return An instance of {@link KeyVaultEkmAsyncClient}.
     */
    public KeyVaultEkmAsyncClient createClient() {
        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.instantiation
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = new KeyVaultEkmClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.instantiation

        return keyVaultEkmAsyncClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmAsyncClient#getEkmConnection()} and
     * {@link KeyVaultEkmAsyncClient#getEkmConnectionWithResponse()}.
     */
    public void getEkmConnection() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmConnection
        keyVaultEkmAsyncClient.getEkmConnection()
            .subscribe(ekmConnection ->
                System.out.printf("Retrieved EKM connection with host '%s'.%n", ekmConnection.getHost()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmConnection

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmConnectionWithResponse
        keyVaultEkmAsyncClient.getEkmConnectionWithResponse()
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Retrieved EKM connection with host"
                    + " '%s'.%n", response.getStatusCode(), response.getValue().getHost()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmConnectionWithResponse
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmAsyncClient#createEkmConnection(KeyVaultEkmConnection)} and
     * {@link KeyVaultEkmAsyncClient#createEkmConnectionWithResponse(KeyVaultEkmConnection)}.
     */
    public void createEkmConnection() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.createEkmConnection#KeyVaultEkmConnection
        KeyVaultEkmConnection ekmConnectionToCreate =
            new KeyVaultEkmConnection("<ekm-proxy-host>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setServerSubjectCommonName("<server-subject-common-name>");

        keyVaultEkmAsyncClient.createEkmConnection(ekmConnectionToCreate)
            .subscribe(createdEkmConnection ->
                System.out.printf("Created EKM connection with host '%s'.%n", createdEkmConnection.getHost()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.createEkmConnection#KeyVaultEkmConnection

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.createEkmConnectionWithResponse#KeyVaultEkmConnection
        KeyVaultEkmConnection myEkmConnectionToCreate =
            new KeyVaultEkmConnection("<ekm-proxy-host>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setServerSubjectCommonName("<server-subject-common-name>");

        keyVaultEkmAsyncClient.createEkmConnectionWithResponse(myEkmConnectionToCreate)
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Created EKM connection with host"
                    + " '%s'.%n", response.getStatusCode(), response.getValue().getHost()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.createEkmConnectionWithResponse#KeyVaultEkmConnection
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmAsyncClient#updateEkmConnection(KeyVaultEkmConnection)} and
     * {@link KeyVaultEkmAsyncClient#updateEkmConnectionWithResponse(KeyVaultEkmConnection)}.
     */
    public void updateEkmConnection() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.updateEkmConnection#KeyVaultEkmConnection
        KeyVaultEkmConnection ekmConnectionToUpdate =
            new KeyVaultEkmConnection("<ekm-proxy-host>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setServerSubjectCommonName("<server-subject-common-name>");

        keyVaultEkmAsyncClient.updateEkmConnection(ekmConnectionToUpdate)
            .subscribe(updatedEkmConnection ->
                System.out.printf("Updated EKM connection with host '%s'.%n", updatedEkmConnection.getHost()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.updateEkmConnection#KeyVaultEkmConnection

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.updateEkmConnectionWithResponse#KeyVaultEkmConnection
        KeyVaultEkmConnection myEkmConnectionToUpdate =
            new KeyVaultEkmConnection("<ekm-proxy-host>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setServerSubjectCommonName("<server-subject-common-name>");

        keyVaultEkmAsyncClient.updateEkmConnectionWithResponse(myEkmConnectionToUpdate)
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Updated EKM connection with host"
                    + " '%s'.%n", response.getStatusCode(), response.getValue().getHost()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.updateEkmConnectionWithResponse#KeyVaultEkmConnection
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmAsyncClient#deleteEkmConnection()} and
     * {@link KeyVaultEkmAsyncClient#deleteEkmConnectionWithResponse()}.
     */
    public void deleteEkmConnection() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.deleteEkmConnection
        keyVaultEkmAsyncClient.deleteEkmConnection()
            .subscribe(deletedEkmConnection ->
                System.out.printf("Deleted EKM connection with host '%s'.%n", deletedEkmConnection.getHost()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.deleteEkmConnection

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.deleteEkmConnectionWithResponse
        keyVaultEkmAsyncClient.deleteEkmConnectionWithResponse()
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Deleted EKM connection with host"
                    + " '%s'.%n", response.getStatusCode(), response.getValue().getHost()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.deleteEkmConnectionWithResponse
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmAsyncClient#checkEkmConnection()} and
     * {@link KeyVaultEkmAsyncClient#checkEkmConnectionWithResponse()}.
     */
    public void checkEkmConnection() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.checkEkmConnection
        keyVaultEkmAsyncClient.checkEkmConnection()
            .subscribe(ekmProxyInfo ->
                System.out.printf("Checked EKM connection. Proxy vendor: '%s', proxy name: '%s'.%n",
                    ekmProxyInfo.getProxyVendor(), ekmProxyInfo.getProxyName()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.checkEkmConnection

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.checkEkmConnectionWithResponse
        keyVaultEkmAsyncClient.checkEkmConnectionWithResponse()
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Proxy vendor: '%s', proxy name: '%s'.%n",
                    response.getStatusCode(), response.getValue().getProxyVendor(), response.getValue().getProxyName()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.checkEkmConnectionWithResponse
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmAsyncClient#getEkmCertificate()} and
     * {@link KeyVaultEkmAsyncClient#getEkmCertificateWithResponse()}.
     */
    public void getEkmCertificate() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmCertificate
        keyVaultEkmAsyncClient.getEkmCertificate()
            .subscribe(certificateInfo ->
                System.out.printf("Retrieved EKM proxy client certificate with subject common name '%s'.%n",
                    certificateInfo.getSubjectCommonName()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmCertificate

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmCertificateWithResponse
        keyVaultEkmAsyncClient.getEkmCertificateWithResponse()
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Retrieved EKM proxy client certificate"
                    + " with subject common name '%s'.%n", response.getStatusCode(),
                    response.getValue().getSubjectCommonName()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmCertificateWithResponse
    }

    /**
     * Generates a code sample for using
     * {@link KeyVaultEkmAsyncClient#beginCreateEkmPrivateEndpoint(String, String, String)}.
     */
    public void beginCreateEkmPrivateEndpoint() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.beginCreateEkmPrivateEndpoint#String-String-String
        keyVaultEkmAsyncClient
            .beginCreateEkmPrivateEndpoint("<private-endpoint-name>", "<private-link-service-alias>",
                "Please approve this connection from my Managed HSM")
            .last()
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(createdPrivateEndpoint ->
                System.out.printf("Created EKM private endpoint '%s'.%n", createdPrivateEndpoint.getName()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.beginCreateEkmPrivateEndpoint#String-String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmAsyncClient#getEkmPrivateEndpoint(String)} and
     * {@link KeyVaultEkmAsyncClient#getEkmPrivateEndpointWithResponse(String)}.
     */
    public void getEkmPrivateEndpoint() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmPrivateEndpoint#String
        keyVaultEkmAsyncClient.getEkmPrivateEndpoint("<private-endpoint-name>")
            .subscribe(privateEndpoint ->
                System.out.printf("Retrieved EKM private endpoint '%s' with provisioning state '%s'.%n",
                    privateEndpoint.getName(), privateEndpoint.getProvisioningState()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmPrivateEndpoint#String

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmPrivateEndpointWithResponse#String
        keyVaultEkmAsyncClient.getEkmPrivateEndpointWithResponse("<private-endpoint-name>")
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Retrieved EKM private endpoint '%s'.%n",
                    response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmPrivateEndpointWithResponse#String
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmAsyncClient#listEkmPrivateEndpoints()} and
     * {@link KeyVaultEkmAsyncClient#listEkmPrivateEndpointsWithResponse()}.
     */
    public void listEkmPrivateEndpoints() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.listEkmPrivateEndpoints
        keyVaultEkmAsyncClient.listEkmPrivateEndpoints()
            .subscribe(privateEndpoints -> {
                for (KeyVaultEkmPrivateEndpoint privateEndpoint : privateEndpoints) {
                    System.out.printf("Retrieved EKM private endpoint '%s' with provisioning state '%s'.%n",
                        privateEndpoint.getName(), privateEndpoint.getProvisioningState());
                }
            });
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.listEkmPrivateEndpoints

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.listEkmPrivateEndpointsWithResponse
        keyVaultEkmAsyncClient.listEkmPrivateEndpointsWithResponse()
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Retrieved %d EKM private endpoints.%n",
                    response.getStatusCode(), response.getValue().size()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.listEkmPrivateEndpointsWithResponse
    }

    /**
     * Generates a code sample for using {@link KeyVaultEkmAsyncClient#beginDeleteEkmPrivateEndpoint(String)}.
     */
    public void beginDeleteEkmPrivateEndpoint() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.beginDeleteEkmPrivateEndpoint#String
        keyVaultEkmAsyncClient.beginDeleteEkmPrivateEndpoint("<private-endpoint-name>")
            .last()
            .subscribe(deleteResponse ->
                System.out.printf("EKM private endpoint deletion finished with status '%s'.%n",
                    deleteResponse.getValue().getStatus()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.beginDeleteEkmPrivateEndpoint#String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultEkmAsyncClient#getEkmPrivateEndpointOperationStatus(String)} and
     * {@link KeyVaultEkmAsyncClient#getEkmPrivateEndpointOperationStatusWithResponse(String)}.
     */
    public void getEkmPrivateEndpointOperationStatus() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmPrivateEndpointOperationStatus#String
        keyVaultEkmAsyncClient.getEkmPrivateEndpointOperationStatus("<operation-id>")
            .subscribe(operation ->
                System.out.printf("EKM private endpoint operation '%s' on '%s' has status '%s'.%n",
                    operation.getOperationId(), operation.getPrivateEndpointName(), operation.getStatus()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmPrivateEndpointOperationStatus#String

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmPrivateEndpointOperationStatusWithResponse#String
        keyVaultEkmAsyncClient.getEkmPrivateEndpointOperationStatusWithResponse("<operation-id>")
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. EKM private endpoint operation '%s' has"
                    + " status '%s'.%n", response.getStatusCode(), response.getValue().getOperationId(),
                    response.getValue().getStatus()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.getEkmPrivateEndpointOperationStatusWithResponse#String
    }

    /**
     * Generates a code sample for creating an EKM connection that reaches the EKM proxy through a private endpoint
     * using {@link KeyVaultEkmAsyncClient#createEkmConnection(KeyVaultEkmConnection)}.
     */
    public void createPrivateEkmConnection() {
        KeyVaultEkmAsyncClient keyVaultEkmAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.createPrivateEkmConnection
        // The host is the private endpoint's name, so the proxy's certificate is validated against the subject name.
        KeyVaultEkmConnection privateEkmConnectionToCreate =
            new KeyVaultEkmConnection("<private-endpoint-name>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setConnectivityMode(KeyVaultEkmConnectivityMode.PRIVATE_ENDPOINT)
                .setServerSubjectCommonName("<server-subject-common-name>");

        keyVaultEkmAsyncClient.createEkmConnection(privateEkmConnectionToCreate)
            .subscribe(createdEkmConnection ->
                System.out.printf("Created EKM connection with connectivity mode '%s'.%n",
                    createdEkmConnection.getConnectivityMode()));
        // END: com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient.createPrivateEkmConnection
    }
}
