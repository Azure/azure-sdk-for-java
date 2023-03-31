// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import org.springframework.data.annotation.Id;

@Container
@CosmosIndexingPolicy(
    overwritePolicy = true,
    includePaths = {"/\"_etag\"/?"},
    excludePaths = {"/*"}
)
public class IndexPolicyOverwriteEntity {

    @Id
    String id;

    String field;

    String compositeField1;

    String compositeField2;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getCompositeField1() {
        return compositeField1;
    }

    public void setCompositeField1(String compositeField1) {
        this.compositeField1 = compositeField1;
    }

    public String getCompositeField2() {
        return compositeField2;
    }

    public void setCompositeField2(String compositeField2) {
        this.compositeField2 = compositeField2;
    }
}
