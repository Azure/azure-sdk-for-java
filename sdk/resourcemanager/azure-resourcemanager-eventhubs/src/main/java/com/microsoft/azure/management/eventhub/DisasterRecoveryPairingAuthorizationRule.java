/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.eventhub.implementation.AuthorizationRuleInner;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Observable;

import java.util.List;

/**
 * Type representing authorization rule of {@link EventHubDisasterRecoveryPairing}.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface DisasterRecoveryPairingAuthorizationRule
        extends
        HasName,
        HasInner<AuthorizationRuleInner>,
        HasManager<EventHubManager> {
    /**
     * @return rights associated with the rule
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    List<AccessRights> rights();
    /**
     * @return an observable that emits a single entity containing access keys (primary and secondary)
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<DisasterRecoveryPairingAuthorizationKey> getKeysAsync();
    /**
     * @return entity containing access keys (primary and secondary)
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    DisasterRecoveryPairingAuthorizationKey getKeys();
}
