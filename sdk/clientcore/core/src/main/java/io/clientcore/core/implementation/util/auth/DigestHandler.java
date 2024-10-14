// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util.auth;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.util.auth.ChallengeHandler;
import io.clientcore.core.util.binarydata.BinaryData;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.clientcore.core.util.auth.AuthUtils.bytesToHexString;
import static io.clientcore.core.util.auth.AuthUtils.buildAuthorizationHeader;
import static io.clientcore.core.util.auth.AuthUtils.calculateHa1NoSess;
import static io.clientcore.core.util.auth.AuthUtils.calculateHa1Sess;
import static io.clientcore.core.util.auth.AuthUtils.calculateHa2AuthIntQop;
import static io.clientcore.core.util.auth.AuthUtils.calculateHa2AuthQopOrEmpty;
import static io.clientcore.core.util.auth.AuthUtils.calculateResponseKnownQop;
import static io.clientcore.core.util.auth.AuthUtils.calculateResponseUnknownQop;
import static io.clientcore.core.util.auth.AuthUtils.calculateUserhash;
import static io.clientcore.core.util.auth.AuthUtils.getDigestFunction;
import static io.clientcore.core.util.auth.AuthUtils.getQop;
import static io.clientcore.core.util.auth.AuthUtils.partitionByChallengeType;

/**
 * Handles Digest authentication challenges.
 *
 * <p>This class is responsible for creating and managing Digest authentication headers
 * based on the provided username, password, and authentication challenges.</p>
 *
 * @see ChallengeHandler
 */
public class DigestHandler implements ChallengeHandler {
    private static final String NONCE = "nonce";
    private static final String USERHASH = "userhash";
    private static final String OPAQUE = "opaque";
    private static final String QOP = "qop";
    private static final String AUTH = "auth";
    private static final String AUTH_INT = "auth-int";
    private static final String REALM = "realm";

    /*
     * Digest proxy supports 3 unique algorithms in SHA-512/256, SHA-256, and MD5. Each algorithm is able to be used in
     * a <algorithm> and <algorithm>-sess variant, if the '-sess' variant is sent the response nonce and generated
     * cnonce (client nonce) will be used to calculate HA1.
     */
    private static final String SESS = "-SESS";

    private static final String SHA_512_256 = "SHA-512-256";
    private static final String SHA_512_256_SESS = SHA_512_256 + SESS;

    private static final String SHA_256 = "SHA-256";
    private static final String SHA_256_SESS = SHA_256 + SESS;

    private static final String MD5 = "MD5";
    private static final String MD5_SESS = MD5 + SESS;

    // TODO: Prefer SESS based challenges?
    private static final String[] ALGORITHM_PREFERENCE_ORDER
        = { SHA_512_256, SHA_512_256_SESS, SHA_256, SHA_256_SESS, MD5, MD5_SESS };

    private final String username;
    private final String password;
    private String method;
    private String uri;
    private BinaryData entityBody;
    private final Map<String, AtomicInteger> nonceTracker = new ConcurrentHashMap<>();
    private AtomicReference<ConcurrentHashMap<String, String>> lastChallenge;
    private final SecureRandom nonceGenerator = new SecureRandom();
    private String userProvidedNonce;

    /**
     * Creates an {@link DigestHandler} using the {@code username} and {@code password} to respond to
     * authentication challenges.
     *
     * @param username Username used to response to authorization challenges.
     * @param password Password used to respond to authorization challenges.
     * @throws NullPointerException If {@code username} or {@code password} are {@code null}.
     */
    public DigestHandler(String username, String password) {
        this.username = Objects.requireNonNull(username, "'username' cannot be null.");
        this.password = Objects.requireNonNull(password, "'password' cannot be null.");

    }

    @Override
    public void handleChallenge(HttpRequest request, HttpResponse<?> response, String cnonce, int nonceCount, AtomicReference<ConcurrentHashMap<String, String>> lastChallenge) {
        this.method = request.getHttpMethod().name();
        this.uri = request.getUri().toString();
        this.entityBody = response.getBody();
        this.userProvidedNonce = cnonce;
        this.lastChallenge = lastChallenge;
        String digestAuthHeader = createDigestAuthorizationHeader(response);
        request.getHeaders().add(HttpHeaderName.AUTHORIZATION, digestAuthHeader);
    }

