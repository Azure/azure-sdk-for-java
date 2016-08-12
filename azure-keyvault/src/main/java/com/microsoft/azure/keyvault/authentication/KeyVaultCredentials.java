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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.rest.credentials.ServiceClientCredentials;

import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * An implementation of {@link ServiceClientCredentials} that supports automatic bearer token refresh.
 *
 */
public abstract class KeyVaultCredentials implements ServiceClientCredentials {

    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String AUTHENTICATE = "Authorization";
    private static final String BEARER_TOKEP_REFIX = "Bearer ";

    private final ChallengeCache cache = new ChallengeCache();

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {

        clientBuilder.addInterceptor(new Interceptor() {

            @Override
            public Response intercept(Chain chain) throws IOException {

                HttpUrl url = chain.request().url();

                Map<String, String> challengeMap = cache.getCachedChallenge(url);

                if (challengeMap != null) {
                    // Get the bearer token
                    String credential = getAuthenticationCredentials(challengeMap);

                    Request newRequest = chain.request().newBuilder()
                            .header(AUTHENTICATE, BEARER_TOKEP_REFIX + credential).build();

                    return chain.proceed(newRequest);
                } else {
                    // challenge is new for the URL and is not cached,
                    // so the request is sent out to get the challenges in
                    // response
                    return chain.proceed(chain.request());
                }
            }
        });

        // Caches the challenge for failed request and re-send the request with
        // access token.
        clientBuilder.authenticator(new Authenticator() {

            @Override
            public Request authenticate(Route route, Response response) throws IOException {

                // if challenge is not cached then extract and cache it
                String authenticateHeader = response.header(WWW_AUTHENTICATE);

                Map<String, String> challengeMap = extractChallenge(authenticateHeader, BEARER_TOKEP_REFIX);

                // Cache the challenge
                cache.addCachedChallenge(response.request().url(), challengeMap);

                // Get the bearer token from the callback by providing the
                // challenges
                String credential = getAuthenticationCredentials(challengeMap);

                if (credential == null) {
                    return null;
                }

                // Add the token header and resume the call.
                // The token should live for duration of this request and never
                // be cached anywhere in our code.
                return response.request().newBuilder().header(AUTHENTICATE, BEARER_TOKEP_REFIX + credential).build();
            }
        });
    }

    /**
     * Extracts the authentication challenges from the challenge map and calls
     * the authentication callback to get the bearer token and return it.
     * 
     * @param challengeMap
     *            the challenge map.
     * @return the bearer token.
     */
    private String getAuthenticationCredentials(Map<String, String> challengeMap) {

        String authorization = challengeMap.get("authorization");
        if (authorization == null) {
            authorization = challengeMap.get("authorization_uri");
        }

        String resource = challengeMap.get("resource");
        String scope = challengeMap.get("scope");

        return doAuthenticate(authorization, resource, scope);
    }

    /**
     * Extracts the challenge off the authentication header.
     * 
     * @param authenticateHeader
     *            the authentication header containing all the challenges.
     * @param authChallengePrefix
     *            the authentication challenge name.
     * @return a challenge map.
     */
    private Map<String, String> extractChallenge(String authenticateHeader, String authChallengePrefix) {
        if (!isValidChallenge(authenticateHeader, authChallengePrefix)) {
            return null;
        }

        authenticateHeader = authenticateHeader.toLowerCase().replace(authChallengePrefix.toLowerCase(), "");

        String[] challenges = authenticateHeader.split(", ");
        Map<String, String> challengeMap = new HashMap<String, String>();
        for (String pair : challenges) {
            String[] keyValue = pair.split("=");
            challengeMap.put(keyValue[0].replaceAll("\"", ""), keyValue[1].replaceAll("\"", ""));
        }
        return challengeMap;
    }

    /**
     * Verifies whether a challenge is bearer or not.
     * 
     * @param authenticateHeader
     *            the authentication header containing all the challenges.
     * @param authChallengePrefix
     *            the authentication challenge name.
     * @return
     */
    private boolean isValidChallenge(String authenticateHeader, String authChallengePrefix) {
        if (authenticateHeader != null && !authenticateHeader.isEmpty()
                && authenticateHeader.toLowerCase().startsWith(authChallengePrefix.toLowerCase())) {
            return true;
        }
        return false;
    }

    /**
     * Abstract method to be implemented.
     * 
     * @param authorization
     *            Identifier of the authority, a URL.
     * @param resource
     *            Identifier of the target resource that is the recipient of the
     *            requested token, a URL.
     * @param scope
     *            The scope of the authentication request.
     * 
     * @return The access token
     * 
     *         Answers a server challenge with a token header.
     *         <p>
     *         Implementations typically use ADAL to get a token, as performed
     *         in the sample below:
     *         </p>
     *
     *         <pre>
     * &#064;Override
     * public String doAuthenticate(String authorization, String resource, String scope) {
     *     String clientId = ...; // client GUID as shown in Azure portal.
     *     String clientKey = ...; // client key as provided by Azure portal.
     *     AuthenticationResult token = getAccessTokenFromClientCredentials(authorization, resource, clientId, clientKey);
     *     return token.getAccessToken();;
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
     *         </pre>
     *
     *         <p>
     *         <b>Note: The client key must be securely stored. It's advised to
     *         use two client applications - one for development and other for
     *         production - managed by separate parties.</b>
     *         </p>
     *
     */
    public abstract String doAuthenticate(String authorization, String resource, String scope);

}
