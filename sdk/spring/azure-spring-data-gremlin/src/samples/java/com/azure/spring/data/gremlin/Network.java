// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin;

import com.azure.spring.data.gremlin.annotation.EdgeSet;
import com.azure.spring.data.gremlin.annotation.Graph;
import com.azure.spring.data.gremlin.annotation.VertexSet;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 * <p>
 * Code samples for the spring-data-gremlin in README.md
 */
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

}
