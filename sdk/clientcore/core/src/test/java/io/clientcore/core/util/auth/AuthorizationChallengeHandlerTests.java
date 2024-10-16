// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.implementation.util.UriBuilder;
import io.clientcore.core.implementation.util.auth.BasicHandler;
import io.clientcore.core.implementation.util.auth.DigestHandler;
import io.clientcore.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link ChallengeHandler}.
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
    private static final HttpRequest HTTP_REQUEST = new HttpRequest(HttpMethod.GET, createUri());
    private static final BinaryData ENTITY_BODY = BinaryData.fromBytes(new byte[0]);

    /**
     * Tests that {@link ChallengeHandler} is able to handle Basic authentication challenges.
     */
    @Test
    public void handleBasic() {
        BasicHandler basicHandler = new BasicHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);
        basicHandler.handleChallenge(HTTP_REQUEST, new HttpResponse<>(HTTP_REQUEST, 200, null, null), null, 0, null);
        assertEquals(EXPECTED_BASIC, HTTP_REQUEST.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    /**
     * Test that {@link ChallengeHandler} is able to preemptively handle Basic authorization challenges
     * once it has been used to handle a Basic challenge.
     */
    @Test
    public void pipelineBasic() {
        BasicHandler basicHandler = new BasicHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);
        basicHandler.handleChallenge(HTTP_REQUEST, new HttpResponse<>(HTTP_REQUEST, 200, null, null), null, 0, null);

        // Validate the initial authorization header
        assertEquals(EXPECTED_BASIC, HTTP_REQUEST.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));

        // Simulate a subsequent request to validate pipelining
        HttpRequest subsequentRequest = new HttpRequest(HttpMethod.GET, createUri());
        basicHandler.handleChallenge(subsequentRequest, new HttpResponse<>(subsequentRequest, 200, null, null), null, 0, null);

        // Validate the authorization header in the subsequent request
        assertEquals(EXPECTED_BASIC, subsequentRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    /**
     * Tests that {@link ChallengeHandler} will return {@code null} for an Authorization header when
     * attempting to pipeline a request when the handler has never handled a request.
     */
    @Test
    public void pipelineBasicWithoutInitialHandleFails() {
        BasicHandler basicHandler = new BasicHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        // Simulate a subsequent request to validate pipelining without initial handling
        HttpRequest subsequentRequest = new HttpRequest(HttpMethod.GET, createUri());

        // Handle challenge with no previous challenge (lastChallenge is null)
        basicHandler.handleChallenge(subsequentRequest, new HttpResponse<>(subsequentRequest, 200, null, null), null, 0, null);

        // Validate that the authorization header is null in the subsequent request
        assertNull(subsequentRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    /**
     * Tests that {@link ChallengeHandler} creates the expected {@code response} value when using
     * {@code MD5} as the authentication challenge algorithm.
     */
    @Test
    public void md5DigestAuthorization() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "6629fae49393a05397450978507c4ef1";
        String uri = "/dir/index.html";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);
        HttpResponse<?> httpResponse = createChallengeResponse("testrealm@host.com", "auth", Collections.singletonList("MD5"),
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null, ENTITY_BODY);

        digestHandler.handleChallenge(httpRequest, httpResponse, "0a4f113b", 0, null);

        assertEquals(expectedResponse, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));
    }

    /**
     * Tests that {@link ChallengeHandler} is able to preemptively handle Digest authorization challenges
     * once it has been used to handle a Digest challenge.
     */
    @Test
    public void pipelineDigest() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "6629fae49393a05397450978507c4ef1";
        String uri = "/dir/index.html";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> httpResponse = createChallengeResponse("testrealm@host.com", "auth", Collections.singletonList("MD5"),
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null, ENTITY_BODY);

        // Handle the initial challenge
        digestHandler.handleChallenge(httpRequest, httpResponse, "0a4f113b", 0, null);

        // Validate the initial authorization header
        assertEquals(expectedResponse, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));

        // Simulate a subsequent request to validate pipelining
        HttpRequest subsequentRequest = new HttpRequest(HttpMethod.GET, uri);
        digestHandler.handleChallenge(subsequentRequest, httpResponse, "0a4f113b", 1, null);

        // Validate that the nonce count is incremented
        assertEquals(2, Integer.parseInt(extractValue(subsequentRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), "nc")));

        // The pipelined response alters due to a change in the nonce count
        String expectedPipelineResponse = "15b6bb427e3fecd23a43cb702ce447d5";
        assertEquals(expectedPipelineResponse, extractValue(subsequentRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));
    }

    /**
     * Tests that {@link ChallengeHandler} will return {@code null} for an Authorization header when
     * attempting to pipeline a request when the handler has never handled a request.
     */
    @Test
    public void pipelineDigestWithoutInitialHandleFails() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        // Simulate a subsequent request to validate pipelining without initial handling
        // i.e has not been initialized with the necessary challenge information (like nonce, realm, etc.)
        HttpRequest subsequentRequest = new HttpRequest(HttpMethod.GET, "/dir/index.html");
        digestHandler.handleChallenge(subsequentRequest, new HttpResponse<>(subsequentRequest, 200, null, null), "0a4f113b", 0, null);

        // Validate that the authorization header is null in the subsequent request
        assertNull(subsequentRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    /**
     * Tests that {@link ChallengeHandler} is able to handle an authentication challenge that uses SHA-256
     * as the challenge algorithm.
     */
    @Test
    public void sha256DigestAuthorization() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, "Circle of Life");

        String expectedResponse = "753927fa0e85d155564e2e272a28d1802ca10daf4496794697cf8db5856cb6c1";
        String uri = "/dir/index.html";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> httpResponse = createChallengeResponse("http-auth@example.org", "auth", Collections.singletonList("SHA-256"),
            "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v", "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS", null, ENTITY_BODY);

        digestHandler.handleChallenge(httpRequest, httpResponse, "f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ", 0, null);

        assertEquals(expectedResponse, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));
    }

    /**
     * Tests that {@link ChallengeHandler} prefers more secure algorithms, {@code SHA-256} over
     * {@code MD5}, when presented with multiple authentication challenges.
     */
    @Test
    public void preferSha256OverMd5DigestAuthorization() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, "Circle of Life");

        String expectedResponse = "753927fa0e85d155564e2e272a28d1802ca10daf4496794697cf8db5856cb6c1";
        String uri = "/dir/index.html";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> challengeResponse = createChallengeResponse("http-auth@example.org", "auth", Arrays.asList("MD5", "SHA-256"),
            "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v", "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS", null, ENTITY_BODY);
        digestHandler.handleChallenge(httpRequest, challengeResponse, "f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ", 0, null);

        assertEquals(expectedResponse, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));
    }

    /**
     * Tests that {@link ChallengeHandler} will default {@code MD5} as the authentication challenge
     * algorithm when no value is given.
     */
    @Test
    public void digestAuthorizationDefaultAlgorithmIsMd5() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "6629fae49393a05397450978507c4ef1";
        String uri = "/dir/index.html";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> challengeResponse = createChallengeResponse("testrealm@host.com", "auth", null,
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null, ENTITY_BODY);

        digestHandler.handleChallenge(httpRequest, challengeResponse, "0a4f113b", 0, null);

        assertEquals(expectedResponse, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));
    }

    /**
     * Tests that {@link ChallengeHandler} will generate a hash of the username if the challenge contains
     * {@code userhash=true}.
     */
    @Test
    public void userHashDigestAuthorization() {
        DigestHandler digestHandler = new DigestHandler("J\u00e4s\u00f8n Doe",
            "Secret, or not?");

        String expectedResponse = "ae66e67d6b427bd3f120414a82e4acff38e8ecd9101d6c861229025f607a79dd";
        String expectedUsername = "488869477bf257147b804c45308cd62ac4e25eb717b12b298c79e62dcea254ec";
        String uri = "/doe.json";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> challengeResponse = createChallengeResponse("api@example.org", "auth", Collections.singletonList("SHA-512-256"),
            "5TsQWLVdgBdmrQ0XsxbDODV+57QdFR34I9HAbC/RVvkK", "HRPCssKJSGjCrkzDg8OhwpzCiGPChXYjwrI2QmXDnsOS", true, ENTITY_BODY);

        digestHandler.handleChallenge(httpRequest, challengeResponse, "NTg6RKcb9boFIAS3KrFK9BGeh+iDa/sm6jUMp2wds69v",0, null);

        assertEquals(expectedUsername, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), USERNAME));
        assertEquals(expectedResponse, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));
    }

    /**
     * Tests that {@link ChallengeHandler} produces the expected authorization response when {@code qop}
     * isn't passed or is unknown.
     */
    @Test
    public void unknownQop() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "670fd8c2df070c60b045671b8b24ff02";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> challengeResponse = createChallengeResponse("testrealm@host.com", "unknownQop", Collections.singletonList("MD5"),
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null, ENTITY_BODY);

        digestHandler.handleChallenge(httpRequest, challengeResponse, "0a4f113b", 0, null);

        assertEquals(expectedResponse, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));
    }

    /**
     * Tests that {@link ChallengeHandler} produces the expected authorization response when {@code qop}
     * is {@code auth-int}.
     */
    @Test
    public void md5DigestWithAuthInt() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "f7b13069066cfdda58d5accbc02a6b98";
        String uri = "/dir/index.html";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> challengeResponse = createChallengeResponse("testrealm@host.com", "auth-int", Collections.singletonList("MD5"),
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null, BinaryData.fromBytes("Hello World!".getBytes(StandardCharsets.UTF_8)));

        digestHandler.handleChallenge(httpRequest, challengeResponse, "0a4f113b", 0, null);

        assertEquals(expectedResponse, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));
    }

    /**
     * Tests that {@link ChallengeHandler} produces the expected authorization response when the algorithm
     * is a {@code -sess} based variant, such as {@code MD5-sess}.
     */
    @Test
    public void md5DigestWithSessAlgorithm() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "4726bc10c33fa6cb357eb27807b1cce8";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> challengeResponse = createChallengeResponse("testrealm@host.com", "", Collections.singletonList("MD5-sess"),
            "dcd98b7102dd2f0e8b11d0f600bfb0c093", "5ccc069c403ebaf9f0171e9517f40e41", null, ENTITY_BODY);

        digestHandler.handleChallenge(httpRequest, challengeResponse, "0a4f113b", 0, null);

        assertEquals(expectedResponse, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));
    }

    /**
     * Tests that {@link ChallengeHandler} will return {@code null} when the challenge algorithm isn't
     * supported, such as {@code SHA3}.
     */
    @Test
    public void unsupportedAlgorithmReturnsNull() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String uri = "/dir/index.html";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> challengeResponse = createChallengeResponse("realm", "auth", Collections.singletonList("SHA3"), "nonce", "opaque", null, ENTITY_BODY);
        digestHandler.handleChallenge(httpRequest, challengeResponse, null, 0, null);

        assertNull(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    /**
     * Tests that {@link ChallengeHandler} will return {@code null} when the challenge algorithm doesn't
     * exist.
     */
    @Test
    public void unknownAlgorithmIsSkipped() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String uri = "/dir/index.html";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> challengeResponse = createChallengeResponse("realm", "auth", Collections.singletonList("SHA9000"), "nonce", "opaque", null, BinaryData.fromBytes(new byte[0]));
        digestHandler.handleChallenge(httpRequest, challengeResponse, null, 0, null);
        assertNull(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    /**
     * Tests that {@link ChallengeHandler} correctly consumes the {@code Authentication-Info} response
     * header.
     */
    @Test
    public void consumeAuthenticationInfoHeader() {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);
        AtomicReference<ConcurrentHashMap<String, String>> lastChallenge = new AtomicReference<>(new ConcurrentHashMap<>());

        String uri = "/dir/index.html";
        String nonce = "dcd98b7102dd2f0e8b11d0f600bfb0c093";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> challengeResponse = createChallengeResponse("realm", "auth", Collections.singletonList("MD5"), nonce, "opaque", null, BinaryData.fromBytes(new byte[0]));

        digestHandler.handleChallenge(httpRequest, challengeResponse, null, 0, null);

        assertNotNull(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));

        String nextNonce = "5ccc069c403ebaf9f0171e9517f40e41";
        Map<String, String> authenticationInfoMap = new HashMap<>();
        authenticationInfoMap.put("nextnonce", nextNonce);

        String nextNonceReturned = AuthUtils.processAuthenticationInfoHeader(authenticationInfoMap);

        // Handle the nextnonce externally, making the handler stateless.
        if (nextNonceReturned != null) {
            lastChallenge.get().put(NONCE, nextNonceReturned);
        }

        // Simulate a subsequent request to validate the updated nonce
        HttpRequest subsequentRequest = new HttpRequest(HttpMethod.GET, uri);
        digestHandler.handleChallenge(subsequentRequest, challengeResponse, null, 1, lastChallenge);

        // Validate that the nonce has been updated
        assertEquals(nextNonce, extractValue(subsequentRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), "nonce"));
    }

    /**
     * Tests that {@link ChallengeHandler} correctly ignored the {@code Authentication-Info} response when
     * it is {@code null} or doesn't have a value.
     */
    @ParameterizedTest
    @MethodSource("nullOrEmptyAuthenticationInfoHeadersSupplier")
    public void consumingNullOrEmptyAuthenticationInfoHeadersDoesNotUpdate(Map<String, String> authenticationInfo) {
        DigestHandler digestHandler = new DigestHandler(DEFAULT_USERNAME, DEFAULT_PASSWORD);

        String expectedResponse = "6629fae49393a05397450978507c4ef1";
        String method = HttpMethod.GET.toString();
        String uri = "/dir/index.html";
        String nonce = "dcd98b7102dd2f0e8b11d0f600bfb0c093";

        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, uri);

        HttpResponse<?> challengeResponse = createChallengeResponse("testrealm@host.com", "auth", Collections.singletonList("MD5"), nonce, "5ccc069c403ebaf9f0171e9517f40e41", null, ENTITY_BODY);

        digestHandler.handleChallenge(httpRequest, challengeResponse, "0a4f113b", 0, null);

        assertEquals(expectedResponse, extractValue(httpRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION), RESPONSE));

        String authorizationHeader = AuthUtils.processAuthenticationInfoHeader(authenticationInfo);

        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(AuthUtils.parseAuthenticationOrAuthorizationHeader(authorizationHeader));
        digestHandler.handleChallenge(httpRequest, challengeResponse, "0a4f113b", 0, new AtomicReference<>(map));

        assertNotNull(authorizationHeader);
        assertEquals(nonce, extractValue(authorizationHeader, NONCE));
    }

    private static Stream<Arguments> nullOrEmptyAuthenticationInfoHeadersSupplier() {
        return Stream.of(Arguments.of((Map<String, String>) null), Arguments.of(Collections.emptyMap()));
    }

    /**
     * Tests that {@link AuthUtils#parseAuthenticationOrAuthorizationHeader(String)} correctly
     * parses values in an {@code Authenticate}, {@code Authorization}, or {@code Authentication-Info} header.
     */
    @ParameterizedTest
    @MethodSource("parseAuthenticationOrAuthorizationHeaderSupplier")
    public void parseAuthenticationOrAuthorizationHeader(String header, int expectedSize,
                                                         Map<String, String> expectedMap) {
        Map<String, String> parsedMap = AuthUtils.parseAuthenticationOrAuthorizationHeader(header);

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
            Arguments.of("nc=00000001, nextnonce=\"" + nextNonce + "\"", 2, expectedMultiMap));
    }

    private static HttpResponse<?> createChallengeResponse(String realm, String qop, List<String> algorithmList, String nonce,
                                                           String opaque, Boolean userhash, BinaryData entityBody) {
        Map<String, String> challenge = new HashMap<>();

        challenge.put(REALM, realm);
        challenge.put(QOP, qop);
        challenge.put(NONCE, nonce);
        challenge.put(OPAQUE, opaque);

        if (algorithmList != null) {
            for (String algorithm : algorithmList) {
                challenge.put(ALGORITHM, algorithm);
            }
        }

        if (userhash != null) {
            challenge.put(USERHASH, String.valueOf(userhash));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.WWW_AUTHENTICATE, "Digest " + challenge);

        return new HttpResponse<>(HTTP_REQUEST, 401, headers, entityBody);
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

    private static URI createUri() {
        try {
            return UriBuilder.parse("http://localhost").toUri();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
