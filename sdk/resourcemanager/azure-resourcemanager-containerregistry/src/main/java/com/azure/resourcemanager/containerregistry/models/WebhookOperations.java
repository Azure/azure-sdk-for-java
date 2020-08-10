// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import reactor.core.publisher.Mono;

/** Grouping of container registry webhook actions. */
@Fluent
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
    Mono<Webhook> getAsync(String webhookName);

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
    Mono<Void> deleteAsync(String webhookName);

    /**
     * Lists all the webhooks for the container registry.
     *
     * @return the list of all the webhooks for the specified container registry
     */
    PagedIterable<Webhook> list();

    /**
     * Lists all the webhooks for the container registry.
     *
     * @return a representation of the future computation of this call, returning the list of all the webhooks for the
     *     specified container registry
     */
    PagedFlux<Webhook> listAsync();
}
