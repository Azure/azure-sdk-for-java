// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link CategorizedEntity} model.
 */
@Immutable
public final class CategorizedEntity {
    /*
     * CategorizedEntity text as appears in the request.
     */
    private final String text;

    /*
     * CategorizedEntity category, such as Person/Location/Org/SSN etc
     */
    private final String category;

    /*
     * CategorizedEntity sub category, such as Age/Year/TimeRange etc
     */
    private final String subCategory;

    /*
     * Start position for the entity text.
     */
    private final int offset;

    /*
     * Length for the entity text.
     */
    private final int length;

    /*
     * Confidence score between 0 and 1 of the extracted entity.
     */
    private final double score;

    /**
     * Creates a {@code CategorizedEntity} model that describes entity.
     *
     * @param text Entity text as appears in the request.
     * @param category Entity category, such as Person/Location/Org/SSN etc.
     * @param subCategory Entity sub category, such as Age/Year/TimeRange etc.
     * @param offset Grapheme start position for the entity text.
     * @param length Grapheme length for the entity text.
     * @param score Confidence score between 0 and 1 of the extracted entity.
     */
    public CategorizedEntity(String text, String category, String subCategory, int offset, int length, double score) {
        this.text = text;
        this.category = category;
        this.subCategory = subCategory;
        this.offset = offset;
        this.length = length;
        this.score = score;
    }

    /**
     * Get the text property: Categorized entity text as appears in the request.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the category property: Categorized entity category, such as Person/Location/Org/SSN etc.
     *
     * @return the category value.
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Get the subcategory property: Categorized entity sub category, such as Age/Year/TimeRange etc.
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
