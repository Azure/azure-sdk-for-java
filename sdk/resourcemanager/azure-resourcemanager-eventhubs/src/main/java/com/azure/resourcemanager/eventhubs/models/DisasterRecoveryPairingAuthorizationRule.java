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
     * @return rights associated with the rule
     */
    List<AccessRights> rights();

    /**
     * @return an observable that emits a single entity containing access keys (primary and secondary)
     */
    Mono<DisasterRecoveryPairingAuthorizationKey> getKeysAsync();
    /**
     * @return entity containing access keys (primary and secondary)
     */
    DisasterRecoveryPairingAuthorizationKey getKeys();
}
