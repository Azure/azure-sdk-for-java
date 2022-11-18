// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AbstractiveSummaryPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

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
    private IterableStream<SummaryContext> contexts;

    static {
        AbstractiveSummaryPropertiesHelper.setAccessor(
            new AbstractiveSummaryPropertiesHelper.AbstractiveSummaryAccessor() {
                @Override
                public void setText(AbstractiveSummary abstractiveSummary, String text) {
                    abstractiveSummary.setText(text);
                }

                @Override
                public void setSummaryContexts(AbstractiveSummary abstractiveSummary,
                    IterableStream<SummaryContext> summaryContexts) {
                    abstractiveSummary.setContexts(summaryContexts);
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
     * Get the contexts property: The context list of the summary.
     *
     * @return the contexts value.
     */
    public IterableStream<SummaryContext> getContexts() {
        return this.contexts;
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setContexts(IterableStream<SummaryContext> contexts) {
        this.contexts = contexts;
    }
}
