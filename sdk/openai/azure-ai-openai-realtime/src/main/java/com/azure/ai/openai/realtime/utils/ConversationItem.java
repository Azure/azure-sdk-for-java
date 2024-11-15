package com.azure.ai.openai.realtime.utils;

import com.azure.ai.openai.realtime.models.RealtimeClientEventConversationItemCreate;
import com.azure.ai.openai.realtime.models.RealtimeContentPart;
import com.azure.ai.openai.realtime.models.RealtimeContentPartType;
import com.azure.ai.openai.realtime.models.RealtimeRequestAssistantMessageItem;
import com.azure.ai.openai.realtime.models.RealtimeRequestFunctionCallItem;
import com.azure.ai.openai.realtime.models.RealtimeRequestFunctionCallOutputItem;
import com.azure.ai.openai.realtime.models.RealtimeRequestSystemMessageItem;
import com.azure.ai.openai.realtime.models.RealtimeRequestTextContentPart;
import com.azure.ai.openai.realtime.models.RealtimeRequestUserMessageItem;
import com.azure.core.util.BinaryData;

import java.util.Arrays;

public class ConversationItem {

    /**
     * Create a user message item.
     *
     * @param itemText the text of the user message.
     * @return the user message item.
     */
    public static RealtimeClientEventConversationItemCreate createUserMessage(String itemText) {
        // `itemText` is not a JSON string, but a prompt. Therefore, BinaryData.fromObject is the appropriate representation.
        RealtimeRequestUserMessageItem messageItem = new RealtimeRequestUserMessageItem(Arrays.asList(BinaryData.fromObject(
                new RealtimeRequestTextContentPart(itemText))));
        return new RealtimeClientEventConversationItemCreate(messageItem);
    }

    /**
     * Create a system message item.
     *
     * @param systemMessage the text of the system message.
     * @return the system message item.
     */
    public static RealtimeClientEventConversationItemCreate createSystemMessage(String systemMessage) {
        RealtimeRequestSystemMessageItem messageItem
            = new RealtimeRequestSystemMessageItem(Arrays.asList(new RealtimeRequestTextContentPart(systemMessage)));
        return new RealtimeClientEventConversationItemCreate(messageItem);
    }

    /**
     * Create an assistant message item.
     *
     * @param assistantMessage the text of the assistant message.
     * @return the assistant message item.
     */
    public static RealtimeClientEventConversationItemCreate createAssistantMessage(String assistantMessage) {
        RealtimeRequestAssistantMessageItem messageItem = new RealtimeRequestAssistantMessageItem(
            Arrays.asList(new RealtimeRequestTextContentPart(assistantMessage)));
        return new RealtimeClientEventConversationItemCreate(messageItem);
    }

    /**
     * Create a function call item.
     *
     * @param functionName the name of the function to call.
     * @param callId the ID of the function call.
     * @param arguments the arguments to pass to the function.
     * @return the function call item.
     */
    public static RealtimeClientEventConversationItemCreate createFunctionCall(String functionName, String callId,
        String arguments) {
        return new RealtimeClientEventConversationItemCreate(
            new RealtimeRequestFunctionCallItem(functionName, callId, arguments));
    }

    /**
     * Create a function call output item.
     *
     * @param callId the ID of the function call.
     * @param functionOutput the output of the function call to be passed to the service.
     * @return the function call output item.
     */
    public static RealtimeClientEventConversationItemCreate createFunctionCallOutput(String callId,
        String functionOutput) {
        return new RealtimeClientEventConversationItemCreate(
            new RealtimeRequestFunctionCallOutputItem(callId, functionOutput));
    }
}
