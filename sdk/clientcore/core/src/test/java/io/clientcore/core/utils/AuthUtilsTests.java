// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link AuthUtils}.
 */
public class AuthUtilsTests {
    @ParameterizedTest
    @MethodSource("validParseAuthenticateHeaderSupplier")
    @Execution(ExecutionMode.SAME_THREAD)
    public void validParseAuthenticateHeader(String authenticationHeader, List<AuthenticateChallenge> expected) {
        List<AuthenticateChallenge> actual = AuthUtils.parseAuthenticateHeader(authenticationHeader);

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getScheme(), actual.get(i).getScheme());
            assertEquals(expected.get(i).getParameters(), actual.get(i).getParameters());
            assertEquals(expected.get(i).getToken68(), actual.get(i).getToken68());
        }
    }

    private static Stream<Arguments> validParseAuthenticateHeaderSupplier() {
        Map<String, String> digestParams = new LinkedHashMap<>();
        digestParams.put("nonce", "123");
        digestParams.put("opaque", "123");
        digestParams.put("qop", "123");
        digestParams.put("algorithm", "SHA-256");

        return Stream.of(Arguments.of(null, emptyList()), Arguments.of("", emptyList()),
            Arguments.of("Basic", singletonList(new AuthenticateChallenge("Basic"))),
            Arguments.of("Basic realm=\"test\"",
                singletonList(new AuthenticateChallenge("Basic", singletonMap("realm", "test")))),
            Arguments.of("Custom ABkd856gkslw-._~+/=",
                singletonList(new AuthenticateChallenge("Custom", "ABkd856gkslw-._~+/="))),
            Arguments.of("Digest nonce = \"123\", opaque=\"123\", qop=\"123\", algorithm=SHA-256",
                singletonList(new AuthenticateChallenge("Digest", digestParams))),
            Arguments.of("Digest nonce = \"123\", opaque=\"123\", qop=\"123\", algorithm=SHA-256, Basic realm=\"test\"",
                Arrays.asList(new AuthenticateChallenge("Digest", digestParams),
                    new AuthenticateChallenge("Basic", singletonMap("realm", "test")))),
            Arguments.of("Basic realm=\"test\", Basic realm=\"test2\"",
                Arrays.asList(new AuthenticateChallenge("Basic", singletonMap("realm", "test")),
                    new AuthenticateChallenge("Basic", singletonMap("realm", "test2")))),
            Arguments.of("  , ,, ,, Basic realm=\"test\"",
                singletonList(new AuthenticateChallenge("Basic", singletonMap("realm", "test")))),
            Arguments.of("Custom1,Custom2",
                Arrays.asList(new AuthenticateChallenge("Custom1"), new AuthenticateChallenge("Custom2"))));
    }

    @ParameterizedTest
    @MethodSource("invalidParseAuthenticateHeaderSupplier")
    @Execution(ExecutionMode.SAME_THREAD)
    public void invalidParseAuthenticateHeader(String authenticationHeader) {
        assertThrows(IllegalArgumentException.class, () -> AuthUtils.parseAuthenticateHeader(authenticationHeader));
    }

    private static Stream<String> invalidParseAuthenticateHeaderSupplier() {
        return Stream.of("ABkd856gkslw-._~+/=", // token68 without scheme
            "realm=\"test\"", // auth-param without scheme
            "Custom ABkd856gkslw-._~+/, ABkd856gkslw-._~+/", // multiple token68s
            "Custom ABkd856gkslw-._~+/, realm=\"test\"", // token68 and auth-param
            "Custom realm=\"test\", ABkd856gkslw-._~+/", // auth-param and token68
            "Custom/", // scheme with invalid character
            "Custom ABkd856gkslw-._~+/!", // token68 with invalid character
            "Custom realm=test/", // auth-param with invalid character
            "Custom realm=test realm2=test2", // missing comma between auth-params
            "Custom realm=\"", // missing closing quote for auth-param
            "Custom realm=123, realm=456", // duplicate auth-param
            "realm=123" // missing scheme
        );
    }
}
