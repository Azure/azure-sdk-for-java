// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayInner;
import com.azure.resourcemanager.network.fluent.models.WebApplicationFirewallPolicyInner;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.KnownWebApplicationGatewayManagedRuleSet;
import com.azure.resourcemanager.network.models.ManagedRuleGroupOverride;
import com.azure.resourcemanager.network.models.ManagedRuleSet;
import com.azure.resourcemanager.network.models.ManagedRulesDefinition;
import com.azure.resourcemanager.network.models.PolicySettings;
import com.azure.resourcemanager.network.models.WebApplicationFirewallEnabledState;
import com.azure.resourcemanager.network.models.WebApplicationFirewallMode;
import com.azure.resourcemanager.network.models.WebApplicationFirewallPolicy;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link WebApplicationFirewallPolicy} interface.
 */
public class WebApplicationFirewallPolicyImpl extends
    GroupableResourceImpl<WebApplicationFirewallPolicy, WebApplicationFirewallPolicyInner, WebApplicationFirewallPolicyImpl, NetworkManager>
    implements WebApplicationFirewallPolicy, WebApplicationFirewallPolicy.Definition,
    WebApplicationFirewallPolicy.Update, WebApplicationFirewallPolicy.UpdateStages.WithRequestBodyOrUpdate {
    private static final String BOT_DETECTION_RULE_SET_TYPE = "Microsoft_BotManagerRuleSet";
    private static final String BOT_DETECTION_RULE_SET_VERSION_DEFAULT = "0.1";

    protected WebApplicationFirewallPolicyImpl(String name, WebApplicationFirewallPolicyInner innerObject,
        NetworkManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public WebApplicationFirewallMode mode() {
        if (this.innerModel().policySettings() == null) {
            return null;
        }
        return this.innerModel().policySettings().mode();
    }

    @Override
    public boolean isRequestBodyInspectionEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().policySettings().requestBodyCheck());
    }

    @Override
    public Integer requestBodySizeLimitInKb() {
        return this.innerModel().policySettings().requestBodyInspectLimitInKB();
    }

    @Override
    public Integer fileUploadSizeLimitInMb() {
        return innerModel().policySettings().fileUploadLimitInMb();
    }

    @Override
    public PolicySettings getPolicySettings() {
        return innerModel().policySettings();
    }

    @Override
    public ManagedRulesDefinition getManagedRules() {
        return innerModel().managedRules();
    }

    @Override
    public boolean isEnabled() {
        return WebApplicationFirewallEnabledState.ENABLED.equals(innerModel().policySettings().state());
    }

    @Override
    public List<String> getAssociatedApplicationGatewayIds() {
        if (CoreUtils.isNullOrEmpty(this.innerModel().applicationGateways())) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.innerModel()
            .applicationGateways()
            .stream()
            .map(ApplicationGatewayInner::id)
            .collect(Collectors.toList()));
    }

    @Override
    public List<ApplicationGateway> getAssociatedApplicationGateways() {
        List<ApplicationGateway> associatedGateways = getAssociatedApplicationGatewaysAsync().collectList().block();
        if (associatedGateways == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(associatedGateways);
    }

    @Override
    public Flux<ApplicationGateway> getAssociatedApplicationGatewaysAsync() {
        return Flux.fromIterable(getAssociatedApplicationGatewayIds())
            .flatMap(applicationGatewayId -> manager().applicationGateways().getByIdAsync(applicationGatewayId));
    }

    @Override
    public WebApplicationFirewallPolicyImpl withDetectionMode() {
        return withMode(WebApplicationFirewallMode.DETECTION);
    }

    @Override
    public WebApplicationFirewallPolicyImpl withPreventionMode() {
        return withMode(WebApplicationFirewallMode.PREVENTION);
    }

    @Override
    public WebApplicationFirewallPolicyImpl withMode(WebApplicationFirewallMode mode) {
        ensurePolicySettings().withMode(mode);
        return this;
    }

    @Override
    public WebApplicationFirewallPolicyImpl enablePolicy() {
        ensurePolicySettings().withState(WebApplicationFirewallEnabledState.ENABLED);
        return this;
    }

    @Override
    public WebApplicationFirewallPolicyImpl disablePolicy() {
        ensurePolicySettings().withState(WebApplicationFirewallEnabledState.DISABLED);
        return this;
    }

    @Override
    public WebApplicationFirewallPolicyImpl withBotProtection() {
        return withBotProtection(BOT_DETECTION_RULE_SET_VERSION_DEFAULT);
    }

    @Override
    public WebApplicationFirewallPolicyImpl withoutBotProtection() {
        ensureManagedRules().managedRuleSets()
            .removeIf(ruleSet -> BOT_DETECTION_RULE_SET_TYPE.equals(ruleSet.ruleSetType()));
        return this;
    }

    @Override
    public WebApplicationFirewallPolicyImpl withBotProtection(String version) {
        String versionOrDefault = CoreUtils.isNullOrEmpty(version) ? BOT_DETECTION_RULE_SET_VERSION_DEFAULT : version;
        Optional<ManagedRuleSet> ruleSetOptional = ensureManagedRules().managedRuleSets()
            .stream()
            .filter(ruleSet -> BOT_DETECTION_RULE_SET_TYPE.equals(ruleSet.ruleSetType()))
            .findFirst();

        if (ruleSetOptional.isPresent()) {
            ManagedRuleSet ruleSet = ruleSetOptional.get();
            // if Bot Protection rule present, only set non-null new version
            if (version != null) {
                ruleSet.withRuleSetVersion(version);
            }
        } else {
            ensureManagedRules().managedRuleSets()
                .add(new ManagedRuleSet().withRuleSetType(BOT_DETECTION_RULE_SET_TYPE)
                    .withRuleSetVersion(versionOrDefault));
        }
        return this;
    }

    @Override
    public WebApplicationFirewallPolicyImpl enableRequestBodyInspection() {
        ensurePolicySettings().withRequestBodyCheck(true);
        return this;
    }

    @Override
    public WebApplicationFirewallPolicyImpl disableRequestBodyInspection() {
        ensurePolicySettings().withRequestBodyCheck(false);
        return this;
    }

    @Override
    public WebApplicationFirewallPolicyImpl withRequestBodySizeLimitInKb(int limitInKb) {
        ensurePolicySettings().withRequestBodyInspectLimitInKB(limitInKb).withMaxRequestBodySizeInKb(limitInKb);
        return this;
    }

    @Override
    public WebApplicationFirewallPolicyImpl withFileUploadSizeLimitInMb(int limitInMb) {
        ensurePolicySettings().withFileUploadLimitInMb(limitInMb);
        return this;
    }

    @Override
    public Mono<WebApplicationFirewallPolicy> createResourceAsync() {
        return this.manager()
            .serviceClient()
            .getWebApplicationFirewallPolicies()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    public WebApplicationFirewallPolicyImpl withManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet managedRuleSet,
        ManagedRuleGroupOverride... managedRuleGroupOverrides) {
        Objects.requireNonNull(managedRuleSet);
        return withManagedRuleSet(managedRuleSet.type(), managedRuleSet.version(), managedRuleGroupOverrides);
    }

    @Override
    public WebApplicationFirewallPolicyImpl withManagedRuleSet(String type, String version,
        ManagedRuleGroupOverride... managedRuleGroupOverrides) {
        ManagedRuleSet managedRuleSet = new ManagedRuleSet().withRuleSetType(type).withRuleSetVersion(version);
        if (managedRuleGroupOverrides != null) {
            managedRuleSet
                .withRuleGroupOverrides(Arrays.stream(managedRuleGroupOverrides).collect(Collectors.toList()));
        }
        return withManagedRuleSet(managedRuleSet);
    }

    @Override
    public WebApplicationFirewallPolicyImpl withManagedRuleSet(ManagedRuleSet managedRuleSet) {
        Objects.requireNonNull(managedRuleSet);
        ManagedRulesDefinition managedRules = ensureManagedRules();

        // deduplicate by type and version
        withoutManagedRuleSet(managedRuleSet.ruleSetType(), managedRuleSet.ruleSetVersion());

        managedRules.managedRuleSets().add(managedRuleSet);
        return this;
    }

    @Override
    public WebApplicationFirewallPolicyImpl
        withoutManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet managedRuleSet) {
        Objects.requireNonNull(managedRuleSet);
        return withoutManagedRuleSet(managedRuleSet.type(), managedRuleSet.version());
    }

    @Override
    public WebApplicationFirewallPolicyImpl withoutManagedRuleSet(String type, String version) {
        if (this.innerModel().managedRules() != null && this.innerModel().managedRules().managedRuleSets() != null) {
            this.innerModel()
                .managedRules()
                .managedRuleSets()
                .removeIf(ruleSet -> Objects.equals(type, ruleSet.ruleSetType())
                    && Objects.equals(version, ruleSet.ruleSetVersion()));
        }
        return this;
    }

    @Override
    protected Mono<WebApplicationFirewallPolicyInner> getInnerAsync() {
        return this.manager()
            .serviceClient()
            .getWebApplicationFirewallPolicies()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private PolicySettings ensurePolicySettings() {
        if (this.innerModel().policySettings() == null) {
            PolicySettings policySettings = new PolicySettings();
            this.innerModel().withPolicySettings(policySettings);
        }
        return this.innerModel().policySettings();
    }

    private ManagedRulesDefinition ensureManagedRules() {
        ManagedRulesDefinition managedRulesDefinition = this.innerModel().managedRules();
        if (managedRulesDefinition == null) {
            managedRulesDefinition = new ManagedRulesDefinition();
            this.innerModel().withManagedRules(managedRulesDefinition);
            if (managedRulesDefinition.managedRuleSets() == null) {
                managedRulesDefinition.withManagedRuleSets(new ArrayList<>());
            }
            if (managedRulesDefinition.exclusions() == null) {
                managedRulesDefinition.withExclusions(new ArrayList<>());
            }
        }
        return managedRulesDefinition;
    }
}
