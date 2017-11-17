/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerregistry.EventRequestMessage;
import com.microsoft.azure.management.containerregistry.EventResponseMessage;
import com.microsoft.azure.management.containerregistry.WebhookEventInfo;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Response containing the webhook event info.
 */
@LangDefinition
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
