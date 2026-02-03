// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;
import com.azure.spring.data.cosmos.core.mapping.Container;

@Container(partitionKeyPath = "/id")
public class EntityImpl extends ParentEntity {
}
