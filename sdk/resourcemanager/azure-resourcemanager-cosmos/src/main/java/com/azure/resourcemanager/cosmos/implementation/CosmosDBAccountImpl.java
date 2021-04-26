// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.cosmos.CosmosManager;
import com.azure.resourcemanager.cosmos.fluent.models.DatabaseAccountGetResultsInner;
import com.azure.resourcemanager.cosmos.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.cosmos.models.Capability;
import com.azure.resourcemanager.cosmos.models.ConnectorOffer;
import com.azure.resourcemanager.cosmos.models.ConsistencyPolicy;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountCreateUpdateParameters;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountKind;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountListConnectionStringsResult;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountListKeysResult;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountListReadOnlyKeysResult;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountOfferType;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountRegenerateKeyParameters;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountUpdateParameters;
import com.azure.resourcemanager.cosmos.models.DefaultConsistencyLevel;
import com.azure.resourcemanager.cosmos.models.FailoverPolicy;
import com.azure.resourcemanager.cosmos.models.IpAddressOrRange;
import com.azure.resourcemanager.cosmos.models.KeyKind;
import com.azure.resourcemanager.cosmos.models.Location;
import com.azure.resourcemanager.cosmos.models.PrivateEndpointConnection;
import com.azure.resourcemanager.cosmos.models.PrivateLinkResource;
import com.azure.resourcemanager.cosmos.models.PrivateLinkServiceConnectionStateProperty;
import com.azure.resourcemanager.cosmos.models.RegionForOnlineOffline;
import com.azure.resourcemanager.cosmos.models.SqlDatabase;
import com.azure.resourcemanager.cosmos.models.VirtualNetworkRule;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation for CosmosDBAccount. */
class CosmosDBAccountImpl
    extends GroupableResourceImpl<CosmosDBAccount, DatabaseAccountGetResultsInner, CosmosDBAccountImpl, CosmosManager>
    implements CosmosDBAccount, CosmosDBAccount.Definition, CosmosDBAccount.Update {
    private List<FailoverPolicy> failoverPolicies;
    private boolean hasFailoverPolicyChanges;
    private static final int MAX_DELAY_DUE_TO_MISSING_FAILOVERS = 60 * 10;
    private Map<String, VirtualNetworkRule> virtualNetworkRulesMap;
    private PrivateEndpointConnectionsImpl privateEndpointConnections;

    CosmosDBAccountImpl(String name, DatabaseAccountGetResultsInner innerObject, CosmosManager manager) {
        super(fixDBName(name), innerObject, manager);
        this.failoverPolicies = new ArrayList<>();
        this.privateEndpointConnections =
            new PrivateEndpointConnectionsImpl(this.manager().serviceClient().getPrivateEndpointConnections(), this);
    }

    @Override
    public DatabaseAccountKind kind() {
        return this.innerModel().kind();
    }

    @Override
    public String documentEndpoint() {
        return this.innerModel().documentEndpoint();
    }

    @Override
    public DatabaseAccountOfferType databaseAccountOfferType() {
        return this.innerModel().databaseAccountOfferType();
    }

    @Override
    public String ipRangeFilter() {
        if (CoreUtils.isNullOrEmpty(ipRules())) {
            return null;
        }
        return this.ipRules().stream().map(IpAddressOrRange::ipAddressOrRange).collect(Collectors.joining(","));
    }

    @Override
    public List<IpAddressOrRange> ipRules() {
        return Collections.unmodifiableList(this.innerModel().ipRules());
    }

    @Override
    public ConsistencyPolicy consistencyPolicy() {
        return this.innerModel().consistencyPolicy();
    }

    @Override
    public DefaultConsistencyLevel defaultConsistencyLevel() {
        if (this.innerModel().consistencyPolicy() == null) {
            throw new RuntimeException("Consistency policy is missing!");
        }

        return this.innerModel().consistencyPolicy().defaultConsistencyLevel();
    }

    @Override
    public List<Location> writableReplications() {
        return this.innerModel().writeLocations();
    }

    @Override
    public List<Location> readableReplications() {
        return this.innerModel().readLocations();
    }

    @Override
    public DatabaseAccountListKeysResult listKeys() {
        return this.listKeysAsync().block();
    }

    @Override
    public Mono<DatabaseAccountListKeysResult> listKeysAsync() {
        return this
            .manager()
            .serviceClient()
            .getDatabaseAccounts()
            .listKeysAsync(this.resourceGroupName(), this.name())
            .map(
                DatabaseAccountListKeysResultImpl::new);
    }

    @Override
    public DatabaseAccountListReadOnlyKeysResult listReadOnlyKeys() {
        return this.listReadOnlyKeysAsync().block();
    }

    @Override
    public Mono<DatabaseAccountListReadOnlyKeysResult> listReadOnlyKeysAsync() {
        return this
            .manager()
            .serviceClient()
            .getDatabaseAccounts()
            .listReadOnlyKeysAsync(this.resourceGroupName(), this.name())
            .map(
                DatabaseAccountListReadOnlyKeysResultImpl::new);
    }

    @Override
    public DatabaseAccountListConnectionStringsResult listConnectionStrings() {
        return this.listConnectionStringsAsync().block();
    }

    @Override
    public Mono<DatabaseAccountListConnectionStringsResult> listConnectionStringsAsync() {
        return this
            .manager()
            .serviceClient()
            .getDatabaseAccounts()
            .listConnectionStringsAsync(this.resourceGroupName(), this.name())
            .map(
                DatabaseAccountListConnectionStringsResultImpl::new);
    }

    @Override
    public List<SqlDatabase> listSqlDatabases() {
        return this.listSqlDatabasesAsync().collectList().block();
    }

    @Override
    public PagedFlux<SqlDatabase> listSqlDatabasesAsync() {
        return PagedConverter.mapPage(this
            .manager()
            .serviceClient()
            .getSqlResources()
            .listSqlDatabasesAsync(this.resourceGroupName(), this.name()),
            SqlDatabaseImpl::new);
    }

    @Override
    public List<PrivateLinkResource> listPrivateLinkResources() {
        return this.listPrivateLinkResourcesAsync().collectList().block();
    }

    @Override
    public PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync() {
        return PagedConverter.mapPage(this
            .manager()
            .serviceClient()
            .getPrivateLinkResources()
            .listByDatabaseAccountAsync(this.resourceGroupName(), this.name()),
            PrivateLinkResourceImpl::new);
    }

    @Override
    public PrivateLinkResource getPrivateLinkResource(String groupName) {
        return this.getPrivateLinkResourceAsync(groupName).block();
    }

    @Override
    public Mono<PrivateLinkResource> getPrivateLinkResourceAsync(String groupName) {
        return this
            .manager()
            .serviceClient()
            .getPrivateLinkResources()
            .getAsync(this.resourceGroupName(), this.name(), groupName)
            .map(PrivateLinkResourceImpl::new);
    }

    @Override
    public Map<String, PrivateEndpointConnection> listPrivateEndpointConnection() {
        return this.listPrivateEndpointConnectionAsync().block();
    }

    @Override
    public Mono<Map<String, PrivateEndpointConnection>> listPrivateEndpointConnectionAsync() {
        return this.privateEndpointConnections.asMapAsync();
    }

    @Override
    public PrivateEndpointConnection getPrivateEndpointConnection(String name) {
        return this.getPrivateEndpointConnectionAsync(name).block();
    }

    @Override
    public Mono<PrivateEndpointConnection> getPrivateEndpointConnectionAsync(String name) {
        return this
            .privateEndpointConnections
            .getImplAsync(name)
            .map(privateEndpointConnection -> privateEndpointConnection);
    }

    @Override
    public boolean multipleWriteLocationsEnabled() {
        return this.innerModel().enableMultipleWriteLocations();
    }

    @Override
    public boolean cassandraConnectorEnabled() {
        return this.innerModel().enableCassandraConnector();
    }

    @Override
    public ConnectorOffer cassandraConnectorOffer() {
        return this.innerModel().connectorOffer();
    }

    @Override
    public boolean keyBasedMetadataWriteAccessDisabled() {
        return this.innerModel().disableKeyBasedMetadataWriteAccess();
    }

    @Override
    public List<Capability> capabilities() {
        List<Capability> capabilities = this.innerModel().capabilities();
        if (capabilities == null) {
            capabilities = new ArrayList<>();
        }
        return Collections.unmodifiableList(capabilities);
    }

    @Override
    public List<VirtualNetworkRule> virtualNetworkRules() {
        List<VirtualNetworkRule> result =
            (this.innerModel() != null && this.innerModel().virtualNetworkRules() != null)
                ? this.innerModel().virtualNetworkRules()
                : new ArrayList<VirtualNetworkRule>();
        return Collections.unmodifiableList(result);
    }

    @Override
    public void offlineRegion(Region region) {
        this.manager().serviceClient().getDatabaseAccounts().offlineRegion(this.resourceGroupName(), this.name(),
            new RegionForOnlineOffline().withRegion(region.label()));
    }

    @Override
    public Mono<Void> offlineRegionAsync(Region region) {
        return this
            .manager()
            .serviceClient()
            .getDatabaseAccounts()
            .offlineRegionAsync(this.resourceGroupName(), this.name(),
                new RegionForOnlineOffline().withRegion(region.label()));
    }

    @Override
    public void onlineRegion(Region region) {
        this.manager().serviceClient().getDatabaseAccounts().onlineRegion(this.resourceGroupName(), this.name(),
            new RegionForOnlineOffline().withRegion(region.label()));
    }

    @Override
    public Mono<Void> onlineRegionAsync(Region region) {
        return this
            .manager()
            .serviceClient()
            .getDatabaseAccounts()
            .onlineRegionAsync(this.resourceGroupName(), this.name(),
                new RegionForOnlineOffline().withRegion(region.label()));
    }

    @Override
    public void regenerateKey(KeyKind keyKind) {
        this.manager().serviceClient().getDatabaseAccounts().regenerateKey(this.resourceGroupName(), this.name(),
            new DatabaseAccountRegenerateKeyParameters().withKeyKind(keyKind));
    }

    @Override
    public Mono<Void> regenerateKeyAsync(KeyKind keyKind) {
        return this
            .manager()
            .serviceClient()
            .getDatabaseAccounts()
            .regenerateKeyAsync(this.resourceGroupName(), this.name(),
                new DatabaseAccountRegenerateKeyParameters().withKeyKind(keyKind));
    }

    @Override
    public CosmosDBAccountImpl withKind(DatabaseAccountKind kind) {
        this.innerModel().withKind(kind);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withKind(DatabaseAccountKind kind, Capability... capabilities) {
        this.innerModel().withKind(kind);
        this.innerModel().withCapabilities(Arrays.asList(capabilities));
        return this;
    }

    @Override
    public CosmosDBAccountImpl withDataModelSql() {
        this.innerModel().withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withDataModelMongoDB() {
        this.innerModel().withKind(DatabaseAccountKind.MONGO_DB);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withDataModelCassandra() {
        this.innerModel().withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB);
        List<Capability> capabilities = new ArrayList<Capability>();
        capabilities.add(new Capability().withName("EnableCassandra"));
        this.innerModel().withCapabilities(capabilities);
        this.withTag("defaultExperience", "Cassandra");
        return this;
    }

    @Override
    public CosmosDBAccountImpl withDataModelAzureTable() {
        this.innerModel().withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB);
        List<Capability> capabilities = new ArrayList<Capability>();
        capabilities.add(new Capability().withName("EnableTable"));
        this.innerModel().withCapabilities(capabilities);
        this.withTag("defaultExperience", "Table");
        return this;
    }

    @Override
    public CosmosDBAccountImpl withDataModelGremlin() {
        this.innerModel().withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB);
        List<Capability> capabilities = new ArrayList<Capability>();
        capabilities.add(new Capability().withName("EnableGremlin"));
        this.innerModel().withCapabilities(capabilities);
        this.withTag("defaultExperience", "Graph");
        return this;
    }

    @Override
    public CosmosDBAccountImpl withIpRangeFilter(String ipRangeFilter) {
        List<IpAddressOrRange> rules = new ArrayList<>();
        if (!CoreUtils.isNullOrEmpty(ipRangeFilter)) {
            for (String ip : ipRangeFilter.split(",")) {
                rules.add(new IpAddressOrRange().withIpAddressOrRange(ip));
            }
        }
        this.innerModel().withIpRules(rules);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withIpRules(List<IpAddressOrRange> ipRules) {
        this.innerModel().withIpRules(ipRules);
        return this;
    }

    @Override
    protected Mono<DatabaseAccountGetResultsInner> getInnerAsync() {
        return this.manager().serviceClient().getDatabaseAccounts().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public CosmosDBAccountImpl withWriteReplication(Region region) {
        FailoverPolicy failoverPolicyInner = new FailoverPolicy();
        failoverPolicyInner.withLocationName(region.name());
        this.hasFailoverPolicyChanges = true;
        this.failoverPolicies.add(failoverPolicyInner);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withReadReplication(Region region) {
        this.ensureFailoverIsInitialized();
        FailoverPolicy failoverPolicyInner = new FailoverPolicy();
        failoverPolicyInner.withLocationName(region.name());
        failoverPolicyInner.withFailoverPriority(this.failoverPolicies.size());
        this.hasFailoverPolicyChanges = true;
        this.failoverPolicies.add(failoverPolicyInner);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withoutReadReplication(Region region) {
        this.ensureFailoverIsInitialized();
        for (int i = 1; i < this.failoverPolicies.size(); i++) {
            if (this.failoverPolicies.get(i).locationName() != null) {
                String locName = formatLocationName(this.failoverPolicies.get(i).locationName());
                if (locName.equals(region.name())) {
                    this.failoverPolicies.remove(i);
                }
            }
        }

        return this;
    }

    @Override
    public CosmosDBAccountImpl withEventualConsistency() {
        this.setConsistencyPolicy(DefaultConsistencyLevel.EVENTUAL, 0, 0);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withSessionConsistency() {
        this.setConsistencyPolicy(DefaultConsistencyLevel.SESSION, 0, 0);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withBoundedStalenessConsistency(long maxStalenessPrefix, int maxIntervalInSeconds) {
        this.setConsistencyPolicy(DefaultConsistencyLevel.BOUNDED_STALENESS, maxStalenessPrefix, maxIntervalInSeconds);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withStrongConsistency() {
        this.setConsistencyPolicy(DefaultConsistencyLevel.STRONG, 0, 0);
        return this;
    }

    @Override
    public PrivateEndpointConnectionImpl defineNewPrivateEndpointConnection(String name) {
        return this.privateEndpointConnections.define(name);
    }

    @Override
    public PrivateEndpointConnectionImpl updatePrivateEndpointConnection(String name) {
        return this.privateEndpointConnections.update(name);
    }

    @Override
    public CosmosDBAccountImpl withoutPrivateEndpointConnection(String name) {
        this.privateEndpointConnections.remove(name);
        return this;
    }

    CosmosDBAccountImpl withPrivateEndpointConnection(PrivateEndpointConnectionImpl privateEndpointConnection) {
        this.privateEndpointConnections.addPrivateEndpointConnection(privateEndpointConnection);
        return this;
    }

    @Override
    public Mono<CosmosDBAccount> createResourceAsync() {
        return this.doDatabaseUpdateCreate();
    }

    private DatabaseAccountCreateUpdateParameters createUpdateParametersInner(DatabaseAccountGetResultsInner inner) {
        this.ensureFailoverIsInitialized();
        DatabaseAccountCreateUpdateParameters createUpdateParametersInner = new DatabaseAccountCreateUpdateParameters();
        createUpdateParametersInner.withLocation(this.regionName().toLowerCase(Locale.ROOT));
        createUpdateParametersInner.withConsistencyPolicy(inner.consistencyPolicy());
        //        createUpdateParametersInner.withDatabaseAccountOfferType(
        //                DatabaseAccountOfferType.STANDARD.toString()); // Enum to Constant
        createUpdateParametersInner.withIpRules(inner.ipRules());
        createUpdateParametersInner.withKind(inner.kind());
        createUpdateParametersInner.withCapabilities(inner.capabilities());
        createUpdateParametersInner.withTags(inner.tags());
        createUpdateParametersInner.withEnableMultipleWriteLocations(inner.enableMultipleWriteLocations());
        this
            .addLocationsForParameters(
                new CreateUpdateLocationParameters(createUpdateParametersInner), this.failoverPolicies);
        createUpdateParametersInner.withIsVirtualNetworkFilterEnabled(inner.isVirtualNetworkFilterEnabled());
        createUpdateParametersInner.withEnableCassandraConnector(inner.enableCassandraConnector());
        createUpdateParametersInner.withConnectorOffer(inner.connectorOffer());
        createUpdateParametersInner.withEnableAutomaticFailover(inner.enableAutomaticFailover());
        createUpdateParametersInner.withDisableKeyBasedMetadataWriteAccess(inner.disableKeyBasedMetadataWriteAccess());
        if (this.virtualNetworkRulesMap != null) {
            createUpdateParametersInner
                .withVirtualNetworkRules(new ArrayList<VirtualNetworkRule>(this.virtualNetworkRulesMap.values()));
            this.virtualNetworkRulesMap = null;
        }
        return createUpdateParametersInner;
    }

    private DatabaseAccountUpdateParameters updateParametersInner(DatabaseAccountGetResultsInner inner) {
        this.ensureFailoverIsInitialized();
        DatabaseAccountUpdateParameters updateParameters = new DatabaseAccountUpdateParameters();
        updateParameters.withTags(inner.tags());
        updateParameters.withLocation(this.regionName().toLowerCase(Locale.ROOT));
        updateParameters.withConsistencyPolicy(inner.consistencyPolicy());
        updateParameters.withIpRules(inner.ipRules());
        updateParameters.withIsVirtualNetworkFilterEnabled(inner.isVirtualNetworkFilterEnabled());
        updateParameters.withEnableAutomaticFailover(inner.enableAutomaticFailover());
        updateParameters.withCapabilities(inner.capabilities());
        updateParameters.withEnableMultipleWriteLocations(inner.enableMultipleWriteLocations());
        updateParameters.withEnableCassandraConnector(inner.enableCassandraConnector());
        updateParameters.withConnectorOffer(inner.connectorOffer());
        updateParameters.withDisableKeyBasedMetadataWriteAccess(inner.disableKeyBasedMetadataWriteAccess());
        if (virtualNetworkRulesMap != null) {
            updateParameters.withVirtualNetworkRules(new ArrayList<>(this.virtualNetworkRulesMap.values()));
            virtualNetworkRulesMap = null;
        }
        this.addLocationsForParameters(new UpdateLocationParameters(updateParameters), this.failoverPolicies);

        return updateParameters;
    }

    private static String fixDBName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private void setConsistencyPolicy(
        DefaultConsistencyLevel level, long maxStalenessPrefix, int maxIntervalInSeconds) {
        ConsistencyPolicy policy = new ConsistencyPolicy();
        policy.withDefaultConsistencyLevel(level);
        if (level == DefaultConsistencyLevel.BOUNDED_STALENESS) {
            policy.withMaxStalenessPrefix(maxStalenessPrefix);
            policy.withMaxIntervalInSeconds(maxIntervalInSeconds);
        }

        this.innerModel().withConsistencyPolicy(policy);
    }

    private void addLocationsForParameters(HasLocations locationParameters, List<FailoverPolicy> failoverPolicies) {
        List<Location> locations = new ArrayList<Location>();

        if (failoverPolicies.size() > 0) {
            for (int i = 0; i < failoverPolicies.size(); i++) {
                FailoverPolicy policyInner = failoverPolicies.get(i);
                Location location = new Location();
                location.withFailoverPriority(i);
                location.withLocationName(policyInner.locationName());
                locations.add(location);
            }
        } else {
            Location location = new Location();
            location.withFailoverPriority(0);
            location.withLocationName(locationParameters.location());
            locations.add(location);
        }
        locationParameters.withLocations(locations);
    }

    private static String formatLocationName(String locationName) {
        return locationName.replace(" ", "").toLowerCase(Locale.ROOT);
    }

    private Mono<CosmosDBAccount> doDatabaseUpdateCreate() {
        final CosmosDBAccountImpl self = this;
        final List<Integer> data = new ArrayList<Integer>();
        data.add(0);

        Mono<DatabaseAccountGetResultsInner> request = null;
        HasLocations locationParameters = null;

        if (isInCreateMode()) {
            final DatabaseAccountCreateUpdateParameters createUpdateParametersInner =
                this.createUpdateParametersInner(this.innerModel());
            request =
                this
                    .manager()
                    .serviceClient()
                    .getDatabaseAccounts()
                    .createOrUpdateAsync(resourceGroupName(), name(), createUpdateParametersInner);
            locationParameters = new CreateUpdateLocationParameters(createUpdateParametersInner);
        } else {
            final DatabaseAccountUpdateParameters updateParametersInner = this.updateParametersInner(this.innerModel());
            request =
                this
                    .manager()
                    .serviceClient()
                    .getDatabaseAccounts()
                    .updateAsync(resourceGroupName(), name(), updateParametersInner);
            locationParameters = new UpdateLocationParameters(updateParametersInner);
        }

        Set<String> locations = locationParameters.locations().stream()
            .map(location -> formatLocationName(location.locationName()))
            .collect(Collectors.toSet());
        return request
            .flatMap(
                databaseAccountInner -> {
                    self.failoverPolicies.clear();
                    self.hasFailoverPolicyChanges = false;
                    return manager()
                        .databaseAccounts()
                        .getByResourceGroupAsync(resourceGroupName(), name())
                        .flatMap(
                            databaseAccount -> {
                                if (MAX_DELAY_DUE_TO_MISSING_FAILOVERS > data.get(0)
                                    && (databaseAccount.id() == null
                                        || databaseAccount.id().length() == 0
                                        || locations.size()
                                            != databaseAccount.innerModel().failoverPolicies().size())) {
                                    return Mono.empty();
                                }

                                if (isAFinalProvisioningState(databaseAccount.innerModel().provisioningState())) {
                                    for (Location location : databaseAccount.readableReplications()) {
                                        if (!isAFinalProvisioningState(location.provisioningState())) {
                                            return Mono.empty();
                                        }
                                        if (!locations.contains(formatLocationName(location.locationName()))) {
                                            return Mono.empty();
                                        }
                                    }
                                } else {
                                    return Mono.empty();
                                }

                                self.setInner(databaseAccount.innerModel());
                                return Mono.just(databaseAccount);
                            })
                        .repeatWhenEmpty(
                            longFlux ->
                                longFlux
                                    .flatMap(
                                        index -> {
                                            data.set(0, data.get(0) + 30);
                                            return Mono.delay(ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(
                                                manager().serviceClient().getDefaultPollInterval()));
                                        }));
                });
    }

    private void ensureFailoverIsInitialized() {
        if (this.isInCreateMode()) {
            return;
        }

        if (!this.hasFailoverPolicyChanges) {
            this.failoverPolicies.clear();
            FailoverPolicy[] policyInners = new FailoverPolicy[this.innerModel().failoverPolicies().size()];
            this.innerModel().failoverPolicies().toArray(policyInners);
            Arrays
                .sort(
                    policyInners,
                    Comparator.comparing(FailoverPolicy::failoverPriority));

            for (int i = 0; i < policyInners.length; i++) {
                this.failoverPolicies.add(policyInners[i]);
            }

            this.hasFailoverPolicyChanges = true;
        }
    }

    private boolean isAFinalProvisioningState(String state) {
        switch (state.toLowerCase(Locale.ROOT)) {
            case "succeeded":
            case "canceled":
            case "failed":
                return true;
            default:
                return false;
        }
    }

    private Map<String, VirtualNetworkRule> ensureVirtualNetworkRules() {
        if (this.virtualNetworkRulesMap == null) {
            this.virtualNetworkRulesMap = new HashMap<>();
            if (this.innerModel() != null && this.innerModel().virtualNetworkRules() != null) {
                for (VirtualNetworkRule virtualNetworkRule : this.innerModel().virtualNetworkRules()) {
                    this.virtualNetworkRulesMap.put(virtualNetworkRule.id(), virtualNetworkRule);
                }
            }
        }

        return this.virtualNetworkRulesMap;
    }

    @Override
    public CosmosDBAccountImpl withVirtualNetwork(String virtualNetworkId, String subnetName) {
        this.innerModel().withIsVirtualNetworkFilterEnabled(true);
        String vnetId = virtualNetworkId + "/subnets/" + subnetName;
        ensureVirtualNetworkRules().put(vnetId, new VirtualNetworkRule().withId(vnetId));
        return this;
    }

    @Override
    public CosmosDBAccountImpl withoutVirtualNetwork(String virtualNetworkId, String subnetName) {
        Map<String, VirtualNetworkRule> vnetRules = ensureVirtualNetworkRules();
        vnetRules.remove(virtualNetworkId + "/subnets/" + subnetName);
        if (vnetRules.size() == 0) {
            this.innerModel().withIsVirtualNetworkFilterEnabled(false);
        }
        return this;
    }

    @Override
    public CosmosDBAccountImpl withVirtualNetworkRules(List<VirtualNetworkRule> virtualNetworkRules) {
        Map<String, VirtualNetworkRule> vnetRules = ensureVirtualNetworkRules();
        if (virtualNetworkRules == null || virtualNetworkRules.isEmpty()) {
            vnetRules.clear();
            this.innerModel().withIsVirtualNetworkFilterEnabled(false);
            return this;
        }
        this.innerModel().withIsVirtualNetworkFilterEnabled(true);
        for (VirtualNetworkRule vnetRule : virtualNetworkRules) {
            this.virtualNetworkRulesMap.put(vnetRule.id(), vnetRule);
        }

        return this;
    }

    @Override
    public CosmosDBAccountImpl withMultipleWriteLocationsEnabled(boolean enabled) {
        this.innerModel().withEnableMultipleWriteLocations(enabled);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withCassandraConnector(ConnectorOffer connectorOffer) {
        this.innerModel().withEnableCassandraConnector(true);
        this.innerModel().withConnectorOffer(connectorOffer);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withoutCassandraConnector() {
        this.innerModel().withEnableCassandraConnector(false);
        this.innerModel().withConnectorOffer(null);
        return this;
    }

    @Override
    public CosmosDBAccountImpl withDisableKeyBaseMetadataWriteAccess(boolean disabled) {
        this.innerModel().withDisableKeyBasedMetadataWriteAccess(disabled);
        return this;
    }

    @Override
    public void approvePrivateEndpointConnection(String privateEndpointConnectionName) {
        approvePrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> approvePrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return manager().serviceClient().getPrivateEndpointConnections().createOrUpdateAsync(
            resourceGroupName(), name(), privateEndpointConnectionName,
            new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionStateProperty()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED.toString())))
            .then();
    }

    @Override
    public void rejectPrivateEndpointConnection(String privateEndpointConnectionName) {
        rejectPrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> rejectPrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return manager().serviceClient().getPrivateEndpointConnections().createOrUpdateAsync(
            resourceGroupName(), name(), privateEndpointConnectionName,
            new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionStateProperty()
                    .withStatus(PrivateEndpointServiceConnectionStatus.REJECTED.toString())))
            .then();
    }

    interface HasLocations {
        String location();

        List<Location> locations();

        void withLocations(List<Location> locations);
    }

    static class CreateUpdateLocationParameters implements HasLocations {
        private DatabaseAccountCreateUpdateParameters parameters;

        CreateUpdateLocationParameters(DatabaseAccountCreateUpdateParameters parametersObject) {
            parameters = parametersObject;
        }

        @Override
        public String location() {
            return parameters.location();
        }

        @Override
        public List<Location> locations() {
            return parameters.locations();
        }

        @Override
        public void withLocations(List<Location> locations) {
            parameters.withLocations(locations);
        }
    }

    static class UpdateLocationParameters implements HasLocations {
        private DatabaseAccountUpdateParameters parameters;

        UpdateLocationParameters(DatabaseAccountUpdateParameters parametersObject) {
            parameters = parametersObject;
        }

        @Override
        public String location() {
            return parameters.location();
        }

        @Override
        public List<Location> locations() {
            return parameters.locations();
        }

        @Override
        public void withLocations(List<Location> locations) {
            parameters.withLocations(locations);
        }
    }
}
