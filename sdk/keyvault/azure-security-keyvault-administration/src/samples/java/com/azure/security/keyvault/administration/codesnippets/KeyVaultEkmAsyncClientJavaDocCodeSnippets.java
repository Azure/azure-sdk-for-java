// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.codesnippets;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultEkmAsyncClient;
import com.azure.security.keyvault.administration.KeyVaultEkmClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;

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
}
