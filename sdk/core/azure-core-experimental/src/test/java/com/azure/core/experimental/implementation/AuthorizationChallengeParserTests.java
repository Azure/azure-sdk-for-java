// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthorizationChallengeParserTests {
    private static final String CAE_INSUFFICIENT_CLAIMS_CHALLENGE = "Bearer realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", client_id=\"00000003-0000-0000-c000-000000000000\", error=\"insufficient_claims\", claims=\"eyJhY2Nlc3NfdG9rZW4iOiB7ImZvbyI6ICJiYXIifX0=\"";
    private static final String POP_CHALLENGE = "PoP realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", client_id=\"00000003-0000-0000-c000-000000000000\", nonce=\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjY3NDYyMDhENkQ5RTgyMkI3NzIxNjcyODg3MTg5OUIwREMzRTU4M0MifQ.eyJ0cyI6MTY5OTkwMDY5OH0.nvO9sU5EY5rQW_b1mElzUKflSKA_sWPPeeGzLAhRPdp9fcxz3HJGJbySvRgMhJCJDKxbveNBG7XrDh-jgKFggw32pAB6N7dCFQcs3Eyh8TEQoj2S303pvk1Hajw0YCcJRH_7GdqNdxyPk8UTip9vkEyOjXRj8YvYO2O8_CKcMJb7-PCaNDh9JBDVAysV8bhZS3wvUw4G--Mi1DZkaFn12kGSm_0odK1ROp11s0U2-5PW7M5gyRL9mU5EX96L9aiICseCipolm1e2tlmy_YpLOGS5oTy2qKukWiZv9cDylrgerbt8tOlO4VETH5hGZC6wken4MM2oTEIwOBZtaXIirg\", Bearer realm=\"\", authorization_uri=\"https://login.microsoftonline.com/common/oauth2/authorize\", client_id=\"00000003-0000-0000-c000-000000000001\"";

    @Test
    public void validatePopAndBearerChallengeParsing() {
        Assertions.assertEquals("eyJhY2Nlc3NfdG9rZW4iOiB7ImZvbyI6ICJiYXIifX0=", AuthorizationChallengeParser.getChallengeParameter(CAE_INSUFFICIENT_CLAIMS_CHALLENGE, "Bearer", "claims"));
        Assertions.assertTrue(AuthorizationChallengeParser.getChallengeParameter(POP_CHALLENGE, "PoP", "nonce").startsWith("eyJ0eXAiOiJKV1QiLCJhbGciOiJ"));
    }
}
