// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3.implementation;

import io.clientcore.core.util.binarydata.BinaryData;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class handles Basic and Digest authorization challenges, complying to RFC 2617 and RFC 7616.
 */
public class DefaultAuthorizationChallengeHandler implements AuthorizationChallengeHandler {
    private static final char[] LOWERCASE_HEX_CHARACTERS = "0123456789abcdef".toCharArray();

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
    private static final String NEXT_NONCE = "nextnonce";

    private static final String SESS = "-SESS";

    private static final String SHA_512_256 = "SHA-512-256";
    private static final String SHA_512_256_SESS = SHA_512_256 + SESS;

    private static final String SHA_256 = "SHA-256";
    private static final String SHA_256_SESS = SHA_256 + SESS;

    private static final String MD5 = "MD5";
    private static final String MD5_SESS = MD5 + SESS;

    private static final String[] ALGORITHM_PREFERENCE_ORDER = {
        SHA_512_256,
        SHA_512_256_SESS,
        SHA_256,
        SHA_256_SESS,
        MD5,
        MD5_SESS
    };

    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
    public static final String AUTHORIZATION = "Authorization";
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
    public static final String AUTHENTICATION_INFO = "Authentication-Info";
    public static final String PROXY_AUTHENTICATION_INFO = "Proxy-Authentication-Info";

    private final String username;
    private final String password;
    private final Map<String, AtomicInteger> nonceTracker = new ConcurrentHashMap<>();
    private final AtomicReference<String> authorizationPipeliningType = new AtomicReference<>();
    private final AtomicReference<ConcurrentHashMap<String, String>> lastChallenge = new AtomicReference<>();
    private final SecureRandom nonceGenerator = new SecureRandom();

    /**
     * Constructs a new DefaultAuthorizationChallengeHandler with the specified username and password.
     *
     * @param username The username for authentication. Cannot be null.
     * @param password The password for authentication. Cannot be null.
     */
    public DefaultAuthorizationChallengeHandler(String username, String password) {
        this.username = Objects.requireNonNull(username, "'username' cannot be null.");
        this.password = Objects.requireNonNull(password, "'password' cannot be null.");
    }

