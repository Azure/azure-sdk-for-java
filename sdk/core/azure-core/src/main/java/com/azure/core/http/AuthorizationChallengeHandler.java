package com.azure.core.http;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    public AuthorizationChallengeHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String handleBasic() {
        String token = username + ":" + password;
        return BASIC + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    public static String digest(String username, String password, String method, String uri,
        List<HttpHeaders> challenges) {
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
            String realm = challenge.getValue(REALM);
            String nonce = challenge.getValue(NONCE);

            String cnonce = null;
            if (algorithm.endsWith(_SESS)) {
                cnonce = generateNonce();
            }

            byte[] ha1 = calculateHa1(digest, username, realm, password, nonce, cnonce);
            byte[] ha2 = calculateHa2(digest, method, uri);

            String qop = challenge.getValue(QOP);
            if ((qop.contains(AUTH) || qop.contains(AUTH_INT)) && cnonce == null) {
                cnonce = generateNonce();
            }

            String response = calculateResponse(digest, ha1, ha2, nonce, , cnonce, qop);
        }

        return DIGEST;
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
     * Attempts to retrieve the digest for the specified algorithm.
     *
     * TODO: Potentially precompute this.
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

    private static String generateNonce() {
        byte[] cnonce = new byte[16];
        new SecureRandom().nextBytes(cnonce);
        return Base64.getEncoder().encodeToString(cnonce);
    }

    /*
     * Computing
     */
    private static byte[] calculateHa1(MessageDigest digest, String username, String realm, String password,
        String nonce, String cnonce) {
        byte[] ha1Bytes = digest.digest(String.join(":", username, realm, password).getBytes(StandardCharsets.UTF_8));

        if (cnonce == null) {
            return ha1Bytes;
        }

        byte[] sessAddition = (":" + nonce + ":" + cnonce).getBytes(StandardCharsets.UTF_8);
        byte[] sessFinalizedHa1 = new byte[ha1Bytes.length + sessAddition.length];
        System.arraycopy(ha1Bytes, 0, sessFinalizedHa1, 0, ha1Bytes.length);
        System.arraycopy(sessAddition, 0, sessFinalizedHa1, ha1Bytes.length, sessAddition.length);

        return digest.digest(sessFinalizedHa1);
    }

    private static byte[] calculateHa2(MessageDigest digest, String method, String uri) {
        return digest.digest((method + ":" + uri).getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] calculateResponse(MessageDigest digest, byte[] ha1, byte[] ha2, String nonce, String nc,
        String cnonce, String qop) {
        if (cnonce == null) {

        }
    }
}
