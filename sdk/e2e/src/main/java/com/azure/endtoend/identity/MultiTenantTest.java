// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.endtoend.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;

import java.io.IOException;
import java.util.Locale;

/**
 * Runs Identity tests across multiple tests.
 */
class MultiTenantTest {
    private static final String AZURE_MULTI_TENANT_TEST_MODE = "AZURE_MULTI_TENANT_TEST_MODE";
    private static final String AZURE_USER_NAME = "AZURE_USER_NAME";
    private static final String AZURE_USER_PASSWORD = "AZURE_USER_PASSWORD";
    private static final String AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
    private static final String AZURE_CLIENT_SECRET = "AZURE_CLIENT_SECRET";
    private static final String AZURE_TENANT_ID = "AZURE_TENANT_ID";
    private static final Configuration CONFIGURATION = Configuration.getGlobalConfiguration().clone();
    private final ClientLogger logger = new ClientLogger(MultiTenantTest.class);

    /**
     * Runs the multi tenant identity tests
     * @throws IllegalStateException if AZURE_MULTI_TENANT_TEST_MODE is not set to "user" or "sp"
     */
    void run() throws IllegalStateException {
        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(AZURE_MULTI_TENANT_TEST_MODE))) {
            throw logger.logExceptionAsError(new IllegalStateException("Test mode is not set. Set environment "
                                                + "variable AZURE_MULTI_TENANT_TEST_MODE to user or sp"));
        }

        String mode = CONFIGURATION.get(AZURE_MULTI_TENANT_TEST_MODE).toLowerCase(Locale.ENGLISH);
        switch (mode) {
            case "user":
                testUserPasswordCanAccessGraph();
                break;
            case "sp":
                testServicePrincipalCanAccessGraph();
                break;
            default:
                throw logger.logExceptionAsError(
                    new IllegalStateException("Invalid Test mode is configured AZURE_MULTI_TENANT_TEST_MODE. "
                                                    + "Possible values are user or sp."));
        }
    }

    private void testUserPasswordCanAccessGraph() {
        assertConfigPresence(AZURE_CLIENT_ID,
            "testUserPasswordCanAccessGraph - AZURE_CLIENT_ID not configured in the environment.");
        assertConfigPresence(AZURE_TENANT_ID,
            "testUserPasswordCanAccessGraph - AZURE_TENANT_ID not configured in the environment.");
        assertConfigPresence(AZURE_USER_NAME,
            "testUserPasswordCanAccessGraph - AZURE_USER_NAME not configured in the environment.");
        assertConfigPresence(AZURE_USER_PASSWORD,
            "testUserPasswordCanAccessGraph - AZURE_USER_PASSWORD not configured in the environment.");
        String clientId = CONFIGURATION.get(AZURE_CLIENT_ID);
        String tenantId = CONFIGURATION.get(AZURE_TENANT_ID);
        String username = CONFIGURATION.get(AZURE_USER_NAME);
        String password = CONFIGURATION.get(AZURE_USER_PASSWORD);

        UsernamePasswordCredential credential = new UsernamePasswordCredentialBuilder()
            .clientId(clientId)
            .tenantId(tenantId)
            .username(username)
            .password(password)
            .build();

        GraphRbacManager graphRbacManager = GraphRbacManager.authenticate(
            new AzureTokenCredentials(AzureEnvironment.AZURE, tenantId) {
                @Override
                public String getToken(String s) throws IOException {
                    return credential.getToken(new TokenRequestContext().addScopes(s + "/.default"))
                        .map(AccessToken::getToken)
                        .block();
                }
            });

        String upn = null;
        String errorMessage = "Error";
        try {
            ActiveDirectoryUser user = graphRbacManager.users().getByName(username);
            if (user != null) {
                upn = user.userPrincipalName();
            }
        } catch (Throwable t) {
            errorMessage += ": " + t.getMessage();
        }
        assertExpectedValue(username, upn, "SUCCESS: testUserPasswordCanAccessGraph - "
            + "Successfully retrieved a user through a multi-tenant app.", errorMessage);
    }

    private void testServicePrincipalCanAccessGraph() {
        assertConfigPresence(AZURE_CLIENT_ID,
            "testServicePrincipalCanAccessGraph - AZURE_CLIENT_ID not configured in the environment.");
        assertConfigPresence(AZURE_TENANT_ID,
            "testServicePrincipalCanAccessGraph - AZURE_TENANT_ID not configured in the environment.");
        assertConfigPresence(AZURE_CLIENT_SECRET,
            "testServicePrincipalCanAccessGraph - AZURE_CLIENT_SECRET not configured in the environment.");
        assertConfigPresence(AZURE_USER_NAME,
            "testServicePrincipalCanAccessGraph - AZURE_USER_NAME not configured in the environment.");
        String clientId = CONFIGURATION.get(AZURE_CLIENT_ID);
        String tenantId = CONFIGURATION.get(AZURE_TENANT_ID);
        String clientSecret = CONFIGURATION.get(AZURE_CLIENT_SECRET);
        String username = CONFIGURATION.get(AZURE_USER_NAME);

        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .clientId(clientId)
            .tenantId(tenantId)
            .clientSecret(clientSecret)
            .build();

        GraphRbacManager graphRbacManager = GraphRbacManager.authenticate(
            new AzureTokenCredentials(AzureEnvironment.AZURE, tenantId) {
                @Override
                public String getToken(String s) throws IOException {
                    return credential.getToken(new TokenRequestContext().addScopes(s + "/.default"))
                        .map(AccessToken::getToken)
                        .block();
                }
            });

        String upn = null;
        String errorMessage = "Error";
        try {
            ActiveDirectoryUser user = graphRbacManager.users().getByName(username);
            if (user != null) {
                upn = user.userPrincipalName();
            }
        } catch (Throwable t) {
            errorMessage += ": " + t.getMessage();
        }
        assertExpectedValue(username, upn, "SUCCESS: testServicePrincipalCanAccessGraph - "
            + "Successfully retrieved a user from another tenant.", errorMessage);
    }

    private void assertExpectedValue(String expected, String actual, String success, String faiure) {
        if (expected.equals(actual)) {
            System.out.println(success);
            return;
        }
        System.out.println(faiure);
    }

    private void assertConfigPresence(String identitfer, String errorMessage) {
        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(identitfer))) {
            throw logger.logExceptionAsError(new IllegalStateException(errorMessage));
        }
    }
}

