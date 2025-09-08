// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.resourcemanager.containerregistry.models.EventRequestMessage;
import com.azure.resourcemanager.containerregistry.models.EventResponseMessage;
import com.azure.resourcemanager.containerregistry.models.WebhookEventInfo;
import com.azure.resourcemanager.containerregistry.fluent.models.EventInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** Response containing the webhook event info. */
public class WebhookEventInfoImpl extends WrapperImpl<EventInner> implements WebhookEventInfo {
    protected WebhookEventInfoImpl(EventInner innerObject) {
        super(innerObject);
    }

    @Override
    public EventRequestMessage eventRequestMessage() {
        return this.innerModel().eventRequestMessage();
    }

    @Override
    public EventResponseMessage eventResponseMessage() {
        return this.innerModel().eventResponseMessage();
    }
}
