// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.CoreUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 *
 */
public final class AuthorizationChallengeHandler {
    private static final String BASIC = "Basic ";
    private static final String DIGEST = "Digest ";

    private static final String ALGORITHM = "algorithm";
    private static final String REALM = "realm";
    private static final String NONCE = "nonce";
    private static final String QOP = "qop";
    private static final String AUTH = "auth";
    private static final String AUTH_INT = "auth-int";
    private static final String USERHASH = "userhash";
    private static final String OPAQUE = "opaque";

    /*
     * Digest proxy supports 3 unique algorithms in SHA-512/256, SHA-256, and MD5. Each algorithm is able to be used in
     * a <algorithm> and <algorithm>-sess variant, if the '-sess' variant is sent the response nonce and generated
     * cnonce (client nonce) will be used to calculate HA1.
     */
    private static final String _SESS = "-sess";

    private static final String SHA_512_256 = "SHA-512-256";
    private static final String SHA_512_256_SESS = SHA_512_256 + _SESS;

    private static final String SHA_256 = "SHA-256";
    private static final String SHA_256_SESS = SHA_256 + _SESS;

    private static final String MD5 = "MD5";
    private static final String MD5_SESS = MD5 + _SESS;

    // TODO: Prefer SESS based challenges?
    private static final String[] ALGORITHM_PREFERENCE_ORDER = {
        SHA_512_256,
        SHA_256_SESS,
        SHA_256,
        SHA_256_SESS,
        MD5,
        MD5_SESS
    };

    private final String username;
    private final String password;
    private final Map<String, AtomicInteger> nonceTracker;

    private AtomicReference<HttpHeaders> lastChallenge = new AtomicReference<>();

    /**
     * Creates an {@link AuthorizationChallengeHandler} using the {@code username} and {@code password} to respond to
     * authentication challenges.
     *
     * @param username Username used to response to authorization challenges.
     * @param password Password used to respond to authorization challenges.
     */
    public AuthorizationChallengeHandler(String username, String password) {
        this.username = username;
        this.password = password;
        this.nonceTracker = new ConcurrentHashMap<>();
    }

