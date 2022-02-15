// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.util.polling.SyncPoller;
import com.azure.mixedreality.remoterendering.models.BeginSessionOptions;
import com.azure.mixedreality.remoterendering.models.RenderingSession;
import com.azure.mixedreality.remoterendering.models.RenderingSessionSize;
import com.azure.mixedreality.remoterendering.models.RenderingSessionStatus;

import java.time.Duration;
import java.util.UUID;

/**
 * Sample class demonstrating how to create a rendering session.
 */
public class CreateRenderingSession extends SampleBase
{
    /**
     * Main method to invoke this demo about how to create a rendering session.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        new CreateRenderingSession().createRenderingSession();
    }

    /**
     * Sample method demonstrating how to create a rendering session.
     *
     * To avoid launching too many sessions during testing, we rely on the live tests.
     */
    public void createRenderingSession() {
        BeginSessionOptions options = new BeginSessionOptions()
            .setMaxLeaseTime(Duration.ofMinutes(30))
            .setSize(RenderingSessionSize.STANDARD);

        // A randomly generated GUID is a good choice for a sessionId.
        String sessionId = UUID.randomUUID().toString();

        SyncPoller<RenderingSession, RenderingSession> startSessionOperation = client.beginSession(sessionId, options);

        RenderingSession session = startSessionOperation.getFinalResult();
        if (session.getStatus() == RenderingSessionStatus.READY) {
            logger.error("Session {} is ready.", session.getId());
        } else if (session.getStatus() == RenderingSessionStatus.ERROR) {
            logger.error("Session {} encountered an error: {} {}", session.getId(), session.getError().getCode(), session.getError().getMessage());
        } else {
            logger.error("Got unexpected session status: {}", session.getStatus());
        }

        // Use the session here.
        // ...

        // The session will automatically timeout, but in this sample we also demonstrate how to shut it down explicitly.
        client.endSession(sessionId);
    }
}
