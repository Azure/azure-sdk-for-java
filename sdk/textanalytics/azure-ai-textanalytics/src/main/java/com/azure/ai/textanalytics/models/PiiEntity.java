// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.PiiEntityPropertiesHelper;

/**
 * The {@link PiiEntity} model.
 */
public final class PiiEntity {
    /*
     * PiiEntity text as appears in the request.
     */
    private final String text;

    /*
     * PiiEntity category, such as Person/Location/Org/SSN etc
     */
    private final EntityCategory category;

    /*
     * PiiEntity sub category, such as Medical/Stock exchange/Sports etc
     */
    private final String subcategory;

    /*
     * Confidence score between 0 and 1 of the extracted entity.
     */
    private final double confidenceScore;

    /*
     * Start position for the entity text.
     */
    private final int offset;

    private int length;

    /**
     * Creates a {@link PiiEntity} model that describes entity.
     * @param text The entity text as appears in the request.
     * @param category The entity category, such as Person/Location/Org/SSN etc.
     * @param subcategory The entity subcategory, such as Medical/Stock exchange/Sports etc.
     * @param confidenceScore A confidence score between 0 and 1 of the recognized entity.
     * @param offset The start position for the entity text
     */
    public PiiEntity(String text, EntityCategory category, String subcategory, double confidenceScore, int offset) {
        this.text = text;
        this.category = category;
        this.subcategory = subcategory;
        this.confidenceScore = confidenceScore;
        this.offset = offset;
    }

    static {
        PiiEntityPropertiesHelper.setAccessor((entity, length) -> entity.setLength(length));
    }

    /**
     * Get the text property: PII entity text as appears in the request.
     *
     * @return The {@code text} value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the category property: Categorized entity category, such as Person/Location/Org/SSN etc.
     *
     * @return The {@code category} value.
     */
    public EntityCategory getCategory() {
        return this.category;
    }

    /**
     * Get the subcategory property: Categorized entity subcategory, such as Medical/Stock exchange/Sports etc.
     *
     * @return The {@code subcategory} value.
     */
    public String getSubcategory() {
        return this.subcategory;
    }

    /**
     * Get the score property: Confidence score between 0 and 1 of the recognized entity.
     *
     * @return The {@code confidenceScore} value.
     */
    public double getConfidenceScore() {
        return this.confidenceScore;
    }

    /**
     * Get the offset property: the start position for the entity text.
     *
     * @return The {@code offset} value.
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * Get the length of entity text.
     *
     * @return The length of entity text.
     */
    public int getLength() {
        return length;
    }

    private void setLength(int length) {
        this.length = length;
    }
}
