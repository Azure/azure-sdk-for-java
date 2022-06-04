// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.cosmos;

import java.util.Arrays;

/**
 * The child model.
 */
public class Child {
    private String familyName;
    private String firstName;
    private String gender;
    private int grade;
    private Pet[] pets = {};

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

    /**
     * Returns the gender.
     * @return the gender.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the gender.
     * @param gender the gender.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Returns the grade.
     * @return the grade.
     */
    public int getGrade() {
        return grade;
    }

    /**
     * Sets the grade.
     * @param grade the grade.
     */
    public void setGrade(int grade) {
        this.grade = grade;
    }

    /**
     * Returns the pets array.
     * @return the pets array.
     */
    public Pet[] getPets() {
        return Arrays.copyOf(pets, pets.length);
    }

    /**
     * Sets the pets array.
     * @param pets the pets array.
     */
    public void setPets(Pet[] pets) {
        this.pets = Arrays.copyOf(pets, pets.length);
    }
}
