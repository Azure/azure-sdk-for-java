// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayListener;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectType;
import com.azure.resourcemanager.network.models.ApplicationGatewayRequestRoutingRule;
import com.azure.resourcemanager.network.fluent.inner.ApplicationGatewayRedirectConfigurationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
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
        return this.inner().name();
    }

    @Override
    public ApplicationGatewayRedirectType type() {
        return this.inner().redirectType();
    }

    @Override
    public ApplicationGatewayListener targetListener() {
        SubResource ref = this.inner().targetListener();
        if (ref == null) {
            return null;
        }

        String name = ResourceUtils.nameFromResourceId(ref.id());
        return this.parent().listeners().get(name);
    }

    @Override
    public String targetUrl() {
        return this.inner().targetUrl();
    }

    @Override
    public Map<String, ApplicationGatewayRequestRoutingRule> requestRoutingRules() {
        Map<String, ApplicationGatewayRequestRoutingRule> rules = new TreeMap<>();
        if (null != this.inner().requestRoutingRules()) {
            for (SubResource ruleRef : this.inner().requestRoutingRules()) {
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
        return Utils.toPrimitiveBoolean(this.inner().includePath());
    }

    @Override
    public boolean isQueryStringIncluded() {
        return Utils.toPrimitiveBoolean(this.inner().includeQueryString());
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
        this.inner().withTargetUrl(url).withTargetListener(null).withIncludePath(null);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withTargetListener(String name) {
        if (name == null) {
            this.inner().withTargetListener(null);
        } else {
            SubResource ref = new SubResource().withId(this.parent().futureResourceId() + "/httpListeners/" + name);
            this.inner().withTargetListener(ref).withTargetUrl(null);
        }

        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withoutTargetListener() {
        this.inner().withTargetListener(null);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withoutTargetUrl() {
        this.inner().withTargetUrl(null);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withType(ApplicationGatewayRedirectType redirectType) {
        this.inner().withRedirectType(redirectType);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withPathIncluded() {
        this.inner().withIncludePath(true);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withQueryStringIncluded() {
        this.inner().withIncludeQueryString(true);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withoutPathIncluded() {
        this.inner().withIncludePath(null);
        return this;
    }

    @Override
    public ApplicationGatewayRedirectConfigurationImpl withoutQueryStringIncluded() {
        this.inner().withIncludeQueryString(null);
        return this;
    }
}
