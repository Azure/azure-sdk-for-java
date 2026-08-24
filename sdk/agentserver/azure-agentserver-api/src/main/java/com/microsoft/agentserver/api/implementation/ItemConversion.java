// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.implementation;

import com.openai.models.responses.ResponseCodeInterpreterToolCall;
import com.openai.models.responses.ResponseComputerToolCall;
import com.openai.models.responses.ResponseFileSearchToolCall;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseFunctionWebSearch;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseInputMessageItem;
import com.openai.models.responses.ResponseItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseReasoningItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal utility for converting {@link ResponseInputItem} instances to
 * {@link ResponseOutputItem} instances suitable for storage and retrieval
 * via the provider. Each converted item receives a correctly prefixed ID
 * via {@link IdGenerator}.
 */
public final class ItemConversion {

    private ItemConversion() {
    }

    /**
     * Converts a single {@link ResponseInputItem} to a {@link ResponseOutputItem}.
     * Generates a type-specific ID using the supplied {@link IdGenerator} and marks
     * status as completed.
     *
     * @param item  the input item to convert.
     * @param idGen the ID generator for creating type-specific IDs.
     * @return the converted output item, or {@code null} if the item type is not
     * convertible (e.g., {@code ItemReference}).
     */
    public static ResponseOutputItem toOutputItem(ResponseInputItem item, IdGenerator idGen) {
        // --- Messages ---
        if (item.isMessage()) {
            return convertMessage(item.asMessage(), idGen);
        }

        // --- Function tool calls ---
        if (item.isFunctionCall()) {
            ResponseFunctionToolCall funcCall = item.asFunctionCall();
            // Re-build with a generated ID if none is present
            ResponseFunctionToolCall withId = funcCall.toBuilder()
                .id(funcCall.id().orElse(idGen.generateFunctionCallItemId()))
                .status(ResponseFunctionToolCall.Status.COMPLETED)
                .build();
            return ResponseOutputItem.ofFunctionCall(withId);
        }

        // --- File search ---
        if (item.isFileSearchCall()) {
            ResponseFileSearchToolCall fileSearch = item.asFileSearchCall();
            return ResponseOutputItem.ofFileSearchCall(fileSearch);
        }

        // --- Web search ---
        if (item.isWebSearchCall()) {
            ResponseFunctionWebSearch webSearch = item.asWebSearchCall();
            return ResponseOutputItem.ofWebSearchCall(webSearch);
        }

        // --- Reasoning ---
        if (item.isReasoning()) {
            ResponseReasoningItem reasoning = item.asReasoning();
            ResponseReasoningItem withId = reasoning.toBuilder()
                .id(reasoning.id())
                .status(ResponseReasoningItem.Status.COMPLETED)
                .build();
            return ResponseOutputItem.ofReasoning(withId);
        }

        // --- Computer tool calls ---
        if (item.isComputerCall()) {
            ResponseComputerToolCall computerCall = item.asComputerCall();
            return ResponseOutputItem.ofComputerCall(computerCall);
        }

        // --- Code interpreter ---
        if (item.isCodeInterpreterCall()) {
            ResponseCodeInterpreterToolCall codeInterpreter = item.asCodeInterpreterCall();
            return ResponseOutputItem.ofCodeInterpreterCall(codeInterpreter);
        }

        // --- Item references are not convertible inline ---
        if (item.isItemReference()) {
            return null;
        }

        // --- EasyInputMessage (shorthand user/system/developer messages) ---
        if (item.isEasyInputMessage()) {
            return convertEasyInputMessage(item.asEasyInputMessage(), idGen);
        }

        // Unknown type — not convertible
        return null;
    }

    /**
     * Converts a sequence of {@link ResponseInputItem} to {@link ResponseOutputItem} instances.
     * Items that cannot be converted inline (e.g., item references) are skipped.
     *
     * @param items the input items to convert.
     * @param idGen the ID generator.
     * @return the converted output items (references excluded).
     */
    static List<ResponseOutputItem> toOutputItems(List<ResponseInputItem> items, IdGenerator idGen) {
        List<ResponseOutputItem> results = new ArrayList<>();
        for (ResponseInputItem item : items) {
            ResponseOutputItem output = toOutputItem(item, idGen);
            if (output != null) {
                results.add(output);
            }
        }
        return results;
    }

    // ── Helpers ─────────────────────────────────────────────────

