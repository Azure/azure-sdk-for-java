// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.fluent.models.DatabaseInner;
import com.azure.resourcemanager.sql.fluent.models.ElasticPoolActivityInner;
import com.azure.resourcemanager.sql.fluent.models.ElasticPoolDatabaseActivityInner;
import com.azure.resourcemanager.sql.fluent.models.ElasticPoolInner;
import com.azure.resourcemanager.sql.fluent.models.MetricDefinitionInner;
import com.azure.resourcemanager.sql.fluent.models.MetricInner;
import com.azure.resourcemanager.sql.models.ElasticPoolActivity;
import com.azure.resourcemanager.sql.models.ElasticPoolDatabaseActivity;
import com.azure.resourcemanager.sql.models.ElasticPoolEdition;
import com.azure.resourcemanager.sql.models.ElasticPoolPerDatabaseSettings;
import com.azure.resourcemanager.sql.models.ElasticPoolSku;
import com.azure.resourcemanager.sql.models.ElasticPoolState;
import com.azure.resourcemanager.sql.models.Sku;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetric;
import com.azure.resourcemanager.sql.models.SqlDatabaseMetricDefinition;
import com.azure.resourcemanager.sql.models.SqlDatabaseStandardServiceObjective;
import com.azure.resourcemanager.sql.models.SqlElasticPool;
import com.azure.resourcemanager.sql.models.SqlElasticPoolBasicEDTUs;
import com.azure.resourcemanager.sql.models.SqlElasticPoolBasicMaxEDTUs;
import com.azure.resourcemanager.sql.models.SqlElasticPoolBasicMinEDTUs;
import com.azure.resourcemanager.sql.models.SqlElasticPoolOperations;
import com.azure.resourcemanager.sql.models.SqlElasticPoolPremiumEDTUs;
import com.azure.resourcemanager.sql.models.SqlElasticPoolPremiumMaxEDTUs;
import com.azure.resourcemanager.sql.models.SqlElasticPoolPremiumMinEDTUs;
import com.azure.resourcemanager.sql.models.SqlElasticPoolPremiumSorage;
import com.azure.resourcemanager.sql.models.SqlElasticPoolStandardEDTUs;
import com.azure.resourcemanager.sql.models.SqlElasticPoolStandardMaxEDTUs;
import com.azure.resourcemanager.sql.models.SqlElasticPoolStandardMinEDTUs;
import com.azure.resourcemanager.sql.models.SqlElasticPoolStandardStorage;
import com.azure.resourcemanager.sql.models.SqlServer;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** Implementation for SqlElasticPool. */
public class SqlElasticPoolImpl
    extends ExternalChildResourceImpl<SqlElasticPool, ElasticPoolInner, SqlServerImpl, SqlServer>
    implements SqlElasticPool,
        SqlElasticPool.SqlElasticPoolDefinition<SqlServerImpl>,
        SqlElasticPoolOperations.DefinitionStages.WithCreate,
        SqlElasticPool.Update,
        SqlElasticPoolOperations.SqlElasticPoolOperationsDefinition {

    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;
    private String sqlServerLocation;

    private SqlDatabasesAsExternalChildResourcesImpl sqlDatabases;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlElasticPoolImpl(
        String name, SqlServerImpl parent, ElasticPoolInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject);

        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.name();
        this.sqlServerLocation = parent.regionName();

        this.sqlDatabases = null;
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
    SqlElasticPoolImpl(
        String resourceGroupName,
        String sqlServerName,
        String sqlServerLocation,
        String name,
        ElasticPoolInner innerObject,
        SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerLocation = sqlServerLocation;

        this.sqlDatabases =
            new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlElasticPoolImpl(String name, ElasticPoolInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;

        this.sqlDatabases =
            new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
    }

    @Override
    public String id() {
        return this.innerModel().id();
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
    public OffsetDateTime creationDate() {
        return this.innerModel().creationDate();
    }

    @Override
    public ElasticPoolState state() {
        return this.innerModel().state();
    }

    @Override
    public ElasticPoolEdition edition() {
        return ElasticPoolEdition.fromString(this.innerModel().sku().tier());
    }

    @Override
    public int dtu() {
        return this.innerModel().sku().capacity();
    }

    @Override
    public Double databaseDtuMax() {
        return this.innerModel().perDatabaseSettings().maxCapacity();
    }

    @Override
    public Double databaseDtuMin() {
        return this.innerModel().perDatabaseSettings().minCapacity();
    }

    @Override
    public Long storageCapacity() {
        return this.innerModel().maxSizeBytes();
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.id());
    }

    @Override
    public String regionName() {
        return this.sqlServerLocation;
    }

    @Override
    public Region region() {
        return Region.fromName(this.regionName());
    }

    @Override
    public List<ElasticPoolActivity> listActivities() {
        List<ElasticPoolActivity> elasticPoolActivities = new ArrayList<>();
        PagedIterable<ElasticPoolActivityInner> elasticPoolActivityInners =
            this
                .sqlServerManager
                .serviceClient()
                .getElasticPoolActivities()
                .listByElasticPool(this.resourceGroupName, this.sqlServerName, this.name());
        for (ElasticPoolActivityInner inner : elasticPoolActivityInners) {
            elasticPoolActivities.add(new ElasticPoolActivityImpl(inner));
        }
        return Collections.unmodifiableList(elasticPoolActivities);
    }

    @Override
    public PagedFlux<ElasticPoolActivity> listActivitiesAsync() {
        return PagedConverter.mapPage(this
            .sqlServerManager
            .serviceClient()
            .getElasticPoolActivities()
            .listByElasticPoolAsync(this.resourceGroupName, this.sqlServerName, this.name()),
            ElasticPoolActivityImpl::new);
    }

    @Override
    public List<ElasticPoolDatabaseActivity> listDatabaseActivities() {
        List<ElasticPoolDatabaseActivity> elasticPoolDatabaseActivities = new ArrayList<>();
        PagedIterable<ElasticPoolDatabaseActivityInner> elasticPoolDatabaseActivityInners =
            this
                .sqlServerManager
                .serviceClient()
                .getElasticPoolDatabaseActivities()
                .listByElasticPool(this.resourceGroupName, this.sqlServerName, this.name());
        for (ElasticPoolDatabaseActivityInner inner : elasticPoolDatabaseActivityInners) {
            elasticPoolDatabaseActivities.add(new ElasticPoolDatabaseActivityImpl(inner));
        }
        return Collections.unmodifiableList(elasticPoolDatabaseActivities);
    }

    @Override
    public PagedFlux<ElasticPoolDatabaseActivity> listDatabaseActivitiesAsync() {
        return PagedConverter.mapPage(this
            .sqlServerManager
            .serviceClient()
            .getElasticPoolDatabaseActivities()
            .listByElasticPoolAsync(this.resourceGroupName, this.sqlServerName, this.name()),
            ElasticPoolDatabaseActivityImpl::new);
    }

    @Override
    public List<SqlDatabaseMetric> listDatabaseMetrics(String filter) {
        List<SqlDatabaseMetric> databaseMetrics = new ArrayList<>();
        PagedIterable<MetricInner> inners =
            this
                .sqlServerManager
                .serviceClient()
                .getElasticPools()
                .listMetrics(this.resourceGroupName, this.sqlServerName, this.name(), filter);
        for (MetricInner inner : inners) {
            databaseMetrics.add(new SqlDatabaseMetricImpl(inner));
        }

        return Collections.unmodifiableList(databaseMetrics);
    }

    @Override
    public PagedFlux<SqlDatabaseMetric> listDatabaseMetricsAsync(String filter) {
        return PagedConverter.mapPage(this
            .sqlServerManager
            .serviceClient()
            .getElasticPools()
            .listMetricsAsync(this.resourceGroupName, this.sqlServerName, this.name(), filter),
            SqlDatabaseMetricImpl::new);
    }

    @Override
    public List<SqlDatabaseMetricDefinition> listDatabaseMetricDefinitions() {
        List<SqlDatabaseMetricDefinition> databaseMetricDefinitions = new ArrayList<>();
        PagedIterable<MetricDefinitionInner> inners =
            this
                .sqlServerManager
                .serviceClient()
                .getElasticPools()
                .listMetricDefinitions(this.resourceGroupName, this.sqlServerName, this.name());
        for (MetricDefinitionInner inner : inners) {
            databaseMetricDefinitions.add(new SqlDatabaseMetricDefinitionImpl(inner));
        }

        return Collections.unmodifiableList(databaseMetricDefinitions);
    }

    @Override
    public PagedFlux<SqlDatabaseMetricDefinition> listDatabaseMetricDefinitionsAsync() {
        return PagedConverter.mapPage(this
            .sqlServerManager
            .serviceClient()
            .getElasticPools()
            .listMetricDefinitionsAsync(this.resourceGroupName, this.sqlServerName, this.name()),
            SqlDatabaseMetricDefinitionImpl::new);
    }

    @Override
    public List<SqlDatabase> listDatabases() {
        List<SqlDatabase> databases = new ArrayList<>();
        PagedIterable<DatabaseInner> databaseInners =
            this
                .sqlServerManager
                .serviceClient()
                .getDatabases()
                .listByElasticPool(this.resourceGroupName, this.sqlServerName, this.name());
        for (DatabaseInner inner : databaseInners) {
            databases
                .add(
                    new SqlDatabaseImpl(
                        this.resourceGroupName,
                        this.sqlServerName,
                        this.sqlServerLocation,
                        inner.name(),
                        inner,
                        this.sqlServerManager));
        }
        return Collections.unmodifiableList(databases);
    }

    @Override
    public PagedFlux<SqlDatabase> listDatabasesAsync() {
        final SqlElasticPoolImpl self = this;
        return PagedConverter.mapPage(this
            .sqlServerManager
            .serviceClient()
            .getDatabases()
            .listByElasticPoolAsync(self.resourceGroupName, self.sqlServerName, this.name()),
                databaseInner ->
                    new SqlDatabaseImpl(
                        self.resourceGroupName,
                        self.sqlServerName,
                        self.sqlServerLocation,
                        databaseInner.name(),
                        databaseInner,
                        self.sqlServerManager));
    }

    @Override
    public SqlDatabase getDatabase(String databaseName) {
        DatabaseInner databaseInner =
            this
                .sqlServerManager
                .serviceClient()
                .getDatabases()
                .get(this.resourceGroupName, this.sqlServerName, databaseName);

        return databaseInner != null
            ? new SqlDatabaseImpl(
                this.resourceGroupName,
                this.sqlServerName,
                this.sqlServerLocation,
                databaseName,
                databaseInner,
                this.sqlServerManager)
            : null;
    }

    @Override
    public SqlDatabase addNewDatabase(String databaseName) {
        return this
            .sqlServerManager
            .sqlServers()
            .databases()
            .define(databaseName)
            .withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation)
            .withExistingElasticPool(this)
            .create();
    }

    @Override
    public SqlDatabase addExistingDatabase(String databaseName) {
        return this.getDatabase(databaseName).update().withExistingElasticPool(this).apply();
    }

    @Override
    public SqlDatabase addExistingDatabase(SqlDatabase database) {
        return database.update().withExistingElasticPool(this).apply();
    }

    @Override
    public SqlDatabase removeDatabase(String databaseName) {
        return this
            .getDatabase(databaseName)
            .update()
            .withoutElasticPool()
            .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
            .apply();
    }

    @Override
    public void delete() {
        this
            .sqlServerManager
            .serviceClient()
            .getElasticPools()
            .delete(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.deleteResourceAsync();
    }

    @Override
    protected Mono<ElasticPoolInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getElasticPools()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<SqlElasticPool> createResourceAsync() {
        final SqlElasticPoolImpl self = this;
        this.innerModel().withLocation(this.sqlServerLocation);
        return this
            .sqlServerManager
            .serviceClient()
            .getElasticPools()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.innerModel())
            .map(
                inner -> {
                    self.setInner(inner);
                    return self;
                });
    }

    @Override
    public Mono<SqlElasticPool> updateResourceAsync() {
        final SqlElasticPoolImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getElasticPools()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.innerModel())
            .map(
                inner -> {
                    self.setInner(inner);
                    return self;
                });
    }

    void addParentDependency(TaskGroup.HasTaskGroup parentDependency) {
        this.addDependency(parentDependency);
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        if (this.sqlDatabases != null) {
            this.sqlDatabases.clear();
        }

        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getElasticPools()
            .deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Update update() {
        super.prepareUpdate();
        return this;
    }

    @Override
    public SqlElasticPoolImpl withExistingSqlServer(String resourceGroupName, String sqlServerName, String location) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerLocation = location;

        return this;
    }

    @Override
    public SqlElasticPoolImpl withExistingSqlServer(SqlServer sqlServer) {
        this.resourceGroupName = sqlServer.resourceGroupName();
        this.sqlServerName = sqlServer.name();
        this.sqlServerLocation = sqlServer.regionName();

        return this;
    }

    public SqlElasticPoolImpl withEdition(ElasticPoolEdition edition) {
        if (this.innerModel().sku() == null) {
            this.innerModel().withSku(new Sku());
        }
        this.innerModel().sku().withTier(edition.toString());
        this.innerModel().sku().withName(edition.toString() + "Pool");
        return this;
    }

    @Override
    public SqlElasticPoolImpl withSku(ElasticPoolSku sku) {
        return withSku(sku.toSku());
    }

    @Override
    public SqlElasticPoolImpl withSku(Sku sku) {
        this.innerModel().withSku(sku);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withBasicPool() {
        this.withEdition(ElasticPoolEdition.BASIC);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withStandardPool() {
        this.withEdition(ElasticPoolEdition.STANDARD);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withPremiumPool() {
        this.withEdition(ElasticPoolEdition.PREMIUM);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withReservedDtu(SqlElasticPoolBasicEDTUs eDTU) {
        return this.withCapacity(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMax(SqlElasticPoolBasicMaxEDTUs eDTU) {
        return this.withDatabaseMaxCapacity(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMin(SqlElasticPoolBasicMinEDTUs eDTU) {
        return this.withDatabaseMinCapacity(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withReservedDtu(SqlElasticPoolStandardEDTUs eDTU) {
        return this.withCapacity(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMax(SqlElasticPoolStandardMaxEDTUs eDTU) {
        return this.withDatabaseMaxCapacity(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMin(SqlElasticPoolStandardMinEDTUs eDTU) {
        return this.withDatabaseMinCapacity(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withStorageCapacity(SqlElasticPoolStandardStorage storageCapacity) {
        this.withStorageCapacity(storageCapacity.capacityInMB() * 1024L * 1024L);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withReservedDtu(SqlElasticPoolPremiumEDTUs eDTU) {
        return this.withCapacity(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMax(SqlElasticPoolPremiumMaxEDTUs eDTU) {
        return this.withDatabaseMaxCapacity(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMin(SqlElasticPoolPremiumMinEDTUs eDTU) {
        return this.withDatabaseMinCapacity(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withStorageCapacity(SqlElasticPoolPremiumSorage storageCapacity) {
        return this.withStorageCapacity(storageCapacity.capacityInMB() * 1024L * 1024L);
    }

    @Override
    public SqlElasticPoolImpl withDatabaseMinCapacity(double minCapacity) {
        if (this.innerModel().perDatabaseSettings() == null) {
            this.innerModel().withPerDatabaseSettings(new ElasticPoolPerDatabaseSettings());
        }
        this.innerModel().perDatabaseSettings().withMinCapacity(minCapacity);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withDatabaseMaxCapacity(double maxCapacity) {
        if (this.innerModel().perDatabaseSettings() == null) {
            this.innerModel().withPerDatabaseSettings(new ElasticPoolPerDatabaseSettings());
        }
        this.innerModel().perDatabaseSettings().withMaxCapacity(maxCapacity);
        return this;
    }

    public SqlElasticPoolImpl withCapacity(int capacity) {
        if (this.innerModel().sku() == null) {
            this.innerModel().withSku(new Sku());
        }
        this.innerModel().sku().withCapacity(capacity);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withStorageCapacity(Long maxSizeBytes) {
        this.innerModel().withMaxSizeBytes(maxSizeBytes);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withNewDatabase(String databaseName) {
        if (this.sqlDatabases == null) {
            this.sqlDatabases =
                new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
        }

        return new SqlDatabaseForElasticPoolImpl(
                this,
                this
                    .sqlDatabases
                    .defineInlineDatabase(databaseName)
                    .withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation))
            .attach();
    }

    @Override
    public SqlElasticPoolImpl withExistingDatabase(String databaseName) {
        if (this.sqlDatabases == null) {
            this.sqlDatabases =
                new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
        }

        return new SqlDatabaseForElasticPoolImpl(
                this,
                this
                    .sqlDatabases
                    .patchUpdateDatabase(databaseName)
                    .withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation))
            .attach();
    }

    @Override
    public SqlElasticPoolImpl withExistingDatabase(SqlDatabase database) {
        if (this.sqlDatabases == null) {
            this.sqlDatabases =
                new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
        }

        return new SqlDatabaseForElasticPoolImpl(
                this,
                this
                    .sqlDatabases
                    .patchUpdateDatabase(database.name())
                    .withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation))
            .attach();
    }

    @Override
    public SqlDatabaseForElasticPoolImpl defineDatabase(String databaseName) {
        if (this.sqlDatabases == null) {
            this.sqlDatabases =
                new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
        }

        return new SqlDatabaseForElasticPoolImpl(
            this,
            this
                .sqlDatabases
                .defineInlineDatabase(databaseName)
                .withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation));
    }

    @Override
    public SqlElasticPoolImpl withTags(Map<String, String> tags) {
        this.innerModel().withTags(new HashMap<>(tags));
        return this;
    }

    @Override
    public SqlElasticPoolImpl withTag(String key, String value) {
        if (this.innerModel().tags() == null) {
            this.innerModel().withTags(new HashMap<String, String>());
        }
        this.innerModel().tags().put(key, value);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withoutTag(String key) {
        if (this.innerModel().tags() != null) {
            this.innerModel().tags().remove(key);
        }
        return this;
    }

    @Override
    public SqlServerImpl attach() {
        return parent();
    }
}
