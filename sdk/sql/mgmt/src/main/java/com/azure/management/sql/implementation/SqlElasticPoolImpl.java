/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.management.resources.fluentcore.dag.TaskGroup;
import com.azure.management.sql.ElasticPoolActivity;
import com.azure.management.sql.ElasticPoolDatabaseActivity;
import com.azure.management.sql.ElasticPoolEdition;
import com.azure.management.sql.ElasticPoolPerDatabaseSettings;
import com.azure.management.sql.ElasticPoolState;
import com.azure.management.sql.Sku;
import com.azure.management.sql.SqlDatabase;
import com.azure.management.sql.SqlDatabaseMetric;
import com.azure.management.sql.SqlDatabaseMetricDefinition;
import com.azure.management.sql.SqlDatabaseStandardServiceObjective;
import com.azure.management.sql.SqlElasticPool;
import com.azure.management.sql.SqlElasticPoolBasicEDTUs;
import com.azure.management.sql.SqlElasticPoolBasicMaxEDTUs;
import com.azure.management.sql.SqlElasticPoolBasicMinEDTUs;
import com.azure.management.sql.SqlElasticPoolOperations;
import com.azure.management.sql.SqlElasticPoolPremiumEDTUs;
import com.azure.management.sql.SqlElasticPoolPremiumMaxEDTUs;
import com.azure.management.sql.SqlElasticPoolPremiumMinEDTUs;
import com.azure.management.sql.SqlElasticPoolPremiumSorage;
import com.azure.management.sql.SqlElasticPoolStandardEDTUs;
import com.azure.management.sql.SqlElasticPoolStandardMaxEDTUs;
import com.azure.management.sql.SqlElasticPoolStandardMinEDTUs;
import com.azure.management.sql.SqlElasticPoolStandardStorage;
import com.azure.management.sql.SqlServer;
import com.azure.management.sql.models.DatabaseInner;
import com.azure.management.sql.models.ElasticPoolActivityInner;
import com.azure.management.sql.models.ElasticPoolDatabaseActivityInner;
import com.azure.management.sql.models.ElasticPoolInner;
import com.azure.management.sql.models.MetricDefinitionInner;
import com.azure.management.sql.models.MetricInner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation for SqlElasticPool.
 */
