/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import rx.Completable;
import rx.Observable;

/**
 * Grouping of container registry webhook actions.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_4_0)
public interface WebhookOperations {
    /**
     * Gets the properties of the specified webhook.
     *
     * @param webhookName the name of the webhook
     * @return the Webhook object if successful
     */
    Webhook get(String webhookName);

    /**
     * Gets the properties of the specified webhook.
     *
     * @param webhookName the name of the webhook
     * @return a representation of the future computation of this call, returning the Webhook object
     */
    Observable<Webhook> getAsync(String webhookName);

    /**
     * Deletes a webhook from the container registry.
     *
     * @param webhookName the name of the webhook
     */
    void delete(String webhookName);

    /**
     * Deletes a webhook from the container registry.
     *
     * @param webhookName the name of the webhook
     * @return a representation of the future computation of this call
     */
    Completable deleteAsync(String webhookName);

    /**
     * Lists all the webhooks for the container registry.
     *
     * @return the list of all the webhooks for the specified container registry
     */
    PagedList<Webhook> list();

    /**
     * Lists all the webhooks for the container registry.
     *
     * @return a representation of the future computation of this call, returning the list of all the webhooks for the specified container registry
     */
    @Beta(Beta.SinceVersion.V1_4_0)
    Observable<Webhook> listAsync();
}
