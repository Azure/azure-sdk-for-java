// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.common.domain;

import com.microsoft.spring.data.gremlin.annotation.Vertex;
import com.microsoft.spring.data.gremlin.common.TestConstants;

import java.util.Objects;

@Vertex(label = TestConstants.VERTEX_PROJECT_LABEL)
public class Project {

    private String id;

    private String name;

    private String uri;

    public Project() {
    }

    public Project(String id, String name, String uri) {
        this.id = id;
        this.name = name;
        this.uri = uri;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Project project = (Project) o;
        return Objects.equals(id, project.id)
            && Objects.equals(name, project.name)
            && Objects.equals(uri, project.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, uri);
    }
}
