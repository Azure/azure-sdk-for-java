// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.util.BinaryData;

final class OpenAIResponseFixtures {
    private OpenAIResponseFixtures() {
    }

    static String response(String id, String outputItems) {
        return "{\"id\":\"" + id + "\",\"object\":\"response\",\"created_at\":1,"
            + "\"model\":\"gpt-4o\",\"status\":\"completed\",\"parallel_tool_calls\":true,"
            + "\"tool_choice\":\"auto\",\"tools\":[],\"output\":[" + outputItems + "]}";
    }

    static String message(String id, String text) {
        return message(id, text, "");
    }

    static String message(String id, String text, String annotations) {
        return "{\"id\":\"" + id + "\",\"type\":\"message\",\"role\":\"assistant\","
            + "\"status\":\"completed\",\"content\":[{\"type\":\"output_text\",\"text\":" + BinaryData.fromObject(text)
            + ",\"annotations\":[" + annotations + "]}]}";
    }

    static String functionCall(String id, String callId, String name, String arguments) {
        return "{\"id\":\"" + id + "\",\"type\":\"function_call\",\"call_id\":\"" + callId + "\",\"name\":\"" + name
            + "\",\"arguments\":" + BinaryData.fromObject(arguments) + ",\"status\":\"completed\"}";
    }

    static String promptAgentVersion(String agentName, String version) {
        return "{\"object\":\"agent.version\",\"id\":\"agent-" + version + "\",\"name\":\"" + agentName
            + "\",\"version\":\"" + version + "\",\"created_at\":1,\"metadata\":{},"
            + "\"definition\":{\"kind\":\"prompt\",\"model\":\"gpt-4o\",\"tools\":[]}}";
    }
}
