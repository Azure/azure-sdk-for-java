// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The NamedEntity model.
 */
@Immutable
public final class NamedEntity {
    /*
     * NamedEntity text as appears in the request.
     */
    private final String text;

    /*
     * NamedEntity type, such as Person/Location/Org/SSN etc
     */
    private final String type;

    /*
     * NamedEntity sub type, such as Age/Year/TimeRange etc
     */
    private final String subtype;

    /*
     * Start position (in Unicode characters) for the entity text.
     */
    private final int offset;

    /*
     * Length (in Unicode characters) for the entity text.
     */
    private final int length;

    /*
     * Confidence score between 0 and 1 of the extracted entity.
     */
    private final double score;

    public NamedEntity(String text, String type, String subtype, int offset, int length, double score) {
        this.text = text;
        this.type = type;
        this.subtype = subtype;
        this.offset = offset;
        this.length = length;
        this.score = score;
    }

    /**
     * Get the text property: NamedEntity text as appears in the request.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the type property: NamedEntity type, such as Person/Location/Org/SSN etc.
     *
     * @return the type value.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get the subtype property: NamedEntity sub type, such as Age/Year/TimeRange
     * etc.
     *
     * @return the subtype value.
     */
    public String getSubtype() {
        return this.subtype;
    }

    /**
     * Get the offset property: Start position (in Unicode characters) for the
     * entity text.
     *
     * @return the offset value.
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * Get the length property: Length (in Unicode characters) for the entity
     * text.
     *
     * @return the length value.
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Get the score property: Confidence score between 0 and 1 of the
     * extracted entity.
     *
     * @return the score value.
     */
    public double getScore() {
        return this.score;
    }
}
