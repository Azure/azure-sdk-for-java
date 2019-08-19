// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.azure.keyvault.messagesecurity.HttpMessageSecurity;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;

import org.apache.commons.lang3.tuple.Pair;

/**
 * An implementation of {@link ServiceClientCredentials} that supports automatic
 * bearer token refresh.
 *
 */
public abstract class KeyVaultCredentials implements ServiceClientCredentials {

    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String BEARER_TOKEP_REFIX = "Bearer ";
    private static final String CLIENT_ENCRYPTION_KEY_TYPE = "RSA";
    private static final int CLIENT_ENCRYPTION_KEY_SIZE = 2048;
    private List<String> supportedMethods = Arrays.asList("sign", "verify", "encrypt", "decrypt", "wrapkey",
            "unwrapkey");

    private JsonWebKey clientEncryptionKey = null;

    private final ChallengeCache cache = new ChallengeCache();

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {

        clientBuilder.addInterceptor(new Interceptor() {

            @Override
            public Response intercept(Chain chain) throws IOException {

                Request originalRequest = chain.request();
                HttpUrl url = chain.request().url();

                Map<String, String> challengeMap = cache.getCachedChallenge(url);
                Response response;
                Pair<Request, HttpMessageSecurity> authenticatedRequestPair;

                if (challengeMap != null) {
                    // challenge is cached, so there is no need to send an empty auth request.
                    authenticatedRequestPair = buildAuthenticatedRequest(originalRequest, challengeMap);
                } else {
                    // challenge is new for the URL and is not cached,
                    // so the request is sent out to get the challenges in
                    // response
                    response = chain.proceed(buildEmptyRequest(originalRequest));

                    if (response.code() !=  401) {
                        return response;
                    }

                    try {
                        authenticatedRequestPair = buildAuthenticatedRequest(originalRequest, response);
                    } finally {
                        response.close();
                    }
                }

                response = chain.proceed(authenticatedRequestPair.getLeft());

                if (response.code() == 200) {
                    return authenticatedRequestPair.getRight().unprotectResponse(response);
                } else {
                    return response;
                }
            }
        });
    }

