// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util.auth;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.auth.ChallengeHandler;
import io.clientcore.core.util.binarydata.BinaryData;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.clientcore.core.util.auth.AuthUtils.DIGEST;
import static io.clientcore.core.util.auth.AuthUtils.MD5;
import static io.clientcore.core.util.auth.AuthUtils.SESS;
import static io.clientcore.core.util.auth.AuthUtils.SHA_512_256;
import static io.clientcore.core.util.auth.AuthUtils.buildAuthorizationHeader;
import static io.clientcore.core.util.auth.AuthUtils.bytesToHexString;
import static io.clientcore.core.util.auth.AuthUtils.calculateHa1NoSess;
import static io.clientcore.core.util.auth.AuthUtils.calculateHa1Sess;
import static io.clientcore.core.util.auth.AuthUtils.calculateHa2AuthIntQop;
import static io.clientcore.core.util.auth.AuthUtils.calculateHa2AuthQopOrEmpty;
import static io.clientcore.core.util.auth.AuthUtils.calculateResponseKnownQop;
import static io.clientcore.core.util.auth.AuthUtils.calculateResponseUnknownQop;
import static io.clientcore.core.util.auth.AuthUtils.calculateUserhash;
import static io.clientcore.core.util.auth.AuthUtils.extractValue;
import static io.clientcore.core.util.auth.AuthUtils.getDigestFunction;
import static io.clientcore.core.util.auth.AuthUtils.partitionByChallengeType;

/**
 * Handles Digest authentication challenges.
 *
 * <p>This class is responsible for creating and managing Digest authentication headers
 * based on the provided username, password, and authentication challenges.</p>
 *
 * @see ChallengeHandler
 */
public class DigestChallengeHandler implements ChallengeHandler {
    private final String username;
    private final String password;
    private final Map<String, String> digestCache; // Cache for nonce, realm, etc.
    private static final String REALM = "realm";
    private static final String NONCE = "nonce";
    private static final String QOP = "qop";
    private static final String AUTH = "auth";
    private static final String AUTH_INT = "auth-int";
    private static final String USERHASH = "userhash";
    private static final String OPAQUE = "opaque";
    private final SecureRandom nonceGenerator = new SecureRandom();
    private final Map<String, AtomicInteger> nonceTracker = new ConcurrentHashMap<>();

    private static final String SHA_512_256_SESS = SHA_512_256 + SESS;
    private static final String SHA_256 = "SHA-256";
    private static final String SHA_256_SESS = SHA_256 + SESS;
    private static final String MD5_SESS = MD5 + SESS;

    // Algorithm preference order
    private static final String[] ALGORITHM_PREFERENCE_ORDER = {
        SHA_512_256, SHA_512_256_SESS, SHA_256, SHA_256_SESS, MD5, MD5_SESS
    };

    /**
     * Creates an {@link DigestChallengeHandler} using the {@code username} and {@code password} to respond to
     * authentication challenges.
     *
     * @param username Username used to response to authorization challenges.
     * @param password Password used to respond to authorization challenges.
     * @throws NullPointerException If {@code username} or {@code password} are {@code null}.
     */
    public DigestChallengeHandler(String username, String password) {
        this.username = username;
        this.password = password;
        this.digestCache = new HashMap<>();
    }

    @Override
    public void handleChallenge(HttpRequest request, Response<?> response) {
        String authHeader = null;
        if (response.getHeaders() != null && response.getHeaders().get(HttpHeaderName.WWW_AUTHENTICATE) != null) {
            authHeader = response.getHeaders().get(HttpHeaderName.WWW_AUTHENTICATE).getValue();
        }
        if (!canHandle(response)) {
            return;
        }

        if (authHeader.contains(NONCE)) {
            updateDigestCache(authHeader);
        }

        // Extract the algorithm if present
        Map<String, List<Map<String, String>>> challengesByType = partitionByChallengeType(response.getHeaders());

        for (String algorithm : ALGORITHM_PREFERENCE_ORDER) {
            if (!challengesByType.containsKey(algorithm.toUpperCase(Locale.ROOT))) {
                continue;
            }

            Function<byte[], byte[]> digestFunction = getDigestFunction(algorithm);
            if (digestFunction == null) {
                continue;
            }

            // Generate Digest Authorization header
            String digestAuthHeader = generateDigestAuthHeader(
                request.getHttpMethod().name(),
                request.getUri().toString(),
                algorithm,
                digestFunction,
                response.getBody()
            );

            request.getHeaders().set(HttpHeaderName.AUTHORIZATION, digestAuthHeader);
        }
    }

