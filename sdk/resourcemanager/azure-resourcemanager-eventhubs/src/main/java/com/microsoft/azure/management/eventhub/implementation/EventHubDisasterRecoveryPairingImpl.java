/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.DisasterRecoveryPairingAuthorizationRule;
import com.microsoft.azure.management.eventhub.EventHubDisasterRecoveryPairing;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.eventhub.ProvisioningStateDR;
import com.microsoft.azure.management.eventhub.RoleDisasterRecovery;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import rx.Completable;
import rx.Observable;

import java.util.Objects;

/**
 * Implementation for {@link EventHubDisasterRecoveryPairing}.
 */
@LangDefinition
class EventHubDisasterRecoveryPairingImpl
        extends
        NestedResourceImpl<EventHubDisasterRecoveryPairing, ArmDisasterRecoveryInner, EventHubDisasterRecoveryPairingImpl>
        implements
        EventHubDisasterRecoveryPairing,
        EventHubDisasterRecoveryPairing.Definition,
        EventHubDisasterRecoveryPairing.Update {

    private Ancestors.OneAncestor ancestor;

    EventHubDisasterRecoveryPairingImpl(String name, ArmDisasterRecoveryInner inner, EventHubManager manager) {
        super(name, inner, manager);
        this.ancestor = new Ancestors().new OneAncestor(inner.id());
    }

    EventHubDisasterRecoveryPairingImpl(String name, EventHubManager manager) {
        super(name, new ArmDisasterRecoveryInner(), manager);
    }

    @Override
    public String primaryNamespaceResourceGroupName() {
        return this.ancestor().resourceGroupName();
    }

    @Override
    public String primaryNamespaceName() {
        return this.ancestor().ancestor1Name();
    }

    @Override
    public String secondaryNamespaceId() {
        return this.inner().partnerNamespace();
    }

    @Override
    public RoleDisasterRecovery namespaceRole() {
        return this.inner().role();
    }

    @Override
    public ProvisioningStateDR provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withNewPrimaryNamespace(Creatable<EventHubNamespace> namespaceCreatable) {
        this.addDependency(namespaceCreatable);
        EventHubNamespaceImpl namespace = ((EventHubNamespaceImpl) namespaceCreatable);
        this.ancestor = new Ancestors().new OneAncestor(namespace.resourceGroupName(), namespaceCreatable.name());
        return this;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withExistingPrimaryNamespace(EventHubNamespace namespace) {
        this.ancestor = new Ancestors().new OneAncestor(selfId(namespace.id()));
        return this;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withExistingPrimaryNamespace(String resourceGroupName, String primaryNamespaceName) {
        this.ancestor = new Ancestors().new OneAncestor(resourceGroupName, primaryNamespaceName);
        return this;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withExistingPrimaryNamespaceId(String namespaceId) {
        this.ancestor = new Ancestors().new OneAncestor(selfId(namespaceId));
        return this;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withNewSecondaryNamespace(Creatable<EventHubNamespace> namespaceCreatable) {
        this.addDependency(namespaceCreatable);
        EventHubNamespaceImpl namespace = ((EventHubNamespaceImpl) namespaceCreatable);
        this.inner().withPartnerNamespace(namespace.name());
        return this;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withExistingSecondaryNamespace(EventHubNamespace namespace) {
        Objects.requireNonNull(namespace.id());
        this.inner().withPartnerNamespace(namespace.id());
        return this;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withExistingSecondaryNamespaceId(String namespaceId) {
        Objects.requireNonNull(namespaceId);
        this.inner().withPartnerNamespace(namespaceId);
        return this;
    }

    @Override
    public Observable<EventHubDisasterRecoveryPairing> createResourceAsync() {
        return this.manager().inner().disasterRecoveryConfigs().createOrUpdateAsync(this.ancestor().resourceGroupName(),
                this.ancestor().ancestor1Name(),
                this.name(),
                this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public Completable breakPairingAsync() {
        return this.manager().inner().disasterRecoveryConfigs().breakPairingAsync(this.ancestor().resourceGroupName(),
                    this.ancestor().ancestor1Name(),
                    this.name())
                .toCompletable()
                .concatWith(this.refreshAsync().toCompletable());
    }

    @Override
    public void breakPairing() {
        this.breakPairingAsync().await();
    }

    @Override
    public Completable failOverAsync() {
        // Fail over is run against secondary namespace (because primary might be down at time of failover)
        //
        ResourceId secondaryNs = ResourceId.fromString(this.inner().partnerNamespace());
        return this.manager().inner().disasterRecoveryConfigs().failOverAsync(secondaryNs.resourceGroupName(),
                secondaryNs.name(),
                this.name())
                .toCompletable()
                .concatWith(this.refreshAsync().toCompletable());
    }

    @Override
    public void failOver() {
        this.failOverAsync().await();
    }

    @Override
    public Observable<DisasterRecoveryPairingAuthorizationRule> listAuthorizationRulesAsync() {
        return this.manager().disasterRecoveryPairingAuthorizationRules().listByDisasterRecoveryPairingAsync(this.ancestor().resourceGroupName(),
                this.ancestor().ancestor1Name(),
                this.name());
    }

    @Override
    public PagedList<DisasterRecoveryPairingAuthorizationRule> listAuthorizationRules() {
        return this.manager().disasterRecoveryPairingAuthorizationRules().listByDisasterRecoveryPairing(this.ancestor().resourceGroupName(),
                this.ancestor().ancestor1Name(),
                this.name());
    }

    @Override
    protected Observable<ArmDisasterRecoveryInner> getInnerAsync() {
        return this.manager().inner().disasterRecoveryConfigs().getAsync(this.ancestor().resourceGroupName(),
                this.ancestor().ancestor1Name(),
                this.name());
    }

    private Ancestors.OneAncestor ancestor() {
        Objects.requireNonNull(this.ancestor);
        return this.ancestor;
    }

    private String selfId(String parentId) {
        return String.format("%s/disasterRecoveryConfig/%s", parentId, this.name());
    }
}
