// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AbstractiveSummaryPropertiesHelper;
import com.azure.core.annotation.Immutable;

import java.util.List;

/** An object representing a single summary with context for given document. */
@Immutable
public final class AbstractiveSummary {
    /*
     * The text of the summary.
     */
    private String text;

    /*
     * The context list of the summary.
     */
    private List<SummaryContext> summaryContexts;

    static {
        AbstractiveSummaryPropertiesHelper.setAccessor(
            new AbstractiveSummaryPropertiesHelper.AbstractiveSummaryAccessor() {
                @Override
                public void setText(AbstractiveSummary abstractiveSummary, String text) {
                    abstractiveSummary.setText(text);
                }

                @Override
                public void setSummaryContexts(AbstractiveSummary abstractiveSummary,
                                               List<SummaryContext> summaryContexts) {
                    abstractiveSummary.setSummaryContexts(summaryContexts);
                }
            });
    }

    /**
     * Get the text property: The text of the summary.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Set the text property: The text of the summary.
     *
     * @param text the text value to set.
     * @return the AbstractiveSummary object itself.
     */
    public AbstractiveSummary setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the contexts property: The context list of the summary.
     *
     * @return the contexts value.
     */
    public List<SummaryContext> getSummaryContexts() {
        return this.summaryContexts;
    }

    /**
     * Set the contexts property: The context list of the summary.
     *
     * @param summaryContexts the contexts value to set.
     * @return the AbstractiveSummary object itself.
     */
    public AbstractiveSummary setSummaryContexts(List<SummaryContext> summaryContexts) {
        this.summaryContexts = summaryContexts;
        return this;
    }
}
