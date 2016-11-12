/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.sql.ElasticPoolUpgrade;
import com.microsoft.azure.management.sql.ServerUpgrade;
import com.microsoft.azure.management.sql.TargetElasticPoolEditions;
import com.microsoft.azure.management.sql.UpgradeRecommendedElasticPoolProperties;

import java.util.List;

/**
 * Implementation for Restore point interface.
 */
@LangDefinition
class ElasticPoolUpgradeImpl
        extends WrapperImpl<UpgradeRecommendedElasticPoolProperties>
        implements
        ElasticPoolUpgrade,
        ElasticPoolUpgrade.UpgradeDefinition {
    private final ServerUpgradeImpl serverUpgrade;

    protected ElasticPoolUpgradeImpl(ServerUpgradeImpl serverUpgrade, String elasticPoolName) {
        super(new UpgradeRecommendedElasticPoolProperties().withName(elasticPoolName));
        this.serverUpgrade = serverUpgrade;
    }

    @Override
    public ElasticPoolUpgradeImpl withEdition(TargetElasticPoolEditions edition) {
        this.inner().withEdition(edition);
        return this;
    }

    @Override
    public ElasticPoolUpgradeImpl withDtu(int dtu) {
        this.inner().withDtu(dtu);
        return this;
    }

    @Override
    public ElasticPoolUpgradeImpl withStorageMb(int storageMb) {
        this.inner().withStorageMb(storageMb);
        return this;
    }

    @Override
    public ElasticPoolUpgradeImpl withDatabaseDtuMin(int databaseDtuMin) {
        this.inner().withDatabaseDtuMin(databaseDtuMin);
        return this;
    }

    @Override
    public ElasticPoolUpgradeImpl withDatabaseDtuMax(int databaseDtuMax) {
        this.inner().withDatabaseDtuMax(databaseDtuMax);
        return this;
    }

    @Override
    public ElasticPoolUpgradeImpl withDatabaseCollection(List<String> databaseCollection) {
        this.inner().withDatabaseCollection(databaseCollection);
        return this;
    }

    @Override
    public ElasticPoolUpgradeImpl withIncludeAllDatabases(boolean includeAllDatabases) {
        this.inner().withIncludeAllDatabases(includeAllDatabases);
        return this;
    }

    @Override
    public ServerUpgrade.UpgradeDefinitions.Schedule attach() {
        this.serverUpgrade.addElasticPoolUpgrade(this.inner());
        return this.serverUpgrade;
    }
}
