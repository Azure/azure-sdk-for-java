// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import org.springframework.data.annotation.Id;

import java.util.Objects;

public class Customer {

    @Id
    private String id;

    private Long level;

    private User user;

    public Customer() {
    }

    public Customer(String id, Long level, User user) {
        this.id = id;
        this.level = level;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getLevel() {
        return level;
    }

    public void setLevel(Long level) {
        this.level = level;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id)
            && Objects.equals(level, customer.level)
            && Objects.equals(user, customer.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, level, user);
    }

    @Override
    public String toString() {
        return "Customer{"
            + "id='"
            + id
            + '\''
            + ", level="
            + level
            + ", user="
            + user
            + '}';
    }

    public static class User {

        private String name;

        private Long age;

        public User() {
        }

        public User(String name, Long age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getAge() {
            return age;
        }

        public void setAge(Long age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            User user = (User) o;
            return Objects.equals(name, user.name)
                && Objects.equals(age, user.age);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        @Override
        public String toString() {
            return "User{"
                + "name='"
                + name
                + '\''
                + ", age="
                + age
                + '}';
        }
    }
}
