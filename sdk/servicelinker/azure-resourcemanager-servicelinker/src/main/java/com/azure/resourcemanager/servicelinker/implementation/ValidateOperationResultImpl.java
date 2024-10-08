// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.servicelinker.implementation;

import com.azure.resourcemanager.servicelinker.fluent.models.ValidateOperationResultInner;
import com.azure.resourcemanager.servicelinker.models.AuthType;
import com.azure.resourcemanager.servicelinker.models.ValidateOperationResult;
import com.azure.resourcemanager.servicelinker.models.ValidationResultItem;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

public final class ValidateOperationResultImpl implements ValidateOperationResult {
    private ValidateOperationResultInner innerObject;

    private final com.azure.resourcemanager.servicelinker.ServiceLinkerManager serviceManager;

    ValidateOperationResultImpl(ValidateOperationResultInner innerObject,
        com.azure.resourcemanager.servicelinker.ServiceLinkerManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
    }

    public String resourceId() {
        return this.innerModel().resourceId();
    }

    public String status() {
        return this.innerModel().status();
    }

    public String linkerName() {
        return this.innerModel().linkerName();
    }

    public Boolean isConnectionAvailable() {
        return this.innerModel().isConnectionAvailable();
    }

    public OffsetDateTime reportStartTimeUtc() {
        return this.innerModel().reportStartTimeUtc();
    }

    public OffsetDateTime reportEndTimeUtc() {
        return this.innerModel().reportEndTimeUtc();
    }

    public String sourceId() {
        return this.innerModel().sourceId();
    }

    public String targetId() {
        return this.innerModel().targetId();
    }

    public AuthType authType() {
        return this.innerModel().authType();
    }

    public List<ValidationResultItem> validationDetail() {
        List<ValidationResultItem> inner = this.innerModel().validationDetail();
        if (inner != null) {
            return Collections.unmodifiableList(inner);
        } else {
            return Collections.emptyList();
        }
    }

    public ValidateOperationResultInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager() {
        return this.serviceManager;
    }
}
