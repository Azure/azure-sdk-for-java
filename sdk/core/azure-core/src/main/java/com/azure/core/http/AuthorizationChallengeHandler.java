// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.CoreUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class handles Basic and Digest authorization challenges, complying to RFC 2617 and RFC 7616.
 */
public final class AuthorizationChallengeHandler {
    private static final char[] HEX_CHARACTERS = "0123456789abcdef".toCharArray();

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
     * @param bodySupplier Supplies the request body, used to compute the hash of the body when using {@code
     * "qop=auth-int"}.
     * @return A preemptive authorization header for a potential Digest authentication challenge.
     */
    public String proactiveDigest(String method, String uri, Supplier<byte[]> bodySupplier) {
        HttpHeaders challenge = lastChallenge.get();

        // Haven't responded to a challenge yet, send the request without an authorization header.
        if (challenge == null) {
            return null;
        }

        String algorithm = challenge.getValue(ALGORITHM);
        return createDigestAuthorizationHeader(method, uri, challenge, algorithm, bodySupplier,
            getAlgorithmDigest(algorithm));
    }

    /**
     * Handles Digest authentication challenges.
     *
     * @param method HTTP method being used in the request.
     * @param uri Relative URI for the request.
     * @param challenges List of challenges that the server returned for the client to choose from and use when creating
     * the authorization header.
     * @param bodySupplier Supplies the request body, used to compute the hash of the body when using {@code
     * "qop=auth-int"}.
     * @return Authorization header for Digest authentication challenges.
     */
    public String handleDigest(String method, String uri, List<HttpHeaders> challenges, Supplier<byte[]> bodySupplier) {
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

            return createDigestAuthorizationHeader(method, uri, challenge, algorithm, bodySupplier, digest);
        }

