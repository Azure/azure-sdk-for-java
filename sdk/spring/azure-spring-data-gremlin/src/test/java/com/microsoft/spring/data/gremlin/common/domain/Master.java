// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.common.domain;

import com.microsoft.spring.data.gremlin.annotation.Vertex;
import com.microsoft.spring.data.gremlin.common.TestConstants;

import java.util.Objects;

@Vertex(label = TestConstants.VERTEX_LABEL)
public class Master {

    private String id;

    private String name;

    public Master() {
    }

    public Master(String id, String name) {
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
        Master master = (Master) o;
        return Objects.equals(id, master.id)
            && Objects.equals(name, master.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
