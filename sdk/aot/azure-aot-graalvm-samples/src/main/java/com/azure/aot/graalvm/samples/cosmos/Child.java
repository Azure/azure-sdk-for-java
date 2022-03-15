// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.cosmos;

import java.util.Arrays;

public class Child {
    private String familyName;
    private String firstName;
    private String gender;
    private int grade;
    private Pet[] pets = {};

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public Pet[] getPets() {
        return Arrays.copyOf(pets, pets.length);
    }

    public void setPets(Pet[] pets) {
        this.pets = Arrays.copyOf(pets, pets.length);
    }
}
