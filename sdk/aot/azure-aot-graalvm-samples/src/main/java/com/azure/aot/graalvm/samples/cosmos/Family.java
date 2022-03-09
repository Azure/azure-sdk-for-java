// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.cosmos;

import java.util.Arrays;

public class Family {
    private String id = "";
    private String lastName = "";
    private String district = "";
    private Parent[] parents = {};
    private Child[] children = {};
    private Address address = new Address();
    private boolean isRegistered = false;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Parent[] getParents() {
        return Arrays.copyOf(parents, parents.length);
    }

    public void setParents(Parent[] parents) {
        this.parents = Arrays.copyOf(parents, parents.length);
    }

    public Child[] getChildren() {
        return Arrays.copyOf(children, children.length);
    }

    public void setChildren(Child[] children) {
        this.children = Arrays.copyOf(children, children.length);
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }
}
