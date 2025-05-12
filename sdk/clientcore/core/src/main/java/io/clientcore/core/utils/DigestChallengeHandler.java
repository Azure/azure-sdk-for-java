// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

import java.net.URI;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.clientcore.core.utils.AuthUtils.ALGORITHM;
import static io.clientcore.core.utils.AuthUtils.DIGEST;
import static io.clientcore.core.utils.AuthUtils.MD5;
import static io.clientcore.core.utils.AuthUtils.SESS;
import static io.clientcore.core.utils.AuthUtils.SHA_512_256;
import static io.clientcore.core.utils.AuthUtils.buildAuthorizationHeader;
import static io.clientcore.core.utils.AuthUtils.bytesToHexString;
import static io.clientcore.core.utils.AuthUtils.calculateHa1NoSess;
import static io.clientcore.core.utils.AuthUtils.calculateHa1Sess;
import static io.clientcore.core.utils.AuthUtils.calculateHa2AuthIntQop;
import static io.clientcore.core.utils.AuthUtils.calculateHa2AuthQopOrEmpty;
import static io.clientcore.core.utils.AuthUtils.calculateResponseKnownQop;
import static io.clientcore.core.utils.AuthUtils.calculateResponseUnknownQop;
import static io.clientcore.core.utils.AuthUtils.calculateUserhash;
import static io.clientcore.core.utils.AuthUtils.getDigestFunction;
import static io.clientcore.core.utils.AuthUtils.partitionByChallengeType;

/**
 * Handles Digest authentication challenges.
 *
 * <p>This class is responsible for creating and managing Digest authentication headers
 * based on the provided username, password, and authentication challenges.</p>
 *
 * @see ChallengeHandler
 */
public class DigestChallengeHandler implements ChallengeHandler {
    private final String username;
    private final String password;
    private static final String REALM = "realm";
    private static final String NONCE = "nonce";
    private static final String QOP = "qop";
    private static final String AUTH = "auth";
    private static final String AUTH_INT = "auth-int";
    private static final String USERHASH = "userhash";
    private static final String OPAQUE = "opaque";
    private final SecureRandom nonceGenerator = new SecureRandom();
    private final Map<String, AtomicInteger> nonceTracker = new ConcurrentHashMap<>();

    private static final String SHA_512_256_SESS = SHA_512_256 + SESS;
    private static final String SHA_256 = "SHA-256";
    private static final String SHA_256_SESS = SHA_256 + SESS;
    private static final String MD5_SESS = MD5 + SESS;

    // Algorithm preference order
    private static final String[] ALGORITHM_PREFERENCE_ORDER
        = { SHA_512_256, SHA_512_256_SESS, SHA_256, SHA_256_SESS, MD5, MD5_SESS };

    /**
     * Creates an {@link DigestChallengeHandler} using the {@code username} and {@code password} to respond to
     * authentication challenges.
     *
     * @param username Username used to response to authorization challenges.
     * @param password Password used to respond to authorization challenges.
     * @throws NullPointerException If {@code username} or {@code password} are {@code null}.
     */
    public DigestChallengeHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void handleChallenge(HttpRequest request, Response<BinaryData> response, boolean isProxy) {
        if (!canHandle(response, isProxy)) {
            return;
        }

        // Extract the algorithm if present
        Map<String, List<Map<String, String>>> challengesByType = partitionByChallengeType(response.getHeaders());

        for (String algorithm : ALGORITHM_PREFERENCE_ORDER) {
            List<Map<String, String>> challengeForType = challengesByType.get(algorithm);
            if (CoreUtils.isNullOrEmpty(challengeForType)) {
                continue;
            }

            Function<byte[], byte[]> digestFunction = getDigestFunction(algorithm);
            if (digestFunction == null) {
                continue;
            }

            // Arbitrarily select the first set of challenge parameters for this algorithm type.
            // TODO (alzimmer): If there aren't features we support, such as the possibility of qop=auth-int, we'll need
            //  to include that as part of the filtering.
            Map<String, String> challengeParams
                = challengeForType.stream().filter(params -> !CoreUtils.isNullOrEmpty(params)).findFirst().orElse(null);
            if (challengeParams == null) {
                continue;
            }

            // Generate Digest Authorization header
            String digestAuthHeader = generateDigestAuthHeader(request.getHttpMethod().name(),
                request.getUri().toString(), algorithm, challengeParams, digestFunction, response.getValue());

            HttpHeaderName headerName = isProxy ? HttpHeaderName.PROXY_AUTHORIZATION : HttpHeaderName.AUTHORIZATION;
            request.getHeaders().set(headerName, digestAuthHeader);
            return;
        }
    }

