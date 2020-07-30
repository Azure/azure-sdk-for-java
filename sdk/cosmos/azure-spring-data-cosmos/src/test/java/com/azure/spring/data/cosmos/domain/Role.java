// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.cosmos.models.IndexingMode;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@CosmosIndexingPolicy(
        mode = IndexingMode.CONSISTENT,
        automatic = TestConstants.INDEXING_POLICY_AUTOMATIC)
@Container(containerName = TestConstants.ROLE_COLLECTION_NAME,
    autoCreateContainer = false)
public class Role {
    @Id
    String id;

    @PartitionKey
    String name;

    String level;

    public Role() {
    }

    public Role(String id, String name, String level) {
        this.id = id;
        this.name = name;
        this.level = level;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Role role = (Role) o;
        return Objects.equals(id, role.id)
            && Objects.equals(name, role.name)
            && Objects.equals(level, role.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, level);
    }

    @Override
    public String toString() {
        return "Role{"
            + "id='"
            + id
            + '\''
            + ", name='"
            + name
            + '\''
            + ", level='"
            + level
            + '\''
            + '}';
    }
}