    /**
     * Extracts the item ID from a {@link ResponseItem} union type.
     * <p>
     * Handles all known variants including input messages, output messages,
     * tool calls, and tool outputs.
     *
     * @param item the response item
     * @return the item ID, or {@code null} if the variant is unrecognized
     */
    public static String extractItemId(ResponseItem item) {
        if (item.isResponseInputMessageItem()) return item.asResponseInputMessageItem().id();
        if (item.isResponseOutputMessage()) return item.asResponseOutputMessage().id();
        if (item.isFunctionCall()) return item.asFunctionCall().id().orElse(null);
        if (item.isFunctionCallOutput()) return item.asFunctionCallOutput().id();
        if (item.isFileSearchCall()) return item.asFileSearchCall().id();
        if (item.isWebSearchCall()) return item.asWebSearchCall().id();
        if (item.isComputerCall()) return item.asComputerCall().id();
        if (item.isReasoning()) return item.asReasoning().id();
        if (item.isCompaction()) return item.asCompaction().id();
        if (item.isCodeInterpreterCall()) return item.asCodeInterpreterCall().id();
        if (item.isShellCall()) return item.asShellCall().id();
        if (item.isShellCallOutput()) return item.asShellCallOutput().id();
        if (item.isApplyPatchCall()) return item.asApplyPatchCall().id();
        if (item.isApplyPatchCallOutput()) return item.asApplyPatchCallOutput().id();
        if (item.isCustomToolCall()) return item.asCustomToolCall().id().orElse(null);
        return null;
    }

    /**
     * Extracts the item ID from a {@link ResponseOutputItem} union type.
     * <p>
     * Handles output-only variants (message, MCP calls) directly, then
     * converts shared variants to {@link ResponseItem} and delegates to
     * {@link #extractItemId(ResponseItem)}.
     *
     * @param item the output item
     * @return the item ID, or {@code null} if the variant is unrecognized
     */
    public static String extractOutputItemId(ResponseOutputItem item) {
        // Output-only cases (no direct ResponseItem equivalent)
        if (item.isMessage()) return item.asMessage().id();
        if (item.isMcpCall()) return item.asMcpCall().id();
        if (item.isMcpListTools()) return item.asMcpListTools().id();
        if (item.isMcpApprovalRequest()) return item.asMcpApprovalRequest().id();

        // Shared cases — convert to ResponseItem and extract
        ResponseItem responseItem = toResponseItem(item);
        if (responseItem != null) {
            return extractItemId(responseItem);
        }

        // Cases where toResponseItem returns null, but we can still extract ID
        if (item.isFunctionCall()) return item.asFunctionCall().id().orElse(null);
        if (item.isFunctionCallOutput()) return item.asFunctionCallOutput().id();
        if (item.isCompaction()) return item.asCompaction().id();
        if (item.isCustomToolCall()) return item.asCustomToolCall().id().orElse(null);
        return null;
    }

    /**
     * Converts a {@link ResponseOutputItem} to a {@link ResponseItem}.
     * <p>
     * Note: {@code FunctionCall} and {@code CustomToolCall} output items cannot be converted
     * because {@link ResponseItem#ofFunctionCall} and {@link ResponseItem#ofCustomToolCall}
     * expect the "item" input variants, not the output-side types.
     *
     * @param item the output item to convert
     * @return the converted response item, or {@code null} if the type is not convertible
     */
    public static ResponseItem toResponseItem(ResponseOutputItem item) {
        if (item.isMessage()) return ResponseItem.ofResponseOutputMessage(item.asMessage());
        if (item.isFileSearchCall()) return ResponseItem.ofFileSearchCall(item.asFileSearchCall());
        if (item.isWebSearchCall()) return ResponseItem.ofWebSearchCall(item.asWebSearchCall());
        if (item.isComputerCall()) return ResponseItem.ofComputerCall(item.asComputerCall());
        if (item.isReasoning()) return ResponseItem.ofReasoning(item.asReasoning());
        if (item.isCodeInterpreterCall()) return ResponseItem.ofCodeInterpreterCall(item.asCodeInterpreterCall());
        if (item.isShellCall()) return ResponseItem.ofShellCall(item.asShellCall());
        if (item.isShellCallOutput()) return ResponseItem.ofShellCallOutput(item.asShellCallOutput());
        if (item.isApplyPatchCall()) return ResponseItem.ofApplyPatchCall(item.asApplyPatchCall());
        if (item.isApplyPatchCallOutput()) return ResponseItem.ofApplyPatchCallOutput(item.asApplyPatchCallOutput());
        return null;
    }

