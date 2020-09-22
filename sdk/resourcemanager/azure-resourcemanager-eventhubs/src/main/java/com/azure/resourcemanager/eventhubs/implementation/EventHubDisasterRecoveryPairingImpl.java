// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.inner.ArmDisasterRecoveryInner;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubDisasterRecoveryPairing;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.ProvisioningStateDR;
import com.azure.resourcemanager.eventhubs.models.RoleDisasterRecovery;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Implementation for {@link EventHubDisasterRecoveryPairing}.
 */
class EventHubDisasterRecoveryPairingImpl
    extends NestedResourceImpl<EventHubDisasterRecoveryPairing,
        ArmDisasterRecoveryInner,
        EventHubDisasterRecoveryPairingImpl>
    implements EventHubDisasterRecoveryPairing,
        EventHubDisasterRecoveryPairing.Definition,
        EventHubDisasterRecoveryPairing.Update {

    private Ancestors.OneAncestor ancestor;
    private final ClientLogger logger = new ClientLogger(EventHubDisasterRecoveryPairingImpl.class);

    EventHubDisasterRecoveryPairingImpl(String name, ArmDisasterRecoveryInner inner, EventHubsManager manager) {
        super(name, inner, manager);
        this.ancestor = new Ancestors().new OneAncestor(inner.id());
    }

    EventHubDisasterRecoveryPairingImpl(String name, EventHubsManager manager) {
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
    public EventHubDisasterRecoveryPairingImpl withNewPrimaryNamespace(
        Creatable<EventHubNamespace> namespaceCreatable) {
        this.addDependency(namespaceCreatable);
        if (namespaceCreatable instanceof EventHubNamespaceImpl) {
            EventHubNamespaceImpl namespace = ((EventHubNamespaceImpl) namespaceCreatable);
            this.ancestor = new Ancestors().new OneAncestor(namespace.resourceGroupName(), namespaceCreatable.name());
        } else {
            logger.logExceptionAsError(new IllegalArgumentException("The namespaceCreatable is invalid."));
        }
        return this;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withExistingPrimaryNamespace(EventHubNamespace namespace) {
        this.ancestor = new Ancestors().new OneAncestor(selfId(namespace.id()));
        return this;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withExistingPrimaryNamespace(
        String resourceGroupName, String primaryNamespaceName) {
        this.ancestor = new Ancestors().new OneAncestor(resourceGroupName, primaryNamespaceName);
        return this;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withExistingPrimaryNamespaceId(String namespaceId) {
        this.ancestor = new Ancestors().new OneAncestor(selfId(namespaceId));
        return this;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl withNewSecondaryNamespace(
        Creatable<EventHubNamespace> namespaceCreatable) {
        this.addDependency(namespaceCreatable);
        if (namespaceCreatable instanceof EventHubNamespaceImpl) {
            EventHubNamespaceImpl namespace = ((EventHubNamespaceImpl) namespaceCreatable);
            this.inner().withPartnerNamespace(namespace.name());
        } else {
            logger.logExceptionAsError(new IllegalArgumentException("The namespaceCreatable is invalid."));
        }
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
    public Mono<EventHubDisasterRecoveryPairing> createResourceAsync() {
        return this.manager().inner().getDisasterRecoveryConfigs()
            .createOrUpdateAsync(this.ancestor().resourceGroupName(),
                this.ancestor().ancestor1Name(),
                this.name(),
                this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<Void> breakPairingAsync() {
        return this.manager().inner().getDisasterRecoveryConfigs()
            .breakPairingAsync(this.ancestor().resourceGroupName(),
                    this.ancestor().ancestor1Name(),
                    this.name())
                .then(refreshAsync())
            .then();
    }

    @Override
    public void breakPairing() {
        this.breakPairingAsync().block();
    }

    @Override
    public Mono<Void> failOverAsync() {
        // Fail over is run against secondary namespace (because primary might be down at time of failover)
        //
        ResourceId secondaryNs = ResourceId.fromString(this.inner().partnerNamespace());
        return this.manager().inner().getDisasterRecoveryConfigs().failOverAsync(secondaryNs.resourceGroupName(),
            secondaryNs.name(),
            this.name())
            .then(refreshAsync())
            .then();
    }

    @Override
    public void failOver() {
        this.failOverAsync().block();
    }

    @Override
    public PagedFlux<DisasterRecoveryPairingAuthorizationRule> listAuthorizationRulesAsync() {
        return this.manager().disasterRecoveryPairingAuthorizationRules()
            .listByDisasterRecoveryPairingAsync(this.ancestor().resourceGroupName(),
                this.ancestor().ancestor1Name(),
                this.name());
    }

    @Override
    public PagedIterable<DisasterRecoveryPairingAuthorizationRule> listAuthorizationRules() {
        return this.manager().disasterRecoveryPairingAuthorizationRules()
            .listByDisasterRecoveryPairing(this.ancestor().resourceGroupName(),
                this.ancestor().ancestor1Name(),
                this.name());
    }

    @Override
    protected Mono<ArmDisasterRecoveryInner> getInnerAsync() {
        return this.manager().inner().getDisasterRecoveryConfigs().getAsync(this.ancestor().resourceGroupName(),
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
