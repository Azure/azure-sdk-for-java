// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents the version of {@link EntityRecognitionSkill}.
 */
public enum EntityRecognitionSkillVersion {
    /**
     * Version 1 of {@link EntityRecognitionSkill}.
     */
    V1("#Microsoft.Skills.Text.EntityRecognitionSkill"),

    /**
     * Version 3 of {@link EntityRecognitionSkill}.
     */
    V3("#Microsoft.Skills.Text.V3.EntityRecognitionSkill");

    @JsonValue
    private final String odataType;

    EntityRecognitionSkillVersion(String odataType) {
        this.odataType = odataType;
    }

    /**
     * Gets the latest {@link EntityRecognitionSkill} version.
     *
     * @return The latest {@link EntityRecognitionSkill} version.
     */
    public static EntityRecognitionSkillVersion getLatest() {
        return V3;
    }

    /**
     * Gets the {@link EntityRecognitionSkillVersion} from the string {@code value}.
     * <p>
     * If the {@code value} doesn't match any version null will be returned.
     *
     * @param value The value to convert to an {@link EntityRecognitionSkillVersion}.
     * @return The {@link EntityRecognitionSkillVersion} corresponding to the {@code value}, or null if no versions
     * match the {@code value}.
     */
    @JsonCreator
    public static EntityRecognitionSkillVersion fromString(String value) {
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
