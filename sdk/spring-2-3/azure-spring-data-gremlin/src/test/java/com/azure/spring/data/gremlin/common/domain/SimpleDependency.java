// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.domain;

import com.azure.spring.data.gremlin.annotation.Edge;
import com.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.azure.spring.data.gremlin.annotation.EdgeTo;

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
}
