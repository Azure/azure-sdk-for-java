// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AbstractiveSummaryContextPropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@code AbstractiveSummaryContext} model.
 */
@Immutable
public final class AbstractiveSummaryContext {
    /*
     * Start position for the context. Use of different 'stringIndexType' values can affect the offset returned.
     */
    private int offset;

    /*
     * The length of the context. Use of different 'stringIndexType' values can affect the length returned.
     */
    private int length;

    static {
        AbstractiveSummaryContextPropertiesHelper.setAccessor(
            new AbstractiveSummaryContextPropertiesHelper.AbstractiveSummaryContextAccessor() {
                @Override
                public void setOffset(AbstractiveSummaryContext abstractiveSummaryContext, int offset) {
                    abstractiveSummaryContext.setOffset(offset);
                }

                @Override
                public void setLength(AbstractiveSummaryContext abstractiveSummaryContext, int length) {
                    abstractiveSummaryContext.setLength(length);
                }
            }
        );
    }

    /**
     * Constructs a {@code AbstractiveSummaryContext} model.
     */
    public AbstractiveSummaryContext() {
    }

    /**
     * Get the offset property: Start position for the context. Use of different 'stringIndexType' values can affect the
     * offset returned.
     *
     * @return the offset value.
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * Get the length property: The length of the context. Use of different 'stringIndexType' values can affect the
     * length returned.
     *
     * @return the length value.
     */
    public int getLength() {
        return this.length;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    private void setLength(int length) {
        this.length = length;
    }
}
