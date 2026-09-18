// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.utils.Beta;

/** Action taken when a voice-agent session's bounded receive queue fills. */
@Beta(warningText = "Preview API. VoiceAgents=V1Preview")
public enum VoiceAgentWebSocketOverflowStrategy {
    /** Terminate the connection with an error. No overflow is silently ignored. */
    ERROR,
    /** Discard the oldest buffered event to accept the new event. This loses data. */
    DROP_OLDEST,
    /** Discard the incoming event. This loses data. */
    DROP_LATEST
}
