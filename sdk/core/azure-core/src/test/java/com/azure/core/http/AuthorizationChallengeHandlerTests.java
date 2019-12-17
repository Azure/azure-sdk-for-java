// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http;

import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link AuthorizationChallengeHandler}.
 */
public class AuthorizationChallengeHandlerTests {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Test
    public void basicChallenge() {
        String authorizationValue = Base64.getEncoder().encodeToString(String.format("%s:%s", USERNAME, PASSWORD)
            .getBytes(StandardCharsets.UTF_8));
        String expected = "Basic " + authorizationValue;

        assertEquals(expected, new AuthorizationChallengeHandler(USERNAME, PASSWORD).handleBasic());
    }

    @ParameterizedTest
    @MethodSource("digestChallengeSupplier")
    public void digestChallenge(String method, String uri, List<HttpHeaders> challenges, String expected) {
        assertEquals(expected, new AuthorizationChallengeHandler(USERNAME, PASSWORD)
            .handleDigest(method, uri, challenges));
    }

    private static Stream<Arguments> digestChallengeSupplier() {
        String realm = "test@example.com";

        return null;
    }

    private static String createChallenge(String realm, String qop, String algorithm, String nonce, String opaque,
        String userhash) {
        StringBuilder challengeBuilder = new StringBuilder("WWW-Authenticate: Digest ");
        challengeBuilder.append("realm=\"").append(realm).append("\", ");

        if (!CoreUtils.isNullOrEmpty(qop)) {
            challengeBuilder.append("qop=\"").append(qop).append("\", ");
        }

        if (!CoreUtils.isNullOrEmpty(algorithm)) {
            challengeBuilder.append("algorithm=").append(algorithm).append(", ");
        }

        challengeBuilder.append("nonce=\"").append(nonce).append("\", ");
        challengeBuilder.append("opaque=\"").append(opaque).append("\"");

        if (!CoreUtils.isNullOrEmpty(userhash)) {
            challengeBuilder.append(", userhash=").append(userhash);
        }

        return challengeBuilder.toString();
    }

    private static byte[] calculateH1(MessageDigest digest, String username, String password, String realm) {
        return digest.digest(String.format("%s:%s:%s", username, realm, password).getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] calculateSessH1(MessageDigest digest, String username, String password, String realm,
        String nonce, String cnonce) {
        byte[] userAndPassHash = digest.digest(String.format("%s:%s:%s", username, realm, password)
            .getBytes(StandardCharsets.UTF_8));
        byte[] completeHashSource = mergeArrays(userAndPassHash, String.format(":%s:%s", nonce, cnonce)
            .getBytes(StandardCharsets.UTF_8));
        return digest.digest(completeHashSource);
    }

    private static byte[] mergeArrays(byte[] firstArray, byte[] secondArray) {
        byte[] mergedArray = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, mergedArray, 0, firstArray.length);
        System.arraycopy(secondArray, 0, mergedArray, firstArray.length, secondArray.length);

        return mergedArray;
    }
}
