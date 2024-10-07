// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.clientcore.core.util.auth.AuthScheme.BASIC;

/**
 * Utility class for handling various HTTP authentication headers.
 */
public class AuthUtils {
    private static final char[] LOWERCASE_HEX_CHARACTERS = "0123456789abcdef".toCharArray();

    /**
     * Header representing a server requesting authentication.
     */
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    /**
     * Header representing a proxy server requesting authentication.
     */
    public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

    /**
     * Header representing the authorization the client is presenting to a server.
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * Header representing the authorization the client is presenting to a proxy server.
     */
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

    /**
     * Header representing additional information a server is expecting during future authentication requests.
     */
    public static final String AUTHENTICATION_INFO = "Authentication-Info";

    /**
     * Header representing additional information a proxy server is expecting during future authentication requests.
     */
    public static final String PROXY_AUTHENTICATION_INFO = "Proxy-Authentication-Info";

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

        if (header.startsWith(BASIC.getScheme()) || header.startsWith(BASIC.getScheme())) {
            header = header.split(" ", 2)[1];
        }

        return Stream.of(header.split(","))
            .map(String::trim)
            .map(kvp -> kvp.split("=", 2))
            .collect(Collectors.toMap(kvpPieces -> kvpPieces[0].toLowerCase(Locale.ROOT),
                kvpPieces -> kvpPieces[1].replace("\"", "")));
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
     * Converts a byte array into a hex string.
     *
     * <p>The hex string returned uses characters {@code 0123456789abcdef}, if uppercase {@code ABCDEF} is required the
     * returned string will need to be {@link String#toUpperCase() uppercased}.</p>
     *
     * <p>If {@code bytes} is null, null will be returned. If {@code bytes} was an empty array an empty string is
     * returned.</p>
     *
     * @param bytes The byte array to convert into a hex string.
     * @return A hex string representing the {@code bytes} that were passed, or null if {@code bytes} were null.
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        if (bytes.length == 0) {
            return "";
        }

        // Hex uses 4 bits, converting a byte to hex will double its size.
        char[] hexString = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            // Convert the byte into an integer, masking all but the last 8 bits (the byte).
            int b = bytes[i] & 0xFF;

            // Shift 4 times to the right to get the leading 4 bits and get the corresponding hex character.
            hexString[i * 2] = LOWERCASE_HEX_CHARACTERS[b >>> 4];

            // Mask all but the last 4 bits and get the corresponding hex character.
            hexString[i * 2 + 1] = LOWERCASE_HEX_CHARACTERS[b & 0x0F];
        }

        return new String(hexString);
    }
}
