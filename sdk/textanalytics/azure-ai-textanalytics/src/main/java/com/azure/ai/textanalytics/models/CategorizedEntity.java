// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The CategorizedEntity model.
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

    /**
     * Creates a {@code CategorizedEntity} model that describes entity.
     *
     * @param text categorizedEntity text as appears in the request
     * @param category categorizedEntity category, such as Person/Location/Org/SSN etc
     * @param subCategory categorizedEntity sub category, such as Age/Year/TimeRange etc
     * @param offset start position (in Unicode characters) for the entity text
     * @param length length (in Unicode characters) for the entity text
     * @param score confidence score between 0 and 1 of the extracted entity
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
     * Get the text property: CategorizedEntity text as appears in the request.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the category property: CategorizedEntity category, such as Person/Location/Org/SSN etc.
     *
     * @return the category value.
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Get the subcategory property: CategorizedEntity sub category, such as Age/Year/TimeRange etc.
     *
     * @return the subcategory value.
     */
    public String getSubCategory() {
        return this.subCategory;
    }

    /**
     * Get the offset property: Start position (in Unicode characters) for the entity text.
     *
     * @return the offset value.
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * Get the length property: Length (in Unicode characters) for the entity text.
     *
     * @return the length value.
     */
    public int getLength() {
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
