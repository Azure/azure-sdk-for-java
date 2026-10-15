// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.codesnippets;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultEkmClient;
import com.azure.security.keyvault.administration.KeyVaultEkmClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyClientCertificateInfo;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyInfo;

import java.util.Collections;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyVaultEkmClient}.
 */
public class KeyVaultEkmClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link KeyVaultEkmClient}.
     *
     * @return An instance of {@link KeyVaultEkmClient}.
     */
    public KeyVaultEkmClient createClient() {
        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.instantiation
        KeyVaultEkmClient keyVaultEkmClient = new KeyVaultEkmClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.instantiation

        return keyVaultEkmClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmClient#getEkmConnection()} and
     * {@link KeyVaultEkmClient#getEkmConnectionWithResponse(Context)}.
     */
    public void getEkmConnection() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmConnection
        KeyVaultEkmConnection ekmConnection = keyVaultEkmClient.getEkmConnection();

        System.out.printf("Retrieved EKM connection with host '%s'.%n", ekmConnection.getHost());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmConnection

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmConnectionWithResponse#Context
        Response<KeyVaultEkmConnection> response =
            keyVaultEkmClient.getEkmConnectionWithResponse(new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Retrieved EKM connection with host '%s'.%n",
            response.getStatusCode(), response.getValue().getHost());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmConnectionWithResponse#Context
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmClient#createEkmConnection(KeyVaultEkmConnection)} and
     * {@link KeyVaultEkmClient#createEkmConnectionWithResponse(KeyVaultEkmConnection, Context)}.
     */
    public void createEkmConnection() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.createEkmConnection#KeyVaultEkmConnection
        KeyVaultEkmConnection ekmConnectionToCreate =
            new KeyVaultEkmConnection("<ekm-proxy-host>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setServerSubjectCommonName("<server-subject-common-name>");

        KeyVaultEkmConnection createdEkmConnection = keyVaultEkmClient.createEkmConnection(ekmConnectionToCreate);

        System.out.printf("Created EKM connection with host '%s'.%n", createdEkmConnection.getHost());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.createEkmConnection#KeyVaultEkmConnection

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.createEkmConnectionWithResponse#KeyVaultEkmConnection-Context
        KeyVaultEkmConnection myEkmConnectionToCreate =
            new KeyVaultEkmConnection("<ekm-proxy-host>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setServerSubjectCommonName("<server-subject-common-name>");

        Response<KeyVaultEkmConnection> response =
            keyVaultEkmClient.createEkmConnectionWithResponse(myEkmConnectionToCreate, new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Created EKM connection with host '%s'.%n",
            response.getStatusCode(), response.getValue().getHost());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.createEkmConnectionWithResponse#KeyVaultEkmConnection-Context
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmClient#updateEkmConnection(KeyVaultEkmConnection)} and
     * {@link KeyVaultEkmClient#updateEkmConnectionWithResponse(KeyVaultEkmConnection, Context)}.
     */
    public void updateEkmConnection() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.updateEkmConnection#KeyVaultEkmConnection
        KeyVaultEkmConnection ekmConnectionToUpdate =
            new KeyVaultEkmConnection("<ekm-proxy-host>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setServerSubjectCommonName("<server-subject-common-name>");

        KeyVaultEkmConnection updatedEkmConnection = keyVaultEkmClient.updateEkmConnection(ekmConnectionToUpdate);

        System.out.printf("Updated EKM connection with host '%s'.%n", updatedEkmConnection.getHost());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.updateEkmConnection#KeyVaultEkmConnection

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.updateEkmConnectionWithResponse#KeyVaultEkmConnection-Context
        KeyVaultEkmConnection myEkmConnectionToUpdate =
            new KeyVaultEkmConnection("<ekm-proxy-host>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setServerSubjectCommonName("<server-subject-common-name>");

        Response<KeyVaultEkmConnection> response =
            keyVaultEkmClient.updateEkmConnectionWithResponse(myEkmConnectionToUpdate, new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Updated EKM connection with host '%s'.%n",
            response.getStatusCode(), response.getValue().getHost());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.updateEkmConnectionWithResponse#KeyVaultEkmConnection-Context
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmClient#deleteEkmConnection()} and
     * {@link KeyVaultEkmClient#deleteEkmConnectionWithResponse(Context)}.
     */
    public void deleteEkmConnection() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.deleteEkmConnection
        KeyVaultEkmConnection deletedEkmConnection = keyVaultEkmClient.deleteEkmConnection();

        System.out.printf("Deleted EKM connection with host '%s'.%n", deletedEkmConnection.getHost());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.deleteEkmConnection

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.deleteEkmConnectionWithResponse#Context
        Response<KeyVaultEkmConnection> response =
            keyVaultEkmClient.deleteEkmConnectionWithResponse(new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Deleted EKM connection with host '%s'.%n",
            response.getStatusCode(), response.getValue().getHost());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.deleteEkmConnectionWithResponse#Context
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmClient#checkEkmConnection()} and
     * {@link KeyVaultEkmClient#checkEkmConnectionWithResponse(Context)}.
     */
    public void checkEkmConnection() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.checkEkmConnection
        KeyVaultEkmProxyInfo ekmProxyInfo = keyVaultEkmClient.checkEkmConnection();

        System.out.printf("Checked EKM connection. Proxy vendor: '%s', proxy name: '%s'.%n",
            ekmProxyInfo.getProxyVendor(), ekmProxyInfo.getProxyName());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.checkEkmConnection

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.checkEkmConnectionWithResponse#Context
        Response<KeyVaultEkmProxyInfo> response =
            keyVaultEkmClient.checkEkmConnectionWithResponse(new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Proxy vendor: '%s', proxy name: '%s'.%n",
            response.getStatusCode(), response.getValue().getProxyVendor(), response.getValue().getProxyName());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.checkEkmConnectionWithResponse#Context
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmClient#getEkmCertificate()} and
     * {@link KeyVaultEkmClient#getEkmCertificateWithResponse(Context)}.
     */
    public void getEkmCertificate() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmCertificate
        KeyVaultEkmProxyClientCertificateInfo certificateInfo = keyVaultEkmClient.getEkmCertificate();

        System.out.printf("Retrieved EKM proxy client certificate with subject common name '%s'.%n",
            certificateInfo.getSubjectCommonName());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmCertificate

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmCertificateWithResponse#Context
        Response<KeyVaultEkmProxyClientCertificateInfo> response =
            keyVaultEkmClient.getEkmCertificateWithResponse(new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Retrieved EKM proxy client certificate with"
            + " subject common name '%s'.%n", response.getStatusCode(), response.getValue().getSubjectCommonName());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmCertificateWithResponse#Context
    }
}
