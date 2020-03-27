/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.management.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.management.resources.fluentcore.dag.TaskGroup;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.sql.AuthenticationType;
import com.azure.management.sql.CreateMode;
import com.azure.management.sql.DatabaseEdition;
import com.azure.management.sql.DatabaseStatus;
import com.azure.management.sql.DatabaseUpdate;
import com.azure.management.sql.ImportRequest;
import com.azure.management.sql.ReplicationLink;
import com.azure.management.sql.RestorePoint;
import com.azure.management.sql.SampleName;
import com.azure.management.sql.ServiceObjectiveName;
import com.azure.management.sql.ServiceTierAdvisor;
import com.azure.management.sql.Sku;
import com.azure.management.sql.SqlDatabase;
import com.azure.management.sql.SqlDatabaseAutomaticTuning;
import com.azure.management.sql.SqlDatabaseBasicStorage;
import com.azure.management.sql.SqlDatabaseMetric;
import com.azure.management.sql.SqlDatabaseMetricDefinition;
import com.azure.management.sql.SqlDatabaseOperations;
import com.azure.management.sql.SqlDatabasePremiumServiceObjective;
import com.azure.management.sql.SqlDatabasePremiumStorage;
import com.azure.management.sql.SqlDatabaseStandardServiceObjective;
import com.azure.management.sql.SqlDatabaseStandardStorage;
import com.azure.management.sql.SqlDatabaseThreatDetectionPolicy;
import com.azure.management.sql.SqlDatabaseUsageMetric;
import com.azure.management.sql.SqlElasticPool;
import com.azure.management.sql.SqlRestorableDroppedDatabase;
import com.azure.management.sql.SqlServer;
import com.azure.management.sql.SqlSyncGroupOperations;
import com.azure.management.sql.SqlWarehouse;
import com.azure.management.sql.StorageKeyType;
import com.azure.management.sql.TransparentDataEncryption;
import com.azure.management.sql.models.DatabaseAutomaticTuningInner;
import com.azure.management.sql.models.DatabaseInner;
import com.azure.management.sql.models.DatabaseSecurityAlertPolicyInner;
import com.azure.management.sql.models.DatabaseUsageInner;
import com.azure.management.sql.models.MetricDefinitionInner;
import com.azure.management.sql.models.MetricInner;
import com.azure.management.sql.models.ReplicationLinkInner;
import com.azure.management.sql.models.RestorePointInner;
import com.azure.management.sql.models.ServiceTierAdvisorInner;
import com.azure.management.sql.models.TransparentDataEncryptionInner;
import com.azure.management.storage.StorageAccount;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation for SqlDatabase and its parent interfaces.
 */
