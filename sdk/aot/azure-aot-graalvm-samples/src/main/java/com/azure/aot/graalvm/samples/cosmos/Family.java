// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.cosmos;

import java.util.Arrays;

/**
 * The family model.
 */
public class Family {
    private String id = "";
    private String lastName = "";
    private String district = "";
    private Parent[] parents = { };
    private Child[] children = { };
    private Address address = new Address();
    private boolean isRegistered = false;

    /**
     * Creates a new instance of {@link Family}.
     */
    public Family() {
    }

    /**
     * Returns the family id.
     * @return the family id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the family id.
     * @param id the family id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the family last name.
     * @return the family last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the family last name.
     * @param lastName the family last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the district.
     * @return the district.
     */
    public String getDistrict() {
        return district;
    }

    /**
     * Sets the district.
     * @param district the district.
     */
    public void setDistrict(String district) {
        this.district = district;
    }

    /**
     * Returns the parents array.
     * @return the parents array.
     */
    public Parent[] getParents() {
        return Arrays.copyOf(parents, parents.length);
    }

    /**
     * Sets the parents array.
     * @param parents the parents array.
     */
    public void setParents(Parent[] parents) {
        this.parents = Arrays.copyOf(parents, parents.length);
    }

    /**
     * Returns the children array.
     * @return the children array.
     */
    public Child[] getChildren() {
        return Arrays.copyOf(children, children.length);
    }

    /**
     * Sets the children array.
     * @param children the children array.
     */
    public void setChildren(Child[] children) {
        this.children = Arrays.copyOf(children, children.length);
    }

    /**
     * Returns the address.
     * @return the address.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the address.
     * @param address the address.
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Returns true if the family is registered.
     * @return true if the family is registered.
     */
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Sets the registration state of the family.
     * @param isRegistered the registration state of the family
     */
    public void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }
}