    /**
     * Handles Basic authentication challenges.
     *
     * @return Authorization header for Basic authentication challenges.
     */
    public String handleBasic() {
        String token = username + ":" + password;
        return BASIC + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Attempts to pipeline requests by applying the most recent Digest challenge used to create an authorization
     * header.
     *
     * @param method HTTP method being used in the request.
     * @param uri Relative URI for the request.
     * @return A preemptive authorization header for a potential Digest authentication challenge.
     */
    public String proactiveDigest(String method, String uri) {
        HttpHeaders challenge = lastChallenge.get();

        // Haven't responded to a challenge yet, send the request without an authorization header.
        if (challenge == null) {
            return null;
        }

        String algorithm = challenge.getValue(ALGORITHM);
        return createDigestAuthorizationHeader(method, uri, challenge, algorithm, getAlgorithmDigest(algorithm));
    }

    /**
     * Handles Digest authentication challenges.
     *
     * @param method HTTP method being used in the request.
     * @param uri Relative URI for the request.
     * @param challenges List of challenges that the server returned for the client to choose from and use when creating
     * the authorization header.
     * @return Authorization header for Digest authentication challenges.
     */
    public String handleDigest(String method, String uri, List<HttpHeaders> challenges) {
        Map<String, List<HttpHeaders>> challengesByType = partitionByChallengeType(challenges);

        for (String algorithm : ALGORITHM_PREFERENCE_ORDER) {
            // No challenges using this algorithm, skip it.
            if (!challengesByType.containsKey(algorithm)) {
                continue;
            }

            MessageDigest digest = getAlgorithmDigest(algorithm);

            // Unable to retrieve a digest for the specified algorithm, skip it.
            if (digest == null) {
                continue;
            }

            HttpHeaders challenge = challengesByType.get(algorithm).get(0);
            lastChallenge.set(challenge);

            return createDigestAuthorizationHeader(method, uri, challenge, algorithm, digest);
        }

        return null;
    }

    /*
     * Creates the Authorization header for the Digest authentication challenge.
     */
    private String createDigestAuthorizationHeader(String method, String uri, HttpHeaders challenge, String algorithm,
        MessageDigest digest) {
        int nc = getNc(challenge);
        String realm = challenge.getValue(REALM);
        String nonce = challenge.getValue(NONCE);
        String qop = getQop(challenge.getValue(QOP));
        String opaque = challenge.getValue(OPAQUE);
        boolean hashUsername = Boolean.parseBoolean(challenge.getValue(USERHASH));

        /*
         * If the algorithm being used is <algorithm>-sess or QOP is 'auth' or 'auth-int' a client nonce will be needed
         * to calculate the authorization header.
         */
        String cnonce = null;
        if (algorithm.endsWith(_SESS) || AUTH.equals(qop) || AUTH_INT.equals(qop)) {
            cnonce = generateCnonce();
        }

        byte[] ha1 = algorithm.endsWith(_SESS)
            ? calculateHa1Sess(digest, realm, nonce, cnonce)
            : calculateHa1NoSess(digest, realm);

        byte[] ha2 = AUTH_INT.equals(qop)
            ? calculateHa2AuthIntQop(digest, method, uri, null)
            : calculateHa2AuthQopOrEmpty(digest, method, uri);

        String response = (AUTH.equals(qop) || AUTH_INT.equals(qop))
            ? calculateResponseKnownQop(digest, ha1, nonce, nc, cnonce, qop, ha2)
            : calculateResponseUnknownQop(digest, ha1, nonce, ha2);

        String headerUsername = (hashUsername) ? calculateUserhash(digest, realm) : username;

        return buildAuthorizationHeader(username, realm, uri, algorithm, nonce, nc, cnonce, qop, response, opaque,
            hashUsername);
    }

    /*
     * Retrieves the nonce count for the given challenge. If the nonce in the challenge has already been used this will
     * increment and return the nonce count tracking, otherwise this will begin a new nonce tracking and return 1.
     */
    private int getNc(HttpHeaders challenge) {
        String nonce = challenge.getValue(NONCE);
        synchronized (nonceTracker) {
            if (nonceTracker.containsKey(nonce)) {
                return nonceTracker.get(nonce).incrementAndGet();
            } else {
                nonceTracker.put(nonce, new AtomicInteger(1));
                return 1;
            }
        }
    }

    private String getQop(String qopHeader) {
        if (CoreUtils.isNullOrEmpty(qopHeader)) {
            return null;
        } else if (qopHeader.contains(AUTH_INT)) {
            return AUTH_INT;
        } else if (qopHeader.contains(AUTH)) {
            return AUTH;
        } else {
            return null;
        }
    }

    private byte[] calculateHa1NoSess(MessageDigest digest, String realm) {
        return digest.digest(String.format("%s:%s:%s", username, realm, password).getBytes(StandardCharsets.UTF_8));
    }

    private byte[] calculateHa1Sess(MessageDigest digest, String realm, String nonce, String cnonce) {
        return digest.digest(mergeArrays(calculateHa1NoSess(digest, realm),
            String.format(":%s:%s", nonce, cnonce).getBytes(StandardCharsets.UTF_8)));
    }

    private byte[] calculateHa2AuthQopOrEmpty(MessageDigest digest, String httpMethod, String uri) {
        return digest.digest(String.format("%s:%s", httpMethod, uri).getBytes(StandardCharsets.UTF_8));
    }

    private byte[] calculateHa2AuthIntQop(MessageDigest digest, String httpMethod, String uri, String body) {
        byte[] bodyHash = digest.digest(body.getBytes(StandardCharsets.UTF_8));
        byte[] ha2AuthIntQopData = mergeArrays(String.format("%s:%s:", httpMethod, uri)
            .getBytes(StandardCharsets.UTF_8), bodyHash);

        return digest.digest(ha2AuthIntQopData);
    }

    private static String calculateResponseUnknownQop(MessageDigest digest, byte[] ha1, String nonce, byte[] ha2) {
        digest.update(ha1);
        digest.update(String.format(":%s:", nonce).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest.digest(ha2));
    }

    private static String calculateResponseKnownQop(MessageDigest digest, byte[] ha1, String nonce, int nc,
        String cnonce, String qop, byte[] ha2) {
        digest.update(ha1);
        digest.update(String.format(":%s:%08X:%s:%s:", nonce, nc, cnonce, qop).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest.digest(ha2));
    }

    /*
     * Calculates the hashed username value if the authenticate challenge has 'userhash=true'.
     */
    private String calculateUserhash(MessageDigest digest, String realm) {
        return Base64.getEncoder().encodeToString(
            digest.digest(String.format("%s:%s", username, realm).getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Attempts to retrieve the digest for the specified algorithm.
     */
    private static MessageDigest getAlgorithmDigest(String algorithm) {
        if (algorithm.endsWith(_SESS)) {
            algorithm = algorithm.substring(0, algorithm.length() - _SESS.length());
        }

        try {
            // The SHA-512/256 algorithm is sent back as SHA-512-256, convert it to its common name.
            return MessageDigest.getInstance(SHA_512_256.equals(algorithm) ? "SHA-512/256" : algorithm);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /*
     * Splits the Authenticate challenges by the algorithm it uses.
     */
    private static Map<String, List<HttpHeaders>> partitionByChallengeType(List<HttpHeaders> challenges) {
        return challenges.stream()
            .collect(Collectors.groupingBy(headers -> {
                String algorithmHeader = headers.getValue(ALGORITHM);

                // RFC7616 specifies that is the "algorithm" header is null it defaults to MD5.
                return (algorithmHeader == null) ? MD5 : algorithmHeader.toUpperCase(Locale.ROOT);
            }));
    }

    /*
     * Creates a client nonce.
     */
    private static String generateCnonce() {
        byte[] cnonce = new byte[16];
        new SecureRandom().nextBytes(cnonce);
        return Base64.getEncoder().encodeToString(cnonce);
    }

    /*
     * Creates the Authorization/Proxy-Authorization header value based on the computed Digest authentication value.
     */
    private static String buildAuthorizationHeader(String username, String realm, String uri, String algorithm,
        String nonce, int nc, String cnonce, String qop, String response, String opaque, boolean userhash) {
        StringBuilder authorizationBuilder = new StringBuilder(DIGEST);
        authorizationBuilder.append("username=\"").append(username).append("\", ");
        authorizationBuilder.append("realm=\"").append(realm).append("\", ");
        authorizationBuilder.append("uri=\"").append(uri).append("\", ");

        if (!CoreUtils.isNullOrEmpty(algorithm)) {
            authorizationBuilder.append("algorithm=").append(algorithm).append(", ");
        }

        authorizationBuilder.append("nonce=\"").append(nonce).append("\", ");
        authorizationBuilder.append("nc=").append(Integer.toHexString(nc)).append(", ");
        authorizationBuilder.append("cnonce=\"").append(cnonce).append("\", ");

        if (!CoreUtils.isNullOrEmpty(qop)) {
            authorizationBuilder.append("qop=").append(qop).append(", ");
        }

        authorizationBuilder.append("response=\"").append(response).append("\", ");
        authorizationBuilder.append("opaque=\"").append(opaque).append("\", ");
        authorizationBuilder.append("userhash=").append(userhash);

        return authorizationBuilder.toString();
    }

    /*
     * Merges two byte arrays.
     */
    private static byte[] mergeArrays(byte[] firstArray, byte[] secondArray) {
        byte[] mergedArray = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, mergedArray, 0, firstArray.length);
        System.arraycopy(secondArray, 0, mergedArray, firstArray.length, secondArray.length);

        return mergedArray;
    }
}
