/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines a search result item to display.
 */
public class RankingRankingItem {
    /**
     * The answer that contains the item to display. Use the type to find the
     * answer in the SearchResponse object. The type is the name of a
     * SearchResponse field. Possible values include: 'WebPages', 'Images',
     * 'SpellSuggestions', 'News', 'RelatedSearches', 'Videos', 'Computation',
     * 'TimeZone'.
     */
    @JsonProperty(value = "answerType", required = true)
    private AnswerType answerType;

    /**
     * A zero-based index of the item in the answer.If the item does not
     * include this field, display all items in the answer. For example,
     * display all news articles in the News answer.
     */
    @JsonProperty(value = "resultIndex", access = JsonProperty.Access.WRITE_ONLY)
    private Integer resultIndex;

    /**
     * The ID that identifies either an answer to display or an item of an
     * answer to display. If the ID identifies an answer, display all items of
     * the answer.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private Identifiable value;

    /**
     * The htmlIndex property.
     */
    @JsonProperty(value = "htmlIndex", access = JsonProperty.Access.WRITE_ONLY)
    private Integer htmlIndex;

    /**
     * The textualIndex property.
     */
    @JsonProperty(value = "textualIndex", access = JsonProperty.Access.WRITE_ONLY)
    private Integer textualIndex;

    /**
     * The screenshotIndex property.
     */
    @JsonProperty(value = "screenshotIndex", access = JsonProperty.Access.WRITE_ONLY)
    private Integer screenshotIndex;

    /**
     * Get the answerType value.
     *
     * @return the answerType value
     */
    public AnswerType answerType() {
        return this.answerType;
    }

    /**
     * Set the answerType value.
     *
     * @param answerType the answerType value to set
     * @return the RankingRankingItem object itself.
     */
    public RankingRankingItem withAnswerType(AnswerType answerType) {
        this.answerType = answerType;
        return this;
    }

    /**
     * Get the resultIndex value.
     *
     * @return the resultIndex value
     */
    public Integer resultIndex() {
        return this.resultIndex;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public Identifiable value() {
        return this.value;
    }

    /**
     * Get the htmlIndex value.
     *
     * @return the htmlIndex value
     */
    public Integer htmlIndex() {
        return this.htmlIndex;
    }

    /**
     * Get the textualIndex value.
     *
     * @return the textualIndex value
     */
    public Integer textualIndex() {
        return this.textualIndex;
    }

    /**
     * Get the screenshotIndex value.
     *
     * @return the screenshotIndex value
     */
    public Integer screenshotIndex() {
        return this.screenshotIndex;
    }

}
