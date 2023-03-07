// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Container()
@CosmosIndexingPolicy()
public class Project {

    @Id
    private String id;

    private String name;

    @PartitionKey
    private String creator;

    private Boolean hasReleased;

    private Long starCount;

    private Long forkCount;

    public Project() {
    }

    public Project(String id, String name, String creator, Boolean hasReleased, Long starCount, Long forkCount) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.hasReleased = hasReleased;
        this.starCount = starCount;
        this.forkCount = forkCount;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Boolean getHasReleased() {
        return hasReleased;
    }

    public void setHasReleased(Boolean hasReleased) {
        this.hasReleased = hasReleased;
    }

    public Long getStarCount() {
        return starCount;
    }

    public void setStarCount(Long starCount) {
        this.starCount = starCount;
    }

    public Long getForkCount() {
        return forkCount;
    }

    public void setForkCount(Long forkCount) {
        this.forkCount = forkCount;
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
            && Objects.equals(creator, project.creator)
            && Objects.equals(hasReleased, project.hasReleased)
            && Objects.equals(starCount, project.starCount)
            && Objects.equals(forkCount, project.forkCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, creator, hasReleased, starCount, forkCount);
    }

    @Override
    public String toString() {
        return "Project{"
            + "id='"
            + id
            + '\''
            + ", name='"
            + name
            + '\''
            + ", creator='"
            + creator
            + '\''
            + ", hasReleased="
            + hasReleased
            + ", starCount="
            + starCount
            + ", forkCount="
            + forkCount
            + '}';
    }
}
