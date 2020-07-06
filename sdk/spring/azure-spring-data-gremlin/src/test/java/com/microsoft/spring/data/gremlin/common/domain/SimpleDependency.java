// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.common.domain;

import com.microsoft.spring.data.gremlin.annotation.Edge;
import com.microsoft.spring.data.gremlin.annotation.EdgeFrom;
import com.microsoft.spring.data.gremlin.annotation.EdgeTo;

import java.util.Objects;

@Edge
public class SimpleDependency {

    private String id;

    private String name;

    @EdgeFrom
    private String fromId;

    @EdgeTo
    private String toId;

    public SimpleDependency() {
    }

    public SimpleDependency(String id, String name, String fromId, String toId) {
        this.id = id;
        this.name = name;
        this.fromId = fromId;
        this.toId = toId;
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

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleDependency that = (SimpleDependency) o;
        return Objects.equals(id, that.id)
            && Objects.equals(name, that.name)
            && Objects.equals(fromId, that.fromId)
            && Objects.equals(toId, that.toId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fromId, toId);
    }
}
