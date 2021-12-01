// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support.pojo;

import java.util.Objects;

/**
 * User
 */
public class User {

    String name;

    /**
     * Default constructor of User.
     */
    public User() {
    }

    /**
     *
     * @param name The name.
     */
    public User(String name) {
        this.name = name;
    }

    /**
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param o another object.
     * @return true if equals.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(name, user.name);
    }

    /**
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
