/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.authentication;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Header;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.credentials.CloudCredentials;
import org.apache.http.auth.AUTH;

/**
 * An implementation of {@link CloudCredentials} that supports automatic bearer
 * token refresh.
 *
 */
public abstract class KeyVaultCredentials extends CloudCredentials implements BearerCredentialsSupport {

    private static final String AUTH_FILTERS_KEY = "AuthFilter";

    private final HashMap<String, Map<String, String>> cachedChallenges = new HashMap<String, Map<String, String>>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> void applyConfig(String profile, Map<String, Object> properties) {

        // Modifies properties to add our authenticating filter.

        properties.put(AUTH_FILTERS_KEY, new ServiceRequestFilter() {

            @Override
            public void filter(ServiceRequestContext request) {

                // Look for a cached challenge for this authority.
                String authority = getAuthority(request);
                Map<String, String> challenge = getCachedChallenge(authority);

                if (challenge == null) {
                    // No cached challenge means this is the first request on
                    // that authority.
                    // Let's send the request without any token. Typically the
                    // server will return a challenge.
                    return;
                }

                // There is a cached challenge. We assume the challenge is the
                // same of a previous request,
                // so we ask for a token header that matches the previous
                // challenge.
                Header header = doAuthenticate(request, challenge);
                if (header == null) {
                    return;
                }

                // Add the token header and resume the call.
                // The token should live for duration of this request and never
                // be cached anywhere in our code.
                request.setHeader(header.getName(), header.getValue());
            }

        });
    }

    @Override
    public Header authenticate(ServiceRequestContext request, BearerAuthentication authentication) {

        // This method is called when the server answers with a 401 -
        // Unauthorized and a challenge.
        // This method must return a token header that answers the challenge.
        // This header is added in a retry.

        // Let's cache the challenge for the current authority, avoiding future
        // 401 answers.
        Map<String, String> challenge = authentication.getParameters();
        challenge = new HashMap<String, String>(challenge); // Defensive copy.
        challenge = Collections.unmodifiableMap(challenge);
        String authority = getAuthority(request);
        addCachedChallenge(authority, challenge);

        // Asks the callback to perform authentication.
        return doAuthenticate(request, challenge);
    }

    /**
     * Answers a server challenge with a token header.
     * <p>
     * Implementations typically use ADAL to get a token, as performed in the
     * sample below:
     * </p>
     *
     * <pre>
     * &#064;Override
     * public Header doAuthenticate(ServiceRequestContext request, Map&lt;String, String&gt; challenge) {
     *     String authorization = challenge.get(&quot;authorization&quot;);
     *     String resource = challenge.get(&quot;resource&quot;);
     *     String clientId = ...; // client GUID as shown in Azure portal.
     *     String clientKey = ...; // client key as provided by Azure portal.
     *     AuthenticationResult token = getAccessTokenFromClientCredentials(authorization, resource, clientId, clientKey);
     *     return new BasicHeader(&quot;Authorization&quot;, token.getAccessTokenType() + &quot; &quot; + token.getAccessToken());
     * }
     *
     * private static AuthenticationResult getAccessTokenFromClientCredentials(String authorization, String resource, String clientId, String clientKey) {
     *     AuthenticationContext context = null;
     *     AuthenticationResult result = null;
     *     ExecutorService service = null;
     *     try {
     *         service = Executors.newFixedThreadPool(1);
     *         context = new AuthenticationContext(authorization, false, service);
     *         ClientCredential credentials = new ClientCredential(clientId, clientKey);
     *         Future&lt;AuthenticationResult&gt; future = context.acquireToken(resource, credentials, null);
     *         result = future.get();
     *     } catch (Exception e) {
     *         throw new RuntimeException(e);
     *     } finally {
     *         service.shutdown();
     *     }
     *
     *     if (result == null) {
     *         throw new RuntimeException(&quot;authentication result was null&quot;);
     *     }
     *     return result;
     * }
     * </pre>
     *
     * <p>
     * <b>Note: The client key must be securely stored. It's advised to use two
     * client applications - one for development and other for production -
     * managed by separate parties.</b>
     * </p>
     *
     */
    public abstract Header doAuthenticate(ServiceRequestContext request, Map<String, String> challenge);

    private Map<String, String> getCachedChallenge(String authority) {
        authority = authority.toLowerCase(Locale.ENGLISH);
        return cachedChallenges.get(authority);
    }

    private void addCachedChallenge(String authority, Map<String, String> challenge) {
        authority = authority.toLowerCase(Locale.ENGLISH);
        cachedChallenges.put(authority, challenge);
    }

    private static String getAuthority(ServiceRequestContext request) {
        return getAuthority(request.getFullURI());
    }

    private static String getAuthority(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        StringBuilder builder = new StringBuilder();
        if (scheme != null) {
            builder.append(scheme).append("://");
        }
        builder.append(host);
        if (port >= 0) {
            builder.append(':').append(port);
        }
        return builder.toString();
    }

}
