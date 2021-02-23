// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.mixedreality.remoterendering.models.BeginSessionOptions;
import com.azure.mixedreality.remoterendering.models.RenderingSession;
import com.azure.mixedreality.remoterendering.models.RenderingSessionSize;
import com.azure.mixedreality.remoterendering.models.RenderingSessionStatus;

import java.time.Duration;
import java.util.UUID;

/**
 * Sample class demonstrating how to list rendering sessions.
 */
public class ListRenderingSessions extends SampleBase {

    /**
     * Main method to invoke this demo about how to list rendering sessions.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        new ListRenderingSessions().listRenderingSessions();
    }

    /**
     * Sample method demonstrating how to list rendering sessions.
     *
     * To avoid launching too many sessions during testing, we rely on the live tests.
     */
    public void listRenderingSessions()  {
        // Ensure there's at least one session to query.
        String sessionId = UUID.randomUUID().toString();

        BeginSessionOptions options = new BeginSessionOptions()
            .setMaxLeaseTime(Duration.ofMinutes(30))
            .setSize(RenderingSessionSize.STANDARD);

        client.beginSession(sessionId, options);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ignored) {
        }

        for (RenderingSession session : client.listSessions()) {
            if (session.getStatus() == RenderingSessionStatus.STARTING) {
                logger.info("Session {} is starting.");
            } else if (session.getStatus() == RenderingSessionStatus.READY) {
                logger.info("Session {} is ready at host {}", session.getId(), session.getHostname());
            } else if (session.getStatus() == RenderingSessionStatus.ERROR) {
                logger.error("Session {} encountered an error: {} {}", session.getId(), session.getError().getCode(), session.getError().getMessage());
            } else {
                logger.error("Session {} has unexpected status {}", session.getId(), session.getStatus());
            }
        }

        client.endSession(sessionId);
    }

}