    /**
     * Builds request with authenticated header. Protects request body if supported.
     *
     * @param originalRequest
     *            unprotected request without auth token.
     * @param challengeMap
     *            the challenge map.
     * @return Pair of protected request and HttpMessageSecurity used for
     *         encryption.
     */
    private Pair<Request, HttpMessageSecurity> buildAuthenticatedRequest(Request originalRequest,
            Map<String, String> challengeMap) throws IOException {

        Boolean supportsPop = supportsMessageProtection(originalRequest.url().toString(), challengeMap);

        // if the service supports pop and a clientEncryptionKey has not been generated yet, generate
        // the key that will be used for encryption on this and all subsequent protected requests
        if (supportsPop && this.clientEncryptionKey == null) {
            try {
                final KeyPairGenerator generator = KeyPairGenerator.getInstance(CLIENT_ENCRYPTION_KEY_TYPE);

                generator.initialize(CLIENT_ENCRYPTION_KEY_SIZE);
                
                this.clientEncryptionKey = JsonWebKey.fromRSA(generator.generateKeyPair()).withKid(UUID.randomUUID().toString());   
                
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        AuthenticationResult authResult = getAuthenticationCredentials(supportsPop, challengeMap);

        if (authResult == null) {
            return null;
        }

        HttpMessageSecurity httpMessageSecurity = new HttpMessageSecurity(authResult.getAuthToken(),
                supportsPop ? authResult.getPopKey() : "",
                supportsPop ? challengeMap.get("x-ms-message-encryption-key") : "",
                supportsPop ? challengeMap.get("x-ms-message-signing-key") : "",
                this.clientEncryptionKey);

        Request request = httpMessageSecurity.protectRequest(originalRequest);
        return Pair.of(request, httpMessageSecurity);
    }

    /**
     * Builds request with authenticated header. Protects request body if supported.
     *
     * @param originalRequest
     *            unprotected request without auth token.
     * @param response
     *            response with unauthorized return code.
     * @return Pair of protected request and HttpMessageSecurity used for
     *         encryption.
     */
    private Pair<Request, HttpMessageSecurity> buildAuthenticatedRequest(Request originalRequest, Response response)
            throws IOException {
        String authenticateHeader = response.header(WWW_AUTHENTICATE);

        Map<String, String> challengeMap = extractChallenge(authenticateHeader, BEARER_TOKEP_REFIX);

        challengeMap.put("x-ms-message-encryption-key", response.header("x-ms-message-encryption-key"));
        challengeMap.put("x-ms-message-signing-key", response.header("x-ms-message-signing-key"));

        // Cache the challenge
        cache.addCachedChallenge(originalRequest.url(), challengeMap);

        return buildAuthenticatedRequest(originalRequest, challengeMap);
    }

    /**
     * Removes request body used for EKV authorization.
     *
     * @param request
     *            unprotected request without auth token.
     * @return request with removed body.
     */
    private Request buildEmptyRequest(Request request) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{}");
        if (request.method().equalsIgnoreCase("get")) {
            return request;
        } else {
            return request.newBuilder().method(request.method(), body).build();
        }
    }

    /**
     * Checks if resource supports message protection.
     *
     * @param url
     *            resource url.
     * @param challengeMap
     *            the challenge map.
     * @return true if message protection is supported.
     */
    private Boolean supportsMessageProtection(String url, Map<String, String> challengeMap) {

        if (!"true".equals(challengeMap.get("supportspop"))) {
            return false;
        }

        // Message protection is enabled only for subset of keys operations.
        if (!url.toLowerCase().contains("/keys/")) {
            return false;
        }

        String[] tokens = url.split("\\?")[0].split("/");
        return supportedMethods.contains(tokens[tokens.length - 1]);
    }

    /**
     * Extracts the authentication challenges from the challenge map and calls the
     * authentication callback to get the bearer token and return it.
     *
     * @param supportsPop
     *            is resource supports pop authentication.
     * @param challengeMap
     *            the challenge map.
     * @return AuthenticationResult with bearer token and PoP key.
     */
    private AuthenticationResult getAuthenticationCredentials(Boolean supportsPop, Map<String, String> challengeMap) {

        String authorization = challengeMap.get("authorization");
        if (authorization == null) {
            authorization = challengeMap.get("authorization_uri");
        }

        String resource = challengeMap.get("resource");
        String scope = challengeMap.get("scope");
        String schema = supportsPop ? "pop" : "bearer";
        return doAuthenticate(authorization, resource, scope, schema);
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
    private static Map<String, String> extractChallenge(String authenticateHeader, String authChallengePrefix) {
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
    private static boolean isValidChallenge(String authenticateHeader, String authChallengePrefix) {
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
     * 
     * @param scope
     *            The scope of the authentication request.
     *
     * @return AuthenticationResult with authorization token and PoP key.
     *
     *         Answers a server challenge with a token header.
     *         <p>
     *         Implementations typically use ADAL to get a token, as performed in
     *         the sample below:
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
     *         <b>Note: The client key must be securely stored. It's advised to use
     *         two client applications - one for development and other for
     *         production - managed by separate parties.</b>
     *         </p>
     *
     */
    public String doAuthenticate(String authorization, String resource, String scope) {
        return "";
    }

    /**
     * Method to be implemented.
     *
     * @param authorization
     *            Identifier of the authority, a URL.
     * @param resource
     *            Identifier of the target resource that is the recipient of the
     *            requested token, a URL.
     * @param scope
     *            The scope of the authentication request.
     *
     * @param schema
     *            Authentication schema. Can be 'pop' or 'bearer'.
     *
     * @return AuthenticationResult with authorization token and PoP key.
     *
     *         Answers a server challenge with a token header.
     *         <p>
     *         Implementations sends POST request to receive authentication token
     *         like in example below. ADAL currently doesn't support POP
     *         authentication.
     *         </p>
     *
     *         <pre>
     *         public AuthenticationResult doAuthenticate(String authorization, String resource, String scope, String schema) {
     *             JsonWebKey clientJwk = GenerateJsonWebKey();
     *             JsonWebKey clientPublicJwk = GetJwkWithPublicKeyOnly(clientJwk);
     *             String token = GetAccessToken(authorization, resource, "pop".equals(schema), clientPublicJwk);
     *
     *             return new AuthenticationResult(token, clientJwk.toString());
     *         }
     *
     *         private JsonWebKey GenerateJsonWebKey() {
     *             final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
     *             generator.initialize(2048);
     *             KeyPair clientRsaKeyPair = generator.generateKeyPair();
     *             JsonWebKey result = JsonWebKey.fromRSA(clientRsaKeyPair);
     *             result.withKid(UUID.randomUUID().toString());
     *             return result;
     *         }
     *
     *         public static JsonWebKey GetJwkWithPublicKeyOnly(JsonWebKey jwk) {
     *             KeyPair publicOnly = jwk.toRSA(false);
     *             JsonWebKey jsonkeyPublic = JsonWebKey.fromRSA(publicOnly);
     *             jsonkeyPublic.withKid(jwk.kid());
     *             jsonkeyPublic.withKeyOps(Arrays.asList(JsonWebKeyOperation.ENCRYPT, JsonWebKeyOperation.WRAP_KEY,
     *                     JsonWebKeyOperation.VERIFY));
     *             return jsonkeyPublic;
     *         }
     *
     *         private String GetAccessToken(String authorization, String resource, boolean supportspop, JsonWebKey jwkPublic) {
     *             CloseableHttpClient httpclient = HttpClients.createDefault();
     *             HttpPost httppost = new HttpPost(authorization + "/oauth2/token");
     * 
     *             // Request parameters and other properties.
     *             List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;(2);
     *             params.add(new BasicNameValuePair("resource", resource));
     *             params.add(new BasicNameValuePair("response_type", "token"));
     *             params.add(new BasicNameValuePair("grant_type", "client_credentials"));
     *             params.add(new BasicNameValuePair("client_id", this.getApplicationId()));
     *             params.add(new BasicNameValuePair("client_secret", this.getApplicationSecret()));
     *
     *             if (supportspop) {
     *                 params.add(new BasicNameValuePair("pop_jwk", jwkPublic.toString()));
     *             }
     *
     *             httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
     *
     *             HttpResponse response = httpclient.execute(httppost);
     *             HttpEntity entity = response.getEntity();
     *
     *             // Read the contents of an entity and return it as a String.
     *             String content = EntityUtils.toString(entity);
     *
     *             ObjectMapper mapper = new ObjectMapper();
     *             authreply reply = mapper.readValue(content, authreply.class);
     *
     *             return reply.access_token;
     *         }
     *         </pre>
     *
     *         <p>
     *         <b>Note: The client key must be securely stored. It's advised to use
     *         two client applications - one for development and other for
     *         production - managed by separate parties.</b>
     *         </p>
     */
    public AuthenticationResult doAuthenticate(String authorization, String resource, String scope, String schema) {
        return new AuthenticationResult(doAuthenticate(authorization, resource, scope), "");
    }
}
