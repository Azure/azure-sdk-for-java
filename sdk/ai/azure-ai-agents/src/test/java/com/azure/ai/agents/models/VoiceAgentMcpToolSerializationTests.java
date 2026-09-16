// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class VoiceAgentMcpToolSerializationTests {

    @Test
    public void allowedToolsVariantsRoundTrip() throws IOException {
        VoiceAgentMcpTool listResult
            = roundTrip(new VoiceAgentMcpTool("server").setAllowedTools(Arrays.asList("search", "browse")));
        assertEquals(Arrays.asList("search", "browse"), listResult.getAllowedToolsAsStringList());
        assertNull(listResult.getAllowedToolsAsMcpToolFilter());

        McpToolFilter filter = new McpToolFilter().setToolNames(Arrays.asList("search"));
        VoiceAgentMcpTool modelResult = roundTrip(new VoiceAgentMcpTool("server").setAllowedTools(filter));
        assertEquals(Arrays.asList("search"), modelResult.getAllowedToolsAsMcpToolFilter().getToolNames());
        assertNull(modelResult.getAllowedToolsAsStringList());
    }

    @Test
    public void requireApprovalVariantsRoundTrip() throws IOException {
        VoiceAgentMcpTool stringResult = roundTrip(new VoiceAgentMcpTool("server").setRequireApproval("never"));
        assertEquals("never", stringResult.getRequireApprovalAsString());
        assertNull(stringResult.getRequireApprovalAsMcpToolRequireApproval());

        McpToolRequireApproval approval
            = new McpToolRequireApproval().setAlways(new McpToolFilter().setToolNames(Arrays.asList("search")));
        VoiceAgentMcpTool modelResult = roundTrip(new VoiceAgentMcpTool("server").setRequireApproval(approval));
        assertEquals(Arrays.asList("search"),
            modelResult.getRequireApprovalAsMcpToolRequireApproval().getAlways().getToolNames());
        assertNull(modelResult.getRequireApprovalAsString());
    }

    @Test
    public void absentAndNullValuesReturnNull() throws IOException {
        VoiceAgentMcpTool result = UnionTypeSerializationTestUtils
            .deserialize("{\"type\":\"mcp\",\"server_label\":\"server\"}", VoiceAgentMcpTool::fromJson);
        assertNull(result.getAllowedToolsAsStringList());
        assertNull(result.getAllowedToolsAsMcpToolFilter());
        assertNull(result.getRequireApprovalAsString());
        assertNull(result.getRequireApprovalAsMcpToolRequireApproval());

        result.setAllowedTools(Arrays.asList("search")).setAllowedTools((McpToolFilter) null);
        result.setRequireApproval("always").setRequireApproval((String) null);
        assertNull(result.getAllowedToolsAsStringList());
        assertNull(result.getRequireApprovalAsString());
    }

    private VoiceAgentMcpTool roundTrip(VoiceAgentMcpTool value) throws IOException {
        String json = UnionTypeSerializationTestUtils.serialize(value);
        return UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentMcpTool::fromJson);
    }
}
