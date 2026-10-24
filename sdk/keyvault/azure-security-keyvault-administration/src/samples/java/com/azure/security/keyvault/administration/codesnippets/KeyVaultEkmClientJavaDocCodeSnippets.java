// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.codesnippets;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultEkmClient;
import com.azure.security.keyvault.administration.KeyVaultEkmClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnection;
import com.azure.security.keyvault.administration.models.KeyVaultEkmConnectivityMode;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpoint;
import com.azure.security.keyvault.administration.models.KeyVaultEkmPrivateEndpointOperation;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyClientCertificateInfo;
import com.azure.security.keyvault.administration.models.KeyVaultEkmProxyInfo;

import java.util.Collections;
import java.util.List;

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

    /**
     * Generates a code sample for using
     * {@link KeyVaultEkmClient#beginCreateEkmPrivateEndpoint(String, String, String)}.
     */
    public void beginCreateEkmPrivateEndpoint() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.beginCreateEkmPrivateEndpoint#String-String-String
        SyncPoller<KeyVaultEkmPrivateEndpointOperation, KeyVaultEkmPrivateEndpoint> createPoller =
            keyVaultEkmClient.beginCreateEkmPrivateEndpoint("<private-endpoint-name>", "<private-link-service-alias>",
                "Please approve this connection from my Managed HSM");
        PollResponse<KeyVaultEkmPrivateEndpointOperation> createResponse = createPoller.waitForCompletion();

        System.out.printf("EKM private endpoint creation finished with status '%s'.%n",
            createResponse.getValue().getStatus());

        KeyVaultEkmPrivateEndpoint createdPrivateEndpoint = createPoller.getFinalResult();

        System.out.printf("Created EKM private endpoint '%s'.%n", createdPrivateEndpoint.getName());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.beginCreateEkmPrivateEndpoint#String-String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmClient#getEkmPrivateEndpoint(String)} and
     * {@link KeyVaultEkmClient#getEkmPrivateEndpointWithResponse(String, Context)}.
     */
    public void getEkmPrivateEndpoint() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmPrivateEndpoint#String
        KeyVaultEkmPrivateEndpoint privateEndpoint = keyVaultEkmClient.getEkmPrivateEndpoint("<private-endpoint-name>");

        System.out.printf("Retrieved EKM private endpoint '%s' with provisioning state '%s'.%n",
            privateEndpoint.getName(), privateEndpoint.getProvisioningState());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmPrivateEndpoint#String

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmPrivateEndpointWithResponse#String-Context
        Response<KeyVaultEkmPrivateEndpoint> response =
            keyVaultEkmClient.getEkmPrivateEndpointWithResponse("<private-endpoint-name>",
                new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Retrieved EKM private endpoint '%s'.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmPrivateEndpointWithResponse#String-Context
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmClient#listEkmPrivateEndpoints()} and
     * {@link KeyVaultEkmClient#listEkmPrivateEndpointsWithResponse(Context)}.
     */
    public void listEkmPrivateEndpoints() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.listEkmPrivateEndpoints
        List<KeyVaultEkmPrivateEndpoint> privateEndpoints = keyVaultEkmClient.listEkmPrivateEndpoints();

        for (KeyVaultEkmPrivateEndpoint privateEndpoint : privateEndpoints) {
            System.out.printf("Retrieved EKM private endpoint '%s' with provisioning state '%s'.%n",
                privateEndpoint.getName(), privateEndpoint.getProvisioningState());
        }
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.listEkmPrivateEndpoints

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.listEkmPrivateEndpointsWithResponse#Context
        Response<List<KeyVaultEkmPrivateEndpoint>> response =
            keyVaultEkmClient.listEkmPrivateEndpointsWithResponse(new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Retrieved %d EKM private endpoints.%n",
            response.getStatusCode(), response.getValue().size());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.listEkmPrivateEndpointsWithResponse#Context
    }

    /**
     * Generates a code sample for using {@link KeyVaultEkmClient#beginDeleteEkmPrivateEndpoint(String)}.
     */
    public void beginDeleteEkmPrivateEndpoint() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.beginDeleteEkmPrivateEndpoint#String
        SyncPoller<KeyVaultEkmPrivateEndpointOperation, Void> deletePoller =
            keyVaultEkmClient.beginDeleteEkmPrivateEndpoint("<private-endpoint-name>");
        PollResponse<KeyVaultEkmPrivateEndpointOperation> deleteResponse = deletePoller.waitForCompletion();

        System.out.printf("EKM private endpoint deletion finished with status '%s'.%n",
            deleteResponse.getValue().getStatus());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.beginDeleteEkmPrivateEndpoint#String
    }

    /**
     * Generates code samples for using {@link KeyVaultEkmClient#getEkmPrivateEndpointOperationStatus(String)} and
     * {@link KeyVaultEkmClient#getEkmPrivateEndpointOperationStatusWithResponse(String, Context)}.
     */
    public void getEkmPrivateEndpointOperationStatus() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmPrivateEndpointOperationStatus#String
        KeyVaultEkmPrivateEndpointOperation operation =
            keyVaultEkmClient.getEkmPrivateEndpointOperationStatus("<operation-id>");

        System.out.printf("EKM private endpoint operation '%s' on '%s' has status '%s'.%n", operation.getOperationId(),
            operation.getPrivateEndpointName(), operation.getStatus());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmPrivateEndpointOperationStatus#String

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmPrivateEndpointOperationStatusWithResponse#String-Context
        Response<KeyVaultEkmPrivateEndpointOperation> response =
            keyVaultEkmClient.getEkmPrivateEndpointOperationStatusWithResponse("<operation-id>",
                new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. EKM private endpoint operation '%s' has"
            + " status '%s'.%n", response.getStatusCode(), response.getValue().getOperationId(),
            response.getValue().getStatus());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.getEkmPrivateEndpointOperationStatusWithResponse#String-Context
    }

    /**
     * Generates a code sample for creating an EKM connection that reaches the EKM proxy through a private endpoint
     * using {@link KeyVaultEkmClient#createEkmConnection(KeyVaultEkmConnection)}.
     */
    public void createPrivateEkmConnection() {
        KeyVaultEkmClient keyVaultEkmClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultEkmClient.createPrivateEkmConnection
        // The host is the private endpoint's name, so the proxy's certificate is validated against the subject name.
        KeyVaultEkmConnection privateEkmConnectionToCreate =
            new KeyVaultEkmConnection("<private-endpoint-name>",
                Collections.singletonList("<server-ca-certificate>".getBytes()))
                .setPathPrefix("<path-prefix>")
                .setConnectivityMode(KeyVaultEkmConnectivityMode.PRIVATE_ENDPOINT)
                .setServerSubjectCommonName("<server-subject-common-name>");

        KeyVaultEkmConnection createdEkmConnection =
            keyVaultEkmClient.createEkmConnection(privateEkmConnectionToCreate);

        System.out.printf("Created EKM connection with connectivity mode '%s'.%n",
            createdEkmConnection.getConnectivityMode());
        // END: com.azure.security.keyvault.administration.KeyVaultEkmClient.createPrivateEkmConnection
    }
}
