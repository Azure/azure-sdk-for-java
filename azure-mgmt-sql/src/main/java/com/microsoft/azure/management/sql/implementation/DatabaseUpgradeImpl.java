/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.sql.DatabaseUpgrade;
import com.microsoft.azure.management.sql.RecommendedDatabaseProperties;
import com.microsoft.azure.management.sql.ServerUpgrade;
import com.microsoft.azure.management.sql.TargetDatabaseEditions;

/**
 * Implementation for Restore point interface.
 */
@LangDefinition
class DatabaseUpgradeImpl
        extends WrapperImpl<RecommendedDatabaseProperties>
        implements
        DatabaseUpgrade,
        DatabaseUpgrade.UpgradeDefinition {
    private final ServerUpgradeImpl serverUpgrade;

    protected DatabaseUpgradeImpl(ServerUpgradeImpl serverUpgrade, String databaseName) {
        super(new RecommendedDatabaseProperties().withName(databaseName));
        this.serverUpgrade = serverUpgrade;
    }

    @Override
    public DatabaseUpgradeImpl withTargetEdition(TargetDatabaseEditions targetEdition) {
        this.inner().withTargetEdition(targetEdition);
        return this;
    }

    @Override
    public DatabaseUpgradeImpl withTargetServiceLevelObjective(String targetServiceLevelObjective) {
        this.inner().withTargetServiceLevelObjective(targetServiceLevelObjective);
        return this;
    }

    @Override
    public ServerUpgrade.UpgradeDefinitions.Schedule attach() {
        this.serverUpgrade.addDatabaseUpgrade(this.inner());
        return this.serverUpgrade;
    }
}