    @Override
    public String handleBasic() {
        authorizationPipeliningType.set(BASIC);
        String token = username + ":" + password;
        return BASIC + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String handleDigest(String method, String uri, List<Map<String, String>> challenges, Supplier<BinaryData> entityBodySupplier) {
        authorizationPipeliningType.set(DIGEST);
        Map<String, List<Map<String, String>>> challengesByType = partitionByChallengeType(challenges);
        for (String algorithm : ALGORITHM_PREFERENCE_ORDER) {
            // No challenges using this algorithm, skip it.
            if (!challengesByType.containsKey(algorithm)) {
                continue;
            }

            Function<byte[], byte[]> digestFunction = getDigestFunction(algorithm);

            // Unable to retrieve a digest for the specified algorithm, skip it.
            if (digestFunction == null) {
                continue;
            }

            ConcurrentHashMap<String, String> challenge
                = new ConcurrentHashMap<>(challengesByType.get(algorithm).get(0));
            lastChallenge.set(challenge);

            return createDigestAuthorizationHeader(method, uri, challenge, algorithm, entityBodySupplier,
                digestFunction);        }
        return null;
    }

    @Override
    public String attemptToPipelineAuthorization(String method, String uri, Supplier<BinaryData> entityBodySupplier) {
        String pipeliningType = authorizationPipeliningType.get();
        if (DIGEST.equals(pipeliningType)) {
            Map<String, String> challenge = new HashMap<>(lastChallenge.get());
            String algorithm = challenge.get(ALGORITHM);

            if (algorithm == null) {
                algorithm = MD5;
            }

            return createDigestAuthorizationHeader(method, uri, challenge, algorithm, entityBodySupplier,
                getDigestFunction(algorithm));
        } else if (BASIC.equals(pipeliningType)) {
            return handleBasic();
        }
        return null;
    }

    @Override
    public void consumeAuthenticationInfoHeader(Map<String, String> authenticationInfoMap) {
        if (isNullOrEmpty(authenticationInfoMap)) {
            return;
        }
        /*
         * If the authentication info header has a nextnonce value set update the last challenge nonce value to it.
         * The nextnonce value indicates to the client which nonce value it should use to generate its response value.
         */
        if (authenticationInfoMap.containsKey(NEXT_NONCE)) {
            lastChallenge.get().put(NONCE, authenticationInfoMap.get(NEXT_NONCE));
        }
    }

    /*
     * Creates the Authorization header for the Digest authentication challenge.
     */
    private String createDigestAuthorizationHeader(String method, String uri, Map<String, String> challenge,
                                                   String algorithm, Supplier<BinaryData> entityBodySupplier, Function<byte[], byte[]> digestFunction) {
        String realm = challenge.get(REALM);
        String nonce = challenge.get(NONCE);
        String qop = getQop(challenge.get(QOP));
        String opaque = challenge.get(OPAQUE);
        boolean hashUsername = Boolean.parseBoolean(challenge.get(USERHASH));

        /*
         * If the algorithm being used is <algorithm>-sess or QOP is 'auth' or 'auth-int' a client nonce will be needed
         * to calculate the authorization header. If the QOP is set a nonce-count will need to retrieve.
         */
        int nc = 0;
        String clientNonce = null;
        if (AUTH.equals(qop) || AUTH_INT.equals(qop)) {
            clientNonce = generateNonce();
            nc = getNc(challenge);
        } else if (algorithm.endsWith(SESS)) {
            clientNonce = generateNonce();
        }

        String ha1 = algorithm.endsWith(SESS)
            ? calculateHa1Sess(digestFunction, username, realm, password, nonce, clientNonce)
            : calculateHa1NoSess(digestFunction, username, realm, password);

        String ha2 = AUTH_INT.equals(qop)
            ? calculateHa2AuthIntQop(digestFunction, method, uri, entityBodySupplier.get().toBytes())
            : calculateHa2AuthQopOrEmpty(digestFunction, method, uri);

        String response = (AUTH.equals(qop) || AUTH_INT.equals(qop))
            ? calculateResponseKnownQop(digestFunction, ha1, nonce, nc, clientNonce, qop, ha2)
            : calculateResponseUnknownQop(digestFunction, ha1, nonce, ha2);

        String headerUsername = (hashUsername) ? calculateUserhash(digestFunction, username, realm) : username;

        return buildAuthorizationHeader(headerUsername, realm, uri, algorithm, nonce, nc, clientNonce, qop, response,
            opaque, hashUsername);
    }

    /*
     * Attempts to retrieve the digest function for the specified algorithm.
     */
    private static Function<byte[], byte[]> getDigestFunction(String algorithm) {
        if (algorithm.endsWith(SESS)) {
            algorithm = algorithm.substring(0, algorithm.length() - SESS.length());
        }

        try {
            /*
             * The SHA-512-256 algorithm is the first half of SHA-512 and needs special handling compared to SHA-256
             * and MD5.
             */
            if (SHA_512_256.equals(algorithm)) {
                MessageDigest digest = MessageDigest.getInstance("SHA-512");
                return (bytes) -> Arrays.copyOf(digest.digest(bytes), 32);
            } else {
                MessageDigest digest = MessageDigest.getInstance(algorithm);
                return digest::digest;
            }
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
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

    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /*
     * Parses the qopHeader for the qop to use. If the qopHeader is null or only contains unknown qop types null will
     * be returned, otherwise the preference is 'auth' followed by 'auth-int'.
     */
    private String getQop(String qopHeader) {
        if (isNullOrEmpty(qopHeader)) {
            return null;
        } else if (qopHeader.equalsIgnoreCase(AUTH)) {
            return AUTH;
        } else if (qopHeader.equalsIgnoreCase(AUTH_INT)) {
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
    private static String calculateHa1NoSess(Function<byte[], byte[]> digestFunction, String username, String realm,
                                             String password) {
        return bytesToHexString(
            digestFunction.apply((username + ":" + realm + ":" + password).getBytes(StandardCharsets.UTF_8)));
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
    private static String calculateHa1Sess(Function<byte[], byte[]> digestFunction, String username, String realm,
                                           String password, String nonce, String cnonce) {
        String ha1NoSess = calculateHa1NoSess(digestFunction, username, realm, password);

        return bytesToHexString(
            digestFunction.apply((ha1NoSess + ":" + nonce + ":" + cnonce).getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Calculates the 'HA2' hex string when using 'qop=auth' or the qop is unknown.
     *
     * This performs the following operations:
     * - Create the digest of (httpMethod + ":" + uri).
     * - Return the resulting bytes as a hex string.
     */
    private static String calculateHa2AuthQopOrEmpty(Function<byte[], byte[]> digestFunction, String httpMethod,
                                                     String uri) {
        return bytesToHexString(digestFunction.apply((httpMethod + ":" + uri).getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Calculates the 'HA2' hex string when using 'qop=auth-int'.
     *
     * This performs the following operations:
     * - Create the digest of (requestEntityBody).
     * - Convert the resulting bytes to a hex string, aliased as bodyHex.
     * - Create the digest of (httpMethod + ":" + uri + ":" bodyHex).
     * - Return the resulting bytes as a hex string.
     *
     * The request entity body is the unmodified body of the request. Using 'qop=auth-int' requires the request body to
     * be replay-able, this is why 'auth' is preferred instead of auth-int as this cannot be guaranteed. In addition to
     * the body being replay-able this runs into risks when the body is very large and potentially consuming large
     * amounts of memory.
     */
    private static String calculateHa2AuthIntQop(Function<byte[], byte[]> digestFunction, String httpMethod, String uri,
                                                 byte[] requestEntityBody) {
        String bodyHex = bytesToHexString(digestFunction.apply(requestEntityBody));

        return bytesToHexString(
            digestFunction.apply((httpMethod + ":" + uri + ":" + bodyHex).getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Calculates the 'response' hex string when qop is unknown.
     *
     * This performs the following operations:
     * - Create the digest of (ha1 + ":" + nonce + ":" + ha2).
     * - Return the resulting bytes as a hex string.
     */
    private static String calculateResponseUnknownQop(Function<byte[], byte[]> digestFunction, String ha1, String nonce,
                                                      String ha2) {
        return bytesToHexString(digestFunction.apply((ha1 + ":" + nonce + ":" + ha2).getBytes(StandardCharsets.UTF_8)));
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
    private static String calculateResponseKnownQop(Function<byte[], byte[]> digestFunction, String ha1, String nonce,
                                                    int nc, String cnonce, String qop, String ha2) {
        String zeroPadNc = String.format("%08X", nc);

        return bytesToHexString(
            digestFunction.apply((ha1 + ":" + nonce + ":" + zeroPadNc + ":" + cnonce + ":" + qop + ":" + ha2)
                .getBytes(StandardCharsets.UTF_8)));
    }

    /*
     * Calculates the hashed username value if the authenticate challenge has 'userhash=true'.
     */
    private static String calculateUserhash(Function<byte[], byte[]> digestFunction, String username, String realm) {
        return bytesToHexString(digestFunction.apply((username + ":" + realm).getBytes(StandardCharsets.UTF_8)));
    }

    private static Map<String, List<Map<String, String>>> partitionByChallengeType(List<Map<String, String>> challenges) {
        return challenges.stream().collect(Collectors.groupingBy(headers -> {
            String algorithmHeader = headers.get(ALGORITHM);
            return (algorithmHeader == null) ? MD5 : algorithmHeader.toUpperCase(Locale.ROOT);
        }));
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (bytes.length == 0) {
            return "";
        }
        char[] hexString = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            hexString[i * 2] = LOWERCASE_HEX_CHARACTERS[b >>> 4];
            hexString[i * 2 + 1] = LOWERCASE_HEX_CHARACTERS[b & 0x0F];
        }
        return new String(hexString);
    }

    /*
     * Creates a unique and secure nonce.
     */
    private String generateNonce() {
        byte[] nonce = new byte[16];
        nonceGenerator.nextBytes(nonce);
        return bytesToHexString(nonce);
    }
    /*
     * Creates the Authorization/Proxy-Authorization header value based on the computed Digest authentication value.
     */
    private static String buildAuthorizationHeader(String username, String realm, String uri, String algorithm,
                                                   String nonce, int nc, String cnonce, String qop, String response, String opaque, boolean userhash) {
        StringBuilder authorizationBuilder = new StringBuilder(512);

        authorizationBuilder.append(DIGEST)
            .append("username=\"")
            .append(username)
            .append("\", ")
            .append("realm=\"")
            .append(realm)
            .append("\", ")
            .append("nonce=\"")
            .append(nonce)
            .append("\", ")
            .append("uri=\"")
            .append(uri)
            .append("\", ")
            .append("response=\"")
            .append(response)
            .append("\"");

        if (!isNullOrEmpty(algorithm)) {
            authorizationBuilder.append(", algorithm=").append(algorithm);
        }

        if (!isNullOrEmpty(cnonce)) {
            authorizationBuilder.append(", cnonce=\"").append(cnonce).append("\"");
        }

        if (!isNullOrEmpty(opaque)) {
            authorizationBuilder.append(", opaque=\"").append(opaque).append("\"");
        }

        if (!isNullOrEmpty(qop)) {
            authorizationBuilder.append(", qop=").append(qop);
            authorizationBuilder.append(", nc=").append(java.lang.String.format("%08X", nc));
        }

        if (userhash) {
            authorizationBuilder.append(", userhash=true");
        }

        return authorizationBuilder.toString();
    }

    /**
     * Parses the {@code Authorization} or {@code Authentication} header into its key-value pairs.
     * <p>
     * This will remove quotes on quoted string values.
     *
     * @param header Authorization or Authentication header.
     * @return The Authorization or Authentication header split into its key-value pairs.
     */
    public static Map<String, String> parseAuthenticationOrAuthorizationHeader(String header) {
        if (isNullOrEmpty(header)) {
            return Collections.emptyMap();
        }

        if (header.startsWith(BASIC) || header.startsWith(DIGEST)) {
            header = header.split(" ", 2)[1];
        }

        return Stream.of(header.split(","))
            .map(String::trim)
            .map(kvp -> kvp.split("=", 2))
            .collect(Collectors.toMap(kvpPieces -> kvpPieces[0].toLowerCase(Locale.ROOT),
                kvpPieces -> kvpPieces[1].replace("\"", "")));
    }
}