public class SqlElasticPoolImpl
    extends
        ExternalChildResourceImpl<SqlElasticPool, ElasticPoolInner, SqlServerImpl, SqlServer>
    implements
        SqlElasticPool,
        SqlElasticPool.SqlElasticPoolDefinition<SqlServer.DefinitionStages.WithCreate>,
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
     * @param name        the name of this external child resource
     * @param parent      reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlElasticPoolImpl(String name, SqlServerImpl parent, ElasticPoolInner innerObject, SqlServerManager sqlServerManager) {
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
     * @param name        the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlElasticPoolImpl(String resourceGroupName, String sqlServerName, String sqlServerLocation, String name, ElasticPoolInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerLocation = sqlServerLocation;

        this.sqlDatabases = new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name        the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlElasticPoolImpl(String name, ElasticPoolInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;

        this.sqlDatabases = new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
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
    public OffsetDateTime creationDate() {
        return this.inner().creationDate();
    }

    @Override
    public ElasticPoolState state() {
        return this.inner().state();
    }

    @Override
    public ElasticPoolEdition edition() {
        return ElasticPoolEdition.fromString(this.inner().sku().tier());
    }

    @Override
    public int dtu() {
        return this.inner().sku().capacity();
    }

    @Override
    public Double databaseDtuMax() {
        return this.inner().perDatabaseSettings().maxCapacity();
    }

    @Override
    public Double databaseDtuMin() {
        return this.inner().perDatabaseSettings().minCapacity();
    }

    @Override
    public Long storageCapacity() {
        return this.inner().maxSizeBytes();
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
        PagedIterable<ElasticPoolActivityInner> elasticPoolActivityInners = this.sqlServerManager.inner()
            .elasticPoolActivities().listByElasticPool(this.resourceGroupName, this.sqlServerName, this.name());
        if (elasticPoolActivityInners != null) {
            for (ElasticPoolActivityInner inner : elasticPoolActivityInners) {
                elasticPoolActivities.add(new ElasticPoolActivityImpl(inner));
            }
        }
        return Collections.unmodifiableList(elasticPoolActivities);
    }

    @Override
    public PagedFlux<ElasticPoolActivity> listActivitiesAsync() {
        return this.sqlServerManager.inner()
            .elasticPoolActivities().listByElasticPoolAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(elasticPoolActivityInner -> new ElasticPoolActivityImpl(elasticPoolActivityInner));
    }

    @Override
    public List<ElasticPoolDatabaseActivity> listDatabaseActivities() {
        List<ElasticPoolDatabaseActivity> elasticPoolDatabaseActivities = new ArrayList<>();
        PagedIterable<ElasticPoolDatabaseActivityInner> elasticPoolDatabaseActivityInners = this.sqlServerManager.inner()
            .elasticPoolDatabaseActivities().listByElasticPool(this.resourceGroupName, this.sqlServerName, this.name());
        if (elasticPoolDatabaseActivityInners != null) {
            for (ElasticPoolDatabaseActivityInner inner : elasticPoolDatabaseActivityInners) {
                elasticPoolDatabaseActivities.add(new ElasticPoolDatabaseActivityImpl(inner));
            }
        }
        return Collections.unmodifiableList(elasticPoolDatabaseActivities);
    }

    @Override
    public PagedFlux<ElasticPoolDatabaseActivity> listDatabaseActivitiesAsync() {
        return this.sqlServerManager.inner()
            .elasticPoolDatabaseActivities().listByElasticPoolAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(elasticPoolDatabaseActivityInner -> new ElasticPoolDatabaseActivityImpl(elasticPoolDatabaseActivityInner));
    }

    @Override
    public List<SqlDatabaseMetric> listDatabaseMetrics(String filter) {
        List<SqlDatabaseMetric> databaseMetrics = new ArrayList<>();
        PagedIterable<MetricInner> inners = this.sqlServerManager.inner().elasticPools().listMetrics(this.resourceGroupName, this.sqlServerName, this.name(), filter);
        if (inners != null) {
            for (MetricInner inner : inners) {
                databaseMetrics.add(new SqlDatabaseMetricImpl(inner));
            }
        }

        return Collections.unmodifiableList(databaseMetrics);
    }

    @Override
    public PagedFlux<SqlDatabaseMetric> listDatabaseMetricsAsync(String filter) {
        return this.sqlServerManager.inner().elasticPools().listMetricsAsync(this.resourceGroupName, this.sqlServerName, this.name(), filter)
            .mapPage(metricInner -> new SqlDatabaseMetricImpl(metricInner));
    }

    @Override
    public List<SqlDatabaseMetricDefinition> listDatabaseMetricDefinitions() {
        List<SqlDatabaseMetricDefinition> databaseMetricDefinitions = new ArrayList<>();
        PagedIterable<MetricDefinitionInner> inners = this.sqlServerManager.inner().elasticPools().listMetricDefinitions(this.resourceGroupName, this.sqlServerName, this.name());
        if (inners != null) {
            for (MetricDefinitionInner inner : inners) {
                databaseMetricDefinitions.add(new SqlDatabaseMetricDefinitionImpl(inner));
            }
        }

        return Collections.unmodifiableList(databaseMetricDefinitions);
    }

    @Override
    public PagedFlux<SqlDatabaseMetricDefinition> listDatabaseMetricDefinitionsAsync() {
        return this.sqlServerManager.inner().elasticPools().listMetricDefinitionsAsync(this.resourceGroupName, this.sqlServerName, this.name())
            .mapPage(metricDefinitionInner -> new SqlDatabaseMetricDefinitionImpl(metricDefinitionInner));
    }

    @Override
    public List<SqlDatabase> listDatabases() {
        List<SqlDatabase> databases = new ArrayList<>();
        PagedIterable<DatabaseInner> databaseInners = this.sqlServerManager.inner().databases()
            .listByElasticPool(this.resourceGroupName, this.sqlServerName, this.name());
        if (databaseInners != null) {
            for (DatabaseInner inner : databaseInners) {
                databases.add(new SqlDatabaseImpl(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation, inner.getName(), inner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableList(databases);
    }

    @Override
    public PagedFlux<SqlDatabase> listDatabasesAsync() {
        final SqlElasticPoolImpl self = this;
        return this.sqlServerManager.inner().databases()
            .listByElasticPoolAsync(self.resourceGroupName, self.sqlServerName, this.name())
            .mapPage(databaseInner -> new SqlDatabaseImpl(self.resourceGroupName, self.sqlServerName, self.sqlServerLocation, databaseInner.getName(), databaseInner, self.sqlServerManager));
    }

    @Override
    public SqlDatabase getDatabase(String databaseName) {
        DatabaseInner databaseInner = this.sqlServerManager.inner().databases()
            .get(this.resourceGroupName, this.sqlServerName, databaseName);

        return databaseInner != null ? new SqlDatabaseImpl(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation, databaseName, databaseInner, this.sqlServerManager) : null;
    }

    @Override
    public SqlDatabase addNewDatabase(String databaseName) {
        return this.sqlServerManager.sqlServers().databases()
            .define(databaseName)
            .withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation)
            .withExistingElasticPool(this)
            .create();
    }

    @Override
    public SqlDatabase addExistingDatabase(String databaseName) {
        return this.getDatabase(databaseName)
            .update()
            .withExistingElasticPool(this)
            .apply();
    }

    @Override
    public SqlDatabase addExistingDatabase(SqlDatabase database) {
        return database
            .update()
            .withExistingElasticPool(this)
            .apply();
    }

    @Override
    public SqlDatabase removeDatabase(String databaseName) {
        return this.getDatabase(databaseName)
            .update()
            .withoutElasticPool()
            .withStandardEdition(SqlDatabaseStandardServiceObjective.S0)
            .apply();
    }

    @Override
    public void delete() {
        this.sqlServerManager.inner().elasticPools().delete(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.deleteResourceAsync();
    }

    @Override
    protected Mono<ElasticPoolInner> getInnerAsync() {
        return this.sqlServerManager.inner().elasticPools().getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<SqlElasticPool> createResourceAsync() {
        final SqlElasticPoolImpl self = this;
        this.inner().setLocation(this.sqlServerLocation);
        return this.sqlServerManager.inner().elasticPools()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.inner())
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<SqlElasticPool> updateResourceAsync() {
        final SqlElasticPoolImpl self = this;
        return this.sqlServerManager.inner().elasticPools()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.inner())
            .map(inner -> {
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
        return this.sqlServerManager.inner().elasticPools().deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
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
        if (this.inner().sku() == null) {
            this.inner().withSku(new Sku());
        }
        this.inner().sku().withTier(edition.toString());
        this.inner().sku().withName(edition.toString() + "Pool");
        return this;
    }

    public SqlElasticPoolImpl withCustomEdition(Sku sku) {
        this.inner().withSku(sku);
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
        return this.withDtu(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMax(SqlElasticPoolBasicMaxEDTUs eDTU) {
        return this.withDatabaseDtuMax(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMin(SqlElasticPoolBasicMinEDTUs eDTU) {
        return this.withDatabaseDtuMin(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withReservedDtu(SqlElasticPoolStandardEDTUs eDTU) {
        return this.withDtu(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMax(SqlElasticPoolStandardMaxEDTUs eDTU) {
        return this.withDatabaseDtuMax(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMin(SqlElasticPoolStandardMinEDTUs eDTU) {
        return this.withDatabaseDtuMin(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withStorageCapacity(SqlElasticPoolStandardStorage storageCapacity) {
        this.withStorageCapacity(storageCapacity.capacityInMB() * 1024L * 1024L);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withReservedDtu(SqlElasticPoolPremiumEDTUs eDTU) {
        return this.withDtu(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMax(SqlElasticPoolPremiumMaxEDTUs eDTU) {
        return this.withDatabaseDtuMax(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMin(SqlElasticPoolPremiumMinEDTUs eDTU) {
        return this.withDatabaseDtuMin(eDTU.value());
    }

    @Override
    public SqlElasticPoolImpl withStorageCapacity(SqlElasticPoolPremiumSorage storageCapacity) {
        return this.withStorageCapacity(storageCapacity.capacityInMB() * 1024L * 1024L);
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMin(double databaseDtuMin) {
        if (this.inner().perDatabaseSettings() == null) {
            this.inner().withPerDatabaseSettings(new ElasticPoolPerDatabaseSettings());
        }
        this.inner().perDatabaseSettings().withMinCapacity(databaseDtuMin);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMax(double databaseDtuMax) {
        if (this.inner().perDatabaseSettings() == null) {
            this.inner().withPerDatabaseSettings(new ElasticPoolPerDatabaseSettings());
        }
        this.inner().perDatabaseSettings().withMaxCapacity(databaseDtuMax);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withDtu(int dtu) {
        if (this.inner().sku() == null) {
            this.inner().withSku(new Sku());
        }
        this.inner().sku().withCapacity(dtu);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withStorageCapacity(Long maxSizeBytes) {
        this.inner().withMaxSizeBytes(maxSizeBytes);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withNewDatabase(String databaseName) {
        if (this.sqlDatabases == null) {
            this.sqlDatabases = new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
        }

        return new SqlDatabaseForElasticPoolImpl(this, this.sqlDatabases
            .defineInlineDatabase(databaseName).withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation))
            .attach();
    }

    @Override
    public SqlElasticPoolImpl withExistingDatabase(String databaseName) {
        if (this.sqlDatabases == null) {
            this.sqlDatabases = new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
        }

        return new SqlDatabaseForElasticPoolImpl(this, this.sqlDatabases
            .patchUpdateDatabase(databaseName).withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation))
            .attach();
    }

    @Override
    public SqlElasticPoolImpl withExistingDatabase(SqlDatabase database) {
        if (this.sqlDatabases == null) {
            this.sqlDatabases = new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
        }

        return new SqlDatabaseForElasticPoolImpl(this, this.sqlDatabases
            .patchUpdateDatabase(database.name()).withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation))
            .attach();
    }

    @Override
    public SqlDatabaseForElasticPoolImpl defineDatabase(String databaseName) {
        if (this.sqlDatabases == null) {
            this.sqlDatabases = new SqlDatabasesAsExternalChildResourcesImpl(this.taskGroup(), this.sqlServerManager, "SqlDatabase");
        }

        return new SqlDatabaseForElasticPoolImpl(this, this.sqlDatabases
            .defineInlineDatabase(databaseName).withExistingSqlServer(this.resourceGroupName, this.sqlServerName, this.sqlServerLocation));
    }

    @Override
    public SqlElasticPoolImpl withTags(Map<String, String> tags) {
        this.inner().setTags(new HashMap<>(tags));
        return this;
    }

    @Override
    public SqlElasticPoolImpl withTag(String key, String value) {
        if (this.inner().getTags() == null) {
            this.inner().setTags(new HashMap<String, String>());
        }
        this.inner().getTags().put(key, value);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withoutTag(String key) {
        if (this.inner().getTags() != null) {
            this.inner().getTags().remove(key);
        }
        return this;
    }

    @Override
    public SqlServerImpl attach() {
        return parent();
    }
}