    @Override
    public boolean canHandle(Response<?> response) {
        String authHeader = null;
        if (response.getHeaders() != null && response.getHeaders().get(HttpHeaderName.WWW_AUTHENTICATE) != null) {
            authHeader = response.getHeaders().get(HttpHeaderName.WWW_AUTHENTICATE).getValue();
        }
        // Ensure that only Digest challenges are handled
        return authHeader != null && authHeader.startsWith(DIGEST);  // Not a Digest authentication challenge
    }

    private void updateDigestCache(String authHeader) {
        // Parse the authHeader and update the digest cache with necessary values like nonce, realm, etc.
        String nonce = extractValue(authHeader, NONCE);
        String realm = extractValue(authHeader, REALM);
        String qop = extractValue(authHeader, QOP);
        String hashUsername = extractValue(authHeader, USERHASH);
        String opaque = extractValue(authHeader, OPAQUE);

        digestCache.put(NONCE, nonce);
        digestCache.put(REALM, realm);
        digestCache.put(QOP, qop);
        digestCache.put(USERHASH, hashUsername);
        digestCache.put(OPAQUE, opaque);
    }

    /*
     * Creates a unique and secure nonce.
     */
    String generateCnonce() {
        byte[] nonce = new byte[16];
        nonceGenerator.nextBytes(nonce);
        return bytesToHexString(nonce);
    }

    private String generateDigestAuthHeader(String method, String uri, String algorithm, Function<byte[], byte[]> digestFunction, BinaryData body) {
        String nonce = digestCache.get(NONCE);
        String realm = digestCache.get(REALM);
        String qop = digestCache.get(QOP);
        String opaque = digestCache.get(OPAQUE);
        boolean hashUsername = Boolean.parseBoolean(digestCache.get(USERHASH));
        /*
         * If the algorithm being used is <algorithm>-sess or QOP is 'auth' or 'auth-int' a client nonce will be needed
         * to calculate the authorization header. If the QOP is set a nonce-count will need to retrieved.
         */
        int nc = 0;
        String clientNonce = null;
        if (AUTH.equals(qop) || AUTH_INT.equals(qop)) {
            clientNonce = generateCnonce();
            nc = getOrUpdateNonceCount(nonce);
        } else if (algorithm.endsWith(SESS)) {
            clientNonce = generateCnonce();
        }

        String ha1 = algorithm.endsWith(SESS)
            ? calculateHa1Sess(digestFunction, username, realm, password, nonce, clientNonce)
            : calculateHa1NoSess(digestFunction, username, realm, password);

        String ha2 = AUTH_INT.equals(qop)
            ? calculateHa2AuthIntQop(digestFunction, method, uri, body.toBytes())
            : calculateHa2AuthQopOrEmpty(digestFunction, method, uri);

        String response = (AUTH.equals(qop) || AUTH_INT.equals(qop))
            ? calculateResponseKnownQop(digestFunction, ha1, nonce, nc, clientNonce, qop, ha2)
            : calculateResponseUnknownQop(digestFunction, ha1, nonce, ha2);

        String headerUsername = (hashUsername) ? calculateUserhash(digestFunction, username, realm) : username;

        return buildAuthorizationHeader(headerUsername, realm, uri, algorithm, nonce, nc, clientNonce, qop, response,
            opaque, hashUsername);
    }

    /*
     * Retrieves the nonce count for the given challenge. If the nonce in the challenge has already been used this will
     * increment and return the nonce count tracking, otherwise this will begin a new nonce tracking and return 1.
     */
    private int getOrUpdateNonceCount(String nonce) {
        return nonceTracker.compute(nonce, (ignored, value) -> {
            if (value == null) {
                return new AtomicInteger(1);
            }

            value.incrementAndGet();
            return value;
        }).get();
    }
}
