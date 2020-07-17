// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.performance.domain;

import com.azure.spring.data.cosmos.core.mapping.Document;
import com.azure.spring.data.cosmos.core.mapping.DocumentIndexingPolicy;
import com.azure.spring.data.cosmos.performance.utils.Constants;

import java.util.Objects;

@Document(container = Constants.SPRING_CONTAINER_NAME)
@DocumentIndexingPolicy()
public class PerfPerson {
    private String id;
    private String name;

    public PerfPerson(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PerfPerson that = (PerfPerson) o;
        return Objects.equals(id, that.id)
            && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "PerfPerson{"
            + "id='"
            + id
            + '\''
            + ", name='"
            + name
            + '\''
            + '}';
    }
}
