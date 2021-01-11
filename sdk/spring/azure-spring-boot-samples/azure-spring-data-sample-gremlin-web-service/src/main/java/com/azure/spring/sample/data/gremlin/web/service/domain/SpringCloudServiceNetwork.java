// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.data.gremlin.web.service.domain;

import com.azure.spring.data.gremlin.annotation.EdgeSet;
import com.azure.spring.data.gremlin.annotation.Graph;
import com.azure.spring.data.gremlin.annotation.VertexSet;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Graph
public class SpringCloudServiceNetwork {

    @Id
    private String id;

    @EdgeSet
    private List<Object> edges = new ArrayList<>();

    @VertexSet
    private List<Object> vertexes = new ArrayList<>();

    public List<Object> getEdges() {
        return edges;
    }

    public List<Object> getVertexes() {
        return vertexes;
    }
}