class SqlDatabaseImpl
    extends
        ExternalChildResourceImpl<SqlDatabase, DatabaseInner, SqlServerImpl, SqlServer>
    implements
        SqlDatabase,
        SqlDatabase.SqlDatabaseDefinition<SqlServer.DefinitionStages.WithCreate>,
        SqlDatabase.DefinitionStages.WithExistingDatabaseAfterElasticPool<SqlServer.DefinitionStages.WithCreate>,
        SqlDatabase.DefinitionStages.WithStorageKeyAfterElasticPool<SqlServer.DefinitionStages.WithCreate>,
        SqlDatabase.DefinitionStages.WithAuthenticationAfterElasticPool<SqlServer.DefinitionStages.WithCreate>,
        SqlDatabase.DefinitionStages.WithRestorePointDatabaseAfterElasticPool<SqlServer.DefinitionStages.WithCreate>,
        SqlDatabase.Update,
        SqlDatabaseOperations.DefinitionStages.WithExistingDatabaseAfterElasticPool,
        SqlDatabaseOperations.DefinitionStages.WithStorageKeyAfterElasticPool,
        SqlDatabaseOperations.DefinitionStages.WithAuthenticationAfterElasticPool,
        SqlDatabaseOperations.DefinitionStages.WithRestorePointDatabaseAfterElasticPool,
        SqlDatabaseOperations.DefinitionStages.WithCreateAfterElasticPoolOptions,
        SqlDatabaseOperations.SqlDatabaseOperationsDefinition {

    private SqlElasticPoolsAsExternalChildResourcesImpl sqlElasticPools;

    protected SqlServerManager sqlServerManager;
    protected String resourceGroupName;
    protected String sqlServerName;
    protected String sqlServerLocation;
    private boolean isPatchUpdate;
    private ImportRequest importRequestInner;

    private SqlSyncGroupOperationsImpl syncGroups;


    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name        the name of this external child resource
     * @param parent      reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlDatabaseImpl(String name, SqlServerImpl parent, DatabaseInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject);

        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.name();
        this.sqlServerLocation = parent.regionName();

        this.sqlElasticPools = null;
        this.isPatchUpdate = false;
        this.importRequestInner = null;
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param resourceGroupName the resource group name
     * @param sqlServerName the parent SQL server name
     * @param sqlServerLocation the parent SQL server location
     * @param name        the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlDatabaseImpl(String resourceGroupName, String sqlServerName, String sqlServerLocation, String name, DatabaseInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerLocation = sqlServerLocation;

        this.sqlElasticPools = new SqlElasticPoolsAsExternalChildResourcesImpl(this.sqlServerManager, "SqlElasticPool");
        this.isPatchUpdate = false;
        this.importRequestInner = null;
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param parentSqlElasticPool the parent SqlElasticPool this database belongs to
     * @param name        the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlDatabaseImpl(TaskGroup.HasTaskGroup parentSqlElasticPool, String name, DatabaseInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(parentSqlElasticPool);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;

        this.sqlElasticPools = new SqlElasticPoolsAsExternalChildResourcesImpl(this.sqlServerManager, "SqlElasticPool");
        this.isPatchUpdate = false;
        this.importRequestInner = null;
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String collation() {
        return this.inner().collation();
    }

    @Override
    public OffsetDateTime creationDate() {
        return this.inner().creationDate();
    }

    @Override
    public String currentServiceObjectiveName() {
        return this.inner().currentServiceObjectiveName();
    }

    @Override
    public String databaseId() {
        return this.inner().databaseId().toString();
    }

    @Override
    public OffsetDateTime earliestRestoreDate() {
        return this.inner().earliestRestoreDate();
    }

    @Override
    public DatabaseEdition edition() {
        return DatabaseEdition.fromString(this.inner().sku().tier());
    }

    @Override
    public long maxSizeBytes() {
        return Long.valueOf(this.inner().maxSizeBytes());
    }

    @Override
    public String requestedServiceObjectiveName() {
        return this.inner().requestedServiceObjectiveName();
    }

    @Override
    public DatabaseStatus status() {
        return this.inner().status();
    }

    @Override
    public String elasticPoolId() {
        return this.inner().elasticPoolId();
    }


    @Override
    public String elasticPoolName() {
        return ResourceUtils.nameFromResourceId(this.inner().elasticPoolId());
    }

    @Override
    public String defaultSecondaryLocation() {
        return this.inner().defaultSecondaryLocation();
    }

    @Override
    public boolean isDataWarehouse() {
        return this.edition().toString().equalsIgnoreCase(DatabaseEdition.DATA_WAREHOUSE.toString());
    }

    @Override
    public SqlWarehouse asWarehouse() {
        if (this.isDataWarehouse()) {
            if (this.parent() != null) {
                return new SqlWarehouseImpl(this.name(), this.parent(), this.inner(), this.sqlServerManager);
            } else {
                return new SqlWarehouseImpl(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation, this.name(), this.inner(), this.sqlServerManager);
            }
        }

        return null;
    }

    @Override
    public List<RestorePoint> listRestorePoints() {
        List<RestorePoint> restorePoints = new ArrayList<>();
        PagedIterable<RestorePointInner> restorePointInners = this.sqlServerManager.inner()
            .restorePoints().listByDatabase(this.resourceGroupName, this.sqlServerName, this.name());
        if (restorePointInners != null) {
            for (RestorePointInner inner : restorePointInners) {
                restorePoints.add(new RestorePointImpl(this.resourceGroupName, this.sqlServerName, inner));
            }
        }
        return Collections.unmodifiableList(restorePoints);
    }

    @Override
    public PagedFlux<RestorePoint> listRestorePointsAsync() {
        final SqlDatabaseImpl self = this;
        return this.sqlServerManager.inner()
            .restorePoints().listByDatabaseAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(restorePointInner -> new RestorePointImpl(self.resourceGroupName, self.sqlServerName, restorePointInner));
    }

    @Override
    public Map<String, ReplicationLink> listReplicationLinks() {
        Map<String, ReplicationLink> replicationLinkMap = new HashMap<>();
        PagedIterable<ReplicationLinkInner> replicationLinkInners = this.sqlServerManager.inner()
            .replicationLinks().listByDatabase(this.resourceGroupName, this.sqlServerName, this.name());
        if (replicationLinkInners != null) {
            for (ReplicationLinkInner inner : replicationLinkInners) {
                replicationLinkMap.put(inner.getName(), new ReplicationLinkImpl(this.resourceGroupName, this.sqlServerName, inner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableMap(replicationLinkMap);
    }

    @Override
    public PagedFlux<ReplicationLink> listReplicationLinksAsync() {
        final SqlDatabaseImpl self = this;
        return this.sqlServerManager.inner()
            .replicationLinks().listByDatabaseAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(replicationLinkInner -> new ReplicationLinkImpl(self.resourceGroupName, self.sqlServerName, replicationLinkInner, self.sqlServerManager));
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(String storageUri) {
        return new SqlDatabaseExportRequestImpl(this, this.sqlServerManager)
            .exportTo(storageUri);
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(StorageAccount storageAccount, String containerName, String fileName) {
        Objects.requireNonNull(storageAccount);
        return new SqlDatabaseExportRequestImpl(this, this.sqlServerManager)
            .exportTo(storageAccount, containerName, fileName);
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(Creatable<StorageAccount> storageAccountCreatable, String containerName, String fileName) {
        Objects.requireNonNull(storageAccountCreatable);
        return new SqlDatabaseExportRequestImpl(this, this.sqlServerManager)
            .exportTo(storageAccountCreatable, containerName, fileName);
    }

    @Override
    public SqlDatabaseImportRequestImpl importBacpac(String storageUri) {
        return new SqlDatabaseImportRequestImpl(this, this.sqlServerManager)
            .importFrom(storageUri);
    }

    @Override
    public SqlDatabaseImportRequestImpl importBacpac(StorageAccount storageAccount, String containerName, String fileName) {
        Objects.requireNonNull(storageAccount);
        return new SqlDatabaseImportRequestImpl(this, this.sqlServerManager)
            .importFrom(storageAccount, containerName, fileName);
    }

    @Override
    public SqlDatabaseThreatDetectionPolicy.DefinitionStages.Blank defineThreatDetectionPolicy(String policyName) {
        return new SqlDatabaseThreatDetectionPolicyImpl(policyName, this, new DatabaseSecurityAlertPolicyInner(), this.sqlServerManager);
    }

    @Override
    public SqlDatabaseThreatDetectionPolicy getThreatDetectionPolicy() {
        DatabaseSecurityAlertPolicyInner policyInner = this.sqlServerManager.inner().databaseThreatDetectionPolicies()
            .get(this.resourceGroupName, this.sqlServerName, this.name());
        return policyInner != null ? new SqlDatabaseThreatDetectionPolicyImpl(policyInner.getName(), this, policyInner, this.sqlServerManager) : null;
    }

    @Override
    public SqlDatabaseAutomaticTuning getDatabaseAutomaticTuning() {
        DatabaseAutomaticTuningInner databaseAutomaticTuningInner = this.sqlServerManager.inner().databaseAutomaticTunings()
            .get(this.resourceGroupName, this.sqlServerName, this.name());
        return databaseAutomaticTuningInner != null ? new SqlDatabaseAutomaticTuningImpl(this, databaseAutomaticTuningInner) : null;
    }

    @Override
    public List<SqlDatabaseUsageMetric> listUsageMetrics() {
        List<SqlDatabaseUsageMetric> databaseUsageMetrics = new ArrayList<>();
        PagedIterable<DatabaseUsageInner> databaseUsageInners = this.sqlServerManager.inner().databaseUsages()
            .listByDatabase(this.resourceGroupName, this.sqlServerName, this.name());
        if (databaseUsageInners != null) {
            for (DatabaseUsageInner inner : databaseUsageInners) {
                databaseUsageMetrics.add(new SqlDatabaseUsageMetricImpl(inner));
            }
        }
        return Collections.unmodifiableList(databaseUsageMetrics);
    }

    @Override
    public PagedFlux<SqlDatabaseUsageMetric> listUsageMetricsAsync() {
        return this.sqlServerManager.inner().databaseUsages()
            .listByDatabaseAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(databaseUsageInner -> new SqlDatabaseUsageMetricImpl(databaseUsageInner));
    }

    @Override
    public SqlDatabase rename(String newDatabaseName) {
        ResourceId resourceId = ResourceId.fromString(this.id());
        String newId = resourceId.parent().id() + "/databases/" + newDatabaseName;
        this.sqlServerManager.inner().databases()
            .rename(this.resourceGroupName, this.sqlServerName, this.name(), newId);
        return this.sqlServerManager.sqlServers().databases()
            .getBySqlServer(this.resourceGroupName, this.sqlServerName, newDatabaseName);
    }

    @Override
    public Mono<SqlDatabase> renameAsync(final String newDatabaseName) {
        final SqlDatabaseImpl self = this;
        ResourceId resourceId = ResourceId.fromString(this.id());
        String newId = resourceId.parent().id() + "/databases/" + newDatabaseName;
        return this.sqlServerManager.inner().databases()
            .renameAsync(this.resourceGroupName, this.sqlServerName, self.name(), newId)
            .flatMap(aVoid -> self.sqlServerManager.sqlServers().databases()
                .getBySqlServerAsync(self.resourceGroupName, self.sqlServerName, newDatabaseName));
    }

    @Override
    public List<SqlDatabaseMetric> listMetrics(String filter) {
        List<SqlDatabaseMetric> sqlDatabaseMetrics = new ArrayList<>();
        PagedIterable<MetricInner> metricInners = this.sqlServerManager.inner().databases()
            .listMetrics(this.resourceGroupName, this.sqlServerName, this.name(), filter);
        if (metricInners != null) {
            for (MetricInner metricInner : metricInners) {
                sqlDatabaseMetrics.add(new SqlDatabaseMetricImpl(metricInner));
            }
        }
        return Collections.unmodifiableList(sqlDatabaseMetrics);
    }

    @Override
    public PagedFlux<SqlDatabaseMetric> listMetricsAsync(final String filter) {
        return this.sqlServerManager.inner().databases()
            .listMetricsAsync(this.resourceGroupName, this.sqlServerName, this.name(), filter)
            .mapPage(metricInner -> new SqlDatabaseMetricImpl(metricInner));
    }

    @Override
    public List<SqlDatabaseMetricDefinition> listMetricDefinitions() {
        List<SqlDatabaseMetricDefinition> sqlDatabaseMetricDefinitions = new ArrayList<>();
        PagedIterable<MetricDefinitionInner> metricDefinitionInners = this.sqlServerManager.inner().databases()
            .listMetricDefinitions(this.resourceGroupName, this.sqlServerName, this.name());
        if (metricDefinitionInners != null) {
            for (MetricDefinitionInner metricDefinitionInner : metricDefinitionInners) {
                sqlDatabaseMetricDefinitions.add(new SqlDatabaseMetricDefinitionImpl(metricDefinitionInner));
            }
        }

        return Collections.unmodifiableList(sqlDatabaseMetricDefinitions);
    }

    @Override
    public PagedFlux<SqlDatabaseMetricDefinition> listMetricDefinitionsAsync() {
        return this.sqlServerManager.inner().databases()
            .listMetricDefinitionsAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(metricDefinitionInner -> new SqlDatabaseMetricDefinitionImpl(metricDefinitionInner));
    }

    @Override
    public TransparentDataEncryption getTransparentDataEncryption() {
        TransparentDataEncryptionInner transparentDataEncryptionInner = this.sqlServerManager.inner()
            .transparentDataEncryptions().get(this.resourceGroupName, this.sqlServerName, this.name());
        return (transparentDataEncryptionInner == null) ? null : new TransparentDataEncryptionImpl(this.resourceGroupName, this.sqlServerName, transparentDataEncryptionInner, this.sqlServerManager);
    }

    @Override
    public Mono<TransparentDataEncryption> getTransparentDataEncryptionAsync() {
        final SqlDatabaseImpl self = this;
        return this.sqlServerManager.inner()
            .transparentDataEncryptions().getAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .map(transparentDataEncryptionInner -> new TransparentDataEncryptionImpl(self.resourceGroupName, self.sqlServerName, transparentDataEncryptionInner, self.sqlServerManager));
    }

    @Override
    public Map<String, ServiceTierAdvisor> listServiceTierAdvisors() {
        Map<String, ServiceTierAdvisor> serviceTierAdvisorMap = new HashMap<>();
        PagedIterable<ServiceTierAdvisorInner> serviceTierAdvisorInners = this.sqlServerManager.inner()
            .serviceTierAdvisors().listByDatabase(this.resourceGroupName, this.sqlServerName, this.name());
        if (serviceTierAdvisorInners != null) {
            for (ServiceTierAdvisorInner serviceTierAdvisorInner : serviceTierAdvisorInners) {
                serviceTierAdvisorMap.put(serviceTierAdvisorInner.getName(),
                    new ServiceTierAdvisorImpl(this.resourceGroupName, this.sqlServerName, serviceTierAdvisorInner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableMap(serviceTierAdvisorMap);
    }

    @Override
    public PagedFlux<ServiceTierAdvisor> listServiceTierAdvisorsAsync() {
        final SqlDatabaseImpl self = this;
        return this.sqlServerManager.inner()
            .serviceTierAdvisors().listByDatabaseAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(serviceTierAdvisorInner -> new ServiceTierAdvisorImpl(self.resourceGroupName, self.sqlServerName, serviceTierAdvisorInner, self.sqlServerManager));
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.id());
    }

    @Override
    public String regionName() {
        return this.inner().getLocation();
    }

    @Override
    public Region region() {
        return Region.fromName(this.regionName());
    }

    @Override
    public SqlSyncGroupOperations.SqlSyncGroupActionsDefinition syncGroups() {
        if (this.syncGroups == null) {
            this.syncGroups = new SqlSyncGroupOperationsImpl(this, this.sqlServerManager);
        }

        return this.syncGroups;
    }


    SqlDatabaseImpl withPatchUpdate() {
        this.isPatchUpdate = true;
        return this;
    }

    @Override
    protected Mono<DatabaseInner> getInnerAsync() {
        return this.sqlServerManager.inner().databases().getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    void addParentDependency(TaskGroup.HasTaskGroup parentDependency) {
        this.addDependency(parentDependency);
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        if (this.importRequestInner != null && this.elasticPoolId() != null) {
            final SqlDatabaseImpl self = this;
            final String epId = this.elasticPoolId();
            this.addPostRunDependent(context -> {
                self.importRequestInner = null;
                self.withExistingElasticPoolId(epId);
                return self.createResourceAsync()
                    .flatMap(sqlDatabase -> context.voidMono());
            });
        }
    }

    @Override
    public Mono<SqlDatabase> createResourceAsync() {
        final SqlDatabaseImpl self = this;
        this.inner().setLocation(this.sqlServerLocation);
        if (this.importRequestInner != null) {
            this.importRequestInner.withDatabaseName(this.name());
            if (this.importRequestInner.edition() == null) {
                this.importRequestInner.withEdition(this.edition());
            }
            if (this.importRequestInner.serviceObjectiveName() == null && this.inner().sku() != null) {
                this.importRequestInner.withServiceObjectiveName(ServiceObjectiveName.fromString(this.requestedServiceObjectiveName()));
            }
            if (this.importRequestInner.maxSizeBytes() == null) {
                this.importRequestInner.withMaxSizeBytes(String.valueOf(this.inner().maxSizeBytes()));
            }

            return this.sqlServerManager.inner().databases()
                .importMethodAsync(this.resourceGroupName, this.sqlServerName, this.importRequestInner)
                .flatMap(importExportResponseInner -> {
                    if (self.elasticPoolId() != null) {
                        self.importRequestInner = null;
                        return self.withExistingElasticPoolId(self.elasticPoolId()).withPatchUpdate().updateResourceAsync();
                    } else {
                        return self.refreshAsync();
                    }
                });
        } else {
            return this.sqlServerManager.inner().databases()
                .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.inner())
                .map(inner -> {
                    self.setInner(inner);
                    return self;
                });
        }
    }

    @Override
    public Mono<SqlDatabase> updateResourceAsync() {
        if (this.isPatchUpdate) {
            final SqlDatabaseImpl self = this;
            DatabaseUpdate databaseUpdateInner = new DatabaseUpdate()
                .withTags(self.inner().getTags())
                .withCollation(self.inner().collation())
                .withSourceDatabaseId(self.inner().sourceDatabaseId())
                .withCreateMode(self.inner().createMode())
                .withSku(self.inner().sku())
                .withMaxSizeBytes(this.inner().maxSizeBytes())
                .withElasticPoolId(this.inner().elasticPoolId());
            return this.sqlServerManager.inner().databases()
                .updateAsync(this.resourceGroupName, this.sqlServerName, this.name(), databaseUpdateInner)
                .map(inner -> {
                    self.setInner(inner);
                    self.isPatchUpdate = false;
                    return self;
                });

        } else {
            return this.createResourceAsync();
        }
    }

    @Override
    public SqlDatabaseImpl update() {
        super.prepareUpdate();
        return this;
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        if (this.sqlElasticPools != null) {
            this.sqlElasticPools.clear();
        }
        this.importRequestInner = null;

        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this.sqlServerManager.inner().databases().deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public void delete() {
        this.sqlServerManager.inner().databases().delete(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.deleteResourceAsync();
    }

    @Override
    public SqlDatabaseImpl withExistingSqlServer(String resourceGroupName, String sqlServerName, String sqlServerLocation) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerLocation = sqlServerLocation;

        return this;
    }

    @Override
    public SqlDatabaseImpl withExistingSqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        this.resourceGroupName = sqlServer.resourceGroupName();
        this.sqlServerName = sqlServer.name();
        this.sqlServerLocation = sqlServer.regionName();

        return this;
    }

    @Override
    public SqlServerImpl attach() {
        return this.parent();
    }

    @Override
    public SqlDatabaseImpl withoutElasticPool() {
        this.inner().withElasticPoolId(null);

        return this;
    }

    private String generateElasticPoolIdFromName(String elasticPoolName) {
        if (this.parentId() == null) {
            return ResourceUtils.constructResourceId(this.sqlServerManager.getSubscriptionId(),
                    this.resourceGroupName,
                    "Microsoft.Sql",
                    "elasticPools",
                    elasticPoolName,
                    String.format("servers/%s", this.sqlServerName));
        }
        return String.format("%s/elasticPools/%s", this.parentId(), elasticPoolName);
    }


    @Override
    public SqlDatabaseImpl withExistingElasticPool(String elasticPoolName) {
        this.inner().withSku(null);
        this.inner().withElasticPoolId(generateElasticPoolIdFromName(elasticPoolName));

        return this;
    }

    @Override
    public SqlDatabaseImpl withExistingElasticPoolId(String elasticPoolId) {
        this.inner().withSku(null);
        this.inner().withElasticPoolId(elasticPoolId);

        return this;
    }

    @Override
    public SqlDatabaseImpl withExistingElasticPool(SqlElasticPool sqlElasticPool) {
        Objects.requireNonNull(sqlElasticPool);
        this.inner().withSku(null);
        this.inner().withElasticPoolId(sqlElasticPool.id());

        return this;
    }

    @Override
    public SqlDatabaseImpl withNewElasticPool(final Creatable<SqlElasticPool> sqlElasticPool) {
        Objects.requireNonNull(sqlElasticPool);
        this.inner().withSku(null);
        this.inner().withElasticPoolId(generateElasticPoolIdFromName(sqlElasticPool.name()));
        this.addDependency(sqlElasticPool);

        return this;
    }

    @Override
    public SqlElasticPoolForDatabaseImpl defineElasticPool(String elasticPoolName) {
        if (this.sqlElasticPools == null) {
            this.sqlElasticPools = new SqlElasticPoolsAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlElasticPool");
        }
        this.inner().withSku(null);
        this.inner().withElasticPoolId(generateElasticPoolIdFromName(elasticPoolName));

        return new SqlElasticPoolForDatabaseImpl(this, this.sqlElasticPools
            .defineIndependentElasticPool(elasticPoolName).withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation));
    }

    @Override
    public SqlDatabaseImpl fromRestorableDroppedDatabase(SqlRestorableDroppedDatabase restorableDroppedDatabase) {
        Objects.requireNonNull(restorableDroppedDatabase);
        this.inner().withRestorableDroppedDatabaseId(restorableDroppedDatabase.id())
                .withCreateMode(CreateMode.RESTORE);
        return this;
    }

    private void initializeImportRequestInner() {
        this.importRequestInner = new ImportRequest();
        if (this.elasticPoolId() != null) {
            this.importRequestInner.withEdition(DatabaseEdition.BASIC);
            this.importRequestInner.withServiceObjectiveName(ServiceObjectiveName.BASIC);
            this.importRequestInner.withMaxSizeBytes(Long.toString(SqlDatabaseBasicStorage.MAX_2_GB.capacity()));
        } else {
            this.withStandardEdition(SqlDatabaseStandardServiceObjective.S0);
        }
    }

    @Override
    public SqlDatabaseImpl importFrom(String storageUri) {
        this.initializeImportRequestInner();
        this.importRequestInner.withStorageUri(storageUri);
        return this;
    }

    @Override
    public SqlDatabaseImpl importFrom(final StorageAccount storageAccount, final String containerName, final String fileName) {
        final SqlDatabaseImpl self = this;
        Objects.requireNonNull(storageAccount);
        this.initializeImportRequestInner();
        this.addDependency(new FunctionalTaskItem() {
            @Override
            public Mono<Indexable> apply(final Context context) {
                return storageAccount.getKeysAsync()
                    .flatMap(storageAccountKeys -> Mono.justOrEmpty(storageAccountKeys.stream().findFirst()))
                    .flatMap(storageAccountKey -> {
                        self.importRequestInner.withStorageUri(String.format("%s%s/%s", storageAccount.endPoints().primary().getBlob(), containerName, fileName));
                        self.importRequestInner.withStorageKeyType(StorageKeyType.STORAGE_ACCESS_KEY);
                        self.importRequestInner.withStorageKey(storageAccountKey.getValue());
                        return context.voidMono();
                    });
            }
        });
        return this;
    }

    @Override
    public SqlDatabaseImpl withStorageAccessKey(String storageAccessKey) {
        this.importRequestInner.withStorageKeyType(StorageKeyType.STORAGE_ACCESS_KEY);
        this.importRequestInner.withStorageKey(storageAccessKey);
        return this;
    }

    @Override
    public SqlDatabaseImpl withSharedAccessKey(String sharedAccessKey) {
        this.importRequestInner.withStorageKeyType(StorageKeyType.SHARED_ACCESS_KEY);
        this.importRequestInner.withStorageKey(sharedAccessKey);
        return this;
    }

    @Override
    public SqlDatabaseImpl withSqlAdministratorLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.importRequestInner.withAuthenticationType(AuthenticationType.SQL);
        this.importRequestInner.withAdministratorLogin(administratorLogin);
        this.importRequestInner.withAdministratorLoginPassword(administratorPassword);
        return this;
    }

    @Override
    public SqlDatabaseImpl withActiveDirectoryLoginAndPassword(String administratorLogin, String administratorPassword) {
        this.importRequestInner.withAuthenticationType(AuthenticationType.ADPASSWORD);
        this.importRequestInner.withAdministratorLogin(administratorLogin);
        this.importRequestInner.withAdministratorLoginPassword(administratorPassword);
        return this;
    }

    @Override
    public SqlDatabaseImpl fromRestorePoint(RestorePoint restorePoint) {
        return fromRestorePoint(restorePoint, restorePoint.earliestRestoreDate());
    }

    @Override
    public SqlDatabaseImpl fromRestorePoint(RestorePoint restorePoint, OffsetDateTime restorePointDateTime) {
        Objects.requireNonNull(restorePoint);
        this.inner().withRestorePointInTime(restorePointDateTime);
        return this.withSourceDatabase(restorePoint.databaseId())
            .withMode(CreateMode.POINT_IN_TIME_RESTORE);
    }

    @Override
    public SqlDatabaseImpl withSourceDatabase(String sourceDatabaseId) {
        this.inner().withSourceDatabaseId(sourceDatabaseId);

        return this;
    }

    @Override
    public SqlDatabaseImpl withSourceDatabase(SqlDatabase sourceDatabase) {
        return this.withSourceDatabase(sourceDatabase.id());
    }

    @Override
    public SqlDatabaseImpl withMode(CreateMode createMode) {
        this.inner().withCreateMode(createMode);

        return this;
    }

    @Override
    public SqlDatabaseImpl withCollation(String collation) {
        this.inner().withCollation(collation);

        return this;
    }

    @Override
    public SqlDatabaseImpl withMaxSizeBytes(long maxSizeBytes) {
        this.inner().withMaxSizeBytes(maxSizeBytes);

        return this;
    }

    @Override
    public SqlDatabaseImpl withEdition(DatabaseEdition edition) {
        if (this.inner().sku() == null) {
            this.inner().withSku(new Sku());
        }
        this.inner().sku().withTier(edition.toString());
        if (this.inner().sku().name() == null) {
            this.inner().sku().withName(edition.toString());
        }
        this.inner().sku().withCapacity(null);
        this.inner().withElasticPoolId(null);

        return this;
    }

    @Override
    public SqlDatabaseImpl withBasicEdition() {
        return this.withBasicEdition(SqlDatabaseBasicStorage.MAX_2_GB);
    }

    @Override
    public SqlDatabaseImpl withBasicEdition(SqlDatabaseBasicStorage maxStorageCapacity) {
        Sku sku = new Sku().withName(ServiceObjectiveName.BASIC.toString())
                .withTier(DatabaseEdition.BASIC.toString());

        this.inner().withSku(sku);
        this.inner().withMaxSizeBytes(maxStorageCapacity.capacity());
        this.inner().withElasticPoolId(null);
        return this;
    }

    @Override
    public SqlDatabaseImpl withStandardEdition(SqlDatabaseStandardServiceObjective serviceObjective) {
        return this.withStandardEdition(serviceObjective, SqlDatabaseStandardStorage.MAX_250_GB);
    }

    @Override
    public SqlDatabaseImpl withStandardEdition(SqlDatabaseStandardServiceObjective serviceObjective, SqlDatabaseStandardStorage maxStorageCapacity) {
        Sku sku = new Sku().withName(serviceObjective.toString())
                .withTier(DatabaseEdition.STANDARD.toString());

        this.inner().withSku(sku);
        this.inner().withMaxSizeBytes(maxStorageCapacity.capacity());
        this.inner().withElasticPoolId(null);
        return this;
    }

    @Override
    public SqlDatabaseImpl withPremiumEdition(SqlDatabasePremiumServiceObjective serviceObjective) {
        return this.withPremiumEdition(serviceObjective, SqlDatabasePremiumStorage.MAX_500_GB);
    }

    @Override
    public SqlDatabaseImpl withPremiumEdition(SqlDatabasePremiumServiceObjective serviceObjective, SqlDatabasePremiumStorage maxStorageCapacity) {
        Sku sku = new Sku().withName(serviceObjective.toString())
                .withTier(DatabaseEdition.PREMIUM.toString());

        this.inner().withSku(sku);
        this.inner().withMaxSizeBytes(maxStorageCapacity.capacity());
        this.inner().withElasticPoolId(null);
        return this;
    }

    @Override
    public SqlDatabaseImpl withCustomEdition(Sku sku) {
        this.inner().withSku(sku);
        this.inner().withElasticPoolId(null);
        return this;
    }

    @Override
    public SqlDatabaseImpl withCustomEdition(DatabaseEdition edition, ServiceObjectiveName serviceObjective, int capacity) {
        Sku sku = new Sku().withName(serviceObjective.toString())
                .withTier(edition.toString())
                .withCapacity(capacity <= 0 ? null : Integer.valueOf(capacity));

        return this.withCustomEdition(sku);
    }

    @Override
    public SqlDatabaseImpl withServiceObjective(ServiceObjectiveName serviceLevelObjective) {
        if (this.inner().sku() == null) {
            this.inner().withSku(new Sku());
        }
        this.inner().sku().withName(serviceLevelObjective.toString());
        this.inner().sku().withCapacity(null);
        return this;
    }

    @Override
    public SqlDatabaseImpl withTags(Map<String, String> tags) {
        this.inner().setTags(new HashMap<>(tags));
        return this;
    }

    @Override
    public SqlDatabaseImpl withTag(String key, String value) {
        if (this.inner().getTags() == null) {
            this.inner().setTags(new HashMap<String, String>());
        }
        this.inner().getTags().put(key, value);
        return this;
    }

    @Override
    public SqlDatabaseImpl withoutTag(String key) {
        if (this.inner().getTags() != null) {
            this.inner().getTags().remove(key);
        }
        return this;
    }

    @Override
    public SqlDatabaseImpl fromSample(SampleName sampleName) {
        this.inner().withSampleName(sampleName);
        return this;
    }
}
