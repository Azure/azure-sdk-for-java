// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.identity.implementation.models.TeamsUserExchangeTokenRequest;
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
    private static TeamsUserExchangeTokenRequest createTeamsUserExchangeTokenRequest() throws MalformedURLException, ExecutionException, InterruptedException {
        TeamsUserExchangeTokenRequest cteParams = new TeamsUserExchangeTokenRequest();
        cteParams.setToken("Sanitized");
        cteParams.setAppId("Sanitized");
        cteParams.setUserId("Sanitized");
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
                cteParams.setToken(result.accessToken());
                cteParams.setAppId(COMMUNICATION_M365_APP_ID);
                String[] accountIds = result.account().homeAccountId().split("\\.");
                cteParams.setUserId(accountIds[0]);
            } catch (Exception e) {
                ClientLogger logger = new ClientLogger(CommunicationIdentityClientTestBase.class);
                logger.error("Could not generate Teams User AAD token, failed with '{}' ", e.getMessage());
                throw e;
            }
        }
        return cteParams;
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
        argumentsList.add(Arguments.of(null, null, null, "token"));
        try {
            TeamsUserExchangeTokenRequest cteParams = createTeamsUserExchangeTokenRequest();
            argumentsList.add(Arguments.of(null, cteParams.getAppId(), cteParams.getUserId(), "token"));
            argumentsList.add(Arguments.of(cteParams.getToken(), null, cteParams.getUserId(), "AppId"));
            argumentsList.add(Arguments.of(cteParams.getToken(), cteParams.getAppId(), null, "UserId"));
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
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithEmptyToken", "", COMMUNICATION_M365_APP_ID, ""));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithInvalidToken", "invalid", COMMUNICATION_M365_APP_ID, ""));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithExpiredToken", COMMUNICATION_EXPIRED_TEAMS_TOKEN, COMMUNICATION_M365_APP_ID, ""));
        return argumentsList.stream();
    }

    /**
     * Generates various invalid appId types for testing getTokenForTeamsUser() method.
     *
     * @return A stream of options to parameterized test.
     */
    static Stream<Arguments> getInvalidAppIds() throws Exception {
        List<Arguments> argumentsList = new ArrayList<>();
        TeamsUserExchangeTokenRequest cteParams = createTeamsUserExchangeTokenRequest();
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithEmptyAppId", cteParams.getToken(), "", cteParams.getUserId()));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithInvalidAppId", cteParams.getToken(), "invalid", cteParams.getUserId()));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithWrongAppId", cteParams.getToken(), cteParams.getUserId(), cteParams.getUserId()));
        return argumentsList.stream();
    }

    /**
     * Generates various invalid userId types for testing getTokenForTeamsUser() method.
     *
     * @return A stream of options to parameterized test.
     */
    static Stream<Arguments> getInvalidUserIds() throws Exception {
        List<Arguments> argumentsList = new ArrayList<>();
        TeamsUserExchangeTokenRequest cteParams = createTeamsUserExchangeTokenRequest();
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithEmptyUserId", cteParams.getToken(), cteParams.getAppId(), ""));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithInvalidUserId", cteParams.getToken(), cteParams.getAppId(), "invalid"));
        argumentsList.add(Arguments.of("getTokenForTeamsUserWithWrongUserId", cteParams.getToken(), cteParams.getAppId(), cteParams.getAppId()));
        return argumentsList.stream();
    }

    /**
     * Generates valid parameters for testing getTokenForTeamsUserWithValidParams() method.
     *
     * @return A stream of options to parameterized test.
     */
    static Stream<Arguments> getValidParams() throws Exception {
        List<Arguments> argumentsList = new ArrayList<>();
        TeamsUserExchangeTokenRequest cteParams = createTeamsUserExchangeTokenRequest();
        argumentsList.add(Arguments.of(cteParams.getToken(), cteParams.getAppId(), cteParams.getUserId()));
        return argumentsList.stream();
    }

}
