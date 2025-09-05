// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesStreamEventType.
 */
public enum ResponsesStreamEventType {
    /**
     * Enum value error.
     */
    ERROR("error"),

    /**
     * Enum value response.completed.
     */
    RESPONSE_COMPLETED("response.completed"),

    /**
     * Enum value response.content_part.added.
     */
    RESPONSE_CONTENT_PART_ADDED("response.content_part.added"),

    /**
     * Enum value response.content_part.done.
     */
    RESPONSE_CONTENT_PART_DONE("response.content_part.done"),

    /**
     * Enum value response.created.
     */
    RESPONSE_CREATED("response.created"),

    /**
     * Enum value response.failed.
     */
    RESPONSE_FAILED("response.failed"),

    /**
     * Enum value response.file_search_call.completed.
     */
    RESPONSE_FILE_SEARCH_CALL_COMPLETED("response.file_search_call.completed"),

    /**
     * Enum value response.file_search_call.in.progress.
     */
    RESPONSE_FILE_SEARCH_CALL_IN_PROGRESS("response.file_search_call.in.progress"),

    /**
     * Enum value response.file_search_call.searching.
     */
    RESPONSE_FILE_SEARCH_CALL_SEARCHING("response.file_search_call.searching"),

    /**
     * Enum value response.function_call_arguments.delta.
     */
    RESPONSE_FUNCTION_CALL_ARGUMENTS_DELTA("response.function_call_arguments.delta"),

    /**
     * Enum value response.function_call_arguments.done.
     */
    RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE("response.function_call_arguments.done"),

    /**
     * Enum value response.in_progress.
     */
    RESPONSE_IN_PROGRESS("response.in_progress"),

    /**
     * Enum value response.incomplete.
     */
    RESPONSE_INCOMPLETE("response.incomplete"),

    /**
     * Enum value response.output_item.added.
     */
    RESPONSE_OUTPUT_ITEM_ADDED("response.output_item.added"),

    /**
     * Enum value response.output_item.done.
     */
    RESPONSE_OUTPUT_ITEM_DONE("response.output_item.done"),

    /**
     * Enum value response.output_text.annotation.added.
     */
    RESPONSE_OUTPUT_TEXT_ANNOTATION_ADDED("response.output_text.annotation.added"),

    /**
     * Enum value response.output_text.delta.
     */
    RESPONSE_OUTPUT_TEXT_DELTA("response.output_text.delta"),

    /**
     * Enum value response.output_text.done.
     */
    RESPONSE_OUTPUT_TEXT_DONE("response.output_text.done"),

    /**
     * Enum value response.refusal.delta.
     */
    RESPONSE_REFUSAL_DELTA("response.refusal.delta"),

    /**
     * Enum value response.refusal.done.
     */
    RESPONSE_REFUSAL_DONE("response.refusal.done"),

    /**
     * Enum value response.web_search_call.completed.
     */
    RESPONSE_WEB_SEARCH_CALL_COMPLETED("response.web_search_call.completed"),

    /**
     * Enum value response.web_search_call.in_progress.
     */
    RESPONSE_WEB_SEARCH_CALL_IN_PROGRESS("response.web_search_call.in_progress"),

    /**
     * Enum value response.web_search_call.searching.
     */
    RESPONSE_WEB_SEARCH_CALL_SEARCHING("response.web_search_call.searching");

    /**
     * The actual serialized value for a ResponsesStreamEventType instance.
     */
    private final String value;

    ResponsesStreamEventType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesStreamEventType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesStreamEventType object, or null if unable to parse.
     */
    public static ResponsesStreamEventType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesStreamEventType[] items = ResponsesStreamEventType.values();
        for (ResponsesStreamEventType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }
}
