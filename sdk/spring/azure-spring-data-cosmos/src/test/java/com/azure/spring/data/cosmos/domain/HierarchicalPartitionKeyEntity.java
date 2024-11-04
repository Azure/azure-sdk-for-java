// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import org.springframework.data.annotation.Id;

@Container(hierarchicalPartitionKeyPaths = {"/id", "/firstName", "/lastName"},
    ru = TestConstants.DEFAULT_MINIMUM_RU)
public class HierarchicalPartitionKeyEntity {

    @Id
    @GeneratedValue
    private String id;

    private String firstName;

    private String lastName;

    private String zipcode;

    public HierarchicalPartitionKeyEntity(String id, String firstName, String lastName, String zipcode) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.zipcode = zipcode;
    }

    public HierarchicalPartitionKeyEntity() {

    }

    public String getId() {
        return this.id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getZipcode() {
        return this.zipcode;
    }
}
