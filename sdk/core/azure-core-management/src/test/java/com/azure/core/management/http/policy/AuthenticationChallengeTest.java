// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.http.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.implementation.http.AuthenticationChallenge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthenticationChallengeTest {
    private static final String DUMMY_SCOPE = "DUMMY_SCOPE";
    private static final String CAE_INSUFFICIENT_CLAIMS_CHALLENGE = "Bearer realm=\"\""
        + ", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\","
        + " client_id=\"00000003-0000-0000-c000-000000000000\", error=\"insufficient_claims\","
        + " claims=\"eyJhY2Nlc3NfdG9rZW4iOiB7ImZvbyI6ICJiYXIifX0=\"";

    private static final String CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE = "Bearer authorization_uri="
        + "\"https://login.windows-ppe.net/\", error=\"invalid_token\","
        + " error_description=\"User session has been revoked\","
        + " claims=\"eyJhY2Nlc3NfdG9rZW4iOnsibmJmIjp7ImVzc2VudGlhbCI6dHJ1ZSwgInZhbHVlIjoiMTYwMzc0MjgwMCJ9fX0=\"";

    private static final String KEY_VAULT_CHALLENGE = "Bearer authorization="
        + "\"https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47\","
        + " resource=\"https://vault.azure.net\"";

    private static final String ARM_CHALLENGE = "Bearer authorization_uri="
        + "\"https://login.windows.net/\", error=\"invalid_token\","
        + " error_description=\"The authentication failed because of missing 'Authorization' header.\"";


    private static final Map<String, String> EXPECTED_CAE_INSUFFICIENT_CLAIMS_CHALLENGE = new HashMap<>();
    private static final Map<String, String> EXPECTED_CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE = new HashMap<>();
    private static final Map<String, String> EXPECTED_KEY_VAULT_CHALLENGE = new HashMap<>();
    private static final Map<String, String> EXPECTED_ARM_CHALLENGE = new HashMap<>();
    private static final Map<String, Map<String, String>> AUTHENTICATION_CHALLENGE_MAP = new HashMap<>();
    static {
        EXPECTED_CAE_INSUFFICIENT_CLAIMS_CHALLENGE.put("realm", "");
        EXPECTED_CAE_INSUFFICIENT_CLAIMS_CHALLENGE.put("authorization_uri",
            "https://login.microsoftonline.com/common/oauth2/authorize");
        EXPECTED_CAE_INSUFFICIENT_CLAIMS_CHALLENGE.put("client_id", "00000003-0000-0000-c000-000000000000");
        EXPECTED_CAE_INSUFFICIENT_CLAIMS_CHALLENGE.put("error", "insufficient_claims");
        EXPECTED_CAE_INSUFFICIENT_CLAIMS_CHALLENGE.put("claims", "eyJhY2Nlc3NfdG9rZW4iOiB7ImZvbyI6ICJiYXIifX0=");


        EXPECTED_CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE.put("authorization_uri", "https://login.windows-ppe.net/");
        EXPECTED_CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE.put("error", "invalid_token");
        EXPECTED_CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE.put("error_description", "User session has been revoked");
        EXPECTED_CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE.put("claims",
            "eyJhY2Nlc3NfdG9rZW4iOnsibmJmIjp7ImVzc2VudGlhbCI6dHJ1ZSwgInZhbHVlIjoiMTYwMzc0MjgwMCJ9fX0=");

        EXPECTED_KEY_VAULT_CHALLENGE.put("authorization",
            "https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47");
        EXPECTED_KEY_VAULT_CHALLENGE.put("resource", "https://vault.azure.net");

        EXPECTED_ARM_CHALLENGE.put("authorization_uri", "https://login.windows.net/");
        EXPECTED_ARM_CHALLENGE.put("error", "invalid_token");
        EXPECTED_ARM_CHALLENGE.put("error_description",
            "The authentication failed because of missing 'Authorization' header.");

        AUTHENTICATION_CHALLENGE_MAP.put(CAE_INSUFFICIENT_CLAIMS_CHALLENGE,
            EXPECTED_CAE_INSUFFICIENT_CLAIMS_CHALLENGE);
        AUTHENTICATION_CHALLENGE_MAP.put(CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE,
            EXPECTED_CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE);
        AUTHENTICATION_CHALLENGE_MAP.put(KEY_VAULT_CHALLENGE, EXPECTED_KEY_VAULT_CHALLENGE);
        AUTHENTICATION_CHALLENGE_MAP.put(ARM_CHALLENGE, EXPECTED_ARM_CHALLENGE);
    }

    private final TokenCredential mockCredential = request -> null;

    @Test
    public void bearerTokenAuthenticationChallengeParsingTest() {
        // Create custom Headers
        ArmChallengeAuthenticationPolicy armChallengeAuthenticationPolicy =
            new ArmChallengeAuthenticationPolicy(mockCredential, DUMMY_SCOPE);

        for (String authChallenge : AUTHENTICATION_CHALLENGE_MAP.keySet()) {
            List<AuthenticationChallenge> authenticationChallenges = armChallengeAuthenticationPolicy
                .parseChallenges(authChallenge);
            Assertions.assertEquals(1, authenticationChallenges.size());

            Map<String, String> parsedChallengeParams =  armChallengeAuthenticationPolicy
                 .parseChallengeParams(authenticationChallenges.get(0).getChallengeParameters());

            Assertions.assertEquals(AUTHENTICATION_CHALLENGE_MAP.get(authChallenge), parsedChallengeParams);
        }
    }
}
