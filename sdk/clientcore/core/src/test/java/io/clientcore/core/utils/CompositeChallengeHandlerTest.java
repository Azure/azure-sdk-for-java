// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link CompositeChallengeHandler}.
 */
public class CompositeChallengeHandlerTest {
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void firstAvailableChallengeHandlerHandlesChallenge(boolean isProxy) {
        MockHandler digestHandler = new MockHandler(true, false);
        MockHandler basicHandler = new MockHandler(false, true);

        ChallengeHandler challengeHandler = ChallengeHandler.of(digestHandler, basicHandler);
        assertDoesNotThrow(() -> challengeHandler.handleChallenge(null, null, isProxy));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lastAvailableChallengeHandlerHandlesChallenge(boolean isProxy) {
        MockHandler digestHandler = new MockHandler(false, true);
        MockHandler basicHandler = new MockHandler(true, false);

        ChallengeHandler challengeHandler = ChallengeHandler.of(digestHandler, basicHandler);
        assertDoesNotThrow(() -> challengeHandler.handleChallenge(null, null, isProxy));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void noChallengeHandlerHandlesChallenge(boolean isProxy) {
        MockHandler digestHandler = new MockHandler(false, true);
        MockHandler basicHandler = new MockHandler(false, true);

        ChallengeHandler challengeHandler = ChallengeHandler.of(digestHandler, basicHandler);
        assertDoesNotThrow(() -> challengeHandler.handleChallenge(null, null, isProxy));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void firstChallengeHandlerHandlesChallengeWhenAllAreAvailable(boolean isProxy) {
        MockHandler digestHandler = new MockHandler(true, false);
        MockHandler basicHandler = new MockHandler(true, false);

        ChallengeHandler challengeHandler = ChallengeHandler.of(digestHandler, basicHandler);
        assertDoesNotThrow(() -> challengeHandler.handleChallenge(null, null, isProxy));
        assertEquals(1, digestHandler.handleChallengeCount);
        assertEquals(0, basicHandler.handleChallengeCount);
    }

    private static final class MockHandler implements ChallengeHandler {
        private final boolean canHandle;
        private final boolean handleChallengeThrows;

        int handleChallengeCount = 0;

        MockHandler(boolean canHandle, boolean handleChallengeThrows) {
            this.canHandle = canHandle;
            this.handleChallengeThrows = handleChallengeThrows;
        }

        @Override
        public boolean canHandle(Response<BinaryData> response, boolean isProxy) {
            return canHandle;
        }

        @Override
        public boolean canHandle(List<AuthenticateChallenge> challenges) {
            return canHandle;
        }

        @Override
        public void handleChallenge(HttpRequest request, Response<BinaryData> response, boolean isProxy) {
            handleChallengeCount++;
            if (handleChallengeThrows) {
                throw new IllegalStateException("Should not be called");
            }
        }

        @Override
        public Map.Entry<String, AuthenticateChallenge> handleChallenge(String method, URI uri,
            List<AuthenticateChallenge> challenges) {
            handleChallengeCount++;
            if (handleChallengeThrows) {
                throw new IllegalStateException("Should not be called");
            }

            return null;
        }
    }
}
