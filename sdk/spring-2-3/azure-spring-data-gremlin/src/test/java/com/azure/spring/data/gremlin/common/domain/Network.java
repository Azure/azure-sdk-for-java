// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.domain;

import com.azure.spring.data.gremlin.annotation.EdgeSet;
import com.azure.spring.data.gremlin.annotation.Graph;
import com.azure.spring.data.gremlin.annotation.VertexSet;

import java.util.ArrayList;
import java.util.List;

@Graph
public class Network {

    private String id;

    @VertexSet
    private List<Object> vertexList;

    @EdgeSet
    private List<Object> edgeList;

    public Network() {
        this.vertexList = new ArrayList<>();
        this.edgeList = new ArrayList<>();
    }

    public void vertexAdd(Object object) {
        this.vertexList.add(object);
    }

    public void edgeAdd(Object object) {
        this.edgeList.add(object);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Object> getVertexList() {
        return vertexList;
    }

    public List<Object> getEdgeList() {
        return edgeList;
    }
}
