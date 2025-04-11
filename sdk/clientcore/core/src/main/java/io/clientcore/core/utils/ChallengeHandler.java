// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class representing a challenge handler for authentication.
 */
public interface ChallengeHandler {
    /**
     * Handles the authentication challenge based on the HTTP request and response.
     *
     * @param request The HTTP request to be updated with authentication info.
     * @param response The HTTP response containing the authentication challenge.
     * @param isProxy Indicates if the challenge is for a proxy.
     * @throws NullPointerException If {@code request} or {@code response} is null.
     */
    void handleChallenge(HttpRequest request, Response<BinaryData> response, boolean isProxy);

    /**
     * Validate if this ChallengeHandler can handle the provided challenge by inspecting the {@code Proxy-Authenticate}
     * or {@code WWW-Authenticate} headers.
     * <p>
     * Use of {@code Proxy-Authenticate} or {@code WWW-Authenticate} is based on {@code isProxy}.
     * <p>
     * This method will return true if at least one of the authenticate challenges in the response header can be handled
     * by this ChallengeHandler. Meaning, if {@link #of(ChallengeHandler...)} is used ordering of the provided
     * ChallengeHandlers is important, where if more secure handling is needed positioning
     * {@link DigestChallengeHandler}, or a custom ChallengeHandler for more secure auth mechanisms, before
     * {@link BasicChallengeHandler} is necessary.
     *
     * @param response The HTTP response containing the authentication challenge.
     * @param isProxy boolean indicating if it is a proxy challenge handler.
     * @return Whether a challenge within the {@link Response#getHeaders()} {@code Proxy-Authenticate} or
     * {@code WWW-Authenticate} can be handled.
     * @throws NullPointerException If {@code response} is null.
     */
    boolean canHandle(Response<BinaryData> response, boolean isProxy);

    /**
     * Creates a {@code Proxy-Authorization} or {@code WWW-Authorization} compliant header from the given
     * {@link AuthenticateChallenge}s.
     * <p>
     * It is left to the ChallengeHandler implementation to decide which of the {@link AuthenticateChallenge}s it
     * {@link #canHandle(List)} to use when creating the header.
     * <p>
     * If none of the {@link AuthenticateChallenge}s can be handled null will be returned.
     *
     * @param method The HTTP method used in the request being authorized.
     * @param uri The URI for the HTTP request being authorized.
     * @param challenges The HTTP authenticate challenges, either from {@code Proxy-Authenticate} or
     * {@code WWW-Authenticate} headers.
     * @return A {@link Map.Entry} where the key is the {@code Proxy-Authorization} or {@code WWW-Authorization}
     * compliant header and value is the {@link AuthenticateChallenge} used to generate the header, or null if none of
     * the challenges can be handled.
     */
    Map.Entry<String, AuthenticateChallenge> handleChallenge(String method, URI uri,
        List<AuthenticateChallenge> challenges);

    /**
     * Validate if this ChallengeHandler can handle any of the provided {@link AuthenticateChallenge}s.
     * <p>
     * This method is meant for scenarios where authenticate headers are processed into {@link AuthenticateChallenge}s
     * externally, normally using {@link AuthUtils#parseAuthenticateHeader(String)}.
     *
     * @param challenges The HTTP authenticate challenges, either from {@code Proxy-Authenticate} or
     * {@code WWW-Authenticate} headers.
     * @return Whether any {@link AuthenticateChallenge} can be handled.
     * @throws NullPointerException If {@code challenges} is null.
     */
    boolean canHandle(List<AuthenticateChallenge> challenges);

    /**
     * Factory method for creating composite handlers.
     *
     * @param handlers The array of ChallengeHandler instances to be combined.
     * @return A CompositeChallengeHandler that combines the provided handlers.
     */
    static ChallengeHandler of(ChallengeHandler... handlers) {
        return new CompositeChallengeHandler(Arrays.asList(handlers));
    }
}
