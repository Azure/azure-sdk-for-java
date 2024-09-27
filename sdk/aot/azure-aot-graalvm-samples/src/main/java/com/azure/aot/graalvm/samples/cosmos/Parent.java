// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.cosmos;

/**
 * The parent model.
 */
public class Parent {
    private String familyName;
    private String firstName;

    /**
     * The empty constructor to create a parent instance.
     */
    public Parent() {
    }

    /**
     * The constructor to create a parent instance with a first name.
     * @param firstName the first name of the parent.
     */
    public Parent(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the family name.
     * @return the family name.
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Sets the family name.
     * @param familyName the family name.
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    /**
     * Returns the first name.
     * @return the first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     * @param firstName the first name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