        return null;
    }

    /*
     * Creates the Authorization header for the Digest authentication challenge.
     */
    private String createDigestAuthorizationHeader(String method, String uri, HttpHeaders challenge, String algorithm,
        Supplier<byte[]> bodySupplier, MessageDigest digest) {
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

        String ha1 = algorithm.endsWith(_SESS)
            ? calculateHa1Sess(digest, realm, nonce, cnonce)
            : calculateHa1NoSess(digest, realm);

        String ha2 = AUTH_INT.equals(qop)
            ? calculateHa2AuthIntQop(digest, method, uri, bodySupplier.get())
            : calculateHa2AuthQopOrEmpty(digest, method, uri);

        String response = (AUTH.equals(qop) || AUTH_INT.equals(qop))
            ? calculateResponseKnownQop(digest, ha1, nonce, nc, cnonce, qop, ha2)
            : calculateResponseUnknownQop(digest, ha1, nonce, ha2);

        String headerUsername = (hashUsername) ? calculateUserhash(digest, realm) : username;

        return buildAuthorizationHeader(headerUsername, realm, uri, algorithm, nonce, nc, cnonce, qop, response, opaque,
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

    /*
     * Parses the qopHeader for the qop to use. If the qopHeader is null or only contains unknown qop types null will
     * be returned, otherwise the preference is 'auth' followed by 'auth-int'.
     */
    private String getQop(String qopHeader) {
        if (CoreUtils.isNullOrEmpty(qopHeader)) {
            return null;
        } else if (qopHeader.contains(AUTH)) {
            return AUTH;
        } else if (qopHeader.contains(AUTH_INT)) {
            return AUTH_INT;
        } else {
            return null;
        }
    }

    /*
     * Calculates the 'HA1' hex string when using an algorithm that isn't a '-sess' variant.
     *
     * This performs the following operations:
     * - Create the digest of (username + ":" + realm + ":" password).
     * - Return the resulting bytes as a hex string.
     */
    private String calculateHa1NoSess(MessageDigest digest, String realm) {
        return hexStringOf(digest.digest(String.format("%s:%s:%s", username, realm, password)
            .getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Calculates the 'HA1' hex string when using a '-sess' algorithm variant.
     *
     * This performs the following operations:
     * - Create the digest of (username + ":" + realm + ":" password).
     * - Convert the resulting bytes to a hex string, aliased as userPassHex.
     * - Create the digest of (userPassHex + ":" nonce + ":" + cnonce).
     * - Return the resulting bytes as a hex string.
     */
    private String calculateHa1Sess(MessageDigest digest, String realm, String nonce, String cnonce) {
        return hexStringOf(digest.digest(String.format("%s:%s:%s", calculateHa1NoSess(digest, realm), nonce, cnonce)
            .getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Calculates the 'HA2' hex string when using 'qop=auth' or the qop is unknown.
     *
     * This performs the following operations:
     * - Create the digest of (httpMethod + ":" + uri).
     * - Return the resulting bytes as a hex string.
     */
    private String calculateHa2AuthQopOrEmpty(MessageDigest digest, String httpMethod, String uri) {
        return hexStringOf(digest.digest(String.format("%s:%s", httpMethod, uri).getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Calculates the 'HA2' hex string when using 'qop=auth-int'.
     *
     * This performs the following operations:
     * - Create the digest of (requestEntity).
     * - Convert the resulting bytes to a hex string, aliased as bodyHex.
     * - Create the digest of (httpMethod + ":" + uri + ":" bodyHex).
     * - Return the resulting bytes as a hex string.
     *
     * The request entity is the entity headers (https://www.w3.org/Protocols/rfc2616/rfc2616-sec7.html) and the body of
     * the request. Using 'qop=auth-int' requires the request body to be replay-able, this is why 'auth' is preferred
     * instead of auth-int as this cannot be guaranteed. In addition to the body being replay-able this runs into risks
     * when the body is very large and potentially consuming large amounts of memory.
     */
    private String calculateHa2AuthIntQop(MessageDigest digest, String httpMethod, String uri, byte[] requestEntity) {
        return hexStringOf(digest.digest(String.format("%s:%s:%s", httpMethod, uri,
            hexStringOf(digest.digest(requestEntity))).getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Calculates the 'response' hex string when qop is unknown.
     *
     * This performs the following operations:
     * - Create the digest of (ha1 + ":" + nonce + ":" + ha2).
     * - Return the resulting bytes as a hex string.
     */
    private String calculateResponseUnknownQop(MessageDigest digest, String ha1, String nonce, String ha2) {
        return hexStringOf(digest.digest(String.format("%s:%s:%s", ha1, nonce, ha2).getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Calculates the 'response' hex string when 'qop=auth' or 'qop=auth-int'.
     *
     * This performs the following operations:
     * - Create the digest of (ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2).
     * - Return the resulting byes as a hex string.
     *
     * nc, nonce count, is represented in a hexadecimal format.
     */
    private String calculateResponseKnownQop(MessageDigest digest, String ha1, String nonce, int nc, String cnonce,
        String qop, String ha2) {
        return hexStringOf(digest.digest(String.format("%s:%s:%08X:%s:%s:%s", ha1, nonce, nc, cnonce, qop, ha2)
            .getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Calculates the hashed username value if the authenticate challenge has 'userhash=true'.
     */
    private String calculateUserhash(MessageDigest digest, String realm) {
        return Base64.getEncoder()
            .encodeToString(digest.digest(String.format("%s:%s", username, realm).getBytes(StandardCharsets.UTF_8)));
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
        return "0a4f113b";
//        byte[] cnonce = new byte[16];
//        new SecureRandom().nextBytes(cnonce);
//        return hexStringOf(cnonce);
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
        authorizationBuilder.append("nc=").append(String.format("%08X", nc)).append(", ");
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
     * Converts the passed byte array into a hex string.
     */
    private static String hexStringOf(byte[] bytes) {
        // Hex uses 4 bits, converting a byte to hex will double its size.
        char[] hexCharacters = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            // Convert the byte into an integer, masking all but the last 8 bits (the byte).
            int b = bytes[i] & 0xFF;

            // Shift 4 times to the right to get the leading 4 bits and get the corresponding hex character.
            hexCharacters[i * 2] = HEX_CHARACTERS[b >>> 4];

            // Mask all but the last 4 bits and get the corresponding hex character.
            hexCharacters[i * 2 + 1] = HEX_CHARACTERS[b & 0x0F];
        }

        return new String(hexCharacters);
    }
}
