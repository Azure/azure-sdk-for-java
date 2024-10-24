// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.implementation.util.ImplUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for handling various authentication-related operations.
 */
public final class AuthUtils {
    public static final String SESS = "-SESS";
    public static final String MD5 = "MD5";
    public static final String SHA_512_256 = "SHA-512-256";
    public static final String ALGORITHM = "algorithm";
    public static final String NEXT_NONCE = "nextnonce";
    public static final String BASIC = "Basic ";
    public static final String DIGEST = "Digest";

    /**
     * Header representing the authorization the client is presenting to a proxy server.
     */
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

    private AuthUtils() {
        // Utility class should not be instantiated
    }

    /**
     * Calculates the 'HA1' hex string when using an algorithm that isn't a '-sess' variant.
     *
     * This performs the following operations:
     * - Create the digest of (username + ":" + realm + ":" password).
     * - Return the resulting bytes as a hex string.
     *
     * @param digestFunction The function to compute the digest.
     * @param username The username.
     * @param realm The authentication realm.
     * @param password The password.
     * @return The HA1 hex string.
     */
    public static String calculateHa1NoSess(Function<byte[], byte[]> digestFunction, String username, String realm,
                                            String password) {
        return bytesToHexString(
            digestFunction.apply((username + ":" + realm + ":" + password).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Calculates the 'HA1' hex string when using a '-sess' algorithm variant.
     *
     * This performs the following operations:
     * - Create the digest of (username + ":" + realm + ":" password).
     * - Convert the resulting bytes to a hex string, aliased as userPassHex.
     * - Create the digest of (userPassHex + ":" nonce + ":" + cnonce).
     * - Return the resulting bytes as a hex string.
     *
     * @param digestFunction The function to compute the digest.
     * @param username The username.
     * @param realm The authentication realm.
     * @param password The password.
     * @param nonce The server-specified nonce.
     * @param cnonce The client-specified nonce.
     * @return The HA1 hex string.
     */
    public static String calculateHa1Sess(Function<byte[], byte[]> digestFunction, String username, String realm,
                                          String password, String nonce, String cnonce) {
        String ha1NoSess = calculateHa1NoSess(digestFunction, username, realm, password);

        return bytesToHexString(
            digestFunction.apply((ha1NoSess + ":" + nonce + ":" + cnonce).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Calculates the 'HA2' hex string when using 'qop=auth' or the qop is unknown.
     *
     * This performs the following operations:
     * - Create the digest of (httpMethod + ":" + uri).
     * - Return the resulting bytes as a hex string.
     *
     * @param digestFunction The function to compute the digest.
     * @param httpMethod The HTTP method (e.g., GET, POST).
     * @param uri The request URI.
     * @return The HA2 hex string.
     */
    public static String calculateHa2AuthQopOrEmpty(Function<byte[], byte[]> digestFunction, String httpMethod,
                                                    String uri) {
        return bytesToHexString(digestFunction.apply((httpMethod + ":" + uri).getBytes(StandardCharsets.UTF_8)));
    }

    /**
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
     *
     * @param digestFunction The function to compute the digest.
     * @param httpMethod The HTTP method (e.g., GET, POST).
     * @param uri The request URI.
     * @param requestEntityBody The request entity body.
     * @return The HA2 hex string.
     */
    public static String calculateHa2AuthIntQop(Function<byte[], byte[]> digestFunction, String httpMethod, String uri,
                                                byte[] requestEntityBody) {
        String bodyHex = bytesToHexString(digestFunction.apply(requestEntityBody));

        return bytesToHexString(
            digestFunction.apply((httpMethod + ":" + uri + ":" + bodyHex).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Calculates the 'response' hex string when qop is unknown.
     *
     * This performs the following operations:
     * - Create the digest of (ha1 + ":" + nonce + ":" + ha2).
     * - Return the resulting bytes as a hex string.
     *
     * @param digestFunction The function to compute the digest.
     * @param ha1 The HA1 hex string.
     * @param nonce The server-specified nonce.
     * @param ha2 The HA2 hex string.
     * @return The response hex string.
     */
    public static String calculateResponseUnknownQop(Function<byte[], byte[]> digestFunction, String ha1, String nonce,
                                                     String ha2) {
        return bytesToHexString(digestFunction.apply((ha1 + ":" + nonce + ":" + ha2).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Calculates the 'response' hex string when 'qop=auth' or 'qop=auth-int'.
     *
     * This performs the following operations:
     * - Create the digest of (ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2).
     * - Return the resulting byes as a hex string.
     *
     * nc, nonce count, is represented in a hexadecimal format.
     *
     * @param digestFunction The function to compute the digest.
     * @param ha1 The HA1 hex string.
     * @param nonce The server-specified nonce.
     * @param nc The nonce count.
     * @param cnonce The client-specified nonce.
     * @param qop The quality of protection.
     * @param ha2 The HA2 hex string.
     * @return The response hex string.
     */
    public static String calculateResponseKnownQop(Function<byte[], byte[]> digestFunction, String ha1, String nonce,
                                                   int nc, String cnonce, String qop, String ha2) {
        String zeroPadNc = String.format("%08X", nc);

        return bytesToHexString(
            digestFunction.apply((ha1 + ":" + nonce + ":" + zeroPadNc + ":" + cnonce + ":" + qop + ":" + ha2)
                .getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Calculates the hashed username value if the authenticate challenge has 'userhash=true'.
     *
     * @param digestFunction The function to compute the digest.
     * @param username The username.
     * @param realm The authentication realm.
     * @return The hashed username value.
     */
    public static String calculateUserhash(Function<byte[], byte[]> digestFunction, String username, String realm) {
        return bytesToHexString(digestFunction.apply((username + ":" + realm).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Attempts to retrieve the digest function for the specified algorithm.
     *
     * @param algorithm The algorithm name.
     * @return The digest function, or null if the algorithm is not supported.
     */
    public static Function<byte[], byte[]> getDigestFunction(String algorithm) {
        if (algorithm.toUpperCase(Locale.ROOT).endsWith(SESS)) {
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

    /**
     * Converts a byte array to a hex string.
     *
     * @param bytes The byte array to convert.
     * @return The hex string representation of the byte array.
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Processes the Authentication-Info header and extracts the next nonce value.
     *
     * @param authenticationInfoMap The map containing key-value pairs from the Authentication-Info header.
     * @return The next nonce value if present, otherwise null.
     */
    public static String processAuthenticationInfoHeader(Map<String, String> authenticationInfoMap) {
        if (authenticationInfoMap == null || authenticationInfoMap.isEmpty()) {
            return null;
        }

        /*
         * Extracts the 'nextnonce' value from the authentication info header, if present.
         * This value is used to replace the current nonce for future digest authentications.
         */
        if (authenticationInfoMap.containsKey(NEXT_NONCE)) {
            return authenticationInfoMap.get(NEXT_NONCE);
        }

        // If no nextnonce is present, return null.
        return null;
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
        if (header == null || header.isEmpty()) {
            return Collections.emptyMap();
        }

        if (header.startsWith(BASIC) || header.startsWith(DIGEST)) {
            header = header.split(" ", 2)[1];
        }

        return Arrays.stream(header.split(","))
            .map(String::trim)
            .map(kvp -> kvp.split("=", 2))
            .collect(Collectors.toMap(
                kvpPieces -> kvpPieces[0].toLowerCase(Locale.ROOT),
                kvpPieces -> kvpPieces[1].replace("\"", "")
            ));
    }

    /**
     * Splits the Authenticate challenges by the algorithm it uses.
     *
     * @param headers The HTTP headers containing the challenges.
     * @return A map of challenges partitioned by their algorithm.
     */
    public static Map<String, List<Map<String, String>>> partitionByChallengeType(HttpHeaders headers) {
        // Extract the challenges from the headers, specifically the "Proxy-Authenticate" or "WWW-Authenticate" headers
        List<Map<String, String>> challenges = extractAllChallenges(headers);

        return challenges.stream().collect(Collectors.groupingBy(challenge -> {
            String algorithmHeader = challenge.get(ALGORITHM);

            // RFC7616 specifies that if the "algorithm" header is null, it defaults to MD5.
            return (algorithmHeader == null) ? MD5 : algorithmHeader.toUpperCase(Locale.ROOT);
        }));
    }

    /**
     * Checks if the character sequence is null or empty.
     *
     * @param charSequence Character sequence being checked for nullness or emptiness.
     *
     * @return True if the character sequence is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * Checks if the map is null or empty.
     *
     * @param map Map being checked for nullness or emptiness.
     *
     * @return True if the map is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Parses challenges from the provided {@link HttpHeaders}.
     *
     * @param headers The {@link HttpHeaders} that may contain challenge information.
     * @return A list of parsed challenges as Map.
     */
    public static List<Map<String, String>> parseChallenges(HttpHeaders headers) {
        List<String> authenticateHeaders = new ArrayList<>();

        if (headers.getValue(HttpHeaderName.WWW_AUTHENTICATE) != null) {
            authenticateHeaders.addAll(headers.getValues(HttpHeaderName.WWW_AUTHENTICATE));
        }
        if (headers.getValue(HttpHeaderName.PROXY_AUTHENTICATE) != null) {
            authenticateHeaders.addAll(headers.getValues(HttpHeaderName.PROXY_AUTHENTICATE));
        }

        if (authenticateHeaders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, String>> challenges = new ArrayList<>();
        for (String header : authenticateHeaders) {
            // Parse each challenge (basic/digest/etc.) into a Map<String, String>.
            challenges.add(parseChallenge(header));
        }
        return challenges;
    }

    /**
     * Extracts all challenges from the provided HTTP headers.
     *
     * @param headers The HTTP headers containing the challenges.
     * @return A list of parsed challenges as Map.
     */
    public static List<Map<String, String>> extractAllChallenges(HttpHeaders headers) {
        // Extract challenges from all relevant header fields
        List<Map<String, String>> challenges = new ArrayList<>();

        headers.stream()
            .filter(header -> header.getName().toString().toLowerCase().endsWith("authenticate"))
            .forEach(authHeader -> {
                // Parse the header value into challenge maps
                HttpHeaders singleHeader = new HttpHeaders().set(authHeader.getName(), authHeader.getValue());
                challenges.addAll(parseChallenges(singleHeader));
            });

        return challenges;
    }

    /**
     * Parses a single challenge from a header string.
     *
     * @param challengeHeader The header value containing the challenge.
     * @return The parsed challenge as a Map.
     */
    public static Map<String, String> parseChallenge(String challengeHeader) {
        Map<String, String> challengeMap = new HashMap<>();

        // Split the challenge into scheme and parameters.
        int firstSpaceIndex = challengeHeader.indexOf(' ');
        if (firstSpaceIndex == -1) {
            // If there's no space, it's just a scheme without parameters.
            challengeMap.put("scheme", challengeHeader.trim());
            return challengeMap;
        }

        // Get the scheme (e.g., "Digest").
        String scheme = challengeHeader.substring(0, firstSpaceIndex).trim();
        challengeMap.put("scheme", scheme);

        // Extract the parameters string (everything after the scheme).
        String params = challengeHeader.substring(firstSpaceIndex + 1).trim();

        // Split the parameters by comma, handling quoted commas correctly.
        String[] paramPairs = params.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String pair : paramPairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                // Remove braces, quotes around values if they exist.
                String key = keyValue[0].replace("{", "").replace("}", "").trim();
                String value = keyValue[1].replace("{", "").replace("}", "").replace("\"", "").trim();

                // Ensure we don't retain any closing brace at the end.
                if (value.endsWith("}")) {
                    value = value.substring(0, value.length() - 1).trim();
                }

                challengeMap.put(key, value);
            }
        }

        return challengeMap;
    }

    /**
     * Extracts a specific value from the Digest authentication header.
     *
     * @param authHeader The Digest authentication header.
     * @param key The key whose value needs to be extracted.
     * @return The extracted value, or null if the key is not found.
     */
    public static String extractValue(String authHeader, String key) {
        if (authHeader == null || !authHeader.startsWith("Digest")) {
            return null;
        }

        String[] headerParts = authHeader.split(",");
        for (String part : headerParts) {
            String[] keyValue = part.trim().split("=", 2);
            // Handle cases where keyValue[0] might contain 'Digest' followed by {qop} or other variations.
            if (keyValue.length == 2) {
                String headerKey = keyValue[0].replace("Digest", "").replace("{", "").replace("}", "").trim();
                if (headerKey.equalsIgnoreCase(key)) {
                    String value = keyValue[1].replace("{", "").replace("}", "").replace("\"", "").trim();
                    // Ensure we don't retain any closing brace at the end.
                    if (value.endsWith("}")) {
                        value = value.substring(0, value.length() - 1).trim();
                    }
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Creates the Authorization/Proxy-Authorization header value based on the computed Digest authentication value.
     *
     * @param username The username.
     * @param realm The authentication realm.
     * @param uri The request URI.
     * @param algorithm The algorithm used for hashing.
     * @param nonce The server-specified nonce.
     * @param nc The nonce count.
     * @param cnonce The client-specified nonce.
     * @param qop The quality of protection.
     * @param response The computed response.
     * @param opaque The opaque string.
     * @param userhash Whether the username is hashed.
     * @return The constructed Authorization/Proxy-Authorization header value.
     */
    public static String buildAuthorizationHeader(String username, String realm, String uri, String algorithm,
                                                  String nonce, int nc, String cnonce, String qop, String response, String opaque, boolean userhash) {
        StringBuilder authorizationBuilder = new StringBuilder(512);

        authorizationBuilder.append(DIGEST + " ")
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

        if (!ImplUtils.isNullOrEmpty(algorithm)) {
            authorizationBuilder.append(", algorithm=").append(algorithm);
        }

        if (!ImplUtils.isNullOrEmpty(cnonce)) {
            authorizationBuilder.append(", cnonce=\"").append(cnonce).append("\"");
        }

        if (!ImplUtils.isNullOrEmpty(opaque)) {
            authorizationBuilder.append(", opaque=\"").append(opaque).append("\"");
        }

        if (!ImplUtils.isNullOrEmpty(qop)) {
            authorizationBuilder.append(", qop=").append(qop);
            authorizationBuilder.append(", nc=").append(java.lang.String.format("%08X", nc));
        }

        if (userhash) {
            authorizationBuilder.append(", userhash=true");
        }

        return authorizationBuilder.toString();
    }
}