    @Override
    public boolean canHandle(Response<BinaryData> response, boolean isProxy) {
        HttpHeaders responseHeaders = response.getHeaders();
        if (responseHeaders == null) {
            return false;
        }

        HttpHeaderName authHeaderName = isProxy ? HttpHeaderName.PROXY_AUTHENTICATE : HttpHeaderName.WWW_AUTHENTICATE;
        List<String> authenticateHeaders = responseHeaders.getValues(authHeaderName);
        if (CoreUtils.isNullOrEmpty(authenticateHeaders)) {
            return false;
        }

        for (String authenticateHeader : authenticateHeaders) {
            for (AuthenticateChallenge challenge : AuthUtils.parseAuthenticateHeader(authenticateHeader)) {
                if (canHandle(challenge)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Map.Entry<String, AuthenticateChallenge> handleChallenge(String method, URI uri,
        List<AuthenticateChallenge> challenges) {
        Objects.requireNonNull(challenges, "Cannot use a null 'challenges' to handle challenges.");
        if (!canHandle(challenges)) {
            return null;
        }

        // Bucket the Digest AuthenticateChallenges by their 'algorithm'. We want to prefer stronger algorithms
        // such as SHA256 over MD5.
        // Extract the challenges from the headers, specifically the "Proxy-Authenticate" or "WWW-Authenticate" headers
        Map<String, List<AuthenticateChallenge>> challengesByType
            = challenges.stream().collect(Collectors.groupingBy(challenge -> {
                String algorithmHeader = challenge.getParameters().get(ALGORITHM);

                // RFC7616 specifies that if the "algorithm" header is null, it defaults to MD5.
                return CoreUtils.isNullOrEmpty(algorithmHeader) ? MD5 : algorithmHeader.toUpperCase(Locale.ROOT);
            }));

        for (String algorithm : ALGORITHM_PREFERENCE_ORDER) {
            List<AuthenticateChallenge> challengeForType = challengesByType.get(algorithm);
            if (CoreUtils.isNullOrEmpty(challengeForType)) {
                continue;
            }

            Function<byte[], byte[]> digestFunction = getDigestFunction(algorithm);
            if (digestFunction == null) {
                continue;
            }

            // Arbitrarily select the first set of challenge parameters for this algorithm type.
            // TODO (alzimmer): If there aren't features we support, such as the possibility of qop=auth-int, we'll need
            //  to include that as part of the filtering.
            AuthenticateChallenge authenticateChallenge = challengeForType.stream()
                .filter(challenge -> !CoreUtils.isNullOrEmpty(challenge.getParameters()))
                .findFirst()
                .orElse(null);
            if (authenticateChallenge == null) {
                continue;
            }

            // Generate Digest Authorization header
            String path = uri.getPath();
            if (path == null) {
                path = "/";
            }
            return new AbstractMap.SimpleImmutableEntry<>(generateDigestAuthHeader(method, path, algorithm,
                authenticateChallenge.getParameters(), digestFunction, BinaryData.empty()), authenticateChallenge);
        }

        return null;
    }

    @Override
    public boolean canHandle(List<AuthenticateChallenge> challenges) {
        Objects.requireNonNull(challenges, "Cannot use a null 'challenges' to determine if it can be handled.");
        for (AuthenticateChallenge challenge : challenges) {
            if (canHandle(challenge)) {
                return true;
            }
        }

        return false;
    }

    private static boolean canHandle(AuthenticateChallenge challenge) {
        return challenge != null && DIGEST.equalsIgnoreCase(challenge.getScheme());
    }

    /*
     * Creates a unique and secure nonce.
     */
    String generateCnonce() {
        byte[] nonce = new byte[16];
        nonceGenerator.nextBytes(nonce);
        return bytesToHexString(nonce);
    }

    private String generateDigestAuthHeader(String method, String uri, String algorithm,
        Map<String, String> challengeParams, Function<byte[], byte[]> digestFunction, BinaryData body) {
        String nonce = challengeParams.get(NONCE);
        String realm = challengeParams.get(REALM);
        String qop = challengeParams.get(QOP);
        String opaque = challengeParams.get(OPAQUE);
        boolean hashUsername = Boolean.parseBoolean(challengeParams.get(USERHASH));
        /*
         * If the algorithm being used is <algorithm>-sess or QOP is 'auth' or 'auth-int' a client nonce will be needed
         * to calculate the authorization header. If the QOP is set a nonce-count will need to retrieved.
         */
        int nc = 0;
        String clientNonce = null;
        if (AUTH.equals(qop) || AUTH_INT.equals(qop)) {
            clientNonce = generateCnonce();
            nc = getOrUpdateNonceCount(nonce);
        } else if (algorithm.endsWith(SESS)) {
            clientNonce = generateCnonce();
        }

        String ha1 = algorithm.endsWith(SESS)
            ? calculateHa1Sess(digestFunction, username, realm, password, nonce, clientNonce)
            : calculateHa1NoSess(digestFunction, username, realm, password);

        String ha2 = AUTH_INT.equals(qop)
            ? calculateHa2AuthIntQop(digestFunction, method, uri, body.toBytes())
            : calculateHa2AuthQopOrEmpty(digestFunction, method, uri);

        String response = (AUTH.equals(qop) || AUTH_INT.equals(qop))
            ? calculateResponseKnownQop(digestFunction, ha1, nonce, nc, clientNonce, qop, ha2)
            : calculateResponseUnknownQop(digestFunction, ha1, nonce, ha2);

        String headerUsername = (hashUsername) ? calculateUserhash(digestFunction, username, realm) : username;

        return buildAuthorizationHeader(headerUsername, realm, uri, algorithm, nonce, nc, clientNonce, qop, response,
            opaque, hashUsername);
    }

    /*
     * Retrieves the nonce count for the given challenge. If the nonce in the challenge has already been used this will
     * increment and return the nonce count tracking, otherwise this will begin a new nonce tracking and return 1.
     */
    private int getOrUpdateNonceCount(String nonce) {
        return nonceTracker.compute(nonce, (ignored, value) -> {
            if (value == null) {
                return new AtomicInteger(1);
            }

            value.incrementAndGet();
            return value;
        }).get();
    }
}
