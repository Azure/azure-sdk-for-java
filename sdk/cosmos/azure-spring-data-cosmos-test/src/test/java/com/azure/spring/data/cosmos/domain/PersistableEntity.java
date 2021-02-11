// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;

@Container()
public class PersistableEntity implements Persistable<String> {

    @Id
    private String id;
    @PartitionKey
    private String partitionKey;
    @Version
    private String version;

    public PersistableEntity() {
    }

    public PersistableEntity(String id, String partitionKey) {
        this(id, partitionKey, null);
    }

    public PersistableEntity(String id, String partitionKey, String version) {
        this.id = id;
        this.partitionKey = partitionKey;
        this.version = version;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return version == null;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public String getVersion() {
        return version;
    }

}
