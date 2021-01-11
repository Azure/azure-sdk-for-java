// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.data.tables.models.TableEntity;

public class SingleFieldEntity extends TableEntity {
    private String subclassProperty;

    public SingleFieldEntity(String partitionKey, String rowKey) {
        super(partitionKey, rowKey);
    }

    public String getSubclassProperty() {
        return subclassProperty;
    }

    public void setSubclassProperty(String subclassProperty) {
        this.subclassProperty = subclassProperty;
    }
}
