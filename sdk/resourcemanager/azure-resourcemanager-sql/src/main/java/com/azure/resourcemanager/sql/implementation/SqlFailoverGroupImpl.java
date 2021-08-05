// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.management.Region;
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
import com.azure.resourcemanager.sql.fluent.models.FailoverGroupInner;
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
        return this.innerModel().id();
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.innerModel().id());
    }

    @Override
    public ReadWriteEndpointFailoverPolicy readWriteEndpointPolicy() {
        return this.innerModel().readWriteEndpoint() != null
            ? this.innerModel().readWriteEndpoint().failoverPolicy()
            : null;
    }

    @Override
    public int readWriteEndpointDataLossGracePeriodMinutes() {
        return this.innerModel().readWriteEndpoint() != null
                && this.innerModel().readWriteEndpoint().failoverWithDataLossGracePeriodMinutes() != null
            ? this.innerModel().readWriteEndpoint().failoverWithDataLossGracePeriodMinutes()
            : 0;
    }

    @Override
    public ReadOnlyEndpointFailoverPolicy readOnlyEndpointPolicy() {
        return this.innerModel().readOnlyEndpoint() != null
            ? this.innerModel().readOnlyEndpoint().failoverPolicy()
            : null;
    }

    @Override
    public FailoverGroupReplicationRole replicationRole() {
        return this.innerModel().replicationRole();
    }

    @Override
    public String replicationState() {
        return this.innerModel().replicationState();
    }

    @Override
    public List<PartnerInfo> partnerServers() {
        return Collections
            .unmodifiableList(
                this.innerModel().partnerServers() != null
                    ? this.innerModel().partnerServers()
                    : new ArrayList<PartnerInfo>());
    }

    @Override
    public List<String> databases() {
        return Collections
            .unmodifiableList(
                this.innerModel().databases() != null ? this.innerModel().databases() : new ArrayList<String>());
    }

    @Override
    public void delete() {
        this
            .sqlServerManager
            .serviceClient()
            .getFailoverGroups()
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
            .serviceClient()
            .getFailoverGroups()
            .createOrUpdateAsync(self.resourceGroupName, self.sqlServerName, self.name(), self.innerModel())
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
            .serviceClient()
            .getFailoverGroups()
            .deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    protected Mono<FailoverGroupInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getFailoverGroups()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public String type() {
        return this.innerModel().type();
    }

    @Override
    public String regionName() {
        return this.innerModel().location();
    }

    @Override
    public Region region() {
        return Region.fromName(this.innerModel().location());
    }

    @Override
    public Map<String, String> tags() {
        return this.innerModel().tags();
    }

    @Override
    public SqlFailoverGroupImpl withTags(Map<String, String> tags) {
        this.innerModel().withTags(new HashMap<>(tags));
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withTag(String key, String value) {
        if (this.innerModel().tags() == null) {
            this.innerModel().withTags(new HashMap<String, String>());
        }
        this.innerModel().tags().put(key, value);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withoutTag(String key) {
        if (this.innerModel().tags() != null) {
            this.innerModel().tags().remove(key);
        }
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(int gracePeriodInMinutes) {
        if (this.innerModel().readWriteEndpoint() == null) {
            this.innerModel().withReadWriteEndpoint(new FailoverGroupReadWriteEndpoint());
        }
        this.innerModel().readWriteEndpoint().withFailoverPolicy(ReadWriteEndpointFailoverPolicy.AUTOMATIC);
        this.innerModel().readWriteEndpoint().withFailoverWithDataLossGracePeriodMinutes(gracePeriodInMinutes);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withManualReadWriteEndpointPolicy() {
        if (this.innerModel().readWriteEndpoint() == null) {
            this.innerModel().withReadWriteEndpoint(new FailoverGroupReadWriteEndpoint());
        }
        this.innerModel().readWriteEndpoint().withFailoverPolicy(ReadWriteEndpointFailoverPolicy.MANUAL);
        this.innerModel().readWriteEndpoint().withFailoverWithDataLossGracePeriodMinutes(null);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withReadOnlyEndpointPolicyEnabled() {
        if (this.innerModel().readOnlyEndpoint() == null) {
            this.innerModel().withReadOnlyEndpoint(new FailoverGroupReadOnlyEndpoint());
        }
        this.innerModel().readOnlyEndpoint().withFailoverPolicy(ReadOnlyEndpointFailoverPolicy.ENABLED);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withReadOnlyEndpointPolicyDisabled() {
        if (this.innerModel().readOnlyEndpoint() == null) {
            this.innerModel().withReadOnlyEndpoint(new FailoverGroupReadOnlyEndpoint());
        }
        this.innerModel().readOnlyEndpoint().withFailoverPolicy(ReadOnlyEndpointFailoverPolicy.DISABLED);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withPartnerServerId(String id) {
        this.innerModel().withPartnerServers(new ArrayList<PartnerInfo>());
        this.innerModel().partnerServers().add(new PartnerInfo().withId(id));
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withDatabaseId(String id) {
        if (this.innerModel().databases() == null) {
            this.innerModel().withDatabases(new ArrayList<String>());
        }
        this.innerModel().databases().add(id);
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withNewDatabaseId(String id) {
        return this.withDatabaseId(id);
    }

    @Override
    public SqlFailoverGroupImpl withDatabaseIds(String... ids) {
        this.innerModel().withDatabases(new ArrayList<String>());
        for (String id : ids) {
            this.innerModel().databases().add(id);
        }
        return this;
    }

    @Override
    public SqlFailoverGroupImpl withoutDatabaseId(String id) {
        if (this.innerModel().databases() != null) {
            this.innerModel().databases().remove(id);
        }
        return this;
    }
}
