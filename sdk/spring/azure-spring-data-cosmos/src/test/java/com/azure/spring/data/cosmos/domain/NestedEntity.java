// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import java.util.Objects;

public class NestedEntity {
    private String nestedPartitionKey;

    public NestedEntity(String nestedPartitionKey) {
        this.nestedPartitionKey = nestedPartitionKey;
    }

    public NestedEntity() {

    }

    public String getNestedPartitionKey() {
        return nestedPartitionKey;
    }

    public void setNestedPartitionKey(String nestedPartitionKey) {
        this.nestedPartitionKey = nestedPartitionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NestedEntity that = (NestedEntity) o;
        return Objects.equals(nestedPartitionKey, that.nestedPartitionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nestedPartitionKey);
    }

    @Override
    public String toString() {
        return "NestedEntity{"
            + "nestedPartitionKey='" + nestedPartitionKey + '\''
            + '}';
    }
}
