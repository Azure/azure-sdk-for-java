// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.cosmos;

/**
 * The pet model.
 */
public class Pet {
    private String givenName;

    /**
     * Creates a new instance of {@link Pet}.
     */
    public Pet() {
    }

    /**
     * Returns the name of the pet.
     * @return the name of the pet.
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Sets the name of the pet.
     * @param givenName the name of the pet.
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
}
