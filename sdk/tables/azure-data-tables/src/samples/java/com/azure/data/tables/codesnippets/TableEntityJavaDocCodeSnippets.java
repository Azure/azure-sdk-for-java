// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.codesnippets;

import com.azure.data.tables.models.TableEntity;

import java.util.HashMap;
import java.util.Map;

public class TableEntityJavaDocCodeSnippets {

    public void createTableEntity() {
        // BEGIN: com.azure.data.tables.models.TableEntity.create#string-string
        TableEntity entity = new TableEntity("partitionKey", "rowKey");
        // END: com.azure.data.tables.models.TableEntity.create#string-string
    }

    public void addProperties() {
        // BEGIN: com.azure.data.tables.models.TableEntity.addProperty#string-object
        TableEntity entity = new TableEntity("partitionKey", "rowKey")
            .addProperty("String", "StringValue")
            .addProperty("Integer", 100)
            .addProperty("Boolean", true);
        // END: com.azure.data.tables.models.TableEntity.addProperty#string-object
    }

    public void setPropertyMap() {
        // BEGIN: com.azure.data.tables.models.TableEntity.setProperties#map
        Map<String, Object> properties = new HashMap<>();
        properties.put("String", "StringValue");
        properties.put("Integer", 100);
        properties.put("Boolean", true);
        TableEntity entity = new TableEntity("partitionKey", "rowKey")
            .setProperties(properties);
        // END: com.azure.data.tables.models.TableEntity.setProperties#map
    }

    public void getProperty() {
        // BEGIN: com.azure.data.tables.models.TableEntity.getProperty#string
        TableEntity entity = new TableEntity("partitionKey", "rowKey")
            .addProperty("String", "StringValue")
            .addProperty("Integer", 100)
            .addProperty("Boolean", true);

        String stringValue = (String) entity.getProperty("String");
        int integerValue = (int) entity.getProperty("Integer");
        boolean booleanValue = (boolean) entity.getProperty("Boolean");
        // END: com.azure.data.tables.models.TableEntity.getProperty#string
    }

    public void getAllProperties() {
        // BEGIN: com.azure.data.tables.models.TableEntity.getProperties
        TableEntity entity = new TableEntity("partitionKey", "rowKey")
            .addProperty("String", "StringValue")
            .addProperty("Integer", 100)
            .addProperty("Boolean", true);

        Map<String, Object> properties = entity.getProperties();
        // END: com.azure.data.tables.models.TableEntity.getProperties
    }

}
