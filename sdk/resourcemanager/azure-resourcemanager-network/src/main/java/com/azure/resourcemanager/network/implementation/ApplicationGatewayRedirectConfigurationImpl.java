// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayListener;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectType;
import com.azure.resourcemanager.network.models.ApplicationGatewayRequestRoutingRule;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayRedirectConfigurationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Implementation for ApplicationGatewayRedirectConfiguration. */
class ApplicationGatewayRedirectConfigurationImpl
    extends ChildResourceImpl<ApplicationGatewayRedirectConfigurationInner, ApplicationGatewayImpl, ApplicationGateway>
    implements ApplicationGatewayRedirectConfiguration,
        ApplicationGatewayRedirectConfiguration.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewayRedirectConfiguration.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayRedirectConfiguration.Update {

    ApplicationGatewayRedirectConfigurationImpl(
        ApplicationGatewayRedirectConfigurationInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public ApplicationGatewayRedirectType type() {
        return this.innerModel().redirectType();
    }

    @Override
    public ApplicationGatewayListener targetListener() {
        SubResource ref = this.innerModel().targetListener();
        if (ref == null) {
            return null;
        }

        String name = ResourceUtils.nameFromResourceId(ref.id());
        return this.parent().listeners().get(name);
    }

    @Override
    public String targetUrl() {
        return this.innerModel().targetUrl();
    }

    @Override
    public Map<String, ApplicationGatewayRequestRoutingRule> requestRoutingRules() {
        Map<String, ApplicationGatewayRequestRoutingRule> rules = new TreeMap<>();
        if (null != this.innerModel().requestRoutingRules()) {
            for (SubResource ruleRef : this.innerModel().requestRoutingRules()) {
                String ruleName = ResourceUtils.nameFromResourceId(ruleRef.id());
                ApplicationGatewayRequestRoutingRule rule = this.parent().requestRoutingRules().get(ruleName);
                if (null != rule) {
                    rules.put(ruleName, rule);
                }
            }
        }

        return Collections.unmodifiableMap(rules);
    }

    @Override
    public boolean isPathIncluded() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().includePath());
    }

    @Override
    public boolean isQueryStringIncluded() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().includeQueryString());
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withRedirectConfiguration(this);
    }

    // Helpers

    // Withers

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withTargetUrl(String url) {
        this.innerModel().withTargetUrl(url).withTargetListener(null).withIncludePath(null);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withTargetListener(String name) {
        if (name == null) {
            this.innerModel().withTargetListener(null);
        } else {
            SubResource ref = new SubResource().withId(this.parent().futureResourceId() + "/httpListeners/" + name);
            this.innerModel().withTargetListener(ref).withTargetUrl(null);
        }

        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withoutTargetListener() {
        this.innerModel().withTargetListener(null);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withoutTargetUrl() {
        this.innerModel().withTargetUrl(null);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withType(ApplicationGatewayRedirectType redirectType) {
        this.innerModel().withRedirectType(redirectType);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withPathIncluded() {
        this.innerModel().withIncludePath(true);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withQueryStringIncluded() {
        this.innerModel().withIncludeQueryString(true);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withoutPathIncluded() {
        this.innerModel().withIncludePath(null);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withoutQueryStringIncluded() {
        this.innerModel().withIncludeQueryString(null);
        return this;
    }
}
