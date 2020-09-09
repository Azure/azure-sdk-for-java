// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.FailoverGroupReadOnlyEndpoint;
import com.azure.resourcemanager.sql.models.FailoverGroupReadWriteEndpoint;
import com.azure.resourcemanager.sql.models.FailoverGroupReplicationRole;
import com.azure.resourcemanager.sql.models.PartnerInfo;
import com.azure.resourcemanager.sql.models.ReadOnlyEndpointFailoverPolicy;
import com.azure.resourcemanager.sql.models.ReadWriteEndpointFailoverPolicy;
import com.azure.resourcemanager.sql.models.SqlFailoverGroup;
import com.azure.resourcemanager.sql.models.SqlFailoverGroupOperations;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.fluent.inner.FailoverGroupInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SqlFailoverGroup. */
public class SqlFailoverGroupImpl
    extends ExternalChildResourceImpl<SqlFailoverGroup, FailoverGroupInner, SqlServerImpl, SqlServer>
    implements SqlFailoverGroup,
        SqlFailoverGroup.Update,
        SqlFailoverGroupOperations.SqlFailoverGroupOperationsDefinition {

    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;
    protected String sqlServerLocation;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses failover group operations
     */
    SqlFailoverGroupImpl(
        String name, SqlServerImpl parent, FailoverGroupInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject);

        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.name();
        this.sqlServerLocation = parent.regionName();
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param resourceGroupName the resource group name
     * @param sqlServerName the parent SQL server name
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses failover group operations
     */
    SqlFailoverGroupImpl(
        String resourceGroupName,
        String sqlServerName,
        String sqlServerLocation,
        String name,
        FailoverGroupInner innerObject,
        SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerLocation = sqlServerLocation;
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses failover group operations
     */
    SqlFailoverGroupImpl(String name, FailoverGroupInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        if (innerObject != null && innerObject.id() != null) {
            try {
                ResourceId resourceId = ResourceId.fromString(innerObject.id());
                this.resourceGroupName = resourceId.resourceGroupName();
                this.sqlServerName = resourceId.parent().name();
                this.sqlServerLocation = innerObject.location();
            } catch (NullPointerException e) {
            }
        }
    }

    protected String sqlServerLocation() {
        return this.sqlServerLocation;
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.inner().id());
    }

    @Override
    public ReadWriteEndpointFailoverPolicy readWriteEndpointPolicy() {
        return this.inner().readWriteEndpoint() != null ? this.inner().readWriteEndpoint().failoverPolicy() : null;
    }

    @Override
    public int readWriteEndpointDataLossGracePeriodMinutes() {
        return this.inner().readWriteEndpoint() != null
                && this.inner().readWriteEndpoint().failoverWithDataLossGracePeriodMinutes() != null
            ? this.inner().readWriteEndpoint().failoverWithDataLossGracePeriodMinutes()
            : 0;
    }

    @Override
    public ReadOnlyEndpointFailoverPolicy readOnlyEndpointPolicy() {
        return this.inner().readOnlyEndpoint() != null ? this.inner().readOnlyEndpoint().failoverPolicy() : null;
    }

    @Override
    public FailoverGroupReplicationRole replicationRole() {
        return this.inner().replicationRole();
    }

    @Override
    public String replicationState() {
        return this.inner().replicationState();
    }

    @Override
    public List<PartnerInfo> partnerServers() {
        return Collections
            .unmodifiableList(
                this.inner().partnerServers() != null ? this.inner().partnerServers() : new ArrayList<PartnerInfo>());
    }

    @Override
    public List<String> databases() {
        return Collections
            .unmodifiableList(this.inner().databases() != null ? this.inner().databases() : new ArrayList<String>());
    }

    @Override
    public void delete() {
        this.sqlServerManager.inner().getFailoverGroups()
            .delete(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.deleteResourceAsync();
    }

    @Override
    public SqlFailoverGroupImpl withExistingSqlServer(
        String resourceGroupName, String sqlServerName, String sqlServerLocation) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerLocation = sqlServerLocation;
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withExistingSqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        this.resourceGroupName = sqlServer.resourceGroupName();
        this.sqlServerName = sqlServer.name();
        this.sqlServerLocation = sqlServer.regionName();
        return this;
    }

    @Override
    public SqlFailoverGroupImpl update() {
        super.prepareUpdate();
        return this;
    }

    @Override
    public Mono<SqlFailoverGroup> createResourceAsync() {
        final SqlFailoverGroupImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getFailoverGroups()
            .createOrUpdateAsync(self.resourceGroupName, self.sqlServerName, self.name(), self.inner())
            .map(
                failoverGroupInner -> {
                    self.setInner(failoverGroupInner);
                    return self;
                });
    }

    @Override
    public Mono<SqlFailoverGroup> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .sqlServerManager
            .inner()
            .getFailoverGroups()
            .deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    protected Mono<FailoverGroupInner> getInnerAsync() {
        return this
            .sqlServerManager
            .inner()
            .getFailoverGroups()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public String type() {
        return this.inner().type();
    }

    @Override
    public String regionName() {
        return this.inner().location();
    }

    @Override
    public Region region() {
        return Region.fromName(this.inner().location());
    }

    @Override
    public Map<String, String> tags() {
        return this.inner().tags();
    }

    @Override
    public SqlFailoverGroupImpl withTags(Map<String, String> tags) {
        this.inner().withTags(new HashMap<>(tags));
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withTag(String key, String value) {
        if (this.inner().tags() == null) {
            this.inner().withTags(new HashMap<String, String>());
        }
        this.inner().tags().put(key, value);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withoutTag(String key) {
        if (this.inner().tags() != null) {
            this.inner().tags().remove(key);
        }
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(int gracePeriodInMinutes) {
        if (this.inner().readWriteEndpoint() == null) {
            this.inner().withReadWriteEndpoint(new FailoverGroupReadWriteEndpoint());
        }
        this.inner().readWriteEndpoint().withFailoverPolicy(ReadWriteEndpointFailoverPolicy.AUTOMATIC);
        this.inner().readWriteEndpoint().withFailoverWithDataLossGracePeriodMinutes(gracePeriodInMinutes);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withManualReadWriteEndpointPolicy() {
        if (this.inner().readWriteEndpoint() == null) {
            this.inner().withReadWriteEndpoint(new FailoverGroupReadWriteEndpoint());
        }
        this.inner().readWriteEndpoint().withFailoverPolicy(ReadWriteEndpointFailoverPolicy.MANUAL);
        this.inner().readWriteEndpoint().withFailoverWithDataLossGracePeriodMinutes(null);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withReadOnlyEndpointPolicyEnabled() {
        if (this.inner().readOnlyEndpoint() == null) {
            this.inner().withReadOnlyEndpoint(new FailoverGroupReadOnlyEndpoint());
        }
        this.inner().readOnlyEndpoint().withFailoverPolicy(ReadOnlyEndpointFailoverPolicy.ENABLED);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withReadOnlyEndpointPolicyDisabled() {
        if (this.inner().readOnlyEndpoint() == null) {
            this.inner().withReadOnlyEndpoint(new FailoverGroupReadOnlyEndpoint());
        }
        this.inner().readOnlyEndpoint().withFailoverPolicy(ReadOnlyEndpointFailoverPolicy.DISABLED);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withPartnerServerId(String id) {
        this.inner().withPartnerServers(new ArrayList<PartnerInfo>());
        this.inner().partnerServers().add(new PartnerInfo().withId(id));
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withDatabaseId(String id) {
        if (this.inner().databases() == null) {
            this.inner().withDatabases(new ArrayList<String>());
        }
        this.inner().databases().add(id);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withNewDatabaseId(String id) {
        return this.withDatabaseId(id);
    }

    @Override
    public SqlFailoverGroupImpl withDatabaseIds(String... ids) {
        this.inner().withDatabases(new ArrayList<String>());
        for (String id : ids) {
            this.inner().databases().add(id);
        }
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withoutDatabaseId(String id) {
        if (this.inner().databases() != null) {
            this.inner().databases().remove(key);
        }
        return this;
    }
}
