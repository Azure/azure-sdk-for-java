// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.identity.models.GetTokenForTeamsUserOptions;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IPublicClientApplication;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import org.junit.jupiter.params.provider.Arguments;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class CteTestHelper {

    private static final TestMode TEST_MODE = getTestMode();
    private static final String COMMUNICATION_M365_APP_ID = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_M365_APP_ID", "Sanitized");
    private static final String COMMUNICATION_M365_AAD_AUTHORITY = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_M365_AAD_AUTHORITY", "Sanitized");
    private static final String COMMUNICATION_M365_AAD_TENANT = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_M365_AAD_TENANT", "Sanitized");
    private static final String COMMUNICATION_M365_REDIRECT_URI = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_M365_REDIRECT_URI", "Sanitized");
    private static final String COMMUNICATION_M365_SCOPE = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_M365_SCOPE", "Sanitized");
    private static final String COMMUNICATION_MSAL_USERNAME = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_MSAL_USERNAME", "Sanitized");
    private static final String COMMUNICATION_MSAL_PASSWORD = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_MSAL_PASSWORD", "Sanitized");
    private static final String COMMUNICATION_EXPIRED_TEAMS_TOKEN = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_EXPIRED_TEAMS_TOKEN", "Sanitized");
    private static final String COMMUNICATION_SKIP_INT_IDENTITY_EXCHANGE_TOKEN_TEST = Configuration.getGlobalConfiguration()
            .get("SKIP_INT_IDENTITY_EXCHANGE_TOKEN_TEST", "false");

    /**
     * Gets a test mode for unit tests.
     *
     * @return the test mode.
     */
    private static TestMode getTestMode() {
        ClientLogger logger = new ClientLogger(CommunicationIdentityClientTestBase.class);
        String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException var3) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        } else {
            logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", "AZURE_TEST_MODE");
            return TestMode.PLAYBACK;
        }
    }

    /**
     * Gets a CTE request parameters for unit tests.
     *
     * @return the Teams User AAD token.
     */
    private static GetTokenForTeamsUserOptions createTeamsUserExchangeTokenRequest() throws MalformedURLException, ExecutionException, InterruptedException {
        GetTokenForTeamsUserOptions options = new GetTokenForTeamsUserOptions("Sanitized", "Sanitized", "Sanitized");
        if (TEST_MODE != TestMode.PLAYBACK) {
            try {
                IPublicClientApplication publicClientApplication = PublicClientApplication.builder(COMMUNICATION_M365_APP_ID)
                        .authority(COMMUNICATION_M365_AAD_AUTHORITY + "/" + COMMUNICATION_M365_AAD_TENANT)
                        .build();
                Set<String> scopes = Collections.singleton(COMMUNICATION_M365_SCOPE);
                char[] password = COMMUNICATION_MSAL_PASSWORD.toCharArray();
                UserNamePasswordParameters userNamePasswordParameters = UserNamePasswordParameters.builder(scopes, COMMUNICATION_MSAL_USERNAME, password)
                        .build();
                Arrays.fill(password, '0');
                IAuthenticationResult result = publicClientApplication.acquireToken(userNamePasswordParameters).get();
                String[] accountIds = result.account().homeAccountId().split("\\.");
                options.setTeamsUserAadToken(result.accessToken())
                        .setClientId(COMMUNICATION_M365_APP_ID)
                        .setUserObjectId(accountIds[0]);
            } catch (Exception e) {
                ClientLogger logger = new ClientLogger(CommunicationIdentityClientTestBase.class);
                logger.error("Could not generate Teams User AAD token, failed with '{}' ", e.getMessage());
                throw e;
            }
        }
        return options;
    }

    static boolean skipExchangeAadTeamsTokenTest() {
        return Boolean.parseBoolean(COMMUNICATION_SKIP_INT_IDENTITY_EXCHANGE_TOKEN_TEST);
    }

    /**
     * Generates various null parameters for testing getTokenForTeamsUser() method.
     *
     * @return A stream of options to parameterized test.
     */
    static Stream<Arguments> getNullParams() {
        List<Arguments> argumentsList = new ArrayList<>();
        argumentsList.add(Arguments.of(new GetTokenForTeamsUserOptions(null, null, null), "token"));
        try {
            GetTokenForTeamsUserOptions options = createTeamsUserExchangeTokenRequest();
            argumentsList.add(Arguments.of(new GetTokenForTeamsUserOptions(null, options.getClientId(), options.getUserObjectId()), "token"));
            argumentsList.add(Arguments.of(new GetTokenForTeamsUserOptions(options.getTeamsUserAadToken(), null, options.getUserObjectId()), "AppId"));
            argumentsList.add(Arguments.of(new GetTokenForTeamsUserOptions(options.getTeamsUserAadToken(), options.getClientId(), null), "UserId"));
        } catch (Exception e) {
        }
        return argumentsList.stream();
    }

    /**
     * Generates various invalid token types for testing getTokenForTeamsUser() method.
     *
     * @return A stream of options to parameterized test.
     */
    static Stream<Arguments> getInvalidTokens() {
        List<Arguments> argumentsList = new ArrayList<>();
        GetTokenForTeamsUserOptions options = new GetTokenForTeamsUserOptions("", COMMUNICATION_M365_APP_ID, "");
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithEmptyToken", options));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithInvalidToken", options.setTeamsUserAadToken("invalid")));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithExpiredToken", options.setTeamsUserAadToken(COMMUNICATION_EXPIRED_TEAMS_TOKEN)));
        return argumentsList.stream();
    }

    /**
     * Generates various invalid appId types for testing getTokenForTeamsUser() method.
     *
     * @return A stream of options to parameterized test.
     */
    static Stream<Arguments> getInvalidAppIds() throws Exception {
        List<Arguments> argumentsList = new ArrayList<>();
        GetTokenForTeamsUserOptions options = createTeamsUserExchangeTokenRequest();
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithEmptyAppId", options.setClientId("")));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithInvalidAppId", options.setClientId("invalid")));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithWrongAppId", options.setClientId(options.getUserObjectId())));
        return argumentsList.stream();
    }

    /**
     * Generates various invalid userId types for testing getTokenForTeamsUser() method.
     *
     * @return A stream of options to parameterized test.
     */
    static Stream<Arguments> getInvalidUserIds() throws Exception {
        List<Arguments> argumentsList = new ArrayList<>();
        GetTokenForTeamsUserOptions options = createTeamsUserExchangeTokenRequest();
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithEmptyUserId", options.setUserObjectId("")));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithInvalidUserId", options.setUserObjectId("invalid")));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithWrongUserId", options.setUserObjectId(options.getClientId())));
        return argumentsList.stream();
    }

    /**
     * Generates valid parameters for testing getTokenForTeamsUserWithValidParams() method.
     *
     * @return A stream of options to parameterized test.
     */
    static Stream<Arguments> getValidParams() throws Exception {
        List<Arguments> argumentsList = new ArrayList<>();
        GetTokenForTeamsUserOptions options = createTeamsUserExchangeTokenRequest();
        argumentsList.add(Arguments.of(options));
        return argumentsList.stream();
    }

}
