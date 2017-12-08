/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Line model.
 */
public class Line {
    /**
     * The boundingBox property.
     */
    @JsonProperty(value = "boundingBox")
    private List<Integer> boundingBox;

    /**
     * The text property.
     */
    @JsonProperty(value = "text")
    private String text;

    /**
     * The words property.
     */
    @JsonProperty(value = "words")
    private List<Word> words;

    /**
     * Get the boundingBox value.
     *
     * @return the boundingBox value
     */
    public List<Integer> boundingBox() {
        return this.boundingBox;
    }

    /**
     * Set the boundingBox value.
     *
     * @param boundingBox the boundingBox value to set
     * @return the Line object itself.
     */
    public Line withBoundingBox(List<Integer> boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String text() {
        return this.text;
    }

    /**
     * Set the text value.
     *
     * @param text the text value to set
     * @return the Line object itself.
     */
    public Line withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the words value.
     *
     * @return the words value
     */
    public List<Word> words() {
        return this.words;
    }

    /**
     * Set the words value.
     *
     * @param words the words value to set
     * @return the Line object itself.
     */
    public Line withWords(List<Word> words) {
        this.words = words;
        return this;
    }

}
