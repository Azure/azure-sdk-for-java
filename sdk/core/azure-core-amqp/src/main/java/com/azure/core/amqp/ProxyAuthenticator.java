// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.implementation.ChallengeResponseAccessHelper;

import java.util.List;
import java.util.Map;

/**
 * A contract to authenticate a proxy server to tunnel a websocket connection to an AMQP broker.
 */
public interface ProxyAuthenticator {
    /**
     * Authenticate a proxy server to tunnel a websocket connection to an AMQP broker.
     * <p>
     * This method is called when the proxy server replies to the CONNECT with 407 (Proxy Authentication Required)
     * challenge. The proxy server's challenge response includes a 'Proxy-Authenticate' header indicating
     * the authentication scheme(s) that the proxy supports. The implementation of this method should
     * <ul>
     *     <li>enumerate the schemes using {@link ChallengeResponse#getAuthenticationSchemes()}) and choose the most
     *     secure scheme the client supports,</li>
     *     <li>identify the credential for the chosen scheme, </li>
     *     <li>compute and return authorization value.The RFC7325 defines authorization format as a value that starts
     *     with the selected scheme, followed by a space and the base64 encoded credentials for the scheme.</li>
     * </ul>
     * The returned authorization value will be sent to the proxy server in 'Proxy-Authorization' header to complete
     * the authentication.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/407">407 Proxy Authentication Required</a>
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Proxy-Authenticate">Proxy-Authenticate</a>
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7235#section-4.4">RFC7235</a>
     *
     * @param response the challenge response from the proxy server.
     * @return the authorization value to send to the proxy server using 'Proxy-Authorization' header.
     */
    String authenticate(ChallengeResponse response);

    /**
     * Represents the 407 challenge response from the proxy server.
     */
    final class ChallengeResponse {
        static {
            ChallengeResponseAccessHelper.setAccessor(ChallengeResponse::new);
        }
        private static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
        private final Map<String, List<String>> headers;

        /**
         * Creates the ChallengeResponse.
         *
         * @param headers the response headers
         */
        ChallengeResponse(Map<String, List<String>> headers) {
            this.headers = headers;
        }

        /**
         * Gets the authentication schemes supported by the proxy server.
         *
         * @return the authentication schemes supported by the proxy server.
         */
        public List<String> getAuthenticationSchemes() {
            return headers.get(PROXY_AUTHENTICATE);
        }
    }
}
