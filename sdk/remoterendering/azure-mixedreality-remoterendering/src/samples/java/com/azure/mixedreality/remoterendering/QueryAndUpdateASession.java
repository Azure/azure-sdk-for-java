// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.mixedreality.remoterendering.models.BeginSessionOptions;
import com.azure.mixedreality.remoterendering.models.RenderingSession;
import com.azure.mixedreality.remoterendering.models.RenderingSessionSize;
import com.azure.mixedreality.remoterendering.models.UpdateSessionOptions;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Sample class demonstrating how to query and update a rendering session.
 */
public class QueryAndUpdateASession extends SampleBase {

    /**
     * Main method to invoke this demo about how to query and update a rendering session.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        new QueryAndUpdateASession().queryAndUpdateASession();
    }

    /**
     * Sample method demonstrating how to query and update a rendering session.
     *
     * To avoid launching too many sessions during testing, we rely on the live tests.
     */
    public void queryAndUpdateASession() {
        String sessionId = UUID.randomUUID().toString();

        BeginSessionOptions options = new BeginSessionOptions()
            .setMaxLeaseTime(Duration.ofMinutes(30))
            .setSize(RenderingSessionSize.STANDARD);

        client.beginSession(sessionId, options).getFinalResult();

        RenderingSession currentSession = client.getSession(sessionId);

        Duration sessionTimeAlive = Duration.between(OffsetDateTime.now(), currentSession.getCreationTime()).abs();
        if (currentSession.getMaxLeaseTime().minus(sessionTimeAlive).toMinutes() < 2) {
            Duration newLeaseTime = currentSession.getMaxLeaseTime().plus(Duration.ofMinutes(30));
            UpdateSessionOptions longerLeaseOptions = new UpdateSessionOptions().maxLeaseTime(newLeaseTime);
            client.updateSession(sessionId, longerLeaseOptions);
        }

        client.endSession(sessionId);
    }

}
