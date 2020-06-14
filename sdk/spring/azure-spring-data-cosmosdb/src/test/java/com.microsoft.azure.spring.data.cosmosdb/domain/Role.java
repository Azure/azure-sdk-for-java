// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.domain;

import com.azure.data.cosmos.IndexingMode;
import com.microsoft.azure.spring.data.cosmosdb.common.TestConstants;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.Document;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.DocumentIndexingPolicy;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@DocumentIndexingPolicy(
        mode = IndexingMode.LAZY,
        automatic = TestConstants.INDEXINGPOLICY_AUTOMATIC,
        includePaths = {
                TestConstants.INCLUDEDPATH_0,
                TestConstants.INCLUDEDPATH_1,
                TestConstants.INCLUDEDPATH_2,
        },
        excludePaths = {
                TestConstants.EXCLUDEDPATH_0,
                TestConstants.EXCLUDEDPATH_1,
        })
@Document(collection = TestConstants.ROLE_COLLECTION_NAME,
    autoCreateCollection = false)
public class Role {
    @Id
    String id;

    @PartitionKey
    String name;

    String level;

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

