// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.domain;

import com.azure.spring.data.gremlin.annotation.Edge;
import com.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.azure.spring.data.gremlin.annotation.EdgeTo;

@Edge
public class Dependency {

    private String id;

    private String type;

    @EdgeFrom
    private Library source;

    @EdgeTo
    private Library target;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Library getSource() {
        return source;
    }

    public void setSource(Library source) {
        this.source = source;
    }

    public Library getTarget() {
        return target;
    }

    public void setTarget(Library target) {
        this.target = target;
    }
}
