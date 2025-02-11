// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
import com.azure.communication.identity.models.GetTokenForTeamsUserOptions;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.List;

import static com.azure.communication.identity.CteTestHelper.skipExchangeAadTeamsTokenTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommunicationIdentityManagedIdentityTests extends CommunicationIdentityClientTestBase {

    private CommunicationIdentityClient client;
    private CommunicationIdentityClientBuilder builder;

    @Override
    public void beforeTest() {
        super.beforeTest();
        builder = createClientBuilderUsingManagedIdentity(buildSyncAssertingClient(httpClient));
    }

    @Test
    public void createUser() {
        // Arrange
        client = setupClient(builder, "createUserUsingManagedIdentitySync");
        assertNotNull(client);

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);
    }

    @Test
    public void createUserWithResponse() {
        // Arrange
        client = setupClient(builder, "createUserWithResponseUsingManagedIdentitySync");

        // Action & Assert
        Response<CommunicationUserIdentifier> response = client.createUserWithResponse(Context.NONE);
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
        verifyUserNotEmpty(response.getValue());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenScopeTestHelper#getTokenScopes")
    public void createUserAndToken(String testName, List<CommunicationTokenScope> scopes) {
        // Arrange
        client = setupClient(builder, "createUserAndTokenUsingManagedIdentityWith" + testName + SYNC_TEST_SUFFIX);

        // Action & Assert
        CommunicationUserIdentifierAndToken result = client.createUserAndToken(scopes);
        verifyUserNotEmpty(result.getUser());
        verifyTokenNotEmpty(result.getUserToken());
    }

    @Test
    public void createUserAndTokenWithResponse() {
        // Arrange
        client = setupClient(builder, "createUserAndTokenWithResponseUsingManagedIdentitySync");

        // Action & Assert
        Response<CommunicationUserIdentifierAndToken> response
            = client.createUserAndTokenWithResponse(SCOPES, Context.NONE);
        CommunicationUserIdentifierAndToken result = response.getValue();
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
        verifyUserNotEmpty(result.getUser());
        verifyTokenNotEmpty(result.getUserToken());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenScopeTestHelper#getTokenScopes")
    public void getToken(String testName, List<CommunicationTokenScope> scopes) {
        // Arrange
        client = setupClient(builder, "getTokenUsingManagedIdentityWith" + testName + SYNC_TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        AccessToken issuedToken = client.getToken(communicationUser, scopes);
        verifyTokenNotEmpty(issuedToken);
    }

    @Test
    public void getTokenWithResponse() {
        // Arrange
        client = setupClient(builder, "getTokenWithResponseUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();

        // Action & Assert
        Response<AccessToken> response = client.getTokenWithResponse(communicationUser, SCOPES, Context.NONE);
        assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(response.getValue());
    }

    @Test
    public void deleteUser() {
        // Arrange
        client = setupClient(builder, "deleteUserUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        client.deleteUser(communicationUser);
    }

    @Test
    public void deleteUserWithResponse() {
        // Arrange
        client = setupClient(builder, "deleteUserWithResponseUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        Response<Void> response = client.deleteUserWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @Test
    public void revokeToken() {
        // Arrange
        client = setupClient(builder, "revokeTokenUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);
        AccessToken issuedToken = client.getToken(communicationUser, SCOPES);
        verifyTokenNotEmpty(issuedToken);

        // Action & Assert
        client.revokeTokens(communicationUser);
    }

    @Test
    public void revokeTokenWithResponse() {
        // Arrange
        client = setupClient(builder, "revokeTokenWithResponseUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);
        AccessToken issuedToken = client.getToken(communicationUser, SCOPES);
        verifyTokenNotEmpty(issuedToken);

        // Action & Assert
        Response<Void> response = client.revokeTokensWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUser(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        client = setupClient(builder, "getTokenForTeamsUserUsingManagedIdentitySync");

        // Action & Assert
        AccessToken issuedToken = client.getTokenForTeamsUser(options);
        verifyTokenNotEmpty(issuedToken);
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithResponse(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        client = setupClient(builder, "getTokenForTeamsUserWithResponseUsingManagedIdentitySync");

        // Action & Assert
        Response<AccessToken> response = client.getTokenForTeamsUserWithResponse(options, Context.NONE);
        assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(response.getValue());
    }
}
