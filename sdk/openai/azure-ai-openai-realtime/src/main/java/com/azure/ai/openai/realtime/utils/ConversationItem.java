// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.realtime.utils;

import com.azure.ai.openai.realtime.models.ConversationItemCreateEvent;
import com.azure.ai.openai.realtime.models.RealtimeRequestAssistantMessageItem;
import com.azure.ai.openai.realtime.models.RealtimeRequestFunctionCallItem;
import com.azure.ai.openai.realtime.models.RealtimeRequestFunctionCallOutputItem;
import com.azure.ai.openai.realtime.models.RealtimeRequestSystemMessageItem;
import com.azure.ai.openai.realtime.models.RealtimeRequestTextContentPart;
import com.azure.ai.openai.realtime.models.RealtimeRequestUserMessageItem;
import com.azure.core.util.BinaryData;

import java.util.Arrays;

/**
 * Convenience class grouping methods to create different text-based conversation items with different
 * {@link com.azure.ai.openai.realtime.models.RealtimeMessageRole} roles.
 */
public final class ConversationItem {

    /**
     * Create a user message item.
     *
     * @param itemText the text of the user message.
     * @return the user message item.
     */
    public static ConversationItemCreateEvent createUserMessage(String itemText) {
        // `itemText` is not a JSON string, but a prompt represented as a JSON Object.
        // Therefore, BinaryData.fromObject is the appropriate representation.
        RealtimeRequestUserMessageItem messageItem = new RealtimeRequestUserMessageItem(
            Arrays.asList(BinaryData.fromObject(new RealtimeRequestTextContentPart(itemText))));
        return new ConversationItemCreateEvent(messageItem);
    }

    /**
     * Create a system message item.
     *
     * @param systemMessage the text of the system message.
     * @return the system message item.
     */
    public static ConversationItemCreateEvent createSystemMessage(String systemMessage) {
        RealtimeRequestSystemMessageItem messageItem
            = new RealtimeRequestSystemMessageItem(Arrays.asList(new RealtimeRequestTextContentPart(systemMessage)));
        return new ConversationItemCreateEvent(messageItem);
    }

    /**
     * Create an assistant message item.
     *
     * @param assistantMessage the text of the assistant message.
     * @return the assistant message item.
     */
    public static ConversationItemCreateEvent createAssistantMessage(String assistantMessage) {
        RealtimeRequestAssistantMessageItem messageItem = new RealtimeRequestAssistantMessageItem(
            Arrays.asList(new RealtimeRequestTextContentPart(assistantMessage)));
        return new ConversationItemCreateEvent(messageItem);
    }

    /**
     * Create a function call item.
     *
     * @param functionName the name of the function to call.
     * @param callId the ID of the function call.
     * @param arguments the arguments to pass to the function.
     * @return the function call item.
     */
    public static ConversationItemCreateEvent createFunctionCall(String functionName, String callId, String arguments) {
        return new ConversationItemCreateEvent(new RealtimeRequestFunctionCallItem(functionName, callId, arguments));
    }

    /**
     * Create a function call output item.
     *
     * @param callId the ID of the function call.
     * @param functionOutput the output of the function call to be passed to the service.
     * @return the function call output item.
     */
    public static ConversationItemCreateEvent createFunctionCallOutput(String callId, String functionOutput) {
        return new ConversationItemCreateEvent(new RealtimeRequestFunctionCallOutputItem(callId, functionOutput));
    }

    /**
     * Private ctr.
     */
    private ConversationItem() {
    }
}
