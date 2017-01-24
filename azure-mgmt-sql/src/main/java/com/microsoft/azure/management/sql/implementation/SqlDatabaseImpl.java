/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.ListToMapConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.sql.CreateMode;
import com.microsoft.azure.management.sql.DatabaseEditions;
import com.microsoft.azure.management.sql.DatabaseMetric;
import com.microsoft.azure.management.sql.ReplicationLink;
import com.microsoft.azure.management.sql.RestorePoint;
import com.microsoft.azure.management.sql.ServiceObjectiveName;
import com.microsoft.azure.management.sql.ServiceTierAdvisor;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.sql.SqlWarehouse;
import com.microsoft.azure.management.sql.TransparentDataEncryption;
import com.microsoft.azure.management.sql.UpgradeHintInterface;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation for SqlDatabase and its parent interfaces.
 */
@LangDefinition
class SqlDatabaseImpl
        extends IndependentChildResourceImpl<
                            SqlDatabase,
                            SqlServer,
                            DatabaseInner,
                            SqlDatabaseImpl,
                            SqlServerManager>
        implements SqlDatabase,
            SqlDatabase.Definition,
            SqlDatabase.DefinitionStages.WithCreateWithElasticPoolOptions,
            SqlDatabase.DefinitionStages.WithExistingDatabase,
            SqlDatabase.Update,
        IndependentChild.DefinitionStages.WithParentResource<SqlDatabase, SqlServer> {
    protected final DatabasesInner innerCollection;
    private String elasticPoolCreatableKey;

    protected SqlDatabaseImpl(String name,
                            DatabaseInner innerObject,
                            DatabasesInner innerCollection,
                            SqlServerManager manager) {
        super(name, innerObject, manager);
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
    public UpgradeHintInterface getUpgradeHint() {
        if (this.inner().upgradeHint() == null) {
            this.setInner(this.innerCollection.get(this.resourceGroupName(), this.sqlServerName(), this.name(), "upgradeHint"));
        }
        if (this.inner().upgradeHint() != null) {
            return new UpgradeHintImpl(this.inner().upgradeHint());
        }
        return null;
    }

    @Override
    public boolean isDataWarehouse() {
        return this.edition().toString().equalsIgnoreCase(DatabaseEditions.DATA_WAREHOUSE.toString());
    }

    @Override
    public SqlWarehouse asWarehouse() {
        if (this.isDataWarehouse()) {
            return (SqlWarehouse) this;
        }

        return null;
    }

    @Override
    public List<RestorePoint> listRestorePoints() {
        PagedListConverter<RestorePointInner, RestorePoint> converter = new PagedListConverter<RestorePointInner, RestorePoint>() {
            @Override
            public RestorePoint typeConvert(RestorePointInner restorePointInner) {

                return new RestorePointImpl(restorePointInner);
            }
        };
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
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
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
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
    public Map<String, ServiceTierAdvisor> listServiceTierAdvisors() {
        final SqlDatabaseImpl self = this;
        ListToMapConverter<ServiceTierAdvisor, ServiceTierAdvisorInner> converter = new ListToMapConverter<ServiceTierAdvisor, ServiceTierAdvisorInner>() {
            @Override
            protected String name(ServiceTierAdvisorInner serviceTierAdvisorInner) {
                return serviceTierAdvisorInner.name();
            }

            @Override
            protected ServiceTierAdvisor impl(ServiceTierAdvisorInner serviceTierAdvisorInner) {
                return new ServiceTierAdvisorImpl(serviceTierAdvisorInner,
                        self.innerCollection);
            }
        };
        return converter.convertToUnmodifiableMap(this.innerCollection.listServiceTierAdvisors(
                this.resourceGroupName(),
                this.sqlServerName(),
                this.name()));
    }

    @Override
    public Map<String, ReplicationLink> listReplicationLinks() {
        final SqlDatabaseImpl self = this;

        ListToMapConverter<ReplicationLink, ReplicationLinkInner> converter = new ListToMapConverter<ReplicationLink, ReplicationLinkInner>() {
            @Override
            protected String name(ReplicationLinkInner replicationLinkInner) {
                return replicationLinkInner.name();
            }

            @Override
            protected ReplicationLink impl(ReplicationLinkInner replicationLinkInner) {
                return new ReplicationLinkImpl(replicationLinkInner, self.innerCollection);
            }
        };
        return converter.convertToUnmodifiableMap(this.innerCollection.listReplicationLinks(
                this.resourceGroupName(),
                this.sqlServerName(),
                this.name()));
    }

    @Override
    public void delete() {
        this.innerCollection.delete(this.resourceGroupName(), this.sqlServerName(), this.name());
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
    public SqlDatabaseImpl withMode(CreateMode createMode) {
        this.inner().withCreateMode(createMode);
        return this;
    }

    @Override
    public SqlDatabase.DefinitionStages.WithCreateMode withSourceDatabase(String sourceDatabaseId) {
        this.inner().withSourceDatabaseId(sourceDatabaseId);
        return this;
    }

    @Override
    public SqlDatabase.DefinitionStages.WithCreateMode withSourceDatabase(SqlDatabase sourceDatabase) {
        return withSourceDatabase(sourceDatabase.id());
    }
}
