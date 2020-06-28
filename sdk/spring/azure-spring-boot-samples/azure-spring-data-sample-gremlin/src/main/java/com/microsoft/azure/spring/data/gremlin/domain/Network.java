// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.gremlin.domain;

import com.microsoft.azure.spring.data.gremlin.annotation.EdgeSet;
import com.microsoft.azure.spring.data.gremlin.annotation.Graph;
import com.microsoft.azure.spring.data.gremlin.annotation.VertexSet;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Graph
public class Network {

    @Id
    private String id;

    public Network() {
        this.edges = new ArrayList<>();
        this.vertexes = new ArrayList<>();
    }

    @EdgeSet
    private List<Object> edges;

    @VertexSet
    private List<Object> vertexes;

    public List<Object> getEdges() {
        return edges;
    }

    public List<Object> getVertexes() {
        return vertexes;
    }
}
