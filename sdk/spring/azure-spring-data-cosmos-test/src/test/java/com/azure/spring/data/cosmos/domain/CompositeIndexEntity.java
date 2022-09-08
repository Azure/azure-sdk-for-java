// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.cosmos.models.CompositePathSortOrder;
import com.azure.spring.data.cosmos.core.mapping.CompositeIndex;
import com.azure.spring.data.cosmos.core.mapping.CompositeIndexPath;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import org.springframework.data.annotation.Id;

@Container
@CosmosIndexingPolicy(compositeIndexes = {
    @CompositeIndex(paths = {
        @CompositeIndexPath(path = "/fieldOne"),
        @CompositeIndexPath(path = "/fieldTwo")
    }),
    @CompositeIndex(paths = {
        @CompositeIndexPath(path = "/fieldThree", order = CompositePathSortOrder.DESCENDING),
        @CompositeIndexPath(path = "/fieldFour", order = CompositePathSortOrder.DESCENDING)
    })
})
public class CompositeIndexEntity {

    @Id
    String id;

    String fieldOne;
    String fieldTwo;
    String fieldThree;
    String fieldFour;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFieldOne() {
        return fieldOne;
    }

    public void setFieldOne(String fieldOne) {
        this.fieldOne = fieldOne;
    }

    public String getFieldTwo() {
        return fieldTwo;
    }

    public void setFieldTwo(String fieldTwo) {
        this.fieldTwo = fieldTwo;
    }

    public String getFieldThree() {
        return fieldThree;
    }

    public void setFieldThree(String fieldThree) {
        this.fieldThree = fieldThree;
    }

    public String getFieldFour() {
        return fieldFour;
    }

    public void setFieldFour(String fieldFour) {
        this.fieldFour = fieldFour;
    }
}
