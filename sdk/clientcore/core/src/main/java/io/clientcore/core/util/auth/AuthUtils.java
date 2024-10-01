//  Copyright (c) Microsoft Corporation. All rights reserved.
//  Licensed under the MIT License.

package io.clientcore.core.util.auth;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.clientcore.core.util.auth.AuthScheme.BASIC;
import static io.clientcore.core.util.auth.AuthScheme.DIGEST;

/**
 * Utility class for handling various HTTP authentication headers.
 */
public class AuthUtils {

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

        if (header.startsWith(BASIC.name()) || header.startsWith(DIGEST.name())) {
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

}
