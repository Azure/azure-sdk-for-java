// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.openai.models.responses.inputitems.ResponseItemList;

/**
 * Wraps a {@link ResponseItemList} together with an optional {@link AgentReference}
 * for the agent server protocol's list-input-items response.
 *
 * @param agent            the agent reference, or {@code null} if not applicable
 * @param responseItemList the paginated list of response items
 */
public record AgentServerResponseItemList(AgentReference agent, @JsonUnwrapped ResponseItemList responseItemList) {
}
