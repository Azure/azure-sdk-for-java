// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.models.EventData;
import com.azure.resourcemanager.monitor.models.EventLevel;
import com.azure.resourcemanager.monitor.models.HttpRequestInfo;
import com.azure.resourcemanager.monitor.models.LocalizableString;
import com.azure.resourcemanager.monitor.models.SenderAuthorization;
import com.azure.resourcemanager.monitor.fluent.inner.EventDataInner;
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
        this.eventName = (inner().eventName() == null) ? null : new LocalizableStringImpl(inner().eventName());
        this.category = (inner().category() == null) ? null : new LocalizableStringImpl(inner().category());
        this.resourceProviderName =
            (inner().resourceProviderName() == null) ? null : new LocalizableStringImpl(inner().resourceProviderName());
        this.resourceType = (inner().resourceType() == null) ? null : new LocalizableStringImpl(inner().resourceType());
        this.operationName =
            (inner().operationName() == null) ? null : new LocalizableStringImpl(inner().operationName());
        this.status = (inner().status() == null) ? null : new LocalizableStringImpl(inner().status());
        this.subStatus = (inner().subStatus() == null) ? null : new LocalizableStringImpl(inner().subStatus());
    }

    @Override
    public SenderAuthorization authorization() {
        return this.inner().authorization();
    }

    @Override
    public Map<String, String> claims() {
        return this.inner().claims();
    }

    @Override
    public String caller() {
        return this.inner().caller();
    }

    @Override
    public String description() {
        return this.inner().description();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String eventDataId() {
        return this.inner().eventDataId();
    }

    @Override
    public String correlationId() {
        return this.inner().correlationId();
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
        return this.inner().httpRequest();
    }

    @Override
    public EventLevel level() {
        return this.inner().level();
    }

    @Override
    public String resourceGroupName() {
        return this.inner().resourceGroupName();
    }

    @Override
    public LocalizableString resourceProviderName() {
        return this.resourceProviderName;
    }

    @Override
    public String resourceId() {
        return this.inner().resourceId();
    }

    @Override
    public LocalizableString resourceType() {
        return this.resourceType;
    }

    @Override
    public String operationId() {
        return this.inner().operationId();
    }

    @Override
    public LocalizableString operationName() {
        return this.operationName;
    }

    @Override
    public Map<String, String> properties() {
        return this.inner().properties();
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
        return this.inner().eventTimestamp();
    }

    @Override
    public OffsetDateTime submissionTimestamp() {
        return this.inner().submissionTimestamp();
    }

    @Override
    public String subscriptionId() {
        return this.inner().subscriptionId();
    }

    @Override
    public String tenantId() {
        return this.inner().tenantId();
    }
}
