/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Image List Metadata.
 */
public class CreateImageListMetadata {
    /**
     * Optional Key value pair to describe your list.
     */
    @JsonProperty(value = "key One")
    private String keyOne;

    /**
     * Optional Key value pair to describe your list.
     */
    @JsonProperty(value = "key Two")
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
     * @return the CreateImageListMetadata object itself.
     */
    public CreateImageListMetadata withKeyOne(String keyOne) {
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
     * @return the CreateImageListMetadata object itself.
     */
    public CreateImageListMetadata withKeyTwo(String keyTwo) {
        this.keyTwo = keyTwo;
        return this;
    }

}
