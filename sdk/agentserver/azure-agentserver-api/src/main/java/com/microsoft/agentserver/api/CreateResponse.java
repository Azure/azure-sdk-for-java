// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.openai.models.responses.Response;

/**
 * Wraps an OpenAI {@link Response} together with an optional {@link AgentReference}
 * for the agent server protocol's create-response endpoint.
 * <p>
 * The {@code response} field is serialized unwrapped (its properties merge into
 * the top-level JSON object), while the {@code agent} field is a nested object.
 *
 * @param agent    the agent that produced this response, or {@code null}
 * @param response the OpenAI Response object
 */
public record CreateResponse(AgentReference agent, @JsonUnwrapped Response response) {
}
