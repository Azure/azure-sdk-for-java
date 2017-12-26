/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata of the list.
 */
public class BodyMetadata {
    /**
     * Optional key value pair to describe your list.
     */
    @JsonProperty(value = "Key One")
    private String keyOne;

    /**
     * Optional key value pair to describe your list.
     */
    @JsonProperty(value = "Key Two")
    private String keyTwo;

    /**
     * Get the keyOne value.
     *
     * @return the keyOne value
     */
    public String keyOne() {
        return this.keyOne;
    }

    /**
     * Set the keyOne value.
     *
     * @param keyOne the keyOne value to set
     * @return the BodyMetadata object itself.
     */
    public BodyMetadata withKeyOne(String keyOne) {
        this.keyOne = keyOne;
        return this;
    }

    /**
     * Get the keyTwo value.
     *
     * @return the keyTwo value
     */
    public String keyTwo() {
        return this.keyTwo;
    }

    /**
     * Set the keyTwo value.
     *
     * @param keyTwo the keyTwo value to set
     * @return the BodyMetadata object itself.
     */
    public BodyMetadata withKeyTwo(String keyTwo) {
        this.keyTwo = keyTwo;
        return this;
    }

}
