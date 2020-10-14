// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.models.EventData;
import com.azure.resourcemanager.monitor.models.EventLevel;
import com.azure.resourcemanager.monitor.models.HttpRequestInfo;
import com.azure.resourcemanager.monitor.models.LocalizableString;
import com.azure.resourcemanager.monitor.models.SenderAuthorization;
import com.azure.resourcemanager.monitor.fluent.models.EventDataInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.time.OffsetDateTime;
import java.util.Map;

/** The Azure {@link EventData} wrapper class implementation. */
class EventDataImpl extends WrapperImpl<EventDataInner> implements EventData {
    private LocalizableString eventName;
    private LocalizableString category;
    private LocalizableString resourceProviderName;
    private LocalizableString resourceType;
    private LocalizableString operationName;
    private LocalizableString status;
    private LocalizableString subStatus;

    EventDataImpl(EventDataInner innerObject) {
        super(innerObject);
        this.eventName =
            (innerModel().eventName() == null) ? null : new LocalizableStringImpl(innerModel().eventName());
        this.category = (innerModel().category() == null) ? null : new LocalizableStringImpl(innerModel().category());
        this.resourceProviderName = (innerModel().resourceProviderName() == null)
            ? null : new LocalizableStringImpl(innerModel().resourceProviderName());
        this.resourceType =
            (innerModel().resourceType() == null) ? null : new LocalizableStringImpl(innerModel().resourceType());
        this.operationName =
            (innerModel().operationName() == null) ? null : new LocalizableStringImpl(innerModel().operationName());
        this.status = (innerModel().status() == null) ? null : new LocalizableStringImpl(innerModel().status());
        this.subStatus =
            (innerModel().subStatus() == null) ? null : new LocalizableStringImpl(innerModel().subStatus());
    }

    @Override
    public SenderAuthorization authorization() {
        return this.innerModel().authorization();
    }

    @Override
    public Map<String, String> claims() {
        return this.innerModel().claims();
    }

    @Override
    public String caller() {
        return this.innerModel().caller();
    }

    @Override
    public String description() {
        return this.innerModel().description();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String eventDataId() {
        return this.innerModel().eventDataId();
    }

    @Override
    public String correlationId() {
        return this.innerModel().correlationId();
    }

    @Override
    public LocalizableString eventName() {
        return this.eventName;
    }

    @Override
    public LocalizableString category() {
        return this.category;
    }

    @Override
    public HttpRequestInfo httpRequest() {
        return this.innerModel().httpRequest();
    }

    @Override
    public EventLevel level() {
        return this.innerModel().level();
    }

    @Override
    public String resourceGroupName() {
        return this.innerModel().resourceGroupName();
    }

    @Override
    public LocalizableString resourceProviderName() {
        return this.resourceProviderName;
    }

    @Override
    public String resourceId() {
        return this.innerModel().resourceId();
    }

    @Override
    public LocalizableString resourceType() {
        return this.resourceType;
    }

    @Override
    public String operationId() {
        return this.innerModel().operationId();
    }

    @Override
    public LocalizableString operationName() {
        return this.operationName;
    }

    @Override
    public Map<String, String> properties() {
        return this.innerModel().properties();
    }

    @Override
    public LocalizableString status() {
        return this.status;
    }

    @Override
    public LocalizableString subStatus() {
        return this.subStatus;
    }

    @Override
    public OffsetDateTime eventTimestamp() {
        return this.innerModel().eventTimestamp();
    }

    @Override
    public OffsetDateTime submissionTimestamp() {
        return this.innerModel().submissionTimestamp();
    }

    @Override
    public String subscriptionId() {
        return this.innerModel().subscriptionId();
    }

    @Override
    public String tenantId() {
        return this.innerModel().tenantId();
    }
}
