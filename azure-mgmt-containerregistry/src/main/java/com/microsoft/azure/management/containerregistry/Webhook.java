/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.containerregistry.implementation.WebhookInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

import java.util.Collection;

/**
 * An object that represents a web hook for a container registry.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_3_0)
public interface Webhook extends
    ExternalChildResource<Webhook, Registry>,
    HasInner<WebhookInner> {

    /**
     * @return the status of the web hook
     */
    boolean isEnabled();

    /**
     * @return the scope of repositories where the event can be triggered
     * <p>
     * For example:
     *  - 'foo:*' means events for all tags under repository 'foo'
     *  - 'foo:bar' means events for 'foo:bar' only
     *  - 'foo' is equivalent to 'foo:latest'
     *  - empty means all events
     */
    String scope();

    /**
     * @return the list of actions that trigger the web hook to post notifications
     */
    Collection<WebhookAction> actions();

    /**
     * @return the provisioning state of the web hook
     */
    ProvisioningState provisioningState();
}
