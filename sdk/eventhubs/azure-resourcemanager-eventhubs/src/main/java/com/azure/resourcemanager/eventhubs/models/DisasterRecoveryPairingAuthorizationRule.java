// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.models.AuthorizationRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Type representing authorization rule of {@link EventHubDisasterRecoveryPairing}.
 */
@Fluent
public interface DisasterRecoveryPairingAuthorizationRule
    extends HasName, HasInnerModel<AuthorizationRuleInner>, HasManager<EventHubsManager> {
    /**
     * Gets rights associated with the rule.
     *
     * @return rights associated with the rule
     */
    List<AccessRights> rights();

    /**
     * Gets an observable that emits a single entity containing access keys (primary and secondary).
     *
     * @return an observable that emits a single entity containing access keys (primary and secondary)
     */
    Mono<DisasterRecoveryPairingAuthorizationKey> getKeysAsync();

    /**
     * Gets entity containing access keys (primary and secondary).
     *
     * @return entity containing access keys (primary and secondary)
     */
    DisasterRecoveryPairingAuthorizationKey getKeys();
}
