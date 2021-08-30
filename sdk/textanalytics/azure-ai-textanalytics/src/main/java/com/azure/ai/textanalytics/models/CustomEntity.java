// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.CustomEntityPropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@link CustomEntity} model.
 */
@Immutable
public final class CustomEntity {
    private String text;
    private CustomEntityCategory category;
    private String subcategory;
    private double confidenceScore;
    private int length;
    private int offset;

    static {
        CustomEntityPropertiesHelper.setAccessor(new CustomEntityPropertiesHelper.CustomEntityAccessor() {
            @Override
            public void setText(CustomEntity entity, String text) {
                entity.setText(text);
            }

            @Override
            public void setCategory(CustomEntity entity, CustomEntityCategory category) {
                entity.setCategory(category);
            }

            @Override
            public void setSubcategory(CustomEntity entity, String subcategory) {
                entity.setSubcategory(subcategory);
            }

            @Override
            public void setConfidenceScore(CustomEntity entity, double confidenceScore) {
                entity.setConfidenceScore(confidenceScore);
            }

            @Override
            public void setOffset(CustomEntity entity, int offset) {
                entity.setOffset(offset);
            }

            @Override
            public void setLength(CustomEntity entity, int length) {
                entity.setLength(length);
            }
        });
    }

    /**
     * Gets the text property: custom entity text as appears in the request.
     *
     * @return The text value.
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the category property: custom entity category, such as Politician etc.
     *
     * @return The category value.
     */
    public CustomEntityCategory getCategory() {
        return category;
    }

    /**
     * Gets the subcategory property: custom entity sub category.
     *
     * @return The subcategory value.
     */
    public String getSubcategory() {
        return subcategory;
    }

    /**
     * Gets the score property: If a well-known item is recognized, a decimal
     * number denoting the confidence level between 0 and 1 will be returned.
     *
     * @return The score value.
     */
    public double getConfidenceScore() {
        return confidenceScore;
    }

    /**
     * Gets the length of entity text.
     *
     * @return The length of entity text.
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the offset of entity text. The start position for the entity text in a document.
     *
     * @return The offset of entity text.
     */
    public int getOffset() {
        return offset;
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setCategory(CustomEntityCategory category) {
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