    /**
     * Converts a {@link ResponseInputItem} (the request-side, role-aware union)
     * directly to a {@link ResponseItem}, preserving the original role
     * (user/system/developer). This is the correct conversion for
     * {@code input_items} sent to Foundry storage — the
     * {@link #toResponseItem(ResponseOutputItem)} overload loses role information
     * because {@code ResponseOutputMessage} is hardcoded to {@code assistant}.
     */
    public static ResponseItem toResponseItem(ResponseInputItem item, IdGenerator idGen) {
        if (item.isMessage()) {
            ResponseInputItem.Message msg = item.asMessage();
            ResponseInputMessageItem.Role role = convertInputRole(msg.role().value());
            return ResponseItem.ofResponseInputMessageItem(
                ResponseInputMessageItem.builder()
                    .id(idGen.generateMessageItemId())
                    .role(role)
                    .content(msg.content())
                    .status(ResponseInputMessageItem.Status.COMPLETED)
                    .build());
        }
        if (item.isEasyInputMessage()) {
            com.openai.models.responses.EasyInputMessage easy = item.asEasyInputMessage();
            ResponseInputMessageItem.Role role = convertEasyRole(easy.role().value());
            List<ResponseInputContent> content = convertEasyContent(easy.content());
            return ResponseItem.ofResponseInputMessageItem(
                ResponseInputMessageItem.builder()
                    .id(idGen.generateMessageItemId())
                    .role(role)
                    .content(content)
                    .status(ResponseInputMessageItem.Status.COMPLETED)
                    .build());
        }
        // Fall through: tool calls / outputs / references → go through the
        // ResponseOutputItem conversion which preserves their structure.
        ResponseOutputItem asOutput = toOutputItem(item, idGen);
        return asOutput != null ? toResponseItem(asOutput) : null;
    }

    private static ResponseInputMessageItem.Role convertInputRole(ResponseInputItem.Message.Role.Value v) {
        return switch (v) {
            case USER -> ResponseInputMessageItem.Role.USER;
            case SYSTEM -> ResponseInputMessageItem.Role.SYSTEM;
            case DEVELOPER -> ResponseInputMessageItem.Role.DEVELOPER;
            default -> ResponseInputMessageItem.Role.USER;
        };
    }

    private static ResponseInputMessageItem.Role convertEasyRole(com.openai.models.responses.EasyInputMessage.Role.Value v) {
        return switch (v) {
            case USER -> ResponseInputMessageItem.Role.USER;
            case SYSTEM -> ResponseInputMessageItem.Role.SYSTEM;
            case DEVELOPER -> ResponseInputMessageItem.Role.DEVELOPER;
            case ASSISTANT -> ResponseInputMessageItem.Role.USER; // shouldn't happen on input side; map defensively
            default -> ResponseInputMessageItem.Role.USER;
        };
    }

    private static List<ResponseInputContent> convertEasyContent(com.openai.models.responses.EasyInputMessage.Content content) {
        if (content.isResponseInputMessageContentList()) {
            return content.asResponseInputMessageContentList();
        }
        if (content.isTextInput()) {
            return List.of(ResponseInputContent.ofInputText(
                com.openai.models.responses.ResponseInputText.builder()
                    .text(content.asTextInput())
                    .build()));
        }
        return List.of();
    }

    private static ResponseOutputItem convertMessage(ResponseInputItem.Message message, IdGenerator idGen) {
        // Convert input content to output content
        List<ResponseOutputMessage.Content> outputContent = new ArrayList<>();
        for (ResponseInputContent inputContent : message.content()) {
            if (inputContent.isInputText()) {
                outputContent.add(ResponseOutputMessage.Content.ofOutputText(
                    ResponseOutputText.builder()
                        .text(inputContent.asInputText().text())
                        .annotations(List.of())
                        .build()));
            }
            // Other content types (audio, image, file) are passed through as-is
            // by the OpenAI API; we convert text content which is the primary case.
        }

        ResponseOutputMessage outputMessage = ResponseOutputMessage.builder()
            .id(idGen.generateMessageItemId())
            .status(ResponseOutputMessage.Status.COMPLETED)
            .content(outputContent)
            .build();

        return ResponseOutputItem.ofMessage(outputMessage);
    }

    private static ResponseOutputItem convertEasyInputMessage(
        com.openai.models.responses.EasyInputMessage easyMessage, IdGenerator idGen) {
        // Extract text from the easy input message content
        String text = "";
        var content = easyMessage.content();
        if (content.isTextInput()) {
            text = content.asTextInput();
        } else if (content.isResponseInputMessageContentList()) {
            StringBuilder sb = new StringBuilder();
            for (ResponseInputContent c : content.asResponseInputMessageContentList()) {
                if (c.isInputText()) {
                    if (!sb.isEmpty()) {
                        sb.append("\n");
                    }
                    sb.append(c.asInputText().text());
                }
            }
            text = sb.toString();
        }

        ResponseOutputMessage outputMessage = ResponseOutputMessage.builder()
            .id(idGen.generateMessageItemId())
            .status(ResponseOutputMessage.Status.COMPLETED)
            .content(List.of(
                ResponseOutputMessage.Content.ofOutputText(
                    ResponseOutputText.builder()
                        .text(text)
                        .annotations(List.of())
                        .build())))
            .build();

        return ResponseOutputItem.ofMessage(outputMessage);
    }
}

