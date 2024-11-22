// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

/**
 * Tests for {@link ChallengeHandler.CompositeChallengeHandler}.
 */
public class CompositeChallengeHandlerTest {
    private ChallengeHandler.CompositeChallengeHandler compositeChallengeHandler;

    @Mock
    private DigestChallengeHandler digestHandler;
    @Mock
    private BasicChallengeHandler basicHandler;
    @Mock
    private HttpRequest request;
    @Mock
    private Response<?> response;

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Initializing composite with mocked challenge handlers
        compositeChallengeHandler
            = new ChallengeHandler.CompositeChallengeHandler(Arrays.asList(digestHandler, basicHandler));
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testDigestHandlerHandlesChallengeWhenAvailable() {
        when(digestHandler.canHandle(response, false)).thenReturn(true);

        compositeChallengeHandler.handleChallenge(request, response, false);

        // Digest handler should be called
        verify(digestHandler).handleChallenge(request, response, false);
        verify(basicHandler, never()).handleChallenge(request, response, false);
    }

    @Test
    public void testBasicHandlerHandlesChallengeWhenDigestNotAvailable() {
        when(digestHandler.canHandle(response, false)).thenReturn(false);
        when(basicHandler.canHandle(response, false)).thenReturn(true);

        compositeChallengeHandler.handleChallenge(request, response, false);

        // Basic handler should be called
        verify(digestHandler, never()).handleChallenge(request, response, false);
        verify(basicHandler).handleChallenge(request, response, false);
    }

    @Test
    public void testNoHandlerHandlesChallengeLogsErrorIsNoOp() {
        when(digestHandler.canHandle(response, false)).thenReturn(false);
        when(basicHandler.canHandle(response, false)).thenReturn(false);

        compositeChallengeHandler.handleChallenge(request, response, false);

        verify(digestHandler, never()).handleChallenge(request, response, false);
        verify(basicHandler, never()).handleChallenge(request, response, false);
    }

    @Test
    public void testDigestHandlerHandlesProxyChallenge() {
        when(digestHandler.canHandle(response, true)).thenReturn(true);

        compositeChallengeHandler.handleChallenge(request, response, true);

        // Digest handler should handle the proxy challenge
        verify(digestHandler).handleChallenge(request, response, true);
        verify(basicHandler, never()).handleChallenge(request, response, true);
    }

    @Test
    public void testBasicHandlerHandlesProxyChallengeWhenDigestNotAvailable() {
        when(digestHandler.canHandle(response, true)).thenReturn(false);
        when(basicHandler.canHandle(response, true)).thenReturn(true);

        compositeChallengeHandler.handleChallenge(request, response, true);

        // Basic handler should handle the proxy challenge
        verify(digestHandler, never()).handleChallenge(request, response, true);
        verify(basicHandler).handleChallenge(request, response, true);
    }
}
