// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests {@link AuthorizationChallengeHandler}.
 */
public class AuthorizationChallengeHandlerTests {
    private static final String REALM = "realm";
    private static final String QOP = "qop";
    private static final String ALGORITHM = "algorithm";
    private static final String NONCE = "nonce";
    private static final String NEXT_NONCE = "nextnonce";
    private static final String OPAQUE = "opaque";
    private static final String USERHASH = "userhash";

    private static final String RESPONSE = "response";
    private static final String USERNAME = "username";

    private static final String DEFAULT_USERNAME = "Mufasa";
    private static final String DEFAULT_PASSWORD = "Circle Of Life";
    private static final String EXPECTED_BASIC = "Basic " + Base64.getEncoder()
        .encodeToString(String.format("%s:%s", DEFAULT_USERNAME, DEFAULT_PASSWORD).getBytes(StandardCharsets.UTF_8));

    private AutoCloseable openMocks;

    @BeforeEach
    public void prepareForTest() {
        this.openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void clearMocks() throws Exception {
        openMocks.close();
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} is able to handle Basic authentication challenges.
     */
    @Test
    public void handleBasic() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        assertEquals(EXPECTED_BASIC, challengeHandler.handleBasic());
    }

    /**
     * Test that {@link AuthorizationChallengeHandler} is able to preemptively handle Basic authorization challenges
     * once it has been used to handle a Basic challenge.
     */
    @Test
    public void pipelineBasic() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        assertEquals(EXPECTED_BASIC, challengeHandler.handleBasic());
        assertEquals(EXPECTED_BASIC, challengeHandler.attemptToPipelineAuthorization(null, null, null));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} will return {@code null} for an Authorization header when
     * attempting to pipeline a request when the handler has never handled a request.
     */
    @Test
    public void pipelineBasicWithoutInitialHandleFails() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        assertNull(challengeHandler.attemptToPipelineAuthorization(null, null, null));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} creates the expected {@code response} value when using
     * {@code MD5} as the authentication challenge algorithm.
     */
    @Test
    public void md5DigestAuthorization() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "6629fae49393a05397450978507c4ef1";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";

        Map<String, String> challenge = createChallenge("testrealm@host.com", "auth", "MD5",
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null);

        when(challengeHandler.generateNonce()).thenReturn("0a4f113b");

        String authorizationHeader = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> new byte[0]);

        assertNotNull(authorizationHeader);
        assertEquals(expectedResponse, extractValue(authorizationHeader, RESPONSE));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} is able to preemptively handle Digest authorization challenges
     * once it has been used to handle a Digest challenge.
     */
    @Test
    public void pipelineDigest() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "6629fae49393a05397450978507c4ef1";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";

        Map<String, String> challenge = createChallenge("testrealm@host.com", "auth", "MD5",
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null);

        when(challengeHandler.generateNonce()).thenReturn("0a4f113b");

        String authorizationHeader = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> new byte[0]);

        assertNotNull(authorizationHeader);
        assertEquals(expectedResponse, extractValue(authorizationHeader, RESPONSE));

        authorizationHeader = challengeHandler.attemptToPipelineAuthorization(method, uri, () -> new byte[0]);

        assertNotNull(authorizationHeader);
        assertEquals(2, Integer.parseInt(extractValue(authorizationHeader, "nc")));

        // The pipelined response alters due to a change in the nonce count.
        String expectedPipelineResponse = "15b6bb427e3fecd23a43cb702ce447d5";
        assertEquals(expectedPipelineResponse, extractValue(authorizationHeader, RESPONSE));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} will return {@code null} for an Authorization header when
     * attempting to pipeline a request when the handler has never handled a request.
     */
    @Test
    public void pipelineDigestWithoutInitialHandleFails() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        assertNull(challengeHandler.attemptToPipelineAuthorization(null, null, null));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} is able to handle an authentication challenge that uses SHA-256
     * as the challenge algorithm.
     */
    @Test
    public void sha256DigestAuthorization() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, "Circle of Life");

        String expectedResponse = "753927fa0e85d155564e2e272a28d1802ca10daf4496794697cf8db5856cb6c1";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";

        Map<String, String> challenge = createChallenge("http-auth@example.org", "auth", "SHA-256",
            "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v", "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS", null);

        when(challengeHandler.generateNonce()).thenReturn("f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ");

        String authorizationHandler = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> new byte[0]);

        assertNotNull(authorizationHandler);
        assertEquals(expectedResponse, extractValue(authorizationHandler, RESPONSE));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} prefers more secure algorithms, {@code SHA-256} over
     * {@code MD5}, when presented with multiple authentication challenges.
     */
    @Test
    public void preferSha256OverMd5DigestAuthorization() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, "Circle of Life");

        String expectedResponse = "753927fa0e85d155564e2e272a28d1802ca10daf4496794697cf8db5856cb6c1";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";

        Map<String, String> md5Challenge = createChallenge("http-auth@example.org", "auth", "MD5",
            "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v", "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS", null);
        Map<String, String> sha256Challenge = createChallenge("http-auth@example.org", "auth", "SHA-256",
            "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v", "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS", null);

        when(challengeHandler.generateNonce()).thenReturn("f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ");

        String authorizationHandler = challengeHandler.handleDigest(method, uri,
            Arrays.asList(md5Challenge, sha256Challenge), () -> new byte[0]);

        assertNotNull(authorizationHandler);
        assertEquals(expectedResponse, extractValue(authorizationHandler, RESPONSE));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} will default {@code MD5} as the authentication challenge
     * algorithm when no value is given.
     */
    @Test
    public void digestAuthorizationDefaultAlgorithmIsMd5() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "6629fae49393a05397450978507c4ef1";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";

        Map<String, String> challenge = createChallenge("testrealm@host.com", "auth", null,
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null);

        when(challengeHandler.generateNonce()).thenReturn("0a4f113b");

        String authorizationHeader = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> new byte[0]);

        assertNotNull(authorizationHeader);
        assertEquals(expectedResponse, extractValue(authorizationHeader, RESPONSE));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} will generate a hash of the username if the challenge contains
     * {@code userhash=true}.
     */
    @Test
    public void userHashDigestAuthorization() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler("J\u00e4s\u00f8n Doe",
            "Secret, or not?");

        String expectedResponse = "ae66e67d6b427bd3f120414a82e4acff38e8ecd9101d6c861229025f607a79dd";
        String expectedUsername = "488869477bf257147b804c45308cd62ac4e25eb717b12b298c79e62dcea254ec";
        String method = HttpMethod.GET.toString();
        String uri = "/doe.json";

        Map<String, String> challenge = createChallenge("api@example.org", "auth", "SHA-512-256",
            "5TsQWLVdgBdmrQ0XsxbDODV+57QdFR34I9HAbC/RVvkK", "HRPCssKJSGjCrkzDg8OhwpzCiGPChXYjwrI2QmXDnsOS", true);

        when(challengeHandler.generateNonce()).thenReturn("NTg6RKcb9boFIAS3KrFK9BGeh+iDa/sm6jUMp2wds69v");

        String authorizationHeader = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> new byte[0]);

        assertNotNull(authorizationHeader);
        assertEquals(expectedUsername, extractValue(authorizationHeader, USERNAME));
        assertEquals(expectedResponse, extractValue(authorizationHeader, RESPONSE));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} produces the expected authorization response when {@code qop}
     * isn't passed or is unknown.
     */
    @Test
    public void unknownQop() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "670fd8c2df070c60b045671b8b24ff02";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";

        Map<String, String> challenge = createChallenge("testrealm@host.com", "unknownQop", "MD5",
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null);

        when(challengeHandler.generateNonce()).thenReturn("0a4f113b");

        String authorizationHeader = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> new byte[0]);

        assertNotNull(authorizationHeader);
        assertEquals(expectedResponse, extractValue(authorizationHeader, RESPONSE));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} produces the expected authorization response when {@code qop}
     * is {@code auth-int}.
     */
    @Test
    public void md5DigestWithAuthInt() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "f7b13069066cfdda58d5accbc02a6b98";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";

        Map<String, String> challenge = createChallenge("testrealm@host.com", "auth-int", "MD5",
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null);

        when(challengeHandler.generateNonce()).thenReturn("0a4f113b");

        String authorizationHeader = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> "Hello World!".getBytes(StandardCharsets.UTF_8));

        assertNotNull(authorizationHeader);
        assertEquals(expectedResponse, extractValue(authorizationHeader, RESPONSE));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} produces the expected authorization response when the algorithm
     * is a {@code -sess} based variant, such as {@code MD5-sess}.
     */
    @Test
    public void md5DigestWithSessAlgorithm() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "4726bc10c33fa6cb357eb27807b1cce8";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";

        Map<String, String> challenge = createChallenge("testrealm@host.com", "", "MD5-sess",
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null);

        when(challengeHandler.generateNonce()).thenReturn("0a4f113b");

        String authorizationHeader = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> new byte[0]);

        assertNotNull(authorizationHeader);
        assertEquals(expectedResponse, extractValue(authorizationHeader, RESPONSE));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} will return {@code null} when the challenge algorithm isn't
     * supported, such as {@code SHA3}.
     */
    @Test
    public void unsupportedAlgorithmReturnsNull() {
        List<Map<String, String>> challenges = Collections
            .singletonList(createChallenge("realm", "auth", "SHA3", "nonce", "opaque", null));

        assertNull(new AuthorizationChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD)
            .handleDigest(HttpMethod.GET.name(), "/get", challenges, () -> new byte[0]));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} will return {@code null} when the challenge algorithm doesn't
     * exist.
     */
    @Test
    public void unknownAlgorithmIsSkipped() {
        List<Map<String, String>> challenges = Collections
            .singletonList(createChallenge("realm", "auth", "SHA9000", "nonce", "opaque", null));

        assertNull(new AuthorizationChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD)
            .handleDigest(HttpMethod.GET.name(), "/get", challenges, () -> new byte[0]));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} correctly consumes the {@code Authentication-Info} response
     * header.
     */
    @Test
    public void consumeAuthenticationInfoHeader() {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String method = HttpMethod.GET.name();
        String uri = "/dir/index.html";
        String nonce = "dcd98b7102dd2f0e8b11d0f600bfb0c093";

        Map<String, String> challenge = createChallenge("realm", "auth", "MD5", nonce, "opaque", null);

        String authorizationHeader = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> new byte[0]);

        assertNotNull(authorizationHeader);

        String nextNonce = "5ccc069c403ebaf9f0171e9517f40e41";
        challengeHandler.consumeAuthenticationInfoHeader(Collections.singletonMap(NEXT_NONCE, nextNonce));

        authorizationHeader = challengeHandler.attemptToPipelineAuthorization(method, uri, () -> new byte[0]);

        assertNotNull(authorizationHeader);
        assertEquals(nextNonce, extractValue(authorizationHeader, NONCE));
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler} correctly ignored the {@code Authentication-Info} response when
     * it is {@code null} or doesn't have a value.
     */
    @ParameterizedTest
    @MethodSource("nullOrEmptyAuthenticationInfoHeadersSupplier")
    public void consumingNullOrEmptyAuthenticationInfoHeadersDoesNotUpdate(Map<String, String> authenticationInfo) {
        AuthorizationChallengeHandler challengeHandler = prepareChallengeHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "6629fae49393a05397450978507c4ef1";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";
        String nonce = "dcd98b7102dd2f0e8b11d0f600bfb0c093";

        Map<String, String> challenge = createChallenge("testrealm@host.com", "auth", "MD5", nonce,
            "5ccc069c403ebaf9f0171e9517f40e41", null);

        when(challengeHandler.generateNonce()).thenReturn("0a4f113b");

        String authorizationHeader = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> new byte[0]);

        assertNotNull(authorizationHeader);
        assertEquals(expectedResponse, extractValue(authorizationHeader, RESPONSE));

        challengeHandler.consumeAuthenticationInfoHeader(authenticationInfo);

        authorizationHeader = challengeHandler.handleDigest(method, uri, Collections.singletonList(challenge),
            () -> new byte[0]);

        assertNotNull(authorizationHeader);
        assertEquals(nonce, extractValue(authorizationHeader, NONCE));
    }

    private static Stream<Arguments> nullOrEmptyAuthenticationInfoHeadersSupplier() {
        return Stream.of(
            Arguments.of((Map<String, String>) null),
            Arguments.of(Collections.emptyMap())
        );
    }

    /**
     * Tests that {@link AuthorizationChallengeHandler#parseAuthenticationOrAuthorizationHeader(String)} correctly
     * parses values in an {@code Authenticate}, {@code Authorization}, or {@code Authentication-Info} header.
     */
    @ParameterizedTest
    @MethodSource("parseAuthenticationOrAuthorizationHeaderSupplier")
    public void parseAuthenticationOrAuthorizationHeader(String header, int expectedSize,
        Map<String, String> expectedMap) {
        Map<String, String> parsedMap = AuthorizationChallengeHandler
            .parseAuthenticationOrAuthorizationHeader(header);

        assertEquals(expectedSize, parsedMap.size());
        assertEquals(expectedMap, parsedMap);
    }

    private static Stream<Arguments> parseAuthenticationOrAuthorizationHeaderSupplier() {
        String nextNonce = "abf7395deb";
        Map<String, String> expectedMultiMap = new HashMap<>();
        expectedMultiMap.put("nc", "00000001");
        expectedMultiMap.put(NEXT_NONCE, nextNonce);

        return Stream.of(
            // Tests that a null header will produce an empty map.
            Arguments.of(null, 0, Collections.emptyMap()),

            // Tests that an empty header will produce an empty map.
            Arguments.of("", 0, Collections.emptyMap()),

            // Tests that a header with quoted strings will produce a key-value pair without the quotes.
            Arguments.of("nextnonce=\"" + nextNonce + "\"", 1, Collections.singletonMap(NEXT_NONCE, nextNonce)),

            // Tests that a header will multiple key-value pairs will parse them into a full map.
            Arguments.of("nc=00000001, nextnonce=\"" + nextNonce + "\"", 2, expectedMultiMap)
        );
    }

    private static AuthorizationChallengeHandler prepareChallengeHandler(String username, String password) {
        return spy(new AuthorizationChallengeHandler(username, password));
    }

    private static Map<String, String> createChallenge(String realm, String qop, String algorithm, String nonce,
        String opaque, Boolean userhash) {
        Map<String, String> challenge = new HashMap<>();

        challenge.put(REALM, realm);
        challenge.put(QOP, qop);
        challenge.put(NONCE, nonce);
        challenge.put(OPAQUE, opaque);

        if (algorithm != null) {
            challenge.put(ALGORITHM, algorithm);
        }

        if (userhash != null) {
            challenge.put(USERHASH, String.valueOf(userhash));
        }

        return challenge;
    }

    private static String extractValue(String authorizationHeader, String valueKey) {
        // The authorization header will be "Digest <authorization info>", this removes the digest portion.
        String authorizationInfo = authorizationHeader.split(" ", 2)[1];

        return Stream.of(authorizationInfo.split(","))
            .map(String::trim) // Cleanup any leading or trailing whitespaces.
            .map(info -> info.split("=", 2)) // Split the info into its key-value pair.
            .filter(kvp -> kvp[0].equalsIgnoreCase(valueKey)) // Select the value we are looking for.
            .map(kvp -> kvp[1].replace("\"", "")) // Unquote the value.
            .findFirst()
            .get();
    }
}
