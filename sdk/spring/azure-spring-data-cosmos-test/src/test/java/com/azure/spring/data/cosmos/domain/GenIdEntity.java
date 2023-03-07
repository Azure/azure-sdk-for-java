// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import org.springframework.data.annotation.Id;

import java.util.Objects;

public class GenIdEntity {

    @Id
    @GeneratedValue
    private String id;

    private String foo;

    public GenIdEntity() {
    }

    public GenIdEntity(String id, String foo) {
        this.id = id;
        this.foo = foo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GenIdEntity that = (GenIdEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(foo, that.foo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, foo);
    }

    @Override
    public String toString() {
        return "GenIdEntity{"
            + "id='"
            + id
            + '\''
            + ", foo='"
            + foo
            + '\''
            + '}';
    }
}
