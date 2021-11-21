// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the version of {@link SentimentSkill}.
 */
public enum SentimentSkillVersion {
    /**
     * Version 1 of {@link SentimentSkill}.
     */
    V1("#Microsoft.Skills.Text.SentimentSkill"),

    /**
     * Version 3 of {@link SentimentSkill}.
     */
    V3("#Microsoft.Skills.Text.V3.SentimentSkill");

    @JsonValue
    private final String odataType;

    SentimentSkillVersion(String odataType) {
        this.odataType = odataType;
    }

    /**
     * Gets the latest {@link SentimentSkill} version.
     *
     * @return The latest {@link SentimentSkill} version.
     */
    public static SentimentSkillVersion getLatest() {
        return V3;
    }

    /**
     * Gets the {@link SentimentSkillVersion} from the string {@code value}.
     * <p>
     * If the {@code value} doesn't match any version null will be returned.
     *
     * @param value The value to convert to an {@link SentimentSkillVersion}.
     * @return The {@link SentimentSkillVersion} corresponding to the {@code value}, or null if no versions match the
     * {@code value}.
     */
    @JsonCreator
    public static SentimentSkillVersion fromString(String value) {
        if (V1.odataType.equals(value)) {
            return V1;
        } else if (V3.odataType.equals(value)) {
            return V3;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return odataType;
    }
}
