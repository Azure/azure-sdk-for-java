/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.sql.CreateMode;
import com.microsoft.azure.management.sql.DatabaseEditions;
import com.microsoft.azure.management.sql.DatabaseMetric;
import com.microsoft.azure.management.sql.RestorePoint;
import com.microsoft.azure.management.sql.ServiceObjectiveName;
import com.microsoft.azure.management.sql.ServiceTierAdvisor;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.sql.TransparentDataEncryption;
import com.microsoft.azure.management.sql.UpgradeHint;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.UUID;

/**
 * Implementation for SqlDatabase and its parent interfaces.
 */
class SqlDatabaseImpl
        extends IndependentChildResourceImpl<
                            SqlDatabase,
                            SqlServer,
                            DatabaseInner,
                            SqlDatabaseImpl>
        implements SqlDatabase,
            SqlDatabase.Definition,
            SqlDatabase.Update,
        IndependentChild.DefinitionStages.WithParentResource<SqlDatabase, SqlServer> {
    private final DatabasesInner innerCollection;
    private String elasticPoolCreatableKey;

    protected SqlDatabaseImpl(String name,
                            DatabaseInner innerObject,
                            DatabasesInner innerCollection) {
        super(name, innerObject);
        this.innerCollection = innerCollection;
    }

    @Override
    public String sqlServerName() {
        return this.parentName;
    }

    @Override
    public String collation() {
        return this.inner().collation();
    }

    @Override
    public DateTime creationDate() {
        return this.inner().creationDate();
    }

    @Override
    public UUID currentServiceObjectiveId() {
        return this.inner().currentServiceObjectiveId();
    }

    @Override
    public String databaseId() {
        return this.inner().databaseId();
    }

    @Override
    public DateTime earliestRestoreDate() {
        return this.inner().earliestRestoreDate();
    }

    @Override
    public DatabaseEditions edition() {
        return this.inner().edition();
    }

    @Override
    public UUID requestedServiceObjectiveId() {
        return this.inner().requestedServiceObjectiveId();
    }

    @Override
    public long maxSizeBytes() {
        return Long.parseLong(this.inner().maxSizeBytes());
    }

    @Override
    public ServiceObjectiveName requestedServiceObjectiveName() {
        return this.inner().requestedServiceObjectiveName();
    }

    @Override
    public ServiceObjectiveName serviceLevelObjective() {
        return this.inner().serviceLevelObjective();
    }

    @Override
    public String status() {
        return this.inner().status();
    }

    @Override
    public String elasticPoolName() {
        return this.inner().elasticPoolName();
    }

    @Override
    public String defaultSecondaryLocation() {
        return this.inner().defaultSecondaryLocation();
    }

    @Override
    public UpgradeHint getUpgradeHint() {
        if (this.inner().upgradeHint() == null) {
            this.setInner(this.innerCollection.get(this.resourceGroupName(), this.sqlServerName(), this.name(), "upgradeHint"));
        }
        if (this.inner().upgradeHint() != null) {
            return new UpgradeHintImpl(this.inner().upgradeHint());
        }
        return null;
    }

    @Override
    public ReplicationLinks replicationLinks() {
        return new ReplicationLinksImpl(this.innerCollection, this.resourceGroupName(), this.sqlServerName(), this.name());
    }

    @Override
    public void pauseDataWarehouse() {
        this.innerCollection.pauseDataWarehouse(this.resourceGroupName(), this.sqlServerName(), this.name());
    }

    @Override
    public void resumeDataWarehouse() {
        this.innerCollection.resumeDataWarehouse(this.resourceGroupName(), this.sqlServerName(), this.name());
    }

    @Override
    public List<RestorePoint> listRestorePoints() {
        PagedListConverter<RestorePointInner, RestorePoint> converter = new PagedListConverter<RestorePointInner, RestorePoint>() {
            @Override
            public RestorePoint typeConvert(RestorePointInner restorePointInner) {

                return new RestorePointImpl(restorePointInner);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                this.innerCollection.listRestorePoints(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name())));
    }

    @Override
    public List<DatabaseMetric> listUsages() {
        PagedListConverter<DatabaseMetricInner, DatabaseMetric> converter = new PagedListConverter<DatabaseMetricInner, DatabaseMetric>() {
            @Override
            public DatabaseMetric typeConvert(DatabaseMetricInner databaseMetricInner) {
                return new DatabaseMetricImpl(databaseMetricInner);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                this.innerCollection.listUsages(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name())));
    }

    @Override
    public TransparentDataEncryption getTransparentDataEncryption() {
        return new TransparentDataEncryptionImpl(
                this.innerCollection.getTransparentDataEncryptionConfiguration(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name()), this.innerCollection);
    }

    @Override
    public List<ServiceTierAdvisor> listServiceTierAdvisors() {
        final SqlDatabaseImpl self = this;
        PagedListConverter<ServiceTierAdvisorInner, ServiceTierAdvisor> converter
                = new PagedListConverter<ServiceTierAdvisorInner, ServiceTierAdvisor>() {
            @Override
            public ServiceTierAdvisor typeConvert(ServiceTierAdvisorInner serviceTierAdvisorInner) {
                return new ServiceTierAdvisorImpl(serviceTierAdvisorInner, self.innerCollection);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                this.innerCollection.listServiceTierAdvisors(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name())));
    }

    @Override
    public SqlDatabase refresh() {
        if (this.inner().upgradeHint() != null) {
            this.setInner(this.innerCollection.get(this.resourceGroupName(), this.sqlServerName(), this.name()));
        }
        else {
            this.setInner(this.innerCollection.get(this.resourceGroupName(), this.sqlServerName(), this.name(), "upgradeHint"));
        }

        return this;
    }

    @Override
    protected Observable<SqlDatabase> createChildResourceAsync() {
        final SqlDatabaseImpl self = this;
        if (this.elasticPoolCreatableKey != null) {
            SqlElasticPool sqlElasticPool = (SqlElasticPool) this.createdResource(this.elasticPoolCreatableKey);
            withExistingElasticPool(sqlElasticPool);
        }
        if (this.inner().elasticPoolName() != null && !this.inner().elasticPoolName().isEmpty()) {
            this.inner().withEdition(new DatabaseEditions(""));
            this.inner().withRequestedServiceObjectiveName(new ServiceObjectiveName(""));
            this.inner().withRequestedServiceObjectiveId(null);
        }

        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.sqlServerName(), this.name(), this.inner())
                .map(new Func1<DatabaseInner, SqlDatabase>() {
            @Override
            public SqlDatabase call(DatabaseInner databaseInner) {
                setInner(databaseInner);
                self.elasticPoolCreatableKey = null;
                return self;
            }
        });
    }

    @Override
    public SqlDatabaseImpl withCollation(String collation) {
        this.inner().withCollation(collation);
        return this;
    }

    @Override
    public SqlDatabaseImpl withEdition(DatabaseEditions edition) {
        this.inner().withEdition(edition);
        return this;
    }

    @Override
    public SqlDatabaseImpl withoutElasticPool() {
        this.inner().withElasticPoolName("");
        return this;
    }

    @Override
    public SqlDatabaseImpl withExistingElasticPool(String elasticPoolName) {
        this.inner().withElasticPoolName(elasticPoolName);
        return this;
    }

    @Override
    public SqlDatabaseImpl withExistingElasticPool(SqlElasticPool sqlElasticPool) {
        return this.withExistingElasticPool(sqlElasticPool.name());
    }

    @Override
    public SqlDatabaseImpl withNewElasticPool(Creatable<SqlElasticPool> sqlElasticPool) {
        if (this.elasticPoolCreatableKey == null) {
            this.elasticPoolCreatableKey = sqlElasticPool.key();
            this.addCreatableDependency(sqlElasticPool);
        }
        return this;
    }

    @Override
    public SqlDatabaseImpl withMaxSizeBytes(long maxSizeBytes) {
        this.inner().withMaxSizeBytes(Long.toString(maxSizeBytes));
        return this;
    }

    @Override
    public SqlDatabaseImpl withServiceObjective(ServiceObjectiveName serviceLevelObjective) {
        this.inner().withRequestedServiceObjectiveName(serviceLevelObjective);
        this.inner().withRequestedServiceObjectiveId(null);
        return this;
    }

    @Override
    public SqlDatabaseImpl withSourceDatabaseId(String sourceDatabaseId) {
        this.inner().withSourceDatabaseId(sourceDatabaseId);
        return this;
    }

    @Override
    public SqlDatabaseImpl withCreateMode(CreateMode createMode) {
        this.inner().withCreateMode(createMode);
        return this;
    }

    @Override
    public SqlDatabaseImpl withoutSourceDatabaseId() {
        return this;
    }
}
