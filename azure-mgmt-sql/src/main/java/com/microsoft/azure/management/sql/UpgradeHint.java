/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.UpgradeHintInner;

import java.util.UUID;


/**
 * An immutable client-side representation of an Azure SQL database's Upgrade hint.
 */
@Fluent
public interface UpgradeHint extends
        Wrapper<UpgradeHintInner> {
    /**
     * @return Target ServiceLevelObjective for upgrade hint.
     */
    String targetServiceLevelObjective();

    /**
     * @return Target ServiceLevelObjectiveId for upgrade hint.
     */
    UUID targetServiceLevelObjectiveId();
}

