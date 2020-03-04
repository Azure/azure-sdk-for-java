// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The Personally Identifiable Information entity model.
 */
@Immutable
public final class PiiEntity {
    /*
     * Personally Identifiable Information entity text as appears in the request.
     */
    private final String text;

    /*
     * Personally Identifiable Information entity category, such as Person/Location/Org/SSN etc
     */
    private final EntityCategory category;

    /*
     * Personally Identifiable Information entity sub category, such as Age/Year/TimeRange etc
     */
    private final String subCategory;

    /*
     * Grapheme start position for the entity text.
     */
    private final int offset;

    /*
     * Grapheme length for the entity text.
     */
    private final int length;

    /*
     * Confidence score between 0 and 1 of the extracted entity.
     */
    private final double score;

    /**
     * Creates a Personally Identifiable Information entity model that describes entity.
     *
     * @param text Personally Identifiable Information entity text as appears in the request
     * @param category Personally Identifiable Information entity category, such as Person/Location/Org/SSN etc
     * @param subCategory Personally Identifiable Information entity sub category, such as Age/Year/TimeRange etc
     * @param offset Grapheme start position for the entity text
     * @param length Grapheme length for the entity text
     * @param score Confidence score between 0 and 1 of the extracted entity
     */
    public PiiEntity(String text, EntityCategory category, String subCategory, int offset, int length, double score) {
        this.text = text;
        this.category = category;
        this.subCategory = subCategory;
        this.offset = offset;
        this.length = length;
        this.score = score;
    }

    /**
     * Get the text property: Personally Identifiable Information entity text as appears in the request.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the category property: Personally Identifiable Information entity category, such as
     * Person/Location/Org/SSN etc.
     *
     * @return the category value.
     */
    public EntityCategory getCategory() {
        return this.category;
    }

    /**
     * Get the subcategory property: Personally Identifiable Information entity sub category, such as
     * Age/Year/TimeRange etc.
     *
     * @return the subcategory value.
     */
    public String getSubCategory() {
        return this.subCategory;
    }

    /**
     * Get the offset property: Grapheme start position for the entity text.
     *
     * @return the offset value.
     */
    public int getGraphemeOffset() {
        return this.offset;
    }

    /**
     * Get the length property: Grapheme length for the entity text.
     *
     * @return the length value.
     */
    public int getGraphemeLength() {
        return this.length;
    }

    /**
     * Get the score property: Confidence score between 0 and 1 of the extracted entity.
     *
     * @return the score value.
     */
    public double getScore() {
        return this.score;
    }
}
