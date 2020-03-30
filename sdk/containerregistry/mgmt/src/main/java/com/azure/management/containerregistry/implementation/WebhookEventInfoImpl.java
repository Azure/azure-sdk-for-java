/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry.implementation;

import com.azure.management.containerregistry.EventRequestMessage;
import com.azure.management.containerregistry.EventResponseMessage;
import com.azure.management.containerregistry.WebhookEventInfo;
import com.azure.management.containerregistry.models.EventInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Response containing the webhook event info.
 */
public class WebhookEventInfoImpl extends WrapperImpl<EventInner> implements WebhookEventInfo {
    protected WebhookEventInfoImpl(EventInner innerObject) {
        super(innerObject);
    }

    @Override
    public EventRequestMessage eventRequestMessage() {
        return this.inner().eventRequestMessage();
    }

    @Override
    public EventResponseMessage eventResponseMessage() {
        return this.inner().eventResponseMessage();
    }
}
