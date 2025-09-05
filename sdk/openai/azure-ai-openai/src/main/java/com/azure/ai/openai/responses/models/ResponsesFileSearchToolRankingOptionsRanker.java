// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesFileSearchToolRankingOptionsRanker.
 */
public enum ResponsesFileSearchToolRankingOptionsRanker {
    /**
     * Enum value auto.
     */
    AUTO("auto"),

    /**
     * Enum value default-2024-11-15.
     */
    DEFAULT_2024_11_15("default-2024-11-15");

    /**
     * The actual serialized value for a ResponsesFileSearchToolRankingOptionsRanker instance.
     */
    private final String value;

    ResponsesFileSearchToolRankingOptionsRanker(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesFileSearchToolRankingOptionsRanker instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesFileSearchToolRankingOptionsRanker object, or null if unable to parse.
     */
    public static ResponsesFileSearchToolRankingOptionsRanker fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesFileSearchToolRankingOptionsRanker[] items = ResponsesFileSearchToolRankingOptionsRanker.values();
        for (ResponsesFileSearchToolRankingOptionsRanker item : items) {
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
