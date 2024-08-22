// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthorizationChallengeParserTests {

    private static final String CAE_INSUFFICIENT_CLAIMS_CHALLENGE
        = "Bearer realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", client_id=\"00000003-0000-0000-c000-000000000000\", error=\"insufficient_claims\", claims=\"eyJhY2Nlc3NfdG9rZW4iOiB7ImZvbyI6ICJiYXIifX0=\"";
    private static final Challenge PARSED_CAE_INSUFFICIENT_CLAIMS_CHALLENGE = new Challenge() {
        {
            setScheme("Bearer");
            addParameter("realm", "");
            addParameter("authorization_uri", "https://login.microsoftonline.com/common/oauth2/authorize");
            addParameter("client_id", "00000003-0000-0000-c000-000000000000");
            addParameter("error", "insufficient_claims");
            addParameter("claims", "eyJhY2Nlc3NfdG9rZW4iOiB7ImZvbyI6ICJiYXIifX0=");
        }
    };

    private static final String CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE
        = "Bearer authorization_uri=\"https://login.windows-ppe.net/\", error=\"invalid_token\", error_description=\"User session has been revoked\", claims=\"eyJhY2Nlc3NfdG9rZW4iOnsibmJmIjp7ImVzc2VudGlhbCI6dHJ1ZSwgInZhbHVlIjoiMTYwMzc0MjgwMCJ9fX0=\"";
    private static final Challenge PARSED_CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE = new Challenge() {
        {
            setScheme("Bearer");
            addParameter("authorization_uri", "https://login.windows-ppe.net/");
            addParameter("error", "invalid_token");
            addParameter("error_description", "User session has been revoked");
            addParameter("claims",
                "eyJhY2Nlc3NfdG9rZW4iOnsibmJmIjp7ImVzc2VudGlhbCI6dHJ1ZSwgInZhbHVlIjoiMTYwMzc0MjgwMCJ9fX0=");
        }
    };

    private static final String KEYVAULT_CHALLENGE
        = "Bearer authorization=\"https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47\", resource=\"https://vault.azure.net\"";
    private static final Challenge PARSED_KEYVAULT_CHALLENGE = new Challenge() {
        {
            setScheme("Bearer");
            addParameter("authorization", "https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47");
            addParameter("resource", "https://vault.azure.net");
        }
    };

    private static final String ARM_CHALLENGE
        = "Bearer authorization_uri=\"https://login.windows.net/\", error=\"invalid_token\", error_description=\"The authentication failed because of missing 'Authorization' header.\"";
    private static final Challenge PARSED_ARM_CHALLENGE = new Challenge() {
        {
            setScheme("Bearer");
            addParameter("authorization_uri", "https://login.windows.net/");
            addParameter("error", "invalid_token");
            addParameter("error_description", "The authentication failed because of missing 'Authorization' header.");
        }
    };

    private static final String STORAGE_CHALLENGE
        = "Bearer authorization_uri=https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/oauth2/authorize resource_id=https://storage.azure.com";
    private static final Challenge PARSED_STORAGE_CHALLENGE = new Challenge() {
        {
            setScheme("Bearer");
            addParameter("authorization_uri",
                "https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/oauth2/authorize");
            addParameter("resource_id", "https://storage.azure.com");
        }
    };

    private static final String POP_CHALLENGE
        = "PoP realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", client_id=\"00000003-0000-0000-c000-000000000000\", nonce=\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjY3NDYyMDhENkQ5RTgyMkI3NzIxNjcyODg3MTg5OUIwREMzRTU4M0MifQ.eyJ0cyI6MTY5OTkwMDY5OH0.nvO9sU5EY5rQW_b1mElzUKflSKA_sWPPeeGzLAhRPdp9fcxz3HJGJbySvRgMhJCJDKxbveNBG7XrDh-jgKFggw32pAB6N7dCFQcs3Eyh8TEQoj2S303pvk1Hajw0YCcJRH_7GdqNdxyPk8UTip9vkEyOjXRj8YvYO2O8_CKcMJb7-PCaNDh9JBDVAysV8bhZS3wvUw4G--Mi1DZkaFn12kGSm_0odK1ROp11s0U2-5PW7M5gyRL9mU5EX96L9aiICseCipolm1e2tlmy_YpLOGS5oTy2qKukWiZv9cDylrgerbt8tOlO4VETH5hGZC6wken4MM2oTEIwOBZtaXIirg\", Bearer realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", client_id=\"00000003-0000-0000-c000-000000000001\"";
    private static final Challenge[] PARSED_POP_CHALLENGES = { new Challenge() {
        {
            setScheme("PoP");
            addParameter("realm", "");
            addParameter("authorization_uri", "https://login.microsoftonline.com/common/oauth2/authorize");
            addParameter("client_id", "00000003-0000-0000-c000-000000000000");
            addParameter("nonce",
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjY3NDYyMDhENkQ5RTgyMkI3NzIxNjcyODg3MTg5OUIwREMzRTU4M0MifQ.eyJ0cyI6MTY5OTkwMDY5OH0.nvO9sU5EY5rQW_b1mElzUKflSKA_sWPPeeGzLAhRPdp9fcxz3HJGJbySvRgMhJCJDKxbveNBG7XrDh-jgKFggw32pAB6N7dCFQcs3Eyh8TEQoj2S303pvk1Hajw0YCcJRH_7GdqNdxyPk8UTip9vkEyOjXRj8YvYO2O8_CKcMJb7-PCaNDh9JBDVAysV8bhZS3wvUw4G--Mi1DZkaFn12kGSm_0odK1ROp11s0U2-5PW7M5gyRL9mU5EX96L9aiICseCipolm1e2tlmy_YpLOGS5oTy2qKukWiZv9cDylrgerbt8tOlO4VETH5hGZC6wken4MM2oTEIwOBZtaXIirg");
        }
    }, new Challenge() {
        {
            setScheme("Bearer");
            addParameter("realm", "");
            addParameter("authorization_uri", "https://login.microsoftonline.com/common/oauth2/authorize");
            addParameter("client_id", "00000003-0000-0000-c000-000000000001");
        }
    } };

    @Test
    public void validateChallengeParsing() {
        validateChallengeParsing(CAE_INSUFFICIENT_CLAIMS_CHALLENGE, PARSED_CAE_INSUFFICIENT_CLAIMS_CHALLENGE);
        validateChallengeParsing(CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE, PARSED_CAE_SESSIONS_REVOKED_CLAIMS_CHALLENGE);
        validateChallengeParsing(KEYVAULT_CHALLENGE, PARSED_KEYVAULT_CHALLENGE);
        validateChallengeParsing(ARM_CHALLENGE, PARSED_ARM_CHALLENGE);
        validateChallengeParsing(STORAGE_CHALLENGE, PARSED_STORAGE_CHALLENGE);
    }

    @Test
    public void validatePopAndBearerChallengeParsing() {
        String challenge = POP_CHALLENGE;

        List<Challenge> parsedChallenges = new ArrayList<>();

        String remainingHeaderValue = challenge.trim();
        while (!remainingHeaderValue.isEmpty()) {
            String scheme = AuthorizationChallengeParser.extractChallengeParameter(remainingHeaderValue, "", "");
            if (scheme == null) {
                break;
            }

            Challenge parsedChallenge = new Challenge();
            parsedChallenge.setScheme(scheme);

            String paramValue;
            while ((paramValue = AuthorizationChallengeParser.extractParameter(remainingHeaderValue, "")) != null) {
                String[] param = paramValue.split("=", 2);
                parsedChallenge.addParameter(param[0].trim(), param.length > 1 ? param[1].trim() : "");
            }

            parsedChallenges.add(parsedChallenge);

            int nextComma = remainingHeaderValue.indexOf(',');
            if (nextComma == -1) {
                break;
            }
            remainingHeaderValue = remainingHeaderValue.substring(nextComma + 1).trim();
        }

        assertEquals(2, parsedChallenges.size());
        validateParsedChallenge(PARSED_POP_CHALLENGES[0], parsedChallenges.get(0));
        validateParsedChallenge(PARSED_POP_CHALLENGES[1], parsedChallenges.get(1));
    }

    private void validateChallengeParsing(String challenge, Challenge expectedChallenge) {
        String remainingHeaderValue = challenge.trim();

        Challenge parsedChallenge = new Challenge();
        parsedChallenge.setScheme(AuthorizationChallengeParser.extractChallengeParameter(remainingHeaderValue, "", ""));

        String paramValue;
        while ((paramValue = AuthorizationChallengeParser.extractParameter(remainingHeaderValue, "")) != null) {
            String[] param = paramValue.split("=", 2);
            parsedChallenge.addParameter(param[0].trim(), param.length > 1 ? param[1].trim() : "");
        }

        validateParsedChallenge(expectedChallenge, parsedChallenge);
    }

    private void validateParsedChallenge(Challenge expected, Challenge actual) {
        assertEquals(expected.getScheme(), actual.getScheme());
        assertEquals(expected.getParameters().size(), actual.getParameters().size());

        for (int i = 0; i < expected.getParameters().size(); i++) {
            assertEquals(expected.getParameters().get(i).getName(), actual.getParameters().get(i).getName());
            assertEquals(expected.getParameters().get(i).getValue(), actual.getParameters().get(i).getValue());
        }
    }
}
