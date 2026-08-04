// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.openai.models.responses.ResponseStreamEvent;

/**
 * Wraps a {@link ResponseStreamEvent} together with its SSE event name.
 * Produced by {@link AgentServerResponseEventStream} emit methods and consumed by the SSE transport layer.
 *
 * @param eventName   the SSE event name, e.g. {@code "response.created"}, {@code "response.output_text.delta"}
 * @param streamEvent the wrapped OpenAI SDK stream event union
 */
public record ResponseEvent(String eventName, ResponseStreamEvent streamEvent) {
}
