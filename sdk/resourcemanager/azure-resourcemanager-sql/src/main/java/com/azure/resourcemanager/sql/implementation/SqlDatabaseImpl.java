// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.AuthenticationType;
import com.azure.resourcemanager.sql.models.CreateMode;
import com.azure.resourcemanager.sql.models.DatabaseEdition;
import com.azure.resourcemanager.sql.models.DatabaseStatus;
import com.azure.resourcemanager.sql.models.DatabaseUpdate;
import com.azure.resourcemanager.sql.models.ImportRequest;
import com.azure.resourcemanager.sql.models.ReplicationLink;
import com.azure.resourcemanager.sql.models.RestorePoint;
import com.azure.resourcemanager.sql.models.SampleName;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyName;
import com.azure.resourcemanager.sql.models.ServiceObjectiveName;
import com.azure.resourcemanager.sql.models.ServiceTierAdvisor;
import com.azure.resourcemanager.sql.models.Sku;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseAutomaticTuning;
import com.azure.resourcemanager.sql.models.SqlDatabaseBasicStorage;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetric;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetricDefinition;
import com.azure.resourcemanager.sql.models.SqlDatabaseOperations;
import com.azure.resourcemanager.sql.models.SqlDatabasePremiumServiceObjective;
import com.azure.resourcemanager.sql.models.SqlDatabasePremiumStorage;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardServiceObjective;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardStorage;
import com.azure.resourcemanager.sql.models.SqlDatabaseThreatDetectionPolicy;
import com.azure.resourcemanager.sql.models.SqlDatabaseUsageMetric;
import com.azure.resourcemanager.sql.models.SqlElasticPool;
import com.azure.resourcemanager.sql.models.SqlRestorableDroppedDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlSyncGroupOperations;
import com.azure.resourcemanager.sql.models.SqlWarehouse;
import com.azure.resourcemanager.sql.models.StorageKeyType;
import com.azure.resourcemanager.sql.models.TransparentDataEncryption;
import com.azure.resourcemanager.sql.fluent.inner.DatabaseAutomaticTuningInner;
import com.azure.resourcemanager.sql.fluent.inner.DatabaseInner;
import com.azure.resourcemanager.sql.fluent.inner.DatabaseSecurityAlertPolicyInner;
import com.azure.resourcemanager.sql.fluent.inner.DatabaseUsageInner;
import com.azure.resourcemanager.sql.fluent.inner.MetricDefinitionInner;
import com.azure.resourcemanager.sql.fluent.inner.MetricInner;
import com.azure.resourcemanager.sql.fluent.inner.ReplicationLinkInner;
import com.azure.resourcemanager.sql.fluent.inner.RestorePointInner;
import com.azure.resourcemanager.sql.fluent.inner.ServiceTierAdvisorInner;
import com.azure.resourcemanager.sql.fluent.inner.TransparentDataEncryptionInner;
import com.azure.resourcemanager.sql.models.TransparentDataEncryptionName;
import com.azure.resourcemanager.storage.models.StorageAccount;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Implementation for SqlDatabase and its parent interfaces. */
class SqlDatabaseImpl extends ExternalChildResourceImpl<SqlDatabase, DatabaseInner, SqlServerImpl, SqlServer>
    implements SqlDatabase,
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
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
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
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlDatabaseImpl(
        String resourceGroupName,
        String sqlServerName,
        String sqlServerLocation,
        String name,
        DatabaseInner innerObject,
        SqlServerManager sqlServerManager) {
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
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlDatabaseImpl(
        TaskGroup.HasTaskGroup parentSqlElasticPool,
        String name,
        DatabaseInner innerObject,
        SqlServerManager sqlServerManager) {
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
        return this.inner().id();
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
        return this.inner().maxSizeBytes();
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
                return new SqlWarehouseImpl(
                    this.resourceGroupName,
                    this.sqlServerName,
                    this.sqlServerLocation,
                    this.name(),
                    this.inner(),
                    this.sqlServerManager);
            }
        }

        return null;
    }

    @Override
    public List<RestorePoint> listRestorePoints() {
        List<RestorePoint> restorePoints = new ArrayList<>();
        PagedIterable<RestorePointInner> restorePointInners =
            this
                .sqlServerManager
                .inner()
                .getRestorePoints()
                .listByDatabase(this.resourceGroupName, this.sqlServerName, this.name());
        for (RestorePointInner inner : restorePointInners) {
            restorePoints.add(new RestorePointImpl(this.resourceGroupName, this.sqlServerName, inner));
        }
        return Collections.unmodifiableList(restorePoints);
    }

    @Override
    public PagedFlux<RestorePoint> listRestorePointsAsync() {
        final SqlDatabaseImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getRestorePoints()
            .listByDatabaseAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(
                restorePointInner ->
                    new RestorePointImpl(self.resourceGroupName, self.sqlServerName, restorePointInner));
    }

    @Override
    public Map<String, ReplicationLink> listReplicationLinks() {
        Map<String, ReplicationLink> replicationLinkMap = new HashMap<>();
        PagedIterable<ReplicationLinkInner> replicationLinkInners =
            this
                .sqlServerManager
                .inner()
                .getReplicationLinks()
                .listByDatabase(this.resourceGroupName, this.sqlServerName, this.name());
        for (ReplicationLinkInner inner : replicationLinkInners) {
            replicationLinkMap
                .put(
                    inner.name(),
                    new ReplicationLinkImpl(
                        this.resourceGroupName, this.sqlServerName, inner, this.sqlServerManager));
        }
        return Collections.unmodifiableMap(replicationLinkMap);
    }

    @Override
    public PagedFlux<ReplicationLink> listReplicationLinksAsync() {
        final SqlDatabaseImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getReplicationLinks()
            .listByDatabaseAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(
                replicationLinkInner ->
                    new ReplicationLinkImpl(
                        self.resourceGroupName, self.sqlServerName, replicationLinkInner, self.sqlServerManager));
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(String storageUri) {
        return new SqlDatabaseExportRequestImpl(this, this.sqlServerManager).exportTo(storageUri);
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(StorageAccount storageAccount, String containerName, String fileName) {
        Objects.requireNonNull(storageAccount);
        return new SqlDatabaseExportRequestImpl(this, this.sqlServerManager)
            .exportTo(storageAccount, containerName, fileName);
    }

    @Override
    public SqlDatabaseExportRequestImpl exportTo(
        Creatable<StorageAccount> storageAccountCreatable, String containerName, String fileName) {
        Objects.requireNonNull(storageAccountCreatable);
        return new SqlDatabaseExportRequestImpl(this, this.sqlServerManager)
            .exportTo(storageAccountCreatable, containerName, fileName);
    }

    @Override
    public SqlDatabaseImportRequestImpl importBacpac(String storageUri) {
        return new SqlDatabaseImportRequestImpl(this, this.sqlServerManager).importFrom(storageUri);
    }

    @Override
    public SqlDatabaseImportRequestImpl importBacpac(
        StorageAccount storageAccount, String containerName, String fileName) {
        Objects.requireNonNull(storageAccount);
        return new SqlDatabaseImportRequestImpl(this, this.sqlServerManager)
            .importFrom(storageAccount, containerName, fileName);
    }

    @Override
    public SqlDatabaseThreatDetectionPolicy.DefinitionStages.Blank defineThreatDetectionPolicy(String policyName) {
        return new SqlDatabaseThreatDetectionPolicyImpl(
            policyName, this, new DatabaseSecurityAlertPolicyInner(), this.sqlServerManager);
    }

    @Override
    public SqlDatabaseThreatDetectionPolicy getThreatDetectionPolicy() {
        DatabaseSecurityAlertPolicyInner policyInner =
            this
                .sqlServerManager
                .inner()
                .getDatabaseThreatDetectionPolicies()
                .get(this.resourceGroupName, this.sqlServerName, this.name(), SecurityAlertPolicyName.DEFAULT);
        return policyInner != null
            ? new SqlDatabaseThreatDetectionPolicyImpl(policyInner.name(), this, policyInner, this.sqlServerManager)
            : null;
    }

    @Override
    public SqlDatabaseAutomaticTuning getDatabaseAutomaticTuning() {
        DatabaseAutomaticTuningInner databaseAutomaticTuningInner =
            this
                .sqlServerManager
                .inner()
                .getDatabaseAutomaticTunings()
                .get(this.resourceGroupName, this.sqlServerName, this.name());
        return databaseAutomaticTuningInner != null
            ? new SqlDatabaseAutomaticTuningImpl(this, databaseAutomaticTuningInner)
            : null;
    }

    @Override
    public List<SqlDatabaseUsageMetric> listUsageMetrics() {
        List<SqlDatabaseUsageMetric> databaseUsageMetrics = new ArrayList<>();
        PagedIterable<DatabaseUsageInner> databaseUsageInners =
            this
                .sqlServerManager
                .inner()
                .getDatabaseUsages()
                .listByDatabase(this.resourceGroupName, this.sqlServerName, this.name());
        for (DatabaseUsageInner inner : databaseUsageInners) {
            databaseUsageMetrics.add(new SqlDatabaseUsageMetricImpl(inner));
        }
        return Collections.unmodifiableList(databaseUsageMetrics);
    }

    @Override
    public PagedFlux<SqlDatabaseUsageMetric> listUsageMetricsAsync() {
        return this
            .sqlServerManager
            .inner()
            .getDatabaseUsages()
            .listByDatabaseAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(SqlDatabaseUsageMetricImpl::new);
    }

    @Override
    public SqlDatabase rename(String newDatabaseName) {
        ResourceId resourceId = ResourceId.fromString(this.id());
        String newId = resourceId.parent().id() + "/databases/" + newDatabaseName;
        this
            .sqlServerManager
            .inner()
            .getDatabases()
            .rename(this.resourceGroupName, this.sqlServerName, this.name(), newId);
        return this
            .sqlServerManager
            .sqlServers()
            .databases()
            .getBySqlServer(this.resourceGroupName, this.sqlServerName, newDatabaseName);
    }

    @Override
    public Mono<SqlDatabase> renameAsync(final String newDatabaseName) {
        final SqlDatabaseImpl self = this;
        ResourceId resourceId = ResourceId.fromString(this.id());
        String newId = resourceId.parent().id() + "/databases/" + newDatabaseName;
        return this
            .sqlServerManager
            .inner()
            .getDatabases()
            .renameAsync(this.resourceGroupName, this.sqlServerName, self.name(), newId)
            .flatMap(
                aVoid ->
                    self
                        .sqlServerManager
                        .sqlServers()
                        .databases()
                        .getBySqlServerAsync(self.resourceGroupName, self.sqlServerName, newDatabaseName));
    }

    @Override
    public List<SqlDatabaseMetric> listMetrics(String filter) {
        List<SqlDatabaseMetric> sqlDatabaseMetrics = new ArrayList<>();
        PagedIterable<MetricInner> metricInners =
            this
                .sqlServerManager
                .inner()
                .getDatabases()
                .listMetrics(this.resourceGroupName, this.sqlServerName, this.name(), filter);
        for (MetricInner metricInner : metricInners) {
            sqlDatabaseMetrics.add(new SqlDatabaseMetricImpl(metricInner));
        }
        return Collections.unmodifiableList(sqlDatabaseMetrics);
    }

    @Override
    public PagedFlux<SqlDatabaseMetric> listMetricsAsync(final String filter) {
        return this
            .sqlServerManager
            .inner()
            .getDatabases()
            .listMetricsAsync(this.resourceGroupName, this.sqlServerName, this.name(), filter)
            .mapPage(SqlDatabaseMetricImpl::new);
    }

    @Override
    public List<SqlDatabaseMetricDefinition> listMetricDefinitions() {
        List<SqlDatabaseMetricDefinition> sqlDatabaseMetricDefinitions = new ArrayList<>();
        PagedIterable<MetricDefinitionInner> metricDefinitionInners =
            this
                .sqlServerManager
                .inner()
                .getDatabases()
                .listMetricDefinitions(this.resourceGroupName, this.sqlServerName, this.name());
        for (MetricDefinitionInner metricDefinitionInner : metricDefinitionInners) {
            sqlDatabaseMetricDefinitions.add(new SqlDatabaseMetricDefinitionImpl(metricDefinitionInner));
        }

        return Collections.unmodifiableList(sqlDatabaseMetricDefinitions);
    }

    @Override
    public PagedFlux<SqlDatabaseMetricDefinition> listMetricDefinitionsAsync() {
        return this
            .sqlServerManager
            .inner()
            .getDatabases()
            .listMetricDefinitionsAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(SqlDatabaseMetricDefinitionImpl::new);
    }

    @Override
    public TransparentDataEncryption getTransparentDataEncryption() {
        TransparentDataEncryptionInner transparentDataEncryptionInner =
            this
                .sqlServerManager
                .inner()
                .getTransparentDataEncryptions()
                .get(this.resourceGroupName, this.sqlServerName, this.name(), TransparentDataEncryptionName.CURRENT);
        return (transparentDataEncryptionInner == null)
            ? null
            : new TransparentDataEncryptionImpl(
                this.resourceGroupName, this.sqlServerName, transparentDataEncryptionInner, this.sqlServerManager);
    }

    @Override
    public Mono<TransparentDataEncryption> getTransparentDataEncryptionAsync() {
        final SqlDatabaseImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getTransparentDataEncryptions()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.name(), TransparentDataEncryptionName.CURRENT)
            .map(
                transparentDataEncryptionInner ->
                    new TransparentDataEncryptionImpl(
                        self.resourceGroupName,
                        self.sqlServerName,
                        transparentDataEncryptionInner,
                        self.sqlServerManager));
    }

    @Override
    public Map<String, ServiceTierAdvisor> listServiceTierAdvisors() {
        Map<String, ServiceTierAdvisor> serviceTierAdvisorMap = new HashMap<>();
        PagedIterable<ServiceTierAdvisorInner> serviceTierAdvisorInners =
            this
                .sqlServerManager
                .inner()
                .getServiceTierAdvisors()
                .listByDatabase(this.resourceGroupName, this.sqlServerName, this.name());
        for (ServiceTierAdvisorInner serviceTierAdvisorInner : serviceTierAdvisorInners) {
            serviceTierAdvisorMap
                .put(
                    serviceTierAdvisorInner.name(),
                    new ServiceTierAdvisorImpl(
                        this.resourceGroupName,
                        this.sqlServerName,
                        serviceTierAdvisorInner,
                        this.sqlServerManager));
        }
        return Collections.unmodifiableMap(serviceTierAdvisorMap);
    }

    @Override
    public PagedFlux<ServiceTierAdvisor> listServiceTierAdvisorsAsync() {
        final SqlDatabaseImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getServiceTierAdvisors()
            .listByDatabaseAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(
                serviceTierAdvisorInner ->
                    new ServiceTierAdvisorImpl(
                        self.resourceGroupName, self.sqlServerName, serviceTierAdvisorInner, self.sqlServerManager));
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.id());
    }

    @Override
    public String regionName() {
        return this.inner().location();
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
        return this
            .sqlServerManager
            .inner()
            .getDatabases()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    void addParentDependency(TaskGroup.HasTaskGroup parentDependency) {
        this.addDependency(parentDependency);
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        if (this.importRequestInner != null && this.elasticPoolId() != null) {
            final SqlDatabaseImpl self = this;
            final String epId = this.elasticPoolId();
            this
                .addPostRunDependent(
                    context -> {
                        self.importRequestInner = null;
                        self.withExistingElasticPoolId(epId);
                        return self.createResourceAsync().flatMap(sqlDatabase -> context.voidMono());
                    });
        }
    }

    @Override
    public Mono<SqlDatabase> createResourceAsync() {
        final SqlDatabaseImpl self = this;
        this.inner().withLocation(this.sqlServerLocation);
        if (this.importRequestInner != null) {
            this.importRequestInner.withDatabaseName(this.name());
            if (this.importRequestInner.edition() == null) {
                this.importRequestInner.withEdition(this.edition());
            }
            if (this.importRequestInner.serviceObjectiveName() == null && this.inner().sku() != null) {
                this
                    .importRequestInner
                    .withServiceObjectiveName(ServiceObjectiveName.fromString(this.inner().sku().name()));
            }
            if (this.importRequestInner.maxSizeBytes() == null) {
                this.importRequestInner.withMaxSizeBytes(String.valueOf(this.inner().maxSizeBytes()));
            }

            return this
                .sqlServerManager
                .inner()
                .getDatabases()
                .importMethodAsync(this.resourceGroupName, this.sqlServerName, this.importRequestInner)
                .then(Mono.defer(() -> {
                    if (self.elasticPoolId() != null) {
                        self.importRequestInner = null;
                        return self
                            .withExistingElasticPoolId(self.elasticPoolId())
                            .withPatchUpdate()
                            .updateResourceAsync();
                    } else {
                        return self.refreshAsync();
                    }
                }));
        } else {
            return this
                .sqlServerManager
                .inner()
                .getDatabases()
                .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.inner())
                .map(
                    inner -> {
                        self.setInner(inner);
                        return self;
                    });
        }
    }

    @Override
    public Mono<SqlDatabase> updateResourceAsync() {
        if (this.isPatchUpdate) {
            final SqlDatabaseImpl self = this;
            DatabaseUpdate databaseUpdateInner =
                new DatabaseUpdate()
                    .withTags(self.inner().tags())
                    .withCollation(self.inner().collation())
                    .withSourceDatabaseId(self.inner().sourceDatabaseId())
                    .withCreateMode(self.inner().createMode())
                    .withSku(self.inner().sku())
                    .withMaxSizeBytes(this.inner().maxSizeBytes())
                    .withElasticPoolId(this.inner().elasticPoolId());
            return this
                .sqlServerManager
                .inner()
                .getDatabases()
                .updateAsync(this.resourceGroupName, this.sqlServerName, this.name(), databaseUpdateInner)
                .map(
                    inner -> {
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
        return this
            .sqlServerManager
            .inner()
            .getDatabases()
            .deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public void delete() {
        this.sqlServerManager.inner().getDatabases().delete(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.deleteResourceAsync();
    }

    @Override
    public SqlDatabaseImpl withExistingSqlServer(
        String resourceGroupName, String sqlServerName, String sqlServerLocation) {
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
            return ResourceUtils
                .constructResourceId(
                    this.sqlServerManager.subscriptionId(),
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
            this.sqlElasticPools =
                new SqlElasticPoolsAsExternalChildResourcesImpl(
                    this.taskGroup(), this.sqlServerManager, "SqlElasticPool");
        }
        this.inner().withSku(null);
        this.inner().withElasticPoolId(generateElasticPoolIdFromName(elasticPoolName));

        return new SqlElasticPoolForDatabaseImpl(
            this,
            this
                .sqlElasticPools
                .defineIndependentElasticPool(elasticPoolName)
                .withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation));
    }

    @Override
    public SqlDatabaseImpl fromRestorableDroppedDatabase(SqlRestorableDroppedDatabase restorableDroppedDatabase) {
        Objects.requireNonNull(restorableDroppedDatabase);
        this.inner().withRestorableDroppedDatabaseId(restorableDroppedDatabase.id()).withCreateMode(CreateMode.RESTORE);
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
    public SqlDatabaseImpl importFrom(
        final StorageAccount storageAccount, final String containerName, final String fileName) {
        final SqlDatabaseImpl self = this;
        Objects.requireNonNull(storageAccount);
        this.initializeImportRequestInner();
        this
            .addDependency(
                context -> storageAccount
                    .getKeysAsync()
                    .flatMap(storageAccountKeys -> Mono.justOrEmpty(storageAccountKeys.stream().findFirst()))
                    .flatMap(
                        storageAccountKey -> {
                            self
                                .importRequestInner
                                .withStorageUri(
                                    String
                                        .format(
                                            "%s%s/%s",
                                            storageAccount.endPoints().primary().blob(),
                                            containerName,
                                            fileName));
                            self.importRequestInner.withStorageKeyType(StorageKeyType.STORAGE_ACCESS_KEY);
                            self.importRequestInner.withStorageKey(storageAccountKey.value());
                            return context.voidMono();
                        }));
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
    public SqlDatabaseImpl withSqlAdministratorLoginAndPassword(
        String administratorLogin, String administratorPassword) {
        this.importRequestInner.withAuthenticationType(AuthenticationType.SQL);
        this.importRequestInner.withAdministratorLogin(administratorLogin);
        this.importRequestInner.withAdministratorLoginPassword(administratorPassword);
        return this;
    }

    @Override
    public SqlDatabaseImpl withActiveDirectoryLoginAndPassword(
        String administratorLogin, String administratorPassword) {
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
        return this.withSourceDatabase(restorePoint.databaseId()).withMode(CreateMode.POINT_IN_TIME_RESTORE);
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
        Sku sku = new Sku().withName(ServiceObjectiveName.BASIC.toString()).withTier(DatabaseEdition.BASIC.toString());

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
    public SqlDatabaseImpl withStandardEdition(
        SqlDatabaseStandardServiceObjective serviceObjective, SqlDatabaseStandardStorage maxStorageCapacity) {
        Sku sku = new Sku().withName(serviceObjective.toString()).withTier(DatabaseEdition.STANDARD.toString());

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
    public SqlDatabaseImpl withPremiumEdition(
        SqlDatabasePremiumServiceObjective serviceObjective, SqlDatabasePremiumStorage maxStorageCapacity) {
        Sku sku = new Sku().withName(serviceObjective.toString()).withTier(DatabaseEdition.PREMIUM.toString());

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
    public SqlDatabaseImpl withCustomEdition(
        DatabaseEdition edition, ServiceObjectiveName serviceObjective, int capacity) {
        Sku sku =
            new Sku()
                .withName(serviceObjective.toString())
                .withTier(edition.toString())
                .withCapacity(capacity <= 0 ? null : capacity);

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
        this.inner().withTags(new HashMap<>(tags));
        return this;
    }

    @Override
    public SqlDatabaseImpl withTag(String key, String value) {
        if (this.inner().tags() == null) {
            this.inner().withTags(new HashMap<String, String>());
        }
        this.inner().tags().put(key, value);
        return this;
    }

    @Override
    public SqlDatabaseImpl withoutTag(String key) {
        if (this.inner().tags() != null) {
            this.inner().tags().remove(key);
        }
        return this;
    }

    @Override
    public SqlDatabaseImpl fromSample(SampleName sampleName) {
        this.inner().withSampleName(sampleName);
        return this;
    }
}