    private String createDigestAuthorizationHeader(HttpResponse<?> response) {
        String authHeader = null;
        if (response.getHeaders()!= null && response.getHeaders().get(HttpHeaderName.WWW_AUTHENTICATE) != null) {
            authHeader = response.getHeaders().get(HttpHeaderName.WWW_AUTHENTICATE).getValue();
        }

        // Clear previous Authorization header
        response.getRequest().getHeaders().set(HttpHeaderName.AUTHORIZATION, (String) null);

        // Extract the nonce from the lastChallenge if it's already stored.
        String nonce = lastChallenge != null ? lastChallenge.get().get(NONCE) : null;

        // If there's no previous nonce, extract it from the current challenge.
        if (nonce == null && authHeader != null) {
            nonce = extractNonceFromHeader(authHeader);
            if (lastChallenge == null) {
                lastChallenge = new AtomicReference<>(new ConcurrentHashMap<>());
            }
            lastChallenge.get().put(NONCE, nonce);  // Update only the nonce in the lastChallenge.
        }

        Map<String, List<Map<String, String>>> challengesByType = partitionByChallengeType(response.getHeaders());

        for (String algorithm : ALGORITHM_PREFERENCE_ORDER) {
            if (!challengesByType.containsKey(algorithm)) {
                continue;
            }

            Function<byte[], byte[]> digestFunction = getDigestFunction(algorithm);
            if (digestFunction == null) {
                continue;
            }
            ConcurrentHashMap<String, String> challenge = new ConcurrentHashMap<>(challengesByType.get(algorithm).get(0));
            // Ensure lastChallenge is initialized and update only if fields in lastChallenge are null while retaining existing values.
            if (lastChallenge == null) {
                lastChallenge = new AtomicReference<>(new ConcurrentHashMap<>(challenge));
            } else {
                challenge.forEach((key, value) -> {
                    lastChallenge.get().putIfAbsent(key, value);
                });
            }
            // Doubt: Should we not be sending lastChallenge here ?
            return generateDigestAuthHeader(lastChallenge.get(), algorithm, digestFunction);
        }
        return null;
    }

    private String extractNonceFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Digest")) {
            return null;
        }

        String[] headerParts = authHeader.split(",");
        for (String part : headerParts) {
            String[] keyValue = part.trim().split("=", 2);
            if (keyValue.length == 2 && keyValue[0].trim().equalsIgnoreCase("nonce")) {
                return keyValue[1].replace("\"", "").trim();
            }
        }
        return null;
    }

    private String generateDigestAuthHeader(Map<String, String> challenge, String algorithm, Function<byte[], byte[]> digestFunction) {
        String realm = challenge.get(REALM);
        String nonce = challenge.get(NONCE);
        String qop = getQop(challenge.get(QOP));
        String opaque = challenge.get(OPAQUE);
        boolean hashUsername = Boolean.parseBoolean(challenge.get(USERHASH));

        String clientNonce = this.userProvidedNonce;
        int nc = 0;

        if (AUTH.equals(qop) || AUTH_INT.equals(qop)) {
            if (clientNonce == null) {
                clientNonce = generateNonce();
            }
            nc = getNc(challenge);
        } else if (algorithm.endsWith(SESS)) {
            if (clientNonce == null) {
                clientNonce = generateNonce();
            }
        }

        String ha1 = algorithm.endsWith(SESS)
            ? calculateHa1Sess(digestFunction, username, realm, password, nonce, clientNonce)
            : calculateHa1NoSess(digestFunction, username, realm, password);

        String ha2 = AUTH_INT.equals(qop)
            ? calculateHa2AuthIntQop(digestFunction, method, uri, entityBody.toBytes())
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
    private int getNc(Map<String, String> challenge) {
        return nonceTracker.compute(challenge.get(NONCE), (ignored, value) -> {
            if (value == null) {
                return new AtomicInteger(1);
            }

            value.incrementAndGet();
            return value;
        }).get();
    }

    /*
     * Creates a unique and secure nonce.
     */
    String generateNonce() {
        byte[] nonce = new byte[16];
        nonceGenerator.nextBytes(nonce);
        return bytesToHexString(nonce);
    }
}
