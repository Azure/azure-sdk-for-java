// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.PiiEntityPropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@link PiiEntity} model.
 */
@Immutable
public final class PiiEntity {
    /*
     * PiiEntity text as appears in the request.
     */
    private String text;

    /*
     * PiiEntity category, such as Person/Location/Org/SSN etc
     */
    private PiiEntityCategory category;

    /*
     * PiiEntity sub category, such as Medical/Stock exchange/Sports etc
     */
    private String subcategory;

    /*
     * Confidence score between 0 and 1 of the extracted entity.
     */
    private double confidenceScore;

    /*
     * Start position for the entity text.
     */
    private int offset;

    private int length;

    static {
        PiiEntityPropertiesHelper.setAccessor(new PiiEntityPropertiesHelper.PiiEntityAccessor() {
            @Override
            public void setText(PiiEntity entity, String text) {
                entity.setText(text);
            }

            @Override
            public void setCategory(PiiEntity entity, PiiEntityCategory category) {
                entity.setCategory(category);
            }

            @Override
            public void setSubcategory(PiiEntity entity, String subcategory) {
                entity.setSubcategory(subcategory);
            }

            @Override
            public void setConfidenceScore(PiiEntity entity, double confidenceScore) {
                entity.setConfidenceScore(confidenceScore);
            }

            @Override
            public void setOffset(PiiEntity entity, int offset) {
                entity.setOffset(offset);
            }

            @Override
            public void setLength(PiiEntity entity, int length) {
                entity.setLength(length);
            }
        });
    }

    /**
     * Gets the text property: PII entity text as appears in the request.
     *
     * @return The {@code text} value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Gets the category property: Categorized entity category, such as Person/Location/Org/SSN etc.
     *
     * @return The {@code category} value.
     */
    public PiiEntityCategory getCategory() {
        return this.category;
    }

    /**
     * Gets the subcategory property: Categorized entity subcategory, such as Medical/Stock exchange/Sports etc.
     *
     * @return The {@code subcategory} value.
     */
    public String getSubcategory() {
        return this.subcategory;
    }

    /**
     * Gets the score property: Confidence score between 0 and 1 of the recognized entity.
     *
     * @return The {@code confidenceScore} value.
     */
    public double getConfidenceScore() {
        return this.confidenceScore;
    }

    /**
     * Gets the offset property: the start position for the entity text.
     *
     * @return The {@code offset} value.
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * Gets the length of entity text.
     *
     * @return The length of entity text.
     */
    public int getLength() {
        return length;
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setCategory(PiiEntityCategory category) {
        this.category = category;
    }

    private void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    private void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    private void setLength(int length) {
        this.length = length;
    }
}
