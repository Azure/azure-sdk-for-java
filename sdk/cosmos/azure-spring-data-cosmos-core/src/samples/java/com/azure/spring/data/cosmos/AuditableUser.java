// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;

@Container(containerName = "myContainer")
public class AuditableUser {
    private String id;
    private String firstName;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private OffsetDateTime createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private OffsetDateTime lastModifiedByDate;
}
