/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.management.network.ApplicationGateway;
import com.azure.management.network.ApplicationGatewayBackend;
import com.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.azure.management.network.ApplicationGatewayPathRule;
import com.azure.management.network.ApplicationGatewayRedirectConfiguration;
import com.azure.management.network.ApplicationGatewayUrlPathMap;
import com.azure.management.network.models.ApplicationGatewayPathRuleInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation for application gateway path rule.
 */
class ApplicationGatewayPathRuleImpl
        extends ChildResourceImpl<ApplicationGatewayPathRuleInner, ApplicationGatewayUrlPathMapImpl, ApplicationGatewayUrlPathMap>
        implements
        ApplicationGatewayPathRule,
        ApplicationGatewayPathRule.Definition<ApplicationGatewayUrlPathMap.DefinitionStages.WithAttach<ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate>>,
        ApplicationGatewayPathRule.UpdateDefinition<ApplicationGatewayUrlPathMap.UpdateDefinitionStages.WithAttach<ApplicationGateway.Update>>,
        ApplicationGatewayPathRule.Update {

    ApplicationGatewayPathRuleImpl(ApplicationGatewayPathRuleInner inner, ApplicationGatewayUrlPathMapImpl parent) {
        super(inner, parent);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public ApplicationGatewayUrlPathMapImpl attach() {
        return this.parent().withPathRule(this);
    }

    @Override
    public ApplicationGatewayPathRuleImpl toBackendHttpConfiguration(String name) {
        SubResource httpConfigRef = new SubResource()
                .setId(this.parent().parent().futureResourceId() + "/backendHttpSettingsCollection/" + name);
        this.inner().withBackendHttpSettings(httpConfigRef);
        return this;
    }

    @Override
    public ApplicationGatewayPathRuleImpl toBackend(String name) {
        this.inner().withBackendAddressPool(this.parent().parent().ensureBackendRef(name));
        return this;
    }

    @Override
    public ApplicationGatewayPathRuleImpl withRedirectConfiguration(String name) {
        if (name == null) {
            this.inner().withRedirectConfiguration(null);
        } else {
            SubResource ref = new SubResource().setId(this.parent().parent().futureResourceId() + "/redirectConfigurations/" + name);
            this.inner().withRedirectConfiguration(ref);
        }
        return this;
    }

    @Override
    public ApplicationGatewayBackend backend() {
        SubResource backendRef = this.inner().backendAddressPool();
        if (backendRef != null) {
            String backendName = ResourceUtils.nameFromResourceId(backendRef.getId());
            return this.parent().parent().backends().get(backendName);
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration() {
        SubResource configRef = this.inner().backendHttpSettings();
        if (configRef != null) {
            String configName = ResourceUtils.nameFromResourceId(configRef.getId());
            return this.parent().parent().backendHttpConfigurations().get(configName);
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayRedirectConfiguration redirectConfiguration() {
        SubResource ref = this.inner().redirectConfiguration();
        if (ref == null) {
            return null;
        } else {
            return this.parent().parent().redirectConfigurations().get(ResourceUtils.nameFromResourceId(ref.getId()));
        }
    }

    @Override
    public List<String> paths() {
        return Collections.unmodifiableList(inner().paths());
    }

    @Override
    public ApplicationGatewayPathRuleImpl withPath(String path) {
        if (inner().paths() == null) {
            inner().withPaths(new ArrayList<String>());
        }
        inner().paths().add(path);
        return this;
    }

    @Override
    public ApplicationGatewayPathRuleImpl withPaths(String... paths) {
        inner().withPaths(Arrays.asList(paths));
        return this;
    }
}
