// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 *
 */
@Immutable
public final class CustomEntity {
    private String text;
    private CustomEntityCategory category;
    private String subcategory;
    private double confidenceScore;
    private int length;
    private int offset;

    /**
     *
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @param text
     * @return
     */
    public CustomEntity setText(String text) {
        this.text = text;
        return this;
    }

    /**
     *
     * @return
     */
    public CustomEntityCategory getCategory() {
        return category;
    }

    /**
     *
     * @param category
     * @return
     */
    public CustomEntity setCategory(CustomEntityCategory category) {
        this.category = category;
        return this;
    }

    /**
     *
     * @return
     */
    public String getSubcategory() {
        return subcategory;
    }

    /**
     *
     * @param subcategory
     * @return
     */
    public CustomEntity setSubcategory(String subcategory) {
        this.subcategory = subcategory;
        return this;
    }

    /**
     *
     * @return
     */
    public double getConfidenceScore() {
        return confidenceScore;
    }

    /**
     *
     * @param confidenceScore
     * @return
     */
    public CustomEntity setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
        return this;
    }

    /**
     *
     * @return
     */
    public int getLength() {
        return length;
    }

    /**
     *
     * @param length
     * @return
     */
    public CustomEntity setLength(int length) {
        this.length = length;
        return this;
    }

    /**
     *
     * @return
     */
    public int getOffset() {
        return offset;
    }

    /**
     *
     * @param offset
     * @return
     */
    public CustomEntity setOffset(int offset) {
        this.offset = offset;
        return this;
    }
}
